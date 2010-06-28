package edu.cornell.mannlib.vitro.webapp.visualization.visutils;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import edu.cornell.mannlib.vitro.webapp.visualization.valueobjects.BiboDocument;

public class VOUtils {
	
	public static Map<String, Integer> getYearToPublicationCount(
			Set<BiboDocument> authorDocuments) {

		//List<Integer> publishedYears = new ArrayList<Integer>();

    	/*
    	 * Create a map from the year to number of publications. Use the BiboDocument's
    	 * parsedPublicationYear to populate the data.
    	 * */
    	Map<String, Integer> yearToPublicationCount = new TreeMap<String, Integer>();

    	for (BiboDocument curr : authorDocuments) {

    		/*
    		 * Increment the count because there is an entry already available for
    		 * that particular year.
    		 * */
    		String publicationYear;
    		if (curr.getPublicationYear() != null 
    				&& curr.getPublicationYear().length() != 0 
    				&& curr.getPublicationYear().trim().length() != 0) {
    			publicationYear = curr.getPublicationYear();
    		} else {
    			publicationYear = curr.getParsedPublicationYear();
    		}
    		
			if (yearToPublicationCount.containsKey(publicationYear)) {
    			yearToPublicationCount.put(publicationYear,
    									   yearToPublicationCount
    									   		.get(publicationYear) + 1);

    		} else {
    			yearToPublicationCount.put(publicationYear, 1);
    		}

//    		if (!parsedPublicationYear.equalsIgnoreCase(BiboDocument.DEFAULT_PUBLICATION_YEAR)) {
//    			publishedYears.add(Integer.parseInt(parsedPublicationYear));
//    		}

    	}

		return yearToPublicationCount;
	}

}
