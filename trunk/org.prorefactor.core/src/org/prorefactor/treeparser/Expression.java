/**
 * Expression.java
 * @author Peter Dalbadie
 * 21-Sep-2004
 * 
 */

package org.prorefactor.treeparser;

import org.prorefactor.core.JPNode;


/**
 * @author pcd
 */
public class Expression extends SemanticRecord {

	private Object value;

	/**
	 * @param node
	 */
	public Expression(JPNode node) {
		super(node);
	}
	
	/**
	 * @param value
	 */
	public void setValue(Object value) {
		this.value = value;
	}

	/**
	 * @return Returns the value.
	 */
	public Object getValue() {
		return value;
	}
}
