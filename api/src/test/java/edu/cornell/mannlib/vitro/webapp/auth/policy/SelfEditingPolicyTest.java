/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy;

import static edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Authorization.AUTHORIZED;
import static edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Authorization.INCONCLUSIVE;
import static edu.cornell.mannlib.vitro.webapp.auth.requestedAction.RequestedAction.SOME_LITERAL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import stubs.edu.cornell.mannlib.vitro.webapp.auth.policy.bean.PropertyRestrictionBeanStub;
import stubs.javax.servlet.ServletContextStub;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.ArrayIdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.common.HasProfile;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Authorization;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.AddNewUser;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.LoadOntology;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.RebuildTextIndex;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.RemoveUser;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.ServerStatus;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.UpdateTextIndex;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ontology.CreateOwlClass;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ontology.DefineDataProperty;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ontology.DefineObjectProperty;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ontology.RemoveOwlClass;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AddDataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AddObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.DropObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.EditDataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.EditObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

public class SelfEditingPolicyTest extends AbstractTestClass {

	private static final String SAFE_NS = "http://test.mannlib.cornell.edu/ns/01#";
	private static final String UNSAFE_NS = VitroVocabulary.vitroURI;

	private static final String SELFEDITOR_URI = SAFE_NS + "individual244";
	private static final String SAFE_RESOURCE = SAFE_NS
			+ "otherIndividual77777";
	private static final String UNSAFE_RESOURCE = UNSAFE_NS
			+ "otherIndividual99999";

	private static final Property SAFE_PREDICATE = new Property(SAFE_NS + "hasHairStyle");
	private static final Property UNSAFE_PREDICATE = new Property(UNSAFE_NS + "hasSuperPowers");

	private ServletContextStub ctx;

	private SelfEditingPolicy policy;
	private IdentifierBundle ids;
	private RequestedAction whatToAuth;
	private OntModel ontModel;

	@Before
	public void setUp() throws Exception {
		ctx = new ServletContextStub();

		PropertyRestrictionBeanStub
				.getInstance(new String[] { UNSAFE_NS });

		policy = new SelfEditingPolicy(ctx);

		IndividualImpl ind = new IndividualImpl();
		ind.setURI(SELFEDITOR_URI);

		ids = new ArrayIdentifierBundle(new HasProfile(SELFEDITOR_URI));

		ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
	}

	@Test
	public void testProhibitedProperties() {
		PropertyRestrictionBeanStub
				.getInstance(new String[] { UNSAFE_NS }, new String[] {
						"http://mannlib.cornell.edu/bad#prp234",
						"http://mannlib.cornell.edu/bad#prp999",
						"http://mannlib.cornell.edu/bad#prp333",
						"http://mannlib.cornell.edu/bad#prp777",
						"http://mannlib.cornell.edu/bad#prp0020" });

		whatToAuth = new AddObjectPropertyStatement(ontModel, SELFEDITOR_URI,
				new Property("http://mannlib.cornell.edu/bad#prp234"), SAFE_RESOURCE);
		assertDecision(INCONCLUSIVE, policy.isAuthorized(ids, whatToAuth));

		whatToAuth = new AddObjectPropertyStatement(ontModel, SAFE_RESOURCE,
				new Property("http://mannlib.cornell.edu/bad#prp234"), SELFEDITOR_URI);
		assertDecision(INCONCLUSIVE, policy.isAuthorized(ids, whatToAuth));

		whatToAuth = new AddObjectPropertyStatement(ontModel, SELFEDITOR_URI,
				new Property("http://mannlib.cornell.edu/bad#prp999"), SAFE_RESOURCE);
		assertDecision(INCONCLUSIVE, policy.isAuthorized(ids, whatToAuth));

		whatToAuth = new AddObjectPropertyStatement(ontModel, SAFE_RESOURCE,
				new Property("http://mannlib.cornell.edu/bad#prp999"), SELFEDITOR_URI);
		assertDecision(INCONCLUSIVE, policy.isAuthorized(ids, whatToAuth));

		whatToAuth = new AddObjectPropertyStatement(ontModel, SAFE_RESOURCE,
				SAFE_PREDICATE, SELFEDITOR_URI);
		assertDecision(AUTHORIZED, policy.isAuthorized(ids, whatToAuth));

		whatToAuth = new AddObjectPropertyStatement(ontModel, SELFEDITOR_URI,
				SAFE_PREDICATE, SAFE_RESOURCE);
		assertDecision(AUTHORIZED, policy.isAuthorized(ids, whatToAuth));

		whatToAuth = new AddObjectPropertyStatement(ontModel, SELFEDITOR_URI,
				UNSAFE_PREDICATE, SAFE_RESOURCE);
		assertDecision(INCONCLUSIVE, policy.isAuthorized(ids, whatToAuth));

		// now with dataprop statements
		whatToAuth = new AddDataPropertyStatement(ontModel, SELFEDITOR_URI,
				"http://mannlib.cornell.edu/bad#prp234", SOME_LITERAL);
		assertDecision(INCONCLUSIVE, policy.isAuthorized(ids, whatToAuth));

		whatToAuth = new AddDataPropertyStatement(ontModel, SELFEDITOR_URI,
				"http://mannlib.cornell.edu/bad#prp999", SOME_LITERAL);
		assertDecision(INCONCLUSIVE, policy.isAuthorized(ids, whatToAuth));

		whatToAuth = new AddDataPropertyStatement(ontModel, SELFEDITOR_URI,
				SAFE_PREDICATE.getURI(), SOME_LITERAL);
		assertDecision(AUTHORIZED, policy.isAuthorized(ids, whatToAuth));

		whatToAuth = new AddDataPropertyStatement(ontModel, SELFEDITOR_URI,
				UNSAFE_PREDICATE.getURI(), SOME_LITERAL);
		assertDecision(INCONCLUSIVE, policy.isAuthorized(ids, whatToAuth));
	}

