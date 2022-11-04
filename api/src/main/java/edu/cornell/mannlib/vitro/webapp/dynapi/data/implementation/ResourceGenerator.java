package edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation;

import java.math.BigInteger;
import java.util.UUID;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;

import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

public class ResourceGenerator {

	private static final String BASE62_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	protected static String JAVA_UUID = "java_uuid";
	protected static String JAVA_UUID_NO_DASH = "java_uuid_no_dash";
	protected static String UUID_NUMBER = "uuid_number";
	protected static String UUID_BASE62 = "uuid_base62";
	
	private final String LEAD_ZEROS = "00000000000000000000000";
	private BigInteger base = BigInteger.valueOf(62);
	private BigInteger zero = BigInteger.valueOf(0);

	private static ResourceGenerator INSTANCE = new ResourceGenerator();

	public static ResourceGenerator getInstance() {
		return INSTANCE;
	}

	private WebappDaoFactory wadf;

	public String serialize(Resource input) {
		return input.toString();
	}

	/**
	 * Creates a resource from input comma separated list of keys.
	 * Values of each parts calculated independently and concatenated result returned.
	 * If part name is not known, then it will be returned as a value.
	 * Key {@value #JAVA_UUID} returns value in format [0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}
	 * Key {@value #JAVA_UUID_NO_DASH} returns value in format [0-9a-f]{32}
	 * Key {@value #UUID_NUMBER} returns value in format [0-9]{40} 
 	 * Key {@value #UUID_BASE62} returns value in format [0-9a-zA-Z]{23} 
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

	private String getUriPart(String keyword) {
		if (JAVA_UUID.equals(keyword)) {
			return getJavaUUID();
		}
		if (JAVA_UUID_NO_DASH.equals(keyword)) {
			return getJavaUUIDwithoutDashes();
		}
		if (UUID_NUMBER.equals(keyword)) {
			return getJavaUUIDNumber();
		}
		if (UUID_BASE62.equals(keyword)) {
			return getJavaUUIDAlphaNum();
		}
		return keyword;
	}

	private static String getJavaUUIDNumber() {
		String base16String = getJavaUUIDwithoutDashes();
		return String.format("%040d", new BigInteger(base16String, 16));
	}
	
	private String getJavaUUIDAlphaNum() {
		String base16String = getJavaUUIDwithoutDashes();
		BigInteger number = new BigInteger(base16String, 16);

		StringBuilder stringBuilder = new StringBuilder(23);
		while (number.compareTo(zero) > 0) {
			stringBuilder.insert(0, BASE62_CHARS.charAt(number.mod(base).intValue()));
			number = number.divide(base);
		}
		String base62String = stringBuilder.toString();
		return (LEAD_ZEROS + base62String).substring(base62String.length());
	}

	private static String getJavaUUID() {
		return UUID.randomUUID().toString();
	} 
	
	private static String getJavaUUIDwithoutDashes() {
		return getJavaUUID().replace("-", "");
	}

	public void init(WebappDaoFactory webappDaoFactory) {
		wadf = webappDaoFactory;
	}	

}
