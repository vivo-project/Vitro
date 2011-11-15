/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Datatype;
import edu.cornell.mannlib.vitro.webapp.dao.jena.DatatypeDaoJena;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactoryJena;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.EditConfiguration;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.Field;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.processEdit.EditSubmission;


public class BasicValidationVTwo {

    Map<String, List<String>> varsToValidations;
    EditConfigurationVTwo editConfig;
    
    public BasicValidationVTwo(EditConfigurationVTwo editConfig, MultiValueEditSubmission editSub){
        this.editConfig = editConfig;
        Map<String,List<String>> validatorsForFields = new HashMap<String,List<String>>();
        for(String fieldName: editConfig.getFields().keySet()){
            FieldVTwo field = editConfig.getField(fieldName);
            validatorsForFields.put(fieldName,field.getValidators());
        }
        this.varsToValidations = validatorsForFields;
        checkValidations();
    }

    public BasicValidationVTwo(Map<String, List<String>> varsToValidations){
        this.varsToValidations = varsToValidations;
        checkValidations();
    }

    public Map<String,String> validateUris(Map<String,List<String>> varNamesToValues){
        HashMap<String,String> errors = new HashMap<String,String>();
        
        for( String name : varNamesToValues.keySet()){
        	
            List<String> values = varNamesToValues.get(name);
            List<String> validations = varsToValidations.get(name);
            if( validations!= null){
                for( String validationType : validations){
                	//Appending validate message if same field has multiple values 
                	String validateMsg = null;
                	for(String value: values){
                		String thisValidateMsg = validate(validationType,value);
                		if(validateMsg != null && thisValidateMsg != null) {
                			validateMsg += ", " + thisValidateMsg;
                		} else {
                			validateMsg = thisValidateMsg;

                		}
                	}
                    if( validateMsg != null) {
                        errors.put(name,validateMsg);
                    }    
                }
            }
        }               
        return errors;
    }


    public Map<String,String> validateLiterals(Map<String, List<Literal>> varNamesToValues){
        HashMap<String,String> errors = new HashMap<String,String>();

        for( String name : editConfig.getLiteralsOnForm() ){
            List<Literal> literals = varNamesToValues.get(name);
            List<String>validations = varsToValidations.get(name);
            if( validations != null ){
                // NB this is case-sensitive
                boolean isRequiredField = validations.contains("nonempty");
                
                for( String validationType : validations){
                    String value = null;
                    String validateMsg = null;
                    //If no literals and this field was required, this is an error message
                    //and can return
                    if((literals == null || literals.size() == 0) && isRequiredField) {
                    	errors.put(name, REQUIRED_FIELD_EMPTY_MSG);     
                        break;
                    }
                    //Loop through literals if literals exist
                    if(literals != null) {
	                    for(Literal literal: literals) {
		                    try{
		                        if( literal != null ){
		                            value = literal.getString();
		                        }
		                    }catch(Throwable th){ 
		                        log.debug("could not convert literal to string" , th); 
		                    }
		                    // Empty field: if required, include only the empty field
		                    // error message, not a format validation message. If non-required, 
		                    // don't do format validation, since that is both unnecessary and may 
		                    // incorrectly generate errors.
		                    if (isEmpty(value)) {
		                        if (isRequiredField) {
		                           errors.put(name, REQUIRED_FIELD_EMPTY_MSG);
		                        }
		                        break;
		                    }
		                    String thisValidateMsg = validate(validationType,value);
	                		if(validateMsg != null && thisValidateMsg != null) {
	                			validateMsg += ", " + thisValidateMsg;
	                		} else {
	                			validateMsg = thisValidateMsg;
	
	                		}
	                    }
                    }
                    if( validateMsg != null) {
                        errors.put(name,validateMsg);
                    }
                }
            }
        }
        return errors;
    }
    
    public Map<String,String>validateFiles(Map<String, List<FileItem>> fileItemMap) {        
        
        HashMap<String,String> errors = new HashMap<String,String>();
        for(String name: editConfig.getFilesOnForm() ){            
            List<String> validators = varsToValidations.get(name);            
            for( String validationType : validators){
                String validateMsg = validate(validationType, fileItemMap.get(name));
                if( validateMsg != null ) {
                    errors.put(name, validateMsg);
                }    
            }
        }            
        return errors;    
    }
    
    private String validate(String validationType, List<FileItem> fileItems) {
        if( "nonempty".equalsIgnoreCase(validationType)){
            if( fileItems == null || fileItems.size() == 0 ){
                return "a file must be entered for this field.";
            }else{
                FileItem fileItem = fileItems.get(0);
                if( fileItem == null || fileItem.getName() == null || fileItem.getName().length() < 1 || fileItem.getSize() < 0){
                    return "a file must be entered for this field.";
                }
            }
        }
        return null;
    }

