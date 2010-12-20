/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import freemarker.template.Configuration;

/**
 * Freemarker controller and template sandbox.
 * @author rjy7
 *
 */
public class TestController extends FreemarkerHttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(TestController.class);
    private static final String TEMPLATE_DEFAULT = "test.ftl";

    @Override
    protected ResponseValues processRequest(VitroRequest vreq) {
        //Portal portal = vreq.getPortal();
        
        Map<String, Object> body = new HashMap<String, Object>();
        
        body.put("title", "Freemarker Test");

        Calendar cal = Calendar.getInstance();
        Date now = cal.getTime();
        body.put("now", now);
        // In template: ${now?date}, ${now?datetime}, ${now?time}
        
        body.put("datetime", "1983-12-07T17:15:28Z");
        
        return new TemplateResponseValues(TEMPLATE_DEFAULT, body);
        
    }
    
    @Override
    protected String getTitle(String siteName) {
        return "Test";
    }

}

