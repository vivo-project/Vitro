
/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.pageDataGetter;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.GraphStoreFactory;
import com.hp.hpl.jena.update.UpdateAction;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceDataset;

/**
 * Test to experement with Jena ARQ SPARQL update and the RDFServiceDataset.
 */
public class SparqlUpdateTestDataGetter implements PageDataGetter{
    private static final Log log = LogFactory.getLog(SparqlUpdateTestDataGetter.class);

    @Override
	public Map<String,Object> 
        getData(ServletContext context, 
                VitroRequest vreq, String pageUri, 
                Map<String, Object> page )
    {
    	HashMap<String, Object> data = new HashMap<String,Object>();
    	
        Dataset ds = new RDFServiceDataset( vreq.getUnfilteredRDFService() );
        GraphStore graphStore = GraphStoreFactory.create(ds);

        log.warn("The SPARQL update is '"+vreq.getParameter("update")+"'");

        UpdateAction.parseExecute( vreq.getParameter("update") , graphStore );
             
        return data;
    }        
   
    
    @Override
	public String getType(){
        return PageDataGetterUtils.generateDataGetterTypeURI(SparqlUpdateTestDataGetter.class.getName());
    }


    @Override
    public String getDataServiceUrl() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public JSONObject convertToJSON(Map<String, Object> map, VitroRequest vreq) {
        // TODO Auto-generated method stub
        return null;
    } 
    
}
