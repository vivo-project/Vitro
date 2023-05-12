/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.audit;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.audit.storage.AuditDAOFactory;
import edu.cornell.mannlib.vitro.webapp.audit.storage.AuditDAOTDB;
import edu.cornell.mannlib.vitro.webapp.modules.Application;
import edu.cornell.mannlib.vitro.webapp.modules.ComponentStartupStatus;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

import java.nio.file.Path;

/**
 * Implementation of the AuditModule that uses Jena TDB storage
 *
 * Configure this in applicationSetup.n3 to enable the Audit module
 */
public class TDBAuditModule implements AuditModule {
    // The path for the tdb model
    String tdbPath;

    /**
     * TDB path configuration property set by the bean loader
     *
     * @param path
     */
    @Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#hasTdbDirectory", minOccurs = 1, maxOccurs = 1)
    public void setTdbPath(String path) {
        tdbPath = path;
    }

    @Override
    public void startup(Application application, ComponentStartupStatus ss) {
        // Get the home directory
        Path vitroHome = ApplicationUtils.instance().getHomeDirectory().getPath();

        // Resolve the auidt tdb store path against the home directory
        String resolvedPath = vitroHome.resolve(tdbPath).toString();

        // Initialize the TDB DAO with the directory
        AuditDAOTDB.initialize(resolvedPath);

        // Initialize the DAO factory to use TDB
        AuditDAOFactory.initialize(AuditDAOFactory.Storage.AUDIT_TDB);
    }

    @Override
    public void shutdown(Application application) {
        // Clean up the Audit DAO TDB
        AuditDAOTDB.shutdown();
    }
}
