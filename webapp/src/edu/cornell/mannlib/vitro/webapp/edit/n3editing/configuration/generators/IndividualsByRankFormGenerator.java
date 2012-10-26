/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators;

import static edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationUtils;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.FieldVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.IndividualsViaObjectPropertyByRankOptions;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.IndividualsViaObjectPropetyOptions;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.validators.AntiXssValidation;
import edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames;
import edu.cornell.mannlib.vitro.webapp.search.beans.ProhibitedFromSearch;
import edu.cornell.mannlib.vitro.webapp.search.solr.SolrSetup;
import edu.cornell.mannlib.vitro.webapp.utils.FrontEndEditingUtils;
import edu.cornell.mannlib.vitro.webapp.utils.FrontEndEditingUtils.EditMode;

/**
 * Generates the edit configuration for a default property form.
 * This handles the default object property auto complete.
 * 
 * If a default property form is request and the number of individuals
 * found in the range is too large, the the auto complete setup and
 * template will be used instead.
 */
public class IndividualsByRankFormGenerator extends DefaultObjectPropertyFormGenerator implements EditConfigurationGenerator {

	private Log log = LogFactory.getLog(IndividualsByRankFormGenerator.class);	
	
	/*
	 *   (non-Javadoc)
	 * @see edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators.DefaultObjectPropertyFormGenerator#setFields(edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo, edu.cornell.mannlib.vitro.webapp.controller.VitroRequest, java.lang.String)
	 *
	 * Updates here include using different field options that enable sorting of individuals for the property by display rank
	 */
   @Override    
    protected void setFields(EditConfigurationVTwo editConfiguration, VitroRequest vreq, String predicateUri) throws Exception {    	
		FieldVTwo field = new FieldVTwo();
    	field.setName("objectVar");    	
    	
    	List<String> validators = new ArrayList<String>();
    	validators.add("nonempty");
    	field.setValidators(validators);
    	    	
    	if( ! doAutoComplete ){
    		field.setOptions( new IndividualsViaObjectPropertyByRankOptions(
    	        super.getSubjectUri(),
    	        super.getPredicateUri(), 
    	        super.getObjectUri(),
    	        vreq.getWebappDaoFactory(), 
    	        vreq.getJenaOntModel()));
    	}else{
    		field.setOptions(null);
    	}
    	
    	Map<String, FieldVTwo> fields = new HashMap<String, FieldVTwo>();
    	fields.put(field.getName(), field);    	
    	    	    	
    	editConfiguration.setFields(fields);
    }       
}
