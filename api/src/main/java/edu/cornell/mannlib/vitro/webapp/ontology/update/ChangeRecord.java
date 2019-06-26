/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.ontology.update;

import org.apache.jena.rdf.model.Model;

public interface ChangeRecord {

	public void recordAdditions(Model incrementalAdditions);

	public void recordRetractions(Model incrementalRetractions);

	public void writeChanges();

	public boolean hasRecordedChanges();

}
