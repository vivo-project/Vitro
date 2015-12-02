/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.json.util.JSONUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Ontology;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyGroup;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyInstance;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.OntologyDao;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyInstanceDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.web.URLEncoder;

public class ListPropertyWebappsController extends FreemarkerHttpServlet {
    private static Log log = LogFactory.getLog( ListPropertyWebappsController.class );

    private static final String TEMPLATE_NAME = "siteAdmin-objectPropHierarchy.ftl";
        
    @Override
	protected AuthorizationRequest requiredActions(VitroRequest vreq) {
		return SimplePermission.EDIT_ONTOLOGY.ACTION;
	}
    
    @Override
    protected ResponseValues processRequest(VitroRequest vreq) {

        Map<String, Object> body = new HashMap<String, Object>();
        try {

            body.put("displayOption", "all");
            body.put("pageTitle", "All Object Properties");
            body.put("propertyType", "object");

            String noResultsMsgStr = "No object properties found";

            String ontologyUri = vreq.getParameter("ontologyUri");

            ObjectPropertyDao dao = vreq.getUnfilteredWebappDaoFactory().getObjectPropertyDao();
            ObjectPropertyDao opDaoLangNeut = vreq.getLanguageNeutralWebappDaoFactory().getObjectPropertyDao();
            PropertyInstanceDao piDao = vreq.getLanguageNeutralWebappDaoFactory().getPropertyInstanceDao();
            VClassDao vcDao = vreq.getUnfilteredWebappDaoFactory().getVClassDao();
            VClassDao vcDaoLangNeut = vreq.getLanguageNeutralWebappDaoFactory().getVClassDao();
            PropertyGroupDao pgDao = vreq.getUnfilteredWebappDaoFactory().getPropertyGroupDao();

            String vclassURI = vreq.getParameter("vclassUri");
        
            List<ObjectProperty> props = new ArrayList<ObjectProperty>();
            if (vreq.getParameter("propsForClass") != null) {
                noResultsMsgStr = "There are no object properties that apply to this class.";
            
                // incomplete list of classes to check, but better than before
                List<String> superclassURIs = vcDao.getAllSuperClassURIs(vclassURI);
                superclassURIs.add(vclassURI);
                superclassURIs.addAll(vcDao.getEquivalentClassURIs(vclassURI));
            
                Map<String, PropertyInstance> propInstMap = new HashMap<String, PropertyInstance>();
                for (String classURI : superclassURIs) {
            	    Collection<PropertyInstance> propInsts = piDao.getAllPropInstByVClass(classURI);
            	    for (PropertyInstance propInst : propInsts) {
            		    propInstMap.put(propInst.getPropertyURI(), propInst);
            	    }
                }
                List<PropertyInstance> propInsts = new ArrayList<PropertyInstance>();
                propInsts.addAll(propInstMap.values());
                Collections.sort(propInsts);
            
                Iterator<PropertyInstance> propInstIt = propInsts.iterator();
                HashSet<String> propURIs = new HashSet<String>();
                while (propInstIt.hasNext()) {
                    PropertyInstance pi = (PropertyInstance) propInstIt.next();
                    if (!(propURIs.contains(pi.getPropertyURI()))) {
                        propURIs.add(pi.getPropertyURI());
                        ObjectProperty prop = (ObjectProperty) dao.getObjectPropertyByURI(pi.getPropertyURI());
                        if (prop != null) {
                            props.add(prop);
                        }
                    }
                }
            } else {
                props = (vreq.getParameter("iffRoot")!=null)
                    ? dao.getRootObjectProperties()
                    : dao.getAllObjectProperties();
            }
        
            OntologyDao oDao = vreq.getUnfilteredWebappDaoFactory().getOntologyDao();
            HashMap<String,String> ontologyHash = new HashMap<String,String>();

            Iterator<ObjectProperty> propIt = props.iterator();
            List<ObjectProperty> scratch = new ArrayList<ObjectProperty>();
            while (propIt.hasNext()) {
                ObjectProperty p = propIt.next();
                if (p.getNamespace() != null) {
                    if( !ontologyHash.containsKey( p.getNamespace() )){
                        Ontology o = oDao.getOntologyByURI(p.getNamespace());
                        if (o==null) {
                            if (!VitroVocabulary.vitroURI.equals(p.getNamespace())) {
                                log.debug("doGet(): no ontology object found for the namespace "+p.getNamespace());
                            }
                        } else {
                            ontologyHash.put(p.getNamespace(), o.getName() == null ? p.getNamespace() : o.getName());
                        }
                    }
                    if (ontologyUri != null && p.getNamespace().equals(ontologyUri)) {
                        scratch.add(p);
                    }
                }
            }

            if (ontologyUri != null) {
                props = scratch;
            }

            if (props != null) {
        	    sortForPickList(props, vreq);
            }

            String json = new String();
            int counter = 0;

            if (props != null) {
                if (props.size()==0) {
                    json = "{ \"name\": \"" + noResultsMsgStr + "\" }";
                } else {
                    Iterator<ObjectProperty> propsIt = props.iterator();
                    while (propsIt.hasNext()) {
                        if ( counter > 0 ) {
                            json += ", ";
                        }
                        ObjectProperty prop = propsIt.next();
                    
                        String propNameStr = ShowObjectPropertyHierarchyController.getDisplayLabel(prop);

                        try {
                            json += "{ \"name\": " + JSONUtils.quote("<a href='./propertyEdit?uri="+URLEncoder.encode(prop.getURI())+"'>" 
                                 + propNameStr + "</a>") + ", "; 
                         } catch (Exception e) {
                             json += "{ \"name\": \"" + propNameStr + "\", "; 
                         }
                    
                         json += "\"data\": { \"internalName\": " + JSONUtils.quote(prop.getLocalNameWithPrefix()) + ", "; 
                    
                         ObjectProperty opLangNeut = opDaoLangNeut.getObjectPropertyByURI(prop.getURI());
                         if(opLangNeut == null) {
                             opLangNeut = prop;
                         }
                         String domainStr = getVClassNameFromURI(opLangNeut.getDomainVClassURI(), vcDao, vcDaoLangNeut); 
                         json += "\"domainVClass\": " + JSONUtils.quote(domainStr) + ", " ;
                    
                         String rangeStr = getVClassNameFromURI(opLangNeut.getRangeVClassURI(), vcDao, vcDaoLangNeut);
                         json += "\"rangeVClass\": " + JSONUtils.quote(rangeStr) + ", " ; 
                    
                         if (prop.getGroupURI() != null) {
                             PropertyGroup pGroup = pgDao.getGroupByURI(prop.getGroupURI());
                             json += "\"group\": " + JSONUtils.quote((pGroup == null) ? "unknown group" : pGroup.getName()) + " } } " ; 
                         } else {
                             json += "\"group\": \"unspecified\" } }" ;
                         }
                         counter += 1;
                     }
                 }
                 body.put("jsonTree",json);
             }

        } catch (Throwable t) {
            t.printStackTrace();
        }

        return new TemplateResponseValues(TEMPLATE_NAME, body);
    }
    
    private String getVClassNameFromURI(String vclassURI, VClassDao vcDao, VClassDao vcDaoLangNeut) {
        if(vclassURI == null) {
            return "";
        }
        VClass vclass = vcDaoLangNeut.getVClassByURI(vclassURI);
        if(vclass == null) {
            return ""; 
        }
        if(vclass.isAnonymous()) {
            return vclass.getPickListName();
        } else {
            VClass vclassWLang = vcDao.getVClassByURI(vclassURI);
            return (vclassWLang != null) ? vclassWLang.getPickListName() : vclass.getPickListName();
        }
    }
}
