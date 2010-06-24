package edu.cornell.mannlib.vitro.webapp.visualization.valueobjects;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.visualization.constants.VOConstants;
import edu.cornell.mannlib.vitro.webapp.visualization.utils.UniqueIDGenerator;
import edu.cornell.mannlib.vitro.webapp.visualization.utils.VOUtils;

/**
 * 
 * This is the Value Object for storing node information mainly for co-author vis.
 * @author cdtank
 *
 */
public class Node extends Individual {

	private int nodeID;
	private Map<String, Integer> yearToPublicationCount;
	private Set<BiboDocument> authorDocuments = new HashSet<BiboDocument>();

	public Node(String nodeURL,
				UniqueIDGenerator uniqueIDGenerator) {
		super(nodeURL);
		nodeID = uniqueIDGenerator.getNextNumericID();
	}

	public int getNodeID() {
		return nodeID;
	}
	
	public String getNodeURL() {
		return this.getIndividualURL();
	}

	public String getNodeName() {
		return this.getIndividualLabel();
	}
	
	public void setNodeName(String nodeName) {
		this.setIndividualLabel(nodeName);
	}
	
	public Set<BiboDocument> getAuthorDocuments() {
		return authorDocuments;
	}
	
	public int getNumOfAuthoredWorks() {
		return authorDocuments.size();
	}

	public void addAuthorDocument(BiboDocument authorDocument) {
		this.authorDocuments.add(authorDocument);
	}
	
	
	/*
	 * getEarliest, Latest & Unknown Publication YearCount should only be used after 
	 * the parsing of the entire sparql is done. Else it will give results based on
	 * incomplete dataset.
	 * */
	@SuppressWarnings("serial")
	public Map<String, Integer> getEarliestPublicationYearCount() {
		if (yearToPublicationCount == null) {
			yearToPublicationCount = VOUtils.getYearToPublicationCount(authorDocuments);
		}
		
		final String earliestYear = Collections.min(yearToPublicationCount.keySet());
		final Integer earliestYearPubCount = yearToPublicationCount.get(earliestYear);
		
		/*
		 * If there is no minimum year available then we should imply so by returning a "null".
		 * */
		if (!earliestYear.equalsIgnoreCase(VOConstants.DEFAULT_PUBLICATION_YEAR)) {
			return new HashMap<String, Integer>(){{
						put(earliestYear, earliestYearPubCount);
					}};
		} else {
			return null;
		}
	}

	@SuppressWarnings("serial")
	public Map<String, Integer> getLatestPublicationYearCount() {
		if (yearToPublicationCount == null) {
			yearToPublicationCount = VOUtils.getYearToPublicationCount(authorDocuments);
		}
		
		final String latestYear = Collections.max(yearToPublicationCount.keySet());
		final Integer latestYearPubCount = yearToPublicationCount.get(latestYear);
		
		/*
		 * If there is no maximum year available then we should imply so by returning a "null".
		 * */
		if (!latestYear.equalsIgnoreCase(VOConstants.DEFAULT_PUBLICATION_YEAR)) {
			return new HashMap<String, Integer>(){{
						put(latestYear, latestYearPubCount);
					}};
		} else {
			return null;
		}
	}
	
	@SuppressWarnings("serial")
	public Map<String, Integer> getUnknownPublicationYearCount() {
		if (yearToPublicationCount == null) {
			yearToPublicationCount = VOUtils.getYearToPublicationCount(authorDocuments);
		}
		
		final Integer unknownYearPubCount = yearToPublicationCount.get(VOConstants.DEFAULT_PUBLICATION_YEAR);
		
		/*
		 * If there is no unknown year available then we should imply so by returning a "null".
		 * */
		if (unknownYearPubCount != null) {
			return new HashMap<String, Integer>(){{
						put(VOConstants.DEFAULT_PUBLICATION_YEAR, unknownYearPubCount);
					}};
		} else {
			return null;
		}
	}
	

}
