/* ProparseVersion.java
 * Created on Nov 29, 2003
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

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;


public class ProparseVersion implements IWorkbenchWindowActionDelegate {

	public ProparseVersion() {}

	protected IWorkbenchWindow window;
	protected ProparseLdr parser = ProparseLdr.getInstance();


	public void run(IAction action) {
		String verInfo = "Proparse version " + parser.getVersion();
		MessageDialog.openInformation(window.getShell(), "ProRefactor", verInfo);
	}



	//// Interface Methods ////

	public void dispose() {}

	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	public void selectionChanged(IAction action, ISelection iselection) {}

}
