package edu.cornell.mannlib.vitro.webapp.visualization.valueobjects;

public class Individual {
	
	private String individualLabel;
	private String individualURL;
	
	public Individual(String individualURL, String individualLabel) {
		this.individualLabel = individualLabel;
		this.individualURL = individualURL;
	}
	
	public Individual(String individualURL) {
		this(individualURL, "");
	}
	
	public String getIndividualLabel() {
		return individualLabel;
	}
	
	public void setIndividualLabel(String individualLabel) {
		this.individualLabel = individualLabel;
	}

	public String getIndividualURL() {
		return individualURL;
	}
	

}
