package edu.cornell.mannlib.vitro.webapp.auth.policy;

import java.util.List;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.rdfservice.adapters.VitroModelFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.impl.StatementImpl;

public class PolicyTemplateController {

    private static final Log log = LogFactory.getLog(PolicyTemplateController.class);
    private static final String PREFIX_AO = "https://vivoweb.org/ontology/vitro-application/auth/vocabulary/";

    public static void createRoleDataSets(String roleUri) {

        // Execute sparql query to get all data set templates that have ao:templateKey access-individual:SubjectRole .
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
        for (String keyTemplate : keyTemplates) {
            if (keyTemplate.equals("https://vivoweb.org/ontology/vitro-application/auth/individual/SubjectRole")) {
                keys.add(roleUri);
            } else {
                log.error(String.format("Not recognized key template found '%s'", keyTemplate));
                return;
            }
        }
        Model dataSetModel = VitroModelFactory.createModel();
        // Add ?dataSets ao:policyDataSet dataSetUri .
        dataSetModel.add(new StatementImpl(dataSetModel.createResource(dataSetsUri),
                dataSetModel.createProperty(PREFIX_AO + "policyDataSet"), dataSetModel.createResource(dataSetUri)));

        dataSetModel.add(new StatementImpl(dataSetModel.createResource(dataSetUri),
                dataSetModel.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                dataSetModel.createResource(PREFIX_AO + "PolicyDataSet")));

        dataSetModel.add(new StatementImpl(dataSetModel.createResource(dataSetUri),
                dataSetModel.createProperty(PREFIX_AO + "dataSetKey"), dataSetModel.createResource(dataSetKeyUri)));

        for (String key : keys) {
            dataSetModel.add(new StatementImpl(dataSetModel.createResource(dataSetKeyUri),
                    dataSetModel.createProperty(PREFIX_AO + "keyComponent"), dataSetModel.createResource(key)));
        }

        List<String> valueSetUris = policyLoader.getDataSetValuesFromTemplate(dataSetTemplateUri);
        for (String valueSetUri : valueSetUris) {
            // Add ?dataSetUri ao:dataSetValues ?valueSetUri .
            dataSetModel.add(new StatementImpl(dataSetModel.createResource(dataSetUri),
                    dataSetModel.createProperty(PREFIX_AO + "dataSetValues"),
                    dataSetModel.createResource(valueSetUri)));
        }

        List<String> valueSetTemplateUris = policyLoader.getDataSetValueTemplatesFromTemplate(dataSetTemplateUri);

        for (String valueSetTemplateUri : valueSetTemplateUris) {
            String valueSetUri = getUriFromTemplate(valueSetTemplateUri, role);
            dataSetModel.add(new StatementImpl(dataSetModel.createResource(dataSetUri),
                    dataSetModel.createProperty(PREFIX_AO + "dataSetValues"),
                    dataSetModel.createResource(valueSetUri)));

            policyLoader.constructValueSet(valueSetTemplateUri, valueSetUri, roleUri, dataSetModel);
            // TODO: Check uri doesn't exists in access control graph
            // If value set template is subject role, then role uri should be added to the set
        }
        policyLoader.updateAccessControlModel(dataSetModel, true);
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
