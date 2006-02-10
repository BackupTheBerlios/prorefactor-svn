/*
 * Feb 18, 2005
 *
 */
package org.prolint.core;

/**
 * Temporarily persistent objects which are used to avoid redundant markers from
 * a lint run.
 * <p>
 * We keep these in a database on disk, rather than just in Java memory, because
 * of the potential for any one large lint run to generate an enormous number of
 * problem markers.
 * </p>
 * <p>
 * We don't just use Eclipse's Marker interface to avoid duplicate markers,
 * because there is no fast, indexed method for finding an existing marker for
 * one resource, line, column, and problem type.
 * <p>
 * We have to consider duplicate markers, because a problem in one include file
 * will be found when linting each of the compile units which reference it.
 * </p>
 * 
 * 
 */
public class TempMarker {

	public TempMarker() { }

	private long id;
	private int fileIndex;
	private int line;
	private int column;
	private int numchars;
	private int ruleIndex;
	private String message;

	public int getColumn() { return column; }
	public int getFileIndex() { return fileIndex; }
	public int getLine() { return line; }
	/** Starting at the marker line/column, how many characters to highlight? */
	public int getNumchars() { return numchars; }
	public int getRuleIndex() { return ruleIndex; }
	/** The record ID for database persistence. */
	public long getId() { return id; }
	public String getMessage() { return message; }

	public TempMarker setColumn(int column) { this.column = column; return this;}
	public TempMarker setFileIndex(int file) { this.fileIndex = file; return this;}
	public TempMarker setLine(int line) { this.line = line; return this;}
	/** Starting at the marker line/column, how many characters to highlight? */
	public TempMarker setNumchars(int numchars) { this.numchars = numchars; return this; }
	public TempMarker setRuleIndex(int problem) { this.ruleIndex = problem; return this;}
	/** The record ID for database persistence. */
	private TempMarker setId(long recordID) { this.id = recordID; return this;}
	public TempMarker setMessage(String message) { this.message = message; return this; }
	
	public String toString() {
		return "ID " + id + " " + fileIndex + ":" + line + ":" + column + " " + ruleIndex + " " + message;
	}
	
	public boolean equals(Object obj) {
		if (obj.getClass()!=TempMarker.class) return false;
		TempMarker other = (TempMarker) obj;
		return
			(	this.fileIndex == other.fileIndex
			&&	this.line == other.line
			&&	this.column == other.column
			&&	this.ruleIndex == other.ruleIndex
			);
	}
	
	public int hashCode() {
		return (fileIndex * 1000 + line);
	}

}