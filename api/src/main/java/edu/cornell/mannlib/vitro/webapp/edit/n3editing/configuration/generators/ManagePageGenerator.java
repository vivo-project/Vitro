/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.XSD;

import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.VClassGroupsForRequest;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VClassGroupCache;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationUtils;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.FieldVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.preprocessors.ManagePagePreprocessor;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.preprocessors.utils.ProcessDataGetterN3;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.preprocessors.utils.ProcessDataGetterN3Utils;
import edu.cornell.mannlib.vitro.webapp.i18n.I18n;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.utils.dataGetter.DataGetterUtils;
import edu.cornell.mannlib.vitro.webapp.utils.menuManagement.MenuManagementDataUtils;

/**
 * Generates the form for adding and editing a page in the display model. 
 *
 */
public class ManagePageGenerator extends BaseEditConfigurationGenerator implements EditConfigurationGenerator{
	
	private String template = "pageManagement.ftl";
	public static final String defaultDisplayNs = DisplayVocabulary.NAMESPACE.getURI() + "n";
	private Log log = LogFactory.getLog(ManagePageGenerator.class);

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
        //Add preprocessor
        conf.addEditSubmissionPreprocessor(new ManagePagePreprocessor(conf));
        //Prepare: add literals/uris in scope based on sparql queries
        prepare(vreq, conf);
        //This method specifically retrieves information for edit
        populateExistingDataGetter(vreq, conf, session.getServletContext());
      
