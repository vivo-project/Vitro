/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.edit;

import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import com.hp.hpl.jena.rdf.model.Literal;


public class EditConfigurationTemplateModel extends BaseTemplateModel {
    EditConfigurationVTwo editConfig;
    HashMap<String, Object> pageData = new HashMap<String, Object>();
    VitroRequest vreq;
    
    public EditConfigurationTemplateModel( EditConfigurationVTwo editConfig, VitroRequest vreq){
        this.editConfig = editConfig;
        this.vreq = vreq;
        //get additional data that may be required to generate template
        this.retrieveEditData();
    }
    
    public String getEditKey(){
        return editConfig.getEditKey();
    }
    
    public boolean isUpdate(){
        return editConfig.isUpdate();
    }
    
    public String getSubmitToUrl(){
        return  getUrl( editConfig.getSubmitToUrl() );
    }
    //TODO: Check whether to include additoinal data here or elsewhere
  //For now, using attributes off of vitro request to add to template
    //TODO: find better mechanism
    private void retrieveEditData() {
    	//Get vitro request attributes for
    	pageData.put("formTitle", (String) vreq.getAttribute("formTitle"));
    	pageData.put("submitLabel", (String) vreq.getAttribute("submitLabel"));

    	if(vreq.getAttribute("rangeOptionsExist") != null && (Boolean) vreq.getAttribute("rangeOptionsExist")  == true) {
    		Map<String,String> rangeOptions = (Map<String,String>)vreq.getAttribute("rangeOptions.objectVar");
    		pageData.put("rangeOptions", rangeOptions);
    	}
    	
    	
    }
    
    //Get page data
    public String getFormTitle() {
    	return (String) pageData.get("formTitle");
    }
    
    public String getSubmitLabel() {
    	return (String) pageData.get("submitLabel");
    }
    
    public Map<String, String> getRangeOptions() {
    	return (Map<String, String>) pageData.get("rangeOptions");
    }
    
    //Get literals in scope, i.e. variable names with values assigned
    public Map<String, List<Literal>> getLiteralValues() {
    	return editConfig.getLiteralsInScope();
    }
    
    //variables names with URIs assigned
    public Map<String, List<String>> getObjectUris() {
    	return editConfig.getUrisInScope();
    }
    
    public List<String> getLiteralStringValue(String key) {
    	List<Literal> ls = editConfig.getLiteralsInScope().get(key);
    	List<String> literalValues = new ArrayList<String>();
    	for(Literal l: ls) {
    		literalValues.add(l.getString());
    	}
    	return literalValues;
    }
}
