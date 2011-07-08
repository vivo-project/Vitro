/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.beans;

import java.util.Comparator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.vocabulary.XSD;

/* This class is used to order authorships on the add author form. It should be removed in favor of using whatever 
 * method is used to order authorships on the publication profile page instead. I've implemented this due to 
 * time constraints.
 */
public class DataPropertyComparator implements Comparator<Individual> {

    private static final Log log = LogFactory.getLog(DataPropertyComparator.class);

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
            if (datatype == null) {
                datatype = dps2.getDatatypeURI();
            }
            if (datatype == null) {
                log.warn("Can't compare data property statements: no datatype specified.");
                // Perhaps we should throw an error here, but for now we need it to return 0
                return 0;
            }
            
            if (XSD.xint.toString().equals(datatype)) {
                int i1 = Integer.valueOf(dps1.getData());
                int i2 = Integer.valueOf(dps2.getData());
                result = ((Integer) i1).compareTo(i2);
            }
            else if (XSD.xstring.toString().equals(datatype)) {
                result = dps1.getData().compareTo(dps2.getData());            
            }
            // Fill in other types here
            else {
                //throw new ClassCastException("Unsupported datatype");
                log.warn("Can't compare data property statements: unsupported datatype.");
                return 0;
            }
        }
        return result;
    }
    
    

}
