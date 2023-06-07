/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.migration.auth;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ContextModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.*;
import org.apache.jena.shared.Lock;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.HashMap;
import java.util.Map;

import static edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary.vitroURI;

public class AuthMigrator implements ServletContextListener { 
    private static final String PREFIX = "http://vitro.mannlib.cornell.edu/ns/vitro/role#";

    private static final Log log = LogFactory.getLog(AuthMigrator.class);

    /**
     * Old ROLE defintions that were used by the hidden / prohibited assertions
     */
    private static final String ROLE_PUBLIC = PREFIX + "public";
    private static final String ROLE_SELF = PREFIX + "selfEditor";
    private static final String ROLE_EDITOR = PREFIX + "editor";
    private static final String ROLE_CURATOR = PREFIX + "curator";
    private static final String ROLE_DB_ADMIN = PREFIX + "dbAdmin";
    private static final String ROLE_NOBODY = PREFIX + "nobody";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        long begin = System.currentTimeMillis();

        ServletContext ctx = sce.getServletContext();
        StartupStatus ss = StartupStatus.getBean(ctx);
        ContextModelAccess models = ModelAccess.getInstance();

        // Get assertions from content store
        Model roleAuthContentModel = constructRoleAuthContentModel(models);

        // Get assertions from configuration store
        Model roleAuthDisplayModel = constructRoleAuthDisplayModel(models);

        if ((roleAuthContentModel != null && !roleAuthContentModel.isEmpty()) ||
                (roleAuthDisplayModel != null && !roleAuthDisplayModel.isEmpty()) ) {
            Map<String, Map<String, String>> actionToRoleAndPropertySetMap = new HashMap<>();

            Map<String, String> displayRoleToPropertySetMap = new HashMap<>();
            Map<String, String> updateRoleToPropertySetMap = new HashMap<>();
            Map<String, String> publishRoleToPropertySetMap = new HashMap<>();

            // Map the roles to equivalent display permissions
            displayRoleToPropertySetMap.put(ROLE_PUBLIC,   AuthMigrator.getClassUri(AccessOperation.DISPLAY) + "#PUBLIC");
            displayRoleToPropertySetMap.put(ROLE_SELF,     AuthMigrator.getClassUri(AccessOperation.DISPLAY) + "#SELF_EDITOR");
            displayRoleToPropertySetMap.put(ROLE_EDITOR,   AuthMigrator.getClassUri(AccessOperation.DISPLAY) + "#EDITOR");
            displayRoleToPropertySetMap.put(ROLE_CURATOR,  AuthMigrator.getClassUri(AccessOperation.DISPLAY) + "#CURATOR");
            displayRoleToPropertySetMap.put(ROLE_DB_ADMIN, AuthMigrator.getClassUri(AccessOperation.DISPLAY) + "#ADMIN");

            // Map the roles to equivalent update permissions
            updateRoleToPropertySetMap.put(ROLE_PUBLIC,   AuthMigrator.getClassUri(AccessOperation.UPDATE) + "#PUBLIC");
            updateRoleToPropertySetMap.put(ROLE_SELF,     AuthMigrator.getClassUri(AccessOperation.UPDATE) + "#SELF_EDITOR");
            updateRoleToPropertySetMap.put(ROLE_EDITOR,   AuthMigrator.getClassUri(AccessOperation.UPDATE) + "#EDITOR");
            updateRoleToPropertySetMap.put(ROLE_CURATOR,  AuthMigrator.getClassUri(AccessOperation.UPDATE) + "#CURATOR");
            updateRoleToPropertySetMap.put(ROLE_DB_ADMIN, AuthMigrator.getClassUri(AccessOperation.UPDATE) + "#ADMIN");

            // Map the roles to equivalent publish permissions
            publishRoleToPropertySetMap.put(ROLE_PUBLIC,   AuthMigrator.getClassUri(AccessOperation.PUBLISH) + "#PUBLIC");
            publishRoleToPropertySetMap.put(ROLE_SELF,     AuthMigrator.getClassUri(AccessOperation.PUBLISH) + "#SELF_EDITOR");
            publishRoleToPropertySetMap.put(ROLE_EDITOR,   AuthMigrator.getClassUri(AccessOperation.PUBLISH) + "#EDITOR");
            publishRoleToPropertySetMap.put(ROLE_CURATOR,  AuthMigrator.getClassUri(AccessOperation.PUBLISH) + "#CURATOR");
            publishRoleToPropertySetMap.put(ROLE_DB_ADMIN, AuthMigrator.getClassUri(AccessOperation.PUBLISH) + "#ADMIN");

            // Create a map between the permission and the appropriate sets
            actionToRoleAndPropertySetMap.put(vitroURI + "hiddenFromDisplayBelowRoleLevelAnnot",    displayRoleToPropertySetMap);
            actionToRoleAndPropertySetMap.put(vitroURI + "prohibitedFromUpdateBelowRoleLevelAnnot", updateRoleToPropertySetMap);
            actionToRoleAndPropertySetMap.put(vitroURI + "hiddenFromPublishBelowRoleLevelAnnot",    publishRoleToPropertySetMap);

            // Convert the assertions retrieved from the content and display models to the assertions in the new permission sets
            OntModel permissionsModel = convertRoleAuthsToPermissions(roleAuthContentModel, roleAuthDisplayModel, actionToRoleAndPropertySetMap);

            // Store the new permissions in the user accounts model
            OntModel accountsModel = models.getOntModel(ModelNames.USER_ACCOUNTS);
            accountsModel.add(permissionsModel);

            // Get assertions from content store
            removeRoleAuthContentModel(models, roleAuthContentModel);

            // Get assertions from configuration store
            removeRoleAuthDisplayModel(models, roleAuthDisplayModel);
        }

