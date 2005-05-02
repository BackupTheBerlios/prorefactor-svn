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
import org.prorefactor.treeparser.Block;


public class BlockNode extends JPNode {

	public BlockNode(int handle) { super(handle); }
	public BlockNode(int handle, TreeConfig config) { super(handle, config); }
	public BlockNode(int file, int line, int column) { super(file, line, column); }

	public Block getBlock() {
		Block block = (Block) getLink(JPNode.BLOCK);
		assert block != null;
		return block;
	}
	public void setBlock(Block block) {
		setLink(JPNode.BLOCK, block);
	}
	
}
