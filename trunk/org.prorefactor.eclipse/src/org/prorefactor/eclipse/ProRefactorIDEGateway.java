/* Created on Mar 26, 2006
 * John Green
 *
 * Copyright (C) 2006 Joanju Software
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.prorefactor.eclipse;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.prorefactor.refactor.IDE;


/** @see IDE */
public class ProRefactorIDEGateway implements IDE {

	/** @see IDE#getProjectRelativePath(File) */
	public String [] getProjectRelativePath(File file) {
		IFile ifile = ResourceUtil.getIFile(file.getAbsolutePath());
		if (ifile==null) return null;
		String [] ret = new String[2];
		ret[0] = ifile.getProject().getName();
		ret[1] = ifile.getProjectRelativePath().toString();
		return ret;
	}

}