        ss.info(this, secondsSince(begin) + " seconds to migrate auth models");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Nothing to tear down.
    }

    private long secondsSince(long startTime) {
        return (System.currentTimeMillis() - startTime) / 1000;
    }

    private Model constructRoleAuthContentModel(ContextModelAccess models) {
        RDFService rdfService = models.getRDFService();
        Model constructedModel = ModelFactory.createDefaultModel();

        String construct = "PREFIX vitro: <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#> \n" +
                "CONSTRUCT \n" +
                "{\n" +
                "   ?displaySubj vitro:hiddenFromDisplayBelowRoleLevelAnnot ?displayObj . \n" +
                "   ?updateSubj vitro:prohibitedFromUpdateBelowRoleLevelAnnot ?updateObj . \n" +
                "   ?publishSubj vitro:hiddenFromPublishBelowRoleLevelAnnot ?publishObj . \n" +
                "} WHERE { \n" +
                "   { \n" +
                "       ?displaySubj vitro:hiddenFromDisplayBelowRoleLevelAnnot ?displayObj . \n" +
                "   } UNION { \n" +
                "       ?updateSubj vitro:prohibitedFromUpdateBelowRoleLevelAnnot ?updateObj . \n" +
                "   } UNION { \n" +
                "       ?publishSubj vitro:hiddenFromPublishBelowRoleLevelAnnot ?publishObj . \n" +
                "   }\n" +
                "}\n";

        try {
            rdfService.sparqlConstructQuery(construct, constructedModel);
        } catch (RDFServiceException e) {
            return null;
        }

        return constructedModel;
    }

    private Model constructRoleAuthDisplayModel(ContextModelAccess models) {
        OntModel displayModel = models.getOntModel(ModelNames.DISPLAY);

        Model constructedModel = ModelFactory.createDefaultModel();

        displayModel.enterCriticalSection(Lock.READ);
        try {
            constructedModel.add(displayModel.listStatements(null, displayModel.getProperty(vitroURI + "hiddenFromDisplayBelowRoleLevelAnnot"), (RDFNode) null));
            constructedModel.add(displayModel.listStatements(null, displayModel.getProperty(vitroURI + "prohibitedFromUpdateBelowRoleLevelAnnot"), (RDFNode) null));
            constructedModel.add(displayModel.listStatements(null, displayModel.getProperty(vitroURI + "hiddenFromPublishBelowRoleLevelAnnot"), (RDFNode) null));
        } finally {
            displayModel.leaveCriticalSection();
        }

        return constructedModel;
    }

    private void removeRoleAuthContentModel(ContextModelAccess models, Model roleAuthContentModel) {
        OntModel tboxModel = models.getOntModelSelector().getTBoxModel();
        tboxModel.enterCriticalSection(Lock.WRITE);

        try {
            tboxModel.remove(roleAuthContentModel);
        } finally {
            tboxModel.leaveCriticalSection();
        }
    }

    private void removeRoleAuthDisplayModel(ContextModelAccess models, Model roleAuthDisplayModel) {
        OntModel displayModel = models.getOntModel(ModelNames.DISPLAY);

        displayModel.enterCriticalSection(Lock.WRITE);
        try {
            displayModel.remove(roleAuthDisplayModel);
        } finally {
            displayModel.leaveCriticalSection();
        }
    }

    private OntModel convertRoleAuthsToPermissions(Model roleAuthContentModel, Model roleAuthDisplayModel, Map<String, Map<String, String>> actionToRoleAndPropertySetMap) {
        OntModel permissionsModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

        if (roleAuthContentModel != null && !roleAuthContentModel.isEmpty()) {
            convertModel(permissionsModel, roleAuthContentModel, actionToRoleAndPropertySetMap);
        }

        if (roleAuthDisplayModel != null && !roleAuthDisplayModel.isEmpty()) {
            convertModel(permissionsModel, roleAuthDisplayModel, actionToRoleAndPropertySetMap);
        }

        return permissionsModel;
    }

    private void convertModel(OntModel permissionsModel, Model roleAuthModel, Map<String, Map<String, String>> actionToRoleAndPropertySetMap) {
        StmtIterator iterator = roleAuthModel.listStatements();

        while (iterator.hasNext()) {
            Statement stmt = iterator.next();

            Map<String, String> roleToPropertySetUriMap = null;

            Property property = stmt.getPredicate();
            roleToPropertySetUriMap = actionToRoleAndPropertySetMap.get(property.getURI());
            if (roleToPropertySetUriMap != null) {
                Resource subject = stmt.getSubject();

                if (subject != null && subject.isURIResource()) {
                    RDFNode objectNode = stmt.getObject();
                    if (objectNode.isResource()) {
                        String role = ((Resource) objectNode).getURI();
                        switch (role) {
                            case ROLE_PUBLIC:
                                addPermissionToModel(permissionsModel, roleToPropertySetUriMap, ROLE_PUBLIC, subject.getURI());

                                // Fall through to grant additional rights

                            case ROLE_SELF:
                                addPermissionToModel(permissionsModel, roleToPropertySetUriMap, ROLE_SELF, subject.getURI());

                                // Fall through to grant additional rights

                            case ROLE_EDITOR:
                                addPermissionToModel(permissionsModel, roleToPropertySetUriMap, ROLE_EDITOR, subject.getURI());

                                // Fall through to grant additional rights

                            case ROLE_CURATOR:
                                addPermissionToModel(permissionsModel, roleToPropertySetUriMap, ROLE_CURATOR, subject.getURI());

                                // Fall through to grant additional rights

                            case ROLE_DB_ADMIN:
                                addPermissionToModel(permissionsModel, roleToPropertySetUriMap, ROLE_DB_ADMIN, subject.getURI());

                                // Fall through to grant additional rights

                            case ROLE_NOBODY:
                                break;

                            default:
                                log.warn("Unknown role <" + property.getURI() + ">");
                                break;
                        }
                    } else {
                        log.warn("Object node is not a resource");
                    }
                } else {
                    log.warn("Can't process this resource");
                }
            }
        }
    }

    private void addPermissionToModel(OntModel permissionsModel, Map<String, String> roleToPropertySetUriMap, String roleUri, String resourceUri) {
        if (roleToPropertySetUriMap.containsKey(roleUri)) {
            permissionsModel.add(
                    permissionsModel.createResource(roleToPropertySetUriMap.get(roleUri)),
                    permissionsModel.createProperty(VitroVocabulary.PERMISSION_FOR_ENTITY),
                    permissionsModel.createResource(resourceUri)
            );
        }
    }

    public static String getClassUri(AccessOperation action) {
        final String actionStr = action.toString();
        return "java:edu.cornell.mannlib.vitro.webapp.auth.permissions.Entity" + actionStr.substring(0,1).toUpperCase() + actionStr.substring(1).toLowerCase() + "Permission";
    }
}
