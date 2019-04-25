/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels;

import java.io.File;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * Provides a mechanism for Freemarker templates (main or included, parent or
 * child) to add to the lists of scripts and style sheets for the current page.
 *
 * Each page uses 3 instances of Tags, exposed as ${scripts}, ${headScripts} and
 * ${stylesheets}. A template may add a complete &lt;script/$gt; element (for
 * scripts or headScripts) or a &lt;link&gt; tag (for stylesheets), and these
 * elements will appear at the proper location in the rendered HTML for the
 * page.
 *
 * VIVO-1405: This process is augmented by the TagVersionInfo inner class, which
 * attempts to add a "version=" query string to the URL in the supplied element.
 * The version number is derived from the last-modified date of the specified
 * script or stylesheet on the server. The effect is that a user's browser cache
 * is effectively invalidated each time a new version of the script or
 * stylesheet is deployed.
 */
public class Tags extends BaseTemplateModel {
	private static final Log log = LogFactory.getLog(Tags.class);

	protected final LinkedHashSet<String> tags;

	public Tags() {
		this.tags = new LinkedHashSet<String>();
	}

	public Tags(LinkedHashSet<String> tags) {
		this.tags = tags;
	}

	public TemplateModel wrap() {
		try {
			return new TagsWrapper().wrap(this);
		} catch (TemplateModelException e) {
			log.error("Error creating Tags template model");
			return null;
		}
	}

	/**
	 * Script and stylesheet lists are wrapped with a specialized BeansWrapper
	 * that exposes certain write methods, instead of the configuration's object
	 * wrapper, which doesn't. The templates can then add stylesheets and
	 * scripts to the lists by calling their add() methods.
	 *
	 * @param Tags
	 *            tags
	 * @return TemplateModel
	 */
	static public class TagsWrapper extends BeansWrapper {

		public TagsWrapper() {
			super(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);

			// Start by exposing all safe methods.
			setExposureLevel(EXPOSE_SAFE);
		}

		@SuppressWarnings("rawtypes")
		@Override
		protected void finetuneMethodAppearance(Class cls, Method method,
				MethodAppearanceDecision decision) {

			try {
				String methodName = method.getName();
				if (!(methodName.equals("add") || methodName.equals("list"))) {
					decision.setExposeMethodAs(null);
				}
			} catch (Exception e) {
				log.error(e, e);
			}
		}
	}

	/* Template methods */

	@SuppressWarnings("hiding")
	public void add(String... tags) {
		for (String tag : tags) {
			add(tag);
		}
	}

	public void add(String tag) {
		TagVersionInfo info = new TagVersionInfo(tag);
		if (info.hasVersion()) {
			tags.add(TagVersionInfo.addVersionNumber(tag, info));
		} else {
			tags.add(tag);
		}
	}

	public String list() {
		return StringUtils.join(tags, "\n");
	}

	/**
	 * Find the value of "href" or "src".
	 *
	 * If there is such a value, and it doesn't have a query string already, and
	 * it represents a local URL, and we can locate the file that is served by
	 * the URL and get the last modified date, then we have found a "version
	 * number" that we can add to the attribute value.
	 *
	 * Reference for parsing attributes:
	 * https://www.w3.org/TR/html/syntax.html#elements-attributes
	 */
	protected static class TagVersionInfo {
		private static final Pattern PATTERN_DOUBLE_QUOTES = Pattern
				.compile("(href|src)\\s*=\\s*\"([^\"]+)\"[\\s|>]");
		private static final int GROUP_INDEX_DOUBLE_QUOTES = 2;

		private static final Pattern PATTERN_SINGLE_QUOTES = Pattern
				.compile("(href|src)\\s*=\\s*'([^']+)'[\\s|>]");
		private static final int GROUP_INDEX_SINGLE_QUOTES = 2;

