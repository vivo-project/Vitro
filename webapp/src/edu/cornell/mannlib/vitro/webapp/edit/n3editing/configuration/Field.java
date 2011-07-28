/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.cornell.mannlib.vitro.webapp.edit.elements.EditElement;

public class Field {

    public enum OptionsType {
        LITERALS, 
        HARDCODED_LITERALS,
        STRINGS_VIA_DATATYPE_PROPERTY, 
        INDIVIDUALS_VIA_OBJECT_PROPERTY, 
        INDIVIDUALS_VIA_VCLASS, 
        CHILD_VCLASSES, 
        CHILD_VCLASSES_WITH_PARENT,
        VCLASSGROUP,
        FILE, 
        UNDEFINED, 
        DATETIME, 
        DATE,
        TIME
    };

    public static String RDF_XML_LITERAL_URI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral";
        
    private boolean newResource;

    private static Log log = LogFactory.getLog( Field.class ); 
       
    private String name;
    
    /**
     * List of basic validators.  See BaiscValidation.
     */
    private List <String> validators;

    /**
     * What type of options is this?
     */
    private OptionsType optionsType;
    
    /**
     * Special class to use for option type
     */
    private Class customOptionType;
    
     /**
     * Used for  building Options when OptionsType is INDIVIDUALS_VIA_OBJECT_PROPERTY
     */
    private String predicateUri;
     /**
     * Used for  building Options when OptionsType is INDIVIDUALS_VIA_VCLASS
     */
    private String objectClassUri;
    
    /**
     * Used for holding the expected/required datatype of the predicate when the predicate is a datatype propertyl.
     * this can be a explicit URI or a qname.
     * example:
     *  "this is the literal"^^<http://someuri.com/v1.2#type23>
     *  or
     *  "this is the literal"^^someprefix:type23
     */
    private String rangeDatatypeUri;
    
    /**
     * Used for holding the language of the literal when the predicate is a datatype property.
     * This is the lang of the literal.  lang strings must be: [a-z]+(-[a-z0-9]+)*
     */
    private String rangeLang;

    /**
     * If this is a Select and it is of OptionsType LITERALS, these are the literals.
     */
    private List<List<String>> literalOptions;

    /**
     * Strings of N3 to add to model.
     */
    private List <String> assertions;

    /**
     * JSON configuration that was used to build this object.
     */ 
    private String originalJson;
    
    /**
     * Do not attempt to set the retractions when configuring a Field; they get built by the
     * edit processing object.
     *
     * The strings in this list should be N3 for statements that need to be retracted to affect an update.
     * Per Field retractions are necessary since we only want to retract for fields that have changed.
     * The Model should be checked to make sure that all of the retractions exist so we are changing the
     * statements that existed when this edit was configured.
     *
     * These retractions are just the assertions with the values subistituted in from before the change.
     */
    private List <String> retractions;

    private Map<String, String> queryForExisting;

    /**
     * Property for special edit element.
     */
    private EditElement editElement=null;;
        
    /* *********************** Constructors ************************** */

    public Field(String config, String varName) {
        name=varName;
        JSONObject jsonObj  = null;
        try{
            jsonObj = new JSONObject(config);
        }catch (JSONException je){
            throw new Error(je);
        }
        originalJson = config;
        setValuesFromJson(jsonObj, varName);
    }

    public Field(JSONObject obj, String varName) {
        setValuesFromJson(obj, varName);
    }

    public Field() {}
        
    private static String[] parameterNames = {"editElement","newResource","validators","optionsType","predicateUri","objectClassUri","rangeDatatypeUri","rangeLang","literalOptions","assertions"};
    static{  Arrays.sort(parameterNames); }
    
