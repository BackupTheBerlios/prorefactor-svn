/**
 * ErrorList.java
 * @author Peter Dalbadie
 * 21-Sep-2004
 * 
 */

package org.prorefactor.treeparser;

import java.util.ArrayList;

/**
 * A list of SemanticError objects found during a
 * tree parse.
 *
 */
public class ErrorList {
	
	private ArrayList list = null;
	
	public ErrorList(){
		list = new ArrayList();
	}
	
	/**
	 * @param o
	 * @return
	 */
	public boolean add(SemanticError o) {
		return list.add(o);
	}

	/**
	 * 
	 */
	public void clear() {
		list.clear();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return list.equals(obj);
	}

	/**
	 * @return
	 */
	public boolean isEmpty() {
		return list.isEmpty();
	}

	/**
	 * @return
	 */
	public int size() {
		return list.size();
	}

}
