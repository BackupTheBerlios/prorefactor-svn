/**
 * AttributedWriter.java
 * @author John Green
 * 2004 May 26
 * www.joanju.com
 * 
 * Copyright (c) 2004 Joanju Limited.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 */

package org.prorefactor.core.unittest;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.TreeSet;

import org.prorefactor.core.IConstants;
import org.prorefactor.core.JPNode;
import org.prorefactor.core.TokenTypes;
import org.prorefactor.treeparser.Block;
import org.prorefactor.treeparser.Symbol;
import org.prorefactor.treeparser.TableBuffer;
import org.prorefactor.treeparser.TreeParserWrapper;
import org.prorefactor.treeparser01.TreeParser01;

import com.joanju.ProparseLdr;


public class AttributedWriter {

	BufferedWriter writer = null;
	ProparseLdr parser = ProparseLdr.getInstance();



	private String getAttributes(JPNode node) {
		StringBuffer nodeComments = new StringBuffer("");
		getAttributesForSymbol(node, nodeComments);
		getAttributesForBlock(node, nodeComments);
		if (nodeComments.length() > 0) {
			nodeComments.insert(0, " /*");
			nodeComments.append(" */ ");
			if (node.getType() == TokenTypes.Program_root)
				nodeComments.append(System.getProperty("line.separator"));
		}
		return nodeComments.toString();
	} // getAttributes



	private void getAttributesForBlock(JPNode node, StringBuffer nodeComments) {
		Block block = (Block) node.getLink(JPNode.BLOCK);
		if (block==null) return;
		TableBuffer [] buffers = block.getBlockBuffers();
		if (buffers.length==0) return;
		// Collect the names in a sorted set, so we can write them in
		// a consistent (sorted) order. Important for running automated
		// unit/regression tests.
		TreeSet names = new TreeSet();
		for (int i = 0; i < buffers.length; i++) {
			TableBuffer buffSymbol = buffers[i];
			StringBuffer name = new StringBuffer();
			if (buffSymbol.getTable().getStoretype() == IConstants.ST_DBTABLE) {
				name.append(buffSymbol.getTable().getDatabase().getName());
				name.append(".");
			}
			name.append(buffSymbol.getName());
			names.add(name.toString());
		}
		nodeComments.append(" buffers=");
		int i = 0;
		for (Iterator it = names.iterator(); it.hasNext(); i++ ) {
			String name = (String) it.next();
			if (i>0) nodeComments.append(",");
			nodeComments.append(name);
		}
	}



	private void getAttributesForSymbol(JPNode node, StringBuffer nodeComments) {
		Symbol symbol = (Symbol) node.getLink(JPNode.SYMBOL);
		if (symbol==null) return;
		nodeComments.append(" ");
		nodeComments.append(symbol.getScope().depth());
		nodeComments.append(":");
		nodeComments.append(symbol.fullName());
		if (node.attrGet(IConstants.ABBREVIATED) > 0)
			nodeComments.append(" abbrev");
		if (node.attrGet(IConstants.UNQUALIFIED_FIELD) > 0) 
			nodeComments.append(" unqualfield");
	}



	private void walker(JPNode node, boolean showSiblings) throws IOException {
		if (node==null) return;
		if (node.attrGet(IConstants.OPERATOR) == IConstants.TRUE) {
			walker(node.firstChild(), false);
			writeNode(node);
			walker(node.firstChild().nextSibling(), true);
		} else {
			writeNode(node);
			walker(node.firstChild(), true);
		}
		if (showSiblings) walker(node.nextSibling(), true);
	} // walker



	/** Parse and write a source file, with comments detailing some of the
	 * node attributes added by TreeParser01.
	 * @param inName Name of the compile unit's source file.
	 * @param outName Name of the file to write out to.
	 */
	public void write(String inName, String outName) throws Exception {
		try {
			TreeParser01 tp = new TreeParser01();
			JPNode theTree = TreeParserWrapper.run(inName, tp);
			writer = new BufferedWriter(new FileWriter(outName));
			walker(theTree, true);
		} finally {
			if (writer!=null) writer.close();
		}
	} // write



	private void writeNode(JPNode node) throws IOException {
		int haveHidden = parser.hiddenGetFirst(node.getHandle());
		while (haveHidden > 0) {
			writer.write(parser.hiddenGetText());
			haveHidden = parser.hiddenGetNext();
		}
		writer.write(getAttributes(node));
		writer.write(node.getText());
	}



}
