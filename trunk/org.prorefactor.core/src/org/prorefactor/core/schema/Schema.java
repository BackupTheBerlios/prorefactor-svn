/**
 * Schema.java
 * @author John Green
 * 17-Nov-2002
 * www.joanju.com
 * 
 * Copyright (c) 2002, 2004 Joanju Limited.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.prorefactor.core.schema;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import com.joanju.ProparseLdr;


/**
 * Schema is a singleton with methods and fields for
 * working with database schema names, and references
 * to those from 4gl compile units.
 */
public class Schema {

	private Schema() { initRefresh(); }

	
	public static Database nullDatabase = new Database("");
	private static Schema theInstance;
	public static Table nullTable = new Table("");

	// See initRefresh() for member initializations.
	private ProparseLdr parser = ProparseLdr.getInstance();
	private TreeMap aliases;
	private TreeSet dbSet;
	private TreeSet allTables;


	
	/**
	 * Schema is a "Singleton"
	 */
	public static Schema getInstance() {
		if (theInstance == null)
			theInstance = new Schema();
		return theInstance;
	}
	public void clear() { initRefresh(); }
	private void initRefresh() {
		aliases = new TreeMap(IGNORECASE_ORDER);
		dbSet = new TreeSet(Database.NAME_ORDER);
		allTables = new TreeSet(ALLTABLES_ORDER);
	}



	static final Comparator ALLTABLES_ORDER = new Comparator() {
		public int compare(Object o1, Object o2) {
			Table s1 = (Table) o1;
			Table s2 = (Table) o2;
			int ret = s1.getName().compareToIgnoreCase(s2.getName());
			if (ret != 0)
				return ret;
			return s1.getDatabase().getName().compareToIgnoreCase(s2.getDatabase().getName());
		}
	};



	static final Comparator IGNORECASE_ORDER = new Comparator() {
		public int compare(Object o1, Object o2) {
			String s1 = (String) o1;
			String s2 = (String) o2;
			int ret = s1.compareToIgnoreCase(s2);
			return ret;
		}
	};



	/**
	 * Add a database alias.
	 * @param aliasname The name for the alias
	 * @param dbname The database's logical name
	 * @return Error message from Proparse, if any
	 */
	public String aliasCreate(String aliasname, String dbname) {
		if (parser.schemaAliasCreate(aliasname.toLowerCase(), dbname.toLowerCase()) != 0)
			return parser.errorGetText();
		aliases.put(aliasname, dbname);
		return "";
	} // aliasCreate



	/**
	 * Delete a database alias.
	 * @param aliasname The name for the alias
	 */
	public void aliasDelete(String aliasname) {
		aliasname.toLowerCase();
		parser.schemaAliasDelete(aliasname.toLowerCase());
		if (aliasname == null || aliasname.length() == 0)
			aliases.clear();
		else
			aliases.remove(aliasname);
	} // aliasDelete



	/** Get an iterator through all tables, sorted by db.table name. */
	public Iterator getAllTablesIterator() {
		return allTables.iterator();
	}



