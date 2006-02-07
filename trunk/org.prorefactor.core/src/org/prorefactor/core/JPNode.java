/** Created: October, 2002
 * Authors: John Green
 * 
 * Copyright (c) 2002-2005 Joanju (www.joanju.com)
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.prorefactor.core;


import java.util.ArrayList;
import java.util.HashMap;

import org.prorefactor.nodetypes.NodeFactory;
import org.prorefactor.treeparser.FieldContainer;
import org.prorefactor.treeparser.Symbol;

import antlr.BaseAST;
import antlr.Token;
import antlr.collections.AST;

import com.joanju.ProparseLdr;



/**
 * Extension to antlr.BaseAST, which allows us to extract an
 * external "antlr" AST view of a Proparse AST, which we can
 * then run tree parsers against.
 * Note that tree transformation functions are currently (Feb 2004)
 * untested and unused, since we tend to only use the AST for
 * analysis and not for code motion.
 */
public class JPNode extends BaseAST implements IJPNode {

	/** For creating from persistent storage */
	public JPNode() { }
	
	public static final TreeConfig nullConfig = null;

	
	/** Create an node with a given token type.
	 * Used extensively by Antlr auto-generated tree constructors.
	 */
	public JPNode(int type) {
		this.type = type;
	}

	/** If this AST is constructed from another, then create with link to the original. */
	public JPNode(int type, JPNode original) {
		this.type = type;
		setLink(ORIGINAL, original);
	}
	
	public JPNode(int type, String text) {
		this.type = type;
		this.text = text;
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
	/** A valid value for setLink() and getLink().
	 * You should not use this directly. Only JPNodes of subtype BlockNode
	 * will have this set, so use BlockNode.getBlock instead.
	 * @see org.prorefactor.nodetypes.BlockNode.
	 */
	public static final Integer BLOCK = new Integer(-214);
	/** A valid value for setLink() and getLink() */
	private static final Integer COMMENTS = new Integer(-215);
	/** A valid value for setLink() and getLink().
	 * If this AST was constructed from another, then this is the link to the original.
	 */
	private static final Integer ORIGINAL = new Integer(-216);
	/** A valid value for setLink() and getLink().
	 * @see #getFieldContainer().
	 */
	private static final Integer FIELD_CONTAINER = new Integer(-217);

	
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
		/** Store the comments?
		 * By default, a "disconnected" tree does not store comments.
		 * @see JPNode#getComments().
		 */
		public boolean storeComments = false;
	} // class TreeConfig



	//// Static funcs


	public static JPNode getTree(int inHandle) {
		return getTree(inHandle, null, null);
	}

	public static JPNode getTree(int inHandle, TreeConfig config) {
		return getTree(inHandle, config, null);
	}

