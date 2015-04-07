/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.migration.rel18;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.WhichService.CONFIGURATION;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.WhichService.CONTENT;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.WhichService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.RDFServiceJena;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

/**
 * The graphs http://vitro.mannlib.cornell.edu/ns/vitro/sdb/metadata were used
 * in Content models through release 1.7 and in Configuration models in release
 * 1.7
 * 
 * In release 1.8, they area no longer used, and produce annoying warning
 * messages.
 */
public class RemoveObsoleteMetadataGraphs {
	private static final String OBSOLETE_METADATA_MODEL = "http://vitro.mannlib.cornell.edu/ns/vitro/sdb/metadata";

	private final ServletContext ctx;
	private final ServletContextListener parent;

	public RemoveObsoleteMetadataGraphs(ServletContext ctx,
			ServletContextListener parent) {
		this.ctx = ctx;
		this.parent = parent;
	}

	public void migrate() {
		StartupStatus ss = StartupStatus.getBean(ctx);
		removeMetadataModel(ctx, ss, CONTENT);
		removeMetadataModel(ctx, ss, CONFIGURATION);
	}

	/**
	 * Ordinarily, RDFServiceJena will issue a warning when a single triple is
	 * removed from a blank node. In this case, however, that's exactly what we
	 * want, so stifle those warnings while we remove these models.
	 */
	private void removeMetadataModel(ServletContext ctx, StartupStatus ss,
			WhichService which) {
		Logger rdfServiceLogger = Logger.getLogger(RDFServiceJena.class);
		Level rdfServiceLogLevel = rdfServiceLogger.getLevel();

		rdfServiceLogger.setLevel(Level.ERROR);
		try {
			ModelAccess.on(ctx).getModelMaker(which)
					.removeModel(OBSOLETE_METADATA_MODEL);
		} catch (Exception e) {
			ss.warning(parent, "Failed to remove '" + OBSOLETE_METADATA_MODEL
					+ "' from " + which, e);
		} finally {
			rdfServiceLogger.setLevel(rdfServiceLogLevel);
		}
	}

}
