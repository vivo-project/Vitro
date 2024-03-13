/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation;

import java.math.BigInteger;
import java.util.Random;
import java.util.UUID;

import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.ResourceImpl;

public class ResourceGenerator {

    private static final String BASE62_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final Log log = LogFactory.getLog(ResourceGenerator.class.getName());

    protected static String JAVA_UUID = "java_uuid";
    protected static String JAVA_UUID_NO_DASH = "java_uuid_no_dash";
    protected static String JAVA_UUID_NUMBER = "java_uuid_number";
    protected static String JAVA_UUID_BASE62 = "java_uuid_base62";
    protected static Object OLD_NUMBER = "old_number";

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

    public static Resource generateResourceFromFormat(String input) throws InitializationException {
        return getInstance().getUriFromFormat(input);
    }

    /**
     * Creates a resource from input comma separated list of keys. Values of each parts calculated independently and
     * concatenated result returned. If part name is not known, then it will be returned as a value. Key
     * {@value #JAVA_UUID} returns value in format [0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12} Key
     * {@value #JAVA_UUID_NO_DASH} returns value in format [0-9a-f]{32} Key {@value #JAVA_UUID_NUMBER} returns value in
     * format [0-9]{40} Key {@value #JAVA_UUID_BASE62} returns value in format [0-9a-zA-Z]{1,23} Key
     * {@value #OLD_NUMBER} returns number between 0 and Integer.MAX_VALUE
     * 
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
            uriBuilder.append(getUriPart(partName, uriBuilder));
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

    private String getUriPart(String keyword, StringBuilder uriBuilder) {
        if (JAVA_UUID.equals(keyword)) {
            return getJavaUUID();
        }
        if (JAVA_UUID_NO_DASH.equals(keyword)) {
            return getJavaUUIDwithoutDashes();
        }
        if (JAVA_UUID_NUMBER.equals(keyword)) {
            return getJavaUUIDNumber();
        }
        if (JAVA_UUID_BASE62.equals(keyword)) {
            return getJavaUUIDAlphaNum();
        }
        if (OLD_NUMBER.equals(keyword)) {
            return getNumber(uriBuilder.toString());
        }
        return keyword;
    }

    private String getNumber(String prefix) {
        if (wadf == null) {
            log.error("WebappDaoFactory is null, fallback to default");
            return getDefaultFormat();
        }
        String uri = null;
        String errMsg = null;
        Random random = new Random();
        boolean uriIsGood = false;
        int attempts = 0;
        int number = 0;
        while (!uriIsGood && attempts < 30) {
            number = random.nextInt(Math.min(Integer.MAX_VALUE, (int) Math.pow(2, attempts + 13)));
            uri = prefix + number;
            errMsg = wadf.checkURI(uri);
            if (errMsg != null) {
                uri = null;
            } else {
                uriIsGood = true;
            }
            attempts++;
        }
        if (uri == null) {
            log.error("Generated old_number is null, fallback to default");
            return getDefaultFormat();
        }
        return String.valueOf(number);
    }

    private String getDefaultFormat() {
        return getJavaUUIDAlphaNum();
    }

    private static String getJavaUUIDNumber() {
        String base16String = getJavaUUIDwithoutDashes();
        return String.format("%040d", new BigInteger(base16String, 16));
    }

    private String getJavaUUIDAlphaNum() {
        String base16String = getJavaUUIDwithoutDashes();
        BigInteger number = new BigInteger(base16String, 16);

        StringBuilder base62Builder = new StringBuilder(23);
        while (number.compareTo(zero) > 0) {
            base62Builder.insert(0, BASE62_CHARS.charAt(number.mod(base).intValue()));
            number = number.divide(base);
        }
        return base62Builder.toString();
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
