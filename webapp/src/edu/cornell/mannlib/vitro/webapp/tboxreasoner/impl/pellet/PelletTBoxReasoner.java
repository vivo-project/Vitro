/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.tboxreasoner.impl.pellet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mindswap.pellet.exceptions.InconsistentOntologyException;
import org.mindswap.pellet.jena.PelletInfGraph;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.Restriction;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vitro.webapp.tboxreasoner.ReasonerConfiguration;
import edu.cornell.mannlib.vitro.webapp.tboxreasoner.ReasonerStatementPattern;
import edu.cornell.mannlib.vitro.webapp.tboxreasoner.TBoxChanges;
import edu.cornell.mannlib.vitro.webapp.tboxreasoner.TBoxReasonerDriver.Status;
import edu.cornell.mannlib.vitro.webapp.tboxreasoner.TBoxReasoner;
import edu.cornell.mannlib.vitro.webapp.utils.jena.criticalsection.LockableOntModel;
import edu.cornell.mannlib.vitro.webapp.utils.jena.criticalsection.LockedOntModel;

/**
 * An implementation the TBoxReasonerWrapper for Pellet.
 */
public class PelletTBoxReasoner implements TBoxReasoner {
	private static final Log log = LogFactory
			.getLog(PelletTBoxReasoner.class);

	private final LockableOntModel lockablePelletModel;

	public PelletTBoxReasoner(ReasonerConfiguration reasonerConfiguration) {
		this.lockablePelletModel = new LockableOntModel(
				ModelFactory.createOntologyModel(reasonerConfiguration
						.getOntModelSpec()));
	}

	@Override
	public void updateReasonerModel(TBoxChanges changes) {
		try (LockedOntModel pelletModel = lockablePelletModel.write()) {
			pelletModel.remove(changes.getRemovedStatements());
			pelletModel.add(changes.getAddedStatements());
		}
	}

	@Override
	public Status performReasoning() {
		try (LockedOntModel pelletModel = lockablePelletModel.write()) {
			try {
				pelletModel.rebind();
				pelletModel.prepare();
				return Status.SUCCESS;
			} catch (InconsistentOntologyException ioe) {
				String explanation = ((PelletInfGraph) pelletModel.getGraph())
						.getKB().getExplanation();
				log.error(ioe);
				log.error(explanation);
				return Status.inconsistent(explanation);
			} catch (Exception e) {
				log.error("Exception during inference", e);
				return Status.ERROR;
			}
		}
	}

	@Override
	public List<ObjectProperty> listObjectProperties() {
		try (LockedOntModel pelletModel = lockablePelletModel.read()) {
			return pelletModel.listObjectProperties().toList();
		}
	}

	@Override
	public List<DatatypeProperty> listDatatypeProperties() {
		try (LockedOntModel pelletModel = lockablePelletModel.read()) {
			return pelletModel.listDatatypeProperties().toList();
		}
	}

	@Override
	public List<Statement> filterResults(
			List<ReasonerStatementPattern> patternList) {
		List<Statement> filtered = new ArrayList<>();
		try (LockedOntModel pelletModel = lockablePelletModel.read()) {
			for (ReasonerStatementPattern pattern : patternList) {
				filtered.addAll(pattern.matchStatementsFromModel(pelletModel));
			}
		}
		for (Iterator<Statement> fit = filtered.iterator(); fit.hasNext(); ) {
			Statement stmt = fit.next();
			if (stmt.getObject().equals(RDFS.Resource)) {
				fit.remove();
			} else if (stmt.getSubject().equals(OWL.Nothing)) {
				fit.remove();
			} else if (stmt.getObject().equals(OWL.Nothing)) {
				fit.remove();
			}
		}
		return filtered;
	}

	@Override
	public List<Restriction> listRestrictions() {
		try (LockedOntModel pelletModel = lockablePelletModel.read()) {
			return pelletModel.listRestrictions().toList();
		}
	}
}
