/**
 * UnitTestBase2.java
 * @author John Green
 * 5-Nov-2002
 * www.joanju.com
 * 
 * Copyright (c) 2002,2004 Joanju Limited.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 */

package org.prorefactor.core.unittest;

import java.io.File;
import java.io.FileFilter;

import org.prorefactor.core.ICallback;

import junit.framework.TestCase;

import com.joanju.ProparseLdr;


/**
 * Base class for unit tests.
 * Does no tests itself, but does import the basics,
 * creates the parser, implements common functions.
 */
public class UnitTestBase2 extends TestCase {

	public UnitTestBase2(String arg0) {
		super(arg0);
	}

	/** The filter used for testCompareDirectories and testCompareFiles.
	 * The default is to ignore directories named "CVS" and ".svn".
	 */
	public FileFilter testCompareDirectoriesFilter = new FileFilter() {
		public boolean accept(File pathname) {
			if (pathname.isDirectory()) {
				if (pathname.getName().equals("CVS")) return false;
				if (pathname.getName().equals(".svn")) return false;
			}
			return true;
		}
	};

	protected ProparseLdr parser;



	/**
	 * Basic setup.
	 * Loads the parser. Sublcasses should call super()
	 * if they override.
	 */
	protected void setUp() throws Exception {
		super.setUp();
		loadParser();
	} // setUp()



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
	 * Print the parser version to standard out.
	 */
	protected void printVersion() {
		System.out.println("Proparse version: " + parser.getVersion());
	}



	/** Recursively compare two directories (ex: "out" and "expect").
	 * Does not compare file contents.
	 * Descends directories recursively.
	 * By default, ignores "CVS" directories.
	 * Tests that all files in dir1 exist in dir2, and also tests the reverse.
	 * @return String error message on first missing file,
	 * null if no differences found.
	 */
	public String testCompareDirectories(File dir1, File dir2) {
		String ret;
		ret = testCompareDirectories2(dir1, dir2, null);
		if (ret!=null) return ret;
		return testCompareDirectories2(dir2, dir1, null);
	} // testCompareDirectories



	/** Recursively compare two directories.
	 * It recursively looks at the files list from dir2, 
	 * and checks that each of those files exists in dir1. 
	 * By default, ignores "CVS" directories.
	 * This does not do the reverse test - does not test that files in dir1
	 * exist in dir2. To do that, call this function a second time with the
	 * directories reversed.
	 * @param dir1 All files in dir2 are tested for existence in dir1.
	 * @param dir2 All files in dir2 are tested for existence in dir1.
	 * @param fileTest A callback to a test. It will receive a Pair (the two
	 * File objects), and is expected to return null if no difference, or a String
	 * error message otherwise. Not called for directories.
	 * @return String error message on the first missing file or file difference,
	 * null if no differences found.
	 */
	public String testCompareDirectories2(File dir1, File dir2, ICallback fileTest) {
		if (! dir1.exists() || ! dir1.isDirectory())
			return (dir1.toString() + " does not exist or is not a directory.");
		if (! dir2.exists() || ! dir2.isDirectory())
			return (dir2.toString() + " does not exist or is not a directory.");
		File [] files1 = dir1.listFiles(testCompareDirectoriesFilter);
		File [] files2 = dir2.listFiles(testCompareDirectoriesFilter);
		String ret;
		for (int i=0; i < files2.length; i++) {
			if (	i + 1 > files1.length
				||	(! files2[i].getName().equals(files1[i].getName()))
				)
				return (files1[i].toString() + ": File list difference: " + files2[i].toString());
			if (files2[i].isDirectory()) {
				ret = testCompareDirectories2(files1[i], files2[i], fileTest);
				if (ret!=null) return ret;
			} else {
				if (! files1[i].isFile())
					return (files1[i].toString() + " is not a file");
				if (fileTest!=null) {
					ret = (String) fileTest.run(new Object[]{files1[i], files2[i]});
					if (ret!=null) return ret;
				}
			}
		}
		return null;
	} // testCompareDirectories2



	/** Recursively compare files in two directories (ex: "out" and "expect").
	 * Uses parser.diff() to compare each file in the two directories.
	 * Descends directories recursively.
	 * By default, ignores "CVS" directories.
	 * Tests that all files in dir1 exist in dir2, and also tests the reverse.
	 * @return String error message on first difference or missing file,
	 * null if no differences found.
	 */
	public String testCompareFiles(File dir1, File dir2) {
		ICallback callback = new ICallback() {
			public Object run(Object obj) {
				Object [] files = (Object[]) obj;
				File first = (File) files[0];
				File second = (File) files[1];
				String ret = parser.diff(first.getAbsolutePath(), second.getAbsolutePath());
				if (ret.length() != 0)
					return first.toString() + " difference at line " + ret;
				return null;
			}
		};
		String ret;
		ret = testCompareDirectories2(dir1, dir2, null);
		if (ret!=null) return ret;
		return testCompareDirectories2(dir2, dir1, callback);
	} // testCompareFiles



	/** Compare two files.
	 * Uses parser.diff() to compare the two files.
	 * @return String error message on first difference or missing file,
	 * null if no differences found.
	 */
	public String testCompareSingle(File first, File second) {
		if (! first.exists()) return "File does not exist: " + first.getAbsolutePath();
		if (! second.exists()) return "File does not exist: " + second.getAbsolutePath();
		String ret = parser.diff(first.getAbsolutePath(), second.getAbsolutePath());
		if (ret.length() != 0)
			return first.toString() + " difference at line " + ret;
		return null;
	} // testCompareSingle



}

