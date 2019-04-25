/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.ResourceFactory;

import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.FieldVTwo;
import freemarker.template.Configuration;

/**
 * This is intended to work in conjunction with a template to create the HTML for a
 * datetime with precision and to convert the submitted parameters into
 * varname -&gt; Literal and varname -&gt; URI maps.
 *
 * The variables that get passed to the template are defined in:
 * DateTimeWithPrecision.getMapForTemplate()
 *
 * Two variables will be defined for the N3 edit graphs (These are NOT variables passed to FM templates):
 * $fieldname.precision - URI of datetime precision
 * $fieldname.value - DateTime literal
 *
 */
public class DateTimeWithPrecisionVTwo extends BaseEditElementVTwo {

    /**
     * This is the minimum datetime precision that this element
     * will accept.  If the parameters submitted do not meet this
     * requirement, then a validation error will be created.
     */
    VitroVocabulary.Precision minimumPrecision;

    /**
     * This is the maximum precision that the form should
     * allow the user to enter.  This value is not used by
     * DateTimeWithPrecision for validation, it is only passed
     * to the template.  This should be removed when it can be
     * specified in a ftl file.
     *
     * This could be thought of as the maximum precision to display.
     */
    VitroVocabulary.Precision displayRequiredLevel;


    VitroVocabulary.Precision DEFAULT_MIN_PRECISION = VitroVocabulary.Precision.DAY;
    VitroVocabulary.Precision DEFAULT_DISPLAY_LEVEL = VitroVocabulary.Precision.DAY;
    VitroVocabulary.Precision[] precisions = VitroVocabulary.Precision.values();

    protected static final String BLANK_SENTINEL = ">SUBMITTED VALUE WAS BLANK<";

    public DateTimeWithPrecisionVTwo(FieldVTwo field) {
        super(field);
        minimumPrecision = DEFAULT_MIN_PRECISION;
        displayRequiredLevel = DEFAULT_DISPLAY_LEVEL;
    }

    public DateTimeWithPrecisionVTwo(FieldVTwo field, VitroVocabulary.Precision minimumPrecision){
        this(field);
        if( minimumPrecision != null )
            this.minimumPrecision = minimumPrecision;
        else
            this.minimumPrecision = DEFAULT_MIN_PRECISION;
        this.displayRequiredLevel = this.minimumPrecision;
    }

    //it would be nice to have only the version of the constructor that takes the enum
    //but this is to quickly get the JSP configuration working.
    public DateTimeWithPrecisionVTwo(FieldVTwo field, String minimumPrecisionURI, String displayRequiredLevelUri){
        this(field);

        this.minimumPrecision = toPrecision( minimumPrecisionURI);
        if( this.minimumPrecision == null )
            throw new IllegalArgumentException(minimumPrecisionURI
                    +" is not a valid precision for minimumPrecision, see VitroVocabulary.Precision");

        this.displayRequiredLevel = toPrecision( displayRequiredLevelUri );
        if( this.displayRequiredLevel == null )
            throw new IllegalArgumentException(displayRequiredLevelUri
                    +" is not a valid precision for displayRequiredLevel, see VitroVocabulary.Precision");

//        if( this.displayRequiredLevel.ordinal() < this.minimumPrecision.ordinal() ){
//            throw new IllegalArgumentException("the display precision level " + this.displayRequiredLevel
//                    + " is less precise than the required minimum precision of " + this.minimumPrecision);
//        }
    }

    private String getFieldName(){
        return field.getName();
    }

    private static final Log log = LogFactory.getLog(DateTimeWithPrecisionVTwo.class);
    protected String TEMPLATE_NAME = "dateTimeWithPrecision.ftl";

    @Override
    public String draw(String fieldName, EditConfigurationVTwo editConfig,
            MultiValueEditSubmission editSub, Configuration fmConfig) {
        Map map = getMapForTemplate( editConfig, editSub);
        return merge( fmConfig, TEMPLATE_NAME, map);
    }

