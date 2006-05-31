/**
 * TP01Support.java
 * @author John Green
 * 19-Nov-2002
 * www.joanju.com
 * 
 * Copyright (c) 2002-2006 Joanju Limited.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.prorefactor.treeparser01;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.prorefactor.core.IConstants;
import org.prorefactor.core.JPNode;
import org.prorefactor.core.TokenTypes;
import org.prorefactor.core.schema.Field;
import org.prorefactor.core.schema.Schema;
import org.prorefactor.core.schema.Table;
import org.prorefactor.nodetypes.BlockNode;
import org.prorefactor.nodetypes.FieldRefNode;
import org.prorefactor.nodetypes.RecordNameNode;
import org.prorefactor.refactor.FileStuff;
import org.prorefactor.refactor.PUB;
import org.prorefactor.refactor.RefactorException;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.Block;
import org.prorefactor.treeparser.BufferScope;
import org.prorefactor.treeparser.CQ;
import org.prorefactor.treeparser.DataType;
import org.prorefactor.treeparser.FieldBuffer;
import org.prorefactor.treeparser.FieldLookupResult;
import org.prorefactor.treeparser.ParseUnit;
import org.prorefactor.treeparser.Primative;
import org.prorefactor.treeparser.Routine;
import org.prorefactor.treeparser.Symbol;
import org.prorefactor.treeparser.SymbolFactory;
import org.prorefactor.treeparser.SymbolScope;
import org.prorefactor.treeparser.SymbolScopeRoot;
import org.prorefactor.treeparser.SymbolScopeSuper;
import org.prorefactor.treeparser.TableBuffer;
import org.prorefactor.treeparser.Variable;
import org.prorefactor.widgettypes.Browse;

import antlr.collections.AST;

import com.joanju.ProparseLdr;




/**
 * Provides all functions called by TreeParser01.
 * TreeParser01 does not, itself, define any actions.
 * Instead, it only makes calls to the functions defined
 * in this class.
 */
public class TP01Support extends TP01Action {


	/* Note that blockStack is *only* valid for determining
	 * the current block - the stack itself cannot be used for determining
	 * a block's parent, buffer scopes, etc. That logic is found within
	 * the Block class.
	 * Conversely, we cannot use Block.parent to find the current block
	 * when we close out a block. That is because a scope's root block 
	 * parent is always the program block, but a programmer may code a 
	 * scope into a non-root block... which we need to make current again
	 * once done inside the scope.
	 */
	private ArrayList<Block> blockStack = new ArrayList<Block>();

	private Block currentBlock;
	private FrameStack frameStack = new FrameStack();
	private HashMap<String, SymbolScope> funcForwards = new HashMap<String, SymbolScope>();
	private Schema schema = Schema.getInstance();

	/** The symbol last, or currently being, defined.
	 * Needed when we have complex syntax like DEFINE id ... LIKE,
	 * where we want to track the LIKE but it's not in the same grammar
	 * production as the DEFINE.
	 */ 
	private Symbol currSymbol;

	SymbolScope currentScope;
	SymbolScopeRoot rootScope;
	TableBuffer lastTableReferenced;
	TableBuffer prevTableReferenced;
	TableBuffer currDefTable;

	{	// initialization
		rootScope = new SymbolScopeRoot();
		currentScope = rootScope;
		// See programRoot() for initiazation of the root Block.
	}



	///// Methods /////

	
	/** Called at the *end* of the statement that defines the symbol. */
	public void addToSymbolScope(Object o) {
		currentScope.add((Symbol)o);
	}

	
	
	/** Get the Table symbol linked from a RECORD_NAME AST. */
	private Table astTableLink(AST tableAST) {
		TableBuffer buffer = (TableBuffer) ((JPNode)tableAST).getLink(JPNode.SYMBOL);
		assert buffer != null;
		return buffer.getTable();
	}



	/** Beginning of a block. */
	public void blockBegin(AST blockAST) {
		BlockNode blockNode = (BlockNode) blockAST;
		currentBlock = pushBlock(new Block(currentBlock, blockNode));
		blockNode.setBlock(currentBlock);
	}



