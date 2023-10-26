package edu.cornell.mannlib.vitro.webapp.migration.auth;

import java.util.HashSet;
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
            + "    ?uri a auth:PermissionSet . \n"
            + "}";

    private String ROLE_PERMISSIONS_QUERY = ""
            + "prefix auth: <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#>\n"
            + "SELECT ?permission \n"
            + "WHERE {\n"
            + "    ?role a auth:PermissionSet . \n"
            + "    ?role auth:hasPermission ?permission .\n"
            + "}";


    public SimplePermissionMigrator(OntModel userAccountsModel) {
        this.userAccountsModel = userAccountsModel;
    }

    public void migrateConfiguration() {
        Set<String> roles = getPermissionSets();
        PolicyLoader policyLoader = PolicyLoader.getInstance();
        for (String role : roles) {
            // get all simple permissions for role
            Set<String> policyPermissions =
                    policyLoader.getDataSetValues(AccessOperation.EXECUTE, AccessObjectType.NAMED_OBJECT, role);
            // Compare with simple permissions in access control graph
            Set<String> userAccountsPermissions = getUserAccountsPermissions(role);
            Set<String> toRevoke = new HashSet<>(policyPermissions);
            toRevoke.removeAll(userAccountsPermissions);
            Set<String> toGrant = new HashSet<>(userAccountsPermissions);
            toGrant.removeAll(policyPermissions);
            for (String entityUri : toGrant) {
                policyLoader.addEntityToPolicyDataSet(entityUri, AccessObjectType.NAMED_OBJECT, AccessOperation.EXECUTE,
                        role);
                log.info(String.format("Granted simple permission %s to role %s ", entityUri, role ));
            }
            for (String entityUri : toRevoke) {
                policyLoader.removeEntityFromPolicyDataSet(entityUri, AccessObjectType.NAMED_OBJECT,
                        AccessOperation.EXECUTE, role);
                log.info(String.format("Revoked simple permission %s from role %s ", entityUri, role ));
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

    Set<String> getUserAccountsPermissions(String role) {
        Set<String> permissions = new HashSet<>();
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
        return permissions;
    }
}
