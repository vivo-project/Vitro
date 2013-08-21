/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.beans;

import java.io.File;

import com.hp.hpl.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.servlet.setup.RDFFilesLoader;

public class FileBasedProhibitedFromSearch extends ProhibitedFromSearch {

    /**
     * Load all the .n3 files in dir, add them to a model and create
     * a ProhibitedFromSearch based on that model
     * @param URI of the search individual.
     * @param dir to find N3 files in.
     */
    public FileBasedProhibitedFromSearch(String uri, File dir){    
        super( uri, RDFFilesLoader.getModelFromDir(dir));
    }
    
    public FileBasedProhibitedFromSearch(String URI, OntModel model) {
        super(URI, model);
    }

    
}
