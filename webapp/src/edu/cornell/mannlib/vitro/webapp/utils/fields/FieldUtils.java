/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.utils.fields;

import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;

public class FieldUtils {
    
    private static final Log log = LogFactory.getLog(FieldUtils.class);
    

    // copied from OptionsForPropertyTag.java in the thought that class may be deprecated
    public static List<Individual> removeIndividualsAlreadyInRange(List<Individual> individuals,
            List<ObjectPropertyStatement> stmts, String predicateUri, String objectUriBeingEdited){        
        HashSet<String>  range = new HashSet<String>();

        for(ObjectPropertyStatement ops : stmts){
            if( ops.getPropertyURI().equals(predicateUri))
                range.add( ops.getObjectURI() );
        }

        int removeCount=0;
        ListIterator<Individual> it = individuals.listIterator();
        while(it.hasNext()){
            Individual ind = it.next();
            if( range.contains( ind.getURI()) && !(ind.getURI().equals(objectUriBeingEdited)) ) {
                it.remove();
                ++removeCount;
            }
        }
        
        return individuals;
    }
    
 
    
}