    /**
     * This produces a map for use in the template. Will be using this b/c
     */
    public Map getMapForTemplate(EditConfigurationVTwo editConfig, MultiValueEditSubmission editSub) {
        Map<String,Object>map = new HashMap<String,Object>();

        //always need the fieldName, required precision, and constants
        map.put("fieldName", getFieldName());
        addPrecisionConstants(map);
        map.put("minimumPrecision", minimumPrecision.uri());
        map.put("requiredLevel", displayRequiredLevel.uri());

        //Still expecting single precision uri not multiple
        String precisionUri= getPrecision(editConfig,editSub);

        VitroVocabulary.Precision existingPrec = toPrecision(precisionUri);

        if( precisionUri != null && !"".equals(precisionUri) && existingPrec == null ){
            if( ! BLANK_SENTINEL.equals( precisionUri )){
                log.debug("field " + getFieldName() + ": existing precision uri was " +
            		"'" + precisionUri + "' but could not convert to Precision object");
            }
        }

        if( precisionUri == null || precisionUri.isEmpty() || existingPrec == null){
            map.put("existingPrecision", "");

            /* no precision so there should also be no datetime */
            DateTime value = getTimeValue(editConfig,editSub);
            if( value != null )
                log.debug("Unexpected state: Precision for " + getFieldName()
                        + " was '" + precisionUri + "' but date time was " + value);

            map.put("year", "");
            map.put("month", "");
            map.put("day", "");
            map.put("hour", "");
            map.put("minute", "");
            map.put("second", "") ;
        } else if( VitroVocabulary.Precision.NONE.uri().equals(precisionUri) ){
            //bdc34: not sure what to do with the NONE precision
            map.put("existingPrecision", precisionUri);

            map.put("year", "");
            map.put("month", "");
            map.put("day", "");
            map.put("hour", "");
            map.put("minute", "");
            map.put("second", "") ;
        }else{
            map.put("existingPrecision", precisionUri);

            DateTime value = getTimeValue(editConfig,editSub);
            /* This is the case where there is a precision so there should be a datetime */
            if( value == null ) {
            	//If there is no value, then this is an error condition
                log.error("Field " + getFieldName() + " has precision " + precisionUri
                        + " but the date time value is null ");
                map.put("year", "");
                map.put("month", "");
                map.put("day", "");
                map.put("hour", "");
                map.put("minute", "");
                map.put("second", "") ;

            } else {

	            /* only put the values in the map for ones which are significant based on the precision */
	            if( existingPrec.ordinal() >= VitroVocabulary.Precision.SECOND.ordinal() )
	            		map.put("second", Integer.toString(value.getSecondOfMinute() )) ;
	            else
	                map.put("second", "");

	            if( existingPrec.ordinal() >= VitroVocabulary.Precision.MINUTE.ordinal()  )
	                map.put("minute", Integer.toString(value.getMinuteOfHour()) );
	            else
	                map.put("minute", "");

	            if( existingPrec.ordinal() >= VitroVocabulary.Precision.HOUR.ordinal() )
	                map.put("hour", Integer.toString(value.getHourOfDay()) );
	            else
	                map.put("hour", "");

	            if( existingPrec.ordinal() >= VitroVocabulary.Precision.DAY.ordinal()  )
	                map.put("day", Integer.toString(value.getDayOfMonth()) );
	            else
	                map.put("day", "");

	            if( existingPrec.ordinal() >= VitroVocabulary.Precision.MONTH.ordinal() )
	                map.put("month", Integer.toString(value.getMonthOfYear()));
	            else
	                map.put("month", "");

	            if( existingPrec.ordinal() >= VitroVocabulary.Precision.YEAR.ordinal() )
	                map.put("year", Integer.toString(value.getYear()));
	            else
	                map.put("year", "");
	            }
        }

        return map;
    }

    /** Adds precisionURIs for use by the templates */
    private void addPrecisionConstants(Map<String,Object> map){
        Map<String,Object> constants = new HashMap<String,Object>();
        for( VitroVocabulary.Precision pc: VitroVocabulary.Precision.values()){
            constants.put(pc.name().toLowerCase(),pc.uri());
        }
        map.put("precisionConstants", constants);
    }

