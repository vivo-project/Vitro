/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Literal;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.FieldVTwo;
import edu.cornell.mannlib.vitro.webapp.freemarker.config.FreemarkerConfiguration;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.LanguageOption;
import freemarker.template.Configuration;

public class EditConfigurationUtils {
	private static Log log = LogFactory.getLog(EditConfigurationUtils.class);

    protected static final String MULTI_VALUED_EDIT_SUBMISSION = "MultiValueEditSubmission";

    /* *************** Static utility methods used in edit configuration and in generators *********** */
    public static String getSubjectUri(VitroRequest vreq) {
    	return vreq.getParameter("subjectUri");
    }

    public static String getPredicateUri(VitroRequest vreq) {
    	return vreq.getParameter("predicateUri");
    }

    /*
    public static String getData(VitroRequest vreq) {
    	return vreq.getParameter("subjectUri");
    }*/

    public static String getObjectUri(VitroRequest vreq) {
    	return vreq.getParameter("objectUri");
    }

    public static String getDomainUri(VitroRequest vreq) {
        return vreq.getParameter("domainUri");
    }

    public static String getRangeUri(VitroRequest vreq) {
        return vreq.getParameter("rangeUri");
    }

    public static VClass getRangeVClass(VitroRequest vreq) {
        WebappDaoFactory ctxDaoFact = ModelAccess.on(
                vreq.getSession().getServletContext()).getWebappDaoFactory();
        return ctxDaoFact.getVClassDao().getVClassByURI(getRangeUri(vreq));
    }

    public static VClass getLangAwardRangeVClass(VitroRequest vreq) {
        // UQAM-Linguistic-Management
        WebappDaoFactory vreqDaoFact = ModelAccess.on(vreq).getWebappDaoFactory(
                LanguageOption.LANGUAGE_AWARE);
        return vreqDaoFact.getVClassDao().getVClassByURI(getRangeUri(vreq));
    }
    
    //get individual
    public static Individual getSubjectIndividual(VitroRequest vreq) {
    	Individual subject = null;
    	String subjectUri = getSubjectUri(vreq);
    	subject = getIndividual(vreq, subjectUri);

    	 if( subject!= null ){
	         vreq.setAttribute("subject", subject);
	 	 }
    	return subject;
    }

    public static Individual getIndividual(VitroRequest vreq, String uri) {
    	Individual individual = null;
    	WebappDaoFactory wdf = vreq.getWebappDaoFactory();

    	 if( uri != null ){
	        individual = wdf.getIndividualDao().getIndividualByURI(uri);
    	 }
    	return individual;
    }

    public static Individual getObjectIndividual(VitroRequest vreq) {
    	String objectUri = getObjectUri(vreq);
    	Individual object = getIndividual(vreq, objectUri);
        if( object != null ) {
             vreq.setAttribute("subject", object);
 	    }
    	return object;
    }


    public static ObjectProperty getObjectProperty(VitroRequest vreq) {
    	return getObjectPropertyForPredicate(vreq, getPredicateUri(vreq));
    }

    public static DataProperty getDataProperty(VitroRequest vreq) {
    	String predicateUri = getPredicateUri(vreq);
    	return getDataPropertyForPredicate(vreq, predicateUri);
    }

    public static ObjectProperty getObjectPropertyForPredicate(VitroRequest vreq,
            String predicateUri) {
        String domainUri = getDomainUri(vreq);
        String rangeUri = getRangeUri(vreq);
        return getObjectPropertyForPredicate(vreq, predicateUri, domainUri, rangeUri);
    }

    public static ObjectProperty getObjectPropertyForPredicate(VitroRequest vreq,
            String predicateUri, String domainUri, String rangeUri) {
    	// WebappDaoFactory wdf = vreq.getWebappDaoFactory();
    	// UQAM-Linguistic-Management Use linguistic context
        WebappDaoFactory wdf = ModelAccess.on(vreq).getWebappDaoFactory(LanguageOption.LANGUAGE_AWARE);
    	ObjectProperty objectProp = wdf.getObjectPropertyDao().getObjectPropertyByURIs(
    	        predicateUri, domainUri, rangeUri);
    	return objectProp;
    }

