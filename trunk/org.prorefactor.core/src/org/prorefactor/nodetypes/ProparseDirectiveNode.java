/* Created Apr, 2005
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

import com.joanju.ProparseLdr;


public class ProparseDirectiveNode extends JPNode {

	/** For creating from persistent storage */
	public ProparseDirectiveNode() { super(); }

	public ProparseDirectiveNode(int handle, TreeConfig config) { super(handle, config); }
	public ProparseDirectiveNode(int file, int line, int column) { super(file, line, column); }
	
	private String directiveText = "";

	/** Get the directive text. Might return empty, but should not return null. */
	public String getDirectiveText() {
		if (getHandle()!=0)
			return ProparseLdr.getInstance().attrGetS(getHandle(), "proparsedirective");
		else
			return directiveText;
	}

	/** Every JPNode subtype has its own index. Used for persistent storage. */
	public int getSubtypeIndex() { return 5; }

	public void setDirectiveText(String text) { directiveText = text; }
	
}
