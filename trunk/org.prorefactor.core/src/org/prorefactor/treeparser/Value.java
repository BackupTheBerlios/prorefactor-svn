/**
 * Value.java 
 * @author Peter Dalbadie
 * 21-Sep-2004
 * 
 */


package org.prorefactor.treeparser;

/**
 * Represents objects that have a value.
 *
 */
public interface Value {

	public void setValue(Object fileName);

	public Object getValue();

}
