/*
 * Created on Jan 21, 2005
 *
 * Authors: John Green
 *
 * Copyright (C) 2005 Prolint.org Contributors
 *
 * This file is part of Prolint.
 *
 * Prolint is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * Prolint is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Prolint; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.prolint.eclipse;

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.prolint.core.FileStuff;


/** This class is responsible for clearing Prolint problem markers
 * from the selected resources and the children of those resources.
 */
public class ClearMarkersAction implements IViewActionDelegate {

	private ISelection currISelection;
	

	/** Clear Prolint markers for a relative or full path name.
	 * Is recursive for directory names.
	 */
	public static void clear(String filename) {
		IFile ifile = FileStuff.getIFileRelaxed(filename);
		if (ifile==null) return;
		clear(ifile);
	}

	/** Clear Prolint markers for a given resource.
	 * Is recursive for directory names.
	 */
	public static void clear(IResource resource) {
		try {
			resource.deleteMarkers(Plugin.PROLINT_MARKER_ID, true, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			Plugin.errorDialog(e.getMessage());
			Plugin.log(e);
		}
	}

	public void run(IAction action) {
		if (currISelection==null) return;
		IStructuredSelection selection = (IStructuredSelection) currISelection;
		for (Iterator it = selection.iterator(); it.hasNext();) {
			Object next = it.next();
			if (!(next instanceof IResource)) continue;
			IResource resource = (IResource)next;
			clear(resource);
		}
	}


	public void selectionChanged(IAction action, ISelection iselection) {
		if (iselection instanceof IStructuredSelection) currISelection = iselection;
	}

	
	public void init(IViewPart view) {}

}
