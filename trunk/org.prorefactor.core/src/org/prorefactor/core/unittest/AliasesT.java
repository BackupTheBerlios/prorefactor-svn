/**
 * AliasesT.java
 * @author John Green
 * 27-Jan-2003
 * www.joanju.com
 * 
 * Copyright (c) 2003 Joanju Limited.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 */

package org.prorefactor.core.unittest;


import java.io.IOException;

import org.prorefactor.core.schema.Schema;



public class AliasesT extends UnitTestBase2 {

	public AliasesT(String arg0) {
		super(arg0);
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(AliasesT.class);
	}


	public void test01() {
		Schema schema = Schema.getInstance();

		// Load the schema
		try {
			schema.loadSchema("proparse.schema");
		} catch (IOException e) {
			throw new Error(e.getMessage());
		}

		schema.aliasCreate("foo", "sports");

		parser.parse("data/aliases.p");
		assertEquals("", parser.errorGetText());

	} // test01()


} // class AliasesT
