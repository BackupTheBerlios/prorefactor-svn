/**
 * Authors: John Green
 * Feb 2, 2006.
 * 
 * Copyright (c) 2006 Joanju (www.joanju.com).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.prorefactor.core.unittest;

import org.prorefactor.core.JPNode;
import org.prorefactor.core.JPNodeLister;
import org.prorefactor.core.schema.Schema;
import org.prorefactor.treeparser.TreeParserWrapper;
import org.prorefactor.treeparser01.TreeParser01;
import org.prorefactor.treeparserbase.TokenTypesReader;


/** Test frame scopes and implicit field associations to frames.
 */
public class TP01FramesTest extends UnitTestBase2 {

	public TP01FramesTest(String arg0) { super(arg0); }
	
	private TokenTypesReader tokenTypesReader = new TokenTypesReader();

	String expectFileName = "data/tp01tests/frames.expect.txt";
	String inFileName = "data/tp01tests/frames.p";
	String outFileName = "data/tp01tests/frames.out.txt";
	String schemaName = "proparse.schema";

	public static void main(String[] args) {
		junit.textui.TestRunner.run(TP01FramesTest.class);
	}

	public void test01() throws Exception {
		Schema schema = Schema.getInstance();
		schema.clear();
		schema.loadSchema(schemaName);
		JPNode topNode = TreeParserWrapper.run(inFileName, new TreeParser01());
		JPNodeLister nodeLister = new TP01FramesTreeLister(topNode, outFileName, tokenTypesReader);
		nodeLister.print();

		String compareResult = parser.diff(expectFileName, outFileName);
		assertEquals("", compareResult);

	}

}
