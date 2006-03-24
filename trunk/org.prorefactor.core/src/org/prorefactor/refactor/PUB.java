/**
 * PUB.java
 * @author John Green
 * Sep 1, 2004
 * www.joanju.com
 *
 * Copyright (C) 2004-2006 Joanju Software.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.prorefactor.refactor;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import org.apache.commons.collections.bidimap.DualHashBidiMap;
import org.prorefactor.core.IConstants;
import org.prorefactor.core.JPNode;
import org.prorefactor.core.TokenTypes;
import org.prorefactor.core.schema.Field;
import org.prorefactor.core.schema.Table;
import org.prorefactor.nodetypes.NodeFactory;
import org.prorefactor.nodetypes.ProparseDirectiveNode;
import org.prorefactor.refactor.source.CompileUnit;
import org.prorefactor.treeparser.FieldBuffer;
import org.prorefactor.treeparser.Symbol;
import org.prorefactor.treeparser.TableBuffer;

import com.joanju.ProparseLdr;

/** The API for "Parse Unit Binary" files, which contain import and export
 * tables as well as a minimal AST for a parse unit.
 */
public class PUB {

	/** Create a PUB file for a compile unit file path/name relative to the project directory.
	 * RefactorSession must be configured for the project (schema loaded, project name set, etc)
	 * before working with PUB files.
	 * @param relPath The path/file name relative to the project directory.
	 * @param fullPath The fully qualified path to the compile unit's source file.
	 */
	public PUB(String relPath, String fullPath) {
		cuFile = new File(fullPath);
		pubFile = new File(refpack.getProRefactorProjectDir() + "/pubs/" + relPath + ".pub");
	}
	
	public static final int LAYOUT_VERSION = 4;

	/** loadTo(PUBFILE_TIMESTAMP) - just check if the binary exists and
	 * check that it is newer than the compile unit file. Does not read anything
	 * from the binary.
	 */
	public static final int PUBFILE_TIMESTAMP = 5;
	/** loadTo(FILES) - the index of include files referenced by this parse unit. */
	public static final int FILES = 10;
	/** loadTo(HEADER) - the segments necessary for checking if the binary is up to date or not. */
	public static final int HEADER = 15;
	/** loadTo(SCHEMA) - the schema tables and fields referenced by this parse unit. */
	public static final int SCHEMA = 20;
	/** loadTo(IMPORTS) - the references to external procedures, funtions, and shared vars */
	public static final int IMPORTS = 30;
	/** loadTo(EXPORTS) - new shared vars and public functions and procedures */
	public static final int EXPORTS = 40;
	/** loadTo(AST) - just loads the node types - you almost certainly need STRINGS as well. */
	public static final int AST = 50;
	/** loadTo(STRINGS) - load the strings into the syntax tree. */
	public static final int STRINGS = 60;
	/** loadTo(END) - all binary file segments will be loaded. */
	public static final int END = 100;
	
	/** Scratch JPNode attributes for storing string index. */
	private static final int NODETEXT = 49001;
	/** Scratch JPNode attributes for storing string index. */
	private static final int NODECOMMENTS = 49002;

	private ArrayList<SymbolRef> exportList;
	private ArrayList<String> fileList;
	private ArrayList<SymbolRef> importList;
	private DualHashBidiMap stringTable;
	private File cuFile;
	private File pubFile;
	private JPNode tree;
	private ProparseLdr parser = ProparseLdr.getInstance();
	private RefactorSession refpack = RefactorSession.getInstance();
	private String [] stringArray;
	private TreeMap<String, TableRef> tableMap;

	/** A record of symbol type and name, for import/export tables. */
	public class SymbolRef {
		SymbolRef(int progressType, String symbolName) {
			this.progressType = progressType;
			this.symbolName = symbolName;
		}
		/** The TokenType, ex: TokenTypes.VARIABLE */
		public int progressType;
		/** The symbol name (Symbol.fullName), with caseAsDefined. */
		public String symbolName;
	}
	
	private class TableRef {
		TableRef(String name) { this.name = name; }
		String name;
		TreeMap<String, String> fieldMap = new TreeMap<String, String>();
	}

	
	
