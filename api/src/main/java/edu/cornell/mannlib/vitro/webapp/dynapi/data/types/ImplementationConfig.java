/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.data.types;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.ConversionMethod;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ImplementationConfig {
    private Class<?> classObject;
    private String methodName;
    private String methodArguments;
    private ConversionMethod conversionMethod = null;
    private boolean staticMethod = false;

    public Class<?> getClassObject() {
        return classObject;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#className", minOccurs = 1, maxOccurs = 1)
    public void setClassName(String className) throws ClassNotFoundException {
        this.classObject = Class.forName(className);
    }

    public String getMethodName() {
        return methodName;
    }

    // Empty method name is a constructor
    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#methodName", minOccurs = 0, maxOccurs = 1)
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodArguments() {
        return methodArguments;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#methodArguments", minOccurs = 1, maxOccurs = 1)
    public void setMethodArguments(String methodArguments) {
        this.methodArguments = methodArguments;
    }

    public boolean isStatic() {
        return staticMethod;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#isStaticMethod", minOccurs = 0, maxOccurs = 1)
    public void setStaticMethod(boolean isStaticMethod) {
        this.staticMethod = isStaticMethod;
    }

    public boolean isMethodInitialized() {
        if (conversionMethod == null) {
            return false;
        }
        return true;
    }

    public ConversionMethod getConversionMethod() {
        return conversionMethod;
    }

    public void setConversionMethod(ConversionMethod conversionMethod) {
        this.conversionMethod = conversionMethod;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ImplementationConfig)) {
            return false;
        }
        if (object == this) {
            return true;
        }
        ImplementationConfig compared = (ImplementationConfig) object;

        return new EqualsBuilder()
                .append(classObject, compared.classObject)
                .append(methodName, compared.methodName)
                .append(methodArguments, compared.methodArguments)
                .append(conversionMethod, compared.conversionMethod)
                .append(staticMethod, compared.staticMethod)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(59, 103)
                .append(classObject)
                .append(methodName)
                .append(methodArguments)
                .append(conversionMethod)
                .append(staticMethod)
                .toHashCode();
    }
}
