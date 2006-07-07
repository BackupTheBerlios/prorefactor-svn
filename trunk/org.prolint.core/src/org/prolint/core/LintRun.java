/* Created on Jan 25, 2005
 * Authors: John Green
 *
 * Copyright (C) 2005 Prolint.org Contributors
 * This file is part of Prolint.
 *    Prolint is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *    Prolint is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *    You should have received a copy of the GNU Lesser General Public
 * License along with Prolint; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.prolint.core;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import net.sf.hibernate.HibernateException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.prolint.eclipse.ClearMarkersAction;
import org.prolint.eclipse.Plugin;
import org.prolint.rules.AbbrevSchemaNames;
import org.prolint.rules.CuRule;
import org.prolint.rules.NoEffect;
import org.prolint.rules.NodeRule;
import org.prolint.rules.Rule;
import org.prorefactor.core.JPNode;
import org.prorefactor.eclipse.ResourceUtil;
import org.prorefactor.refactor.RefactorException;
import org.prorefactor.refactor.source.CompileUnit;


/** This class stores the configuration for a lint run, and it contains
 * the methods for launching lint against a given compile unit.
 * The lint run keeps track of all files visited for all compile units.
 * The first time a file is visited (think of include files), it
 * is cleared of any old Prolint markers.
 */
public class LintRun {

	public LintRun() throws HibernateException {
		buildCuRulesList();
		buildNodeRulesList();
	}
	
	private int fileIndexSequence = 1;
	private int ruleIndexSequence = 1;
	private ArrayList cuRulesList = new ArrayList();
	private HashMap<Integer, IFile> indexToResourceMap = new HashMap<Integer, IFile>();
	private HashMap nodeRulesMap = new HashMap();
	private HashMap ruleIndexMap = new HashMap();
	private HashMap<IFile, Integer> resourceToIndexMap = new HashMap<IFile, Integer>();
	private IFile [] currentUnitIFiles = null;
	private TempMarkerTable tempMarkerTable = new TempMarkerTable();


	
	private void addCuRule(CuRule rule) {
		if (rule==null) return;
		indexRule(rule);
		cuRulesList.add(rule);
	}

	/* Add a rule to the map of node types and node rules.
	 * Note that <Rule>.create() will return null if the rule
	 * is not active in the current Prolint configuration.
	 */
	private void addNodeRule(NodeRule rule) {
		if (rule==null) return;
		indexRule(rule);
		Integer [] watchedTypes = rule.getWatchedNodeTypes();
		for (int i = 0; i < watchedTypes.length; i++) {
			Integer nodeType = watchedTypes[i];
			ArrayList rulesArray = (ArrayList) nodeRulesMap.get(nodeType);
			if (rulesArray==null) {
				rulesArray = new ArrayList();
				nodeRulesMap.put(nodeType, rulesArray);
			}
			rulesArray.add(rule);
		}
	}

	/* Build the list of rules to launch against the full compile unit.
	 * Later: Pull creation of rules lists out to separate classes, make configurable.
	 */
	private void buildCuRulesList() {
		// example: addCuRule(new ...);
	}
	
	/* Build the list of rules to launch against specific node types.
	 * Later: Pull creation of rules lists out to separate classes, make configurable.
	 * See Notes[1] at the bottom of this file for NodeRules implementation notes.
	 */
	private void buildNodeRulesList() {
		addNodeRule(new AbbrevSchemaNames());
		addNodeRule(new NoEffect());
		// add more rules here ...
	}

	/* Run all CU rules for a given CU. */
	private void checkCuRules(CompileUnit cu) throws HibernateException {
		for (Iterator it = cuRulesList.iterator(); it.hasNext();) {
			CuRule irule = (CuRule) it.next();
			tempMarkerTable.storeMarkers(irule.run(cu));
		}
	}

	/* Check an individual node's type for lint rules mapped to it. */
	private void checkNodeRules(JPNode node) throws HibernateException {
		ArrayList rulesList = (ArrayList) nodeRulesMap.get(new Integer(node.getType()));
		if (rulesList==null) return;
		for (Iterator it = rulesList.iterator(); it.hasNext();) {
			NodeRule rule = (NodeRule) it.next();
			tempMarkerTable.storeMarkers(rule.run(node));
		}
	}
	
	/* Build an index of all source files for the compile unit, and clear old Prolint problem
	 * markers from those.
	 * Returns a positional array of IFile objects, so that the IFile for a Proparse
	 * file index can be retrieved quickly.
	 */
	private void clearAndIndexFiles(CompileUnit cu) {
		ArrayList<IFile> resourceList = new ArrayList<IFile>();
		String [] filenameArray = cu.getFileIndex();
		for (int i = 0; i < filenameArray.length; i++) {
			String name = filenameArray[i];
			IFile ifile = null;
			if (name.length() > 0) ifile = ResourceUtil.getIFileRelaxed(name);
			if (ifile==null) continue;
			resourceList.add(ifile);
			if (resourceToIndexMap.containsKey(ifile)) continue;
			ClearMarkersAction.clear(ifile);
			Integer index = new Integer(fileIndexSequence++);
			resourceToIndexMap.put(ifile, index);
			indexToResourceMap.put(index, ifile);
		}
		currentUnitIFiles = new IFile[resourceList.size()];
		resourceList.toArray(currentUnitIFiles);
	}
	