	/** It's possible, maybe even sensible, to reuse a PUB object.
	 * This method clears out old lists in preparation for reloading or rebuilding.
	 */
	private void _refresh() {
		exportList = new ArrayList<SymbolRef>();
		fileList = new ArrayList<String>();
		importList = new ArrayList<SymbolRef>();
		tableMap = new TreeMap<String, TableRef>();
		stringTable = new DualHashBidiMap();
		/* String index zero is not used.
		 * This allows us to use 0 from JPNode.attrGet() to indicate "no string value present".
		 */
		stringIndex("");
	}


	
	/** Force a fresh build. 
	 * You would normally call load() first, to check whether
	 * a fresh build is really necessary. Once a build() has been done,
	 * then all of the values for the PUB are available - it is not necessary
	 * for you to call load() or loadTo().
	 * @throws RefactorException
	 */
	public CompileUnit build() throws IOException, RefactorException {
		_refresh();
		CompileUnit cu = new CompileUnit(cuFile, null, CompileUnit.DEFAULT);
		cu.treeParser01();
		pubFile.getParentFile().mkdirs();
		OutputStream fileOut = new BufferedOutputStream(new FileOutputStream(pubFile));
		ObjectOutputStream out = new ObjectOutputStream(fileOut);
		writeVersion(out);
		writeFileIndex(out);
		ArrayList allSymbols = cu.getRootScope().getAllSymbols();
		writeSchemaSegment(out, allSymbols);
		writeImportSegment(out, allSymbols);
		writeExportSegment(out, allSymbols);
		tree = cu.getTopNode();
		writeTree(out, tree);
		writeStrings(out);
		out.close();
		return cu;
	}
	
	
	
	/** Copies the lower case names of all schema tables into your collection.
	 * The names are of the format "database.table".
	 * You might use a sorted set or a hash set, depending on what you need it for.
	 * To get the mixed-case names, use the "org.prorefactor.core.schema" package
	 * to look up the table objects.
	 * @param c
	 */
	@SuppressWarnings("unchecked")
	public void copySchemaTableLowercaseNamesInto(Collection c) {
		c.addAll(tableMap.keySet());
	}
	
	
	
	/** Copies the lower case names of all schema fields for one table into your collection.
	 * The names are of the format "field" - i.e. no db or table name prefix.
	 * You might use a sorted set or a hash set, depending on what you need it for.
	 * To get the mixed-case names, use the "org.prorefactor.core.schema" package
	 * to look up the field objects.
	 * @param fromTableName Your table name. Case insenstitive. Must be of the format
	 * "database.table".
	 */
	@SuppressWarnings("unchecked")
	public void copySchemaFieldLowercaseNamesInto(Collection c, String fromTableName) {
		TableRef tableRef = tableMap.get(fromTableName.toLowerCase());
		if (tableRef==null) return;
		for (String fieldName : tableRef.fieldMap.keySet()) {
			c.add(fieldName);
		}
	}
	
	
	
	/** Get the array of exported symbols, in no particular order.
	 * Currently just for DEF NEW [GLOBAL] SHARED symbols.
	 */
	public SymbolRef [] getExportTable() {
		SymbolRef [] ret = new SymbolRef[exportList.size()];
		exportList.toArray(ret);
		return ret;
	}
	
	

	/** Get the array of file names. The file at index zero is always the compile unit.
	 * The others are include files. The array index position corresponds to JPNode.getFileIndex().
	 * It is possible (even likely) that there will be "" or null String entries.
	 */
	public String [] getFileIndex() {
		String [] ret = new String[fileList.size()];
		fileList.toArray(ret);
		return ret;
	}
	
	
	
	/** Get the array of imported symbols, in no particular order.
	 * Currently just for DEF SHARED symbols.
	 */
	public SymbolRef [] getImportTable() {
		SymbolRef [] ret = new SymbolRef[importList.size()];
		importList.toArray(ret);
		return ret;
	}
	
	

	private ObjectInputStream getObjectInputStream() {
		try {
			InputStream fileIn = new BufferedInputStream(new FileInputStream(pubFile));
			ObjectInputStream inStream = new ObjectInputStream(fileIn);
			return inStream;
		} catch (FileNotFoundException e) {
			return null;
		} catch (IOException e) {
			return null;
		}
	}
	
	
	
	/** Return the JPNode syntax tree that was loaded with load() */
	public JPNode getTree() { return tree; }
	
	

	/** Same as loadTo(PUB.END) */
	public boolean load() { return loadTo(END); }



	/** Load the PUB file to the end of the specified segment.
	 * For example, if you only need to read as far as the "imports" segment,
	 * then use loadTo(PUB.IMPORTS).
	 * @return false if the file is out of date and you need to call build() instead.
	 */
	public boolean loadTo(int lastSegmentToLoad) {
		_refresh();
		if (! pubFile.exists()) return false;
		if (cuFile.lastModified() > pubFile.lastModified()) return false;
		if (lastSegmentToLoad==PUBFILE_TIMESTAMP) return true;
		ObjectInputStream inStream = getObjectInputStream();
		if (inStream==null) return false;
		try {
			if (! readVersion(inStream)) return false;
			readFileIndex(inStream);
			if (! testTimeStamps()) return false;
			if (lastSegmentToLoad==PUB.FILES) return true;
			if (lastSegmentToLoad==PUB.HEADER) return true;
			readSchema(inStream);
			if (lastSegmentToLoad==PUB.SCHEMA) return true;
			readImportSegment(inStream);
			if (lastSegmentToLoad==PUB.IMPORTS) return true;
			readExportSegment(inStream);
			if (lastSegmentToLoad==PUB.EXPORTS) return true;
			tree = readTree(inStream);
			if (lastSegmentToLoad==PUB.AST) return true;
			readStrings(inStream);
			setStrings(tree);
		} catch (IOException e1) {
			return false;
		} finally {
			try { inStream.close(); } catch (IOException e) { }
		}
		return true;
	}
	
	
	
