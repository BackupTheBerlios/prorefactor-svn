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


import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.prorefactor.core.Util;
import org.prorefactor.eclipse.wizards.RefactorWizard;
import org.prorefactor.eclipse.wizards.TextPage;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.refactor.Rollback;
import org.prorefactor.refactor.wrapproc.WrapProcedure;



/**
 * Action for applying the "Wrap Procedure Block" refactoring to a single compile unit.
 */
public class WrapProcedureBlock implements IWorkbenchWindowActionDelegate, IProRefactorAction {

	public WrapProcedureBlock() {
	}

	private boolean errors;
	private ActionManager manager = null;
	private ISelection currISelection = null;
	private IWorkbenchWindow window;
	private IFile theFile;
	private RefactorSession refpack = RefactorSession.getInstance();



	public RefactorResult processFile(int topNode) {
		RefactorResult ret = new RefactorResult();
		manager.console(" wrap procedure block ");
		try {
			WrapProcedure refactor = new WrapProcedure(topNode, new Rollback());
			refactor.run();
			theFile = (IFile) manager.getCurrResource();
		} catch (Throwable e) {
			ret.message = "\n" + Util.getExceptionText(e) + "\n";
			errors = true;
		}
		return ret;
	}



	public String processTargetSet() {
		// Only used for interactive refactorings.
		return null;
	}



	public void run(IAction action) {

		errors = false;
		theFile = null;

		// manager.setup *first*, because it deals with errors from loading parser, etc.
		manager = new ActionManager(window, this);
		if (manager.setup(currISelection) < 1) return;
		if (manager.getNumSelected() > 1) {
			manager.reportError("Select only one compile unit for the Procedure Wrap refactoring");
			return;
		}

		RefactorWizard wizard = new RefactorWizard();
		TextPage page1 = new TextPage("page1");
		page1.setTitle("Wrap Procedure Block");
		wizard.addPage(page1);
		WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
		// Create the dialog first, otherwise adding text grows its size
		dialog.create();
		page1.setText(
				"This wraps the program's procedural code into an internal PROCEDURE.\n\n"
				+ "Changes made by this refactoring can be undone by choosing Roll Back "
				+ "from the Progress Refactoring menu.\n\n"
				+ "After refactoring, for a side-by-side diff review of changes made to an "
				+ "individual file, choose Compare With -> Local History from the "
				+ "Navigator pop-up menu."
				);
		dialog.open();
		if (!wizard.didFinish()) return;

		refpack.enableParserListing();
		manager.doRun();
		refpack.disableParserListing();

		if (errors) manager.reportError("See the Console for messages.");

		if (theFile!=null) {
			IWorkbenchPage activePage = window.getActivePage();
			try {
				theFile.getParent().refreshLocal(IResource.DEPTH_ONE, null);
				if (theFile!=null) org.eclipse.ui.ide.IDE.openEditor(activePage, theFile);
			} catch (Exception e) {}
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
