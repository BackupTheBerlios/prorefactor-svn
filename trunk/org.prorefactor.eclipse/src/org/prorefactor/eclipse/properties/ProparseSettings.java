/* ProparseSettings.java
 * 2003 John Green
 * 
 * Copyright (C) 2003 Joanju Limited
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.prorefactor.eclipse.properties;



import org.eclipse.core.resources.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.refactor.settings.ProparseProjectSettings;

import java.io.*;

public class ProparseSettings extends PropertyPage {

	private Composite composite = null;
	private ProparseProjectSettings proparseSettings = null;
	private static final int TEXT_FIELD_WIDTH = 75;

	// When adding fields, look for: fields!
	protected Button tabButton = null;
	private Button capKeyword;
	protected Combo spacesCombo = null;
	private Text keywordall;
//	private Text rCodeDir;
	private Text schemaFile;

	public ProparseSettings() {
		super();
	}

	private void addSection(Composite parent) {
		composite = createDefaultComposite(parent);

		try {
			String projectName = ((IProject)getElement()).getName();
			String settingsFile = RefactorSession.getProparseSettingsFilename(projectName);
			proparseSettings = new ProparseProjectSettings(settingsFile, projectName);
			proparseSettings.loadSettings();
		} catch (FileNotFoundException e) {
		} catch (Throwable e) {
			MessageDialog.openError(getShell(), "Problem opening settings", e.getMessage());
		}

		// fields! - create fields here
		capKeyword = addButtonField("&Capitalize Keywords");
		keywordall = addTextField("&Keyword-all");
//		rCodeDir = addTextField("&r-code Directory");
		schemaFile = addTextField("&Schema File");
		addIndentGroup();

		// fields! - get stored settings here
		capKeyword.setSelection(proparseSettings.capKeyword);
		keywordall.setText(proparseSettings.keywordall);
//		rCodeDir.setText(proparseSettings.rCodeDir);
		schemaFile.setText(proparseSettings.schemaFile);
		tabButton.setSelection(proparseSettings.indentTab);
		spacesCombo.setText(Integer.toString(proparseSettings.indentSpaces));
		spacesCombo.setEnabled(! tabButton.getSelection());

	} // addSection



	private Button addButtonField(String title) {
		Label theLabel = new Label(composite, SWT.NONE);
		theLabel.setText(title);
		Button theButton = new Button(composite, SWT.CHECK);
		GridData gd = new GridData();
		theButton.setLayoutData(gd);
		return theButton;
	}



	private void addIndentGroup() {
		final Group indentGroup = new Group(composite, SWT.NONE);
		indentGroup.setText("Indent");
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 4;
		indentGroup.setLayout(gridLayout);
		final GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.horizontalSpan = 8;
		indentGroup.setLayoutData(gridData);
		{
			tabButton = new Button(indentGroup, SWT.CHECK);
			tabButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					spacesCombo.setEnabled(! tabButton.getSelection());
				}
			});
			tabButton.setText("Tab");
		}
		{
			spacesCombo = new Combo(indentGroup, SWT.READ_ONLY);
			final GridData gridData_1 = new GridData();
			gridData_1.widthHint = 32;
			spacesCombo.setLayoutData(gridData_1);
			spacesCombo.setItems(new String[] { "1", "2", "3", "4", "5", "6", "7", "8" });
			spacesCombo.setText("3");
		}
		{
			final Label spacesLabel = new Label(indentGroup, SWT.NONE);
			spacesLabel.setText("Spaces");
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

	protected void performDefaults() {
		// fields! - IDE specific default values from here
		capKeyword.setSelection(true);
		keywordall.setText("");
//		rCodeDir.setText("");
		schemaFile.setText("");
		tabButton.setSelection(false);
		spacesCombo.setText("3");
	}

	public boolean performOk() {
		try {
			// fields! - store persistent settings here
			proparseSettings.capKeyword = capKeyword.getSelection();
			proparseSettings.keywordall = keywordall.getText();
//			proparseSettings.rCodeDir = rCodeDir.getText();
			proparseSettings.schemaFile = schemaFile.getText();
			proparseSettings.indentTab = tabButton.getSelection();
			proparseSettings.indentSpaces = spacesCombo.getSelectionIndex() + 1;
			proparseSettings.saveSettings();
			RefactorSession.invalidateCurrentSettings();
		} catch (Throwable e) {
			MessageDialog.openError(getShell(),	"Error saving settings", e.getMessage());	
			return false;
		}
		return true;
	}

}