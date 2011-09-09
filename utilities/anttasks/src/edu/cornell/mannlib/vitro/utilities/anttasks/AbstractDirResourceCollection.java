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
public abstract class AbstractDirResourceCollection extends DataType implements
		ResourceCollection {
	protected List<FileResource> files = null;
	protected File primaryDir;

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

	protected abstract void fillFilesList();

	/**
	 * The directory path must be provided, and unless the optional flag is set,
	 * must point to an existing, readable directory.
	 */
	protected void confirmValidDirectory(File dir, String label,
			boolean optional) {
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

	protected void includeAllFiles(File primary) {
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
	protected FileResource buildResource(File file) {
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
