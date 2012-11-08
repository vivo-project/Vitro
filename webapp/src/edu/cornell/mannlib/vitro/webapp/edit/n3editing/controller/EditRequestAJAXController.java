/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.controller;

import static edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationUtils.getPredicateUri;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.ParamMap;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.DirectRedirectResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.json.GetDataForPage;
import edu.cornell.mannlib.vitro.webapp.controller.json.GetEntitiesByVClass;
import edu.cornell.mannlib.vitro.webapp.controller.json.GetEntitiesByVClassContinuation;
import edu.cornell.mannlib.vitro.webapp.controller.json.GetRenderedSolrIndividualsByVClass;
import edu.cornell.mannlib.vitro.webapp.controller.json.GetSolrIndividualsByVClass;
import edu.cornell.mannlib.vitro.webapp.controller.json.GetSolrIndividualsByVClasses;
import edu.cornell.mannlib.vitro.webapp.controller.json.GetVClassesForVClassGroup;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationUtils;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditSubmissionUtils;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.MultiValueEditSubmission;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators.EditConfigurationAJAXGenerator;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators.EditConfigurationGenerator;
import edu.cornell.mannlib.vitro.webapp.utils.log.LogUtils;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.edit.EditConfigurationTemplateModel;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.edit.MultiValueEditSubmissionTemplateModel;

/**
 * This servlet is intended to handle all requests to create a form for use
 * by the N3 editing system.  It will examine the request parameters, determine
 * which form to use, execute a EditConfiguration setup, and evaluate the
 * view indicated by the EditConfiguration.
 * 
 * Do not add code to this class to achieve some behavior in a 
 * form.  Try adding the behavior logic to the code that generates the
 * EditConfiguration for the form.  
 */
public class EditRequestAJAXController extends VitroHttpServlet {
    private static final long serialVersionUID = 1L;
    public static Log log = LogFactory.getLog(EditRequestDispatchController.class);
  
   
	protected Actions requiredActions(VitroRequest vreq) {
    	return SimplePermission.DO_FRONT_END_EDITING.ACTIONS;
	}

    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);
        log.debug(LogUtils.formatRequestProperties(log, "debug", req));

        VitroRequest vreq = new VitroRequest(req);
        try {
	        //Get edit configuration object based on editk key in request
	        EditConfigurationVTwo config = getEditConfiguration(vreq);
	        //Get the generator name also from the request parameter
	        String generatorName = vreq.getParameter("generator");
	        String javaGeneratorName = "edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators." + generatorName;
	        //TODO: Add this method to generators, but for now constructing a new class of generator specifically for AJAX requests
	        EditConfigurationAJAXGenerator generator = getAJAXGenerator(javaGeneratorName, vreq, vreq.getSession());
	        //run the modification
	        generator.modifyEditConfiguration(config, vreq);
        } catch(Exception ex) {
        	log.error("An error occurred in retrieving configuration and/or generator ", ex);
        }
        
    }
    
    protected EditConfigurationVTwo getEditConfiguration(VitroRequest vreq) {
    	
    	EditConfigurationVTwo config = EditConfigurationVTwo.getConfigFromSession(vreq.getSession(), vreq);
    	return config;
    }
    
    private EditConfigurationAJAXGenerator getAJAXGenerator(
            String editConfGeneratorName, VitroRequest vreq, HttpSession session) throws Exception {
    	
    	EditConfigurationAJAXGenerator EditConfigurationVTwoGenerator = null;
    	
        Object object = null;
        try {
            Class classDefinition = Class.forName(editConfGeneratorName);
            object = classDefinition.newInstance();
            EditConfigurationVTwoGenerator = (EditConfigurationAJAXGenerator) object;
        } catch (InstantiationException e) {
            System.out.println(e);
        } catch (IllegalAccessException e) {
            System.out.println(e);
        } catch (ClassNotFoundException e) {
            System.out.println(e);
        }    	
        
        if(EditConfigurationVTwoGenerator == null){
        	throw new Error("Could not find EditConfigurationVTwoGenerator " + editConfGeneratorName);        	
        } else {
           return EditConfigurationVTwoGenerator;
        }
        
    }

    
}
