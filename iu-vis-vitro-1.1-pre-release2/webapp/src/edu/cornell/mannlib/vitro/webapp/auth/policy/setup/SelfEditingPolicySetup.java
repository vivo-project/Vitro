/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy.setup;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.SelfEditingIdentifierFactory;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.ServletIdentifierBundleFactory;
import edu.cornell.mannlib.vitro.webapp.auth.policy.SelfEditingPolicy;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ServletPolicyList;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyIface;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.VisitingPolicyIface;
import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

/**
 * Policy for SelfEditors.  This will set up the self-editing policy which
 * will will look for SelfEditing identifier in the IdentifierBundle.  If
 * the user is associated with a URI in the system then they will be allowed
 * to edit resources related to that URI.
 *  
 *  To use this add it as a listener to the web.xml. 
 *  
 * The SelfEditing policy may return  
 * Authorization.UNAUTHORIZED so it should be at the start of the 
 * ServletPolicyList if you want it to override other Policies.
 * For example, this Listener should be before the curator listener so
 * that if a curator is faking selfEditing the capabilities they have
 * as curator will not override the results of the SelfEditing policy.
 * 
 * @author bdc34
 *
 */
public class SelfEditingPolicySetup  implements ServletContextListener  {
    private static final Log log = LogFactory.getLog(SelfEditingPolicySetup.class.getName());
   
    public void contextInitialized(ServletContextEvent sce) {
        try{
            log.debug("Setting up SelfEditingPolicy");
            
            OntModel model = (OntModel)sce.getServletContext().getAttribute("jenaOntModel");
            replaceSelfEditing(sce.getServletContext(), model);

            
            SelfEditingIdentifierFactory niif =new SelfEditingIdentifierFactory();
            ServletIdentifierBundleFactory.addIdentifierBundleFactory(sce.getServletContext(), niif);

            log.debug( "SelfEditingPolicy has been setup. " );            
        }catch(Exception e){
            log.error("could not run SelfEditingPolicySetup: " + e);
            e.printStackTrace();
        }
    }
    
    public void contextDestroyed(ServletContextEvent sce) { /*nothing*/  }
    
    public static SelfEditingPolicy makeSelfEditPolicyFromModel( Model model ){
        SelfEditingPolicy pol = null;
        if( model == null )
            pol = new SelfEditingPolicy(null,null,null,null);
        else{
            Set<String> prohibitedProps = new HashSet<String>();
            //ResIterator it = model.listSubjectsWithProperty( model.createProperty( VitroVocabulary.PROPERTY_SELFEDITPROHIBITEDANNOT ) );
            
            // need to iterate through one level higher than SELF (the lowest level where restrictions make sense) plus all higher levels
            for (BaseResourceBean.RoleLevel e : EnumSet.range(BaseResourceBean.RoleLevel.EDITOR,BaseResourceBean.RoleLevel.NOBODY)) {
                ResIterator it = model.listSubjectsWithProperty( model.createProperty( VitroVocabulary.PROHIBITED_FROM_UPDATE_BELOW_ROLE_LEVEL_ANNOT),ResourceFactory.createResource(e.getURI()));
                while( it.hasNext() ){
                    Resource resource = it.nextResource();
                    if( resource != null && resource.getURI() != null ) {
                        log.debug("adding \""+resource.getURI()+"\" to properties prohibited from self-editing ("+e.getLabel()+")"); 
                        prohibitedProps.add( resource.getURI() );
                    }
                }
            }
            pol = new SelfEditingPolicy(prohibitedProps,null,null,null);
        }               
        return pol;
    }
        
    
    public static void replaceSelfEditing( ServletContext sc, Model model ){
        ServletPolicyList.replacePolicy(sc, makeSelfEditPolicyFromModel(model));
    }    
}