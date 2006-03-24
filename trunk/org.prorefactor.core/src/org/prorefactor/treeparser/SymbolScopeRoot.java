/**
 * ScopeRoot.java
 * @author John Green
 * 6-Nov-2002
 * www.joanju.com
 * 
 * Copyright (c) 2002-2006 Joanju Software.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 */

package org.prorefactor.treeparser;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.prorefactor.core.IConstants;
import org.prorefactor.core.schema.Field;
import org.prorefactor.core.schema.Table;




/**
 * A ScopeRoot object is created for each compile unit, and
 * it represents the program (topmost) scope. For classes, it is the class
 * scope, but it may also have a super class scope by way of inheritance.
 */
public class SymbolScopeRoot extends SymbolScope {

	public SymbolScopeRoot() {
		super(null);
		this.rootScope = this;
	}
	
	private String className = null;

	private Set<Table> tableSet = new HashSet<Table>();

	
	
	/** Assign a super (inherited) class scope to this class scope. */
	public void assignSuper(SymbolScopeRoot superScope) {
		assert parentScope == null;
		parentScope = superScope;
	}


	/** Define a temp or work table.
	 * @param name The name, with mixed case as in DEFINE node.
	 * @param type IConstants.ST_TTABLE or IConstants.ST_WTABLE.
	 * @return A newly created BufferSymbol for this temp/work table.
	 */
	public TableBuffer defineTable(String name, int type) {
		Table table = new Table(name, type);
		tableSet.add(table);
		// Pass empty string for name for default buffer.
		TableBuffer bufferSymbol = new TableBuffer("", this, table);
		// The default buffer for a temp/work table is not "unnamed" the way
		// that the default buffer for schema tables work. So, the buffer
		// goes into the regular bufferMap, rather than the unnamedBuffers map.
		bufferMap.put(name.toLowerCase(), bufferSymbol);
		return bufferSymbol;
	} // defineTable()

	
	
	/** Define a temp or work table field */
	public FieldBuffer defineTableField(String name, TableBuffer buffer) {
		Table table = buffer.getTable();
		Field field = new Field(name, table);
		FieldBuffer fieldBuff = new FieldBuffer(this, buffer, field);
		return fieldBuff;
	}


	/** Valid only if the parse unit is a CLASS.
	 * Returns null otherwise. 
	 */
	public String getClassName() { return className; }

	
	public TableBuffer getLocalTableBuffer(Table table) {
		assert table.getStoretype() != IConstants.ST_DBTABLE;
		return (TableBuffer) bufferMap.get(table.getName().toLowerCase());
	}



	/** Lookup an unqualified temp/work table field name.
	 * Does not test for uniqueness. That job is left to the compiler.
	 * (In fact, anywhere this is run, the compiler would check that the
	 * field name is also unique against schema tables.)
	 * Returns null if nothing found.
	 */
	protected Field lookupUnqualifiedField(String name) {
		Field field;
		for (Table table : tableSet) {
			field = table.lookupField(name);
			if (field!=null) return field;
		}
		return null;
	}



	/**
	 * @return a Collection containing all Routine objects
	 * defined in this RootSymbolScope.
	 */
	public Map getRoutineMap() {
		return routineMap;
	}


	public void setClassName(String s) { className=s; }


}
