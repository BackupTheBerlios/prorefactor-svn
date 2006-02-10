/* Created Mar 18, 2005
 *
 * Authors: John Green
 *
 * Copyright (C) 2005 Prolint.org Contributors
 * This file is part of Prolint.
 *
 * Prolint is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * Prolint is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Prolint; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.prolint.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.prorefactor.eclipse.ResourceUtil;

/** Long and short term extensions to org.prorefactor.refactor.FileStuff.
 * Some of the methods added here may be migrated to ProRefactor. 
 */
public class FileStuff extends org.prorefactor.refactor.FileStuff {
	
	/** Get the character offset of the first character of desired line (zero relative).
	 * For example, for line 1, this returns 0.
	 * Proparse does not work with Mac style line termination, and neither does this.
	 * @throws IOException if the file cannot be read or if the requested line number
	 * does not exist in the file.
	 * NOTE: If Proparse adds support for charpos (character offset) then we should
	 * be able to stop using this (rather expensive) operation entirely.
	 */
	public static int calculateLineOffset(File file, int line) throws IOException {
		if (line==1) return 0;
		int currPos = -1;
		int currLine = 1;
		BufferedReader buff = new BufferedReader(new FileReader(file));
		int c;
		while ((c = buff.read()) != -1) {
			currPos++;
			if (c == '\n') {
				currLine++;
				if (currLine==line) {
					buff.close();
					return currPos + 1;
				}
			}
		}
		buff.close();
		throw new IOException("No such line: " + line + " in file: " + file.getPath());
	}

	/** Find an IFile for a string filename, or null if not found.
	 * Maybe later: Remove once it's been added to org.prorefactor.eclipse.ResourceUtil.
	 */
	public static IFile getIFileRelaxed(String filename) {
		File file = FileStuff.findFile(filename);
		if (file==null) return null;
		return ResourceUtil.getIFile((FileStuff.fullpath(file)));
	}

}
