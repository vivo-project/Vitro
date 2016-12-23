/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.json.util.JSONUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.VClassGroupDao;
import edu.cornell.mannlib.vitro.webapp.web.URLEncoder;


public class ListClassGroupsController extends FreemarkerHttpServlet {

    private static final Log log = LogFactory.getLog(ListClassGroupsController.class.getName());
    
    private static final String TEMPLATE_NAME = "siteAdmin-classHierarchy.ftl";
        
    @Override
	protected AuthorizationRequest requiredActions(VitroRequest vreq) {
		return SimplePermission.EDIT_ONTOLOGY.ACTION;
	}
    
    @Override
    protected ResponseValues processRequest(VitroRequest vreq) {

        Map<String, Object> body = new HashMap<String, Object>();
        try {

            body.put("displayOption", "group");
            body.put("pageTitle", "Class Groups");

            VClassGroupDao dao = vreq.getUnfilteredWebappDaoFactory().getVClassGroupDao();

            List<VClassGroup> groups = dao.getPublicGroupsWithVClasses(); 

            String json = new String();
            int counter = 0;

            if (groups != null) {
            	for(VClassGroup vcg: groups) {
                    if ( counter > 0 ) {
                        json += ", ";
                    }
                    String publicName = vcg.getPublicName();
                    if ( StringUtils.isBlank(publicName) ) {
                        publicName = "(unnamed group)";
                    }           
                    try {
                        json += "{ \"name\": " + JSONUtils.quote("<a href='./editForm?uri="+URLEncoder.encode(vcg.getURI())+"&amp;controller=Classgroup'>"+publicName+"</a>") + ", ";
                    } catch (Exception e) {
                        json += "{ \"name\": " + JSONUtils.quote(publicName) + ", ";
                    }
                    Integer t;
                    
                    json += "\"data\": { \"displayRank\": \"" + (((t = Integer.valueOf(vcg.getDisplayRank())) != -1) ? t.toString() : "") + "\"}, ";
                    
                    List<VClass> classList = vcg.getVitroClassList();
                    if (classList != null && classList.size()>0) {
                        json += "\"children\": [";
                        Iterator<VClass> classIt = classList.iterator();
                        while (classIt.hasNext()) {
                            VClass vcw = classIt.next();
                            if (vcw.getName() != null && vcw.getURI() != null) {
                                try {
                                    json += "{ \"name\": " + JSONUtils.quote("<a href='vclassEdit?uri="+URLEncoder.encode(vcw.getURI())+"'>"+vcw.getName()+"</a>") + ", ";
                                } catch (Exception e) {
                                    json += "" + JSONUtils.quote(vcw.getName()) + ", ";
                                }
                            } else {
                                json += "\"\", ";
                            }

                            String shortDefStr = (vcw.getShortDef() == null) ? "" : vcw.getShortDef();
                            json += "\"data\": { \"shortDef\": " + JSONUtils.quote(shortDefStr) + "}, \"children\": [] ";
                            if (classIt.hasNext())
                                json += "} , ";
                            else 
                                json += "}] ";
                        }
                    }
                    else {
                        json += "\"children\": [] ";
                    }
                    json += "} ";
                    counter += 1;
                }
            }

            body.put("jsonTree",json);

        } catch (Throwable t) {
                t.printStackTrace();
        }
        
        return new TemplateResponseValues(TEMPLATE_NAME, body);
    }

}
