/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package stubs.edu.cornell.mannlib.vitro.webapp.auth.policy.bean;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.rdf.model.impl.Util;

import edu.cornell.mannlib.vitro.webapp.auth.policy.bean.PropertyRestrictionBean;
import edu.cornell.mannlib.vitro.webapp.auth.policy.bean.PropertyRestrictionLevels;
import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel;
import edu.cornell.mannlib.vitro.webapp.beans.Property;

/**
 * Allow the unit test to specify a variety of restrictions
 */
public class PropertyRestrictionBeanStub extends PropertyRestrictionBean {

	// ----------------------------------------------------------------------
	// Stub infrastructure
	// ----------------------------------------------------------------------

	/** Don't prohibit or restrict anything. */
	public static PropertyRestrictionBean getInstance() {
		return getInstance(null, null);
	}

	/** Prohibit some namespaces. */
	public static PropertyRestrictionBeanStub getInstance(
			String[] restrictedNamespaces) {
		return getInstance(restrictedNamespaces, null);
	}

	/**
	 * Prohibit some namespaces and restrict some properties from modification
	 * by anybody. They may still be displayed or published.
	 * 
	 * We can implement more granular control if we need it.
	 */
	public static PropertyRestrictionBeanStub getInstance(
			String[] restrictedNamespaces, String[] restrictedProperties) {
		PropertyRestrictionBeanStub stub = new PropertyRestrictionBeanStub(
				restrictedNamespaces, restrictedProperties);
		PropertyRestrictionBean.instance = stub;
		return stub;
	}

	private final Set<String> restrictedNamespaces;
	private final Set<String> restrictedProperties;

	private PropertyRestrictionBeanStub(String[] restrictedNamespaces,
			String[] restrictedProperties) {
		this.restrictedNamespaces = (restrictedNamespaces == null) ? Collections
				.<String> emptySet() : new HashSet<>(
				Arrays.asList(restrictedNamespaces));
		this.restrictedProperties = (restrictedProperties == null) ? Collections
				.<String> emptySet() : new HashSet<>(
				Arrays.asList(restrictedProperties));
	}

	private boolean isPermittedNamespace(String uri) {
		return !restrictedNamespaces.contains(namespace(uri));
	}

	private String namespace(String uri) {
		return uri.substring(0, Util.splitNamespaceXML(uri));
	}

	private boolean isPermittedProperty(String uri) {
		return !restrictedProperties.contains(uri);
	}

	// ----------------------------------------------------------------------
	// Stub methods
	// ----------------------------------------------------------------------

	@Override
	public boolean canDisplayResource(String resourceUri, RoleLevel userRole) {
		return isPermittedNamespace(resourceUri);
	}

	@Override
	public boolean canModifyResource(String resourceUri, RoleLevel userRole) {
		return isPermittedNamespace(resourceUri)
				&& isPermittedProperty(resourceUri);
	}

	@Override
	public boolean canPublishResource(String resourceUri, RoleLevel userRole) {
		return isPermittedNamespace(resourceUri);
	}

	@Override
	public boolean canDisplayPredicate(Property predicate, RoleLevel userRole) {
		return isPermittedNamespace(predicate.getURI());
	}

	@Override
	public boolean canModifyPredicate(Property predicate, RoleLevel userRole) {
		return isPermittedNamespace(predicate.getURI())
				&& isPermittedProperty(predicate.getURI());
	}

	@Override
	public boolean canPublishPredicate(Property predicate, RoleLevel userRole) {
		return isPermittedNamespace(predicate.getURI());
	}

	// ----------------------------------------------------------------------
	// Un-implemented methods
	// ----------------------------------------------------------------------

	@Override
	public void updateProperty(PropertyRestrictionLevels levels) {
		throw new RuntimeException(
				"PropertyRestrictionBeanStub.updateProperty() not implemented.");
	}

}
