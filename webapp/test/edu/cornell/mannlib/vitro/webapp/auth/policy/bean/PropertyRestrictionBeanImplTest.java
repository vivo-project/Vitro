/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy.bean;

import static edu.cornell.mannlib.vitro.webapp.auth.policy.bean.PropertyRestrictionLevels.Which.DISPLAY;
import static edu.cornell.mannlib.vitro.webapp.auth.policy.bean.PropertyRestrictionLevels.Which.MODIFY;
import static edu.cornell.mannlib.vitro.webapp.auth.policy.bean.PropertyRestrictionLevels.Which.PUBLISH;
import static edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel.CURATOR;
import static edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel.DB_ADMIN;
import static edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel.EDITOR;
import static edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel.NOBODY;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import stubs.edu.cornell.mannlib.vitro.webapp.dao.DataPropertyDaoStub;
import stubs.edu.cornell.mannlib.vitro.webapp.dao.FauxPropertyDaoStub;
import stubs.edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyDaoStub;
import stubs.edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactoryStub;
import stubs.edu.cornell.mannlib.vitro.webapp.modelaccess.ContextModelAccessStub;
import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.auth.policy.bean.PropertyRestrictionLevels.Which;
import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel;
import edu.cornell.mannlib.vitro.webapp.beans.FauxProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyDao.FullPropertyKey;

/**
 * If we believe that authorization will succeed regardless of user role, check
 * that it succeeds with the lowest role.
 * 
 * If we believe that authorization will fail regardless of user role, check
 * that it fails with the highest role.
 * 
 * If we believe that authorization depends on the user role, it should pass if
 * the user role is higher than or equal to the threshold, and fail if the user
 * role is less than the threshold.
 * 
 * Note that we can't set a threshold to be tested to either the lowest or
 * highest role, or the attempt to test with higher or lower role will throw an
 * exception.
 */
public class PropertyRestrictionBeanImplTest extends AbstractTestClass {
	private static final String NS_PROHIBITED = "http://prbi.prohibited/";
	private static final String URI_RESOURCE_EXCEPTIONAL = NS_PROHIBITED
			+ "exceptionalResource";
	private static final String URI_PREDICATE_EXCEPTIONAL = NS_PROHIBITED
			+ "exceptionalPredicate";

	private static final List<String> PROHIBITED_NAMESPACES = Arrays
			.asList(new String[] { NS_PROHIBITED });
	private static final List<String> PERMITTED_EXCEPTIONS = Arrays
			.asList(new String[] { URI_RESOURCE_EXCEPTIONAL,
					URI_PREDICATE_EXCEPTIONAL });

	private static final String URI_RESOURCE_ANY = "http://prbi.test/anyResource";
	private static final String URI_RESOURCE_PROHIBITED = NS_PROHIBITED
			+ "resource";

	private static final String URI_PREDICATE_BARE = "http://prbi.test/barePredicate";
	private static final String URI_PREDICATE_PROHIBITED = NS_PROHIBITED
			+ "predicate";
	private static final String URI_PREDICATE_UNKNOWN = "http://prbi.test/unknownPredicate";

	private static final String URI_DOMAIN_1 = "http://prbi.test/domain1";
	private static final String URI_RANGE_1 = "http://prbi.test/range1";
	private static final String URI_DOMAIN_2 = "http://prbi.test/domain2";
	private static final String URI_RANGE_2 = "http://prbi.test/range2";

	private PropertyRestrictionBeanImpl bean;

	private ObjectProperty barePredicate;
	private ObjectProperty prohibitedPredicate;
	private ObjectProperty exceptionalPredicate;
	private ObjectProperty unknownPredicate;

	private FauxProperty emptyFaux;
	private FauxProperty restrictedFaux;
	private FauxProperty unknownFaux;

