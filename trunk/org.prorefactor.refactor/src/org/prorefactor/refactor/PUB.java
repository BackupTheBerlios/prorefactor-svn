/**
 * PUB.java
 * @author John Green
 * Sep 1, 2004
 * www.joanju.com
 *
 * Copyright (C) 2004 Joanju Limited.
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
import java.util.TreeMap;

import org.prorefactor.core.IConstants;
import org.prorefactor.core.schema.Field;
import org.prorefactor.core.schema.Table;
import org.prorefactor.refactor.source.CompileUnit;
import org.prorefactor.treeparser.FieldBuffer;
import org.prorefactor.treeparser.SymbolScopeRoot;
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
	
	public static final int LAYOUT_VERSION = 1;

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
	/** loadTo(AST) - minimal node data for a functional syntax tree */
	public static final int AST = 50;
	/** loadTo(END) - all binary file segments will be loaded. */
	public static final int END = 100;

	private ArrayList fileList;
	private File cuFile;
	private File pubFile;
	private ProparseLdr parser = ProparseLdr.getInstance();
	private RefactorSession refpack = RefactorSession.getInstance();
	private TreeMap tableMap;
	
	private class TableRef {
		TableRef(String name) { this.name = name; }
		String name;
		TreeMap fieldMap = new TreeMap();
	}
	
	/** It's possible, maybe even sensible, to reuse a PUB object.
	 * This method clears out old lists in preparation for reloading or rebuilding.
	 */
	private void _refresh() {
		fileList = new ArrayList();
		tableMap = new TreeMap();
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
		writeSchemaSegment(out, cu.getRootScope());
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
	public void copySchemaFieldLowercaseNamesInto(Collection c, String fromTableName) {
		TableRef tableRef = (TableRef) tableMap.get(fromTableName.toLowerCase());
		if (tableRef==null) return;
		for (Iterator it = tableRef.fieldMap.keySet().iterator(); it.hasNext();) {
			String fieldName = (String) it.next();
			c.add(fieldName);
		}
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
		} catch (IOException e1) {
			return false;
		} finally {
			try { inStream.close(); } catch (IOException e) { }
		}
		return true;
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
	
	
	
	/** Read the version, return false if the PUB file is out of date, true otherwise. */
	private boolean readVersion(ObjectInputStream in) throws IOException {
		if (in.readInt() != LAYOUT_VERSION) return false;
		return true;
	}
	
	
	
	private boolean testTimeStamps() {
		long pubTime = pubFile.lastModified();
		for (Iterator it = fileList.iterator(); it.hasNext();) {
			String filename = (String) it.next();
			if (filename==null || filename.length()==0) continue;
			File file = FileStuff.findFile(filename);
			if (file==null) return false;
			if (file.lastModified() > pubTime) return false;
		}
		return true;
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
	
	
	
	private void writeSchemaSegment(ObjectOutputStream out, SymbolScopeRoot rootScope) throws IOException {
		ArrayList allSymbols = rootScope.getAllSymbols();
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
		for (Iterator it = tableMap.values().iterator(); it.hasNext();) {
			TableRef tableRef = (TableRef) it.next();
			out.writeUTF(tableRef.name);
			for (Iterator it2 = tableRef.fieldMap.values().iterator(); it2.hasNext();) {
				String fieldName = (String) it2.next();
				out.writeUTF(fieldName);
			}
			out.writeUTF(""); // terminate the list of fields in the table
		}
		out.writeUTF(""); // terminate the schema segment
	}
	private TableRef writeSchema_addTable(Table table) {
		String name = table.getDatabase().getName() + "." + table.getName();
		String lowerName = name.toLowerCase();
		TableRef tableRef = (TableRef) tableMap.get(lowerName);
		if (tableRef != null) return tableRef;
		tableRef = new TableRef(name);
		tableMap.put(lowerName, tableRef);
		return tableRef;
	}



	private void writeVersion(ObjectOutputStream out) throws IOException {
		out.writeInt(LAYOUT_VERSION);
	}

	

}
