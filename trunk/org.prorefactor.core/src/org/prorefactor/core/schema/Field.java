/**
 * Field.java
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



/**
 * Field objects are created both by the Schema class and
 * they are also created for temp and work table fields
 * defined within a 4gl compile unit.
 */
public class Field {

	/** Standard constructor.
	 */
	public Field(String inName, Table table) {
		this.name = inName;
		this.table = table;
		table.add(this);
	}
	/** Constructor for temporary "lookup" fields. "Package" visibility. */
	Field(String inName) {
		this.name = inName;
		this.table = Schema.nullTable;
	}

	String name; // "Package" access for fast access in NAME_ORDER
	private Table table;
	
	/** This is a convenience class for working with a string field name, where
	 * there may or may not be a database or table qualifier in the name.
	 */
	public static class Name {
		public Name(String dbPart, String tablePart, String fieldPart) {
			db = dbPart;
			table = tablePart;
			field = fieldPart;
		}
		public Name(String name) {
			String [] parts = name.split("\\.");
			if (parts.length==1) {
				field = parts[0];
			} else if (parts.length==2) {
				table = parts[0];
				field = parts[1];
			} else {
				db = parts[0];
				table = parts[1];
				field = parts[2];
			}
		}
		public String db;
		public String table;
		public String field;
		public String generateName() {
			StringBuffer buff = new StringBuffer();
			if (table!=null && table.length()>0) {
				if (db!=null && db.length()>0) {
					buff.append(db);
					buff.append(".");
				}
				buff.append(table);
				buff.append(".");
			}
			buff.append(field);
			return buff.toString();
		}
	}

	

	/** Comparator for sorting by name. */
	static final Comparator NAME_ORDER = new Comparator() {
		public int compare(Object o1, Object o2) {
			Field f1 = (Field) o1;
			Field f2 = (Field) o2;
			return f1.name.compareToIgnoreCase(f2.name);
		}
	};


	
	public String getName() { return name; }
	public Table getTable() { return table; }
	
	
}
