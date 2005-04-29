/* org.prorefactor.eclipse.Plugin.java
 * Copyright (C) 2003-2004 Joanju Limited
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.prorefactor.eclipse;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.prorefactor.core.ICallback;
import org.prorefactor.refactor.Rollback;


/** The Plugin class for org.prorefactor.eclipse. */
public class Plugin extends AbstractUIPlugin {

	public Plugin() {
		super();
		plugin = this;
		try {
			resourceBundle= ResourceBundle.getBundle("org.prorefactor.eclipse.ProRefactorPluginResources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
		Rollback.externPreModify = new ICallback() {
			public Object run(Object obj) {
				ResourceUtil.saveLocalHistory((String)obj);
				return null;
			}
		};
		selectionManager = new SelectionManager();
	}

	//The shared instance.
	private static Plugin plugin;
	
	// What's the proper way for getting this?
	private static String ID = "org.prorefactor.eclipse";

	private SelectionManager selectionManager;
	private ResourceBundle resourceBundle;
	


	/** General purpose message/dialog for displaying an error message.
	 */
	public static void errorDialog(String errMess) {
		MessageDialog.openError(
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell()
			, "ProRefactor"
			, errMess
			);
	}



	/** Shortcut to get the active workbench page */
	public static IWorkbenchPage getActivePage() {
		return getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
	}
	
	
	
	/** Convenience method for writing an Exception to the Error Log */
	public static void log(Throwable e) {
		plugin.getLog().log(
			new Status(
				IStatus.ERROR
				, Plugin.ID
				, IStatus.ERROR
				, ( e.getMessage() != null ? e.getMessage() : "" )
				, e 
				)
			);
	}



	/** Returns the shared instance. */
	public static Plugin getDefault() { return plugin; }



	public SelectionManager getSelectionManager() { return selectionManager; }



	/** Returns the plugin's resource bundle */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}



	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle= Plugin.getDefault().getResourceBundle();
		try {
			return bundle.getString(key);
		} catch (MissingResourceException e) {
			return key;
		}
	}



	/** Returns the workspace instance. */
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}



	/** Shortcut to get the active workbench window */
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}



} // class
