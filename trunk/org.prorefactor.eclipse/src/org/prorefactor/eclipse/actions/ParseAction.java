/* Created 2003
 * John Green
 *
 * Copyright (C) 2003 Joanju Limited
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.prorefactor.eclipse.actions;


import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.prorefactor.eclipse.Plugin;
import org.prorefactor.refactor.PUB;
import org.prorefactor.refactor.RefactorException;



public class ParseAction
	implements IViewActionDelegate, IWorkbenchWindowActionDelegate, IProRefactorAction {

	public ParseAction() { }

	private boolean errors;
	private ActionManager manager;
	private ISelection currISelection = null;
	private IWorkbenchWindow window;



	public void dispose() { }



	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
	public void init(IViewPart view) {
		this.window = view.getSite().getWorkbenchWindow();
	}



	protected String findPpr(IResource res) {
		if (res==null || res.getType()!=IResource.FILE) return null;
		IPath resPath = res.getLocation().removeFileExtension().addFileExtension("ppr");
		if (resPath.toFile().canRead()) return resPath.toOSString();
		return null;
	}



	public RefactorResult processFile(int notUsed) {
		manager.console(manager.getCurrFile().getName());
		RefactorResult ret = new RefactorResult();
		try {
			IFile iFile = (IFile) manager.getCurrResource();
			PUB pub = new PUB(iFile.getProjectRelativePath().toString(), manager.getCurrFile().getPath());
			boolean isCurrent = pub.loadTo(PUB.HEADER);
			if (isCurrent) {
				manager.console(" up to date");
				return ret;
			}
			manager.console(" parse");
			pub.build();
			manager.console(" OK");
			return ret;
		} catch (IOException e1) {
			ret.message = e1.getMessage();
			Plugin.log(e1);
			errors = true;
			return ret;
		} catch (RefactorException e) {
			ret.message = e.getMessage();
			Plugin.log(e);
			errors = true;
			return ret;
		}
	} // processFile



	public String processTargetSet() {
		return null;
	}

	public void run(IAction action) {
		// manager.setup *first*, because it deals with errors from loading parser, etc.
		manager = new ActionManager(window, this);
		if (manager.setup(currISelection) < 1) return;
		errors = false;
		manager.doTheParse = false;
		manager.doRun();
		if (errors) {
			manager.reportError("See the Console for messages.");
			return;
		}
	}

	public void selectionChanged(IAction action, ISelection iselection) {
		if (iselection instanceof IStructuredSelection) currISelection = iselection;
	}

}
