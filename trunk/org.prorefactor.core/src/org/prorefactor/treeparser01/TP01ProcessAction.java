/**
 * TP01ProcessAction.java
 * @author Peter Dalbadie
 * 21-Sep-2004
 * 
 * Copyright (c) 2004,2006 ProRefactor.org.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */


package org.prorefactor.treeparser01;


import java.util.Map;

import org.prorefactor.core.JPNode;
import org.prorefactor.treeparser.Call;
import org.prorefactor.treeparser.ErrorList;
import org.prorefactor.treeparser.Expression;
import org.prorefactor.treeparser.Routine;
import org.prorefactor.treeparser.RunHandle;
import org.prorefactor.treeparser.SemanticError;
import org.prorefactor.treeparser.SymbolScope;
import org.prorefactor.treeparser.SymbolScopeRoot;
import org.prorefactor.treeparser.TableBuffer;
import org.prorefactor.treeparser.Variable;

import antlr.SemanticException;
import antlr.collections.AST;


/**
 * @author pcd
 *
 */
public class TP01ProcessAction extends TP01Action {

	public TP01ProcessAction(SymbolScopeRoot rootScope){
		this.rootScope = rootScope;
		currentScope = rootScope;
		errorList = new ErrorList();
	}
	
	SymbolScope currentScope;
	SymbolScopeRoot rootScope;
	TableBuffer lastTableReferenced;
	TableBuffer prevTableReferenced;
	TableBuffer currDefTable;

	private Call wipCall;
	private ErrorList errorList;
	private Expression wipExpression;
	private JPNode wipFieldNode;
	

	/**
	 * Get the RunHandle value in "run <proc> in <handle>." Where <handle>
	 * is a handle valued Expression; then save the RunHandle value
	 * in the current call.
	 * @see com.qad.parse.ParseAction#runInHandle(antlr.collections.AST, antlr.collections.AST)
	 */
	public void runInHandle(AST node) {
		//wipCall.setRunHandle((RunHandle) wipExpression.getValue());
		// TODO - enable expressionExprt
		Variable handleVar = currentScope.lookupVariable(wipFieldNode.getText());
		wipCall.setRunHandle((RunHandle) handleVar.getValue());
	}

	/** 
	 * Update the <handle> in "run <proc> persistent set <handle>.":
	 * save a reference to the external procedure <proc> in <handle>.
	 * The AST structure for this form of the run is:
	 * runstate	: 
	 * 		#(	RUN filenameorvalue (#(PERSISTENT ( #(SET (field)? ) <A> )? )
	 * where <A> is this action. Thus, we expect a value in wipFieldNode
	 * with the name of the handle variable.
	 * This method gets the variable from the current scope and stores
	 * a reference to it in the current call (being built), so that
	 * the Call.finalize method can update its value.
	 * @param fld is used for error reporting.
	 */
	public void runPersistentSet(AST fld) {
		String varName = wipFieldNode.getText();

		Variable var = (Variable) currentScope.lookupVariable(varName);
		
		if (var != null){
			// Store the Variable in the Call under construction.
			Call call = wipCall;
			call.setPersistentHandleVar(var);
		} else {
			String errMsg = new String("Undefined handle variable: " + varName);
			SemanticError err = new SemanticError(errMsg, (JPNode) fld);
			errorList.add(err);
		}
	}

	/* (non-Javadoc)
	 * @see com.qad.parse.ParseAction#run(antlr.collections.AST)
	 */
	public void runBegin(AST node) {
		// Expect a FileName at the top of semantic stack;
		String fileName = (String) wipExpression.getValue();
		Call call = new Call((JPNode) node);
		call.setRunArgument(fileName);
		wipCall = call;
	}

	/* (non-Javadoc)
	 * @see com.qad.parse.ParseAction#runEnd()
	 */
	public void runEnd(AST node) {

/* 
 * Cannot resolve whether internal or external procedure here.
 * run <name> in <handle>: <name> is an internal procedure.
 * run <name> and <name> is defined in the current scope: <name> is an internal procedure.
 * run <name> and <name> is not defined in the current scope: <name> is an external procedure.
 * 
 */
		Call call = wipCall;
		
		if (call.isUnresolved()){
			errorList.add(new SemanticError("Unresolved call", null));
		} else {
			try {
				String routineId = call.getRunArgument();
				call.finalize(rootScope.hasRoutine(routineId));
			} catch (SemanticException e) {
				throw new RuntimeException("Unhandled SemanticException.");
			}
		}
			
		// Record the call in the current context.
		currentScope.registerCall(call);
	}
	
	/**
	 * Action taken in:
	 * filenameorvalue: ... expression ... production
	 */
	public void fnvExpression(AST node){
		wipExpression = new Expression((JPNode) node);
	}
	
	/**
	 * Action taken in:
	 * filenameorvalue: FILENAME  production
	 * @return
	 */
	public void fnvFilename(AST node){
		Expression exp = new Expression((JPNode) node);
		exp.setValue(node.getText());
		wipExpression = exp;
	}

	
	public void field(AST refAST, AST idAST, int contextQualifier, int whichTable) {
		wipFieldNode = (JPNode) idAST;
	}

	
	/**
	 * Switch SymbolTable scope to that of the procedure.
	 * @see com.qad.parse.ParseAction#procedureBegin(antlr.collections.AST)
	 */
	public void procedureBegin(AST p, AST id) {
		/* TODO - review implementation of scope open and close */
		Routine routine = rootScope.lookupRoutine(id.getText());
		currentScope = routine.getRoutineScope();
	}


	/**
	 * @see com.qad.parse.ParseAction#procedureEnd(antlr.collections.AST)
	 */
	public void procedureEnd(AST p) {
		currentScope = currentScope.getParentScope();
	}

	/**
	 * 
	 * @see com.qad.parse.ParseAction#expressionExprt(antlr.collections.AST)
	 */
	public void expressionExprt(AST node) {
		wipExpression = new Expression((JPNode) node);
		wipExpression.setValue(wipFieldNode.getText());
	}

	
	/**
	 * Pop a SystemHandle from the semantic stack, create an
	 * Expression, save the SystemHandle as the Expression value
	 * and place the Expression on top of semantic stack.
	 * @see com.qad.parse.ParseAction#exprtSystemHandleName(antlr.collections.AST)
	 */
	public void exprtSystemHandleName(AST sysHandleName) {
	}


	/** @return the Map of Call objects registered in the currentScope. */
	public Map<String, Call> getCallList() { return currentScope.getCallMap(); }


	/**
	 * @return
	 */
	public SymbolScopeRoot getRootScope() {
		// TODO Move Up: should scope elements be in action base class.
		return rootScope;
	}

	/**
	 * @return
	 */
	public ErrorList getErrorList() {
		return errorList;
	}
}
