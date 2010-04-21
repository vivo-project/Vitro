/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller; 

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.template.freemarker.FreeMarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.beans.TestBean;

import freemarker.template.SimpleDate;
import freemarker.template.TemplateDateModel;

public class AboutControllerFreeMarker extends FreeMarkerHttpServlet {
	
	private static final Log log = LogFactory.getLog(AboutControllerFreeMarker.class.getName());
    
    protected String getTitle() {
    	return "About " + portal.getAppName();
    }
    
    protected String getBody() {
        
    	Map body = new HashMap();
    	
    	/* For testing equivalences of Java Strings and FreeMarker SimpleScalar in the template
    	String about = portal.getAboutText();
    	SimpleScalar aboutSS = new SimpleScalar(about);
    	String aboutStr = aboutSS.getAsString();
    	String aboutStr2 = aboutSS.toString();
    	body.put("aboutSS", aboutSS);
    	body.put("aboutStr", aboutStr);
    	body.put("aboutStr2", aboutStr2);
    	*/
    	
    	Calendar date = Calendar.getInstance();
    	Date d1 = date.getTime();
    	SimpleDate d = new SimpleDate(d1, TemplateDateModel.DATE);
    	body.put("date", d);

        Calendar datetime = Calendar.getInstance();
        Date d2 = datetime.getTime();
    	SimpleDate dt = new SimpleDate(d2, TemplateDateModel.DATETIME);
        body.put("datetime", dt); 	
    
        body.put("aboutText", portal.getAboutText());
        body.put("acknowledgeText", portal.getAcknowledgeText()); 
        
        String templateName = "about.ftl";             
        return mergeBodyToTemplate(templateName, body);

    }


}
