/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.individual;

import static org.apache.jena.ontology.OntModelSpec.OWL_MEM;
import static edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary.LABEL;
import static edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary.RDF_TYPE;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.LanguageOption.LANGUAGE_NEUTRAL;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.IteratorUtils;
import org.apache.log4j.Level;
import org.junit.Before;
import org.junit.Test;

import stubs.edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccessFactoryStub;
import stubs.javax.servlet.ServletContextStub;
import stubs.javax.servlet.http.HttpServletRequestStub;
import stubs.javax.servlet.http.HttpSessionStub;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.policy.BasicPolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ServletPolicyList;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Authorization;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyIface;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.publish.PublishDataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.publish.PublishObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.model.RDFServiceModel;
import edu.cornell.mannlib.vitro.webapp.web.ContentType;

/**
 * Start with a set of data and filter it appropriately.
 */
public class IndividualRdfAssemblerTest extends AbstractTestClass {
	private static final String INDIVIDUAL_URI = "http://vivo.mydomain.edu/individual/n3639";
	private static final String DOCUMENT_URI = "http://vivo.mydomain.edu/individual/n3639/n3639.n3";

	/** When comparing, consider all date-stamps to be equal. */
	private static final String DATE_DATA_PROPERTY = "http://purl.org/dc/elements/1.1/date";

	private static final String DP1 = "http://dataPropertyUri1";
	private static final String OP1 = "http://objectPropertyUri1";
	private static final String C1 = "http://class1";
	private static final String C2 = "http://class2";
	private static final String I1 = "http://individual1";

	private static final String RAW_RDF_FILENAME = "IndividualRdfAssemblerTest.rawRdf.n3";
	private static final String FILTERED_RDF_FILENAME = "IndividualRdfAssemblerTest.filteredRdf.n3";
	private static final String UNFILTERED_RDF_FILENAME = "IndividualRdfAssemblerTest.unfilteredRdf.n3";

	private static final String[] REAL_WORLD_PROPERTIES = {
			"http://vivoweb.org/ontology/core#overview",
			"http://vivoweb.org/ontology/core#hasResearchArea",
			"http://vivoweb.org/ontology/core#researchAreaOf" };

	private OntModel rawRdf;
	private OntModel expectedLod;
	private OntModel actualLod;

	private ServletContextStub ctx;
	private HttpSessionStub session;
	private HttpServletRequestStub req;
	private VitroRequest vreq;
	private RDFServiceModel rdfService;
	private IndividualRdfAssembler ira;

	@Before
	public void setLoggingLevels() {
		setLoggerLevel(ModelAccess.class, Level.ERROR);
		// setLoggerLevel(IndividualRdfAssembler.class, Level.DEBUG);
		// setLoggerLevel(RDFServiceStub.class, Level.DEBUG);
	}

	@Before
	public void setup() throws IOException {
		ctx = new ServletContextStub();
		session = new HttpSessionStub();
		session.setServletContext(ctx);

		req = new HttpServletRequestStub();
		req.setSession(session);

		req.setRequestUrl(new URL(DOCUMENT_URI));
	}

	@Test
	public void getOutgoingStatements() {
		Statement s1 = dataStmt(INDIVIDUAL_URI, DP1, "value");
		Statement s2 = objectStmt(INDIVIDUAL_URI, OP1, I1);
		rawRdf = model(s1, s2);
		expectedLod = includeDocInfo(model(s1, s2));
		policyUnrestricted();
		filterAndCompare("getOutgoingStatements");
	}

	@Test
	public void filterOutgoingStatements() {
		Statement s1 = dataStmt(INDIVIDUAL_URI, DP1, "value");
		Statement s2 = objectStmt(INDIVIDUAL_URI, OP1, I1);
		rawRdf = model(s1, s2);
		expectedLod = includeDocInfo(model());
		policyRestrictByPredicate(DP1, OP1);
		filterAndCompare("filterOutgoingStatements");
	}

	@Test
	public void getIncomingStatements() {
		Statement s1 = objectStmt(I1, OP1, INDIVIDUAL_URI);
		rawRdf = model(s1);
		expectedLod = includeDocInfo(model(s1));
		policyUnrestricted();
		filterAndCompare("getIncomingStatements");
	}

	@Test
	public void filterIncomingStatements() {
		Statement s1 = objectStmt(I1, OP1, INDIVIDUAL_URI);
		rawRdf = model(s1);
		expectedLod = includeDocInfo(model());
		policyRestrictByPredicate(OP1);
		filterAndCompare("filterIncomingStatements");
	}

