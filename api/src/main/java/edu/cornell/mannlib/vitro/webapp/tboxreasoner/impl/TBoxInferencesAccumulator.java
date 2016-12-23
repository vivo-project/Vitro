/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.tboxreasoner.impl;

import static org.apache.jena.rdf.model.ResourceFactory.createProperty;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLNamedObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

/**
 * Build a model of inferred statements by walking through the ontology as
 * represented in the reasoner.
 * 
 * TODO Get rid of the kluges. Either decide that they are not necessary, or
 * give them full status.
 */
public class TBoxInferencesAccumulator {
	private static final Log log = LogFactory
			.getLog(TBoxInferencesAccumulator.class);

	private static final Property RDFS_TYPE = createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
	private static final Resource OWL_CLASS = createResource("http://www.w3.org/2002/07/owl#Class");
	private static final Property OWL_EQUIVALENT_CLASS = createProperty("http://www.w3.org/2002/07/owl#equivalentClass");
	private static final Property OWL_DISJOINT_WITH = createProperty("http://www.w3.org/2002/07/owl#disjointWith");
	private static final Property OWL_SUBCLASS_OF = createProperty("http://www.w3.org/2000/01/rdf-schema#subClassOf");
	private static final Property OWL_SUBPROPERTY_OF = createProperty("http://www.w3.org/2000/01/rdf-schema#subPropertyOf");
	private static final Property OWL_INVERSE_OF = createProperty("http://www.w3.org/2002/07/owl#inverseOf");

	public Model populateModelFromReasonerQueries(OWLReasoner reasoner) {
		Model m = ModelFactory.createDefaultModel();
		populateClasses(reasoner, m);
		populateObjectProperties(reasoner, m);
		populateDataProperties(reasoner, m);
		klugeOwlInvariants(m);
		klugeMistakes(m);
		return m;
	}

	private void populateClasses(OWLReasoner r, Model m) {
		OWLClass bottom = r.getBottomClassNode().getRepresentativeElement();
		for (OWLClass c : r.getSuperClasses(bottom, false).getFlattened()) {
			populateClass(c, r, m);
		}
	}

	private void populateClass(OWLClass c, OWLReasoner r, Model m) {
		log.debug("Owl class: " + c);
		populateClassType(c, m);
		populateEquivalentClasses(c, r, m);
		populateDisjointClasses(c, r, m);
		populateSubClasses(c, r, m);
	}

	private void populateClassType(OWLClass c, Model m) {
		log.debug(c + " is a class.");
		m.add(toResource(c), RDFS_TYPE, OWL_CLASS);
	}

	private void populateEquivalentClasses(OWLClass c, OWLReasoner r, Model m) {
		for (OWLClass equiv : r.getEquivalentClasses(c).getEntities()) {
			log.debug("Equivalent class: " + c + ", " + equiv);
			m.add(toResource(c), OWL_EQUIVALENT_CLASS, toResource(equiv));
		}
	}

	private void populateDisjointClasses(OWLClass c, OWLReasoner r, Model m) {
		for (OWLClass d : r.getDisjointClasses(c).getFlattened()) {
			if (!d.isOWLNothing()) {
				log.debug("Disjoint class: " + c + ", " + d);
				m.add(toResource(c), OWL_DISJOINT_WITH, toResource(d));
			}
		}
	}

	private void populateSubClasses(OWLClass c, OWLReasoner r, Model m) {
		for (OWLClass sub : r.getSubClasses(c, false).getFlattened()) {
			log.debug(sub + " is subclass of " + c);
			if (!sub.isOWLNothing()) {
				m.add(toResource(sub), OWL_SUBCLASS_OF, toResource(c));
			}
		}
	}

	private void populateObjectProperties(OWLReasoner r, Model m) {
		OWLObjectPropertyExpression bottom = r.getBottomObjectPropertyNode()
				.getRepresentativeElement();
		populateObjectProperty(bottom, r, m);
		for (OWLObjectPropertyExpression op : r.getSuperObjectProperties(
				bottom, false).getFlattened()) {
			populateObjectProperty(op, r, m);
		}
	}

	private void populateObjectProperty(OWLObjectPropertyExpression ope,
			OWLReasoner r, Model m) {
		if (!ope.isAnonymous()) {
			OWLObjectProperty op = ope.asOWLObjectProperty();
			log.debug("object property: " + op);
			populateObjectSubProperties(op, r, m);
			populateObjectInverseProperties(op, r, m);
		}
	}

