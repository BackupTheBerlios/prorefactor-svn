/**
 * Symbol.java
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


import org.prorefactor.core.JPNode;
import org.prorefactor.core.TokenTypes;



/** Base class for any type of symbol which needs to be
 * kept track of when parsing a 4gl compile unit's AST.
 */
abstract public class Symbol implements SymbolI {
	
	Symbol(SymbolScope scope) {
		this.scope = scope;
		scope.addSymbol(this);
	}

	private int allRefsCount = 0;
	private int numReads = 0;
	private int numWrites = 0;
	private JPNode asNode;

	/** We store the DEFINE node if available and sensible.
	 * If defined in a syntax where there is no DEFINE node briefly
	 * preceeding the ID node, then we store the ID node.
	 * If this is a schema symbol, then this member is null.
	 */
	private JPNode defNode;

	private JPNode likeNode;

	/** What scope this symbol was defined in. */
	private SymbolScope scope;

	/** Stores the full name, original (mixed) case as in definition. */	
	private String name;
	
	/* (non-Javadoc)
	 * @see org.prorefactor.treeparser.SymbolI#fullName()
	 */
	public abstract String fullName();
	
	

	/* (non-Javadoc)
	 * @see org.prorefactor.treeparser.SymbolI#getAllRefsCount()
	 */
	public int getAllRefsCount() { return allRefsCount; }

	/* (non-Javadoc)
	 * @see org.prorefactor.treeparser.SymbolI#getNumReads()
	 */
	public int getNumReads() { return numReads; }
	
	/* (non-Javadoc)
	 * @see org.prorefactor.treeparser.SymbolI#getNumWrites()
	 */
	public int getNumWrites() { return numWrites; }


	
	/* (non-Javadoc)
	 * @see org.prorefactor.treeparser.SymbolI#getAsNode()
	 */
	public JPNode getAsNode() { return asNode; }

	
	
	/* (non-Javadoc)
	 * @see org.prorefactor.treeparser.SymbolI#getDefineNode()
	 */
	public JPNode getDefineNode() {
		if (defNode!=null && defNode.getType()==TokenTypes.DEFINE) return defNode;
		return null;
	}
	


	/* (non-Javadoc)
	 * @see org.prorefactor.treeparser.SymbolI#getIndirectDefineIdNode()
	 */
	public JPNode getIndirectDefineIdNode() {
		if (defNode!=null && defNode.getType()==TokenTypes.ID) return defNode;
		return null;
	}

	
	
	/* (non-Javadoc)
	 * @see org.prorefactor.treeparser.SymbolI#getLikeNode()
	 */
	public JPNode getLikeNode() { return likeNode; }

	/* (non-Javadoc)
	 * @see org.prorefactor.treeparser.SymbolI#getName()
	 */
	public String getName() { return name; }

	
	
	/* (non-Javadoc)
	 * @see org.prorefactor.treeparser.SymbolI#getProgressType()
	 */
	public abstract int getProgressType();
	
	
	/* (non-Javadoc)
	 * @see org.prorefactor.treeparser.SymbolI#getScope()
	 */
	public SymbolScope getScope() { return scope; }

	
	/* (non-Javadoc)
	 * @see org.prorefactor.treeparser.SymbolI#isExported()
	 */
	public boolean isExported() {
		// If there is no DEFINE node (inline var def), then it is not NEW..SHARED.
		if (	defNode == null
			||	defNode.getType() != TokenTypes.DEFINE
			) return false;
		if (defNode.firstChild().getType() == TokenTypes.NEW) return true;
		return false;
	}

	
	
	/* (non-Javadoc)
	 * @see org.prorefactor.treeparser.SymbolI#isImported()
	 */
	public boolean isImported() { 
		// If there is no DEFINE node (inline var def), then it is not SHARED.
		if (	defNode == null
			||	defNode.getType() != TokenTypes.DEFINE
			) return false;
		if (defNode.firstChild().getType() == TokenTypes.SHARED) return true;
		return false;
	}
	
	
	
	/* (non-Javadoc)
	 * @see org.prorefactor.treeparser.SymbolI#noteReference(int)
	 */
	public void noteReference(int contextQualifier) {
		allRefsCount++;
		if (CQ.isRead(contextQualifier)) numReads++;
		if (CQ.isWrite(contextQualifier)) numWrites++;
	}
	
	
	
	/* (non-Javadoc)
	 * @see org.prorefactor.treeparser.SymbolI#setAsNode(org.prorefactor.core.JPNode)
	 */
	public void setAsNode(JPNode asNode) { this.asNode = asNode; }

	
	
	/* (non-Javadoc)
	 * @see org.prorefactor.treeparser.SymbolI#setDefOrIdNode(org.prorefactor.core.JPNode)
	 */
	public void setDefOrIdNode(JPNode node) { defNode = node; }



	/* (non-Javadoc)
	 * @see org.prorefactor.treeparser.SymbolI#setLikeNode(org.prorefactor.core.JPNode)
	 */
	public void setLikeNode(JPNode likeNode) { this.likeNode = likeNode; }



	/* (non-Javadoc)
	 * @see org.prorefactor.treeparser.SymbolI#setName(java.lang.String)
	 */
	public void setName(String name) { this.name = name; }



}
