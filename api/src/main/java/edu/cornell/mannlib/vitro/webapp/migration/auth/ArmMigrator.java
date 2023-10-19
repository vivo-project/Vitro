package edu.cornell.mannlib.vitro.webapp.migration.auth;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.OperationGroup;
import edu.cornell.mannlib.vitro.webapp.auth.policy.EntityPolicyController;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyLoader;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.query.ParameterizedSparqlString;
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

    protected static final List<String> armRoles = Arrays.asList(ARM_ADMIN, ARM_CURATOR, ARM_EDITOR, ARM_SELF_EDITOR,
            ARM_PUBLIC);
    protected static final List<String> armOperations = Arrays.asList(DISPLAY, UPDATE, PUBLISH);
    protected static final List<AccessObjectType> entityTypes = Arrays.asList(AccessObjectType.CLASS,
            AccessObjectType.OBJECT_PROPERTY, AccessObjectType.DATA_PROPERTY, AccessObjectType.FAUX_OBJECT_PROPERTY,
            AccessObjectType.FAUX_DATA_PROPERTY);

    private RDFService contentRdfService;
    private RDFService configurationRdfService;

    private String VALUE_QUERY = "" + "SELECT ?uri \n" + "WHERE {\n" + "  GRAPH <" + ModelNames.USER_ACCOUNTS + "> {\n"
            + "  ?permission <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#forEntity> ?uri . \n" + "  }\n"
            + "}";

    private static Map<String, String> roleMap;
    private static Map<String, OperationGroup> operationMap;

    public ArmMigrator(RDFService contentRdfService, RDFService configurationRdfService) {
        this.contentRdfService = contentRdfService;
        this.configurationRdfService = configurationRdfService;
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
    }

    public static String getArmPermissionSubject(String operation, String role) {
        return "java:edu.cornell.mannlib.vitro.webapp.auth.permissions.Entity" + operation + "Permission#" + role;
    }

    public void migrateConfiguration() {
        cleanEntityDataSetValues();
        Map<AccessObjectType, Set<String>> entityTypeMap = getEntityMap();
        StringBuilder additions = new StringBuilder();
        collectAdditions(entityTypeMap, additions);
        PolicyLoader.getInstance().updateAccessControlModel(additions.toString(), true);
    }

    private void cleanEntityDataSetValues() {
        StringBuilder removals = getStatementsToRemove();
        PolicyLoader.getInstance().updateAccessControlModel(removals.toString(), false);
    }

    protected StringBuilder getStatementsToRemove() {
        StringBuilder removals = new StringBuilder();
        for (String role : armRoles) {
            String newRole = roleMap.get(role);
            for (String operation : armOperations) {
                OperationGroup og = operationMap.get(operation);
                for (AccessObjectType aot : entityTypes) {
               //     Set<String> entityUris = PolicyLoader.getInstance().getDataSetValues(og, aot, newRole);
               //     for (String entityUri : entityUris) {
               //         EntityPolicyController.getDataValueStatements(entityUri, aot, og,
               //                 Collections.singleton(newRole), removals);
               //     }
                }
            }
        }
        return removals;
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

    protected void collectAdditions(Map<AccessObjectType, Set<String>> entityTypeMap, StringBuilder additions) {

        for (String role : armRoles) {
            String newRole = roleMap.get(role);
            for (String operation : armOperations) {
                String permissionUri = getArmPermissionSubject(operation, role);
                Set<String> armEntities = getArmEntites(permissionUri);
                OperationGroup og = operationMap.get(operation);
                Set<String> addFauxOP = new HashSet<>();
                Set<String> addFauxDP = new HashSet<>();
                for (AccessObjectType type : entityTypes) {
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
                    for (String entityUri : intersectionEntities) {
                       // EntityPolicyController.getDataValueStatements(entityUri, type, og,
                       //         Collections.singleton(newRole), additions);
                    }
                }
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
        return "" + "PREFIX vitro: <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#> \n"
                + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
                + "PREFIX owl: <http://www.w3.org/2002/07/owl#> \n" + "SELECT ?base ?uri \n" + "WHERE {\n"
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
        try {
            ResultSet rs = RDFServiceUtils.sparqlSelectQuery(queryText, configurationRdfService);
            while (rs.hasNext()) {
                QuerySolution qs = rs.next();
                String entity = qs.getResource("uri").getURI();
                entities.add(entity);
            }
        } catch (Exception e) {
            log.error(e, e);
        }
        return entities;
    }

    public boolean isArmConfiguation() {
        return containsAdminDisplayPermission();
    }

    private boolean containsAdminDisplayPermission() {
        boolean result = false;
        String query = "" + "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + "ASK WHERE {\n"
                + "  GRAPH <http://vitro.mannlib.cornell.edu/default/vitro-kb-userAccounts> {\n" + "       <"
                + getArmPermissionSubject(DISPLAY, ARM_ADMIN) + "> ?p ?o .\n" + "  }\n" + "}";
        try {
            result = configurationRdfService.sparqlAskQuery(query);
        } catch (RDFServiceException e) {
            log.error(e, e);
        }
        return result;
    }
}
