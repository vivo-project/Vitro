package edu.cornell.mannlib.vitro.webapp.migration.auth;

import static edu.cornell.mannlib.vitro.webapp.migration.auth.AuthMigrator.ALL_ROLES;
import static edu.cornell.mannlib.vitro.webapp.migration.auth.AuthMigrator.ROLE_ADMIN_URI;
import static edu.cornell.mannlib.vitro.webapp.migration.auth.AuthMigrator.ROLE_CURATOR_URI;
import static edu.cornell.mannlib.vitro.webapp.migration.auth.AuthMigrator.ROLE_EDITOR_URI;
import static edu.cornell.mannlib.vitro.webapp.migration.auth.AuthMigrator.ROLE_PUBLIC_URI;
import static edu.cornell.mannlib.vitro.webapp.migration.auth.AuthMigrator.ROLE_SELF_EDITOR_URI;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

public class SimplePermissionMigrator {

    private static final Log log = LogFactory.getLog(SimplePermissionMigrator.class);

    private OntModel userAccountsModel;

    private String PERMISSION_SETS_QUERY = ""
            + "prefix auth: <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#>\n"
            + "SELECT ?uri \n"
            + "WHERE {\n"
            + "  ?uri a auth:PermissionSet . \n"
            + "}";

    private String ROLE_PERMISSIONS_QUERY = ""
            + "prefix auth: <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#>\n"
            + "SELECT ?permission \n"
            + "WHERE {\n"
            + "  ?role a auth:PermissionSet . \n"
            + "  ?role auth:hasPermission ?permission .\n"
            + "  FILTER (strstarts(str(?permission), "
            + "'java:edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission#'))"
            + "}";

    private Map<String, Set<String>> roleMap;

    public SimplePermissionMigrator(OntModel userAccountsModel) {
        this.userAccountsModel = userAccountsModel;
        roleMap = new HashMap<>();
        roleMap.put(ROLE_ADMIN_URI, ALL_ROLES);
        roleMap.put(ROLE_CURATOR_URI, new HashSet<String>(
                Arrays.asList(ROLE_CURATOR_URI, ROLE_EDITOR_URI, ROLE_SELF_EDITOR_URI, ROLE_PUBLIC_URI)));
        roleMap.put(ROLE_EDITOR_URI,
                new HashSet<String>(Arrays.asList(ROLE_EDITOR_URI, ROLE_SELF_EDITOR_URI, ROLE_PUBLIC_URI)));
        roleMap.put(ROLE_SELF_EDITOR_URI, new HashSet<String>(Arrays.asList(ROLE_SELF_EDITOR_URI, ROLE_PUBLIC_URI)));
        roleMap.put(ROLE_PUBLIC_URI, new HashSet<String>(Arrays.asList(ROLE_PUBLIC_URI)));
    }

    public void migrateConfiguration() {
        Set<String> roles = getPermissionSets();
        if (!roles.containsAll(AuthMigrator.ALL_ROLES)) {
            log.error("Roles not found. Simple permission migration failed.");
            return;
        }
        migrateRoles(roles);
    }

    private void migrateRoles(Set<String> roles) {
        PolicyLoader policyLoader = PolicyLoader.getInstance();
        for (String role : roles) {
            // get all simple permissions for role
            Set<String> policyPermissions =
                    policyLoader.getDataSetValues(AccessOperation.EXECUTE, AccessObjectType.NAMED_OBJECT, role);
            // Compare with simple permissions in access control graph
            Set<String> userAccountsPermissions = getUserAccountPermissions(role);
            Set<String> toRevoke = new HashSet<>(policyPermissions);
            toRevoke.removeAll(userAccountsPermissions);
            Set<String> toGrant = new HashSet<>(userAccountsPermissions);
            toGrant.removeAll(policyPermissions);
            for (String entityUri : toGrant) {
                policyLoader.addEntityToPolicyDataSet(entityUri, AccessObjectType.NAMED_OBJECT, AccessOperation.EXECUTE,
                        role);
                log.info(String.format("Granted simple permission %s to role %s ", entityUri, role));
            }
            for (String entityUri : toRevoke) {
                policyLoader.removeEntityFromPolicyDataSet(entityUri, AccessObjectType.NAMED_OBJECT,
                        AccessOperation.EXECUTE, role);
                log.info(String.format("Revoked simple permission %s from role %s ", entityUri, role));
            }
        }
    }

    Set<String> getPermissionSets() {
        Set<String> permissionSets = new HashSet<>();
        String queryText = PERMISSION_SETS_QUERY;
        userAccountsModel.enterCriticalSection(false);
        try {
            QueryExecution qexec = QueryExecutionFactory.create(queryText, userAccountsModel);
            try {
                ResultSet results = qexec.execSelect();
                while (results.hasNext()) {
                    QuerySolution qs = results.next();
                    String entity = qs.getResource("uri").getURI();
                    permissionSets.add(entity);
                }
            } finally {
                qexec.close();
            }
        } finally {
            userAccountsModel.leaveCriticalSection();
        }
        return permissionSets;
    }

    Set<String> getUserAccountPermissions(String role) {
        Set<String> permissions = new HashSet<>();
        if (AuthMigrator.ALL_ROLES.contains(role)) {
            for (String standardRole : roleMap.get(role)) {
                populateUserAccountPermissions(standardRole, permissions);
            }
        } else {
            populateUserAccountPermissions(role, permissions);
        }
        return permissions;
    }

    private void populateUserAccountPermissions(String role, Set<String> permissions) {
        ParameterizedSparqlString pss = new ParameterizedSparqlString(ROLE_PERMISSIONS_QUERY);
        pss.setIri("role", role);
        String queryText = pss.toString();
        userAccountsModel.enterCriticalSection(false);
        try {
            QueryExecution qexec = QueryExecutionFactory.create(queryText, userAccountsModel);
            try {
                ResultSet results = qexec.execSelect();
                while (results.hasNext()) {
                    QuerySolution qs = results.next();
                    String entity = qs.getResource("permission").getURI();
                    permissions.add(entity);
                }
            } finally {
                qexec.close();
            }
        } finally {
            userAccountsModel.leaveCriticalSection();
        }
    }
}