	private void populateObjectSubProperties(OWLObjectProperty op,
			OWLReasoner r, Model m) {
		for (OWLObjectPropertyExpression subOPE : r.getSubObjectProperties(op,
				false).getFlattened()) {
			if (!subOPE.isAnonymous()) {
				OWLObjectProperty subOP = subOPE.asOWLObjectProperty();
				log.debug(subOP + " object sub-property of " + op);
				m.add(toResource(subOP), OWL_SUBPROPERTY_OF, toResource(op));
			}
		}
		// getSubObjectProperties is strict, so add the reflexive statement.
		log.debug(op + " object sub-property of " + op);
		m.add(toResource(op), OWL_SUBPROPERTY_OF, toResource(op));
	}

	private void populateObjectInverseProperties(OWLObjectProperty op,
			OWLReasoner r, Model m) {
		for (OWLObjectPropertyExpression inverseE : r
				.getInverseObjectProperties(op)) {
			if (!inverseE.isAnonymous()
					&& !inverseE.isOWLBottomObjectProperty()
					&& !inverseE.isOWLTopObjectProperty()) {
				OWLObjectProperty inverse = inverseE.asOWLObjectProperty();
				log.debug(inverse + " object inverse of " + op);
				m.add(toResource(inverse), OWL_INVERSE_OF, toResource(op));
			}
		}
	}

	private void populateDataProperties(OWLReasoner r, Model m) {
		OWLDataProperty bottom = r.getBottomDataPropertyNode()
				.getRepresentativeElement();
		populateDataProperty(bottom, r, m);
		for (OWLDataProperty dp : r.getSuperDataProperties(bottom, false)
				.getFlattened()) {
			populateDataProperty(dp, r, m);
		}
	}

	private void populateDataProperty(OWLDataProperty dp, OWLReasoner r, Model m) {
		log.debug("data property: " + dp);
		populateDataSubProperties(dp, r, m);
	}

	private void populateDataSubProperties(OWLDataProperty dp, OWLReasoner r,
			Model m) {
		for (OWLDataProperty subDP : r.getSubDataProperties(dp, false)
				.getFlattened()) {
			log.debug(subDP + " data sub-property of " + dp);
			m.add(toResource(subDP), OWL_SUBPROPERTY_OF, toResource(dp));
		}
		// getSubDataProperties is strict, so add the reflexive statement.
		log.debug(dp + " data sub-property of " + dp);
		m.add(toResource(dp), OWL_SUBPROPERTY_OF, toResource(dp));
	}

	private Resource toResource(OWLNamedObject owlObject) {
		return createResource(owlObject.getIRI().toString());
	}

	private static final Resource OWL_THING = createResource("http://www.w3.org/2002/07/owl#Thing");

	private static final Resource OWL_OBJECT_PROPERTY = createResource("http://www.w3.org/2002/07/owl#ObjectProperty");
	private static final Resource OWL_TOP_OBJECT_PROPERTY = createResource("http://www.w3.org/2002/07/owl#topObjectProperty");
	private static final Resource OWL_BOTTOM_OBJECT_PROPERTY = createResource("http://www.w3.org/2002/07/owl#bottomObjectProperty");
	private static final Resource OWL_DATA_PROPERTY = createResource("http://www.w3.org/2002/07/owl#DatatypeProperty");
	private static final Resource OWL_TOP_DATA_PROPERTY = createResource("http://www.w3.org/2002/07/owl#topDataProperty");
	private static final Resource OWL_BOTTOM_DATA_PROPERTY = createResource("http://www.w3.org/2002/07/owl#bottomDataProperty");
	private static final Resource OWL_FUNCTIONAL_PROPERTY = createResource("http://www.w3.org/2002/07/owl#FunctionalProperty");
	private static final Resource OWL_INVERSE_FUNCTIONAL_PROPERTY = createResource("http://www.w3.org/2002/07/owl#InverseFunctionalProperty");
	private static final Resource OWL_TRANSITIVE_PROPERTY = createResource("http://www.w3.org/2002/07/owl#TransitiveProperty");
	private static final Resource OWL_SYMMETRIC_PROPERTY = createResource("http://www.w3.org/2002/07/owl#SymmetricProperty");
	private static final Resource OWL_ASYMMETRIC_PROPERTY = createResource("http://www.w3.org/2002/07/owl#AsymmetricProperty");
	private static final Resource OWL_REFLEXIVE_PROPERTY = createResource("http://www.w3.org/2002/07/owl#ReflexiveProperty");
	private static final Resource OWL_IRREFLEXIVE_PROPERTY = createResource("http://www.w3.org/2002/07/owl#IrreflexiveProperty");
	private static final Resource OWL_ANNOTATION_PROPERTY = createResource("http://www.w3.org/2002/07/owl#AnnotationProperty");

