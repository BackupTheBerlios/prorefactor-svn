/* 
 * Authors: John Green
 *
 * Copyright (C) 2005 Prolint.org Contributors
 *
 * This file is part of Prolint.
 *
 * Prolint is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * Prolint is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Prolint; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
 

package org.prolint.eclipse;

import java.io.File;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class Plugin extends AbstractUIPlugin {

	//The shared instance.
	private static Plugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;

	// What's the proper way for getting this? -John
	private static String ID = "org.prolint";
	
	public static String PROLINT_MARKER_ID = "org.prolint.prolintmarker";
	public static String PROLINT_MARKER_COLUMN = "org.prolint.prolintmarker.column";
	public static String PROLINT_MARKER_RULEID = "org.prolint.prolintmarker.ruleid";

	
	/**
	 * The constructor.
	 */
	public Plugin() {
		super();
		plugin = this;
		try {
			resourceBundle = ResourceBundle.getBundle("org.prolint.eclipse.PluginResources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		// Truncate the hibernate log file at startup
		File logfile = new File("prolint/hibernate.log");
		logfile.getParentFile().mkdirs();
		logfile.delete();
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
	}

	
	/** Convenient dialog for displaying an error message.
	 */
	public static void errorDialog(String errMess) {
		MessageDialog.openError(
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell()
			, "Prolint"
			, errMess
			);
	}

	/**
	 * Returns the shared instance.
	 */
	public static Plugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = Plugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}


	/** Shortcut to get the active workbench page */
	public static IWorkbenchPage getActivePage() {
		return getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
	}
	

	/** Convenience method for writing an Exception to the Error Log */
	public static void log(Exception e) {
		plugin.getLog().log(
			new Status(
				IStatus.ERROR
				, Plugin.ID
				, IStatus.ERROR
				, ( e.getMessage()==null ? "" : e.getMessage() )
				, e 
				)
			);
	}


}
