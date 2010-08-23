/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.utilities.testrunner.datamodel;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import edu.cornell.mannlib.vitro.utilities.testrunner.IgnoredTests;
import edu.cornell.mannlib.vitro.utilities.testrunner.IgnoredTests.IgnoredTestInfo;
import edu.cornell.mannlib.vitro.utilities.testrunner.LogStats;
import edu.cornell.mannlib.vitro.utilities.testrunner.Status;
import edu.cornell.mannlib.vitro.utilities.testrunner.datamodel.SuiteData.TestData;
import edu.cornell.mannlib.vitro.utilities.testrunner.output.OutputDataListener;
import edu.cornell.mannlib.vitro.utilities.testrunner.output.OutputDataListener.ProcessOutput;
import edu.cornell.mannlib.vitro.utilities.testrunner.output.SuiteResults;

/**
 * Collect all that we know about suites, tests, and their current status.
 */
public class DataModel {

	/* base data */
	private Collection<SuiteContents> suiteContents = Collections.emptyList();
	private Collection<File> selectedSuites = Collections.emptyList();
	private Collection<SuiteResults> suiteResults = Collections.emptyList();
	private OutputDataListener.Info dataListenerInfo = OutputDataListener.Info.EMPTY_INFO;
	private IgnoredTests ignoredTestList = IgnoredTests.EMPTY_LIST;
	private LogStats logStats = LogStats.EMPTY_LOG_STATS; // TODO

	/* derived data */
	private Status runStatus = Status.PENDING;

	private final SortedMap<String, SuiteData> suiteDataMap = new TreeMap<String, SuiteData>();
	private final EnumMap<Status, List<SuiteData>> suiteMapByStatus = new EnumMap<Status, List<SuiteData>>(
			Status.class);

	private final List<TestData> allTests = new ArrayList<TestData>();
	private final EnumMap<Status, List<TestData>> testMapByStatus = new EnumMap<Status, List<TestData>>(
			Status.class);

	// ----------------------------------------------------------------------
	// Constructor
	// ----------------------------------------------------------------------

	public DataModel() {
		calculate();
	}

	// ----------------------------------------------------------------------
	// Update the base data.
	// ----------------------------------------------------------------------

	public void setSuiteContents(Collection<SuiteContents> suiteContents) {
		this.suiteContents = new ArrayList<SuiteContents>(suiteContents);
		calculate();
	}

	public void setSelectedSuites(Collection<File> selectedSuites) {
		this.selectedSuites = new ArrayList<File>(selectedSuites);
		calculate();
	}

	public Collection<File> getSelectedSuites() {
		return new ArrayList<File>(selectedSuites);
	}

	public void setSuiteResults(Collection<SuiteResults> suiteResults) {
		this.suiteResults = new ArrayList<SuiteResults>(suiteResults);
		calculate();
	}

	public void captureDataListener(OutputDataListener dataListener) {
		this.dataListenerInfo = dataListener.getInfo();
		calculate();
	}

	public void setIgnoredTestList(IgnoredTests ignoredTestList) {
		this.ignoredTestList = ignoredTestList;
		calculate();
	}

	public void setLogStats(LogStats logStats) { // TODO
		this.logStats = logStats;
		calculate();
	}

	// ----------------------------------------------------------------------
	// Keep the derived data current.
	// ----------------------------------------------------------------------