	/** End of a block. */
	public void blockEnd() {
		currentBlock = popBlock();
	}

	
	/** The ID node in a BROWSE ID pair. */
	protected void browseRef(AST idAST) {
		frameStack.browseRefNode((JPNode)idAST, currentScope);
	}

	
	/** A CAN-FIND needs to have its own buffer and buffer scope,
	 * because CAN-FIND(x where x.y = z) does *not* cause a buffer
	 * reference to be created for x within the surrounding block.
	 * (Ensuring that the x.y reference does not create a buffer
	 * reference was the tricky part.)
	 * Also note the behaviour of the 4GL: You can use an existing
	 * named buffer within a CAN-FIND, but of course the CAN-FIND
	 * does not move any pointers around. We accomplish this by
	 * making a local-scoped named buffer using that same name.
	 */
	protected void canFindBegin(AST canfindAST, AST recordAST) {
		RecordNameNode recordNode = (RecordNameNode) recordAST;
		// Keep a ref to the current block...
		Block b = currentBlock;
		// ...create a can-find scope and block (assigns currentBlock)...
		scopeAdd(canfindAST);
		// ...and then set this "can-find block" to use it as its parent.
		currentBlock.setParent(b);
		String buffName = recordAST.getText();
		Table table;
		boolean isDefault = false;
		TableBuffer tableBuffer = currentScope.lookupBuffer(buffName);
		if (tableBuffer != null) {
			table = tableBuffer.getTable();
			isDefault = tableBuffer.isDefault();
		} else {
			table = schema.lookupTable(buffName);
			isDefault = true;
		}
		TableBuffer newBuff = currentScope.defineBuffer(isDefault ? "" : buffName, table);
		recordNode.setTableBuffer(newBuff);
		currentBlock.addHiddenCursor(recordNode);
	}



	protected void canFindEnd(AST canfindAST) {
		scopeClose(canfindAST);
	}
	
	
	protected void classState(AST classAST) {
		JPNode classNode = (JPNode) classAST;
		JPNode idNode = classNode.firstChild();
		rootScope.setClassName(idNode.getText());
		if (idNode.nextSibling().getType()==TokenTypes.INHERITS)
			classStateInherits(classNode, idNode.nextSibling().firstChild());
	}
	private void classStateInherits(JPNode classNode, JPNode inheritsTypeNode) {
		String className = inheritsTypeNode.getText();
		SymbolScopeSuper cachedCopy = SymbolScopeSuper.cache.get(className.toLowerCase());
		if (cachedCopy==null) cachedCopy = classStateSuper(classNode, className);
		// Whether we got it from the cache or created it new, we put it back in the cache.
		// Putting a cached copy back into the cache just moves it up in the MRU list.
		SymbolScopeSuper.cache.put(className.toLowerCase(), cachedCopy);
		// We take a copy of the cached superScope, because the tree parser messes with
		// the attributes of the symbols, and we don't want to mess with the symbols that
		// are in the super scopes in the cache.
		rootScope.assignSuper(cachedCopy.generateSymbolScopeSuper());
	}
	private SymbolScopeSuper classStateSuper(JPNode classNode, String className) {
		// Notes: It's possible we might want to change the logic here some day.
		// If the PUB file ever stores enough symbol information that we could get
		// the inheritance information straight from the PUB, then we might want to
		// go straight to that (if up to date), ignoring what's in Proparse.
		// I don't know if there would be any performance or memory advantage one
		// way or the other, since reading from disk is slower, but building the
		// whole JPNode tree takes more memory.
		File file = FileStuff.findFileForClassName(className);
		if (! file.exists()) throw new Error("Could not find source on PROPATH for class: " + className);
		RefactorSession refpack = RefactorSession.getInstance();
		String [] projFile = refpack.getIDE().getProjectRelativePath(file);
		if (projFile==null) throw new Error("Could not find IDE project/file for class: " + className);
		PUB pub = new PUB(projFile[0], projFile[1], FileStuff.fullpath(file));
		boolean pubIsCurrent = pub.loadTo(PUB.HEADER);
		ParseUnit pu = new ParseUnit(file);
		pu.setPUB(pub);
		int superClassHandle = classNode.attrGet(IConstants.SUPER_CLASS_HANDLE);
		try {
			if (superClassHandle > 0) {
				classStateSuperProparse(pu, superClassHandle);
			} else {
				if (!pubIsCurrent) throw new Error("Internal error: No tree from PUB or Proparse, for class: " + className);
				pub.load();
				pu.setTopNode(pub.getTree());
				pu.treeParser01();
			}
		} catch (Exception e) { throw new Error(e); }
		return pu.getRootScope().generateSymbolScopeSuper();
	}
	private void classStateSuperProparse(ParseUnit pu, int superClassHandle) throws RefactorException {
		assert superClassHandle > 0;
		ProparseLdr parser = ProparseLdr.getInstance();
		int superRootHandle = parser.getHandle();
		parser.nodeParent(superClassHandle, superRootHandle);
		assert parser.getNodeTypeI(superRootHandle) == TokenTypes.Program_root;
		pu.setTopNode(JPNode.getTree(superRootHandle));
		pu.treeParser01();
	}


	
	protected void clearState(AST headAST) {
		JPNode headNode = (JPNode)headAST;
		JPNode firstChild = headNode.firstChild();
		if (firstChild.getType()==TokenTypes.FRAME)
			frameStack.simpleFrameInitStatement(headNode, firstChild.nextNode(), currentBlock);
	}
	
	
	/** The tree parser calls this at an AS node */
	protected void defAs(AST asAST) {
		JPNode asNode = (JPNode)asAST;
		currSymbol.setAsNode(asNode);
		Primative primative = (Primative) currSymbol;
		JPNode typeNode = asNode.nextNode();
		if (typeNode.getType()==TokenTypes.CLASS) typeNode = typeNode.nextNode();
		if (typeNode.getType()==TokenTypes.TYPE_NAME) {
			primative.setDataType(DataType.getDataType(TokenTypes.CLASS));
			primative.setClassName(typeNode.getText());
		} else {
			primative.setDataType(DataType.getDataType(typeNode.getType()));
		}
		assert
			primative.getDataType() != null
			: "Failed to set datatype at " + asNode.getFilename() + " line " + asNode.getLine()
			;
	}

	
	
