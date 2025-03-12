/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.filter;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.impl.OntModelImpl;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;

/**
 * Custom OntModel implementation that stores a link to an underlying OntModel being wrapped
 * allowing it to be interrogated in ways that are hidden by the Model wrapper.
 */
public class LangAwareOntModel extends OntModelImpl {
    private OntModel wrappedModel;

    public LangAwareOntModel(OntModelSpec spec, Model model, OntModel wrappedModel) {
        super(spec, model);
        this.wrappedModel = wrappedModel;
    }

    /**
     * Determine if a uri is a subject in submodels attached to the underlying OntModel
     */
    public boolean isDefinedInSubModel(String uri) {
        ExtendedIterator<OntModel> subModels = wrappedModel.listSubModels();
        while (subModels.hasNext()) {
            OntModel subModel = subModels.next();
            if (subModel.contains(subModel.getResource(uri), RDF.type)) {
                return true;
            }
        }

        return false;
    }
}
