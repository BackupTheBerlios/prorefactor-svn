/* Created on 25-Nov-2005
 * Authors: john
 *
 * Copyright (c) 2002-2005 Joanju (www.joanju.com)
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.prorefactor.core;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import org.prorefactor.treeparserbase.TokenTypesReader;



/** Prints out the structure of a JPNode AST.
 * Prints nodes one per line, using indentation to show the tree structure.
 * Use TokenLister instead if you want to print using Proparse's API directly.
 */
public class JPNodeLister {

	public JPNodeLister(JPNode topNode, String outfilename, TokenTypesReader typesReader) {
		this.topNode = topNode;
		this.outfilename = outfilename;
		this.typesReader = typesReader;
	}

	// Change the comments for the setter if you change the default here.
	private int indentby = 4;
	private int indentnum = 0;
	private FileWriter ofile;
	private HashMap<Integer, String> indentStrings = new HashMap<Integer, String>();
	private JPNode topNode;
	private String outfilename;
	// Change the comments for the setter if you change the default here.
	protected String spacer = "    ";
	protected TokenTypesReader typesReader = null;



	/** This returns the line's text including the text indent, but not including the newline.
	 * Override this method in order to generate your own line text.
	 * If you override this, you can use indent() to get the current indent string, or use
	 * getIndentby() and your own indent string generator.
	 */
	protected String generateLineText(JPNode node) {
		StringBuffer buff = new StringBuffer();
		buff
			.append(indent())
			.append(typesReader.getName(node.getType()))
			.append(spacer)
			.append(node.getText());
		return buff.toString();
	}

	
	/** Get the current indent based on indentby */
	protected final String indent() {
		String indent = indentStrings.get(indentnum);
		if (indent==null) {
			char[] indentArray = new char[indentnum];
			java.util.Arrays.fill(indentArray, ' ');
			indent = new String(indentArray);
			indentStrings.put(indentnum, indent);
		}
		return indent;
	}
	
	
	/** Call this method to write the output file. */
	public void print() throws IOException {
		typesReader.init(); 
		ofile = new FileWriter(outfilename);
		print_sub(topNode);
		ofile.close();
	}


	private void print_sub(JPNode node) throws IOException {
		printline(node);
		JPNode child = node.firstChild();
		indentnum += indentby;
		while (child!=null) {
			if (child.firstChild()!=null)
				print_sub(child);
			else
				printline(child);
			child = child.nextSibling();
		}
		indentnum -= indentby;
	}
	

	private void printline(JPNode node) throws IOException {
		ofile.write(generateLineText(node));
		ofile.write(Util.LINESEP);
	}


	/** Number of spaces to indent by.
	 * Default indentby is four spaces. You can change the number of indent spaces,
	 * or use your own indent generator when you override generateLineText.
	 */
	public JPNodeLister setIndentby(int indentby) { this.indentby = indentby; return this; }

	/** The String spacer is used to separate tokens or components of what is printed on one line.
	 * Default is four spaces.
	 */
	protected JPNodeLister setSpacer(String spacer) { this.spacer = spacer; return this; }


}
