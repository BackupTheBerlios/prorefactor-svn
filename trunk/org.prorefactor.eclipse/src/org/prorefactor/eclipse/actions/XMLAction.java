/* XMLAction.java
 * Created on Nov 9, 2003
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
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.prorefactor.eclipse.wizards.RefactorWizard;
import org.prorefactor.eclipse.wizards.XMLWriterPage;
import org.prorefactor.refactor.XMLWriter;

import com.joanju.ProparseLdr;

public class XMLAction
	implements IViewActionDelegate, IWorkbenchWindowActionDelegate, IProRefactorAction {

	private ISelection currISelection = null;
	private ActionManager manager = null;
	private boolean showHidden = false;
	private IWorkbenchWindow window;


	public XMLAction() { }

	public void dispose() { }

	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
	public void init(IViewPart view) {
		this.window = view.getSite().getWorkbenchWindow();
	}

	public RefactorResult processFile(int topNode) {
		RefactorResult ret = new RefactorResult();
		manager.console(" xml ");
		IResource res = manager.getCurrResource();
		IPath resPath = res.getLocation().removeFileExtension().addFileExtension("xml");
		XMLWriter writer = new XMLWriter(topNode, resPath.toOSString());
		writer.showHidden = showHidden;
		ret.message = writer.run();
		// We refresh the parent folder with the new/changed listing file.
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
		RefactorWizard wizard = new RefactorWizard();
		XMLWriterPage writerPage = new XMLWriterPage("writerpage");
		writerPage.setTitle("Write XML From Syntax Tree");
		wizard.addPage(writerPage);
		new WizardDialog(window.getShell(), wizard).open();
		if (!wizard.didFinish()) return;
		showHidden = writerPage.showHidden;
		ProparseLdr parser = ProparseLdr.getInstance();
		parser.configSet("show-proparse-directives", "true");
		manager.doRun();
		parser.configSet("show-proparse-directives", "");
	}

	public void selectionChanged(IAction action, ISelection iselection) {
		if (iselection instanceof IStructuredSelection) currISelection = iselection;
	}

}
