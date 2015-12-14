/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.application;

import static edu.cornell.mannlib.vitro.webapp.application.BuildProperties.WEBAPP_PATH_BUILD_PROPERTIES;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;
import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Encapsulates some of the info relating to the Vitro home directory.
 */
public class VitroHomeDirectory {
	private static final Log log = LogFactory.getLog(VitroHomeDirectory.class);
	
	public static VitroHomeDirectory find(ServletContext ctx) {
		HomeDirectoryFinder finder = new HomeDirectoryFinder(ctx);
		return new VitroHomeDirectory(ctx, finder.getPath(),
				finder.getMessage());
	}

	private final ServletContext ctx;
	private final Path path;
	private final String discoveryMessage;

	public VitroHomeDirectory(ServletContext ctx, Path path,
			String discoveryMessage) {
		this.ctx = ctx;
		this.path = path;
		this.discoveryMessage = discoveryMessage;
	}

	public ServletContext getCtx() {
		return ctx;
	}

	public Path getPath() {
		return path;
	}

	public String getDiscoveryMessage() {
		return discoveryMessage;
	}
	
	/**
	 * Find something that specifies the location of the Vitro home directory.
	 * Look in the JDNI environment, the system properties, and the
	 * build.properties file.
	 * 
	 * If we don't find it, fail. If we find it more than once, use the first
	 * one (with a warning). If it is not an existing, readable directory, fail.
	 */
	private static class HomeDirectoryFinder {
		/** JNDI path that defines the Vitro home directory */
		private static final String VHD_JNDI_PATH = "java:comp/env/vitro/home";

		/** System property that defines the Vitro home directory */
		private static final String VHD_SYSTEM_PROPERTY = "vitro.home";

		/** build.properties property that defines the Vitro home directory */
		private static final String VHD_BUILD_PROPERTY = "vitro.home";

		private final ServletContext ctx;
		private final List<Found> foundLocations = new ArrayList<>();

		public HomeDirectoryFinder(ServletContext ctx) {
			this.ctx = ctx;

			getVhdFromJndi();
			getVhdFromSystemProperties();
			getVhdFromBuildProperties();
			confirmExactlyOneResult();
			confirmValidDirectory();
		}

		public String getMessage() {
			return foundLocations.get(0).getMessage();
		}

		public Path getPath() {
			return foundLocations.get(0).getPath();
		}
		
		public void getVhdFromJndi() {
			try {
				String vhdPath = (String) new InitialContext()
						.lookup(VHD_JNDI_PATH);
				if (vhdPath == null) {
					log.debug("Didn't find a JNDI value at '" + VHD_JNDI_PATH
							+ "'.");
				} else {
					log.debug("'" + VHD_JNDI_PATH + "' as specified by JNDI: "
							+ vhdPath);
					String message = String.format(
							"JNDI environment '%s' was set to '%s'",
							VHD_JNDI_PATH, vhdPath);
					foundLocations.add(new Found(Paths.get(vhdPath), message));
				}
			} catch (Exception e) {
				log.debug("JNDI lookup failed. " + e);
			}
		}

		private void getVhdFromSystemProperties() {
			String vhdPath = System.getProperty(VHD_SYSTEM_PROPERTY);
			if (vhdPath == null) {
				log.debug("Didn't find a system property value at '"
						+ VHD_SYSTEM_PROPERTY + "'.");
			} else {
				log.debug("'" + VHD_SYSTEM_PROPERTY
						+ "' as specified by system property: " + vhdPath);
				String message = String.format(
						"System property '%s' was set to '%s'",
						VHD_SYSTEM_PROPERTY, vhdPath);
				foundLocations.add(new Found(Paths.get(vhdPath), message));
			}
		}

		private void getVhdFromBuildProperties() {
			try {
				Map<String, String> buildProps = new BuildProperties(ctx)
						.getMap();
				String vhdPath = buildProps.get(VHD_BUILD_PROPERTY);
				if (vhdPath == null) {
					log.debug("build properties doesn't contain a value for '"
							+ VHD_BUILD_PROPERTY + "'.");
				} else {
					log.debug("'" + VHD_BUILD_PROPERTY
							+ "' as specified by build.properties: " + vhdPath);
					String message = String.format(
							"In resource '%s', '%s' was set to '%s'.",
							WEBAPP_PATH_BUILD_PROPERTIES, VHD_BUILD_PROPERTY,
							vhdPath);
					foundLocations.add(new Found(Paths.get(vhdPath), message));
				}
			} catch (Exception e) {
				log.warn("Reading build properties failed. " + e);
			}
		}

		private void confirmExactlyOneResult() {
			if (foundLocations.isEmpty()) {
				String message = String.format("Can't find a value "
						+ "for the Vitro home directory. "
						+ "Looked in JNDI environment at '%s'. "
						+ "Looked for a system property named '%s'. "
						+ "Looked in 'WEB-INF/resources/build.properties' "
						+ "for '%s'.", VHD_JNDI_PATH, VHD_SYSTEM_PROPERTY,
						VHD_BUILD_PROPERTY);
				throw new IllegalStateException(message);
			} else if (foundLocations.size() > 1) {
				String message = String.format("Found multiple values for the "
						+ "Vitro home directory: " + foundLocations);
				log.warn(message);
			}
		}

		private void confirmValidDirectory() {
			Path vhd = getPath();
			if (!Files.exists(vhd)) {
				throw new IllegalStateException("Vitro home directory '" + vhd
						+ "' does not exist.");
			}
			if (!Files.isDirectory(vhd)) {
				throw new IllegalStateException("Vitro home directory '" + vhd
						+ "' is not a directory.");
			}
			if (!Files.isReadable(vhd)) {
				throw new IllegalStateException(
						"Cannot read Vitro home directory '" + vhd + "'.");
			}
			if (!Files.isWritable(vhd)) {
				throw new IllegalStateException(
						"Can't write to Vitro home directory: '" + vhd + "'.");
			}
		}

		/** We found it: where and how. */
		private static class Found {
			private final Path path;
			private final String message;

			public Found(Path path, String message) {
				this.path = path;
				this.message = message;
			}

			public Path getPath() {
				return path;
			}

			public String getMessage() {
				return message;
			}

			@Override
			public String toString() {
				return "Found[path=" + path + ", message=" + message + "]";
			}
		}
	}

}
