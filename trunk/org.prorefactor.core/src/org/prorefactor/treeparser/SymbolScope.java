/**
 * SymbolScope.java
 * @author John Green
 * 6-Nov-2002
 * www.joanju.com
 * 
 * Copyright (c) 2002-2004 Joanju Limited.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 */

package org.prorefactor.treeparser;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.prorefactor.core.IConstants;
import org.prorefactor.core.schema.Schema;
import org.prorefactor.core.schema.Table;



/**
 * For keeping track of PROCEDURE, FUNCTION, and trigger
 * scopes within a 4gl compile unit.
 * Note that scopes are nested. There is the outer program scope,
 * and within it the other types of scopes which may themselves
 * nest trigger scopes. (Trigger scopes may be deeply nested).
 * These scopes are defined <b>Symbol</b> scopes. They have nothing to
 * do with record or frame scoping!
 */
public class SymbolScope {

	/** Only Scope and derivatives may create a Scope object.
	 * @param parentScope null if called by the SymbolScopeRoot constructor.
	 */
	protected SymbolScope(SymbolScope parentScope) {
		this.parentScope = parentScope;
		if (parentScope!=null) this.rootScope = parentScope.rootScope;
	}

	private static Schema schema = Schema.getInstance();

	private ArrayList allSymbols = new ArrayList();
	private ArrayList childScopes = new ArrayList();
	private Block rootBlock;
	protected Map bufferMap = new HashMap();
	protected Map callMap = new HashMap();
	protected Map routineMap = new HashMap();
	protected Map variableMap = new HashMap();
	protected Map unnamedBuffers = new HashMap();
	private SymbolScope parentScope;
	protected SymbolScopeRoot rootScope;


	/** Add a Routine for call handling. */
	public void add(Routine routine){
		routineMap.put(routine.getName().toLowerCase(), routine);
	}

	
	/** Add a Variable for names lookup. */
	public void add(Variable fieldSymbol) {
		variableMap.put(fieldSymbol.getName().toLowerCase(), fieldSymbol);
	}



	/** Add a new scope to this scope. */
	public SymbolScope addScope() {
		SymbolScope newScope = new SymbolScope(this);
		childScopes.add(newScope);
		return newScope;
	}
	
	
	
	/** All symbols within this scope are added to this scope's symbol list.
	 * This method has "package" visibility, since the Symbol object adds itself to its scope.
	 */
	void addSymbol(Symbol symbol) {
		allSymbols.add(symbol);
	}

	
	
	/** Define a new BufferSymbol.
	 * @param name Input "" for a default or unnamed buffer, otherwise the "named buffer" name.
	 */
	public TableBuffer defineBuffer(String name, Table table) {
		TableBuffer buffer = new TableBuffer(name, this, table);
		if (name.length()==0) {
			if (table.getStoretype()==IConstants.ST_DBTABLE)
				unnamedBuffers.put(table, buffer);
			else // default buffers for temp/work tables go into the "named" buffer map
				bufferMap.put(table.getName().toLowerCase(), buffer);
		} else
			bufferMap.put(name.toLowerCase(), buffer);
		return buffer;
	}



	/** Get the integer "depth" of the scope, with the program root scope
	 * being zero. Functions and procedures will always be depth one, and trigger
	 * scopes can be nested, so they will always be one or greater. I use this
	 * function for unit testing - I want to be able to examine the scope of a
	 * symbol, and make sure that the symbol belongs to the scope that I expect.
	 */
	public int depth() {
		int depth = 0;
		SymbolScope scope = this;
		while ( (scope = scope.getParentScope()) != null ) depth++;
		return depth;
	}
	
	
	
	/** Get a *copy* of the list of all symbols in this scope */
	public ArrayList getAllSymbols() { return new ArrayList(allSymbols); }
	
	
	
	/** Get a list of this scope's symbols, and all symbols of all descendant scopes. */
	public ArrayList getAllSymbolsDeep() {
		ArrayList ret = new ArrayList(allSymbols);
		for (Iterator it = childScopes.iterator(); it.hasNext();) {
			SymbolScope child = (SymbolScope) it.next();
			ret.add(child.getAllSymbolsDeep());
		}
		return ret;
	}



	/** Get the set of named buffers */
	public Set getBufferSet() { return bufferMap.entrySet(); }



	/** Given a name, find a BufferSymbol (or create if necessary for unnamed buffer). */
	public TableBuffer getBufferSymbol(String inName) {
		TableBuffer symbol = lookupBuffer(inName);
		if (symbol!=null) return symbol;
		// The default buffer for temp and work tables was defined at
		// the time that the table was defined. So, lookupBuffer() would have found
		// temp/work table references, and all we have to search now is schema.
		Table table = schema.lookupTable(inName);
		if (table==null) return null;
		return getUnnamedBuffer(table);
	} // getBufferSymbol
	
	
	
