/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.edit;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.Literal;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.ParamMap;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationUtils;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditElementVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.FieldVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.SelectListGeneratorVTwo;
import edu.cornell.mannlib.vitro.webapp.web.beanswrappers.ReadOnlyBeansWrapper;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual.DataPropertyStatementTemplateModel;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual.EditingPolicyHelper;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual.ObjectPropertyStatementTemplateModel;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual.ObjectPropertyTemplateModel;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual.PropertyStatementTemplateModel;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

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
        return editConfig.isObjectPropertyUpdate();
    }
    
    public String getSubmitToUrl(){
        return  getUrl( editConfig.getSubmitToUrl() );
    }
   
    /*
     * Used to calculate/retrieve/extract additional form-specific data
     * Such as options for a drop-down etc. 
     */
    
    private void retrieveEditData() {
    	//Get vitro request attributes for
    	setFormTitle();
    	setSubmitLabel();
    	
    	//Get the form specific data
    	HashMap<String, Object> formSpecificData = editConfig.getFormSpecificData();
    	if( formSpecificData != null)
    	    pageData.putAll(formSpecificData);
    	populateDropdowns();
    	//populate html with edit element where appropriate
    	populateGeneratedHtml();
    }
   

    //Based on certain pre-set fields/variables, look for what
    //drop-downs need to be populated
	private void populateDropdowns() {
		
		//For each field with an optionType defined, create the options
		WebappDaoFactory wdf = vreq.getWebappDaoFactory();
		for(String fieldName: editConfig.getFields().keySet()){
		    FieldVTwo field = editConfig.getField(fieldName);
		    if( field.getOptionsType() == FieldVTwo.OptionsType.UNDEFINED 
		         || field.getOptionsType() == null ){
		        continue;
		    }
		    Map<String, String> optionsMap = SelectListGeneratorVTwo.getOptions(editConfig, fieldName, wdf);
		    optionsMap = SelectListGeneratorVTwo.getSortedMap(optionsMap);
		    pageData.put(fieldName, optionsMap);		       
		}
	}

	//TODO: Check if this should return a list instead
	//Also check if better manipulated/handled within the freemarker form itself
	private String getSelectedValue(String field) {
		String selectedValue = null;
		Map<String, List<String>> urisInScope = editConfig.getUrisInScope();
		if(urisInScope.containsKey(field)) {
			List<String> values = urisInScope.get(field);
			//Unsure how to deal with multi-select drop-downs
			//TODO: Handle multiple select dropdowns
			selectedValue = StringUtils.join(values, ",");
		}
		return selectedValue;
	}
    
	private void setFormTitle() {
		String predicateUri = editConfig.getPredicateUri();
		if(predicateUri != null) {
			if(EditConfigurationUtils.isObjectProperty(editConfig.getPredicateUri(), vreq)) {
				setObjectFormTitle();
			} else {
				setDataFormTitle();
			}
		}
	}
	
    private void setDataFormTitle() {
		String formTitle = "";		
	    DataProperty  prop = EditConfigurationUtils.getDataProperty(vreq);
	    if(prop != null) {
	        if( editConfig.isDataPropertyUpdate() ) {
	            formTitle   = "Change text for: <em>"+prop.getPublicName()+"</em>";		        
	        } else {
	            formTitle   ="Add new entry for: <em>"+prop.getPublicName()+"</em>";
	        }
	    }		
		pageData.put("formTitle", formTitle);
	}

	//Process and set data
    //Both form title and submit label would depend on whether this is data property
    //or object property
    private void setObjectFormTitle() {
    	String formTitle = null;
    	Individual objectIndividual = EditConfigurationUtils.getObjectIndividual(vreq);
    	ObjectProperty prop = EditConfigurationUtils.getObjectProperty(vreq);
    	Individual subject = EditConfigurationUtils.getSubjectIndividual(vreq);
    	String propertyTitle = getObjectPropertyNameForDisplay();
    	if(objectIndividual != null) {
    		formTitle = "Change entry for: <em>" + propertyTitle + " </em>";
    	}  else {
            if ( prop.getOfferCreateNewOption() ) {
            	
                log.debug("property set to offer \"create new\" option; custom form: ["+prop.getCustomEntryForm()+"]");
                formTitle   = "Select an existing "+propertyTitle+" for "+subject.getName();
               
            } else {
                formTitle   = "Add an entry to: <em>"+propertyTitle+"</em>";
            }
        }
    	pageData.put("formTitle", formTitle);
    }
    
    //Also used above and can be used in object auto complete form
    public String getObjectPropertyNameForDisplay() {
    	String propertyTitle = null;
    	Individual objectIndividual = EditConfigurationUtils.getObjectIndividual(vreq);
    	ObjectProperty prop = EditConfigurationUtils.getObjectProperty(vreq);
    	Individual subject = EditConfigurationUtils.getSubjectIndividual(vreq);
    	if(objectIndividual != null) {
    		propertyTitle = prop.getDomainPublic();
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
                propertyTitle   = classOfObjectFillers.getName();
               
            } else {
                propertyTitle   = prop.getDomainPublic();
            }
        }
    	return propertyTitle;
    }
    
    
    private void setSubmitLabel() {
    	String submitLabel = null;
    	String predicateUri = editConfig.getPredicateUri();
    	if(predicateUri != null) {
			if(EditConfigurationUtils.isObjectProperty(editConfig.getPredicateUri(), vreq)) {
		    	Individual objectIndividual = EditConfigurationUtils.getObjectIndividual(vreq);
		    	ObjectProperty prop = EditConfigurationUtils.getObjectProperty(vreq);
		    	
		    	if(objectIndividual != null) {
		    		submitLabel = "Save change";
		    	}  else {
		            if ( prop.getOfferCreateNewOption() ) {
		                submitLabel = "Select existing";
		            } else {
		                submitLabel = "Save entry";
		            }
		        }
			} else {
				if(editConfig.isDataPropertyUpdate()) {
					submitLabel = "Save change";
				} else {
					submitLabel = "Save entry";
				}
			}
    	}
    	pageData.put("submitLabel", submitLabel);
    	
    }
    
    
    public String getFormTitle() {
    	return (String) pageData.get("formTitle");
    }
    
    public String getSubmitLabel() {
    	return (String) pageData.get("submitLabel");
    }
    
    /*
    public Map<String, String> getRangeOptions() {
    	Map<String, String> rangeOptions = (Map<String, String>) pageData.get("rangeOptions");
    	return rangeOptions;
    }*/
    
    //Get literals in scope, i.e. variable names with values assigned
    public Map<String, List<Literal>> getLiteralValues() {
    	return editConfig.getLiteralsInScope();
    }
    
    //variables names with URIs assigned
    public Map<String, List<String>> getObjectUris() {
    	return editConfig.getUrisInScope();
    }
    
    public List<String> getLiteralStringValue(String key) {
    	List<String> literalValues = new ArrayList<String>();
    	Map<String, List<Literal>> literalsInScope = editConfig.getLiteralsInScope();
    	if(literalsInScope.containsKey(key)) {
	    	List<Literal> ls = literalsInScope.get(key);
	    	for(Literal l: ls) {
	    		literalValues.add(l.getString());
	    	}
    	}
    	return literalValues;
    }
    

    //Check if possible to send in particular parameter
    public String dataLiteralValueFor(String dataLiteralName) {
    	List<String> literalValues = getLiteralStringValue(dataLiteralName);
    	return StringUtils.join(literalValues, ",");
    }
    
    public String testMethod(String testValue) {
    	return testValue + "test";
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
    	if(predicateUri != null) {
	    	if(EditConfigurationUtils.isObjectProperty(predicateUri, vreq)){
	    		return EditConfigurationUtils.getObjectPropertyForPredicate(this.vreq, predicateUri);
	    	}
			//otherwise return Data property
	    	return EditConfigurationUtils.getDataPropertyForPredicate(this.vreq, predicateUri);
    	}
    	return null;
    }
    
    public ObjectProperty getObjectPredicateProperty() {
    	//explore usuing EditConfigurationUtils.getObjectProperty(this.vreq)
    	//return this.vreq.getWebappDaoFactory().getObjectPropertyDao().getObjectPropertyByURI(getPredicateUri());
    	return EditConfigurationUtils.getObjectPropertyForPredicate(this.vreq, getPredicateUri());
    }
    
    public DataProperty getDataPredicateProperty() {
    	return EditConfigurationUtils.getDataPropertyForPredicate(this.vreq, getPredicateUri());
    }
    
    public String getDataPredicatePublicDescription() {
    	DataProperty dp = getDataPredicateProperty();
    	return dp.getPublicDescription();
    }
    public String getPredicateUri() {
    	return editConfig.getPredicateUri();
    }
    
    public String getSubjectUri() {
    	return editConfig.getSubjectUri();
    }
    
    public String getSubjectName() {
    	
    	Individual subject = EditConfigurationUtils.getIndividual(vreq, getSubjectUri());
    	return subject.getName();
    }
    
    public String getObjectUri() {
    	return editConfig.getObject();
    }
    
    
    //data literal
    //Thus would depend on the literals on the form
    //Here we are assuming there is only one data literal but there may be more than one
    //TODO: Support multiple data literals AND/or leaving the data literal to the 
    public String getDataLiteral() {
    	List<String> literalsOnForm = editConfig.getLiteralsOnForm();
    	String dataLiteralName = null;
    	if(literalsOnForm.size() == 1) {
    		dataLiteralName = literalsOnForm.get(0);
    	}
    	return dataLiteralName;
    }
    
    public String getVarNameForObject() {
        return editConfig.getVarNameForObject();
    }
    
    //Get data property key
    
    //public description only appears visible for object property
    public String getPropertyPublicDescription() {
    	return getObjectPredicateProperty().getPublicDescription();
    }
    
    //properties queried on the main object property
    public boolean getPropertySelectFromExisting() {
    	return getObjectPredicateProperty().getSelectFromExisting();
    }
    
    //used for form title for object properties
    //TODO: update because assumes domain public
    public String getPropertyPublicDomainTitle() {
    	ObjectProperty prop = EditConfigurationUtils.getObjectProperty(vreq);
    	return  prop.getDomainPublic();
    }
    
    //used for form title for data properties
    //TODO: Update b/c assumes data property
    public String getPropertyPublicName() {
    	DataProperty  prop = EditConfigurationUtils.getDataProperty(vreq);
		return prop.getPublicName();
    }
    
    public boolean getPropertyOfferCreateNewOption() {
    	return getObjectPredicateProperty().getOfferCreateNewOption();
    }
    
    public String getPropertyName() {
    	if(isObjectProperty()) {
    		return getPropertyPublicDomainTitle().toLowerCase();
    	}
    	if(isDataProperty()) {
    		return getPropertyPublicName();
    	}
    	return null;
    }
    
    //TODO: Implement statement display
    public TemplateModel getObjectStatementDisplay() throws TemplateModelException {
    	Map<String, String> statementDisplay = new HashMap<String, String>();
    	String subjectUri = EditConfigurationUtils.getSubjectUri(vreq);
		String predicateUri = EditConfigurationUtils.getPredicateUri(vreq);
    	String objectUri = EditConfigurationUtils.getObjectUri(vreq);
		
		//Set data map
		Map params = vreq.getParameterMap();
		for (Object key : params.keySet()) {
	        String keyString = (String) key; //key.toString()
	        if (keyString.startsWith("statement_")) {
	            keyString = keyString.replaceFirst("statement_", "");
	            String value = ( (String[]) params.get(key))[0];
	            statementDisplay.put(keyString, value);
	        }
	    }
		
		
		//If no statement parameters being sent back, then just pass back null
		if(statementDisplay.size() == 0) {
			return null;
		}
		
		//ObjectPropertyStatementTemplate Model should pass the object key as part of the delete url
		String objectKey = vreq.getParameter("objectKey");
		statementDisplay.put(objectKey, objectUri);
		
		//Using object property statement template model here
		ObjectPropertyStatementTemplateModel osm = new ObjectPropertyStatementTemplateModel(
				subjectUri, 
				predicateUri, 
				objectKey, 
		        statementDisplay, 
		        null, null, vreq);
		ReadOnlyBeansWrapper wrapper = new ReadOnlyBeansWrapper();
		return wrapper.wrap(osm);
    }
    
    //HasEditor and HasReviewer Roles also expect the Property template model to be passed
    public TemplateModel getObjectPropertyStatementDisplayPropertyModel() throws TemplateModelException {
    	Individual subject = EditConfigurationUtils.getSubjectIndividual(vreq);
    	ObjectProperty op = EditConfigurationUtils.getObjectProperty(vreq);
		List<ObjectProperty> propList = new ArrayList<ObjectProperty>();
		propList.add(op);
    	EditingPolicyHelper policyHelper = new EditingPolicyHelper(vreq);
    	ObjectPropertyTemplateModel otm = ObjectPropertyTemplateModel.getObjectPropertyTemplateModel(op, subject, vreq, policyHelper,propList);
		ReadOnlyBeansWrapper wrapper = new ReadOnlyBeansWrapper();
		return wrapper.wrap(otm);
    }
    
    public String getDataStatementDisplay() {
    	//Just return the value of the data property
    	return getDataLiteralValuesFromParameter();
    }
    
    //Used for deletion in case there's a specific template to be employed
    public String getDeleteTemplate() {
    	String templateName = vreq.getParameter("templateName");
    	if(templateName == null || templateName.isEmpty()) {
    		templateName = "propStatement-default.ftl";
    	}
    	return templateName;
    }
    
    //Retrieves data propkey from parameter and gets appropriate data value
    private String getDataLiteralValuesFromParameter() {
    	String dataValue = null;
		//Get data hash
		int dataHash = EditConfigurationUtils.getDataHash(vreq);
		DataPropertyStatement dps = EditConfigurationUtils.getDataPropertyStatement(vreq, 
				vreq.getSession(), 
				dataHash, 
				EditConfigurationUtils.getPredicateUri(vreq));
		if(dps != null) {
			dataValue = dps.getData().trim();
		}
		return dataValue;
		
	}
    
   

	//TODO:Check where this logic should actually go, copied from input element formatting tag
    public Map<String, String> getOfferTypesCreateNew() {
		WebappDaoFactory wdf = vreq.getWebappDaoFactory();
    	ObjectProperty op = 
    		wdf.getObjectPropertyDao().getObjectPropertyByURI(editConfig.getPredicateUri());

    	Individual sub = 
    		wdf.getIndividualDao().getIndividualByURI(editConfig.getSubjectUri());
    	
    	List<VClass> vclasses = null;
    	vclasses = wdf.getVClassDao().getVClassesForProperty(sub.getVClassURI(), op.getURI());    	
    	if( vclasses == null )
    		vclasses = wdf.getVClassDao().getAllVclasses();
    	
    	HashMap<String,String> types = new HashMap<String, String>();
    	for( VClass vclass : vclasses ){
    		
    		String name = null;
    		if( vclass.getPickListName() != null && vclass.getPickListName().length() > 0){
    			name = vclass.getPickListName();
    		}else if( vclass.getName() != null && vclass.getName().length() > 0){
    			name = vclass.getName();
    		}else if (vclass.getLocalNameWithPrefix() != null && vclass.getLocalNameWithPrefix().length() > 0){
    			name = vclass.getLocalNameWithPrefix();
    		}
    		if( name != null && name.length() > 0)
    			types.put(vclass.getURI(),name);
    	}
    	
    	//Unlike input element formatting tag, including sorting logic here
    	return  getSortedMap(types);
    }
    
    public Map<String,String> getSortedMap(Map<String,String> hmap){
        // first make temporary list of String arrays holding both the key and its corresponding value, so that the list can be sorted with a decent comparator
        List<String[]> objectsToSort = new ArrayList<String[]>(hmap.size());
        for (String key:hmap.keySet()) {
            String[] x = new String[2];
            x[0] = key;
            x[1] = hmap.get(key);
            objectsToSort.add(x);
        }
        Collections.sort(objectsToSort, new MapComparator());

        HashMap<String,String> map = new LinkedHashMap<String,String>(objectsToSort.size());
        for (String[] pair:objectsToSort) {
            map.put(pair[0],pair[1]);
        }
        return map;
    }
    
    private class MapComparator implements Comparator<String[]> {
        public int compare (String[] s1, String[] s2) {
            Collator collator = Collator.getInstance();
            if (s2 == null) {
                return 1;
            } else if (s1 == null) {
                return -1;
            } else {
            	if ("".equals(s1[0])) {
            		return -1;
            	} else if ("".equals(s2[0])) {
            		return 1;
            	}
                if (s2[1]==null) {
                    return 1;
                } else if (s1[1] == null){
                    return -1;
                } else {
                    return collator.compare(s1[1],s2[1]);
                }
            }
        }
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
        if(   editConfig.getEntityToReturnTo() != null && 
            ! editConfig.getEntityToReturnTo().trim().isEmpty()) {
            ParamMap params = new ParamMap();
            params.put("uri", editConfig.getEntityToReturnTo());
            return UrlBuilder.getUrl(UrlBuilder.Route.INDIVIDUAL, params);
        }else if( vreq.getParameter("urlPattern") != null ){
            return vreq.getParameter("urlPattern");
        }else{
            return UrlBuilder.Route.INDIVIDUAL.path(); 
        }
    }
    
    public String getCurrentUrl() {
    	return EditConfigurationUtils.getEditUrl(vreq) + "?" + vreq.getQueryString();
    }
    
    public String getMainEditUrl() {
    	return EditConfigurationUtils.getEditUrl(vreq);
    }
    
    //this url is for canceling
    public String getCancelUrl() {
    	String editKey = editConfig.getEditKey();
    	return EditConfigurationUtils.getCancelUrlBase(vreq) + "?editKey=" + editKey + "&cancel=true";
    }
    
    //Get confirm deletion url
    public String getDeleteProcessingUrl() {
    	return vreq.getContextPath() + "/deletePropertyController";
    }
    
    //TODO: Check if this logic is correct and delete prohibited does not expect a specific value
    public boolean isDeleteProhibited() {
    	String deleteProhibited = vreq.getParameter("deleteProhibited");
    	return (deleteProhibited != null && !deleteProhibited.isEmpty());
    }
    
    public String getDatapropKey() {
        if( editConfig.getDatapropKey() == null )
            return null;
        else
            return editConfig.getDatapropKey().toString();
    }
    
    public DataPropertyStatement getDataPropertyStatement() {
    	int dataHash = EditConfigurationUtils.getDataHash(vreq);
    	String predicateUri = EditConfigurationUtils.getPredicateUri(vreq);
    	return EditConfigurationUtils.getDataPropertyStatement(vreq, 
    			vreq.getSession(), 
    			dataHash, 
    			predicateUri);
    }
    
    //Check whether deletion form should be included for default object property
    public boolean getIncludeDeletionForm() {
    	if( isDeleteProhibited() ) 
    		return false;
    	if( isObjectProperty() ) {
    	    return editConfig.isObjectPropertyUpdate();
    	} else {
    	    return  editConfig.isDataPropertyUpdate();     	        
    	}
     }
    
    public String getVitroNsProperty() {
    	String vitroNsProp =  vreq.getParameter("vitroNsProp");
    	if(vitroNsProp == null) {
    		vitroNsProp = "";
    	}
    	return vitroNsProp;
    }
    
    //Additional data to be returned
    public HashMap<String, Object> getPageData() {
    	return pageData;
    }
    
    //Literals in scope and uris in scope are the values
    //that currently exist for any of the fields/values
    
  //Get literals in scope returned as string values
    public Map<String, List<String>> getExistingLiteralValues() {
    	return EditConfigurationUtils.getExistingLiteralValues(vreq, editConfig);
    }
    
    public Map<String, List<String>> getExistingUriValues() {
    	return editConfig.getUrisInScope();
    }
    
    //Get editElements with html
    public void populateGeneratedHtml() {
    	Map<String, String> generatedHtml = new HashMap<String, String>();
    	Map<String, FieldVTwo> fieldMap = editConfig.getFields();
    	//Check if any of the fields have edit elements and should be generated
    	Set<String> keySet = fieldMap.keySet();
    	for(String key: keySet) {
    		FieldVTwo field = fieldMap.get(key);
    		EditElementVTwo editElement = field.getEditElement();
    		String fieldName = field.getName();
    		if(editElement != null) {
    			generatedHtml.put(fieldName, EditConfigurationUtils.generateHTMLForElement(vreq, fieldName, editConfig));
    		}
    	}
    	
    	//Put in pageData
    	pageData.put("htmlForElements", generatedHtml);
    }
   
  
   
}
