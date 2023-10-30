package edu.cornell.mannlib.vitro.webapp.migration.auth;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.OperationGroup;
import edu.cornell.mannlib.vitro.webapp.auth.policy.EntityPolicyController;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyLoader;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyTemplateController;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

public class ArmMigrator {

    private static final Log log = LogFactory.getLog(ArmMigrator.class);

    protected static String DISPLAY = "Display";
    protected static String UPDATE = "Update";
    protected static String PUBLISH = "Publish";

    protected static final String ARM_ADMIN = "ADMIN";
    protected static final String ARM_CURATOR = "CURATOR";
    protected static final String ARM_EDITOR = "EDITOR";
    protected static final String ARM_SELF_EDITOR = "SELF_EDITOR";
    protected static final String ARM_PUBLIC = "PUBLIC";
    protected static final String ARM_CUSTOM = "CUSTOM";
    protected StringBuilder removals = new StringBuilder();
    protected StringBuilder additions = new StringBuilder();

    protected static final List<String> armOperations = Arrays.asList(DISPLAY, UPDATE, PUBLISH);
    protected static final List<AccessObjectType> entityTypes =
            Arrays.asList(AccessObjectType.CLASS, AccessObjectType.OBJECT_PROPERTY, AccessObjectType.DATA_PROPERTY,
                    AccessObjectType.FAUX_OBJECT_PROPERTY, AccessObjectType.FAUX_DATA_PROPERTY);

    private RDFService contentRdfService;
    private RDFService configurationRdfService;
    private OntModel userAccountsModel;

    private String VALUE_QUERY = ""
            + "SELECT ?uri \n"
            + "WHERE {\n"
            + "  ?permission <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#forEntity> ?uri . \n"
            + "}";

    private String PERMISSION_SETS_QUERY = ""
            + "prefix auth: <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#>\n"
            + "SELECT ?uri \n"
            + "WHERE {\n"
            + "    ?uri a auth:PermissionSet . \n"
            + "}";

    private static Map<String, String> roleMap;
    private static Map<String, OperationGroup> operationMap;

    public ArmMigrator(RDFService contentRdfService, RDFService configurationRdfService, OntModel userAccountsModel) {
        this.contentRdfService = contentRdfService;
        this.configurationRdfService = configurationRdfService;
        this.userAccountsModel = userAccountsModel;
        operationMap = new HashMap<>();
        operationMap.put(DISPLAY, OperationGroup.DISPLAY_GROUP);
        operationMap.put(UPDATE, OperationGroup.UPDATE_GROUP);
        operationMap.put(PUBLISH, OperationGroup.PUBLISH_GROUP);

        roleMap = new HashMap<>();
        roleMap.put(ARM_ADMIN, AuthMigrator.ROLE_ADMIN_URI);
        roleMap.put(ARM_CURATOR, AuthMigrator.ROLE_CURATOR_URI);
        roleMap.put(ARM_EDITOR, AuthMigrator.ROLE_EDITOR_URI);
        roleMap.put(ARM_SELF_EDITOR, AuthMigrator.ROLE_SELF_EDITOR_URI);
        roleMap.put(ARM_PUBLIC, AuthMigrator.ROLE_PUBLIC_URI);
        Set<String> actualRoles = getPermissionSets();
        for (String role : actualRoles) {
            if (!roleMap.values().contains(role)) {
                String roleName = getRoleName(role);
                log.info(String.format("Custom role %s found.", roleName));
                roleMap.put(roleName, role);
                PolicyTemplateController.createRoleDataSets(role);
                log.info(String.format("Created policy data sets for custom role %s.", roleName));
            }
        }
    }

    public static String getArmPermissionSubject(String operation, String role) {
        return "java:edu.cornell.mannlib.vitro.webapp.auth.permissions.Entity" + operation + "Permission#" + role;
    }

    private static String getRoleName(String roleUri) {
        return roleUri.substring(roleUri.lastIndexOf('#') + 1);
    }

    public void migrateConfiguration() {
        cleanEntityDataSetValues();
        Map<AccessObjectType, Set<String>> entityTypeMap = getEntityMap();
        collectAdditions(entityTypeMap);
        PolicyLoader.getInstance().updateAccessControlModel(additions.toString(), true);
    }

    private void cleanEntityDataSetValues() {
        getStatementsToRemove();
        PolicyLoader.getInstance().updateAccessControlModel(removals.toString(), false);
    }

    protected StringBuilder getStatementsToRemove() {
        for (String role : roleMap.keySet()) {
            String newRole = roleMap.get(role);
            getRoleStatementsToRemove(newRole);
        }
        return removals;
    }

