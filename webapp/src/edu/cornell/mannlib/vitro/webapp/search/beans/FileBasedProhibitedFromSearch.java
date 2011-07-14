/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.beans;

import java.io.File;
import java.io.FileInputStream;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class FileBasedProhibitedFromSearch extends ProhibitedFromSearch {

    /**
     * Load all the .n3 files in dir, add them to a model and create
     * a ProhibitedFromSearch based on that model
     * @param URI of the search individual.
     * @param dir to find N3 files in.
     */
    public FileBasedProhibitedFromSearch(String uri, File dir){    
        super( uri, getModelFromDir(dir));
    }
    
    public FileBasedProhibitedFromSearch(String URI, OntModel model) {
        super(URI, model);
    }

    protected static OntModel getModelFromDir( File dir){
        if( dir == null )
            throw new IllegalStateException("Must pass a File to FileBasedProhibitedFromSearch");
        if( !dir.isDirectory() )
            throw new IllegalStateException("Parameter dir to FileBasedProhibitedFromSearch " +
                    "must be a File object for a directory");
        if( !dir.canRead() )
            throw new IllegalStateException("Parameter dir to FileBasedProhibitedFromSearch must " +
                    "be a directory that is readable, check premissions on " + dir.getAbsolutePath());
        
        OntModel modelOnlyForPFS = ModelFactory.createOntologyModel();
        for( File file : dir.listFiles()){
            if( file.isFile() 
                && file.canRead() 
                && file.getName() != null 
                && file.getName().endsWith(".n3")){
                try{
                    modelOnlyForPFS.read( new FileInputStream(file), null, "N3");
                }catch( Throwable th){
                    log.warn("could not load file " + 
                            file.getAbsolutePath() + file.separator + file.getName(), th);
                }
            }
        }
        
        if( modelOnlyForPFS.size() == 0 ){
            log.warn("No class exclusion statements found.");
        }                       
        
        return modelOnlyForPFS;
    }
}
