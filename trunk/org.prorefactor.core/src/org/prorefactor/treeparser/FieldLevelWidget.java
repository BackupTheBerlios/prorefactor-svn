/* Created on Jun 28, 2005
 * Authors: john
 * 
 * Copyright (c) 2005 Joanju (www.joanju.com)
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.prorefactor.treeparser;


/** Field-level widgets can be used in some of the same syntax
 * as variables can. (Ex: GUI statements such as frame definitions, display statemements, etc.)
 * In fact, symbol names within a scope must be unique across all VARIABLEs, BROWSEs, BUTTONs,
 * IMAGEs, and RECTANGLEs.
 */
public abstract class FieldLevelWidget extends Widget {

	public FieldLevelWidget(String name, SymbolScope scope) {
		super(name, scope);
	}

}
