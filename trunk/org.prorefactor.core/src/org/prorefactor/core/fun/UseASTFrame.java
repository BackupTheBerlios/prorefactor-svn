/**
 * UseASTFrame.java
 * @author John Green
 * 23-Oct-2002
 * www.joanju.com
 * 
 * Copyright (c) 2002 Joanju Limited.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 */

package org.prorefactor.core.fun;


import org.prorefactor.core.*;

import com.joanju.*;

import antlr.debug.misc.ASTFrame;


public class UseASTFrame {

	public static void main(String[] args) {
		ProparseLdr parser = ProparseLdr.getInstance();
		String theFilename = "data/substitute.p";
		parser.parse(theFilename);
		int topNode = parser.getHandle();
		parser.nodeTop(topNode);
		JPNode theAST = JPNode.getTree(topNode);
		ASTFrame theFrame = new ASTFrame(theFilename, theAST);
		theFrame.setVisible(true);
	}


	public static void showFrame(int topNode) {
		JPNode theAST = JPNode.getTree(topNode);
		ASTFrame theFrame = new ASTFrame("ASTFrame", theAST);
		theFrame.setVisible(true);
	}


}
