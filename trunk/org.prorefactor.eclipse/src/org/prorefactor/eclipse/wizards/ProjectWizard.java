/* ProjectWizard.java
 * 2003 John Green
 *
 * Copyright (C) 2003-2004 Joanju Limited
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.prorefactor.eclipse.wizards;


import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
import org.prorefactor.eclipse.Plugin;
import org.prorefactor.refactor.RefactorSession;



/**
 * Create a ProRefactor Project.
 */
public class ProjectWizard extends BasicNewProjectResourceWizard {

	public boolean performFinish() {
		if (! super.performFinish()) return false;
		try {
			RefactorSession refPack = RefactorSession.getInstance();
			IProject project = super.getNewProject();
			IProjectDescription description = project.getDescription();
			String[] natures = description.getNatureIds();
			String[] newNatures = new String[natures.length + 1];
			System.arraycopy(natures, 0, newNatures, 0, natures.length);
			newNatures[natures.length] = org.prorefactor.eclipse.ProRefactorNature.id;
			description.setNatureIds(newNatures);
			project.setDescription(description, null);
			// Set the default PROPATH for the project. (The project top directory.)
			refPack.loadProject(project.getName());
			refPack.getProgressSettings().propath = project.getLocation().toString();
			refPack.getProgressSettings().saveSettings();
			// Force reset of parser settings, to set Proparse's propath.
			refPack.loadProjectForced(project.getName());
		} catch (Exception e) {
			MessageDialog.openError(new org.eclipse.swt.widgets.Shell(),"Create Progress Project",e.getMessage());
			Plugin.log(e);
		}
		return true;
	}

}
