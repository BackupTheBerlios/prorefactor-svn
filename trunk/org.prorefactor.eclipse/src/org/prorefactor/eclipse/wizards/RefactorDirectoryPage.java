/* RefactorDirectoryPage.java
 * Created on Oct 9, 2003
 * John Green
 *
 * Copyright (C) 2003 Joanju Limited
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.prorefactor.eclipse.wizards;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;


public class RefactorDirectoryPage extends WizardPage {

	private Button browseButton;
	private Label locationLabel;
	private Text locationPathField;
	private String selectedPath = null;

	private Listener locationModifyListener = new Listener() {
		public void handleEvent(Event e) {
			setPageComplete(validatePage());
		}
	};

	private static final int SIZING_TEXT_FIELD_WIDTH = 250;
	public static final String CONTEXT_HELP_ID = "org.prorefactor.doc.outputDirPage";


	public RefactorDirectoryPage(String pageName) {
		super(pageName);
		setPageComplete(false);
	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setFont(parent.getFont());
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, CONTEXT_HELP_ID);

		initializeDialogUnits(parent);

		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		createProjectLocationGroup(composite);
		setPageComplete(validatePage());
		// Show description on opening
		setErrorMessage(null);
		setMessage("Enter output directory. Choose Browse to select or create.\nOutput directory must be empty.");
		setControl(composite);
	}

	private void createProjectLocationGroup(Composite parent) {

		Font font = parent.getFont();
		// project specification group
		Group projectGroup = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		projectGroup.setLayout(layout);
		projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		projectGroup.setFont(font);

		createUserSpecifiedProjectLocationGroup(projectGroup);

	}

	private void createUserSpecifiedProjectLocationGroup(Composite projectGroup) {

		Font font = projectGroup.getFont();

		// location label
		locationLabel = new Label(projectGroup, SWT.NONE);
		locationLabel.setText("&Output Directory");
		locationLabel.setFont(font);

		// project location entry field
		locationPathField = new Text(projectGroup, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH;
		locationPathField.setLayoutData(data);
		locationPathField.setFont(font);

		// browse button
		browseButton = new Button(projectGroup, SWT.PUSH);
		browseButton.setText("B&rowse...");
		browseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				handleLocationBrowseButtonPressed();
			}
		});

		browseButton.setFont(font);
		setButtonLayoutData(browseButton);

		locationPathField.setText(Platform.getLocation().toOSString());
		locationPathField.addListener(SWT.Modify, locationModifyListener);
	}

	/**
	 * Returns the value of the project location field
	 * with leading and trailing spaces removed.
	 * 
	 * @return the project location directory in the field
	 */
	private String getProjectLocationFieldValue() {
		if (locationPathField == null)
			return ""; //$NON-NLS-1$
		return locationPathField.getText().trim();
	}

	/** If the wizard "Finish" was completed, use this to get the selected directory.
	 */
	public String getSelectedPath() {
		return selectedPath;
	}

	/**
	 *	Open an appropriate directory browser
	 */
	void handleLocationBrowseButtonPressed() {
		DirectoryDialog dialog =
			new DirectoryDialog(locationPathField.getShell());
		dialog.setMessage("Select or create refactoring output directory. Directory must be empty.");

		String dirName = getProjectLocationFieldValue();

		if (!dirName.equals("")) { //$NON-NLS-1$
			File path = new File(dirName);
			if (path.exists() && path.isDirectory())
				dialog.setFilterPath((new Path(path.getAbsolutePath())).toOSString());
		}

		String selectedDirectory = dialog.open();
		if (selectedDirectory != null) {
			locationPathField.setText(selectedDirectory);
		}
	}

	/**
	 * Returns whether this page's controls currently all contain valid 
	 * values.
	 *
	 * @return <code>true</code> if all controls are valid, and
	 *   <code>false</code> if at least one is invalid
	 */
	protected boolean validatePage() {

		String locationFieldContents = getProjectLocationFieldValue();
		selectedPath = null;

		if (locationFieldContents.equals("")) {
			setErrorMessage(null);
			setMessage("Enter output directory");
			return false;
		}

		IPath path = new Path(""); //$NON-NLS-1$
		if (!path.isValidPath(locationFieldContents)) {
			setErrorMessage("Directory does not exist");
			return false;
		}

		File outputFile = new File(locationFieldContents);
		if (!outputFile.isDirectory()) {
			setErrorMessage("Not a directory");
			return false;
		}

		if (outputFile.listFiles().length > 0) {
			setErrorMessage("That directory is not empty");
			return false;
		}

		selectedPath = locationFieldContents;
		setErrorMessage(null);
		setMessage(null);
		return true;
	} // validatePage()

	/*
	 * see @DialogPage.setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			locationPathField.setFocus();
			locationPathField.selectAll();
		}
	}

}
