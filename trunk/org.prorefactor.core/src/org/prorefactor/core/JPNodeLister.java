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

import org.prorefactor.treeparserbase.TokenTypesReader;



/** Prints out the structure of a JPNode AST.
 * Prints nodes one per line, using indentation to show the tree structure.
 * Use TokenLister instead if you want to print using Proparse's API directly.
 */
public class JPNodeLister {

	public JPNodeLister(JPNode topNode, String outfilename, TokenTypesReader typesReader) {
		this.topNode = topNode;
		this.outfilename = outfilename;
		indentby = 4;
		indentnum = 0;
		this.typesReader = typesReader;
	}

	public int indentby;
	public String outfilename;

	private JPNode topNode;
	private int indentnum;
	private FileWriter ofile;
	private TokenTypesReader typesReader = null;


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
		char[] indent = new char[indentnum];
		java.util.Arrays.fill(indent, ' ');
		String spacer = "    ";
		ofile.write(indent);
		ofile.write(typesReader.getName(node.getType()) + spacer);
		ofile.write(node.getText() + spacer);
		ofile.write(Util.LINESEP);
	}

}
