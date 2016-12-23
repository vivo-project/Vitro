/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.migration.rel18;

import static org.apache.jena.rdf.model.ResourceFactory.createPlainLiteral;
import static org.apache.jena.rdf.model.ResourceFactory.createProperty;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;
import static edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary.vitroURI;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;
import edu.cornell.mannlib.vitro.webapp.utils.jena.criticalsection.LockableOntModel;
import edu.cornell.mannlib.vitro.webapp.utils.jena.criticalsection.LockedOntModel;

/**
 * PropertyConfig.n3 has moved to rdf/display/firsttime, so it is only pulled in
 * if the display model is empty.
 * 
 * Let's pull it in one time, anyway.
 * 
 * Check askUpdated.sparql for an example of how we know that it is necessary.
 * Also check success.n3
 * 
 * If a special triple is not found in the display model, read the file from
 * firsttime. If we can't find the file, warn
 */
public class FauxPropertiesUpdater {
	private static final String[] PATH_TO_PROPERTY_CONFIG = { "rdf", "display",
			"firsttime", "PropertyConfig.n3" };

	private static final Resource DISPLAY_MODEL = createResource(ModelNames.DISPLAY);
	private static final Property UPDATED_DISPLAY_MODEL = createProperty(vitroURI
			+ "updatedDisplayModel");
	private static final Literal VERSION_1_8 = createPlainLiteral("1.8");

	private final ServletContextListener parent;
	private final StartupStatus ss;
	private final LockableOntModel lockableDisplayModel;

	private Path propertyConfigPath;

	public FauxPropertiesUpdater(ServletContext ctx,
			ServletContextListener parent) {
		this.parent = parent;
		this.ss = StartupStatus.getBean(ctx);
		this.lockableDisplayModel = new LockableOntModel(ModelAccess.on(ctx)
				.getOntModel(ModelNames.DISPLAY));
	}

	public void migrate() {
		if (!isAlreadyUpdated()) {
			if (locateFile()) {
				if (loadFile()) {
					writeSuccess();
				}
			}
		}
	}

	private boolean isAlreadyUpdated() {
		try (LockedOntModel m = lockableDisplayModel.read()) {
			return m.contains(DISPLAY_MODEL, UPDATED_DISPLAY_MODEL, VERSION_1_8);
		}
	}

	private boolean locateFile() {
		String homePath = ApplicationUtils.instance().getHomeDirectory()
				.getPath().toString();
		propertyConfigPath = Paths.get(homePath, PATH_TO_PROPERTY_CONFIG);
		if (Files.exists(propertyConfigPath)) {
			return true;
		} else {
			ss.warning(parent, "Could not find attributes "
					+ "for faux properties at " + propertyConfigPath);
			return false;
		}
	}

	private boolean loadFile() {
		try (LockedOntModel m = lockableDisplayModel.write()) {
			m.read(new FileInputStream(propertyConfigPath.toFile()), null, "N3");
			ss.info(parent, "Read " + propertyConfigPath
					+ " into display model: "
					+ "attributes for faux properties.");
			return true;
		} catch (Exception e) {
			ss.warning(parent,
					"Failed to read attributes for faux properties.", e);
			return false;
		}
	}

	private void writeSuccess() {
		try (LockedOntModel m = lockableDisplayModel.write()) {
			m.add(DISPLAY_MODEL, UPDATED_DISPLAY_MODEL, VERSION_1_8);
		}
	}
}
