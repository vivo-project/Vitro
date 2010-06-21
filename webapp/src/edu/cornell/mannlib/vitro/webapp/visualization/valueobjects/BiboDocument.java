package edu.cornell.mannlib.vitro.webapp.visualization.valueobjects;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BiboDocument {

	public static final String DEFAULT_PUBLICATION_YEAR = "Unknown";
	public static final int MINIMUM_PUBLICATION_YEAR = 1800;
	private static final int CURRENT_YEAR = Calendar.getInstance().get(Calendar.YEAR);

	private String authorURL;
	private String authorLabel;
	private String documentURL;
	private String documentMoniker;
	private String documentLabel;
	private String documentBlurb;
	private String documentDescription;
	private String publicationYear;
	private String parsedPublicationYear = DEFAULT_PUBLICATION_YEAR;



	public BiboDocument(String documentURL) {
		this.documentURL = documentURL;
	}
	
	public String getAuthorURL() {
		return authorURL;
	}
	
	public void setAuthorURL(String authorURL) {
		this.authorURL = authorURL;
	}
	
	public String getAuthorLabel() {
		return authorLabel;
	}
	
	public void setAuthorLabel(String authorLabel) {
		this.authorLabel = authorLabel;
	}
	
	public String getDocumentURL() {
		return documentURL;
	}
	
	public void setDocumentURL(String documentURL) {
		this.documentURL = documentURL;
	}
	
	public String getDocumentMoniker() {
		return documentMoniker;
	}
	
	public void setDocumentMoniker(String documentMoniker) {
		this.documentMoniker = documentMoniker;
	}
	
	public String getDocumentLabel() {
		return documentLabel;
	}
	
	public void setDocumentLabel(String documentLabel) {
		this.documentLabel = documentLabel;
	}
	
	public String getDocumentBlurb() {
		return documentBlurb;
	}
	
	public void setDocumentBlurb(String documentBlurb) {
		this.documentBlurb = documentBlurb;

		if (documentBlurb != null) {
			this.setParsedPublicationYear(parsePublicationYear(documentBlurb));
		}
	}
	
	private String parsePublicationYear(String documentBlurb) {

		/*
		 * This pattern will match all group of numbers which have only 4 digits
		 * delimited by the word boundary.
		 * */
//		String pattern = "\\b\\d{4}\\b";
		String pattern = "(?<!-)\\b\\d{4}\\b(?=[^-])";
		
		Pattern yearPattern = Pattern.compile(pattern);
		String publishedYear = DEFAULT_PUBLICATION_YEAR;

		Matcher yearMatcher = yearPattern.matcher(documentBlurb);

		while (yearMatcher.find()) {

            String yearCandidate = yearMatcher.group();

            Integer candidateYearInteger = Integer.valueOf(yearCandidate);

            /*
             * Published year has to be equal or less than the current year
             * and more than a minimum default year.
             * */
			if (candidateYearInteger <= CURRENT_YEAR
					&& candidateYearInteger >= MINIMUM_PUBLICATION_YEAR) {
            	publishedYear = candidateYearInteger.toString();
            }

		}

		return publishedYear;
	}

	public String getDocumentDescription() {
		return documentDescription;
	}
	public void setDocumentDescription(String documentDescription) {
		this.documentDescription = documentDescription;
	}

	/*
	 * Only the
	 * */
	private void setParsedPublicationYear(String parsedPublicationYear) {
		this.parsedPublicationYear = parsedPublicationYear;
	}
	
	public String getParsedPublicationYear() {
		return parsedPublicationYear;
	}

	/*
	 * This publicationYear value is directly from the data supported by the ontology. If this is empty only 
	 * then use the parsedPublicationYear.
	 * */
	public String getPublicationYear() {
		return publicationYear;
	}

	public void setPublicationYear(String publicationYear) {
		this.publicationYear = publicationYear;
	}

}
