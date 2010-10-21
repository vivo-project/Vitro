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
			System.err.println("path=" + path);

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
			System.err.println(e); // TODO
			return null;
		}
	}

	private String figurePath() throws Exception {
		if (infoResponse == null) {
			throw new Exception("infoResponse is null.");
		}

		String url = getUrlFromResponse();
		String root = getRootFromResponse();
		System.err.println("url=" + url); // TODO
		System.err.println("root=" + root); // TODO

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
			throw new Exception("no match with '" + pattern + "'.");
		}

		String value = matcher.group(groupIndex);
		if ((value == null) || (value.isEmpty())) {
			throw new Exception("match with '" + pattern + "' is empty.");
		}

		return value;
	}

	private boolean isTrunkPath() {
		return path.equals(TRUNK_PREFIX);
	}

	private boolean isTagPath() {
		return path.startsWith(TAGS_PREFIX);
	}

	private String getTagName() {
		return path.substring(TAGS_PREFIX.length());
	}

	private boolean isBranchPath() {
		return path.startsWith(BRANCHES_PREFIX);
	}

	private String getBranchName() {
		return path.substring(BRANCHES_PREFIX.length());
	}

}
