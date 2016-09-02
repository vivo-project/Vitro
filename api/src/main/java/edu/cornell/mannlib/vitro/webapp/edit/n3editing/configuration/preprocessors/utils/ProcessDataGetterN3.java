/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.preprocessors.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;
import javax.servlet.ServletContext;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Literal;

import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.FieldVTwo;

//Returns the appropriate n3 based on data getter

public interface ProcessDataGetterN3 {
	public String getClassType();
	public List<String> retrieveN3Required(int counter);
    public List<String> retrieveN3Optional(int counter);
    public List<String >retrieveLiteralsOnForm(int counter);
    
     
    public List<String> retrieveUrisOnForm(int counter);
    public List<FieldVTwo> retrieveFields(int counter);
    public List<String> getLiteralVarNamesBase();
    public List<String> getUriVarNamesBase();
    public String getVarName(String base, int counter);
    public String getDataGetterVar(int counter);
    public String getDataGetterVarName(int counter);
    public List<String> getNewResources(int counter);
    
    //Get Existing values to put in scope
    public Map<String, List<Literal>> retrieveExistingLiteralValues();
    public Map<String, List<String>> retrieveExistingUriValues();
    public void populateExistingValues(String dataGetterURI, int counter, OntModel queryModel);
    public JSONObject getExistingValuesJSON(String dataGetterURI, OntModel queryModel, ServletContext context);
    public String replaceEncodedQuotesWithEscapedQuotes(String inputStr);

}
