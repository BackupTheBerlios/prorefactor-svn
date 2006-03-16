/* IncludeRef.java
 * Created on Nov 29, 2003
 * John Green
 *
 * Copyright (C) 2003 Joanju Limited
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.prorefactor.refactor.macrolevel;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 */
public class IncludeRef extends MacroRef {

	public boolean usesNamedArgs;
	public int fileIndex;
	private ArrayList includeArgs = new ArrayList();
	private TreeMap argMap = new TreeMap();



	public void addNamedArg(MacroDef arg) {
		includeArgs.add(arg);
		argMap.put(arg.name, arg);
	}



	public void addNumberedArg(MacroDef arg) {
		includeArgs.add(arg);
	}



	public MacroDef getArgNumber(int num) {
		if (num>0 && num < includeArgs.size()) return (MacroDef) includeArgs.get(num - 1);
		return null;
	}



	public MacroDef lookupNamedArg(String name) {
		if (!usesNamedArgs) return null;
		return (MacroDef)argMap.get(name);
	}



	public int numArgs() {
		return includeArgs.size();
	}



	public MacroDef undefine(String name) {
		MacroDef theArg = (MacroDef) argMap.get(name);
		if (theArg != null) {
			argMap.remove(name);
			argMap.put("", theArg);
			return theArg;
		}
		return null;
	}


}
