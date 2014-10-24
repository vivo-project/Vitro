/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.configuration;

import static com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDfloat;
import static com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDstring;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;

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
			return new ResourcePropertyStatement(s.getPredicate(), s
					.getObject().asResource().getURI());
		}

		@Override
		protected PropertyMethod buildPropertyMethod(Method method) {
			return new ResourcePropertyMethod(method);
		}

	},
	STRING {
		@Override
		public PropertyStatement buildPropertyStatement(Statement s) {
			return new StringPropertyStatement(s.getPredicate(), s.getObject()
					.asLiteral().getString());
		}

		@Override
		protected PropertyMethod buildPropertyMethod(Method method) {
			return new StringPropertyMethod(method);
		}
	},
	FLOAT {
		@Override
		public PropertyStatement buildPropertyStatement(Statement s) {
			return new FloatPropertyStatement(s.getPredicate(), s.getObject()
					.asLiteral().getFloat());
		}

		@Override
		protected PropertyMethod buildPropertyMethod(Method method) {
			return new FloatPropertyMethod(method);
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
			if (datatype == null || datatype.equals(XSDstring)) {
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

	public static PropertyMethod createPropertyMethod(Method method)
			throws PropertyTypeException {
		Class<?> parameterType = method.getParameterTypes()[0];
		PropertyType type = PropertyType.typeForParameterType(parameterType);
		return type.buildPropertyMethod(method);
	}

	protected abstract PropertyStatement buildPropertyStatement(Statement s);

	protected abstract PropertyMethod buildPropertyMethod(Method method);

	public static abstract class PropertyStatement {
		private final PropertyType type;
		private final String predicateUri;

		public PropertyStatement(PropertyType type, Property predicate) {
			this.type = type;
			this.predicateUri = predicate.getURI();
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

		public ResourcePropertyStatement(Property predicate, String objectUri) {
			super(RESOURCE, predicate);
			this.objectUri = objectUri;
		}

		@Override
		public String getValue() {
			return objectUri;
		}
	}

	public static class StringPropertyStatement extends PropertyStatement {
		private final String string;

		public StringPropertyStatement(Property predicate, String string) {
			super(STRING, predicate);
			this.string = string;
		}

		@Override
		public String getValue() {
			return string;
		}
	}

	public static class FloatPropertyStatement extends PropertyStatement {
		private final float f;

		public FloatPropertyStatement(Property predicate, float f) {
			super(FLOAT, predicate);
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

		public PropertyMethod(PropertyType type, Method method) {
			this.type = type;
			this.method = method;
		}

		public Method getMethod() {
			return method;
		}

		public Class<?> getParameterType() {
			return method.getParameterTypes()[0];
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
		public ResourcePropertyMethod(Method method) {
			super(RESOURCE, method);
		}
	}

	public static class StringPropertyMethod extends PropertyMethod {
		public StringPropertyMethod(Method method) {
			super(STRING, method);
		}
	}

	public static class FloatPropertyMethod extends PropertyMethod {
		public FloatPropertyMethod(Method method) {
			super(FLOAT, method);
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
