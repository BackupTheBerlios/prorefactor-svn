/* Created on Apr 11, 2005
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
import org.prorefactor.core.JPNode;
import org.prorefactor.core.TokenTypes;

import com.joanju.ProparseLdr;


/** Tests for statements that have no effect
 *
 * To Do Later: Make the "nobracketlist" configurable like Prolint/4GL. 
 */
public class NoEffect extends NodeRule {
	
	private static final String RULENAME = "noeffect";
	
	private static Integer [] watchedTypes = {
			new Integer(TokenTypes.Expr_statement)
		};

	public String getName() { return RULENAME; }

	public Integer[] getWatchedNodeTypes() { return watchedTypes; }

	public List run(JPNode node) {
		int nodeType = node.getType();
		assert nodeType==TokenTypes.Expr_statement;
		if (mightHaveSideEffects(node)) return null;
		/* "Expr_statement" is a synthetic node, so it won't have a line
		 * number. Get the first non-synthetic node.
		 */
		JPNode realNode = node.firstNaturalChild();
		int lintRunFileIndex = lintRun.getLintRunFileIndex(realNode.getFileIndex());
		if (lintRunFileIndex == -1) return null;
		TempMarker tempMarker = new TempMarker()
			.setFileIndex(lintRunFileIndex)
			.setLine(realNode.getLine())
			.setColumn(realNode.getColumn())
			.setRuleIndex(lintRunIndex.intValue())
			.setNumchars(realNode.getText().length())
			.setMessage("noeffect: Statement has no effect"); // Begins with rule name for sorting.
		ArrayList retList = new ArrayList();
		retList.add(tempMarker);
		return retList;
	}


	private boolean mightHaveSideEffects(JPNode node) {
		ProparseLdr parser = ProparseLdr.getInstance();
		final String qname = "rule_noeffect";
		int nodeHandle = node.getHandle();
		/* User-defined functions and the following built-in functions might have an effect. */
		if (parser.queryCreate(nodeHandle, qname, "USER_FUNC") > 0) return true;
		if (parser.queryCreate(nodeHandle, qname, "DYNAMICFUNCTION") > 0) return true;
		if (parser.queryCreate(nodeHandle, qname, "ETIME") > 0) return true;
		if (parser.queryCreate(nodeHandle, qname, "SETUSERID") > 0) return true;
		if (parser.queryCreate(nodeHandle, qname, "SUPER") > 0) return true;
		/* A statement like handle:READ-ONLY has no effect, 
	     * a statement like handle:GET-FIRST() does have effect,
	     * so look for OBJCOLON _not_ followed by node type "Method_param_list"
	     */
		int resultHandle = parser.getHandle();
		int numResults = parser.queryCreate(nodeHandle, qname, "OBJCOLON");
		for (int i = 1; i <= numResults; i++) {
			parser.queryGetResult(qname, i, resultHandle);
			parser.nodeNextSiblingI(resultHandle, resultHandle);
			/* methods are supposed to have (), but these can be omitted if there are
			 * no parameters. When omitted, noeffect will raise a warning.
			 * Try to suppress the warning for some of commonly used methods.
			 * Re-use the same list of methods in rule 'nobrackets'
			 */
			final String nobracketlist = "QUERY-OPEN,GET-NEXT,GET-FIRST,GET-PREV,GET-LAST,CLOSE-QUERY,SELECT-ALL";
			if (nobracketlist.indexOf(parser.getNodeText(resultHandle).toUpperCase()) >=0) return true;
			/* From "widattr" in the tree spec: 
			 *   (OBJCOLON . #(Array_subscript...)? #(Method_param_list...)? )+ 
			 * First, move to the method or attribute name
			 * node - the . (i.e. any) token after the OBJCOLON. Then, simply check next
			 * sibling twice. First time might be Array_subscript.
			 */
			if (parser.nodeNextSibling(resultHandle,resultHandle).equals("Method_param_list")) return true;
			if (parser.nodeNextSibling(resultHandle,resultHandle).equals("Method_param_list")) return true;
		}
		return false;
	}

}
