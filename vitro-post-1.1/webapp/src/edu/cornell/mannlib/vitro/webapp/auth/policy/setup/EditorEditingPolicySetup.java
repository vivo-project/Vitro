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

import edu.cornell.mannlib.vitro.webapp.auth.identifier.EditorEditingIdentifierFactory;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundleFactory;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.ServletIdentifierBundleFactory;
import edu.cornell.mannlib.vitro.webapp.auth.policy.EditorEditingPolicy;
import edu.cornell.mannlib.vitro.webapp.auth.policy.SelfEditingPolicy;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ServletPolicyList;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyIface;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.VisitingPolicyIface;
import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

/**
 * Sets up RoleBasedPolicy and IdentifierBundleFactory.  
 * This will cause the vitro native login to add Identifiers that can
 * be used by the Auth system and the in-line editing.
 * 
 * To use this add it as a listener to the web.xml. 
 * 
 * See RoleBasedPolicy.java
 * 
 * @author bdc34
 *
 */
public class EditorEditingPolicySetup  implements ServletContextListener  {
    private static final Log log = LogFactory.getLog(EditorEditingPolicySetup.class.getName());
   
    public void contextInitialized(ServletContextEvent sce) {
        try{
            log.debug("Setting up EditorEditingPolicy");
            
            //need to make a policy and add it to the ServeltContext
            OntModel model = (OntModel)sce.getServletContext().getAttribute("jenaOntModel");
            EditorEditingPolicy cep = makeEditorEditPolicyFromModel(model);
            ServletPolicyList.addPolicy(sce.getServletContext(), cep);
            
            //need to put an IdentifierFactory for EditorEditingIds into the ServletContext
            IdentifierBundleFactory ibfToAdd = new EditorEditingIdentifierFactory();
            ServletIdentifierBundleFactory.addIdentifierBundleFactory(sce.getServletContext(), ibfToAdd); 
            
            log.debug( "Finished setting up EditorEditingPolicy: " + cep );            
        }catch(Exception e){
            log.error("could not run EditorEditingPolicySetup: " + e);
            e.printStackTrace();
        }
    }
    
    public void contextDestroyed(ServletContextEvent sce) { /*nothing*/  }
    
    public static EditorEditingPolicy makeEditorEditPolicyFromModel( Model model ){
        EditorEditingPolicy pol = null;
        if( model == null )
            pol = new EditorEditingPolicy(null,null,null,null);
        else{
            Set<String> prohibitedProps = new HashSet<String>();
            // need to iterate through one level higher than EDITOR (the higher of current 2 targeted levels) plus all higher levels
            for (BaseResourceBean.RoleLevel e : EnumSet.range(BaseResourceBean.RoleLevel.CURATOR,BaseResourceBean.RoleLevel.NOBODY)) {
                ResIterator it = model.listSubjectsWithProperty( model.createProperty( VitroVocabulary.PROHIBITED_FROM_UPDATE_BELOW_ROLE_LEVEL_ANNOT),ResourceFactory.createResource(e.getURI()));
                while( it.hasNext() ){
                    Resource resource = it.nextResource();
                    if( resource != null && resource.getURI() != null ) {
                        log.debug("adding \""+resource.getURI()+"\" to properties prohibited from inline editor editing ("+e.getLabel()+")"); 
                        prohibitedProps.add( resource.getURI() );
                    }
                }
            }
            pol = new EditorEditingPolicy(prohibitedProps,null,null,null);
        }               
        return pol;
    }
            
    public static void replaceEditorEditing( ServletContext sc, Model model ){
        ServletPolicyList.replacePolicy(sc, makeEditorEditPolicyFromModel(model));                                
    }  
}