/* RefactorResult.java
 * Created on Oct 8, 2003
 * John Green
 *
 * Copyright (C) 2003-2004 Joanju Limited
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.prorefactor.eclipse.actions;

/**
 */
public class RefactorResult {

	/** Message to be displayed by the ActionManager in the Console */
	public String message = null;

	/** Is the ActionManager to break out of the file loop in order
	 * to process interactive refactoring?
	 */
	public boolean breakout = false;

	/** Is there an Exception associated with this refactor result?
	 * The presense of an exception causes the ActionManager to stop
	 * processing files.
	 */
	public Exception exception = null;

}
