/* Preview.java
 * Created on Mar 22, 2004
 * John Green
 *
 * Copyright (C) 2004 Joanju Limited
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.prorefactor.eclipse.dialogs;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareViewerPane;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;


/** Preview changes to a source file.
 * Usage: new..., open(), getUserInput().
 */
public class Preview {

	/**
	 * @param left Left text
	 * @param right Right text
	 * @param windowTitle Window title
	 * @param parentShell Parent shell
	 */
	public Preview(String left, String right, String windowTitle, Shell parentShell) {
		this.leftText = left;
		this.rightText = right;
		this.windowTitle = windowTitle;
		this.parentShell = parentShell;
	}



	// Member data
	boolean changesAccepted = false;
	boolean displayResponsible = false;
	boolean standalone = false;
	CompareConfiguration config;
	CompareViewerPane viewPane;
	Display display;
	Shell parentShell = null;
	Shell shell;
	String leftText;
	String rightText;
	String windowTitle;
	TextMergeViewer viewer;



	class Input implements ITypedElement, IStreamContentAccessor {
		String fContent;
		Input(String s) {
			fContent= s;
		}
		public Image getImage() { return null; }
		public String getName() { return "Some name"; }
		public String getType() { return "txt"; }
		public InputStream getContents() {
			return new ByteArrayInputStream(fContent.getBytes());
		}
	}



	/** Most of this doesn't work without an Eclipse workbench, but at least we can
	 * see the layout.
	 */
	public static void main(String [] args) {
		Preview preview = new Preview(
			"left"
			, "right"
			, "Insert Witty Title Here"
			, null // parentShell
			);
		preview.standalone = true;
		preview.getUserInput();
	}



	/**
	 * Wait for the user to close the dialog.
	 * Sets up the readAndDispatch loop.
	 * @return true on OK, false on Cancel
	 */
	public boolean getUserInput() {
		open();
		// This can only be called from an Eclipse workbench, not from a standalone launch.
		if (!standalone)
			viewer.setInput(new DiffNode(new Input(leftText), new Input(rightText)));
		while (!shell.isDisposed())
			if (!display.readAndDispatch()) display.sleep();
		if (displayResponsible) display.dispose();
		return changesAccepted;
	} // wait()



	void createShell() {

		Button button;
		GridData griddata;

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;

		shell = new Shell(
			display, SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL | SWT.MAX | SWT.CLOSE );
		if (parentShell!=null) {
			shell.setImage(parentShell.getImage());
			shell.setFont(parentShell.getFont());
		}

		// TextMergeViewer
		viewPane = new CompareViewerPane(shell, SWT.BORDER | SWT.FLAT);
		config = new CompareConfiguration(null);
		viewer = new TextMergeViewer(viewPane, SWT.BORDER, config);
		viewPane.setLayoutData(new GridData(GridData.FILL_BOTH));
		viewPane.setContent(viewer.getControl());


		// Group for the buttons to be in
		Composite buttonCompo = new Composite(shell, SWT.NONE);
		griddata = new GridData(GridData.HORIZONTAL_ALIGN_END);
		buttonCompo.setLayoutData(griddata);
		RowLayout rowLayout = new RowLayout();
		rowLayout.wrap = false;
		rowLayout.pack = false;
		buttonCompo.setLayout (rowLayout);


		// "OK" button
		button = new Button(buttonCompo, SWT.PUSH);
		button.setText("OK");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				changesAccepted = true;
				shell.close();
			}
		});


		// "Cancel" button
		button = new Button(buttonCompo, SWT.PUSH);
		button.setText("Cancel");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				changesAccepted = false;
				shell.close();
			}
		});


		// Shell settings
		shell.setText(windowTitle);
		shell.setLayout(gridLayout);
		shell.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
			}
		});


	} // createShell()



	void open() {
		if (Display.getCurrent() == null) displayResponsible = true;
		display = Display.getDefault();
		createShell();
		shell.open();
	}



} // class
