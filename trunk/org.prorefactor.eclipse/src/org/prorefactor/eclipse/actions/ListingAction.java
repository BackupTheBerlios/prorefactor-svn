/* ListingAction.java
 * Created on Nov 28, 2003
 * John Green
 *
 * Copyright (C) 2003 Joanju Limited
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.prorefactor.eclipse.actions;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.prorefactor.core.ICallback;

import com.joanju.ProparseLdr;


public class ListingAction 
	implements IViewActionDelegate, IWorkbenchWindowActionDelegate, IProRefactorAction {

	public ListingAction() {}

	protected ActionManager manager = null;
	protected ISelection currISelection = null;
	protected IWorkbenchWindow window;
	protected ProparseLdr parser = ProparseLdr.getInstance();



	private ICallback createPreParseCallback() {
		return new ICallback() {
			public Object run(Object obj) {
				IResource res = (IResource) obj;
				IPath resPath = res.getLocation().removeFileExtension().addFileExtension("listing");
				parser.configSet("listing-file", resPath.toOSString());
				return null;
			}
		};
	}


	public RefactorResult processFile(int topNode) {
		RefactorResult ret = new RefactorResult();
		// We refresh the parent folder with the new/changed listing file.
		try {
			manager.getCurrResource().getParent().refreshLocal(IResource.DEPTH_ONE, null);
		} catch (CoreException e) {}
		return ret;
	}


	public String processTargetSet() {
		return null;
	}


	public void run(IAction action) {
		// manager.setup *first*, because it deals with errors from loading parser, etc.
		manager = new ActionManager(window, this);
		if (manager.setup(currISelection) < 1) return;
		try {
			manager.preParse = createPreParseCallback();
			manager.doRun();
		} finally {
			parser.configSet("listing-file", "");
		}
	}




	//// Interface Methods ////

	public void dispose() {}

	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
	public void init(IViewPart view) {
		this.window = view.getSite().getWorkbenchWindow();
	}

	public void selectionChanged(IAction action, ISelection iselection) {
		if (iselection instanceof IStructuredSelection) currISelection = iselection;
	}

}