	@Test
	public void testVisitIdentifierBundleAddObjectPropStmt() {
		whatToAuth = new AddObjectPropertyStatement(ontModel, SELFEDITOR_URI,
				SAFE_PREDICATE, SAFE_RESOURCE);
		assertDecision(AUTHORIZED, policy.isAuthorized(ids, whatToAuth));

		whatToAuth = new AddObjectPropertyStatement(ontModel, SAFE_RESOURCE,
				SAFE_PREDICATE, SELFEDITOR_URI);
		assertDecision(AUTHORIZED, policy.isAuthorized(ids, whatToAuth));

		// this is the case where the editor is not part of the stmt
		whatToAuth = new AddObjectPropertyStatement(ontModel, SAFE_RESOURCE,
				SAFE_PREDICATE, SAFE_RESOURCE);
		assertDecision(INCONCLUSIVE, policy.isAuthorized(ids, whatToAuth));

		whatToAuth = new AddObjectPropertyStatement(ontModel, SELFEDITOR_URI,
				UNSAFE_PREDICATE, SAFE_RESOURCE);
		assertDecision(INCONCLUSIVE, policy.isAuthorized(ids, whatToAuth));

		whatToAuth = new AddObjectPropertyStatement(ontModel, SELFEDITOR_URI,
				SAFE_PREDICATE, UNSAFE_RESOURCE);
		assertDecision(INCONCLUSIVE, policy.isAuthorized(ids, whatToAuth));
	}

	//
	// @Test
	// public void testVisitIdentifierBundleDropResource() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testVisitIdentifierBundleDropDataPropStmt() {
	// fail("Not yet implemented");
	// }
	//
	@Test
	public void testVisitIdentifierBundleDropObjectPropStmt() {
		whatToAuth = new DropObjectPropertyStatement(ontModel, SELFEDITOR_URI,
				SAFE_PREDICATE, SAFE_RESOURCE);
		assertDecision(AUTHORIZED, policy.isAuthorized(ids, whatToAuth));

		whatToAuth = new DropObjectPropertyStatement(ontModel, SAFE_RESOURCE,
				SAFE_PREDICATE, SELFEDITOR_URI);
		assertDecision(AUTHORIZED, policy.isAuthorized(ids, whatToAuth));

		// this is the case where the editor is not part of the stmt
		whatToAuth = new DropObjectPropertyStatement(ontModel, SAFE_RESOURCE,
				SAFE_PREDICATE, SAFE_RESOURCE);
		assertDecision(INCONCLUSIVE, policy.isAuthorized(ids, whatToAuth));

		whatToAuth = new DropObjectPropertyStatement(ontModel, SELFEDITOR_URI,
				UNSAFE_PREDICATE, SAFE_RESOURCE);
		assertDecision(INCONCLUSIVE, policy.isAuthorized(ids, whatToAuth));

		whatToAuth = new DropObjectPropertyStatement(ontModel, SELFEDITOR_URI,
				SAFE_PREDICATE, UNSAFE_RESOURCE);
		assertDecision(INCONCLUSIVE, policy.isAuthorized(ids, whatToAuth));
	}