	@Test
	public void getLabelAndTypeOfOutgoingObjects() {
		Statement s1 = objectStmt(INDIVIDUAL_URI, OP1, I1);
		Statement s2 = dataStmt(I1, LABEL, "silly label");
		Statement s3 = objectStmt(I1, RDF_TYPE, C1);
		rawRdf = model(s1, s2, s3);
		expectedLod = includeDocInfo(model(s1, s2, s3));
		policyUnrestricted();
		filterAndCompare("getLabelAndTypeOfOutgoingObjects");
	}

	@Test
	public void filterOrphanStatementsOfOutgoingObjects() {
		Statement s1 = objectStmt(INDIVIDUAL_URI, OP1, I1);
		Statement s2 = dataStmt(I1, LABEL, "silly label");
		Statement s3 = objectStmt(I1, RDF_TYPE, C1);
		rawRdf = model(s1, s2, s3);
		expectedLod = includeDocInfo(model());
		policyRestrictByPredicate(OP1);
		filterAndCompare("filterOrphanStatementsOfOutgoingObjects");
	}

	@Test
	public void getLabelAndTypeOfIncomingObjects() {
		Statement s1 = objectStmt(I1, OP1, INDIVIDUAL_URI);
		Statement s2 = dataStmt(I1, LABEL, "silly label");
		Statement s3 = objectStmt(I1, RDF_TYPE, C1);
		rawRdf = model(s1, s2, s3);
		expectedLod = includeDocInfo(model(s1, s2, s3));
		policyUnrestricted();
		filterAndCompare("getLabelAndTypeOfIncomingObjects");
	}

	@Test
	public void filterOrphanStatementsOfIncomingObjects() {
		Statement s1 = objectStmt(I1, OP1, INDIVIDUAL_URI);
		Statement s2 = dataStmt(I1, LABEL, "silly label");
		Statement s3 = objectStmt(I1, RDF_TYPE, C1);
		rawRdf = model(s1, s2, s3);
		expectedLod = includeDocInfo(model());
		policyRestrictByPredicate(OP1);
		filterAndCompare("filterOrphanStatementsOfIncomingObjects");
	}

	@Test
	public void getTypeStatements() {
		Statement s1 = objectStmt(INDIVIDUAL_URI, RDF_TYPE, C1);
		rawRdf = model(s1);
		expectedLod = includeDocInfo(model(s1));
		policyUnrestricted();
		filterAndCompare("getTypeStatements");
	}

	@Test
	public void filterTypeStatements() {
		Statement s1 = objectStmt(INDIVIDUAL_URI, RDF_TYPE, C1);
		rawRdf = model(s1);
		expectedLod = includeDocInfo(model());
		policyRestrictByClass(C1);
		filterAndCompare("filterTypeStatements");
	}

	@Test
	public void getTypeAndLabelOfType() {
		Statement s1 = objectStmt(INDIVIDUAL_URI, RDF_TYPE, C1);
		Statement s2 = dataStmt(C1, LABEL, "silly label");
		Statement s3 = objectStmt(C1, RDF_TYPE, C2);
		rawRdf = model(s1, s2, s3);
		expectedLod = includeDocInfo(model(s1, s2, s3));
		policyUnrestricted();
		filterAndCompare("getTypeAndLabelOfType");
	}

	@Test
	public void filterOrphanTypeAndLabelOfType() {
		Statement s1 = objectStmt(INDIVIDUAL_URI, RDF_TYPE, C1);
		Statement s2 = dataStmt(I1, LABEL, "silly label");
		Statement s3 = objectStmt(I1, RDF_TYPE, C2);
		rawRdf = model(s1, s2, s3);
		expectedLod = includeDocInfo(model());
		policyRestrictByClass(C1);
		filterAndCompare("filterOrphanTypeAndLabelOfType");
	}

	@Test
	public void dontGetOtherStatementsFromOutgoingObjects() {
		Statement s1 = objectStmt(INDIVIDUAL_URI, OP1, I1);
		Statement s2 = dataStmt(I1, DP1, "silly data property");
		rawRdf = model(s1, s2);
		expectedLod = includeDocInfo(model(s1));
		policyUnrestricted();
		filterAndCompare("dontGetOtherStatementsFromOutgoingObjects");
	}

	@Test
	public void dontGetOtherStatementsFromIncomingObjects() {
		Statement s1 = objectStmt(I1, OP1, INDIVIDUAL_URI);
		Statement s2 = dataStmt(I1, DP1, "silly data property");
		rawRdf = model(s1, s2);
		expectedLod = includeDocInfo(model(s1));
		policyUnrestricted();
		filterAndCompare("dontGetOtherStatementsFromIncomingObjects");
	}

