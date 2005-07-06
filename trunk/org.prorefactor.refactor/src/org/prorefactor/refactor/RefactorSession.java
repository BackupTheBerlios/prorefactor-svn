/* RefactorSession.java
 * Created on Sep 30, 2003
 * John Green
 *
 * Copyright (C) 2003 Joanju Limited
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.prorefactor.refactor;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;

import org.prorefactor.core.schema.Schema;
import org.prorefactor.refactor.settings.ApplicationSettings;
import org.prorefactor.refactor.settings.ProgressProjectSettings;
import org.prorefactor.refactor.settings.ProparseProjectSettings;

import com.joanju.ProparseLdr;



/**
 * This "Singleton" class provides an interface to an org.prorefactor.refactor session.
 */
public class RefactorSession {

	private long timeStamp;
	private ApplicationSettings appSettings = null;
	private String projectName = null;
	private ProgressProjectSettings progressSettings = null;
	private ProparseLdr parser = null;
	private ProparseProjectSettings proparseSettings = null;
	private Schema schema = null;

	// Singleton
	public static RefactorSession getInstance() {
		if (theInstance == null)
			theInstance = new RefactorSession();
		return theInstance;
	}
	private static RefactorSession theInstance;
	private RefactorSession() {
		appSettings = new ApplicationSettings(getAppSettingsFilename());
		try {
			appSettings.loadSettings();
		} catch (Throwable e) {}
		parser = ProparseLdr.getInstance();
		schema = Schema.getInstance();
		(new File(LISTING_FILE)).getParentFile().mkdirs();
		rollbackDir.mkdirs();
		tempDir.mkdirs();
	}

	public static final String LISTING_FILE = "prorefactor/temp/listingfile.txt";
	public static final String MESSAGES_FILE = "prorefactor/refactor.messages";
	public static final String ROLLBACK_DIR = "prorefactor/rollback";

	private File rollbackDir = new File(ROLLBACK_DIR);
	public File tempDir = new File("prorefactor/temp");


	private void configureProparse() throws Exception {
		parser.configSet("init","false");
		schema.clear();
		try {
			schema.loadSchema(proparseSettings.schemaFile);
		} catch (FileNotFoundException e) {}

		parser.configSet("batch-mode", progressSettings.batchmode ? "true" : "false");
		parser.configSet("keyword-all", proparseSettings.keywordall);
		parser.configSet("opsys", progressSettings.opsys);
		parser.configSet("propath", progressSettings.propath);
		parser.configSet("proversion", progressSettings.proversion);
		parser.configSet("window-system", progressSettings.windowSystem);

		parser.schemaAliasDelete(""); // deletes all
		String [] alias = progressSettings.dbAliases.split(",");
		for (int i = 0; i < alias.length - 1; i = i + 2) {
			parser.schemaAliasCreate(alias[i], alias[i+1]);
		}

		parser.configSet("init","true");
	}



	public void disableParserListing() {
		parser.configSet("listing-file", "");
	}



	public void enableParserListing() {
		parser.configSet("listing-file", LISTING_FILE);
	}



	public ApplicationSettings getAppSettings() {
		return appSettings;
	}

	public static String getAppSettingsFilename() {
		return "prorefactor/settings/application.properties";
	}

	/** Get a string for the indent for the current project.
	 * Returns a tab or some number of spaces (ex: "   ").
	 */
	public String getIndentString() {
		String indentString;
		if (getProparseSettings().indentTab) indentString = "\t";
		else {
			int spaces = getProparseSettings().indentSpaces;
			char [] ca = new char[spaces];
			Arrays.fill(ca, ' ');
			indentString = new String(ca);
		}
		return indentString;
	}

	public ProgressProjectSettings getProgressSettings() {
		return progressSettings;
	}

	public static String getProgressSettingsFilename(String projectName) {
		return "prorefactor/projects/" + projectName + "/progress.properties";
	}

	/** Returns the name of the currently loaded project */
	public String getProjectName() {
		return projectName;
	}

	/** Returns the Settings for the currently loaded project */
	public ProparseProjectSettings getProparseSettings() {
		return proparseSettings;
	}

	public static String getProparseSettingsFilename(String projectName) {
		return "prorefactor/projects/" + projectName + "/proparse.properties";
	}

	public String getProRefactorProjectDir() {
		return "prorefactor/projects/" + projectName;
	}

	public File getRollbackDir() { return rollbackDir; }



	/** Make sure that Proparse's configuration gets reloaded */
	public static void invalidateCurrentSettings() {
		if (theInstance!=null) theInstance.projectName = null;
	}



	/** Only loads the project's settings if it's not already the current project */
	public void loadProject(String projectName) throws Exception {
		if (projectName==null || projectName.length()==0)
			throw new Exception("No project selected");
		if (	this.projectName!=null
			&&	this.projectName.equals(projectName)
			&&	schemaFileIsCurrent()
			) return;
		try {appSettings.loadSettings();} catch (FileNotFoundException e) {}
		progressSettings = new ProgressProjectSettings(getProgressSettingsFilename(projectName));
		try {progressSettings.loadSettings();} catch (FileNotFoundException e) {}
		proparseSettings = 
			new ProparseProjectSettings(
				getProparseSettingsFilename(projectName)
				, projectName
				);
		try {proparseSettings.loadSettings();} catch (FileNotFoundException e) {}
		configureProparse();
		this.projectName = projectName;
		timeStamp = System.currentTimeMillis();
	}


	/** Loads the project, whether it's already loaded or not */
	public void loadProjectForced(String projectName) throws Exception {
		invalidateCurrentSettings();
		loadProject(projectName);
	}


	/** Check that the schema file has not been modified since load. */
	private boolean schemaFileIsCurrent() {
		assert (proparseSettings != null);
		File schemaFile = new File(proparseSettings.schemaFile);
		if (! schemaFile.exists()) return true; // scratch projects might have no schema
		return (schemaFile.lastModified() < timeStamp);
	}


} // class