	/** Get a *copy* of the list of child scopes */
	public ArrayList getChildScopes() {
		return new ArrayList(childScopes);
	}
	
	
	
	/** Get a list of all child scopes, and their child scopes, etc */
	public ArrayList getChildScopesDeep() {
		ArrayList ret = new ArrayList();
		for (Iterator it = childScopes.iterator(); it.hasNext();) {
			SymbolScope child = (SymbolScope) it.next();
			ret.add(child);
			ret.addAll(child.getChildScopesDeep());
		}
		return ret;
	}

	
	
	public SymbolScope getParentScope() { return parentScope; }


	public Block getRootBlock() { return rootBlock; }


	public SymbolScopeRoot getRootScope() { return rootScope; }



	/** Get or create the unnamed buffer for a schema table. */
	public TableBuffer getUnnamedBuffer(Table table) {
		assert table.getStoretype() == IConstants.ST_DBTABLE;
		// Check this and parents for the unnamed buffer. Table triggers
		// can scope an unnamed buffer - that's why we don't go straight to
		// the root scope.
		SymbolScope nextScope = this;
		while (nextScope!=null) {
			TableBuffer buffer = (TableBuffer) nextScope.unnamedBuffers.get(table);
			if (buffer!=null) return buffer;
			nextScope = nextScope.parentScope;
		}
		return rootScope.defineBuffer("", table);
	}

	
	
	/** Get the set of Variables. (vars, params, etc, etc.) */
	public Set getVariableSet() { return variableMap.entrySet(); }


	/** Answer whether the scope has a Routine named
	 * by param.
	 * @param name - the name of the routine.
	 */
	public boolean hasRoutine(String name){
		return routineMap.containsKey(name.toLowerCase());
	}
	
	
	/**
	 * Lookup a named record/table buffer in this scope or an enclosing scope.
	 * @param inName String buffer name.
	 * @return A Buffer, or null if not found.
	 */
	public TableBuffer lookupBuffer(String inName) {
		// - Buffer names cannot be abbreviated.
		// - Buffer names *can* be qualified with a database name.
		// - Buffer names *are* unique in a given scope: you cannot have two
		//   buffers with the same name in the same scope even if they are
		//   for two different databases.
		String [] parts = inName.split("\\.");
		String bufferPart;
		String dbPart = "";
		if (parts.length == 1)
			bufferPart = inName;
		else {
			dbPart = parts[0];
			bufferPart = parts[1];
		}
		TableBuffer symbol = (TableBuffer) bufferMap.get(bufferPart.toLowerCase());
		if (	symbol==null
			||	(	dbPart.length()!=0
				&& ! dbPart.equalsIgnoreCase(symbol.getTable().getDatabase().getName())
				)
			) {
			if (parentScope==null) return null;
			return parentScope.lookupBuffer(inName);
		}
		return symbol;
	} // lookupBuffer()



	/** Lookup a Table or a BufferSymbol, schema table first.
	 * It seems to work like this: unabbreviated schema name, then
	 * buffer/temp/work name, then abbreviated schema names. Sheesh.
	 */
	public TableBuffer lookupTableOrBufferSymbol(String inName) {
		Table table = schema.lookupTable(inName);
		if (table!=null && table.getName().length()==inName.length()) return getUnnamedBuffer(table);
		TableBuffer ret2 = lookupBuffer(inName);
		if (ret2!=null) return ret2;
		if (table!=null) return getUnnamedBuffer(table);
		if (parentScope==null) return null;
		return parentScope.lookupTableOrBufferSymbol(inName);
	} // lookupTableSchemaFirst()



	public TableBuffer lookupTempTable(String name) {
		TableBuffer buff = (TableBuffer) bufferMap.get(name.toLowerCase());
		if (buff != null) return buff;
		if (parentScope==null) return null;
		return parentScope.lookupTempTable(name);
	}



	/**
	 * Lookup a Variable in this scope or an enclosing scope.
	 * @param inName The string field name to lookup.
	 * @return A Variable, or null if not found.
	 */
	public Variable lookupVariable(String inName) {
		Variable var = (Variable) variableMap.get(inName.toLowerCase());
		if (var==null && parentScope!=null) return parentScope.lookupVariable(inName);
		return var;
	}



	public void setRootBlock(Block block) { rootBlock = block; }


	/**
	 * @param call
	 */
	public void registerCall(Call call) {
		callMap.put(call.id(), call);
	}


	/**
	 * @return a Collection view of the calls registered in
	 * this SymbolScope. Caution: changes to the Collection
	 * will affect the internal list in the SymbolScope.
	 */
	public Collection getCallList() {
		// TODO - change signature to Map to allow key search.
		return callMap.values();
	}


	/**
	 * @param name
	 * @return
	 */
	public Routine lookupRoutine(String name) {
		Routine routine = (Routine) routineMap.get(name.toLowerCase());
		return routine;
	}



}
