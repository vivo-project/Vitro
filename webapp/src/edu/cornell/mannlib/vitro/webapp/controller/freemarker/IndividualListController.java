/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.utils.StringUtils;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.IndividualTemplateModel;
import freemarker.template.Configuration;

/** 
 * Generates a list of individuals for display in a template 
 */
public class IndividualListController extends FreemarkerHttpServlet {
  
    private static final long serialVersionUID = 1L;   
    private static final Log log = LogFactory.getLog(IndividualListController.class.getName());

    private static final String TEMPLATE_DEFAULT = "individualList.ftl";

    @Override
    protected ResponseValues processRequest(VitroRequest vreq) {
 
        String templateName = TEMPLATE_DEFAULT;
        Map<String, Object> body = new HashMap<String, Object>();
        String errorMessage = null;
        String message = null;
        
        try {
            Object obj = vreq.getAttribute("vclass");
            VClass vclass = null;
            if ( obj == null ) { // look for vitroclass id parameter
                String vitroClassIdStr = vreq.getParameter("vclassId");
                if ( !StringUtils.isEmpty(vitroClassIdStr)) { 
                    try {
                        //TODO have to change this so vclass's group and entity count are populated
                        vclass = vreq.getWebappDaoFactory().getVClassDao().getVClassByURI(vitroClassIdStr);
                        if (vclass == null) {
                            log.error("Couldn't retrieve vclass " + vitroClassIdStr);   
                            errorMessage = "Class " + vitroClassIdStr + " not found";
                        }
                    } catch (Exception ex) {
                        throw new HelpException("IndividualListController: request parameter 'vclassId' must be a URI string.");
                    }
                }
            } else if (obj instanceof VClass) {
                vclass = (VClass)obj;
            } else {
                throw new HelpException("IndividualListController: attribute 'vclass' must be of type "
                        + VClass.class.getName() + ".");
            }
            
            if (vclass != null) {
                // Create list of individual view objects
                List<Individual> individualList = vreq.getWebappDaoFactory().getIndividualDao().getIndividualsByVClass(vclass);
                List<IndividualTemplateModel> individuals = new ArrayList<IndividualTemplateModel>(individualList.size());

                if (individualList == null) {
                    // RY Is this really an error? 
                    log.error("individuals list is null");
                    message = "No individuals to display.";
                } else {            
                    for (Individual i: individualList) {
                        individuals.add(new IndividualTemplateModel(i));
                    }                   
                }

                // Set title and subtitle. Title will be retrieved later in getTitle().   
                VClassGroup classGroup = vclass.getGroup();  
                String title;
                if (classGroup == null) {
                    title = vclass.getName();
                } else {
                    title = classGroup.getPublicName();
                    body.put("subtitle", vclass.getName());
                }
                body.put("title", title);
                
                body.put("individuals", individuals);
            }   
            
        } catch (HelpException help){
            errorMessage = "Request attribute 'vclass' or request parameter 'vclassId' must be set before calling. Its value must be a class uri."; 
        } catch (Throwable e) {
            return new ExceptionResponseValues(e);
        }

        if (errorMessage != null) {
            templateName = Template.ERROR_MESSAGE.toString();
            body.put("errorMessage", errorMessage);
        } else if (message != null) {
            body.put("message", message);
        }
    
        return new TemplateResponseValues(templateName, body);
    }
      
    private class HelpException extends Throwable {
        private static final long serialVersionUID = 1L;

        public HelpException(String string) {
            super(string);
        }
    }
}
