/**
 * TP01Action.java
 * @author John Green
 * Aug 4, 2004
 * www.joanju.com
 *
 * Copyright (C) 2004 Joanju Limited.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.prorefactor.treeparser01;


import org.prorefactor.treeparser.ParseUnit;
import org.prorefactor.treeparser.Symbol;
import org.prorefactor.treeparser.Variable;
import org.prorefactor.widgettypes.Browse;

import antlr.collections.AST;


/** Superclass of empty actions methods for TreeParser01.
 * Sublcasses can override and implement any of these methods,
 * which are all called directly by TreeParser01.
 * TP01Support is the default implementation.
 */
public class TP01Action {

	protected ParseUnit parseUnit = new ParseUnit(null);

	

	/** Called by the tree parser at the end of a DEFINE statement, passing
	 * in the new Variable object which is expected now to be added to the current scope.
	 */
	protected void addToSymbolScope(Object o) {}


	/** Beginning of a block. */
	protected void blockBegin(AST blockAST) {}


	/** End of a block. */
	protected void blockEnd() {}


	/** The ID node in a BROWSE ID pair. */
	protected void browseRef(AST idAST) {}

	
	/** The tree parser calls this at the start of a can-find,
	 * because it needs to have its own buffer and buffer scope.
	 */
	protected void canFindBegin(AST canfindAST, AST recordAST) {}


	/** Called by the tree parser at the end of a can-find. */
	protected void canFindEnd(AST canfindAST) {}
	
	
	/** Called by the tree parser at the CLASS node. */
	protected void classState(AST classAST) {}
	
	
	/** Called at the end of a CLEAR statement. */
	protected void clearState(AST headAST) {}
	
	
	/** The tree parser calls this at an AS node */
	protected void defAs(AST asAST) {}

	
	/** The tree parser calls this at a LIKE node */
	protected void defLike(AST likeAST) {}
	
	
	/** Define a buffer. If the buffer is initialized at the same time it is
	 * defined (as in a buffer parameter), then parameter init should be true.
	 */
	protected void defineBuffer(AST defAST, AST idAST, AST recAST, boolean init) {}

	
	/** Called at the start of a DEFINE BROWSE statement. */
	protected Browse defineBrowse(AST defAST, AST idAST) {return null;}
	

	/** Define an unnamed buffer which is scoped (symbol and buffer) to the trigger scope/block.
	 * @param anode The RECORD_NAME node. Must already have the Table symbol linked to it.
	 */
	protected void defineBufferForTrigger(AST recAST) {}

	
	/** Called by the tree parser to define anything other than
	 * buffers, temp/work tables, and variables/parameters.
	 */
	protected Symbol defineSymbol(int symbolType, AST defAST, AST idAST) { return null; }

	
	/** Called by the tree parser at a temp or work table field definition. */
	protected void defineTableField(AST idNode) {}

	
	/** Called by the tree parser if a LIKE node is encountered in a temp/work table definition. */
	protected void defineTableLike(AST recNode) {}


	/** Called by the tree parser when a temp-table is defined. */
	protected void defineTemptable(AST defAST, AST idNode) {}

	
	/** Called by the tree parser when a variable is defined. */
	protected Variable defineVariable(AST defAST, AST idNode) {return null;}
	/** Some syntaxes imply a data type without LIKE/AS. */
	protected Variable defineVariable(AST defAST, AST idAST, int dataType) {return null;}
	/** Some syntaxes have an implicit LIKE. */
	protected Variable defineVariable(AST defAST, AST idAST, AST likeAST) {return null;}
	
	/** Called by the tree parser when a work-table is defined. */
	protected void defineWorktable(AST defAST, AST idNode) {}

	
	/** Process a Field_ref node.
	 * @param refAST The Field_ref node.
	 * @param idAST The ID node.
	 * @param contextQualifier What sort of reference is this? Read? Update? Etc.
	 * @param 
	 * @param whichTable For name resolution - which table must this be a field of?
	 * Input 0 for any table, 1 for the lastTableReferenced, 2 for the prevTableReferenced.
	 */
	protected void field(AST refAST, AST idAST, int contextQualifier, int whichTable) {}

	/**
	 * Action taken in:
	 * filenameorvalue: FILENAME  production
	 */
	protected void fnvFilename(AST fn) {}

	/**
	 * Action taken in:
	 * filenameorvalue: ... expression ... production
	 */
	protected void fnvExpression(AST exp) {}

	
	/** Called from Form_item node */
	protected void formItem(AST ast) {}

	/** Called from DO|REPEAT|FOR blocks. */
	protected void frameBlockCheck(AST ast) {}

	/** Called at tree parser DEFINE FRAME statement. */
	protected void frameDef(AST defAST, AST idAST) {}
	
	/** This is called at the beginning of a frame affecting statement, with the statement head node. */
	protected void frameInitializingStatement(AST ast) {}
	
	/** This is called at the end of a frame affecting statement. */
	protected void frameStatementEnd() {}

	/** Called for the ID node in a #(FRAME ID) pair. */
	protected void frameRef(AST idAST) {}

	/** Called by the tree parser in a function definition immediately
	 * before the code block begins.
	 * @param funcAST The FUNCTION node.
	 * @param idAST The ID node (the function name).
	 */
	protected void funcDef(AST funcAST, AST idAST) {}
	
	/** Called by the tree parser if a FUNCTION statement is found to be any
	 * sort of a function FORWARD, IN, or MAP TO.
	 * @param idAST The ID node (name of the function).
	 */
	protected void funcForward(AST idAST) {}
	
	public ParseUnit getParseUnit() { return parseUnit; }

	/** Called by the tree parser at METHOD statement, after method's scope has been created. */
	protected void methodDef(AST idAST) {}

	/** Called by the tree parser at the beginning 
	 * of a PROCEDURESTATE rule.
	 */
	protected void procedureBegin(AST p, AST id){}

	/** Called by the tree parser at the end
	 * of a PROCEDURESTATE rule.
	 */
	protected void procedureEnd(AST p){}
	
	
	/** Called by the tree parser right off the bat, at the Program_root node */
	protected void programRoot(AST rootAST) {}
	
	
	/** Called by the tree parser at the end of the program, after Program_tail. */
	protected void programTail() {}
	

	/** Action to take at RECORD_NAME nodes. */
	protected void recordNameNode(AST anode, int contextQualifier) {}


	/** Action to take at the start of RUNSTATE. */
	protected void runBegin(AST t){}

	/** Action to take at the end of RUNSTATE. */
	protected void runEnd(AST node){}
	
	/** Action to take in a RUNSTATE of the form
	 * run <p> in <handle expression>.
	 * @param hn - the node for <handle expression>.
	 */ 
	protected void runInHandle(AST hn){}
	
	/** Action to take in RUNSTATE of the form
	 * run <p> persistent set <handle>.
	 * @param fld - the field node for <handle>.
	 */
	protected void runPersistentSet(AST fld){}
	
	/** Called by the tree parser where a symbol scope needs to be added,
	 * in other words, in functions, procedures, and triggers.
	 * @param anode The function, procedure, triggers, or on node.
	 */
	protected void scopeAdd(AST anode) {}


	/** Called by the tree parser immediately after the end of a function,
	 * procedure, or trigger block (a symbol scope).
	 * @param scopeRootNode The function, procedure, triggers, or on node.
	 */
	protected void scopeClose(AST scopeRootNode) {}
	
	
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
	protected void strongScope(AST anode) {}
	

	/** Called with the VIEW statement head, after the VIEW branch has been traversed. */
	protected void viewState(AST headAST) {}


}