	/** The tree parser calls this at a LIKE node */
	protected void defLike(AST likeAST) {
		JPNode likeNode = (JPNode)likeAST;
		currSymbol.setLikeNode(likeNode);
		FieldRefNode likeRefNode = (FieldRefNode) likeNode.nextNode();
		Primative primative = (Primative) currSymbol;
		primative.setDataType(likeRefNode.getDataType());
		primative.setClassName(likeRefNode.getClassName());
		assert
			primative.getDataType() != null
			: "Failed to set datatype at " + likeNode.getFilename() + " line " + likeNode.getLine()
			;
	}
	
	
	
	/** Called at the start of a DEFINE BROWSE statement. */
	protected Browse defineBrowse(AST defAST, AST idAST) {
		Browse browse = (Browse) defineSymbol(TokenTypes.BROWSE, defAST, idAST);
		frameStack.nodeOfDefineBrowse(browse);
		return browse;
	}
	

	/** Define a buffer. If the buffer is initialized at the same time it is
	 * defined (as in a buffer parameter), then parameter init should be true.
	 */
	protected void defineBuffer(AST defAST, AST idAST, AST tableAST, boolean init) {
		JPNode idNode = (JPNode) idAST;
		Table table = astTableLink(tableAST);
		TableBuffer bufSymbol = currentScope.defineBuffer(idNode.getText(), table);
		currSymbol = bufSymbol;
		bufSymbol.setDefOrIdNode((JPNode)defAST);
		idNode.setLink(JPNode.SYMBOL, bufSymbol);
		if (init) {
			BufferScope bufScope = currentBlock.getBufferForReference(bufSymbol);
			idNode.setLink(JPNode.BUFFERSCOPE, bufScope);
		}
	}



	/** Define an unnamed buffer which is scoped (symbol and buffer) to the trigger scope/block.
	 * @param anode The RECORD_NAME node. Must already have the Table symbol linked to it.
	 */
	protected void defineBufferForTrigger(AST tableAST) {
		Table table = astTableLink(tableAST);
		TableBuffer bufSymbol = currentScope.defineBuffer("", table);
		currentBlock.getBufferForReference(bufSymbol); // Create the BufferScope
		currSymbol = bufSymbol;
	}



	protected Symbol defineSymbol(int symbolType, AST defAST, AST idAST) {
		/* Some notes:
		 * We need to create the Symbol right away, because further
		 * actions in the grammar might need to set attributes on it.
		 * We can't add it to the scope yet, because of statements like this:
		 *   def var xyz like xyz.
		 * The tree parser is responsible for calling addToScope at the end of
		 * the statement or when it is otherwise safe to do so.
		 */
		JPNode defNode = (JPNode) defAST;
		JPNode idNode = (JPNode) idAST;
		Symbol symbol = SymbolFactory.create(symbolType, idNode.getText(), currentScope);
		symbol.setDefOrIdNode(defNode);
		currSymbol = symbol;
		idNode.setLink(JPNode.SYMBOL, symbol);
		return symbol;
	}

	
	
