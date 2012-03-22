/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.images;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

/**
 * A utility for finding the URL of the correct Placeholder image.
 * 
 * The mapping of image URLs to classes is created at startup, and stored in the
 * ServletContext.
 */
public class PlaceholderUtil {
	private static final Log log = LogFactory.getLog(PlaceholderUtil.class);

	private static final String ATTRIBUTE_NAME = PlaceholderUtil.class
			.getName();
	private static final String PROPERTIES_FILE_PATH = "/images/placeholders/placeholders.properties";
	private static final String DEFAULT_IMAGE_PATH = "/images/placeholders/thumbnail.jpg";

	// ----------------------------------------------------------------------
	// Static methods
	// ----------------------------------------------------------------------

	/**
	 * If we have a placeholder image for this exact type, return it. Otherwise,
	 * return the default.
	 */
	public static String getPlaceholderImagePathForType(VitroRequest vreq,
			String typeUri) {
		PlaceholderUtil pu = getPlaceholderUtil(vreq);
		if (pu == null) {
			return DEFAULT_IMAGE_PATH;
		} else {
			return pu.getPlaceholderImagePathForType(typeUri);
		}
	}

	/**
	 * If there is a placeholder image for any type that this Individual
	 * instantiates, return that image. Otherwise, return the default.
	 */
	public static String getPlaceholderImagePathForIndividual(
			VitroRequest vreq, String individualUri) {
		PlaceholderUtil pu = getPlaceholderUtil(vreq);
		if (pu == null) {
			return DEFAULT_IMAGE_PATH;
		} else {
			IndividualDao iDao = vreq.getWebappDaoFactory().getIndividualDao();
			return pu.getPlaceholderImagePathForIndividual(iDao, individualUri);
		}
	}

	/** Get the PlaceholderUtil instance from the context, or null if none. */
	private static PlaceholderUtil getPlaceholderUtil(VitroRequest vreq) {
		if (vreq == null) {
			return null;
		}
		ServletContext ctx = vreq.getSession().getServletContext();
		Object attrib = ctx.getAttribute(ATTRIBUTE_NAME);
		if (attrib instanceof PlaceholderUtil) {
			return (PlaceholderUtil) attrib;
		} else {
			log.warn("Looked for a PlaceholerUtil, but found " + attrib);
			return null;
		}
	}

	// ----------------------------------------------------------------------
	// The object
	// ----------------------------------------------------------------------

	private final Map<String, String> mapUrlsByClass;

	private PlaceholderUtil(Map<String, String> map) {
		this.mapUrlsByClass = Collections
				.unmodifiableMap(new HashMap<String, String>(map));
	}

	/**
	 * If we have a placeholder image for this exact type, return it. Otherwise,
	 * return the default.
	 */
	private String getPlaceholderImagePathForType(String typeUri) {
		if (typeUri == null) {
			log.debug("getPlaceholderImagePathForType: typeUri is null");
			return DEFAULT_IMAGE_PATH;
		}
		String url = mapUrlsByClass.get(typeUri);
		if (url == null) {
			log.debug("getPlaceholderImagePathForType: no value for '"
					+ typeUri + "'");
			return DEFAULT_IMAGE_PATH;
		}
		log.debug("getPlaceholderImagePathForType: value for '" + typeUri
				+ "' is '" + url + "'");
		return url;
	}

	/**
	 * If there is a placeholder image for any type that this Individual
	 * instantiates, return that image. Otherwise, return the default.
	 */
	private String getPlaceholderImagePathForIndividual(IndividualDao iDao,
			String individualUri) {
		if (individualUri == null) {
			log.debug("getPlaceholderImagePathForIndividual: "
					+ "individualUri is null");
			return DEFAULT_IMAGE_PATH;
		}
		for (String classUri : mapUrlsByClass.keySet()) {
			String imageUrl = mapUrlsByClass.get(classUri);
			if (iDao.isIndividualOfClass(classUri, individualUri)) {
				log.debug("getPlaceholderImagePathForIndividual: individual '"
						+ individualUri + "' is of class '" + classUri
						+ "', value is '" + imageUrl + "'");
				return imageUrl;
			}
		}
		log.debug("getPlaceholderImagePathForIndividual: individual '"
				+ individualUri + "' is not of any recognized class");
		return DEFAULT_IMAGE_PATH;
	}

	// ----------------------------------------------------------------------
	// Classes used for setup
	// ----------------------------------------------------------------------

	public static class Setup implements ServletContextListener {
		@Override
		public void contextInitialized(ServletContextEvent sce) {
			ServletContext ctx = sce.getServletContext();
			StartupStatus ss = StartupStatus.getBean(ctx);

			try {
				File propertiesFile = confirmRealPath(ctx, PROPERTIES_FILE_PATH);
				Map<String, String> map = loadPropertiesToMap(propertiesFile);
				confirmImagesArePresent(ctx, map);
				ctx.setAttribute(ATTRIBUTE_NAME, new PlaceholderUtil(map));
			} catch (SetupException e) {
				if (e.getCause() == null) {
					ss.warning(this, e.getMessage());
				} else {
					ss.warning(this, e.getMessage(), e.getCause());
				}
			}
		}

		private Map<String, String> loadPropertiesToMap(File propertiesFile)
				throws SetupException {
			Properties props = new Properties();
			try {
				props.load(new FileReader(propertiesFile));
			} catch (IOException e) {
				throw new SetupException(
						"Can't load properties from the file at '"
								+ PROPERTIES_FILE_PATH
								+ "'. Is it in a valid format?", e);
			}

			Map<String, String> map = new HashMap<String, String>();
			for (Enumeration<Object> keys = props.keys(); keys
					.hasMoreElements();) {
				String key = (String) keys.nextElement();
				String value = props.getProperty(key);
				map.put(key, value);
			}

			return map;
		}

		private void confirmImagesArePresent(ServletContext ctx,
				Map<String, String> map) throws SetupException {
			Set<String> imageUrls = new HashSet<String>();
			imageUrls.add(DEFAULT_IMAGE_PATH);
			imageUrls.addAll(map.values());
			for (String imageUrl : imageUrls) {
				confirmRealPath(ctx, imageUrl);
			}

		}

		private File confirmRealPath(ServletContext ctx, String url)
				throws SetupException {
			String path = ctx.getRealPath(url);
			if (path == null) {
				throw new SetupException("Can't translate to real path: '"
						+ url + "'");
			}
			File file = new File(path);
			if (!file.exists()) {
				throw new SetupException("No file found at '" + url + "'.");
			}
			if (!file.canRead()) {
				throw new SetupException("Can't read the file at '" + url
						+ "'.");
			}
			return file;
		}

		@Override
		public void contextDestroyed(ServletContextEvent sce) {
			sce.getServletContext().removeAttribute(ATTRIBUTE_NAME);
		}

	}

	private static class SetupException extends Exception {
		public SetupException(String message) {
			super(message);
		}

		public SetupException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
