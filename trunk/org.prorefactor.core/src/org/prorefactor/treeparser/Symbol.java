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

import java.util.Comparator;

import org.prorefactor.core.JPNode;
import org.prorefactor.core.TokenTypes;



/** Base class for any type of symbol which needs to be
 * kept track of when parsing a 4gl compile unit's AST.
 */
abstract public class Symbol {
	
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
	
	/** Comparator for sorting by name. */
	public static final Comparator NAME_ORDER = new Comparator() {
		public int compare(Object o1, Object o2) {
			Symbol s1 = (Symbol) o1;
			Symbol s2 = (Symbol) o2;
			return s1.getName().compareToIgnoreCase(s2.getName());
		}
	};

	
	
	/** Get the "full" name for this symbol. This is expected to be overridden
	 * in subclasses. For example, we might expect "database.buffer.field" to
	 * be the return for a field buffer.
	 */
	public abstract String fullName();
	
	

	public int getAllRefsCount() { return allRefsCount; }

	public int getNumReads() { return numReads; }
	
	public int getNumWrites() { return numWrites; }


	
	/** If this was defined AS something, then we have an AS node */
	public JPNode getAsNode() { return asNode; }

	
	
	/** If this symbol was defined directly by a DEFINE syntax,
	 * then this returns the DEFINE node, otherwise null.
	 */
	public JPNode getDefineNode() {
		if (defNode!=null && defNode.getType()==TokenTypes.DEFINE) return defNode;
		return null;
	}
	


	/** If this symbol was defined with syntax other than a direct DEFINE,
	 * then this returns the ID node, otherwise null.
	 */
	public JPNode getIndirectDefineIdNode() {
		if (defNode!=null && defNode.getType()==TokenTypes.ID) return defNode;
		return null;
	}

	
	
	/** If this was defined LIKE something, then we have a LIKE node */
	public JPNode getLikeNode() { return likeNode; }

	public String getName() { return name; }

	
	
	/** From TokenTypes: VARIABLE, FRAME, MENU, MENU-ITEM, etc.
	 * A TableBuffer object always returns BUFFER, regardless of whether
	 * the object is a named buffer or a default buffer.
	 * @see org.prorefactor.treeparser.TableBuffer#getProgressType().
	 * A FieldBuffer object always returns FIELD.
	 */
	public int getProgressType() {
		// If there is no DEFINE node, then we assume inline VARIABLE def,
		// as in "message..update x as char"
		if (	defNode == null
			||	defNode.getType() != TokenTypes.DEFINE
			) return TokenTypes.VARIABLE;
		JPNode n = defNode.firstChild();
		int type = n.getType();
		if (type==TokenTypes.NEW || type==TokenTypes.SHARED) {
			type = n.nextSibling().getType();
		}
		return type;
	}
	
	
	
	public SymbolScope getScope() { return scope; }


	
	/** Defined as NEW [GLOBAL] SHARED? */
	public boolean isExported() {
		// If there is no DEFINE node (inline var def), then it is not NEW..SHARED.
		if (	defNode == null
			||	defNode.getType() != TokenTypes.DEFINE
			) return false;
		if (defNode.firstChild().getType() == TokenTypes.NEW) return true;
		return false;
	}

	
	
	/** Defined as SHARED? */
	public boolean isImported() { 
		// If there is no DEFINE node (inline var def), then it is not SHARED.
		if (	defNode == null
			||	defNode.getType() != TokenTypes.DEFINE
			) return false;
		if (defNode.firstChild().getType() == TokenTypes.SHARED) return true;
		return false;
	}
	
	
	
	/** Take note of a symbol reference (read, write, reference by name). */
	public void noteReference(int contextQualifier) {
		allRefsCount++;
		if (CQ.isRead(contextQualifier)) numReads++;
		if (CQ.isWrite(contextQualifier)) numWrites++;
	}
	
	
	
	/** @see #getAsNode() */
	public void setAsNode(JPNode asNode) { this.asNode = asNode; }

	
	
	/** We store the DEFINE node if available and sensible.
	 * If defined in a syntax where there is no DEFINE node briefly
	 * preceeding the ID node, then we store the ID node.
	 */
	public void setDefOrIdNode(JPNode node) { defNode = node; }



	/** @see #getLikeNode() */
	public void setLikeNode(JPNode likeNode) { this.likeNode = likeNode; }



	public void setName(String name) { this.name = name; }



}