	//
	// @Test
	// public void testVisitIdentifierBundleAddResource() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testVisitIdentifierBundleAddDataPropStmt() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testVisitIdentifierBundleUploadFile() {
	// fail("Not yet implemented");
	// }
	//
	//
	@Test
	public void testVisitIdentifierBundleEditDataPropStmt() {
		whatToAuth = new EditDataPropertyStatement(ontModel, SELFEDITOR_URI,SAFE_PREDICATE.getURI(), SOME_LITERAL);
		assertDecision(AUTHORIZED, policy.isAuthorized(ids, whatToAuth));

		whatToAuth = new EditDataPropertyStatement(ontModel, SELFEDITOR_URI, UNSAFE_PREDICATE.getURI(), SOME_LITERAL);
		assertDecision(INCONCLUSIVE, policy.isAuthorized(ids, whatToAuth));

		whatToAuth = new EditDataPropertyStatement(ontModel, UNSAFE_RESOURCE, SAFE_PREDICATE.getURI(), SOME_LITERAL);
		assertDecision(INCONCLUSIVE, policy.isAuthorized(ids, whatToAuth));

		whatToAuth = new EditDataPropertyStatement(ontModel, SAFE_RESOURCE, SAFE_PREDICATE.getURI(), SOME_LITERAL);
		assertDecision(INCONCLUSIVE, policy.isAuthorized(ids, whatToAuth));
	}

	@Test
	public void testVisitIdentifierBundleEditObjPropStmt() {
		whatToAuth = new EditObjectPropertyStatement(ontModel, SELFEDITOR_URI,
				SAFE_PREDICATE, SAFE_RESOURCE);
		assertDecision(AUTHORIZED, policy.isAuthorized(ids, whatToAuth));

		whatToAuth = new EditObjectPropertyStatement(ontModel, SAFE_RESOURCE,
				SAFE_PREDICATE, SELFEDITOR_URI);
		assertDecision(AUTHORIZED, policy.isAuthorized(ids, whatToAuth));

		// this is the case where the editor is not part of the stmt
		whatToAuth = new EditObjectPropertyStatement(ontModel, SAFE_RESOURCE,
				SAFE_PREDICATE, SAFE_RESOURCE);
		assertDecision(INCONCLUSIVE, policy.isAuthorized(ids, whatToAuth));

		whatToAuth = new EditObjectPropertyStatement(ontModel, SELFEDITOR_URI,
				UNSAFE_PREDICATE, SAFE_RESOURCE);
		assertDecision(INCONCLUSIVE, policy.isAuthorized(ids, whatToAuth));

		whatToAuth = new EditObjectPropertyStatement(ontModel, SELFEDITOR_URI,
				SAFE_PREDICATE, UNSAFE_RESOURCE);
		assertDecision(INCONCLUSIVE, policy.isAuthorized(ids, whatToAuth));
	}

	// ----------------------------------------------------------------------
	// What if there are two SelfEditor Identifiers?
	// ----------------------------------------------------------------------

	@Test
	public void twoSEIsFindObjectPropertySubject() {
		setUpTwoSEIs();
		whatToAuth = new DropObjectPropertyStatement(ontModel, SELFEDITOR_URI,
				SAFE_PREDICATE, SAFE_RESOURCE);
		assertDecision(AUTHORIZED, policy.isAuthorized(ids, whatToAuth));
	}

	@Test
	public void twoSEIsFindObjectPropertyObject() {
		setUpTwoSEIs();
		whatToAuth = new DropObjectPropertyStatement(ontModel, SAFE_RESOURCE,
				SAFE_PREDICATE, SELFEDITOR_URI);
		assertDecision(AUTHORIZED, policy.isAuthorized(ids, whatToAuth));
	}

