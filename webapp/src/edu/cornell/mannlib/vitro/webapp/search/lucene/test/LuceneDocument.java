package edu.cornell.mannlib.vitro.webapp.search.lucene.test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LuceneDocument {

    private static final Log log = LogFactory.getLog(LuceneDocument.class.getName());

    String URI;
	String DOCID;
	String JCLASS;
	String RDFTYPE;
	String CLASSGROUP_URI;
	String MODTIME;
	String NAME;
	String PORTAL;
	String INDEXEDTIME;
	String TIMEKEY;
	String SUNSET;
	String MONIKER;
	String ALLTEXT;
	String KEYWORDS;
	String THUMBNAIL;
	String PROHIBITED_FROM_TEXT_RESULTS;
	String CLASSLOCALNAME;
	String CONTEXTNODE;
	
	static final String FILE = "~/Desktop/LuceneIndividuals.txt";


	public String getURI() {
		return URI;
	}

	public void setURI(String uRI) {
		URI = uRI;
	}

	public String getDOCID() {
		return DOCID;
	}

	public void setDOCID(String dOCID) {
		DOCID = dOCID;
	}

	public String getJCLASS() {
		return JCLASS;
	}

	public void setJCLASS(String jCLASS) {
		JCLASS = jCLASS;
	}

	public String getRDFTYPE() {
		return RDFTYPE;
	}

	public void setRDFTYPE(String rDFTYPE) {
		RDFTYPE = rDFTYPE;
	}

	public String getCLASSGROUP_URI() {
		return CLASSGROUP_URI;
	}

	public void setCLASSGROUP_URI(String cLASSGROUP_URI) {
		CLASSGROUP_URI = cLASSGROUP_URI;
	}

	public String getMODTIME() {
		return MODTIME;
	}

	public void setMODTIME(String mODTIME) {
		MODTIME = mODTIME;
	}

	public String getNAME() {
		return NAME;
	}

	public void setNAME(String nAME) {
		NAME = nAME;
	}

	public String getPORTAL() {
		return PORTAL;
	}

	public void setPORTAL(String pORTAL) {
		PORTAL = pORTAL;
	}

	public String getINDEXEDTIME() {
		return INDEXEDTIME;
	}

	public void setINDEXEDTIME(String iNDEXEDTIME) {
		INDEXEDTIME = iNDEXEDTIME;
	}

	public String getTIMEKEY() {
		return TIMEKEY;
	}

	public void setTIMEKEY(String tIMEKEY) {
		TIMEKEY = tIMEKEY;
	}

	public String getSUNSET() {
		return SUNSET;
	}

	public void setSUNSET(String sUNSET) {
		SUNSET = sUNSET;
	}

	public String getMONIKER() {
		return MONIKER;
	}

	public void setMONIKER(String mONIKER) {
		MONIKER = mONIKER;
	}

	public String getALLTEXT() {
		return ALLTEXT;
	}

	public void setALLTEXT(String aLLTEXT) {
		ALLTEXT = aLLTEXT;
	}

	public String getKEYWORDS() {
		return KEYWORDS;
	}

	public void setKEYWORDS(String kEYWORDS) {
		KEYWORDS = kEYWORDS;
	}

	public String getTHUMBNAIL() {
		return THUMBNAIL;
	}

	public void setTHUMBNAIL(String tHUMBNAIL) {
		THUMBNAIL = tHUMBNAIL;
	}

	public String getPROHIBITED_FROM_TEXT_RESULTS() {
		return PROHIBITED_FROM_TEXT_RESULTS;
	}

	public void setPROHIBITED_FROM_TEXT_RESULTS(String pROHIBITED_FROM_TEXT_RESULTS) {
		PROHIBITED_FROM_TEXT_RESULTS = pROHIBITED_FROM_TEXT_RESULTS;
	}

	public String getCLASSLOCALNAME() {
		return CLASSLOCALNAME;
	}

	public void setCLASSLOCALNAME(String cLASSLOCALNAME) {
		CLASSLOCALNAME = cLASSLOCALNAME;
	}

	@Override
	public String toString(){
		
		StringBuffer result = new StringBuffer();
		
		result.append("\n==================================\n");
		
		result.append("URI : " + URI);
		result.append("\nDOCID : " + DOCID);
		result.append("\nJCLASS : " + JCLASS);
		result.append("\nRDFTYPE : " + RDFTYPE);
		result.append("\nCLASSGROUP_URI : " + CLASSGROUP_URI);
		result.append("\nMODTIME : " + MODTIME);
		result.append("\nNAME : " + NAME);
		result.append("\nPORTAL : " + PORTAL);
		result.append("\nINDEXEDTIME : " + INDEXEDTIME);
		result.append("\nCONTEXTNODE : " + CONTEXTNODE);
		result.append("\nTIMEKEY : " + TIMEKEY);
		result.append("\nSUNSET : " + SUNSET);
		result.append("\nMONIKER : " + MONIKER);
		result.append("\nALLTEXT : " + ALLTEXT);
		result.append("\nKEYWORDS : " + KEYWORDS);
		result.append("\nTHUMBNAIL : " + THUMBNAIL);
		result.append("\nPROHIBITED_FROM_TEXT_RESULTS : " + PROHIBITED_FROM_TEXT_RESULTS);
		result.append("\nCLASSLOCALNAME : " + CLASSLOCALNAME);
		
		return result.toString();
		
	}
	
	public void writeToLog(){
		log.info(this.toString());
	}

	public void setCONTEXTNODE(String contextNodePropertyValues) {
		this.CONTEXTNODE = contextNodePropertyValues;
	}
}
