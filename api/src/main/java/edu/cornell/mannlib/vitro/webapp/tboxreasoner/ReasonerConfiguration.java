/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.tboxreasoner;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

public class ReasonerConfiguration {
	
	public Set<ReasonerStatementPattern> inferenceDrivingPatternAllowSet;
	public Set<ReasonerStatementPattern> inferenceDrivingPatternDenySet;
	public Set<ReasonerStatementPattern> inferenceReceivingPatternAllowSet;
	
	private boolean queryForAllObjectProperties = false;
	private boolean incrementalReasoningEnabled = true;
	
	// These are some hamfisted stopgap measures until I add better support for dataproperty reasoning
	private boolean reasonOnAllDatatypePropertyStatements = false;
	private boolean queryForAllDatatypeProperties = false;
	
	/**
	 * The default reasoner configuration is designed to provide acceptable performance on larger knowledge bases.
	 * It will classify and realize, and add inferred disjointWith statements.
	 * It ignores domain and range "axioms," on the assumption that they are not truly axioms but editing constraints.  
	 */
	public static ReasonerConfiguration DEFAULT;
	
	/**
	 * This configuration will ask Pellet for "all" inferences (calls listStatements(null,null,null)).
	 * Usually suitable only for smaller ontologies.
	 */
	public static ReasonerConfiguration COMPLETE;
	
	/**
	 * This configuration will ask Pellet for the default inferences, plus all statements where the predicate
	 * is a property in the user's ontology(ies).
	 * Can lead to drastic performance improvements, depending on the ontology.
	 */
	public static ReasonerConfiguration PSEUDOCOMPLETE;
	
	/**
	 * This configuration will ask Pellet for the default inferences, plus all statements where the predicate
	 * is an object property in the user's ontology(ies).
	 */
	public static ReasonerConfiguration PSEUDOCOMPLETE_IGNORE_DATAPROPERTIES;
		
	static {	
		//ask the reasoner only to classify, realize, and infer disjointWith statements (based on a somewhat incomplete information)
		DEFAULT = new ReasonerConfiguration();
		HashSet<ReasonerStatementPattern> defaultInferenceDrivingPatternAllowSet = new HashSet<>();
		defaultInferenceDrivingPatternAllowSet.add(ReasonerStatementPattern.objectPattern(RDF.type));
        defaultInferenceDrivingPatternAllowSet.add(ReasonerStatementPattern.objectPattern(RDFS.subClassOf));
        defaultInferenceDrivingPatternAllowSet.add(ReasonerStatementPattern.objectPattern(RDFS.subPropertyOf));
        defaultInferenceDrivingPatternAllowSet.add(ReasonerStatementPattern.objectPattern(OWL.equivalentClass));
        defaultInferenceDrivingPatternAllowSet.add(ReasonerStatementPattern.objectPattern(OWL.unionOf));
        defaultInferenceDrivingPatternAllowSet.add(ReasonerStatementPattern.objectPattern(OWL.intersectionOf));
        defaultInferenceDrivingPatternAllowSet.add(ReasonerStatementPattern.objectPattern(OWL.complementOf));
        defaultInferenceDrivingPatternAllowSet.add(ReasonerStatementPattern.objectPattern(OWL.oneOf));
        defaultInferenceDrivingPatternAllowSet.add(ReasonerStatementPattern.objectPattern(OWL.onProperty));
        defaultInferenceDrivingPatternAllowSet.add(ReasonerStatementPattern.objectPattern(OWL.someValuesFrom));
        defaultInferenceDrivingPatternAllowSet.add(ReasonerStatementPattern.objectPattern(OWL.allValuesFrom));
        defaultInferenceDrivingPatternAllowSet.add(ReasonerStatementPattern.objectPattern(OWL.hasValue));
        defaultInferenceDrivingPatternAllowSet.add(ReasonerStatementPattern.objectPattern(OWL.minCardinality));
        defaultInferenceDrivingPatternAllowSet.add(ReasonerStatementPattern.objectPattern(OWL.maxCardinality));
        defaultInferenceDrivingPatternAllowSet.add(ReasonerStatementPattern.objectPattern(OWL.cardinality));
        defaultInferenceDrivingPatternAllowSet.add(ReasonerStatementPattern.objectPattern(RDF.first));
        defaultInferenceDrivingPatternAllowSet.add(ReasonerStatementPattern.objectPattern(RDF.rest));
        defaultInferenceDrivingPatternAllowSet.add(ReasonerStatementPattern.objectPattern(OWL.disjointWith));
        defaultInferenceDrivingPatternAllowSet.add(ReasonerStatementPattern.objectPattern(OWL.inverseOf));
        DEFAULT.setInferenceDrivingPatternAllowSet(defaultInferenceDrivingPatternAllowSet);
        Set<ReasonerStatementPattern> defaultInferenceReceivingPatternAllowSet = new HashSet<>();
        defaultInferenceReceivingPatternAllowSet.add(ReasonerStatementPattern.objectPattern(RDF.type));
        defaultInferenceReceivingPatternAllowSet.add(ReasonerStatementPattern.objectPattern(RDFS.subClassOf));
        defaultInferenceReceivingPatternAllowSet.add(ReasonerStatementPattern.objectPattern(RDFS.subPropertyOf));
        defaultInferenceReceivingPatternAllowSet.add(ReasonerStatementPattern.objectPattern(OWL.equivalentClass));
        defaultInferenceReceivingPatternAllowSet.add(ReasonerStatementPattern.objectPattern(OWL.disjointWith));
        defaultInferenceReceivingPatternAllowSet.add(ReasonerStatementPattern.objectPattern(OWL.inverseOf));
        DEFAULT.setInferenceReceivingPatternAllowSet(defaultInferenceReceivingPatternAllowSet);
        DEFAULT.setQueryForAllObjectProperties(false);

		PSEUDOCOMPLETE = new ReasonerConfiguration();
		PSEUDOCOMPLETE.setQueryForAllObjectProperties(true); 
		PSEUDOCOMPLETE.setReasonOnAllDatatypePropertyStatements(true);
		PSEUDOCOMPLETE.setQueryForAllDatatypeProperties(true);
        PSEUDOCOMPLETE.setInferenceReceivingPatternAllowSet(defaultInferenceReceivingPatternAllowSet);

		PSEUDOCOMPLETE_IGNORE_DATAPROPERTIES = new ReasonerConfiguration();
		PSEUDOCOMPLETE_IGNORE_DATAPROPERTIES.setQueryForAllObjectProperties(true); 
		PSEUDOCOMPLETE_IGNORE_DATAPROPERTIES.setReasonOnAllDatatypePropertyStatements(false);
		PSEUDOCOMPLETE_IGNORE_DATAPROPERTIES.setQueryForAllDatatypeProperties(false);
        PSEUDOCOMPLETE_IGNORE_DATAPROPERTIES.setInferenceReceivingPatternAllowSet(defaultInferenceReceivingPatternAllowSet);
        
		// ask the reasoner for "all" inferred statements
		// change from earlier version because Pellet seemed to stop including sameAs/differentFrom with listStatements()
		COMPLETE = new ReasonerConfiguration();
		COMPLETE.setQueryForAllObjectProperties(true); 
		COMPLETE.setReasonOnAllDatatypePropertyStatements(true); 
		COMPLETE.setQueryForAllDatatypeProperties(true); 
		Set<ReasonerStatementPattern> completeInferenceReceivingPatternAllowSet = new HashSet<>(); 
		completeInferenceReceivingPatternAllowSet.addAll(defaultInferenceReceivingPatternAllowSet);
		completeInferenceReceivingPatternAllowSet.add(ReasonerStatementPattern.objectPattern(OWL.sameAs));
		// getting NPEs inside Pellet with differentFrom on 2.0.0-rc7 
        //completeInferenceReceivingPatternAllowSet.add(ReasonerStatementPattern.objectPattern( OWL.differentFrom, null));
        COMPLETE.setInferenceReceivingPatternAllowSet(completeInferenceReceivingPatternAllowSet);
        
	}
	
