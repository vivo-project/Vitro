/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchindex.documentBuilding;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchInputDocument;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchInputField;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Validation;

public class FieldBooster implements DocumentModifier {
	private final List<String> fieldNames = new ArrayList<>();
	private volatile Float boost;

	@Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#hasTargetField", minOccurs = 1)
	public void addTargetField(String fieldName) {
		fieldNames.add(fieldName);
	}

	@Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#hasBoost", minOccurs = 1)
	public void setBoost(float boost) {
		this.boost = boost;
	}

	@Validation
	public void validate() {
		Set<String> uniqueFieldNames = new HashSet<>(fieldNames);
		List<String> duplicateFieldNames = new ArrayList<>(fieldNames);
		for (String fn : uniqueFieldNames) {
			duplicateFieldNames.remove(fn);
		}
		if (!duplicateFieldNames.isEmpty()) {
			throw new IllegalStateException(
					"Configuration contains duplicate names for target fields: "
							+ duplicateFieldNames);
		}
	}

	@Override
	public void modifyDocument(Individual individual, SearchInputDocument doc) {

		for (String fieldName : fieldNames) {
			SearchInputField field = doc.getField(fieldName);
			if (field != null) {
				field.setBoost(field.getBoost() + boost);
			}
		}
	}

	@Override
	public void shutdown() {
		// do nothing.
	}

	@Override
	public String toString() {
		return "FieldBooster[fieldNames=" + fieldNames + ", boost=" + boost
				+ "]";
	}

}
