/**
 * JPNode
 * @author John Green
 * October, 2002
 * www.joanju.com
 * 
 * Copyright (c) 2002-2004 Joanju Limited.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 */

package org.prorefactor.core;


import com.joanju.ProparseLdr;

import antlr.BaseAST;
import antlr.Token;
import antlr.collections.AST;

import java.util.HashMap;



/**
 * Extension to antlr.BaseAST, which allows us to extract an
 * external "antlr" AST view of a Proparse AST, which we can
 * then run tree parsers against.
 * Note that tree transformation functions are currently (Feb 2004)
 * untested and unused, since we tend to only use the AST for
 * analysis and not for code motion.
 */
public class JPNode extends BaseAST implements IJPNode {


	public JPNode(int handle) {
		nodeHandle = handle;
		setType(parser.getNodeTypeI(nodeHandle));
	}

	public JPNode(int handle, TreeConfig config) {
		nodeHandle = handle;
		setType(parser.getNodeTypeI(nodeHandle));
		if (config!=null) this.configure(config);
	}

	/** For temporary nodes for comparison in set of nodes sorted by position */
	public JPNode(int file, int line, int column) {
		this.fileIndex = file;
		this.line = line;
		this.column = column;
	}

	/** Just an Integer object for the int IConstants.STATE2 */
	public static final Integer STATE2 = new Integer(IConstants.STATE2);

	/** Just an Integer object for the int IConstants.CONTEXT_QUALIFIER */
	public static final Integer CONTEXT_QUALIFIER = new Integer(IConstants.CONTEXT_QUALIFIER);

	/** A valid value for setLink() and getLink() */
	public static final Integer SYMBOL = new Integer(-210);

	/** A valid value for setLink() and getLink() */
	public static final Integer TETNode = new Integer(-211);

	/** A valid value for setLink() and getLink().
	 * Link to a BufferScope object, set by tp01 for RECORD_NAME nodes
	 * and for Field_ref nodes for Field (not for Variable).
	 * Will not be present if this Field_ref is a reference to the
	 * symbol without referencing its value (i.e. no buffer scope).
	 * @see #BUFFERSYMBOL
	 */
	public static final Integer BUFFERSCOPE = new Integer(-212);

	/** A valid value for setLink() and getLink() */
	public static final Integer BLOCK = new Integer(-214);

	static private ProparseLdr parser = ProparseLdr.getInstance();

	private int column = -1;
	private int fileIndex = -1;
	private int line = -1;
	private int nodeHandle = 0;
	private int type = 0;
	private HashMap attrMap;
	private JPNode parent;
	private String filename;
	private String text;

	public static class TreeConfig {
		public TreeConfig() {}
		/** For specialized per-node configuration, create a callback. */
		public ICallback callback;
		/** Disconnected mode? Default is false. */
		boolean disconnected = false;
		String [] filenames;
		public void makeDisconnected(String [] filenames) {
			this.filenames = filenames;
			disconnected = true;
		}
		/** Store the file/line/column positions in JPNode?
		 * Some functions make heavy use of these node attributes.
		 * For those, it makes sense to store those attributes within JPNode,
		 * rather than going through Proparse, to reduce function call overhead.
		 * Note that if "disconnected", this flag is meaningless.
		 */
		public boolean storePosition = false;
	} // class TreeConfig



	//// Static funcs


	public static JPNode getTree(int inHandle) {
		return getTree(inHandle, null, null);
	}

	public static JPNode getTree(int inHandle, TreeConfig config) {
		return getTree(inHandle, config, null);
	}

	private static JPNode getTree(int inHandle, TreeConfig config, JPNode parent) {
		JPNode thisNode = new JPNode(inHandle, config);
		thisNode.parent = parent;
		int handle = parser.getHandle();
		if (parser.nodeFirstChildI(inHandle, handle) != 0) {
			thisNode.down = getTree(handle, config, thisNode);
			handle = parser.getHandle();
		}
		if (parser.nodeNextSiblingI(inHandle, handle) != 0) {
			thisNode.right = getTree(handle, config, parent);
		} else {
			parser.releaseHandle(handle);
		}
		return thisNode;
	}



	//// Member funcs



	/** Get an attribute from proparse if connected, from JPNode if disconnected */
	public int attrGet(int key) {
		if (nodeHandle!=0) return parser.attrGetI(nodeHandle, key);
		if (attrMap==null) return 0;
		Integer retInt = (Integer) attrMap.get(new Integer(key));
		if (retInt==null) return 0;
		return retInt.intValue();
	}



