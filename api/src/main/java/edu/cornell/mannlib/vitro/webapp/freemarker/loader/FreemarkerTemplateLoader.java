/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.freemarker.loader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import freemarker.cache.TemplateLoader;

/**
 * Loads Freemarker templates from a given directory.
 * 
 * Different from a file loader in two ways:
 * 
 * 1) Flattens the directory. When it searches for a template, it will look in
 * the base directory and in any sub-directories. While doing this, it ignores
 * any path that is attached to the template name.
 * 
 * So if you were to ask for 'admin/silly.ftl', it would search for 'silly.ftl'
 * in the base directory, and in any sub-directories, until it finds one.
 * 
 * 2) Accepts approximate matches on locales. When asked for a template, it will
 * accepts an approximate match that matches the basename and extension, and
 * language or region if specifed. So a search for a template with no language
 * or region will prefer an exact match, but will accept one with language or
 * both language and region.
 * 
 * <pre>
 * "this_es_MX.ftl" matches "this_es_MX.ftl"
 * "this_es.ftl"    matches "this_es.ftl" or "this_es_MX.ftl"
 * "this.ftl"       matches "this.ftl" or "this_es.ftl" or "this_es_MX.ftl"
 * </pre>
 * 
 * This allows Freemarker to mimic the behavior of the language filtering RDF
 * service, because if Freemarker does not find a match for "this_es_MX.ftl", it
 * will try again with "this_es.ftl" and "this.ftl". So the net effect is that a
 * search for "silly_es_MX.ftl" would eventually return any of these, in order
 * of preference:
 * 
 * <pre>
 * silly_es_MX.ftl
 * silly_es.ftl
 * silly_es_*.ftl
 * silly.ftl
 * silly_*.ftl
 * </pre>
 * 
 * If more than one template file qualifies, we choose by best fit, shortest
 * path, and alphabetical order, to insure that identical requests produce
 * identical results.
 */
public class FreemarkerTemplateLoader implements TemplateLoader {
	private static final Log log = LogFactory
			.getLog(FreemarkerTemplateLoader.class);

	private final File baseDir;

	public FreemarkerTemplateLoader(File baseDir) {
		if (baseDir == null) {
			throw new NullPointerException("baseDir may not be null.");
		}

		String path = baseDir.getAbsolutePath();
		if (!baseDir.exists()) {
			throw new IllegalArgumentException("Template directory '" + path
					+ "' does not exist");
		}
		if (!baseDir.isDirectory()) {
			throw new IllegalArgumentException("Template directory '" + path
					+ "' is not a directory");
		}
		if (!baseDir.canRead()) {
			throw new IllegalArgumentException(
					"Can't read template directory '" + path + "'");
		}

		log.debug("Created template loader - baseDir is '" + path + "'");
		this.baseDir = baseDir;
	}

	/**
	 * Get the best template for this name. Walk the tree finding all possible
	 * matches, then choose our favorite.
	 */
	@Override
	public Object findTemplateSource(String name) throws IOException {
		if (StringUtils.isBlank(name)) {
			return null;
		}

		SortedSet<PathPieces> matches = findAllMatches(new PathPieces(name));

		if (matches.isEmpty()) {
			return null;
		} else {
			return matches.last().path.toFile();
		}
	}

	private SortedSet<PathPieces> findAllMatches(PathPieces searchTerm) {
		PathPiecesFileVisitor visitor = new PathPiecesFileVisitor(searchTerm);
		try {
			Files.walkFileTree(baseDir.toPath(), visitor);
		} catch (IOException e) {
			log.error(e);
		}
		return visitor.getMatches();
	}

	/**
	 * Ask the file when it was last modified.
	 * 
	 * @param templateSource
	 *            a File that was obtained earlier from findTemplateSource().
	 */
	@Override
	public long getLastModified(Object templateSource) {
		return asFile(templateSource).lastModified();
	}

	/**
	 * Get a Reader on this File. The framework will close the Reader after
	 * reading it.
	 * 
	 * @param templateSource
	 *            a File that was obtained earlier from findTemplateSource().
	 */
	@Override
	public Reader getReader(Object templateSource, String encoding)
			throws IOException {
		return new FileReader(asFile(templateSource));
	}

	/**
	 * Nothing to do here. No resources to free up.
	 * 
	 * @param templateSource
	 *            a File that was obtained earlier from findTemplateSource().
	 */
	@Override
	public void closeTemplateSource(Object templateSource) throws IOException {
		// Nothing to do.
	}

