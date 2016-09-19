/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.impl.virtuoso;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.SimpleSelector;
import org.apache.jena.rdf.model.Statement;

public class VirtuosoDoubleSelector extends SimpleSelector {
    public VirtuosoDoubleSelector() {
    }

    public VirtuosoDoubleSelector(Resource subject, Property predicate, RDFNode object) {
        super(subject, predicate, object);
    }

    @Override
    public boolean test(Statement statement) {
        RDFNode objectToMatch = statement.getObject();

        // Both values are numeric, so compare them as parsed doubles
        if (objectToMatch.isLiteral()) {
            String num1 = object.asLiteral().getString();
            String num2 = objectToMatch.asLiteral().getString();

            return Double.parseDouble(num1) == Double.parseDouble(num2);
        }

        return false;
    }
}
