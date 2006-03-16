/* CompileUnit.java
 * Created on Jan 15, 2004
 * John Green
 *
 * Copyright (C) 2004 Joanju Limited
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.prorefactor.refactor.source;

import com.joanju.ProparseLdr;

import java.io.File;
import java.io.IOException;

import org.prorefactor.core.JPNode;
import org.prorefactor.core.PRCException;
import org.prorefactor.refactor.RefactorException;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.refactor.macrolevel.ListingParser;
import org.prorefactor.treeparser.IJPTreeParser;
import org.prorefactor.treeparser.ParseUnit;
import org.prorefactor.treeparser.TreeParserWrapper;
import org.prorefactor.treeparser01.TP01Support;
import org.prorefactor.treeparser01.TreeParser01;


/** A single point of reference for an AST, a macro tree, symbol tables, etc.
 */
public class CompileUnit extends ParseUnit {
	
	/** Constructor with default values.
	 * @param file The compile unit's source file - usually a ".p" or ".w".
	 */
	public CompileUnit(File file) {
		this.file = file;
		this.style = CompileUnit.DEFAULT;
		this.sourceFilePool = new SourceFilePool();
	}

	/** Constructor with specified SourceFilePool and connection style.
	 * @param file The compile unit's source file - usually a ".p" or ".w".
	 * @param pool Will create a new one if null.
	 * @param style A bitset, use bitwise OR to combine flags. ex: CompileUnit.DEFAULT
	 */
	public CompileUnit(File file, SourceFilePool pool, int style) {
		this.style = style;
		if (pool==null) this.sourceFilePool = new SourceFilePool();
		else this.sourceFilePool = pool;
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

	private int style;
	private File file;
	private IncludeExpansion tokenTree = null;
	private ListingParser listingParser;
	private RefactorSession refpack = RefactorSession.getInstance();
	private String [] filenames;

	private ProparseLdr parser = ProparseLdr.getInstance();
	protected SourceFilePool sourceFilePool;



	/** Load everything.
	 * Parse into an AST, load the macro tree (macrolevel),
	 * call TreeParser01,
	 * load the Token Expansion Tree, do all additional processing
	 * and synchronizations.
	 */
	public void fullMonty() throws RefactorException {
		String prevListingSetting = parser.configGet("listing-file");
		try { 
			refpack.enableParserListing();
			parse();
			treeParser(new TreeParser01());
			loadMacroTree();
			// TODO generateTokenTree();
		} finally {
			parser.configSet("listing-file", prevListingSetting);
		}
	} // fullMonty



	SourceFilePool getSourceFilePool() {
		return sourceFilePool;
	}



	void generateTokenTree() throws IOException, RefactorException {
		Processor processor = new Processor(file, sourceFilePool);
		tokenTree = processor.generateTree(this);
	}



	public IncludeExpansion getTokenTree() throws IOException, RefactorException {
		if (tokenTree == null) generateTokenTree();
		return tokenTree;
	}



	void loadMacroTree() throws RefactorException {
		listingParser = new ListingParser(RefactorSession.LISTING_FILE);
		try {
			listingParser.parse();
		} catch (Exception e) {
			throw new RefactorException("Error parsing the listing file: " + e.getMessage());
		}
	}



	public void parse() throws RefactorException {
		parser.parse(file.getPath());
		if (parser.errorGetStatus() < 0) throw new RefactorException(parser.errorGetText());
		filenames = parser.getFilenameArray();
		int topHandle = parser.getHandle();
		parser.nodeTop(topHandle);
		if ((style & DISCONNECTED) == 0) {
			super.setTopNode(JPNode.getTree(topHandle));
		}
		else {
			JPNode.TreeConfig config = new JPNode.TreeConfig();
			config.makeDisconnected(filenames);
			super.setTopNode(JPNode.getTree(topHandle, config));
		}
		assert parser.errorGetStatus()==0 : parser.errorGetText();
	}



	/** Run any IJPTreeParser against the AST.
	 * This will call parse() if the JPNode AST has not already been built.
	 */
	public void treeParser(IJPTreeParser tp) throws RefactorException {
		if (super.getTopNode()==null) parse();
		try {
			TreeParserWrapper.run2(tp, super.getTopNode());
		} catch (PRCException e) {
			throw new RefactorException(e.getMessage(), e);
		}
	}

	
	
	/** Run TreeParser01. This takes care of calling parse() first, if that
	 * has not already been done.
	 */
	public void treeParser01() throws RefactorException {
		if (super.getTopNode()==null) parse();
		TreeParser01 tp = new TreeParser01();
		((TP01Support)tp.getTpSupport()).setParseUnit(this);
		treeParser(tp);
	}



} // class
