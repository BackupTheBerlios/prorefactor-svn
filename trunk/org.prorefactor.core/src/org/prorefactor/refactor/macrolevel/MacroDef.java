/* MacroDef.java
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

/**
 * A macro DEFINE (global or scoped) or UNDEFINE
 * or an include argument (named or numbered/positional).
 */
public class MacroDef {

	public MacroDef() {}

	public static final int GLOBAL = 1;
	public static final int SCOPED = 2;
	public static final int UNDEFINE = 3;
	public static final int NAMEDARG = 4;
	public static final int NUMBEREDARG = 5;

	public int column;
	public int line;
	/** One of this class's values: GLOBAL, SCOPED, UNDEFINE, NAMEDARG, NUMBEREDARG */
	public int type;
	/** For an UNDEFINE - undef what? */
	public MacroDef undefWhat = null;
	/** For an include argument - what include reference is it for? */
	public IncludeRef includeRef = null;
	/** The source where this definition can be found */
	public MacroRef parent;
	public String name;
	public String value;

	
}
