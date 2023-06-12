package edu.cornell.mannlib.vitro.webapp.migration.auth;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.OperationGroup;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyLoader;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;

import static edu.cornell.mannlib.vitro.webapp.migration.auth.AuthMigrator.*;

public class AnnotationMigrator {
    
    private static final Log log = LogFactory.getLog(AnnotationMigrator.class);

    private static final String PREFIX = "http://vitro.mannlib.cornell.edu/ns/vitro/role#";
    private static final String OLD_ROLE_PUBLIC = PREFIX + "public";
    private static final String OLD_ROLE_SELF = PREFIX + "selfEditor";
    private static final String OLD_ROLE_EDITOR = PREFIX + "editor";
    private static final String OLD_ROLE_CURATOR = PREFIX + "curator";
    private static final String OLD_ROLE_DB_ADMIN = PREFIX + "dbAdmin";
    private static final String OLD_ROLE_NOBODY = PREFIX + "nobody";
    
    private RDFService contentRdfService;
    private RDFService configurationRdfService;
    private Map<String,Set<String>> showMap;
    
    public AnnotationMigrator(RDFService contentRdfService, RDFService configurationRdfService) {
        this.contentRdfService = contentRdfService;
        this.configurationRdfService = configurationRdfService;
        showMap = new HashMap<>();
        showMap.put(OLD_ROLE_PUBLIC, allRoles);
        showMap.put(OLD_ROLE_SELF, new HashSet<String>(Arrays.asList(ROLE_ADMIN_URI, ROLE_CURATOR_URI, ROLE_EDITOR_URI, ROLE_SELF_EDITOR_URI)));
        showMap.put(OLD_ROLE_EDITOR, new HashSet<String>(Arrays.asList(ROLE_ADMIN_URI, ROLE_CURATOR_URI, ROLE_EDITOR_URI)));
        showMap.put(OLD_ROLE_CURATOR, new HashSet<String>(Arrays.asList(ROLE_ADMIN_URI, ROLE_CURATOR_URI)));
        showMap.put(OLD_ROLE_DB_ADMIN, new HashSet<String>(Arrays.asList(ROLE_ADMIN_URI)));
        showMap.put(OLD_ROLE_NOBODY, Collections.emptySet());
    }

    protected void migrateConfiguration() {
        log.info("Started annotation configuration conversion");
        Map<String, Map<OperationGroup, Set<String>>> opConfigs = getObjectPropertyAnnotations();
        log.info(String.format("Found %s object property annotation configurations", opConfigs.size()));
        Map<String, Map<OperationGroup, Set<String>>> dpConfigs = getDataPropertyAnnotations();
        log.info(String.format("Found %s data property annotation configurations", dpConfigs.size()));
        Map<String, Map<OperationGroup, Set<String>>> classConfigs = getClassAnnotations();
        log.info(String.format("Found %s class annotation configurations", classConfigs.size()));
        Map<String, Map<OperationGroup, Set<String>>> fopConfigs = getFauxObjectPropertyAnnotations(opConfigs.keySet());
        log.info(String.format("Found %s faux object property annotation configurations", fopConfigs.size()));
        Map<String, Map<OperationGroup, Set<String>>> fdpConfigs = getFauxDataPropertyAnnotations(dpConfigs.keySet());
        log.info(String.format("Found %s faux data property annotation configurations", fdpConfigs.size()));
        Long[] valueCounts = AuthMigrator.updatePolicyDatasets(AccessObjectType.OBJECT_PROPERTY, opConfigs);
        log.info(String.format("Updated object property datasets. Added %d values, removed %d values", valueCounts[0], valueCounts[1]));
        valueCounts = AuthMigrator.updatePolicyDatasets(AccessObjectType.DATA_PROPERTY, dpConfigs);
        log.info(String.format("Updated data property datasets, added %d values, removed %d values", valueCounts[0], valueCounts[1]));
        valueCounts = AuthMigrator.updatePolicyDatasets(AccessObjectType.CLASS, classConfigs);
        log.info(String.format("Updated class property datasets, added %d values, removed %d values", valueCounts[0], valueCounts[1]));
        valueCounts = AuthMigrator.updatePolicyDatasets(AccessObjectType.FAUX_OBJECT_PROPERTY, fopConfigs);
        log.info(String.format("Updated faux object property datasets, added %d values, removed %d values", valueCounts[0], valueCounts[1]));
        valueCounts = AuthMigrator.updatePolicyDatasets(AccessObjectType.FAUX_DATA_PROPERTY, fdpConfigs);
        log.info(String.format("Updated data property datasets, added %d values, removed %d values", valueCounts[0], valueCounts[1]));
        PolicyLoader.getInstance().loadPolicies();
    }

    protected Map<String, Map<OperationGroup, Set<String>>> getFauxDataPropertyAnnotations(Set<String> dataProperties) {
        String queryText = getAnnotationQuery(fauxTypeSpecificPatterns);
        return getFauxConfigurations(queryText, configurationRdfService, dataProperties);
    }

    protected Map<String, Map<OperationGroup, Set<String>>> getFauxObjectPropertyAnnotations(Set<String> objectProperties) {
        String queryText = getAnnotationQuery(fauxTypeSpecificPatterns);
        return getFauxConfigurations(queryText, configurationRdfService, objectProperties);
    }

