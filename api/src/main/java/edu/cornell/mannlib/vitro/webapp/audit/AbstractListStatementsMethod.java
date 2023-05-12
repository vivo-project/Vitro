/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.audit;

import freemarker.ext.beans.StringModel;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

import java.util.List;

/**
 * Base helper method for Freemarker
 */
public abstract class AbstractListStatementsMethod implements TemplateMethodModelEx {
    @Override
    public Object exec(List arguments) throws TemplateModelException {
        // We expect two arguments
        // 1 - an AuditChangeSet
        // 2 - a graph URI
        if (arguments.size() == 2) {
            Object arg1 = arguments.get(0);
            Object arg2 = arguments.get(1);

            // This looks odd, but the AuditChangeSet is wrapped in a StringModel
            if (arg1 instanceof StringModel) {
                arg1 = ((StringModel)arg1).getWrappedObject();
            }

            if (arg1 instanceof AuditChangeSet && arg2 instanceof SimpleScalar) {
                AuditChangeSet dataset = (AuditChangeSet)arg1;
                String graphUri = ((SimpleScalar) arg2).getAsString();

                // Get the statements from the changeset for the named graph
                return getStatements(dataset, graphUri);
            }
        }

        throw new TemplateModelException("Wrong arguments");
    }

    /**
     * Abstract method to be implemented for Added / Removed statements
     *
     * @param dataset
     * @param graphUri
     * @return
     */
    protected abstract Object getStatements(AuditChangeSet dataset, String graphUri);
}
