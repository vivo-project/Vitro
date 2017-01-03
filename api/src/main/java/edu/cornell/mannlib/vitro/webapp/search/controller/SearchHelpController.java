/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.search.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Enumeration;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.datatypes.RDFDatatype ;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.jena.QueryUtils;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;


public class SearchHelpController extends FreemarkerHttpServlet {

    private static final Log log = LogFactory.getLog(SearchHelpController.class.getName());
    private static final String TEMPLATE_NAME = "search-help.ftl";
    
    @Override
    protected ResponseValues processRequest(VitroRequest vreq) {

        Map<String, Object> body = new HashMap<String, Object>();

        String pointOfOrigin = "helpLink";
        
        body.put("origination", pointOfOrigin);

        return new TemplateResponseValues(TEMPLATE_NAME, body);
    }
  
}