	/** Get an attribute from proparse if connected, from JPNode if disconnected */
	public int attrGet(Integer key) {
		if (nodeHandle!=0) return parser.attrGetI(nodeHandle, key.intValue());
		if (attrMap==null) return 0;
		Integer retInt = (Integer) attrMap.get(key);
		if (retInt==null) return 0;
		return retInt.intValue();
	}



	/** Set an attribute in proparse if connected, in JPNode if disconnected */
	public void attrSet(int key, int val) {
		if (nodeHandle!=0) parser.attrSet(nodeHandle, key, val);
		else {
			if (attrMap==null) initMap();
			attrMap.put(new Integer(key), new Integer(val));
		} 
	}



	/** Set an attribute in proparse if connected, in JPNode if disconnected */
	public void attrSet(Integer key, int val) {
		if (nodeHandle!=0) parser.attrSet(nodeHandle, key.intValue(), val);
		else {
			if (attrMap==null) initMap();
			attrMap.put(key, new Integer(val));
		} 
	}



	private void clearParentFromChildren() {
		for (AST child = getFirstChild(); child!=null; child = child.getNextSibling()) {
			((JPNode)child).parent = null;
		}
	}



	private void configure(TreeConfig config) {
		if (config.disconnected) {
			attrSet(JPNode.STATE2, parser.attrGetI(nodeHandle, IConstants.STATE2));
			fileIndex = parser.getNodeFileIndex(nodeHandle);
			filename = config.filenames[fileIndex];
			line = parser.getNodeLine(nodeHandle);
			column = parser.getNodeColumn(nodeHandle);
			nodeHandle = 0;
		} else if (config.storePosition) {  // storePosition is meaningless if disconnected
			fileIndex = parser.getNodeFileIndex(nodeHandle);
			line = parser.getNodeLine(nodeHandle);
			column = parser.getNodeColumn(nodeHandle);
		}
		if (config.callback != null) config.callback.run(this);
	} // configure



	public void disconnect() {
		nodeHandle = 0;
	}



	public void disconnectBranch() {
		nodeHandle = 0;
		if (down != null) ((JPNode)down).disconnectBranch2();
	}
	private void disconnectBranch2() {
		nodeHandle = 0;
		if (down != null) ((JPNode)down).disconnectBranch2();
		if (right != null) ((JPNode)right).disconnectBranch2();
	}



	public JPNode firstChild() {
		return (JPNode) getFirstChild();
	}



	/** Find the first direct child with a given node type. */
	public JPNode findDirectChild(int nodeType) {
		for (JPNode node = firstChild(); node!=null; node = node.nextSibling()) {
			if (node.getType() == nodeType) return node;
		}
		return null;
	}




	/**
	 * First Natural Child is found by repeating firstChild() until a natural node is found.
	 * If the start node is a natural node, then it is returned.
	 * The test for "natural" node is line number. Synthetic node line numbers == 0.
	 * Note: This is very different than Prolint's "NextNaturalNode" in lintsuper.p.
	 */
	public JPNode firstNaturalChild() {
		if (getLine()>0) return this;
		for (JPNode n = (JPNode)getFirstChild(); n!=null; n = (JPNode)n.getFirstChild()) {
			if (n.getLine()>0) return n;
		}
		return null;
	} // firstNaturalChild()



	public int getColumn() {
		if (column == -1) return parser.getNodeColumn(nodeHandle);
		return column;
	}



	public int getFileIndex() {
		if (fileIndex == -1) return parser.getNodeFileIndex(nodeHandle);
		return fileIndex;
	}



	public String getFilename() {
		if (filename==null) return parser.getNodeFilename(nodeHandle);
		return filename;
	}



	public int getHandle() {
		return nodeHandle;
	}



	public int getLine() {
		if (line == -1) return parser.getNodeLine(nodeHandle);
		return line;
	}



	/** Get a link to an arbitrary object.
	 * We use the attributes map for this. Note that negative numbers
	 * are invalid attribute numbers in Proparse, so we use negative
	 * numbers here, in order to avoid clash. Integers from -100
	 * through -199 are free for any use.
	 * Integers from -200 through -499 are reserved for Joanju.
	 */
	public Object getLink(Integer key) {
		if (attrMap==null) return null;
		return attrMap.get(key);
	}



