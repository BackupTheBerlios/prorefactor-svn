/* Rollback.java
 * Created on Dec 3, 2003
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
import org.prorefactor.refactor.Rollback;


public class RollbackPage extends WizardPage {

	public RollbackPage(String pageName, Rollback rollback) {
		super(pageName);
		setPageComplete(true);
		this.rollback = rollback;
	}

	private Rollback rollback;
	private Text text;



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
		text = new Text(parent, SWT.MULTI | SWT.READ_ONLY);
		text.setText(
			"The following changes will be rolled back:\n"
			+ rollback.getChangeList()
			);
		text.setFont(font);
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
