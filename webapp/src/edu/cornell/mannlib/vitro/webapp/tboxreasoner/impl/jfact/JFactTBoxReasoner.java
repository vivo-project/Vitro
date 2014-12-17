/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.tboxreasoner.impl.jfact;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

import uk.ac.manchester.cs.jfact.JFactFactory;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.Restriction;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;

import edu.cornell.mannlib.vitro.webapp.tboxreasoner.ReasonerStatementPattern;
import edu.cornell.mannlib.vitro.webapp.tboxreasoner.TBoxChanges;
import edu.cornell.mannlib.vitro.webapp.tboxreasoner.TBoxReasoner;
import edu.cornell.mannlib.vitro.webapp.tboxreasoner.impl.TBoxInferencesAccumulator;

/**
 * An implementation of the JFact reasoner for the TBox.
 * 
 * It maintains a model of all the assertions it has been given, adding or
 * removing statements as change sets are received.
 * 
 * Each time a change is received, it will create a fresh ontology from the
 * assertions model, and apply a reasoner to that ontology. A model of
 * inferences is built by querying the reasoner.
 * 
 * The assertions and inferences are combined into an OntModel, which is kept to
 * answer queries.
 * 
 * -----------------
 * 
 * This class it not thread-safe.
 */
public class JFactTBoxReasoner implements
		TBoxReasoner {
	private static final Log log = LogFactory.getLog(JFactTBoxReasoner.class);

	private final OWLReasonerFactory reasonerFactory;
	private final TBoxInferencesAccumulator accumulator;

	private final Model filteredAssertionsModel;
	private final OntModel combinedInferencedModel;

	public JFactTBoxReasoner() {
		this.filteredAssertionsModel = ModelFactory.createDefaultModel();
		this.combinedInferencedModel = ModelFactory
				.createOntologyModel(OntModelSpec.OWL_MEM);

		this.reasonerFactory = new JFactFactory();
		this.accumulator = new TBoxInferencesAccumulator();
	}

	@Override
	public void updateReasonerModel(TBoxChanges changes) {
		log.debug("Adding " + changes.getAddedStatements().size()
				+ ", removing " + changes.getRemovedStatements().size());
		filteredAssertionsModel.add(changes.getAddedStatements());
		filteredAssertionsModel.remove(changes.getRemovedStatements());
	}

	@Override
	public Status performReasoning() {
		try {
			OWLOntology ont = copyModelToOntology(filteredAssertionsModel);

			OWLReasoner reasoner = createReasoner(ont);
			reasoner.precomputeInferences(InferenceType.values());

			try {
				if (!reasoner.isConsistent()) {
					return Status.inconsistent("Reasoner axioms are not "
							+ "consistent");
				}
			} catch (Exception e) {
				log.error(e);
				return Status.ERROR;
			}

			Model inferences = accumulator.populateModelFromReasonerQueries(reasoner);
			mergeModels(filteredAssertionsModel, inferences);
			return Status.SUCCESS;
		} catch (Exception e) {
			log.error(e);
			return Status.ERROR;
		}
	}

	private OWLOntology copyModelToOntology(Model m)
			throws OWLOntologyCreationException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		m.write(out, "RDF/XML");

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		return OWLManager.createOWLOntologyManager()
				.loadOntologyFromOntologyDocument(in);
	}

	private OWLReasoner createReasoner(OWLOntology ont) {
		OWLReasonerConfiguration config = new SimpleConfiguration(50000);
		OWLReasoner reasoner = this.reasonerFactory.createReasoner(ont, config);
		return reasoner;
	}

	private void mergeModels(Model assertions, Model inferences) {
		combinedInferencedModel.removeAll();
		combinedInferencedModel.add(assertions);
		combinedInferencedModel.add(inferences);
		log.debug("Assertions: " + assertions.size() + ", inferences: "
				+ inferences.size() + ", combined:  "
				+ combinedInferencedModel.size());
	}

	@Override
	public List<ObjectProperty> listObjectProperties() {
		return combinedInferencedModel.listObjectProperties().toList();
	}

	@Override
	public List<DatatypeProperty> listDatatypeProperties() {
		return combinedInferencedModel.listDatatypeProperties().toList();
	}

	@Override
	public List<Restriction> listRestrictions() {
		return combinedInferencedModel.listRestrictions().toList();
	}

	@Override
	public List<Statement> filterResults(
			List<ReasonerStatementPattern> patternList) {
		List<Statement> filtered = new ArrayList<>();
		for (ReasonerStatementPattern pattern : patternList) {
			filtered.addAll(pattern
					.matchStatementsFromModel(combinedInferencedModel));
		}
		return filtered;
	}

}
