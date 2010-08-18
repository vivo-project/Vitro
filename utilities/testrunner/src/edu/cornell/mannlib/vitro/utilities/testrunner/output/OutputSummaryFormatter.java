/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.utilities.testrunner.output;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import edu.cornell.mannlib.vitro.utilities.testrunner.FileHelper;
import edu.cornell.mannlib.vitro.utilities.testrunner.LogStats;
import edu.cornell.mannlib.vitro.utilities.testrunner.SeleniumRunnerParameters;
import edu.cornell.mannlib.vitro.utilities.testrunner.Status;
import edu.cornell.mannlib.vitro.utilities.testrunner.output.SuiteResults.TestResults;

/**
 * Creates the summary HTML file.
 */
public class OutputSummaryFormatter {
	public static final String SUMMARY_HTML_FILENAME = "summary.html";
	public static final String SUMMARY_CSS_FILENAME = "summary.css";
	private final SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	private final SeleniumRunnerParameters parms;

	private LogStats log;
	private Map<String, SuiteResults> suites;
	private OutputDataModel dataModel;
	private Status runStatus;
	private List<TestResults> allTests = new ArrayList<TestResults>();
	private int passingTestCount;
	private List<TestResults> failingTests = new ArrayList<TestResults>();
	private List<TestResults> ignoredTests = new ArrayList<TestResults>();
	private List<String> passingSuites = new ArrayList<String>();
	private List<String> failingSuites = new ArrayList<String>();
	private List<String> ignoredSuites = new ArrayList<String>();
	private List<String> remainingSuites = new ArrayList<String>();

	public OutputSummaryFormatter(SeleniumRunnerParameters parms) {
		this.parms = parms;
	}

