/* JoanjuEclipse.java
 * Created on Feb 28, 2004
 * John Green
 *
 * Copyright (C) 2004 Joanju Limited
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.prorefactor.eclipse;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;


/** Utility class for common conversions etc.
 */
public class ProRefactorEclipse {


	/** Convert an IDocument offset to line/column. Returns null on error. */
	public static int [] convertOffsetToPos(IDocument doc, int offset) {
		try {
			int [] ret = new int[2];
			ret[0] = doc.getLineOfOffset(offset) + 1;
			IRegion lineInfo = doc.getLineInformation(ret[0] - 1);
			ret[1] = offset - lineInfo.getOffset() + 1;
			return ret;
		} catch (BadLocationException e) {
			return null;
		}
	}



	/** Convert line/col to IDocument offset. Returns -1 on error. */
	public static int convertPosToOffset(IDocument doc, int line, int col) {
		try {
			IRegion lineInfo = doc.getLineInformation(line - 1);
			return lineInfo.getOffset() + col - 1;
		} catch (BadLocationException e) {
			return -1;
		}
	}



	/** Get an offset for the other end of a comment.
	 * We might be going backwards or forwards, depending on if we're
	 * gathering leading or trailing comments.
	 * @param the offset of the opening slash.
	 * @param direction Either 1 or -1, depending on the direction you are going.
	 * @return the offset of the closing slash
	 */
	public static int offsetOfCommentEnd(IDocument doc, int origin, int direction) throws BadLocationException {
		int ret = origin + direction;
		int commentlevel = 1;
		char c;
		while (commentlevel>0) {
			ret += direction;
			c = doc.getChar(ret);
			if (c=='*' && doc.getChar(ret+direction)=='/') {
				ret += direction;
				commentlevel--;
				continue;
			}
			if (c=='/' && doc.getChar(ret+direction)=='*') {
				ret += direction;
				commentlevel++;
				continue;
			}
		}
		return ret;
	} // offsetOfCommentEnd



	/** Get an offset for leading comments and whitespace.
	 * The origin is assumed to be the first character of a real node.
	 * Comments separated by only one newline are considered to be the
	 * same comment.
	 */
	public static int offsetWithLeadingWhitespace(IDocument doc, int origin) {
		int ret = origin;
		int numNewlines = 0;
		try {
			for (char c = doc.getChar(--ret); ; c = doc.getChar(--ret) ) {
				if (c=='\n') {
					if (++numNewlines > 1) return ret+1;
					continue;
				}
				if (c=='/' && doc.getChar(ret-1)=='*') {
					ret = offsetOfCommentEnd(doc, ret, -1);
					numNewlines = 0;
					continue;
				}
				if (! Character.isWhitespace(c)) return ret+1;
			}
		} catch (BadLocationException e) {
			return origin;
		}
	} // offsetWithLeadingWhitespace



	/** Get an offset for trailing comments and whitespace.
	 * The origin is assumed to be the last character of a real node.
	 * We stop at the first non-commented newline.
	 */
	public static int offsetWithTrailingWhitespace(IDocument doc, int origin) {
		int ret = origin;
		try {
			for (char c = doc.getChar(++ret); ; c = doc.getChar(++ret) ) {
				if (c=='\r' || c=='\n') return origin;
				if (c=='/' && doc.getChar(ret+1)=='*') {
					// We're done at the end of the first comment.
					return 1 + offsetOfCommentEnd(doc, ret, +1);
				}
				if (! Character.isWhitespace(c)) return origin;
			}
		} catch (BadLocationException e) {
			return origin;
		}
	} // offsetWithTrailingWhitespace



} // class
