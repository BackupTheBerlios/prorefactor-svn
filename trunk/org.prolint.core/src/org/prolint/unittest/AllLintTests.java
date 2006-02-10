/* Created Mar 18, 2005
 * Authors: John Green
 *
 * Copyright (C) 2005 Prolint.org Contributors
 * This file is part of Prolint.
 *    Prolint is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *    Prolint is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *    You should have received a copy of the GNU Lesser General Public
 * License along with Prolint; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.prolint.unittest;


import junit.framework.Test;
import junit.framework.TestSuite;


/** Launch all Prolint unit tests */
public class AllLintTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("All Lint tests");
		//$JUnit-BEGIN$
		suite.addTest(new TestSuite(AbbrevNamesT.class));
		suite.addTest(new TestSuite(NoEffectT.class));
		//$JUnit-END$
		return suite;
	}

}
