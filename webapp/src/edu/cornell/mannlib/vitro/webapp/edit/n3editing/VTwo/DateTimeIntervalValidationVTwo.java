/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.rdf.model.Literal;

import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary.Precision;
import edu.cornell.mannlib.vitro.webapp.edit.elements.DateTimeWithPrecision;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.EditConfiguration;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.processEdit.EditSubmission;

/*
 * Assumption for date time interval validation: Only one start field/end field/and precision.
 * We are currently not accepting multiple values for start time, end time, or precision. 
 */


public class DateTimeIntervalValidationVTwo implements N3ValidatorVTwo {
    private static Log log = LogFactory.getLog(DateTimeIntervalValidationVTwo.class);
    
    private String startFieldName;
    private String endFieldName;

    private String startValueName;
    private String endValueName;

    private String startPrecisionName;
    private String endPrecisionName;
    
    public DateTimeIntervalValidationVTwo(String startFieldName, String endFieldName){
        this.startFieldName = startFieldName;
        this.endFieldName = endFieldName;
        startValueName = startFieldName + "-value";
        endValueName = endFieldName + "-value";
        startPrecisionName = startFieldName + "-precision";
        endPrecisionName = endFieldName + "-precision";
    }
    
    public Map<String, String> validate(EditConfigurationVTwo editConfig,
            MultiValueEditSubmission editSub) {
        Map<String, List<Literal>> existingLiterals = editConfig.getLiteralsInScope();
        List<Literal> existingStartYear = existingLiterals.get(startValueName);
        List<Literal> existingEndYear = existingLiterals.get(endValueName);

        Map<String, List<Literal>> literalsFromForm = editSub.getLiteralsFromForm();
        List<Literal> formStartYear = literalsFromForm.get(startValueName);
        List<Literal> formEndYear = literalsFromForm.get(endValueName);

        VitroVocabulary.Precision startPrecision = getPrecision(startPrecisionName, editConfig, editSub);
        VitroVocabulary.Precision endPrecision = getPrecision(endPrecisionName, editConfig, editSub);
        
        Map<String, String> errors = new HashMap<String, String>();

        // NIHVIVO-2541 Commented out to allow end date with no start date
//        if( formStartYear == null && formEndYear != null ){                               
//            errors.put(startFieldName, "If there is an end date, there should be a start date");
//            return errors;              
//        }
        
        
        //Assuming form start year and form end year are working in conjunction with multiple values
        int index;
        if (!literalListIsNull(formStartYear) && !literalListIsNull(formEndYear)) {
        	int numberStartYears = formStartYear.size();
        	int numberEndYears = formEndYear.size();
        	if(numberStartYears > 1 && numberEndYears > 1) {
        		errors.put(startFieldName, "DateTimeIntervalValidationVTwo does not support multiple start years or end years");
        		return errors;
        	}
        	
        	if(numberStartYears > 0 && numberEndYears > 0) {
        		errors.putAll(checkDateLiterals(formStartYear.get(0), formEndYear.get(0), startPrecision, endPrecision));
        	}
        } else if (!literalListIsNull(formStartYear) && !literalListIsNull(existingEndYear)) {
        	int numberStartYears = formStartYear.size();
        	int numberEndYears = existingEndYear.size();
        	if(numberStartYears > 1 && numberEndYears > 1) {
        		errors.put(startFieldName, "DateTimeIntervalValidationVTwo does not support multiple start years or end years");
        		return errors;
        	}
        	
        	if(numberStartYears > 0 && numberEndYears > 0) {
        		errors.putAll(checkDateLiterals(formStartYear.get(0), existingEndYear.get(0), startPrecision, endPrecision));
        	}
        } else if (!literalListIsNull(existingStartYear)  && !literalListIsNull(formEndYear)) {
        	
        	int numberStartYears = existingStartYear.size();
        	int numberEndYears = formEndYear.size();
        	if(numberStartYears > 1 && numberEndYears > 1) {
        		errors.put(startFieldName, "DateTimeIntervalValidationVTwo does not support multiple start years or end years");
        		return errors;
        	}
        	
        	if(numberStartYears > 0 && numberEndYears > 0) {
        		errors.putAll(checkDateLiterals(existingStartYear.get(0), formEndYear.get(0), startPrecision, endPrecision));
        	}
        } else if (!literalListIsNull(existingStartYear) && !literalListIsNull(existingEndYear)) {
        	int numberStartYears = existingStartYear.size();
        	int numberEndYears = existingEndYear.size();
        	if(numberStartYears > 1 && numberEndYears > 1) {
        		errors.put(startFieldName, "DateTimeIntervalValidationVTwo does not support multiple start years or end years");
        		return errors;
        	}
        	
        	if(numberStartYears > 0 && numberEndYears > 0) {
        		errors.putAll(checkDateLiterals(existingStartYear.get(0), existingEndYear.get(0), startPrecision, endPrecision));
        	}
        }

        if (errors.size() != 0)
            return errors;
        else
            return null;
    }

