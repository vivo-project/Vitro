/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Obtains and provides the properties from the web application's context.xml
 *
 * @author awoods
 * @since 2020-10-23
 */
public class ContextProperties {
	private static final Log log = LogFactory.getLog(ContextProperties.class);

	private static final String DEFAULT_NAMESPACE_JNDI_PATH = "java:comp/env/vitro/defaultNamespace";
	private static final String ROOT_USER_ADDRESS_JNDI_PATH = "java:comp/env/vitro/rootUserAddress";
	private static final String APP_NAME_JNDI_PATH = "java:comp/env/vitro/appName";

	private static final String DEFAULT_NAMESPACE_KEY = "Vitro.defaultNamespace";
	private static final String ROOT_USER_ADDRESS_KEY = "rootUser.emailAddress";
	private static final String APP_NAME_KEY = "app-name";

	private final Map<String, String> propertyMap;

	public ContextProperties() {
		Map<String, String> map = new HashMap<>();

		// Find default namespace
		map.put(DEFAULT_NAMESPACE_KEY, findJndiProperty(DEFAULT_NAMESPACE_JNDI_PATH));

		// Find root user email address
		map.put(ROOT_USER_ADDRESS_KEY, findJndiProperty(ROOT_USER_ADDRESS_JNDI_PATH));

		// Find application name
		map.put(APP_NAME_KEY, findJndiProperty(APP_NAME_JNDI_PATH));

		propertyMap = Collections.unmodifiableMap(map);
	}

	public static String findJndiProperty(String jndiProperty) {
		try {
			return  (String) new InitialContext().lookup(jndiProperty);

		} catch (NamingException e) {
			log.error("Unable to find name in JNDI: " + jndiProperty, e);

			StringBuilder msg = new StringBuilder("\n====================\n");
			msg.append("Error loading JNDI property: ");
			msg.append(jndiProperty);
			msg.append("\n");
			msg.append("\tAn application context XML file (named after deployed war file, e.g. vivo.xml) ");
			msg.append("must be placed in servlet container.\n");
			msg.append("\tFor Tomcat, see documentation for location of file: \n");
			msg.append("\t\thttps://tomcat.apache.org/tomcat-9.0-doc/config/context.html#Defining_a_context \n");
			msg.append("\tThe common location on the server is: $CATALINA_BASE/conf/[enginename]/[hostname]/ \n");
			msg.append("\t\te.g. /var/lib/tomcat9/conf/Catalina/localhost/vivo.xml\n");
			msg.append("\tAn example 'context.xml' file is in the META-INF directory of this project.\n");
			msg.append("====================\n");
			throw new RuntimeException(msg.toString(), e);
		}
	}

	public Map<String, String> getMap() {
		return this.propertyMap;
	}

}
