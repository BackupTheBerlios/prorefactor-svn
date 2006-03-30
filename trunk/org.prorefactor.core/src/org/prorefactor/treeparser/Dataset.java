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

import org.prorefactor.core.TokenTypes;


/** A Symbol defined with DEFINE DATASET. */
public class Dataset extends Symbol {

	public Dataset(String name, SymbolScope scope) {
		super(scope);
		setName(name);
	}

	@Override
	public Symbol copyBare(SymbolScope scope) {
		return new Dataset(getName(), scope);
	}

	/** For this subclass of Symbol, fullName() returns the same value as getName(). */
	public String fullName() { return getName(); }

	/** Returns TokenTypes.DATASET.
	 * @see org.prorefactor.treeparser.Symbol#getProgressType()
	 */
	public int getProgressType() {
		return TokenTypes.DATASET;
	}

}
