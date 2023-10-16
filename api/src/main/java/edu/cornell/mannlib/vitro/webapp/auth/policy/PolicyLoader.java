/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.AttributeFactory;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.OperationGroup;
import edu.cornell.mannlib.vitro.webapp.auth.rules.AccessRule;
import edu.cornell.mannlib.vitro.webapp.auth.rules.AccessRuleFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.event.BulkUpdateEvent;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeSet;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService.ModelSerializationFormat;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ResultSetConsumer;
import edu.cornell.mannlib.vitro.webapp.rdfservice.adapters.VitroModelFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;

public class PolicyLoader {

    private static final String PRIORITY = "priority";
    public static final String POLICY = "policy";
    private static final Log log = LogFactory.getLog(PolicyLoader.class);
    private static final String POLICY_QUERY = "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "prefix owl: <http://www.w3.org/2002/07/owl#>\n"
            + "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
            + "prefix xsd: <http://www.w3.org/2001/XMLSchema#>\n"
            + "prefix auth: <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#>\n"
            + "prefix ai: <https://vivoweb.org/ontology/vitro-application/auth/individual/>\n"
            + "prefix ao: <https://vivoweb.org/ontology/vitro-application/auth/vocabulary/>\n"
            + "SELECT DISTINCT ?" + POLICY + " ?" + PRIORITY + " \n"
            + "WHERE {\n"
            + "  GRAPH <http://vitro.mannlib.cornell.edu/default/access-control> {\n"
            + "    ?" + POLICY + " rdf:type ao:Policy .\n"
            + "    OPTIONAL {?" + POLICY + " ao:priority ?set_priority"
            + " . }\n"
            + "    BIND(COALESCE(?set_priority, 0 ) as ?" + PRIORITY + " ) .\n"
            + "  }\n"
            + "} ORDER BY ?" + PRIORITY;

    private static final String PRIORITY_QUERY = "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "prefix owl: <http://www.w3.org/2002/07/owl#>\n"
            + "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
            + "prefix xsd: <http://www.w3.org/2001/XMLSchema#>\n"
            + "prefix auth: <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#>\n"
            + "prefix ai: <https://vivoweb.org/ontology/vitro-application/auth/individual/>\n"
            + "prefix ao: <https://vivoweb.org/ontology/vitro-application/auth/vocabulary/>\n"
            + "SELECT DISTINCT ?" + PRIORITY + " \n"
            + "WHERE {\n"
            + "  GRAPH <http://vitro.mannlib.cornell.edu/default/access-control> {\n"
            + "    OPTIONAL {?" + POLICY + " ao:priority ?set_priority" + " . }\n"
            + "    BIND(COALESCE(?set_priority, 0 ) as ?" + PRIORITY + " ) .\n"
            + "  }\n"
            + "} ORDER BY ?" + PRIORITY;

    private static final String DATASET_QUERY = "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "prefix owl: <http://www.w3.org/2002/07/owl#>\n"
            + "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
            + "prefix xsd: <http://www.w3.org/2001/XMLSchema#>\n"
            + "prefix auth: <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#>\n"
            + "prefix ai: <https://vivoweb.org/ontology/vitro-application/auth/individual/>\n"
            + "prefix ao: <https://vivoweb.org/ontology/vitro-application/auth/vocabulary/>\n"
            + "SELECT DISTINCT ?dataSet \n" + "WHERE {\n"
            + "  GRAPH <http://vitro.mannlib.cornell.edu/default/access-control> {\n"
            + "       ?policy ao:testDatasets ?dataSets .\n"
            + "       ?policy rdf:type ao:Policy .\n"
            + "       ?dataSets ao:testDataset ?dataSet .\n"
            + "  }\n"
            + "} ORDER BY ?dataSet";

