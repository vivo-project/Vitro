/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import com.hp.hpl.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationUtils;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.FieldVTwo;
import edu.cornell.mannlib.vitro.webapp.utils.dataGetter.DataGetterUtils;
import edu.cornell.mannlib.vitro.webapp.utils.menuManagement.MenuManagementDataUtils;

/**
 * Generates the form for adding and editing a page in the display model. 
 *
 */
public class ManagePageGenerator extends BaseEditConfigurationGenerator implements EditConfigurationGenerator{
	
	private String template = "pageManagement.ftl";
	
	@Override
    public EditConfigurationVTwo getEditConfiguration( VitroRequest vreq, HttpSession session) { 
        EditConfigurationVTwo conf = new EditConfigurationVTwo();
        conf.setTemplate(template);

        //get editkey and url of form
        initBasics(conf, vreq);        
        initPropertyParameters(vreq, session, conf);
        //if object uri exists, sets object URI
        initObjectPropForm(conf, vreq);     
        //Depending on whether this is a new individual to be created or editing
        //an existing one, the var names will differ
        setVarNames(conf);
        //Set N3 required and optional
        setN3Required(conf);
        setN3Optional(conf);
        
        //Designate new resources if any exist
        setNewResources(conf);
        
        //Add sparql queries
        setSparqlQueries(conf);
     // In scope
     	setUrisAndLiteralsInScope(conf, vreq);

     // on Form
     	setUrisAndLiteralsOnForm(conf, vreq);
        //Set the fields
        setFields(conf);
       
        //Adding additional data, specifically edit mode
        addFormSpecificData(conf, vreq);
        //Prepare
        prepare(vreq, conf);
        
        return conf	;
    }
	
	private void setUrisAndLiteralsOnForm(EditConfigurationVTwo conf,
			VitroRequest vreq) {
		conf.setUrisOnForm(new String[]{"page", "menuItem"}); //new resources: should this be on form for new - should be for existing
		conf.setLiteralsOnForm(new String[]{"pageTitle", "urlMapping", "linkText", "menuPosition", "menuLinkText", "bodyTemplate", "pageContentUnit"}); //page content unit = data getter JSON object
		
	}

	private void setUrisAndLiteralsInScope(EditConfigurationVTwo conf,
			VitroRequest vreq) {
		//URIs
		conf.addUrisInScope(conf.getVarNameForSubject(), 
							Arrays.asList(new String[]{conf.getSubjectUri()}));
		conf.addUrisInScope(conf.getVarNameForPredicate(), 
				Arrays.asList(new String[]{conf.getPredicateUri()}));

		
	}

	private void setN3Optional(EditConfigurationVTwo conf) {
		//body template is not required, and a given page may or may not be a menu item, but should linked to menu if menu item
	      conf.setN3Optional(Arrays.asList(prefixes + pageBodyTemplateN3, 
	    		  							prefixes + menuItemN3 + menuN3));
	}

	private void setN3Required(EditConfigurationVTwo conf) {
	      conf.setN3Required(Arrays.asList(prefixes + pageN3));
		
	}
	
	private void setFields(EditConfigurationVTwo conf) {
		//Required fields for page include: Page title, page URL Mapping
		//Data getter fields will be dealt with in preprocessor/util classes
		//Optional fields for page include body template
		
		//required, therefore nonempty
		FieldVTwo titleField = new FieldVTwo().setName("pageTitle").
												setValidators(Arrays.asList("nonempty"));
		conf.addField(titleField);

		FieldVTwo urlField = new FieldVTwo().setName("urlMapping").setValidators(Arrays.asList("nonempty"));
		conf.addField(urlField);
		
		//optional: body template
		FieldVTwo bodyTemplateField = new FieldVTwo().setName("bodyTemplate");
		conf.addField(bodyTemplateField);

		
		//For menu item, these are optional b/c they depend on menu item
		FieldVTwo menuItemLinkTextField = new FieldVTwo().setName("linkText");
		conf.addField(menuItemLinkTextField);
		
		FieldVTwo menuItemPositionField = new FieldVTwo().setName("menuPosition");
		conf.addField(menuItemPositionField);
	}



	private void setVarNames(EditConfigurationVTwo conf) {
		if(conf.getSubjectUri() != null) {
        	conf.setVarNameForSubject("page");
        	conf.setVarNameForPredicate("predicate");
        } else {
        	conf.setVarNameForSubject("subjectNotUsed");
        	conf.setVarNameForPredicate("predicateNotUsed");
        }
		
	}

	//overriding
	@Override
	  void  initPropertyParameters(VitroRequest vreq, HttpSession session, EditConfigurationVTwo editConfiguration) {
	        
	        String subjectUri = EditConfigurationUtils.getSubjectUri(vreq);
	        String predicateUri = EditConfigurationUtils.getPredicateUri(vreq);                           
	        
	      //For the case of a new page
	        if(subjectUri == null) {
	        	//Once added, return to pageList
	        	editConfiguration.setUrlToReturnTo("/pageList");
		    	editConfiguration.setEntityToReturnTo("?page");
		    	editConfiguration.setPredicateUri(predicateUri);
		        
	        } else {
	        	//For the case of an existing page
	        	//Page title pageTitle or page hasDataGetter dataGetter
		        editConfiguration.setUrlPatternToReturnTo("/individual"); 
		        editConfiguration.setEntityToReturnTo(subjectUri);
	        }
	        editConfiguration.setSubjectUri(subjectUri);
	    	editConfiguration.setPredicateUri(predicateUri);
	    }
	
	  
	 //also overriding
	@Override
	  void prepare(VitroRequest vreq, EditConfigurationVTwo editConfig) {
	        //setup the model selectors for query, write and display models on editConfig
			//Double-check if this will even work with over-written model in the case of display  model?
	        setupModelSelectorsFromVitroRequest(vreq, editConfig);         
	        OntModel queryModel = (OntModel)vreq.getAttribute("jenaOntModel");
	       
	        if (editConfig.getSubjectUri() != null) { 
	            editConfig.prepareForObjPropUpdate(queryModel);
	        }
	         else{
	            //if no subject uri, this is creating a new page
	            editConfig.prepareForNonUpdate(queryModel);
	        }
	    }     
	  
    
    //In the case where this is a new page, need to ensure page gets a new 
    private void setNewResources(EditConfigurationVTwo conf) {
		//null makes default namespace be triggered
    	conf.addNewResource("page", DEFAULT_NS_FOR_NEW_RESOURCE);
    	conf.addNewResource("menuItem", DEFAULT_NS_FOR_NEW_RESOURCE);
		
	}    
    
