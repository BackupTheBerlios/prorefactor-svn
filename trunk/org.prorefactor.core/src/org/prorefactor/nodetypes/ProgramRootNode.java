/** 8-May-2006 by John Green
 * Copyright (C) 2006 Joanju Software. All rights reserved.
 */
package org.prorefactor.nodetypes;

import com.joanju.ProparseLdr;


public class ProgramRootNode extends BlockNode {

	public ProgramRootNode() {
		super();
	}
	public ProgramRootNode(int handle, TreeConfig config) {
		super(handle, config);
		loadFilenameArray(handle);
	}
	public ProgramRootNode(int file, int line, int column) {
		super(file, line, column);
	}

	private static final long serialVersionUID = 7160983003100786995L;
	private String [] filenames = new String[0];


	
	@Override
	protected String filenameFromIndex(int index) {
		if (index >= filenames.length) return null;
		return filenames[index];
	}

	
	/** Get the array of file names. The file at index zero is always the compile unit.
	 * The others are include files. The array index position corresponds to JPNode.getFileIndex().
	 */
	public String[] getFilenames() {return filenames;}
	
	
	/** Every JPNode subtype has its own index. Used for persistent storage. */
	@Override
	public int getSubtypeIndex() { return 6; }


	private void loadFilenameArray(int handle) {
		String namesList = ProparseLdr.getInstance().attrGetS(handle, "filename-list");
		if (namesList!=null) filenames = namesList.split("\n");
	}
	
	
	/** This should only be called by PUB. */
	public void setFilenames(String [] namesArray) {
		this.filenames = namesArray;
	}


}
