/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.identifier.factory;

import java.util.ArrayList;
import java.util.Collection;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.ArrayIdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.common.HasProfile;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.SelfEditingConfiguration;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Find all of the individuals that are associated with the current user and create HasAssociatedIndividual for each
 * one.
 */
public class HasProfileFactory extends BaseUserBasedIdentifierBundleFactory {
    private static final Log log = LogFactory.getLog(HasProfileFactory.class);

    @Override
    public IdentifierBundle getIdentifierBundleForUser(UserAccount user) {
        ArrayIdentifierBundle ids = new ArrayIdentifierBundle();

        for (Individual ind : getAssociatedIndividuals(user)) {
            ids.add(new HasProfile(ind.getURI()));
        }

        return ids;
    }

    /**
     * Get all Individuals associated with the current user as SELF.
     */
    private Collection<Individual> getAssociatedIndividuals(UserAccount user) {
        Collection<Individual> individuals = new ArrayList<Individual>();

        if (user == null) {
            log.debug("No Associated Individuals: not logged in.");
            return individuals;
        }

        SelfEditingConfiguration sec = SelfEditingConfiguration.getInstance();
        individuals.addAll(sec.getAssociatedIndividuals(indDao, user));

        return individuals;
    }
}
