/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;

public class SelectListGeneratorVTwo {

	static Log log = LogFactory.getLog(SelectListGeneratorVTwo.class);
    public static Map<String,String> getOptions(
            EditConfigurationVTwo editConfig,
            String fieldName,
            WebappDaoFactory wDaoFact){


        if( editConfig == null ){
            log.error( "fieldToSelectItemList() must be called with a non-null EditConfigurationVTwo ");
            return Collections.emptyMap();
        }
        if( fieldName == null ){
            log.error( "fieldToSelectItemList() must be called with a non-null fieldName");
            return Collections.emptyMap();
        }

        FieldVTwo field = editConfig.getField(fieldName);
        if (field==null) {
            log.error("no field \""+fieldName+"\" found from editConfig.");
            return Collections.emptyMap();
        }

        if( field.getFieldOptions() == null ){
            return Collections.emptyMap();
        }

        try {
            return field.getFieldOptions().getOptions(editConfig,fieldName,wDaoFact);
        } catch (Exception e) {
            log.error("Error runing getFieldOptionis()",e);
            return Collections.emptyMap();
        }
    }
    // UQAM Overcharge method for linguistic contexte processisng
	public static Map<String,String> getOptions(
			EditConfigurationVTwo editConfig,
			String fieldName,
			VitroRequest vreq){


		if( editConfig == null ){
			log.error( "fieldToSelectItemList() must be called with a non-null EditConfigurationVTwo ");
			return Collections.emptyMap();
		}
		if( fieldName == null ){
			log.error( "fieldToSelectItemList() must be called with a non-null fieldName");
			return Collections.emptyMap();
		}

		FieldVTwo field = editConfig.getField(fieldName);
		if (field==null) {
			log.error("no field \""+fieldName+"\" found from editConfig.");
			return Collections.emptyMap();
		}

		if( field.getFieldOptions() == null ){
			return Collections.emptyMap();
		}

		try {
			//UQAM need vreq instead of WebappDaoFactory
			Map<String, String> parentClass = Collections.emptyMap();
			FieldOptions fieldOptions = field.getFieldOptions();
			// UQAM TODO - Only internationalization of ChildVClassesWithParent are implemented. For TODO, implement the internationalization for the rest of instanceof "FieldOptions"
			if (fieldOptions instanceof ChildVClassesWithParent ) {
				return ((ChildVClassesWithParent)fieldOptions).getOptions(editConfig,fieldName,vreq);
			} else {
				return fieldOptions.getOptions(editConfig,fieldName,vreq.getWebappDaoFactory());
			}
		} catch (Exception e) {
			log.error("Error runing getFieldOptionis()",e);
			return Collections.emptyMap();
		}
	}


	//Methods to sort the options map
	// from http://forum.java.sun.com/thread.jspa?threadID=639077&messageID=4250708
	//Modified to allow for a custom comparator to be sent in, defaults to mapPairsComparator
	public static Map<String,String> getSortedMap(Map<String,String> hmap,
			Comparator<String[]> comparator, VitroRequest vreq){
		// first make temporary list of String arrays holding both the key and its corresponding value, so that the list can be sorted with a decent comparator
		List<String[]> objectsToSort = new ArrayList<String[]>(hmap.size());
		for (String key:hmap.keySet()) {
			String[] x = new String[2];
			x[0] = key;
			x[1] = hmap.get(key);
			objectsToSort.add(x);
		}

		//if no comparator is passed in, utilize MapPairsComparator
		if(comparator == null) {
			comparator = new MapPairsComparator(vreq);
		}

		objectsToSort.sort(comparator);

		HashMap<String,String> map = new LinkedHashMap<String,String>(objectsToSort.size());
		for (String[] pair:objectsToSort) {
			map.put(pair[0],pair[1]);
		}
		return map;
	}

	//Sorts by the value of the 2nd element in each of the arrays
	private static class MapPairsComparator implements Comparator<String[]> {

		private Collator collator;

		public MapPairsComparator(VitroRequest vreq) {
			this.collator = vreq.getCollator();
		}
		public int compare (String[] s1, String[] s2) {
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
}