    private void getRoleStatementsToRemove(String role) {
        for (String operation : armOperations) {
            OperationGroup og = operationMap.get(operation);
            getRoleOpGroupStatementsToRemove(role, og);
        }
    }

    private void getRoleOpGroupStatementsToRemove(String role, OperationGroup og) {
        for (AccessOperation ao : OperationGroup.getOperations(og)) {
            getRoleOperationStatementsToRemove(role, ao);
        }
    }

    private void getRoleOperationStatementsToRemove(String role, AccessOperation ao) {
        for (AccessObjectType aot : entityTypes) {
            getRoleOperationObjectTypedStatementsToRemove(role, ao, aot);
        }
    }

    private void getRoleOperationObjectTypedStatementsToRemove(String role, AccessOperation ao, AccessObjectType aot) {
        Set<String> entityUris = PolicyLoader.getInstance().getDataSetValues(ao, aot, role);
        for (String entityUri : entityUris) {
            EntityPolicyController.getDataValueStatements(entityUri, aot, ao, Collections.singleton(role), removals);
        }
    }

    private Set<String> getFauxByBase(String baseUri) {
        Set<String> entities = new HashSet<>();
        String queryText = getQueryText(AccessObjectType.FAUX_OBJECT_PROPERTY);
        RDFService service = getRdfService(AccessObjectType.FAUX_OBJECT_PROPERTY);
        try {
            ResultSet rs = RDFServiceUtils.sparqlSelectQuery(queryText, service);
            while (rs.hasNext()) {
                QuerySolution qs = rs.next();
                String entity = qs.getResource("uri").getURI();
                String base = qs.getResource("base").getURI();
                if (baseUri.equals(base)) {
                    entities.add(entity);
                }
            }
        } catch (Exception e) {
            log.error(e, e);
        }
        return entities;
    }

    protected void collectAdditions(Map<AccessObjectType, Set<String>> entityTypeMap) {
        for (String role : roleMap.keySet()) {
            String newRole = roleMap.get(role);
            getRoleStatementsToAdd(entityTypeMap, role, newRole);
        }
    }

    private void getRoleStatementsToAdd(Map<AccessObjectType, Set<String>> entityTypeMap, String role, String newRole) {
        for (String operation : armOperations) {
            getRoleOperationStatementsToAdd(entityTypeMap, role, newRole, operation);
        }
    }

    private void getRoleOperationStatementsToAdd(Map<AccessObjectType, Set<String>> entityTypeMap, String role,
            String newRole, String operation) {
        String permissionUri = getArmPermissionSubject(operation, role);
        Set<String> armEntities = getArmEntites(permissionUri);
        OperationGroup og = operationMap.get(operation);
        Set<String> addFauxOP = new HashSet<>();
        Set<String> addFauxDP = new HashSet<>();
        for (AccessObjectType type : entityTypes) {
            getRoleOperationObjectTypedStatementsToAdd(entityTypeMap, role, newRole, armEntities, og, addFauxOP,
                    addFauxDP, type);
        }
    }

    private void getRoleOperationObjectTypedStatementsToAdd(Map<AccessObjectType, Set<String>> entityTypeMap,
            String role, String newRole, Set<String> armEntities, OperationGroup og, Set<String> addFauxOP,
            Set<String> addFauxDP, AccessObjectType type) {
        Set<String> allTypeEntitities = entityTypeMap.get(type);
        HashSet<String> intersectionEntities = new HashSet<>(armEntities);
        intersectionEntities.retainAll(allTypeEntitities);

        // Workaround for ARM behavior
        if (AccessObjectType.OBJECT_PROPERTY.equals(type)) {
            // find all faux object properties for each of base property from entityUri set
            // and add to the list of additional faux object properties
            for (String entity : intersectionEntities) {
                Set<String> faux = getFauxByBase(entity);
                addFauxOP.addAll(faux);
            }
        }
        if (AccessObjectType.DATA_PROPERTY.equals(type)) {
            // find all faux data properties for each of base property from entityUri set
            // and add to the list of additional faux data properties
            for (String entity : intersectionEntities) {
                Set<String> faux = getFauxByBase(entity);
                addFauxDP.addAll(faux);
            }
        }
        if (AccessObjectType.FAUX_OBJECT_PROPERTY.equals(type)) {
            intersectionEntities.addAll(addFauxOP);
        }
        if (AccessObjectType.FAUX_DATA_PROPERTY.equals(type)) {
            intersectionEntities.addAll(addFauxDP);
        }
        for (AccessOperation ao : OperationGroup.getOperations(og)) {
            for (String entityUri : intersectionEntities) {
                log.info(String.format("Allow %s role to %s %s entity <%s>", role, ao, type, entityUri));
                EntityPolicyController.getDataValueStatements(entityUri, type, ao, Collections.singleton(newRole),
                        additions);
            }
        }
    }

