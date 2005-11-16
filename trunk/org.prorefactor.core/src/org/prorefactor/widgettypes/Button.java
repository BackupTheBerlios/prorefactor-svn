/* Created on May 3, 2005
 * Authors: John Green
 * 
 * Copyright (c) 2005 Joanju (www.joanju.com)
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.prorefactor.widgettypes;

import org.prorefactor.core.TokenTypes;
import org.prorefactor.treeparser.FieldLevelWidget;
import org.prorefactor.treeparser.SymbolScope;


public class Button extends FieldLevelWidget {

	public Button(String name, SymbolScope scope) { super(name, scope); }

	/** Returns TokenTypes.BUTTON. */
	public int getProgressType() { return TokenTypes.BUTTON; }

}