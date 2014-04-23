/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.search.documentBuilding;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;

/**
 * Exclude individuals based on the namespaces of their types. 
 */
public class ExcludeBasedOnTypeNamespace implements SearchIndexExcluder {

    final List<String> namespaces;
    Pattern nsRegexPattern; 
    
    public ExcludeBasedOnTypeNamespace(String ... namespaces) {
        super();              
        this.namespaces = Collections.unmodifiableList(Arrays.asList( namespaces ));
        String nsOrPattern = "";
        for( int i=0; i<namespaces.length; i++){
            String ns = namespaces[i];
            nsOrPattern = nsOrPattern + (i!=0?"|":"") + Pattern.quote(ns) + "[^/#]*$";            
        }
        this.nsRegexPattern = Pattern.compile(nsOrPattern);
    }
    

    @Override
    public String checkForExclusion(Individual ind) {
        if( ind != null && ind.getVClasses() != null ){
            for( VClass vclass : ind.getVClasses() ){
                String excludeMsg = checkForSkip(ind, vclass);
                if(excludeMsg != null)
                    return excludeMsg;
            }
        }
        return null;
    }

    String checkForSkip(Individual individual, VClass vclass) {
        if( vclass != null && vclass.getURI() != null ){
            Matcher match=nsRegexPattern.matcher( vclass.getURI() );
            if( match.matches() ){
                return "Skipping because it is of a type in an excluded namespace.";
            }
        }
        return null;
    }    


}
