package org.prorefactor.eclipse;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;

public class ProRefactorNature implements IProjectNature {

	public ProRefactorNature() {
	}

	public static final String id = "org.prorefactor.eclipse.ProRefactorNature";

	private IProject project;


	public void configure() { }

	public void deconfigure() {	}

	public IProject getProject()  {
		return project;
	}

	public void setProject(IProject project)  {
		this.project = project;
	}

}
