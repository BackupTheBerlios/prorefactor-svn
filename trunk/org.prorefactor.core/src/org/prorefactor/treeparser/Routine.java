/**
 * Routine.java - 
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

/**
 * Represents the definition of a Routine.
 * Is a Symbol - used as an entry in the symbol table.
 * A Routine is a PROCEDURE, FUNCTION, or METHOD.
 */
public class Routine extends Symbol {
		
	private SymbolScope routineScope;
	private int progressType;
	
	public Routine (String name, SymbolScope definingScope, SymbolScope routineScope){
		super(definingScope);
		setName(name);
		this.routineScope = routineScope;
	}

	@Override
	public Symbol copyBare(SymbolScope scope) {
		Routine ret = new Routine(getName(), scope, scope);
		ret.progressType = this.progressType;
		return ret;
	}

	/** @see org.prorefactor.treeparser.Symbol#fullName() */
	public String fullName() { return getName(); }

	/** Return TokenTypes: PROCEDURE, FUNCTION, or METHOD. */
	public int getProgressType() { return progressType; }
	
	public SymbolScope getRoutineScope(){ return routineScope; }
	
	public Routine setProgressType(int t) { progressType=t; return this; }

}
