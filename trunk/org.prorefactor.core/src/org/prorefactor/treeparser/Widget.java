/** 3 May 2005
 * Authors: John Green
 * 
 * Copyright (c) 2005 Joanju (www.joanju.com)
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.prorefactor.treeparser;


/**
 * A Symbol defined with DEFINE <widget-type> or any of the other various
 * syntaxes which implicitly define a widget.
 * This includes FRAMEs, WINDOWs, MENUs, etc.
 */
public abstract class Widget extends Symbol {

	public Widget(String name, SymbolScope scope) {
		super(scope);
		setName(name);
	}

	/** For this subclass of Symbol, fullName() returns the same value as getName(). */
	public String fullName() { return getName(); }

}
