/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.IllegalFieldValueException;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONObject;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.XSD;

import edu.cornell.mannlib.vitro.webapp.edit.EditLiteral;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditElementVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.FieldVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.validators.BasicValidation;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.validators.N3Validator;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.processEdit.EditN3Utils;

public class MultiValueEditSubmission {

    String editKey;
    
    private Map<String,List<Literal>> literalsFromForm ;
    private Map<String,List<String>> urisFromForm ;

    private Map<String,String> validationErrors;
    private BasicValidationVTwo basicValidation;

    private Map<String, List<FileItem>> filesFromForm;

    private static Model literalCreationModel;
    
    private String entityToReturnTo;
    
    static{
        literalCreationModel = ModelFactory.createDefaultModel();
    }
    
    public MultiValueEditSubmission(Map<String,String[]> queryParameters,  EditConfigurationVTwo editConfig){
        if( editConfig == null )
            throw new Error("EditSubmission needs an EditConfiguration");            
        this.editKey = editConfig.getEditKey();         
        if( this.editKey == null || this.editKey.trim().length() == 0)
            throw new Error("EditSubmission needs an 'editKey' parameter from the EditConfiguration");        

        entityToReturnTo = editConfig.getEntityToReturnTo();
        
        validationErrors = new HashMap<String,String>();
        
        this.urisFromForm = new HashMap<String,List<String>>();
        for( String var: editConfig.getUrisOnform() ){     
            String[] valuesArray = queryParameters.get( var );
            //String uri = null;
            addUriToForm(editConfig, var, valuesArray);
        }
        
        this.literalsFromForm =new HashMap<String,List<Literal>>();        
        for(String var: editConfig.getLiteralsOnForm() ){            
            FieldVTwo field = editConfig.getField(var);
            if( field == null ) {
                log.error("could not find field " + var + " in EditConfiguration" );
                continue;   
            } else if( field.getEditElement() != null ){                
                log.debug("skipping field with edit element, it should not be in literals on form list");
            }else{
               String[] valuesArray = queryParameters.get(var); 
               addLiteralToForm(editConfig, field, var, valuesArray);
            }
        }

        if( log.isDebugEnabled() ){         
            for( String key : literalsFromForm.keySet() ){
                log.debug( key + " literal " + literalsFromForm.get(key) );
            }
            for( String key : urisFromForm.keySet() ){
                log.debug( key + " uri " + urisFromForm.get(key) );
            }
        }
        
        processEditElementFields(editConfig,queryParameters);
        //Incorporating basic validation
        //Validate URIS
        this.basicValidation = new BasicValidationVTwo(editConfig, this);
        Map<String,String> errors = basicValidation.validateUris( urisFromForm ); 
        //Validate literals and add errors to the list of existing errors
        errors.putAll(basicValidation.validateLiterals( literalsFromForm ));
        if( errors != null ) {
            validationErrors.putAll( errors);
        }              
        
        if(editConfig.getValidators() != null ){
            for( N3ValidatorVTwo validator : editConfig.getValidators()){
                if( validator != null ){     
                    //throw new Error("need to implemente a validator interface that works with the new MultivalueEditSubmission.");
                    errors = validator.validate(editConfig, this);
                    if ( errors != null )
                        validationErrors.putAll(errors);
                }
            }
        }           
                
        if( log.isDebugEnabled() )
            log.debug( this.toString() );
    }

    protected void processEditElementFields(EditConfigurationVTwo editConfig, Map<String,String[]> queryParameters ){
        for( String fieldName : editConfig.getFields().keySet()){
            FieldVTwo field = editConfig.getFields().get(fieldName);
            if( field != null && field.getEditElement() != null ){
                EditElementVTwo element = field.getEditElement();                
                log.debug("Checking EditElement for field " + fieldName + " type: " + element.getClass().getName());
                
                //check for validation error messages
                Map<String,String> errMsgs = 
                    element.getValidationMessages(fieldName, editConfig, queryParameters);
                validationErrors.putAll(errMsgs);
                                
                if( errMsgs == null || errMsgs.isEmpty()){                    
                    //only check for uris and literals when element has no validation errors
                    Map<String,List<String>> urisFromElement = element.getURIs(fieldName, editConfig, queryParameters);
                    if( urisFromElement != null )
                        urisFromForm.putAll(urisFromElement);
                    Map<String,List<Literal>> literalsFromElement = element.getLiterals(fieldName, editConfig, queryParameters);
                    if( literalsFromElement != null )
                        literalsFromForm.putAll(literalsFromElement);
                }else{
                    log.debug("got validation errors for field " + fieldName + " not processing field for literals or URIs");
                }
            }            
        }        
    }    

    /* maybe this could be static */
    public Literal createLiteral(String value, String datatypeUri, String lang) {
        if( datatypeUri != null ){            
            if( "http://www.w3.org/2001/XMLSchema:anyURI".equals(datatypeUri) ){
                try {
                    return literalCreationModel.createTypedLiteral( URLEncoder.encode(value, "UTF8"), datatypeUri);
                } catch (UnsupportedEncodingException e) { 
                    log.error(e, e);
                }                
            }   
            return literalCreationModel.createTypedLiteral(value, datatypeUri);
        }else if( lang != null && lang.length() > 0 )
            return literalCreationModel.createLiteral(value, lang);
        else 
            return ResourceFactory.createPlainLiteral(value);        
    }               

