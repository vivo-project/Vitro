/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.ontology.update;

public class AtomicOntologyChange {

	private String sourceURI;	
	private String destinationURI;
	private AtomicChangeType atomicChangeType;

	public String getSourceURI() {
		return this.sourceURI;
	}
	
	public void setSourceURI(String sourceURI) {
		this.sourceURI = sourceURI;
	}
	
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
	
	public enum AtomicChangeType {
		ADD, DELETE, RENAME
	}
	
}
