/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import org.apache.jena.ontology.OntModel;

public class SingleContentOntModelSelector extends SimpleOntModelSelector {

    public SingleContentOntModelSelector(OntModel contentModel, 
                                         OntModel displayModel, 
                                         OntModel userAccountsModel) {
        super(contentModel);
        super.displayModel = displayModel;
        super.userAccountsModel = userAccountsModel;
    }
    
}
