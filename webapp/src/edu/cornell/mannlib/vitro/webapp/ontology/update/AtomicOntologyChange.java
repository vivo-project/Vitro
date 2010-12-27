/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.ontology.update;

/**
 * 
 * We need to document what is in source and destinationURI for each
 * change type.
 *
 */
public class AtomicOntologyChange {

	private String sourceURI;	
	private String destinationURI;
	private AtomicChangeType atomicChangeType;
	private String notes;

	public AtomicOntologyChange() {

    }

	public AtomicOntologyChange(String sourceURI,
	                            String destinationURI,
	                            AtomicChangeType atomicChangeType,
	                            String notes) {
		
		this.sourceURI = sourceURI;
		this.destinationURI = destinationURI;
		this.atomicChangeType = atomicChangeType;
		this.notes = notes;
    }

	
	/**
	 * Contains the URI of a class or property in the previous version of
	 * the ontology, or null if a new class or property was introduced
	 * in the current version of the ontology.
	 * @return
	 */
	public String getSourceURI() {
		return this.sourceURI;
	}
	
	public void setSourceURI(String sourceURI) {
		this.sourceURI = sourceURI;
	}
	
	/**
	 * Contains the URI of a class or property in the current version of 
	 * the ontology, or null if a class or property was removed from the
	 * previous version of the ontology.
	 * @return
	 */
	public String getDestinationURI() {
		return this.destinationURI;
	}
	
	public void setDestinationURI(String destinationURI) {
		this.destinationURI = destinationURI;
	}
	
	public AtomicChangeType getAtomicChangeType() {
		return atomicChangeType;
	}

	public void setAtomicChangeType(AtomicChangeType atomicChangeType) {
		this.atomicChangeType = atomicChangeType;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public enum AtomicChangeType {
		ADD, DELETE, RENAME
	}

}
