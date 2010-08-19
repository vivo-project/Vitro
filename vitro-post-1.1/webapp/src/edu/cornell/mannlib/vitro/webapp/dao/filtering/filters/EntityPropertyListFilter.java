/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.filtering.filters;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.jga.fn.UnaryFunctor;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

public class EntityPropertyListFilter extends UnaryFunctor<List<Property>, List<Property>> {

	private static final com.hp.hpl.jena.rdf.model.Property MASKS_PROPERTY = ResourceFactory.createProperty(VitroVocabulary.MASKS_PROPERTY);
	private Map<String,Collection<String>> propertyMaskMap;
	
	public EntityPropertyListFilter(OntModel ontModel) {
		propertyMaskMap = new HashMap<String,Collection<String>>();
		ontModel.enterCriticalSection(Lock.READ);
		try {
			StmtIterator maskStmtIt = ontModel.listStatements((Resource) null, MASKS_PROPERTY, (RDFNode) null );
			while (maskStmtIt.hasNext()) {
				Statement maskStmt = maskStmtIt.nextStatement();
				if ( !maskStmt.getSubject().isAnon() && maskStmt.getObject().isResource() && !((Resource) maskStmt.getObject()).isAnon()) {
					String maskedPropertyURI = ((Resource) maskStmt.getObject()).getURI();
					String maskingPropertyURI = maskStmt.getSubject().getURI();
					Collection<String> collectionOfMaskers = propertyMaskMap.get(maskedPropertyURI);
					if (collectionOfMaskers == null) {
						collectionOfMaskers = new LinkedList<String>();
					}
					if (!collectionOfMaskers.contains(maskingPropertyURI)) {
						collectionOfMaskers.add(maskingPropertyURI);
					}
					propertyMaskMap.put(maskedPropertyURI, collectionOfMaskers);
				}
			}
		} finally {
			ontModel.leaveCriticalSection();
		}
	}
		
	@Override
	public List<Property> fn(List<Property> propertyList) {
		List<Property> filteredList = new ArrayList<Property>();
		HashMap<String, Property> urisToProps = new HashMap<String, Property>();
		for (Property p: propertyList) {
			urisToProps.put(p.getURI(), p);
		}
		for (Property p : propertyList) {
			Collection<String> maskingPropertyURIs = propertyMaskMap.get(p.getURI());
			if (maskingPropertyURIs == null) {
				filteredList.add(p);
			} else {
				Property maskingProp = null;
				for (String maskingURI : maskingPropertyURIs) {
					if (urisToProps.keySet().contains(maskingURI)) {
						maskingProp = urisToProps.get(maskingURI);
						break;
					}
				}
				// BUT: don't mask a prop if it "has" (used in) statements and its masker does not
				boolean propHasStatements = false;
				boolean maskerHasStatements = false;
				if (maskingProp != null) {
					if (p instanceof ObjectProperty) {
						List<ObjectPropertyStatement> stmtList = ((ObjectProperty) p).getObjectPropertyStatements();
						propHasStatements = (stmtList != null) && (stmtList.size() > 0);
					} else if (p instanceof DataProperty) {
						List<DataPropertyStatement> stmtList = ((DataProperty) p).getDataPropertyStatements(); 
						propHasStatements = (stmtList != null) && (stmtList.size() > 0);
					}
					if (maskingProp instanceof ObjectProperty) {
						List<ObjectPropertyStatement> stmtList = ((ObjectProperty) maskingProp).getObjectPropertyStatements();
						maskerHasStatements = (stmtList != null) && (stmtList.size() > 0);
					} else if (maskingProp instanceof DataProperty) {
						List<DataPropertyStatement> stmtList = ((DataProperty) maskingProp).getDataPropertyStatements(); 
						maskerHasStatements = (stmtList != null) && (stmtList.size() > 0);
					}
				}
				if ( (maskingProp == null) || (propHasStatements & !maskerHasStatements) ) {
					filteredList.add(p);
				}
			}
		}
		return filteredList;
	}
	
}
