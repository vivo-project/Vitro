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
        DataPropertyStatement dps1 = ind1.getDataPropertyStatement(dataPropertyUri);
        DataPropertyStatement dps2 = ind2.getDataPropertyStatement(dataPropertyUri);

        int result;
        
        // Push null values to the end of the list.
        // Is this generally what's wanted? Or should this class be 
        // NullLastDataPropertyComparator?
        if (dps1 == null) {
            result = (dps2 == null) ? 0 : 1;
        } else if (dps2 == null) {
            result = -1;
        } else {
        
            String datatype = dps1.getDatatypeURI();

            if (datatype.equals(XSD.xint.toString())) {
                int i1 = Integer.valueOf(dps1.getData());
                int i2 = Integer.valueOf(dps2.getData());
                result = ((Integer) i1).compareTo(i2);
            }
            else if (datatype.equals(XSD.xstring.toString())) {
                result = dps1.getData().compareTo(dps2.getData());            
            }
            // Fill in other types here
            else {
                throw new ClassCastException("Unsupported datatype");
            }
        }
        return result;
    }
    
    

}
