/* ParserMessages.java
 * Created on Oct 8, 2003
 * John Green
 *
 * Copyright (C) 2003 Joanju Limited
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.prorefactor.eclipse.messages;


import java.io.*;

import org.prorefactor.eclipse.Plugin;


/** For working with error and warning messages from the parser.
 * Each instance overwrites the contents of prorefactor/parser.messages.
 */
public class ParserMessages {

	public static final String filename = "prorefactor/parser.messages";
	File outfile = null;
	FileWriter writer = null;

	public ParserMessages() {
		try {
			outfile = new File(filename);
			outfile.getParentFile().mkdirs();
			outfile.createNewFile();
			writer = new FileWriter(outfile, false);
		} catch (IOException e) {
			Plugin.errorDialog("Failed to open file for writing: " + filename);
		}
	}

	public void close() {
		if (writer==null) return;
		try {
			writer.close();
		} catch (IOException e) {}
	}

	public void write(String theMessage) {
		if (writer==null) return;
		try {
			writer.write(theMessage);
		} catch (IOException e) {}
	}

}