	private void readExportSegment(ObjectInputStream in) throws IOException {
		for (;;) {
			SymbolRef symbolRef = new SymbolRef(in.readInt(), in.readUTF());
			if (symbolRef.progressType == -1) break;
			exportList.add(symbolRef);
		}
	}
	

	
	private void readFileIndex(ObjectInputStream in) throws IOException {
		int index;
		String filename;
		for (;;) {
			index = in.readInt();
			filename = in.readUTF();
			if (index == -1) break;
			fileList.add(filename);
		}
	}
	
	
	
	private void readImportSegment(ObjectInputStream in) throws IOException {
		for (;;) {
			SymbolRef symbolRef = new SymbolRef(in.readInt(), in.readUTF());
			if (symbolRef.progressType == -1) break;
			importList.add(symbolRef);
		}
	}
	

	
	private void readSchema(ObjectInputStream in) throws IOException {
		for (;;) {
			String tableName = in.readUTF();
			if (tableName.length()==0) break;
			TableRef tableRef = new TableRef(tableName);
			tableMap.put(tableName.toLowerCase(), tableRef);
			for (;;) {
				String fieldName = in.readUTF();
				if (fieldName.length()==0) break;
				tableRef.fieldMap.put(fieldName.toLowerCase(), fieldName);
			}
		}
	}
	
	
	
	private void readStrings(ObjectInputStream in) throws IOException {
		int size = in.readInt();
		stringArray = new String[size];
		for(int i = 0; i < size; i++) {
			stringArray[i] = in.readUTF();
		}
	}
	
	
	
	private JPNode readTree(ObjectInputStream in) throws IOException {
		int nodeClass = in.readInt();
		if (nodeClass == -1) return null;
		JPNode node = NodeFactory.createByIndex(nodeClass);
		node.setType(in.readInt());
		int key;
		int value;
		for (	key=in.readInt(), value=in.readInt()
			;	key != -1
			;	key=in.readInt(), value=in.readInt()
			) {
			node.attrSet(key, value);
		}
		node.setFirstChild(readTree(in));
		node.setParentInChildren();
		node.setNextSibling(readTree(in));
		return node;
	}
	
	
	
	/** Read the version, return false if the PUB file is out of date, true otherwise. */
	private boolean readVersion(ObjectInputStream in) throws IOException {
		if (in.readInt() != LAYOUT_VERSION) return false;
		return true;
	}
	
	
	
	private void setStrings(JPNode node) {
		if (node==null) return;
		int index;
		if ((index=node.attrGet(NODETEXT)) > 0) node.setText(stringArray[index]);
		if ((index=node.attrGet(NODECOMMENTS)) > 0) node.setComments(stringArray[index]);
		if ((index=node.attrGet(IConstants.PROPARSEDIRECTIVE)) > 0)
			((ProparseDirectiveNode)node).setDirectiveText(stringArray[index]);
		setStrings(node.firstChild());
		setStrings(node.nextSibling());
	}
	
	
	
	private int stringIndex(String s) {
		Integer index = (Integer) stringTable.getKey(s);
		if (index==null) {
			index = new Integer(stringTable.size()); // index is 0 if this is the first entry...
			stringTable.put(index, s);
		}
		return index.intValue();
	}
	
	
	
	private boolean testTimeStamps() {
		long pubTime = pubFile.lastModified();
		for (String filename : fileList) {
			if (filename==null || filename.length()==0) continue;
			File file = FileStuff.findFile(filename);
			if (file==null) return false;
			if (file.lastModified() > pubTime) return false;
		}
		return true;
	}
	
	
	
	private void writeExportSegment(ObjectOutputStream out, List allSymbols) throws IOException {
		for (Iterator it = allSymbols.iterator(); it.hasNext();) {
			Symbol symbol = (Symbol) it.next();
			if (symbol.isExported()) {
				out.writeInt(symbol.getProgressType());
				out.writeUTF(symbol.fullName()); // We write caseAsDefined
			}
		}
		out.writeInt(-1);
		out.writeUTF("");
	}
	
	
	
