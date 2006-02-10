/* Created on Jan 25, 2005
 * Authors: John Green
 *
 * Copyright (C) 2005 Prolint.org Contributors
 * This file is part of Prolint.
 *    Prolint is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *    Prolint is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *    You should have received a copy of the GNU Lesser General Public
 * License along with Prolint; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.prolint.rules;

import java.util.ArrayList;
import java.util.List;

import org.prolint.core.TempMarker;
import org.prorefactor.core.IConstants;
import org.prorefactor.core.JPNav;
import org.prorefactor.core.JPNode;
import org.prorefactor.core.TokenTypes;


/** Tests for abbreviated schema names (table and field) in a compile unit. */
public class AbbrevSchemaNames extends NodeRule {
	
	private static final String RULENAME = "abbrevname";
	
	private static Integer [] watchedTypes = {
			new Integer(TokenTypes.Field_ref)
			, new Integer(TokenTypes.RECORD_NAME)
		};

	public String getName() { return RULENAME; }

	public Integer[] getWatchedNodeTypes() { return watchedTypes; }

	public List run(JPNode node) {
		int nodeType = node.getType();
		assert nodeType==TokenTypes.Field_ref || nodeType==TokenTypes.RECORD_NAME;
		if (node.attrGet(IConstants.ABBREVIATED) != IConstants.TRUE) return null;
		JPNode idNode = node;
		if (nodeType==TokenTypes.Field_ref) idNode = JPNav.findFieldRefIdNode(node);
		int lintRunFileIndex = lintRun.getLintRunFileIndex(idNode.getFileIndex());
		if (lintRunFileIndex == -1) return null;
		TempMarker tempMarker = new TempMarker()
			.setFileIndex(lintRunFileIndex)
			.setLine(idNode.getLine())
			.setColumn(idNode.getColumn())
			.setRuleIndex(lintRunIndex.intValue())
			.setNumchars(idNode.getText().length())
			.setMessage("abbrevname: " + idNode.getText() + " is abbreviated");
		ArrayList retList = new ArrayList();
		retList.add(tempMarker);
		return retList;
	}

}
