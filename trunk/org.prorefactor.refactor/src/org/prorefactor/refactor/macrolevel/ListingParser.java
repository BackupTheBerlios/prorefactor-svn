/* ListingParser.java
 * Created on Nov 28, 2003
 * John Green
 *
 * Copyright (C) 2003 Joanju Limited
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.prorefactor.refactor.macrolevel;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.HashMap;

import org.prorefactor.refactor.RefactorException;



/** For parsing Proparse's "preprocessor listing" file.
 * Generates a "macro tree". The macro tree's root is
 * an IncludeRef object. The root IncludeRef represents
 * the main.p source file.
 */
public class ListingParser {

	public ListingParser(String listingFile) {
		this.listingFile = listingFile;
	}

	/** Map of fileIndex (Integer) to fileName (String) */
	public HashMap fileIndexes = new HashMap();

	private IncludeRef root = null;

	/* Temp stack of scopes, just used during tree creation */
	private int column;
	private int line;
	private int listingFileLine = 0;
	private LinkedList scopeStack = new LinkedList();
	private IncludeRef currInclude;
	/* Temp stack of global defines, just used during tree creation */
	private HashMap globalDefMap = new HashMap();
	private MacroRef currRef;
	private String listingFile;

	/* These scopes are temporary, just used during tree creation */
	private class Scope {
		public Scope(IncludeRef ref) {
			this.includeRef = ref;
		}
		HashMap defMap = new HashMap();
		IncludeRef includeRef;
	}

