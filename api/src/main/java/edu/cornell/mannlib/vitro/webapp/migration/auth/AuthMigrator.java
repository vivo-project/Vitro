/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.migration.auth;

import static edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary.ROLE_ADMIN_URI;
import static edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary.ROLE_CURATOR_URI;
import static edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary.ROLE_EDITOR_URI;
import static edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary.ROLE_PUBLIC_URI;
import static edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary.ROLE_SELF_EDITOR_URI;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyLoader;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ContextModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.WhichService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

public class AuthMigrator implements ServletContextListener {

    private static final long CURRENT_VERSION = 2;
    private static final Log log = LogFactory.getLog(AuthMigrator.class);
    protected static final Set<String> ALL_ROLES = new HashSet<String>(
            Arrays.asList(ROLE_ADMIN_URI, ROLE_CURATOR_URI, ROLE_EDITOR_URI, ROLE_SELF_EDITOR_URI, ROLE_PUBLIC_URI));

    private static final String SET_VERSION_TEMPLATE = ""
            + "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n"
            + "@prefix access: <https://vivoweb.org/ontology/vitro-application/auth/vocabulary/> .\n"
            + "<https://vivoweb.org/ontology/vitro-application/auth/individual/Configuration> "
            + "rdf:type access:Configuration ;\n"
            + "access:version ?version .";

    private static final String REMOVE_VERSION_TEMPLATE = ""
            + "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n"
            + "<https://vivoweb.org/ontology/vitro-application/auth/individual/Configuration> "
            + "<https://vivoweb.org/ontology/vitro-application/auth/vocabulary/version> ?version .";

    private static String VERSION_QUERY = ""
            + "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
            + "prefix access: <https://vivoweb.org/ontology/vitro-application/auth/vocabulary/>\n"
            + "SELECT ?version \n"
            + "WHERE {\n"
            + "  GRAPH <http://vitro.mannlib.cornell.edu/default/access-control> {\n"
            + "       ?configuration rdf:type access:Configuration .\n"
            + "       ?configuration access:version ?version .\n"
            + "  }\n"
            + "}";

    private RDFService contentRdfService;
    private RDFService configurationRdfService;
    private ContextModelAccess modelAccess;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        long begin = System.currentTimeMillis();
        modelAccess = ModelAccess.getInstance();
        initialize(modelAccess.getRDFService(WhichService.CONTENT),
                modelAccess.getRDFService(WhichService.CONFIGURATION));
        if (!isMigrationRequired()) {
            return;
        }
        long currentVersion = getVersion();
        if (currentVersion == 0) {
            runCompleteMigration(sce, begin);
        } else if (currentVersion == 1) {
            migratePublishPublicPermissions(sce, begin);
        }
    }

    private void migratePublishPublicPermissions(ServletContextEvent sce, long begin) {
        ServletContext ctx = sce.getServletContext();
        StartupStatus ss = StartupStatus.getBean(ctx);
        log.info("Started publish permissions authorization reconfiguration for public role");
        convertPublicPublishPermissions();
        ss.info(this, secondsSince(begin) + " seconds spent to reconfigure publish permissions for public role");
        removeVersion(getVersion());
        setVersion(CURRENT_VERSION);
        log.info(String.format("Updated access control configuration to version %d", CURRENT_VERSION));
        PolicyLoader.getInstance().loadPolicies();
        log.info("Reloaded all policies after migration");
    }

    private void convertPublicPublishPermissions() {
        AnnotationMigrator annotationMigrator = new AnnotationMigrator(contentRdfService, configurationRdfService);
        annotationMigrator.updatePublicPublishPermissions();
    }

    private void runCompleteMigration(ServletContextEvent sce, long begin) {
        ServletContext ctx = sce.getServletContext();
        StartupStatus ss = StartupStatus.getBean(ctx);
        log.info("Started authorization configuration update");
        convertAuthorizationConfiguration();
        log.info("Finished authorization configuration update");
        ss.info(this, secondsSince(begin) + " seconds to migrate auth models");
        log.info("Reload all policies after migration");
        PolicyLoader.getInstance().loadPolicies();
    }

    protected void initialize(RDFService content, RDFService configuration) {
        contentRdfService = content;
        configurationRdfService = configuration;
    }

    protected void convertAuthorizationConfiguration() {
        OntModel userAccountsModel = modelAccess.getOntModelSelector().getUserAccountsModel();
        ArmMigrator armMigrator = new ArmMigrator(contentRdfService, configurationRdfService, userAccountsModel);
        if (armMigrator.isArmConfiguation()) {
            armMigrator.migrateConfiguration();
        } else {
            migrateAnnotationConfiguation();
        }
        migrateSimplePermissions();
        removeVersion(getVersion());
        setVersion(CURRENT_VERSION);
        log.info(String.format("Updated access control configuration to version %d", CURRENT_VERSION));
    }

    private void migrateSimplePermissions() {
        OntModel userAccountsModel = modelAccess.getOntModelSelector().getUserAccountsModel();
        SimplePermissionMigrator spm = new SimplePermissionMigrator(userAccountsModel);
        spm.migrateConfiguration();
    }

    private void migrateAnnotationConfiguation() {
        AnnotationMigrator annotationMigrator = new AnnotationMigrator(contentRdfService, configurationRdfService);
        annotationMigrator.migrateConfiguration();
    }

    private boolean isMigrationRequired() {
        if (getVersion() < CURRENT_VERSION) {
            return true;
        }
        return false;
    }

    protected long getVersion() {
        long version = 0L;
        try {
            ResultSet rs = RDFServiceUtils.sparqlSelectQuery(VERSION_QUERY, configurationRdfService);
            while (rs.hasNext()) {
                QuerySolution qs = rs.next();
                if (!qs.contains("version") || !qs.get("version").isLiteral()) {
                    continue;
                }
                version = qs.getLiteral("version").getLong();
            }
        } catch (Exception e) {
            log.error(e, e);
        }
        return version;
    }

    protected void removeVersion(long version) {
        ParameterizedSparqlString pss = new ParameterizedSparqlString(REMOVE_VERSION_TEMPLATE);
        pss.setLiteral("version", version);
        PolicyLoader.getInstance().updateAccessControlModel(pss.toString(), false);
    }

    protected void setVersion(long version) {
        ParameterizedSparqlString pss = new ParameterizedSparqlString(SET_VERSION_TEMPLATE);
        pss.setLiteral("version", version);
        PolicyLoader.getInstance().updateAccessControlModel(pss.toString(), true);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Nothing to tear down.
    }

    private long secondsSince(long startTime) {
        return (System.currentTimeMillis() - startTime) / 1000;
    }
}
