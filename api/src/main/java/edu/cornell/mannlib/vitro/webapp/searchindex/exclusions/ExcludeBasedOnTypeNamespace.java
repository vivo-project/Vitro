/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchindex.exclusions;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Validation;

/**
 * Exclude individuals based on the namespaces of their types. 
 */
public class ExcludeBasedOnTypeNamespace implements SearchIndexExcluder {

    private final List<String> namespaces = new ArrayList<>();
    Pattern nsRegexPattern; 
    
	@Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#excludes")
	public void addExcludedNamespace(String uri) {
		namespaces.add(uri);
	}
	
	@Validation
	public void compileRegexPattern() {
        String nsOrPattern = "";
        for( int i=0; i<namespaces.size(); i++){
            String ns = namespaces.get(i);
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


	@Override
	public String toString() {
		return "ExcludeBasedOnTypeNamespace[namespaces=" + namespaces + "]";
	}    
}
