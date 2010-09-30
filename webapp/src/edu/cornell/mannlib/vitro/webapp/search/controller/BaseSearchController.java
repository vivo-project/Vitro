/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;

public class BaseSearchController extends FreemarkerHttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(AutocompleteController.class);
    
    protected String cleanQueryString(String querystr) {
        log.debug("Query string is '" + querystr + "'");

        // Prevent org.apache.lucene.queryParser.ParseException: 
        // Cannot parse 'mary *': '*' or '?' not allowed as first character in WildcardQuery     
        // The * is redundant in this case anyway, so just remove it.
        querystr = querystr.replaceAll("([\\s^])[?*]", "$1");
        
        log.debug("Cleaned query string is '" + querystr + "'");           
        return querystr;
    }
}
