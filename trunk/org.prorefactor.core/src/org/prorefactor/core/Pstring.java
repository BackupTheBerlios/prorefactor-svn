/**
 * Pstring.java
 * @author John Green
 * 24-Oct-2002
 * www.joanju.com
 * 
 * Copyright (c) 2002 Joanju Limited.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 */

package org.prorefactor.core;


/**
 * This class is for working with the text of Proparse's QSTRING nodes.
 * Proparse's QSTRING nodes contain the string literal, including the
 * delimiting quotation marks as well as any string attributes.
 * This class will allow us to easily fetch and work with things like
 * just the text portion, just the attributes portion, check if the
 * delimiting quotes are single-quotes or double-quotes, etc.
 */
public class Pstring {

	private char theQuote;
	private String theText;
	private String theAttributes;


	/**
	 * Constructor - should generally only be constructed
	 * by passing in the results of parser.getNodeText()
	 */
	public Pstring(String inString) {
		theQuote = inString.charAt(0);
		int secondQuote = inString.lastIndexOf(theQuote);
		theText = inString.substring(1, secondQuote);
		theAttributes = inString.substring(secondQuote + 1);
	}



	/**
	 * Get the string attributes, including the colon.
	 */
	public String getAttributes() {
		return theAttributes;
	}



	/**
	 * Get the character quotation mark.
	 */
	public char getQuote() {
		return theQuote;
	}



	/**
	 * Is this string translatable?
	 * @return True if translatable
	 */
	public boolean isTrans() {
		return theAttributes.indexOf('U') < 0 && theAttributes.indexOf('u') < 0;
	}



	/**
	 * Just the text portion of the node's text - er - you know.
	 */
	public String justText() {
		return theText;
	}


} // class Pstring

