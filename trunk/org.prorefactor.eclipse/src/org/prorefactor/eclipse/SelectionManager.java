/* SelectionManager.java
 * Created on Feb 29, 2004
 * John Green
 *
 * Copyright (C) 2004 Joanju Limited
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.prorefactor.eclipse;

import java.awt.Toolkit;
import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.prorefactor.core.CodeSelect;
import org.prorefactor.core.ICallback;
import org.prorefactor.core.IConstants;
import org.prorefactor.core.JPNode;
import org.prorefactor.core.PositionIndex;
import org.prorefactor.core.TokenTypes;
import org.prorefactor.eclipse.actions.ActionManager;
import org.prorefactor.eclipse.actions.IProRefactorAction;
import org.prorefactor.eclipse.actions.RefactorResult;



/** Manages editor selection, like "expand" and "contract"
 * for automatic selection of AST based ranges for expressions,
 * statements, and blocks.
 */
public class SelectionManager implements IProRefactorAction {

	private boolean didParse = false;
	private long timeStamp;
	private AbstractTextEditor textEditor;
	private CodeSelect codeSelect = null;
	private File javaFile;
	private IFile prevFile;
	private ITextSelection prevSelection;
	private PositionIndex positionIndex;


	/* @see org.prorefactor.eclipse.actions.IProRefactorAction#processFile(int) */
	public RefactorResult processFile(int topNode) {
		final PositionIndex index = new PositionIndex();
		positionIndex = index;
		JPNode.TreeConfig treeConfig = new JPNode.TreeConfig();
		treeConfig.storePosition = true;
		treeConfig.callback = new ICallback() {
			public Object run(Object obj) {
				index.addNode((JPNode)obj);
				return null;
			}
		};
		JPNode.getTree(topNode, treeConfig);
		didParse = true;
		return new RefactorResult();
	}



	/* @see org.prorefactor.eclipse.actions.IProRefactorAction#processTargetSet() */
	public String processTargetSet() { return null; }



	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {

		if (textEditor.isDirty()) {
			MessageDialog.openError(
				Plugin.getActiveWorkbenchWindow().getShell()
				, "ProRefactor"
				, "Save changes first" );
			return;
		}

		IDocument document = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());

		ISelection tselection = textEditor.getEditorSite().getSelectionProvider().getSelection();
		if (! (tselection instanceof ITextSelection)) return;
		ITextSelection selection = (ITextSelection) tselection;

		if (! (textEditor.getEditorInput() instanceof IFileEditorInput) ) return;
		IFile file = ((IFileEditorInput) textEditor.getEditorInput()).getFile();

		if (file!=prevFile || javaFile==null || javaFile.lastModified()!=timeStamp) {
			prevFile = file;
			javaFile = new File(file.getLocation().toOSString());
			timeStamp = javaFile.lastModified();
			ActionManager manager = new ActionManager(Plugin.getActiveWorkbenchWindow(), this);
			if (manager.setup(new StructuredSelection(file)) < 1) return;
			didParse = false;
			manager.doRun();
			if (!didParse) return;
			prevSelection = null;
		}

		if (	prevSelection==null
			||	selection.getOffset() != prevSelection.getOffset()
			||	selection.getLength() != prevSelection.getLength()
			) {
			prevSelection = selection;
			int [] pos = ProRefactorEclipse.convertOffsetToPos(document, selection.getOffset());
			if (pos==null) return;
			// TODO Using file=0, should allow for selection in include files too.
			codeSelect = new CodeSelect(0, pos[0], pos[1], positionIndex);
		}

		JPNode branch = codeSelect.expand();
		while (true) {
			if (branch==null || branch.getType()==TokenTypes.Program_root) {
				Toolkit.getDefaultToolkit().beep();
				return;
			}
			// If the first selected node has no children, it's not interesting. Expand.
			if (branch.firstChild()==null) {
				branch = codeSelect.expand();
				continue;
			}
			// If the node is synthetic, jump up one.
			if (branch.getLine() < 1) {
				branch = codeSelect.expand();
				continue;
			}
			break;
		}

		int [] begin;
		// For an operator node, we want the first child as the start.
		if (branch.attrGet(IConstants.OPERATOR) == IConstants.TRUE) {
			begin = branch.firstChild().firstNaturalChild().getPos();
		} else begin = branch.getPos();

		int [] end = CodeSelect.branchEndPos(branch);
		// TODO This changes when working with include files
		if (begin==null || end==null || begin[0]!=0 || end[0]!=0) return;
		int beginOff = ProRefactorEclipse.convertPosToOffset(document, begin[1], begin[2]);
		int endOff = ProRefactorEclipse.convertPosToOffset(document, end[1], end[2]);
		if (beginOff<0 || endOff<0) return;
		beginOff = ProRefactorEclipse.offsetWithLeadingWhitespace(document, beginOff);
		endOff = ProRefactorEclipse.offsetWithTrailingWhitespace(document, endOff);

		ITextSelection newSelection = new TextSelection(beginOff, endOff-beginOff);
		textEditor.getSelectionProvider().setSelection(newSelection);
		prevSelection = newSelection;

		textEditor.setFocus();
	} // run



	public void runForContract() {

		IDocument document = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());

		ISelection tselection = textEditor.getEditorSite().getSelectionProvider().getSelection();
		if (! (tselection instanceof ITextSelection)) return;
		ITextSelection selection = (ITextSelection) tselection;

		if (! (textEditor.getEditorInput() instanceof IFileEditorInput) ) return;
		IFile file = ((IFileEditorInput) textEditor.getEditorInput()).getFile();
		if (file != prevFile) return;

		if (	prevSelection==null
			||	selection.getOffset() != prevSelection.getOffset()
			||	selection.getLength() != prevSelection.getLength()
			) return;

		JPNode branch = codeSelect.contract();
		while (true) {
			if (branch==null) {
				Toolkit.getDefaultToolkit().beep();
				return;
			}
			// If the node is synthetic, jump down one.
			if (branch.getLine() < 1) {
				branch = codeSelect.contract();
				continue;
			}
			break;
		}

		int [] begin;
		// For an operator node, we want the first child as the start.
		if (branch.attrGet(IConstants.OPERATOR) == IConstants.TRUE) {
			begin = branch.firstChild().firstNaturalChild().getPos();
		} else begin = branch.getPos();

		int [] end = CodeSelect.branchEndPos(branch);
		// TODO This changes when working with include files
		if (begin==null || end==null || begin[0]!=0 || end[0]!=0) return;
		int beginOff = ProRefactorEclipse.convertPosToOffset(document, begin[1], begin[2]);
		int endOff = ProRefactorEclipse.convertPosToOffset(document, end[1], end[2]);
		if (beginOff<0 || endOff<0) return;

		ITextSelection newSelection = new TextSelection(beginOff, endOff-beginOff);
		textEditor.getSelectionProvider().setSelection(newSelection);
		prevSelection = newSelection;

		textEditor.setFocus();
	} // runForContract



	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {}



	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction, org.eclipse.ui.IEditorPart)
	 */
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		textEditor = (AbstractTextEditor) targetEditor;
	}



} // class
