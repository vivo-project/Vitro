/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.widgets;

import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import freemarker.core.Environment;

/**
 * This widget is intended to assist in dealing with xsd:dateTime values from
 * Literals.
 * 
 *  It is intended to be used like this:
 *  
 *  <@widget name="DateTime" dateTime="2010-11-23T11:03:23" >
 *    The year is ${year}
 *    The month is ${month}
 *    The day is ${day}
 *    The hour is ${hour}
 *    The second is ${second}
 *    precision of date entered: ${precisionUri}    
 *  </@widget>
 *  
 * @author bdc34
 *
 */
public class DateTimeWidget extends Widget {

    @Override
    protected WidgetTemplateValues process(Environment env, Map params,
            HttpServletRequest request, ServletContext context)
            throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

}