    private void setValuesFromJson(JSONObject obj, String fieldName){
        try{
            this.name = fieldName;
            setNewResource(obj.getBoolean("newResource"));
            validators = EditConfiguration.JsonArrayToStringList(obj.getJSONArray("validators"));
            setOptionsType(obj.getString("optionsType"));
            predicateUri = obj.getString("predicateUri");
            objectClassUri = obj.getString("objectClassUri");
            
            rangeDatatypeUri = obj.getString("rangeDatatypeUri");
            if( rangeDatatypeUri != null && rangeDatatypeUri.trim().length() == 0)
                rangeDatatypeUri = null;
            
            rangeLang = obj.getString("rangeLang");
            if( rangeLang != null && rangeLang.trim().length() == 0)
                rangeLang = null;
                        
            setLiteralOptions(obj.getJSONArray("literalOptions"));
            setAssertions(EditConfiguration.JsonArrayToStringList(obj.getJSONArray("assertions")));
                                          
            setEditElement( obj, fieldName);           
            
            //check for odd parameters
            JSONArray names = obj.names();
            int size = names.length();
            for(int i=0 ; i < size ; i++ ){
                String name = (String)names.optString(i);
                if( Arrays.binarySearch(parameterNames, name) < 0 )                
                    log.debug("setValuesFromJson(): the field  " + fieldName + " has the unrecognized parameter " + name);                                      
            }
            
        }catch(JSONException ex){
            throw new Error(ex);
        }
    }

    public void setEditElement(EditElement editElement){
        this.editElement = editElement;
    }
    
    /**
     * A field may specify a class for additional features. 
     */
    private void setEditElement(JSONObject fieldConfigObj, String fieldName) {        
        String className = fieldConfigObj.optString("editElement");
        if( className == null || className.isEmpty() )
            return;
        setOptionsType(Field.OptionsType.UNDEFINED);
        Class clz = null;
        try {
            clz = Class.forName(className);           
        } catch (ClassNotFoundException e) {
            log.error("Java Class " + className + " not found for field " + name);
            return;
        } catch (SecurityException e) {
            log.error("Problem with Java Class " + className + " for field " + name, e);
            return;
        } catch (IllegalArgumentException e) {
            log.error("Problem with Java Class " +className + " for field " + name, e);
            return;
        } 

        Class[] types = new Class[]{ Field.class };
        Constructor cons;
        try {
            cons = clz.getConstructor(types);
        } catch (SecurityException e) {
            log.error("Problem with Java Class " + className + " for field " + name, e);            
            return;                        
        } catch (NoSuchMethodException e) {
            log.error("Java Class " + className + " must have a constructor that takes a Field.", e);            
            return;
        }
        Object[] args = new Object[] { this };        
        Object obj;
        try {
            obj = cons.newInstance(args);
        } catch (Exception e) {
            log.error("Problem with Java Class " + className + " for field " + name, e);            
            return;   
        }  
        
        editElement = (EditElement)obj;                       
    }

    /* ****************** Getters and Setters ******************************* */

    public String getName(){
        return name;
    }
    
    public List<String> getRetractions() {
        return retractions;
    }

    public void setRetractions(List<String> retractions) {
        this.retractions = retractions;
    }

    public List<String> getAssertions() {
        return assertions;
    }

    public void setAssertions(List<String> assertions) {
        this.assertions = assertions;
    }

    public boolean isNewResource() {
        return newResource;
    }
    public void setNewResource(boolean b) {
        newResource = b;
    }

    public List <String> getValidators() {
        return validators;
    }
    public void setValidators(List <String> v) {
        validators = v;
    }

    public OptionsType getOptionsType() {
        return optionsType;
    }
    public void setOptionsType(OptionsType ot) {
        optionsType = ot;
    }
    public void setOptionsType(String s) {
        setOptionsType( getOptionForString(s));
    }