	protected void defineTableField(AST idAST) {
		JPNode idNode = (JPNode)idAST;
		FieldBuffer fieldBuff = rootScope.defineTableField(idNode.getText(), currDefTable);
		currSymbol = fieldBuff;
		fieldBuff.setDefOrIdNode(idNode);
		idNode.setLink(JPNode.SYMBOL, fieldBuff);
	}



	protected void defineTableLike(AST tableAST) {
		// Get table for "LIKE table"
		Table table = astTableLink(tableAST);
		// For each field in "table", create a field def in currDefTable
		for ( Iterator it = table.getFieldSet().iterator() ; it.hasNext() ; ) {
			Field field = (Field)it.next();
			rootScope.defineTableField(field.getName(), currDefTable )
				.setDataType(field.getDataType())
				;
		}
	}
	
	
	
	protected void defineTable(JPNode defNode, JPNode idNode, int storeType) {
		TableBuffer buffer = rootScope.defineTable(idNode.getText(), storeType);
		buffer.setDefOrIdNode(defNode);
		currSymbol = buffer;
		currDefTable = buffer;
		idNode.setLink(JPNode.SYMBOL, buffer);
	}



	protected void defineTemptable(AST defAST, AST idAST) {
		defineTable((JPNode)defAST, (JPNode)idAST, IConstants.ST_TTABLE);
	}



	protected Variable defineVariable(AST defAST, AST idAST) {
		/* Some notes:
		 * We need to create the Variable Symbol right away, because further
		 * actions in the grammar might need to set attributes on it.
		 * We can't add it to the scope yet, because of statements like this:
		 *   def var xyz like xyz.
		 * The tree parser is responsible for calling addToScope at the end of
		 * the statement or when it is otherwise safe to do so.
		 */
		JPNode defNode = (JPNode) defAST;
		JPNode idNode = (JPNode) idAST;
		String name = idNode.getText();
		if (name==null || name.length()==0) {
			/* Variable Name: There was a subtle bug here when parsing trees extracted
			 * from PUB files. In PUB files, the text of keyword nodes are not stored.
			 * But in the case of an ACCUMULATE statement -> aggregatephrase -> aggregate_opt,
			 * we are defining variable/symbols using the COUNT|MAXIMUM|TOTAL|whatever node.
			 * I added a check for empty text from the "id" node.
			 */
			name = TokenTypes.getTokenName(idNode.getType());
		}
		Variable variable = new Variable(name, currentScope);
		variable.setDefOrIdNode(defNode);
		currSymbol = variable;
		idNode.setLink(JPNode.SYMBOL, variable);
		return variable;
	}
	protected Variable defineVariable(AST defAST, AST idAST, int dataType) {
		assert dataType != TokenTypes.CLASS;
		Variable v = defineVariable(defAST, idAST);
		v.setDataType(DataType.getDataType(dataType));
		return v;
	}
	protected Variable defineVariable(AST defAST, AST idAST, AST likeAST) {
		Variable v = defineVariable(defAST, idAST);
		FieldRefNode likeRefNode = (FieldRefNode) likeAST;
		v.setDataType(likeRefNode.getDataType());
		v.setClassName(likeRefNode.getClassName());
		return v;
	}



	protected void defineWorktable(AST defAST, AST idAST) {
		defineTable((JPNode)defAST, (JPNode)idAST, IConstants.ST_WTABLE);
	}



