/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.utilities.anttasks;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.resources.FileResource;

/**
 * TODO
 */
public class DirDifferenceFileSet extends AbstractWrappedFileSet {
	private Path blockingPath;

    public Path createBlockingPath() {
        if (blockingPath == null) {
        	blockingPath = new Path(getProject());
        }
        return blockingPath.createPath();
    }

	@Override
	protected void fillFileList() {
		if (files != null) {
			return;
		}

		FileSet fs = getInternalFileSet();

		@SuppressWarnings("unchecked")
		Iterator<FileResource> iter = fs.iterator();

		files = new ArrayList<FileResource>();
		while (iter.hasNext()) {
			FileResource fr = iter.next();
			if (!isBlocked(fr)) {
				files.add(fr);
			}
		}
	}

	/**
	 * Check to see whether this same file exists in any of the blocking
	 * directories.
	 */
	private boolean isBlocked(FileResource fr) {
		for (String blockingDir : blockingPath.list()) {
			File f = new File(blockingDir + File.separator + fr.getName());
			if (f.exists()) {
				return true;
			}
		}
		return false;
	}
}
