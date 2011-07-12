package edu.cornell.mannlib.vitro.webapp.search.indexing;

import java.util.Collections;
import java.util.List;

import com.hp.hpl.jena.ontology.OntModel;

public class AdditionalURIsForContextNodes implements AdditionalURIsToIndex {

    private OntModel model;

    public AdditionalURIsForContextNodes( OntModel jenaOntModel){
        this.model = jenaOntModel;
    }
    
    @Override
    public List<String> findAdditionalURIsToIndex(String uri) {
        // TODO Auto-generated method stub
        return Collections.emptyList();
    }

}
