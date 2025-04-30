package edu.cornell.mannlib.vitro.webapp.auth.policy;

import static edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary.AUTH_INDIVIDUAL_PREFIX;
import static edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary.AUTH_VOCABULARY_PREFIX;
import static edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary.RDF_TYPE;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.WhichService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.adapters.VitroModelFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.impl.StatementImpl;

public class PolicyTemplateController {

    private static final Log log = LogFactory.getLog(PolicyTemplateController.class);
    public static Collection<String> createRoleDataSets(String roleUri) {

        // Execute sparql query to get all data set templates that have ao:hasTemplateKeyComponent
        // access-individual:SubjectRole .
        Set<String> datasetUris = new HashSet<>();
        Model dataSetModel = VitroModelFactory.createModel();
        Map<String, String> templates = PolicyLoader.getInstance().getRoleDataSetTemplates();
        for (String templateUri : templates.keySet()) {
            Map<String, Set<String>> ids = createRoleDataSetFromTemplate(templateUri, roleUri, templates.get(
                    templateUri), dataSetModel);
            Set<String> currentDataSets = ids.get("dataSets");
            if (currentDataSets != null) {
                datasetUris.addAll(currentDataSets);
            }
        }
        PolicyLoader.getInstance().updateAccessControlModel(dataSetModel, true);
        return datasetUris;
    }

    private static Map<String, Set<String>> createRoleDataSetFromTemplate(String dataSetTemplateUri, String roleUri,
            String dataSetsUri, Model dataSetModel) {
        Map<String, Set<String>> resourceMap = new HashMap<>();
        String role = getRoleShortName(roleUri);

        //dataSetUri might need to be collected to be used later
        String dataSetUri = getUriFromTemplate(dataSetTemplateUri, role);
        resourceMap.put("dataSets", Collections.singleton(dataSetUri));
        Set<String> valueSets = new HashSet<>();
        resourceMap.put("valueSets", valueSets);
        String dataSetKeyUri = dataSetUri + "Key";
        // TODO: Check uri doesn't exists in access control graph
        PolicyLoader policyLoader = PolicyLoader.getInstance();
        List<String> keys = policyLoader.getDataSetKeysFromTemplate(dataSetTemplateUri);
        List<String> keyTemplates = policyLoader.getDataSetKeyTemplatesFromTemplate(dataSetTemplateUri);

        for (String keyTemplate : keyTemplates) {
            if (keyTemplate.equals(AUTH_INDIVIDUAL_PREFIX + "SubjectRole")) {
                String roleKeyUri = null;
                List<String> roleValuePatterns = PolicyLoader.getInstance().getSubjectRoleValuePattern(roleUri);
                if (roleValuePatterns.isEmpty()) {
                    roleKeyUri = createSubjectRoleUri(roleUri, dataSetModel);
                } else {
                    roleKeyUri = roleValuePatterns.get(0);
                }
                keys.add(roleKeyUri);
            } else {
                log.error(String.format("Not recognized key template found '%s'", keyTemplate));
                return resourceMap;
            }
        }
        // Add ?dataSets ao:hasDataSet dataSetUri .
        dataSetModel.add(new StatementImpl(dataSetModel.createResource(dataSetsUri),
                dataSetModel.createProperty(AUTH_VOCABULARY_PREFIX + "hasDataSet"),
                dataSetModel.createResource(dataSetUri)));

        dataSetModel.add(new StatementImpl(dataSetModel.createResource(dataSetUri),
                dataSetModel.createProperty(RDF_TYPE),
                dataSetModel.createResource(AUTH_VOCABULARY_PREFIX + "PolicyDataSet")));

        dataSetModel.add(new StatementImpl(dataSetModel.createResource(dataSetUri),
                dataSetModel.createProperty(AUTH_VOCABULARY_PREFIX + "hasDataSetKey"),
                dataSetModel.createResource(dataSetKeyUri)));

        dataSetModel.add(new StatementImpl(dataSetModel.createResource(dataSetKeyUri),
                dataSetModel.createProperty(RDF_TYPE),
                dataSetModel.createResource(AUTH_VOCABULARY_PREFIX + "DataSetKey")));

        for (String key : keys) {
            dataSetModel.add(new StatementImpl(dataSetModel.createResource(dataSetKeyUri),
                    dataSetModel.createProperty(AUTH_VOCABULARY_PREFIX + "hasKeyComponent"),
                    dataSetModel.createResource(key)));
        }

        List<String> valueSetUris = policyLoader.getDataSetValuesFromTemplate(dataSetTemplateUri);
        for (String valueSetUri : valueSetUris) {
            // Add ?dataSetUri ao:hasRelatedValueSet ?valueSetUri .
            dataSetModel.add(new StatementImpl(dataSetModel.createResource(dataSetUri),
                    dataSetModel.createProperty(AUTH_VOCABULARY_PREFIX + "hasRelatedValueSet"),
                    dataSetModel.createResource(valueSetUri)));
        }

        List<String> valueSetTemplateUris = policyLoader.getDataSetValueTemplatesFromTemplate(dataSetTemplateUri);

        for (String valueSetTemplateUri : valueSetTemplateUris) {
            String valueSetUri = getUriFromTemplate(valueSetTemplateUri, role);
            valueSets.add(valueSetUri);
            dataSetModel.add(new StatementImpl(dataSetModel.createResource(dataSetUri),
                    dataSetModel.createProperty(AUTH_VOCABULARY_PREFIX + "hasRelatedValueSet"),
                    dataSetModel.createResource(valueSetUri)));

            policyLoader.constructValueSet(valueSetTemplateUri, valueSetUri, roleUri, dataSetModel);
            // TODO: Check uri doesn't exists in access control graph
            // If value set template is subject role, then role uri should be added to the set
        }
        return resourceMap;
    }

