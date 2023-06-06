/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.identifier.factory;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundleFactory;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.dao.UserAccountsDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

/**
 * Some fields and methods that are helpful to IdentifierBundleFactory classes.
 */
public abstract class BaseIdentifierBundleFactory implements
		IdentifierBundleFactory {
	protected final WebappDaoFactory wdf;
	protected final UserAccountsDao uaDao;
	protected final IndividualDao indDao;

	public BaseIdentifierBundleFactory() {
		this.wdf = ModelAccess.getInstance().getWebappDaoFactory();
		this.uaDao = wdf.getUserAccountsDao();
		this.indDao = wdf.getIndividualDao();
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " - " + hashCode();
	}

}
