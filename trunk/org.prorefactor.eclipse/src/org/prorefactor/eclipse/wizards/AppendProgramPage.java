/* AppendProgramPage.java
 * Created on Dec 15, 2003
 * John Green
 *
 * Copyright (C) 2003 Joanju Limited
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.prorefactor.eclipse.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.prorefactor.refactor.FileStuff;


import java.io.File;


public class AppendProgramPage extends WizardPage {

	public AppendProgramPage(String pageName, File firstFile) {
		super(pageName);
		setPageComplete(false);
	}

	private File secondFile;
	private Label filenameLabel;
	private Text filenameText;
	private Text messageText;



	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setFont(parent.getFont());
		initializeDialogUnits(parent);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		createContents(composite);
		setPageComplete(validatePage());
		// Show description on opening
		setErrorMessage(null);
		setMessage("");
		setControl(composite);
		filenameText.setFocus();
	}



	private void createContents(Composite parent) {
		Font font = parent.getFont();
		messageText = new Text(parent, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP);
		messageText.setFont(font);
		messageText.setText(
			"Enter the name of the program file to append to this program file. Both files must be compilable."
			+ "\n\n"
			+ "Changes made by this refactoring can be undone by choosing Roll Back from the Progress Refactoring menu."
			+ "\n\n"
			+ "After refactoring, for a side-by-side diff review of changes made to an individual file,\nchoose Compare With -> Local History from the Navigator pop-up menu."
			);

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout (2, false);
		composite.setLayout(layout);

		filenameLabel = new Label(composite, SWT.NONE);
		filenameLabel.setText("&File to append:");
		filenameLabel.setFont(font);

		// project location entry field
		filenameText = new Text(composite, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = 250;
		filenameText.setLayoutData(data);
		filenameText.setFont(font);
		filenameText.addListener(
			SWT.Modify
			, new Listener() {
				public void handleEvent(Event e) {
					setPageComplete(validatePage());
				}
			}
			);

	}



	public File getSecondFile() {
		return secondFile;
	}



	private String getFilenameValue() {
		if (filenameText == null) return "";
		return filenameText.getText().trim();
	}



	protected boolean validatePage() {
		String filenameValue = getFilenameValue();
		if (filenameValue.equals("")) {
			setErrorMessage(null);
			setMessage("Enter name of file to append");
			return false;
		}
		secondFile = FileStuff.findFile(filenameValue);
		if (secondFile==null || (! secondFile.isFile()) ) {
			setErrorMessage("File not found");
			return false;
		}
		setErrorMessage(null);
		setMessage(null);
		return true;
	} // validatePage()



	public void setVisible(boolean visible) {
		super.setVisible(visible);
	}


}