    public static String createSubjectRoleUri(String roleUri, Model dataSetModel) {
        String roleShortName = getRoleShortName(roleUri);
        String roleValueSetUri = AUTH_INDIVIDUAL_PREFIX + roleShortName + "RoleUri";
        dataSetModel.add(new StatementImpl(dataSetModel.createResource(roleValueSetUri),
                dataSetModel.createProperty(AUTH_VOCABULARY_PREFIX + "id"),
                dataSetModel.createLiteral(roleUri)));
        dataSetModel.add(new StatementImpl(dataSetModel.createResource(roleValueSetUri),
                dataSetModel.createProperty(RDF_TYPE),
                dataSetModel.createResource(AUTH_VOCABULARY_PREFIX + "SubjectRoleUri")));
        return roleValueSetUri;
    }

    private static String getRoleShortName(String roleUri) {
        String slashSplit = roleUri.substring(roleUri.lastIndexOf('/') + 1);
        String hashSplit = slashSplit.substring(slashSplit.lastIndexOf('#') + 1);
        return Character.toUpperCase(hashSplit.charAt(0)) + hashSplit.substring(1).toLowerCase();
    }

    private static String getUriFromTemplate(String templateUri, String name) {
        String templatePrefix = templateUri.substring(0, templateUri.lastIndexOf('/') + 1);
        String templateName = templateUri.substring(templateUri.lastIndexOf('/') + 1);
        int index = templateName.lastIndexOf("Template");
        if (templateName.endsWith("Template") && index > 0) {
            templateName = templateName.substring(0, index);
        }
        return templatePrefix + name + templateName;
    }

    public static void removeRoleDataSets(String role) {
        Model model = VitroModelFactory.createModel();
        Map<String, String> templates = PolicyLoader.getInstance().getRoleDataSetTemplates();
        for (String template : templates.keySet()) {
            Map<String, Set<String>> ids = createRoleDataSetFromTemplate(template, role, templates.get(template),
                    model);
            addTemplateValueSetValues(model, ids.get("valueSets"));
            Set<String> dataSets = ids.get("dataSets");
            if (!dataSets.isEmpty()) {
                PolicyStore.getInstance().remove(dataSets.iterator().next());
            }
        }
        PolicyLoader.getInstance().updateAccessControlModel(model, false);
    }

    public static final String VALUE_SET_VALUES_QUERY = ""
            + "prefix access: <https://vivoweb.org/ontology/vitro-application/auth/vocabulary/>\n"
            + "CONSTRUCT {\n"
            + "  ?valueSetUri access:value ?dataValue .\n"
            + "}\n"
            + "WHERE {\n"
            + "  GRAPH <http://vitro.mannlib.cornell.edu/default/access-control> {\n"
            + "    ?valueSetUri access:value ?dataValue .\n"
            + "  }"
            + "}\n";

    private static void addTemplateValueSetValues(Model model, Set<String> valueSets) {
        RDFService rdfService = ModelAccess.getInstance().getRDFService(WhichService.CONFIGURATION);
        for (String valueSet : valueSets) {
            ParameterizedSparqlString pss = new ParameterizedSparqlString(VALUE_SET_VALUES_QUERY);
            pss.setIri("valueSetUri", valueSet);
            try {
                rdfService.sparqlConstructQuery(pss.toString(), model);
            } catch (RDFServiceException e) {
                log.error(e, e);
            }
        }
    }

}
