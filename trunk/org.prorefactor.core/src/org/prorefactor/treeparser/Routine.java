/**
 * Routine.java - 
 * @author Peter Dalbadie
 * 21-Sep-2004
 * 
 */

package org.prorefactor.treeparser;

/**
 * Represents the definition of a Routine.
 * Use as an entry in the symbol table.
 *
 */
public class Routine extends Symbol {
		
	private SymbolScope routineScope;
	
	public Routine (String name, SymbolScope definingScope, SymbolScope routineScope){
		super(definingScope);
		setName(name);
		this.routineScope = routineScope;
	}


	/**
	 * @see org.prorefactor.treeparser.Symbol#fullName()
	 */
	public String fullName() {
		return getName();
	}

	public SymbolScope getRoutineScope(){
		return routineScope;
	}
}
