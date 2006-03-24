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
	private String className = null;



	/** Return the name of the variable. For this subclass of
	 * Symbol, fullName() returns the same value as getName().
	 */
	public String fullName() { return getName(); }

	/** @see Primative#getClassName() */
	public String getClassName() { return className; }
	
	public DataType getDataType() { return dataType; }
	
	/** @see org.prorefactor.treeparser.Value#getValue() */
	public Object getValue() { return value; }
	
	/** Returns TokenTypes.VARIABLE. */
	@Override
	public int getProgressType() { return TokenTypes.VARIABLE; }

	public Primative setDataType(DataType dataType) { this.dataType = dataType; return this; }

	public Primative setClassName(String s) { this.className = s; return this; }

	/** @see org.prorefactor.treeparser.Value#setValue(java.lang.Object) */
	public void setValue(Object value) { this.value = value; }

}