	@Test
	public void dontGetUnrelatedStatements() {
		Statement s1 = dataStmt(I1, DP1, "silly data property");
		rawRdf = model(s1);
		expectedLod = includeDocInfo(model());
		policyUnrestricted();
		filterAndCompare("dontGetUnrelatedStatements");
	}

	@Test
	public void realWorldTestRoot() throws IOException {
		rawRdf = readModelFromFile(RAW_RDF_FILENAME, "N3");
		expectedLod = readModelFromFile(UNFILTERED_RDF_FILENAME, "N3");
		policyUnrestricted();
		filterAndCompare("real world test - root");
	}

	@Test
	public void realWorldTestPublic() throws IOException {
		rawRdf = readModelFromFile(RAW_RDF_FILENAME, "N3");
		expectedLod = readModelFromFile(FILTERED_RDF_FILENAME, "N3");
		policyRestrictByPredicate(REAL_WORLD_PROPERTIES);
		filterAndCompare("real world test - public");
	}

	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

	private Statement dataStmt(String subjectUri, String predicateUri,
			String value) {
		Resource subject = ResourceFactory.createResource(subjectUri);
		Property predicate = ResourceFactory.createProperty(predicateUri);
		Literal object = ResourceFactory.createPlainLiteral(value);
		return ResourceFactory.createStatement(subject, predicate, object);
	}

	private Statement objectStmt(String subjectUri, String predicateUri,
			String objectUri) {
		Resource subject = ResourceFactory.createResource(subjectUri);
		Property predicate = ResourceFactory.createProperty(predicateUri);
		Resource object = ResourceFactory.createResource(objectUri);
		return ResourceFactory.createStatement(subject, predicate, object);
	}

	private OntModel model(Statement... stmts) {
		OntModel m = ModelFactory.createOntologyModel(OWL_MEM);
		m.add(Arrays.asList(stmts));
		return m;
	}

	private OntModel includeDocInfo(OntModel m) {
		List<Statement> list = new ArrayList<>();
		list.add(dataStmt(DOCUMENT_URI, LABEL, "RDF description of "
				+ INDIVIDUAL_URI));
		list.add(dataStmt(DOCUMENT_URI, DATE_DATA_PROPERTY, "bogusTimeStamp"));
		list.add(objectStmt(DOCUMENT_URI, RDF_TYPE,
				"http://xmlns.com/foaf/0.1/Document"));
		list.add(objectStmt(DOCUMENT_URI,
				"http://purl.org/dc/elements/1.1/publisher",
				"http://vivo.mydomain.edu"));
		list.add(objectStmt(DOCUMENT_URI,
				"http://purl.org/dc/elements/1.1/rights",
				"http://vivo.mydomain.edu/termsOfUse"));

		m.add(list);
		return m;

	}

	private void policyUnrestricted() {
		ServletPolicyList.addPolicy(ctx, new UnrestrictedPolicy());
	}

	private void policyRestrictByPredicate(String... predicateUris) {
		ServletPolicyList.addPolicy(ctx, new RestrictionsPolicy(predicateUris,
				new String[0]));
	}

	private void policyRestrictByClass(String... classUris) {
		ServletPolicyList.addPolicy(ctx, new RestrictionsPolicy(new String[0],
				classUris));
	}

	private void filterAndCompare(String message) {
		setupIndividualRdfAssembler();

		actualLod = runGetRdf();

		List<Statement> missing = modelDifference(expectedLod, actualLod);
		List<Statement> extra = modelDifference(actualLod, expectedLod);
		removeMatchingDateStatements(missing, extra);

		if (missing.isEmpty() && extra.isEmpty()) {
			return;
		}

		failComparison(message, missing, extra);
	}

	private void setupIndividualRdfAssembler() {
		rdfService = new RDFServiceModel(rawRdf);
		new ModelAccessFactoryStub().get(req).setRDFService(rdfService, LANGUAGE_NEUTRAL);
		vreq = new VitroRequest(req);
		ira = new IndividualRdfAssembler(vreq, INDIVIDUAL_URI, ContentType.N3);
	}

