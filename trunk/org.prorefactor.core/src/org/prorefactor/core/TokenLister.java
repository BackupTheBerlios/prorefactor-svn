/* TokenLister.java
 */

package org.prorefactor.core;


import java.io.*;

import com.joanju.ProparseLdr;


public class TokenLister {

	public TokenLister(int topNode, String outfilename_) {
		this.topNode = topNode;
		outfilename = outfilename_;
		showColumn = false;
		showFilename = false;
		showLinenum = false;
		showStoretype = false;
		indentby = 4;
		indentnum = 0;
		grandchild = parser.getHandle();
	}

	public boolean showColumn;
	public boolean showFilename;
	public boolean showLinenum;
	public boolean showStoretype;
	public int indentby;
	public String outfilename;

	private int topNode;
	private ProparseLdr parser = ProparseLdr.getInstance();
	private int grandchild;
	private int indentnum;
	private FileWriter ofile;


	public void print() throws IOException {
		ofile = new FileWriter(outfilename);
		print_sub(topNode);
		parser.releaseHandle(grandchild);
		ofile.close();
	}


	private void print_sub(int node) throws IOException {
		int child = parser.getHandle();
		printline(node);
		String nodeType = parser.nodeFirstChild(node, child);
		indentnum += indentby;
		while (! nodeType.equals("")) {
			if (! parser.nodeFirstChild(child, grandchild).equals(""))
				print_sub(child);
			else
				printline(child);
			nodeType = parser.nodeNextSibling(child, child);
		}
		indentnum -= indentby;
		parser.releaseHandle(child);
	}
	
	
	private void printline(int node) throws IOException {
		char[] indent = new char[indentnum];
		java.util.Arrays.fill(indent, ' ');
		String spacer = "    ";
		ofile.write(indent);
		ofile.write(parser.getNodeType(node) + spacer);
		ofile.write(parser.getNodeText(node) + spacer);
		if (showLinenum) {
			ofile.write(Integer.toString(parser.getNodeLine(node)));
			ofile.write(" ");
		}
		if (showColumn) {
			ofile.write(Integer.toString(parser.getNodeColumn(node)));
			ofile.write(" ");
		}
		if (showFilename)
			ofile.write(parser.getNodeFilename(node) + spacer);
		if (showStoretype) {
			String storetype = parser.attrGetS(node, "storetype");
			if (storetype.length() != 0)
				ofile.write(storetype + spacer);
		}
		ofile.write("\n");
	}

} // class TokenLister