	/**
	 * Load schema names and RECID from a flat file.
	 * @param from The filename to read from.
	 */
	public void loadSchema(String from) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(from));
		StreamTokenizer tokenstream  = new StreamTokenizer(reader);
		tokenstream.eolIsSignificant(false);
		tokenstream.wordChars('!', 'z');
		Database currDatabase = null;
		Table currTable = null;

		// Determine if we want to load db and table names into proparse.dll as well.
		boolean parserLoad = false;
		if (parser.configGet("init").equals("false")) {
			parserLoad = true;
			parser.schemaClear();
		}

		while (tokenstream.nextToken() != StreamTokenizer.TT_EOF) {
			String theString = tokenstream.sval;
			if (theString.equals("::")) {
				// database name
				tokenstream.nextToken();
				String dbname = tokenstream.sval;
				tokenstream.nextToken(); // database number is no longer stored
				currDatabase = new Database(dbname);
				dbSet.add(currDatabase);
				if (parserLoad)
					parser.schemaAddDb(dbname.toLowerCase());
			} else if (theString.equals(":")) {
				// table name
				tokenstream.nextToken();
				String tablename = tokenstream.sval;
				tokenstream.nextToken(); // table recid is no longer stored
				currTable = new Table(tablename, currDatabase);
				allTables.add(currTable);
				if (parserLoad)
					parser.schemaAddTable(tablename.toLowerCase());
			} else {
				// field name
				String fieldname = tokenstream.sval;
				tokenstream.nextToken(); // field recid is no longer stored
				new Field(fieldname, currTable);
				// Fields are not needed or used in Proparse.dll.
			}
		} // while
		reader.close();
	} // loadSchema()



	/**
	 * Lookup Database, with alias checks.
	 * Uses lookupDatabase2().
	 */
	private Database lookupDatabase(String inName) {
		Database db = lookupDatabase2(inName);
		if (db != null)
			return db;
		// Check for database alias
		String realName = (String)(aliases.get(inName));
		if (realName == null)
			return null;
		return lookupDatabase2(realName);
	} // lookupDatabase



	/**
	 * Lookup Database by name.
	 * Called twice by lookupDatabase().
	 */
	private Database lookupDatabase2(String inName) {
		SortedSet dbTailSet = dbSet.tailSet(new Database(inName));
		if (dbTailSet.size() == 0)
			return null;
		Database db = (Database)(dbTailSet.first());
		if ( db == null || db.getName().compareToIgnoreCase(inName) != 0 )
			return null;
		return db;
	} // lookupDatabase2
	
	
	
	/** Lookup a Field, given the db, table, and field names */
	public Field lookupField(String dbName, String tableName, String fieldName) {
		Table table = lookupTable(dbName, tableName);
		if (table==null) return null;
		return table.lookupField(fieldName);
	}



	/**
	 * Lookup a table by name.
	 * @param inName The string table name to lookup.
	 * @return A Table, or null if not found.
	 * If a name like "db.table" fails on the first lookup try,
	 * we next search dictdb for the table, in case it's something
	 * like "sports._file". In that case, the Table from the "dictdb"
	 * database would be returned. We don't keep meta-schema records
	 * in the rest of the databases.
	 */
	public Table lookupTable(String inName) {
		if (inName.indexOf('.') > -1) {
			Table firstTry = lookupTable2(inName);
			if (firstTry != null) return firstTry;
			return lookupMetaTable(inName);
		}
		return lookupTableCheckName(allTables.tailSet(new Table(inName)), inName);
	} // lookupTable()
	
	
	
	/** Lookup a table, given a database name and a table name. */
	public Table lookupTable(String dbName, String tableName) {
		Database db = lookupDatabase(dbName);
		if (db==null) return null;
		return lookupTableCheckName(db.getTableSet().tailSet(new Table(tableName)), tableName);
	}



	// It turns out that we *do* have to test for uniqueness - we can't just leave
	// that job to the compiler. That's because when looking up schema names for
	// a DEF..LIKE x, if x is non-unique in schema, then we move on to temp/work/buffer names.
	private Table lookupTableCheckName(SortedSet set, String name) {
		String lname = name.toLowerCase();
		Iterator it = set.iterator();
		if (! it.hasNext()) return null;
		Table table = (Table)(it.next());
		// test that we got a match
		if (! table.getName().toLowerCase().startsWith(lname)) return null;
		// test that we got a unique match
		if (lname.length() < table.getName().length()  &&  it.hasNext()) {
			Table next = (Table)(it.next());
			if (next.getName().toLowerCase().startsWith(lname)) return null;
		}
		return table;
	} // lookupTableCheckName


	
	/** Lookup a qualified table name */
	private Table lookupTable2(String inName) {
		String [] parts = inName.split("\\.");
		return lookupTable(parts[0], parts[1]);
	} // lookupTable2()



	/**
	 * This is for looking up names like "sports._file". We return the dictdb Table.
	 */
	private Table lookupMetaTable(String inName) {
		String [] parts = inName.split("\\.");
		Database db = lookupDatabase("dictdb");
		if (db == null) return null;
		return lookupTableCheckName(db.getTableSet().tailSet(new Table(parts[1])), parts[1]);
	} // lookupMetaTable()



	/** Lookup an unqualified schema field name.
	 * Does not test for uniqueness. That job is left to the compiler.
	 * (In fact, anywhere this is run, the compiler would check that the
	 * field name is also unique against temp/work tables.)
	 * Returns null if nothing found.
	 */
	public Field lookupUnqualifiedField(String name) {
		Field field;
		for (Iterator it = allTables.iterator(); it.hasNext();) {
			Table table = (Table) it.next();
			field = table.lookupField(name);
			if (field!=null) return field;
		}
		return null;
	}



} // class Schema

