package edu.cornell.mannlib.vitro.webapp.dynapi.data.types;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Removable;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.serialization.SerializationType;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.ConversionMethod;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.JsonContainer;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class ParameterType implements Removable {

	private String name;
	private SerializationType serializationType;
	private RDFType rdftype;
	private ImplementationType implementationType;
	protected ParameterType valuesType = this;
	private boolean isInternal = false;

	public String getName() {
		return name;
	}
	
	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#name", minOccurs = 1, maxOccurs = 1)
	public void setName(String name) {
		this.name = name;
	}

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#serializationType", minOccurs = 1, maxOccurs = 1)
	public void setSerializationType(SerializationType serializationType) {
		this.serializationType = serializationType;
	}

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#rdfType", minOccurs = 0, maxOccurs = 1)
	public void setRdfType(RDFType rdftype) {
		this.rdftype = rdftype;
	}

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#isInternal", maxOccurs = 1)
	public void setIsInternal(boolean isInternal) {
		this.isInternal = isInternal;
	}

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#implementationType", minOccurs = 1, maxOccurs = 1)
	public void setImplementationType(ImplementationType implementationType) {
		this.implementationType = implementationType;
	}

	public ImplementationType getImplementationType() {
		return implementationType;
	}

	public ParameterType getValuesType() {
		return valuesType;
	}

	public boolean isLiteral() {
		if (!isRdfType()) {
			return false;
		}
		return rdftype.isLiteral();
	}

	public boolean isUri() {
		if (!isRdfType()) {
			return false;
		}
		return rdftype.isUri();
	}

	public boolean isRdfType() {
		if (rdftype != null) {
			return true;
		}
		return false;
	}

	public RDFType getRdfType() {
		return rdftype;
	}

	public SerializationType getSerializationType() {
		return serializationType;
	}

	@Override
	public void dereference() {
	}

	public boolean isArray() {
		return getImplementationType().getClassName().getCanonicalName().equals("java.util.ArrayList");
	}
	
    public boolean isString() {
        return getImplementationType().getClassName().getCanonicalName().equals(String.class.getCanonicalName());
    }
    
    public boolean isPlainString() {
        return isString() && !isRdfType();
    }

	public boolean isInternal() {
		return isInternal;
	}

	public boolean isJsonContainer() {
		final String canonicalName = getImplementationType().getClassName().getCanonicalName();
		if (JsonContainer.class.getCanonicalName().equals(canonicalName)){
			return true;
		}
		return false;
	}

	public void initialize() throws InitializationException {
		if (implementationType == null) {
			throw new InitializationException("implementation type is null");
		}
		if (implementationType.getDeserializationConfig() != null) {
			implementationType.getDeserializationConfig().setConversionMethod(new ConversionMethod(this, false));
		}
		if (implementationType.getSerializationConfig() != null) {
			implementationType.getSerializationConfig().setConversionMethod(new ConversionMethod(this, true));
		}

	}
}