        return conf	;
    }
	
	

	private void setUrisAndLiteralsOnForm(EditConfigurationVTwo conf,
			VitroRequest vreq) {
		conf.setUrisOnForm(new String[]{"page", "menuItem", "action"}); //new resources: should this be on form for new - should be for existing
		conf.setLiteralsOnForm(new String[]{"pageName", "prettyUrl", "menuPosition", "menuLinkText", "customTemplate", "isSelfContainedTemplate", "pageContentUnit"}); //page content unit = data getter JSON object
		
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
	      conf.setN3Optional(new ArrayList<String>(Arrays.asList(prefixes + pageBodyTemplateN3, 
	    		  							prefixes + menuItemN3 + menuN3,
	    		  							prefixes + isSelfContainedTemplateN3,
	    		  							prefixes + permissionN3)));
	}

	private void setN3Required(EditConfigurationVTwo conf) {
	      conf.setN3Required(new ArrayList<String>(Arrays.asList(prefixes + pageN3)));
		
	}
	
	private void setFields(EditConfigurationVTwo conf) {
		//Required fields for page include: Page title, page URL Mapping
		//Data getter fields will be dealt with in preprocessor/util classes
		//Optional fields for page include body template
		
		//required, therefore nonempty
		FieldVTwo titleField = new FieldVTwo().setName("pageName").
												setValidators(Arrays.asList("nonempty"));
		conf.addField(titleField);

		FieldVTwo urlField = new FieldVTwo().setName("prettyUrl").setValidators(Arrays.asList("nonempty"));
		conf.addField(urlField);
		
		//optional: body template
		FieldVTwo bodyTemplateField = new FieldVTwo().setName("customTemplate");
		conf.addField(bodyTemplateField);

		
		//For menu item, these are optional b/c they depend on menu item
		FieldVTwo menuItemLinkTextField = new FieldVTwo().setName("menuLinkText");
		conf.addField(menuItemLinkTextField);
		
		FieldVTwo menuItemPositionField = new FieldVTwo().setName("menuPosition").setRangeDatatypeUri(XSD.integer.getURI());
		conf.addField(menuItemPositionField);
		
		//If this is a self contained template, the appropriate flag will be set
		FieldVTwo isSelfContainedTemplateField = new FieldVTwo().setName("isSelfContainedTemplate");
		conf.addField(isSelfContainedTemplateField);
		
		//Permission for the page
		FieldVTwo permissionField = new FieldVTwo().setName("action");
		conf.addField(permissionField);
		
		//The actual page content information is stored in this field, and then
		//interpreted using the preprocessor
		FieldVTwo pageContentUnitField = new FieldVTwo().setName("pageContentUnit");
		conf.addField(pageContentUnitField);
		
		//For existing values, will need to include fields here
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
        	editConfiguration.setUrlToReturnTo(UrlBuilder.getUrl("/pageList"));
	      //For the case of a new page
	        if(subjectUri == null) {
	        	//Once added, return to pageList
		    	editConfiguration.setEntityToReturnTo("?page");
		    	editConfiguration.setPredicateUri(predicateUri);
		        
	        } else {
	        	//For the case of an existing page
	        	//Page title pageName or page hasDataGetter dataGetter
		        editConfiguration.setEntityToReturnTo(subjectUri);
		        //Set update version here
			    //if subject uri = page uri != null or empty, editing existing page
	        	editConfiguration.setParamUpdate(true);
		        
	        }
	        editConfiguration.setSubjectUri(subjectUri);
	    	editConfiguration.setPredicateUri(predicateUri);
	    }
	
	  
	 //also overriding
	@Override
	  void prepare(VitroRequest vreq, EditConfigurationVTwo editConfig) {
	        //setup the model selectors for query, write and display models on editConfig
	        setupModelSelectorsFromVitroRequest(vreq, editConfig);         
			OntModel queryModel = ModelAccess.on(vreq).getOntModel();
	        if (editConfig.isParamUpdate()) { 
	        	editConfig.prepareForParamUpdate(queryModel);
	        	
	        }
	         else{
	            //if no subject uri, this is creating a new page
	            editConfig.prepareForNonUpdate(queryModel);
	        }
	    }     
	  
	private void populateExistingDataGetter(VitroRequest vreq,
			EditConfigurationVTwo editConfig,
			ServletContext context) {
        if (editConfig.isParamUpdate()) { 
        	 //setup the model selectors for query, write and display models on editConfig
	        setupModelSelectorsFromVitroRequest(vreq, editConfig);         
			OntModel queryModel = ModelAccess.on(vreq).getOntModel();
        	retrieveExistingDataGetterInfo(context, editConfig, queryModel);
        }
		
	}
    
	//This method will get the data getters related to this page
	//And retrieve the current information for each of those data getters
    private void retrieveExistingDataGetterInfo(ServletContext context, 
    		EditConfigurationVTwo editConfig, 
    		OntModel queryModel) {
		String pageUri = editConfig.getSubjectUri();
		executeExistingDataGettersInfo(context, editConfig, pageUri, queryModel);
		 
		
	}
    
    private void executeExistingDataGettersInfo(ServletContext servletContext,
    		EditConfigurationVTwo editConfig, 
    		String pageUri, 
    		OntModel queryModel) {
    	//Create json array to be set within form specific data
    	JSONArray jsonArray = new JSONArray();
    	String querystr = getExistingDataGettersQuery();
    	//Bind pageUri to query
    	QuerySolutionMap initialBindings = new QuerySolutionMap();
        initialBindings.add("page", ResourceFactory.createResource(pageUri));
    	QueryExecution qe = null;
        try{
            Query query = QueryFactory.create(querystr);
            qe = QueryExecutionFactory.create(query, queryModel, initialBindings);
            ResultSet results = qe.execSelect();
            int counter = 0;
            while( results.hasNext()){
            	QuerySolution qs = results.nextSolution();
            	Resource dg = qs.getResource("dataGetter");
            	Resource dgType = qs.getResource("dataGetterType");
            	String dgClassName = getClassName(dgType.getURI());
            	//literals in scope/on form and fields/ as well as
            	//json representation to be returned to template saved in json array
            	processExistingDataGetter(counter, 
            			dg.getURI(), 
            			dgClassName, editConfig, queryModel, jsonArray, servletContext);
            
            	counter++;
            }
            //add json array to form specific data to be returned
        	addJSONArrayToFormSpecificData(jsonArray, editConfig);


        } catch(Exception ex) {
        	log.error("Error occurred in executing query " + querystr, ex);
        }
    }
    
    private void addJSONArrayToFormSpecificData(JSONArray jsonArray, EditConfigurationVTwo editConfig) {
    	HashMap<String, Object> data = editConfig.getFormSpecificData();		
		data.put("existingPageContentUnits", jsonArray.toString());
		//Experimenting with putting actual array in
		data.put("existingPageContentUnitsJSONArray", jsonArray);
		
	}

	private void processExistingDataGetter(int counter, String dataGetterURI, String dgClassName,
			EditConfigurationVTwo editConfig, OntModel queryModel, JSONArray jsonArray, ServletContext context) {
    	ProcessDataGetterN3 pn = ProcessDataGetterN3Utils.getDataGetterProcessorN3(dgClassName, null);
    	
		//Add N3 Optional as well
		addExistingN3Optional(editConfig, pn, counter);
		// Add URIs on Form and Add Literals On Form
		addExistingLiteralsAndUrisOnForm(editConfig, pn, counter);
		// Add fields
		addExistingFields(editConfig, pn, counter);
		//Add new resources - data getters need to be new resources
		addExistingDataGetterNewResources(editConfig, pn, counter);
		//Add values in scope
    	addValuesInScope(editConfig, pn, counter, dataGetterURI, queryModel);
    	//Get data getter-specific form specific data should it exist
    	addDataGetterSpecificFormData(dataGetterURI, pn, queryModel, jsonArray, context);
    	
		
	}

	//Any data that needs to be placed within form specific data
	//including: (i) The JSON object representing the existing information to be returned to template
	
	//Takes data getter information, packs within JSON object to send back to the form
	private void addDataGetterSpecificFormData(String dataGetterURI, ProcessDataGetterN3 pn, OntModel queryModel, JSONArray jsonArray, ServletContext context) {
		JSONObject jo = pn.getExistingValuesJSON(dataGetterURI, queryModel, context);
		//Add dataGetterURI to jsonObject
		jo.element("URI", dataGetterURI);
		jsonArray.add(jo);
	}

	//We're adding everything as optional - even what is considered "required" for a specific data getter
	private void addExistingN3Optional(EditConfigurationVTwo editConfig, ProcessDataGetterN3 pn, int counter) {
		List<String> n3 = pn.retrieveN3Required(counter);
		if(pn.retrieveN3Optional(counter) != null) {
			n3.addAll(pn.retrieveN3Optional(counter));
		}
		//Add n3 connecting page to this data getter
		n3.add(getPageToDataGetterN3(pn, counter));
		editConfig.addN3Optional(n3);
		
	}
	
	private String getPageToDataGetterN3(ProcessDataGetterN3 pn, int counter) {
		String dataGetterVar = pn.getDataGetterVar(counter);
		//Put this method in the generator but can be put elsewhere
		return getDataGetterN3(dataGetterVar);
	}

	private void addExistingLiteralsAndUrisOnForm(EditConfigurationVTwo editConfig, ProcessDataGetterN3 pn,
			int counter) {
		List<String> literalsOnForm = pn.retrieveLiteralsOnForm(counter);
		editConfig.addLiteralsOnForm(literalsOnForm);
		List<String> urisOnForm = pn.retrieveUrisOnForm(counter);
		editConfig.addUrisOnForm(urisOnForm);
		
	}

	private void addExistingFields(EditConfigurationVTwo editConfig, ProcessDataGetterN3 pn, int counter) {
		List<FieldVTwo> existingFields = pn.retrieveFields(counter);
		editConfig.addFields(existingFields);
		
	}

	private void addExistingDataGetterNewResources(EditConfigurationVTwo editConfig, ProcessDataGetterN3 pn,
			int counter) {
		//Should we even add new resources?
		List<String> newResources = pn.getNewResources(counter);
		for(String r: newResources) {
			//using default for now but will have to check
			editConfig.addNewResource(r, null);
		}
		
	}
	
	 private void addValuesInScope(EditConfigurationVTwo editConfig,
				ProcessDataGetterN3 pn, int counter, String dataGetterURI, OntModel queryModel) {
		 	pn.populateExistingValues(dataGetterURI, counter, queryModel);
		 	Map<String, List<Literal>> existingLiteralValues = pn.retrieveExistingLiteralValues();
		 	Map<String, List<String>> existingUriValues = pn.retrieveExistingUriValues();
		 	editConfig.addLiteralsInScope(existingLiteralValues);
		 	editConfig.addUrisInScope(existingUriValues);
		}

	private String getClassName(String dataGetterURI) {
    	if(dataGetterURI.contains("java:")) {
    		return dataGetterURI.substring("java:".length());
    	}
    	return dataGetterURI;
    }
    
    //Get the data getter uri and the type of the data getter
    private String getExistingDataGettersQuery() {
    	String query = getSparqlPrefix() + "SELECT ?dataGetter ?dataGetterType WHERE {" + 
    			"?page display:hasDataGetter ?dataGetter .  ?dataGetter rdf:type ?dataGetterType .}";
    	return query;
    }
    

	//In the case where this is a new page, need to ensure page gets a new 
    private void setNewResources(EditConfigurationVTwo conf) {
		//null makes default namespace be triggered
    	//conf.addNewResource("page", defaultDisplayNs);
    	//conf.addNewResource("menuItem", defaultDisplayNs);
    	conf.addNewResource("page", null);
    	conf.addNewResource("menuItem", null);
		
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
    
    
    private HashMap<String, String> generateSparqlForExistingUris() {
    	HashMap<String, String> map = new HashMap<String, String>();
    	map.put("menuItem", getExistingMenuItemQuery());
    	map.put("action", getExistingActionQuery());
    	return map;
    }
    
    private String getExistingMenuItemQuery() {
		String query = getSparqlPrefix() + "SELECT ?menuItem WHERE {?menuItem display:toPage ?page .}";
		return query;
	}
    
    private String getExistingActionQuery() {
		String query = getSparqlPrefix() + "SELECT ?action WHERE {?page display:requiresAction ?action .}";
		return query;
	}

	//Page level literals:
    //"pageName", "prettyUrl", "menuPosition", "menuLinkText", "customTemplate"

    private HashMap<String, String> generateSparqlForExistingLiterals() {
    	HashMap<String, String> map = new HashMap<String, String>();
    	map.put("pageName", getExistingPageNameQuery());
    	map.put("prettyUrl", getExistingPrettyUrlQuery());
    	map.put("menuPosition", getExistingMenuPositionQuery());
    	map.put("menuLinkText", getExistingMenuLinkTextQuery());
    	map.put("customTemplate", getExistingCustomTemplateQuery());
    	map.put("isSelfContainedTemplate", getExistingIsSelfContainedTemplateQuery());
    	return map;
    }
    
  private String getSparqlPrefix() {
	  return  "PREFIX display: <http://vitro.mannlib.cornell.edu/ontologies/display/1.1#> \n" + 
			  "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>";
  }
    
  private String getExistingPageNameQuery() {
		// TODO Auto-generated method stub
	  String query = getSparqlPrefix() + "SELECT ?pageName WHERE {" +
		"?page display:title ?pageName .}";
	  return query;
	}

private String getExistingPrettyUrlQuery() {
		String query = getSparqlPrefix() + "SELECT ?prettyUrl WHERE {" + 
		"?page	display:urlMapping ?prettyUrl .}";
		return query;
	}

//If menu, return menu position
private String getExistingMenuPositionQuery() {
		String menuPositionQuery = getSparqlPrefix() + "SELECT ?menuPosition WHERE {" + 
		"?menuItem display:toPage ?page . ?menuItem display:menuPosition ?menuPosition. }";
		return menuPositionQuery;
	}

private String getExistingMenuLinkTextQuery() {
	String menuPositionQuery = getSparqlPrefix() + "SELECT ?menuLinkText WHERE {" + 
			"?menuItem display:toPage ?page . ?menuItem display:linkText ?menuLinkText. }";
			return menuPositionQuery;
	}

private String getExistingCustomTemplateQuery() {
	String query = getSparqlPrefix() + "SELECT ?customTemplate WHERE {?page display:requiresBodyTemplate ?customTemplate .}";
	return query;
}

private String getExistingIsSelfContainedTemplateQuery() {
	String query = getSparqlPrefix() + "SELECT ?isSelfContainedTemplate WHERE {?page display:isSelfContainedTemplate ?isSelfContainedTemplate .}";

	return query;
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
		//Is param update not 
		if(editConfiguration.getSubjectUri() != null) {
			addExistingPageData(vreq, formSpecificData);
		} else {
			addNewPageData(vreq, formSpecificData);
		}		

		  //for menu position, return either existing menu position or get the highest available menu position
        retrieveMenuPosition(editConfiguration, vreq, formSpecificData);
		editConfiguration.setFormSpecificData(formSpecificData);
	}
	
	private void addRequiredPageData(VitroRequest vreq, Map<String, Object> data) {
     	MenuManagementDataUtils.includeRequiredSystemData(vreq.getSession().getServletContext(), data);
    	data.put("classGroup", new ArrayList<String>());
    	data.put("classGroups", DataGetterUtils.getClassGroups(vreq));
    	//for search individuals data get getter
    	data.put("classes", this.getAllVClasses(vreq));
    	data.put("availablePermissions", this.getAvailablePermissions(vreq));
    	data.put("availablePermissionOrderedList", this.getAvailablePermissonsOrderedURIs());
	}
	
	private void addExistingPageData(VitroRequest vreq, Map<String, Object> data) {
		addNewPageData(vreq, data);
		data.put("menuAction", "Edit");
		data.put("title", "Edit Menu Item");
		//Set up pageContentUnits as String - to save later
		data.put("existingPageContentUnits", null);
	}
	
	private void addNewPageData(VitroRequest vreq, Map<String, Object> data) {
    	data.put("title", "Add Menu Item");
		data.put("menuAction", "Add");    
    	data.put("selectedTemplateType", "default");
    	//Check for parameter specifying that this is a new menu page, in which case
    	//menu item should be selected
    	String menuItemParam = vreq.getParameter("addMenuItem");
    	if(menuItemParam != null) {
    		data.put("addMenuItem", menuItemParam);
    	} 
	}
	
	private HashMap<String, String> getAvailablePermissions(VitroRequest vreq) {
		HashMap<String, String> availablePermissions = new HashMap<String, String>();
		String actionNamespace = "java:edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission#";
		availablePermissions.put(actionNamespace + "PageViewableAdmin", I18n.text(vreq, "page_admin_permission_option"));
		availablePermissions.put(actionNamespace + "PageViewableCurator", I18n.text(vreq,"page_curator_permission_option"));
		availablePermissions.put(actionNamespace + "PageViewableEditor", I18n.text(vreq,"page_editor_permission_option"));
		availablePermissions.put(actionNamespace + "PageViewableLoggedIn", I18n.text(vreq,"page_loggedin_permission_option"));
		availablePermissions.put(actionNamespace + "PageViewablePublic", I18n.text(vreq,"page_public_permission_option"));
		return availablePermissions;
	}
	
	//To display the permissions in a specific order, we can't rely on the hashmap whose keys are not guaranteed to return in a specific order
	//This is to allow the display to work correctly
	private List<String> getAvailablePermissonsOrderedURIs() {
		List<String> availablePermissionsOrdered = new ArrayList<String>();
		String actionNamespace = "java:edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission#";
		availablePermissionsOrdered.add(actionNamespace + "PageViewableAdmin");
		availablePermissionsOrdered.add(actionNamespace + "PageViewableCurator");
		availablePermissionsOrdered.add(actionNamespace + "PageViewableEditor");
		availablePermissionsOrdered.add(actionNamespace + "PageViewableLoggedIn");
		availablePermissionsOrdered.add(actionNamespace + "PageViewablePublic");

		return availablePermissionsOrdered;
	}
	
	//N3 strings
	
	//For new or existing page
	final static String prefixes = "@prefix display: <http://vitro.mannlib.cornell.edu/ontologies/display/1.1#> . \n" + 
	"@prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> . \n";
	
	final static String pageN3 = "?page a display:Page ;  \n" +  
		"display:title ?pageName ;\n" +  
		"display:urlMapping ?prettyUrl .";  

	//"display:hasDataGetter ?pageDataGetter .";
	
	//A page may also require a body template so we can get that here as well
	//That would be optional
	
	final static String pageBodyTemplateN3 = "?page display:requiresBodyTemplate ?customTemplate .";
	
	//If self contained template, n3 will denote this using a flag
	final static String isSelfContainedTemplateN3 = "?page display:isSelfContainedTemplate ?isSelfContainedTemplate .";

	
	//Menu position is added dynamically at end by default and can be changed on reordering page
	final static String menuItemN3 = "?menuItem a display:NavigationElement ; \n" + 
    	"display:menuPosition ?menuPosition; \n" + 
    	"display:linkText ?menuLinkText; \n" + 
    	"display:toPage ?page .";
	
	//We define n3 here from default menu item up through page, but data getters are added dyamically
	//so will be dealt with in the preprocessor
	
	final static String menuN3 = "display:DefaultMenu display:hasElement ?menuItem .";
	
	//N3 that will assign a permission to a page
	final static String permissionN3 = "?page display:requiresAction ?action .";
	
	//These are public static methods that can be used in the preprocessor
	public final static String getDataGetterN3(String dataGetterVar) {
		return prefixes + "?page display:hasDataGetter " + dataGetterVar + ".";
	}
	
	public void retrieveMenuPosition(EditConfigurationVTwo editConfig, VitroRequest vreq, Map<String, Object> formSpecificData) {
		boolean returnHighestMenuPosition = false;
		//if adding a new page or if editing an existing page which does not have an associated
		//menu position
		int availableMenuPosition = this.getAvailableMenuPosition(editConfig, vreq);
		formSpecificData.put("highestMenuPosition", "" + availableMenuPosition);
	}
	
	//Get the highest menu position and return that + 1 for the menu position that can be associated
	//for a new menu item 
	public int getAvailableMenuPosition(EditConfigurationVTwo editConfig, VitroRequest vreq) {
		//Execute sparql query to get highest menu position
		int maxMenuPosition = getMaxMenuPosition(editConfig, vreq);
		return maxMenuPosition + 1;
	}



	private int getMaxMenuPosition(EditConfigurationVTwo editConfig, VitroRequest vreq) {
		int maxMenuPosition = 0;
		Literal menuPosition = null;
		setupModelSelectorsFromVitroRequest(vreq, editConfig);         
		OntModel queryModel = ModelAccess.on(vreq).getOntModel();

		String maxMenuPositionQuery = getMaxMenuPositionQueryString();
    	QueryExecution qe = null;
        try{
            Query query = QueryFactory.create(maxMenuPositionQuery);
            qe = QueryExecutionFactory.create(query, queryModel);
            ResultSet results = qe.execSelect();
            while( results.hasNext()){
            	QuerySolution qs = results.nextSolution();
            	menuPosition = qs.getLiteral("menuPosition");
            }

        } catch(Exception ex) {
        	log.error("Error occurred in executing query " + maxMenuPositionQuery, ex);
        }
        
        if(menuPosition != null) {
        	maxMenuPosition = menuPosition.getInt();
        }
		return maxMenuPosition;
	}



	private String getMaxMenuPositionQueryString() {
		String query = getSparqlPrefix() + 
				"SELECT ?menuPosition WHERE {?pageUri display:menuPosition ?menuPosition . } " + 
				"ORDER BY DESC(?menuPosition) LIMIT 1";
		return query;
	}
	
	//Get all vclasses for the list of vclasses for search
	//Originally considered using an ajax request to get the vclasses list which is fine for adding a new content type
	//but for an existing search content type, would need to make yet another ajax request which seems too much
	private List<HashMap<String, String>> getAllVClasses(VitroRequest vreq) {
		List<VClass> vclasses = new ArrayList<VClass>();     
        VClassGroupsForRequest vcgc = VClassGroupCache.getVClassGroups(vreq);
        List<VClassGroup> groups = vcgc.getGroups();
        for(VClassGroup vcg: groups) {
             for( VClass vc : vcg){
                 vclasses.add(vc);
             }
            
        }
       
        //Sort vclass by name
        Collections.sort(vclasses);
		ArrayList<HashMap<String, String>> classes = new ArrayList<HashMap<String, String>>(vclasses.size());


        for(VClass vc: vclasses) {
        	HashMap<String, String> vcObj = new HashMap<String, String>();
        	vcObj.put("name", vc.getName());
        	vcObj.put("URI", vc.getURI());
        	classes.add(vcObj);            
        }
        return classes;
	}
	
	
}