    private static final String DATE_TIME_URI = XSD.dateTime.getURI();
    private static final String DATE_URI = XSD.date.getURI();
    private static final String TIME_URI = XSD.time.getURI();

    private static DateTimeFormatter dformater = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:00");
    private static DateTimeFormatter dateFormater = DateTimeFormat.forPattern("yyyy-MM-dd");

    public Map<String,String> getValidationErrors(){
        return validationErrors;
    }

    public Map<String, List<Literal>> getLiteralsFromForm() {
        return literalsFromForm;
    }

    public Map<String, List<String>> getUrisFromForm() {
        return urisFromForm;
    }
    /**
     * need to generate something like
     *  "09:10:11"^^<http://www.w3.org/2001/XMLSchema#time>
     */ 
    public Literal getTime(Map<String,String[]> queryParameters,String fieldName) {
        List<String> hour = Arrays.asList(queryParameters.get("hour" + fieldName));
        List<String> minute = Arrays.asList(queryParameters.get("minute" + fieldName));
        
        if ( hour == null || hour.size() == 0 ||
             minute == null || minute.size() == 0 ) {
            log.info("Could not find query parameter values for time field " + fieldName);
            validationErrors.put(fieldName, "time must be supplied");
            return null;
        }
        
        int hourInt = -1;
        int minuteInt = -1;
        
        String hourParamStr = hour.get(0);
        String minuteParamStr = minute.get(0);
        
        // if all fields are blank, just return a null value
        if (hourParamStr.length() == 0 && minuteParamStr.length() == 0) {
            return null;
        }
        
         String errors = "";
         try{ 
             hourInt = Integer.parseInt(hour.get(0));
             if (hourInt < 0 || hourInt > 23) {
                 throw new NumberFormatException();
             }
         } catch( NumberFormatException nfe ) { 
             errors += "Please enter a valid hour.  "; 
         }        
         try{
             minuteInt = Integer.parseInt(minute.get(0));
             if (minuteInt < 0 || minuteInt > 59) {
                 throw new NumberFormatException();
             }
         } catch( NumberFormatException nfe ) { 
             errors += "Please enter a valid minute.  "; 
         }        
         if( errors.length() > 0 ){
             validationErrors.put( fieldName, errors);
             return null;
         }
        
         
         String hourStr = (hourInt < 10) ? "0" + Integer.toString(hourInt) :  Integer.toString(hourInt);
         String minuteStr = (minuteInt < 10) ? "0" + Integer.toString(minuteInt) :  Integer.toString(minuteInt);
         String secondStr = "00";
         
         return new EditLiteral(hourStr + ":" + minuteStr + ":" + secondStr, TIME_URI, null);
         
    }
    public void setLiteralsFromForm(Map<String, List<Literal>> literalsFromForm) {
        this.literalsFromForm = literalsFromForm;
    }

    public void setUrisFromForm(Map<String, List<String>> urisFromForm) {
        this.urisFromForm = urisFromForm;
    }

    public String toString(){        
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);        
    }

    private Log log = LogFactory.getLog(MultiValueEditSubmission.class);

    public String getEntityToReturnTo() {
        return entityToReturnTo;
    }

    public void setEntityToReturnTo(String string) {
        entityToReturnTo = string;
    }    
    
    //Added specifically to help with "dynamic" forms such as addition of concept
    public void addLiteralToForm(EditConfigurationVTwo editConfig, FieldVTwo field, String var, String[] valuesArray) {
    	List<String> valueList = (valuesArray != null) ? Arrays.asList(valuesArray) : null;                
        if( valueList != null && valueList.size() > 0 ) {
        	List<Literal> literalsArray = new ArrayList<Literal>();
        	//now support multiple values
        	for(String value:valueList) {
        		value = EditN3Utils.stripInvalidXMLChars(value);
                //Add to array of literals corresponding to this variable
                if (!StringUtils.isEmpty(value)) {
                    literalsArray.add(createLiteral(
                                                value, 
                                                field.getRangeDatatypeUri(), 
                                                field.getRangeLang()));
                }
        	}
        	literalsFromForm.put(var, literalsArray);
            
        }else{
            log.debug("could not find value for parameter " + var  );
        }
    }
    //Add literal to form
    //Add uri to form
    public void addUriToForm(EditConfigurationVTwo editConfig, String var, String[] valuesArray) {
         List<String> values = (valuesArray != null) ? Arrays.asList(valuesArray) : null;
         if( values != null && values.size() > 0){
	            //Iterate through the values and check to see if they should be added or removed from form
	            urisFromForm.put(var, values);
	            for(String uri : values) {
		            if( uri != null && uri.length() == 0 && editConfig.getNewResources().containsKey(var) ){
		                log.debug("A new resource URI will be made for var " + var + " since it was blank on the form.");
		                urisFromForm.remove(var);
		            }
	            }
         }  else {
             log.debug("No value found for query parameter " + var);              
         }
    }
}
