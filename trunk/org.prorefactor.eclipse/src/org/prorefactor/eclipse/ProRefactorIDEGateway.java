/* Created on Mar 26, 2006
 * John Green
 *
 * Copyright (C) 2006 Joanju Software
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.prorefactor.eclipse;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.prorefactor.refactor.FileStuff;
import org.prorefactor.refactor.IDE;
import org.prorefactor.refactor.RefactorSession;


/** @see IDE */
public class ProRefactorIDEGateway implements IDE {


	/** Get the project relative path for a File object.
	 * <p>
	 * Returns an array of two strings.
	 * <p>
	 * If the file was found in the current RefactorSession getProjectName() project,
	 * then that project name and the IFile relative path are returned.
	 * <p>
	 * If the file was found in <b>any</b> project, then the <b>first matching</b> project name and
	 * the IFile relative path are returned.
	 * <p>
	 * Otherwise, the project name returned is null, and File.getPath() is returned.
	 * @see IDE#getProjectRelativePath(File)
	 */
	public String [] getProjectRelativePath(File file) {
		String [] ret = new String[2];
		IWorkspaceRoot workspaceRoot = null;
		IFile ifile = null;
		try {
			workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
			String projectName = RefactorSession.getInstance().getProjectName();
			for (IFile i : workspaceRoot.findFilesForLocation(Path.fromOSString(FileStuff.fullpath(file)))) {
				if (i.getProject().getName().equalsIgnoreCase(projectName)) {
					ifile = i;
					break;
				}
				if (ifile==null) ifile = i;
			}
		} catch (IllegalStateException e) {
			// The workspace is closed, and can't be searched.
		}
		if (ifile!=null) {
			ret[0] = ifile.getProject().getName();
			ret[1] = ifile.getProjectRelativePath().toString();
		} else {
			ret[0] = null;
			ret[1] = file.getPath();
		}
		return ret;
	}

	
}
