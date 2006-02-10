/* Created on Feb 3, 2005
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

import java.util.List;

import org.prorefactor.core.JPNode;



/** Abstract base class for Prolint rules which must be launched against an individual node.
 * LintRun does not launch rules by name. Instead, it launches a list of
 * CuRule objects and a list of NodeRule objects.
 * @see org.prolint.core.LintRun
 */
public abstract class NodeRule extends Rule {

	/** Get an array of Integer objects which represents the node types that this
	 * lint rule is interested in.
	 * Frequently, there is only one node type that the rule is watching for.
	 * The implementing class would typically just define a static array, and return it.
	 */
	public abstract Integer [] getWatchedNodeTypes();

	/** Launch the lint rule object for a given node. 
	 * Returns a List of TempMarker objects.
	 */
	public abstract List run(JPNode node);

}