	private static final Resource OWL_VERSION_INFO = createResource("http://www.w3.org/2002/07/owl#versionInfo");
	private static final Resource OWL_BACKWARD_COMPATIBLE_WITH = createResource("http://www.w3.org/2002/07/owl#backwardCompatibleWith");
	private static final Resource OWL_INCOMPATIBLE_WITH = createResource("http://www.w3.org/2002/07/owl#incompatibleWith");
	private static final Resource OWL_PRIOR_VERSION = createResource("http://www.w3.org/2002/07/owl#priorVersion");

	private static final Resource RDFS_SEE_ALSO = createResource("http://www.w3.org/2000/01/rdf-schema#seeAlso");
	private static final Resource RDFS_COMMENT = createResource("http://www.w3.org/2000/01/rdf-schema#comment");
	private static final Resource RDFS_IS_DEFINED_BY = createResource("http://www.w3.org/2000/01/rdf-schema#isDefinedBy");
	private static final Resource RDFS_LABEL = createResource("http://www.w3.org/2000/01/rdf-schema#label");

	private void klugeOwlInvariants(Model m) {
		m.add(OWL_BOTTOM_DATA_PROPERTY, RDFS_TYPE, OWL_DATA_PROPERTY);
		m.add(OWL_BOTTOM_DATA_PROPERTY, RDFS_TYPE, OWL_FUNCTIONAL_PROPERTY);

		m.add(OWL_TOP_DATA_PROPERTY, RDFS_TYPE, OWL_DATA_PROPERTY);

		m.add(OWL_BOTTOM_OBJECT_PROPERTY, RDFS_TYPE, OWL_OBJECT_PROPERTY);
		m.add(OWL_BOTTOM_OBJECT_PROPERTY, RDFS_TYPE, OWL_FUNCTIONAL_PROPERTY);
		m.add(OWL_BOTTOM_OBJECT_PROPERTY, RDFS_TYPE,
				OWL_INVERSE_FUNCTIONAL_PROPERTY);
		m.add(OWL_BOTTOM_OBJECT_PROPERTY, RDFS_TYPE, OWL_TRANSITIVE_PROPERTY);
		m.add(OWL_BOTTOM_OBJECT_PROPERTY, RDFS_TYPE, OWL_SYMMETRIC_PROPERTY);
		m.add(OWL_BOTTOM_OBJECT_PROPERTY, RDFS_TYPE, OWL_ASYMMETRIC_PROPERTY);
		m.add(OWL_BOTTOM_OBJECT_PROPERTY, RDFS_TYPE, OWL_IRREFLEXIVE_PROPERTY);

		m.add(OWL_TOP_OBJECT_PROPERTY, RDFS_TYPE, OWL_OBJECT_PROPERTY);
		m.add(OWL_TOP_OBJECT_PROPERTY, RDFS_TYPE, OWL_TRANSITIVE_PROPERTY);
		m.add(OWL_TOP_OBJECT_PROPERTY, RDFS_TYPE, OWL_REFLEXIVE_PROPERTY);
		m.add(OWL_TOP_OBJECT_PROPERTY, RDFS_TYPE, OWL_SYMMETRIC_PROPERTY);

		m.add(OWL_THING, RDFS_TYPE, OWL_CLASS);

		m.add(OWL_VERSION_INFO, RDFS_TYPE, OWL_ANNOTATION_PROPERTY);
		m.add(OWL_BACKWARD_COMPATIBLE_WITH, RDFS_TYPE, OWL_ANNOTATION_PROPERTY);
		m.add(OWL_PRIOR_VERSION, RDFS_TYPE, OWL_ANNOTATION_PROPERTY);
		m.add(OWL_INCOMPATIBLE_WITH, RDFS_TYPE, OWL_ANNOTATION_PROPERTY);
		m.add(RDFS_SEE_ALSO, RDFS_TYPE, OWL_ANNOTATION_PROPERTY);
		m.add(RDFS_COMMENT, RDFS_TYPE, OWL_ANNOTATION_PROPERTY);
		m.add(RDFS_IS_DEFINED_BY, RDFS_TYPE, OWL_ANNOTATION_PROPERTY);
		m.add(RDFS_LABEL, RDFS_TYPE, OWL_ANNOTATION_PROPERTY);
	}

