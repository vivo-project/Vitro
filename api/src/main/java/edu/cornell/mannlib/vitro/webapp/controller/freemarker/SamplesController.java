/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;

/**
 * Freemarker controller and template samples.
 * @author rjy7
 *
 */
public class SamplesController extends FreemarkerHttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(SamplesController.class);
    private static final String TEMPLATE_DEFAULT = "samples.ftl";

    @Override
    protected ResponseValues processRequest(VitroRequest vreq) {
        
        Map<String, Object> body = new HashMap<String, Object>();
        // Test of #list directive in template on undefined, null, and empty values.
        // Basic idea: empty list okay, null or undefined value not okay.
        List<String> apples = new ArrayList<String>();  // no error
        // List<String> apples = null; // error
        body.put("apples", apples); // without this: error        
        
        // You can add to a collection AFTER putting it in the template data model.
        // The data model contains a reference to the collection, not a copy.
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
        
        // Because the data model contains a reference to the collection, changing
        // one also changes the other.
        List<String> animals = new ArrayList<String>();
        animals.add("elephant");
        animals.add("tiger");
        Map<String, List> zoo1 = new HashMap<String, List>();
        Map<String, List> zoo2 = new HashMap<String, List>();
        zoo1.put("animals", animals);
        zoo2.put("animals", animals);
        zoo1.get("animals").add("monkey");       
        body.put("zoo1", zoo1);
        body.put("zoo2", zoo2);
        
        // Test recursive dump - array of arrays
//        String[] fruitArray = { "apples", "bananas", "strawberries" };
//        String[] animalArray = { "cat", "dog", "mouse" };
//        String[] dayArray = { "Monday", "Tuesday", "Wednesday" };
//        String[][] arrays = { fruitArray, animalArray, dayArray };
//        body.put("arrays", arrays);
        
        body.put("trueStatement", true);
        body.put("falseStatement", false);
        
        getBerries(body);
        
        body.put("bookTitle", "Pride and Prejudice");
        body.put("bookTitle", "Persuasion");
        
        body.put("year", "2001");
        
        body.put("xsddatetime", "1983-12-07T17:15:28Z");
        
        body.put("title", "Freemarker Samples");
        
        return new TemplateResponseValues(TEMPLATE_DEFAULT, body);
    }
    
    @Override
    protected String getTitle(String siteName, VitroRequest vreq) {
        return "Freemarker Samples";
    }
    
    private void getBerries(Map<String, Object> body) {
        body.put("berries", "strawberries, raspberries, blueberries");
    }

}

