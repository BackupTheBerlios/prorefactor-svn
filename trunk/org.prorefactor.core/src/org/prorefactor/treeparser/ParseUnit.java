/**
 * ParseUnit.java
 * @author John Green
 * Aug 12, 2004
 * www.joanju.com
 *
 * Copyright (C) 2004,2006 Joanju Software.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.prorefactor.treeparser;

import java.io.File;
import java.io.IOException;

import org.prorefactor.core.JPNode;
import org.prorefactor.core.PRCException;
import org.prorefactor.nodetypes.ProgramRootNode;
import org.prorefactor.refactor.FileStuff;
import org.prorefactor.refactor.PUB;
import org.prorefactor.refactor.RefactorException;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser01.TP01Action;
import org.prorefactor.treeparser01.TreeParser01;

import com.joanju.ProparseLdr;


/** Provides parse unit information, such as the symbol table and a reference to the AST.
 * TreeParser01 calls symbolUsage() in this class in order to build the symbol table.
 */
public class ParseUnit {

	public ParseUnit(File file) {
		this.file = file;
	}

	/** The JPNode tree is "connected" to Proparse, by default */
	public static final int CONNECTED = 0;

	/** The JPNode tree is "connected" to Proparse, by default */
	public static final int DEFAULT = 0;

	/** Working with JPNode in disconnected mode may not yet be fully supported.
	 * See JPNode docs.
	 */
	public static final int DISCONNECTED = 1 << 1;


	protected int style = DEFAULT;
	protected File file;
	private ProgramRootNode topNode;
	protected ProparseLdr parser = ProparseLdr.getInstance();
	protected PUB pub = null;
	protected RefactorSession refpack = RefactorSession.getInstance();
	private SymbolScopeRoot rootScope;

	
	
	public File getFile() {
		if (file==null) {
			// A lot of old code starts with a string filename, sends that to Proparse, gets the top node
			// handle, builds JPNode, and then runs TreeParser01 from that. (All the stuff ParseUnit does
			// now.) In those cases, this ParseUnit might have been created as an empty shell by TreeParser01
			// itself, and "file" would not be set. In that case, we attempt to find the File from the file index.
			if (topNode==null) return null;
			file = new File(topNode.getFilenames()[0]);
		}
		return file;
	}
	
	
	/** Get the file index, either from the PUB file or from the parser, whichever was used to get the tree.
	 * The return is the array of file names. The file at index zero is always the compile unit.
	 * The others are include files. The array index position corresponds to JPNode.getFileIndex().
	 * @see org.prorefactor.nodetypes.ProgramRootNode#getFilenames()
	 */
	public String [] getFileIndex() {
		if (topNode==null) return null;
		return topNode.getFilenames();
	}
	
	
	/** Get or create a PUB */
	public PUB getPUB() {
		if (pub==null) {
			// Note that we might be parsing a super class from another project, because it was
			// found on the propath. We have to tell the PUB which project the file was found in.
			String [] projFile = refpack.getIDE().getProjectRelativePath(getFile());
			pub = new PUB(projFile[0], projFile[1], FileStuff.fullpath(file));
			pub.setParseUnit(this);
		}
		return pub;
	}
	
	
	public SymbolScopeRoot getRootScope() { return rootScope; }


	/** Get the syntax tree top (Program_root) node */
	public ProgramRootNode getTopNode() { return topNode; }
	
	
	/** Load from PUB, or build PUB if it's out of date.
	 * TreeParser01 is run in order to build the PUB.
	 * If the PUB was up to date, then TreeParser01 is run
	 * after the PUB is loaded. (Either way, the symbol tables
	 * etc. are available.)
	 */
	public void loadOrBuildPUB() throws RefactorException, IOException {
		getPUB();
		if (pub.load()) {
			setTopNode(pub.getTree());
			treeParser01();
		} else {
			pub.build();
		}
	}


	public void parse() throws RefactorException {
		parser.parse(file.getPath());
		if (parser.errorGetStatus() < 0) throw new RefactorException(parser.errorGetText());
		int topHandle = parser.getHandle();
		parser.nodeTop(topHandle);
		if ((style & DISCONNECTED) == 0) {
			this.setTopNode(JPNode.getTree(topHandle));
		}
		else {
			JPNode.TreeConfig config = new JPNode.TreeConfig();
			config.makeDisconnected();
			this.setTopNode(JPNode.getTree(topHandle, config));
		}
		assert parser.errorGetStatus()==0 : parser.errorGetText();
	}
	
	
	public ParseUnit setPUB(PUB pub) {
		this.pub = pub;
		if (pub.getParseUnit()!=this) pub.setParseUnit(this);
		return this;
	}

	
	public void setRootScope(SymbolScopeRoot rootScope) { this.rootScope = rootScope; }

	
	/** Set the syntax tree top (Program_root) node. */
	public void setTopNode(JPNode topNode) { this.topNode = (ProgramRootNode) topNode; }


	/** Run any IJPTreeParser against the AST.
	 * This will call parse() if the JPNode AST has not already been built.
	 */
	public void treeParser(IJPTreeParser tp) throws RefactorException {
		if (this.getTopNode()==null) parse();
		try {
			TreeParserWrapper.run2(tp, this.getTopNode());
		} catch (PRCException e) {
			throw new RefactorException(e.getMessage(), e);
		}
	}

	
	/** Run TreeParser01.
	 * Takes care of calling parse() first, if that has not already been done.
	 */
	public void treeParser01() throws RefactorException {
		if (this.getTopNode()==null) parse();
		TreeParser01 tp = new TreeParser01();
		tp.getActionObject().setParseUnit(this);
		treeParser(tp);
	}

	
	/** Run TreeParser01 with any TP01Action object.
	 * Takes care of calling parse() first, if that has not already been done.
	 */
	public void treeParser01(TP01Action action) throws RefactorException {
		if (this.getTopNode()==null) parse();
		TreeParser01 tp = new TreeParser01();
		tp.setActionObject(action);
		action.setParseUnit(this);
		treeParser(tp);
	}

	
}
