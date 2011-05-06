/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vitro.webapp.beans.PermissionSet;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.dao.UserAccountsDao;

/**
 * Implement UserAccountsDao for Jena models.
 */
public class UserAccountsDaoJena extends JenaBaseDao implements UserAccountsDao {
	public UserAccountsDaoJena(WebappDaoFactoryJena wadf) {
		super(wadf);
	}

	@Override
	protected OntModel getOntModel() {
		return getOntModelSelector().getUserAccountsModel();
	}

	@Override
	public UserAccount getUserAccountByUri(String uri) {
		if (uri == null) {
			return null;
		}

		getOntModel().enterCriticalSection(Lock.READ);
		try {
			OntResource r = getOntModel().getOntResource(uri);
			if (r == null) {
				return null;
			}

			UserAccount u = new UserAccount();
			u.setUri(r.getURI());
			u.setEmailAddress(getPropertyStringValue(r,
					USERACCOUNT_EMAIL_ADDRESS));
			u.setFirstName(getPropertyStringValue(r, USERACCOUNT_FIRST_NAME));
			u.setLastName(getPropertyStringValue(r, USERACCOUNT_LAST_NAME));
			u.setMd5Password(getPropertyStringValue(r, USERACCOUNT_MD5_PASSWORD));
			u.setOldPassword(getPropertyStringValue(r, USERACCOUNT_OLD_PASSWORD));
			u.setPasswordLinkExpires(getPropertyLongValue(r,
					USERACCOUNT_PASSWORD_LINK_EXPIRES));
			u.setPasswordChangeRequired(getPropertyBooleanValue(r,
					USERACCOUNT_PASSWORD_CHANGE_REQUIRED));
			u.setLoginCount(getPropertyIntValue(r, USERACCOUNT_LOGIN_COUNT));
			u.setStatusFromString(getPropertyStringValue(r, USERACCOUNT_STATUS));
			u.setPermissionSetUris(getPropertyResourceURIValues(r,
					USERACCOUNT_HAS_PERMISSION_SET));
			return u;
		} finally {
			getOntModel().leaveCriticalSection();
		}
	}

	@Override
	public PermissionSet getPermissionSetByUri(String uri) {
		if (uri == null) {
			return null;
		}

		getOntModel().enterCriticalSection(Lock.READ);
		try {
			OntResource r = getOntModel().getOntResource(uri);
			if (r == null) {
				return null;
			}

			PermissionSet ps = new PermissionSet();
			ps.setUri(uri);
			ps.setLabel(getPropertyStringValue(r, RDFS.label));
			ps.setPermissionUris(getPropertyResourceURIValues(r,
					PERMISSIONSET_HAS_PERMISSION));
			return ps;
		} finally {
			getOntModel().leaveCriticalSection();
		}
	}

	@Override
	public Collection<PermissionSet> getAllPermissionSets() {
		List<PermissionSet> list = new ArrayList<PermissionSet>();

		getOntModel().enterCriticalSection(Lock.READ);
		try {
			ClosableIterator<Statement> stmtIt = getOntModel().listStatements(
					null, RDF.type, PERMISSIONSET);
			try {
				while (stmtIt.hasNext()) {
					Statement stmt = stmtIt.next();
					OntResource r = stmt.getSubject().as(OntResource.class);

					PermissionSet ps = new PermissionSet();
					ps.setUri(r.getURI());
					ps.setLabel(getPropertyStringValue(r, RDFS.label));
					ps.setPermissionUris(getPropertyResourceURIValues(r,
							PERMISSIONSET_HAS_PERMISSION));
					list.add(ps);
				}
			} finally {
				stmtIt.close();
			}
		} finally {
			getOntModel().leaveCriticalSection();
		}

		return list;
	}

}
