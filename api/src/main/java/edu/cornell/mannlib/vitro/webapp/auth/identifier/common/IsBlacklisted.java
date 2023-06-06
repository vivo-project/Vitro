/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.identifier.common;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.Identifier;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;

/**
 * The current user is blacklisted for this reason.
 */
public class IsBlacklisted extends AbstractCommonIdentifier implements
		Identifier {

	private static final String NOT_BLACKLISTED = null;

	// ----------------------------------------------------------------------
	// static methods
	// ----------------------------------------------------------------------

	/**
	 * If this individual is blacklisted, return an appropriate Identifier.
	 * Otherwise, return null.
	 */
	public static IsBlacklisted getInstance(Individual individual) {
		if (individual == null) {
			throw new NullPointerException("individual may not be null.");
		}

		String reasonForBlacklisting = NOT_BLACKLISTED;
		IsBlacklisted id = new IsBlacklisted(individual.getURI(),
				reasonForBlacklisting);
		if (id.isBlacklisted()) {
			return id;
		} else {
			return null;
		}
	}

	public static Collection<IsBlacklisted> getIdentifiers(IdentifierBundle ids) {
		return getIdentifiersForClass(ids, IsBlacklisted.class);
	}

	public static Collection<String> getBlacklistReasons(IdentifierBundle ids) {
		Set<String> set = new HashSet<String>();
		for (IsBlacklisted id : getIdentifiers(ids)) {
			if (id.isBlacklisted()) {
				set.add(id.getReasonForBlacklisting());
			}
		}
		return set;
	}

	// ----------------------------------------------------------------------
	// the Identifier
	// ----------------------------------------------------------------------

	private final String associatedIndividualUri;
	private final String reasonForBlacklisting;

	public IsBlacklisted(String associatedIndividualUri,
			String reasonForBlacklisting) {
		this.associatedIndividualUri = associatedIndividualUri;
		this.reasonForBlacklisting = reasonForBlacklisting;
	}

	public String getAssociatedIndividualUri() {
		return associatedIndividualUri;
	}

	public boolean isBlacklisted() {
		return reasonForBlacklisting != NOT_BLACKLISTED;
	}

	public String getReasonForBlacklisting() {
		return reasonForBlacklisting;
	}

	@Override
	public String toString() {
		return "IsBlacklisted[" + associatedIndividualUri + ", "
				+ reasonForBlacklisting + "]";
	}

}
