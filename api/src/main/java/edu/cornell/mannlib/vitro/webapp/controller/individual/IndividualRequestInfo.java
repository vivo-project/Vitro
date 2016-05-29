/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.individual;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.web.ContentType;

/**
 * All sorts of requests are fielded by the IndividualController. This is the
 * essence of such a the request.
 */
public class IndividualRequestInfo {
	public enum Type {
		RDF_REDIRECT, // Redirect a requst for RDF to the preferred URL.
		BYTESTREAM_REDIRECT, // Redirect a request for file contents.
		NO_INDIVIDUAL, // The requested individual doesn't exist.
		LINKED_DATA, // Requesting RDF for this individual.
		DEFAULT // Requesting HTML response for this individual.
	}

	public static IndividualRequestInfo buildRdfRedirectInfo(String redirectUrl) {
		return new IndividualRequestInfo(Type.RDF_REDIRECT, redirectUrl, null,
				null);
	}

	public static IndividualRequestInfo buildBytestreamRedirectInfo(
			String redirectUrl) {
		return new IndividualRequestInfo(Type.BYTESTREAM_REDIRECT, redirectUrl,
				null, null);
	}

	public static IndividualRequestInfo buildNoIndividualInfo() {
		return new IndividualRequestInfo(Type.NO_INDIVIDUAL, null, null, null);
	}

	public static IndividualRequestInfo buildLinkedDataInfo(
			Individual individual, ContentType rdfFormat) {
		return new IndividualRequestInfo(Type.LINKED_DATA, null, individual,
				rdfFormat);
	}

	public static IndividualRequestInfo buildDefaultInfo(Individual individual) {
		return new IndividualRequestInfo(Type.DEFAULT, null, individual, null);
	}

	private final Type type;
	private final String redirectUrl;
	private final Individual individual;
	private final ContentType rdfFormat;

	private IndividualRequestInfo(Type type, String redirectUrl,
			Individual individual, ContentType rdfFormat) {
		if (type == null) {
			throw new NullPointerException("type may not be null.");
		}

		if (((type == Type.RDF_REDIRECT) || (type == Type.BYTESTREAM_REDIRECT))
				&& (redirectUrl == null)) {
			throw new NullPointerException(
					"redirectUrl may not be null if type is " + type + ".");
		}

		if (((type == Type.LINKED_DATA) || (type == Type.DEFAULT))
				&& (individual == null)) {
			throw new NullPointerException(
					"individual may not be null if type is " + type + ".");
		}

		if ((type == Type.LINKED_DATA) && (rdfFormat == null)) {
			throw new NullPointerException(
					"rdfFormat may not be null if type is " + type + ".");
		}

		this.type = type;
		this.redirectUrl = redirectUrl;
		this.individual = individual;
		this.rdfFormat = rdfFormat;
	}

	public Type getType() {
		return this.type;
	}

	public String getRedirectUrl() {
		return this.redirectUrl;
	}

	public Individual getIndividual() {
		return this.individual;
	}

	public ContentType getRdfFormat() {
		return this.rdfFormat;
	}
}
