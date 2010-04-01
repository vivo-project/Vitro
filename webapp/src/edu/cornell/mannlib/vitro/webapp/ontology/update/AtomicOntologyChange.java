/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.ontology.update;

import com.hp.hpl.jena.ontology.OntModel;
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

	public AtomicOntologyChange() {

    }

	public AtomicOntologyChange(String sourceURI,
	                            String destinationURI,
	                            AtomicChangeType atomicChangeType) {
		
		this.sourceURI = sourceURI;
		this.destinationURI = destinationURI;
		this.atomicChangeType = atomicChangeType;
    }

	
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
