/* RenamePage.java
 * Sep 13, 2004
 * John Green
 *
 * Copyright (C) 2004 Joanju Limited
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.prorefactor.eclipse.wizards;

import java.io.File;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;



/** Options for schema table and field renaming. */
public class RenamePage extends WizardPage {

	public RenamePage(String pageName) {
		super(pageName);
		setPageComplete(false);
	}

	Text fileName;
	Text inputFromWidget = null;
	Text namePairsEditor;
	String returnText;
	
	private Listener changeListener = new Listener() {
		public void handleEvent(Event e) {
			setPageComplete(validatePage());
		}
	};
	
	

	void browseButtonPressed() {
		FileDialog dialog = new FileDialog(fileName.getShell());
		String dirName = fileName.getText();
		if (!dirName.equals("")) {
			File path = new File(dirName);
			if (path.exists()) {
				if (! path.isDirectory()) path = path.getParentFile();
				dialog.setFilterPath((new Path(path.getAbsolutePath())).toOSString());
			}
		}
		String selectedFile = dialog.open();
		if (selectedFile != null) {
			fileName.setText(selectedFile);
		}
	}

	

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setFont(parent.getFont());
		initializeDialogUnits(parent);
		composite.setLayout(new GridLayout());
		final GridData gridData = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(gridData);
		createContents(composite);
		setPageComplete(validatePage());
		setControl(composite);
	} // createControl


	
	private void createContents(Composite parent) {
		final Group inputFromGroup = new Group(parent, SWT.NONE);
		inputFromGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		inputFromGroup.setLayout(gridLayout);

		final Button inputFromFileButton = new Button(inputFromGroup, SWT.RADIO);
		inputFromFileButton.setText("Input from &File");

		final Button inputFromEditorButton = new Button(inputFromGroup, SWT.RADIO);
		inputFromEditorButton.setText("Input from &Editor");

		final Composite fileComposite = new Composite(parent, SWT.NONE);
		fileComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		final GridLayout gridLayout_1 = new GridLayout();
		gridLayout_1.numColumns = 2;
		fileComposite.setLayout(gridLayout_1);
		final GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		fileComposite.setLayoutData(gridData);

		final Label fileLabel = new Label(fileComposite, SWT.NONE);
		final GridData gridData_1 = new GridData();
		gridData_1.horizontalSpan = 2;
		fileLabel.setLayoutData(gridData_1);
		fileLabel.setText("&Input from File:");

		fileName = new Text(fileComposite, SWT.BORDER);
		fileName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fileName.setToolTipText("Enter file name to get renaming list from");
		fileName.addListener(SWT.Modify, changeListener);

		final Button browseButton = new Button(fileComposite, SWT.NONE);
		browseButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		browseButton.setText("&Browse");

		final Composite editorComposite = new Composite(parent, SWT.NONE);
		final GridData gridData_3 = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_VERTICAL);
		editorComposite.setLayoutData(gridData_3);
		editorComposite.setLayout(new GridLayout());

		final Label editorLabel = new Label(editorComposite, SWT.NONE);
		editorLabel.setText("I&nput from Editor:");

		namePairsEditor = new Text(editorComposite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		namePairsEditor.setToolTipText("Enter before/after name pairs, delimited by space, tabs, or newlines");
		namePairsEditor.setTabs(4);
		final GridData gridData_2 = new GridData(GridData.FILL_BOTH);
		namePairsEditor.setLayoutData(gridData_2);
		namePairsEditor.addListener(SWT.Modify, changeListener);
	
		browseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				browseButtonPressed();
				setPageComplete(validatePage());
			}
		});
		
		inputFromFileButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				fileName.setEnabled(true);
				browseButton.setEnabled(true);
				namePairsEditor.setEnabled(false);
				inputFromWidget = fileName;
				setPageComplete(validatePage());
			}
		});
		
		inputFromEditorButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				fileName.setEnabled(false);
				browseButton.setEnabled(false);
				namePairsEditor.setEnabled(true);
				inputFromWidget = namePairsEditor;
				setPageComplete(validatePage());
			}
		});
		
		// Set up defaults
		inputFromFileButton.setSelection(true);
		fileName.setEnabled(true);
		browseButton.setEnabled(true);
		namePairsEditor.setEnabled(false);
		inputFromWidget = fileName;

	} // createContents
	
	

	/** Get the input text - either the file name or the name pairs from the UI editor.
	 * @see RenamePage#isGetFromFile()
	 */
	public String getText() {
		return returnText;
	}

	
	
	/** Get the name pairs from a file? If not, then the name pairs came from the editor.
	 * @see RenamePage#getText()
	 */
	public boolean isGetFromFile() {
		return inputFromWidget == fileName;
	}
	
	
	
	public void setVisible(boolean visible) {
		super.setVisible(visible);
	}


	
	protected boolean validatePage() {
		if (inputFromWidget==fileName) {
			returnText = fileName.getText();
			if (returnText.length()==0)
				return valMsg("Enter text file to get name pairs from");
			File f = new File(returnText);
			if (! f.exists())
				return valErr("File does not exist");
			if (! f.isFile())
				return valErr("Not a file");
		} else {
			returnText = namePairsEditor.getText();
			if (returnText.trim().length() < 5)
				return valMsg("Enter old/new name pairs");
		}
		setErrorMessage(null);
		setMessage(null);
		return true;
	} // validatePage()
	
	
	
	private boolean valErr(String s) {
		setMessage(null);
		setErrorMessage(s);
		return false;
	}

	
	
	private boolean valMsg(String s) {
		setMessage(s);
		setErrorMessage(null);
		return false;
	}


}
