/**
 * RunHandle.java
 * @author Peter Dalbadie
 * 21-Sep-2004
 * 
 */

package org.prorefactor.treeparser;

/**
 * Represents a procedure handle value, used in a run
 * statement of the form: run <proc> in <handle>.
 *
 */
public class RunHandle implements Value {

	private String fileName;

	/**
	 * @param string
	 */
	public void setValue(Object fileName) {
		this.fileName = (String) fileName;
	}

	/**
	 * Get the name of the external procedure
	 * associated with the runHandle
	 * @return
	 */
	public Object getValue() {
		return fileName;
	}

}
