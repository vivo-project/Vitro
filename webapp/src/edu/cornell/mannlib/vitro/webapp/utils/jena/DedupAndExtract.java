/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.jena;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.OWL;

public class DedupAndExtract {

	/**
	 * Returns a model where redundant individuals that are sameAs one another are smushed
	 * using URIs in preferred namespaces where possible.
	 * @param model
	 * @param preferredIndividualNamespace
	 * @return
	 */
	public Model dedupAndExtract( Model model, String preferredNamespace ) {
		
		Model extractsModel = ModelFactory.createDefaultModel();
		
		HashMap<String, String> rewriteURIUsing = new HashMap<String, String>();
		
		Iterator haveSameAsIt = model.listSubjectsWithProperty(OWL.sameAs);
		while (haveSameAsIt.hasNext()) {
			String preferredURI = null;
			Resource hasSameAs = (Resource) haveSameAsIt.next();
			List<Statement> sameAsList = hasSameAs.listProperties(OWL.sameAs).toList();
			if (sameAsList.size()>1) { // if sameAs something other than the same URI (we assume reasoning model)
				List<String> sameAsURIs = new LinkedList<String>();
				Iterator sameAsStmtIt = sameAsList.iterator();
				for (int i=0; i<sameAsList.size(); i++) {
					Statement sameAsStmt = (Statement) sameAsStmtIt.next();
					if (!sameAsStmt.getObject().isResource()) {
						throw new RuntimeException( sameAsStmt.getResource().getURI() + " is sameAs() a literal!" );
					}
					Resource sameAsRes = (Resource) sameAsStmt.getObject();
					if (!sameAsRes.isAnon()) {
						sameAsURIs.add(sameAsRes.getURI());
						if (preferredNamespace != null & preferredNamespace.equals(sameAsRes.getNameSpace())) {
							preferredURI = sameAsRes.getURI();
						}
					}
					if (preferredURI == null) {
						preferredURI = sameAsURIs.get(0);
					}
					for (String s : sameAsURIs) {
						rewriteURIUsing.put(s,preferredURI);
					}
				}
			}
		}
		
		StmtIterator modelStmtIt = model.listStatements();
		while (modelStmtIt.hasNext()) {
			Statement origStmt = modelStmtIt.nextStatement();
			Resource newSubj = null;
			RDFNode newObj = null;
			if (!origStmt.getSubject().isAnon()) { 
				String rewriteURI = rewriteURIUsing.get(origStmt.getSubject().getURI());
				if (rewriteURI != null) {
					newSubj = extractsModel.getResource(rewriteURI);
				}
			}
			if (origStmt.getObject().isResource() && !origStmt.getResource().isAnon()) {
				String rewriteURI = rewriteURIUsing.get(((Resource) origStmt.getObject()).getURI());
				if (rewriteURI != null) {
					newObj = extractsModel.getResource(rewriteURI);
				}
			}
			if (newSubj == null) {
				newSubj = origStmt.getSubject();
			}
			if (newObj == null) {
				newObj = origStmt.getObject();
			}
			extractsModel.add(newSubj, origStmt.getPredicate(), newObj);
		}
		
		return extractsModel;
		
	}
	
}