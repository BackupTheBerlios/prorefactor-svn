/* RollbackAction.java
 * Created on Dec 3, 2003
 * John Green
 *
 * Copyright (C) 2003 Joanju Limited
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.prorefactor.eclipse.actions;


import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.prorefactor.eclipse.wizards.RefactorWizard;
import org.prorefactor.eclipse.wizards.RollbackPage;
import org.prorefactor.refactor.FileChange;
import org.prorefactor.refactor.Rollback;

import java.util.HashSet;
import java.util.Iterator;


public class RollbackAction implements IWorkbenchWindowActionDelegate {

	public RollbackAction() {}

	protected IWorkbenchWindow window;
	private IWorkspaceRoot wsroot = ResourcesPlugin.getWorkspace().getRoot();



	private HashSet preservePathList() {
		HashSet set = new HashSet();
		for (Iterator it = Rollback.getCurrent().getFileChanges().iterator(); it.hasNext(); ) {
			try {
				FileChange change = (FileChange)it.next();
				String dir = change.sourceFile.getParent();
				if (dir!=null) set.add(dir);
			} catch (Exception e) {}
		}
		return set;
	}



	public void run(IAction action) {
		
		Rollback rollback = Rollback.getCurrent();

		if (	rollback == null
			||	Rollback.getCurrent().getFileChanges().size() < 1
			) {
			MessageDialog.openInformation(
				window.getShell(), "ProRefactor"
				, "The rollback list is empty. Note that not all refactoring types generate a rollback list.\n(Also, the rollback list is not persistent across sessions. You can find preserved files in your working directory; prorefactor/rollback, to find files to manually restore.)"
				);
			return;
		}

		RefactorWizard wizard = new RefactorWizard();
		RollbackPage page1 = new RollbackPage("page1", Rollback.getCurrent());
		page1.setTitle("Roll Back Previous Refactoring");
		wizard.addPage(page1);
		new WizardDialog(window.getShell(), wizard).open();
		if (!wizard.didFinish()) return;

		HashSet dirSet = preservePathList();

		String errors = rollback.rollback();

		if (errors!=null)
			MessageDialog.openError(window.getShell(), "ProRefactor", errors);

		// Refresh directories in Navigator etc.
		for (Iterator it = dirSet.iterator(); it.hasNext(); ) {
			IContainer path = wsroot.getContainerForLocation(new Path((String)it.next()));
			try {
				if (path!=null) path.refreshLocal(IResource.DEPTH_ONE, null);
			} catch (CoreException e) {}
		}

	} // run



	//// Interface Methods ////

	public void dispose() {}

	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	public void selectionChanged(IAction action, ISelection iselection) {}

}