    /**
     * Gets the currently set precision.  May return null.
     */
    private String getPrecision(EditConfigurationVTwo editConfig, MultiValueEditSubmission editSub) {
        if( editSub != null ){
            List<String> submittedPrecisionURI = editSub.getUrisFromForm().get( getPrecisionVariableName() );
            if( submittedPrecisionURI != null
            		&& submittedPrecisionURI.size() > 0
            		&& submittedPrecisionURI.get(0) != null){
                return submittedPrecisionURI.get(0);
            }
        }

        List<String> existingPrecisionURI = editConfig.getUrisInScope().get( getPrecisionVariableName() );
        if( existingPrecisionURI != null
        		&& existingPrecisionURI.size() > 0
        		&& existingPrecisionURI.get(0) != null){
        	//TODO: Check if relevant to return more than one element
            return existingPrecisionURI.get(0);
        }else{
            return null;
        }
    }

    private DateTime getTimeValue(EditConfigurationVTwo editConfig, MultiValueEditSubmission editSub) {
        if( editSub != null ){
            List<Literal> submittedValue = editSub.getLiteralsFromForm().get( getValueVariableName() );
            if( submittedValue != null ) {
            	//TODO: Check what to do with multiple values, currently handling one value
            	if(submittedValue.size() > 0 && submittedValue.get(0) != null) {
            		return new DateTime( submittedValue.get(0).getLexicalForm() );
            	}
            }
        }

        List<Literal> dtValue = editConfig.getLiteralsInScope().get( getValueVariableName() );
        if( dtValue != null ){
        	if(dtValue.size() > 0 && dtValue.get(0) != null)
        	{
        		return new DateTime( dtValue.get(0).getLexicalForm() );
        	}
        }

        return null;
    }

    /**
     * This gets the literals for a submitted form from the queryParmeters.
     * It will only be called if getValidationErrors() doesn't return any errors.
     */
    @Override
    public Map<String, List<Literal>> getLiterals(String fieldName,
            EditConfigurationVTwo editConfig, Map<String, String[]> queryParameters) {
        Map<String,List<Literal>> literalMap = new HashMap<String,List<Literal>>();

        Literal datetime =getDateTime( queryParameters);
        List<Literal> literals = new ArrayList<Literal>();
        literals.add(datetime);
        literalMap.put(fieldName+"-value", literals);

        return literalMap;
    }

    protected Literal getDateTime(  Map<String, String[]> queryParameters ) {
        String submittedPrec = BLANK_SENTINEL;
        try {
            submittedPrec = getSubmittedPrecision( queryParameters);
        } catch (Exception e) {
            log.error("could not get submitted precsion",e);
        }

        if( BLANK_SENTINEL.equals( submittedPrec ) )
            return null;

        Integer year = parseToInt(getFieldName()+"-year", queryParameters);

        //this is the case where date has not been filled out at all.
        if( year == null )
            return null;

        Integer month = parseToInt(getFieldName()+"-month", queryParameters);
        if( month == null || month == 0 )
            month = 1;
        Integer day = parseToInt(getFieldName()+"-day", queryParameters);
        if( day == null || day == 0 )
            day = 1;
        Integer hour = parseToInt(getFieldName()+"-hour", queryParameters);
        if( hour == null )
            hour = 0;
        Integer minute = parseToInt(getFieldName()+"-minute", queryParameters);
        if( minute == null )
            minute = 0;
        Integer second = parseToInt(getFieldName()+"-second", queryParameters);
        if( second == null )
            second = 0;

        DateTime value = new DateTime(
                year.intValue(),month.intValue(),day.intValue(),
                hour.intValue(),minute.intValue(),second.intValue(),0/*millis*/
        );

        return ResourceFactory.createTypedLiteral(
                ISODateTimeFormat.dateHourMinuteSecond().print(value), /*does not include timezone*/
                XSDDatatype.XSDdateTime);
    }