    /* null indicates success. A returned string is the validation
    error message.
     */
    public String validate(String validationType, String value){
        // Required field validation.
        // For literals, testing empty required values in validateLiterals.
        // This case may be needed for validation of other field types.
        if( "nonempty".equalsIgnoreCase(validationType)){
            if( isEmpty(value) )
                return REQUIRED_FIELD_EMPTY_MSG;
        }
        // Format validation
        else if("isDate".equalsIgnoreCase(validationType)){
            if( isDate( value))
                return SUCCESS;
            else
                return "must be in valid date format mm/dd/yyyy.";
        }
        else if( validationType.indexOf("datatype:") == 0 ) {
            String datatypeURI = validationType.substring(9);
            String errorMsg = validateAgainstDatatype( value, datatypeURI ); 
            if ( errorMsg == null ) { 
                return SUCCESS;
            } else {
                return errorMsg;
            }
        } else if ("httpUrl".equalsIgnoreCase(validationType)){ 
        	//check if it has http or https, we could do more but for now this is all.
        	if(! value.startsWith("http://") && ! value.startsWith("https://") ){
        		return "This URL must start with http:// or https://"; 
        	}else{
        		return SUCCESS;        		
        	}        	 
        }
        //Date not past validation
        else if( "dateNotPast".equalsIgnoreCase(validationType)){
        	//if( ! past (value) )
        	// return "date must not be in the past";
        	//Current date
        	Calendar c = Calendar.getInstance();
        	//Input
        	Calendar inputC = Calendar.getInstance();
        	String yearParamStr, monthParamStr, dayParamStr;
        	int yearDash = value.indexOf("-");
    		int monthDash = value.lastIndexOf("-");
        	if(yearDash != -1 && yearDash != monthDash) {
        		yearParamStr = value.substring(0, yearDash);
        		monthParamStr = value.substring(yearDash + 1, monthDash);
        		dayParamStr = value.substring(monthDash + 1, value.length());
        		inputC.set(Integer.parseInt(yearParamStr), Integer.parseInt(monthParamStr) - 1, Integer.parseInt(dayParamStr));
        		if(inputC.before(c)) {
            		return this.DATE_NOT_PAST_MSG;
            		//Returning null makes the error message "field is empty" display instead
            		//return null;
            	} else {
            		return SUCCESS;
            	}
        	}	
        }
        return null; //
    }

    private boolean isDate(String in){
         return datePattern.matcher(in).matches();
    }
    
    private static DatatypeDaoJena ddao = null;
    
    public static synchronized String validateAgainstDatatype( String value, String datatypeURI ) {
        if ( ( datatypeURI != null ) && ( datatypeURI.length()>0 ) ) {
            RDFDatatype datatype = TypeMapper.getInstance().getSafeTypeByName(datatypeURI);
            if ( datatype == null ) {
                throw new RuntimeException( datatypeURI + " is not a recognized datatype");
            }
            if ( datatype.isValid(value) ) {
                return null;
            } else {
                // TODO: better way of getting more friendly names for common datatypes
                if (ddao == null) {
                    ddao = new DatatypeDaoJena(new WebappDaoFactoryJena(ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM)));
                }
                Datatype dtype = ddao.getDatatypeByURI(datatypeURI);
                String dtypeMsg = (dtype != null) ? dtype.getName() : datatypeURI;
                return " Please correct this value: must be a valid " + dtypeMsg + ".";
            }
        }
        return null;
    }

    private void checkValidations(){
        List<String> unknown = new ArrayList<String>();
        for( String key : varsToValidations.keySet()){
            if( varsToValidations.get(key) == null )
                continue;            
            for( String validator : varsToValidations.get(key)){
                if( ! basicValidations.contains( validator)) {
                    if ( ! ( ( validator != null) &&  
                         ( validator.indexOf( "datatype:" ) == 0 ) ) ) {
                        unknown.add(validator);
                    }
                }
            }
        }
        if( unknown.isEmpty() )
            return ;

        throw new Error( "Unknown basic validators: " + unknown.toArray());
    }
    
    private static boolean isEmpty(String value) {
        return (value == null || value.trim().length() == 0); 
    }

    
    
    private static Pattern urlRX = Pattern.compile("(([a-zA-Z][0-9a-zA-Z+\\-\\.]*:)/{0,2}[0-9a-zA-Z;/?:@&=+$\\.\\-_!~*'()%]+)(#[0-9a-zA-Z;/?:@&=+$\\.\\-_!~*'()%]+)?");

    /** we use null to indicate success */
    public final static String SUCCESS = null;
    public final static String REQUIRED_FIELD_EMPTY_MSG = "This field must not be empty.";
    public final static String DATE_NOT_PAST_MSG = "Please enter a future target date for publication (past dates are invalid).";
    //public final static String MIN_FIELDS_NOT_POPULATED = "Please enter values for at least ";
    //public final static String FORM_ERROR_FIELD_ID = "formannotationerrors";
    /** regex for strings like "12/31/2004" */
    private final String dateRegex = "((1[012])|([1-9]))/((3[10])|([12][0-9])|([1-9]))/[\\d]{4}";
    private final Pattern datePattern = Pattern.compile(dateRegex);

    static final List<String> basicValidations;
    static{
        basicValidations = Arrays.asList(
        "nonempty","isDate","dateNotPast","httpUrl" );
    }

    private Log log = LogFactory.getLog(BasicValidationVTwo.class);
}