	// UQAM Use linguistic context
    public static ObjectProperty getObjectPropertyForPredicateLangAware(VitroRequest vreq,
            String predicateUri, String domainUri, String rangeUri) {
    	// WebappDaoFactory wdf = vreq.getWebappDaoFactory();
    	// UQAM Use linguistic context
        WebappDaoFactory wdf = ModelAccess.on(vreq).getWebappDaoFactory(LanguageOption.LANGUAGE_AWARE);
    	ObjectProperty objectProp = wdf.getObjectPropertyDao().getObjectPropertyByURIs(
    	        predicateUri, domainUri, rangeUri);
    	return objectProp;
    }
    public static DataProperty getDataPropertyForPredicate(VitroRequest vreq, String predicateUri) {
    	WebappDaoFactory wdf = vreq.getWebappDaoFactory();
    	//TODO: Check reason for employing unfiltered webapp dao factory and note if using a different version
    	//would change the results
    	//For some reason, note that edit data prop statement request dispatch utilizes unfiltered webapp dao facotry
    	//DataProperty dataProp = wdf.getDataPropertyDao().getDataPropertyByURI(predicateUri);
    	 WebappDaoFactory unfilteredWdf = vreq.getUnfilteredWebappDaoFactory();
    	 DataProperty dataProp = unfilteredWdf.getDataPropertyDao().getDataPropertyByURI( predicateUri );
    	 if( dataProp != null )
    	     return dataProp;
    	 else{
    	     //when editing the display model, unfitlering wdf doesn't seem to have the needed properties.
    	     return wdf.getDataPropertyDao().getDataPropertyByURI(predicateUri);
    	 }
    }

    //get url without context - used for edit configuration object
    public static String getFormUrlWithoutContext(VitroRequest vreq) {
    	return getEditUrlWithoutContext(vreq) + "?" + vreq.getQueryString();
    }
    public static String getFormUrl(VitroRequest vreq) {
    	return getEditUrl(vreq) + "?" + vreq.getQueryString();
    }

    public static String getEditUrl(VitroRequest vreq) {
    	return vreq.getContextPath() + getEditUrlWithoutContext(vreq);
    }

    public static String getEditUrlWithoutContext(VitroRequest vreq) {
    	return "/editRequestDispatch";
    }

    public static String getCancelUrlBase(VitroRequest vreq) {
    	 return vreq.getContextPath() + "/postEditCleanupController";
    }


    public static String getEditKey(VitroRequest vreq) {
    	HttpSession session = vreq.getSession();
        String editKey =
            (EditConfigurationVTwo.getEditKeyFromRequest(vreq) == null)
                ? EditConfigurationVTwo.newEditKey(session)
                : EditConfigurationVTwo.getEditKeyFromRequest(vreq);
        return editKey;

    }

  //is data property or vitro label
    public static boolean isDataProperty(String predicateUri, VitroRequest vreq) {
    	if(predicateUri == null) {
    		log.debug("Predicate URI is null so not data property");
    		return false;
    	}
    	if(isVitroLabel(predicateUri)) {
    		return true;
    	}
    	DataProperty dataProp = vreq.getWebappDaoFactory().getDataPropertyDao().getDataPropertyByURI(predicateUri);
    	return (dataProp != null);
    }

    protected static String getDataPropKey(VitroRequest vreq) {
        return vreq.getParameter("datapropKey");
    }

    //is object property
    public static boolean isObjectProperty(String predicateUri, VitroRequest vreq) {
    	if(predicateUri == null) {
    		log.debug("Predicate URI is null so not object property");
    		return false;
    	}
    	WebappDaoFactory wdf = vreq.getWebappDaoFactory();
    	ObjectProperty op = wdf.getObjectPropertyDao().getObjectPropertyByURI(predicateUri);
    	DataProperty dp = wdf.getDataPropertyDao().getDataPropertyByURI(predicateUri);
    	log.debug("For " + predicateUri + ", object property from dao null? " + (op == null) + " and data property  null?" + (dp == null));
    	return (op != null && dp == null);
    }


	private static boolean isVitroLabel(String predicateUri) {
		return predicateUri.equals(VitroVocabulary.LABEL);
	}

	/**
	 * May return null if data property statement cannot be found.
	 */
    public static DataPropertyStatement getDataPropertyStatement(VitroRequest vreq, HttpSession session, Integer dataHash, String predicateUri) {
    	DataPropertyStatement dps = null;
   	    if( dataHash != 0) {
   			OntModel model = ModelAccess.on(session.getServletContext()).getOntModel();
   	        dps = RdfLiteralHash.getPropertyStmtByHash(EditConfigurationUtils.getSubjectUri(vreq), predicateUri, dataHash, model);
   	    }
   	    return dps;
    }

