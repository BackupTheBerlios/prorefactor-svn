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

import java.util.Iterator;
import java.util.TreeSet;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.prorefactor.refactor.substitute.SubstituteLint;
import org.prorefactor.refactor.substitute.SubstituteTarget;
import org.prorefactor.refactor.substitute.SubstituteWrap;

import com.joanju.ProparseLdr;

/**
 * Our sample action implements workbench action delegate.
 * The action proxy will be created by the workbench and
 * shown in the UI. When the user tries to use the action,
 * this delegate will be created and execution will be 
 * delegated to it.
 * @see IWorkbenchWindowActionDelegate
 */
public class SubstituteAction
	implements IWorkbenchWindowActionDelegate, IViewActionDelegate, IProRefactorAction {

	private ISelection currISelection = null;
	private ActionManager manager = null;
	private ProparseLdr parser = null;
	private TreeSet targetSet = null;
	private IWorkbenchWindow window;

	public SubstituteAction() { }

	/**
	 * We can use this method to dispose of any system
	 * resources we previously allocated.
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() { }

	/** @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart) */
	public void init(IViewPart view) {
		this.window = view.getSite().getWorkbenchWindow();
	}
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	public RefactorResult processFile(int topNode) {
		RefactorResult ret = new RefactorResult();
		manager.console(" refactorable: ");
		SubstituteLint lint = new SubstituteLint();
		targetSet = lint.run(topNode);
		//TODO This is a horrible hack.
		int numRefactorable = 0;
		for (Iterator it2 = targetSet.iterator(); it2.hasNext(); ) {
			SubstituteTarget target = (SubstituteTarget) it2.next();
			// If there's less than two translatable strings, we don't refactor.
			if (target.numTranslatable < 2) continue;
			numRefactorable++;
		}
		manager.console(Integer.toString(numRefactorable));
		if (numRefactorable>0) {
			manager.console("\n");
			ret.breakout = true;
		}
		return ret;
	}

	public String processTargetSet() {
		try {
			if (targetSet==null || targetSet.size() == 0) return "";
			lint_results_loop:
			for (Iterator it = targetSet.iterator(); it.hasNext(); ) {
				SubstituteTarget target = (SubstituteTarget) it.next();
				// If there's less than two translatable strings, we don't refactor.
				if (target.numTranslatable < 2) continue lint_results_loop;
				String theReturn = SubstituteWrap.processTarget(target);
				if (theReturn.length() > 0) return theReturn;
			} // lint_results_loop
			return "";
		} finally {
			// We are responsible for cleaning up handles in the resultSet.
			if (targetSet!=null) {
				for (Iterator it = targetSet.iterator(); it.hasNext(); ) {
					SubstituteTarget target = (SubstituteTarget) it.next();
					parser.releaseHandle(target.nodeHandle);
				}
			}
		}
	}

	public void run(IAction action) {
		// manager.setup *first*, because it deals with errors from loading parser, etc.
		manager = new ActionManager(window, this);
		if (manager.setup(currISelection) < 1) return;
		if (parser==null) parser = ProparseLdr.getInstance();
		manager.doRun();
	}

	public void selectionChanged(IAction action, ISelection iselection) {
		if (iselection instanceof IStructuredSelection) currISelection = iselection;
	}

}