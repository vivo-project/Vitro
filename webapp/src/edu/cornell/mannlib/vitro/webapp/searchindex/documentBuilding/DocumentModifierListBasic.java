/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchindex.documentBuilding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchInputDocument;

/**
 * The basic implementation.
 */
public class DocumentModifierListBasic implements DocumentModifierList {
	private final List<DocumentModifier> modifiers;

	public DocumentModifierListBasic(
			Collection<? extends DocumentModifier> modifiers) {
		this.modifiers = Collections
				.unmodifiableList(new ArrayList<>(modifiers));
	}

	@Override
	public void startIndexing() {
		// Nothing to do.
	}

	@Override
	public void stopIndexing() {
		// Nothing to do.
	}

	@Override
	public void modifyDocument(Individual ind, SearchInputDocument doc) {
		for (DocumentModifier m : modifiers) {
			m.modifyDocument(ind, doc);
		}
	}

}
