/* RefactorNamesPage.java
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

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.prorefactor.refactor.tfnames.TFNamesRefactor;
// import org.eclipse.swt.widgets.Label;




public class RefactorNamesPage extends WizardPage {

	TFNamesRefactor refactor = null;

	private Group caseGroup;
	Button
		lowerButton, upperButton, qualifyDbButton, tempTablesButton,
		qualifyTableButton, unabbreviateButton, fixCaseButton, qualifyButton;

	public RefactorNamesPage(String pageName, TFNamesRefactor refactorObject) {
		super(pageName);
		refactor = refactorObject;
		setPageComplete(true);
	}

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
	}

	private void createContents(Composite parent) {

		Font font = parent.getFont();

		GridLayout gridLayout;

		caseGroup = new Group(parent, SWT.NONE);
		// caseGroup.setText("Fixes and Code Gen");
		gridLayout = new GridLayout ();
		caseGroup.setLayout(gridLayout);
		gridLayout.numColumns = 2;
		caseGroup.setLayoutData (new GridData (GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));

		lowerButton = new Button(caseGroup, SWT.RADIO);
		lowerButton.setText("use &lowercase names");
		lowerButton.setSelection(refactor.useLowercase);
		lowerButton.setFont(font);
		lowerButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				refactor.useLowercase = lowerButton.getSelection();
			}
		} );

		upperButton = new Button(caseGroup, SWT.RADIO);
		upperButton.setText("use &UPPERCASE names");
		upperButton.setSelection(!refactor.useLowercase);
		upperButton.setFont(font);
		upperButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				refactor.useLowercase = !upperButton.getSelection();
			}
		} );

//		qualifyGroup = new Group(parent, SWT.NONE);
//		qualifyGroup.setText("Qualify");
//		gridLayout = new GridLayout ();
//		qualifyGroup.setLayout(gridLayout);
//		gridLayout.numColumns = 2;
//		qualifyGroup.setLayoutData (new GridData (GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));

		qualifyButton = new Button(parent, SWT.CHECK | SWT.RIGHT);
		qualifyButton.setText("&Qualify field names with table/buffer name");
		qualifyButton.setSelection(refactor.qualify);
		qualifyButton.setFont(font);
		qualifyButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				refactor.qualify = qualifyButton.getSelection();
//				qualifyTableButton.setEnabled(qualifyButton.getSelection());
//				qualifyDbButton.setEnabled(qualifyButton.getSelection());
			}
		} );

//		//Find out how to do this "properly".
//		//I'm using an empty Label as a gridlayout filler...
//		Label label = new Label(qualifyGroup, SWT.NONE);
//
//		qualifyTableButton = new Button(qualifyGroup, SWT.RADIO);
//		qualifyTableButton.setText("&table.field");
//		qualifyTableButton.setSelection(!refactor.useDbQualifier);
//		qualifyTableButton.setFont(font);
//		qualifyTableButton.setEnabled(refactor.qualify);
//		qualifyTableButton.addSelectionListener(new SelectionAdapter() {
//			public void widgetSelected(SelectionEvent e) {
//				refactor.useDbQualifier = !qualifyTableButton.getSelection();
//			}
//		} );
//
//		qualifyDbButton = new Button(qualifyGroup, SWT.RADIO);
//		qualifyDbButton.setText("&db.table.field");
//		qualifyDbButton.setSelection(refactor.useDbQualifier);
//		qualifyDbButton.setFont(font);
//		qualifyDbButton.setEnabled(refactor.qualify);
//		qualifyDbButton.addSelectionListener(new SelectionAdapter() {
//			public void widgetSelected(SelectionEvent e) {
//				refactor.useDbQualifier = qualifyDbButton.getSelection();
//			}
//		} );

		unabbreviateButton = new Button(parent, SWT.CHECK | SWT.RIGHT);
		unabbreviateButton.setText("&Expand abbreviated names");
		unabbreviateButton.setSelection(true);
		unabbreviateButton.setFont(font);
		unabbreviateButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				refactor.unabbreviate = unabbreviateButton.getSelection();
			}
		} );

		fixCaseButton = new Button(parent, SWT.CHECK | SWT.RIGHT);
		fixCaseButton.setText("Change/fix &case on names");
		fixCaseButton.setSelection(true);
		fixCaseButton.setFont(font);
		fixCaseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				refactor.fixCase = fixCaseButton.getSelection();
			}
		} );

		tempTablesButton = new Button(parent, SWT.CHECK | SWT.RIGHT);
		tempTablesButton.setText("Also apply changes to &Work and Temp tables");
		tempTablesButton.setSelection(true);
		tempTablesButton.setFont(font);
		tempTablesButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				refactor.workTempTables = tempTablesButton.getSelection();
			}
		} );

	}

	protected boolean validatePage() {
		setErrorMessage(null);
		setMessage(null);
		return true;
	} // validatePage()

	public void setVisible(boolean visible) {
		super.setVisible(visible);
	}

}