    private static final String NO_DATASET_RULES_QUERY = "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "prefix owl: <http://www.w3.org/2002/07/owl#>\n"
            + "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
            + "prefix xsd: <http://www.w3.org/2001/XMLSchema#>\n"
            + "prefix auth: <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#>\n"
            + "prefix ai: <https://vivoweb.org/ontology/vitro-application/auth/individual/>\n"
            + "prefix ao: <https://vivoweb.org/ontology/vitro-application/auth/vocabulary/>\n"
            + "SELECT DISTINCT ?rules ?rule ?attribute ?testId ?typeId ?value ?lit_value ?decision_id \n" + "WHERE {\n"
            + "  GRAPH <http://vitro.mannlib.cornell.edu/default/access-control> {\n"
            + "?policy rdf:type ao:Policy .\n"
            + "?policy ao:rules ?rules . \n"
            + "?rules ao:rule ?rule . \n"
            + "?rule ao:attribute ?attribute .\n"
            + "OPTIONAL {\n"
            + "  ?attribute ao:operator ?attributeTest .\n"
            + "  OPTIONAL {\n"
            + "    ?attributeTest ao:id ?testId . \n"
            + "  }\n"
            + "}"
            + "OPTIONAL {\n"
            + "  ?attribute ao:type ?attributeType . \n"
            + "  OPTIONAL {\n"
            + "    ?attributeType ao:id ?typeId . \n"
            + "  }\n"
            + "}\n"
            + "OPTIONAL {\n"
            + "   ?rule ao:decision ?decision . \n"
            + "   ?decision ao:id ?decision_id . \n"
            + "}\n"
            + "?attribute ao:value ?value . \n"
            + "OPTIONAL {?value ao:id ?lit_value . }\n"
            + "  }\n"
            + "} ORDER BY ?rule ?attribute";

    private static final String DATASET_RULES_QUERY = "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "prefix owl: <http://www.w3.org/2002/07/owl#>\n"
            + "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
            + "prefix xsd: <http://www.w3.org/2001/XMLSchema#>\n"
            + "prefix auth: <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#>\n"
            + "prefix ai: <https://vivoweb.org/ontology/vitro-application/auth/individual/>\n"
            + "prefix ao: <https://vivoweb.org/ontology/vitro-application/auth/vocabulary/>\n"
            + "SELECT DISTINCT ?rules ?rule ?attribute ?testId ?typeId ?value ?lit_value ?decision_id ?dataSetUri \n"
            + "WHERE {\n"
            + "  GRAPH <http://vitro.mannlib.cornell.edu/default/access-control> {\n"
            + "    ?policy a ao:PolicyTemplate .\n"
            + "    ?policy ao:rules ?rules .\n"
            + "    ?rules rdf:type ao:Rules .\n"
            + "    ?rules ao:rule ?rule .\n"
            + "    ?rule ao:attribute ?attribute .\n"
            + "    ?attribute rdf:type ao:Attribute .\n"
            + "    OPTIONAL {\n"
            + "      ?attribute ao:operator ?attributeTest .\n"
            + "      OPTIONAL {\n"
            + "        ?attributeTest ao:id ?testId .\n"
            + "      }\n"
            + "    }\n"
            + "    OPTIONAL {\n"
            + "      ?attribute ao:type ?attributeType .\n"
            + "      OPTIONAL {\n"
            + "        ?attributeType ao:id ?typeId .\n"
            + "      }\n"
            + "    }\n"
            + "    OPTIONAL {\n"
            + "       ?rule ao:decision ?decision .\n"
            + "       ?decision ao:id ?decision_id .\n"
            + "    }\n"
            + "    OPTIONAL {\n"
            + "       ?attribute ao:templateValue ?attributeValueSet .\n"
            + "       ?attributeValueSet a ao:AttributeValueSet .\n"
            + "       ?attributeValueSet ao:attributeValue ?attributeValue .\n"
            + "       ?attributeValue ao:dataValue ?value .\n"
            + "       ?dataSet ao:dataSetValues ?attributeValue .\n"
            + "       OPTIONAL {?value ao:id ?lit_value . }\n"
            + "    }\n"
            + "    OPTIONAL {\n"
            + "       ?attribute ao:value ?value .\n"
            + "       OPTIONAL {?value ao:id ?lit_value . }\n"
            + "    }\n" 
            + "    BIND(?dataSet as ?dataSetUri)\n"
            + "  }\n"
            + "} ORDER BY ?rule ?attribute";

