/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.dataGetter;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.GraphStoreFactory;
import com.hp.hpl.jena.update.UpdateAction;

import edu.cornell.mannlib.vitro.webapp.search.indexing.IndexBuilder;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceDataset;

/**
 * Test to experiment with Jena ARQ SPARQL update and the RDFServiceDataset.
 */
public class SparqlUpdateTestDataGetter implements DataGetter{
    private static final Log log = LogFactory.getLog(SparqlUpdateTestDataGetter.class);

    VitroRequest vreq;
    ServletContext context;
    
    /**
     * Constructor with display model and data getter URI that will be called by reflection.
     */
    public SparqlUpdateTestDataGetter(VitroRequest vreq ){
    	if( vreq == null ) 
    		throw new IllegalArgumentException("VitroRequest  may not be null.");                
        this.vreq = vreq;
        this.context = vreq.getSession().getServletContext();
    }        


    @Override
	public Map<String,Object> getData( Map<String, Object> valueMap ) {
    	HashMap<String, Object> data = new HashMap<String,Object>();

        String update = vreq.getParameter("update");

        if( update != null && !update.trim().isEmpty()){
            try{
                IndexBuilder.getBuilder(context).pause();
                Dataset ds = new RDFServiceDataset( vreq.getUnfilteredRDFService() );
                GraphStore graphStore = GraphStoreFactory.create(ds);
                log.warn("The SPARQL update is '"+vreq.getParameter("update")+"'");
                UpdateAction.parseExecute( vreq.getParameter("update") , graphStore );
            }finally{
                IndexBuilder.getBuilder(context).unpause();
            }

        }

        data.put("bodyTemplate", "page-sparqlUpdateTest.ftl");        
        return data;
    }        
       
}