	/**
	 * That templateSource is a File, right?
	 */
	private File asFile(Object templateSource) {
		if (templateSource instanceof File) {
			return (File) templateSource;
		} else {
			throw new IllegalArgumentException("templateSource is not a File: "
					+ templateSource);
		}
	}

	// ----------------------------------------------------------------------
	// Helper classes
	// ----------------------------------------------------------------------

	/**
	 * Break a path into handy segments, so we can see whether they match the
	 * search term, and how well they match.
	 */
	static class PathPieces {
		static final Pattern PATTERN = Pattern.compile("(.+?)" // base name
				+ "(_[a-z]{2})?" // optional language
				+ "(_[A-Z]{2})?" // optional country
				+ "(\\.\\w+)?" // optional extension
		);

		final Path path;
		final String base;
		final String language;
		final String region;
		final String extension;

		public PathPieces(String pathString) {
			this(Paths.get(pathString));
		}

		public PathPieces(Path path) {
			this.path = path;

			String filename = path.getFileName().toString();

			Matcher m = PATTERN.matcher(filename);
			if (m.matches()) {
				base = getGroup(m, 1);
				language = getGroup(m, 2);
				region = getGroup(m, 3);
				extension = getGroup(m, 4);
			} else {
				base = filename;
				language = "";
				region = "";
				extension = "";
			}
		}

		private String getGroup(Matcher m, int i) {
			return (m.start(i) == -1) ? "" : m.group(i);
		}

		/**
		 * If I'm searching for this, is that an acceptable match?
		 * 
		 * Note that this is asymetrical -- a search term without a region will
		 * match a candidate with a region, but not vice versa. Same with
		 * language.
		 */
		public boolean matches(PathPieces that) {
			return base.equals(that.base) && extension.equals(that.extension)
					&& (language.isEmpty() || language.equals(that.language))
					&& (region.isEmpty() || region.equals(that.region));
		}

		/**
		 * How good a match is that to this?
		 */
		public int score(PathPieces that) {
			if (matches(that)) {
				if (that.language.equals(language)) {
					if (that.region.equals(region)) {
						return 3; // exact match.
					} else {
						return 2; // same language, approximate region.
					}
				} else {
					return 1; // approximate language.
				}
			} else {
				return -1; // doesn't match.
			}
		}

		@Override
		public String toString() {
			return "PathPieces[" + base + ", " + language + ", " + region
					+ ", " + extension + "]";
		}

	}

	/**
	 * While walking the file tree, collect all files that match the search
	 * term, as a sorted set of PathPieces.
	 */
	static class PathPiecesFileVisitor extends SimpleFileVisitor<Path> {
		private final PathPieces searchTerm;
		private final SortedSet<PathPieces> matches;

		public PathPiecesFileVisitor(PathPieces searchTerm) {
			this.searchTerm = searchTerm;
			this.matches = new TreeSet<>(new PathPiecesComparator(searchTerm));
		}

		@Override
		public FileVisitResult visitFile(Path path, BasicFileAttributes attrs)
				throws IOException {
			if (fileQualifies(path)) {
				PathPieces found = new PathPieces(path);
				if (searchTerm.matches(found)) {
					matches.add(found);
				}
			}
			return FileVisitResult.CONTINUE;
		}

		public boolean fileQualifies(Path path) {
			return !Files.isDirectory(path);
		}

		public SortedSet<PathPieces> getMatches() {
			return matches;
		}
	}

	/**
	 * Produce an ordering of paths by desirability. Best match, then shortest
	 * directory path, and finally alphabetical order.
	 */
	static class PathPiecesComparator implements Comparator<PathPieces> {
		private final PathPieces searchFor;

		public PathPiecesComparator(PathPieces searchFor) {
			this.searchFor = searchFor;
		}

		@Override
		public int compare(PathPieces p1, PathPieces p2) {
			int scoring = searchFor.score(p1) - searchFor.score(p2);
			if (scoring != 0) {
				return scoring; // prefer matches to region and language
			}

			int pathLength = p1.path.getNameCount() - p2.path.getNameCount();
			if (pathLength != 0) {
				return -pathLength; // shorter is better
			}

			return -p1.path.compareTo(p2.path); // early in alphabet is better
		}

	}
}
