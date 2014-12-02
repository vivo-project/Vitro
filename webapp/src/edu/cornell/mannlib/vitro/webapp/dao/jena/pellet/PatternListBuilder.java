/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena.pellet;

import static com.hp.hpl.jena.rdf.model.ResourceFactory.createProperty;

import java.util.LinkedList;
import java.util.Set;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

import edu.cornell.mannlib.vitro.webapp.tboxreasoner.ReasonerConfiguration;
import edu.cornell.mannlib.vitro.webapp.tboxreasoner.ReasonerStatementPattern;
import edu.cornell.mannlib.vitro.webapp.utils.jena.criticalsection.LockableModel;
import edu.cornell.mannlib.vitro.webapp.utils.jena.criticalsection.LockableOntModel;
import edu.cornell.mannlib.vitro.webapp.utils.jena.criticalsection.LockedModel;
import edu.cornell.mannlib.vitro.webapp.utils.jena.criticalsection.LockedOntModel;

/**
 * The list of patterns for filtering the models will include:
 * 
 * All patterns specified by the ReasonerConfiguration,
 * 
 * One pattern for each deleted property, to match the use of that property as a
 * predicate.
 */
public class PatternListBuilder {
	private static final String OWL_NS = "http://www.w3.org/2002/07/owl#";

	private final ReasonerConfiguration reasonerConfiguration;
	private final LockableOntModel lockableReasonerModel;
	private final LockableModel lockableDeletedObjectProperties;
	private final LockableModel lockableDeletedDataProperties;

	public PatternListBuilder(ReasonerConfiguration reasonerConfiguration,
			OntModel reasonerModel, Model deletedObjectProperties,
			Model deletedDataProperties) {
		this.reasonerConfiguration = reasonerConfiguration;
		this.lockableReasonerModel = new LockableOntModel(reasonerModel);
		this.lockableDeletedObjectProperties = new LockableModel(
				deletedObjectProperties);
		this.lockableDeletedDataProperties = new LockableModel(
				deletedDataProperties);
	}

	/**
	 * @return
	 */
	public LinkedList<ReasonerStatementPattern> build() {
		LinkedList<ReasonerStatementPattern> irpl = new LinkedList<>();

		Set<ReasonerStatementPattern> allowSet = reasonerConfiguration
				.getInferenceReceivingPatternAllowSet();
		if (allowSet != null) {
			irpl.addAll(allowSet);
		} else {
			irpl.add(ReasonerStatementPattern.ANY_OBJECT_PROPERTY);
		}

		if (reasonerConfiguration.getQueryForAllObjectProperties()) {
			try (LockedOntModel reasonerModel = lockableReasonerModel.read()) {
				for (ObjectProperty objProp : reasonerModel
						.listObjectProperties().toList()) {
					if (!(OWL_NS.equals(objProp.getNameSpace()))) {
						irpl.add(ReasonerStatementPattern
								.objectPattern(objProp));
					}
				}
			}

			try (LockedModel deletedObjectProperties = lockableDeletedObjectProperties
					.write()) {
				for (Resource subj : deletedObjectProperties.listSubjects()
						.toList()) {
					irpl.add(ReasonerStatementPattern
							.objectPattern(createProperty(subj.getURI())));
				}
				deletedObjectProperties.removeAll();
			}
		}

		if (reasonerConfiguration.getQueryForAllDatatypeProperties()) {
			try (LockedOntModel reasonerModel = lockableReasonerModel.read()) {
				for (DatatypeProperty dataProp : reasonerModel
						.listDatatypeProperties().toList()) {
					if (!(OWL_NS.equals(dataProp.getNameSpace()))) {
						// TODO: THIS WILL WORK, BUT NEED TO GENERALIZE THE
						// PATTERN CLASSES
						irpl.add(ReasonerStatementPattern
								.objectPattern(dataProp));
					}
				}
			}
			try (LockedModel deletedDataProperties = lockableDeletedDataProperties
					.write()) {
				for (Resource subj : deletedDataProperties.listSubjects()
						.toList()) {
					irpl.add(ReasonerStatementPattern
							.objectPattern(createProperty(subj.getURI())));
				}
				deletedDataProperties.removeAll();
			}
		}
		return irpl;
	}
}
