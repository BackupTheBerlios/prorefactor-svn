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



/**
 * FieldBuffer is the Symbol object linked to from the AST
 * for schema, temp, and work table fields, and FieldBuffer
 * provides the link to the Field object.
 */
public class FieldBuffer extends Symbol {

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

	
	
	/** Invalid - do not call. Name comes from the Field. */
	public void setName(String name) { assert false; }


}
