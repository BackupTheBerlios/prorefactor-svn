/* ScanLister.java
 * Created on Nov 10, 2003
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
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;

import com.joanju.ProparseLdr;


public class ScanLister implements IWorkbenchWindowActionDelegate, IProRefactorAction {

	private ISelection currISelection = null;
	private ActionManager manager = null;
	private ProparseLdr parser = null;
	private IWorkbenchWindow window;


	public ScanLister() { }

	public void dispose() { }

	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	private void printNode(int node) {
		String s =
			parser.getNodeType(node)
			+ "\t"
			+ parser.getNodeText(node)
			+ "\t"
			+ Integer.toString(parser.getNodeLine(node))
			+ ":"
			+ Integer.toString(parser.getNodeColumn(node))
			+ "\n"
			;
		manager.console(s);
	}

	public RefactorResult processFile(int topNode) {
		RefactorResult ret = new RefactorResult();
		return ret;
	}

	public String processTargetSet() {
		return null;
	}

	public void run(IAction action) {
		manager = new ActionManager(window, this);
		if (manager.setup(currISelection) < 1) return;
		IResource theRes = manager.getCurrResource();
		if (theRes==null || ! (theRes instanceof IFile) || manager.getNumSelected() > 1) {
			manager.reportError("Select one file to scan");
			return;
		}
		IFile theFile = (IFile) theRes;
		try {
			manager.showConsole();
			manager.consoleClear();
		} catch (PartInitException e) {
			manager.reportError(e.getMessage());
			return;
		}
		parser = ProparseLdr.getInstance();
		int parseNum = parser.parseCreate("scan", theFile.getLocation().toOSString());
		if (parser.errorGetStatus() != 0) {
			manager.reportError(parser.errorGetText());
			return;
		}
		int currNode = parser.getHandle();
		parser.parseGetTop(parseNum, currNode);
		int nodeType = parser.getNodeTypeI(currNode);
		while (nodeType>0) {
			printNode(currNode);
			nodeType = parser.nodeNextSiblingI(currNode, currNode);
		}
		parser.parseDelete(parseNum);
	}

	public void selectionChanged(IAction action, ISelection iselection) {
		if (iselection instanceof IStructuredSelection) currISelection = iselection;
	}

}
