/* XMLWriterPage.java
 * Created on Nov 12, 2003
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



public class XMLWriterPage extends WizardPage {

	Button showHiddenButton;

	public boolean showHidden = false;

	public XMLWriterPage(String pageName) {
		super(pageName);
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
		showHiddenButton = new Button(parent, SWT.CHECK | SWT.RIGHT);
		showHiddenButton.setText("&Show Hidden Tokens");
		showHiddenButton.setSelection(showHidden);
		showHiddenButton.setFont(font);
		showHiddenButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				showHidden = showHiddenButton.getSelection();
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
