/**
 * Table.java
 * @author John Green
 * 20-Nov-2002
 * www.joanju.com
 * 
 * Copyright (c) 2002, 2004 Joanju Limited.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.prorefactor.core.schema;


import java.util.Comparator;
import java.util.TreeSet;

import org.prorefactor.core.IConstants;



/** Table objects are created both by the Schema class
 * and also when temp and work tables are defined
 * within a 4gl compile unit.
 * For temp and work tables, the database is Schema.nullDatabase.
 */
public class Table {

	/** Constructor for schema */
	public Table(String name, Database database) {
		this.name = name;
		this.database = database;
		database.add(this);
	}
	/** Constructor for temp / work tables */
	public Table(String name, int storetype) {
		this.name = name;
		this.storetype = storetype;
		this.database = Schema.nullDatabase;
	}
	/** Constructor for temporary "comparator" objects. */
	public Table(String name) {
		this.name = name;
		database = Schema.nullDatabase;
	}

	private int storetype = IConstants.ST_DBTABLE;
	String name; // package access
	private Database database;
	private TreeSet fieldSet = new TreeSet(Field.NAME_ORDER);

	/** This is a convenience class for working with a string table name, where
	 * there may or may not be a database qualifier in the name.
	 */
	public static class Name {
		public Name(String dbPart, String tablePart) {
			db = dbPart;
			table = tablePart;
		}
		public Name(String name) {
			String [] parts = name.split("\\.");
			if (parts.length==1) {
				table = parts[0];
			} else {
				db = parts[0];
				table = parts[1];
			}
		}
		public String db;
		public String table;
		public String generateName() {
			StringBuffer buff = new StringBuffer();
			if (db!=null && db.length()>0) {
				buff.append(db);
				buff.append(".");
			}
			buff.append(table);
			return buff.toString();
		}
	}

	

	/** Comparator for sorting by name. */
	public static final Comparator NAME_ORDER = new Comparator() {
		public int compare(Object o1, Object o2) {
			Table t1 = (Table) o1;
			Table t2 = (Table) o2;
			return t1.name.compareToIgnoreCase(t2.name);
		}
	};



	/** Add a Field to this table. "Package" visibility only. */
	void add(Field field) {
		fieldSet.add(field);
	}



	public Database getDatabase() { return database; }
	public TreeSet getFieldSet() { return fieldSet; }
	public String getName() { return name; }
	public int getStoretype() { return storetype; }



	/**
	 * Lookup a field by name.
	 * We do not test for uniqueness. We leave that job to the compiler.
	 * This function expects an unqualified field name (no name dots).
	 */
	public Field lookupField(String name) {
		java.util.SortedSet fieldTailSet = fieldSet.tailSet(new Field(name));
		if (fieldTailSet.size() == 0)
			return null;
		Field field = (Field)(fieldTailSet.first());
		if (	field == null
			||	! field.getName().toLowerCase().startsWith(name.toLowerCase())
			)
			return null;
		return field;
	} // lookupField()
	
	
	
} // class Table
