/* RefactorWizard.java
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

import org.eclipse.jface.wizard.*;


/** Base class for a Refactor wizard.
 * Contains an optional "target directory" page.
 * Additional pages can be added by client.
 */
public class RefactorWizard extends Wizard {

	private boolean finished = false;
	private boolean needsPreviewButton = false;
	private boolean preview = false;

	public RefactorWizard() {
		setWindowTitle("ProRefactor");
	}

	public boolean didFinish() {
		return finished;
	}

	public boolean isPreview() { return preview; }

	public boolean needsPreviewButton() { return needsPreviewButton; }

	public boolean performFinish() {
		finished = true;
		return true;
	}

	public void setNeedsPreviewButton(boolean b) { needsPreviewButton = b; }

	public void setPreview(boolean b) { preview = b; }

} // class
