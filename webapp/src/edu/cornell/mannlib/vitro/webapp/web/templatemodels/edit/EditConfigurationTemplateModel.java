/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.edit;

import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationUtils;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.SelectListGeneratorVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators.DefaultObjectPropertyFormGenerator;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.Literal;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;

public class EditConfigurationTemplateModel extends BaseTemplateModel {
    EditConfigurationVTwo editConfig;
    HashMap<String, Object> pageData = new HashMap<String, Object>();
    VitroRequest vreq;
	private Log log = LogFactory.getLog(EditConfigurationTemplateModel.class);

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
    //Calculate data here
    private void retrieveEditData() {
    	//Get vitro request attributes for
    	setFormTitle();
    	setSubmitLabel();
    	//this should only be called if this is an object property form
    	//how would we do this?
    	if(EditConfigurationUtils.isObjectProperty(editConfig.getPredicateUri(), vreq)) {
    		setRangeOptions();
    	}
    	//range data type probably not set here but for edit configuration's field
    	/*
    	if(EditConfigurationUtils.isDataProperty(editConfig.getPredicateUri(), vreq)) {
    		setRangeDatatype();
    	}*/
    	
    	
    }
    //TODO: remove
    private void setRangeDatatype() {

	}

	private boolean isRangeOptionsExist() {
    	boolean rangeOptionsExist = (pageData.get("rangeOptionsExist") != null && (Boolean) pageData.get("rangeOptionsExist")  == true);
    	return rangeOptionsExist;
    }
    
	private void setFormTitle() {
		if(editConfig.isObjectResource()) {
			setObjectFormTitle();
		} else {
			setDataFormTitle();
		}
	}
	
    private void setDataFormTitle() {
		String formTitle = null;
		String datapropKeyStr = editConfig.getDatapropKey();
		DataProperty  prop = EditConfigurationUtils.getDataProperty(vreq);
		if( datapropKeyStr != null && datapropKeyStr.trim().length() > 0  ) {
	        formTitle   = "Change text for: <em>"+prop.getPublicName()+"</em>";
	        
	    } else {
	        formTitle   ="Add new entry for: <em>"+prop.getPublicName()+"</em>";
	    }
		pageData.put("formTitle", formTitle);
	}

	//Process and set data
    private void setObjectFormTitle() {
    	String formTitle = null;
    	Individual objectIndividual = EditConfigurationUtils.getObjectIndividual(vreq);
    	ObjectProperty prop = EditConfigurationUtils.getObjectProperty(vreq);
    	Individual subject = EditConfigurationUtils.getSubjectIndividual(vreq);
    	if(objectIndividual != null) {
    		formTitle = "Change entry for: <em>" + prop.getDomainPublic() + " </em>";
    	}  else {
    		WebappDaoFactory wdf = vreq.getWebappDaoFactory();
            if ( prop.getOfferCreateNewOption() ) {
            	//Try to get the name of the class to select from
           	  	VClass classOfObjectFillers = null;
        
    		    if( prop.getRangeVClassURI() == null ) {    	
    		    	// If property has no explicit range, try to get classes 
    		    	List<VClass> classes = wdf.getVClassDao().getVClassesForProperty(subject.getVClassURI(), prop.getURI());
    		    	if( classes == null || classes.size() == 0 || classes.get(0) == null ){	    	
    			    	// If property has no explicit range, we will use e.g. owl:Thing.
    			    	// Typically an allValuesFrom restriction will come into play later.	    	
    			    	classOfObjectFillers = wdf.getVClassDao().getTopConcept();	    	
    		    	} else {
    		    		if( classes.size() > 1 )
    		    			log.debug("Found multiple classes when attempting to get range vclass.");
    		    		classOfObjectFillers = classes.get(0);
    		    	}
    		    }else{
    		    	classOfObjectFillers = wdf.getVClassDao().getVClassByURI(prop.getRangeVClassURI());
    		    	if( classOfObjectFillers == null )
    		    		classOfObjectFillers = wdf.getVClassDao().getTopConcept();
    		    }
                log.debug("property set to offer \"create new\" option; custom form: ["+prop.getCustomEntryForm()+"]");
                formTitle   = "Select an existing "+classOfObjectFillers.getName()+" for "+subject.getName();
               
            } else {
                formTitle   = "Add an entry to: <em>"+prop.getDomainPublic()+"</em>";
            }
        }
    	pageData.put("formTitle", formTitle);
    }
    
    private void setSubmitLabel() {
    	String submitLabel = null;
    	Individual objectIndividual = EditConfigurationUtils.getObjectIndividual(vreq);
    	ObjectProperty prop = EditConfigurationUtils.getObjectProperty(vreq);
    	
    	if(objectIndividual != null) {
    		submitLabel = "save change";
    	}  else {
            if ( prop.getOfferCreateNewOption() ) {
                submitLabel = "select existing";
            } else {
                submitLabel = "save entry";
            }
        }
    	pageData.put("submitLabel", submitLabel);
    }
    
