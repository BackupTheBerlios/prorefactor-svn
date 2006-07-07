/* ResourceUtil.java
 * Created on Dec 6, 2003
 * John Green
 *
 * Copyright (C) 2003 Joanju Limited
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.prorefactor.eclipse;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.prorefactor.refactor.FileStuff;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.refactor.messages.Message;



/** Utilities for dealing with resources, especially for helping
 * deal with the fact that org.prorefactor.refactor operates directly on the
 * filesystem, but Eclipse works with IResource objects.
 */
public class ResourceUtil {



	/** Clear all "org.prorefactor.markers.refactor" markers.
	 * This should be done before each new refactoring which creates those.
	 */
	public static void clearRefactorMarkers() {
		WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
			protected void execute(IProgressMonitor monitor) throws CoreException {
				Plugin.getWorkspace().getRoot().deleteMarkers(
					"org.prorefactor.markers.refactor"
					, true
					, IResource.DEPTH_INFINITE
					);
			} 
		};
		try {
			op.run(null);
		} catch (Exception e) {}
	} // clearRefactorMarkers


	/** Attempt to find an appropriate IFile for a file fullpath.
	 * Since more than one linked resource can point at any one file
	 * on the OS filesystem, any number of IFile matches are possible.
	 * This attempts to find an IFile, in the following order:
	 * 1. Unique "simple" (not linked) IFile in the workspace.
	 * 2. First in current ProRefactor project.
	 * 3. First in workspace.
	 * @return null if there is no IFile representation.
	 */
	public static IFile getIFile(String fullPath) {
		Path path = new Path(fullPath);
		IFile ifile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
		/* If we found a "simple" IFile, we're done and we don't need to
		 * widen the search to include linked resources.
		 */ 
		if (ifile!=null) return ifile;
		
		try {
			IWorkspaceRoot workspaceRoot = null;
			workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
			String projectName = RefactorSession.getInstance().getProjectName();
			for (IFile i : workspaceRoot.findFilesForLocation(path)) {
				if (i.getProject().getName().equalsIgnoreCase(projectName)) {
					return i;
				}
				if (ifile==null) ifile = i;
			}
		} catch (IllegalStateException e) {
			// The workspace is closed, and can't be searched.
		}
		return ifile;
	}
	
	
	/** Find an IFile in the workspace for any relative or qualified file path.
	 * Returns null if the file or resource is not found.
	 */
	public static IFile getIFileRelaxed(String filename) {
		File file = FileStuff.findFile(filename);
		if (file==null) return null;
		return getIFile((FileStuff.fullpath(file)));
	}

	
	
	/** For an ArrayList of org.prorefactor.refactor.messages.Message objects,
	 * create markers of type "org.prorefactor.markers.refactor".
	 */
	public static void messagesToMarkers(final ArrayList messages) {
		WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
			@Override
			protected void execute(IProgressMonitor monitor) {
				for (Iterator it = messages.iterator(); it.hasNext();) {
					try {
						Message message = (Message) it.next();
						IFile file = getIFile(message.file.getCanonicalPath());
						if (file==null) continue;
						IMarker marker = file.createMarker("org.prorefactor.markers.refactor");
						marker.setAttribute(IMarker.LINE_NUMBER, message.line);
						marker.setAttribute(IMarker.MESSAGE, message.message);
					} catch (Exception e) {}
				}
			}
		};
		try {
			op.run(null);
		} catch (Exception e) {}
	} // messagesToMarkers



	/** Tries to find an IFile for a String pathname, and calls
	 * saveLocalHistory(IFile) if found.
	 * @see #saveLocalHistory(IFile)
	 * @param fullpath Full path name to file.
	 */
	public static void saveLocalHistory(String fullpath) {
		IFile theFile = getIFile(fullpath);
		if (theFile!=null) {
			saveLocalHistory(theFile);
		}
	}
	/** Hack to force Eclipse to create a "Local History" for a file.
	 * Accomplishes this by doing an append of zero bytes to the IFile.
	 * <p>
	 * A more "proper" way to do this might be: Allow overrides for how
	 * org.prorefactor.refactor does file delete/new/overwrite/append, passing an
	 * InputStream as the callback arg. That way, from Eclipse/ProRefactor,
	 * we can have a "write" in org.prorefactor.refactor do a callback to ProRefactor,
	 * and ProRefactor would then use Eclipse methods for writing to the file,
	 * which would properly trigger "local history" and other Eclipse things.
	 * This would make org.prorefactor.refactor nicely extensible for other workbenches
	 * as well. The "default" behaviour, if the workbench did not override
	 * this, would just be to write directly to the OS using java.io.
	 * </p> 
	 */
	public static void saveLocalHistory(IFile theFile) {
		InputStream theStream = new ByteArrayInputStream(new byte [0]);
		try {
			theFile.appendContents(theStream, false, true, null);
		} catch (Exception e) {}
	} // saveLocalHistory(IFile)



} // class ResourceUtil
