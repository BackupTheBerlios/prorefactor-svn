/* ExtractMethodAction.java
 * Created on Feb 23, 2004
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

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.prorefactor.core.Util;
import org.prorefactor.eclipse.ProRefactorEclipse;
import org.prorefactor.eclipse.Plugin;
import org.prorefactor.eclipse.dialogs.Preview;
import org.prorefactor.eclipse.wizards.ExtractMethodPage;
import org.prorefactor.eclipse.wizards.RefactorWizard;
import org.prorefactor.eclipse.wizards.RefactorWizardDialog;
import org.prorefactor.refactor.action.ExtractMethod;


/** Editor action for extracting a method given a text selection.
 */
public class ExtractMethodAction implements IEditorActionDelegate, IProRefactorAction  {

	boolean didParse = false;
	int begin[];
	int end[];
	AbstractTextEditor textEditor;
	ActionManager manager;
	ExtractMethod refactor;
	File currFile = null;
	ITextSelection currSelection;



	/* This is called via ActionManager.doRun().
	 * @see org.prorefactor.eclipse.actions.IProRefactorAction#processFile(int)
	 */
	public RefactorResult processFile(int topNode) {
		try {
			refactor.run(currFile, begin, end, currFile);
		} catch (Exception e) {
			manager.console("\n" + Util.getExceptionText(e));
			return null;
		}
		didParse = true;
		return null;
	}



	/* (non-Javadoc)
	 * @see org.prorefactor.eclipse.actions.IProRefactorAction#processTargetSet()
	 */
	public String processTargetSet() { return null; }



	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		if (currSelection==null || currSelection.getLength() < 1) return;

		if (! (textEditor.getEditorInput() instanceof IFileEditorInput) ) return;
		IFile ifile = ((IFileEditorInput) textEditor.getEditorInput()).getFile();
		currFile = new File(ifile.getLocation().toOSString());

		IDocument document = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());

		int beginOffset = currSelection.getOffset();
		int endOffset = beginOffset + currSelection.getLength() - 1;

		begin = ProRefactorEclipse.convertOffsetToPos(document, beginOffset);
		end = ProRefactorEclipse.convertOffsetToPos(document, endOffset);
		if (begin==null || end==null) return;

		manager = new ActionManager(Plugin.getActiveWorkbenchWindow(), this);
		if (manager.setup(new StructuredSelection(ifile)) < 1) return;
		if (! manager.checkSaved(textEditor)) return;
		manager.doTheParse = false;

		refactor = new ExtractMethod(null);
		didParse = false;
		manager.doRun();
		if (!didParse) {
			manager.reportError("There were errors. See the console for details.");
			return;
		}


		RefactorWizard wizard = new RefactorWizard();
		ExtractMethodPage page = new ExtractMethodPage(refactor);
		page.setTitle("Extract Method");
		wizard.addPage(page);
		wizard.setNeedsPreviewButton(true);
		new RefactorWizardDialog(Plugin.getActiveWorkbenchWindow().getShell(), wizard).open();
		if (!wizard.didFinish()) return;

		refactor.setSelectedText(currSelection.getText());
		
		StringBuffer buff = new StringBuffer(document.get());
		buff.replace(beginOffset, endOffset, refactor.generateCallText());
		buff.append(refactor.generateMethodText());

		if (wizard.isPreview()) {
			Preview preview = new Preview(
				document.get()
				, buff.toString()
				, currFile.toString()
				, Plugin.getActiveWorkbenchWindow().getShell() );
			if (! preview.getUserInput()) return;
		}

		document.set(buff.toString());
		textEditor.selectAndReveal(beginOffset, 0);

	} // run



	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection != null && selection instanceof ITextSelection) {
			currSelection = (ITextSelection) selection;
			if (currSelection.getLength() > 1) action.setEnabled(true);
			else action.setEnabled(false);
		} else {
			currSelection = null;
			action.setEnabled(false);
		}
	} // selectionChanged



	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction, org.eclipse.ui.IEditorPart)
	 */
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		textEditor = (AbstractTextEditor) targetEditor;
	}



} // class
