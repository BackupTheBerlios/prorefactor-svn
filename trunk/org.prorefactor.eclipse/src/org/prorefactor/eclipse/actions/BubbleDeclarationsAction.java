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

import com.joanju.ProparseLdr;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.prorefactor.core.Util;
import org.prorefactor.eclipse.ResourceUtil;
import org.prorefactor.eclipse.wizards.RefactorWizard;
import org.prorefactor.eclipse.wizards.TextPage;
import org.prorefactor.refactor.FileChange;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.refactor.Rollback;
import org.prorefactor.refactor.bubbledecs.BubbleDecsRefactor;

import java.util.HashSet;
import java.util.Iterator;

/**
 * Action for applying the "Bubble Declarations" refactoring to a single compile unit.
 */
public class BubbleDeclarationsAction implements IWorkbenchWindowActionDelegate, IProRefactorAction {

	public BubbleDeclarationsAction() {}

	private boolean didRun;
	private boolean errors;
	private ActionManager manager = null;
	private ISelection currISelection = null;
	private IWorkbenchWindow window;
	private IWorkspaceRoot wsroot = ResourcesPlugin.getWorkspace().getRoot();
	private RefactorSession refpack = RefactorSession.getInstance();
	private Rollback rollback = null;



	/**
	 * We can use this method to dispose of any system
	 * resources we previously allocated.
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	/**
	 * We will cache window object in order to
	 * be able to provide parent shell for the message dialog.
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	public RefactorResult processFile(int topNode) {
		RefactorResult ret = new RefactorResult();
		manager.console(" bubble declarations ");
		BubbleDecsRefactor refactor = new BubbleDecsRefactor(rollback);
		try {
			ret.message = refactor.run(topNode);
		} catch (Throwable e) {
			errors = true;
			manager.console("\n" + Util.getExceptionText(e) + "\n");
		}
		for (Iterator it = refactor.messages.iterator(); it.hasNext(); ) {
			manager.console("\n" + (String)it.next());
		}
		if (ret.message!=null || refactor.messages.size()>0) {
			errors = true;
			ret.breakout = true;
		}

		try {
			manager.getCurrResource().getParent().refreshLocal(IResource.DEPTH_ONE, null);
		} catch (CoreException e) {}


		didRun = true; 
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
		if (manager.getNumSelected() > 1) {
			manager.reportError("Select only one compile unit for the Bubble Declarations refactoring");
			return;
		}

		RefactorWizard wizard = new RefactorWizard();
		TextPage page1 = new TextPage("page1");
		page1.setTitle("Bubble Declarations");
		wizard.addPage(page1);
		WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
		// Create the dialog before adding text, otherwise the text grows the size
		dialog.create();
		page1.setText(
				"Changes made by this refactoring can be undone by choosing Roll "
				+ "Back from the Progress Refactoring menu.\n\n"
				+ "After refactoring, for a side-by-side diff review of changes "
				+ "made to an individual file, choose Compare With -> Local History "
				+ "from the Navigator pop-up menu."
				);
		dialog.open();
		if (!wizard.didFinish()) return;

		didRun = false;
		errors = false;

		ProparseLdr parser = ProparseLdr.getInstance();
		rollback = new Rollback();

		parser.configSet("show-proparse-directives", "true");
		refpack.enableParserListing();
		manager.doRun();
		refpack.disableParserListing();
		parser.configSet("show-proparse-directives", "");

		if (! didRun) return;
		if (errors) manager.reportError("There were errors. See the Console for details.");
		if (rollback.getFileChanges().size()<1) manager.console("No changes\n");

		IWorkbenchPage activePage = window.getActivePage();
		manager.console("\n");
		HashSet dirSet = new HashSet();
		for (Iterator it = rollback.getFileChanges().iterator(); it.hasNext(); ) {
			try {
				FileChange change = (FileChange)it.next();
				String fullpath = change.sourceFile.getCanonicalPath();
				manager.console(
					FileChange.whatLabel[change.whatHappened]
					+ " "
					+ fullpath
					+ "\n"
					);
				IFile theFile = ResourceUtil.getIFile(fullpath);
				if (theFile!=null) org.eclipse.ui.ide.IDE.openEditor(activePage, theFile);
				String dir = change.sourceFile.getParent();
				if (dir!=null) dirSet.add(dir);
			} catch (Exception e) {}
		}

		// Refresh directories in Navigator etc.
		for (Iterator it = dirSet.iterator(); it.hasNext(); ) {
			IContainer path = wsroot.getContainerForLocation(new Path((String)it.next()));
			try {
				if (path!=null) path.refreshLocal(IResource.DEPTH_ONE, null);
			} catch (CoreException e) {}
		}

	}

	public void selectionChanged(IAction action, ISelection iselection) {
		if (iselection instanceof IStructuredSelection) currISelection = iselection;
	}

}
