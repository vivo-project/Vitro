/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields;

import java.text.Collator;
import java.util.Comparator;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

public class IndividualsViaObjectPropertyByRankOptions extends IndividualsViaObjectPropetyOptions {
    private WebappDaoFactory wdf = null;
    public IndividualsViaObjectPropertyByRankOptions(String subjectUri,
            String predicateUri, String objectUri, WebappDaoFactory wdf)  throws Exception {
       super(subjectUri, predicateUri, objectUri);
       this.wdf = wdf;
    }
    
    public Comparator<String[]> getCustomComparator() {
    	return new DisplayRankComparator(wdf);
    }
    
    private static class DisplayRankComparator implements Comparator<String[]> {
    	private WebappDaoFactory wdf = null;
    	public DisplayRankComparator(WebappDaoFactory wdf) {
    		this.wdf = wdf;
    	}
        public int compare (String[] s1, String[] s2) {
            Collator collator = Collator.getInstance();
            if (s2 == null) {
                return 1;
            } else if (s1 == null) {
                return -1;
            } else {
            	if ("".equals(s1[0])) {
            		return -1;
            	} else if ("".equals(s2[0])) {
            		return 1;
            	}
                if (s2[1]==null) {
                    return 1;
                } else if (s1[1] == null){
                    return -1;
                } else {
                    return compareRanks(collator, s1, s2); 
                }
            }
        }
        
        private  int compareRanks(Collator collator, String[] s1, String[] s2) {
        	String uri1 = s1[0];
        	String uri2 = s2[0];
        	Individual ind1 = this.wdf.getIndividualDao().getIndividualByURI(uri1);
        	Individual ind2 = this.wdf.getIndividualDao().getIndividualByURI(uri2);
        	//Get display ranks 
        	return collator.compare(ind1.getLocalName(), ind2.getLocalName());
        }
    }    
}
