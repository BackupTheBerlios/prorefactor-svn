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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.prolint.core.FileStuff;
import org.prolint.core.LintRun;
import org.prolint.core.StreamUtil;
import org.prolint.eclipse.MarkerUtil;
import org.prorefactor.core.unittest.UnitTestBase2;


/** Test the Lint rule for abbreviated table/field names
 * within a JUnit plug-in runtime test workbench.
 */
public class AbbrevNamesT extends UnitTestBase2 {

	public AbbrevNamesT(String arg0) {
		super(arg0);
	}

	public static void main(String[] args) { }
	
	public void test01() throws Exception {
		
		String projectName = "junit_s2k";
		
		// Create the project in the runtime workspace
		TestProject project = new TestProject(projectName);
		project.createDirectory("abbrevnames");
		project.createFile("testdata/abbrevnames/billto.p", "abbrevnames/billto.p");
		project.createFile("testdata/abbrevnames/customer.p", "abbrevnames/customer.p");
		project.createFile("testdata/abbrevnames/displaypostal.i", "abbrevnames/displaypostal.i");

		// Prepare and check the list of files to lint
		String [] unitNames = {
				"abbrevnames/customer.p"
				, "abbrevnames/billto.p"
				};
		File [] files = new File[unitNames.length];
		for (int i = 0; i < unitNames.length; i++) {
			files[i] = FileStuff.findFile(unitNames[i]);
			assertNotNull("Unit test configuration error", files[i]);
		}

		// We lint everything twice, so that we can test that the old markers get cleared.
		for (int j = 0; j < 2; j++) {
			LintRun lintRun = new LintRun();
			for (int i = 0; i < files.length; i++) {
				lintRun.lint(files[i], unitNames[i]);
			}
			lintRun.writeAllMarkers();
		}

		/* Report on the markers, compare to expected:
		 * 1. Get the "abbrevnames" folder.
		 * 2. Set up a scratch file for reporting to.
		 * 3. Report all Prolint markers in abbrevnames folder.
		 * 4. Close the output.
		 * 5. Read the "expect" file on the classpath.
		 * 6. Assert no differences between output and expected.
		 */
		IFolder folder = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName).getFolder("abbrevnames");
		File outFile = new File("prolint/tmp/abbrevnamestest.txt");
		outFile.getParentFile().mkdirs();
		BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
		MarkerUtil.writeMarkers(folder, writer);
		writer.close();
		InputStream expectStream = AbbrevNamesT.class.getClassLoader().getResourceAsStream("testdata/abbrevnames/expect.txt");
		assertEquals("Difference at line", 0, StreamUtil.lineOfDiff(expectStream, new FileInputStream(outFile)));
		
	}

}
