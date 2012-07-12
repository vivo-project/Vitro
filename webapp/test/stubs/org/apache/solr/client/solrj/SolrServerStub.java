/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package stubs.org.apache.solr.client.solrj;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.util.NamedList;

/**
 * TODO
 */
public class SolrServerStub extends SolrServer {
	private static final Log log = LogFactory.getLog(SolrServerStub.class);

	// ----------------------------------------------------------------------
	// Stub infrastructure
	// ----------------------------------------------------------------------

	// ----------------------------------------------------------------------
	// Stub methods
	// ----------------------------------------------------------------------

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.solr.client.solrj.SolrServer#request(org.apache.solr.client
	 * .solrj.SolrRequest)
	 */
	@Override
	public NamedList<Object> request(SolrRequest request)
			throws SolrServerException, IOException {
		// TODO not really an implementation.
		return new NamedList<Object>();
	}

	// ----------------------------------------------------------------------
	// Un-implemented methods
	// ----------------------------------------------------------------------

}
