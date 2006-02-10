/* Created Apr 18, 2005
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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.prolint.core.FileStuff;
import org.prolint.core.LintRun;
import org.prolint.core.StreamUtil;
import org.prolint.eclipse.MarkerUtil;
import org.prorefactor.core.unittest.UnitTestBase2;


/** Rule "noeffect" unit test. */
public class NoEffectT extends UnitTestBase2 {

	public NoEffectT(String arg0) {
		super(arg0);
	}

	public static void main(String[] args) { }
	
	public void test01() throws Exception {

		final String projectName = "junit_s2k";
		final String directoryName = "simpleunit";
		final String cuName = "simpleunit/noeffect.p";
		final String dataFileName = "testdata/" + cuName;
		final String expectFileName = "testdata/simpleunit/noeffect.expect";
		final String resultsFileName = "prolint/tmp/noeffecttest.txt";
		
		// Create the project in the runtime workspace.
		TestProject project = new TestProject(projectName);
		project.createDirectory(directoryName);
		project.createFile(dataFileName, cuName);

		// Find the File to lint.
		File sourceFile = FileStuff.findFile(cuName);
		assertNotNull("Unit test configuration error", sourceFile);

		// Run the lint.
		LintRun lintRun = new LintRun();
		lintRun.lint(sourceFile, cuName);
		lintRun.writeAllMarkers();

		/* Report on the markers, compare to expected:
		 * 1. Get the Resource.
		 * 2. Set up a scratch file for reporting to.
		 * 3. Report all Prolint markers in the resource.
		 * 4. Close the output.
		 * 5. Read the "expect" file on the classpath.
		 * 6. Assert no differences between output and expected.
		 */
		IResource resource = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName).getFile(cuName);
		File outFile = new File(resultsFileName);
		outFile.getParentFile().mkdirs();
		BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
		MarkerUtil.writeMarkers(resource, writer);
		writer.close();
		InputStream expectStream = NoEffectT.class.getClassLoader().getResourceAsStream(expectFileName);
		assertEquals("Difference at line", 0, StreamUtil.lineOfDiff(expectStream, new FileInputStream(outFile)));
		
	}

}
