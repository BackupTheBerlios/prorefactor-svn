/* Created 2003
 * John Green
 *
 * Copyright (C) 2003-2004 Joanju Limited
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.prorefactor.eclipse.actions;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.prorefactor.core.ICallback;
import org.prorefactor.eclipse.Plugin;
import org.prorefactor.eclipse.messages.ParserMessages;
import org.prorefactor.refactor.RefactorSession;

import com.joanju.ProparseLdr;


public class ActionManager {

	public ActionManager(IWorkbenchWindow window, IProRefactorAction action) {
		this.action = action;
		this.window = window;
		display = window.getShell().getDisplay();
	}

	private IProRefactorAction action = null;
	private MessageConsole console = new MessageConsole("ProRefactor", null);
	private MessageConsoleStream consoleStream = console.newMessageStream();
	private IResource currResource = null;
	protected Iterator currIter = null;
	private String currProject = null;
	protected Display display = null;
	/** Is the action manager to parse the files when it does the run? */
	public boolean doTheParse = true;
	private String errorMess = null;
	/** A list of IFile *.p and *.w objects, generated from the selected resources */
	protected ArrayList fileList = null;
	private ProparseLdr parser = null;
	private boolean parseErrors = false;
	public ParserMessages parserMessages;
	public ICallback preParse = null;
	private RefactorSession refPack = null;
	private SpecialMonitorDialog theDialog = null;
	protected int topNode;
	private IWorkbenchWindow window;
	protected int workDone = 0;



	protected class SpecialMonitorDialog extends ProgressMonitorDialog {
		protected int taskSize = 0;
		SpecialMonitorDialog(Shell theShell, int taskSize) {
			super(theShell);
			this.taskSize = taskSize;
		}
		protected Control createDialogArea(Composite parent) {
			getShell().setText("ProRefactor");
			super.createDialogArea(parent);
			taskLabel.setText("Processing files. See Console for output...");
			progressIndicator.beginTask(taskSize);
			progressIndicator.worked(workDone);
			return dialogArea;
		}
		public void didOne() {
			final ProgressIndicator indicator = progressIndicator;
			display.asyncExec(new Runnable() {
				public void run() {
					try {
						indicator.worked(1);
					} catch (Exception e) {
						Plugin.log(e);
					}
				}
			});
		}
	}


	
	protected void addContainer(IContainer theContainer) {
		IResource [] resArray = null;
		try {
			resArray = theContainer.members();
		} catch (CoreException e) {
			Plugin.log(e);
			return;
		}
		for (int i = 0; i<resArray.length; i++) {
			addIResource(resArray[i]);
		}
	}

	
	
	protected void addFile(IFile theFile) {
		String extension = theFile.getFileExtension();
		if (extension!=null && (extension.equalsIgnoreCase("p") || extension.equalsIgnoreCase("w")))
			fileList.add(theFile);
	}

	
	
	protected void addIResource(IResource theRes) {
		if (theRes==null) return;
		if (theRes.getType()==IResource.FILE) addFile((IFile)theRes);
		else if (	theRes.getType()==IResource.FOLDER
				||	theRes.getType()==IResource.PROJECT
				) addContainer((IContainer)theRes);
	}

	
	
	protected void checkForProjectChange(IResource theResource) {
		String projName = theResource.getProject().getName();
		if (currProject!=null && projName.equals(currProject)) return;
		try {
			currProject = projName;
			refPack.loadProject(currProject);
		} catch (Throwable e) {
			console("Error Loading Project Settings\n" + e.toString());
			Plugin.log(e);
			return;
		}
	}


	
	/** Check that the editor is not "dirty", give option to save if it is.
	 * @return true if clean or saved, false if still dirty.
	 */
	public boolean checkSaved(AbstractTextEditor textEditor) {
		if (! textEditor.isDirty()) return true;
		boolean ok = MessageDialog.openConfirm(window.getShell(), "ProRefactor"
			, "Your changes must be saved before the refactor is run. Save changes?" );
		if (! ok) return false;
		textEditor.doSave(null);
		return true;
	}


	
	/**
	 * Write a message to the console.
	 */
	public synchronized void console(String theMessage) {
		consoleStream.print(theMessage);
	}


	
	/**
	 * Clear the console.
	 */
	public void consoleClear() {
		try {
			console.getDocument().replace(0, console.getDocument().getLength(), "");
		} catch (Exception e) {
			Plugin.log(e);
		}
	}


	
	/**
	 * Run the refactoring for the current selection.
	 */
	public int doRun() {
		IRunnableWithProgress op = null;
		currIter = fileList.iterator();
		workDone = 0; 
		parserMessages = new ParserMessages(); 
		try {
			op = new IRunnableWithProgress() {
				public ActionManager outerclass = null;
				public void run(IProgressMonitor monitor) throws InterruptedException {
						run2(monitor, currIter);
				}
			};
			while (currIter.hasNext()) {
				theDialog = new SpecialMonitorDialog(window.getShell(), fileList.size());
				theDialog.run(true, true, op);
				String theRet = action.processTargetSet();
				if (theRet!=null && theRet.length()!=0) {
					if (theRet.equals("cancel")) {
						console("Canceled");
						break;
					}
					console(theRet);
				}
			}
		} catch (InvocationTargetException e) {
			reportError(org.prorefactor.core.Util.getExceptionText(e.getTargetException()));
			Plugin.log(e);
		} catch (InterruptedException e) {
			console("\n" + e.getMessage());
			Plugin.log(e);
		} finally {
			if (errorMess!=null) {
				reportError(errorMess);
				errorMess = null;
			}
			if (parseErrors)
				reportError(
					"There were parse errors. See Console, or this file (in your working directory) for a list of errors: "
					+ ParserMessages.filename
					);
			parserMessages.close();
		}
		return 1;
	}


	
	/** Get the current java.io.File from the current IResource. */
	public File getCurrFile() {
		IFile ifile = (IFile) currResource;
		return new File(ifile.getLocation().toOSString());
	}

	
	
	/**
	 * Get the current resource.
	 * Useful if the Action class wants to examine the resource directly.
	 */
	public IResource getCurrResource() {
		return currResource;
	}

	
	
	/**
	 * Get the number of selected <b>files</b>.
	 * (May be different than the number of selected resources!)
	 */
	public int getNumSelected() {
		return fileList.size();
	}

	
	
	protected void loadSelectionList(ISelection iselection) {
		fileList = new ArrayList();
		if (iselection==null) return;
		IStructuredSelection selection = (IStructuredSelection) iselection;
		if (selection.isEmpty()) return;
		for (Iterator it = selection.iterator(); it.hasNext();) {
			IResource currRes = null;
			Object next = it.next();
			if (next instanceof IResource) currRes = (IResource)next;
			else if (next instanceof IAdaptable) { // necessary for Package Explorer
				Object resource = ((IAdaptable) next).getAdapter(IResource.class);
				if (resource != null) currRes = (IResource)resource;
			}
			addIResource(currRes);
		}
	}



	private boolean parse() {
		if (! doTheParse) return true;
		console("parse ");
		String currFilePath = currResource.getLocation().toOSString();
		int retVal = parser.parse(currFilePath);
		if (retVal<=0) {
			String theText = parser.errorGetText();
			console("\n" + theText + "\n");
			parserMessages.write(
				"Parsing " + currFilePath + "\n"
				+ theText + "\n"
				);
			parseErrors = true;
			return false;
		}
		console("OK ");
		topNode = parser.getHandle();
		parser.nodeTop(topNode);
		return true;
	}



	/**
	 * Shows an error message in a MessageDialog.
	 */
	public void reportError(String errMess) {
		MessageDialog.openError(window.getShell(), "ProRefactor", errMess);
		console(errMess);
	}



	public void run2(IProgressMonitor monitor, Iterator it) throws InterruptedException {
		while (it.hasNext()) {
			if (monitor.isCanceled()) throw new InterruptedException("Canceled"); 
			currResource = (IResource) it.next();
			workDone += 1;
			checkForProjectChange(currResource);
			if (preParse!=null) preParse.run(currResource);
			console(currResource.getName() + " ");
			if (!parse()) continue;
			if (monitor.isCanceled()) throw new InterruptedException("Canceled"); 
			RefactorResult result = action.processFile(topNode);
			if (result!=null) {
				if (result.message!=null && result.message.length()!=0)
					console("\n" + result.message + "\n");
				if (result.exception!=null)
					throw new InterruptedException(result.exception.getMessage());
				if (result.breakout) return;
			}
			console("\n");
			theDialog.didOne();
		} // for each resource in resources list
		console("Finished\n");
	}



	/** Set up this ActionManager.
	 * Requires a non-empty ISelection.
	 * @return success>0, fail<=0
	 */
	public int setup(ISelection iselection) {
		loadSelectionList(iselection);
		if (fileList!=null && ! fileList.isEmpty()) {
			currResource = (IResource) fileList.get(0);
		} else { 
			// Try to get the IFile from the current editor.
			IEditorPart textEditor = Plugin.getActiveWorkbenchWindow().getActivePage().getActiveEditor();
			if (	textEditor==null
				||	(! (textEditor.getEditorInput() instanceof IFileEditorInput) )
				) {
				reportError("Nothing selected");
				return -1;
			}
			IFile theFile = ((IFileEditorInput) textEditor.getEditorInput()).getFile();
			fileList.add(theFile);
			currResource = theFile;
		}
		try {
			parser = ProparseLdr.getInstance();
			parser.configSet("show-proparse-directives", "true");
		} catch (Throwable e) {
			reportError("Error Loading Proparse\n" + e.toString());
			Plugin.log(e);
			return -2;
		}
		try {
			refPack = RefactorSession.getInstance();
			currProject = ((IResource)fileList.get(0)).getProject().getName();
			refPack.loadProject(currProject);
		} catch (Throwable e) {
			reportError("Error Loading Project Settings\n" + e.toString());
			Plugin.log(e);
			return -3;
		}
		try {
			showConsole();
			consoleClear();
		} catch (PartInitException e) {
			reportError(e.getMessage());
			Plugin.log(e);
			return -4;
		}
		return 1;
	}


	
	protected synchronized void showConsole() throws PartInitException {
		IConsoleView consoleView =
			(IConsoleView) Plugin.getActivePage()
				.showView("org.eclipse.ui.console.ConsoleView");
		ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] {console});
 		console.createPage(consoleView);
	}


}