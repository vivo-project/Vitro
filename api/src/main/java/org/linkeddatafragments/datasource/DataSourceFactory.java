package org.linkeddatafragments.datasource;

import com.fasterxml.jackson.databind.JsonNode;
import org.linkeddatafragments.exceptions.DataSourceCreationException;
import org.linkeddatafragments.exceptions.UnknownDataSourceTypeException;

/**
 *
 * @author Miel Vander Sande
 * @author Bart Hanssens
 * @author <a href="http://olafhartig.de">Olaf Hartig</a>
 */
public class DataSourceFactory {
    /**
     * Create a datasource using a JSON config
     *
     * @param config
     * @return datasource interface
     * @throws DataSourceCreationException
     */
    public static IDataSource create(JsonNode config) throws DataSourceCreationException {
        String title = config.get("title").asText();
        String description = config.get("description").asText();
        String typeName = config.get("type").asText();

        JsonNode settings = config.get("settings");

        final IDataSourceType type = DataSourceTypesRegistry.getType(typeName);
        if ( type == null )
            throw new UnknownDataSourceTypeException(typeName);

        return type.createDataSource( title, description, settings );
    }

}
