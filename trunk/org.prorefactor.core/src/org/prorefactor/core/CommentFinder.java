/* CommentFinder.java
 * Created on Nov 6, 2003
 * John Green
 *
 * Copyright (C) 2003 Joanju Limited
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.prorefactor.core;

import com.joanju.ProparseLdr;

/**
 * Use this class to find specific comments in,
 * before, or after a node hierarchy.
 * See org.prorefactor.refactor.unittest.NoUndoT.java (and its data file)
 * for a complete unit test and example for this class.
 * Currently the only search option is a case insensitive search.
 */
public class CommentFinder {

	public CommentFinder() {
	}

	private String findString = null;
	private int numResults = 0;
	private ProparseLdr parser = ProparseLdr.getInstance();
	private JPUtil pluspack = JPUtil.getInstance();



	/** Review the text of the current comment, to see if it matches. */
	public int commentTextReview() {
		if (! parser.hiddenGetType().equals("COMMENT")) return 0;
		String theText = parser.hiddenGetText();
		if (theText==null || theText.length()==0) return 0;
		if (theText.toLowerCase().indexOf(findString) > -1) return 1;
		return 0;
	}

	/** Find comments which come after the last descendant of the
	 * node, which match, and are not separated from that last
	 * sibling by any newline characters.
	 * @param node
	 * @return number of COMMENT tokens which match.
	 */
	public int examineAfter(int node) {
		int numAfter = 0;
		if (pluspack.findFirstHiddenAfterLastDescendant(node) < 1) return 0;
		while (true) {
			if (parser.hiddenGetType().equals("COMMENT")) {
				numAfter += commentTextReview();
			} else {
				if (parser.hiddenGetText().indexOf('\n') > -1) break;
			}
			if (parser.hiddenGetNext() < 1) break;
		}
		return numAfter;
	}

	/** Find comments before the node which match and
	 * are not separated from the node by any blank lines.
	 * @param node
	 * @return number of COMMENT tokens which match.
	 */
	public int examineBefore(int node) {
		int numBefore = 0;
		if (parser.hiddenGetBefore(node) < 1) return 0;
		int consecutiveBreaks = 0;
		while (true) {
			if (parser.hiddenGetType().equals("COMMENT")) {
				numBefore += commentTextReview();
				consecutiveBreaks = 0;
			} else {
				String theText = parser.hiddenGetText();
				int firstBreak = theText.indexOf('\n');
				if (firstBreak > -1) {
					// Look for two line breaks in the same token
					int secondBreak = theText.lastIndexOf('\n');
					if (secondBreak!=firstBreak) break;
					consecutiveBreaks++;
					if (consecutiveBreaks > 1) break;
				}
			}
			if (parser.hiddenGetPrevious() < 1) break;
		}
		return numBefore;
	}

	/** Find comments before the node which match.
	 * @param node
	 * @return number of COMMENT tokens which match.
	 */
	public int examineInner(int node) {
		int numInner = 0;
		if (parser.hiddenGetBefore(node) < 1) return 0;
		numInner += commentTextReview();
		while (parser.hiddenGetPrevious() > 0) {
			numInner += commentTextReview();
		}
		return numInner;
	}


	public void setFindString(String input) {
		findString = input.toLowerCase();
	}


	/** Return the number of COMMENT tokens which meet the search criteria */
	public int search(int node) {
		numResults = 0;
		numResults += examineBefore(node);
		numResults += examineAfter(node);
		int firstChild = parser.getHandle();
		if (parser.nodeFirstChildI(node, firstChild) > 0) walkDescendants(firstChild);
		parser.releaseHandle(firstChild);
		return numResults;
	}

	/** Recursively examine the descendants of the node, incrementing numResults.
	 */
	private void walkDescendants(int node) {
		int nextNode = parser.getHandle();
		numResults += examineInner(node);
		if (parser.nodeFirstChildI(node, nextNode) > 0) walkDescendants(nextNode);
		if (parser.nodeNextSiblingI(node, nextNode) > 0) walkDescendants(nextNode);
		parser.releaseHandle(nextNode);
	}


} // class CommentFinder
