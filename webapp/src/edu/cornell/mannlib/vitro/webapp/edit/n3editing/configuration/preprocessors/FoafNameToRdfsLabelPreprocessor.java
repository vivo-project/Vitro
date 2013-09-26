/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.preprocessors;

import javax.servlet.http.HttpServletRequest;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

public class FoafNameToRdfsLabelPreprocessor implements ModelChangePreprocessor {

    private static final String FOAF = "http://xmlns.com/foaf/0.1/";

	@Override
	public void preprocess(Model retractionsModel, Model additionsModel,
			HttpServletRequest request) {
		Property firstNameP = additionsModel.getProperty(FOAF+"firstName");
		Property lastNameP = additionsModel.getProperty(FOAF+"lastName");
		//middle name is optional
		Property middleNameP = additionsModel.getProperty(FOAF+"middleName");

		Property rdfsLabelP = additionsModel.getProperty(VitroVocabulary.LABEL);
		
		ResIterator subs = 
			additionsModel.listSubjectsWithProperty(firstNameP);
		while( subs.hasNext() ){
			Resource sub = subs.nextResource();
			Statement fname = sub.getProperty( firstNameP );
			Statement lname = sub.getProperty( lastNameP );
			Statement mname = sub.getProperty(middleNameP);
			if( fname != null && lname != null && fname.getString() != null && lname.getString() != null ){
				//Check if there are languages associated with first name and last name and add the language
				//attribute to the label
				//This preprocessor is used in multiple places, including for managing labels
				Literal firstNameLiteral = fname.getLiteral();
				Literal lastNameLiteral = lname.getLiteral();
				
				String firstNameLanguage = firstNameLiteral.getLanguage();
				String lastNameLanguage = lastNameLiteral.getLanguage();
				//Start creating string for new label
				String newLabel = lname.getString() + ", " + fname.getString();

				//Middle name handling
				if(mname != null && mname.getString() != null) {
					newLabel += " " + mname.getString();
				} 
				
				if(firstNameLanguage != null && lastNameLanguage != null && firstNameLanguage.equals(lastNameLanguage)) {
					//create a literal with the appropriate value and the language
					Literal labelWithLanguage = additionsModel.createLiteral(newLabel, firstNameLanguage);
					additionsModel.add(sub, rdfsLabelP, labelWithLanguage);
				} else {
					additionsModel.add(sub, rdfsLabelP, newLabel );
				}
			}
		}
		
	}

}