	private static JPNode getTree(int inHandle, TreeConfig config, JPNode parent) {
		JPNode thisNode = NodeFactory.create(inHandle).configure(config);
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



	private JPNode configure(TreeConfig config) {
		if (config==null) return this;
		if (config.disconnected) {
			attrSet(JPNode.STATE2, parser.attrGetI(nodeHandle, IConstants.STATE2));
			fileIndex = parser.getNodeFileIndex(nodeHandle);
			filename = config.filenames[fileIndex];
			line = parser.getNodeLine(nodeHandle);
			column = parser.getNodeColumn(nodeHandle);
			if (config.storeComments) setComments(getComments());
			// And, the final step in making this node "disconnected"...
			nodeHandle = 0;
		} else if (config.storePosition) {  // storePosition is meaningless if disconnected
			fileIndex = parser.getNodeFileIndex(nodeHandle);
			line = parser.getNodeLine(nodeHandle);
			column = parser.getNodeColumn(nodeHandle);
		}
		if (config.callback != null) config.callback.run(this);
		return this;
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
	 * Note: This is very different than Prolint's "NextNaturalNode" in lintsuper.p.
	 * @see TokenTypes#isNatural(int)
	 */
	public JPNode firstNaturalChild() {
		if (TokenTypes.isNatural(getType())) return this;
		for (JPNode n = firstChild(); n!=null; n = n.firstChild()) {
			if (TokenTypes.isNatural(n.getType())) return n;
		}
		return null;
	}



	public int getColumn() {
		if (column == -1) return parser.getNodeColumn(nodeHandle);
		return column;
	}
	
	
	
	/** Get the comments that precede this node.
	 * Gets the comments from Proparse if "connected", otherwise gets
	 * the comments stored within this node object.
	 * CAUTION: We want to know if line breaks exist between comments and nodes,
	 * and if they exist between consecutive comments. To preserve that information,
	 * the String returned here may have "\n" in front of the first comment,
	 * may have "\n" separating comments, and may have "\n" appended to the
	 * last comment. We do not preserve the number of newlines, nor do we
	 * preserve any other whitespace.
	 * @return null if no comments.
	 */
	public String getComments() {
		if (nodeHandle==0) return (String) getLink(COMMENTS);
		StringBuffer buff = new StringBuffer();
		boolean newline = false;
		for (	int isAvail = parser.hiddenGetFirst(nodeHandle)
			;	isAvail>0
			;	isAvail=parser.hiddenGetNext()
			) {
			if (	parser.hiddenGetType().equals("WS")
				&&	parser.hiddenGetText().indexOf('\n') > -1
				)
				newline = true;
			if (parser.hiddenGetType().equals("COMMENT")) {
				if (newline) buff.append("\n");
				buff.append(parser.hiddenGetText());
			}
		}
		if (buff.length()==0) return null;
		// Trailine newline(s)?
		if (newline) buff.append("\n");
		return buff.toString();
	}



	/** Get the FieldContainer (Frame or Browse) for a statement head node or an INPUT node in a Field_ref.
	 * This value is set by TreeParser01.
	 * Only statements with the [WITH FRAME | WITH BROWSE] option have this value set.
	 * Is also available on the INPUT node for #(Field_ref INPUT ...).
	 */
	public FieldContainer getFieldContainer() { return (FieldContainer) getLink(FIELD_CONTAINER); }



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


	/** If this AST was constructed from another, then get the original. */
	public JPNode getOriginal() {
		if (attrMap==null) return null;
		return (JPNode)attrMap.get(ORIGINAL);
	}


	/** Return int[3] of nodes file/line/col. */
	public int[] getPos() {
		return new int[] {getFileIndex(), getLine(), getColumn()};
	}



	public int getState2() {
		return attrGet(JPNode.STATE2);
	}
	
	
	/** Every JPNode subtype has its own index. Used for persistent storage. */
	public int getSubtypeIndex() { return 1; }
	
	
	/** Certain nodes will have a link to a Symbol, set by TreeParser01. */
	public Symbol getSymbol() { return (Symbol) getLink(SYMBOL); }



	private void initMap() {
		if (attrMap==null) attrMap = new HashMap();
	}
	
	
	
	/** Is this a natural node (from real source text)?
	 * If not, then it is a synthetic node, added just for tree structure.
	 * @see TokenTypes#isNatural(int)
	 */
	public boolean isNatural() { return TokenTypes.isNatural(type); }
	
	
	
	/** Does this node have the Proparse STATEHEAD attribute? */
	public boolean isStateHead() {
		return attrGet(IConstants.STATEHEAD) == IConstants.TRUE;
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


	/** First child if there is one, otherwise next sibling. */
	public JPNode nextNode() {
		if (firstChild()!=null) return firstChild();
		return nextSibling();
	}
	
	
	public JPNode nextSibling() { return (JPNode) getNextSibling(); }


	public JPNode parent() { return parent; }

	
	/** Previous sibling if there is one, otherwise parent. */
	public JPNode prevNode() {
		if (parent==null) return null;
		JPNode n = parent().firstChild();
		if (n==null || n==this) return parent;
		while (n!=null) {
			if (n.nextSibling()==this) return n;
			n = n.nextSibling();
		}
		throw new AssertionError("JPNode.prevNode() failed - corrupt tree?");
	}
	
	
	
	/** Get an array of all descendant nodes (including this node) of a given type.
	 * Same idea as Proparse's "query" functions.
	 */
	public ArrayList<JPNode> query(int findType) {
		ArrayList<JPNode> list = new ArrayList<JPNode>();
		if (this.type == findType) list.add(this);
		queryHelper(this.firstChild(), findType, list);
		return list;
	}
	private static void queryHelper(JPNode node, int type, ArrayList<JPNode> list) {
		if (node==null) return;
		if (node.type == type) list.add(node);
		queryHelper(node.firstChild(), type, list);
		queryHelper(node.nextSibling(), type, list);
	}



	/** Set the comments preceding this node.
	 * CAUTION: Does not change any values in Proparse. Only use this
	 * if the JPNode tree is "disconnected", because getComments returns
	 * the comments from the "hidden tokens" in Proparse in "connected" mode.
	 */
	public void setComments(String comments) { setLink(COMMENTS, comments); }

	

	/** @see #getFieldContainer() */
	public void setFieldContainer(FieldContainer fieldContainer) { setLink(FIELD_CONTAINER, fieldContainer); }

	
	
	/** @see #getLink(Integer) */
	public void setLink(Integer key, Object value) {
		if (attrMap==null) initMap(); 
		attrMap.put(key, value);
	}



	public void setParentInChildren() {
		for (JPNode child = firstChild(); child!=null; child = child.nextSibling()) {
			child.parent = this;
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