    private void setRangeOptions() {
    	ObjectProperty prop = EditConfigurationUtils.getObjectProperty(vreq);
    	if( prop.getSelectFromExisting() ){
    		WebappDaoFactory wdf = vreq.getWebappDaoFactory();

        	// set ProhibitedFromSearch object so picklist doesn't show
            // individuals from classes that should be hidden from list views
        	//uncomment out when we know what to do here
           /*
        	OntModel displayOntModel = 
                (OntModel) session.getServletContext()
                    .getAttribute("displayOntModel");
            if (displayOntModel != null) {
                ProhibitedFromSearch pfs = new ProhibitedFromSearch(
                    DisplayVocabulary.PRIMARY_SEARCH_INDEX_URI, displayOntModel);
                if( editConfiguration != null )
                    editConfiguration.setProhibitedFromSearch(pfs);
            }*/
        	Map<String,String> rangeOptions = SelectListGeneratorVTwo.getOptions(editConfig, "objectVar" , wdf);    	
        	if( rangeOptions != null && rangeOptions.size() > 0 ) {
        		pageData.put("rangeOptionsExist", true);
        	    pageData.put("rangeOptions", rangeOptions);
        	} else { 
        		pageData.put("rangeOptionsExist",false);
        	}
        }
    	
    }
    
    
    //Get page data
   public boolean getRangeOptionsExist() {
	   return isRangeOptionsExist();
   }
    
    public String getFormTitle() {
    	return (String) pageData.get("formTitle");
    }
    
    public String getSubmitLabel() {
    	return (String) pageData.get("submitLabel");
    }
    
    public Map<String, String> getRangeOptions() {
    	Map<String, String> rangeOptions = (Map<String, String>) pageData.get("rangeOptions");
    	return rangeOptions;
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
    
    public String getDataLiteralValuesAsString() {
    	List<String> values = getDataLiteralValues();
    	return StringUtils.join(values, ",");
    }
    public List<String> getDataLiteralValues() {
    	//this is the name of the text element/i.e. variable name of data value by which literal stored
    	String dataLiteral = getDataLiteral();
    	List<String> literalValues = getLiteralStringValue(dataLiteral);
    	return literalValues;
    }
    
    private String literalToString(Literal lit){
        if( lit == null || lit.getValue() == null) return "";
        String value = lit.getValue().toString();
        if( "http://www.w3.org/2001/XMLSchema#anyURI".equals( lit.getDatatypeURI() )){
            //strings from anyURI will be URLEncoded from the literal.
            try{
                value = URLDecoder.decode(value, "UTF8");
            }catch(UnsupportedEncodingException ex){
                log.error(ex);
            }
        }
        return value;
}
    
    //Get predicate
    //What if this is a data property instead?
    public Property getPredicateProperty() {
    	String predicateUri = getPredicateUri();
    	//If predicate uri corresponds to object property, return that
    	if(EditConfigurationUtils.isObjectProperty(predicateUri, vreq)){
    		return this.vreq.getWebappDaoFactory().getObjectPropertyDao().getObjectPropertyByURI(predicateUri);
    	}
		//otherwise return Data property
    	return this.vreq.getWebappDaoFactory().getDataPropertyDao().getDataPropertyByURI(predicateUri);
    }
    
    public ObjectProperty getObjectPredicateProperty() {
    	return this.vreq.getWebappDaoFactory().getObjectPropertyDao().getObjectPropertyByURI(getPredicateUri());
    }
    
    public DataProperty getDataPredicateProperty() {
    	return this.vreq.getWebappDaoFactory().getDataPropertyDao().getDataPropertyByURI(getPredicateUri());

    }
    
    public String getPredicateUri() {
    	return editConfig.getPredicateUri();
    }
    
    public String getSubjectUri() {
    	return editConfig.getSubjectUri();
    }
    
    public String getObjectUri() {
    	return editConfig.getObject();
    }
    
    //data literal
    public String getDataLiteral() {
    	return getDataPredicateProperty().getLocalName() + "Edited";
    }
    
    //public description only appears visible for object property
    public String getPropertyPublicDescription() {
    	return getObjectPredicateProperty().getPublicDescription();
    }
    
    public boolean getPropertySelectFromExisting() {
    	return getObjectPredicateProperty().getSelectFromExisting();
    }
    
    //booleans for checking whether predicate is data or object
    public boolean isDataProperty() {
    	return EditConfigurationUtils.isDataProperty(getPredicateUri(), vreq);
    }
    public boolean isObjectProperty() {
    	return EditConfigurationUtils.isObjectProperty(getPredicateUri(), vreq);
    }
    
    //Additional methods that were originally in edit request dispatch controller
    //to be employed here instead
    
    public String getUrlToReturnTo() {
    	return vreq
        .getParameter("urlPattern") == null ? "/entity" : vreq
                .getParameter("urlPattern");
    }
    
    public String getCurrentUrl() {
    	return "/edit/editRequestDispatch?" + vreq.getQueryString();
    }
    
}
