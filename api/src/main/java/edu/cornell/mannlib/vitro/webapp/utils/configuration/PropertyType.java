/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.configuration;

import static org.apache.jena.datatypes.xsd.XSDDatatype.XSDfloat;
import static org.apache.jena.datatypes.xsd.XSDDatatype.XSDstring;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.impl.RDFLangString;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;

/**
 * An enumeration of the types of properties that the ConfigurationBeanLoader
 * will support.
 * 
 * Also, classes that represent the Java methods and RDF statements associated
 * with those types.
 */
public enum PropertyType {
	RESOURCE {
		@Override
		public PropertyStatement buildPropertyStatement(Statement s) {
			return new ResourcePropertyStatement(s.getPredicate().getURI(), s
					.getObject().asResource().getURI());
		}

		@Override
		protected PropertyMethod buildPropertyMethod(Method method,
				Property annotation) {
			return new ResourcePropertyMethod(method, annotation);
		}

	},
	STRING {
		@Override
		public PropertyStatement buildPropertyStatement(Statement s) {
			return new StringPropertyStatement(s.getPredicate().getURI(), s
					.getObject().asLiteral().getString());
		}

		@Override
		protected PropertyMethod buildPropertyMethod(Method method,
				Property annotation) {
			return new StringPropertyMethod(method, annotation);
		}
	},
	FLOAT {
		@Override
		public PropertyStatement buildPropertyStatement(Statement s) {
			return new FloatPropertyStatement(s.getPredicate().getURI(), s
					.getObject().asLiteral().getFloat());
		}

		@Override
		protected PropertyMethod buildPropertyMethod(Method method,
				Property annotation) {
			return new FloatPropertyMethod(method, annotation);
		}
	};

	public static PropertyType typeForObject(RDFNode object)
			throws PropertyTypeException {
		if (object.isURIResource()) {
			return RESOURCE;
		}
		if (object.isLiteral()) {
			Literal literal = object.asLiteral();
			RDFDatatype datatype = literal.getDatatype();
			if (datatype == null || datatype.equals(XSDstring) || datatype.equals(RDFLangString.rdfLangString)) {
				return STRING;
			}
			if (datatype.equals(XSDfloat)) {
				return FLOAT;
			}
		}
		throw new PropertyTypeException("Unsupported datatype on object: "
				+ object);
	}

	public static PropertyType typeForParameterType(Class<?> parameterType)
			throws PropertyTypeException {
		if (Float.TYPE.equals(parameterType)) {
			return FLOAT;
		}
		if (String.class.equals(parameterType)) {
			return STRING;
		}
		if (!parameterType.isPrimitive()) {
			return RESOURCE;
		}
		throw new PropertyTypeException(
				"Unsupported parameter type on method: " + parameterType);
	}

	public static PropertyStatement createPropertyStatement(Statement s)
			throws PropertyTypeException {
		PropertyType type = PropertyType.typeForObject(s.getObject());
		return type.buildPropertyStatement(s);
	}

	public static PropertyMethod createPropertyMethod(Method method,
			Property annotation) throws PropertyTypeException {
		Class<?> parameterType = method.getParameterTypes()[0];
		PropertyType type = PropertyType.typeForParameterType(parameterType);
		return type.buildPropertyMethod(method, annotation);
	}

	protected abstract PropertyStatement buildPropertyStatement(Statement s);

	protected abstract PropertyMethod buildPropertyMethod(Method method,
			Property annotation);

	public static abstract class PropertyStatement {
		private final PropertyType type;
		private final String predicateUri;

		public PropertyStatement(PropertyType type, String predicateUri) {
			this.type = type;
			this.predicateUri = predicateUri;
		}

		public PropertyType getType() {
			return type;
		}

		public String getPredicateUri() {
			return predicateUri;
		}

		public abstract Object getValue();
	}

	public static class ResourcePropertyStatement extends PropertyStatement {
		private final String objectUri;

		public ResourcePropertyStatement(String predicateUri, String objectUri) {
			super(RESOURCE, predicateUri);
			this.objectUri = objectUri;
		}

		@Override
		public String getValue() {
			return objectUri;
		}
	}

	public static class StringPropertyStatement extends PropertyStatement {
		private final String string;

		public StringPropertyStatement(String predicateUri, String string) {
			super(STRING, predicateUri);
			this.string = string;
		}

		@Override
		public String getValue() {
			return string;
		}
	}

	public static class FloatPropertyStatement extends PropertyStatement {
		private final float f;

		public FloatPropertyStatement(String predicateUri, float f) {
			super(FLOAT, predicateUri);
			this.f = f;
		}

		@Override
		public Float getValue() {
			return f;
		}
	}

	public static abstract class PropertyMethod {
		protected final PropertyType type;
		protected final Method method;
		protected final String propertyUri;
		protected final int minOccurs;
		protected final int maxOccurs;

		// Add cardinality values here! Final, with getters.
		public PropertyMethod(PropertyType type, Method method,
				Property annotation) {
			this.type = type;
			this.method = method;
			this.propertyUri = annotation.uri();
			this.minOccurs = annotation.minOccurs();
			this.maxOccurs = annotation.maxOccurs();
			checkCardinalityBounds();
		}

		private void checkCardinalityBounds() {
			// This is where we check for negative values or out of order.
		}

		public Method getMethod() {
			return method;
		}

		public Class<?> getParameterType() {
			return method.getParameterTypes()[0];
		}

		public String getPropertyUri() {
			return propertyUri;
		}

		public int getMinOccurs() {
			return minOccurs;
		}

		public int getMaxOccurs() {
			return maxOccurs;
		}

		public void confirmCompatible(PropertyStatement ps)
				throws PropertyTypeException {
			if (type != ps.getType()) {
				throw new PropertyTypeException(
						"Can't apply statement of type " + ps.getType()
								+ " to a method of type " + type);
			}
		}

		public void invoke(Object instance, Object value)
				throws PropertyTypeException {
			try {
				method.invoke(instance, value);
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				throw new PropertyTypeException("Property method failed.", e);
			}
		}

	}

	public static class ResourcePropertyMethod extends PropertyMethod {
		public ResourcePropertyMethod(Method method, Property annotation) {
			super(RESOURCE, method, annotation);
		}
	}

	public static class StringPropertyMethod extends PropertyMethod {
		public StringPropertyMethod(Method method, Property annotation) {
			super(STRING, method, annotation);
		}
	}

	public static class FloatPropertyMethod extends PropertyMethod {
		public FloatPropertyMethod(Method method, Property annotation) {
			super(FLOAT, method, annotation);
		}
	}

	public static class PropertyTypeException extends Exception {
		public PropertyTypeException(String message) {
			super(message);
		}

		public PropertyTypeException(String message, Throwable cause) {
			super(message, cause);
		}

	}

}