    /**
     * This gets the URIs for a submitted form from the queryParmeters.
     * It will only be called if getValidationErrors() doesn't return any errors.
     */
    @Override
    public Map<String, List<String>> getURIs(String fieldName,
            EditConfigurationVTwo editConfig, Map<String, String[]> queryParameters) {
        String precisionUri;
        try {
            precisionUri = getSubmittedPrecision( queryParameters);
        } catch (Exception e) {
            log.error("getURIs() should only be called on input that passed getValidationErrors()");
            return Collections.emptyMap();
        }
        Map<String,List<String>> uriMap = new HashMap<String,List<String>>();
        if( precisionUri != null ){
        	List<String> uris = new ArrayList<String>();
        	uris.add(precisionUri);
            uriMap.put(fieldName+"-precision", uris);
        }
        return uriMap;
    }

    /**
     * Precision is based on the values returned by the form. Throws an exception with
     * the error message if the queryParameters cannot make a valid date/precision because
     * there are values missing.
     */
    protected String getSubmittedPrecision(Map<String, String[]> queryParameters) throws Exception {

        Integer year = parseToInt(getFieldName()+"-year",queryParameters);
        Integer month = parseToInt(getFieldName()+"-month",queryParameters);
        Integer day = parseToInt(getFieldName()+"-day",queryParameters);
        Integer hour  = parseToInt(getFieldName()+"-hour",queryParameters);
        Integer minute = parseToInt(getFieldName()+"-minute",queryParameters);
        Integer second = parseToInt(getFieldName()+"-second",queryParameters);
        Integer[] values = { year, month, day, hour, minute, second };

        /*  find the most significant date field that is null. */
        int indexOfFirstNull= -1;
        for(int i=0; i < values.length ; i++){
            if( values[i] == null ){
                indexOfFirstNull = i;
                break;
            }
        }

        /* the field wasn't filled out at all */
        if( indexOfFirstNull == 0 )
            //return VitroVocabulary.Precision.NONE.uri();
            return BLANK_SENTINEL;

        /* if they all had values then we have seconds precision */
        if( indexOfFirstNull == -1 )
            return VitroVocabulary.Precision.SECOND.uri();


        /* check that there are no values after the most significant null field
         * that are non-null. */
        boolean nonNullAfterFirstNull=false;
        for(int i=0; i < values.length ; i++){
            if( i > indexOfFirstNull && values[i] != null ){
                nonNullAfterFirstNull = true;
                break;
            }
        }
        if( nonNullAfterFirstNull )
            throw new Exception("Invalid date-time value. When creating a date-time value, there cannot be gaps between any of the selected fields.");
        else{
            return precisions[ indexOfFirstNull ].uri();
        }
    }

    @Override
    public Map<String, String> getValidationMessages(String fieldName,
            EditConfigurationVTwo editConfig, Map<String, String[]> queryParameters) {
        Map<String,String> errorMsgMap = new HashMap<String,String>();

        //check that any parameters we got are single values
        String[] names = {"year","month","day","hour","minute","second", "precision"};
        for( String name:names){
            if ( !hasNoneOrSingle(fieldName+"-"+name, queryParameters))
                errorMsgMap.put(fieldName+"-"+name, "must have only one value for " + name);
        }

        String precisionURI = null;
        try{
            precisionURI = getSubmittedPrecision( queryParameters);
        }catch(Exception ex){
            errorMsgMap.put(fieldName,ex.getMessage());
            return errorMsgMap;
        }

        errorMsgMap.putAll(checkDate( precisionURI,  queryParameters) );

        return errorMsgMap;
    }

    /**
     * This checks for invalid date times.
     */
    final static String NON_INTEGER_YEAR = "must enter a valid year";
    final static String NON_INTEGER_MONTH = "must enter a valid month";
    final static String NON_INTEGER_DAY = "must enter a valid day";
    final static String NON_INTEGER_HOUR = "must enter a valid hour";
    final static String NON_INTEGER_MINUTE = "must enter a valid minute";
    final static String NON_INTEGER_SECOND = "must enter a valid second";