	/** Process a Field_ref node.
	 * @param refAST The Field_ref node.
	 * @param idAST The ID node.
	 * @param contextQualifier What sort of reference is this? Read? Update? Etc.
	 * @param 
	 * @param whichTable For name resolution - which table must this be a field of?
	 * Input 0 for any table, 1 for the lastTableReferenced, 2 for the prevTableReferenced.
	 */
	protected void field(AST refAST, AST idAST, int contextQualifier, int whichTable) {
		JPNode idNode = (JPNode) idAST;
		FieldRefNode refNode = (FieldRefNode) refAST;
		String name = idNode.getText();
		FieldLookupResult result = null;
		
		refNode.attrSet(IConstants.CONTEXT_QUALIFIER, contextQualifier);

		// Check if this is a Field_ref being "inline defined"
		// If so, we define it right now.
		if (refNode.attrGet(IConstants.INLINE_VAR_DEF) == 1)
			addToSymbolScope(defineVariable(idAST, idAST));

		if (	refNode.firstChild().getType()==TokenTypes.INPUT
			&&	// I've seen at least one instance of "INPUT objHandle:attribute" in code,
				// which for some reason compiled clean. As far as I'm aware, the INPUT was
				// meaningless, and the compiler probably should have complained about it.
				// At any rate, the handle:attribute isn't an input field, and we don't want
				// to try to look up the handle using frame field rules.
				(	refNode.nextSibling()==null
				||	refNode.nextSibling().getType() != TokenTypes.OBJCOLON
				)
			) {
			// Searching the frames for an existing INPUT field is very different than
			// the usual field/variable lookup rules. It is done based on what is in
			// the referenced FRAME or BROWSE, or what is found in the frames most
			// recently referenced list.
			result = frameStack.inputFieldLookup(refNode, currentScope);
		} else if (whichTable == 0) {
			// Lookup the field, with special handling for FIELDS/USING/EXCEPT phrases	
			boolean getBufferScope = (contextQualifier != CQ.SYMBOL);
			result = currentBlock.lookupField(name, getBufferScope);
		} else {
			// If we are in a FIELDS phrase, then we know which table the field is from.
			// The field lookup in Table expects an unqualified name.
			String [] parts = name.split("\\.");
			String fieldPart = parts[parts.length - 1];
			TableBuffer ourBuffer = whichTable==2 ? prevTableReferenced : lastTableReferenced;
			Field field = ourBuffer.getTable().lookupField(fieldPart);
			if (field==null) throw new Error(
					idNode.getFilename()
					+ ":"
					+ idNode.getLine()
					+ " Unknown field or variable name: " + fieldPart
					);
			FieldBuffer fieldBuffer = ourBuffer.getFieldBuffer(field);
			result = new FieldLookupResult();
			result.field = fieldBuffer;
		}

		if (result == null) 
			throw new Error(
				idNode.getFilename()
				+ ":"
				+ idNode.getLine()
				+ " Unknown field or variable name: " + name
				);

		if (result.isUnqualified)
			refNode.attrSet(IConstants.UNQUALIFIED_FIELD, IConstants.TRUE);
		if (result.isAbbreviated)
			refNode.attrSet(IConstants.ABBREVIATED, IConstants.TRUE);
		// Variable
		if (result.variable != null) {
			refNode.setSymbol(result.variable);
			refNode.attrSet(IConstants.STORETYPE, IConstants.ST_VAR);
			result.variable.noteReference(contextQualifier);
		}
		// FieldLevelWidget
		if (result.fieldLevelWidget != null) {
			refNode.setSymbol(result.fieldLevelWidget);
			refNode.attrSet(IConstants.STORETYPE, IConstants.ST_VAR);
			result.fieldLevelWidget.noteReference(contextQualifier);
		}
		// Buffer attributes
		if (result.bufferScope != null) {
			refNode.setBufferScope(result.bufferScope);
		}
		// Table field
		if (result.field != null) {
			refNode.setSymbol(result.field);
			refNode.attrSet(IConstants.STORETYPE, result.field.getField().getTable().getStoretype());
			result.field.noteReference(contextQualifier);
		}

	} // field()


	/** Called from Form_item node */
	protected void formItem(AST ast) {
		frameStack.formItem((JPNode)ast);
	}

	/** Called from DO|REPEAT|FOR blocks. */
	protected void frameBlockCheck(AST ast) {
		frameStack.nodeOfBlock((JPNode)ast, currentBlock);
	}

	/** Called at tree parser DEFINE FRAME statement. */
	protected void frameDef(AST defAST, AST idAST) {
		frameStack.nodeOfDefineFrame((JPNode)defAST, (JPNode)idAST, currentScope);
	}
	
	/** This is called at the beginning of a frame affecting statement, with the statement head node. */
	protected void frameInitializingStatement(AST ast) {
		frameStack.nodeOfInitializingStatement((JPNode)ast, currentBlock);
	}
	
	/** This is called at the end of a frame affecting statement. */
	protected void frameStatementEnd() {
		frameStack.statementEnd(); 
	}

