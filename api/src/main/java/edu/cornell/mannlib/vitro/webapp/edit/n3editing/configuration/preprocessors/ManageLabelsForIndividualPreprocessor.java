/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.preprocessors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
/*
 * This preprocessor is used to set the language attribute on the label based on the user selection
 * on the manage labels page when adding a new label.
 */
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.BaseEditSubmissionPreprocessorVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.MultiValueEditSubmission;

public class ManageLabelsForIndividualPreprocessor extends BaseEditSubmissionPreprocessorVTwo {

	

	
	public ManageLabelsForIndividualPreprocessor(EditConfigurationVTwo editConfig) {
		super(editConfig);
		
	}
	
	@Override
	public void preprocess(MultiValueEditSubmission inputSubmission, VitroRequest vreq) {
		//Check and see if a language was selected by the user, and this is the regular label submission
		//TODO: Check if firstname and lastname should be changed here or elsewhere
		if(inputSubmission.hasLiteralValue("label") && inputSubmission.hasLiteralValue("newLabelLanguage")) {
			Map<String, List<Literal>> literalsFromForm = inputSubmission.getLiteralsFromForm();
			List<Literal> newLabelLanguages = literalsFromForm.get("newLabelLanguage");
			List<Literal> labels = literalsFromForm.get("label");

			//Expecting only one language
			if(labels.size() > 0 && newLabelLanguages.size() > 0) {
				Literal newLabelLanguage = newLabelLanguages.get(0);
				Literal labelLiteral = labels.get(0);
				//Get the string
				String lang = this.getLanguage(newLabelLanguage.getString());
				String label = labelLiteral.getString();
				//Now add the language category to the literal
				Literal labelWithLanguage = inputSubmission.createLiteral(label, 
						newLabelLanguage.getDatatypeURI(), 
						lang);
				labels = new ArrayList<Literal>();
				labels.add(labelWithLanguage);
				//replace the label with one with language, again assuming only one label being returned
				literalsFromForm.put("label", labels);
				inputSubmission.setLiteralsFromForm(literalsFromForm);
			}
		}
		
	}
	
	//The language code returned from JAVA locales has an underscore whereas we need a hyphen
	protected String getLanguage(String inputLanguageCode) {
		if(inputLanguageCode.contains("_")) {
			return inputLanguageCode.replace("_", "-");
		}
		return inputLanguageCode;
	}

}
