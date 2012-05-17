/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.preprocessors.utils;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.jena.ModelContext;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.preprocessors.utils.ProcessDataGetterN3;

/*
 * This class determines what n3 should be returned for a particular data getter and can be overwritten or extended in VIVO. 
 */
public class ProcessDataGetterN3Utils {
    private static final Log log = LogFactory.getLog(ProcessDataGetterN3Utils.class);
    public  static HashMap<String, String> getDataGetterTypeToProcessorMap() {
    	 HashMap<String, String> map = new HashMap<String, String>();
    	 map.put("edu.cornell.mannlib.vitro.webapp.utils.dataGetter.SparqlQueryDataGetter", "edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.preprocessors.utils.ProcessSparqlDataGetterN3");
    	 return map;
    }
    
    public static ProcessDataGetterN3 getDataGetterProcessorN3(String dataGetterClass, JSONObject jsonObject) {
    	HashMap<String, String> map = getDataGetterTypeToProcessorMap();
    	if(map.containsKey(dataGetterClass)) {
    		String processorClass = map.get(dataGetterClass);
    		try {
    			Class<?> clz = Class.forName(processorClass);
    			ProcessDataGetterN3 pn = (ProcessDataGetterN3) clz.getConstructor(JSONObject.class).newInstance(jsonObject);
    			return pn;
    		} catch(Exception ex) {
    			log.error("Exception occurred in trying to get processor class for n3 for " + dataGetterClass, ex);
    			return null;
    		}
    	}
    	return null;
    }
    
}