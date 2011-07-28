/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.preprocessors;

import javax.servlet.http.HttpServletRequest;

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
		Property rdfsLabelP = additionsModel.getProperty(VitroVocabulary.LABEL);
		
		ResIterator subs = 
			additionsModel.listSubjectsWithProperty(firstNameP);
		while( subs.hasNext() ){
			Resource sub = subs.nextResource();
			Statement fname = sub.getProperty( firstNameP );
			Statement lname = sub.getProperty( lastNameP );
			if( fname != null && lname != null && fname.getString() != null && lname.getString() != null ){
				additionsModel.add(sub, rdfsLabelP, lname.getString() + ", " + fname.getString() );
			}
		}
		
	}

}
