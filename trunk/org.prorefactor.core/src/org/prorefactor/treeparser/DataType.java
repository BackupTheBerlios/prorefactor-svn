/* Created on Apr 20, 2005
 * Authors: John Green
 * 
 * Copyright (C) 2005 Joanju (joanju.com).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.prorefactor.treeparser;

import java.util.HashMap;

import org.prorefactor.core.TokenTypes;


/** One static instance of DataType is created for each data type in the 4GL.
 * You can access each of those through this class's public static final variables.
 * This class was created just so that we could look up, and store, an object
 * instead of a String or int to represent data type. For example, we'll be
 * adding datatype support into Field and schemaLoad next.
 */
public class DataType {

	private DataType(int tokenType, String progressName) {
		this.tokenType = new Integer(tokenType);
		this.progressName = progressName;
		nameMap.put(progressName, this);
		tokenTypeMap.put(this.tokenType, this);
	}
	
	private Integer tokenType;
	private String progressName;

	private static HashMap nameMap = new HashMap();
	private static HashMap tokenTypeMap = new HashMap();
	
	public static final DataType BIGINT = new DataType(TokenTypes.BIGINT, "BIGINT");
	public static final DataType BLOB = new DataType(TokenTypes.BLOB, "BLOB");
	public static final DataType BYTE = new DataType(TokenTypes.BYTE, "BYTE");
	public static final DataType CHARACTER = new DataType(TokenTypes.CHARACTER, "CHARACTER");
	public static final DataType CLOB = new DataType(TokenTypes.CLOB, "CLOB");
	public static final DataType COMHANDLE = new DataType(TokenTypes.COMHANDLE, "COM-HANDLE");
	public static final DataType DATE = new DataType(TokenTypes.DATE, "DATE");
	public static final DataType DATETIME = new DataType(TokenTypes.DATETIME, "DATETIME");
	public static final DataType DATETIMETZ = new DataType(TokenTypes.DATETIMETZ, "DATETIME-TZ");
	public static final DataType DECIMAL = new DataType(TokenTypes.DECIMAL, "DECIMAL");
	public static final DataType DOUBLE = new DataType(TokenTypes.DOUBLE, "DOUBLE");
	public static final DataType FIXCHAR = new DataType(TokenTypes.FIXCHAR, "FIXCHAR");
	public static final DataType FLOAT = new DataType(TokenTypes.FLOAT, "FLOAT");
	public static final DataType HANDLE = new DataType(TokenTypes.HANDLE, "HANDLE");
	public static final DataType INTEGER = new DataType(TokenTypes.INTEGER, "INTEGER");
	public static final DataType LONG = new DataType(TokenTypes.LONG, "LONG");
	public static final DataType LONGCHAR = new DataType(TokenTypes.LONGCHAR, "LONGCHAR");
	public static final DataType LOGICAL = new DataType(TokenTypes.LOGICAL, "LOGICAL");
	public static final DataType MEMPTR = new DataType(TokenTypes.MEMPTR, "MEMPTR");
	public static final DataType NUMERIC = new DataType(TokenTypes.NUMERIC, "NUMERIC");
	public static final DataType RAW = new DataType(TokenTypes.RAW, "RAW");
	public static final DataType RECID = new DataType(TokenTypes.RECID, "RECID");
	public static final DataType ROWID = new DataType(TokenTypes.ROWID, "ROWID");
	public static final DataType SHORT = new DataType(TokenTypes.SHORT, "SHORT");
	public static final DataType TIME = new DataType(TokenTypes.TIME, "TIME");
	public static final DataType TIMESTAMP = new DataType(TokenTypes.TIMESTAMP, "TIMESTAMP");
	public static final DataType UNSIGNEDSHORT = new DataType(TokenTypes.UNSIGNEDSHORT, "UNSIGNED-SHORT");
	public static final DataType WIDGETHANDLE = new DataType(TokenTypes.WIDGETHANDLE, "WIDGET-HANDLE");

	/** Get the DataType object for an integer token type.
	 * This can return null - when you use this function, adding
	 * a check with assert or throw might be appropriate.
	 */
	public static DataType getDataType(int tokenType) { 
		return (DataType) tokenTypeMap.get(new Integer(tokenType));
	}

	/** Get the DataType object for a String "progress data type name", ex: "COM-HANDLE".
	 * <b>Requires all caps characters, not abbreviated.</b>
	 * This can return null - when you use this function, adding
	 * a check with assert or throw might be appropriate.
	 */
	public static DataType getDataType(String progressCapsName) { 
		return (DataType) nameMap.get(progressCapsName);
	}

	/** The progress name for the data type is all caps, ex: "COM-HANDLE" */ 
	public String getProgressName() { return progressName; }

	/** Returns the Proparse integer token type, ex: TokenTypes.COMHANDLE */
	public int getTokenType() { return tokenType.intValue(); }

	/** Same as getProgressName.
	 * @see #getProgressName()
	 */
	public String toString() { return progressName; }

}
