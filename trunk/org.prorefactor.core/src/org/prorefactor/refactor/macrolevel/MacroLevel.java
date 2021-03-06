/* MacroLevel.java
 * Created on Dec 17, 2003
 * John Green
 *
 * Copyright (C) 2003 Joanju Limited
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.prorefactor.refactor.macrolevel;


/** Static functions for working with an existing macro tree.
 */
public class MacroLevel {



	/** Trace back nested macro definitions until we find the original source.
	 * @return int[3] - file, line, column.
	 */
	public static int[] getDefinitionPosition(MacroDef def) {
		int[] ret = new int[3];
		if (def.includeRef==null) {
			if (def.parent instanceof NamedMacroRef) {
				return getDefinitionPosition(((NamedMacroRef)def.parent).macroDef);
			}
			ret[0] = ((IncludeRef)def.parent).fileIndex;
			ret[1] = def.line;
			ret[2] = def.column;
		} else {
			// Include arguments don't get their file/line/col stored, so
			// we have to find the include reference source.
			if (! (def.includeRef.parent instanceof IncludeRef))
				return getDefinitionPosition(((NamedMacroRef)def.includeRef.parent).macroDef);
			ret[0] = ((IncludeRef)def.includeRef.parent).fileIndex;
			ret[1] = def.includeRef.refLine;
			ret[2] = def.includeRef.refColumn;
		}
		return ret;
	} // getDefinitionPosition

}