    protected Map<String, Map<OperationGroup, Set<String>>> getClassAnnotations() {
        String queryText = getAnnotationQuery("  ?uri rdf:type owl:Class .\n");
        return getConfigurations(queryText, contentRdfService);
    }

    protected Map<String, Map<OperationGroup, Set<String>>> getDataPropertyAnnotations() {
        String queryText = getAnnotationQuery("  ?uri rdf:type owl:DatatypeProperty .\n");
        return getConfigurations(queryText, contentRdfService);
    }

    protected Map<String, Map<OperationGroup, Set<String>>> getObjectPropertyAnnotations() {
        String queryText = getAnnotationQuery("  ?uri rdf:type owl:ObjectProperty .\n");
        return getConfigurations(queryText, contentRdfService);
    }

    /**
     * @return map of entity URIs and maps of operations and list of allowed roles  
     */
    private Map<String, Map<OperationGroup, Set<String>>> getConfigurations(String queryText, RDFService service) {
        Map<String, Map<OperationGroup, Set<String>>> configs = new HashMap<>();
        try {
            ResultSet rs = RDFServiceUtils.sparqlSelectQuery(queryText, service);
            while (rs.hasNext()) {
                QuerySolution qs = rs.next();
                collectConfiguration(configs, qs);
            }
        } catch (Exception e) {
            log.error(e, e);
        }
        return configs;
    }

    /**
     * @param targetProperties set of property URIs to get configurations from
     * @return map of entity URIs and maps of operations and list of allowed roles  
     */
    private Map<String, Map<OperationGroup, Set<String>>> getFauxConfigurations(String queryText, RDFService service, Set<String> targetProperties) {
        Map<String, Map<OperationGroup, Set<String>>> configs = new HashMap<>();
        try {
            ResultSet rs = RDFServiceUtils.sparqlSelectQuery(queryText, service);
            while (rs.hasNext()) {
                QuerySolution qs = rs.next();
                String baseUri = qs.getResource("base").getURI();
                if (!targetProperties.contains(baseUri)) {
                    continue;
                }
                collectConfiguration(configs, qs);
            }
        } catch (Exception e) {
            log.error(e, e);
        }
        return configs;
    }

    private void collectConfiguration(Map<String, Map<OperationGroup, Set<String>>> configs, QuerySolution qs) {
        String uri = qs.getResource("uri").getURI();
        String displayAnnotation = qs.getResource("display").getURI();
        Set<String> displayRoles = showMap.get(displayAnnotation);
    
        String publishAnnotation = qs.getResource("publish").getURI();
        Set<String> publishRoles = showMap.get(publishAnnotation);
        publishRoles.remove(ROLE_PUBLIC_URI);
    
        String updateAnnotation = qs.getResource("update").getURI();
        Set<String> updateRoles = new HashSet<>(showMap.get(updateAnnotation));
        updateRoles.remove(ROLE_PUBLIC_URI);
    
        Map<OperationGroup, Set<String>> config = new HashMap<>();
        config.put(OperationGroup.UPDATE_GROUP, updateRoles);
        config.put(OperationGroup.PUBLISH_GROUP, publishRoles);
        config.put(OperationGroup.DISPLAY_GROUP, displayRoles);
        configs.put(uri, config);
    }

    private static String getAnnotationQuery(String typeSpecificPatterns) {
        return ""
                + "PREFIX vitro: <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#> \n"
                + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
                + "PREFIX owl: <http://www.w3.org/2002/07/owl#> \n"
                + "SELECT ?base ?uri ?update ?display ?publish\n"
                + "WHERE {\n"
                + typeSpecificPatterns
                + "{  OPTIONAL { ?uri vitro:hiddenFromDisplayBelowRoleLevelAnnot ?displayAssigned . }\n"
                + "  BIND (COALESCE(?displayAssigned, <http://vitro.mannlib.cornell.edu/ns/vitro/role#public> ) AS ?display )\n"
                + "  OPTIONAL { ?uri vitro:prohibitedFromUpdateBelowRoleLevelAnnot ?updateAssigned . }\n"
                + "  BIND (COALESCE(?updateAssigned, <http://vitro.mannlib.cornell.edu/ns/vitro/role#selfEditor> ) AS ?update )\n"
                + "  OPTIONAL { ?uri vitro:hiddenFromPublishBelowRoleLevelAnnot ?publishAssigned . }\n"
                + "  BIND (COALESCE(?publishAssigned, <http://vitro.mannlib.cornell.edu/ns/vitro/role#public> ) AS ?publish )\n"
                + "  FILTER (!isBlank(?uri))\n"
                + "}} \n";
    }

    private static final String fauxTypeSpecificPatterns = ""
      + "GRAPH <" + ModelNames.DISPLAY + "> {"
      + "  ?context <http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#configContextFor> ?base .\n"
      + "  ?context <http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#hasConfiguration> ?uri .\n"
      + "  ?uri rdf:type <http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#ObjectPropertyDisplayConfig> .\n"
      + "} GRAPH <" + ModelNames.DISPLAY + ">";


}
