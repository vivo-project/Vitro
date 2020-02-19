/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.beans.Ontology;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.OntologyDao;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.utils.json.JacksonUtils;

import javax.servlet.annotation.WebServlet;


@WebServlet(name = "ListVClassWebappsController", urlPatterns = {"/listVClassWebapps"} )
public class ListVClassWebappsController extends FreemarkerHttpServlet {

    private static final Log log = LogFactory.getLog(ListVClassWebappsController.class.getName());

    private static final String TEMPLATE_NAME = "siteAdmin-classHierarchy.ftl";

    @Override
	protected AuthorizationRequest requiredActions(VitroRequest vreq) {
		return SimplePermission.EDIT_ONTOLOGY.ACTION;
	}

    @Override
    protected ResponseValues processRequest(VitroRequest vreq) {

        Map<String, Object> body = new HashMap<String, Object>();

        body.put("displayOption", "all");
        body.put("pageTitle", "All Classes");

		if ( vreq.getParameter("propertyURI") != null ) {
			body.put("propertyURI", vreq.getParameter("propertyURI"));
		}

        List<VClass> classes = null;

        if (vreq.getParameter("showPropertyRestrictions") != null) {
			if ( vreq.getParameter("propertyType").equals("object") ) {
				body.put("editController", "propertyEdit?uri=");
			}
			else {
				body.put("editController", "datapropEdit?uri=");
			}

			body.put("propertyName", vreq.getParameter("propertyName"));
        	PropertyDao pdao = vreq.getLanguageNeutralWebappDaoFactory().getObjectPropertyDao();
        	classes = pdao.getClassesWithRestrictionOnProperty(vreq.getParameter("propertyURI"));
        } else {
        	VClassDao vcdao = vreq.getUnfilteredWebappDaoFactory().getVClassDao();

        	if (vreq.getParameter("iffRoot") != null) {
                classes = vcdao.getRootClasses();
        	} else {
        		classes = vcdao.getAllVclasses();
        	}

        }
        StringBuilder json = new StringBuilder();
        int counter = 0;

        String ontologyURI = vreq.getParameter("ontologyUri");

        if (classes != null) {
            sortForPickList(classes, vreq);
			for (VClass aClass : classes) {
				if (counter > 0) {
					json.append(", ");
				}
				VClass cls = aClass;
				if ((ontologyURI == null) || ((ontologyURI != null) && (cls.getNamespace() != null) && (ontologyURI.equals(cls.getNamespace())))) {
					if (cls.getName() != null)
						try {
							json.append("{ \"name\": ").append(JacksonUtils.quote("<a href='./vclassEdit?uri=" + URLEncoder.encode(cls.getURI(), "UTF-8") + "'>" + cls.getPickListName() + "</a>")).append(", ");
						} catch (Exception e) {
							json.append("{ \"name\": ").append(JacksonUtils.quote(cls.getPickListName())).append(", ");
						}
					else
						json.append("{ \"name\": \"\"");
					String shortDef = (cls.getShortDef() == null) ? "" : cls.getShortDef();

					json.append("\"data\": { \"shortDef\": ").append(JacksonUtils.quote(shortDef)).append(", ");

					// get group name
					WebappDaoFactory wadf = vreq.getUnfilteredWebappDaoFactory();
					VClassGroupDao groupDao = wadf.getVClassGroupDao();
					String groupURI = cls.getGroupURI();
					String groupName = "";
					VClassGroup classGroup = null;
					if (groupURI != null) {
						classGroup = groupDao.getGroupByURI(groupURI);
						if (classGroup != null) {
							groupName = classGroup.getPublicName();
						}
					}

					json.append("\"classGroup\": ").append(JacksonUtils.quote(groupName)).append(", ");

					// get ontology name
					OntologyDao ontDao = wadf.getOntologyDao();
					String ontName = cls.getNamespace();
					Ontology ont = ontDao.getOntologyByURI(ontName);
					if (ont != null && ont.getName() != null) {
						ontName = ont.getName();
					}
					json.append("\"ontology\": ").append(JacksonUtils.quote(ontName)).append("} }");

					counter++;

				}
			}
            body.put("jsonTree", json.toString());
        }

        return new TemplateResponseValues(TEMPLATE_NAME, body);
    }

}