	/**
	 * Create a summary HTML file from the info contained in this log file and
	 * these suite outputs.
	 */
	public void format(LogStats log, Map<String, SuiteResults> suites,
			OutputDataModel dataModel) {
		this.log = log;
		this.suites = suites;
		this.dataModel = dataModel;
		this.runStatus = figureOverallStatus(log, suites);
		tallyTests();
		tallySuites();

		PrintWriter writer = null;
		try {
			copyCssFile();

			File outputFile = new File(parms.getOutputDirectory(),
					SUMMARY_HTML_FILENAME);
			writer = new PrintWriter(outputFile);

			writeHeader(writer);
			writeStatsSection(writer);
			writeErrorMessagesSection(writer);
			writeFailureSection(writer);
			writeIgnoreSection(writer);
			writeSuitesSection(writer);
			writeAllTestsSection(writer);
			writeFooter(writer);
		} catch (IOException e) {
			// There is no appeal for any problems here. Just report them.
			e.printStackTrace();
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	/**
	 * Copy the CSS file into the output directory.
	 */
	private void copyCssFile() {
		InputStream cssStream = this.getClass().getResourceAsStream(
				SUMMARY_CSS_FILENAME);
		if (cssStream == null) {
			System.out.println("Couldn't find the CSS file: '"
					+ SUMMARY_CSS_FILENAME + "'");
		} else {
			File cssTarget = new File(parms.getOutputDirectory(),
					SUMMARY_CSS_FILENAME);
			try {
				FileHelper.copy(cssStream, cssTarget);
				cssStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * The overall status for the run is the worst status of any component.
	 */
	public Status figureOverallStatus(LogStats log,
			Map<String, SuiteResults> suites) {
		if (log.hasErrors()) {
			return Status.ERROR;
		}
		boolean hasWarnings = log.hasWarnings();

		for (SuiteResults s : suites.values()) {
			if (s.getStatus() == Status.ERROR) {
				return Status.ERROR;
			} else if (s.getStatus() == Status.WARN) {
				hasWarnings = true;
			}
		}

		if (hasWarnings) {
			return Status.WARN;
		} else {
			return Status.OK;
		}
	}

	private void tallyTests() {
		for (SuiteResults s : suites.values()) {
			for (TestResults t : s.getTests()) {
				this.allTests.add(t);
				if (t.getStatus() == Status.OK) {
					this.passingTestCount++;
				} else if (t.getStatus() == Status.WARN) {
					this.ignoredTests.add(t);
				} else {
					this.failingTests.add(t);
				}
			}
		}
	}

	private void tallySuites() {
		List<String> ignoredSuiteNames = dataModel.getIgnoredSuiteNames();

		for (String name : dataModel.getSuiteNames()) {
			if (ignoredSuiteNames.contains(name)) {
				this.ignoredSuites.add(name);
			} else if (!suites.containsKey(name)) {
				this.remainingSuites.add(name);
			} else {
				SuiteResults s = suites.get(name);
				if (s.getStatus() == Status.ERROR) {
					this.failingSuites.add(name);
				} else {
					this.passingSuites.add(name);
				}
			}
		}
	}

	private void writeHeader(PrintWriter writer) {
		String startString = formatDateTime(dataModel.getStartTime());

		writer.println("<html>");
		writer.println("<head>");
		writer.println("  <title>Summary of Acceptance Tests " + startString
				+ "</title>");
		writer.println("  <link rel=\"stylesheet\" type=\"text/css\" "
				+ "href=\"summary.css\">");
		writer.println("</head>");
		writer.println("<body>");
		writer.println();
		writer.println("  <div class=\"heading\">");
		writer.println("    Acceptance test results: " + startString);
		writer.println("    <div class=\"" + this.runStatus.getHtmlClass()
				+ " one-word\">" + this.runStatus + "</div>");
		writer.println("  </div>");
	}

	private void writeStatsSection(PrintWriter writer) {
		String passClass = Status.OK.getHtmlClass();
		String failClass = this.failingTests.isEmpty() ? "" : Status.ERROR
				.getHtmlClass();
		String ignoreClass = this.ignoredTests.isEmpty() ? "" : Status.WARN
				.getHtmlClass();

		String start = formatDateTime(dataModel.getStartTime());
		String end = formatDateTime(dataModel.getEndTime());
		String elapsed = formatElapsedTime(dataModel.getElapsedTime());

		writer.println("  <div class=\"section\">Summary</div>");
		writer.println();
		writer.println("  <table class=\"summary\" cellspacing=\"0\">");
		writer.println("    <tr>");
		writer.println("      <td>");
		writer.println("        <table cellspacing=\"0\">");
		writer.println("  	      <tr><td>Start time:</td><td>" + start
				+ "</td></tr>");
		writer.println("  	      <tr><td>End time:</td><td>" + end
				+ "</td></tr>");
		writer.println("  	      <tr><td>Elapsed time</td><td>" + elapsed
				+ "</td></tr>");
		writer.println("  	    </table>");
		writer.println("      </td>");
		writer.println("      <td>");
		writer.println("        <table cellspacing=\"0\">");
		writer.println("          <tr><th>&nbsp;</th><th>Suites</th><th>Tests</th>");
		writer.println("          <tr><th>Total</th><td>" + "</td><td>"
				+ "</td>");
		writer.println("          <tr class=\"" + passClass
				+ "\"><th>Passed</th><td>" + this.passingSuites.size()
				+ "</td><td>" + this.passingTestCount + "</td>");
		writer.println("          <tr class=\"" + failClass
				+ "\"><th>Failed</th><td>" + this.failingSuites.size()
				+ "</td><td>" + this.failingTests.size() + "</td>");
		writer.println("          <tr class=\"" + ignoreClass
				+ "\"><th>Ignored</th><td>" + this.ignoredSuites.size()
				+ "</td><td>" + this.ignoredTests.size() + "</td>");
		if (!this.remainingSuites.isEmpty()) {
			writer.println("          <tr><th>Remaining</th><td>"
					+ this.remainingSuites.size() + "</td><td>?</td>");
		}
		writer.println("  	    </table>");
		writer.println("      </td>");
		writer.println("    </tr>");
		writer.println("  </table>");
		writer.println();
	}

	private void writeErrorMessagesSection(PrintWriter writer) {
		String errorClass = Status.ERROR.getHtmlClass();
		String warnClass = Status.WARN.getHtmlClass();

		writer.println("  <div class=section>Errors and warnings</div>");
		writer.println();
		writer.println("  <table cellspacing=\"0\">");

		if ((!log.hasErrors()) && (!log.hasWarnings())) {
			writer.println("      <tr><td colspan=\"2\">No errors or warnings</td></tr>");
		} else {
			for (String e : log.getErrors()) {
				writer.println("      <tr class=\"" + errorClass
						+ "\"><td>ERROR</td><td>" + e + "</td></tr>");
			}
			for (String w : log.getWarnings()) {
				writer.println("      <tr class=\"" + warnClass
						+ "\"><td>ERROR</td><td>" + w + "</td></tr>");
			}
		}
		writer.println("    </table>");
		writer.println();
	}

	private void writeFailureSection(PrintWriter writer) {
		String errorClass = Status.ERROR.getHtmlClass();

		writer.println("  <div class=section>Failing tests</div>");
		writer.println();
		writer.println("  <table cellspacing=\"0\">");
		writer.println("    <tr><th>Suite name</th><th>Test name</th></tr>\n");
		if (failingTests.isEmpty()) {
			writer.println("    <tr><td colspan=\"2\">No tests failed.</td>"
					+ "</tr>");
		} else {
			for (TestResults t : failingTests) {
				writer.println("    <tr class=\"" + errorClass + "\">");
				writer.println("      <td>" + t.getSuiteName() + "</td>");
				writer.println("      <td><a href=\"" + t.getOutputLink()
						+ "\">" + t.getTestName() + "</a></td>");
				writer.println("    </tr>");
			}
		}
		writer.println("  </table>");
		writer.println();
	}

	private void writeIgnoreSection(PrintWriter writer) {
		String warnClass = Status.WARN.getHtmlClass();

		writer.println("  <div class=section>Ignored tests</div>");
		writer.println();
		writer.println("  <table cellspacing=\"0\">");
		writer.println("    <tr><th>Suite name</th><th>Test name</th>"
				+ "<th>Reason for ignoring</th></tr>\n");
		if (ignoredTests.isEmpty()) {
			writer.println("    <tr><td colspan=\"3\">No tests ignored.</td>"
					+ "</tr>");
		} else {
			for (TestResults t : ignoredTests) {
				writer.println("    <tr class=\"" + warnClass + "\">");
				writer.println("      <td>" + t.getSuiteName() + "</td>");
				writer.println("      <td><a href=\"" + t.getOutputLink()
						+ "\">" + t.getTestName() + "</a></td>");
				writer.println("      <td>" + t.getReasonForIgnoring()
						+ "</td>");
				writer.println("    </tr>");
			}
		}
		writer.println("  </table>");
		writer.println();
	}

	private void writeSuitesSection(PrintWriter writer) {
		writer.println("  <div class=section>Suites</div>");
		writer.println();
		writer.println("  <table cellspacing=\"0\">");

		for (SuiteResults s : suites.values()) {
			writer.println("    <tr class=\"" + s.getStatus().getHtmlClass()
					+ "\">");
			writer.println("      <td><a href=\"" + s.getOutputLink() + "\">"
					+ s.getName() + "</a></td>");
			writer.println("    </tr>");
		}

		writer.println("  </table>");
		writer.println();
	}

	private void writeAllTestsSection(PrintWriter writer) {
		writer.println("  <div class=section>All tests</div>");
		writer.println();
		writer.println("  <table cellspacing=\"0\">");

		writer.println("    <tr><th>Suite name</th><th>Test name</th></tr>\n");
		for (TestResults t : allTests) {
			writer.println("    <tr class=\"" + t.getStatus().getHtmlClass()
					+ "\">");
			writer.println("      <td>" + t.getSuiteName() + "</td>");
			writer.println("      <td><a href=\"" + t.getOutputLink() + "\">"
					+ t.getTestName() + "</a></td>");
			writer.println("    </tr>");
		}

		writer.println("  </table>");
		writer.println();
	}

	private void writeFooter(PrintWriter writer) {
		writer.println("  <div class=section>Log</div>");
		writer.println("  <pre>");

		Reader reader = null;
		try {
			reader = new FileReader(parms.getLogFile());
			char[] buffer = new char[4096];
			int howMany;
			while (-1 != (howMany = reader.read(buffer))) {
				writer.write(buffer, 0, howMany);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		writer.println("  </pre>");
		writer.println("</body>");
		writer.println("</html>");
	}

	private String formatElapsedTime(long elapsed) {
		if (elapsed == 0) {
			return "---";
		}

		long elapsedSeconds = elapsed / 1000L;
		long seconds = elapsedSeconds % 60L;
		long elapsedMinutes = elapsedSeconds / 60L;
		long minutes = elapsedMinutes % 60L;
		long hours = elapsedMinutes / 60L;

		String elapsedTime = "";
		if (hours > 0) {
			elapsedTime += hours + "h ";
		}
		if (minutes > 0 || hours > 0) {
			elapsedTime += minutes + "m ";
		}
		elapsedTime += seconds + "s";

		return elapsedTime;
	}

	private String formatDateTime(long dateTime) {
		if (dateTime == 0) {
			return "---";
		}

		return dateFormat.format(new Date(dateTime));
	}

}
