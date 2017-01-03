/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ExceptionResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.jena.QueryUtils;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.i18n.selection.SelectedLocale;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual.DataPropertyStatementTemplateModel;


/*Servlet to view all labels in various languages for individual*/

public class ViewLabelsServlet extends FreemarkerHttpServlet{
    private static final Log log = LogFactory.getLog(ViewLabelsServlet.class.getName());
    
    @Override
	protected ResponseValues processRequest(VitroRequest vreq) {
		Map<String, Object> body = new HashMap<String, Object>();
		String subjectUri = vreq.getParameter("subjectUri");
		body.put("subjectUri", subjectUri);
		try {
			//Get all language codes/labels in the system, and this list is sorted by language name
	        List<HashMap<String, String>> locales = this.getLocales(vreq);
	        //Get code to label hashmap - we use this to get the language name for the language code returned in the rdf literal
	        HashMap<String, String> localeCodeToNameMap = this.getFullCodeToLanguageNameMap(locales);
			//the labels already added by the user
			ArrayList<Literal> existingLabels = this.getExistingLabels(subjectUri, vreq);
			//existing labels keyed by language name and each of the list of labels is sorted by language name
			HashMap<String, List<LabelInformation>> existingLabelsByLanguageName = this.getLabelsSortedByLanguageName(existingLabels, localeCodeToNameMap, vreq, subjectUri);
			//Get available locales for the drop down for adding a new label, also sorted by language name
			HashSet<String> existingLanguageNames = new HashSet<String>(existingLabelsByLanguageName.keySet());
			body.put("labelsSortedByLanguageName", existingLabelsByLanguageName);
		  Individual subject = vreq.getWebappDaoFactory().getIndividualDao().getIndividualByURI(subjectUri);
	       
		  
		  if( subject != null && subject.getName() != null ){
	            body.put("subjectName", subject.getName());
	        }else{
	            body.put("subjectName", null);
	        }
		
		} catch (Throwable e) {
			log.error(e, e);
			return new ExceptionResponseValues(e);
		}
		
		String template = "viewLabelsForIndividual.ftl";
        
		return new TemplateResponseValues(template, body);
    }
  //Languages sorted by language name
  	private HashMap<String, List<LabelInformation>> getLabelsSortedByLanguageName(List<Literal> labels, Map<String, String> localeCodeToNameMap,
  			VitroRequest vreq, String subjectUri) {
  		
  		
  		//Iterate through the labels and create a hashmap
  		HashMap<String, List<LabelInformation>> labelsHash= new HashMap<String, List<LabelInformation>>();
  		
  		for(Literal l: labels) {
  			String languageTag = l.getLanguage();
  			String languageName = "";
  			if(languageTag == "") {
  				languageName = "untyped";
  			}
  			else if(localeCodeToNameMap.containsKey(languageTag)) {
  				languageName = localeCodeToNameMap.get(languageTag);
  			} else {
  				log.warn("This language tag " + languageTag + " does not have corresponding name in the system and was not processed");
  			}
  			
  			if(languageName != "") {
  				if(!labelsHash.containsKey(languageName)) {
  					labelsHash.put(languageName, new ArrayList<LabelInformation>());
  				}
  				ArrayList<LabelInformation> labelsList = (ArrayList<LabelInformation>)labelsHash.get(languageName);
  				//This should put the label in the list
  				//Create label information instance with the required information
  				//To generate link
  				
  				
  				labelsList.add(new LabelInformation(
  						l, languageTag, languageName));
  			}
  		}
  		
  		//Sort each label list
  		LabelInformationComparator lic = new LabelInformationComparator();
  		for(String languageName: labelsHash.keySet()) {
  			List<LabelInformation> labelInfo = labelsHash.get(languageName);
  			Collections.sort(labelInfo, lic);
  		}
  		return labelsHash;
  		
  	}
  	
  	
  	public static class LabelInformationComparator implements Comparator<LabelInformation> {
  		
  		public int compare(LabelInformation l1, LabelInformation l2) {
  			return l1.getLabelStringValue().compareTo(l2.getLabelStringValue());
  		}
  	}

    
    
    @Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
    
    //get locales
    public List<HashMap<String, String>> getLocales(VitroRequest vreq) {
    	List<Locale> selectables = SelectedLocale.getSelectableLocales(vreq);
		if (selectables.isEmpty()) {
			return Collections.emptyList();
		}
		List<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
		Locale currentLocale = SelectedLocale.getCurrentLocale(vreq);
		for (Locale locale : selectables) {
			try {
				list.add(buildLocaleMap(locale, currentLocale));
			} catch (FileNotFoundException e) {
				log.warn("Can't show the Locale selector for '" + locale
						+ "': " + e);
			}
		}
		
		return list;
    }
    
    //copied from locale selection data getter but don't need all this information
    private HashMap<String, String> buildLocaleMap(Locale locale,
			Locale currentLocale) throws FileNotFoundException {
		HashMap<String, String> map = new HashMap<String, String>();
		//Replacing the underscore with a hyphen because that is what is represented in the actual literals
		map.put("code", locale.toString().replace("_", "-"));
		map.put("label", locale.getDisplayName(currentLocale));
		return map;
	}
    
    public HashMap<String, String> getFullCodeToLanguageNameMap(List<HashMap<String, String>> localesList) {
    	HashMap<String, String> codeToLanguageMap = new HashMap<String, String>();
    	for(Map<String, String> locale: localesList) {
    		String code = (String) locale.get("code");
    		String label = (String) locale.get("label");
    		if(!codeToLanguageMap.containsKey(code)) {
    			codeToLanguageMap.put(code, label); 
    		} 
    		else {
    			log.warn("Language code " + code + " for " + label  + " was not associated in map becayse label already exists");    		
    		}
    	}
    	return codeToLanguageMap;
    }
    
    private ArrayList<Literal>  getExistingLabels(String subjectUri, VitroRequest vreq) {
        String queryStr = QueryUtils.subUriForQueryVar(LABEL_QUERY, "subject", subjectUri);
        log.debug("queryStr = " + queryStr);

        ArrayList<Literal>  labels = new ArrayList<Literal>();
        try {
        	//We want to get the labels for all the languages, not just the display language
            ResultSet results = QueryUtils.getLanguageNeutralQueryResults(queryStr, vreq);
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                Literal nodeLiteral = soln.get("label").asLiteral();
                labels.add(nodeLiteral); 


            }
        } catch (Exception e) {
            log.error(e, e);
        }    
       return labels;
    }
    
    //Class used to store the information needed for the template, such as the labels, their languages, their edit links
    public class LabelInformation {
    	private Literal labelLiteral = null;
    	
    	private String languageCode; //languageCode
    	private String languageName; 
    	public LabelInformation(Literal inputLiteral,  String inputLanguageCode, String inputLanguageName) {
    		this.labelLiteral = inputLiteral;
    	
    		this.languageCode = inputLanguageCode;
    		this.languageName = inputLanguageName;
    	}
    	
    	
    	public Literal getLabelLiteral() {
    		return this.labelLiteral;
    	}
    	
    	public String getLabelStringValue() {
    		return this.labelLiteral.getString();
    	}
    	
    	
    	public String getLanguageCode() {
    		return this.languageCode;
    	}
    	
    	public String getLanguageName() {
    		return this.languageName;
    	}
    }
    
    private static String LABEL_QUERY = ""
	        + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
	        + "SELECT DISTINCT ?label WHERE { \n"
	        + "    ?subject rdfs:label ?label \n"
	        + "} ORDER BY ?label";
	    
}