    protected Map<AccessObjectType, Set<String>> getEntityMap() {
        Map<AccessObjectType, Set<String>> map = new HashMap<>();
        for (AccessObjectType type : entityTypes) {
            addEntitiesForType(type, map);
        }
        return map;
    }

    private void addEntitiesForType(AccessObjectType type, Map<AccessObjectType, Set<String>> map) {
        Set<String> entities = new HashSet<>();
        String queryText = getQueryText(type);
        RDFService service = getRdfService(type);
        try {
            ResultSet rs = RDFServiceUtils.sparqlSelectQuery(queryText, service);
            while (rs.hasNext()) {
                QuerySolution qs = rs.next();
                String entity = qs.getResource("uri").getURI();
                if (AccessObjectType.FAUX_DATA_PROPERTY.equals(type)) {
                    String baseUri = qs.getResource("base").getURI();
                    Set<String> dataPropSet = map.get(AccessObjectType.DATA_PROPERTY);
                    if (!dataPropSet.contains(baseUri)) {
                        continue;
                    }
                }
                if (AccessObjectType.FAUX_OBJECT_PROPERTY.equals(type)) {
                    String baseUri = qs.getResource("base").getURI();
                    Set<String> objectPropSet = map.get(AccessObjectType.OBJECT_PROPERTY);
                    if (!objectPropSet.contains(baseUri)) {
                        continue;
                    }
                }
                entities.add(entity);
            }
        } catch (Exception e) {
            log.error(e, e);
        }
        map.put(type, entities);
    }

    private String getQueryText(AccessObjectType type) {
        String fauxTypeSpecificPatterns = "" + "GRAPH <" + ModelNames.DISPLAY + "> {"
                + "  ?context <http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#configContextFor> ?base .\n"
                + "  ?context <http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#hasConfiguration> ?uri .\n"
                + "  ?uri rdf:type <http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#ObjectPropertyDisplayConfig> .\n"
                + "}";
        switch (type) {
            case FAUX_OBJECT_PROPERTY:
                return getQuery(fauxTypeSpecificPatterns);
            case FAUX_DATA_PROPERTY:
                return getQuery(fauxTypeSpecificPatterns);
            case CLASS:
                return getQuery("{?uri rdf:type owl:Class .}\n");
            case DATA_PROPERTY:
                return getQuery("{?uri rdf:type owl:DatatypeProperty .}\n");
            default:
                return getQuery("{?uri rdf:type owl:ObjectProperty .}\n");
        }
    }

    private static String getQuery(String typePatterns) {
        return ""
                + "PREFIX vitro: <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#> \n"
                + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
                + "PREFIX owl: <http://www.w3.org/2002/07/owl#> \n"
                + "SELECT ?base ?uri \n"
                + "WHERE {\n"
                + typePatterns + "} \n";
    }

    private RDFService getRdfService(AccessObjectType type) {
        if (AccessObjectType.FAUX_OBJECT_PROPERTY.equals(type) || AccessObjectType.FAUX_DATA_PROPERTY.equals(type)) {
            return configurationRdfService;
        }
        return contentRdfService;
    }

    private Set<String> getArmEntites(String permissionUri) {
        Set<String> entities = new HashSet<>();
        ParameterizedSparqlString pss = new ParameterizedSparqlString(VALUE_QUERY);
        pss.setIri("permission", permissionUri);
        String queryText = pss.toString();
        userAccountsModel.enterCriticalSection(false);
        try {
            QueryExecution qexec = QueryExecutionFactory.create(queryText, userAccountsModel);
            try {
                ResultSet results = qexec.execSelect();
                while (results.hasNext()) {
                    QuerySolution qs = results.next();
                    String entity = qs.getResource("uri").getURI();
                    entities.add(entity);
                }
            } finally {
                qexec.close();
            }
        } finally {
            userAccountsModel.leaveCriticalSection();
        }
        return entities;
    }

    public boolean isArmConfiguation() {
        return containsAdminDisplayPermission();
    }

    private boolean containsAdminDisplayPermission() {
        boolean result = false;
        String queryText = ""
                + "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
                + "ASK WHERE {\n"
                + "    <"
                + getArmPermissionSubject(DISPLAY, ARM_ADMIN) + "> ?p ?o .\n" + "}";
        userAccountsModel.enterCriticalSection(false);
        try {
            QueryExecution qexec = QueryExecutionFactory.create(queryText, userAccountsModel);
            try {
                result = qexec.execAsk();
            } finally {
                qexec.close();
            }
        } finally {
            userAccountsModel.leaveCriticalSection();
        }
        return result;
    }

    private Set<String> getPermissionSets() {
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
}
