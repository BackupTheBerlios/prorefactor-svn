/**
 * Block.java
 * @author John Green
 * Apr 20, 2004
 * www.joanju.com
 * 
 * Copyright (c) 2004 Joanju Limited.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 */

package org.prorefactor.treeparser;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.prorefactor.core.JPNode;
import org.prorefactor.core.TokenTypes;
import org.prorefactor.core.schema.Field;
import org.prorefactor.core.schema.Schema;



/** For keeping track of blocks, block attributes,
 * and the things that are scoped within those blocks -
 * especially buffer scopes.
 */
public class Block {

	/** For constructing nested blocks */
	public Block(Block parent, JPNode node) {
		this.blockStatementNode = node;
		this.parent = parent;
		this.symbolScope = parent.symbolScope;
	}

	/** For constructing a root (method root or program root) block.
	 * @param symbolScope
	 * @param node Is the Program_root if this is the program root block.
	 */
	public Block(SymbolScope symbolScope, JPNode node) {
		this.blockStatementNode = node;
		this.symbolScope = symbolScope;
		if (symbolScope.getParentScope()!=null)
			this.parent = symbolScope.getParentScope().getRootBlock();
		else
			this.parent = null; // is program-block
	}
	
	private static Schema schema = Schema.getInstance();

	private Block parent;
	private JPNode blockStatementNode;
	private Set bufferScopes = new HashSet();

	/** The SymbolScope for a block is going to be the root program scope,
	 * unless the block is inside a method (function/trigger/procedure).
	 */
	private SymbolScope symbolScope;





	/** Add a reference to a BufferScope to this and all outer blocks.
	 * These references are required for duplicating Progress's scope
	 * and "raise scope" behaviours.
	 * BufferScope references are not added up past the symbol's scope.
	 */
	public void addBufferScopeReferences(BufferScope bufferScope) {
		// References do not get added to DO blocks.
		if (blockStatementNode.getType() != TokenTypes.DO) bufferScopes.add(bufferScope);
		if (parent!=null && bufferScope.getSymbol().getScope().getRootBlock() != this) {
			parent.addBufferScopeReferences(bufferScope);
		}
	}
	
	
	
	/** A "hidden cursor" is a BufferScope which has no side-effects on surrounding
	 * blocks like strong, weak, and reference scopes do. These are used within
	 * a CAN-FIND function. (2004.Sep:John: Maybe in triggers too? Haven't checked.)
	 * @param node The RECORD_NAME node. Must have the BufferSymbol linked to it already.
	 */
	public void addHiddenCursor(JPNode node) {
		TableBuffer symbol = (TableBuffer) node.getLink(JPNode.SYMBOL);
		assert symbol != null;
		BufferScope buff = new BufferScope(this, symbol, BufferScope.HIDDEN_CURSOR);
		bufferScopes.add(buff);
		// Note the difference compared to addStrong and addWeak - we don't add
		// BufferScope references to the enclosing blocks.
		node.setLink(JPNode.BUFFERSCOPE, buff);
	}



	/** Create a "strong" buffer scope.
	 * This is called within a DO FOR or REPEAT FOR statement.
	 * A STRONG scope prevents the scope from being raised to an enclosing block.
	 * Note that the compiler performs additional checks here that we don't.
	 * @param node The RECORD_NAME node. It must already have
	 * the BufferSymbol linked to it.
	 */
	public void addStrongBufferScope(JPNode node) {
		TableBuffer symbol = (TableBuffer) node.getLink(JPNode.SYMBOL);
		assert symbol != null;
		BufferScope buff = new BufferScope(this, symbol, BufferScope.STRONG);
		bufferScopes.add(buff);
		addBufferScopeReferences(buff);
		node.setLink(JPNode.BUFFERSCOPE, buff);
	} // addStrongBufferScope



	/** Create a "weak" buffer scope.
	 * This is called within a FOR or PRESELECT statement.
	 * @param node The RECORD_NAME node. It must already have
	 * the BufferSymbol linked to it.
	 */
	public BufferScope addWeakBufferScope(TableBuffer symbol) {
		BufferScope buff = getBufferScope(symbol, BufferScope.WEAK);
		if (buff==null) buff = new BufferScope(this, symbol, BufferScope.WEAK);
		// Yes, add reference to outer blocks, even if we got this buffer from
		// an outer block. Might have blocks in between which need the reference
		// to be added.
		addBufferScopeReferences(buff);
		bufferScopes.add(buff); // necessary in case this is DO..PRESELECT block
		return buff;
	} // addWeakBufferScope



	/** Can a buffer reference be scoped to this block? */
	private boolean canScopeBufferReference(TableBuffer symbol) {
		// REPEAT, FOR, and Program_root blocks can scope a buffer.
		switch (blockStatementNode.getType()) {
			case TokenTypes.REPEAT :
			case TokenTypes.FOR :
			case TokenTypes.Program_root :
				return true;
		}
		// If this is the root block for the buffer's symbol, then the scope
		// cannot be any higher.
		if (symbol.getScope().getRootBlock() == this) return true;
		return false;
	} // canScopeBufferReference