	/** Return int[3] of nodes file/line/col. */
	public int[] getPos() {
		return new int[] {getFileIndex(), getLine(), getColumn()};
	}



	public int getState2() {
		return attrGet(JPNode.STATE2);
	}



	private void initMap() {
		if (attrMap==null) attrMap = new HashMap();
	}



	/** Return the last immediate child (no grandchildren). */
	public JPNode lastChild() {
		JPNode ret = firstChild();
		if (ret==null) return null;
		while (ret.nextSibling()!=null) ret = ret.nextSibling();
		return ret;
	}



	public JPNode lastDescendant() {
		JPNode ret = lastChild();
		for (JPNode temp = ret; temp!=null; temp = ret.lastChild()) {
			ret = temp;
		}
		return ret;
	}


	public JPNode nextSibling() {
		return (JPNode) getNextSibling();
	}



	public JPNode parent() { return parent; }



	/** @see #getLink(Integer) */
	public void setLink(Integer key, Object value) {
		if (attrMap==null) initMap(); 
		attrMap.put(key, value);
	}



	private void setParentInChildren() {
		for (AST child = getFirstChild(); child!=null; child = child.getNextSibling()) {
			((JPNode)child).parent = this;
		}
	}



	/** Walk the tree from the input node down. */
	public void walk(ICallback callback) {
		callback.run(this);
		JPNode child;
		if ((child=firstChild()) != null) child.walk2(callback);
	}
	private void walk2(ICallback callback) {
		JPNode next;
		if ((next=firstChild()) != null) next.walk2(callback);
		callback.run(this);
		if ((next=nextSibling()) != null) next.walk2(callback);
	}





	//// Override some BaseAST functions

	public void initialize(int t, String txt) {
		setType(t);
		setText(txt);
	}

	public void initialize(AST t) {
		setType(t.getType());
		setText(t.getText());
	}

	public void initialize(Token t) {
		setType(t.getType());
		setText(t.getText());
	}

	/** CURRENTLY ONLY SUPPORTED IF DISCONNECTED */
	public void addChild(AST node) {
		if (nodeHandle!=0) throw new RuntimeException("Attempt to modify JPNode with proparse node handle.");
		super.addChild(node);
		((JPNode)node).parent = this;
	}

	/** Get the token text for this node, from proparse if connected, from JPNode otherwise */
	public String getText() {
		if (nodeHandle!=0) return parser.getNodeText(nodeHandle);
		if (text==null) return "";
		return text;
	}

	/** Get the type -- always from JPNode, never from proparse */
	public int getType() {
		return type;
	}

	/** CURRENTLY ONLY SUPPORTED IF DISCONNECTED.
	 * Clears "parent".
	 */
	public void removeChildren() {
		if (nodeHandle!=0) throw new RuntimeException("Attempt to modify JPNode with proparse node handle.");
		clearParentFromChildren();
		super.removeChildren();
	}

	/** CURRENTLY ONLY SUPPORTED IF DISCONNECTED.
	 * Clears and sets "parent".
	 */
	public void setFirstChild(AST c) {
		if (nodeHandle!=0) throw new RuntimeException("Attempt to modify JPNode with proparse node handle.");
		clearParentFromChildren();
		super.setFirstChild(c);
		setParentInChildren();
	}

	/** CURRENTLY ONLY SUPPORTED IF DISCONNECTED.
	 * Clears and sets "parent".
	 */
	public void setNextSibling(AST n) {
		if (nodeHandle!=0) throw new RuntimeException("Attempt to modify JPNode with proparse node handle.");
		for (AST next = getNextSibling(); next!=null; next = next.getNextSibling()) {
			((JPNode)next).parent = null;
		}
		super.setNextSibling(n);
		for (AST next = getNextSibling(); next!=null; next = next.getNextSibling()) {
			((JPNode)next).parent = this.parent;
		}
	}

	/** Set the text for this node.
	 * Sets the text in proparse if connected, in JPNode otherwise.
	 */
	public void setText(String text) {
		if (nodeHandle!=0) parser.setNodeText(nodeHandle, text);
		else this.text = text;
	}

	/** Set the type in JPNode, as well as in proparse if connected. */
	public void setType(int type) {
		if (nodeHandle!=0) parser.setNodeTypeI(nodeHandle, type);
		this.type = type;
	}


} // class JPNode
