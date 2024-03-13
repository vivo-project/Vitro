/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import java.util.LinkedList;
import java.util.List;

import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class ResourceAPI extends AbstractPoolComponent implements Versionable<ResourceAPIKey> {

    private String name;
    private String versionMin;
    private String versionMax;
    private String procedureUriOnGet;
    private String procedureUriOnGetAll;
    private String procedureUriOnPost;
    private String procedureUriOnDelete;
    private String procedureUriOnPut;
    private String procedureUriOnPatch;
    private List<CustomRESTAction> customRESTActions = new LinkedList<CustomRESTAction>();

    @Override
    public void dereference() {
        // TODO Auto-generated method stub
    }

    @Override
    public String getVersionMin() {
        return versionMin;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#minAPIVersion", minOccurs = 0, maxOccurs = 1)
    public void setVersionMin(String versionMin) {
        this.versionMin = versionMin;
    }

    @Override
    public String getVersionMax() {
        return versionMax;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#maxAPIVersion", maxOccurs = 1)
    public void setVersionMax(String versionMax) {
        this.versionMax = versionMax;
    }

    public String getName() {
        return name;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#name", minOccurs = 1, maxOccurs = 1)
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public ResourceAPIKey getKey() {
        return ResourceAPIKey.of(name, versionMin);
    }

    @Override
    public boolean isValid() {
        return true;
    }

    public String getProcedureUriOnGet() {
        return procedureUriOnGet;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#onGet", minOccurs = 0, maxOccurs = 1,
            asString = true)
    public void setProcedureUrionGet(String procedureOnGet) {
        this.procedureUriOnGet = procedureOnGet;
    }

    public String getProcedureUriOnGetAll() {
        return procedureUriOnGetAll;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#onGetAll", minOccurs = 0, maxOccurs = 1,
            asString = true)
    public void setProcedureUriOnGetAll(String procedureOnGetAll) {
        this.procedureUriOnGetAll = procedureOnGetAll;
    }

    public String getProcedureUriOnPost() {
        return procedureUriOnPost;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#onPost", minOccurs = 0, maxOccurs = 1,
            asString = true)
    public void setProcedureUriOnPost(String procedureOnPost) {
        this.procedureUriOnPost = procedureOnPost;
    }

    public String getProcedureUriOnDelete() {
        return procedureUriOnDelete;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#onDelete", minOccurs = 0, maxOccurs = 1,
            asString = true)
    public void setProcedureUriOnDelete(String procedureOnDelete) {
        this.procedureUriOnDelete = procedureOnDelete;
    }

    public String getProcedureUriOnPut() {
        return procedureUriOnPut;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#onPut", minOccurs = 0, maxOccurs = 1,
            asString = true)
    public void setProcedureUriOnPut(String procedureOnPut) {
        this.procedureUriOnPut = procedureOnPut;
    }

    public String getProcedureUriOnPatch() {
        return procedureUriOnPatch;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#onPatch", minOccurs = 0, maxOccurs = 1,
            asString = true)
    public void setProcedureUriOnPatch(String procedureOnPatch) {
        this.procedureUriOnPatch = procedureOnPatch;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#hasCustomRESTAction")
    public void addCustomRESTAction(CustomRESTAction customRESTAction) {
        customRESTActions.add(customRESTAction);
    }

    public List<CustomRESTAction> getCustomRESTActions() {
        return customRESTActions;
    }

    public String getProcedureUri(String method, boolean isResourceRequest) {
        String procedureUri = getProcedureUriByMethod(method, isResourceRequest);
        if (procedureUri != null) {
            return procedureUri;
        }
        throw new UnsupportedOperationException("Unsupported method");
    }

    private String getProcedureUriByMethod(String method, boolean isResourceRequest) {
        switch (method.toUpperCase()) {
            case "POST":
                return procedureUriOnPost;
            case "GET":
                return isResourceRequest ? procedureUriOnGet : procedureUriOnGetAll;
            case "DELETE":
                return procedureUriOnDelete;
            case "PUT":
                return procedureUriOnPut;
            case "PATCH":
                return procedureUriOnPatch;
            default:
                return null;
        }
    }

    public String getProcedureUriByActionName(String method, String actionName) {
        String uri = getProcedureUriByCustomActionName(method, actionName);
        if (uri != null) {
            return uri;
        }
        throw new UnsupportedOperationException("Unsupported custom action");
    }

    private String getProcedureUriByCustomActionName(String method, String name) {
        String uri = null;
        for (CustomRESTAction customRestAction : customRESTActions) {
            if (customRestAction.getName().equals(name) && method.equals(customRestAction.getHttpMethodName())) {
                uri = customRestAction.getTargetProcedureUri();
                break;
            }
        }
        return uri;
    }

}
