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
 * This is the Value Object for storing edge information mainly for co-author vis.
 * @author cdtank
 *
 */
public class Edge {

	private int edgeID;
	private Map<String, Integer> yearToPublicationCount;
	private Set<BiboDocument> collaboratorDocuments = new HashSet<BiboDocument>();
	private Node sourceNode;
	private Node targetNode;

	public Edge(Node sourceNode, Node  targetNode, BiboDocument seedCoAuthoredDocument, 
				UniqueIDGenerator uniqueIDGenerator) {
		edgeID = uniqueIDGenerator.getNextNumericID();
		this.sourceNode = sourceNode;
		this.targetNode = targetNode;
		this.collaboratorDocuments.add(seedCoAuthoredDocument);
	}

	public int getEdgeID() {
		return edgeID;
	}
	
	public Node getSourceNode() {
		return sourceNode;
	}

	public Node getTargetNode() {
		return targetNode;
	}
	
	public Set<BiboDocument> getCollaboratorDocuments() {
		return collaboratorDocuments;
	}
	
	public int getNumOfCoAuthoredWorks() {
		return collaboratorDocuments.size();
	}

	public void addCollaboratorDocument(BiboDocument authorDocument) {
		this.collaboratorDocuments.add(authorDocument);
	}
	
	/*
	 * getEarliest, Latest & Unknown Publication YearCount should only be used after 
	 * the parsing of the entire sparql is done. Else it will give results based on
	 * incomplete dataset.
	 * */
	@SuppressWarnings("serial")
	public Map<String, Integer> getEarliestCollaborationYearCount() {
		if (yearToPublicationCount == null) {
			yearToPublicationCount = VOUtils.getYearToPublicationCount(collaboratorDocuments);
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
	public Map<String, Integer> getLatestCollaborationYearCount() {
		if (yearToPublicationCount == null) {
			yearToPublicationCount = VOUtils.getYearToPublicationCount(collaboratorDocuments);
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
	public Map<String, Integer> getUnknownCollaborationYearCount() {
		if (yearToPublicationCount == null) {
			yearToPublicationCount = VOUtils.getYearToPublicationCount(collaboratorDocuments);
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
