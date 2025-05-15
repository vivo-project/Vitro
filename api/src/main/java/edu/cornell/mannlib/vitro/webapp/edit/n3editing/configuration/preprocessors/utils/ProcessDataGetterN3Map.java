/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.preprocessors.utils;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.utils.dataGetter.ClassGroupPageData;
import edu.cornell.mannlib.vitro.webapp.utils.dataGetter.FixedHTMLDataGetter;
import edu.cornell.mannlib.vitro.webapp.utils.dataGetter.IndividualsForClassesDataGetter;
import edu.cornell.mannlib.vitro.webapp.utils.dataGetter.SearchFilterValuesDataGetter;
import edu.cornell.mannlib.vitro.webapp.utils.dataGetter.SearchIndividualsDataGetter;
import edu.cornell.mannlib.vitro.webapp.utils.dataGetter.SparqlQueryDataGetter;

/*
 * This class determines what n3 should be returned for a particular data getter and can be overwritten or extended in VIVO.
 */
public class ProcessDataGetterN3Map {
    private static final Log log = LogFactory.getLog(ProcessDataGetterN3Map.class);

	private static HashMap<String, Class> dataGetterMap;

	static {
		dataGetterMap = new HashMap<String, Class>(); 
		dataGetterMap.put(SparqlQueryDataGetter.class.getCanonicalName(), ProcessSparqlDataGetterN3.class);
		dataGetterMap.put(ClassGroupPageData.class.getCanonicalName(), ProcessClassGroupDataGetterN3.class);
		dataGetterMap.put(SearchFilterValuesDataGetter.class.getCanonicalName(), ProcessSearchFilterValuesDataGetterN3.class);
		dataGetterMap.put(IndividualsForClassesDataGetter.class.getCanonicalName(), ProcessIndividualsForClassesDataGetterN3.class);
		dataGetterMap.put(FixedHTMLDataGetter.class.getCanonicalName(), ProcessFixedHTMLN3.class);
		dataGetterMap.put(SearchIndividualsDataGetter.class.getCanonicalName(), ProcessSearchIndividualsDataGetterN3.class);
	}

    public static HashMap<String, Class> getDataGetterTypeToProcessorMap() {
		return dataGetterMap;
    }

	public static void replaceDataGetterMap(HashMap<String, Class> newMap) {
		dataGetterMap = new HashMap<String, Class>();
		dataGetterMap.putAll(newMap);
	}
}
