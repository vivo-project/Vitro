/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.utilities.anttasks;

import java.util.ArrayList;

import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileResource;

/**
 * If the primary directory exists, include all files that are descendent from
 * it. Otherwise, don't complain but act as a collection of zero files.
 */
public class OptionalDirResourceCollection extends
		AbstractDirResourceCollection implements ResourceCollection {

	/**
	 * If the list hasn't already been filled, fill it with all files in the
	 * primary directory that are not blocked by files with the same path under
	 * the blocking directory.
	 */
	@Override
	protected void fillFilesList() {
		if (files != null) {
			return;
		}
		files = new ArrayList<FileResource>();

		confirmValidDirectory(primaryDir, "The", true);
		
		if (primaryDir.exists()) {
			includeAllFiles(primaryDir);
		}
	}
}
