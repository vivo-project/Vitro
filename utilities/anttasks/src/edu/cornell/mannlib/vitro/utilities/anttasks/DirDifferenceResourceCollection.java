/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.utilities.anttasks;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileResource;

/**
 * Include all files that are in the primary directory, but do not have matching
 * files in the blocking directory.
 */
public class DirDifferenceResourceCollection extends DataType implements
		ResourceCollection {
	private List<FileResource> files = null;
	private File primaryDir;
	private File blockingDir;
	private boolean blockingOptional;

	@Override
	public boolean isFilesystemOnly() {
		return true;
	}

	/**
	 * Insure that the list has been filled and return an iterator to the list.
	 */
	@Override
	public Iterator<? extends Resource> iterator() {
		fillFilesList();
		return files.iterator();
	}

	/**
	 * Insure that the list has been filled and return the size of the list.
	 */
	@Override
	public int size() {
		fillFilesList();
		return files.size();
	}

	public void setPrimary(File primaryDir) {
		this.primaryDir = primaryDir;
	}

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
	private void fillFilesList() {
		if (files != null) {
			return;
		}

		confirmValidDirectory(primaryDir, "Primary", false);
		confirmValidDirectory(blockingDir, "Blocking", blockingOptional);

		files = new ArrayList<FileResource>();
		includeUnblockedFiles(primaryDir, blockingDir);

	}

	/**
	 * The primary and blocking directory paths must be provided, and must point
	 * to existing, readable directories.
	 */
	private void confirmValidDirectory(File dir, String label, boolean optional) {
		if (dir == null) {
			throw new BuildException(label + " directory not specified.");
		}
		if (!dir.exists()) {
			if (optional) {
				return;
			} else {
				throw new BuildException(label + " directory '" + dir.getPath()
						+ "' does not exist.");
			}
		}
		if (!dir.isDirectory()) {
			throw new BuildException(label + " directory '" + dir.getPath()
					+ "' is not a directory.");
		}
		if (!dir.canRead()) {
			throw new BuildException(label + " directory '" + dir.getPath()
					+ "' is not readable.");
		}
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

	private void includeAllFiles(File primary) {
		for (File file : primary.listFiles(new NonDirectoryFilter())) {
			files.add(buildResource(file));
		}
		for (File primarySubDir : primary.listFiles(new DirectoryFilter())) {
			includeAllFiles(primarySubDir);
		}
	}

	/**
	 * All file resources are based on the original primary directory.
	 */
	private FileResource buildResource(File file) {
		String primaryBasePath = primaryDir.getAbsolutePath();
		String filePath = file.getAbsolutePath();
		if (!filePath.startsWith(primaryBasePath)) {
			throw new IllegalStateException("File is not a descendant "
					+ "of the primary directory: file='" + file
					+ "', primary='" + primaryDir + "'");
		}

		String pathPart = filePath.substring(primaryBasePath.length());
		if (pathPart.startsWith(File.separator)) {
			pathPart = pathPart.substring(1);
		}

		// System.out.println("Resource: b='" + primaryDir + "', name='" +
		// pathPart + "'");
		return new FileResource(primaryDir, pathPart);
	}

	public class DirectoryFilter implements FileFilter {
		@Override
		public boolean accept(File file) {
			return file.isDirectory();
		}
	}

	public class NonDirectoryFilter implements FileFilter {
		@Override
		public boolean accept(File file) {
			return !file.isDirectory();
		}
	}
}
