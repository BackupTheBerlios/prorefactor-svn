/**
 * RenameAction.java
 * @author John Green
 * Sep 14, 2004
 * www.joanju.com
 *
 * Copyright (C) 2004 Joanju Limited.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.prorefactor.eclipse.actions;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.prorefactor.core.Util;
import org.prorefactor.eclipse.wizards.RefactorDirectoryPage;
import org.prorefactor.eclipse.wizards.RefactorWizard;
import org.prorefactor.eclipse.wizards.RenamePage;
import org.prorefactor.eclipse.wizards.TextPage;
import org.prorefactor.refactor.RefactorException;
import org.prorefactor.refactor.action.RenameSchema;


/** Action for renaming tables and fields within the selected parse units. */
public class RenameAction implements IProRefactorAction,
		IWorkbenchWindowActionDelegate, IViewActionDelegate {

	public RenameAction() { }
	
	static {
		zzHelpText();
	}

	private static String HELP_TEXT;
	private static final String TITLE = "Rename Schema";

	private ActionManager manager = null;
	private ISelection currISelection = null;
	private IWorkbenchWindow window;
	private RenameSchema theRefactor = null;
	private String namePairsList;

	
	
	public void dispose() { }


	
	private String getFileContents(String filename) {
		File file = new File(filename);
		String ret;
		try {
			StringBuffer buff = Util.readFile(file);
			ret = buff.toString();
		} catch (Exception e) {
			manager.reportError(e.getMessage());
			return null;
		}
		return ret;
	}



	public void init(IViewPart view) {
		this.window = view.getSite().getWorkbenchWindow();
	}


	
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
	
	
	
	public RefactorResult processFile(int topNode) {
		manager.console(" check for rename ");
		RefactorResult ret = new RefactorResult();
		IFile iFile = (IFile) manager.getCurrResource();
		try {
			int matches = theRefactor.run(manager.getCurrFile(), iFile.getProjectRelativePath().toString());
			manager.console(matches + " matches ");
		} catch (Exception e) {
			ret.message = e.getMessage();
		}
		return ret;
	}


	
	/* Only used by interactive refactorings */
	public String processTargetSet() { return null; }


	
	public void run(IAction action) {

		// manager.setup *first*, because it deals with errors from loading parser, etc.
		manager = new ActionManager(window, this);
		if (manager.setup(currISelection) < 1) return;
		
		// We leave the parsing up to the refactor class
		manager.doTheParse = false;

		// Create and run the wizard
		RefactorWizard wizard = new RefactorWizard();
		TextPage textPage = new TextPage("text_page");
		textPage.setTitle(TITLE);
		wizard.addPage(textPage);
		RenamePage renamePage = new RenamePage("rename_page");
		renamePage.setTitle(TITLE);
		wizard.addPage(renamePage);
		RefactorDirectoryPage dirPage = new RefactorDirectoryPage("dir_page");
		dirPage.setTitle(TITLE);
		wizard.addPage(dirPage);
		WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
		// Create the dialog, to set a size, before filling the Text page with text.
		// Otherwise, the Text page will force the dialog to the full screen width.
		dialog.create();
		Point size = dialog.getShell().getSize();
		size.y = size.y + 100; // Add a little height for the "names" editor
		dialog.getShell().setSize(size);
		textPage.setText(HELP_TEXT);
		dialog.open();
		if (!wizard.didFinish()) return;
		
		namePairsList = renamePage.getText();
		if (renamePage.isGetFromFile()) namePairsList = getFileContents(namePairsList);
		if (namePairsList==null || namePairsList.trim().length()==0) return;

		try {
			theRefactor = new RenameSchema(namePairsList, dirPage.getSelectedPath());
		} catch (RefactorException e) {
			manager.reportError(e.getMessage());
			return;
		}
		
		manager.doRun();

	} // run


	
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) currISelection = selection;
	}
	
	
	
	static void zzHelpText() {
		HELP_TEXT = 
			"Rename schema tables and fields from an input from/to name mapping.\n\n"
			+ "Database name changes are not supported (logical, alias, or otherwise). "
			+ "Buffer names are not changed.\n\n"
			+ "Input names map is a string of old/new name pairs, all separated by "
			+ "any sort of whitespace (space, tab, newline,...). "
			+ "The old names must be qualified: either db.table or db.table.field. "
			+ "Any qualifier on the new name is completely ignored.\n\n"
			+ "Any database qualifier from the original source code is retained. "
			+ "For fields, any table qualifier is retained from the original source code. "
			+ "A table name, and names for fields in that table, can be changed at the same time.\n\n"
			+ "Example:\n"
			+ "db1.tblOldName tblNewName\n"
			+ "db1.tblOldName.fld newFldName\n"
			+ "...will rename a table and one of its fields. Note that the old field "
			+ "name must be qualified with the old table name.\n\n"
			+ "Once this refactor is complete, and you have merged the results into "
			+ "your code, you should use a grep tool to find any remaining old name references in "
			+ "comments, strings, and preprocessor directives. Also consider any "
			+ "dynamic queries where you may be deriving the strings from external sources."
			;
	}

	
	
}