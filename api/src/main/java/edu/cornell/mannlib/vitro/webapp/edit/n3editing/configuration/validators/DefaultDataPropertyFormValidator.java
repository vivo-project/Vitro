/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.validators;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.rdf.model.Literal;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.N3ValidatorVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.MultiValueEditSubmission;
import edu.cornell.mannlib.vitro.webapp.i18n.I18n;
import edu.cornell.mannlib.vitro.webapp.i18n.I18nBundle;

/**
 * Check if the submitted text has potential XSS problems.
 * Error messages from this validator always start with XSS_ERROR_MESSAGE 
 * 
 * @author bdc34  
 */
public class DefaultDataPropertyFormValidator implements N3ValidatorVTwo{
	private Log log = LogFactory.getLog(DefaultDataPropertyFormValidator.class);

    VitroRequest vreq;
    private String datatype;
	private final I18nBundle i18n;
    private final String dtRegex = "^([0-9]{4})-((0[1-9])|(1[0-2]))-((0[1-9])|([1-2][0-9])|(3[0-1]))(T|\\s)(([0-1][0-9])|(2[0-3])):([0-5][0-9]):([0-5][0-9])";
    private final Pattern dtPattern = Pattern.compile(dtRegex);
    private final String dateRegex = "^([0-9]{4})-([0-9]{1,2})-([0-9]{1,2})";
    private final Pattern datePattern = Pattern.compile(dateRegex);
    private final String timeRegex = "^(([0-1][0-9])|(2[0-3])):([0-5][0-9]):([0-5][0-9])";
    private final Pattern timePattern = Pattern.compile(timeRegex);
    private final String yearRegex = "^\\d{4}";
    private final Pattern yearPattern = Pattern.compile(yearRegex);
    private final String ymRegex = "^([0-9]{4})-(0[1-9]|1[012])";
    private final Pattern ymPattern = Pattern.compile(ymRegex);
    private final String monthRegex = "^--(0[1-9]|1[012])";
    private final Pattern monthPattern = Pattern.compile(monthRegex);
    private final String floatRegex = "^[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?.";
    private final Pattern floatPattern = Pattern.compile(floatRegex);
    private final String intRegex = "^-?\\d+$";
    private final Pattern intPattern = Pattern.compile(intRegex);
    
    public DefaultDataPropertyFormValidator(String datatype, VitroRequest vreq) {
        this.datatype = datatype;
		this.vreq = vreq;
		this.i18n = I18n.bundle(vreq);
    }

    @Override
    public Map<String, String> validate(EditConfigurationVTwo editConfig,
            MultiValueEditSubmission editSub) {

        Map<String,List<Literal>> literalsFromForm = editSub.getLiteralsFromForm();

        Map<String,String> errors = new HashMap<String,String>();   
        
        List<Literal> formLiterals = literalsFromForm.get("literal");
        Literal literal = null;
        if(formLiterals != null && formLiterals.size() > 0) {
    	    literal = formLiterals.get(0);
        }
        String literalValue = "";
        if (literal != null) {
            literalValue = literal.getLexicalForm();
            if( "".equals(literalValue) ) {
                literal = null;
            }
        }
		
		if ( literal != null ) {

	        if ( datatype.indexOf("#dateTime") > -1 ) {
	        	if ( !dtPattern.matcher(literalValue).matches() ) {
	            	errors.put("dateTime", i18n.text("minimum_ymd"));
				}
	        }
			else if ( datatype.indexOf("#date") > -1 ) {
	        	if ( !datePattern.matcher(literalValue).matches() ) {
	            	errors.put("date", i18n.text("year_month_day"));
				}
			}
			else if ( datatype.indexOf("#time") > -1 ) {
	        	if ( !timePattern.matcher(literalValue).matches() ) {
	            	errors.put("time", i18n.text("minimum_hour"));
				}
			}
	        else if ( datatype.indexOf("#gYearMonth") > -1 ) {
	        	if ( !ymPattern.matcher(literalValue).matches() ) {
	            	errors.put("yearMonth", i18n.text("year_month"));
				}
	        }
			else if ( datatype.indexOf("#gYear") > -1 ) {
	        	if ( !yearPattern.matcher(literalValue).matches() ) {
	            	errors.put("year", i18n.text("four_digit_year"));
				}
			}
			else if ( datatype.indexOf("#float") > -1 ) {
	        	if ( !floatPattern.matcher(literalValue).matches() ) {
	            	errors.put("float", i18n.text("decimal_only"));
				}
			}
			else if ( datatype.indexOf("#int") > -1 ) {
	        	if ( !intPattern.matcher(literalValue).matches() ) {
	            	errors.put("integer", i18n.text("whole_number"));
				}
			}
		}
        else {
            return null;
        }               
        
        return errors.size() != 0 ? errors : null;
    }
}