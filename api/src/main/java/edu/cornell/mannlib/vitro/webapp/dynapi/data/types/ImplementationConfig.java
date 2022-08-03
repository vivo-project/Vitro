package edu.cornell.mannlib.vitro.webapp.dynapi.data.types;

import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class ImplementationConfig {

	private Class<?> className;
	private String methodName;
	private String methodArguments;
	private boolean isStatic = false;

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#className", minOccurs = 1, maxOccurs = 1)
	public void setClassName(String className) throws ClassNotFoundException {
		this.className = Class.forName(className);
	}
	
	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#methodName", minOccurs = 1, maxOccurs = 1)
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	
	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#methodArguments", minOccurs = 1, maxOccurs = 1)
	public void setMethodArguments(String methodArguments) {
		this.methodArguments = methodArguments;
	}
	
	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#isStaticMethod", minOccurs = 0, maxOccurs = 1)
	public void setStaticMethod(boolean isStatic) {
		this.isStatic = isStatic;
	}
}
