/* XMLAction.java
 * Created on Feb 6, 2006
 * John Green
 *
 * Copyright (C) 2006 Joanju Limited
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.prorefactor.eclipse.actions;

import java.io.IOException;

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
import org.prorefactor.core.TokenLister;

import com.joanju.ProparseLdr;

public class TokenListerAction
	implements IViewActionDelegate, IWorkbenchWindowActionDelegate, IProRefactorAction {

	private ISelection currISelection = null;
	private ActionManager manager = null;
	private IWorkbenchWindow window;
	private ProparseLdr parser = ProparseLdr.getInstance();


	public TokenListerAction() { }

	public void dispose() { }

	public void init(IWorkbenchWindow windowIn) {
		this.window = windowIn;
	}
	public void init(IViewPart view) {
		this.window = view.getSite().getWorkbenchWindow();
	}

	public RefactorResult processFile(int topNode) {
		RefactorResult ret = new RefactorResult();
		manager.console(" tokens ");
		IResource res = manager.getCurrResource();
		IPath resPath = res.getLocation().addFileExtension("tokens");
		TokenLister lister = new TokenLister(topNode, resPath.toOSString());
		lister.showLinenum = true;
		lister.showFilename = true;
		try {
			lister.print();
		} catch (IOException e1) {
			ret.exception = e1;
		}
		// We refresh the parent folder with the new/changed output file.
		try {
			res.getParent().refreshLocal(IResource.DEPTH_ONE, null);
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
		parser.configSet("show-proparse-directives", "true");
		manager.doRun();
		parser.configSet("show-proparse-directives", "");
	}

	public void selectionChanged(IAction action, ISelection iselection) {
		if (iselection instanceof IStructuredSelection) currISelection = iselection;
	}

}
