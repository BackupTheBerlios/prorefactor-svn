/* Created Mar 24, 2005
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/** General purpose utilities for working with Streams. */
public class StreamUtil {

	/** Line by line comparison of two input streams.
	 * Returns zero if the two streams are the same, or else the line number of the
	 * first difference found. First line is line 1. If different line terminators
	 * (ex: unix vs. DOS) are present, that difference will not be reported.
	 * Number of lines in the two streams must be equal, otherwise last line counted
	 * is reported.
	 */
	public static int lineOfDiff(InputStream stream1, InputStream stream2) throws IOException {
		BufferedReader r1 = new BufferedReader(new InputStreamReader(stream1));
		BufferedReader r2 = new BufferedReader(new InputStreamReader(stream2));
		int line = 1;
		String string1;
		while ( (string1 = r1.readLine()) != null) {
			if (! string1.equals(r2.readLine())) return line;
			line++;
		}
		// Check that stream2 does not have more lines than stream1
		if (r2.readLine()!=null) return line;
		return 0;
	}

}