    private Precision getPrecision(String precisionVarName,
            EditConfigurationVTwo editConfig, MultiValueEditSubmission editSub) {
        if( editSub != null 
                && editSub.getUrisFromForm() != null 
                && editSub.getUrisFromForm().containsKey(precisionVarName)){            
            List<String> precisionStr = editSub.getUrisFromForm().get(precisionVarName);
            //TODO: Check if we need to handle multiple precision strings and what to do then
            //Currently checks first precision str and then returns response
            if(precisionStr.size() > 0) {
            	String precisionString = precisionStr.get(0);
            	VitroVocabulary.Precision precision = DateTimeWithPrecision.toPrecision( precisionString );
                if( precision == null )
                    log.warn("cannot convert " + precisionStr + " to a precision");
                else
                    return precision;
            } else {
            	log.error("No precision strings returned");
            }
            
        }else if( editConfig != null 
                && editConfig.getUrisInScope() != null 
                && editConfig.getUrisInScope().containsKey(precisionVarName)){
            List<String> precisionStr = editConfig.getUrisInScope().get(precisionVarName);
            //TODO: Check if we need to handle multiple precision strings and what to do then
            //Currently checks first precision str and then returns response
            if(precisionStr.size() > 0) {
            	String precisionString = precisionStr.get(0);
            	VitroVocabulary.Precision precision = DateTimeWithPrecisionVTwo.toPrecision( precisionString );
                if( precision == null )
                    log.warn("cannot convert " + precisionString + " to a precision");
                else
                    return precision; 
            } else {
            	log.error("No precision strings returned");
            }
                   
        }
        //this is what is returned if a precision was not found in the config or submission
        return null;
    }

    private Map<String, String> checkDateLiterals(
            Literal startLit, Literal endLit, 
            VitroVocabulary.Precision startPrecision, VitroVocabulary.Precision endPrecision) {                
        Map<String, String> errors = new HashMap<String, String>();        
        
        if( endPrecision == null ){
            //there is no end date, nothing to check
            return errors;
        }             
        
        try{
             XSDDateTime startDate = (XSDDateTime)startLit.getValue();
             XSDDateTime endDate = (XSDDateTime)endLit.getValue();
             if( startDate != null && endDate!= null ){
                 Calendar startCal = startDate.asCalendar();
                 Calendar endCal = endDate.asCalendar();
                                  
                 if( endCal != null ){
                     if( !startCal.before( endCal ) ){
                         if( startPrecision == VitroVocabulary.Precision.YEAR 
                             && endPrecision == VitroVocabulary.Precision.YEAR ){
                             errors.putAll( checkYears(startCal,endCal));
                         }else{
                             errors.put(startFieldName, "Start must be before end");
                             errors.put(endFieldName, "End must be after start");
                         }
                     }
                 }
             }
        }catch(ClassCastException cce){
            errors.put(startFieldName, "could not format start or end date");
            errors.put(endFieldName, "could not format start or end date");
            log.debug("could not format dates " + cce);
        }
            
        return errors;
    }

    private Map<? extends String, ? extends String> checkYears(
            Calendar startCal, Calendar endCal) {
        
        Map<String, String> errors = new HashMap<String, String>();    
     
        if( ! (endCal.get(Calendar.YEAR) >=  startCal.get(Calendar.YEAR) )){
            errors.put(startFieldName, "Start must be before end");
            errors.put(endFieldName, "End must be after start");
        }
        
        return errors;
    }
    
    //MEthod that checks whether list of literals is null or contains only null
    private boolean literalListIsNull(List<Literal> literalList) {
    	if(literalList == null)
    		return true;
    	boolean allNulls = true;
    	for(Literal l: literalList) {
    		if(l != null)
    			allNulls = false;
    	}
    	return allNulls;
    }
}
