package org.vivoweb.tools;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sdb.SDBFactory;
import org.apache.jena.sdb.Store;
import org.apache.jena.sdb.StoreDesc;
import org.apache.jena.sdb.sql.SDBConnection;
import org.apache.jena.sdb.sql.SDBExceptionSQL;
import org.apache.jena.sdb.store.DatabaseType;
import org.apache.jena.sdb.store.LayoutType;
import org.apache.jena.sdb.util.StoreUtils;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.vocabulary.RDF;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ApplicationStores {
    private final Model applicationModel;

    private Dataset contentDataset;
    private Dataset configurationDataset;

    private boolean configured = false;

    public ApplicationStores(String homeDir) {
        File config = Utils.resolveFile(homeDir, "config/applicationSetup.n3");
        try {
            InputStream in = new FileInputStream(config);
            applicationModel = ModelFactory.createDefaultModel();
            applicationModel.read(in, null, "N3");
            in.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Application setup not found");
        } catch (IOException e) {
            throw new RuntimeException("Error closing config");
        }

        try {
            Resource contentSource = getObjectFor("http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#hasContentTripleSource");
            Resource configurationSource = getObjectFor("http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#hasConfigurationTripleSource");

            if (isType(contentSource, "java:edu.cornell.mannlib.vitro.webapp.triplesource.impl.sdb.ContentTripleSourceSDB")) {
                Properties props = new Properties();
                try {
                    InputStream in = new FileInputStream(Utils.resolveFile(homeDir, "runtime.properties"));
                    props.load(in);
                    in.close();
                } catch (FileNotFoundException f) {
                    throw new RuntimeException("Unable to load properties");
                } catch (IOException e) {
                    throw new RuntimeException("Unable to load properties");
                }

                Store store = SDBFactory.connectStore(makeConnection(props), makeStoreDesc(props));
                if (store == null) {
                    throw new RuntimeException("Unable to connect to SDB content triple store");
                }

                if (!(StoreUtils.isFormatted(store))) {
                    store.getTableFormatter().create();
                    store.getTableFormatter().truncate();
                }

                contentDataset = SDBFactory.connectDataset(store);
                if (contentDataset == null) {
                    throw new RuntimeException("Unable to connect to SDB content triple store");
                }
            } else if (isType(contentSource, "java:edu.cornell.mannlib.vitro.webapp.triplesource.impl.sdb.ContentTripleSourceTDB")) {
                Statement stmt = contentSource.getProperty(applicationModel.createProperty("http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#hasTdbDirectory"));

                String tdbDirectory = null;
                if (stmt != null) {
                    tdbDirectory = stmt.getObject().asLiteral().getString();
                }

                if (StringUtils.isEmpty(tdbDirectory)) {
                    throw new RuntimeException("Content Source TDB missing directory property");
                }

                File contentFile = Utils.resolveFile(homeDir, tdbDirectory);
                if (!contentFile.exists()) {
                    if (!contentFile.mkdirs()) {
                        throw new RuntimeException("Unable to create content TDB source " + contentFile.getAbsolutePath());
                    }
                } else if (!contentFile.isDirectory()) {
                    throw new RuntimeException("Configuration triple source exists but is not a directory " + contentFile.getAbsolutePath());
                }

                contentDataset = TDBFactory.createDataset(contentFile.getAbsolutePath());
                if (contentDataset == null) {
                    throw new RuntimeException("Unable to open TDB content triple store");
                }
            }

            if (isType(configurationSource, "java:edu.cornell.mannlib.vitro.webapp.triplesource.impl.tdb.ConfigurationTripleSourceTDB")) {
                File configFile = Utils.resolveFile(homeDir, "tdbModels");
                if (!configFile.exists()) {
                    if (!configFile.mkdirs()) {
                        throw new RuntimeException("Unable to create configuration source " + configFile.getAbsolutePath());
                    }
                } else if (!configFile.isDirectory()) {
                    throw new RuntimeException("Configuration triple source exists but is not a directory " + configFile.getAbsolutePath());
                }

                configurationDataset = TDBFactory.createDataset(configFile.getAbsolutePath());
                if (configurationDataset == null) {
                    throw new RuntimeException("Unable to open TDB content triple store");
                }
            }

            configured = true;
        } catch (SQLException e) {
            throw new RuntimeException("SQL Exception");
        } finally {
            if (!configured) {
                close();
            }
        }
    }

    public void readConfiguration(File input) {
        if (configurationDataset != null) {
            try {
                InputStream inputStream = new BufferedInputStream(new FileInputStream(input));
                if (configurationDataset.supportsTransactions()) {
                    configurationDataset.begin(ReadWrite.WRITE) ;
                }
                RDFDataMgr.read(configurationDataset, inputStream, Lang.TRIG);
                inputStream.close();
                if (configurationDataset.supportsTransactions()) {
                    configurationDataset.commit();
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Unable to read configuration file");
            } catch (IOException e) {
                throw new RuntimeException("Unable to read configuration file");
            }
        }
    }

    public void readContent(File input) {
        if (contentDataset != null) {
            try {
                InputStream inputStream = new BufferedInputStream(new FileInputStream(input));
                if (contentDataset.supportsTransactions()) {
                    contentDataset.begin(ReadWrite.WRITE);
                }
                RDFDataMgr.read(contentDataset, inputStream, Lang.TRIG);
                inputStream.close();
                if (contentDataset.supportsTransactions()) {
                    contentDataset.commit();
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Unable to read content file");
            } catch (IOException e) {
                throw new RuntimeException("Unable to read content file");
            }
        }
    }

    public void writeConfiguration(File output) {
        if (configurationDataset != null) {
            try {
                OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(output, false));
                RDFDataMgr.write(outputStream, configurationDataset, RDFFormat.TRIG_BLOCKS);
                outputStream.close();
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Unable to write configuration file");
            } catch (IOException e) {
                throw new RuntimeException("Unable to write configuration file");
            }
        }
    }

    public void writeContent(File output) {
        if (contentDataset != null) {
            try {
                OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(output, false));
                RDFDataMgr.write(outputStream, contentDataset, RDFFormat.TRIG_BLOCKS);
                outputStream.close();
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Unable to write content file");
            } catch (IOException e) {
                throw new RuntimeException("Unable to write content file");
            }
        }
    }

    public boolean isEmpty() {
        boolean empty = true;

        try {
            if (configurationDataset != null) {
                empty &= configurationDataset.asDatasetGraph().isEmpty();
            }
        } catch (SDBExceptionSQL s) {

        }

        try {
            if (contentDataset != null) {
                empty &= contentDataset.asDatasetGraph().isEmpty();
            }
        } catch (SDBExceptionSQL s) {

        }

        return empty;
    }

    public boolean validateFiles(File configurationDump, File contentDump) {
        if (configurationDataset != null && !configurationDump.exists()) {
            return false;
        }

        if (contentDataset != null && !contentDump.exists()) {
            return false;
        }

        return true;
    }

    public void close() {
        if (configurationDataset != null) {
            configurationDataset.close();
        }

        if (contentDataset != null) {
            contentDataset.close();
        }
    }

    private boolean isType(Resource resource, String type) {
        return resource.hasProperty(RDF.type, applicationModel.createResource(type));
    }

    private Resource getObjectFor(String property) {
        NodeIterator iter = applicationModel.listObjectsOfProperty(
                applicationModel.createProperty(property)
        );

        try {
            while (iter.hasNext()) {
                RDFNode node = iter.next();
                if (node != null && node.isResource()) {
                    return node.asResource();
                }
            }
        } finally {
            iter.close();
        }

        return null;
    }

    // SDB

    private StoreDesc makeStoreDesc(Properties props) {
        String layoutStr = props.getProperty(PROPERTY_DB_SDB_LAYOUT, DEFAULT_LAYOUT);
        String dbtypeStr = props.getProperty(PROPERTY_DB_TYPE, DEFAULT_TYPE);
        return new StoreDesc(LayoutType.fetch(layoutStr), DatabaseType.fetch(dbtypeStr));
    }

    private Connection makeConnection(Properties props) {
        try {
            Class.forName(props.getProperty(PROPERTY_DB_DRIVER_CLASS_NAME, DEFAULT_DRIVER_CLASS));
            String url = props.getProperty(PROPERTY_DB_URL);
            String user = props.getProperty(PROPERTY_DB_USERNAME);
            String pass = props.getProperty(PROPERTY_DB_PASSWORD);

            String dbtypeStr = props.getProperty(PROPERTY_DB_TYPE, DEFAULT_TYPE);
            if (DEFAULT_TYPE.equals(dbtypeStr) && !url.contains("?")) {
                url += "?useUnicode=yes&characterEncoding=utf8";
            }
            
            return DriverManager.getConnection(url, user, pass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Unable to create JDBC connection");
        } catch (SQLException e) {
            throw new RuntimeException("Unable to create JDBC connection");
        }
    }

    static final String DEFAULT_DRIVER_CLASS = "com.mysql.jdbc.Driver";
    static final String DEFAULT_LAYOUT = "layout2/hash";
    static final String DEFAULT_TYPE = "MySQL";

    static final String PROPERTY_DB_URL = "VitroConnection.DataSource.url";
    static final String PROPERTY_DB_USERNAME = "VitroConnection.DataSource.username";
    static final String PROPERTY_DB_PASSWORD = "VitroConnection.DataSource.password";
    static final String PROPERTY_DB_DRIVER_CLASS_NAME = "VitroConnection.DataSource.driver";
    static final String PROPERTY_DB_SDB_LAYOUT = "VitroConnection.DataSource.sdb.layout";
    static final String PROPERTY_DB_TYPE = "VitroConnection.DataSource.dbtype";
}