	@Before
	public void setup() {
		barePredicate = createObjectProperty(null, URI_PREDICATE_BARE, null,
				EDITOR, CURATOR, DB_ADMIN);
		unknownPredicate = createObjectProperty(null, URI_PREDICATE_UNKNOWN,
				null, null, null, null);
		prohibitedPredicate = createObjectProperty(null,
				URI_PREDICATE_PROHIBITED, null, null, null, null);
		exceptionalPredicate = createObjectProperty(null,
				URI_PREDICATE_EXCEPTIONAL, null, CURATOR, EDITOR, EDITOR);

		emptyFaux = createFauxProperty(URI_DOMAIN_1, URI_PREDICATE_BARE,
				URI_RANGE_1, null, null, null);
		restrictedFaux = createFauxProperty(URI_DOMAIN_2, URI_PREDICATE_BARE,
				URI_RANGE_2, EDITOR, DB_ADMIN, CURATOR);
		unknownFaux = createFauxProperty(URI_DOMAIN_1, URI_PREDICATE_UNKNOWN,
				URI_RANGE_1, NOBODY, NOBODY, NOBODY);

		ObjectPropertyDaoStub opDao = new ObjectPropertyDaoStub();
		opDao.addObjectProperty(barePredicate);
		opDao.addObjectProperty(prohibitedPredicate);
		opDao.addObjectProperty(exceptionalPredicate);

		DataPropertyDaoStub dpDao = new DataPropertyDaoStub();

		FauxPropertyDaoStub fpDao = new FauxPropertyDaoStub();
		fpDao.insertFauxProperty(emptyFaux);
		fpDao.insertFauxProperty(restrictedFaux);

		WebappDaoFactoryStub wadf = new WebappDaoFactoryStub();
		wadf.setObjectPropertyDao(opDao);
		wadf.setDataPropertyDao(dpDao);
		wadf.setFauxPropertyDao(fpDao);

		ContextModelAccessStub models = new ContextModelAccessStub();
		models.setWebappDaoFactory(wadf);

		bean = new PropertyRestrictionBeanImpl(PROHIBITED_NAMESPACES,
				PERMITTED_EXCEPTIONS, models);
	}

	private ObjectProperty createObjectProperty(String domainUri, String uri,
			String rangeUri, RoleLevel displayThreshold,
			RoleLevel modifyThreshold, RoleLevel publishThreshold) {
		ObjectProperty op = new ObjectProperty();
		op.setURI(uri);
		op.setDomainVClassURI(domainUri);
		op.setRangeVClassURI(rangeUri);
		op.setHiddenFromDisplayBelowRoleLevel(displayThreshold);
		op.setProhibitedFromUpdateBelowRoleLevel(modifyThreshold);
		op.setHiddenFromPublishBelowRoleLevel(publishThreshold);
		return op;
	}

	private FauxProperty createFauxProperty(String domainUri, String uri,
			String rangeUri, RoleLevel displayThreshold,
			RoleLevel modifyThreshold, RoleLevel publishThreshold) {
		FauxProperty fp = new FauxProperty(domainUri, uri, rangeUri);
		fp.setHiddenFromDisplayBelowRoleLevel(displayThreshold);
		fp.setProhibitedFromUpdateBelowRoleLevel(modifyThreshold);
		fp.setHiddenFromPublishBelowRoleLevel(publishThreshold);
		return fp;
	}

	// ----------------------------------------------------------------------
	// Resources
	// ----------------------------------------------------------------------

	@Test
	public void nullResource_display_false() {
		assertFalse(bean.canDisplayResource(null, highestRole()));
	}

	@Test
	public void anyResource_display_true() {
		assertTrue(bean.canDisplayResource(URI_RESOURCE_ANY, lowestRole()));
	}

	@Test
	public void anyResource_nullUserRole_display_false() {
		assertFalse(bean.canDisplayResource(URI_RESOURCE_ANY, null));
	}

	@Test
	public void nullResource_modify_false() {
		assertFalse(bean.canModifyResource(null, highestRole()));
	}

	@Test
	public void prohibitedResource_modify_false() {
		assertFalse(bean.canModifyResource(URI_RESOURCE_PROHIBITED,
				highestRole()));
	}

	@Test
	public void exceptionalResource_modify_true() {
		assertTrue(bean.canModifyResource(URI_RESOURCE_EXCEPTIONAL,
				lowestRole()));
	}

