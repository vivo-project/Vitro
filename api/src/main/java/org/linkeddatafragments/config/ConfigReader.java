package org.linkeddatafragments.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.linkeddatafragments.datasource.IDataSourceType;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Reads the configuration of a Linked Data Fragments server.
 *
 * @author Ruben Verborgh
 * @author <a href="http://olafhartig.de">Olaf Hartig</a>
 */
public class ConfigReader {
    private final Map<String, IDataSourceType> dataSourceTypes = new HashMap<>();
    private final Map<String, JsonNode> dataSources = new HashMap<>();
    private final Map<String, String> prefixes = new HashMap<>();
    private final String baseURL;

    /**
     * Creates a new configuration reader.
     *
     * @param configReader the configuration
     */
    public ConfigReader(Reader configReader) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = null;
        try {
            root = mapper.readTree(configReader);
            this.baseURL = root.has("baseURL") ? root.get("baseURL").asText() : null;

            Iterator<Entry<String, JsonNode>> iterator;

            iterator = root.get("datasourcetypes").fields();
            while (iterator.hasNext()) {
                Entry<String, JsonNode> entry = iterator.next();
                final String className = entry.getValue().asText();
                dataSourceTypes.put(entry.getKey(), initDataSouceType(className) );
            }

            iterator = root.get("datasources").fields();
            while (iterator.hasNext()) {
                Entry<String, JsonNode> entry = iterator.next();
                this.dataSources.put(entry.getKey(), entry.getValue());
            }

            iterator = root.get("prefixes").fields();
            while (iterator.hasNext()) {
                Entry<String, JsonNode> entry = iterator.next();
                this.prefixes.put(entry.getKey(), entry.getValue().asText());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the data source types.
     *
     * @return a mapping of names of data source types to these types
     */
    public Map<String, IDataSourceType> getDataSourceTypes() {
        return dataSourceTypes;
    }

    /**
     * Gets the data sources.
     *
     * @return the data sources
     */
    public Map<String, JsonNode> getDataSources() {
        return dataSources;
    }

    /**
     * Gets the prefixes.
     *
     * @return the prefixes
     */
    public Map<String, String> getPrefixes() {
        return prefixes;
    }

    /**
     * Gets the base URL
     *
     * @return the base URL
     */
    public String getBaseURL() {
        return baseURL;
    }

    /**
     * Loads a certain {@link IDataSourceType} class at runtime
     *
     * @param className IDataSourceType class
     * @return the created IDataSourceType object
     */
    protected IDataSourceType initDataSouceType(final String className )
    {
        final Class<?> c;
        try {
            c = Class.forName( className );
        }
        catch ( ClassNotFoundException e ) {
            throw new IllegalArgumentException( "Class not found: " + className,
                                                e );
        }

        final Object o;
        try {
            o = c.newInstance();
        }
        catch ( Exception e ) {
            throw new IllegalArgumentException(
                        "Creating an instance of class '" + className + "' " +
                        "caused a " + e.getClass().getSimpleName() + ": " +
                        e.getMessage(), e );
        }

        if ( ! (o instanceof IDataSourceType) )
            throw new IllegalArgumentException(
                        "Class '" + className + "' is not an implementation " +
                        "of IDataSourceType." );

        return (IDataSourceType) o;
    }

}
