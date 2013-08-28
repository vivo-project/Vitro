/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.dataGetter;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.GraphStoreFactory;
import com.hp.hpl.jena.update.UpdateAction;

import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequiresActions;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceDataset;
import edu.cornell.mannlib.vitro.webapp.search.indexing.IndexBuilder;

/**
 * Handle a SPARQL Update request. This uses Jena ARQ and the RDFServiceDataset to
 * evaluate a SPARQL Update with the RDFService.
 * 
 * The reason to make this a DataGettere was to allow configuration in RDF of this
 * service.  
 */
public class SparqlUpdate implements DataGetter, RequiresActions{
    
    private static final Log log = LogFactory.getLog(SparqlUpdate.class);

    VitroRequest vreq;
    ServletContext context;
    
    public SparqlUpdate( 
            VitroRequest vreq, Model displayModel, String dataGetterURI ) {
    	if( vreq == null ) 
    		throw new IllegalArgumentException("VitroRequest  may not be null.");                
        this.vreq = vreq;
        this.context = vreq.getSession().getServletContext();
    }        


    /**
     * Gets the update from the request and then executes it on
     * the RDFService.
     */
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


    /**
     * Check if this request is authorized by the email/password.
     * If not do normal authorization. 
     */
    @Override
    public Actions requiredActions(VitroRequest vreq) {
        String email = vreq.getParameter("email");
        String password = vreq.getParameter("password");
                        
        boolean isAuth = PolicyHelper.isAuthorizedForActions(vreq, 
                email, password, SimplePermission.MANAGE_SEARCH_INDEX.ACTIONS);
        
        if( isAuth )
            return Actions.AUTHORIZED;
        else
            return SimplePermission.MANAGE_SEARCH_INDEX.ACTIONS;
    }        
       
}
