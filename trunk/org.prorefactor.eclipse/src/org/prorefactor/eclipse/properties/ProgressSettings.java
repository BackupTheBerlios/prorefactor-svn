/* ProgressSettings.java
 * 2003 John Green
 *
 * Copyright (C) 2003-2004 Joanju Limited
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.prorefactor.eclipse.properties;

import java.io.FileNotFoundException;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.refactor.settings.ProgressProjectSettings;


public class ProgressSettings extends PropertyPage {

	private Composite composite = null;
	private static final int TEXT_FIELD_WIDTH = 100;

	// When adding fields, look for: fields!
	private Button batchmode;
	private Text dbAliases;
	private Text opsys;
	private Text propath;
	private Text proversion;
	private Text windowSystem;

	private ProgressProjectSettings progressSettings = null;

	public ProgressSettings() {
		super();
	}

	private void addSection(Composite parent) {
		composite = createDefaultComposite(parent);
		boolean noFile = false;

		try {
			String projectName = ((IProject)getElement()).getName();
			String settingsFile = RefactorSession.getProgressSettingsFilename(projectName);
			progressSettings = new ProgressProjectSettings(settingsFile);
			progressSettings.loadSettings();
		} catch (FileNotFoundException e) {
			noFile = true;
		} catch (Throwable e) {
			MessageDialog.openError(getShell(), "Problem opening settings", e.getMessage());
		}

		// fields! - create fields here
		batchmode = addButtonField("&Batch Mode");
		dbAliases = addTextField("DB Aliase&s");
		opsys = addTextField("&Opsys");
		propath = addTextField("&Propath");
		proversion = addTextField("Pro&version");
		windowSystem = addTextField("&Window System");

		if (noFile) {
			performDefaults();
		} else {
			// fields! - get stored settings here
			batchmode.setSelection(progressSettings.batchmode);
			dbAliases.setText(progressSettings.dbAliases);
			opsys.setText(progressSettings.opsys);
			propath.setText(progressSettings.propath);
			proversion.setText(progressSettings.proversion);
			windowSystem.setText(progressSettings.windowSystem);
		}
	}

	private Button addButtonField(String title) {
		Label theLabel = new Label(composite, SWT.NONE);
		theLabel.setText(title);
		Button theButton = new Button(composite, SWT.CHECK);
		GridData gd = new GridData();
		theButton.setLayoutData(gd);
		return theButton;
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

	protected void performDefaults() {
		// fields! - IDE specific default values from here
		batchmode.setSelection(false);
		dbAliases.setText("");
		opsys.setText("WIN32");
		propath.setText(".");
		proversion.setText("");
		windowSystem.setText("MS-WIN95");
	}
	
	public boolean performOk() {
		try {
			// fields! - store persistent settings here
			progressSettings.batchmode = batchmode.getSelection();
			progressSettings.dbAliases = dbAliases.getText();
			progressSettings.opsys = opsys.getText();
			progressSettings.propath = propath.getText();
			progressSettings.proversion = proversion.getText();
			progressSettings.windowSystem = windowSystem.getText();
			progressSettings.saveSettings();
			RefactorSession.invalidateCurrentSettings();
		} catch (Throwable e) {
			MessageDialog.openError(getShell(),	"Error saving settings", e.toString());	
			return false;
		}
		return true;
	}

}