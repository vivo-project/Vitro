/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A place for storing test cases.
 * @author rjy7
 *
 */
public class TestController extends FreeMarkerHttpServlet {

    private static final long serialVersionUID = 1L;

    protected String getTitle() {
        return "Test";
    }
    
    protected String getBody() {
        
        Map<String, Object> body = new HashMap<String, Object>();
        
        // Test of #list directive in template on undefined, null, and empty values.
        // Basic idea: empty list okay, null or undefined value not okay.
        List<String> apples = new ArrayList<String>();  // no error
        // List<String> apples = null; // error
        body.put("apples", apples); // without this: error        
        
        Calendar cal = Calendar.getInstance();
        Date now = cal.getTime();
        body.put("now", now);
        // In template: ${now?date}, ${now?datetime}, ${now?time}
        
        // You can add to a collection AFTER putting it in the template data model
        List<String> fruit = new ArrayList<String>();
        fruit.add("apples");
        fruit.add("bananas");
        body.put("fruit", fruit);
        fruit.add("oranges");
        
        // But you cannot modify a scalar after putting it in the data model - the
        // template still gets the old value
        String animal = "elephant";
        body.put("animal", animal);
        animal = "camel";
   
        // Create the template to see the examples live.
        String bodyTemplate = "test.ftl";             
        return mergeBodyToTemplate(bodyTemplate, body);

    }

}

