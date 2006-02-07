/**
 * TableBuffer.java
 * @author John Green
 * 6-Nov-2002
 * www.joanju.com
 * 
 * Copyright (c) 2002, 2004 Joanju Limited.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.prorefactor.treeparser;

import java.util.HashMap;

import org.prorefactor.core.IConstants;
import org.prorefactor.core.TokenTypes;
import org.prorefactor.core.schema.Field;
import org.prorefactor.core.schema.Schema;
import org.prorefactor.core.schema.Table;


/** A TableBuffer is a Symbol which provides a link from the syntax tree to a Table object.
 */
public class TableBuffer extends Symbol {

	/** Constructor for a named buffer.
	 * @param name Input "" for an unnamed or default buffer
	 */
	public TableBuffer(String name, SymbolScope scope, Table table) {
		super(scope);
		this.setName(name);
		this.table = table;
		if (name.length()==0) {
			isDefault = true;
			// The default buffer for temp/work tables is not really "unnamed"
			if (table.getStoretype()!=IConstants.ST_DBTABLE) this.setName(table.getName());
		}
	}

	private boolean isDefault = false;
	private HashMap<Field, FieldBuffer> fieldBuffers = new HashMap<Field, FieldBuffer>();
	private Table table = Schema.nullTable;

	

	void addFieldBuffer(FieldBuffer fieldBuffer) {
		fieldBuffers.put(fieldBuffer.getField(), fieldBuffer);
	}
	
	
	
	/** Get the "database.buffer" name for schema buffers,
	 * get "buffer" for temp/work table buffers.
	 */
	public String fullName() {
		if (table.getStoretype()!=IConstants.ST_DBTABLE) return getName();
		StringBuffer buff = new StringBuffer();
		buff.append(table.getDatabase().getName());
		buff.append(".");
		buff.append(getName());
		return buff.toString();
	}

	
	
	/** Always returns BUFFER, whether this is a named buffer or a default buffer.
	 * @see org.prorefactor.treeparser.Symbol#getProgressType().
	 * To see if this buffer Symbol is for a schema table, temp-table, or work-table,
	 * see Table.getStoreType().
	 * @see org.prorefactor.core.schema.Table#getStoretype().
	 */
	public int getProgressType() { return TokenTypes.BUFFER; }


	
	/** Get or create a FieldBuffer for a Field. */
	public FieldBuffer getFieldBuffer(Field field) {
		assert field.getTable() == this.table;
		FieldBuffer ret = fieldBuffers.get(field);
		if (ret!=null) return ret;
		ret = new FieldBuffer(this.getScope(), this, field);
		fieldBuffers.put(field, ret);
		return ret;
	}


	
	/** Get the name of the buffer (overrides Symbol.getName).
	 * Returns the name of the table for default (unnamed) buffers.
	 */
	public String getName() {
		if (super.getName().length()==0) return table.getName();
		return super.getName();
	}

	
	public Table getTable() { return table; }
	
	
	/** Is this the default (unnamed) buffer? */
	public boolean isDefault() { return isDefault; }


	/** Is this a default (unnamed) buffer for a schema table? */
	public boolean isDefaultSchema() { 
		return isDefault && table.getStoretype()==IConstants.ST_DBTABLE; 
	}

	
	
	public void setTable(Table table) { this.table = table; }



} // class
