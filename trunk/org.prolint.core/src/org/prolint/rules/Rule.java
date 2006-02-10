/* Created Mar 17, 2005
 * Authors: John Green
 * 
 * Copyright (C) 2005 Prolint.org Contributors
 * This file is part of Prolint.
 *
 * Prolint is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * Prolint is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Prolint; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.prolint.rules;

import org.prolint.core.LintRun;


/** Abstract Prolint rule.
 * This class is not intended to be subclassed by the client.
 * See NodeRule and CuRule, which are intended to be subclassed.
 */
public abstract class Rule {

	protected LintRun lintRun = null;
	protected Integer lintRunIndex = null; 	// See implementation Note[1] at end of file.

	/** All rules have an identifying short name. */
	public abstract String getName();
	
	/** All rules are associated with a LintRun */
	public LintRun getLintRun() { return lintRun; }

	/** For each IRule created for a LintRun, an Integer index is kept. */
	public Integer getLintRunIndex() { return lintRunIndex; }

	/** @see #getLintRun() */
	public Rule setLintRun(LintRun lintRun) { this.lintRun = lintRun; return this; }

	/** @see #getLintRunIndex() */
	public Rule setLintRunIndex(Integer index) { this.lintRunIndex = index; return this; }

}


/*
 * Note[1]
 * Storing an integer index for every TempMarker record in our 
 * temp-table is much more efficient than storing the string name
 * would have been. This saves us the overhead of writing a bunch
 * of strings to disk for something that is short-lived anyway.
 */
