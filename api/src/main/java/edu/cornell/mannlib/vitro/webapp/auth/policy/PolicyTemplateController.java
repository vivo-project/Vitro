package edu.cornell.mannlib.vitro.webapp.auth.policy;

import static edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary.AUTH_INDIVIDUAL_PREFIX;
import static edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary.AUTH_VOCABULARY_PREFIX;
import static edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary.RDF_TYPE;

import java.util.List;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.rdfservice.adapters.VitroModelFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.impl.StatementImpl;

public class PolicyTemplateController {

    private static final Log log = LogFactory.getLog(PolicyTemplateController.class);
    public static void createRoleDataSets(String roleUri) {

        // Execute sparql query to get all data set templates that have ao:hasTemplateKeyComponent
        // access-individual:SubjectRole .
        Map<String, String> templates = PolicyLoader.getInstance().getRoleDataSetTemplates();
        for (String templateUri : templates.keySet()) {
            createRoleDataSet(templateUri, roleUri, templates.get(templateUri));
        }
    }

    private static void createRoleDataSet(String dataSetTemplateUri, String roleUri, String dataSetsUri) {
        String role = getRoleShortName(roleUri);
        String dataSetUri = getUriFromTemplate(dataSetTemplateUri, role);
        String dataSetKeyUri = dataSetUri + "Key";
        // TODO: Check uri doesn't exists in access control graph
        PolicyLoader policyLoader = PolicyLoader.getInstance();
        List<String> keys = policyLoader.getDataSetKeysFromTemplate(dataSetTemplateUri);
        List<String> keyTemplates = policyLoader.getDataSetKeyTemplatesFromTemplate(dataSetTemplateUri);

        Model dataSetModel = VitroModelFactory.createModel();

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
                return;
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
            dataSetModel.add(new StatementImpl(dataSetModel.createResource(dataSetUri),
                    dataSetModel.createProperty(AUTH_VOCABULARY_PREFIX + "hasRelatedValueSet"),
                    dataSetModel.createResource(valueSetUri)));

            policyLoader.constructValueSet(valueSetTemplateUri, valueSetUri, roleUri, dataSetModel);
            // TODO: Check uri doesn't exists in access control graph
            // If value set template is subject role, then role uri should be added to the set
        }
        policyLoader.updateAccessControlModel(dataSetModel, true);
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

}
