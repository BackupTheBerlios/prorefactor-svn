/* RefactorWizardDialog.java
 * Created on Mar 24, 2004
 * John Green
 *
 * Copyright (C) 2004 Joanju Limited
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.prorefactor.eclipse.wizards;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/** Extends with optional buttons, especially: a Preview button.
 * The wizard must be an instance of org.prorefactor.eclipse.wizards.RefactorWizard.
 */
public class RefactorWizardDialog extends WizardDialog {

	public RefactorWizardDialog(Shell parentShell, RefactorWizard newWizard) {
		super(parentShell, newWizard);
		this.refactorWizard = newWizard;
	}
	
	Button previewButton;
	RefactorWizard refactorWizard;

	protected void createButtonsForButtonBar(Composite parent) {
		if (refactorWizard.needsPreviewButton()) {
			// increment the number of columns in the button bar
			((GridLayout) parent.getLayout()).numColumns++;
			previewButton = new Button(parent, SWT.PUSH);
			previewButton.setText("&Preview");
			setButtonLayoutData(previewButton);
			previewButton.setFont(parent.getFont());
			previewButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					previewPressed();
				}
			});
		}
		super.createButtonsForButtonBar(parent);
	}


	void previewPressed() {
		refactorWizard.setPreview(true);
		finishPressed();
	}



	public void updateButtons() {
		if (previewButton != null) previewButton.setEnabled(getWizard().canFinish());
		super.updateButtons();
	}



} // class