	@Test
	public void unremarkableResource_modify_true() {
		assertTrue(bean.canModifyResource(URI_RESOURCE_ANY, lowestRole()));
	}

	@Test
	public void unremarkableResource_nullUserRole_modify_false() {
		assertFalse(bean.canModifyResource(URI_RESOURCE_ANY, null));
	}

	@Test
	public void nullResource_publish_false() {
		assertFalse(bean.canPublishResource(null, highestRole()));
	}

	@Test
	public void anyResource_publish_true() {
		assertTrue(bean.canPublishResource(URI_RESOURCE_ANY, lowestRole()));
	}

	@Test
	public void anyResource_nullUserRole_publish_false() {
		assertFalse(bean.canPublishResource(URI_RESOURCE_ANY, null));
	}

	// ----------------------------------------------------------------------
	// Predicates
	// ----------------------------------------------------------------------

	// ----- display

	@Test
	public void nullPredicate_display_false() {
		assertFalse(bean.canDisplayPredicate(null, highestRole()));
	}

	@Test
	public void barePredicate_display_byRole() {
		assertTrue(bean.canDisplayPredicate(barePredicate,
				higherRole(barePredicate, DISPLAY)));
		assertTrue(bean.canDisplayPredicate(barePredicate,
				sameRole(barePredicate, DISPLAY)));
		assertFalse(bean.canDisplayPredicate(barePredicate,
				lowerRole(barePredicate, DISPLAY)));
	}

	@Test
	public void unknownBarePredicate_display_true() {
		assertTrue(bean.canDisplayPredicate(unknownPredicate, lowestRole()));
	}

	@Test
	public void emptyQualifiedPredicate_display_byBareRole() {
		assertTrue(bean.canDisplayPredicate(asProperty(emptyFaux),
				higherRole(barePredicate, DISPLAY)));
		assertTrue(bean.canDisplayPredicate(asProperty(emptyFaux),
				sameRole(barePredicate, DISPLAY)));
		assertFalse(bean.canDisplayPredicate(asProperty(emptyFaux),
				lowerRole(barePredicate, DISPLAY)));
	}

	@Test
	public void restrictedQualifiedPredicate_display_byRole() {
		assertTrue(bean.canDisplayPredicate(asProperty(restrictedFaux),
				higherRole(restrictedFaux, DISPLAY)));
		assertTrue(bean.canDisplayPredicate(asProperty(restrictedFaux),
				sameRole(restrictedFaux, DISPLAY)));
		assertFalse(bean.canDisplayPredicate(asProperty(restrictedFaux),
				lowerRole(restrictedFaux, DISPLAY)));
	}

	@Test
	public void unknownQualifiedPredicate_display_true() {
		assertTrue(bean.canDisplayPredicate(asProperty(unknownFaux),
				lowestRole()));
	}

	// ----- modify

	@Test
	public void nullPredicate_modify_false() {
		assertFalse(bean.canModifyPredicate(null, highestRole()));
	}

	@Test
	public void prohibitedPredicate_modify_false() {
		assertFalse(bean.canModifyPredicate(prohibitedPredicate, lowestRole()));
	}

	@Test
	public void exceptionalPredicate_modify_byRole() {
		assertTrue(bean.canModifyPredicate(exceptionalPredicate,
				higherRole(exceptionalPredicate, MODIFY)));
		assertTrue(bean.canModifyPredicate(exceptionalPredicate,
				sameRole(exceptionalPredicate, MODIFY)));
		assertFalse(bean.canModifyPredicate(exceptionalPredicate,
				lowerRole(exceptionalPredicate, MODIFY)));
	}

	@Test
	public void barePredicate_modify_byRole() {
		assertTrue(bean.canModifyPredicate(barePredicate,
				higherRole(barePredicate, MODIFY)));
		assertTrue(bean.canModifyPredicate(barePredicate,
				sameRole(barePredicate, MODIFY)));
		assertFalse(bean.canModifyPredicate(barePredicate,
				lowerRole(barePredicate, MODIFY)));
	}

