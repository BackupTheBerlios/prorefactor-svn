/**
 * JPTreeParserT.java
 * @author John Green
 * 5-Nov-2002
 * www.joanju.com
 * 
 * Copyright (c) 2002 Joanju Limited.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 */

package org.prorefactor.core.unittest;


import org.prorefactor.treeparser.*;
import org.prorefactor.treeparserbase.*;



public class JPTreeParserT extends UnitTestBase2 {

	public JPTreeParserT(String arg0) {
		super(arg0);
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(JPTreeParserT.class);
	}



	public void testWrapperCall() {
		parser.parse("data/hello2.p");
		assertEquals("", parser.errorGetText());
		int topNode = parser.getHandle();
		parser.nodeTop(topNode);
		JPTreeParser tp = new JPTreeParser();
		String theReturn = TreeParserWrapper.run(tp, topNode);
		assertTrue(theReturn, theReturn.length()==0);
	} // testWrapperCall()



} // class JPTreeParserT
