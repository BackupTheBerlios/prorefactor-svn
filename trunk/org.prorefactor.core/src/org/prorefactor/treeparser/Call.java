/**
 * Call.java - 
 * @author Peter Dalbadie
 * 21-Sep-2004
 * 
 * Copyright (c) 2004,2006 ProRefactor.org.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.prorefactor.treeparser;

import org.prorefactor.core.JPNode;

import antlr.SemanticException;




/**
 * Represents a Call to some 4GL procedure. The target procedure is 
 * identified by the external and internal procedure names.
 * The expecte values for externalName and internalName are as follows:
 *                                 externalName - internalName
 * run <proc> [in this-procedure]: compile-unit   <proc>
 * run <proc> in <handle>.       : handle:target  <proc>
 * run <proc> [persistent [...]. : compile-unit   null
 * @author pcd
 *
 */
public class Call extends SemanticRecord {


	private String runArgument = null;
	private RunHandle runHandle = null;
	private Variable persistentHandleVar;
	
	private String internalName = null;
	private String externalName = null;

	public boolean isPersistent(){
		return persistentHandleVar != null;
	}
	
	public boolean isInHandle(){
		return runHandle != null;
	}
	
	/**
	 * @param node
	 */
	public Call(JPNode node) {
		super(node);
	}

	/**
	 * Construct a call to an internal procedure in a
	 * specific containing procedure. The refererence
	 * is fully resolved.
	 * @param externalName
	 * @param internalName
	 */
	public Call(String externalName, String internalName){
		this.internalName = internalName;
		this.externalName = externalName;
	}

	/**
	 * Get the internal procedure name, if any, to which
	 * this call refers.
	 * @return
	 */
	public String getInternalName() {
		return internalName;
	}

	/**
	 * Get the external procedure name to which this
	 * call refers.
	 * @return
	 */
	public String getExternalName() {
		return externalName;
	}

	public void setRunHandle(RunHandle handle) {
		this.runHandle = handle;
	}

	/**
	 * The fully qualified routine name to which
	 * this call refers.
	 * @return
	 */
	public String id(){
		return externalName + "." + internalName;
	}
	
	/**
	 * Equality definition: two calls are equal if 
	 * their id()'s are equal -- i.e. they refer to
	 * the same routine.
	 */
	public boolean equals(Object other){
		if (other.getClass() == this.getClass()){
			Call otherCall = (Call) other;
			return id().equalsIgnoreCase(otherCall.id());
		} else return false;
	}

	/**
	 * Sets runArgument: the parameter in run <fileName>,
	 * which may be an explicit string or a string expression,
	 * and which identifies either an external or an internal 
	 * procedure.
	 * @param f
	 */
	public void setRunArgument(String f) {
		runArgument = f;
	}

	public void finalize(boolean definedInternal) throws SemanticException{
		if (isUnresolved()){
			throw new SemanticException("Attempt to finalize unresolved call.");
		}
		if (isInHandle()){ // Internal procedure call - using a handle.
			internalName = runArgument;
			externalName = (String) runHandle.getValue(); 
		} else if (definedInternal){ // Internal procedure call - without a handle.
			internalName = runArgument;
			externalName = baseFilename(getFilename());
		} else if (isPersistent()){ // External procedure call - as persistent proc.
			internalName = null;
			externalName = runArgument;

			// Update the handle Variable; the variable is
			// shared by reference with the SymbolTable.
			RunHandle hValue = new RunHandle();
			hValue.setValue(externalName);
			persistentHandleVar.setValue(hValue);
		} else { // External procedure call - non persistent.
			internalName = null;
			externalName = runArgument;
		}
	}

	/**
	 * @return
	 */
	public boolean isUnresolved() {
		// TODO - file name may be an expression, or run
		// statement can be of in-handle form, where handle
		// is given by a function call.
		return false;
	}

	/**
	 * 
	 */
	public String getRunArgument() {
		return runArgument;
	}

	/**
	 * Set persistentHandleVar: the variable that will be
	 * used to refer to the persistent procedure instance
	 * created by this call - if any.
	 * Only used in connection with:
	 * run <proc> persistent set <handle>.
	 * @param var
	 */
	public void setPersistentHandleVar(Variable var) {
		persistentHandleVar = var;
	}
	
	public String baseFilename(String filename){
		int startAt = filename.lastIndexOf("/") + 1;
		return filename.substring(startAt);
	}

	/**
	 * @return
	 */
	public boolean isLocal() {
		return getFilename() == externalName;
	}

	/**
	 * @return
	 */
	public String getLocalTarget() {
		return internalName;
	}

}