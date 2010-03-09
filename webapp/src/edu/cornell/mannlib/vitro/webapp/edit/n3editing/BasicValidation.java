/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Datatype;
import edu.cornell.mannlib.vitro.webapp.dao.jena.DatatypeDaoJena;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactoryJena;

import java.util.*;
import java.util.regex.Pattern;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * User: bdc34
 * Date: Jan 24, 2008
 * Time: 1:55:39 PM
 */
public class BasicValidation {

    Map<String, List<String>> varsToValidations;
    EditConfiguration editConfig;
    
    public BasicValidation(EditConfiguration editConfig, EditSubmission editSub){
        this.editConfig = editConfig;
        Map<String,List<String>> validatorsForFields = new HashMap<String,List<String>>();
        for(String fieldName: editConfig.getFields().keySet()){
            Field field = editConfig.getField(fieldName);
            validatorsForFields.put(fieldName,field.getValidators());
        }
        this.varsToValidations = validatorsForFields;
        checkValidations();
    }

    public BasicValidation(Map<String, List<String>> varsToValidations){
        this.varsToValidations = varsToValidations;
        checkValidations();
    }

    public Map<String,String> validateUris(Map<String,String> varNamesToValues){
        HashMap<String,String> errors = new HashMap<String,String>();

        for( String name : varNamesToValues.keySet()){

            String value = varNamesToValues.get(name);
            List<String> validations = varsToValidations.get(name);
            if( validations!= null){
                for( String validationType : validations){
                    String validateMsg = validate(validationType,value);
                    if( validateMsg != null) {
                        errors.put(name,validateMsg);
                    }    
                }
            }
        }               
        return errors;
    }


    public Map<String,String> validateLiterals(Map<String, Literal> varNamesToValues){
        HashMap<String,String> errors = new HashMap<String,String>();

        for( String name : editConfig.getLiteralsOnForm() ){
            Literal literal = varNamesToValues.get(name);
            List<String>validations = varsToValidations.get(name);
            if( validations != null ){
                // NB this is case-sensitive
                boolean isRequiredField = validations.contains("nonempty");
                for( String validationType : validations){
                    String value = null;
                    try{
                        if( literal != null )
                            value = literal.getString();
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
                    String validateMsg = validate(validationType, value);
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
        if("isDate".equalsIgnoreCase(validationType)){
            if( isDate( value))
                return SUCCESS;
            else
                return "must be in valid date format mm/dd/yyyy.";
        }else if( validationType.indexOf("datatype:") == 0 ) {
            String datatypeURI = validationType.substring(9);
            String errorMsg = validateAgainstDatatype( value, datatypeURI ); 
            if ( errorMsg == null ) { 
                return SUCCESS;
            } else {
                return errorMsg;
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


    /** we use null to indicate success */
    public final static String SUCCESS = null;
    public final static String REQUIRED_FIELD_EMPTY_MSG = "This field must not be empty.";
    
    /** regex for strings like "12/31/2004" */
    private final String dateRegex = "((1[012])|([1-9]))/((3[10])|([12][0-9])|([1-9]))/[\\d]{4}";
    private final Pattern datePattern = Pattern.compile(dateRegex);

    static final List<String> basicValidations;
    static{
        basicValidations = Arrays.asList(
        "nonempty","isDate" );
    }

    private Log log = LogFactory.getLog(BasicValidation.class);
}
