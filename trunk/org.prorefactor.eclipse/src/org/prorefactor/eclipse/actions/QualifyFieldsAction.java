/* Created 2004
 * John Green
 *
 * Copyright (C) 2004 Joanju Limited
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.prorefactor.eclipse.actions;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.prorefactor.core.Util;
import org.prorefactor.eclipse.Plugin;
import org.prorefactor.eclipse.dialogs.Preview;
import org.prorefactor.refactor.action.QualifyFieldsRefactor;




/** Fix unqualified field names. */
public class QualifyFieldsAction implements IWorkbenchWindowActionDelegate, IProRefactorAction {

	public QualifyFieldsAction() { }

	private boolean errors;
	private int numChanges = 0;
	private ActionManager manager = null;
	private QualifyFieldsRefactor refactor;
	private File sourceFile;
	private ISelection currISelection = null;
	private IWorkbenchWindow window;



	public RefactorResult processFile(int topNode) {
		RefactorResult ret = new RefactorResult();
		manager.console(" checking... ");
		try {
			numChanges = refactor.run(sourceFile, sourceFile);
			manager.console(numChanges + " unqualified fields");
		} catch (Throwable e) {
			ret.message = "\n" + Util.getExceptionText(e) + "\n";
		}
		if (ret.message!=null) {
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
			manager.reportError("Select only one compile unit for the Qualify Fields refactoring");
			return;
		}
		manager.doTheParse = false;

		IFile ifile = (IFile) manager.getCurrResource();
		sourceFile = new File(ifile.getLocation().toOSString());

		refactor = new QualifyFieldsRefactor();

		IWorkbenchPage activePage = window.getActivePage();
		try {
			org.eclipse.ui.ide.IDE.openEditor(activePage, ifile);
		} catch (Exception e) {}
		AbstractTextEditor textEditor = (AbstractTextEditor) activePage.getActiveEditor();
		IDocument document = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());
		if (! manager.checkSaved(textEditor)) return;

		manager.doRun();
		if (errors) {
			manager.reportError("See the Console for messages.");
			return;
		}

		String newSource;
		try {
			newSource = refactor.generateNewSource();
		} catch (IOException e) {
			manager.console("\n" + Util.getExceptionText(e) + "\n");
			manager.reportError("See the Console for messages.");
			return;
		}

		Preview preview = new Preview(
			document.get()
			, newSource
			, sourceFile.toString()
			, Plugin.getActiveWorkbenchWindow().getShell() );
		if (! preview.getUserInput()) return;

		document.set(newSource);


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
