/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import edu.cornell.mannlib.vitro.webapp.utils.json.JacksonUtils;
import edu.cornell.mannlib.vitro.webapp.web.URLEncoder;

import javax.servlet.annotation.WebServlet;

@WebServlet(name = "ListPropertyWebappsController", urlPatterns = {"/listPropertyWebapps"} )
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

            StringBuilder json = new StringBuilder();
            int counter = 0;

            if (props != null) {
                if (props.size()==0) {
                    json = new StringBuilder("{ \"name\": \"" + noResultsMsgStr + "\" }");
                } else {
                    for (ObjectProperty prop1 : props) {
                        if (counter > 0) {
                            json.append(", ");
                        }
                        ObjectProperty prop = prop1;

                        String propNameStr = ShowObjectPropertyHierarchyController.getDisplayLabel(prop);

                        try {
                            json.append("{ \"name\": ").append(JacksonUtils.quote("<a href='./propertyEdit?uri=" + URLEncoder.encode(prop.getURI()) + "'>"
                                    + propNameStr + "</a>")).append(", ");
                        } catch (Exception e) {
                            json.append("{ \"name\": \"").append(propNameStr).append("\", ");
                        }

                        json.append("\"data\": { \"internalName\": ").append(JacksonUtils.quote(prop.getLocalNameWithPrefix())).append(", ");

                        ObjectProperty opLangNeut = opDaoLangNeut.getObjectPropertyByURI(prop.getURI());
                        if (opLangNeut == null) {
                            opLangNeut = prop;
                        }
                        String domainStr = getVClassNameFromURI(opLangNeut.getDomainVClassURI(), vcDao, vcDaoLangNeut);
                        json.append("\"domainVClass\": ").append(JacksonUtils.quote(domainStr)).append(", ");

                        String rangeStr = getVClassNameFromURI(opLangNeut.getRangeVClassURI(), vcDao, vcDaoLangNeut);
                        json.append("\"rangeVClass\": ").append(JacksonUtils.quote(rangeStr)).append(", ");

                        if (prop.getGroupURI() != null) {
                            PropertyGroup pGroup = pgDao.getGroupByURI(prop.getGroupURI());
                            json.append("\"group\": ").append(JacksonUtils.quote((pGroup == null) ? "unknown group" : pGroup.getName())).append(" } } ");
                        } else {
                            json.append("\"group\": \"unspecified\" } }");
                        }
                        counter += 1;
                    }
                 }
                 body.put("jsonTree", json.toString());
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