    //TODO: Include get object property statement
    public static Integer getDataHash(VitroRequest vreq) {
    	Integer dataHash = null;
		String datapropKey = EditConfigurationUtils.getDataPropKey(vreq);
		if (datapropKey!=null && datapropKey.trim().length()>0) {
	        try {
	            dataHash = Integer.parseInt(datapropKey);
	        } catch (NumberFormatException ex) {
	            log.error("Cannot decode incoming dataprop key str " + datapropKey + "as integer hash");
	        	//throw new JspException("Cannot decode incoming datapropKey String value "+datapropKeyStr+" as an integer hash in datapropStmtDelete.jsp");
	        }
	    }
		return dataHash;
    }

    //

    //Copied from the original input element formatting tag
    //Allows the retrieval of the string values for the literals
    //Useful for cases with date/time and other mechanisms
    public static Map<String, List<String>> getExistingLiteralValues(VitroRequest vreq, EditConfigurationVTwo editConfig) {
    	Map<String, List<String>> literalsInScopeStringValues = transformLiteralMap(editConfig.getLiteralsInScope());
    	return literalsInScopeStringValues;
    }

    private static List<String> transformLiteralListToStringList(List<Literal> literalValues){
    	List<String> stringValues = new ArrayList<String>();
    	if(literalValues != null) {
	    	for(Literal l: literalValues) {
	    		//Could do additional processing here if required, for example if date etc. if need be
	    		if(l != null) {
	    			stringValues.add(l.getValue().toString());
	    		}
	    		//else {
	    			//TODO: //Do we keep null as a value for this key?
	    			//stringValues.add(null);
	    		//}
	    	}
		}
    	return stringValues;
    }

    public static Map<String, List<String>> transformLiteralMap(Map<String, List<Literal>> map) {
    	Map<String, List<String>> literalMapStringValues = new HashMap<String, List<String>>();

    	for(String key: map.keySet() ) {
    		List<String> stringValues = transformLiteralListToStringList(map.get(key));
    		literalMapStringValues.put(key, stringValues);
    	}
    	return literalMapStringValues;
    }

	public static Map<String, List<String>> getExistingUriValues(EditConfigurationVTwo editConfig) {
    	return editConfig.getUrisInScope();
    }

	//Generate HTML for a specific field name given
	public static String generateHTMLForElement(VitroRequest vreq, String fieldName, EditConfigurationVTwo editConfig) {
		String html = "";
        Configuration fmConfig = FreemarkerConfiguration.getConfig(vreq);

        FieldVTwo field = editConfig == null ? null : editConfig.getField(fieldName);
        MultiValueEditSubmission editSub = EditSubmissionUtils.getEditSubmissionFromSession(vreq.getSession(), editConfig);
        //Should we create one if one doesn't exist?
        //TODO: Check if one needs to be created if it doesn't exist?
        //MultiValueEditSubmission editSub =  new MultiValueEditSubmission(vreq.getParameterMap(), editConfig);
        if( field != null && field.getEditElement() != null ){
    	  html = field.getEditElement().draw(fieldName, editConfig, editSub, fmConfig);
        }
		return html;
	}

	   /** Make a copy of list of Strings. */
    public static List<String> copy(List<String> list) {
        List<String> copyList = new ArrayList<String>();
        copyList.addAll(list);
        return copyList;
   }

    public static Map<String,String> copyMap(Map<String,String> source) {
        HashMap<String, String> map = new HashMap<String, String>();
        Set<String> keys = map.keySet();
        for(String key: keys) {
            if( source.get(key) != null )
                map.put(key, source.get(key));
            else
                map.put(key,null);
        }
        return map;
    }

    public static Map<String, List<String>> copyListMap(Map<String, List<String>> source) {
        HashMap<String, List<String>> map = new HashMap<String, List<String>>();
        Set<String> keys = map.keySet();
        for(String key: keys) {
            List<String> vals = map.get(key);
            map.put(key, copy(vals));
        }
        return map;
    }

    public static EditConfigurationVTwo getEditConfiguration(HttpServletRequest request) {
        HttpSession session = request.getSession();
        EditConfigurationVTwo editConfiguration = EditConfigurationVTwo.getConfigFromSession(session, request);
        return editConfiguration;
    }

}
