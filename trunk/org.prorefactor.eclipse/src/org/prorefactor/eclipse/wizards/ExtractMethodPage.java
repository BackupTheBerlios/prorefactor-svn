/* ExtractMethodPage.java
 * Created on Mar 24, 2004
 * John Green
 *
 * Copyright (C) 2004 Joanju Limited
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Based on org.eclipse.jdt.internal.ui.refactoring.code.ExtractMethodInputPage.
 */
package org.prorefactor.eclipse.wizards;


import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
//import org.eclipse.swt.events.SelectionAdapter;
//import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
//import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.dialogs.Dialog;
// import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.WizardPage;
import org.prorefactor.refactor.action.ExtractMethod;


public class ExtractMethodPage extends WizardPage {

	public ExtractMethodPage(ExtractMethod extractMethod) {
		super(PAGE_NAME);
		// setImageDescriptor(...);
		// setDescription(DESCRIPTION);
		fFirstTime= true;
		this.refactoring = extractMethod;
	}

	public static final String PAGE_NAME = "ExtractMethodPage";

	private ExtractMethod refactoring;
	private Text methodNameText;
	private boolean fFirstTime;
//	private Label fPreview;
//	private IDialogSettings fSettings;



	public void createControl(Composite parent) {
		loadSettings();
		
		Composite result= new Composite(parent, SWT.NONE);
		setControl(result);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		result.setLayout(layout);
//		GridData gd= null;
		
		initializeDialogUnits(result);
		
		Label label= new Label(result, SWT.NONE);
		label.setText("Method Name");
		
		methodNameText= createTextInputField(result, SWT.BORDER);
		methodNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		
//		label= new Label(result, SWT.NONE);
//		label.setText("Access Modifiers");
//		
//		Composite group= new Composite(result, SWT.NONE);
//		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		layout= new GridLayout();
//		layout.numColumns= 4; layout.marginWidth= 0;
//		group.setLayout(layout);
//		
//		String[] labels= new String[] {"public", "protected", "default", "private"};
//		String[] data= new String[] {"public", "protected", "", "private" };
//		String visibility= "public";
//		for (int i= 0; i < labels.length; i++) {
//			Button radio= new Button(group, SWT.RADIO);
//			radio.setText(labels[i]);
//			radio.setData(data[i]);
//			if (data[i].equals(visibility))
//				radio.setSelection(true);
//			radio.addSelectionListener(new SelectionAdapter() {
//				public void widgetSelected(SelectionEvent event) {
//					setVisibility((String)event.widget.getData());
//				}
//			});
//		}
		
//		if (!theRefactoring.getParameterInfos().isEmpty()) {
//			ChangeParametersControl cp= new ChangeParametersControl(result, SWT.NULL, 
//				RefactoringMessages.getString("ExtractMethodInputPage.parameters"), //$NON-NLS-1$
//				new IParameterListChangeListener() {
//				public void parameterChanged(ParameterInfo parameter) {
//					parameterModified(parameter);
//				}
//				public void parameterListChanged() {
//					updatePreview(getText());
//				}
//				public void parameterAdded(ParameterInfo parameter) {
//					updatePreview(getText());
//				}
//			}, true, false, false);
//			gd= new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
//			gd.horizontalSpan= 2;
//			cp.setLayoutData(gd);
//			cp.setInput(theRefactoring.getParameterInfos());
//		}
		
//		Button checkBox= new Button(result, SWT.CHECK);
//		checkBox.setText("Throw Runtime Exceptions");
//		checkBox.setSelection(fSettings.getBoolean(THROW_RUNTIME_EXCEPTIONS));
//		checkBox.addSelectionListener(new SelectionAdapter() {
//			public void widgetSelected(SelectionEvent e) {
//				setRethrowRuntimeException(((Button)e.widget).getSelection());
//			}
//		});
		
//		label= new Label(result, SWT.SEPARATOR | SWT.HORIZONTAL);
//		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		
//		label= new Label(result, SWT.NONE);
//		gd= new GridData();
//		gd.verticalAlignment= GridData.BEGINNING;
//		label.setLayoutData(gd);
//		label.setText("Signature Preview");
//		
//		fPreview= new Label(result, SWT.WRAP);
//		gd= new GridData(GridData.FILL_BOTH);
//		gd.widthHint= convertWidthInCharsToPixels(50);
//		fPreview.setLayoutData(gd);


		Dialog.applyDialogFont(result);
	}	



	private Text createTextInputField(Composite parent, int style) {
		Text result= new Text(parent, style);
		result.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				textModified(getNameText());
			}
		});
		return result;
	}
	
	protected String getNameText() {
		if (methodNameText == null) return null;
		return methodNameText.getText();	
	}
	
//	private void setVisibility(String s) {
//	}
	
//	private void setRethrowRuntimeException(boolean value) {
//		fSettings.put(THROW_RUNTIME_EXCEPTIONS, value);
//		theRefactoring.setThrowRuntimeExceptions(value);
//		updatePreview(getText());
//	}
	
//	private void updatePreview(String text) {
//		if (fPreview == null)
//			return;
//			
//		if (text.length() == 0)
//			text= "someMethodName";			 //$NON-NLS-1$
//			
//		fPreview.setText(theRefactoring.getSignature(text));
//	}
	
	private void loadSettings() {
//		fSettings= getDialogSettings().getSection(ExtractMethodWizard.DIALOG_SETTING_SECTION);
//		if (fSettings == null) {
//			fSettings= getDialogSettings().addNewSection(ExtractMethodWizard.DIALOG_SETTING_SECTION);
//			fSettings.put(THROW_RUNTIME_EXCEPTIONS, false);
//		}
//		theRefactoring.setThrowRuntimeExceptions(fSettings.getBoolean(THROW_RUNTIME_EXCEPTIONS));
	}
	
	//---- Input validation ------------------------------------------------------
	
	public void setVisible(boolean visible) {
		if (visible) {
			if (fFirstTime) {
				fFirstTime= false;
				setPageComplete(false);
//				updatePreview(getMethodName());
				methodNameText.setFocus();
			} else {
				setPageComplete(validatePage());
			}
		}
		super.setVisible(visible);
	}
	
	protected void textModified(String text) {
//		theRefactoring.setMethodName(text);
//		updatePreview(text);
		setPageComplete(validatePage());
	}
	
//	private void parameterModified(ParameterInfo parameter) {
//		updatePreview(getText());
//		setPageComplete(validatePage(false));
//	}
	
	private boolean validatePage() {
		return (validateMethodName());
	}
	
	private boolean validateMethodName() {
		String text = getNameText();
		if (text.length()==0) {
			setErrorMessage("Enter Method Name");
			return false;
		}
		refactoring.setMethodName(text);
		setErrorMessage(null);
		return true;
	}
	
//	private RefactoringStatus validateParameters() {
//		RefactoringStatus result= new RefactoringStatus();
//		List parameters= theRefactoring.getParameterInfos();
//		for (Iterator iter= parameters.iterator(); iter.hasNext();) {
//			ParameterInfo info= (ParameterInfo) iter.next();
//			if ("".equals(info.getNewName())) { //$NON-NLS-1$
//				result.addFatalError(RefactoringMessages.getString("ExtractMethodInputPage.validation.emptyParameterName")); //$NON-NLS-1$
//				return result;
//			}
//		}
//		result.merge(theRefactoring.checkParameterNames());
//		return result;
//	}


} // class
