/**
 * Authors: John Green
 * July 4, 2006.
 * 
 * Copyright (c) 2006 Joanju (www.joanju.com).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.prorefactor.core.unittest;

import java.io.File;

import junit.framework.TestCase;

import org.prorefactor.treeparser.ParseUnit;


/** Test bug fixes. */
public class BugFixTests extends TestCase {


	public void test01() throws Exception {
		File file = new File("data/bugsfixed/bug01.p");
		ParseUnit pu = new ParseUnit(file);
		pu.treeParser01();
		// No further tests needed. Passes if parses clean.
	}

}
