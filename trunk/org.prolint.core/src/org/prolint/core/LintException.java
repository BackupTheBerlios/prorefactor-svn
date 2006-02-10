/* Created on Jan 25, 2005
 * Authors: John Green
 *
 * Copyright (C) 2005 Prolint.org Contributors
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
package org.prolint.core;


/** Prolint exception */
public class LintException extends Exception {

	public LintException() {
		super();
	}

	public LintException(String message) {
		super(message);
	}

	public LintException(Throwable cause) {
		super(cause);
	}

	public LintException(String message, Throwable cause) {
		super(message, cause);
	}

}
