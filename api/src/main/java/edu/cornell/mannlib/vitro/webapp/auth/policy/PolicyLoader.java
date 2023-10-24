/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.CheckFactory;
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
            + "SELECT DISTINCT ?policy \n"
            + "WHERE {\n"
            + "  GRAPH <http://vitro.mannlib.cornell.edu/default/access-control> {\n"
            + "    { ?policy a ao:Policy . }\n"
            + "      UNION \n"
            + "    { ?policy a ao:PolicyTemplate . }\n"
            + "  }\n"
            + "}";

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


    private static final String DATASET_PRIORITY_QUERY = "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "prefix owl: <http://www.w3.org/2002/07/owl#>\n"
            + "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
            + "prefix xsd: <http://www.w3.org/2001/XMLSchema#>\n"
            + "prefix auth: <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#>\n"
            + "prefix ai: <https://vivoweb.org/ontology/vitro-application/auth/individual/>\n"
            + "prefix ao: <https://vivoweb.org/ontology/vitro-application/auth/vocabulary/>\n"
            + "SELECT DISTINCT ?" + PRIORITY + " \n"
            + "WHERE {\n"
            + "  GRAPH <http://vitro.mannlib.cornell.edu/default/access-control> {\n"
            + "    ?policy ao:priority ?set_priority .\n"
            + "    ?policy ao:policyDataSets ?dataSets .\n"
            + "    ?dataSets ao:policyDataSet ?dataSet .\n"
            + "    OPTIONAL {?policy ao:priority ?policyPriority" + " . }\n"
            + "    OPTIONAL {?dataSet ao:priority ?dataSetPriority" + " . }\n"
            + "    BIND(COALESCE(?dataSetPriority, ?policyPriority, 0 ) as ?" + PRIORITY + " ) .\n"
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
            + "       ?policy a ao:PolicyTemplate .\n"
            + "       ?policy ao:policyDataSets ?dataSets .\n"
            + "       ?dataSets ao:policyDataSet ?dataSet .\n"
            + "  }\n"
            + "} ORDER BY ?dataSet";

    private static final String NO_DATASET_RULES_QUERY = "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "prefix owl: <http://www.w3.org/2002/07/owl#>\n"
            + "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
            + "prefix xsd: <http://www.w3.org/2001/XMLSchema#>\n"
            + "prefix auth: <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#>\n"
            + "prefix ai: <https://vivoweb.org/ontology/vitro-application/auth/individual/>\n"
            + "prefix ao: <https://vivoweb.org/ontology/vitro-application/auth/vocabulary/>\n"
            + "SELECT DISTINCT ?policyUri ?rules ?rule ?check ?testId ?typeId ?value ?lit_value ?decision_id \n"
            + "WHERE {\n"
            + "  GRAPH <http://vitro.mannlib.cornell.edu/default/access-control> {\n"
            + "?policy rdf:type ao:Policy .\n"
            + "?policy ao:rules ?rules . \n"
            + "?rules ao:rule ?rule . \n"
            + "?rule ao:check ?check .\n"
            + "OPTIONAL {\n"
            + "  ?check ao:operator ?checkTest .\n"
            + "  OPTIONAL {\n"
            + "    ?checkTest ao:id ?testId . \n"
            + "  }\n"
            + "}"
            + "OPTIONAL {\n"
            + "  ?check ao:attribute ?checkType . \n"
            + "  OPTIONAL {\n"
            + "    ?checkType ao:id ?typeId . \n"
            + "  }\n"
            + "}\n"
            + "OPTIONAL {\n"
            + "   ?rule ao:decision ?decision . \n"
            + "   ?decision ao:id ?decision_id . \n"
            + "}\n"
            + "?check ao:value ?value . \n"
            + "OPTIONAL {?value ao:id ?lit_value . }\n"
            + "  }\n"
            + "BIND(?policy as ?policyUri)\n"
            + "} ORDER BY ?rule ?check";

    private static final String DATASET_RULES_QUERY = "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "prefix owl: <http://www.w3.org/2002/07/owl#>\n"
            + "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
            + "prefix xsd: <http://www.w3.org/2001/XMLSchema#>\n"
            + "prefix auth: <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#>\n"
            + "prefix ai: <https://vivoweb.org/ontology/vitro-application/auth/individual/>\n"
            + "prefix ao: <https://vivoweb.org/ontology/vitro-application/auth/vocabulary/>\n"
            + "SELECT DISTINCT ?policyUri ?rules ?rule ?check ?testId ?typeId ?value ?lit_value ?decision_id"
            + " ?dataSetUri \n"
            + "WHERE {\n"
            + "  GRAPH <http://vitro.mannlib.cornell.edu/default/access-control> {\n"
            + "    ?policy a ao:PolicyTemplate .\n"
            + "    ?policy ao:rules ?rules .\n"
            + "    ?policy ao:policyDataSets ?policyDataSets .\n"
            + "    ?policyDataSets ao:policyDataSet ?dataSet .\n"
            + "    ?rules rdf:type ao:Rules .\n"
            + "    ?rules ao:rule ?rule .\n"
            + "    ?rule ao:check ?check .\n"
            + "    ?check rdf:type ao:Check .\n"
            + "    OPTIONAL {\n"
            + "      ?check ao:operator ?checkTest .\n"
            + "      OPTIONAL {\n"
            + "        ?checkTest ao:id ?testId .\n"
            + "      }\n"
            + "    }\n"
            + "    OPTIONAL {\n"
            + "      ?check ao:attribute ?checkType .\n"
            + "      OPTIONAL {\n"
            + "        ?checkType ao:id ?typeId .\n"
            + "      }\n"
            + "    }\n"
            + "    OPTIONAL {\n"
            + "       ?rule ao:decision ?decision .\n"
            + "       ?decision ao:id ?decision_id .\n"
            + "    }\n"
            + "    OPTIONAL {\n"
            + "       ?check ao:templateValue ?attributeValueSet .\n"
            + "       ?attributeValueSet a ao:AttributeValueSet .\n"
            + "       ?attributeValueSet ao:attributeValue ?attributeValue .\n"
            + "       ?attributeValue ao:dataValue ?value .\n"
            + "       ?dataSet ao:dataSetValues ?attributeValue .\n"
            + "       OPTIONAL {?value ao:id ?lit_value . }\n"
            + "    }\n"
            + "    OPTIONAL {\n"
            + "       ?check ao:value ?value .\n"
            + "       OPTIONAL {?value ao:id ?lit_value . }\n"
            + "    }\n"
            + "    BIND(?dataSet as ?dataSetUri)\n"
            + "    BIND(?policy as ?policyUri)\n"
            + "  }\n"
            + "} ORDER BY ?rule ?check";

    private static final String policyKeyTemplatePrefix =
              "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "prefix owl: <http://www.w3.org/2002/07/owl#>\n"
            + "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
            + "prefix xsd: <http://www.w3.org/2001/XMLSchema#>\n"
            + "prefix auth: <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#>\n"
            + "prefix ai: <https://vivoweb.org/ontology/vitro-application/auth/individual/>\n"
            + "prefix ao: <https://vivoweb.org/ontology/vitro-application/auth/vocabulary/>\n"
            + "SELECT DISTINCT ?"
            + POLICY + " ?dataSet ?testData ?value ?valueId ?valueContainer ( COUNT(?key) AS ?keySize ) \n"
            + "WHERE {\n"
            + "  GRAPH <http://vitro.mannlib.cornell.edu/default/access-control> {\n"
            + "  ?" + POLICY + " ao:policyDataSets ?policyDataSets .\n"
            + "  ?policyDataSets ao:policyDataSet ?dataSet .\n"
            + "  ?dataSet ao:dataSetKey ?dataSetKeyUri .\n"
            + "  ?dataSet ao:dataSetValues ?valueContainer .\n"
            + "  ?valueContainer ao:containerType ?containerType .\n"
            + "  ?containerType ao:id ?containerId .\n"
            + "  OPTIONAL { ?valueContainer ao:dataValue ?value .\n"
            + "    OPTIONAL { ?value ao:id ?valueId . }\n"
            + "  }\n"
            + "  ?dataSetKeyUri ao:keyComponent ?key .\n";

    private static final String policyKeyTemplateSuffix =
            "}} GROUP BY ?" + POLICY + " ?dataSet ?value ?valueId ?testData ?valueContainer";

    private static final String policyStatementByKeyTemplatePrefix =
              "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "prefix owl: <http://www.w3.org/2002/07/owl#>\n"
            + "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
            + "prefix xsd: <http://www.w3.org/2001/XMLSchema#>\n"
            + "prefix auth: <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#>\n"
            + "prefix ai: <https://vivoweb.org/ontology/vitro-application/auth/individual/>\n"
            + "prefix ao: <https://vivoweb.org/ontology/vitro-application/auth/vocabulary/>\n"
            + "CONSTRUCT { \n"
            + "  ?valueContainer ao:dataValue <%s> .\n"
            + "}\n"
            + "WHERE {\n"
            + "  GRAPH <http://vitro.mannlib.cornell.edu/default/access-control> {\n"
            + "  ?dataSet ao:dataSetKey ?dataSetKeyUri .\n"
            + "  ?" + POLICY + " ao:policyDataSets ?policyDataSets .\n"
            + "  ?policyDataSets ao:policyDataSet ?dataSet . \n"
            + "  ?dataSet ao:dataSetValues ?valueContainer . \n"
            + "  ?valueContainer ao:containerType ?containerTypeUri . \n"
            + "  ?containerTypeUri ao:id ?containerType . \n";

    private static final String policyStatementByKeyTemplateSuffix = "}}";

    public static final String DATA_SET_TEMPLATES_QUERY = ""
            + "prefix ao: <https://vivoweb.org/ontology/vitro-application/auth/vocabulary/>\n"
            + "prefix ai: <https://vivoweb.org/ontology/vitro-application/auth/individual/>\n"
            + "SELECT ?dataSetTemplate ?dataSets ( COUNT(?key) AS ?keySize )\n"
            + "WHERE {\n"
            + "  GRAPH <http://vitro.mannlib.cornell.edu/default/access-control> {\n"
            + "    ?dataSets ao:policyDataSetTemplate ?dataSetTemplate .\n"
            + "    ?dataSetTemplate ao:dataSetTemplateKey ?dataSetTemplateKey .\n"
            + "    ?dataSetTemplateKey ao:templateKey ai:SubjectRole .\n"
            + "    ?dataSetTemplateKey ao:templateKey ?key .\n"
            + "  }\n"
            + "}\n"
            + "GROUP BY ?dataSetTemplate ?dataSets";

    public static final String DATA_SET_KEY_TEMPLATE_QUERY = ""
            + "prefix ao: <https://vivoweb.org/ontology/vitro-application/auth/vocabulary/>\n"
            + "prefix ai: <https://vivoweb.org/ontology/vitro-application/auth/individual/>\n"
            + "SELECT ?keyComponent \n"
            + "WHERE {\n"
            + "  GRAPH <http://vitro.mannlib.cornell.edu/default/access-control> {\n"
            + "    ?dataSetTemplate ao:dataSetKeyTemplate ?dataSetKeyTemplate .\n"
            + "    ?dataSetKeyTemplate ao:keyComponent ?keyComponent .\n"
            + "  }\n"
            + "}\n";

    public static final String DATA_SET_KEY_TEMPLATES_TEMPLATE_QUERY = ""
            + "prefix ao: <https://vivoweb.org/ontology/vitro-application/auth/vocabulary/>\n"
            + "prefix ai: <https://vivoweb.org/ontology/vitro-application/auth/individual/>\n"
            + "SELECT ?keyComponentTemplate \n"
            + "WHERE {\n"
            + "  GRAPH <http://vitro.mannlib.cornell.edu/default/access-control> {\n"
            + "    ?dataSetTemplate ao:dataSetKeyTemplate ?dataSetKeyTemplate .\n"
            + "    ?dataSetKeyTemplate ao:keyComponentTemplate ?keyComponentTemplate .\n"
            + "  }\n"
            + "}\n";

    public static final String DATA_SET_VALUE_TEMPLATE_QUERY = ""
            + "prefix ao: <https://vivoweb.org/ontology/vitro-application/auth/vocabulary/>\n"
            + "prefix ai: <https://vivoweb.org/ontology/vitro-application/auth/individual/>\n"
            + "SELECT ?valueContainer \n"
            + "WHERE {\n"
            + "  GRAPH <http://vitro.mannlib.cornell.edu/default/access-control> {\n"
            + "    ?dataSetTemplate ao:dataSetValues ?valueContainer .\n"
            + "  }\n"
            + "}\n";

    public static final String DATA_SET_VALUE_CONTAINER_TEMPLATES_TEMPLATE_QUERY = ""
            + "prefix ao: <https://vivoweb.org/ontology/vitro-application/auth/vocabulary/>\n"
            + "prefix ai: <https://vivoweb.org/ontology/vitro-application/auth/individual/>\n"
            + "SELECT ?valueContainerTemplate \n"
            + "WHERE {\n"
            + "  GRAPH <http://vitro.mannlib.cornell.edu/default/access-control> {\n"
            + "    ?dataSetTemplate ao:dataSetValueTemplate ?valueContainerTemplate .\n"
            + "  }\n"
            + "}\n";

    public static final String CONSTRUCT_VALUE_CONTAINER_QUERY = ""
            + "prefix ai: <https://vivoweb.org/ontology/vitro-application/auth/individual/>\n"
            + "prefix ao: <https://vivoweb.org/ontology/vitro-application/auth/vocabulary/>\n"
            + "CONSTRUCT {\n"
            + "  ?relatedAttributeValueSet ao:attributeValue ?valueContainer .\n"
            + "  ?valueContainer a ao:ValueContainer .\n"
            + "  ?valueContainer ao:containerType ?containerType .\n"
            + "  ?valueContainer ao:dataValue ?newRoleUri .\n"
            + "  ?valueContainer ao:dataValue ?dataValue ."
            + "}\n"
            + "WHERE {\n"
            + "  GRAPH <http://vitro.mannlib.cornell.edu/default/access-control> {\n"
            + "    ?valueContainerTemplateUri ao:relatedValueSet ?relatedAttributeValueSet .\n"
            + "    ?valueContainerTemplateUri ao:containerTypeTemplate ?containerType .\n"
            + "    OPTIONAL {\n"
            + "      ?valueContainerTemplateUri ao:defaultValue ?dataValue .\n"
            + "    }\n"
            + "    OPTIONAL {\n"
            + "      FILTER ( str(?containerType) = 'https://vivoweb.org/ontology/vitro-application/auth/individual/SubjectRole' )\n"
            + "      BIND(?role as ?newRoleUri)\n"
            + "    }"
            + "  }"
            + "}\n";

    private RDFService rdfService;
    public static final String RULE = "rule";
    public static final String LITERAL_VALUE = "lit_value";
    public static final String ATTR_VALUE = "value";
    public static final String CHECK = "check";
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
            Set<DynamicPolicy> policies = loadPolicies(uri);
            for (DynamicPolicy policy : policies) {
                PolicyStore.getInstance().add(policy);
                log.info("Loaded policy " + policy.getUri());
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

    public Set<DynamicPolicy> loadPolicies(String uri) {
        Set<DynamicPolicy> policies = new HashSet<>();
        List<String> dataSetNames = getDataSetNames(uri);
        if (dataSetNames.isEmpty()) {
            Set<AccessRule> rules = new HashSet<>();
            try {
                loadRulesWithoutDataSet(uri, rules);
            } catch (Exception e) {
                log.info(String.format("Policy '%s' failed to load ", uri));
                log.debug(e, e);
            }
            if (!rules.isEmpty()) {
                long priority = getPriority(uri);
                DynamicPolicy policy = new DynamicPolicy(uri, priority);
                policy.addRules(rules);
                policies.add(policy);
            }
        } else {
            for (String dataSetName : dataSetNames) {
                DynamicPolicy policy = loadPolicyFromTemplateDataSet(dataSetName);
                if (policy != null) {
                    policies.add(policy);
                }
            }
        }

        return policies;
    }

    public DynamicPolicy loadPolicyFromTemplateDataSet(String dataSetUri) {
        Set<AccessRule> rules = new HashSet<>();
        long priority = getPriorityFromDataSet(dataSetUri);
        try {
            loadRulesForDataSet(rules, dataSetUri);
        } catch (Exception e) {
            log.info(String.format("Policy template dataset '%s' failed to load ", dataSetUri));
            log.debug(e, e);
            return null;
        }
        if (rules.isEmpty()) {
            return null;
        }
        DynamicPolicy policy = new DynamicPolicy(dataSetUri, priority);
        policy.addRules(rules);
        return policy;
    }

    private long getPriorityFromDataSet(String dataSetUri) {
        ParameterizedSparqlString pss = new ParameterizedSparqlString(DATASET_PRIORITY_QUERY);
        pss.setIri("dataSet", dataSetUri);
        debug("Get priority for dataset uri %s query:\n %s", dataSetUri, pss.toString());
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

    public Set<String> getDataSetValues(AccessOperation ao, AccessObjectType aot, String role) {
        Set<String> values = new HashSet<>();
        long expectedSize = 3;
        String queryText = getDataSetByKeyQuery(new String[] { role },
                new String[] { ao.toString(), aot.toString() });
        ParameterizedSparqlString pss = new ParameterizedSparqlString(queryText);
        pss.setLiteral("containerId", aot.toString());
        queryText = pss.toString();
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

    public String getEntityValueContainerUri(AccessOperation ao, AccessObjectType aot, String role) {
        long expectedSize = 3;
        String queryText = getDataSetByKeyQuery(new String[] { role }, new String[] { ao.toString(), aot.toString() });
        ParameterizedSparqlString pss = new ParameterizedSparqlString(queryText);
        pss.setLiteral("containerId", aot.toString());
        queryText = pss.toString();
        debug("SPARQL Query to get entity value container uri:\n %s", queryText);
        String[] uri = new String[1];
        try {
            rdfService.sparqlSelectQuery(queryText, new ResultSetConsumer() {
                @Override
                protected void processQuerySolution(QuerySolution qs) {
                    if (!qs.contains("dataSet") || !qs.get("dataSet").isResource() || !qs.contains("keySize")
                            || !qs.get("keySize").isLiteral()) {
                        return;
                    }
                    long keySize = qs.getLiteral("keySize").getLong();
                    if (expectedSize != keySize) {
                        log.error("wrong key size. Expected " + expectedSize + ". Actual " + keySize );
                        return;
                    }
                    uri[0] = qs.getResource("valueContainer").getURI();
                }
            });
        } catch (RDFServiceException e) {
            log.error(e, e);
        }
        return uri[0];
    }

    public void modifyPolicyDataSetValue(String entityUri, AccessOperation ao, AccessObjectType aot, String role,
            boolean isAdd) {
        String queryText = getPolicyDataSetValueStatementByKeyQuery(entityUri, new String[] { role },
                new String[] { ao.toString(), aot.toString() });
        ParameterizedSparqlString pss = new ParameterizedSparqlString(queryText);
        pss.setLiteral("containerType", aot.toString());
        queryText = pss.toString();
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
        updateAccessControlModel(m, isAdd);
    }

    void updateAccessControlModel(Model data, boolean isAdd) {
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
        String message = (isAdd ? "Adding to" : "Removing from") + " access control model \n" + ruleData;
        return message;
    }

    private static String getPolicyDataSetValueStatementByKeyQuery(String entityUri, String[] uris, String[] ids) {
        StringBuilder query = new StringBuilder();
        query.append(String.format(policyStatementByKeyTemplatePrefix, entityUri));
        for (String uri : uris) {
            query.append(String.format("  ?dataSetKeyUri ao:keyComponent <%s> . \n", uri));
        }
        int i = 0;
        for (String id : ids) {
            query.append(String.format("  ?dataSetKeyUri ao:keyComponent ?uri%d . ?uri%d ao:id \"%s\" . \n", i, i, id));
            i++;
        }
        query.append(policyStatementByKeyTemplateSuffix);
        return query.toString();
    }

    private static String getDataSetByKeyQuery(String[] uris, String[] ids) {
        StringBuilder query = new StringBuilder(policyKeyTemplatePrefix);
        for (String uri : uris) {
            query.append(String.format("  ?dataSetKeyUri ao:keyComponent <%s> . \n", uri));
        }
        int i = 0;
        for (String id : ids) {
            query.append(
                    String.format("  ?dataSetKeyUri ao:keyComponent ?uri%d .\n  ?uri%d ao:id \"%s\" . \n", i, i, id));
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
                        if (isInvalidPolicySolution(qs)) {
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

    private void loadRulesForDataSet(Set<AccessRule> rules, String dataSetUri) throws Exception {
        ParameterizedSparqlString pss = new ParameterizedSparqlString(DATASET_RULES_QUERY);
        pss.setIri("dataSet", dataSetUri);
        debug(pss.toString());
        AccessRule rule[] = new AccessRule[1];
        Exception ex[] = new Exception[1];
        try {
            rdfService.sparqlSelectQuery(pss.toString(), new ResultSetConsumer() {
                @Override
                protected void processQuerySolution(QuerySolution qs) {
                    try {
                        if (isInvalidPolicySolution(qs)) {
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
            debug("\nLoaded %s rules for %s dataset", rules.size(), dataSetUri);
        } else {
            debug("\nNo rules loaded from access control model for %s dataset.", dataSetUri);
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
        String checkUri = qs.getResource("check").getURI();
        if (ar.containsCheckUri(checkUri)) {
            CheckFactory.extendAttribute(ar.getCheck(checkUri), qs);
            return;
        }
        try {
            ar.addCheck(CheckFactory.createCheck(qs));
        } catch (Exception e) {
            log.error(e, e);
        }
    }

    private static boolean isInvalidPolicySolution(QuerySolution qs) {
        if (!qs.contains("policyUri") || !qs.get("policyUri").isResource()) {
            log.debug("Query solution doesn't contain policy uri");
            return true;
        }
        String policy = qs.get("policyUri").asResource().getURI();
        if (!qs.contains("rules") || !qs.get("rules").isResource()) {
            log.debug(String.format("Query solution for policy <%s> doesn't contain rules uri", policy));
            return true;
        }
        if (!qs.contains("rule") || !qs.get("rule").isResource()) {
            log.debug(String.format("Query solution for policy <%s> doesn't contain rule uri", policy));
            return true;
        }
        String rule = qs.get("rule").asResource().getLocalName();
        if (!qs.contains("check") || !qs.get("check").isResource()) {
            log.debug(String.format("Query solution for policy <%s> doesn't contain check uri", policy));
            return true;
        }
        String check = qs.get("check").asResource().getLocalName();
        if (!qs.contains("value")) {
            log.debug(String.format("Query solution for policy <%s> rule %s check %s doesn't contain value", policy,
                    rule, check));
            return true;
        }
        if (!qs.contains("typeId") || !qs.get("typeId").isLiteral()) {
            log.debug(String.format("Query solution for policy <%s> doesn't contain check type id", policy));
            return true;
        }
        if (!qs.contains("testId") || !qs.get("testId").isLiteral()) {
            log.debug(String.format("Query solution for policy <%s> doesn't contain check test id", policy));
            return true;
        }
        return false;
    }

    private static void debug(String template, Object... objects) {
        if (log.isDebugEnabled()) {
            log.debug(String.format(template, objects));
        }
    }

    public void addEntityToPolicyDataSet(String entityUri, AccessObjectType aot, AccessOperation og, String role) {
        modifyPolicyDataSetValue(entityUri, og, aot, role, true);
    }

    public void removeEntityFromPolicyDataSet(String entityUri, AccessObjectType aot, AccessOperation og, String role) {
        modifyPolicyDataSetValue(entityUri, og, aot, role, false);
    }

    private ChangeSet makeChangeSet() {
        ChangeSet cs = rdfService.manufactureChangeSet();
        cs.addPreChangeEvent(new BulkUpdateEvent(null, true));
        cs.addPostChangeEvent(new BulkUpdateEvent(null, false));
        return cs;
    }

    public String getDataSetUriByKey(String[] uris, String[] ids) {
        long expectedSize = uris.length + ids.length;
        final String queryText = getDataSetByKeyQuery(uris, ids);
        debug("SPARQL Query to get policy data set values:\n %s", queryText);
        String[] uri = new String[1];
        try {
            rdfService.sparqlSelectQuery(queryText, new ResultSetConsumer() {
                @Override
                protected void processQuerySolution(QuerySolution qs) {
                    if (!qs.contains("dataSet") || !qs.get("dataSet").isResource() || !qs.contains("keySize")
                            || !qs.get("keySize").isLiteral()) {
                        return;
                    }
                    long keySize = qs.getLiteral("keySize").getLong();
                    if (expectedSize != keySize) {
                        return;
                    }
                    uri[0] = qs.getResource("dataSet").getURI();
                }
            });
        } catch (RDFServiceException e) {
            log.error(e, e);
        }
        return uri[0];
    }

    public Map<String, String> getRoleDataSetTemplates() {
        Map<String, String> dataSetTemplates = new HashMap<>();
        long expectedSize = 1;
        final String queryText = DATA_SET_TEMPLATES_QUERY;
        debug("SPARQL Query to get data set templates:\n %s", queryText);
        try {
            rdfService.sparqlSelectQuery(queryText, new ResultSetConsumer() {
                @Override
                protected void processQuerySolution(QuerySolution qs) {
                    if (!qs.contains("keySize") || !qs.get("keySize").isLiteral()) {
                        return;
                    }
                    long keySize = qs.getLiteral("keySize").getLong();
                    if (expectedSize != keySize) {
                        return;
                    }
                    if (!qs.contains("dataSetTemplate") || !qs.get("dataSetTemplate").isResource()) {
                        return;
                    }
                    if (!qs.contains("dataSets") || !qs.get("dataSets").isResource()) {
                        return;
                    }
                    dataSetTemplates.put(qs.getResource("dataSetTemplate").getURI(),
                            qs.getResource("dataSets").getURI());
                }
            });
        } catch (RDFServiceException e) {
            log.error(e, e);
        }
        return dataSetTemplates;
    }

    public List<String> getDataSetKeysFromTemplate(String templateUri) {
        List<String> dataSetKeys = new LinkedList<>();
        ParameterizedSparqlString pss = new ParameterizedSparqlString(DATA_SET_KEY_TEMPLATE_QUERY);
        pss.setIri("dataSetTemplate", templateUri);
        final String queryText = pss.toString();
        debug("SPARQL Query to get data set templates:\n %s", queryText);
        try {
            rdfService.sparqlSelectQuery(queryText, new ResultSetConsumer() {
                @Override
                protected void processQuerySolution(QuerySolution qs) {
                    if (qs.contains("keyComponent") && qs.get("keyComponent").isResource()) {
                        dataSetKeys.add(qs.getResource("keyComponent").getURI());
                    }
                }
            });
        } catch (RDFServiceException e) {
            log.error(e, e);
        }
        return dataSetKeys;
    }

    public List<String> getDataSetKeyTemplatesFromTemplate(String templateUri) {
        List<String> dataSetDraftKeys = new LinkedList<>();
        ParameterizedSparqlString pss = new ParameterizedSparqlString(DATA_SET_KEY_TEMPLATES_TEMPLATE_QUERY);
        pss.setIri("dataSetTemplate", templateUri);
        final String queryText = pss.toString();
        debug("SPARQL Query to get data set templates:\n %s", queryText);
        try {
            rdfService.sparqlSelectQuery(queryText, new ResultSetConsumer() {
                @Override
                protected void processQuerySolution(QuerySolution qs) {
                    if (qs.contains("keyComponentTemplate") && qs.get("keyComponentTemplate").isResource()) {
                        dataSetDraftKeys.add(qs.getResource("keyComponentTemplate").getURI());
                    }
                }
            });
        } catch (RDFServiceException e) {
            log.error(e, e);
        }
        return dataSetDraftKeys;
    }

    public List<String> getDataSetValuesFromTemplate(String templateUri) {
        List<String> valueContainers = new LinkedList<>();
        ParameterizedSparqlString pss = new ParameterizedSparqlString(DATA_SET_VALUE_TEMPLATE_QUERY);
        pss.setIri("dataSetTemplate", templateUri);
        final String queryText = pss.toString();
        debug("SPARQL Query to get data set values from data set template:\n %s", queryText);
        try {
            rdfService.sparqlSelectQuery(queryText, new ResultSetConsumer() {
                @Override
                protected void processQuerySolution(QuerySolution qs) {
                    if (qs.contains("valueContainer") && qs.get("valueContainer").isResource()) {
                        valueContainers.add(qs.getResource("valueContainer").getURI());
                    }
                }
            });
        } catch (RDFServiceException e) {
            log.error(e, e);
        }
        return valueContainers;
    }

    public List<String> getDataSetValueTemplatesFromTemplate(String templateUri) {
        List<String> valueContainerTemplates = new LinkedList<>();
        ParameterizedSparqlString pss =
                new ParameterizedSparqlString(DATA_SET_VALUE_CONTAINER_TEMPLATES_TEMPLATE_QUERY);
        pss.setIri("dataSetTemplate", templateUri);
        final String queryText = pss.toString();
        debug("SPARQL Query to get data set value container templates from data set template:\n %s", queryText);
        try {
            rdfService.sparqlSelectQuery(queryText, new ResultSetConsumer() {
                @Override
                protected void processQuerySolution(QuerySolution qs) {
                    if (qs.contains("valueContainerTemplate") && qs.get("valueContainerTemplate").isResource()) {
                        valueContainerTemplates.add(qs.getResource("valueContainerTemplate").getURI());
                    }
                }
            });
        } catch (RDFServiceException e) {
            log.error(e, e);
        }
        return valueContainerTemplates;
    }

    public void constructValueContainer(String valueContainerTemplateUri, String valueContainer, String role,
            Model dataSetModel) {
        ParameterizedSparqlString pss = new ParameterizedSparqlString(CONSTRUCT_VALUE_CONTAINER_QUERY);
        pss.setIri("valueContainerTemplateUri", valueContainerTemplateUri);
        pss.setIri("valueContainer", valueContainer);
        pss.setIri("role", role);
        debug("SPARQL Construct Query to create value container \n %s", pss.toString());
        try {
            rdfService.sparqlConstructQuery(pss.toString(), dataSetModel);
        } catch (RDFServiceException e) {
            log.error(e, e);
        }
    }

}
