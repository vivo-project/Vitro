/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package stubs.edu.cornell.mannlib.vitro.webapp.dao;

import java.util.List;

import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.dao.ApplicationDao;

/**
 * A minimal implementation of the ApplicationDao.
 * 
 * I have only implemented the methods that I needed. Feel free to implement
 * others.
 */
public class ApplicationDaoStub implements ApplicationDao {
	// ----------------------------------------------------------------------
	// Stub infrastructure
	// ----------------------------------------------------------------------

	private ApplicationBean applicationBean;

	public void setApplicationBean(ApplicationBean applicationBean) {
		this.applicationBean = applicationBean;
	}

	// ----------------------------------------------------------------------
	// Stub methods
	// ----------------------------------------------------------------------

	@Override
	public ApplicationBean getApplicationBean() {
		return this.applicationBean;
	}

	// ----------------------------------------------------------------------
	// Un-implemented methods
	// ----------------------------------------------------------------------

	@Override
	public void updateApplicationBean(ApplicationBean appBean) {
		throw new RuntimeException(
				"ApplicationDaoStub.updateApplicationBean() not implemented.");
	}

	@Override
	public List<String> getExternallyLinkedNamespaces() {
		throw new RuntimeException(
				"ApplicationDaoStub.getExternallyLinkedNamespaces() not implemented.");
	}

	@Override
	public boolean isExternallyLinkedNamespace(String namespace) {
		throw new RuntimeException(
				"ApplicationDaoStub.isExternallyLinkedNamespace() not implemented.");
	}

}