		private static final Pattern PATTERN_NO_QUOTES = Pattern
				.compile("(href|src)\\s*=\\s*([^\"'<=>\\s]+)[\\s|>]");
		private static final int GROUP_INDEX_NO_QUOTES = 2;

		public static String addVersionNumber(String rawTag,
				TagVersionInfo info) {
			String versionString = (info.match.style == MatchResult.Style.NO_QUOTES)
					? "?version&eq;"
					: "?version=";
			return rawTag.substring(0, info.match.start) + info.match.group
					+ versionString + smushTimeStamp(info)
					+ rawTag.substring(info.match.end);
		}

		private static String smushTimeStamp(TagVersionInfo info) {
			int smushed = (((char) (info.timestamp >> 48))
					^ ((char) (info.timestamp >> 32))
					^ ((char) (info.timestamp >> 16))
					^ ((char) info.timestamp));
			return String.format("%04x", smushed);
		}

		private MatchResult match;
		private long timestamp = 0L;

		public TagVersionInfo(String rawTag) {
			try {
				match = findUrlValue(rawTag);

				if (match != null && !hasQueryString(match.group)) {
					String stripped = stripContextPath(match.group);

					if (stripped != null) {
						String realPath = locateRealPath(stripped);

						if (realPath != null) {
							timestamp = getLastModified(realPath);
						}
					}
				}
			} catch (Exception e) {
				log.debug("Failed to add version info to tag: " + rawTag, e);
				timestamp = 0L;
			}
		}

		public boolean hasVersion() {
			return timestamp != 0L;
		}

		private static MatchResult findUrlValue(String rawTag) {
			Matcher mDouble = PATTERN_DOUBLE_QUOTES.matcher(rawTag);
			if (mDouble.find()) {
				return new MatchResult(mDouble, GROUP_INDEX_DOUBLE_QUOTES,
						MatchResult.Style.DOUBLE_QUOTES);
			}

			Matcher mSingle = PATTERN_SINGLE_QUOTES.matcher(rawTag);
			if (mSingle.find()) {
				return new MatchResult(mSingle, GROUP_INDEX_SINGLE_QUOTES,
						MatchResult.Style.SINGLE_QUOTES);
			}

			Matcher mNo = PATTERN_NO_QUOTES.matcher(rawTag);
			if (mNo.find()) {
				return new MatchResult(mNo, GROUP_INDEX_NO_QUOTES,
						MatchResult.Style.NO_QUOTES);
			}

			log.debug(rawTag + " no match");
			return null;
		}

		private static boolean hasQueryString(String group) {
			if (group.indexOf('?') > -1) {
				log.debug(group + " has query string already");
				return true;
			} else {
				return false;
			}
		}

		private static String stripContextPath(String group) {
			String contextPath = UrlBuilder.getBaseUrl();
			if (contextPath.isEmpty() || group.startsWith(contextPath)) {
				return group.substring(contextPath.length());
			} else {
				log.debug(group + " doesn't match context path");
				return null;
			}
		}

		private static String locateRealPath(String stripped) {
			ServletContext ctx = ApplicationUtils.instance()
					.getServletContext();
			String realPath = ctx.getRealPath(stripped);
			if (realPath == null) {
				log.debug(stripped + " has no real path");
			}
			return realPath;
		}

		private static long getLastModified(String realPath) {
			return new File(realPath).lastModified();
		}

		protected static class MatchResult {
			public enum Style {
				SINGLE_QUOTES, DOUBLE_QUOTES, NO_QUOTES
			}

			public final String group;
			public final int start;
			public final int end;
			public final Style style;

			public MatchResult(Matcher matcher, int group, Style style) {
				this.group = matcher.group(group);
				this.start = matcher.start(group);
				this.end = matcher.end(group);
				this.style = style;
				log.debug(this);
			}

			@Override
			public String toString() {
				return "MatchResult[start=" + start + ", end=" + end
						+ ", group=" + group + ", style=" + style + "]";
			}
		}
	}
}
