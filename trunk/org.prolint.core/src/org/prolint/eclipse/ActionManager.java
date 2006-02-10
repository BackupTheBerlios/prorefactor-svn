/*
 * Created on Jan 21, 2005
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

import org.eclipse.ui.IWorkbenchWindow;
import org.prorefactor.eclipse.actions.IProRefactorAction;


/** This subclass of ProRefactor's ActionManager is intended to serve
 * as an interface to ProRefactor's class, to protect Prolint from
 * unwanted changes, to allow Prolint to change how some of the functions
 * work, etc.
 * ProRefactor's ActionManager class is responsible for finding all compile
 * units in the selected resources, setting up the "busy" state, launching
 * the caller's processFile() method for each compile unit, showing  a
 * progress meter, writing status and errors to the console, etc.
 * Currently, only *.p and *.w files are considered to be "compile units".
 * This may need to change, of course.
 */
public class ActionManager extends
		org.prorefactor.eclipse.actions.ActionManager {

	public ActionManager(IWorkbenchWindow window, IProRefactorAction action) {
		super(window, action);
	}

}