	/**
	 * Data in the model has been updated. Refresh all derived data.
	 */
	private void calculate() {
		// Clear all derived data.
		runStatus = Status.OK;

		suiteDataMap.clear();
		suiteMapByStatus.clear();
		for (Status s : Status.values()) {
			suiteMapByStatus.put(s, new ArrayList<SuiteData>());
		}

		allTests.clear();
		testMapByStatus.clear();
		for (Status s : Status.values()) {
			testMapByStatus.put(s, new ArrayList<TestData>());
		}

		/*
		 * Populate the Suite map with all Suites.
		 */
		Map<String, SuiteResults> resultsMap = new HashMap<String, SuiteResults>();
		for (SuiteResults result : suiteResults) {
			resultsMap.put(result.getName(), result);
		}
		Map<String, SuiteContents> contentsMap = new HashMap<String, SuiteContents>();
		for (SuiteContents contents : suiteContents) {
			contentsMap.put(contents.getName(), contents);
		}

		for (SuiteContents contents : suiteContents) {
			String name = contents.getName();
			SuiteResults result = resultsMap.get(name);
			boolean ignored = ignoredTestList.isIgnored(name);
			ProcessOutput failureMessages = dataListenerInfo
					.getFailureMessages().get(name);
			suiteDataMap.put(name, new SuiteData(name, ignored, contents,
					result, failureMessages));
		}

		/*
		 * Map the Suites by status.
		 */
		for (SuiteData s : suiteDataMap.values()) {
			getSuites(s.getStatus()).add(s);
		}

		/**
		 * Populate the Test map with all Tests, and map by status.
		 */
		for (SuiteData s : suiteDataMap.values()) {
			for (TestData t : s.getTestMap().values()) {
				allTests.add(t);
				getTests(t.getStatus()).add(t);
			}
		}

		if (logStats.hasErrors() || !getSuites(Status.ERROR).isEmpty()) {
			runStatus = Status.ERROR;
		} else if (!getSuites(Status.PENDING).isEmpty()) {
			runStatus = Status.PENDING;
		} else {
			runStatus = Status.OK;
		}
	}

	// ----------------------------------------------------------------------
	// Access the derived data.
	// ----------------------------------------------------------------------

	public Status getRunStatus() {
		return runStatus;
	}

	public long getStartTime() {
		return dataListenerInfo.getStartTime();
	}

	public long getEndTime() {
		return dataListenerInfo.getEndTime();
	}

	public long getElapsedTime() {
		return dataListenerInfo.getElapsedTime();
	}

	public boolean isAnyPasses() {
		return !getTests(Status.OK).isEmpty();
	}

	public boolean isAnyFailures() {
		return !getTests(Status.ERROR).isEmpty();
	}

	public boolean isAnyIgnores() {
		return !getTests(Status.IGNORED).isEmpty();
	}

	public boolean isAnyPending() {
		return !getTests(Status.PENDING).isEmpty();
	}

	public int getTotalSuiteCount() {
		return suiteDataMap.size();
	}

	public int getPassingSuiteCount() {
		return getSuites(Status.OK).size();
	}

	public int getFailingSuiteCount() {
		return getSuites(Status.ERROR).size();
	}

	public int getIgnoredSuiteCount() {
		return getSuites(Status.IGNORED).size();
	}

	public int getPendingSuiteCount() {
		return getSuites(Status.PENDING).size();
	}

	public Collection<SuiteData> getAllSuites() {
		return suiteDataMap.values();
	}

	public Map<String, SuiteData> getSuitesWithFailureMessages() {
		Map<String, SuiteData> map = new TreeMap<String, SuiteData>();
		for (SuiteData s : suiteDataMap.values()) {
			if (s.getFailureMessages() != null) {
				map.put(s.getName(), s);
			}
		}
		return map;
	}

	public int getTotalTestCount() {
		return allTests.size();
	}

	public int getPassingTestCount() {
		return getTests(Status.OK).size();
	}

	public int getFailingTestCount() {
		return getTests(Status.ERROR).size();
	}

	public int getIgnoredTestCount() {
		return getTests(Status.IGNORED).size();
	}

	public int getPendingTestCount() {
		return getTests(Status.PENDING).size();
	}

	public Collection<TestData> getAllTests() {
		return Collections.unmodifiableCollection(allTests);
	}

	public Collection<TestData> getFailingTests() {
		return Collections.unmodifiableCollection(getTests(Status.ERROR));
	}

	public Collection<TestData> getIgnoredTests() {
		return Collections.unmodifiableCollection(getTests(Status.IGNORED));
	}

	public Collection<IgnoredTestInfo> getIgnoredTestInfo() {
		return ignoredTestList.getList();
	}

	public String getReasonForIgnoring(String suiteName, String testName) {
		return ignoredTestList.getReasonForIgnoring(suiteName, testName);
	}

	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

	/**
	 * Get the list of suites that have this status.
	 */
	private List<SuiteData> getSuites(Status st) {
		return suiteMapByStatus.get(st);
	}

	/**
	 * Get the list of tests that have this status.
	 */
	private List<TestData> getTests(Status st) {
		return testMapByStatus.get(st);
	}

}
