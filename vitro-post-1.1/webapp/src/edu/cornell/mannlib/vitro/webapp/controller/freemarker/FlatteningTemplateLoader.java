/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import freemarker.cache.TemplateLoader;

/**
 * <p>
 * A {@link TemplateLoader} that treats a directory and its sub-directories as a
 * flat namespace.
 * </p>
 * <p>
 * When a request is made to find a template source, the loader will search its
 * base directory and any sub-directories for a file with a matching name. So a
 * request for <code>myFile.ftl</code> might return a reference to a file at
 * <code>base/myFile.ftl</code> or at <code>base/this/myFile.ftl</code>
 * </p>
 * <p>
 * The order in which the sub-directories are searched is unspecified. The first
 * matching file will be returned.
 * </p>
 * <p>
 * A path (absolute or relative) on the source name would be meaningless, so any
 * such path will be stripped before the search is made. That is, a request for
 * <code>path/file.ftl</code> or <code>/absolute/path/file.ftl</code>is
 * functionally identical to a request for <code>file.ftl</code>
 * </p>
 * <p>
 * </p>
 */
public class FlatteningTemplateLoader implements TemplateLoader {
	private static final Log log = LogFactory
			.getLog(FlatteningTemplateLoader.class);

	private final File baseDir;

	public FlatteningTemplateLoader(File baseDir) {
		if (baseDir == null) {
			throw new NullPointerException("baseDir may not be null.");
		}
		if (!baseDir.exists()) {
			throw new IllegalArgumentException("Template directory '"
					+ baseDir.getAbsolutePath() + "' does not exist");
		}
		if (!baseDir.isDirectory()) {
			throw new IllegalArgumentException("Template directory '"
					+ baseDir.getAbsolutePath() + "' is not a directory");
		}
		if (!baseDir.canRead()) {
			throw new IllegalArgumentException("Can't read template "
					+ "directory '" + baseDir.getAbsolutePath() + "'");
		}

		log.debug("Created template loader - baseDir is '"
				+ baseDir.getAbsolutePath() + "'");
		this.baseDir = baseDir;
	}

	/**
	 * Look for a file by this name in the base directory, or its
	 * subdirectories, disregarding any path information.
	 * 
	 * @return a {@link File} that can be used in subsequent calls the template
	 *         loader methods, or <code>null</code> if no template is found.
	 */
	@Override
	public Object findTemplateSource(String name) throws IOException {
		if (name == null) {
			return null;
		}

		int lastSlashHere = name.indexOf('/');
		String trimmedName = (lastSlashHere == -1) ? name : name
				.substring(lastSlashHere + 1);

		// start the recursive search.
		File source = findFile(trimmedName, baseDir);
		if (source == null) {
			log.debug("For template name '" + name
					+ "', found no template file.");
		} else {
			log.debug("For template name '" + name + "', template file is "
					+ source.getAbsolutePath());
		}
		return source;
	}

	/**
	 * Recursively search for a file of this name.
	 */
	private File findFile(String name, File dir) {
		for (File child : dir.listFiles()) {
			if (child.isDirectory()) {
				File file = findFile(name, child);
				if (file != null) {
					return file;
				}
			} else {
				if (child.getName().equals(name)) {
					return child;
				}
			}
		}
		return null;
	}

	/**
	 * Ask the file when it was last modified.
	 * 
	 * @param templateSource
	 *            a {@link File} that was obtained earlier from
	 *            {@link #findTemplateSource(String)}.
	 */
	@Override
	public long getLastModified(Object templateSource) {
		if (!(templateSource instanceof File)) {
			throw new IllegalArgumentException("templateSource is not a File: "
					+ templateSource);
		}

		return ((File) templateSource).lastModified();
	}

	/**
	 * Get a {@link Reader} on this {@link File}. The framework will see that
	 * the {@link Reader} is closed when it has been read.
	 * 
	 * @param templateSource
	 *            a {@link File} that was obtained earlier from
	 *            {@link #findTemplateSource(String)}.
	 */
	@Override
	public Reader getReader(Object templateSource, String encoding)
			throws IOException {
		if (!(templateSource instanceof File)) {
			throw new IllegalArgumentException("templateSource is not a File: "
					+ templateSource);
		}

		return new FileReader(((File) templateSource));
	}

	/**
	 * Nothing to do here. No resources to free up.
	 * 
	 * @param templateSource
	 *            a {@link File} that was obtained earlier from
	 *            {@link #findTemplateSource(String)}.
	 */
	@Override
	public void closeTemplateSource(Object templateSource) throws IOException {
	}

}
