/* XMLWriter.java
 * Created on Nov 9, 2003
 * John Green
 *
 * Copyright (C) 2003 Joanju Limited
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.prorefactor.refactor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.prorefactor.core.IConstants;
import org.prorefactor.treeparser.TreeParserWrapper;
import org.prorefactor.treeparser01.TreeParser01;

import com.joanju.ProparseLdr;


/**
 * Given an node and a filename, we write an XML representation
 * of the syntax tree.
 */
public class XMLWriter {

	private String filename;
	private ProparseLdr parser;
	private int topNode;
	private FileWriter writer = null;

	public boolean showHidden = false;

	public XMLWriter(int node, String filename) {
		this.filename = filename;
		this.topNode = node;
		parser = ProparseLdr.getInstance();
	}



	private String addAttr(int node, String attrKey) {
		String attrVal = parser.attrGetS(node, attrKey);
		if (attrVal.length()!=0)
			return " " + attrKey + "=" + "\"" + attrVal + "\"";
		return "";
	}

	private String addAttr(int node, int attrKey) {
		int attrVal = parser.attrGetI(node, attrKey);
		if (attrVal!=0)
			return " " + getAttrKeyName(attrKey) + "=" + "\"" + Integer.toString(attrVal) + "\"";
		return "";
	}

	private String getAttrKeyName(int attrKey) {
		switch (attrKey) {
		case IConstants.ABBREVIATED: return "ABBREVIATED";
		case IConstants.UNQUALIFIED_FIELD: return "UNQUALIFIED_FIELD";
		default: return ""; 
		}
	}

	private String getPos(int node) {
		return " pos=" + "\"" 
			+ Integer.toString(parser.getNodeFileIndex(node)) + " "
			+ Integer.toString(parser.getNodeLine(node)) + " "
			+ Integer.toString(parser.getNodeColumn(node))
			+ "\"";
	}

	/**
	 * Run the writer.
	 * @return A string representing any error message, null or empty if no error
	 */
	public String run() {
		String tempString;
		TreeParser01 tp01 = new TreeParser01();
		tempString = TreeParserWrapper.run(tp01, topNode);
		if (tempString!=null && tempString.length()!=0) return tempString;
		File outfile = null;
		try {
			outfile = new File(filename);
			outfile.getParentFile().mkdirs();
			outfile.createNewFile();
			writer = new FileWriter(outfile);
			walker(topNode);
			writer.close();
		} catch (IOException e) {
			return e.getMessage();
		}
		return null;
	} // run()



	private void walker(int node) throws IOException {

		int nextNode = parser.getHandle();

		String tokenName = parser.getNodeType(node);


		writer.write( 
			"<"
			+ tokenName
			+ getPos(node)
			+ addAttr(node, "storetype")
			+ addAttr(node, "operator")
			+ addAttr(node, "state2")
			+ addAttr(node, "statehead")
			+ addAttr(node, "proparsedirective")
			+ addAttr(node, "node-type-keyword")
			+ addAttr(node, "abbreviated")
			+ addAttr(node, "from-user-dict")
			+ addAttr(node, "inline-var-def")
			+ addAttr(node, IConstants.ABBREVIATED)
			+ addAttr(node, IConstants.UNQUALIFIED_FIELD)
			+ ">\n"
			);
		if (showHidden) writeHiddenBefore(node);
		String tokenText = parser.getNodeText(node);
		if (tokenText!=null && tokenText.length()>0) {
			writer.write(
				"<![CDATA[" 
				+ parser.getNodeText(node) 
				+ "]]>\n"
				);
		}

		if (parser.nodeFirstChildI(node, nextNode)!=0) {
			walker(nextNode);
		}

		writer.write("</" + tokenName + ">\n");

		if (parser.nodeNextSiblingI(node, nextNode)!=0)
			walker(nextNode);
		parser.releaseHandle(nextNode);

	} // walker()


	private void writeHiddenBefore(int node) throws IOException {
		int haveToken = parser.hiddenGetFirst(node);
		while (haveToken > 0) {
			writer.write( 
				"<hidden_token type=\""
				+ parser.hiddenGetType()
				+ "\"><![CDATA["
				+ parser.hiddenGetText()
				+ "]]></hidden_token>\n"
				);
			haveToken = parser.hiddenGetNext();
		}
	}


} // XMLWriter class