    private static final String policyKeyTemplatePrefix =
              "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "prefix owl: <http://www.w3.org/2002/07/owl#>\n"
            + "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
            + "prefix xsd: <http://www.w3.org/2001/XMLSchema#>\n"
            + "prefix auth: <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#>\n"
            + "prefix ai: <https://vivoweb.org/ontology/vitro-application/auth/individual/>\n"
            + "prefix ao: <https://vivoweb.org/ontology/vitro-application/auth/vocabulary/>\n"
            + "SELECT DISTINCT ?"
            + POLICY + "?testData ?value ?valueId ( COUNT(?key) AS ?keySize ) \n"
            + "WHERE {\n"
            + "  GRAPH <http://vitro.mannlib.cornell.edu/default/access-control> {\n"
            + "  ?" + POLICY + " ao:policyKey ?policyKeyUri .\n"
            + "  ?" + POLICY + " ao:testDatasets ?testDataSets .\n"
            + "  ?testDataSets ao:testDataset ?dataSet . \n"
            + "  ?dataSet ao:testData ?testData . \n"
            + "  OPTIONAL { ?testData ao:dataValue ?value . \n"
            + "    OPTIONAL { ?value ao:id ?valueId . } \n"
            + "  }"
            + "  ?policyKeyUri ao:keyComponent ?key .\n";

    private static final String policyKeyTemplateSuffix = "}} GROUP BY ?" + POLICY + " ?value ?valueId ?testData";

    private static final String policyStatementByKeyTemplatePrefix =
              "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "prefix owl: <http://www.w3.org/2002/07/owl#>\n"
            + "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
            + "prefix xsd: <http://www.w3.org/2001/XMLSchema#>\n"
            + "prefix auth: <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#>\n"
            + "prefix ai: <https://vivoweb.org/ontology/vitro-application/auth/individual/>\n"
            + "prefix ao: <https://vivoweb.org/ontology/vitro-application/auth/vocabulary/>\n"
            + "CONSTRUCT { \n"
            + "  ?testData ao:dataValue <%s> .\n"
            + "}\n"
            + "WHERE {\n"
            + "  GRAPH <http://vitro.mannlib.cornell.edu/default/access-control> {\n"
            + "  ?" + POLICY + " ao:policyKey ?policyKeyUri .\n"
            + "  ?" + POLICY + " ao:testDatasets ?testDataSets .\n"
            + "  ?testDataSets ao:testDataset ?dataSet . \n"
            + "  ?dataSet ao:testData ?testData . \n";

    private static final String policyStatementByKeyTemplateSuffix = "}}";

    private RDFService rdfService;
    public static final String RULE = "rule";
    public static final String LITERAL_VALUE = "lit_value";
    public static final String ATTR_VALUE = "value";
    public static final String ATTRIBUTE = "attribute";
    public static final String TEST_ID = "testId";
    public static final String TYPE_ID = "typeId";
    private static PolicyLoader INSTANCE;

    public static PolicyLoader getInstance() {
        return INSTANCE;
    }

    private PolicyLoader(RDFService rdfs) {
        this.rdfService = rdfs;
    }

    public static void initialize(RDFService rdfService) {
        INSTANCE = new PolicyLoader(rdfService);
    }

    public void loadPolicies() {
        List<String> policyUris = getPolicyUris();
        for (String uri : policyUris) {
            debug("Loading policy %s", uri);
            DynamicPolicy policy = loadPolicy(uri);
            if (policy != null) {
                log.debug("Loaded policy " + uri);
                // take policy priority into account
                PolicyStore.getInstance().add(policy);
            }
        }
    }

