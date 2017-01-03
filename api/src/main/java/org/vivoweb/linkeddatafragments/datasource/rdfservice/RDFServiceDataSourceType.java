package org.vivoweb.linkeddatafragments.datasource.rdfservice;

import com.google.gson.JsonObject;
import org.linkeddatafragments.datasource.IDataSource;
import org.linkeddatafragments.datasource.IDataSourceType;
import org.linkeddatafragments.exceptions.DataSourceCreationException;

import java.io.File;

/**
 * The type of Triple Pattern Fragment data sources that are backed by
 * a Jena TDB instance.
 *
 * @author <a href="http://olafhartig.de">Olaf Hartig</a>
 */
public class RDFServiceDataSourceType implements IDataSourceType
{
    @Override
    public IDataSource createDataSource( final String title,
                                         final String description,
                                         final JsonObject settings )
                                                     throws DataSourceCreationException
    {
        try {
            return new RDFServiceDataSource(title, description);
        } catch (Exception ex) {
            throw new DataSourceCreationException(ex);
        }
    }

}
