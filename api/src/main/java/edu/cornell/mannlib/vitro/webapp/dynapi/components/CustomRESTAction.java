package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class CustomRESTAction implements Removable {

    private String name;
    private String targetProcedureUri;
    private String httpMethodName = "PUT";

    @Override
    public void dereference() {
        // TODO Auto-generated method stub
    }

    public String getName() {
        return name;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#name", minOccurs = 1, maxOccurs = 1)
    public void setName(String name) {
        this.name = name;
    }
    
    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#hasDefaultMethod")
    public void setHttpMethod(HTTPMethod httpMethod) {
        this.httpMethodName = httpMethod.getName();
    }

    public String getTargetProcedureUri() {
        return targetProcedureUri;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#forwardsTo", minOccurs = 1, maxOccurs = 1, asString = true)
    public void setTargetProcedureUri(String procedureUri) {
        this.targetProcedureUri = procedureUri;
    }
    
    public String getHttpMethodName() {
        return httpMethodName;
    }

}
