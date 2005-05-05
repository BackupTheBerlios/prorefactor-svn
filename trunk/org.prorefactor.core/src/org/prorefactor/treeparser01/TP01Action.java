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


import org.prorefactor.treeparser.Symbol;
import org.prorefactor.treeparser.Variable;

import antlr.collections.AST;


/** Superclass of empty actions methods for TreeParser01.
 * Sublcasses can override and implement any of these methods,
 * which are all called directly by TreeParser01.
 * TP01Support is the default implementation.
 */
public class TP01Action {
	
	
	/** Called by the tree parser at the end of a DEFINE statement, passing
	 * in the new Variable object which is expected now to be added to the current scope.
	 */
	public void addToScope(Object o) {}


	/** Beginning of a block. */
	public void blockBegin(AST blockAST) {}


	/** End of a block. */
	public void blockEnd() {}


	/** The tree parser calls this at the start of a can-find,
	 * because it needs to have its own buffer and buffer scope.
	 */
	protected void canFindBegin(AST canfindAST, AST recordAST) {
	}


	/** Called by the tree parser at the end of a can-find. */
	protected void canFindEnd(AST canfindAST) {}
	
	
	/** The tree parser calls this at an AS node */
	public void defAs(AST asAST) {}

	
	/** The tree parser calls this at a LIKE node */
	public void defLike(AST likeAST) {}
	
	
	/** Define a buffer. If the buffer is initialized at the same time it is
	 * defined (as in a buffer parameter), then parameter init should be true.
	 */
	public void defineBuffer(AST defAST, AST idAST, AST recAST, boolean init) {}

	
	/** Define an unnamed buffer which is scoped (symbol and buffer) to the trigger scope/block.
	 * @param anode The RECORD_NAME node. Must already have the Table symbol linked to it.
	 */
	public void defineBufferForTrigger(AST recAST) {}

	
	/** Called by the tree parser to define anything other than
	 * buffers, temp/work tables, and variables/parameters.
	 */
	public Symbol defineSymbol(int symbolType, AST defAST, AST idAST) { return null; }

	
	/** Called by the tree parser at a temp or work table field definition. */
	public void defineTableField(AST idNode) {}

	
	/** Called by the tree parser if a LIKE node is encountered in a temp/work table definition. */
	public void defineTableLike(AST recNode) {}


	/** Called by the tree parser when a temp-table is defined. */
	public void defineTemptable(AST defAST, AST idNode) {}

	
	/** Called by the tree parser when a variable is defined. */
	public Variable defineVariable(AST defAST, AST idNode) {return null;}
	/** Some syntaxes imply a data type without LIKE/AS. */
	public Variable defineVariable(AST defAST, AST idAST, int dataType) {return null;}
	/** Some syntaxes have an implicit LIKE. */
	public Variable defineVariable(AST defAST, AST idAST, AST likeAST) {return null;}
	
	/** Called by the tree parser when a work-table is defined. */
	public void defineWorktable(AST defAST, AST idNode) {}

	
	/** Process a Field_ref node.
	 * @param refAST The Field_ref node.
	 * @param idAST The ID node.
	 * @param contextQualifier What sort of reference is this? Read? Update? Etc.
	 * @param 
	 * @param whichTable For name resolution - which table must this be a field of?
	 * Input 0 for any table, 1 for the lastTableReferenced, 2 for the prevTableReferenced.
	 */
	public void field(AST refAST, AST idAST, int contextQualifier, int whichTable) {}


	/** Called by the tree parser in a function definition immediately
	 * before the code block begins.
	 * @param funcAST The FUNCTION node.
	 * @param idAST The ID node (the function name).
	 */
	protected void funcDef(AST funcAST, AST idAST) {}


	/**
	 * Action taken in:
	 * filenameorvalue: FILENAME  production
	 */
	public void fnvFilename(AST fn) {}


	/**
	 * Action taken in:
	 * filenameorvalue: ... expression ... production
	 */
	public void fnvExpression(AST exp) {}

		
	/** Called by the tree parser if a FUNCTION statement is found to be any
	 * sort of a function FORWARD, IN, or MAP TO.
	 * @param idAST The ID node (name of the function).
	 */
	protected void funcForward(AST idAST) {}

	/** Called by the tree parser at the beginning 
	 * of a PROCEDURESTATE rule.
	 */
	public void procedureBegin(AST p, AST id){}

	/** Called by the tree parser at the end
	 * of a PROCEDURESTATE rule.
	 */
	public void procedureEnd(AST p){}
	

	/** Called by the tree parser right off the bat, at the Program_root node */
	public void programRoot(AST rootAST) {}
	
	
	/** Action to take at RECORD_NAME nodes. */
	public void recordNameNode(AST anode, int contextQualifier) {}


	/** Action to take at the start of RUNSTATE. */
	public void runBegin(AST t){}

	/** Action to take at the end of RUNSTATE. */
	public void runEnd(AST node){}
	
	/** Action to take in a RUNSTATE of the form
	 * run <p> in <handle expression>.
	 * @param hn - the node for <handle expression>.
	 */ 
	public void runInHandle(AST hn){}
	
	/** Action to take in RUNSTATE of the form
	 * run <p> persistent set <handle>.
	 * @param fld - the field node for <handle>.
	 */
	public void runPersistentSet(AST fld){}
	
	/** Called by the tree parser where a symbol scope needs to be added,
	 * in other words, in functions, procedures, and triggers.
	 * @param anode The function, procedure, triggers, or on node.
	 */
	public void scopeAdd(AST anode) {}


	/** Called by the tree parser immediately after the end of a function,
	 * procedure, or trigger block (a symbol scope).
	 * @param scopeRootNode The function, procedure, triggers, or on node.
	 */
	protected void scopeClose(AST scopeRootNode) {}
	
	
	/** Create a "strong" buffer scope.
	 * This is called within a DO FOR or REPEAT FOR statement.
	 * @param anode Is the RECORD_NAME node. It must already have
	 * the BufferSymbol linked to it.
	 */
	public void strongScope(AST anode) {}


}