    private Map<String,String> checkDate( String precisionURI, Map<String, String[]> qp){
        if( precisionURI == null )
            return Collections.emptyMap();

        Map<String,String> errors = new HashMap<String,String>();

        Integer year,month,day,hour,minute,second;

        //just check if the values for the precision parse to integers
        if( precisionURI.equals(VitroVocabulary.Precision.YEAR.uri() ) ){
            if( ! canParseToNumber(getFieldName()+"-year" ,qp))
                errors.put(getFieldName()+"-year", NON_INTEGER_YEAR);
        }else if( precisionURI.equals( VitroVocabulary.Precision.MONTH.uri() )){
            if( ! canParseToNumber(getFieldName()+"-year" ,qp))
                errors.put(getFieldName()+"-year", NON_INTEGER_YEAR);
            if( ! canParseToNumber(getFieldName()+"-month" ,qp))
                errors.put(getFieldName()+"-month", NON_INTEGER_MONTH);
        }else if( precisionURI.equals( VitroVocabulary.Precision.DAY.uri() )){
            if( ! canParseToNumber(getFieldName()+"-year" ,qp))
                errors.put(getFieldName()+"-year", NON_INTEGER_YEAR);
            if( ! canParseToNumber(getFieldName()+"-month" ,qp))
                errors.put(getFieldName()+"-month", NON_INTEGER_MONTH);
            if( ! canParseToNumber(getFieldName()+"-day" ,qp))
                errors.put(getFieldName()+"-day", NON_INTEGER_DAY);
        }else if( precisionURI.equals( VitroVocabulary.Precision.HOUR.uri() )){
            if( ! canParseToNumber(getFieldName()+"-year" ,qp))
                errors.put(getFieldName()+"-year", NON_INTEGER_YEAR);
            if( ! canParseToNumber(getFieldName()+"-month" ,qp))
                errors.put(getFieldName()+"-month", NON_INTEGER_MONTH);
            if( ! canParseToNumber(getFieldName()+"-day" ,qp))
                errors.put(getFieldName()+"-day", NON_INTEGER_DAY);
            if( ! canParseToNumber(getFieldName()+"-hour" ,qp))
                errors.put(getFieldName()+"-hour", NON_INTEGER_HOUR);
        }else if( precisionURI.equals( VitroVocabulary.Precision.MINUTE.uri() )){
            if( ! canParseToNumber(getFieldName()+"-year" ,qp))
                errors.put(getFieldName()+"-year", NON_INTEGER_YEAR);
            if( ! canParseToNumber(getFieldName()+"-month" ,qp))
                errors.put(getFieldName()+"-month", NON_INTEGER_MONTH);
            if( ! canParseToNumber(getFieldName()+"-day" ,qp))
                errors.put(getFieldName()+"-day", NON_INTEGER_DAY);
            if( ! canParseToNumber(getFieldName()+"-hour" ,qp))
                errors.put(getFieldName()+"-hour", NON_INTEGER_HOUR);
            if( ! canParseToNumber(getFieldName()+"-minute" ,qp))
                errors.put(getFieldName()+"-minute", NON_INTEGER_HOUR);
        }else if( precisionURI.equals( VitroVocabulary.Precision.SECOND.uri() )){
            if( ! canParseToNumber(getFieldName()+"-year" ,qp))
                errors.put(getFieldName()+"-year", NON_INTEGER_YEAR);
            if( ! canParseToNumber(getFieldName()+"-month" ,qp))
                errors.put(getFieldName()+"-month", NON_INTEGER_MONTH);
            if( ! canParseToNumber(getFieldName()+"-day" ,qp))
                errors.put(getFieldName()+"-day", NON_INTEGER_DAY);
            if( ! canParseToNumber(getFieldName()+"-hour" ,qp))
                errors.put(getFieldName()+"-hour", NON_INTEGER_HOUR);
            if( ! canParseToNumber(getFieldName()+"-minute" ,qp))
                errors.put(getFieldName()+"-minute", NON_INTEGER_HOUR);
            if( ! canParseToNumber(getFieldName()+"-second" ,qp))
                errors.put(getFieldName()+"-second", NON_INTEGER_SECOND);
        }

        //check if we can make a valid date with these integers
        year = parseToInt(getFieldName()+"-year", qp);
        if( year == null )
            year = 1999;
        month= parseToInt(getFieldName()+"-month", qp);
        if(month == null )
            month = 1;
        day = parseToInt(getFieldName()+"-day", qp);
        if( day == null )
             day = 1;
        hour = parseToInt(getFieldName()+"-hour", qp);
        if( hour == null )
            hour = 0;
        minute = parseToInt(getFieldName()+"-minute",qp);
        if( minute == null )
            minute = 0;
        second = parseToInt(getFieldName()+"-second", qp);
        if( second == null )
            second = 0;

        //initialize to something so that we can be assured not to get
        //system date dependent behavior
        DateTime dateTime = new DateTime("1970-01-01T00:00:00Z");

        try{
            dateTime = dateTime.withYear(year);
        }catch(IllegalArgumentException iae){
           errors.put(getFieldName()+"-year", iae.getLocalizedMessage());
        }
        try{
            dateTime = dateTime.withMonthOfYear(month);
        }catch(IllegalArgumentException iae){
            errors.put(getFieldName()+"-month", iae.getLocalizedMessage());
        }
        try{
            dateTime = dateTime.withDayOfMonth(day);
        }catch(IllegalArgumentException iae){
            errors.put(getFieldName()+"-day", iae.getLocalizedMessage());
        }
        try{
            dateTime = dateTime.withHourOfDay(hour);
        }catch(IllegalArgumentException iae){
            errors.put(getFieldName()+"-hour", iae.getLocalizedMessage());
        }
        try{
            dateTime = dateTime.withSecondOfMinute(second);
        }catch(IllegalArgumentException iae){
            errors.put(getFieldName()+"-second", iae.getLocalizedMessage());
        }

        return errors;
    }


