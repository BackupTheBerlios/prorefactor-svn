/** 6-Nov-2002
 * Authors: John Green
 * 
 * Copyright (c) 2002, 2004, 2005 Joanju (www.joanju.com)
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.prorefactor.treeparser;

import org.prorefactor.core.TokenTypes;



/**
 * A Symbol defined with DEFINE VARIABLE or any of the other various
 * syntaxes which implicitly define a variable.
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

	/** Returns TokenTypes.VARIABLE.
	 * @see org.prorefactor.treeparser.Symbol#getProgressType()
	 */
	public int getProgressType() {
		return TokenTypes.VARIABLE;
	}

	/** @see org.prorefactor.treeparser.Value#setValue(java.lang.Object) */
	public void setValue(Object value) { this.value = value; }

}
