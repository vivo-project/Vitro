/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyGroup;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyGroupDao;
import edu.cornell.mannlib.vitro.webapp.utils.json.JacksonUtils;

import javax.servlet.annotation.WebServlet;

@WebServlet(name = "ListPropertyGroupsController", urlPatterns = {"/listPropertyGroups"} )
public class ListPropertyGroupsController extends FreemarkerHttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(ListPropertyGroupsController.class);
    private static final boolean WITH_PROPERTIES = true;
    private static final String TEMPLATE_NAME = "siteAdmin-objectPropHierarchy.ftl";

    @Override
	protected AuthorizationRequest requiredActions(VitroRequest vreq) {
		return SimplePermission.EDIT_ONTOLOGY.ACTION;
	}

    @Override
	protected ResponseValues processRequest(VitroRequest vreq) {

        Map<String, Object> body = new HashMap<String, Object>();
        try {

            body.put("displayOption", "group");
            body.put("pageTitle", "Property Groups");

            PropertyGroupDao dao = vreq.getUnfilteredWebappDaoFactory().getPropertyGroupDao();

            List<PropertyGroup> groups = dao.getPublicGroups(WITH_PROPERTIES);
                StringBuilder json = new StringBuilder();
                int counter = 0;

                if (groups != null) {
                	for(PropertyGroup pg: groups) {
                        if ( counter > 0 ) {
                            json.append(", ");
                        }
                        String publicName = pg.getName();
                        if ( StringUtils.isBlank(publicName) ) {
                            publicName = "(unnamed group)";
                        }
                        try {
                            json.append("{ \"name\": ").append(JacksonUtils.quote("<a href='./editForm?uri=" + URLEncoder.encode(pg.getURI(), "UTF-8") + "&amp;controller=PropertyGroup'>" + publicName + "</a>")).append(", ");
                        } catch (Exception e) {
                            json.append("{ \"name\": ").append(JacksonUtils.quote(publicName)).append(", ");
                        }
                        Integer t;

                        json.append("\"data\": { \"displayRank\": \"").append(((t = Integer.valueOf(pg.getDisplayRank())) != -1) ? t.toString() : "").append("\"}, ");

                        List<Property> propertyList = pg.getPropertyList();
                        if (propertyList != null && propertyList.size()>0) {
                            json.append("\"children\": [");
                            Iterator<Property> propIt = propertyList.iterator();
                            while (propIt.hasNext()) {
                                Property prop = propIt.next();
                                String controllerStr = "propertyEdit";
                                String nameStr =
                                	   (prop.getLabel() == null)
                                	           ? ""
                                	           : prop.getLabel();
                                if (prop instanceof ObjectProperty) {
                                	nameStr = ((ObjectProperty) prop).getDomainPublic();
                                } else if (prop instanceof DataProperty) {
                                	controllerStr = "datapropEdit";
                                	nameStr = ((DataProperty) prop).getName();
                                }
                                if (prop.getURI() != null) {
                                    try {
                                        json.append("{ \"name\": ").append(JacksonUtils.quote("<a href='" + controllerStr
                                                + "?uri=" + URLEncoder.encode(prop.getURI(), "UTF-8") + "'>" + nameStr + "</a>")).append(", ");
                                    } catch (Exception e) {
                                        json.append(JacksonUtils.quote(nameStr)).append(", ");
                                    }
                                } else {
                                    json.append("\"\", ");
                                }

                                json.append("\"data\": { \"shortDef\": \"\"}, \"children\": [] ");
                                if (propIt.hasNext())
                                    json.append("} , ");
                                else
                                    json.append("}] ");
                            }
                        }
                        else {
                            json.append("\"children\": [] ");
                        }
                        json.append("} ");
                        counter += 1;
                    }
                }

                body.put("jsonTree", json.toString());
                log.debug("json = " + json);
            } catch (Throwable t) {
                    t.printStackTrace();
            }

            return new TemplateResponseValues(TEMPLATE_NAME, body);
        }

}
