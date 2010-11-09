/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.utilities.revisioninfo;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import edu.cornell.mannlib.vitro.utilities.revisioninfo.ProcessRunner.ProcessException;

/**
 * Get release and revision information to display on screen.
 * 
 * Ask Subversion for the information. If Subversion is available, and if this
 * is a working directory, then we can build the info from the responses we get
 * from "svn info" and "svnversion".
 * 
 * If that doesn't work, read the information from the ".revisionInfo" file in
 * the product directory. Presumably, that file was created when the source was
 * exported from Subversion.
 * 
 * If that doesn't work either, return something like this:
 * "productName ~ unknown ~ unknown"
 */
public class RevisionInfoBuilder {
	private static final String SVN_DIRECTORY_NAME = ".svn";
	private static final String[] SVNVERSION_COMMAND = { "svnversion" };
	private static final String[] SVN_INFO_COMMAND = { "svn", "info" };
	private static final String INFO_LINE_DELIMITER = " ~ ";
	private static final String REVISION_INFO_FILENAME = ".revisionInfo";

	private final String productName;
	private final File productDirectory;

	public RevisionInfoBuilder(String[] args) {
		if (args.length != 2) {
			throw new IllegalArgumentException(
					"RevisionInfoBuilder requires 2 arguments, not "
							+ args.length);
		}

		productName = args[0];
		productDirectory = new File(args[1]);

		if (!productDirectory.isDirectory()) {
			throw new IllegalArgumentException("Directory '"
					+ productDirectory.getAbsolutePath() + "' does not exist.");
		}
	}

	private String buildInfo() {
		String infoLine;
		infoLine = buildInfoFromSubversion();
		if (infoLine == null) {
			infoLine = buildInfoFromFile();
		}
		if (infoLine == null) {
			infoLine = buildDummyInfo();
		}
		return infoLine;
	}

	private String buildInfoFromSubversion() {
		if (!isThisASubversionWorkspace()) {
			return null;
		}

		String release = assembleReleaseNameFromSubversion();
		String revision = obtainRevisionLevelFromSubversion();
		System.err.println("release=" + release); // TODO
		System.err.println("revision=" + revision); // TODO
		return buildLine(release, revision);
	}

	private boolean isThisASubversionWorkspace() {
		File svnDirectory = new File(productDirectory, SVN_DIRECTORY_NAME);
		return svnDirectory.isDirectory();
	}

	private String assembleReleaseNameFromSubversion() {
		String infoResponse = runSubProcess(SVN_INFO_COMMAND);
		return new InfoResponseParser(infoResponse).parse();
	}

	private String obtainRevisionLevelFromSubversion() {
		String response = runSubProcess(SVNVERSION_COMMAND);
		return (response == null) ? null : response.trim();
	}

	private String runSubProcess(String[] cmdArray) {
		List<String> command = Arrays.asList(cmdArray);
		try {
			ProcessRunner runner = new ProcessRunner();
			runner.setWorkingDirectory(productDirectory);

			runner.run(command);

			int rc = runner.getReturnCode();
			if (rc != 0) {
				throw new ProcessRunner.ProcessException("Return code from "
						+ command + " was " + rc);
			}

			String output = runner.getStdOut();
			// System.err.println(command + " response was '" + output + "'");
			return output;
		} catch (ProcessException e) {
			return null;
		}
	}

	private String buildInfoFromFile() {
		try {
			File revisionInfoFile = new File(productDirectory,
					REVISION_INFO_FILENAME);
			BufferedReader reader = new BufferedReader(new FileReader(
					revisionInfoFile));

			String release = reader.readLine();
			if (release == null) {
				throw new EOFException("No release line in file.");
			}

			String revision = reader.readLine();
			if (revision == null) {
				throw new EOFException("No revision line in file.");
			}

			return buildLine(release, revision);
		} catch (IOException e) {
			return null;
		}
	}

	private String buildDummyInfo() {
		return buildLine(null, null);
	}

	private String buildLine(String release, String revision) {
		if (release == null) {
			release = "unknown";
		}
		if (revision == null) {
			revision = "unknown";
		}
		return productName + INFO_LINE_DELIMITER + release.trim()
				+ INFO_LINE_DELIMITER + revision.trim();
	}

	public static void main(String[] args) {
		try {
			System.out.println(new RevisionInfoBuilder(args).buildInfo());
		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.err.println("usage: RevisionInfoBuilder [product_name] "
					+ "[product_directory] [supplied_values]");
			System.exit(1);
		}
	}

}
