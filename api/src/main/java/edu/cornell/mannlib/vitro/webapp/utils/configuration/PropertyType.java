/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.utils.configuration;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.impl.RDFLangString;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;

import static org.apache.jena.datatypes.xsd.XSDDatatype.*;

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
		protected PropertyMethod buildPropertyMethod(Method method, Property annotation) {
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
		protected PropertyMethod buildPropertyMethod(Method method, Property annotation) {
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
		protected PropertyMethod buildPropertyMethod(Method method, Property annotation) {
			return new FloatPropertyMethod(method, annotation);
		}
	},
	INTEGER {
		@Override
		public PropertyStatement buildPropertyStatement(Statement s) {
			return new IntegerPropertyStatement(s.getPredicate().getURI(), s
					.getObject().asLiteral().getInt());
		}

		@Override
		protected PropertyMethod buildPropertyMethod(Method method, Property annotation) {
			return new IntegerPropertyMethod(method, annotation);
		}
	},
	BOOLEAN{
		@Override
		public PropertyStatement buildPropertyStatement(Statement s) {
			return new BooleanPropertyStatement(s.getPredicate().getURI(), s
					.getObject().asLiteral().getBoolean());
		}

		@Override
		protected PropertyMethod buildPropertyMethod(Method method, Property annotation) {
			return new BooleanPropertyMethod(method, annotation);
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
			if (datatype == null || 
					datatype.equals(XSDstring) || 
					datatype.equals(RDFLangString.rdfLangString) ||
					//TODO: Until more suitable type defined 
					datatype.equals(XSDdateTime)) {
				return STRING;
			}
			if (datatype.equals(XSDfloat) ||
					datatype.equals(XSDdecimal)){
				return FLOAT;
			}
			if (datatype.equals(XSDint) ||
					datatype.equals(XSDinteger)) {
				return INTEGER;
			}
			if (datatype.equals(XSDboolean)) {
				return BOOLEAN;
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
		if (Integer.TYPE.equals(parameterType)) {
			return INTEGER;
		}
		if (Boolean.TYPE.equals(parameterType)) {
			return BOOLEAN;
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

	public static PropertyMethod createPropertyMethod(Method method, Property annotation) throws PropertyTypeException {
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

	public static class IntegerPropertyStatement extends PropertyStatement {
		private final int i;

		public IntegerPropertyStatement(String predicateUri, int i) {
			super(INTEGER, predicateUri);
			this.i = i;
		}

		@Override
		public Integer getValue() {
			return i;
		}
	}
	
	public static class BooleanPropertyStatement extends PropertyStatement {
		private final Boolean bool;

		public BooleanPropertyStatement(String predicateUri, Boolean b) {
			super(BOOLEAN, predicateUri);
			this.bool = b;
		}

		@Override
		public Boolean getValue() {
			return bool;
		}
	}

	public static abstract class PropertyMethod {
		protected final PropertyType type;
		protected final Method method;
		protected final String propertyUri;
		protected final boolean asString;
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
			this.asString = annotation.asString();
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
		
        public boolean getAsString() {
            return asString;
        }

        public void confirmCompatible(PropertyStatement ps) throws PropertyTypeException {
            final PropertyType psType = ps.getType();
            if (asString && psType.equals(PropertyType.RESOURCE) && type.equals(PropertyType.STRING)) {
                return;
            }
            if (type != psType && !(isSubtype(psType, type))) {
                throw new PropertyTypeException(
                        "Can't apply statement of type " + psType + " to a method of type " + type);
            }
        }

		private boolean isSubtype(PropertyType subType, PropertyType superType){
			if (subType.equals(INTEGER) && superType.equals(FLOAT))
				return true;
			return false;
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

	public static class IntegerPropertyMethod extends PropertyMethod {
		public IntegerPropertyMethod(Method method, Property annotation) {
			super(INTEGER, method, annotation);
		}
	}
	
	public static class BooleanPropertyMethod extends PropertyMethod {
		public BooleanPropertyMethod(Method method, Property annotation) {
			super(BOOLEAN, method, annotation);
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