	private OntModel runGetRdf() {
		try {
			Method m = IndividualRdfAssembler.class.getDeclaredMethod("getRdf");
			m.setAccessible(true);
			return (OntModel) m.invoke(ira);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void failComparison(String message, List<Statement> missing,
			List<Statement> extra) {
		StringWriter sw = new StringWriter();
		PrintWriter w = new PrintWriter(sw);
		w.println(message);
		writeStatementList("Missing statements:", missing, w);
		writeStatementList("Extra statements:", extra, w);
		System.err.print(sw.toString());
		fail(sw.toString());
	}

	private void writeStatementList(String label, List<Statement> list,
			PrintWriter w) {
		if (list.isEmpty()) {
			return;
		}
		w.println(label);
		for (Statement s : list) {
			w.println("   " + s);
		}
	}

	private List<Statement> modelDifference(OntModel first, OntModel second) {
		OntModel temp = ModelFactory.createOntologyModel(OWL_MEM);
		temp.add(first);
		temp.remove(statementList(second));
		return statementList(temp);
	}

	@SuppressWarnings("unchecked")
	private List<Statement> statementList(OntModel m) {
		return IteratorUtils.toList(m.listStatements());
	}

	/**
	 * If two date statements have the same subject, then lets assume that their
	 * dates are the same. In fact, the actual date statement will have the
	 * current date/time.
	 */
	private void removeMatchingDateStatements(List<Statement> list1,
			List<Statement> list2) {
		for (Iterator<Statement> it1 = list1.iterator(); it1.hasNext();) {
			Statement stmt1 = it1.next();
			String subject1 = stmt1.getSubject().getURI();
			String predicate1 = stmt1.getPredicate().getURI();
			if (DATE_DATA_PROPERTY.equals(predicate1)) {
				for (Iterator<Statement> it2 = list2.iterator(); it2.hasNext();) {
					Statement stmt2 = it2.next();
					String subject2 = stmt2.getSubject().getURI();
					String predicate2 = stmt2.getPredicate().getURI();
					if (predicate1.equals(predicate2)
							&& subject1.equals(subject2)) {
						it1.remove();
						it2.remove();
					}
				}
			}
		}
	}

	// ----------------------------------------------------------------------
	// Helper class
	// ----------------------------------------------------------------------

	private abstract static class AbstractTestPolicy implements PolicyIface {
		@Override
		public PolicyDecision isAuthorized(IdentifierBundle whoToAuth,
				RequestedAction whatToAuth) {
			if (whatToAuth instanceof PublishDataPropertyStatement) {
				return filterDataProperty((PublishDataPropertyStatement) whatToAuth);
			} else if (whatToAuth instanceof PublishObjectPropertyStatement) {
				return filterObjectProperty((PublishObjectPropertyStatement) whatToAuth);
			} else {
				return inconclusive("Bogus");
			}
		}

		private PolicyDecision filterDataProperty(
				PublishDataPropertyStatement pdps) {
			return filter(pdps.getPredicateUri(), null);
		}

		private PolicyDecision filterObjectProperty(
				PublishObjectPropertyStatement pops) {
			String propertyUri = pops.getPredicateUri();
			if (VitroVocabulary.RDF_TYPE.equals(propertyUri)) {
				return filter(propertyUri, pops.getObjectUri());
			} else {
				return filter(propertyUri, null);
			}
		}

		protected abstract PolicyDecision filter(String propertyUri,
				String classUri);

		protected BasicPolicyDecision authorized(String message) {
			return new BasicPolicyDecision(Authorization.AUTHORIZED, message);
		}

		protected BasicPolicyDecision inconclusive(String message) {
			return new BasicPolicyDecision(Authorization.INCONCLUSIVE, message);
		}
	}

	private static class UnrestrictedPolicy extends AbstractTestPolicy {
		@Override
		protected PolicyDecision filter(String propertyUri, String classUri) {
			return authorized("Totally unrestricted");
		}
	}

	private static class RestrictionsPolicy extends AbstractTestPolicy {
		private final List<String> filteredPropertyUris;
		private final List<String> filteredClassUris;

		public RestrictionsPolicy(String[] filteredPropertyUris,
				String[] filteredClassUris) {
			this.filteredPropertyUris = Arrays.asList(filteredPropertyUris);
			this.filteredClassUris = Arrays.asList(filteredClassUris);
		}

		@Override
		protected PolicyDecision filter(String propertyUri, String classUri) {
			if ((propertyUri != null)
					&& filteredPropertyUris.contains(propertyUri)) {
				return inconclusive("Filtered property: " + propertyUri);
			}
			if ((classUri != null) && filteredClassUris.contains(classUri)) {
				return inconclusive("Filtered class: " + classUri);
			}
			return authorized("Passed the filters: " + propertyUri + ","
					+ classUri);
		}
	}

}
