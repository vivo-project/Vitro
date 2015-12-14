/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.identifier.factory;

import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.ArrayIdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.Identifier;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.common.HasProfile;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.common.IsBlacklisted;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.SelfEditingConfiguration;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;

/**
 * Find all of the individuals that are associated with the current user, and
 * create either an IsBlacklisted or HasAssociatedIndividual for each one.
 */
public class HasProfileOrIsBlacklistedFactory extends
		BaseUserBasedIdentifierBundleFactory {
	private static final Log log = LogFactory
			.getLog(HasProfileOrIsBlacklistedFactory.class);

	public HasProfileOrIsBlacklistedFactory(ServletContext ctx) {
		super(ctx);
	}

	@Override
	public IdentifierBundle getIdentifierBundleForUser(UserAccount user) {
		ArrayIdentifierBundle ids = new ArrayIdentifierBundle();

		for (Individual ind : getAssociatedIndividuals(user)) {
			// If they are blacklisted, this factory will return an identifier
			Identifier id = IsBlacklisted.getInstance(ind, ctx);
			if (id != null) {
				ids.add(id);
			} else {
				ids.add(new HasProfile(ind.getURI()));
			}
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

		SelfEditingConfiguration sec = SelfEditingConfiguration.getBean(ctx);
		individuals.addAll(sec.getAssociatedIndividuals(indDao, user));

		return individuals;
	}
}