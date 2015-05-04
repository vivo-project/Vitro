/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.configuration;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.utils.configuration.PropertyType.PropertyStatement;

public class ConfigurationRdf<T> {
	private final Class<? extends T> concreteClass;
	private final Set<PropertyStatement> properties;

	public ConfigurationRdf(Class<? extends T> concreteClass,
			Set<PropertyStatement> properties) {
		this.concreteClass = concreteClass;
		this.properties = Collections
				.unmodifiableSet(new HashSet<>(properties));
	}

	public Class<? extends T> getConcreteClass() {
		return concreteClass;
	}

	public Set<PropertyStatement> getPropertyStatements() {
		return new HashSet<>(properties);
	}
}