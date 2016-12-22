package org.vivoweb.linkeddatafragments.datasource.rdfservice;

import org.linkeddatafragments.datasource.DataSourceBase;
import org.linkeddatafragments.datasource.IFragmentRequestProcessor;
import org.linkeddatafragments.fragments.IFragmentRequestParser;
import org.linkeddatafragments.fragments.tpf.TPFRequestParserForJenaBackends;

import java.io.File;

/**
 * Experimental Jena TDB-backed data source of Basic Linked Data Fragments.
 *
 * @author <a href="mailto:bart.hanssens@fedict.be">Bart Hanssens</a>
 * @author <a href="http://olafhartig.de">Olaf Hartig</a>
 */
public class RDFServiceDataSource extends DataSourceBase {

    /**
     * The request processor
     * 
     */
    protected final RDFServiceBasedRequestProcessorForTPFs requestProcessor;

    @Override
    public IFragmentRequestParser getRequestParser()
    {
        return TPFRequestParserForJenaBackends.getInstance();
    }

    @Override
    public IFragmentRequestProcessor getRequestProcessor()
    {
        return requestProcessor;
    }

    /**
     * Constructor
     *
     * @param title
     * @param description
     */
    public RDFServiceDataSource(String title, String description) {
        super(title, description);
        requestProcessor = new RDFServiceBasedRequestProcessorForTPFs();
    }
}