    public List<String> getPolicyUris() {
        debug("SPARQL Query to get policy uris from the graph:\n %s", POLICY_QUERY);
        List<String> policyUris = new LinkedList<String>();
        try {
            rdfService.sparqlSelectQuery(POLICY_QUERY, new ResultSetConsumer() {
                @Override
                protected void processQuerySolution(QuerySolution qs) {
                    if (!qs.contains(POLICY) || !qs.get(POLICY).isResource()) {
                        // debug("Policy solution doesn't contain policy
                        // resource");
                        return;
                    }
                    policyUris.add(qs.getResource(POLICY).getURI());
                }
            });
        } catch (RDFServiceException e) {
            log.error(e, e);
        }

        return policyUris;
    }
    
    @Deprecated
    public DynamicPolicy loadPolicy(String uri) {
        List<String> dataSetNames = getDataSetNames(uri);
        Set<AccessRule> rules = new HashSet<>();
        long priority = getPriority(uri);
        try {
            if (dataSetNames.isEmpty()) {
                loadRulesWithoutDataSet(uri, rules);
            } else {
                for (String dataSetName : dataSetNames) {
                    loadRulesForDataSet(uri, rules, dataSetName);
                }
            }
        } catch (Exception e) {
            return null;
        }
        if (rules.isEmpty()) {
            return null;
        }
        DynamicPolicy policy = new DynamicPolicy(uri, priority);
        policy.addRules(rules);
        return policy;
    }
    
    public DynamicPolicy loadPolicyWithDataSet(String uri, String dataSetUri) {
        Set<AccessRule> rules = new HashSet<>();
        long priority = getPriority(uri);
        try {
            if (dataSetUri == null) {
                loadRulesWithoutDataSet(uri, rules);
            } else {
                loadRulesForDataSet(uri, rules, dataSetUri);
            }
        } catch (Exception e) {
            log.error(e, e);
            return null;
        }
        if (rules.isEmpty()) {
            return null;
        }
        String policyUri = uri;
        if (dataSetUri != null) {
            policyUri += "+" + dataSetUri;
        }
        DynamicPolicy policy = new DynamicPolicy(policyUri, priority);
        policy.addRules(rules);
        return policy;
    }

    public Set<String> getPolicyDataSetValues(OperationGroup og, AccessObjectType aot, String role) {
        Set<String> values = new HashSet<>();
        long expectedSize = 3;
        final String queryText = getPolicyTestValuesByKeyQuery(new String[] { role },
                new String[] { og.toString(), aot.toString() });
        debug("SPARQL Query to get policy data set values:\n %s", queryText);
        try {
            rdfService.sparqlSelectQuery(queryText, new ResultSetConsumer() {
                @Override
                protected void processQuerySolution(QuerySolution qs) {
                    if (!qs.contains(POLICY) || !qs.contains("keySize") || !qs.get("keySize").isLiteral()) {
                        return;
                    }
                    long keySize = qs.getLiteral("keySize").getLong();
                    if (expectedSize != keySize) {
                        return;
                    }
                    if (qs.contains("valueId") && qs.get("valueId").isLiteral()) {
                        values.add(qs.getLiteral("valueId").getString());
                    } else if (qs.contains("value")) {
                        RDFNode node = qs.get("value");
                        if (node.isLiteral()) {
                            values.add(node.asLiteral().toString());
                        } else if (node.isResource()) {
                            values.add(node.asResource().getURI());
                        }
                    }
                }
            });
        } catch (RDFServiceException e) {
            log.error(e, e);
        }
        return values;
    }

    public String getPolicyUriByKey(OperationGroup og, AccessObjectType aot, String role) {
        long expectedSize = 3;
        final String queryText = getPolicyTestValuesByKeyQuery(new String[] { role },
                new String[] { og.toString(), aot.toString() });
        debug("SPARQL Query to get policy data set values:\n %s", queryText);
        String[] uri = new String[1];
        try {
            rdfService.sparqlSelectQuery(queryText, new ResultSetConsumer() {
                @Override
                protected void processQuerySolution(QuerySolution qs) {
                    if (!qs.contains(POLICY) || !qs.get(POLICY).isResource() || !qs.contains("keySize")
                            || !qs.get("keySize").isLiteral()) {
                        return;
                    }
                    long keySize = qs.getLiteral("keySize").getLong();
                    if (expectedSize != keySize) {
                        return;
                    }
                    uri[0] = qs.getResource(POLICY).getURI();
                }
            });
        } catch (RDFServiceException e) {
            log.error(e, e);
        }
        return uri[0];
    }

