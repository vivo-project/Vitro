/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.triplesource.impl.virtuoso;

import edu.cornell.mannlib.vitro.webapp.modules.ComponentStartupStatus;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.virtuoso.RDFServiceVirtuoso;
import edu.cornell.mannlib.vitro.webapp.triplesource.impl.sparql.ContentTripleSourceSPARQL;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;
import edu.cornell.mannlib.vitro.webapp.utils.logging.ToString;

/**
 * So far, it's just like a ContentTripleSourceSPARQL but it uses an instance of
 * RDFServiceVirtuoso.
 */
public class ContentTripleSourceVirtuoso extends ContentTripleSourceSPARQL {
	private String baseUri;
	private String username;
	private String password;

	@Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#hasBaseURI", minOccurs = 1, maxOccurs = 1)
	public void setBaseUri(String uri) {
		baseUri = uri;
	}

	@Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#hasUsername", minOccurs = 1, maxOccurs = 1)
	public void setUsername(String user) {
		username = user;
	}

	@Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#hasPassword", minOccurs = 1, maxOccurs = 1)
	public void setPassword(String pass) {
		password = pass;
	}

	@Override
	protected RDFService createRDFService(ComponentStartupStatus ss) {
		ss.info("Using Virtuoso at " + baseUri + ", authenticating as "
				+ username);
		return new RDFServiceVirtuoso(baseUri, username, password);
	}

	@Override
	public String toString() {
		return "ContentTripleSourceVirtuoso[" + ToString.hashHex(this)
				+ ", baseUri=" + baseUri + ", username=" + username + "]";
	}

}
