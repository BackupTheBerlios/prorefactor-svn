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

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.prorefactor.eclipse.wizards.RefactorDirectoryPage;
import org.prorefactor.eclipse.wizards.RefactorWizard;
import org.prorefactor.refactor.noundo.NoundoWrap;



/**
 * Action for launching the NO-UNDO refactoring to selected compile units.
 */
public class NoUndoAction
	implements IWorkbenchWindowActionDelegate, IViewActionDelegate, IProRefactorAction {

	private ISelection currISelection = null;
	private ActionManager manager = null;
	private String outputDirectory = null;
	private IWorkbenchWindow window;

	public NoUndoAction() { }

	/**
	 * We can use this method to dispose of any system
	 * resources we previously allocated.
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	/** @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart) */
	public void init(IViewPart view) {
		this.window = view.getSite().getWorkbenchWindow();
	}
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	public RefactorResult processFile(int topNode) {
		manager.console(" no-undo ");
		RefactorResult ret = new RefactorResult();
		NoundoWrap wrap = new NoundoWrap(outputDirectory);
		wrap.overwrite = false;
		ret.message = wrap.run(topNode);
		return ret;
	}

	public String processTargetSet() {
		// Only used for interactive refactorings.
		return null;
	}

	public void run(IAction action) {
		// manager.setup *first*, because it deals with errors from loading parser, etc.
		manager = new ActionManager(window, this);
		if (manager.setup(currISelection) < 1) return;

		RefactorWizard wizard = new RefactorWizard();
		RefactorDirectoryPage dirPage = new RefactorDirectoryPage("dirpage");
		dirPage.setTitle("NO-UNDO Refactoring");
		wizard.addPage(dirPage);
		new WizardDialog(window.getShell(), wizard).open();
		if (!wizard.didFinish()) return;
		outputDirectory = dirPage.getSelectedPath();
		
		manager.doRun();
	}

	public void selectionChanged(IAction action, ISelection iselection) {
		if (iselection instanceof IStructuredSelection) currISelection = iselection;
	}

}
