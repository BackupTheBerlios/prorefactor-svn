/* Created on 31-Jan-2006
 * Authors: John Green
 *
 * Copyright (c) 2006 Joanju (www.joanju.com)
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.prorefactor.treeparser;

import java.util.HashSet;

import org.prorefactor.core.schema.Field;

/** Frame and Browse widgets are FieldContainers.
 * This class provides the services for looking up fields/variables in a Frame or Browse.
 * 
 */
public abstract class FieldContainer extends Widget {

	public FieldContainer(String name, SymbolScope scope) { super(name, scope); }

	private HashSet<Variable> variableSet = new HashSet<Variable>();
	private HashSet<FieldBuffer> fieldSet = new HashSet<FieldBuffer>();


	/** Add a FieldBuffer or Variable to this Frame or Browse object. */
	public void addSymbol(Symbol symbol) {
		if (symbol instanceof FieldBuffer) fieldSet.add((FieldBuffer)symbol);
		else if (symbol instanceof Variable) variableSet.add((Variable)symbol);
		// else... we could add other field level widgets here, but we don't yet have a use for those.
	}

	
	/** Check to see if a name matches a Variable or a FieldBuffer in this FieldContainer.
	 * Used by the tree parser at the INPUT function for resolving the name reference.
	 */
	public Symbol lookupFieldOrVar(Field.Name name) {
		if (name.table==null) for (Variable var : variableSet) {
			if (var.getName().equalsIgnoreCase(name.field)) return var;
		}
		for (FieldBuffer fieldBuffer : fieldSet) {
			if (fieldBuffer.canMatch(name)) return fieldBuffer;
		}
		return null;
	}

}