    public static OptionsType getOptionForString(String s){
        if( s== null || s.isEmpty() )
            return OptionsType.UNDEFINED;
        if ("LITERALS".equals(s)) {
            return Field.OptionsType.LITERALS;
        } else if ("HARDCODED_LITERALS".equals(s)) {
            return Field.OptionsType.HARDCODED_LITERALS;
        } else if ("STRINGS_VIA_DATATYPE_PROPERTY".equalsIgnoreCase(s)) {
            return Field.OptionsType.STRINGS_VIA_DATATYPE_PROPERTY;
        } else if ("INDIVIDUALS_VIA_OBJECT_PROPERTY".equalsIgnoreCase(s)) {
            return Field.OptionsType.INDIVIDUALS_VIA_OBJECT_PROPERTY;
        } else if ("INDIVIDUALS_VIA_VCLASS".equalsIgnoreCase(s)) {
            return Field.OptionsType.INDIVIDUALS_VIA_VCLASS;
        } else if ("DATETIME".equalsIgnoreCase(s)) {
            return Field.OptionsType.DATETIME;
        } else if ("CHILD_VCLASSES".equalsIgnoreCase(s)) {            
            return Field.OptionsType.CHILD_VCLASSES;
        } else if ("CHILD_VCLASSES_WITH_PARENT".equalsIgnoreCase(s)) {            
            return Field.OptionsType.CHILD_VCLASSES_WITH_PARENT;  
        } else if ("VCLASSGROUP".equalsIgnoreCase(s)) {            
            return Field.OptionsType.VCLASSGROUP;              
        } else if ("FILE".equalsIgnoreCase(s)) {
            return Field.OptionsType.FILE;            
        } else if ("DATE".equalsIgnoreCase(s)) {
            return Field.OptionsType.DATE;
        } else if ("TIME".equalsIgnoreCase(s)) {
            return Field.OptionsType.TIME;
        } else {
            return Field.OptionsType.UNDEFINED;
        } 
    }
    
    public String getPredicateUri() {
        return predicateUri;
    }
    public void setPredicateUri(String s) {
        predicateUri = s;
    }

    public String getObjectClassUri() {
        return objectClassUri;
    }
    public void setObjectClassUri(String s) {
        objectClassUri = s;
    }
    
    public String getRangeDatatypeUri() {
        return rangeDatatypeUri;
    }
    public void setRangeDatatypeUri(String r) {
        if( rangeLang != null && rangeLang.trim().length() > 0 )
            throw new IllegalArgumentException("A Field object may not have both rangeDatatypeUri and rangeLanguage set");
        
        rangeDatatypeUri = r;
    }

    public List <List<String>> getLiteralOptions() {
        return literalOptions;
    }
    public void setLiteralOptions(List<List<String>> literalOptions) {
        this.literalOptions = literalOptions;
    }
    
    /**
     * Expects a JSONArray of JSONArrays like: 
     * [ ["http://example.org/bob", "bob"] , ["http://example.org/kate", "kate"] ]
     */
    private void setLiteralOptions(JSONArray array) {
        if( array == null ) 
            literalOptions = Collections.EMPTY_LIST;
        
        literalOptions = Collections.EMPTY_LIST;
        List<List<String>> out = new ArrayList<List<String>>( array.length() );
        
        for(int i =0; i<array.length() ; i++){
            JSONArray pair = array.optJSONArray(i);            
            if( pair == null ){
                String value = array.optString(i);            
                if( value != null ){
                    List<String>option = new ArrayList<String>(2);
                    option.add(value);
                    option.add(value);
                    out.add( option );
                } else { log.warn("could not get option list for " + this.name ); }
            }else{
                if( pair.length() == 0 ){
                    log.warn("option list too short for " + this.name + ": " + array.opt(i));
                    continue;
                }
                if( pair.length() > 2 )
                    log.warn("option list too long for " + this.name + ": " + array.opt(i) + " using first two items");
                
                List<String>option = new ArrayList<String>(2);
                option.add(pair.optString(0));
                if( pair.length() > 1 )
                    option.add(pair.optString(1));
                else 
                    option.add(pair.optString(0));
                out.add( option );            
            }            
        }        
        literalOptions = out;
    }
    
    public String getRangeLang() {
        return rangeLang;
    }

    public void setRangeLang(String rangeLang) {
        if( rangeDatatypeUri != null && rangeDatatypeUri.trim().length() > 0)
            throw new IllegalArgumentException("A Field object may not have both rangeDatatypeUri and rangeLanguage set");
        
        this.rangeLang = rangeLang;
    }
    
    public Field copy(){
       Field copy = new Field(this.originalJson, name);       
       return copy;
    }

    public EditElement getEditElement(){
        return editElement;
    }
    
    /* this is mainly for unit testing */
    public void setName(String name){
        this.name = name;    
    }

}
