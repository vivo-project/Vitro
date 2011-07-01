/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.Field;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.SelectListGenerator;
import edu.cornell.mannlib.vitro.webapp.search.beans.ProhibitedFromSearch;

/**
 * Generates the edit configuration for a default property form.
 *
 */
public class MenuEditingFormGenerator implements EditConfigurationGenerator {
	
	private Log log = LogFactory.getLog(DefaultObjectPropertyFormGenerator.class);
	
    @Override
    public EditConfigurationVTwo getEditConfiguration(VitroRequest vreq,
            HttpSession session) {
       
    	//The actual N3 created here needs to include multiple levels from hasElement all the way down to the
    	//actual pagej
    	
        Individual subject = (Individual)vreq.getAttribute("subject");
        ObjectProperty prop = (ObjectProperty)vreq.getAttribute("predicate");

        WebappDaoFactory wdf = vreq.getWebappDaoFactory();  

    	String queryForInverse = "PREFIX owl:  <http://www.w3.org/2002/07/owl#>"
    							+ " SELECT ?inverse_property "
    							+ "    WHERE { ?inverse_property owl:inverseOf ?predicate } ";
   	
    	
    	// retrieving attributes from the request object to build editjson string
    	String formUrl = (String)vreq.getAttribute("formUrl");
    	String editKey = (String)vreq.getAttribute("editKey");
    	
    	String subjectUriJson = (String)vreq.getAttribute("subjectUriJson");
    	String predicateUriJson = (String)vreq.getAttribute("predicateUriJson");
    	String objectUriJson = (String)vreq.getAttribute("objectUriJson");
    	String objectUri = (String)vreq.getAttribute("objectUriJson");
    	//Get actual object uri as not concerned with json escaped version
    	System.out.println("Object Uri is " + objectUri + " and json version is " + objectUriJson);
    	
    	
    	//building the editjson object
    	//TODO: There has to be a better way of doing this.
    	// Tried building a java object with Google Gson and then
    	// deserialize it to json, but the values in the string
    	// are sometimes lists, maps, literals.
/*    	String editjson = "{" +
    			" formUrl	:	" + formUrl + " ," +
    			" editKey	:	" + editKey + " ," +
    			" urlPatternToReturnTo	:	" + "/individual ," + 
    			
    			" subject	:	[ subject , " + subjectUriJson + " ] , " +
    			" predicate : 	[ predicate , " + predicateUriJson + " ] ," +
    			" object : [ objectVar , " + objectUriJson + ", URI ] , " +
    			
    			" n3required	:	[ " + n3ForEdit + "] ," +
    			" n3optional 	: 	[ " + n3Inverse + "] ," +
    			" newResources  :   { } ," +
    			
    			" urisInScope	: 	{ } ," +
    			" literalsInScope:   { } ," +
    			
    			" urisOnForm		: 	[objectVar] ," +
    			" literalsOnForm	:	[ ] ," +
    			" filesOnForm		:	[ ] ," +
    			
    			"sparqlForLiterals	:	{ }	," +
    			"sparqlForUris		:	{	inverseProp	: " + queryForInverse + " } ," +
    			
    			"sparqlForExistingLiterals	: { } ," +
    			"sparqlForExistingUris		: { } ," +
    			
    			"fields	:			{  objectVar : { " +
    									 " newResource : false ," +
    									 " queryForExisting : { }, " +
    									 " validators : [ nonempty ] ," +
    									 " optionsType : INDIVIDUALS_VIA_OBJECT_PROPERTY , " +
    									 " subjectUri  : " + subjectUriJson + " ," +
    									 " subjectClassUri :  ," +
    									 " predicateUri : " + predicateUriJson + " ," +
    									 " objectClassUri :  ," +
    									 " rangeDatatypeUri :  ," +
    									 " rangeLang : , " +
    									 " literalOptions : [ ] , " +
    									 " assertions : [ " + n3ForEdit + " ," + n3Inverse + " ] " +
    							      " } " +
    							   " } " +
    			" } ";
    			
*/	     	
    	
    	//set the editjson attribute in the request
    //	vreq.setAttribute("editjson", editjson);
    //	log.debug(vreq.getAttribute("editjson"));
    	
    	//Alternative: Set
    	
    	
    	EditConfigurationVTwo editConfiguration = new EditConfigurationVTwo();
    	//Setting a custom test template for now
    	//TODO: Where to get this custom template from?  Should it be a predicate in display model somewhere?
    	editConfiguration.setTemplate("testMenuEdit.ftl");
    	
    	
    	editConfiguration.setFormUrl(formUrl);
    	editConfiguration.setEditKey(editKey); 
    	
    	editConfiguration.setUrlPatternToReturnTo("/individual");
    	
    	editConfiguration.setVarNameForSubject("subject");
    	editConfiguration.setSubjectUri(subjectUriJson);

    	editConfiguration.setVarNameForPredicate("predicate");
    	editConfiguration.setPredicateUri(predicateUriJson);

    	editConfiguration.setVarNameForObject("objectVar");    	
    	editConfiguration.setObject(objectUriJson);
    	//Above, try regular Object uri if json does not work
    	//this needs to be set for the editing to be triggered properly, otherwise the 'prepare' method
    	//pretends this is a data property editing statement and throws an error
    	//"object"       : [ "objectVar" ,  "${objectUriJson}" , "URI"],
    	if(objectUriJson != null) {
    		editConfiguration.setObjectResource(true);
    	} else {
    		//ObjectUriJson is null, so should include data prop info here
    	}
    	
    	
    	List<String> n3ForEdit = new ArrayList<String>();
    	n3ForEdit.add("?subject");
    	n3ForEdit.add("?predicate");
    	n3ForEdit.add("?objectVar");
    	editConfiguration.setN3Required(n3ForEdit);
    	    	
    	List<String> n3Inverse = new ArrayList<String>();
    	n3Inverse.add("?objectVar");
    	n3Inverse.add("?inverseProp");
    	n3Inverse.add("?subject"); 
    	editConfiguration.setN3Optional(n3Inverse);
    	
    	editConfiguration.setNewResources(new HashMap<String, String>());
    	
    	editConfiguration.setUrisInScope(new HashMap<String, List<String>>());
    	
    	editConfiguration.setLiteralsInScope(new HashMap<String, List<Literal>>());
    	
    	List<String> urisOnForm = new ArrayList<String>();
    	urisOnForm.add("objectVar");
    	editConfiguration.setN3Optional(urisOnForm);
    	
    	editConfiguration.setLiteralsOnForm(new ArrayList<String>());
    	
    	editConfiguration.setFilesOnForm(new ArrayList<String>());
    	
    	editConfiguration.setSparqlForAdditionalLiteralsInScope(new HashMap<String, String>());
    	
    	Map<String, String> urisInScope = new HashMap<String, String>();
    	urisInScope.put("inverseProp", queryForInverse);
    	editConfiguration.setSparqlForAdditionalUrisInScope(urisInScope);
    	
    	editConfiguration.setSparqlForExistingLiterals(new HashMap<String, String>());
    	editConfiguration.setSparqlForExistingUris(new HashMap<String, String>());
    	
    	Map<String, Field> fields = new HashMap<String, Field>();
    	
    	Field field = new Field();
    	field.setName("objectVar");
    	field.setNewResource(false);
    	//queryForExisting is not being used anywhere in Field
    	
    	List<String> validators = new ArrayList<String>();
    	validators.add("nonempty");
    	field.setValidators(validators);
    	
    	//subjectUri and subjectClassUri are not being used in Field
    	
    	field.setOptionsType("INDIVIDUALS_VIA_OBJECT_PROPERTY");
    	field.setPredicateUri(predicateUriJson);
    	
    	field.setObjectClassUri(null);
    	field.setRangeDatatypeUri(null);
    	
    	field.setRangeLang(null);
    	field.setLiteralOptions(new ArrayList<List<String>>());
    	
    	List<String> assertions = new ArrayList<String>();
    	assertions.add("?subject");
    	assertions.add("?predicate");
    	assertions.add("?objectVar");
    	assertions.add("?objectVar");
    	assertions.add("?inverseProp");
    	assertions.add("?subject");
    	field.setAssertions(assertions);
    	
    	fields.put("objectVar", field);
    	
    	//TODO: Check why/how this method signature has changed
    	//editConfiguration.setFields(fields);
    	
    	editConfiguration.putConfigInSession(editConfiguration, session);
    	
    	editConfiguration.setTemplate("defaultPropertyForm.ftl");

    	
    	String formTitle = " ";
    	String submitLabel = " ";
    	
    	//Here, retrieve model from 
    	//Model model = (Model) session.getServletContext().getAttribute("jenaOntModel");
    	//Use special model instead
    	Model model = (Model) vreq.getWriteModel();
    	
    	//this block is for an edit of an existing object property statement
    	if(vreq.getAttribute("object") != null) {
    		editConfiguration.prepareForObjPropUpdate(model);
    		formTitle = "Change entry for: <em>" + prop.getDomainPublic() + " </em>";
    		submitLabel = "save change";
    	}  else {
            editConfiguration.prepareForNonUpdate( model );
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
                submitLabel = "select existing";
            } else {
                formTitle   = "Add an entry to: <em>"+prop.getDomainPublic()+"</em>";
                submitLabel = "save entry";
            }
        }
    	
    	vreq.setAttribute("formTitle", formTitle);
    	
//        if( prop.getSelectFromExisting() ){
//        	// set ProhibitedFromSearch object so picklist doesn't show
//            // individuals from classes that should be hidden from list views
//            OntModel displayOntModel = 
//                (OntModel) session.getServletContext()
//                    .getAttribute("displayOntModel");
//            if (displayOntModel != null) {
//                ProhibitedFromSearch pfs = new ProhibitedFromSearch(
//                    DisplayVocabulary.PRIMARY_LUCENE_INDEX_URI, displayOntModel);
//                if( editConfiguration != null )
//                    editConfiguration.setProhibitedFromSearch(pfs);
//            }
//        	Map<String,String> rangeOptions = SelectListGenerator.getOptions(editConfiguration, "objectVar" , wdf);    	
//        	if( rangeOptions != null && rangeOptions.size() > 0 ) {
//        		vreq.setAttribute("rangeOptionsExist", true);
//        	    vreq.setAttribute("rangeOptions.objectVar", rangeOptions);
//        	} else { 
//        		vreq.setAttribute("rangeOptionsExist",false);
//        	}
//        }
    	
    	return editConfiguration;
    }

}