	/** Just for build/test/debug */
	public static void main(String [] args) {
		ListingParser listingParser = new ListingParser("tmp/temp.txt");
		try {
			listingParser.parse();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		System.out.println("Done");
	}



	/** Global or scoped define */
	private void ampdef(String [] parts, int type) {
		MacroDef newDef = new MacroDef();
		getPosition(parts);
		newDef.parent = currRef;
		newDef.line = line;
		newDef.column = column;
		newDef.name = parts[4];
		newDef.value = parts[5];
		newDef.type = type;
		if (type==MacroDef.GLOBAL) globalDefMap.put(newDef.name, newDef);
		if (type==MacroDef.SCOPED) {
			Scope currScope = (Scope) scopeStack.getFirst();
			currScope.defMap.put(newDef.name, newDef);
		}
		currRef.macroEventList.add(newDef); 
	}



	private void ampelse(String [] parts) {
		return;
	}



	private void ampelseif(String [] parts) {
		return;
	}



	private void ampendif(String [] parts) {
		return;
	}



	private void ampif(String [] parts) {
		return;
	}



	private void createRootNode() {
		root = new IncludeRef();
		currRef = root;
		currInclude = root;
		scopeStack.addFirst(new Scope(root));
	}



	private void fileindex(String [] parts) {
		fileIndexes.put(new Integer(Integer.parseInt(parts[4])), parts[5]);
	}



	/** Find a MacroDef by name.
	 * NOTE: I have not yet implemented {*} and other such
	 * built-in macro reference tricks. Not sure how soon I'll need those.
	 * There's a good chance that this function will return null.
	 */
	private MacroDef findMacroDef(String name) {
		MacroDef ret;
		Scope currScope = (Scope)scopeStack.getFirst();
		// First look for local SCOPED define
		ret = (MacroDef) currScope.defMap.get(name);
		if (ret!=null) return ret;
		// Second look for a named include file argument
		ret = currInclude.lookupNamedArg(name);
		if (ret!=null) return ret;
		// Third look for a non-local SCOPED define
		Iterator it = scopeStack.iterator();
		it.next(); // skip the current scope - already checked.
		while (it.hasNext()) {
			currScope = (Scope) it.next();
			ret = (MacroDef) currScope.defMap.get(name);
			if (ret!=null) return ret;
		}
		// Fourth look for a GLOBAL define
		ret  = (MacroDef) globalDefMap.get(name);
		return ret;		
	} // findMacroDef()



	private void getPosition(String [] parts) {
		line = Integer.parseInt(parts[1]);
		column = Integer.parseInt(parts[2]);
	}



	/** Get the macro tree's root - an IncludeRef object
	 * which represents the main.p source file.
	 */
	public IncludeRef getRoot() {
		return root;
	}



	private void globdef(String [] parts) {
		ampdef(parts, MacroDef.GLOBAL);
	}



	private void incarg(String [] parts) {
		MacroDef newArg = new MacroDef();
		newArg.value = replaceEscapes(parts[5]);
		newArg.includeRef = currInclude;
		newArg.parent = currInclude.parent;
		int argNum = 0;
		try {
			argNum = Integer.parseInt(parts[4]);
		} catch (NumberFormatException e) {}
		if (argNum==0 || argNum != currInclude.numArgs() + 1) {
			newArg.name = parts[4];
			currInclude.usesNamedArgs = true;
			newArg.type = MacroDef.NAMEDARG;
			currInclude.addNamedArg(newArg);
		} else {
			newArg.type = MacroDef.NUMBEREDARG;
			currInclude.addNumberedArg(newArg);
		}
	}



	private void incend(String [] parts) {
		scopeStack.removeFirst();
		currInclude = ((Scope)scopeStack.getFirst()).includeRef;
		currRef = currRef.parent;
	}



	private void include(String [] parts) {
		IncludeRef newRef = new IncludeRef();
		scopeStack.addFirst(new Scope(newRef));
		currRef.macroEventList.add(newRef);
		newRef.parent = currRef; // not necessarily an include file!
		currInclude = newRef;
		currRef = newRef;
		getPosition(parts);
		newRef.refLine = line;
		newRef.refColumn = column;
		newRef.fileIndex = Integer.parseInt(parts[4]);
	}



	private void macroref(String [] parts) {
		NamedMacroRef newRef = new NamedMacroRef();
		currRef.macroEventList.add(newRef);
		newRef.parent = currRef;
		currRef = newRef;
		getPosition(parts);
		newRef.refLine = line;
		newRef.refColumn = column;
		newRef.macroDef = findMacroDef(parts[4]);
	}



	private void macrorefend(String [] parts) {
		currRef = currRef.parent;
	}



	public void parse() throws IOException, RefactorException {
		BufferedReader reader = new BufferedReader(new FileReader(listingFile));
		createRootNode();
		while (true) {
			String currLine = reader.readLine();
			if (currLine==null || currLine.length()<1) break;
			listingFileLine++;
			String [] parts = currLine.split("\\s", 6);
			String token = parts[3];
			if (token.equals("globdef")) { globdef(parts); continue; }
			if (token.equals("scopdef")) { scopdef(parts); continue; }
			if (token.equals("macroref")) { macroref(parts); continue; }
			if (token.equals("macrorefend")) { macrorefend(parts); continue; }
			if (token.equals("undef")) { undef(parts); continue; }
			if (token.equals("include")) { include(parts); continue; }
			if (token.equals("incarg")) { incarg(parts); continue; }
			if (token.equals("incend")) { incend(parts); continue; }
			if (token.equals("ampif")) { ampif(parts); continue; }
			if (token.equals("ampelseif")) { ampelseif(parts); continue; }
			if (token.equals("ampelse")) { ampelse(parts); continue; }
			if (token.equals("ampendif")) { ampendif(parts); continue; }
			if (token.equals("fileindex")) { fileindex(parts); continue; }
			throw new RefactorException(
				"Invalid token in Proparse listing file."
				+ " Token: " + token
				+ " Line: " + (new Integer(listingFileLine)).toString()
				);
		}
	} // parse()



	/** Proparse's preprocess listing replaces '\n' with "\\n",
	 * '\r' with "\\r", and '\\' with "\\\\".
	 * This function gets the string back to its original form.
	 */
	private String replaceEscapes(String s) {
		StringBuffer r = new StringBuffer("");
		int len = s.length();
		for (int i=0; i < len; i++) {
			char c = s.charAt(i);
			if (c != '\\' || i == len-1 ) {
				r.append(c);
				continue;
			}
			char c2 = s.charAt(i+1);
			switch (c2) {
			case '\\' :
				r.append('\\');
				i++;
				break;
			case 'n' :
				r.append('\n');
				i++;
				break;
			case 'r' :
				r.append('\r');
				i++;
				break;
			default :
				r.append(c);
			}
		}
		return r.toString();
	} // replaceEscapes



	private void scopdef(String [] parts) {
		ampdef(parts, MacroDef.SCOPED);
	}



	private void undef(String [] parts) {
		// Add an object for this macro event.
		String name = parts[4];
		MacroDef newDef = new MacroDef();
		currRef.macroEventList.add(newDef);
		getPosition(parts);
		newDef.parent = currRef;
		newDef.line = line;
		newDef.column = column;
		newDef.name = name;
		newDef.type = MacroDef.UNDEFINE;

		// Now process the undefine.
		Scope currScope = (Scope)scopeStack.getFirst();
		// First look for local SCOPED define
		if (currScope.defMap.containsKey(name)) {
			newDef.undefWhat = (MacroDef) currScope.defMap.remove(name);
			return;
		}
		// Second look for a named include file argument
		newDef.undefWhat = currInclude.undefine(name);
		if (newDef.undefWhat!=null) return;
		// Third look for a non-local SCOPED define
		Iterator it = scopeStack.iterator();
		it.next(); // skip the current scope - already checked.
		while (it.hasNext()) {
			currScope = (Scope) it.next();
			if (currScope.defMap.containsKey(name)) {
				newDef.undefWhat = (MacroDef) currScope.defMap.remove(name);
				return;
			}
		}
		// Fourth look for a GLOBAL define
		if (globalDefMap.containsKey(name))
			newDef.undefWhat = (MacroDef) globalDefMap.remove(name);
	} // undef



} // class ListingParser
