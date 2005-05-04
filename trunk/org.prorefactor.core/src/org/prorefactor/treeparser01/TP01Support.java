/**
 * TP01Support.java
 * @author John Green
 * 19-Nov-2002
 * www.joanju.com
 * 
 * Copyright (c) 2002-2004 Joanju Limited.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.prorefactor.treeparser01;


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
import org.prorefactor.treeparser.TableBuffer;
import org.prorefactor.treeparser.Variable;

import antlr.collections.AST;




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
	private ArrayList blockStack = new ArrayList();

	private Block currentBlock;
	private HashMap funcForwards = new HashMap();
	private ParseUnit parseUnit = new ParseUnit();
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

	
	public void addToScope(Object o) {
		currentScope.add((Variable)o);
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
	
	
	
	/** The tree parser calls this at an AS node */
	public void defAs(AST asAST) {
		JPNode asNode = (JPNode)asAST;
		currSymbol.setAsNode(asNode);
		((Primative)currSymbol).setDataType(DataType.getDataType(asNode.nextNode().getType()));
		assert
			((Primative)currSymbol).getDataType() != null
			: "Failed to set datatype at " + asNode.getFilename() + " line " + asNode.getLine()
			;
	}

	
	
	/** The tree parser calls this at a LIKE node */
	public void defLike(AST likeAST) {
		JPNode likeNode = (JPNode)likeAST;
		currSymbol.setLikeNode(likeNode);
		((Primative)currSymbol).setDataType(fieldRefDataType(likeNode.nextNode()));
		assert
			((Primative)currSymbol).getDataType() != null
			: "Failed to set datatype at " + likeNode.getFilename() + " line " + likeNode.getLine()
			;
	}
	
	
	
	/** Define a buffer. If the buffer is initialized at the same time it is
	 * defined (as in a buffer parameter), then parameter init should be true.
	 */
	public void defineBuffer(AST defAST, AST idAST, AST tableAST, boolean init) {
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
	} // defineBuffer()



	/** Define an unnamed buffer which is scoped (symbol and buffer) to the trigger scope/block.
	 * @param anode The RECORD_NAME node. Must already have the Table symbol linked to it.
	 */
	public void defineBufferForTrigger(AST tableAST) {
		Table table = astTableLink(tableAST);
		TableBuffer bufSymbol = currentScope.defineBuffer("", table);
		currentBlock.getBufferForReference(bufSymbol); // Create the BufferScope
		currSymbol = bufSymbol;
	} // defineTriggerBuffer(AST anode)



	public Symbol defineSymbol(AST defAST, AST idAST, int symbolType) {
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

	
	
	public void defineTableField(AST idAST) {
		JPNode idNode = (JPNode)idAST;
		FieldBuffer fieldBuff = rootScope.defineTableField(idNode.getText(), currDefTable);
		currSymbol = fieldBuff;
		fieldBuff.setDefOrIdNode(idNode);
		idNode.setLink(JPNode.SYMBOL, fieldBuff);
	}



	public void defineTableLike(AST tableAST) {
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
	
	
	
	private void defineTable(JPNode defNode, JPNode idNode, int storeType) {
		TableBuffer buffer = rootScope.defineTable(idNode.getText(), storeType);
		buffer.setDefOrIdNode(defNode);
		currSymbol = buffer;
		currDefTable = buffer;
		idNode.setLink(JPNode.SYMBOL, buffer);
	}



	public void defineTemptable(AST defAST, AST idAST) {
		defineTable((JPNode)defAST, (JPNode)idAST, IConstants.ST_TTABLE);
	}



	public Variable defineVariable(AST defAST, AST idAST) {
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
		Variable variable = new Variable(idNode.getText(), currentScope);
		variable.setDefOrIdNode(defNode);
		currSymbol = variable;
		idNode.setLink(JPNode.SYMBOL, variable);
		return variable;
	}
	public Variable defineVariable(AST defAST, AST idAST, int dataType) {
		Variable v = defineVariable(defAST, idAST);
		((Variable)currSymbol).setDataType(DataType.getDataType(dataType));
		return v;
	}
	public Variable defineVariable(AST defAST, AST idAST, AST likeAST) {
		Variable v = defineVariable(defAST, idAST);
		((Variable)currSymbol).setDataType(fieldRefDataType(likeAST));
		return v;
	}



	public void defineWorktable(AST defAST, AST idAST) {
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
	public void field(AST refAST, AST idAST, int contextQualifier, int whichTable) {
		JPNode idNode = (JPNode) idAST;
		FieldRefNode refNode = (FieldRefNode) refAST;
		String name = idNode.getText();
		FieldLookupResult result = null;

		refNode.attrSet(IConstants.CONTEXT_QUALIFIER, contextQualifier);

		// Check if this is a Field_ref being "inline defined"
		// If so, we define it right now.
		if (refNode.attrGet(IConstants.INLINE_VAR_DEF) == 1)
			addToScope(defineVariable(idAST, idAST));

		// Lookup the field, with special handling for FIELDS/USING/EXCEPT phrases	
		if (whichTable == 0) {
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
		// Buffer attributes
		if (result.bufferScope != null) {
			refNode.setBufferScope(result.bufferScope);
		}
		// Table field
		if (result.field != null) {
			refNode.setSymbol(result.field);
			result.field.noteReference(contextQualifier);
		}

	} // field()


	private static DataType fieldRefDataType(AST refAST) {
		return ((FieldRefNode)refAST).getDataType();
	}


	/** If this function definition did not list any parameters, but it had a
	 * function forward declaration, then we use the block and scope from that
	 * declaration, in case it is where the parameters were listed.
	 */
	protected void funcDef(AST funcAST, AST idAST) {
		SymbolScope forwardScope = (SymbolScope) funcForwards.get(idAST.getText());
		if (forwardScope==null) funcSymbolCreate(idAST);
		// If there are symbols (i.e. parameters, buffer params) already defined in
		// this function scope, then we don't do anything.
		if (	currentScope.getVariableSet().size() > 0
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
		SymbolScope definingScope = currentScope.getParentScope();
		Routine r = new Routine(idAST.getText(), definingScope, currentScope);
		r.setProgressType(TokenTypes.FUNCTION);
		definingScope.add(r);
		return r;
	}



	public ParseUnit getParseUnit() { return parseUnit; }


	
	// Shortcut to JPNode.getHandle()
	protected int h(AST node) {
		return ((JPNode)node).getHandle();
	}



	protected Block popBlock() {
		blockStack.remove(blockStack.size()-1);
		return (Block) blockStack.get(blockStack.size()-1);
	}


	public void procedureBegin(AST procNode, AST idNode){
		SymbolScope definingScope = currentScope;
		scopeAdd(procNode);
		Routine r = new Routine(idNode.getText(), definingScope, currentScope);
		r.setProgressType(TokenTypes.PROCEDURE);
		definingScope.add(r);
	}
	
	
	public void procedureEnd(AST node){
		scopeClose(node);
	}

	public void programRoot(AST rootAST) {
		BlockNode blockNode = (BlockNode) rootAST;
		currentBlock = pushBlock(new Block(rootScope, blockNode));
		rootScope.setRootBlock(currentBlock);
		blockNode.setBlock(currentBlock);
		parseUnit.setTopNode(blockNode);
		parseUnit.setRootScope(rootScope);
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
	public void recordNameNode(AST anode, int contextQualifier) {
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
	


	public void scopeAdd(AST anode) {
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



	/** It would be unusual to already have a ParseUnit before calling
	 * TP01, since TP01 is usually the first tree parser and it (by default)
	 * creates its own ParseUnit. However, after instantiating TP01, you can
	 * assign your own ParseUnit before executing the tree parse.
	 */
	public void setParseUnit(ParseUnit parseUnit) { this.parseUnit = parseUnit; }

	
	
	/** Create a "strong" buffer scope.
	 * This is called within a DO FOR or REPEAT FOR statement.
	 * @param anode Is the RECORD_NAME node. It must already have
	 * the BufferSymbol linked to it.
	 */
	public void strongScope(AST anode) {
		currentBlock.addStrongBufferScope((RecordNameNode)anode);
	}



	public SymbolScopeRoot getRootScope(){
		return rootScope;
	}
	
	public SymbolScope getCurrentScope(){
		return currentScope;
	}
	



} // class TP01Support
