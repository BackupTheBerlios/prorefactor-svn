/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.prorefactor.eclipse.preferences;

import java.io.FileNotFoundException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.refactor.settings.ApplicationSettings;


public class PreferencePage extends PropertyPage implements IWorkbenchPreferencePage {

	public PreferencePage() {
		super();
	}

	private Composite composite = null;
	private static final int TEXT_FIELD_WIDTH = 75;

	// When adding fields, look for: fields!
	private Text externalEditor;

	private ApplicationSettings appSettings = null;

	private void addSection(Composite parent) {
		composite = createDefaultComposite(parent);
		boolean noFile = false;

		try {
			appSettings = new ApplicationSettings(RefactorSession.getAppSettingsFilename());
			appSettings.loadSettings();
		} catch (FileNotFoundException e) {
			noFile = true;
		} catch (Throwable e) {
			MessageDialog.openError(getShell(), "Problem opening settings", e.getMessage());
		}

		// fields! - create fields here
		externalEditor = addTextField("&External Editor");

		if (noFile) {
			performDefaults();
		} else {
			// fields! - get stored settings here
			externalEditor.setText(appSettings.externalEditor);
		}
	}

	private Text addTextField(String title) {
		Label theLabel = new Label(composite, SWT.NONE);
		theLabel.setText(title);
		Text theText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		GridData gd = new GridData();
		gd.widthHint = convertWidthInCharsToPixels(TEXT_FIELD_WIDTH);
		theText.setLayoutData(gd);
		return theText;
	}

	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);

		addSection(composite);
		return composite;
	}

	private Composite createDefaultComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);

		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);

		return composite;
	}

	public void init(IWorkbench workbench) {
	}

	protected void performDefaults() {
		// fields! - IDE specific default values from here
		externalEditor.setText("");
	}
	
	public boolean performOk() {
		try {
			// fields! - store persistent settings here
			appSettings.externalEditor = externalEditor.getText();
			appSettings.saveSettings();
		} catch (Throwable e) {
			MessageDialog.openError(getShell(),	"Error saving settings", e.getMessage());	
			return false;
		}
		return true;
	}


}
