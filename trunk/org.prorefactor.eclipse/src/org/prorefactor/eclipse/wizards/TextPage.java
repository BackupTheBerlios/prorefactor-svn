/* TextPage.java
 * Created on Dec 13, 2003
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.FillLayout;


/** A wizard page which displays a read-only edit widget full of text.
 * Used for displaying information about the action, such as user help.
 * See the constructor for important usage notes.
 */
public class TextPage extends WizardPage {

	/** Important - use WizardDialog.create() to create the dialog
	 * before setting this page's text. This ensures that the dialog's size
	 * is set before the size of the Text widget is calculated. Otherwise,
	 * the Text widget may use it's contained text to calculate a widget
	 * width which is the full width of the screen, which looks pretty stupid.
	 */
	public TextPage(String pageName) {
		super(pageName);
		setPageComplete(true);
	}

	private String displayText = "";
	private Text text;


	
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new FillLayout());
		composite.setFont(parent.getFont());
		initializeDialogUnits(parent);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		createContents(composite);
		setPageComplete(validatePage());
		setErrorMessage(null);
		setMessage("");
		setControl(composite);
	}

	private void createContents(Composite parent) {
		final Composite editorComposite = new Composite(parent, SWT.NONE);
		editorComposite.setLayout(new FillLayout());
		text = new Text(editorComposite, SWT.BORDER | SWT.MULTI | SWT.READ_ONLY | SWT.V_SCROLL | SWT.WRAP);
		text.setTabs(4);
		text.setText(displayText);
	}

	public void setText(String theText) {
		displayText = theText;
		text.setText(displayText);
	}

	protected boolean validatePage() {
		setErrorMessage(null);
		setMessage(null);
		return true;
	}

	public void setVisible(boolean visible) {
		super.setVisible(visible);
	}

}
