/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.utilities.anttasks;

import java.io.File;
import java.util.ArrayList;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileResource;

/**
 * Include all files that are in the primary directory, but do not have matching
 * files in the blocking directory.
 */
public class DirDifferenceResourceCollection extends
		AbstractDirResourceCollection implements ResourceCollection {
	private File blockingDir;
	private boolean blockingOptional;

	public void setBlocking(File blockingDir) {
		this.blockingDir = blockingDir;
	}

	public void setBlockingOptional(boolean blockingOptional) {
		this.blockingOptional = blockingOptional;
	}

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

		confirmValidDirectory(primaryDir, "Primary", false);
		confirmValidDirectory(blockingDir, "Blocking", blockingOptional);

		files = new ArrayList<FileResource>();
		includeUnblockedFiles(primaryDir, blockingDir);
	}

	/**
	 * Include any file from the primary directory that does not match a file in
	 * the blocking directory.
	 * 
	 * Include all files from any subdirectory that has no matching subdirectory
	 * in the blocking directory.
	 * 
	 * Include all unblocked files from any subdirectory that has a matching
	 * subdirectory in the blocking directory.
	 * 
	 * NOTE: if a file is matched by a subdirectory, the file is blocked. If a
	 * subdirectory is matched by a file, an exception is thrown.
	 */
	private void includeUnblockedFiles(File primary, File blocking) {
		for (File file : primary.listFiles(new NonDirectoryFilter())) {
			if (!isBlocked(file, blocking)) {
				files.add(buildResource(file));
			}
		}
		for (File primarySubDir : primary.listFiles(new DirectoryFilter())) {
			File blockingSubDir = findMatchingDir(primarySubDir, blocking);
			if (blockingSubDir == null) {
				includeAllFiles(primarySubDir);
			} else {
				includeUnblockedFiles(primarySubDir, blockingSubDir);
			}
		}
	}

	private boolean isBlocked(File file, File blocking) {
		return new File(blocking, file.getName()).exists();
	}

	private File findMatchingDir(File primary, File blockingParent) {
		File dir = new File(blockingParent, primary.getName());
		if (!dir.exists()) {
			return null;
		}
		if (!dir.isDirectory()) {
			throw new BuildException("Directory '" + primary
					+ "' is blocked by a non-directory '" + dir.getPath() + "'");
		}
		return dir;
	}

}
