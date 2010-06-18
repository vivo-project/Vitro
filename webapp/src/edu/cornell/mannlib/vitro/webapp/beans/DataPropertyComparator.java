/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.beans;

import java.util.Comparator;

import com.hp.hpl.jena.vocabulary.XSD;

public class DataPropertyComparator implements Comparator<Individual> {

    private String dataPropertyUri = null;
    
    public DataPropertyComparator(String dataPropertyUri) {
        this.dataPropertyUri = dataPropertyUri;
    }

    public int compare(Individual ind1, Individual ind2) {
        DataPropertyStatement s1 = ind1.getDataPropertyStatement(dataPropertyUri);
        DataPropertyStatement s2 = ind2.getDataPropertyStatement(dataPropertyUri);
        
        String datatype = s1.getDatatypeURI();
        
        int result;
        
        if (datatype.equals(XSD.xint.toString())) {
            int i1 = Integer.valueOf(s1.getData());
            int i2 = Integer.valueOf(s2.getData());
            result = ((Integer) i1).compareTo(i2);
        }
        else if (datatype.equals(XSD.xstring.toString())) {
            result = s1.getData().compareTo(s2.getData());            
        }
        // Fill in other types here
        else {
            throw new ClassCastException("Unsupported datatype");
        }

        return result;
    }
    
    

}