	/** Find nearest BufferScope for a BufferSymbol, if any */
	private BufferScope findBufferScope(TableBuffer symbol) {
		for (Iterator it = bufferScopes.iterator(); it.hasNext();) {
			BufferScope buff = (BufferScope) it.next();
			if (buff.getSymbol() != symbol) continue;
			if (buff.getBlock() == this) return buff;
		}
		if (parent!=null && symbol.getScope().getRootBlock() != this)
			return parent.findBufferScope(symbol);
		return null;
	}



	/** Get the buffers that are scoped to this block */
	public TableBuffer [] getBlockBuffers() {
		// We can't just return bufferScopes, because it also contains
		// references to BufferScope objects which are scoped to child blocks.
		HashSet set = new HashSet();
		for (Iterator it = bufferScopes.iterator(); it.hasNext();) {
			BufferScope buff = (BufferScope) it.next();
			if (buff.getBlock() == this) set.add(buff.getSymbol());
		}
		return (TableBuffer[]) set.toArray(new TableBuffer[set.size()]);
	} // getBlockBuffers



	/** Find or create a buffer for the input BufferSymbol */
	public BufferScope getBufferForReference(TableBuffer symbol) {
		BufferScope buffer = getBufferScope(symbol, BufferScope.REFERENCE);
		if (buffer==null) buffer = getBufferForReferenceSub(symbol);
		// Yes, add reference to outer blocks, even if we got this buffer from
		// an outer block. Might have blocks in between which need the reference
		// to be added.
		addBufferScopeReferences(buffer);
		return buffer;
	} // getBufferForReference
	private BufferScope getBufferForReferenceSub(TableBuffer symbol) {
		if (! canScopeBufferReference(symbol))
			return parent.getBufferForReferenceSub(symbol);
		return new BufferScope(this, symbol, BufferScope.REFERENCE);
	}



	/** Attempt to get or raise a BufferScope in this block. */
	private BufferScope getBufferScope(TableBuffer symbol, int creating) {
		// First try to find an existing buffer scope for this symbol.
		BufferScope buff = findBufferScope(symbol);
		if (buff!=null) return buff;
		return getBufferScopeSub(symbol, creating);
	}
	private BufferScope getBufferScopeSub(TableBuffer symbol, int creating) {
		// First try to get a buffer from outermost blocks.
		if (parent!=null && symbol.getScope().getRootBlock() != this) {
			BufferScope buff = parent.getBufferScopeSub(symbol, creating);
			if (buff!=null) return buff;
		}
		BufferScope raiseBuff = null;
		for (Iterator it = bufferScopes.iterator(); it.hasNext();) {
			BufferScope buff = (BufferScope) it.next();
			if (buff.getSymbol() != symbol) continue;
			// Note that if it was scoped to this (or an outer block), then
			// we would have already found it with findBufferScope.
			// If it's strong scoped (to a child block, or we would have found it already),
			// then we can't raise the scope to here.
			if (buff.isStrong()) return null;
			if (	creating == BufferScope.REFERENCE
				||	buff.getStrength() == BufferScope.REFERENCE ) {
				raiseBuff = buff;
			}
		}
		if (raiseBuff==null) return null;
		// Can this block scope a reference to this buffer symbol?
		if (! canScopeBufferReference(symbol)) return null;
		// We are creating, or there exists, more than one sub-BufferScope, and at least
		// one is a REFERENCE. We raise the BufferScope to this block.
		for (Iterator it = bufferScopes.iterator(); it.hasNext();) {
			BufferScope buff = (BufferScope) it.next();
			if (buff.getSymbol() != symbol) continue;
			buff.setBlock(this);
			buff.setStrength(BufferScope.REFERENCE);
		}
		return raiseBuff;
	} // getBufferScopeSub



	/** Get the node for this block. Returns a node of one of these types:
	 * Program_root/DO/FOR/REPEAT/EDITING/PROCEDURE/FUNCTION/ON/TRIGGERS.
	 */
	public JPNode getNode() { return blockStatementNode; }



	public Block getParent() { return parent; }



	/** Is a buffer scoped to this or any parent of this block. */
	public boolean isBufferLocal(BufferScope buff) {
		for (Block block = this; block.parent!=null; block = block.parent) {
			if (buff.getBlock()==block) return true;
		}
		return false;
	}



	/** A method-block is a block for a function/trigger/internal-procedure. */
	public boolean isMethodBlock() {
		return (	symbolScope.getRootBlock() == this
				&&	symbolScope.getParentScope() != null );
	}



	/** The program-block is the outer program block (not internal procedure block) */
	public boolean isProgramBlock() {
		return (	symbolScope.getRootBlock() == this
				&&	symbolScope.getParentScope() == null );
	}



