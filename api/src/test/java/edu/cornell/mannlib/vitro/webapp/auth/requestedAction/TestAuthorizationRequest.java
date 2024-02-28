package edu.cornell.mannlib.vitro.webapp.auth.requestedAction;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation;
import edu.cornell.mannlib.vitro.webapp.auth.objects.AccessObject;

public class TestAuthorizationRequest extends SimpleAuthorizationRequest {

    private Set<String> roleUris = Collections.emptySet();
    private Set<String> editorUris = Collections.emptySet();
    private boolean isRoot = false;

    public TestAuthorizationRequest(AccessObject object, AccessOperation operation) {
        super(object, operation);
    }

    public TestAuthorizationRequest(String namedAccessObject) {
        super(namedAccessObject);
    }

    public void setRoleUris(Collection<String> roleUris) {
        this.roleUris = new HashSet<String>(roleUris);
    }

    public void setEditorUris(Set<String> uris) {
        this.editorUris = new HashSet<String>(uris);

    }

    @Override
    public Set<String> getRoleUris() {
        return roleUris;
    }

    @Override
    public Set<String> getEditorUris() {
        return editorUris;
    }

    @Override
    public boolean isRootUser() {
        return isRoot;
    }

}
