package edu.cornell.mannlib.vitro.webapp.dynapi.data;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.NullProcedure;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Procedure;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;

public class DataStore {

    protected Map<String, Data> dataMap = new HashMap<>();
    private ContentType responseType = ContentType.APPLICATION_JSON;
    private List<String> acceptLangs = new LinkedList<>();
    private String resourceId = "";
    private UserAccount user;
    private Map<String, Procedure> dependencyComponents = new HashMap<>();

    public DataStore() {
    }

    public void addData(String name, Data data) {
        dataMap.put(name, data);
    }

    public Data getData(String name) {
        return dataMap.get(name);
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceID(String resourceId) {
        this.resourceId = resourceId;
    }

    public ContentType getResponseType() {
        return responseType;
    }

    public void setResponseType(ContentType contentType) {
        this.responseType = contentType;
    }

    public void setAcceptLangs(List<String> acceptLangs) {
        this.acceptLangs.clear();
        this.acceptLangs.addAll(acceptLangs);
    }

    public List<String> getAcceptLangs() {
        return acceptLangs;
    }

    public boolean contains(String name) {
        return dataMap.containsKey(name);
    }

    protected Set<Entry<String, Data>> entrySet() {
        return dataMap.entrySet();
    }

    protected Set<String> keySet() {
        return dataMap.keySet();
    }

    public void putDependency(String uri, Procedure dependency) {
        dependencyComponents.put(uri, dependency);
    }

    public void putDependencies(Map<String, Procedure> dependencies) {
        dependencyComponents.putAll(dependencies);
    }

    public Map<String, Procedure> getDependencies() {
        return dependencyComponents;
    }

    public Procedure getDependency(String uri) {
        if (StringUtils.isEmpty(uri)) {
            return NullProcedure.getInstance();
        }
        return dependencyComponents.get(uri);
    }

    public boolean containsDependency(String uri) {
        return dependencyComponents.containsKey(uri);
    }

    public void removeDependencies() {
        for (Procedure dependencyComponent : dependencyComponents.values()) {
            dependencyComponent.removeClient();
        }
        dependencyComponents.clear();
    }

    public boolean containsData(DataStore expectedDatas) {
        Set<String> expectedDataNames = expectedDatas.dataMap.keySet();
        if (!dataMap.keySet().containsAll(expectedDataNames)) {
            return false;
        }
        for (String name : expectedDataNames) {
            Data actualData = getData(name);
            Data expectedData = expectedDatas.getData(name);
            if (!actualData.equals(expectedData)) {
                return false;
            }
        }
        return true;
    }

    public void setUser(UserAccount user) {
        this.user = user;
    }

    public UserAccount getUser() {
        return user;
    }

    public String getUserUri() {
        if (user != null) {
            return user.getUri();
        }
        return "";
    }

    public void remove(String name) {
        dataMap.remove(name);
    }

}
