/* Created Apr 4, 2005
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.refactor.settings.ProgressProjectSettings;
import org.prorefactor.refactor.settings.ProparseProjectSettings;


/** Provides methods for creating, populating, and destroying
 * a project containing test code.
 * Each Prolint test that requires "sample code" to run against
 * should create and destroy its own TestProject.
 * <p>
 * Much of the complexity in building test projects comes from
 * the fact that at "JUnit Plug-in Runtime", there may be no
 * easy place for us to copy the test project's files from; those
 * files and folders might only exist in the org.prolint.core jar
 * file! This class provides a method for copying a resource from
 * the class path into source files in the project directory.
 */
public class TestProject {

	/** Basic constructor.
	 * The creation of a new TestProject implies that any old instance
	 * of that project should be discarded. After all, it's just a
	 * test project, and previous test runs might have crashed without
	 * finishing their cleanup. This also saves us from needing a
	 * try/finally block in each test - we don't have to worry so much
	 * about cleaning up after each test run.
	 * <p>
	 * The project gets created in the default (workspace) directory.
	 * <p>
	 * Creating a TestProject sets up some defaults:
	 * 1. It copies ProRefactor settings files from resources on Prolint's classpath
	 * which must be in the directory: testdata/XXyourProjectNameXX.
	 * The files are: progress.properties, proparse.properties, proparse.schema
	 * 2. It overrides the project's PROPATH, setting it to the project directory.
	 * 3. It loads all of these project settings into Proparse and ProRefactor.
	 * <p>
	 * Be careful not to have two TestProject instances of the same name
	 * at the same time!
	 * @throws Exception
	 */
	public TestProject(String projectName) throws Exception {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		project = root.getProject(projectName);
		if (project!=null) project.delete(true, null);
		project = root.getProject(projectName);
		final IProjectDescription description = workspace.newProjectDescription(projectName);
		project.create(description, null);
		project.open(null);
		String settingResourceDir = "testdata/" + projectName + "/";
		// Copy progress.properties, set the propath.
		String progressSettingsFilename = RefactorSession.getProgressSettingsFilename(projectName);
		copyProjectSettings(settingResourceDir + "progress.properties", progressSettingsFilename);
		ProgressProjectSettings progressSettings = new ProgressProjectSettings(progressSettingsFilename);
		progressSettings.loadSettings();
		progressSettings.propath = project.getLocation().toOSString();
		progressSettings.saveSettings();
		// Copy proparse.properties, get the schema filename.
		String proparseSettingsFilename = RefactorSession.getProparseSettingsFilename(projectName);
		copyProjectSettings(settingResourceDir + "proparse.properties", proparseSettingsFilename);
		ProparseProjectSettings proparseSettings = new ProparseProjectSettings(proparseSettingsFilename, projectName);
		proparseSettings.loadSettings();
		String schemaFilename = proparseSettings.schemaFile;
		// Copy proparse.schema
		copyProjectSettings(settingResourceDir + "proparse.schema", schemaFilename);
		// Now load all of our project settings
		RefactorSession.getInstance().loadProjectForced(projectName);
	}

	private IProject project;
	
	private void copyProjectSettings(String resourceName, String fileName) throws IOException {
		URL url = getClass().getClassLoader().getResource(resourceName);
		if (url != null) {
			File file = new File(fileName);
			FileUtils.copyURLToFile(url, file);
		}
	}

	/** Create a directory in the project directory 
	 * @throws CoreException*/
	public void createDirectory(String name) throws CoreException {
		IFolder folder = project.getFolder(name);
		if (! folder.exists())
			folder.create(true, true, null);
	}
	
	/** Create a file in the project from an input stream.
	 * @throws CoreException
	 */
	public IFile createFile(InputStream stream, String name) throws CoreException {
		IFile file = project.getFile(name);
		file.create(stream, true, null);
		return file;
	}
	
	/** Create a file in the project from a resource in the org.prolint.core classpath.
	 * In other words, the original source file must be from the "src" directory.
	 * If you are creating a file from something that is not in the org.prolint.core
	 * classpath, then @see #createFile(InputStream, String).
	 * @param prolintPluginResource ex: "testdata/myDir/myTestFile.p"
	 * @param projectFileName ex: "myDir/myTest.p"
	 * @throws CoreException
	 */
	public IFile createFile(String prolintPluginResource, String projectFileName) throws CoreException {
		return createFile(
				getClass().getClassLoader().getResourceAsStream(prolintPluginResource)
				, projectFileName
				);
	}
	
	public void delete() throws CoreException {
		project.delete(true, null);
	}
	
	public IProject getProject() { return project; }
	
	
}