	@Test
	public void unknownBarePredicate_modify_true() {
		assertTrue(bean.canModifyPredicate(unknownPredicate, lowestRole()));
	}

	@Test
	public void emptyQualifiedPredicate_modify_byBareRole() {
		assertTrue(bean.canModifyPredicate(asProperty(emptyFaux),
				higherRole(barePredicate, MODIFY)));
		assertTrue(bean.canModifyPredicate(asProperty(emptyFaux),
				sameRole(barePredicate, MODIFY)));
		assertFalse(bean.canModifyPredicate(asProperty(emptyFaux),
				lowerRole(barePredicate, MODIFY)));
	}

	@Test
	public void restrictedQualifiedPredicate_modify_byRole() {
		assertTrue(bean.canModifyPredicate(asProperty(restrictedFaux),
				higherRole(restrictedFaux, MODIFY)));
		assertTrue(bean.canModifyPredicate(asProperty(restrictedFaux),
				sameRole(restrictedFaux, MODIFY)));
		assertFalse(bean.canModifyPredicate(asProperty(restrictedFaux),
				lowerRole(restrictedFaux, MODIFY)));
	}

	@Test
	public void unknownQualifiedPredicate_modify_true() {
		assertTrue(bean.canModifyPredicate(asProperty(unknownFaux),
				lowestRole()));
	}

	// ----- publish

	@Test
	public void nullPredicate_publish_false() {
		assertFalse(bean.canPublishPredicate(null, highestRole()));
	}

	@Test
	public void barePredicate_publish_byRole() {
		assertTrue(bean.canPublishPredicate(barePredicate,
				higherRole(barePredicate, PUBLISH)));
		assertTrue(bean.canPublishPredicate(barePredicate,
				sameRole(barePredicate, PUBLISH)));
		assertFalse(bean.canPublishPredicate(barePredicate,
				lowerRole(barePredicate, PUBLISH)));
	}

	@Test
	public void unknownBarePredicate_publish_true() {
		assertTrue(bean.canPublishPredicate(unknownPredicate, lowestRole()));
	}

	@Test
	public void emptyQualifiedPredicate_publish_byBareRole() {
		assertTrue(bean.canPublishPredicate(asProperty(emptyFaux),
				higherRole(barePredicate, PUBLISH)));
		assertTrue(bean.canPublishPredicate(asProperty(emptyFaux),
				sameRole(barePredicate, PUBLISH)));
		assertFalse(bean.canPublishPredicate(asProperty(emptyFaux),
				lowerRole(barePredicate, PUBLISH)));
	}

	@Test
	public void restrictedQualifiedPredicate_publish_byRole() {
		assertTrue(bean.canPublishPredicate(asProperty(restrictedFaux),
				higherRole(restrictedFaux, PUBLISH)));
		assertTrue(bean.canPublishPredicate(asProperty(restrictedFaux),
				sameRole(restrictedFaux, PUBLISH)));
		assertFalse(bean.canPublishPredicate(asProperty(restrictedFaux),
				lowerRole(restrictedFaux, PUBLISH)));
	}

	@Test
	public void unknownQualifiedPredicate_publish_true() {
		assertTrue(bean.canPublishPredicate(asProperty(unknownFaux),
				lowestRole()));
	}

	// ----------------------------------------------------------------------
	// update permissions
	// ----------------------------------------------------------------------

	@Test
	public void updateToAllowDisplay() {
		RoleLevel desiredDisplayLevel = lowerRole(barePredicate, DISPLAY);

		assertFalse(bean
				.canDisplayPredicate(barePredicate, desiredDisplayLevel));

		bean.updateProperty(updatedLevels(barePredicate, desiredDisplayLevel,
				sameRole(barePredicate, MODIFY),
				sameRole(barePredicate, PUBLISH)));

		assertTrue(bean.canDisplayPredicate(barePredicate, desiredDisplayLevel));
	}

