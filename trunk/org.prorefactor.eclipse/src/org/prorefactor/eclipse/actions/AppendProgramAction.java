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

import java.io.File;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.prorefactor.core.Util;
import org.prorefactor.eclipse.ResourceUtil;
import org.prorefactor.eclipse.wizards.AppendProgramPage;
import org.prorefactor.eclipse.wizards.RefactorWizard;
import org.prorefactor.refactor.Rollback;
import org.prorefactor.refactor.appendprogram.AppendProgram;




/**
 * Action for applying the "Wrap Procedure Block" refactoring to a single compile unit.
 */
public class AppendProgramAction implements IWorkbenchWindowActionDelegate, IProRefactorAction {

	public AppendProgramAction() {
	}

	private boolean errors;
	private ActionManager manager = null;
	private AppendProgram refactor;
	private File first;
	private File second;
	private ISelection currISelection = null;
	private IWorkbenchWindow window;



	public RefactorResult processFile(int topNode) {
		RefactorResult ret = new RefactorResult();
		manager.console(" append program ");
		try {
			ret.message = refactor.run(first, second);
		} catch (Throwable e) {
			ret.message = "\n" + Util.getExceptionText(e) + "\n";
		}
		// Bubble Declarations is called by Append Program. Check it for String messages.
		for (Iterator it = refactor.bubbler.messages.iterator(); it.hasNext(); ) {
			manager.console("\n" + (String)it.next());
		}
		if (ret.message!=null || refactor.bubbler.messages.size()>0) {
			errors = true;
			ret.breakout = true;
		}
		return ret;
	}



	public String processTargetSet() {
		// Only used for interactive refactorings.
		return null;
	}



	public void run(IAction action) {

		errors = false;

		// manager.setup *first*, because it deals with errors from loading parser, etc.
		manager = new ActionManager(window, this);
		if (manager.setup(currISelection) < 1) return;
		if (manager.getNumSelected() > 1) {
			manager.reportError("Select only one compile unit for the Procedure Wrap refactoring");
			return;
		}
		manager.doTheParse = false;

		IFile ifile = (IFile) manager.fileList.get(0);
		first = new File(ifile.getLocation().toOSString());

		RefactorWizard wizard = new RefactorWizard();
		AppendProgramPage page1 = new AppendProgramPage("page1", first);
		page1.setTitle("Append Program Refactoring");
		wizard.addPage(page1);
		new WizardDialog(window.getShell(), wizard).open();
		if (!wizard.didFinish()) return;
		second = page1.getSecondFile();

		refactor = new AppendProgram(new Rollback());

		manager.doRun();

		if (errors) manager.reportError("See the Console for messages.");

		IWorkbenchPage activePage = window.getActivePage();
		try {
			ifile.getParent().refreshLocal(IResource.DEPTH_ONE, null);
			org.eclipse.ui.ide.IDE.openEditor(activePage, ifile);
		} catch (Exception e) {}

		ResourceUtil.clearRefactorMarkers();
		if (refactor.messageList.size() > 0) {
			ResourceUtil.messagesToMarkers(refactor.messageList);
			MessageDialog.openInformation(
				window.getShell()
				, "ProRefactor"
				, "There are additional tasks. See the Tasks view for Refactor tasks in the workspace. (Check your Tasks filter settings.)" );
		}


	} // run



	/////// Inherited Methods ///////

	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	public void selectionChanged(IAction action, ISelection iselection) {
		if (iselection instanceof IStructuredSelection) currISelection = iselection;
	}

}
