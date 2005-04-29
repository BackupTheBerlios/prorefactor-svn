/**
 * Variable.java
 * @author John Green
 * 6-Nov-2002
 * www.joanju.com
 * 
 * Copyright (c) 2002, 2004 Joanju Limited.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 */

package org.prorefactor.treeparser;



/**
 * Represents a defined symbol in a 4gl compile unit, such as a
 * variable, frame, menu, menu-item, etc.
 * @see org.prorefactor.treeparser.TableBuffer
 * @see org.prorefactor.treeparser.FieldBuffer
 */
public class Variable extends Symbol implements Primative, Value {

	public Variable(String name, SymbolScope scope) {
		super(scope);
		setName(name);
	}

	private DataType dataType;
	private Object value;

	
	/** Return the name of the variable. For this subclass of
	 * Symbol, fullName() returns the same value as getName().
	 */
	public String fullName() { return getName(); }

	/** @see org.prorefactor.treeparser.Value#getValue() */
	public Object getValue() { return value; }
	
	public DataType getDataType() { return dataType; }
	
	public void setDataType(DataType dataType) { this.dataType = dataType; }

	/** @see org.prorefactor.treeparser.Value#setValue(java.lang.Object) */
	public void setValue(Object value) { this.value = value; }

}
