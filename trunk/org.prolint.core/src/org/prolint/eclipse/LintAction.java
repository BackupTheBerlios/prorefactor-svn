/* Created on Jan 21, 2005
 * Authors: John Green
 *
 * Copyright (C) 2005 Prolint.org Contributors
 * This file is part of Prolint.
 *
 * Prolint is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * Prolint is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Prolint; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.prolint.eclipse;

import net.sf.hibernate.HibernateException;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.prolint.core.LintException;
import org.prolint.core.LintRun;
import org.prorefactor.eclipse.actions.IProRefactorAction;
import org.prorefactor.eclipse.actions.RefactorResult;
import org.prorefactor.refactor.RefactorException;

			
/** This class is responsible for launching Prolint against the selected
 * resources, and the children of those resources.
 */
public class LintAction implements IViewActionDelegate, IProRefactorAction {

	private boolean errors = false;
	private ActionManager manager = null;
	private ISelection currISelection = null;
	private IWorkbenchWindow window;
	private LintRun lintRun;

	public LintAction() { }

	public void init(IViewPart view) {
		window = view.getSite().getWorkbenchWindow();
	}
	
	public RefactorResult processFile(int topNode) {
		manager.console(" lint ");
		RefactorResult ret = new RefactorResult();
		IFile iFile = (IFile) manager.getCurrResource();
		Exception logException = null;
		try {
			lintRun.lint(manager.getCurrFile(), iFile.getProjectRelativePath().toString());
		} catch (LintException e) {
			logException = e;
		} catch (RefactorException e) {
			logException = e;
		} catch (HibernateException e) {
			logException = e;
			ret.exception = e; // causes processing to halt
		}
		if (logException!=null) {
			Plugin.log(logException);
			ret.message = logException.getMessage();
			errors = true;
		}
		return ret;
	}

	public String processTargetSet() {
		return null; // not used
	}

	public void run(IAction action) {
		errors = false;
		// manager.setup *first*, because it deals with errors from loading parser, etc.
		manager = new ActionManager(window, this);
		if (manager.setup(currISelection) < 1) return;
		// We leave the parsing up to the LintRun object.
		manager.doTheParse = false;
		try {
			lintRun = new LintRun();
			manager.doRun();
			lintRun.writeAllMarkers();
		} catch (Exception e) {
			// Creation of the LintRun object sets up a temp-table, which we
			// could potentially get a HibernateException from.
			Plugin.log(e);
			if (e.getMessage()!=null) manager.console(e.getMessage());
		}
		if (errors) {
			manager.reportError("There were errors. See the Console or the Error Log for details.");
			return;
		}
	} // run

	public void selectionChanged(IAction action, ISelection iselection) {
		if (iselection instanceof IStructuredSelection) currISelection = iselection;
	}

}
