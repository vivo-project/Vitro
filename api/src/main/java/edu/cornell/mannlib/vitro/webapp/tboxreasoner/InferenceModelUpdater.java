/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.tboxreasoner;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;

import edu.cornell.mannlib.vitro.webapp.tboxreasoner.ConfiguredReasonerListener;
import edu.cornell.mannlib.vitro.webapp.tboxreasoner.ConfiguredReasonerListener.Suspension;
import edu.cornell.mannlib.vitro.webapp.tboxreasoner.ReasonerStatementPattern;
import edu.cornell.mannlib.vitro.webapp.tboxreasoner.TBoxReasoner;
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

	private final TBoxReasoner reasoner;
	private final LockableModel lockableInferencesModel;
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

	public InferenceModelUpdater(TBoxReasoner reasoner,
			LockableModel lockableInferencesModel,
			LockableOntModel lockableFullModel,
			ConfiguredReasonerListener listener) {
		this.reasoner = reasoner;
		this.lockableInferencesModel = lockableInferencesModel;
		this.lockableFullModel = lockableFullModel;
		this.listener = listener;
	}

	/**
	 * Synchronize the inferences model with the reasoner model, with this
	 * proviso:
	 * 
	 * If a statement exists anywhere in the full TBox, don't bother adding it
	 * to the inferences model.
	 */
	public void update(List<ReasonerStatementPattern> patternList) {
		List<Statement> filteredReasonerStatements = reasoner
				.filterResults(patternList);
		addNewInferences(filteredReasonerStatements);
		removeOldInferences(filterInferencesModel(patternList),
				filteredReasonerStatements);
		log.debug("Added: " + addCount + ", Retracted: " + retractCount);
	}

	private void addNewInferences(List<Statement> filteredReasonerModel) {
		for (Statement stmt : filteredReasonerModel) {
			if (!fullModelContainsStatement(stmt)) {
				try (LockedModel inferenceModel = lockableInferencesModel
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
			List<ReasonerStatementPattern> patternList) {
		Model filtered = ModelFactory.createDefaultModel();
		try (LockedModel inferencesModel = lockableInferencesModel.read()) {
			for (ReasonerStatementPattern pattern : patternList) {
				filtered.add(pattern.matchStatementsFromModel(inferencesModel));
			}
		}
		log.debug("Filtered inferences model: " + filtered.size());
		return filtered;
	}

	private void removeOldInferences(Model filteredInferencesModel,
			List<Statement> filteredReasonerStatements) {
		Model filteredReasonerModel = ModelFactory.createDefaultModel();
		filteredReasonerModel.add(filteredReasonerStatements);

		for (Statement stmt : filteredInferencesModel.listStatements().toList()) {
			if (!filteredReasonerModel.contains(stmt)) {
				try (LockedModel inferenceModel = lockableInferencesModel
						.write(); Suspension susp = listener.suspend()) {
					retractCount++;
					inferenceModel.remove(stmt);
				}
			}
		}
	}

}
