package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.preprocessors.utils;

import static org.junit.Assert.*;

import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.junit.Test;

public class ProcessIndividualsForClassesDataGetterN3Test {

	@Test
	public void testRetrieveN3Required() {
		JSONObject jsonObject = new JSONObject();
		JSONArray ja = new JSONArray();
		ja.add("test1");
		ja.add("test2");
		jsonObject.element("classesSelectedInClassGroup", ja);
		ProcessIndividualsForClassesDataGetterN3 pn = new ProcessIndividualsForClassesDataGetterN3(jsonObject);
		List<String> retrievedN3 = pn.retrieveN3Required(0);
		String firstString = retrievedN3.get(0);
		//check whether correct type returned
		int index = firstString.indexOf(pn.getClassType());
		assert(firstString.indexOf(index) != -1);
	}
		

}
