/**
 * ParseUnit.java
 * @author John Green
 * Aug 12, 2004
 * www.joanju.com
 *
 * Copyright (C) 2004 Joanju Limited.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.prorefactor.treeparser;

import org.prorefactor.core.JPNode;


/** Provides parse unit information, such as the symbol table and a reference to the AST.
 * TreeParser01 calls symbolUsage() in this class in order to build the symbol table.
 */
public class ParseUnit {

	public ParseUnit() {}
	public ParseUnit(JPNode topNode) {
		this.topNode = topNode;
	}

	private JPNode topNode;
	private SymbolScopeRoot rootScope;

	
	
	/** Get the syntax tree top (Program_root) node */
	public JPNode getTopNode() { return topNode; }

	public SymbolScopeRoot getRootScope() { return rootScope; }

	/** Set the syntax tree top (Program_root) node */
	public void setTopNode(JPNode topNode) { this.topNode = topNode; }

	public void setRootScope(SymbolScopeRoot rootScope) { this.rootScope = rootScope; }

	
}