	protected void frameRef(AST idAST) {
		frameStack.frameRefNode((JPNode)idAST, currentScope);
	}

	
	
	/** If this function definition did not list any parameters, but it had a
	 * function forward declaration, then we use the block and scope from that
	 * declaration, in case it is where the parameters were listed.
	 */
	protected void funcDef(AST funcAST, AST idAST) {
		SymbolScope forwardScope = funcForwards.get(idAST.getText());
		if (forwardScope==null) funcSymbolCreate(idAST);
		// If there are symbols (i.e. parameters, buffer params) already defined in
		// this function scope, then we don't do anything.
		if (	currentScope.getVariables().size() > 0
			||	currentScope.getBufferSet().size() > 0
			) return;
		if (forwardScope==null) return;
		scopeSwap(forwardScope);
		((BlockNode)funcAST).setBlock(currentBlock);
	}

	protected void funcForward(AST idAST) {
		funcForwards.put(idAST.getText(), currentScope);
		funcSymbolCreate(idAST);
	}
	
	private Routine funcSymbolCreate(AST idAST) {
		JPNode idNode = (JPNode) idAST;
		SymbolScope definingScope = currentScope.getParentScope();
		Routine r = new Routine(idAST.getText(), definingScope, currentScope);
		r.setProgressType(TokenTypes.FUNCTION);
		r.setDefOrIdNode(idNode.parent());
		definingScope.add(r);
		return r;
	}


	
	public SymbolScope getCurrentScope(){ return currentScope; }

	public SymbolScopeRoot getRootScope(){ return rootScope; }
	
	
	
	// Shortcut to JPNode.getHandle()
	protected int h(AST node) {
		return ((JPNode)node).getHandle();
	}


	protected void methodDef(AST idAST) {
		JPNode idNode = (JPNode) idAST;
		SymbolScope definingScope = currentScope.getParentScope();
		Routine r = new Routine(idAST.getText(), definingScope, currentScope);
		r.setProgressType(TokenTypes.METHOD);
		r.setDefOrIdNode(idNode.parent());
		definingScope.add(r);
	}


	protected Block popBlock() {
		blockStack.remove(blockStack.size()-1);
		return blockStack.get(blockStack.size()-1);
	}


	protected void procedureBegin(AST procAST, AST idAST){
		SymbolScope definingScope = currentScope;
		scopeAdd(procAST);
		Routine r = new Routine(idAST.getText(), definingScope, currentScope);
		r.setProgressType(TokenTypes.PROCEDURE);
		r.setDefOrIdNode((JPNode)procAST);
		definingScope.add(r);
	}
	
	
	protected void procedureEnd(AST node){
		scopeClose(node);
	}

	protected void programRoot(AST rootAST) {
		BlockNode blockNode = (BlockNode) rootAST;
		currentBlock = pushBlock(new Block(rootScope, blockNode));
		rootScope.setRootBlock(currentBlock);
		blockNode.setBlock(currentBlock);
		parseUnit.setTopNode(blockNode);
		parseUnit.setRootScope(rootScope);
	}
	
	
	
	protected void programTail() {
		// Because the tree parser depends on PUB files for getting inheritance information
		// from super classes, the tree parser is responsible for keeping the PUB files up
		// to date.
		try {
			PUB pub = parseUnit.getPUB();
			if (! pub.isChecked()) pub.loadTo(PUB.HEADER);
			if (! pub.isCurrent()) pub.build(this);
		} catch (Exception e) { throw new Error(e); }
	}



	protected Block pushBlock(Block block) {
		blockStack.add(block);
		return block;
	}



	/** For a RECORD_NAME node, do checks and assignments for the TableBuffer. */
	private void recordNodeSymbol(JPNode node, TableBuffer buffer) {
		String nodeText = node.getText();
		if (buffer == null)
			throw new Error(
				node.getFilename()
				+ ":"
				+ node.getLine()
				+ " Could not resolve table: " + nodeText
				);
		Table table = buffer.getTable();
		// If we get a mismatch between storetype here and the storetype determined
		// by proparse.dll then there's a bug somewhere. This is just a double-check.
		if (table.getStoretype() != node.attrGet(IConstants.STORETYPE) )
			throw new Error(
				node.getFilename()
				+ ":"
				+ node.getLine()
				+ " Storetype mismatch between proparse.dll and treeparser01: "
				+ nodeText
				+ " " + node.attrGet(IConstants.STORETYPE)
				+ " " + table.getStoretype()
				);
		prevTableReferenced = lastTableReferenced;
		lastTableReferenced = buffer;
		// For an unnamed buffer, determine if it's abbreviated.
		// Note that named buffers, temp and work table names cannot be abbreviated.
		if (buffer.isDefault() && table.getStoretype()==IConstants.ST_DBTABLE) {
			String [] nameParts = nodeText.split("\\.");
			int tableNameLen = nameParts[nameParts.length-1].length();
			if (table.getName().length() > tableNameLen)
				node.attrSet(IConstants.ABBREVIATED, 1);
		}
	} // recordNameBufferSymbol



