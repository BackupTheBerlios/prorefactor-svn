/**
 * JPUtilT.java
 * @author John Green
 * 20-Oct-2002
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


import org.prorefactor.core.JPUtil;

import com.joanju.ProparseLdr;



public class JPUtilT extends UnitTestBase2 {

	public JPUtilT(String arg0) {
		super(arg0);
		loadParser();
		plus1 = JPUtil.getInstance();
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(JPUtilT.class);
	}

	private JPUtil plus1;

	private void loadParser() {
		try {
			parser = ProparseLdr.getInstance();
		} catch (Throwable e) {
			fail("Failed to load proparse.dll");
		}
	}

	private void parserErrCheck() {
		if (parser.errorGetStatus() != 0)
			fail(parser.errorGetText());
	}



	/**
	 * Test findFirstDescendant()
	 */
	public void testFindFirstDescendant() {
		parser.parse("data/hello.p");
		parserErrCheck();
		int topNode = parser.getHandle();
		parser.nodeTop(topNode);
		int stringNode = parser.getHandle();
		if (plus1.findFirstDescendant(topNode, stringNode, "QSTRING") < 1)
			fail("findFirstDescendant failed");
		if (! parser.getNodeType(stringNode).equals("QSTRING"))
			fail("findFirstDescendant: did not get a QSTRING node");
	} // testFindFirstDescendant()



	/**
	 * Test firstNaturalChild()
	 */
	public void testFirstNaturalChild() {
		parser.parse("data/empty.p");
		parserErrCheck();
		int theHandle = parser.getHandle();
		parser.nodeTop(theHandle);
		if (plus1.firstNaturalChild(theHandle, theHandle) != 0)
			fail("testFirstNaturalChild: empty.p is supposed to be empty!");
		parser.parse("data/hello.p");
		parserErrCheck();
		theHandle = parser.getHandle();
		parser.nodeTop(theHandle);
		if (plus1.firstNaturalChild(theHandle, theHandle) == 0)
			fail("firstNaturalChild: Did not find any node");
		String nodeType = parser.getNodeType(theHandle);
		if (! nodeType.equals("DO"))
			fail("testFirstNaturalChild: expected DO node in hello.p, got: " + nodeType);
	}


	/**
	 * Test lastChild()
	 */
	public void testLastChild() {
		parser.parse("data/hello.p");
		parserErrCheck();
		int theHandle = parser.getHandle();
		parser.nodeTop(theHandle);
		plus1.firstNaturalChild(theHandle, theHandle); // gets the DO node
		if (
			plus1.lastChild(theHandle, theHandle) == 0
			|| ! parser.getNodeType(theHandle).equals("PERIOD")
			|| ! parser.nodePrevSibling(theHandle, theHandle).equals("END")
			)
			fail(
				"testLastChild expected END PERIOD, got: "
				+ parser.getNodeType(theHandle)
				+ " " + parser.nodeNextSibling(theHandle, theHandle)
				);
	} // testLastChild()


} // class

