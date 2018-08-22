/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.audit;

/**
 * Freemarker helper method to retrieve the statements added to a graph
 */
public class ListAddedStatementsMethod extends AbstractListStatementsMethod {
    @Override
    protected Object getStatements(AuditChangeSet dataset, String graphUri) {
        return dataset.getAddedStatements(graphUri);
    }
}
