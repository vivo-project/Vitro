/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.migration.auth;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.OperationGroup;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyLoader;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ContextModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.WhichService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AuthMigrator implements ServletContextListener { 
    private static final String PREFIX = "http://vitro.mannlib.cornell.edu/ns/vitro/role#";

    private static final Log log = LogFactory.getLog(AuthMigrator.class);

    private static final String OLD_ROLE_PUBLIC = PREFIX + "public";
    private static final String OLD_ROLE_SELF = PREFIX + "selfEditor";
    private static final String OLD_ROLE_EDITOR = PREFIX + "editor";
    private static final String OLD_ROLE_CURATOR = PREFIX + "curator";
    private static final String OLD_ROLE_DB_ADMIN = PREFIX + "dbAdmin";
    private static final String OLD_ROLE_NOBODY = PREFIX + "nobody";
    
    private static final String ROLE_ADMIN_URI = "http://vitro.mannlib.cornell.edu/ns/vitro/authorization#ADMIN";
    private static final String ROLE_CURATOR_URI = "http://vitro.mannlib.cornell.edu/ns/vitro/authorization#CURATOR";
    private static final String ROLE_EDITOR_URI = "http://vitro.mannlib.cornell.edu/ns/vitro/authorization#EDITOR";
    private static final String ROLE_SELF_EDITOR_URI = "http://vitro.mannlib.cornell.edu/ns/vitro/authorization#SELF_EDITOR";
    private static final String ROLE_PUBLIC_URI = "http://vitro.mannlib.cornell.edu/ns/vitro/authorization#PUBLIC";

    private static final String fauxTypeSpecificPatterns = 
            "  ?context <http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#configContextFor> ?base .\n"
          + "  ?context <http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#hasConfiguration> ?uri .\n"
          + "  ?uri rdf:type <http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#ObjectPropertyDisplayConfig> .\n";
    protected static final Map<String,Set<String>> showMap;
    protected static final Set<String> allRoles;

    private RDFService contentRdfService;
    private RDFService configurationRdfService;
    private static Map<String,String> policyKeyToDataValueMap = new HashMap<String,String>();

    static {
        allRoles = new HashSet<String>(Arrays.asList(ROLE_ADMIN_URI, ROLE_CURATOR_URI, ROLE_EDITOR_URI, ROLE_SELF_EDITOR_URI, ROLE_PUBLIC_URI));
        showMap = new HashMap<>();
        showMap.put(OLD_ROLE_PUBLIC, allRoles);
        showMap.put(OLD_ROLE_SELF, new HashSet<String>(Arrays.asList(ROLE_ADMIN_URI, ROLE_CURATOR_URI, ROLE_EDITOR_URI, ROLE_SELF_EDITOR_URI)));
        showMap.put(OLD_ROLE_EDITOR, new HashSet<String>(Arrays.asList(ROLE_ADMIN_URI, ROLE_CURATOR_URI, ROLE_EDITOR_URI)));
        showMap.put(OLD_ROLE_CURATOR, new HashSet<String>(Arrays.asList(ROLE_ADMIN_URI, ROLE_CURATOR_URI)));
        showMap.put(OLD_ROLE_DB_ADMIN, new HashSet<String>(Arrays.asList(ROLE_ADMIN_URI)));
        showMap.put(OLD_ROLE_NOBODY, Collections.emptySet());
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        long begin = System.currentTimeMillis();
        ContextModelAccess modelAccess = ModelAccess.getInstance();
        initialize(modelAccess.getRDFService(WhichService.CONTENT), modelAccess.getRDFService(WhichService.CONFIGURATION));
        ServletContext ctx = sce.getServletContext();
        StartupStatus ss = StartupStatus.getBean(ctx);
        log.info("Started authorization configuration update");
        convertAuthorizationConfiguration();
        log.info("Finished authorization configuration update");
        ss.info(this, secondsSince(begin) + " seconds to migrate auth models");
    }
    
    protected void initialize(RDFService content, RDFService configuration) {
        contentRdfService = content;
        configurationRdfService = configuration;
    }

    protected void convertAuthorizationConfiguration() {
        if(isArmConfiguration()) {
            convertArmConfiguration();
        } else {
            convertAnnotationConfiguration();
        }
    }
    
    protected void convertAnnotationConfiguration() {
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
        long lines = updatePolicyDatasets(AccessObjectType.OBJECT_PROPERTY, opConfigs);
        log.info(String.format("Updated object property datasets, added %s values", lines));
        lines = updatePolicyDatasets(AccessObjectType.DATA_PROPERTY, dpConfigs);
        log.info(String.format("Updated data property datasets, added %s values", lines));
        lines = updatePolicyDatasets(AccessObjectType.CLASS, classConfigs);
        log.info(String.format("Updated class property datasets, added %s values", lines));
        lines = updatePolicyDatasets(AccessObjectType.FAUX_OBJECT_PROPERTY, fopConfigs);
        log.info(String.format("Updated faux object property datasets, added %s values", lines));
        lines = updatePolicyDatasets(AccessObjectType.FAUX_DATA_PROPERTY, fdpConfigs);
        log.info(String.format("Updated data property datasets, added %s values", lines));
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

    private long updatePolicyDatasets(AccessObjectType aot, Map<String, Map<OperationGroup, Set<String>>> configs) {
        StringBuilder sb = new StringBuilder();
        for (String entityUri : configs.keySet()) {
            Map<OperationGroup, Set<String>> groupMap = configs.get(entityUri);
            for (OperationGroup og : groupMap.keySet()) {
                Set<String> roles = groupMap.get(og);
                addDataSetStatements(entityUri, aot, og, roles, sb);
                log.debug(String.format("Updated entity %s dataset for operation group %s access object type %s roles %s", entityUri, og, aot, roles));
            }
        }
        long begin = System.currentTimeMillis();
        PolicyLoader.getInstance().updateUserAccountsModel(sb.toString(), true);
        log.info(secondsSince(begin) + "Spent on write to model");
        return getLineCount(sb.toString());
    }
    
    public void addDataSetStatements(String entityUri, AccessObjectType aot, OperationGroup og, Set<String> selectedRoles, StringBuilder sb) {
        if (StringUtils.isBlank(entityUri)) {
            return;
        }
        for (String role : selectedRoles) {
            String testDataUri = getPolicyTestDataUri(aot, og, role);
            if (testDataUri == null) {
                log.error(String.format("Policy test data wasn't found by key:\n%s\n%s\n%s", og, aot, role));
                continue;
            }
            sb.append("<").append(testDataUri).append("> <https://vivoweb.org/ontology/vitro-application/auth/vocabulary/dataValue> <").append(entityUri).append("> .\n");
        }
    }

    private static String getPolicyTestDataUri(AccessObjectType aot, OperationGroup og, String role) {
        String key = aot.toString() + "." + og.toString() + "." + role ;
        if (policyKeyToDataValueMap.containsKey(key)) {
            return policyKeyToDataValueMap.get(key);
        }
        String uri = PolicyLoader.getInstance().getEntityPolicyTestDataValue(og, aot, role);
        policyKeyToDataValueMap.put(key, uri);
        return uri;
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

    private void convertArmConfiguration() {
        // TODO Auto-generated method stub
    }

    private boolean isArmConfiguration() {
        return false;
    }

    private String getAnnotationQuery(String typeSpecificPatterns) {
        return ""
                + "PREFIX vitro: <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#> \n"
                + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
                + "PREFIX owl: <http://www.w3.org/2002/07/owl#> \n"
                + "SELECT ?base ?uri ?update ?display ?publish\n"
                + "WHERE {\n"
                + typeSpecificPatterns
                + "  OPTIONAL { ?uri vitro:hiddenFromDisplayBelowRoleLevelAnnot ?displayAssigned . }\n"
                + "  BIND (COALESCE(?displayAssigned, <http://vitro.mannlib.cornell.edu/ns/vitro/role#public> ) AS ?display )\n"
                + "  OPTIONAL { ?uri vitro:prohibitedFromUpdateBelowRoleLevelAnnot ?updateAssigned . }\n"
                + "  BIND (COALESCE(?updateAssigned, <http://vitro.mannlib.cornell.edu/ns/vitro/role#selfEditor> ) AS ?update )\n"
                + "  OPTIONAL { ?uri vitro:hiddenFromPublishBelowRoleLevelAnnot ?publishAssigned . }\n"
                + "  BIND (COALESCE(?publishAssigned, <http://vitro.mannlib.cornell.edu/ns/vitro/role#public> ) AS ?publish )\n"
                + "  FILTER (!isBlank(?uri))\n"
                + "} \n";
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Nothing to tear down.
    }
    
    private long getLineCount(String lines) {
        Matcher matcher = Pattern.compile("\n").matcher(lines);
        long i = 0;
        while (matcher.find()) {
            i ++;
        }
        return i;
    }

    private long secondsSince(long startTime) {
        return (System.currentTimeMillis() - startTime) / 1000;
    }
}