    public String getEntityPolicyTestDataValue(OperationGroup og, AccessObjectType aot, String role) {
        String[] valueUri = new String[1];
        long expectedSize = 3;
        final String queryText = getPolicyTestValuesByKeyQuery(new String[] { role },
                new String[] { og.toString(), aot.toString() });
        debug("SPARQL Query to get policy data set value:\n %s", queryText);
        try {
            rdfService.sparqlSelectQuery(queryText, new ResultSetConsumer() {
                @Override
                protected void processQuerySolution(QuerySolution qs) {
                    if (!qs.contains(POLICY) || !qs.contains("keySize") || !qs.get("keySize").isLiteral()) {
                        return;
                    }
                    long keySize = qs.getLiteral("keySize").getLong();
                    if (expectedSize != keySize) {
                        return;
                    }
                    if (qs.contains("testData") && qs.get("testData").isResource()) {
                        valueUri[0] = qs.getResource("testData").getURI();
                    }
                }
            });
        } catch (RDFServiceException e) {
            log.error(e, e);
        }
        return valueUri[0];
    }

    public void modifyPolicyDataSetValue(String entityUri, OperationGroup og, AccessObjectType aot, String role,
            boolean isAdd) {
        final String queryText = getPolicyDataSetValueStatementByKeyQuery(entityUri, new String[] { role },
                new String[] { og.toString(), aot.toString() });
        debug("SPARQL Query to get policy data set values:\n %s", queryText);
        Model m = VitroModelFactory.createModel();
        try {
            rdfService.sparqlConstructQuery(queryText, m);
        } catch (RDFServiceException e) {
            log.error(e, e);
        }
        if (m.isEmpty()) {
            log.error("statements to add/delete are empty");
            return;
        }
        updateUserAccountsModel(m, isAdd);
    }

    private void updateUserAccountsModel(Model data, boolean isAdd) {
        StringWriter sw = new StringWriter();
        data.write(sw, "TTL");
        updateAccessControlModel(sw.toString(), isAdd);
    }

    public void updateAccessControlModel(String data, boolean isAdd) {
        try {
            ChangeSet changeSet = makeChangeSet();
            InputStream in = new ByteArrayInputStream(data.getBytes());
            debug(modelToString(data, isAdd));
            if (isAdd) {
                changeSet.addAddition(in, ModelSerializationFormat.N3, ModelNames.ACCESS_CONTROL);
            } else {
                changeSet.addRemoval(in, ModelSerializationFormat.N3, ModelNames.ACCESS_CONTROL);
            }
            rdfService.changeSetUpdate(changeSet);
        } catch (RDFServiceException e) {
            String message = modelToString(data, isAdd);
            log.error(message);
            log.error(e, e);
        }
    }

    private String modelToString(String ruleData, boolean isAdd) {
        String message = (isAdd ? "Adding to" : "Removing from") + " user accounts model \n" + ruleData;
        return message;
    }

    private static String getPolicyDataSetValueStatementByKeyQuery(String entityUri, String[] uris, String[] ids) {
        StringBuilder query = new StringBuilder();
        query.append(String.format(policyStatementByKeyTemplatePrefix, entityUri));
        for (String uri : uris) {
            query.append(String.format("  ?policyKeyUri ao:keyComponent <%s> . \n", uri));
        }
        int i = 0;
        for (String id : ids) {
            query.append(String.format("  ?policyKeyUri ao:keyComponent ?uri%d . ?uri%d ao:id \"%s\" . \n", i, i, id));
            i++;
        }
        query.append(policyStatementByKeyTemplateSuffix);
        return query.toString();
    }

