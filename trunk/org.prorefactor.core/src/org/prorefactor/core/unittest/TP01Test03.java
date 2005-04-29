/**
 * Authors: John Green
 * April, 2005.
 * 
 * Copyright (c) 2005 Joanju (www.joanju.com).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.prorefactor.core.unittest;

import org.prorefactor.core.schema.Schema;
import org.prorefactor.treeparser.TreeParserWrapper;
import org.prorefactor.treeparser01.TreeParser01;


/** This class simply runs the tree parser through various code,
 * and as long as the tree parser does not throw any errors, then
 * the tests pass.
 */
public class TP01Test03 extends UnitTestBase2 {

	public TP01Test03(String arg0) { super(arg0); }

	String inName = "data/tp01tests/test03.p";
	String schemaName = "proparse.schema";

	public static void main(String[] args) {
		junit.textui.TestRunner.run(TP01Test03.class);
	}

	public void test01() throws Exception {
		Schema schema = Schema.getInstance();
		schema.clear();
		schema.loadSchema(schemaName);
		TreeParserWrapper.run(inName, new TreeParser01());
	}

}
