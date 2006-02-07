/**
 * TreeParserWrapper.java
 * @author John Green
 * 18-Oct-2002
 * www.joanju.com
 * 
 * Copyright (c) 2002 Joanju Limited.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 */

package org.prorefactor.treeparser;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.prorefactor.core.JPNode;
import org.prorefactor.core.PRCException;

import com.joanju.ProparseLdr;


/**
 * This class just makes it easier to interface with
 * an Antlr generated tree parser.
 */
public class TreeParserWrapper {

	private static ProparseLdr parser = ProparseLdr.getInstance();



	/** This method wraps the pre and post processing necessary for calling a JPTreeParser. */
	public static JPNode getTree(IJPTreeParser tp, int inHandle) throws PRCException {
		// JPNode.releaseAll() will release the handle,
		// which we don't want to do to the input handle.
		// So, instead, we grab our own handle.
		int myHandle = parser.getHandle();
		parser.copyHandle(inHandle, myHandle);
		JPNode theAST = JPNode.getTree(myHandle);
		String err = run(tp, theAST);
		if (err!=null && err.length()>0) throw new PRCException(err);
		return theAST;
	}



	/** This variant does the parse, and it throws
	 * an Exception rather than return any String error message.
	 */
	public static JPNode run(String inName, IJPTreeParser tp) throws PRCException {
		parser.parse(inName);
		if (parser.errorGetStatus() < 0) throw new PRCException(parser.errorGetText());
		int topNode = parser.getHandle();
		parser.nodeTop(topNode);
		JPNode theAST = JPNode.getTree(topNode);
		String retval = run(tp, theAST);
		if (retval!=null && retval.length()>0) throw new PRCException(retval);
		return theAST;
	}



	/**
	 * This method wraps the pre and post processing
	 * necessary for calling a JPTreeParser.
	 * @param JPTreeParser Reference to a tree parser.
	 * @param inHandle Handle to the topmost node.
	 * @return String with error message or empty on success.
	 */
	public static String run(IJPTreeParser tp, int inHandle) {
		// JPNode.releaseAll() will release the handle,
		// which we don't want to do to the input handle.
		// So, instead, we grab our own handle.
		int myHandle = parser.getHandle();
		parser.copyHandle(inHandle, myHandle);
		JPNode theAST = JPNode.getTree(myHandle);
		return run(tp, theAST);
	}



	/** OLD RUN This variant requires that you have a JPNode AST to pass in. */
	public static String run(IJPTreeParser tp, JPNode theAST) {
		try {
			tp.program(theAST);
		} catch (antlr.RecognitionException e) {
			String s = new String(e.getMessage());
			JPNode leftOff = (JPNode) tp.get_retTree();
			if (leftOff != null) {
				int theNode = leftOff.getHandle();
				boolean done = false;
				while (!done) {
					s	+=	" -> File: " + parser.getNodeFilename(theNode)
						+	" Line: " + parser.getNodeLine(theNode)
						+	" Column: " + parser.getNodeColumn(theNode)
						+	" Type: " + parser.getNodeType(theNode)
						+	" Text: " + parser.getNodeText(theNode)
						;
					if (parser.getNodeLine(theNode) == 0) {
						if (parser.nodeFirstChildI(theNode, theNode) == 0) {
							// this shouldn't happen, but we'll check anyway.
							done = true;
						}
					} else
						done = true;
				}
			} else {
				s += unknownError(theAST);
			}
			return s;
		} catch (Throwable e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
		    return sw.toString();
		}
		return "";
	} // run
	
	
	
	/** Run a tree parser for a given JPNode. 
	 * @throws PRCException
	 */
	public static void run2(IJPTreeParser tp, JPNode theAST) throws PRCException {
		try {
			tp.program(theAST);
		} catch (Throwable e) {
			String s = e.getMessage();
			JPNode leftOff = (JPNode) tp.get_retTree();
			if (leftOff != null) {
				int theNode = leftOff.getHandle();
				boolean done = false;
				while (!done) {
					s	+=	" -> File: " + parser.getNodeFilename(theNode)
						+	" Line: " + parser.getNodeLine(theNode)
						+	" Column: " + parser.getNodeColumn(theNode)
						+	" Type: " + parser.getNodeType(theNode)
						+	" Text: " + parser.getNodeText(theNode)
						;
					if (parser.getNodeLine(theNode) == 0) {
						if (parser.nodeFirstChildI(theNode, theNode) == 0) {
							// this shouldn't happen, but we'll check anyway.
							done = true;
						}
					} else
						done = true;
				}
			} else {
				s += unknownError(theAST);
			}
			throw new PRCException(s, e);
		}
	} // run2

	
	private static String unknownError(JPNode theAST) {
		
		JPNode firstNatural = theAST.firstNaturalChild();
		return " -> No return node. Error on line one? "
			+ (firstNatural==null ? "" : firstNatural.getFilename());
	}



} // class