    private static String getPolicyTestValuesByKeyQuery(String[] uris, String[] ids) {
        StringBuilder query = new StringBuilder(policyKeyTemplatePrefix);
        for (String uri : uris) {
            query.append(String.format("  ?policyKeyUri ao:keyComponent <%s> . \n", uri));
        }
        int i = 0;
        for (String id : ids) {
            query.append(String.format("  ?policyKeyUri ao:keyComponent ?uri%d . ?uri%d ao:id \"%s\" . \n", i, i, id));
            i++;
        }
        query.append(policyKeyTemplateSuffix);
        return query.toString();
    }

    private long getPriority(String uri) {
        ParameterizedSparqlString pss = new ParameterizedSparqlString(PRIORITY_QUERY);
        pss.setIri(POLICY, uri);
        debug("Get priority for uri %s query:\n %s", uri, pss.toString());
        long[] priority = new long[1];
        try {
            rdfService.sparqlSelectQuery(pss.toString(), new ResultSetConsumer() {
                @Override
                protected void processQuerySolution(QuerySolution qs) {
                    if (!qs.contains(PRIORITY) || !qs.get(PRIORITY).isLiteral()) {
                        priority[0] = 0L;
                        return;
                    }
                    priority[0] = qs.getLiteral(PRIORITY).getLong();
                }
            });
        } catch (RDFServiceException e) {
            log.error(e, e);
        }
        return priority[0];
    }

    private void loadRulesWithoutDataSet(String policyUri, Set<AccessRule> rules) throws Exception {
        ParameterizedSparqlString pss = new ParameterizedSparqlString(NO_DATASET_RULES_QUERY);
        pss.setIri(POLICY, policyUri);
        debug(pss.toString());
        AccessRule rule[] = new AccessRule[1];
        Exception ex[] = new Exception[1];
        try {
            rdfService.sparqlSelectQuery(pss.toString(), new ResultSetConsumer() {
                @Override
                protected void processQuerySolution(QuerySolution qs) {
                    try {
                        if (isInvalidPolicySolution(policyUri, qs)) {
                            throw new Exception();
                        }
                        if (isRuleContinues(rule[0], qs)) {
                            populateRule(rule[0], qs);
                        } else {
                            if (rule[0] != null) {
                                rules.add(rule[0]);
                            }
                            rule[0] = AccessRuleFactory.createRule(qs);
                        }
                    } catch (Exception e) {
                        ex[0] = e;
                    }
                }
            });
        } catch (RDFServiceException e) {
            log.error(e, e);
        }
        if (ex[0] != null) {
            throw ex[0];
        }
        if (rule[0] != null) {
            rules.add(rule[0]);
            debug("\nLoaded %s rules for %s policy", rules.size(), policyUri);
        } else {
            debug("\nNo rules loaded from the user accounts model for %s policy.", policyUri);
        }
    }

    private void loadRulesForDataSet(String policyUri, Set<AccessRule> rules, String dataSetName) throws Exception {
        ParameterizedSparqlString pss = new ParameterizedSparqlString(DATASET_RULES_QUERY);
        pss.setIri(POLICY, policyUri);
        pss.setIri("dataSet", dataSetName);
        debug(pss.toString());
        AccessRule rule[] = new AccessRule[1];
        Exception ex[] = new Exception[1];
        try {
            rdfService.sparqlSelectQuery(pss.toString(), new ResultSetConsumer() {
                @Override
                protected void processQuerySolution(QuerySolution qs) {
                    try {
                        if (isInvalidPolicySolution(policyUri, qs)) {
                            throw new Exception();
                        }
                        if (isRuleContinues(rule[0], qs)) {
                            populateRule(rule[0], qs);
                        } else {
                            if (rule[0] != null) {
                                rules.add(rule[0]);
                            }
                            rule[0] = AccessRuleFactory.createRule(qs);
                        }
                    } catch (Exception e) {
                        ex[0] = e;
                    }
                }
            });
        } catch (RDFServiceException e) {
            log.error(e, e);
        }
        if (ex[0] != null) {
            throw ex[0];
        }
        if (rule[0] != null) {
            rules.add(rule[0]);
            debug("\nLoaded %s rules for %s policy", rules.size(), policyUri);
        } else {
            debug("\nNo rules loaded from the user accounts model for %s policy.", policyUri);
        }
    }