	public Set<ReasonerStatementPattern> getInferenceDrivingPatternAllowSet() {
		return this.inferenceDrivingPatternAllowSet;
	}
	
	public void setInferenceDrivingPatternAllowSet(Set<ReasonerStatementPattern> patternSet) {
		this.inferenceDrivingPatternAllowSet = patternSet;
	}
	
	public Set<ReasonerStatementPattern> getInferenceDrivingPatternDenySet() {
		return this.inferenceDrivingPatternDenySet;
	}
	
	public void setInferenceDrivingPatternDenySet(Set<ReasonerStatementPattern> patternSet) {
		this.inferenceDrivingPatternDenySet = patternSet;
	}
	
	public Set<ReasonerStatementPattern> getInferenceReceivingPatternAllowSet() {
		return this.inferenceReceivingPatternAllowSet;
	}
	
	public void setInferenceReceivingPatternAllowSet(Set<ReasonerStatementPattern> patternSet) {
		this.inferenceReceivingPatternAllowSet = patternSet;
	}
	
	public boolean getQueryForAllObjectProperties() {
		return queryForAllObjectProperties;
	}

	public void setQueryForAllObjectProperties(boolean boole) {
		this.queryForAllObjectProperties = boole;
	}
	
	public boolean getReasonOnAllDatatypePropertyStatements() {
		return this.reasonOnAllDatatypePropertyStatements;
	}
	
	public void setReasonOnAllDatatypePropertyStatements(boolean boole) {
		this.reasonOnAllDatatypePropertyStatements = boole;
	}
	
	public boolean getQueryForAllDatatypeProperties() {
		return this.queryForAllDatatypeProperties;
	}
	
	public void setQueryForAllDatatypeProperties(boolean boole) {
		this.queryForAllDatatypeProperties = boole;
	}

	public boolean isIncrementalReasoningEnabled() {
		return this.incrementalReasoningEnabled;
	}
	
	public void setIncrementalReasongingEnabled(boolean value) {
		this.incrementalReasoningEnabled = value;
	}
	
}

