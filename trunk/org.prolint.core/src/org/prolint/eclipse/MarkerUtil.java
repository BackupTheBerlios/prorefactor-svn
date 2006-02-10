/* Created Mar 23, 2005
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
package org.prolint.eclipse;

import java.io.IOException;
import java.io.Writer;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.prolint.core.FileStuff;

/** Utilities for working with "Prolint Problem" Eclipse markers. */ 
public class MarkerUtil {
	
	/** Write a log of Prolint markers to a stream writer.
	 * @throws CoreException
	 * @throws IOException
	 * @see #toString(IMarker) 
	 */
	public static void writeMarkers(IResource resource, Writer writer)
			throws IOException, CoreException {
		IMarker [] markers = resource.findMarkers(Plugin.PROLINT_MARKER_ID, true, IResource.DEPTH_INFINITE);
		for (int i = 0; i < markers.length; i++) {
			writer.write(toString(markers[i]));
			writer.write(FileStuff.LINESEP);
		}
	}

	/** Get a String representation of a Prolint marker.
	 * All on one line - does not contain any line breaks.
	 * Format is: rule-name file line column message 
	 * @throws CoreException
	 */
	public static String toString(IMarker marker) throws CoreException {
		StringBuffer buff = new StringBuffer()
			.append(marker.getAttribute(Plugin.PROLINT_MARKER_RULEID))
			.append(" ")
			.append(marker.getResource().getProjectRelativePath())
			.append(" ")
			.append(marker.getAttribute(IMarker.LINE_NUMBER))
			.append(" ")
			.append(marker.getAttribute(Plugin.PROLINT_MARKER_COLUMN))
			.append(" ")
			.append(marker.getAttribute(IMarker.MESSAGE))
			;
		return buff.toString();
	}
	
}
