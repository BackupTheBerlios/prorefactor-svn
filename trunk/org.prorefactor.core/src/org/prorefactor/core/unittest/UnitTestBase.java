/**
 * UnitTestBase.java
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


package org.prorefactor.core.unittest;


import com.joanju.ProparseLdr;



/**
 * Base class for unit tests.
 * Does no tests itself, but does import the basics,
 * creates the parser, implements common functions.
 */
public abstract class UnitTestBase {

	protected ProparseLdr parser;


	/**
	 * This base constructor loads the parser.
	 */
	protected UnitTestBase() throws UnitTestException {
		loadParser();
	}


	/**
	 * Load the parser, throw UnitTestException if it fails to load.
	 */
	private void loadParser() throws UnitTestException {
		try {
			parser = ProparseLdr.getInstance();
		} catch (Throwable e) {
			throw new UnitTestException("Failed to load proparse.dll");
		}
	}


	/**
	 * Check the parser's error status, throw the errorText (if any)
	 * in a UnitTestException.
	 */
	protected void parserErrCheck() throws UnitTestException {
		if (parser.errorGetStatus() != 0)
			throw new UnitTestException(parser.errorGetText());
	}


	/**
	 * Print the parser version to standard out.
	 */
	protected void printVersion() {
		System.out.println("Proparse version: " + parser.getVersion());
	}


	/**
	 * Run all tests within the subclass.
	 * Must be implemented by the subclass.
	 */
	public abstract void runAllTests() throws UnitTestException;



} // class UnitTestBase

