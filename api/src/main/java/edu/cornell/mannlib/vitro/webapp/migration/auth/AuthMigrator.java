/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.migration.auth;

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

    private static final Log log = LogFactory.getLog(AuthMigrator.class);
    protected static final String ROLE_ADMIN_URI = "http://vitro.mannlib.cornell.edu/ns/vitro/authorization#ADMIN";
    protected static final String ROLE_CURATOR_URI = "http://vitro.mannlib.cornell.edu/ns/vitro/authorization#CURATOR";
    protected static final String ROLE_EDITOR_URI = "http://vitro.mannlib.cornell.edu/ns/vitro/authorization#EDITOR";
    protected static final String ROLE_SELF_EDITOR_URI = "http://vitro.mannlib.cornell.edu/ns/vitro/authorization#SELF_EDITOR";
    protected static final String ROLE_PUBLIC_URI = "http://vitro.mannlib.cornell.edu/ns/vitro/authorization#PUBLIC";
    protected static final Set<String> ALL_ROLES = new HashSet<String>(
            Arrays.asList(ROLE_ADMIN_URI, ROLE_CURATOR_URI, ROLE_EDITOR_URI, ROLE_SELF_EDITOR_URI, ROLE_PUBLIC_URI));

    private static final String SET_VERSION_TEMPLATE = ""
            + "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n"
            + "@prefix ao: <https://vivoweb.org/ontology/vitro-application/auth/vocabulary/> .\n"
            + "<https://vivoweb.org/ontology/vitro-application/auth/individual/Configuration> "
            + "rdf:type ao:Configuration ;\n"
            + "ao:version ?version .";

    private static final String REMOVE_VERSION_TEMPLATE = ""
            + "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n"
            + "<https://vivoweb.org/ontology/vitro-application/auth/individual/Configuration> "
            + "<https://vivoweb.org/ontology/vitro-application/auth/vocabulary/version> ?version .";

    private static String VERSION_QUERY = ""
            + "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
            + "prefix ao: <https://vivoweb.org/ontology/vitro-application/auth/vocabulary/>\n"
            + "SELECT ?version \n"
            + "WHERE {\n"
            + "  GRAPH <http://vitro.mannlib.cornell.edu/default/access-control> {\n"
            + "       ?configuration rdf:type ao:Configuration .\n"
            + "       ?configuration ao:version ?version .\n"
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
        if (isArmConfiguration()) {
            migrateArmConfiguration();
        } else {
            migrateAnnotationConfiguation();
        }
        migrateSimplePermissions();
        removeVersion(getVersion());
        setVersion(1L);
    }

    private void migrateSimplePermissions() {
        OntModel userAccountsModel = modelAccess.getOntModelSelector().getUserAccountsModel();
        SimplePermissionMigrator spm = new SimplePermissionMigrator(userAccountsModel);
        spm.migrateConfiguration();
    }

    private void migrateArmConfiguration() {
        OntModel userAccountsModel = modelAccess.getOntModelSelector().getUserAccountsModel();
        ArmMigrator armMigrator = new ArmMigrator(contentRdfService, configurationRdfService, userAccountsModel);
        armMigrator.migrateConfiguration();
    }

    private void migrateAnnotationConfiguation() {
        AnnotationMigrator annotationMigrator = new AnnotationMigrator(contentRdfService, configurationRdfService);
        annotationMigrator.migrateConfiguration();
    }

    private boolean isMigrationRequired() {
        if (getVersion() == 0L) {
            return true;
        }
        return false;
    }

    private boolean isArmConfiguration() {
        OntModel userAccountsModel = modelAccess.getOntModelSelector().getUserAccountsModel();
        ArmMigrator armMigrator = new ArmMigrator(contentRdfService, configurationRdfService, userAccountsModel);
        return armMigrator.isArmConfiguation();
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