	@Test
	public void twoSEIsDontFindInObjectProperty() {
		setUpTwoSEIs();
		whatToAuth = new DropObjectPropertyStatement(ontModel, SAFE_RESOURCE,
				SAFE_PREDICATE, SAFE_RESOURCE);
		assertDecision(INCONCLUSIVE, policy.isAuthorized(ids, whatToAuth));
	}

	@Test
	public void twoSEIsFindDataPropertySubject() {
		setUpTwoSEIs();

		whatToAuth = new EditDataPropertyStatement(ontModel, SELFEDITOR_URI, SAFE_PREDICATE.getURI(), SOME_LITERAL);
		assertDecision(AUTHORIZED, policy.isAuthorized(ids, whatToAuth));
	}

	@Test
	public void twoSEIsDontFindInDataProperty() {
		setUpTwoSEIs();

		whatToAuth = new EditDataPropertyStatement(ontModel, SAFE_RESOURCE, SAFE_PREDICATE.getURI(), SOME_LITERAL);
		assertDecision(INCONCLUSIVE, policy.isAuthorized(ids, whatToAuth));
	}

	private void setUpTwoSEIs() {
		ids = new ArrayIdentifierBundle();

		IndividualImpl ind1 = new IndividualImpl();
		ind1.setURI(SAFE_NS + "bozoUri");
		ids.add(new HasProfile(ind1.getURI()));

		IndividualImpl ind2 = new IndividualImpl();
		ind2.setURI(SELFEDITOR_URI);
		ids.add(new HasProfile(ind2.getURI()));
	}

	// ----------------------------------------------------------------------
	// Ignore administrative requests.
	// ----------------------------------------------------------------------

	@Test
	public void testServerStatus() {
		assertDecision(INCONCLUSIVE,
				policy.isAuthorized(ids, new ServerStatus()));
	}

	@Test
	public void testCreateOwlClass() {
		CreateOwlClass a = new CreateOwlClass();
		a.setSubjectUri("http://someClass/test");
		assertDecision(INCONCLUSIVE, policy.isAuthorized(ids, a));
	}

	@Test
	public void testRemoveOwlClass() {
		RemoveOwlClass a = new RemoveOwlClass();
		a.setSubjectUri("http://someClass/test");
		assertDecision(INCONCLUSIVE, policy.isAuthorized(ids, a));
	}

	@Test
	public void testDefineDataProperty() {
		DefineDataProperty a = new DefineDataProperty();
		a.setSubjectUri("http://someClass/test");
		assertDecision(INCONCLUSIVE, policy.isAuthorized(ids, a));
	}

	@Test
	public void testDefineObjectProperty() {
		DefineObjectProperty a = new DefineObjectProperty();
		a.setSubjectUri("http://someClass/test");
		assertDecision(INCONCLUSIVE, policy.isAuthorized(ids, a));
	}

	@Test
	public void testAddNewUser() {
		assertDecision(INCONCLUSIVE, policy.isAuthorized(ids, new AddNewUser()));
	}

	@Test
	public void testRemoveUser() {
		assertDecision(INCONCLUSIVE, policy.isAuthorized(ids, new RemoveUser()));
	}

	@Test
	public void testLoadOntology() {
		assertDecision(INCONCLUSIVE,
				policy.isAuthorized(ids, new LoadOntology()));
	}

	@Test
	public void testRebuildTextIndex() {
		assertDecision(INCONCLUSIVE,
				policy.isAuthorized(ids, new RebuildTextIndex()));
	}

	@Test
	public void testVisitIdentifierBundleUpdateTextIndex() {
		assertDecision(INCONCLUSIVE,
				policy.isAuthorized(ids, new UpdateTextIndex()));
	}

	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

	private void assertDecision(Authorization expectedAuth,
			PolicyDecision decision) {
		if (expectedAuth == null) {
			assertNull("expecting null decision", decision);
		} else {
			assertNotNull("expecting a decision", decision);
			assertEquals("wrong authorization", expectedAuth,
					decision.getAuthorized());
		}
	}
}
