/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.triplesource.impl.virtuoso;

import edu.cornell.mannlib.vitro.webapp.modules.ComponentStartupStatus;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.virtuoso.RDFServiceVirtuoso;
import edu.cornell.mannlib.vitro.webapp.triplesource.impl.sparql.ContentTripleSourceSPARQL;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Validation;
import edu.cornell.mannlib.vitro.webapp.utils.logging.ToString;

/**
 * So far, it's just like a ContentTripleSourceSPARQL but it uses an instance of
 * RDFServiceVirtuoso.
 */
public class ContentTripleSourceVirtuoso extends ContentTripleSourceSPARQL {
	private String baseUri;
	private String username;
	private String password;

	@Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#hasBaseURI")
	public void setBaseUri(String uri) {
		if (baseUri == null) {
			baseUri = uri;
		} else {
			throw new IllegalStateException(
					"Configuration includes multiple instances of BaseURI: "
							+ baseUri + ", and " + uri);
		}
	}

	@Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#hasUsername")
	public void setUsername(String user) {
		if (username == null) {
			username = user;
		} else {
			throw new IllegalStateException(
					"Configuration includes multiple instances of Username: "
							+ username + ", and " + user);
		}
	}

	@Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#hasPassword")
	public void setPassword(String pass) {
		if (password == null) {
			password = pass;
		} else {
			throw new IllegalStateException(
					"Configuration includes multiple instances of Password: "
							+ password + ", and " + pass);
		}
	}

	@Override
	@Validation
	public void validate() throws Exception {
		if (baseUri == null) {
			throw new IllegalStateException(
					"Configuration did not include a BaseURI.");
		}
		if (username == null) {
			throw new IllegalStateException(
					"Configuration did not include a Username.");
		}
		if (password == null) {
			throw new IllegalStateException(
					"Configuration did not include a Password.");
		}
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