	private void writeFileIndex(ObjectOutputStream out) throws IOException {
		String [] files = parser.getFilenameArray();
		for (int i = 0; i < files.length; i++) {
			out.writeInt(i);
			out.writeUTF(files[i]);
		}
		out.writeInt(-1);
		out.writeUTF("");
	}
	
	
	
	private void writeImportSegment(ObjectOutputStream out, List allSymbols) throws IOException {
		for (Iterator it = allSymbols.iterator(); it.hasNext();) {
			Symbol symbol = (Symbol) it.next();
			if (symbol.isImported()) {
				out.writeInt(symbol.getProgressType());
				out.writeUTF(symbol.fullName()); // We write caseAsDefined
			}
		}
		out.writeInt(-1);
		out.writeUTF("");
	}
	
	
	
	private void writeTree(ObjectOutputStream out, JPNode node) throws IOException {
		out.writeInt(node.getSubtypeIndex());
		out.writeInt(node.getType());
		if ( ! TokenTypes.hasDefaultText(node.getType()) ) {
			out.writeInt(NODETEXT);
			out.writeInt(stringIndex(node.getText()));
		}
		String comments = node.getComments();
		if (comments != null) {
			out.writeInt(NODECOMMENTS);
			out.writeInt(stringIndex(comments));
		}
		if (node.attrGet(IConstants.STATEHEAD) == IConstants.TRUE) {
			out.writeInt(IConstants.STATEHEAD);
			out.writeInt(IConstants.TRUE);
			out.writeInt(IConstants.STATE2);
			out.writeInt(node.getState2());
		}
		int attrVal;
		if ( (attrVal = node.attrGet(IConstants.STORETYPE)) > 0 ) {
			out.writeInt(IConstants.STORETYPE);
			out.writeInt(attrVal);
		}
		if (node instanceof ProparseDirectiveNode) {
			out.writeInt(IConstants.PROPARSEDIRECTIVE);
			out.writeInt(stringIndex(((ProparseDirectiveNode)node).getDirectiveText()));
		}
		if ( (attrVal = node.attrGet(IConstants.OPERATOR)) > 0 ) {
			out.writeInt(IConstants.OPERATOR);
			out.writeInt(attrVal);
		}
		if ( (attrVal = node.attrGet(IConstants.INLINE_VAR_DEF)) > 0 ) {
			out.writeInt(IConstants.INLINE_VAR_DEF);
			out.writeInt(attrVal);
		}
		out.writeInt(-1);
		out.writeInt(-1); // Terminate the attribute key/value pairs.
		JPNode next;
		if ( (next = node.firstChild()) != null ) writeTree(out, next);
		else out.writeInt(-1);
		if ( (next = node.nextSibling()) != null ) writeTree(out, next);
		else out.writeInt(-1);
	}
	
	
	
	private void writeSchemaSegment(ObjectOutputStream out, List allSymbols) throws IOException {
		for (Iterator it = allSymbols.iterator(); it.hasNext();) {
			Object obj = it.next();
			if (obj instanceof TableBuffer) {
				Table table = ((TableBuffer)obj).getTable();
				if (table.getStoretype() != IConstants.ST_DBTABLE) continue;
				writeSchema_addTable(table);
				continue;
			}
			if (obj instanceof FieldBuffer) {
				Field field = ((FieldBuffer)obj).getField();
				Table table = field.getTable();
				if (table.getStoretype() != IConstants.ST_DBTABLE) continue;
				TableRef tableRef = writeSchema_addTable(table);
				tableRef.fieldMap.put(field.getName().toLowerCase(), field.getName());
			}
		}
		for (TableRef tableRef : tableMap.values()) {
			out.writeUTF(tableRef.name);
			for (String fieldName : tableRef.fieldMap.values()) {
				out.writeUTF(fieldName);
			}
			out.writeUTF(""); // terminate the list of fields in the table
		}
		out.writeUTF(""); // terminate the schema segment
	}
	private TableRef writeSchema_addTable(Table table) {
		String name = table.getDatabase().getName() + "." + table.getName();
		String lowerName = name.toLowerCase();
		TableRef tableRef = tableMap.get(lowerName);
		if (tableRef != null) return tableRef;
		tableRef = new TableRef(name);
		tableMap.put(lowerName, tableRef);
		return tableRef;
	}
	
	
	
	private void writeStrings(ObjectOutputStream out) throws IOException {
		int size = stringTable.size();
		out.writeInt(size);
		for(int i = 0; i < size; i++) {
			out.writeUTF((String) stringTable.get(new Integer(i)));
		}
	}



	private void writeVersion(ObjectOutputStream out) throws IOException {
		out.writeInt(LAYOUT_VERSION);
	}

	

}
