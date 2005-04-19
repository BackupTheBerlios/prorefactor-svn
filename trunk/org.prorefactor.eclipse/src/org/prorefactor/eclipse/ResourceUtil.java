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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.prorefactor.refactor.FileStuff;
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



	/** Find an IFile in the workspace that matches a full String pathname. */
	public static IFile getIFile(String fullPath) {
		Path temp = new Path(fullPath);
		if (! temp.toFile().exists() ) return null;
		IFile tempIFile = Plugin.getWorkspace().getRoot().getFileForLocation(temp);
		return tempIFile;
	}

	
	
	/** Find an IFile in the workspace for any relative or qualified file path.
	 * Returns null if the file or resource is not found.
	 */
	private static IFile getIFileRelaxed(String filename) {
		File file = FileStuff.findFile(filename);
		if (file==null) return null;
		return getIFile((FileStuff.fullpath(file)));
	}

	
	
	/** For an ArrayList of org.prorefactor.refactor.messages.Message objects,
	 * create markers of type "org.prorefactor.markers.refactor".
	 */
	public static void messagesToMarkers(final ArrayList messages) {
		WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
			protected void execute(IProgressMonitor monitor) {
				for (Iterator it = messages.iterator(); it.hasNext();) {
					try {
						Message message = (Message) it.next();
						IFile file = getIFile(message.file.getCanonicalPath());
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
