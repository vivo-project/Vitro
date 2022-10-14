package edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation;

import java.math.BigInteger;
import java.util.Random;
import java.util.UUID;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;

import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

public class ResourceGenerator {

	private static final Log log = LogFactory.getLog(ResourceGenerator.class.getName());

	protected static Object JAVA_UUID = "java_uuid";
	protected static Object OLD_NUMBER = "old_number";
	protected static Object UUID_NUMBER = "uuid_number";

	private static ResourceGenerator INSTANCE = new ResourceGenerator();

	public static ResourceGenerator getInstance() {
		return INSTANCE;
	}

	private WebappDaoFactory wadf;

	public String serialize(Resource input) {
		return input.toString();
	}

	/**
	 * Creates a resource from input comma separated list of uri part names Values
	 * of each parts calculated independently and concatenated result returned If
	 * part name is not defined in the class, then it is added to the uri name If
	 * part name is {@value #JAVA_UUID}, then new generated UUID will be used as a
	 * part If part name is {@value #UUID_NUMBER}, then new generated UUID
	 * represented as a string of 40 numbers will be used as a part If part name is
	 * {@value #OLD_NUMBER} and it is the last one, then old individual uri should
	 * be created. All previously defined parts will be used as a new namespace.
	 * @throws InitializationException 
	 */

	public Resource getUriFromFormat(String input) throws InitializationException {
		String uri = getUri(input);
		Resource res = new ResourceImpl(uri);
		return res;
	}

	private String getUri(String input) throws InitializationException {
		String[] parts = input.split("[\\s,]+");
		StringBuilder uriBuilder = new StringBuilder();
		for (int i = 0; i < parts.length; i++) {
			String partName = parts[i].trim();
			if (isOldFormatRequested(parts, i, partName) && wadf != null) {
				return getOldURI(uriBuilder.toString());
			}
			uriBuilder.append(getUriPart(partName));
		}
		if (uriBuilder.length() == 0) {
			uriBuilder.append(getJavaUUID());
		}
		String uri = uriBuilder.toString();
		if (wadf != null && wadf.checkURI(uri) != null) {
			throw new InitializationException("Uri check failed");
		}
		return uri;
	}

	private boolean isOldFormatRequested(String[] parts, int i, String part) {
		return parts.length == i + 1 && OLD_NUMBER.equals(part);
	}

	private static String getUriPart(String keyword) {
		if (JAVA_UUID.equals(keyword)) {
			return getJavaUUID();
		}
		if (UUID_NUMBER.equals(keyword)) {
			return getJavaUUIDNumber();
		}
		//If wadf is not available fallback to use of UUID_NUMBER
		if (OLD_NUMBER.equals(keyword)) {
			return getJavaUUIDNumber();
		}
		return keyword;
	}

	private static String getJavaUUIDNumber() {
		final UUID randomUUID = UUID.randomUUID();
		final String base16String = randomUUID.toString().replace("-", "");
		return String.format("%040d", new BigInteger(base16String, 16));
	}

	private static String getJavaUUID() {
		return UUID.randomUUID().toString();
	}

	public void init(WebappDaoFactory webappDaoFactory) {
		wadf = webappDaoFactory;
	}

	private String getOldURI(String newNamespace) throws InitializationException {
		String uri = null;
		String errMsg = null;
		Random random = new Random();
		boolean uriIsGood = false;
		int attempts = 0;

		while (!uriIsGood && attempts < 30) {
			uri = newNamespace + random.nextInt(Math.min(Integer.MAX_VALUE, (int) Math.pow(2, attempts + 13)));
			errMsg = wadf.checkURI(uri);
			if (errMsg != null) {
				uri = null;
			} else {
				uriIsGood = true;
			}
			attempts++;
		}
		if (uri == null) {
			log.error("Generated uri is null, fallback to generate uri in new format");
			uri = newNamespace + getJavaUUIDNumber();
		}
		if (wadf.checkURI(uri) != null) {
			throw new InitializationException("Uri check failed");
		}

		return uri;
	}

}