	/** Action to take at various RECORD_NAME nodes. */
	protected void recordNameNode(AST anode, int contextQualifier) {
		RecordNameNode recordNode = (RecordNameNode) anode;
		recordNode.attrSet(IConstants.CONTEXT_QUALIFIER, contextQualifier);
		TableBuffer buffer = null;
		switch (contextQualifier) {
			case CQ.INIT :
			case CQ.INITWEAK :
			case CQ.REF :
			case CQ.REFUP :
			case CQ.UPDATING :
			case CQ.BUFFERSYMBOL :
				buffer = currentScope.getBufferSymbol(recordNode.getText());
				break;
			case CQ.SYMBOL :
				buffer = currentScope.lookupTableOrBufferSymbol(anode.getText());
				break;
			case CQ.TEMPTABLESYMBOL :
				buffer = currentScope.lookupTempTable(anode.getText());
				break;
			case CQ.SCHEMATABLESYMBOL :
				Table table = schema.lookupTable(anode.getText());
				if (table!=null) buffer = currentScope.getUnnamedBuffer(table);
				break;
			default :
				assert false;
		}
		recordNodeSymbol(recordNode, buffer); // Does checks, sets attributes.
		recordNode.setTableBuffer(buffer);
		switch (contextQualifier) {
			case CQ.INIT :
			case CQ.REF :
			case CQ.REFUP :
			case CQ.UPDATING :
				recordNode.setBufferScope(currentBlock.getBufferForReference(buffer));
				break;
			case CQ.INITWEAK :
				recordNode.setBufferScope(currentBlock.addWeakBufferScope(buffer));
				break;
		}
		buffer.noteReference(contextQualifier);
	} // recordNameNode
	


	protected void scopeAdd(AST anode) {
		BlockNode blockNode = (BlockNode) anode;
		currentScope = currentScope.addScope();
		currentBlock = pushBlock(new Block(currentScope, blockNode));
		currentScope.setRootBlock(currentBlock);
		blockNode.setBlock(currentBlock);
	} // scopeAdd()



	protected void scopeClose(AST scopeRootNode) {
		currentScope = currentScope.getParentScope();
		blockEnd();
	} // scopeClose()



	/** In the case of a function definition that comes some time after a function
	 * forward declaration, we want to use the scope that was created with the forward
	 * declaration, because it is the scope that has all of the parameter definitions.
	 * We have to do this because the definition itself may have left out the parameter
	 * list - it's not required - it just uses the parameter list from the declaration.
	 */
	private void scopeSwap(SymbolScope scope) {
		currentScope = scope;
		blockEnd(); // pop the unused block from the stack
		currentBlock = pushBlock(scope.getRootBlock());
	}



	/** Create a "strong" buffer scope.
	 * This is called within a DO FOR or REPEAT FOR statement.
	 * @param anode Is the RECORD_NAME node. It must already have
	 * the BufferSymbol linked to it.
	 */
	protected void strongScope(AST anode) {
		currentBlock.addStrongBufferScope((RecordNameNode)anode);
	}
	

	/** Called at the end of a VIEW statement. */
	protected void viewState(AST headAST) {
		// The VIEW statement grammar uses gwidget, so we have to do some
		// special searching for FRAME to initialize.
		JPNode headNode = (JPNode)headAST;
		for (JPNode frameNode : headNode.query(TokenTypes.FRAME)) {
			int parentType = frameNode.parent().getType();
			if (parentType==TokenTypes.Widget_ref || parentType==TokenTypes.IN_KW) {
				frameStack.simpleFrameInitStatement(headNode, frameNode.nextNode(), currentBlock);
				return;
			}
		}
	}


}