	/** Get the LintRun file index for the compile unit file index.
	 * IMPORTANT: Check the return value for -1.
	 * @return -1 if the IFile isn't available.
	 */
	public int getLintRunFileIndex(int cuFileIndex) {
		IFile ifile = currentUnitIFiles[cuFileIndex];
		if (ifile==null) return -1;
		Integer retIndex = (Integer) resourceToIndexMap.get(ifile);
		if (retIndex==null) return -1;
		return retIndex.intValue();
	}

	private void indexRule(Rule rule) {
		Integer index = new Integer(ruleIndexSequence++);
		ruleIndexMap.put(index, rule);
		rule.setLintRunIndex(index);
		rule.setLintRun(this);
	}

	/** Launch lint against a single compile unit.
	 * @param file The File object to lint. Must be a compile unit.
	 * @param relPath A string representation of the file's relative path, for display purposes.
	 * @throws LintException If any part of Prolint fails - no need to halt.
	 * @throws RefactorException If the parse or tree-parse fails - no need to halt.
	 * @throws HibernateException Should halt processing.
	 */
	public void lint(File file, String relPath)
			throws LintException, RefactorException, HibernateException {
		CompileUnit cu = new CompileUnit(file, null, CompileUnit.DEFAULT);
		cu.treeParser01();
		clearAndIndexFiles(cu);
		// We'll run the "whole CU" rules first.
		checkCuRules(cu);
		// Now walk the syntax tree, checking for "one node" rules to call.
		treeWalk(cu.getTopNode());
	}
	
	/** Write a log of this run's TempMarkers to a stream writer.
	 * @throws IOException
	 * @see #toString(TempMarker) 
	 */
	public void logTempMarkers(Writer writer) throws IOException, HibernateException {
		TempMarkerTable.QuerySession querySession = tempMarkerTable.getAll();
		for (Iterator it = querySession.iterator(); it.hasNext();) {
			writer.write(toString((TempMarker)it.next()));
			writer.write(FileStuff.LINESEP);
		}
		querySession.close();
	}

	/** Get a String representation of a TempMarker.
	 * All on one line - does not contain any line breaks.
	 * Format is: rule-name file line column message 
	 * @throws CoreException
	 */
	public String toString(TempMarker tempMarker) {
		IFile ifile = (IFile) indexToResourceMap.get(new Integer(tempMarker.getFileIndex()));
		Rule rule = (Rule) ruleIndexMap.get(new Integer(tempMarker.getRuleIndex()));
		StringBuffer buff = new StringBuffer()
			.append(rule.getName())
			.append(" ")
			.append(ifile.getProjectRelativePath())
			.append(" ")
			.append(tempMarker.getLine())
			.append(" ")
			.append(tempMarker.getColumn())
			.append(" ")
			.append(tempMarker.getMessage())
			;
		return buff.toString();
	}
	
	/* Walk the syntax tree, checking each node type for rules to run against it. */
	private void treeWalk(JPNode node) throws HibernateException {
		if (node==null) return;
		checkNodeRules(node);
		// Depth-first
		treeWalk(node.firstChild());
		treeWalk(node.nextSibling());
	}

	/** Write all of the accumulated markers. 
	 * @throws HibernateException
	 * @throws CoreException*/
	public void writeAllMarkers() throws HibernateException, CoreException, IOException {
		TempMarkerTable.QuerySession querySession = tempMarkerTable.getAll();
		for (Iterator it = querySession.iterator(); it.hasNext();) {
			TempMarker tempMarker = (TempMarker) it.next();
			IFile ifile = (IFile) indexToResourceMap.get(new Integer(tempMarker.getFileIndex()));
			/* Mayber later: If support for char offset is added to Proparse, then we could
			 * save ourselves a very expensive operation here by getting the charpos from
			 * the Proparse token.
			 */
			int charStart =
				FileStuff.calculateLineOffset(ifile.getLocation().toFile(), tempMarker.getLine())
				+ tempMarker.getColumn() - 1;
			Rule rule = (Rule) ruleIndexMap.get(new Integer(tempMarker.getRuleIndex()));
			IMarker imarker = ifile.createMarker(Plugin.PROLINT_MARKER_ID);
			imarker.setAttribute(IMarker.LINE_NUMBER, tempMarker.getLine());
			imarker.setAttribute(IMarker.CHAR_START, charStart);
			imarker.setAttribute(IMarker.CHAR_END, charStart + tempMarker.getNumchars() + 1);
			imarker.setAttribute(IMarker.MESSAGE, tempMarker.getMessage());
			imarker.setAttribute(Plugin.PROLINT_MARKER_COLUMN, tempMarker.getColumn());
			imarker.setAttribute(Plugin.PROLINT_MARKER_RULEID, rule.getName());
		}
		querySession.close();
	}

}


/*
 * Notes[1]: Since there might be many (say, 20+) rules that need to be run for specific
 * node types (rather than for the entier CU), I figured that it would be most efficient
 * to look up a node's type in a HashMap for rules to run against it. One alternative
 * would be to call twenty rules for each node, so each rule could check on the node
 * for itself. Another alternative would be to walk the entire syntax tree 20 times,
 * once for each rule.
 * --john@joanju.com
 */
