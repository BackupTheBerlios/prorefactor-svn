/* Created on Apr 25, 2005
 * Authors: John Green
 * Copyright (c) 2005 Joanju (www.joanju.com)
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.prorefactor.treeparser;


/** Field and Variable implement Primative because they
 * both have a "primative" Progress data type (INTEGER, CHARACTER, etc).
 */
public interface Primative {

	/** The name of the CLASS that this variable was defined for.
	 * This is more interesting than getDataType, which returns CLASS.
	 * Returns null if this variable was not defined for a CLASS.
	 */
	public String getClassName();
	public DataType getDataType();
	public Primative setClassName(String className);
	public Primative setDataType(DataType dataType);

}
