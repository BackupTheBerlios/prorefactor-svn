/* Created 2003
 * John Green
 *
 * Copyright (C) 2003-2004 Joanju Limited
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.prorefactor.eclipse.actions;


import java.io.IOException;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.prorefactor.eclipse.wizards.RefactorDirectoryPage;
import org.prorefactor.eclipse.wizards.RefactorNamesPage;
import org.prorefactor.eclipse.wizards.RefactorWizard;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.refactor.TempDirWrap;
import org.prorefactor.refactor.tfnames.NamesLint;
import org.prorefactor.refactor.tfnames.TFNamesRefactor;



public class NamesAction 
	implements IWorkbenchWindowActionDelegate, IViewActionDelegate, IProRefactorAction {

	public NamesAction() {}

	private boolean hasMessages;
	private ISelection currISelection = null;
	private ActionManager manager = null;
	private TFNamesRefactor theRefactor = null;
	private IWorkbenchWindow window;
	private TempDirWrap wrap = null;



	public void dispose() {}



	public void init(IViewPart view) {
		this.window = view.getSite().getWorkbenchWindow();
	}



	public void init(IWorkbenchWindow window) {
		this.window = window;
	}



	public RefactorResult processFile(int topNode) {
		manager.console(" names ");
		NamesLint lint = new NamesLint(theRefactor);
		RefactorResult ret = new RefactorResult();
		try {
			ret.message = wrap.run(topNode, lint, theRefactor);
		} catch (IOException e) {
			ret.exception = e;
		}
		if (ret.message!=null && ret.message.length()>0) hasMessages = true;
		return ret;
	}

	public String processTargetSet() {
		// Only used for interactive refactorings
		return null;
	}

	public void run(IAction action) {
		// Reset state
		hasMessages = false;
		// manager.setup *first*, because it deals with errors from loading parser, etc.
		manager = new ActionManager(window, this);
		if (manager.setup(currISelection) < 1) return;
		theRefactor = new TFNamesRefactor();
		RefactorWizard wizard = new RefactorWizard();
		RefactorNamesPage namesPage = new RefactorNamesPage("namespage", theRefactor);
		namesPage.setTitle("Table and Field Names Refactoring");
		wizard.addPage(namesPage);
		RefactorDirectoryPage dirPage = new RefactorDirectoryPage("dirpage");
		dirPage.setTitle("Table and Field Names Refactoring");
		wizard.addPage(dirPage);
		new WizardDialog(window.getShell(), wizard).open();
		if (!wizard.didFinish()) return;
		wrap = new TempDirWrap(dirPage.getSelectedPath());
		manager.doRun();
		if (hasMessages) manager.console(
			"There were messages. See " + RefactorSession.MESSAGES_FILE
			+ " in your working directory." );
	}

	public void selectionChanged(IAction action, ISelection iselection) {
		if (iselection instanceof IStructuredSelection) currISelection = iselection;
	}

}
