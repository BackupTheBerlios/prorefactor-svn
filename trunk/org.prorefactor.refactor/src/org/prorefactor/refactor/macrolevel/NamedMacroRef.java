/* NamedMacroRef.java
 * Created on Nov 29, 2003
 * John Green
 *
 * Copyright (C) 2003 Joanju Limited
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.prorefactor.refactor.macrolevel;

/** A reference to a macro argument, i.e. {1} or {&name}.
 * Origin might be an include argument or an &DEFINE.
 */
public class NamedMacroRef extends MacroRef {

	public MacroDef macroDef;

}
