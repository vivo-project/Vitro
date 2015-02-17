/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.impl.virtuoso;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.sparql.RDFServiceSparql;

/**
 * For now, at least, it is just like an RDFServiceSparql except for:
 * 
 * A small change in the syntax of an UPDATE request.
 * 
 * Allow for the nonNegativeInteger bug when checking to see whether a graph has
 * changed.
 */
public class RDFServiceVirtuoso extends RDFServiceSparql {

	public RDFServiceVirtuoso(String readEndpointURI, String updateEndpointURI,
			String defaultWriteGraphURI) {
		super(readEndpointURI, updateEndpointURI, defaultWriteGraphURI);
	}

	public RDFServiceVirtuoso(String readEndpointURI, String updateEndpointURI) {
		super(readEndpointURI, updateEndpointURI);
	}

	public RDFServiceVirtuoso(String endpointURI) {
		super(endpointURI);
	}

	@Override
	protected void executeUpdate(String updateString)
			throws RDFServiceException {
		super.executeUpdate(updateString.replace("INSERT DATA", "INSERT"));
	}

	/**
	 * Virtuoso has a bug, which it shares with TDB: if given a literal of type
	 * xsd:nonNegativeInteger, it stores a literal of type xsd:integer.
	 * 
	 * To determine whether this serialized graph is equivalent to what is
	 * already in Virtuoso, we need to do the same.
	 */
	@Override
	public boolean isEquivalentGraph(String graphURI,
			InputStream serializedGraph,
			ModelSerializationFormat serializationFormat)
			throws RDFServiceException {
		return super.isEquivalentGraph(graphURI,
				adjustForNonNegativeIntegers(serializedGraph),
				serializationFormat);
	}

	/**
	 * Convert all of the references to "nonNegativeInteger" to "integer" in
	 * this serialized graph.
	 * 
	 * This isn't rigorous: it could fail if another property contained the text
	 * "nonNegativeInteger" in its name, or if that text were used as part of a
	 * string literal. If that happens before this Virtuoso bug is fixed, we'll
	 * need to improve this method.
	 * 
	 * It also isn't scalable: if we wanted real scalability, we would write to
	 * a temporary file as we converted.
	 */
	private InputStream adjustForNonNegativeIntegers(InputStream serializedGraph)
			throws RDFServiceException {
		try {
			String raw = IOUtils.toString(serializedGraph, "UTF-8");
			String modified = raw.replace("nonNegativeInteger", "integer");
			return new ByteArrayInputStream(modified.getBytes("UTF-8"));
		} catch (IOException e) {
			throw new RDFServiceException(e);
		}
	}

}
