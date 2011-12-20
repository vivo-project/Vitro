/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.jsptags;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.vocabulary.XSD;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerConfigurationLoader;
import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.EditConfiguration;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.Field;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.SelectListGenerator;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.processEdit.EditSubmission;
import edu.cornell.mannlib.vitro.webapp.search.beans.ProhibitedFromSearch;
import freemarker.template.Configuration;

/**
 * This tag will build an option list for individuals of a VClass.
 *
 * User: bdc34
 * Date: Jan 4, 2008
 * Time: 12:16:29 PM
 */
public class InputElementFormattingTag extends TagSupport {
    private String  id;
    // NB name is optional. If not specified, the name comes from the id value.
    private String  name; 
    private String  type;
    private String  label;
    private String  cancelLabel;
    private String  cancelUrl;
    private String  cssClass;
    private String  labelClass;
    private String  disabled;
    private String  value;
    private String  error;
    private int     size = 0;
    private int     rows = 0;
    private int     cols = 0;
    private String  multiple = "";
    private String  listMarkup;
    private String  cancel;

    private static final Log log = LogFactory.getLog(InputElementFormattingTag.class.getName());

    public String getId() {
        return id;
    }
    public void setId(String idStr) {
        this.id = idStr;
    }

    public String getName() {
        return StringUtils.isEmpty(name) ? id : name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    public String getType() {
        return type;
    }
    public void setType(String typeStr) {
        this.type = typeStr;
    }

    public String getLabel() {
        return label;
    }
    public void setLabel(String labelStr) {
        this.label = labelStr;
    }

    public String getCancelLabel() {
        return cancelLabel;
    }
    public void setCancelLabel(String cancelLabel) {
        this.cancelLabel = cancelLabel;
    }
    public String getCancelUrl() {
        return cancelUrl;
    }
    public void setCancelUrl(String cancelUrl) {
        this.cancelUrl = cancelUrl;
    }
    public String getCssClass() {
        return cssClass;
    }
    public void setCssClass(String classStr) {
        this.cssClass = classStr;
    }
    public String getLabelClass() {
        return labelClass;
    }
    public void setLabelClass(String labelClassStr) {
        this.labelClass = labelClassStr;
    }
    public String getDisabled() {
        return disabled;
    }
    public void setDisabled(String disabled) {
        this.disabled = disabled;
    }
    public String getValue() {
        return value;
    }
    public void setValue(String valueStr) {
        this.value = valueStr;
    }

/*  public String getDefaultValue() {
        return defaultValue;
    }
    public void setDefaultValue(String valueStr) {
        this.defaultValue = valueStr;
    } */

    public String getError() {
        return error;
    }
    public void setError(String errorStr) {
        this.error = errorStr;
    }

    public int getSize() {
        return size;
    }
    public void setSize(int i) {
        size = i;
    }

    public int getRows() {
        return rows;
    }
    public void setRows(int i) {
        rows = i;
    }

    public int getCols() {
        return cols;
    }
    public void setCols(int i) {
        cols = i;
    }
    
    public String getMultiple() {
        return multiple;
    }
    public void setMultiple(String s) {
        multiple = s;
    }
    
    public String getListMarkup() {
        return listMarkup;
    }
    public void setListMarkup(String s){
        listMarkup = s;
    }

    public String getCancel() {
        return cancel;
    }
    public void setCancel(String s){
        cancel = s;
    }
    
    private String doLabelClass() {
        String labelClass = getLabelClass();
        if (labelClass != null && !labelClass.equals("")) {
            return " class=\""+labelClass+"\"";
        }
        return "";
    }

    private String doClass() {
        /* only insert the class attribute if it has been populated */
        if (getCssClass()!=null && !getCssClass().equals("")) {
            return "class=\""+getCssClass()+"\"";
        }
        return "";
    }
    
    private String doDisabled() {
        /* Only insert the disabled attribute if it has been populated. Support
         * both "true" and "disabled" as values. */
        String disabled = getDisabled();
        if ("true".equals(disabled) || "disabled".equals(disabled)) {
            return "disabled=\"disabled\"";
        }
        return "";
    }

    private String doSize() {
        if (getSize()>0) {
            return "size=\"" + getSize() + "\"";
        }
        return "";
    }

    private String doRows() {
        if (getRows()>0) {
            return "rows=\"" + getRows() + "\"";
        }
        return "";
    }

    private String doCols() {
        if (getCols()>0) {
            return "cols=\"" + getCols() + "\"";
        }
        return "";
    }

    /**
     * attempt to get value from editSub.  If that value is there then this is a re-draw
     * of the form after a validation failure.  If there is no editSub, or no value in
     * the editSub, then try to get it from the editConfig.  This is where the value would
     * be if the form is being built for an edit of an existing property.
     *
     */
    private String doValue(EditConfiguration editConfig, EditSubmission editSub) {
        Literal literal = null;
        String uri = null;
        if( editConfig == null && editSub == null ) {
            log.error("both editSub and editConfig null in InputElementFormattingTag.doValue()");
            return "";
        }
        try{
            Map<String, Literal> literals = null;
            Map<String, String> uris = null;
            if (editSub != null) { // are reloading a form after a validation error, look in editSub
                literals = editSub.getLiteralsFromForm();
                if( literals!= null && literals.containsKey(getName())){
                    literal = literals.get(getName());
                    if( literal != null ) {
                        return literalToString(literal);
                    }
                } else { //look for a uri
                    uris = editSub.getUrisFromForm();
                    if( uris != null )
                        uri = uris.get(getName());
                    if( uri != null ){
                        return uri;
                    }
                }
            } else { // check in editConfig
                literals = editConfig.getLiteralsInScope();
                if( literals != null && literals.containsKey(getName()) ) { 
                    literal = literals.get(getName());                    
                    return literalToString(literal);                    
                } else {
                    uris = editConfig.getUrisInScope();
                    if( uris != null && uris.containsKey( getName() ) && uris.get( getName() ) != null  ) {
                        return editConfig.getUrisInScope().get(getName());
                    }
                }
            }
        }catch(Exception ex){
            log.debug("doValue():", ex);
        }

        log.debug("doValue(): No existing or default value for key '"+getId()+"' found from in editConfig or"
                +" or editSub");
        return "";
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

    private String doCancel(String labelStr, EditConfiguration editConfig){
        if (labelStr==null || labelStr.equals("")) {
            labelStr = getCancelLabel();
            if (labelStr==null || labelStr.equals("")) {
                labelStr="Cancel";
            }
        }
        VitroRequest vreq = new VitroRequest((HttpServletRequest)pageContext.getRequest());
        if( "about".equals( getCancel() )){
        	return " or <a class=\"cancel\" href=\"" + vreq.getContextPath() 
        	+ Controllers.ABOUT + " title=\"Cancel\">"+labelStr+"</a>";
        } else if ( "admin".equals( getCancel() )){
            return " or <a class=\"cancel\" href=\"" + vreq.getContextPath()
            + Controllers.SITE_ADMIN + " title=\"Cancel\">"+labelStr+"</a>";
        }else if( "dashboard".equals( getCancel() )){ //this case is Datastar-specific.
            	return " or <a class=\"cancel\" href=\"" + vreq.getContextPath() 
            	+ "/dashboard\" title=\"Cancel\">"+labelStr+"</a>";
        }else if (getCancel()!=null && !getCancel().equals("") && !getCancel().equals("false")) {        	
            if( editConfig != null && editConfig.getEditKey() != null ){
                try{
                    String url =  vreq.getContextPath() + 
                                  "/edit/postEditCleanUp.jsp?editKey="+ 
                                  URLEncoder.encode(editConfig.getEditKey(),"UTF-8") +
                                  "&cancel=true";
                    String cancelUrl = getCancelUrl();
                    if (!StringUtils.isEmpty(cancelUrl)) {
                        url += "&url=" + cancelUrl;
                    }
                    return "<span class=\"or\"> or </span><a class=\"cancel\" href=\"" +
                        url + "\" title=\"Cancel\">"+labelStr+"</a>";
                }catch(UnsupportedEncodingException ex){
                    log.error( "doCancel(): " , ex);
                }
            }  else {//no edit config? try to just use the subjectUri.  This is the case when on propDelete.jsp                
                try {
                    String url = vreq.getContextPath();
                    if( vreq.getParameter("subjectUri") != null )
                        url += "/entity?uri=" + URLEncoder.encode( vreq.getParameter("subjectUri"),"UTF-8");
                    String pred = vreq.getParameter("predicateUri");
                    if( pred != null && pred.length() > 0 ){       
                        String[] kf = pred.split("#");
                        if( kf != null && kf.length == 2 )
                            url += "&property="   + URLEncoder.encode( kf[1],"UTF-8");
                    }
                    
                    return "or <a class=\"cancel\" href=\"" + url + "\" title=\"Cancel\">"+labelStr+"</a>";
                } catch (UnsupportedEncodingException ex) {
                    log.error( "doCancel(): " , ex);
                } 
            }
        }
        return "";
    }

    private String getValidationErrors(EditSubmission editSub) {
        if( editSub == null )
            return "";
        Map<String,String> errors = editSub.getValidationErrors();
        if( errors == null || errors.isEmpty())
            return "";
           
        String val = errors.get(getName());
        if( val != null){
            return val;
        }
        return "";
    }


    public int doStartTag() {
        try {

            if (getId()==null || getId().equals("")){
                log.error("Error in doStartTag: input element id is blank or not specified.");
            }
            
            HttpSession session = pageContext.getSession();
            EditConfiguration editConfig = EditConfiguration.getConfigFromSession(session,(HttpServletRequest) pageContext.getRequest());
            EditSubmission editSub = EditSubmission.getEditSubmissionFromSession(session,editConfig);
            
            VitroRequest vreq = new VitroRequest((HttpServletRequest)pageContext.getRequest());
            WebappDaoFactory wdf;
            if (editConfig != null) { 
            	wdf = editConfig.getWdfSelectorForOptons().getWdf(
                        (HttpServletRequest)pageContext.getRequest(),
                                            pageContext.getServletContext());
            } else {                
                wdf = vreq.getWebappDaoFactory();
            }
            
            //get freemarker Configuration
            Configuration fmConfig = FreemarkerConfigurationLoader.getConfig(vreq, pageContext.getServletContext());
            
            /* populate the pieces */
            String classStr = doClass();
            String disabledStr = doDisabled();
            String errorStr = getValidationErrors(editSub);
            JspWriter out = pageContext.getOut();

            boolean definitionTags = false; // current default
            /* if (getListMarkup()!=null && getListMarkup().equalsIgnoreCase("definition")) {
                definitionTags=true;
            } */
            
            if( getLabel()!=null && !getLabel().equals("")) {
                if (definitionTags) { out.println("<dt>"); }
                out.println("<label" + doLabelClass() + " for=\""+getId()+"\">"+getLabel()+"</label>");
                if (definitionTags) { out.println("</dt>"); }
            }
            
   
            // bdc34 2010-11-08 field may be null
            Field field = editConfig == null ? null : editConfig.getField(getId());
            
              /* bdc34 2010-11-08 : this is odd, I'm having problems with the submit on this next line because
               * it is not a field in the editConfig.  This wasn't happening before.  */
//            if( field == null ){
//                log.error("could not get field for id " + getId());
//                return SKIP_BODY;
//            }
            
           
            if ((getType()==null || getType().equals("")) && (field != null && field.getEditElement() == null )){
                log.error("Error in doStartTag: input element type not specified and editElement not specified.");
            }
            
            // set ProhibitedFromSearch object so picklist doesn't show
            // individuals from classes that should be hidden from list views
        	OntModel displayOntModel = 
    		    (OntModel) pageContext.getServletContext()
    		        .getAttribute("displayOntModel");
        	if (displayOntModel != null) {
    	     	ProhibitedFromSearch pfs = new ProhibitedFromSearch(
    				DisplayVocabulary.SEARCH_INDEX_URI, displayOntModel);
    	     	if( editConfig != null )
    	     		editConfig.setProhibitedFromSearch(pfs);
        	}
           
        	if( field != null && field.getEditElement() != null ){
        	    out.print( field.getEditElement().draw(getId(), editConfig, editSub, fmConfig));
        	}else if( getType() == null ){
        	    log.error("type or editElement must be specified for input element " + getId() );
        	}else if( getType().equalsIgnoreCase("date") || 
                    (field != null && field.getRangeDatatypeUri() != null && field.getRangeDatatypeUri().equals(XSD.date.getURI())) ){
                //if its a dataprop that should be a string override type and use date picker    
                if (definitionTags) { out.print("<dg>"); }
                out.print(  generateHtmlForDate(getId(),editConfig,editSub)  );
                if (definitionTags) { out.print("</dg>"); }                                
            } else if ( getType().equalsIgnoreCase("time") || 
            		(field != null && field.getRangeDatatypeUri() != null && field.getRangeDatatypeUri().equals(XSD.time.getURI()))  ) {
            	if (definitionTags) { out.print("<dd>"); }
                out.print(  generateHtmlForTime(getId(),editConfig,editSub)  );
                if (definitionTags) { out.print("</dd>\n"); }
            } else if ( getType().equalsIgnoreCase("datetime") ||
                    (field != null && field.getRangeDatatypeUri() != null && field.getRangeDatatypeUri().equals(XSD.dateTime.getURI()))) {
                if (definitionTags) { out.print("<dd>"); }
                out.print(  generateHtmlForDateTime(getId(),editConfig,editSub)  );
                if (definitionTags) { out.print("</dd>\n"); }
            } else if( getType().equalsIgnoreCase("text")) {
                String valueStr = doValue(editConfig, editSub);
                String sizeStr  = doSize();
                if (definitionTags) { out.print("<dd>"); }
                out.print("<input "+classStr+" "+sizeStr+" " + disabledStr + " type=\"text\" id=\""+getId()+"\" name=\""+getName()+"\" value=\""+valueStr+"\" />");
                if (definitionTags) { out.print("</dd>"); }
                out.println();
            // Handle hidden inputs where Javascript writes a value that needs to be returned with an invalid submission.
            } else if( getType().equalsIgnoreCase("hidden")) {
                String valueStr = doValue(editConfig, editSub);
                if (definitionTags) { out.print("<dd>"); }
                out.print("<input "+classStr+ "type=\"hidden\" id=\""+getId()+"\" name=\""+getName()+"\" value=\""+valueStr+"\" />");
                if (definitionTags) { out.print("</dd>"); }
                out.println();
            } else if (getType().equalsIgnoreCase("textarea")) {
                String valueStr = doValue(editConfig, editSub);
                String rowStr = doRows();
                String colStr = doCols();
                if (definitionTags) { out.print("<dd>"); }
                out.print("<textarea "+classStr+" id=\""+getId()+"\" name=\""+getName()+"\" "+rowStr+" "+colStr+" >"+valueStr+"</textarea>");
                if (definitionTags) { out.print("</dd>"); }
                out.println();
            } else if( getType().equalsIgnoreCase("select")) {
                String valueStr = doValue(editConfig, editSub);
                //String sizeStr = getSize(); //"style=\"width:"+getSize()+"%;\"";
                Map<String,String> optionsMap = (Map<String,String>) pageContext.getRequest().getAttribute("rangeOptions." + getId());
                if (optionsMap == null) {
                	optionsMap = SelectListGenerator.getOptions(editConfig,getName(), wdf);
                }
                if (optionsMap==null){
                    log.error("Error in InputElementFormattingTag.doStartTag(): null optionsMap returned from getOptions()");
                }
                if (optionsMap.size()>0) { // e.g., an Educational Background where may be no choices left after remove existing
                    if (definitionTags) { out.print("<dd>"); }
                    if (multiple!=null && !multiple.equals("")) {
                        out.print("<select "+ classStr+ " " + disabledStr + " id=\""+getId()+"\" name=\""+getName()+"\" multiple=\"multiple\" size=\""+(optionsMap.size() > 10? "10" : optionsMap.size())+"\">");
                    } else {
                        out.print("<select "+classStr+ " " + disabledStr + " id=\""+getId()+"\" name=\""+getName()+"\">");
                    }
                    
                    Field thisField = editConfig.getField(getName());
                    if (! thisField.getOptionsType().equals(Field.OptionsType.HARDCODED_LITERALS)) {
                        optionsMap = getSortedMap(optionsMap);
                    }
                    Iterator iter = optionsMap.keySet().iterator();
                    while (iter.hasNext()) {
                        String key = (String) iter.next();
                        String mapValue = optionsMap.get(key);
                        out.print("    <option value=\""+StringEscapeUtils.escapeHtml(key)+"\"");
                        if( key.equals( valueStr )){
                            out.print(" selected=\"selected\"");
                        }
                        out.println(">"+ StringEscapeUtils.escapeHtml(trim(mapValue,getSize())) +"</option>");
                    }
                    out.print("</select>");
                    if (definitionTags) { out.print("</dd>"); }
                } else {
                    out.println("<p><input type=\"hidden\" id=\""+getId()+"\" name=\""+getName()+"\" value=\""+valueStr+"\"/>no appropriate choice available</p>");
                }
            } else if( getType().equalsIgnoreCase("checkbox")) {
                String valueStr = doValue(editConfig, editSub);
                if (definitionTags) { out.print("<dd>"); }
                Map<String,String> optionsMap = (Map<String,String>) pageContext.getRequest().getAttribute("rangeOptions." + getId());
                if (optionsMap == null) {
                    optionsMap = SelectListGenerator.getOptions(editConfig,getName(),wdf);
                }
                if (optionsMap==null){
                    log.error("Error in InputElementFormattingTag.doStartTag(): null optionsMap returned from getOptions()");
                }
                optionsMap = getSortedMap(optionsMap);
                Iterator iter = optionsMap.keySet().iterator();
                while (iter.hasNext()) {
                    String key = (String) iter.next();
                    String mapValue = optionsMap.get(key);
                    out.print("<input "+classStr+" type=\"checkbox\" name=\""+getName()+"\" value=\""+StringEscapeUtils.escapeHtml(key)+"\"");
                    if( key.equals( valueStr )){
                        out.print(" checked=\"checked\"");
                    }
                    out.println(">"+ StringEscapeUtils.escapeHtml(trim(mapValue,getSize())) +"</input>");
                }
                if (definitionTags) { out.print("</dd>"); }
            } else if( getType().equalsIgnoreCase("radio")) {
                String valueStr = doValue(editConfig, editSub);
                if (definitionTags) { out.print("<dd>"); }
                Map<String,String> optionsMap = (Map<String,String>) pageContext.getRequest().getAttribute("rangeOptions." + getId());
                if (optionsMap == null) {
                    optionsMap = SelectListGenerator.getOptions(editConfig,getName(),wdf);
                }
                if (optionsMap==null){
                    log.error("Error in InputElementFormattingTag.doStartTag(): null optionsMap returned from getOptions()");
                }
                optionsMap = getSortedMap(optionsMap);
                Iterator iter = optionsMap.keySet().iterator();
                while (iter.hasNext()) {
                    String key = (String) iter.next();
                    String mapValue = optionsMap.get(key);
                    out.print("<input "+classStr+" type=\"radio\" name=\""+getName()+"\" value=\""+StringEscapeUtils.escapeHtml(key)+"\"");
                    if( key.equals( valueStr )){
                        out.print(" checked=\"checked\"");
                    }
                    out.println(">"+ StringEscapeUtils.escapeHtml(trim(mapValue,getSize())) +"</input>");
                }
                if (definitionTags) { out.print("</dd>"); }
            } else if( getType().equalsIgnoreCase("file")) {

                String fieldName = getName();
                if( editConfig.getUrisInScope().containsKey(fieldName) ){
                    //if there is a link to a file Individual then this is 
                    //a update and we should just disable the control
                    out.println("<input type='file' disabled='true'/>");
                } else {
                    if(definitionTags) {out.print("<dd>");}
                    out.print("<input type=\"file\" id=\""+getId()+"\" name=\""+getName() +"\" />");
                    if(definitionTags) {out.print("</dd>");}
                    out.print("\n");
                }
            } else if( getType().equalsIgnoreCase("cancel")) {
                out.println(doCancel(getLabel(),editConfig));
            } else if( getType().equalsIgnoreCase("submit")) { // add cancel URL, too
                
                if( editConfig != null && editConfig.getEditKey() != null )
                    out.println("<input type=\"hidden\" name = \"editKey\" value=\""+ editConfig.getEditKey() +"\"/>");
                
                out.println("<input type=\"submit\" id=\""+getId()+"\" value=\""+getValue()+"\"/>");
                out.println( doCancel("",editConfig) );
            } else if( getType().equals("typesForCreateNew")){

            	//this is only for use on the defaultObjPropForm.jsp to create a select list of types.            	                  
            	Map <String,String> optionsMap = getTypesForCreateNew(editConfig,wdf);
            	if (optionsMap==null){
            		log.error("Error in InputElementFormattingTag.doStartTag(): null optionsMap returned from getTypesForCreateNew()");
            	}
            	if (optionsMap.size()>0) {
            		if (definitionTags) { out.print("<dd>"); }
            		out.print("<select "+classStr+" id=\""+getId()+"\" name=\""+getName()+"\">");                       
            		optionsMap = getSortedMap(optionsMap);
            		Iterator<String> iter = optionsMap.keySet().iterator();
            		while (iter.hasNext()) {
            			String key = iter.next();
            			String mapValue = optionsMap.get(key);
            			out.print("    <option value=\""+StringEscapeUtils.escapeHtml(key)+"\"");
            			out.println(">"+ StringEscapeUtils.escapeHtml(trim(mapValue,getSize())) +"</option>");
            		}
            		out.print("</select>");
            		if (definitionTags) { out.print("</dd>"); }            	
            	}
                   
            } else if( getType().equalsIgnoreCase("editKey")) {
                log.warn("Input element of type editKey was ignored, editKey fields are created by InputElementFormat submit and cancel.");
            } 
            //added this for general form validation errors
            else if(getType().equalsIgnoreCase("formerror")) {
            	//print nothing since error string still printed below
            }
            else { // among other things, not supporting input type "reset"
                log.error("Error in InputElementFormattingTag.doStartTag(): unknown input element type "+getType());
            }

            if( errorStr!=null && !errorStr.equals("")) {
                out.println("<p id=\""+getId()+"_validationError\" class=\"validationError\">"+errorStr+"</p>");
            }
        } catch (IOException ex) {
            log.error("Error in doStartTag: " + ex.getMessage());
        }
        return SKIP_BODY;
    }
    

    
  
    
    private Map<String, String> getTypesForCreateNew(
			EditConfiguration editConfig, WebappDaoFactory wdf) {    	    
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
		return types;
	}
    
    
	private String trim(String in, int sizeLimit){
        if (sizeLimit>0 && in.trim().length()>(sizeLimit+4))
            return in.trim().substring(0,sizeLimit)+" ...";
         else 
             return in.trim();        
    }

    public int doEndTag(){
      return EVAL_PAGE;
    }

    // from http://forum.java.sun.com/thread.jspa?threadID=639077&messageID=4250708
    public Map<String,String> getSortedMap(Map<String,String> hmap){
        // first make temporary list of String arrays holding both the key and its corresponding value, so that the list can be sorted with a decent comparator
        List<String[]> objectsToSort = new ArrayList<String[]>(hmap.size());
        for (String key:hmap.keySet()) {
            String[] x = new String[2];
            x[0] = key;
            x[1] = hmap.get(key);
            objectsToSort.add(x);
        }
        Collections.sort(objectsToSort, new MapPairsComparator());

        HashMap<String,String> map = new LinkedHashMap<String,String>(objectsToSort.size());
        for (String[] pair:objectsToSort) {
            map.put(pair[0],pair[1]);
        }
        return map;
    }

    private class MapPairsComparator implements Comparator<String[]> {
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

    private String generateHtmlForDate(String fieldName,
            EditConfiguration editConfig, EditSubmission editSub) {
        Date dateFromLit = null;   
        String dateStrFromLit = null;
        if( editSub != null && editSub.getLiteralsFromForm() != null && 
            editSub.getLiteralsFromForm().get(fieldName) != null ){
            //found the field on the EditSubmission
            Literal date = editSub.getLiteralsFromForm().get(fieldName);
            Object valueFromLiteral = date.getValue();            
            if( valueFromLiteral != null && valueFromLiteral instanceof Date){
                dateFromLit = (Date)valueFromLiteral;
                log.debug("found literal in submission of type Date for field " + fieldName);
            }else if( valueFromLiteral != null && valueFromLiteral instanceof String){
                dateStrFromLit = (String) valueFromLiteral;            
                log.debug("found literal in submission of type String for field " + fieldName);
            } else if ( valueFromLiteral != null && valueFromLiteral instanceof XSDDateTime) {
            	dateStrFromLit = date.getLexicalForm();
            	log.debug("found existing literal of type XSDDateTime for field " + fieldName);
            } else {
               log.error("found a value from the submsission but it was not a String or Date.");
            }
        }else if( editConfig != null && editConfig.getLiteralsInScope() != null
                    && editConfig.getLiteralsInScope().containsKey(fieldName)){
                //No EditSubmission found, try to get an existing value                 
                Literal date = editConfig.getLiteralsInScope().get(fieldName);
                Object valueFromLiteral = date.getValue();                               
                if( valueFromLiteral != null && valueFromLiteral instanceof Date){
                    dateFromLit = (Date)valueFromLiteral;
                    log.debug("found existing literal of type Date for field " + fieldName);
                }else if( valueFromLiteral != null && valueFromLiteral instanceof String){
                    dateStrFromLit = (String) valueFromLiteral;            
                    log.debug("found exisitng literal of type String for field " + fieldName);
                } else if ( valueFromLiteral != null && valueFromLiteral instanceof XSDDateTime) {
                	dateStrFromLit = date.getLexicalForm();
                	log.debug("found existing literal of type XSDDateTime for field " + fieldName);
                } else {
                    log.error("found an existing value from the editConfig but it was not a String or Date:");
                    log.error(valueFromLiteral.getClass().getName());
                }                
        }else{
            //try to get default value
        	
            Field field = editConfig.getField(fieldName);
            List<List<String>> options = field.getLiteralOptions();
            if( options.size() >=1 && options.get(0) != null && 
                    options.get(0).size() >= 1 && options.get(0).get(0) != null){
                dateStrFromLit = options.get(0).get(0);                
            }else{
            	
                log.debug("no default found for field " + fieldName);
            }
        }

        
        
        DateTime dt = null;
        if( dateStrFromLit != null){
            try {
                /* See:
                 * http://joda-time.sourceforge.net/api-release/org/joda/time/format/ISODateTimeFormat.html#dateParser() 
                 * for date format information*/
                DateTimeFormatter dtFmt = ISODateTimeFormat.dateParser();
                dt = new DateTime( dtFmt.parseDateTime( dateStrFromLit ));
            } catch (Exception e) {
                log.warn("Could not convert '" + dateStrFromLit +"' to DateTime.",e);
                dt = null;
            }
        }else if (dateFromLit != null){
            dt = new DateTime(dateFromLit);            
        }
        
        int year = 0;  // negative years (BCE) are legal, but 0 isn't (treat as null)
        int month = -1;
        int day = -1;
        
        //if( dt == null ) 
        //    dt = new DateTime();
        
        if (dt != null) {
	        year =  dt.getYear();
	        month =  dt.getMonthOfYear();
	        day =  dt.getDayOfMonth();
        }

        String sb = "";
		
        sb += " <div class=\"inlineForm\" id=\"textdate"+fieldName+"\"> \n";
        sb += "      <label for=\"year"+fieldName+"\">year</label> \n";
        sb += "      <input type=\"text\"  size=\"4\" id=\"year"+fieldName+"\" "+ "name=\"year"+fieldName+"\" maxlength=\"4\" value=\"" + ((year != 0) ? year : "") + "\"/>\n";
        sb += "      <label for=\"month"+fieldName+"\"></label> \n";
        sb += "      <select id=\"month"+fieldName+"\"  name=\"month"+fieldName+"\"> \n";
        sb += "        <option value=\"\" "   +(month == -1?SELECTED:"")+ ">--</option> \n";
        sb += "        <option value=\"01\" " +(month == 1?SELECTED:"")+ ">January</option> \n";
        sb += "        <option value=\"02\" " +(month == 2?SELECTED:"")+ ">February</option> \n";
        sb += "        <option value=\"03\" " +(month == 3?SELECTED:"")+ ">March</option> \n";
        sb += "        <option value=\"04\" " +(month == 4?SELECTED:"")+ ">April</option> \n";
        sb += "        <option value=\"05\" " +(month == 5?SELECTED:"")+ ">May</option> \n";
        sb += "        <option value=\"06\" " +(month == 6?SELECTED:"")+ ">June</option> \n";
        sb += "        <option value=\"07\" " +(month == 7?SELECTED:"")+ ">July</option> \n";
        sb += "        <option value=\"08\" " +(month == 8?SELECTED:"")+ ">August</option> \n";
        sb += "        <option value=\"09\" " +(month == 9?SELECTED:"")+ ">September</option> \n";
        sb += "        <option value=\"10\" " +(month == 10?SELECTED:"")+ ">October</option> \n";
        sb += "        <option value=\"11\" " +(month == 11?SELECTED:"")+ ">November</option> \n";
        sb += "        <option value=\"12\" " +(month == 12?SELECTED:"")+ ">December</option> \n";
        sb += "      </select> \n";
        sb += " \n";
        sb += "      <label for=\"day"+fieldName+"\">day</label> \n";
        sb += "      <select id=\"day"+fieldName+"\" name=\"day"+fieldName+"\"> \n";
        sb += "        <option value=\"\" "  +(day == -1?SELECTED:"")+">--</option> \n";
        sb += "        <option value=\"01\" "+(day == 1?SELECTED:"")+">1</option>  \n";
        sb += "        <option value=\"02\" "+(day == 2?SELECTED:"")+">2</option> \n";
        sb += "        <option value=\"03\" "+(day == 3?SELECTED:"")+">3</option>  \n";
        sb += "        <option value=\"04\" "+(day == 4?SELECTED:"")+">4</option> \n";
        sb += "        <option value=\"05\" "+(day == 5?SELECTED:"")+">5</option>  \n";
        sb += "        <option value=\"06\" "+(day == 6?SELECTED:"")+">6</option> \n";
        sb += "        <option value=\"07\" "+(day == 7?SELECTED:"")+">7</option>  \n";
        sb += "        <option value=\"08\" "+(day == 8?SELECTED:"")+">8</option> \n";
        sb += "        <option value=\"09\" "+(day == 9?SELECTED:"")+">9</option>  \n";
        sb += "        <option value=\"10\" "+(day == 10?SELECTED:"")+">10</option> \n";
        sb += "        <option value=\"11\" "+(day == 11?SELECTED:"")+">11</option>  \n";
        sb += "        <option value=\"12\" "+(day == 12?SELECTED:"")+">12</option> \n";
        sb += "        <option value=\"13\" "+(day == 13?SELECTED:"")+">13</option>  \n";
        sb += "        <option value=\"14\" "+(day == 14?SELECTED:"")+">14</option> \n";
        sb += "        <option value=\"15\" "+(day == 15?SELECTED:"")+">15</option>  \n";
        sb += "        <option value=\"16\" "+(day == 16?SELECTED:"")+">16</option> \n";
        sb += "        <option value=\"17\" "+(day == 17?SELECTED:"")+">17</option>  \n";
        sb += "        <option value=\"18\" "+(day == 18?SELECTED:"")+">18</option> \n";
        sb += "        <option value=\"19\" "+(day == 19?SELECTED:"")+">19</option>  \n";
        sb += "        <option value=\"20\" "+(day == 20?SELECTED:"")+">20</option> \n";
        sb += "        <option value=\"21\" "+(day == 21?SELECTED:"")+">21</option>  \n";
        sb += "        <option value=\"22\" "+(day == 22?SELECTED:"")+">22</option> \n";
        sb += "        <option value=\"23\" "+(day == 23?SELECTED:"")+">23</option>  \n";
        sb += "        <option value=\"24\" "+(day == 24?SELECTED:"")+">24</option> \n";
        sb += "        <option value=\"25\" "+(day == 25?SELECTED:"")+">25</option>  \n";
        sb += "        <option value=\"26\" "+(day == 26?SELECTED:"")+">26</option> \n";
        sb += "        <option value=\"27\" "+(day == 27?SELECTED:"")+">27</option>  \n";
        sb += "        <option value=\"28\" "+(day == 28?SELECTED:"")+">28</option> \n";
        sb += "        <option value=\"29\" "+(day == 29?SELECTED:"")+">29</option>  \n";
        sb += "        <option value=\"30\" "+(day == 30?SELECTED:"")+">30</option> \n";
        sb += "        <option value=\"31\" "+(day == 31?SELECTED:"")+">31</option>  \n";
        sb += "      </select> \n";
        sb += "</div> \n";
        if(fieldName.equals("expectedPublicationDateEdited")) {
        	
        	sb += "<input type='hidden' id='validDateParam' name='validDateParam' value='dateNotPast'/>";
        }
        return sb;
    }
    
    
    public String generateHtmlForTime(String fieldName,
    		EditConfiguration editConfig, EditSubmission editSub ) {
            DateTime dt = null;                
           
            int hour = -1;
            int minute = -1;
            
            if( editSub != null && editSub.getLiteralsFromForm() != null && 
                editSub.getLiteralsFromForm().get(fieldName) != null ){
                //found the field on the EditSubmission
                Literal time = editSub.getLiteralsFromForm().get(fieldName);
                Object valueFromLiteral = time.getValue();            
                if( valueFromLiteral != null && valueFromLiteral instanceof XSDDateTime){
                    XSDDateTime xsdDateTime = (XSDDateTime) valueFromLiteral;
                    hour = xsdDateTime.getHours();
                    minute = xsdDateTime.getMinutes();
                    log.debug("found literal of type XSDDateTime for field " + fieldName);
                }else{
                    try {
                        /* See:
                         * http://joda-time.sourceforge.net/api-release/org/joda/time/format/ISODateTimeFormat.html#dateParser() 
                         * for date format information*/
                        DateTimeFormatter dtFmt = ISODateTimeFormat.dateParser();
                        dt = new DateTime( dtFmt.parseDateTime(time.getLexicalForm()));
                        //what about timezones?
                        log.debug("found string literal with lexical form '"
                                + time.getLexicalForm()+ "' and convered into DateTime.");
                    } catch (Exception e) {
                        log.warn("Could not convert lexical form '" 
                                + time.getLexicalForm() +"' to DateTime.",e);
                        // dt = new DateTime();
                    }
                }
            }else if( editConfig != null && editConfig.getLiteralsInScope() != null
                    && editConfig.getLiteralsInScope().containsKey(fieldName)){
                //No EditSubmission found, try to get an existing value                 
                Literal time = editConfig.getLiteralsInScope().get(fieldName);
                Object valueFromLiteral = time.getValue();
                if (valueFromLiteral instanceof XSDDateTime) {
                	XSDDateTime xsddt = (XSDDateTime) valueFromLiteral;
                	hour = xsddt.getHours();
                	minute = xsddt.getMinutes();
                } else {
                	log.warn("Unrecognizeed value of type " + 
                			valueFromLiteral.getClass().getName() + 
                			" for time value in field " + fieldName);
                }
            }else{
                //No EditSubmission found, try to get default value from EditConfig 
                Field field = editConfig.getField(fieldName);
                List<List<String>> options = field.getLiteralOptions();
                if( options.size() >=1 && options.get(0) != null && 
                    options.get(0).size() >= 1 && options.get(0).get(0) != null){
                    String defaultValue = options.get(0).get(0);
                    try {                    
                        /* See:
                         * http://joda-time.sourceforge.net/api-release/org/joda/time/format/ISODateTimeFormat.html#dateParser() 
                         * for date format information*/                
                        //DateTimeFormatter dtFmt = ISODateTimeFormat.dateParser();                
                        //dt = new DateTime( dtFmt.parseDateTime(defaultValue) );
                        dt = new DateTime( defaultValue );
                        //what about timezones? currently we use local.
                        log.debug("found default value of " + defaultValue + " and converted into DateTime.");
                    } catch (RuntimeException e) {
                        log.warn("Could not parse '" + defaultValue +"' into DateTime" , e);
                        //dt = new DateTime();
                    }
                }else{
                    log.debug("no default found for field " + fieldName);
                }
            }
            
            
            //if( dt == null )
            //    dt = new DateTime();
            
            if (dt != null) {
            	hour =  dt.getHourOfDay();
            	minute =  dt.getMinuteOfHour();
            }

            String sb = " <div id=\"texttime"+fieldName+"\"> \n";
            
            sb += generateMarkupForTime(fieldName, hour, minute);
            
            sb += " </div> \n";
            
            return sb;
    }
    
    public String generateMarkupForTime(String fieldName, int hour, int minute) {
    	StringBuffer sb = new StringBuffer();
    	sb.append("   <div class=\"inlineForm\">");
        sb.append("      <label for=\"hour"+fieldName+"\">hour</label> \n");
        sb.append("      <select id=\"hour"+fieldName+"\" name=\"hour"+fieldName+"\" > \n");
        sb.append("        <option value=\"\" "+(hour == -1?SELECTED:"")+">--</option> \n");
        sb.append("        <option value=\"00\" "+(hour == 0?SELECTED:"")+">12am</option> \n");
        sb.append("        <option value=\"01\" "+(hour == 1?SELECTED:"")+">1am</option> \n");
        sb.append("        <option value=\"02\" "+(hour == 2?SELECTED:"")+">2am</option> \n");
        sb.append("        <option value=\"03\" "+(hour == 3?SELECTED:"")+">3am</option> \n");
        sb.append("        <option value=\"04\" "+(hour == 4?SELECTED:"")+">4am</option> \n");
        sb.append("        <option value=\"05\" "+(hour == 5?SELECTED:"")+">5am</option> \n");
        sb.append("        <option value=\"06\" "+(hour == 6?SELECTED:"")+">6am</option> \n");
        sb.append("        <option value=\"07\" "+(hour == 7?SELECTED:"")+">7am</option> \n");
        sb.append("        <option value=\"08\" "+(hour == 8?SELECTED:"")+">8am</option> \n");
        sb.append("        <option value=\"09\" "+(hour == 9?SELECTED:"")+">9am</option> \n");
        sb.append("        <option value=\"10\" "+(hour == 10?SELECTED:"")+">10am</option> \n");
        sb.append("        <option value=\"11\" "+(hour == 11?SELECTED:"")+">11am</option> \n");
        sb.append("        <option value=\"12\" "+(hour == 12?SELECTED:"")+">12pm</option> \n");
        sb.append("        <option value=\"13\" "+(hour == 13?SELECTED:"")+">1pm</option> \n");
        sb.append("        <option value=\"14\" "+(hour == 14?SELECTED:"")+">2pm</option> \n");
        sb.append("        <option value=\"15\" "+(hour == 15?SELECTED:"")+">3pm</option> \n");
        sb.append("        <option value=\"16\" "+(hour == 16?SELECTED:"")+">4pm</option> \n");
        sb.append("        <option value=\"17\" "+(hour == 17?SELECTED:"")+">5pm</option> \n");
        sb.append("        <option value=\"18\" "+(hour == 18?SELECTED:"")+">6pm</option> \n");
        sb.append("        <option value=\"19\" "+(hour == 19?SELECTED:"")+">7pm</option> \n");
        sb.append("        <option value=\"20\" "+(hour == 20?SELECTED:"")+">8pm</option> \n");
        sb.append("        <option value=\"21\" "+(hour == 21?SELECTED:"")+">9pm</option> \n");
        sb.append("        <option value=\"22\" "+(hour == 22?SELECTED:"")+">10pm</option> \n");
        sb.append("        <option value=\"23\" "+(hour == 23?SELECTED:"")+">11pm</option> \n");
        sb.append("      </select> \n");
        sb.append(" \n");
        sb.append("      <label for=\"minute"+fieldName+"\">minute</label> \n");
        sb.append("      <select name=\"minute"+fieldName+"\"> \n");
        sb.append("        <option value=\"\" "+(minute == -1?SELECTED:"")+">--</option> \n");
        sb.append("        <option value=\"00\" "+(minute == 0?SELECTED:"")+">00</option> \n");
        sb.append("        <option value=\"01\" "+(minute == 1?SELECTED:"")+">01</option> \n");
        sb.append("        <option value=\"02\" "+(minute == 2?SELECTED:"")+">02</option> \n");
        sb.append("        <option value=\"03\" "+(minute == 3?SELECTED:"")+">03</option> \n");
        sb.append("        <option value=\"04\" "+(minute == 4?SELECTED:"")+">04</option> \n");
        sb.append("        <option value=\"05\" "+(minute == 5?SELECTED:"")+">05</option> \n");
        sb.append("        <option value=\"06\" "+(minute == 6?SELECTED:"")+">06</option> \n");
        sb.append("        <option value=\"07\" "+(minute == 7?SELECTED:"")+">07</option> \n");
        sb.append("        <option value=\"08\" "+(minute == 8?SELECTED:"")+">08</option> \n");
        sb.append("        <option value=\"09\" "+(minute == 9?SELECTED:"")+">09</option> \n");
        sb.append("        <option value=\"10\" "+(minute == 10?SELECTED:"")+">10</option> \n");
        sb.append("        <option value=\"11\" "+(minute == 11?SELECTED:"")+">11</option> \n");
        sb.append("        <option value=\"12\" "+(minute == 12?SELECTED:"")+">12</option> \n");
        sb.append("        <option value=\"13\" "+(minute == 13?SELECTED:"")+">13</option> \n");
        sb.append("        <option value=\"14\" "+(minute == 14?SELECTED:"")+">14</option> \n");
        sb.append("        <option value=\"15\" "+(minute == 15?SELECTED:"")+">15</option> \n");
        sb.append("        <option value=\"16\" "+(minute == 16?SELECTED:"")+">16</option> \n");
        sb.append("        <option value=\"17\" "+(minute == 17?SELECTED:"")+">17</option> \n");
        sb.append("        <option value=\"18\" "+(minute == 18?SELECTED:"")+">18</option> \n");
        sb.append("        <option value=\"19\" "+(minute == 19?SELECTED:"")+">19</option> \n");
        sb.append("        <option value=\"20\" "+(minute == 20?SELECTED:"")+">20</option> \n");
        sb.append("        <option value=\"21\" "+(minute == 21?SELECTED:"")+">21</option> \n");
        sb.append("        <option value=\"22\" "+(minute == 22?SELECTED:"")+">22</option> \n");
        sb.append("        <option value=\"23\" "+(minute == 23?SELECTED:"")+">23</option> \n");
        sb.append("        <option value=\"24\" "+(minute == 24?SELECTED:"")+">24</option> \n");
        sb.append("        <option value=\"25\" "+(minute == 25?SELECTED:"")+">25</option> \n");
        sb.append("        <option value=\"26\" "+(minute == 26?SELECTED:"")+">26</option> \n");
        sb.append("        <option value=\"27\" "+(minute == 27?SELECTED:"")+">27</option> \n");
        sb.append("        <option value=\"28\" "+(minute == 28?SELECTED:"")+">28</option> \n");
        sb.append("        <option value=\"29\" "+(minute == 29?SELECTED:"")+">29</option> \n");
        sb.append("        <option value=\"30\" "+(minute == 30?SELECTED:"")+">30</option> \n");
        sb.append("        <option value=\"31\" "+(minute == 31?SELECTED:"")+">31</option> \n");
        sb.append("        <option value=\"32\" "+(minute == 32?SELECTED:"")+">32</option> \n");
        sb.append("        <option value=\"33\" "+(minute == 33?SELECTED:"")+">33</option> \n");
        sb.append("        <option value=\"34\" "+(minute == 34?SELECTED:"")+">34</option> \n");
        sb.append("        <option value=\"35\" "+(minute == 35?SELECTED:"")+">35</option> \n");
        sb.append("        <option value=\"36\" "+(minute == 36?SELECTED:"")+">36</option> \n");
        sb.append("        <option value=\"37\" "+(minute == 37?SELECTED:"")+">37</option> \n");
        sb.append("        <option value=\"38\" "+(minute == 38?SELECTED:"")+">38</option> \n");
        sb.append("        <option value=\"39\" "+(minute == 39?SELECTED:"")+">39</option> \n");
        sb.append("        <option value=\"40\" "+(minute == 40?SELECTED:"")+">40</option> \n");
        sb.append("        <option value=\"41\" "+(minute == 41?SELECTED:"")+">41</option> \n");
        sb.append("        <option value=\"42\" "+(minute == 42?SELECTED:"")+">42</option> \n");
        sb.append("        <option value=\"43\" "+(minute == 43?SELECTED:"")+">43</option> \n");
        sb.append("        <option value=\"44\" "+(minute == 44?SELECTED:"")+">44</option> \n");
        sb.append("        <option value=\"45\" "+(minute == 45?SELECTED:"")+">45</option> \n");
        sb.append("        <option value=\"46\" "+(minute == 46?SELECTED:"")+">46</option> \n");
        sb.append("        <option value=\"47\" "+(minute == 47?SELECTED:"")+">47</option> \n");
        sb.append("        <option value=\"48\" "+(minute == 48?SELECTED:"")+">48</option> \n");
        sb.append("        <option value=\"49\" "+(minute == 49?SELECTED:"")+">49</option> \n");
        sb.append("        <option value=\"50\" "+(minute == 50?SELECTED:"")+">50</option> \n");
        sb.append("        <option value=\"51\" "+(minute == 51?SELECTED:"")+">51</option> \n");
        sb.append("        <option value=\"52\" "+(minute == 52?SELECTED:"")+">52</option> \n");
        sb.append("        <option value=\"53\" "+(minute == 53?SELECTED:"")+">53</option> \n");
        sb.append("        <option value=\"54\" "+(minute == 54?SELECTED:"")+">54</option> \n");
        sb.append("        <option value=\"55\" "+(minute == 55?SELECTED:"")+">55</option> \n");
        sb.append("        <option value=\"56\" "+(minute == 56?SELECTED:"")+">56</option> \n");
        sb.append("        <option value=\"57\" "+(minute == 57?SELECTED:"")+">57</option> \n");
        sb.append("        <option value=\"58\" "+(minute == 58?SELECTED:"")+">58</option> \n");
        sb.append("        <option value=\"59\" "+(minute == 59?SELECTED:"")+">59</option> \n");
        sb.append("      </select> \n");
        sb.append("   </div>");
        return sb.toString();
    }
    
    public String generateHtmlForDateTime(String fieldName, 
            EditConfiguration editConfig, EditSubmission editSub ){
        DateTime dt = null;                     
        if( editSub != null && editSub.getLiteralsFromForm() != null && 
            editSub.getLiteralsFromForm().get(fieldName) != null ){
        	//found the field on the EditSubmission
        	Literal date = editSub.getLiteralsFromForm().get(fieldName);
        	Object valueFromLiteral = date.getValue();            
        	if( valueFromLiteral != null && valueFromLiteral instanceof Date){
        		dt = new DateTime( (Date)valueFromLiteral);
        		log.debug("found literal of type Date for field " + fieldName);
        	}else{
        		try {
        			/* See:
        			 * http://joda-time.sourceforge.net/api-release/org/joda/time/format/ISODateTimeFormat.html#dateParser() 
        			 * for date format information*/
        			DateTimeFormatter dtFmt = ISODateTimeFormat.dateParser();
        			dt = new DateTime( dtFmt.parseDateTime(date.getLexicalForm()));
        			//what about timezones?
        			log.debug("found string literal with lexical form '"
        					+ date.getLexicalForm()+ "' and convered into DateTime.");
        		} catch (Exception e) {
        			log.warn("Could not convert lexical form '" 
        					+ date.getLexicalForm() +"' to DateTime.",e);
        			dt = new DateTime();
        		}
        	}
        }else if( editConfig != null && editConfig.getLiteralsInScope() != null
        		&& editConfig.getLiteralsInScope().containsKey(fieldName)){
        	//No EditSubmission found, try to get an existing value                 
        	Literal date = editConfig.getLiteralsInScope().get(fieldName);
        	Object valueFromLiteral = date.getValue();
        	String strFromLit = null;
        	Date dateFromLit = null;
        	if( valueFromLiteral != null){
        		if( valueFromLiteral instanceof Date){
        			dateFromLit = (Date)valueFromLiteral;        			
        			log.debug("found existing literal of type Date for field " + fieldName);
        		}else if( valueFromLiteral instanceof String){
        			strFromLit = (String) valueFromLiteral;        			
        			log.debug("found existing literal of type String for field " + fieldName);
        		} else if ( valueFromLiteral instanceof XSDDateTime) {
        			strFromLit = date.getLexicalForm();
        			log.debug("found existing literal of type XSDDateTime for field " + fieldName);
            	} else {
					log.error("found an existing value for field " + fieldName
							+ "but it was not a String or Date:"
							+ valueFromLiteral.getClass().getName());
        		} 
        		
        		if( dateFromLit != null ){
        			dt = new DateTime(dateFromLit);
        		}else{
        			//DateTimeFormatter dtFmt = ISODateTimeFormat.dateParser();                
                    //dt = new DateTime( dtFmt.parseDateTime(strFromLit) );
        			dt = new DateTime( strFromLit );
        		}
        	}                
        }else{
        	//No EditSubmission found, try to get default value from EditConfig 
            Field field = editConfig.getField(fieldName);
            List<List<String>> options = field.getLiteralOptions();
            if( options.size() >=1 && options.get(0) != null && 
                options.get(0).size() >= 1 && options.get(0).get(0) != null){
                String defaultValue = options.get(0).get(0);
                try {                    
                    /* See:
                     * http://joda-time.sourceforge.net/api-release/org/joda/time/format/ISODateTimeFormat.html#dateParser() 
                     * for date format information*/                
                    //DateTimeFormatter dtFmt = ISODateTimeFormat.dateParser();                
                    //dt = new DateTime( dtFmt.parseDateTime(defaultValue) );
                    dt = new DateTime( defaultValue );
                    //what about timezones? currently we use local.
                    log.debug("found default value of " + defaultValue + " and converted into DateTime.");
                } catch (RuntimeException e) {
                    log.warn("Could not parse '" + defaultValue +"' into DateTime" , e);
                    dt = new DateTime();
                }
            }else{
                log.debug("no default found for field " + fieldName);
            }
        }
        
        int year = 0;  // negative years (BCE) are legal, but 0 isn't (treat as null)
        int month = -1;
        int day = -1;
        int hour = -1;
        int minute = -1;
        
        //if( dt == null )
        //    dt = new DateTime();
        
        if (dt != null) {
	        year =  dt.getYear();
	        month =  dt.getMonthOfYear();
	        day =  dt.getDayOfMonth();
	        hour =  dt.getHourOfDay();
	        minute =  dt.getMinuteOfHour();
        }
		
        String sb = "";

        sb += " <div class=\"inlineForm\" id=\"textdate"+fieldName+"\"> \n";
        sb += "      <label for=\"year"+fieldName+"\">year</label> \n";
        sb += "      <input type=\"text\"  size=\"4\" id=\"year"+fieldName+"\" "+ "name=\"year"+fieldName+"\" maxlength=\"4\" value=\"" + ((year != 0) ? year : "") + "\"/>\n";
        sb += "      <label for=\"month"+fieldName+"\"></label> \n";
        sb += "      <select id=\"month"+fieldName+"\"  name=\"month"+fieldName+"\"> \n";
        sb += "        <option value=\"\" "   +(month == -1?SELECTED:"")+ ">--</option> \n";
        sb += "        <option value=\"01\" " +(month == 1?SELECTED:"")+ ">January</option> \n";
        sb += "        <option value=\"02\" " +(month == 2?SELECTED:"")+ ">February</option> \n";
        sb += "        <option value=\"03\" " +(month == 3?SELECTED:"")+ ">March</option> \n";
        sb += "        <option value=\"04\" " +(month == 4?SELECTED:"")+ ">April</option> \n";
        sb += "        <option value=\"05\" " +(month == 5?SELECTED:"")+ ">May</option> \n";
        sb += "        <option value=\"06\" " +(month == 6?SELECTED:"")+ ">June</option> \n";
        sb += "        <option value=\"07\" " +(month == 7?SELECTED:"")+ ">July</option> \n";
        sb += "        <option value=\"08\" " +(month == 8?SELECTED:"")+ ">August</option> \n";
        sb += "        <option value=\"09\" " +(month == 9?SELECTED:"")+ ">September</option> \n";
        sb += "        <option value=\"10\" " +(month == 10?SELECTED:"")+ ">October</option> \n";
        sb += "        <option value=\"11\" " +(month == 11?SELECTED:"")+ ">November</option> \n";
        sb += "        <option value=\"12\" " +(month == 12?SELECTED:"")+ ">December</option> \n";
        sb += "      </select> \n";
        sb += " \n";
        sb += "      <label for=\"day"+fieldName+"\">day</label> \n";
        sb += "      <select id=\"day"+fieldName+"\" name=\"day"+fieldName+"\"> \n";
        sb += "        <option value=\"\" "  +(day == -1?SELECTED:"")+">--</option> \n";
        sb += "        <option value=\"01\" "+(day == 1?SELECTED:"")+">1</option>  \n";
        sb += "        <option value=\"02\" "+(day == 2?SELECTED:"")+">2</option> \n";
        sb += "        <option value=\"03\" "+(day == 3?SELECTED:"")+">3</option>  \n";
        sb += "        <option value=\"04\" "+(day == 4?SELECTED:"")+">4</option> \n";
        sb += "        <option value=\"05\" "+(day == 5?SELECTED:"")+">5</option>  \n";
        sb += "        <option value=\"06\" "+(day == 6?SELECTED:"")+">6</option> \n";
        sb += "        <option value=\"07\" "+(day == 7?SELECTED:"")+">7</option>  \n";
        sb += "        <option value=\"08\" "+(day == 8?SELECTED:"")+">8</option> \n";
        sb += "        <option value=\"09\" "+(day == 9?SELECTED:"")+">9</option>  \n";
        sb += "        <option value=\"10\" "+(day == 10?SELECTED:"")+">10</option> \n";
        sb += "        <option value=\"11\" "+(day == 11?SELECTED:"")+">11</option>  \n";
        sb += "        <option value=\"12\" "+(day == 12?SELECTED:"")+">12</option> \n";
        sb += "        <option value=\"13\" "+(day == 13?SELECTED:"")+">13</option>  \n";
        sb += "        <option value=\"14\" "+(day == 14?SELECTED:"")+">14</option> \n";
        sb += "        <option value=\"15\" "+(day == 15?SELECTED:"")+">15</option>  \n";
        sb += "        <option value=\"16\" "+(day == 16?SELECTED:"")+">16</option> \n";
        sb += "        <option value=\"17\" "+(day == 17?SELECTED:"")+">17</option>  \n";
        sb += "        <option value=\"18\" "+(day == 18?SELECTED:"")+">18</option> \n";
        sb += "        <option value=\"19\" "+(day == 19?SELECTED:"")+">19</option>  \n";
        sb += "        <option value=\"20\" "+(day == 20?SELECTED:"")+">20</option> \n";
        sb += "        <option value=\"21\" "+(day == 21?SELECTED:"")+">21</option>  \n";
        sb += "        <option value=\"22\" "+(day == 22?SELECTED:"")+">22</option> \n";
        sb += "        <option value=\"23\" "+(day == 23?SELECTED:"")+">23</option>  \n";
        sb += "        <option value=\"24\" "+(day == 24?SELECTED:"")+">24</option> \n";
        sb += "        <option value=\"25\" "+(day == 25?SELECTED:"")+">25</option>  \n";
        sb += "        <option value=\"26\" "+(day == 26?SELECTED:"")+">26</option> \n";
        sb += "        <option value=\"27\" "+(day == 27?SELECTED:"")+">27</option>  \n";
        sb += "        <option value=\"28\" "+(day == 28?SELECTED:"")+">28</option> \n";
        sb += "        <option value=\"29\" "+(day == 29?SELECTED:"")+">29</option>  \n";
        sb += "        <option value=\"30\" "+(day == 30?SELECTED:"")+">30</option> \n";
        sb += "        <option value=\"31\" "+(day == 31?SELECTED:"")+">31</option>  \n";
        sb += "      </select> \n";
        sb += " \n";
		
        sb += generateMarkupForTime(fieldName, hour, minute);
       
        sb += "</div> \n";

        return sb;
    }
    
    final String SELECTED = "selected=\"selected\"";
}