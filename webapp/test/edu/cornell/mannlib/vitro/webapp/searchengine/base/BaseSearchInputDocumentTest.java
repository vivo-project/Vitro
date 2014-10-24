/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchengine.base;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Map;

import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchInputField;

/**
 * TODO
 */
public class BaseSearchInputDocumentTest {
	/**
	 * The copy constructor should make a deep copy, down to (but not including)
	 * the field values. The component parts should be equal, but not the same.
	 */
	@Test
	public void copyConstructor() {
		BaseSearchInputDocument doc = new BaseSearchInputDocument();
		doc.setDocumentBoost(42.6F);

		SearchInputField field1 = new BaseSearchInputField("testField");
		field1.addValues("value1", "value2");
		field1.setBoost(1.1F);
		doc.addField(field1);

		SearchInputField field2 = new BaseSearchInputField("anotherField");
		field2.setBoost(-16F);
		doc.addField(field2);
		
		BaseSearchInputDocument other = new BaseSearchInputDocument(doc);
		assertEquals(doc, other);
		assertEquals(doc.getDocumentBoost(), other.getDocumentBoost(), 0.01F);

		Map<String, SearchInputField> docMap = doc.getFieldMap();
		Map<String, SearchInputField> otherMap = other.getFieldMap();
		assertEquals(docMap, otherMap);
		assertNotSame(docMap, otherMap);

		for (String fieldName : docMap.keySet()) {
			SearchInputField docField = doc.getField(fieldName);
			SearchInputField otherField = other.getField(fieldName);
			assertEquals(docField, otherField);
			assertNotSame(docField, otherField);

			Collection<Object> docFieldValues = docField.getValues();
			Collection<Object> otherFieldValues = otherField.getValues();
			assertEquals(docFieldValues, otherFieldValues);
		}
	}
}