    //This is for various items
    private void setSparqlQueries(EditConfigurationVTwo editConfiguration) {
    	//Sparql queries defining retrieval of literals etc.
    	editConfiguration.setSparqlForAdditionalLiteralsInScope(new HashMap<String, String>());
    	
    	Map<String, String> urisInScope = new HashMap<String, String>();
    	editConfiguration.setSparqlForAdditionalUrisInScope(urisInScope);
    	
    	editConfiguration.setSparqlForExistingLiterals(generateSparqlForExistingLiterals());
    	editConfiguration.setSparqlForExistingUris(generateSparqlForExistingUris());
    }
    
    
    //Get page uri for object
    private HashMap<String, String> generateSparqlForExistingUris() {
    	HashMap<String, String> map = new HashMap<String, String>();
    	return map;
    }
    
    private HashMap<String, String> generateSparqlForExistingLiterals() {
    	HashMap<String, String> map = new HashMap<String, String>();
    	return map;
    }

    


    
  //Form specific data
    //In this case, need to get all the different data getter TYPES and labels
    //Also need to pass back the map for the options presented to the user
    //which is different from the above
    //Maybe mapping where it does exist? I.e. sparql query from drop-down IS sparql query data getter
    //Class group is hard-coded to class group but otherwise it can be changed
    //Based on circumstances - specifically internal class data getter
    //Need to get the hash for data getter to label TO the form so
    //that can then be read by javascript?
    //Also pass back current menu position?
	public void addFormSpecificData(EditConfigurationVTwo editConfiguration, VitroRequest vreq) {
		HashMap<String, Object> formSpecificData = new HashMap<String, Object>();
		//Get options for user: label to data getter type
		//For every type of page, will need some "always required" data
		addRequiredPageData(vreq, formSpecificData);
		//For a new page, we will need to add the following data
		addNewPageData(vreq, formSpecificData);
		
		editConfiguration.setFormSpecificData(formSpecificData);
	}
	
	private String getTemplate(EditConfigurationVTwo editConfiguration) {
		String returnTemplate = "default";
		if(editConfiguration.getSubjectUri() != null) {
			//Then template is EXISTING template
			//TODO: Get existing template value for page
		}
		return returnTemplate;
		
	}
	
	private void addRequiredPageData(VitroRequest vreq, Map<String, Object> data) {
     	MenuManagementDataUtils.includeRequiredSystemData(vreq.getSession().getServletContext(), data);
	}
	
	private void addNewPageData(VitroRequest vreq, Map<String, Object> data) {
    	data.put("title", "Add Menu Item");
		data.put("menuAction", "Add");
    	//Generate empty values for fields
    	data.put("menuItem", "");
    	data.put("menuName", "");
    	data.put("prettyUrl", "");
    	data.put("associatedPage", "");
    	data.put("associatedPageURI", "");
    	data.put("classGroup", new ArrayList<String>());
    	//not a page already assigned a class group
    	data.put("isClassGroupPage", false);
    	data.put("includeAllClasses", false);
    	data.put("classGroups", DataGetterUtils.getClassGroups(vreq.getSession().getServletContext()));
    	data.put("selectedTemplateType", "default");
    	//defaults to regular class group page
	}
	
	//N3 strings
	
	//For new or existing page
	final static String prefixes = "@prefix display: <http://vitro.mannlib.cornell.edu/ontologies/display/1.1#> . \n" + 
	"@prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> . \n";
	
	final static String pageN3 = "?page a display:Page ;  \n" +  
		"display:title ?pageTitle ;\n" +  
		"display:urlMapping ?urlMapping .";  

	//"display:hasDataGetter ?pageDataGetter .";
	
	//A page may also require a body template so we can get that here as well
	//That would be optional
	
	final static String pageBodyTemplateN3 = "?page display:requiresBodyTemplate ?bodyTemplate .";
	
	//Menu position is added dynamically at end by default and can be changed on reordering page
	final static String menuItemN3 = "?menuItem a display:NavigationElement ; \n" + 
    	"display:menuPosition ?menuPosition; \n" + 
    	"display:linkText ?menuLinkText; \n" + 
    	"display:toPage ?page .";
	
	//We define n3 here from default menu item up through page, but data getters are added dyamically
	//so will be dealt with in the preprocessor
	
	final static String menuN3 = "display:DefaultMenu display:hasElement ?menuItem .";
	
	//These are public static methods that can be used in the preprocessor
	public final static String getDataGetterN3(int numberDataGetter) {
		return prefixes + "?page display:hasDataGetter ?dataGetter" + numberDataGetter + ".";
	}
	

}