    private boolean fieldMatchesPattern( String fieldName, Map<String,String[]>queryParameters, Pattern pattern){
        String[] varg = queryParameters.get(fieldName);
        if( varg == null || varg.length != 1 || varg[0] == null)
            return false;
        String value = varg[0];
        Matcher match = pattern.matcher(value);
        return match.matches();
    }

    private boolean emptyOrBlank(String key,Map<String, String[]> queryParameters){
        String[] vt = queryParameters.get(key);
        return ( vt == null || vt.length ==0 || vt[0] == null || vt[0].length() == 0 );
    }

    private boolean canParseToNumber(String key,Map<String, String[]> queryParameters){
        Integer out = null;
        try{
            String[] vt = queryParameters.get(key);
            if( vt == null || vt.length ==0 || vt[0] == null)
                return false;
            else{
                out = Integer.parseInt(vt[0]);
                return true;
            }
        }catch(IndexOutOfBoundsException | NullPointerException | NumberFormatException iex){
            out =  null;
        }
        return false;
    }



    private Integer parseToInt(String key,Map<String, String[]> queryParameters){
        Integer out = null;
        try{
            String[] vt = queryParameters.get(key);
            if( vt == null || vt.length ==0 || vt[0] == null)
                out = null;
            else
                out = Integer.parseInt(vt[0]);
        }catch(IndexOutOfBoundsException | NullPointerException | NumberFormatException iex){
            out =  null;
        }
        return out;
    }

    public VitroVocabulary.Precision getRequiredMinimumPrecision() {
        return minimumPrecision;
    }

    public void setRequiredMinimumPrecision(
            VitroVocabulary.Precision requiredMinimumPrecision) {
        this.minimumPrecision = requiredMinimumPrecision;
    }

    /* returns null if it cannot convert */
    public static VitroVocabulary.Precision toPrecision(String precisionUri){
        for( VitroVocabulary.Precision precision : VitroVocabulary.Precision.values()){
            if( precision.uri().equals(precisionUri))
                return precision;
        }
        return null;
    }

    public String getValueVariableName(){ return getFieldName() + "-value" ; }
    public String getPrecisionVariableName(){ return getFieldName() + "-precision" ; }
}


