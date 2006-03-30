/* CompileUnit.java
 * Created on Jan 15, 2004
 * John Green
 *
 * Copyright (C) 2004,2006 Joanju Software
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.prorefactor.refactor.source;


import java.io.File;
import java.io.IOException;

import org.prorefactor.refactor.RefactorException;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.refactor.macrolevel.ListingParser;
import org.prorefactor.treeparser.ParseUnit;
import org.prorefactor.treeparser01.TreeParser01;


/** A ParseUnit subclass, extended with source file features like the ListingParser.
 */
public class CompileUnit extends ParseUnit {
	
	/** Constructor with default values.
	 * @param file The compile unit's source file - usually a ".p" or ".w".
	 */
	public CompileUnit(File file) {
		super(file);
	}

	/** Constructor with specified SourceFilePool and connection style.
	 * @param file The compile unit's source file - usually a ".p" or ".w".
	 * @param pool Will create a new one if null.
	 * @param style A bitset, use bitwise OR to combine flags. ex: CompileUnit.DEFAULT
	 */
	public CompileUnit(File file, SourceFilePool pool, int style) {
		super(file);
		this.style = style;
		if (pool!=null) this.sourceFilePool = pool;
		this.file = file;
	}

	private IncludeExpansion tokenTree = null;
	private ListingParser listingParser;
	protected SourceFilePool sourceFilePool = new SourceFilePool();



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
			// If the TokenTree implementation was complete, then we'd call it here.
		} finally {
			parser.configSet("listing-file", prevListingSetting);
		}
	}



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



}
