/**
 * SemanticRecord.java
 * @author Peter Dalbadie
 * 21-Sep-2004
 * 
 */

package org.prorefactor.treeparser;

import org.prorefactor.core.JPNode;

/**
 * Represents a record used in semantic processing.
 * It is a base class for more specific semantic records, which can
 * be definitions that appear in the SymbolTable, references
 * to previously defined items or other things of semantic
 * significance.
 *
 */
public class SemanticRecord {

	protected JPNode node;
	
	public SemanticRecord(){
		node = null;
	}
	
	public SemanticRecord(JPNode node){
		this.node = node;
	}


	/**
	 * @return
	 */
	public int getColumn() {
		return node.getColumn();
	}

	/**
	 * @return
	 */
	public String getFilename() {
		return node.getFilename();
	}

	/**
	 * @return
	 */
	public int getLine() {
		return node.getLine();
	}

}
