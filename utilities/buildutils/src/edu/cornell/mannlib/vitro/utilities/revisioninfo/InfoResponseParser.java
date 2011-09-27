/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.utilities.revisioninfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse the response that we got from SVN info.
 * 
 * Not thread-safe.
 */
public class InfoResponseParser {
	private static final Pattern URL_PATTERN = Pattern.compile("URL: (\\S+)");
	private static final Pattern ROOT_PATTERN = Pattern
			.compile("Repository Root: (\\S+)");

	private static final String TRUNK_PREFIX = "/trunk";
	private static final String TAGS_PREFIX = "/tags/";
	private static final String BRANCHES_PREFIX = "/branches/";

	private final String infoResponse;
	private String path;

	public InfoResponseParser(String infoResponse) {
		this.infoResponse = infoResponse;
	}

	public String parse() {
		try {
			path = figurePath();

			if (isTrunkPath()) {
				return "trunk";
			} else if (isTagPath()) {
				return "tag " + getTagName();
			} else if (isBranchPath()) {
				return "branch " + getBranchName();
			} else {
				return null;
			}
		} catch (Exception e) {
			System.err.println(e);
			return null;
		}
	}

	private String figurePath() throws Exception {
		if (infoResponse == null) {
			throw new Exception("infoResponse is null.");
		}

		String url = getUrlFromResponse();
		String root = getRootFromResponse();

		if (!url.startsWith(root)) {
			throw new Exception("url doesn't start with root.");
		}

		return url.substring(root.length());
	}

	private String getUrlFromResponse() throws Exception {
		return findNonEmptyMatch(URL_PATTERN, 1);
	}

	private String getRootFromResponse() throws Exception {
		return findNonEmptyMatch(ROOT_PATTERN, 1);
	}

	private String findNonEmptyMatch(Pattern pattern, int groupIndex)
			throws Exception {
		Matcher matcher = pattern.matcher(infoResponse);
		if (!matcher.find()) {
			throw new Exception("no match with '" + pattern + "'. Is your Subversion client out of date?");
		}

		String value = matcher.group(groupIndex);
		if ((value == null) || (value.isEmpty())) {
			throw new Exception("match with '" + pattern + "' is empty.");
		}

		return value;
	}

	private boolean isTrunkPath() {
		return path.startsWith(TRUNK_PREFIX);
	}

	private boolean isTagPath() {
		return path.startsWith(TAGS_PREFIX);
	}

	private String getTagName() {
		return getFirstLevel(discardPrefix(path, TAGS_PREFIX));
	}

	private boolean isBranchPath() {
		return path.startsWith(BRANCHES_PREFIX);
	}

	private String getBranchName() {
		return getFirstLevel(discardPrefix(path, BRANCHES_PREFIX));
	}

	private String discardPrefix(String string, String prefix) {
		if (string.length() < prefix.length()) {
			return "";
		} else {
			return string.substring(prefix.length());
		}
	}

	private String getFirstLevel(String string) {
		int slashHere = string.indexOf('/');
		if (slashHere == -1) {
			return string;
		} else {
			return string.substring(0, slashHere);
		}
	}

}
