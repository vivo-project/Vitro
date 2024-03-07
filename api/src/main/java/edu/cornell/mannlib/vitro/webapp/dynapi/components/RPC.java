package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;
import org.apache.commons.lang3.StringUtils;

public class RPC extends AbstractPoolComponent implements Removable, Poolable<String> {

    private String name;
    private String minVersion;
    private String maxVersion;
    private String procedureUri;
    private HTTPMethod httpMethod;

    public String getProcedureUri() {
        return procedureUri;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#procedure", minOccurs = 1, maxOccurs = 1,
            asString = true)
    public void setProcedureUri(String procedureUri) {
        this.procedureUri = procedureUri;
    }

    @Override
    public void dereference() {
    }

    public String getName() {
        return name;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#name", minOccurs = 1, maxOccurs = 1)
    public void setName(String name) {
        this.name = name;
    }

    public String getMinVersion() {
        return minVersion;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#minAPIVersion", maxOccurs = 1)
    public void setMinVersion(String minVersion) {
        this.minVersion = minVersion;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#maxAPIVersion", maxOccurs = 1)
    public void setMaxVersion(String maxVersion) {
        this.maxVersion = maxVersion;
    }

    public String getMaxVersion() {
        return maxVersion;
    }

    @Override
    public String getKey() {
        return name;
    }

    @Override
    public boolean isValid() {
        if (StringUtils.isBlank(procedureUri)) {
            return false;
        }
        return true;
    }

    public HTTPMethod getHttpMethod() {
        return httpMethod;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#hasDefaultMethod")
    public void setHttpMethod(HTTPMethod httpMethod) {
        this.httpMethod = httpMethod;
    }

}
