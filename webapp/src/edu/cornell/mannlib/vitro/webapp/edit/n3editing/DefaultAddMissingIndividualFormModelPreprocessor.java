/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * This will remove old relation triple for an edit.  
 * @author bdc34
 *
 */
public class DefaultAddMissingIndividualFormModelPreprocessor implements
        ModelChangePreprocessor {
    private String subjectUri, predicateUri, objectUri;
    
    public DefaultAddMissingIndividualFormModelPreprocessor(String subjectUri,
            String predicateUri, String objectUri) {
        super();
        this.subjectUri = subjectUri;
        this.predicateUri = predicateUri;
        this.objectUri = objectUri;
    }

    public void preprocess( Model retractionsModel, Model additionsModel) {
        if( retractionsModel == null || additionsModel == null)
            return;                
        
        retractionsModel.add(
                ResourceFactory.createResource(subjectUri),
                ResourceFactory.createProperty(predicateUri),
                ResourceFactory.createResource(objectUri));           
    }

}
