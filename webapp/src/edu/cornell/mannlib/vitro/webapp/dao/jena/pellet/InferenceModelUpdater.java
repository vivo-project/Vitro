/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena.pellet;

import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vitro.webapp.tboxreasoner.ConfiguredReasonerListener;
import edu.cornell.mannlib.vitro.webapp.tboxreasoner.ConfiguredReasonerListener.Suspension;
import edu.cornell.mannlib.vitro.webapp.tboxreasoner.ReasonerStatementPattern;
import edu.cornell.mannlib.vitro.webapp.utils.jena.criticalsection.LockableModel;
import edu.cornell.mannlib.vitro.webapp.utils.jena.criticalsection.LockableOntModel;
import edu.cornell.mannlib.vitro.webapp.utils.jena.criticalsection.LockedModel;
import edu.cornell.mannlib.vitro.webapp.utils.jena.criticalsection.LockedOntModel;

/**
 * A tool that will adjust the inferences model to match the reasoner model,
 * after applying the proper filters to both.
 */
public class InferenceModelUpdater {
	private static final Log log = LogFactory
			.getLog(InferenceModelUpdater.class);

	private final LockableOntModel lockableReasonerModel;
	private final LockableModel lockableInferenceModel;
	private final LockableOntModel lockableFullModel;
	private final ConfiguredReasonerListener listener;

	private int addCount;
	private int retractCount;

	public int getAddCount() {
		return addCount;
	}

	public int getRetractCount() {
		return retractCount;
	}

	public InferenceModelUpdater(OntModel reasonerModel, Model inferenceModel,
			OntModel fullModel, ConfiguredReasonerListener listener) {
		this.lockableReasonerModel = new LockableOntModel(reasonerModel);
		this.lockableInferenceModel = new LockableModel(inferenceModel);
		this.lockableFullModel = new LockableOntModel(fullModel);
		this.listener = listener;
	}

	/**
	 * Synchronize the inferences model with the reasoner model, with these
	 * provisos:
	 * 
	 * Statements in the reasoner model about RDFS.Resource or OWL.Nothing are
	 * ignored.
	 * 
	 * If a statement exists anywhere in the full TBox, don't bother adding it
	 * to the inferences model.
	 */
	public void update(LinkedList<ReasonerStatementPattern> patternList) {
		Model filteredReasonerModel = filterReasonerModel(patternList);
		addNewInferences(filteredReasonerModel);
		removeOldInferences(filterInferencesModel(patternList),
				filteredReasonerModel);
		log.warn("Added: " + addCount + ", Retracted: " + retractCount);
	}

	private Model filterReasonerModel(
			LinkedList<ReasonerStatementPattern> patternList) {
		Model filtered = ModelFactory.createDefaultModel();
		try (LockedOntModel reasonerModel = lockableReasonerModel.read()) {
			for (ReasonerStatementPattern pattern : patternList) {
				filtered.add(pattern.matchStatementsFromModel(reasonerModel));
			}
		}
		for (Statement stmt : filtered.listStatements().toList()) {
			if (stmt.getObject().equals(RDFS.Resource)) {
				filtered.remove(stmt);
			} else if (stmt.getSubject().equals(OWL.Nothing)) {
				filtered.remove(stmt);
			} else if (stmt.getObject().equals(OWL.Nothing)) {
				filtered.remove(stmt);
			}
		}
		log.warn("Filtered reasoner model: " + filtered.size());
		return filtered;
	}

	private void addNewInferences(Model filteredReasonerModel) {
		for (Statement stmt : filteredReasonerModel.listStatements().toList()) {
			if (!fullModelContainsStatement(stmt)) {
				try (LockedModel inferenceModel = lockableInferenceModel
						.write(); Suspension susp = listener.suspend()) {
					inferenceModel.add(stmt);
					addCount++;
				}
			}
		}
	}

	private boolean fullModelContainsStatement(Statement stmt) {
		try (LockedOntModel fullModel = lockableFullModel.read()) {
			return fullModel.contains(stmt);
		}
	}

	private Model filterInferencesModel(
			LinkedList<ReasonerStatementPattern> patternList) {
		Model filtered = ModelFactory.createDefaultModel();
		try (LockedOntModel reasonerModel = lockableReasonerModel.read()) {
			for (ReasonerStatementPattern pattern : patternList) {
				filtered.add(pattern.matchStatementsFromModel(reasonerModel));
			}
		}
		log.warn("Filtered inferences model: " + filtered.size());
		return filtered;
	}

	private void removeOldInferences(Model filteredInferencesModel,
			Model filteredReasonerModel) {
		for (Statement stmt : filteredInferencesModel.listStatements().toList()) {
			if (!filteredReasonerModel.contains(stmt)) {
				try (LockedModel inferenceModel = lockableInferenceModel
						.write(); Suspension susp = listener.suspend()) {
					retractCount++;
					inferenceModel.remove(stmt);
				}
			}
		}
	}

}
