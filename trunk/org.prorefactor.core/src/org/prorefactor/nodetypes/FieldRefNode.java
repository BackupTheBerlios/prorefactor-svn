/* Created on Apr 29, 2005
 * Authors: John Green
 * 
 * Copyright (c) 2005 Joanju (www.joanju.com)
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.prorefactor.nodetypes;

import org.prorefactor.core.JPNode;
import org.prorefactor.core.TokenTypes;
import org.prorefactor.treeparser.BufferScope;
import org.prorefactor.treeparser.DataType;
import org.prorefactor.treeparser.FieldBuffer;
import org.prorefactor.treeparser.Primative;
import org.prorefactor.treeparser.Symbol;
import org.prorefactor.treeparser.Variable;
import org.prorefactor.widgettypes.FieldLevelWidgetI;


public class FieldRefNode extends JPNode {

	/** For creating from persistent storage */
	public FieldRefNode() { super(); }

	public FieldRefNode(int handle, TreeConfig config) { super(handle, config); }
	public FieldRefNode(int file, int line, int column) { super(file, line, column); }

	public BufferScope getBufferScope() {
		BufferScope bufferScope = (BufferScope) getLink(JPNode.BUFFERSCOPE);
		assert bufferScope!=null;
		return bufferScope;
	}
	
	public String getClassName() { return ((Primative)getSymbol()).getClassName(); }
	public DataType getDataType() { return ((Primative)getSymbol()).getDataType(); }

	/** We very often need to reference the ID node for a Field_ref node.
	 * The Field_ref node is a synthetic node - it doesn't have any text.
	 * If we want the field/variable name, or the file/line/column, then
	 * we probably want to get those from the ID node.
	 */
	public JPNode getIdNode() {
		JPNode idNode = findDirectChild(TokenTypes.ID);
		assert idNode != null;
		return idNode;
	}
	
	/** Every JPNode subtype has its own index. Used for persistent storage. */
	@Override
	public int getSubtypeIndex() { return 3; }

	/** Get the Symbol for a Field_ref node.
	 * @return Always returns one of two Symbol types: Variable or FieldBuffer.
	 */
	@Override
	public Symbol getSymbol() {
		Symbol symbol = (Symbol) getLink(JPNode.SYMBOL);
		assert symbol != null;
		return symbol;
	}
	
	public void setBufferScope(BufferScope bufferScope) {
		assert bufferScope!=null;
		setLink(JPNode.BUFFERSCOPE, bufferScope);
	}
	
	public void setSymbol(FieldBuffer symbol) {
		assert symbol!=null;
		setLink(JPNode.SYMBOL, symbol);
	}
	public void setSymbol(FieldLevelWidgetI symbol) {
		assert symbol!=null;
		setLink(JPNode.SYMBOL, symbol);
	}
	public void setSymbol(Variable symbol) {
		assert symbol!=null;
		setLink(JPNode.SYMBOL, symbol);
	}

}