	/** A root-block is the root block for any SymbolScope
	 * whether program, function, trigger, or internal procedure.
	 */
	public boolean isRootBlock() {
		return symbolScope.getRootBlock() == this;
	}



	/** General lookup for Field or Variable.
	 * Does not guarantee uniqueness. That job is left to the compiler.
	 */
	public FieldLookupResult lookupField(String name, boolean getBufferScope) {
		FieldLookupResult result = new FieldLookupResult();
		TableBuffer tableBuff;
		int lastDot = name.lastIndexOf('.');
		// Variable or unqualified field
		if (lastDot == -1) {
			// Variables come first.
			result.variable = symbolScope.lookupVariable(name);
			if (result.variable != null) return result;
			// Lookup unqualified field by buffers in nearest scopes.
			result = lookupUnqualifiedField(name);
			// Lookup unqualified field by any table.
			// The compiler expects the name to be unique
			// amongst all schema and temp/work tables. We don't check for
			// uniqueness, we just take the first we find.
			if (result==null) {
				Field field;
				result = new FieldLookupResult();
				field = symbolScope.getRootScope().lookupUnqualifiedField(name);
				if (field!=null) {
					tableBuff = 
						symbolScope.getRootScope().getLocalTableBuffer(field.getTable());
				} else {
					field = schema.lookupUnqualifiedField(name);
					if (field==null) return null;
					tableBuff = symbolScope.getUnnamedBuffer(field.getTable());
				}
				result.field = tableBuff.getFieldBuffer(field);
			}
			result.isUnqualified = true;
			if (name.length() < result.field.getName().length()) result.isAbbreviated = true;
		} else {  // Qualified Field Name
			String fieldPart = name.substring(lastDot + 1);
			String tablePart = name.substring(0, lastDot);
			tableBuff = symbolScope.getBufferSymbol(tablePart);
			if (tableBuff==null) return null;
			Field field = tableBuff.getTable().lookupField(fieldPart);
			if (field==null) return null;
			result.field = tableBuff.getFieldBuffer(field);
			if (fieldPart.length() < result.field.getName().length()) result.isAbbreviated = true;
			// Temp/work/buffer names can't be abbreviated, but direct refs to schema can be.
			if (tableBuff.isDefaultSchema()) {
				String [] parts = tablePart.split("\\.");
				String tblPart = parts[parts.length-1];
				if (tblPart.length() < tableBuff.getTable().getName().length())
					result.isAbbreviated = true;
			}
		} // if ... Qualified Field Name
		if (getBufferScope) {
			BufferScope buffScope = getBufferForReference(result.field.getBuffer());
			result.bufferScope = buffScope;
		}
		return result;
	} // lookupField()



	/** Find a field based on buffers which are referenced in nearest enclosing blocks.
	 * Note that the compiler enforces uniqueness here. We don't, we just find the first
	 * possible and return it.
	 */
	protected FieldLookupResult lookupUnqualifiedField(String name) {
		Map buffs = new HashMap();
		FieldLookupResult result = null;
		for (Iterator it = bufferScopes.iterator(); it.hasNext();) {
			BufferScope buff = (BufferScope) it.next();
			TableBuffer symbol = buff.getSymbol();
			if (buff.getBlock() == this) {
				buffs.put(symbol, buff);
				continue;
			}
			BufferScope buffSeen = (BufferScope) buffs.get(symbol);
			if (buffSeen!=null) {
				if (buffSeen.getBlock()==this) continue;
				if (buffSeen.isStrong()) continue;
			}
			buffs.put(symbol, buff);
		}
		for (Iterator it = buffs.values().iterator(); it.hasNext();) {
			BufferScope buffScope = (BufferScope) it.next();
			TableBuffer tableBuff = buffScope.getSymbol();
			// Check for strong scope preventing raise to this block.
			if (buffScope.isStrong() && !isBufferLocal(buffScope)) continue;
			// Weak scoped named buffers don't get raised for field references.
			if (buffScope.isWeak() && !isBufferLocal(buffScope) && !tableBuff.isDefault())
				continue;
			Field field = tableBuff.getTable().lookupField(name);
			if (field==null) continue;
			// The buffers aren't sorted, but "named" buffers and temp/work
			// tables take priority. Default buffers for schema take lower priority.
			// So, if we got a named buffer or work/temp table, we return with it.
			// Otherwise, we just hang on to the result until the loop is done.
			result = new FieldLookupResult();
			result.field = tableBuff.getFieldBuffer(field);
			if (!tableBuff.isDefaultSchema()) return result;
		}
		if (result!=null) return result;
		// Resolving names is done by looking at inner blocks first, then outer blocks.
		if (parent!=null) return parent.lookupUnqualifiedField(name);
		return null;
	} // lookupUnqualifiedField



	public void setParent(Block parent) { this.parent = parent; }



}