	@Test
	public void updateToAllowModify() {
		RoleLevel desiredModifyLevel = lowerRole(barePredicate, MODIFY);

		assertFalse(bean.canModifyPredicate(barePredicate, desiredModifyLevel));

		bean.updateProperty(updatedLevels(barePredicate,
				sameRole(barePredicate, DISPLAY), desiredModifyLevel,
				sameRole(barePredicate, PUBLISH)));

		assertTrue(bean.canModifyPredicate(barePredicate, desiredModifyLevel));
	}

	@Test
	public void updateToAllowPublish() {
		RoleLevel desiredPublishLevel = lowerRole(barePredicate, PUBLISH);

		assertFalse(bean
				.canPublishPredicate(barePredicate, desiredPublishLevel));

		bean.updateProperty(updatedLevels(barePredicate,
				sameRole(barePredicate, DISPLAY),
				sameRole(barePredicate, MODIFY), desiredPublishLevel));

		assertTrue(bean.canPublishPredicate(barePredicate, desiredPublishLevel));
	}

	@Test
	public void updateToDisallowDisplay() {
		RoleLevel desiredDisplayLevel = sameRole(barePredicate, DISPLAY);

		assertTrue(bean.canDisplayPredicate(barePredicate, desiredDisplayLevel));

		bean.updateProperty(updatedLevels(barePredicate,
				higherRole(barePredicate, DISPLAY),
				sameRole(barePredicate, MODIFY),
				sameRole(barePredicate, PUBLISH)));

		assertFalse(bean
				.canDisplayPredicate(barePredicate, desiredDisplayLevel));
	}

	@Test
	public void updateToDisallowModify() {
		RoleLevel desiredModifyLevel = sameRole(barePredicate, MODIFY);

		assertTrue(bean.canModifyPredicate(barePredicate, desiredModifyLevel));

		bean.updateProperty(updatedLevels(barePredicate,
				sameRole(barePredicate, DISPLAY),
				higherRole(barePredicate, MODIFY),
				sameRole(barePredicate, PUBLISH)));

		assertFalse(bean.canModifyPredicate(barePredicate, desiredModifyLevel));
	}

	@Test
	public void updateToDisallowPublish() {
		RoleLevel desiredPublishLevel = sameRole(barePredicate, PUBLISH);

		assertTrue(bean.canPublishPredicate(barePredicate, desiredPublishLevel));

		bean.updateProperty(updatedLevels(barePredicate,
				sameRole(barePredicate, DISPLAY),
				sameRole(barePredicate, MODIFY),
				higherRole(barePredicate, PUBLISH)));

		assertFalse(bean
				.canPublishPredicate(barePredicate, desiredPublishLevel));
	}

	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

	private RoleLevel lowestRole() {
		return RoleLevel.values()[0];
	}

	private RoleLevel highestRole() {
		RoleLevel[] values = RoleLevel.values();
		return values[values.length - 1];
	}

	private RoleLevel sameRole(RoleRestrictedProperty p, Which which) {
		switch (which) {
		case DISPLAY:
			return p.getHiddenFromDisplayBelowRoleLevel();
		case MODIFY:
			return p.getProhibitedFromUpdateBelowRoleLevel();
		default: // PUBLISH
			return p.getHiddenFromPublishBelowRoleLevel();
		}
	}

	private RoleLevel lowerRole(RoleRestrictedProperty p, Which which) {
		return RoleLevel.values()[sameRole(p, which).ordinal() - 1];
	}

	private RoleLevel higherRole(RoleRestrictedProperty p, Which which) {
		return RoleLevel.values()[sameRole(p, which).ordinal() + 1];
	}

	private Property asProperty(FauxProperty fp) {
		Property p = new Property();
		p.setURI(fp.getBaseURI());
		p.setDomainVClassURI(fp.getDomainURI());
		p.setRangeVClassURI(fp.getRangeURI());
		return p;
	}

	private PropertyRestrictionLevels updatedLevels(RoleRestrictedProperty p,
			RoleLevel displayLevel, RoleLevel modifyLevel,
			RoleLevel publishLevel) {
		return new PropertyRestrictionLevels(new FullPropertyKey(p),
				displayLevel, modifyLevel, publishLevel);
	}

}