	private void klugeMistakes(Model m) {
		Property ISF_DEPRECATED = createProperty("http://isf/deprecated_op");
		m.add(ISF_DEPRECATED, RDFS_TYPE, OWL_OBJECT_PROPERTY);
		m.add(ISF_DEPRECATED, OWL_SUBPROPERTY_OF, ISF_DEPRECATED);
		m.add(ISF_DEPRECATED, OWL_SUBPROPERTY_OF, OWL_TOP_OBJECT_PROPERTY);
		m.add(OWL_BOTTOM_OBJECT_PROPERTY, OWL_SUBPROPERTY_OF, ISF_DEPRECATED);

		Resource OWL_DEPRECATED = createResource("http://www.w3.org/2002/07/owl#DeprecatedProperty");
		m.add(OWL_DEPRECATED, RDFS_TYPE, OWL_OBJECT_PROPERTY);
		m.add(OWL_DEPRECATED, OWL_SUBPROPERTY_OF, OWL_DEPRECATED);
		m.add(OWL_DEPRECATED, OWL_SUBPROPERTY_OF, OWL_TOP_OBJECT_PROPERTY);
		m.add(OWL_BOTTOM_OBJECT_PROPERTY, OWL_SUBPROPERTY_OF, OWL_DEPRECATED);

		Property DCT_CONTRIBUTOR = createProperty("http://purl.org/dc/terms/contributor");
		m.add(DCT_CONTRIBUTOR, RDFS_TYPE, OWL_OBJECT_PROPERTY);
		m.add(DCT_CONTRIBUTOR, OWL_SUBPROPERTY_OF, DCT_CONTRIBUTOR);
		m.add(DCT_CONTRIBUTOR, OWL_SUBPROPERTY_OF, OWL_TOP_OBJECT_PROPERTY);
		m.add(OWL_BOTTOM_OBJECT_PROPERTY, OWL_SUBPROPERTY_OF, DCT_CONTRIBUTOR);

		Resource ARG_2000400 = createResource("http://purl.obolibrary.org/obo/ARG_2000400");
		Resource BFO_0000001 = createResource("http://purl.obolibrary.org/obo/BFO_0000001");
		Resource BFO_0000002 = createResource("http://purl.obolibrary.org/obo/BFO_0000002");
		Resource BFO_0000031 = createResource("http://purl.obolibrary.org/obo/BFO_0000031");
		Resource IAO_0000003 = createResource("http://purl.obolibrary.org/obo/IAO_0000003");
		Resource IAO_0000009 = createResource("http://purl.obolibrary.org/obo/IAO_0000009");
		Resource IAO_0000030 = createResource("http://purl.obolibrary.org/obo/IAO_0000030");
		m.add(ARG_2000400, RDFS_TYPE, BFO_0000001);
		m.add(ARG_2000400, RDFS_TYPE, BFO_0000002);
		m.add(ARG_2000400, RDFS_TYPE, BFO_0000031);
		m.add(ARG_2000400, RDFS_TYPE, IAO_0000003);
		m.add(ARG_2000400, RDFS_TYPE, IAO_0000009);
		m.add(ARG_2000400, RDFS_TYPE, IAO_0000030);
		m.add(ARG_2000400, RDFS_TYPE, OWL_THING);

		Resource BIBO_ACCEPTED = createResource("http://purl.org/ontology/bibo/accepted");
		Resource BIBO_DRAFT = createResource("http://purl.org/ontology/bibo/draft");
		Resource BIBO_PEER_REVIEWED = createResource("http://purl.org/ontology/bibo/peerReviewed");
		Resource BIBO_PUBLISHED = createResource("http://purl.org/ontology/bibo/published");
		Resource BIBO_REJECTED = createResource("http://purl.org/ontology/bibo/rejected");
		Resource BIBO_UNPUBLISHED = createResource("http://purl.org/ontology/bibo/unpublished");
		m.add(BIBO_ACCEPTED, RDFS_TYPE, OWL_THING);
		m.add(BIBO_DRAFT, RDFS_TYPE, OWL_THING);
		m.add(BIBO_PEER_REVIEWED, RDFS_TYPE, OWL_THING);
		m.add(BIBO_PUBLISHED, RDFS_TYPE, OWL_THING);
		m.add(BIBO_REJECTED, RDFS_TYPE, OWL_THING);
		m.add(BIBO_UNPUBLISHED, RDFS_TYPE, OWL_THING);

		Resource CORE_YMDT_PRECISION = createResource("http://vivoweb.org/ontology/core#yearMonthDayTimePrecision");
		Resource CORE_YMD_PRECISION = createResource("http://vivoweb.org/ontology/core#yearMonthDayPrecision");
		Resource CORE_YM_PRECISION = createResource("http://vivoweb.org/ontology/core#yearMonthPrecision");
		Resource CORE_Y_PRECISION = createResource("http://vivoweb.org/ontology/core#yearPrecision");
		Resource SKOS_CONCEPT = createResource("http://www.w3.org/2004/02/skos/core#Concept");
		m.add(CORE_YMDT_PRECISION, RDFS_TYPE, OWL_THING);
		m.add(CORE_YMDT_PRECISION, RDFS_TYPE, SKOS_CONCEPT);
		m.add(CORE_YMD_PRECISION, RDFS_TYPE, OWL_THING);
		m.add(CORE_YMD_PRECISION, RDFS_TYPE, SKOS_CONCEPT);
		m.add(CORE_YM_PRECISION, RDFS_TYPE, OWL_THING);
		m.add(CORE_YM_PRECISION, RDFS_TYPE, SKOS_CONCEPT);
		m.add(CORE_Y_PRECISION, RDFS_TYPE, OWL_THING);
		m.add(CORE_Y_PRECISION, RDFS_TYPE, SKOS_CONCEPT);

		Resource CORE_CONTACT_INFO = createResource("http://vivoweb.org/ontology/core#contactInformation");
		m.add(CORE_CONTACT_INFO, RDFS_TYPE, OWL_DATA_PROPERTY);

		Resource CORE_HAS_FACILITY = createResource("http://vivoweb.org/ontology/core#hasFacility");
		m.add(CORE_HAS_FACILITY, RDFS_TYPE, OWL_OBJECT_PROPERTY);

		Resource CORE_HAS_FUNDING = createResource("http://vivoweb.org/ontology/core#hasFundingVehicle");
		m.add(CORE_HAS_FUNDING, RDFS_TYPE, OWL_OBJECT_PROPERTY);

		Resource CORE_HAS_GOV_AUTH = createResource("http://vivoweb.org/ontology/core#hasGoverningAuthority");
		m.add(CORE_HAS_GOV_AUTH, RDFS_TYPE, OWL_OBJECT_PROPERTY);

		Resource CORE_IN_PRESS = createResource("http://vivoweb.org/ontology/core#inPress");
		m.add(CORE_IN_PRESS, RDFS_TYPE, OWL_THING);

		Resource CORE_INVITED = createResource("http://vivoweb.org/ontology/core#invited");
		m.add(CORE_INVITED, RDFS_TYPE, OWL_THING);

		Resource CORE_SUBMITTED = createResource("http://vivoweb.org/ontology/core#submitted");
		m.add(CORE_SUBMITTED, RDFS_TYPE, OWL_THING);

		Resource OBO_HAS_AGENT = createResource("http://www.obofoundry.org/ro/ro.owl#has_agent");
		m.add(OBO_HAS_AGENT, RDFS_TYPE, OWL_OBJECT_PROPERTY);

		Resource OBI_0000066 = createResource("http://purl.obolibrary.org/obo/OBI_0000066");
		m.remove(OBI_0000066, RDFS_TYPE, OWL_CLASS);
		m.remove(OBI_0000066, OWL_SUBCLASS_OF, OWL_THING);
		m.remove(OBI_0000066, OWL_EQUIVALENT_CLASS, OBI_0000066);

		Resource OBI_0000086 = createResource("http://purl.obolibrary.org/obo/OBI_0000086");
		m.remove(OBI_0000086, RDFS_TYPE, OWL_CLASS);
		m.remove(OBI_0000086, OWL_SUBCLASS_OF, OWL_THING);
		m.remove(OBI_0000086, OWL_EQUIVALENT_CLASS, OBI_0000086);

		Resource OBI_0000094 = createResource("http://purl.obolibrary.org/obo/OBI_0000094");
		m.remove(OBI_0000094, RDFS_TYPE, OWL_CLASS);
		m.remove(OBI_0000094, OWL_SUBCLASS_OF, OWL_THING);
		m.remove(OBI_0000094, OWL_EQUIVALENT_CLASS, OBI_0000094);

		Resource OBI_0000571 = createResource("http://purl.obolibrary.org/obo/OBI_0000571");
		m.remove(OBI_0000571, RDFS_TYPE, OWL_CLASS);
		m.remove(OBI_0000571, OWL_SUBCLASS_OF, OWL_THING);
		m.remove(OBI_0000571, OWL_EQUIVALENT_CLASS, OBI_0000571);
	}
}