    private List<String> getDataSetNames(String policyUri) {
        List<String> result = new ArrayList<>();
        ParameterizedSparqlString pss = new ParameterizedSparqlString(DATASET_QUERY);
        pss.setIri(POLICY, policyUri);
        try {
            rdfService.sparqlSelectQuery(pss.toString(), new ResultSetConsumer() {
                @Override
                protected void processQuerySolution(QuerySolution qs) {
                    if (qs.contains("dataSet")) {
                        RDFNode dataSet = qs.get("dataSet");
                        if (dataSet.isResource()) {
                            result.add(dataSet.asResource().getURI());
                        }
                    }
                }
            });
        } catch (RDFServiceException e) {
            log.error(e, e);
        }
        return result;
    }

    private void debugSelectQueryResults(ResultSet rs) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ResultSetFormatter.outputAsJSON(baos, rs);
        String json = new String(baos.toByteArray());
        debug(json);
    }

    private static boolean isRuleContinues(AccessRule rule, QuerySolution qs) {
        if (rule == null) {
            return false;
        }
        String ruleUri = qs.getResource("rule").getURI();
        if (qs.contains("dataSetUri")) {
            ruleUri += "." + qs.getResource("dataSetUri").getURI();
        }
        return rule.getRuleUri().equals(ruleUri);
    }

    private static void populateRule(AccessRule ar, QuerySolution qs) throws Exception {
        if (ar == null) {
            return;
        }
        String attributeUri = qs.getResource("attribute").getURI();
        if (ar.containsAttributeUri(attributeUri)) {
            AttributeFactory.extendAttribute(ar.getAttribute(attributeUri), qs);
            return;
        }
        try {
            ar.addAttribute(AttributeFactory.createAttribute(qs));
        } catch (Exception e) {
            log.error(e, e);
        }
    }

    private static boolean isInvalidPolicySolution(String uri, QuerySolution qs) {
        if (!qs.contains("rules") || !qs.get("rules").isResource()) {
            debug("Policy <%s> solution doesn't contain rules uri", uri);
            return true;
        }
        if (!qs.contains("rule") || !qs.get("rule").isResource()) {
            debug("Policy <%s> solution doesn't contain rule uri", uri);
            return true;
        }
        if (!qs.contains("value")) {
            debug("Policy <%s> solution doesn't contain value", uri);
            return true;
        }
        if (!qs.contains("typeId") || !qs.get("typeId").isLiteral()) {
            debug("Policy <%s> solution doesn't contain attribute type id", uri);
            return true;
        }
        if (!qs.contains("testId") || !qs.get("testId").isLiteral()) {
            debug("Policy <%s> solution doesn't contain attribute test id", uri);
            return true;
        }
        return false;
    }

    private static void debug(String template, Object... objects) {
        if (log.isDebugEnabled()) {
            log.debug(String.format(template, objects));
        }
    }

    public void addEntityToPolicyDataSet(String entityUri, AccessObjectType aot, OperationGroup og, String role) {
        modifyPolicyDataSetValue(entityUri, og, aot, role, true);
    }

    public void removeEntityFromPolicyDataSet(String entityUri, AccessObjectType aot, OperationGroup og, String role) {
        modifyPolicyDataSetValue(entityUri, og, aot, role, false);
    }

    private ChangeSet makeChangeSet() {
        ChangeSet cs = rdfService.manufactureChangeSet();
        cs.addPreChangeEvent(new BulkUpdateEvent(null, true));
        cs.addPostChangeEvent(new BulkUpdateEvent(null, false));
        return cs;
    }
}
