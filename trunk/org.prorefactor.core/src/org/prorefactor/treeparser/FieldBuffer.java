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

package org.prorefactor.treeparser;

import org.prorefactor.core.TokenTypes;
import org.prorefactor.core.schema.Field;
import org.prorefactor.core.schema.Schema;



/**
 * FieldBuffer is the Symbol object linked to from the AST
 * for schema, temp, and work table fields, and FieldBuffer
 * provides the link to the Field object.
 */
public class FieldBuffer extends Symbol implements Primative {

	/** When you create a FieldBuffer object, you do not set the name,
	 * because that comes from the Field object.
	 */
	public FieldBuffer(SymbolScope scope, TableBuffer buffer, Field field) {
		super(scope);
		this.buffer = buffer;
		this.field = field;
		buffer.addFieldBuffer(this);
	}

	Field field;
	TableBuffer buffer;

	
	
	/** Could this FieldBuffer be referenced by the input name?
	 * Input Field.Name must already be all lowercase.
	 * Deals with abbreviations, unqualified table/database, and db aliases.
	 */
	public boolean canMatch(Field.Name input) {
		// Assert that the input name is already lowercase.
		assert input.generateName().toLowerCase().equals(input.generateName());
		Field.Name self = new Field.Name(this.fullName().toLowerCase());
		if (input.db!=null) {
			Schema schema = Schema.getInstance();
			if (this.buffer.getTable().getDatabase() != schema.lookupDatabase(input.db)) return false;
		}
		if (input.table!=null) {
			if (buffer.isDefaultSchema()) {
				if (! self.table.startsWith(input.table)) return false;
			} else {
				// Temp/work/buffer names can't be abbreviated.
				if (! self.table.equals(input.table)) return false;
			}
		}
		if (! self.field.startsWith(input.field)) return false;
		return true;
	}
	

	/** @deprecated
	 * INVALID. Do not use. There is never any reason to copy a FieldBuffer,
	 * since they are created by the tree parser on the fly. They are not
	 * defined formally in the syntax.
	 */
	@Override
	public Symbol copyBare(SymbolScope scope) {
		assert false;
		return null;
	}


	/** Get "database.buffer.field" for schema fields, or
	 * "buffer.field" for temp/work table fields.
	 */
	public String fullName() {
		StringBuffer buff = new StringBuffer(buffer.fullName());
		buff.append(".");
		buff.append(field.getName());
		return buff.toString();
	}

	

	public TableBuffer getBuffer() { return buffer; }
	
	/** Gets the underlying Field's className (or null if not a class).
	 * @see Primative#getClassName()
	 */
	public String getClassName() { return field.getClassName(); }
	
	/** Gets the underlying Field's dataType. */
	public DataType getDataType() { return field.getDataType(); }
	
	public Field getField() { return field; }
	
	
	/** Returns the Field name. There is no "field buffer name". */
	public String getName() { return field.getName(); }
	
	
	
	/** Always returns FIELD.
	 * @see org.prorefactor.treeparser.Symbol#getProgressType().
	 * To see if this field buffer is for a schema table, temp-table, or work-table,
	 * see Table.getStoreType().
	 * @see org.prorefactor.core.schema.Table#getStoretype().
	 */
	public int getProgressType() { return TokenTypes.FIELD; }

	
	
	/** @see org.prorefactor.treeparser.Symbol#isExported() */
	public boolean isExported() { return buffer.isExported(); }

	
	
	/** @see org.prorefactor.treeparser.Symbol#isImported() */
	public boolean isImported() { return buffer.isImported(); }

	
	/** Sets the underlying Field's className. */
	public Primative setClassName(String className) { field.setClassName(className); return this; }
	
	
	/** Sets the underlying Field's dataType. */
	public Primative setDataType(DataType dataType) { field.setDataType(dataType); return this; }
	
	
	/** Invalid - do not call. Name comes from the Field. */
	public void setName(String name) { assert false; }


}
