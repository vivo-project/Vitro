/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.web.MiscWebUtils;

public class DefaultDataPropertyFormGenerator implements EditConfigurationGenerator {
	
	private Log log = LogFactory.getLog(DefaultDataPropertyFormGenerator.class);
	private static HashMap<String,String> defaultsForXSDtypes;
	
	  static {
			defaultsForXSDtypes = new HashMap<String,String>();
			//defaultsForXSDtypes.put("http://www.w3.org/2001/XMLSchema#dateTime","2001-01-01T12:00:00");
			defaultsForXSDtypes.put("http://www.w3.org/2001/XMLSchema#dateTime","#Unparseable datetime defaults to now");
		  }

	@Override
	public EditConfigurationVTwo getEditConfiguration(VitroRequest vreq,
			HttpSession session) {
	    
		String subjectUri   = vreq.getParameter("subjectUri");
	    String predicateUri = vreq.getParameter("predicateUri");
    	String subjectUriJson = (String)vreq.getAttribute("subjectUriJson");
    	String predicateUriJson = (String)vreq.getAttribute("predicateUriJson");
    	String objectUriJson = (String)vreq.getAttribute("objectUriJson");
	    
	    DataPropertyStatement dps = (DataPropertyStatement)vreq.getAttribute("dataprop");
	    
	    String datapropKeyStr = vreq.getParameter("datapropKey");
	    int dataHash=0;
	    
	    DataProperty prop = (DataProperty)vreq.getAttribute("predicate");
	    //if( prop == null ) return doHelp(vreq, "In DefaultDataPropertyFormGenerator, could not find predicate " + predicateUri);
	    vreq.setAttribute("propertyName",prop.getPublicName());

	    Individual subject = (Individual)vreq.getAttribute("subject");
	    //if( subject == null ) return doHelp(vreq,"In DefaultDataPropertyFormGenerator, could not find subject " + subjectUri);
	    vreq.setAttribute("subjectName",subject.getName());
	    
	    String rangeDatatypeUri = vreq.getWebappDaoFactory().getDataPropertyDao().getRequiredDatatypeURI(subject, prop);
	    //String rangeDatatypeUri = prop.getRangeDatatypeURI();
	    vreq.setAttribute("rangeDatatypeUriJson", MiscWebUtils.escape(rangeDatatypeUri));
	    
	    
	    if( dps != null ){
	        try {
	            dataHash = Integer.parseInt(datapropKeyStr);
	            log.debug("dataHash is " + dataHash);            
	        } catch (NumberFormatException ex) {
	            log.debug("could not parse dataprop hash "+ 
	                    "but there was a dataproperty; hash: '"+datapropKeyStr+"'"); 
	        }
	        
	        String rangeDatatype = dps.getDatatypeURI();
	        if( rangeDatatype == null ){
	            log.debug("no range datatype uri set on data property statement when property's range datatype is "+prop.getRangeDatatypeURI()+" in DefaultDataPropertyFormGenerator");
	            vreq.setAttribute("rangeDatatypeUriJson","");
	        } else {
	            log.debug("range datatype uri of ["+rangeDatatype+"] on data property statement in DefaultDataPropertyFormGenerator");
	            vreq.setAttribute("rangeDatatypeUriJson",rangeDatatype);
	        }
	        String rangeLang = dps.getLanguage();
	        if( rangeLang == null ) {
	            log.debug("no language attribute on data property statement in DefaultDataPropertyFormGenerator");
	            vreq.setAttribute("rangeLangJson","");
	        }else{
	            log.debug("language attribute of ["+rangeLang+"] on data property statement in DefaultDataPropertyFormGenerator");
	            vreq.setAttribute("rangeLangJson", rangeLang);
	        }
	    } else {
	        log.debug("No incoming dataproperty statement attribute for property "+prop.getPublicName()+"; adding a new statement");                
	        if(rangeDatatypeUri != null && rangeDatatypeUri.length() > 0) {                        
	            String defaultVal = defaultsForXSDtypes.get(rangeDatatypeUri);
	            if( defaultVal == null )            	
	            	vreq.setAttribute("rangeDefaultJson", "");
	            else
	            	vreq.setAttribute("rangeDefaultJson", '"' + MiscWebUtils.escape(defaultVal)  + '"' );
	        }
	    }
	    
	    
	    String localName = prop.getLocalName();
	    String dataLiteral = localName + "Edited";
	    String formUrl = (String)vreq.getAttribute("formUrl");
	    String editKey = (String)vreq.getAttribute("editKey");
	    
    	EditConfigurationVTwo editConfiguration = new EditConfigurationVTwo();
	    
    	List<String> n3ForEdit = new ArrayList<String>();
    	n3ForEdit.add("?subject");
    	n3ForEdit.add("?predicate");
    	n3ForEdit.add("?"+dataLiteral);
    	editConfiguration.setN3Required(n3ForEdit);	    
    	
    	editConfiguration.setFormUrl(formUrl);
    	editConfiguration.setEditKey(editKey); 
    	
    	editConfiguration.setDatapropKey((datapropKeyStr==null)?"":datapropKeyStr);
    	editConfiguration.setUrlPatternToReturnTo("/individual");
    	
    	editConfiguration.setVarNameForSubject("subject");
    	editConfiguration.setSubjectUri(subjectUriJson);
    	
    	editConfiguration.setVarNameForPredicate("predicate");
    	editConfiguration.setPredicateUri(predicateUriJson);



	    
    	
    	
		return null;
	}

	private EditConfigurationVTwo doHelp(VitroRequest vreq, String string) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
