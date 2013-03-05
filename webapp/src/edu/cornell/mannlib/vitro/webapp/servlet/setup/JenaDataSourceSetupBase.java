/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.store.DatabaseType;
import com.hp.hpl.jena.sdb.store.LayoutType;
import com.mchange.v2.c3p0.ComboPooledDataSource;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.jena.JenaBaseDaoCon;
import edu.cornell.mannlib.vitro.webapp.dao.jena.RDBGraphGenerator;
import edu.cornell.mannlib.vitro.webapp.dao.jena.RegeneratingGraph;
import edu.cornell.mannlib.vitro.webapp.dao.jena.SDBGraphGenerator;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VitroJenaModelMaker;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VitroJenaSDBModelMaker;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VitroModelSource;

public class JenaDataSourceSetupBase extends JenaBaseDaoCon {
    private static final String VITRO_DEFAULT_NAMESPACE = "Vitro.defaultNamespace";

    private static final Log log = LogFactory.getLog(
            JenaDataSourceSetupBase.class);

    protected final static String MAX_ACTIVE_PROPERTY = 
        "VitroConnection.DataSource.pool.maxActive";
    
    protected final static String MAX_IDLE_PROPERTY = 
        "VitroConnection.DataSource.pool.maxIdle";
    
    protected final static int DEFAULT_MAXWAIT = 10000, // ms
            DEFAULT_MAXACTIVE = 40,
            MINIMUM_MAXACTIVE = 20,
            DEFAULT_MAXIDLE = 10,
            DEFAULT_TIMEBETWEENEVICTIONS =  180 * 1000, // ms
            DEFAULT_TESTSPEREVICTION = DEFAULT_MAXACTIVE,
            DEFAULT_MINEVICTIONIDLETIME = 180 * 1000; // ms

    protected final static boolean DEFAULT_TESTONBORROW = true,
            DEFAULT_TESTONRETURN = true, DEFAULT_TESTWHILEIDLE = true;

    protected static String BASE = "/WEB-INF/ontologies/";
    protected static String USERPATH = BASE+"user/";
    protected static String USER_ABOX_PATH = BASE+"user/abox";
    protected static String USER_TBOX_PATH = BASE+"user/tbox";
    protected static String USER_APPMETA_PATH = BASE+"user/applicationMetadata";
    protected static String SYSTEMPATH = BASE+"system/";       
    public static String APPPATH = BASE+"app/";
    //these files are loaded everytime the system starts up
    public static String APPPATH_LOAD = APPPATH + "menuload/";
    
    //All files in this directory will be reloaded every startup
    //and attached as sub-models to the displayOntModel.
    static final String DISPLAY_MODEL_LOAD_AT_STARTUP_DIR =
        APPPATH + "loadedAtStartup";
    
    protected static boolean firstStartup = false;

    String DB_USER =   "jenatest";                          // database user id
    String DB_PASSWD = "jenatest";                          // database password
     
    // ABox assertions. These are stored in a database (Jena SDB) and the 
    // application works (queries and updates) with the ABox data from the DB - 
    // this model is not maintained in memory. For query performance reasons, 
    // there won't be any submodels for the ABox data.
   
    public static final String JENA_DB_MODEL = 
            "http://vitro.mannlib.cornell.edu/default/vitro-kb-2";
   
    // ABox inferences. This is ABox data that is inferred, using VIVO's native 
    // simple, specific-purpose reasoning based on the combination of the ABox 
    // (assertion and inferences) data and the TBox (assertions and inferences)
    // data.
    public static final String JENA_INF_MODEL = 
           "http://vitro.mannlib.cornell.edu/default/vitro-kb-inf";
      
    // TBox assertions. 
    // Some of these (the local extensions) are stored and maintained in a Jena 
    // database and are also maintained in memory while the application is 
    // running. Other parts of the TBox, the 'VIVO Core,' are also backed by a 
    // Jena DB, but they are read fresh from files each time the application 
    // starts. While the application is running, they are kept in memory, as 
    // submodels of the in memory copy of this named graph. 
    public static final String JENA_TBOX_ASSERTIONS_MODEL = 
            "http://vitro.mannlib.cornell.edu/default/asserted-tbox";

   
    // Inferred TBox. This is TBox data that is inferred from the combination of 
    // VIVO core TBox and any local extension TBox assertions. Pellet computes 
    // these inferences. These are stored in the DB.
    public static final String JENA_TBOX_INF_MODEL = 
            "http://vitro.mannlib.cornell.edu/default/inferred-tbox";
   
    // Model for tracking edit changes. Obsolete.
    static final String JENA_AUDIT_MODEL = 
            "http://vitro.mannlib.cornell.edu/ns/db/experimental/audit";

    // User accounts data
    public static final String JENA_USER_ACCOUNTS_MODEL = 
            "http://vitro.mannlib.cornell.edu/default/vitro-kb-userAccounts";
   
    // This model doesn't exist yet. It's a placeholder for the application 
    // ontology.
    public static final String JENA_APPLICATION_METADATA_MODEL = 
        "http://vitro.mannlib.cornell.edu/default/vitro-kb-applicationMetadata";
   
    // This is Brian C's application.owl file. We may not have to be concerned 
    // with this for release 1.2.
    static final String JENA_DISPLAY_METADATA_MODEL = 
            "http://vitro.mannlib.cornell.edu/default/vitro-kb-displayMetadata";
    
    //TBox and display model related
    static final String JENA_DISPLAY_TBOX_MODEL = 
        DisplayVocabulary.DISPLAY_TBOX_MODEL_URI;
    static final String JENA_DISPLAY_DISPLAY_MODEL = 
        DisplayVocabulary.DISPLAY_DISPLAY_MODEL_URI;    
    
    // use OWL models with no reasoning
    static final OntModelSpec DB_ONT_MODEL_SPEC = OntModelSpec.OWL_MEM;
    static final OntModelSpec MEM_ONT_MODEL_SPEC = OntModelSpec.OWL_MEM; 
   
    private String getJdbcUrl(ServletContext ctx) {
        String jdbcUrl = ConfigurationProperties.getBean(ctx).getProperty(
                "VitroConnection.DataSource.url");

        // Ensure that MySQL handles unicode properly, else all kinds of
        // horrible nastiness ensues.
        if ("MySQL".equals(getDbType(ctx)) && !jdbcUrl.contains("?")) {
            jdbcUrl += "?useUnicode=yes&characterEncoding=utf8";
        }
        
        return jdbcUrl;
        
    }
    
    /**
    * Sets up a Model and DB connection using values from
    * a properties file.
    */
    public final Model makeDBModelFromConfigurationProperties(
            String jenaDbModelName, 
            OntModelSpec jenaDbOntModelSpec, ServletContext ctx) {
       
        String jdbcUrl = getJdbcUrl(ctx);
   
        String username = ConfigurationProperties.getBean(ctx).getProperty(
                "VitroConnection.DataSource.username");
        String password = ConfigurationProperties.getBean(ctx).getProperty(
                "VitroConnection.DataSource.password");
        DataSource ds = makeC3poDataSource(
                getDbDriverClassName(ctx), jdbcUrl, username, password, ctx);
//        DataSource ds = makeBasicDataSource(
//                getDbDriverClassName(ctx), jdbcUrl, username, password, ctx);
       jenaDbOntModelSpec = (jenaDbOntModelSpec != null) 
               ? jenaDbOntModelSpec 
               : DB_ONT_MODEL_SPEC;
       
       return makeDBModel(ds, jenaDbModelName, jenaDbOntModelSpec, ctx);
       
   }
   
    /**
    * Sets up a DataSource using values from
    * a properties file.
    */
    public final DataSource makeDataSourceFromConfigurationProperties(
            ServletContext ctx) {
        String dbDriverClassname = ConfigurationProperties.getBean(ctx)
                .getProperty("VitroConnection.DataSource.driver",
                        getDbDriverClassName(ctx));
        String jdbcUrl = getJdbcUrl(ctx);
        String username = ConfigurationProperties.getBean(ctx).getProperty(
                "VitroConnection.DataSource.username");
        String password = ConfigurationProperties.getBean(ctx).getProperty(
                "VitroConnection.DataSource.password");
        return makeC3poDataSource(
                dbDriverClassname, jdbcUrl, username, password, ctx);
//        makeBasicDataSource(
//                dbDriverClassname, jdbcUrl, username, password, ctx);
    }
   
   public void setApplicationDataSource(DataSource ds, 
                                        ServletContext ctx) {
       ctx.setAttribute(getDataSourceAttributeName(), ds);
   }
   
   public static DataSource getApplicationDataSource(ServletContext ctx) {
       return (DataSource) ctx.getAttribute(getDataSourceAttributeName());
   }
   
   private static String getDataSourceAttributeName() {
       return JenaDataSourceSetupBase.class.getName() + ".dataSource";
   }
   
   public static DataSource makeC3poDataSource(String dbDriverClassname,
                                                     String jdbcUrl,
                                                     String username, 
                                                     String password, 
                                                     ServletContext ctx) {
       
       ComboPooledDataSource cpds = new ComboPooledDataSource();
       try {
           cpds.setDriverClass(dbDriverClassname);
       } catch (PropertyVetoException pve) {
           throw new RuntimeException(pve);
       }
       cpds.setJdbcUrl(jdbcUrl);
       cpds.setUser(username);
       cpds.setPassword(password);
       int[] maxActiveAndIdle = getMaxActiveAndIdle(ctx);
       cpds.setMaxPoolSize(maxActiveAndIdle[0]);
       cpds.setMinPoolSize(maxActiveAndIdle[1]);
       cpds.setMaxIdleTime(3600); // ms
       cpds.setMaxIdleTimeExcessConnections(300);
       cpds.setAcquireIncrement(5);
       cpds.setNumHelperThreads(6);
       cpds.setTestConnectionOnCheckout(DEFAULT_TESTONBORROW);
       cpds.setTestConnectionOnCheckin(DEFAULT_TESTONRETURN);
       cpds.setPreferredTestQuery(getValidationQuery(ctx));
       return cpds;
   }

   private static int[] getMaxActiveAndIdle(ServletContext ctx) {
       int maxActiveInt = DEFAULT_MAXACTIVE;
       String maxActiveStr = ConfigurationProperties.getBean(ctx).getProperty(
                MAX_ACTIVE_PROPERTY);
       if (!StringUtils.isEmpty(maxActiveStr)) {
           try {
               int maxActiveIntFromConfigProperties = Integer.parseInt(maxActiveStr);
               if (maxActiveIntFromConfigProperties < MINIMUM_MAXACTIVE) {
                   log.warn("Specified value for " + MAX_ACTIVE_PROPERTY + 
                            " is too low. Using minimum value of " + 
                            MINIMUM_MAXACTIVE);
                   maxActiveInt = MINIMUM_MAXACTIVE;
               } else {
                   maxActiveInt = maxActiveIntFromConfigProperties;
               }
           } catch (NumberFormatException nfe) {
               log.error("Unable to parse connection pool maxActive setting " 
                       + maxActiveStr + " as an integer");
           }
       }
       String maxIdleStr = ConfigurationProperties.getBean(ctx).getProperty(
               MAX_IDLE_PROPERTY);
       int maxIdleInt = (maxActiveInt > DEFAULT_MAXACTIVE) 
               ? maxActiveInt / 4
               : DEFAULT_MAXIDLE;
       if (!StringUtils.isEmpty(maxIdleStr)) {
           try {
               maxIdleInt = Integer.parseInt(maxIdleStr);    
           } catch (NumberFormatException nfe) {
               log.error("Unable to parse connection pool maxIdle setting " 
                       + maxIdleStr + " as an integer");
           }
       }       
       int[] result = new int[2];
       result[0] = maxActiveInt;
       result[1] = maxIdleInt;
       return result;
   }
   
   public static DataSource makeBasicDataSource(String dbDriverClassname,
                                                     String jdbcUrl,
                                                     String username, 
                                                     String password, 
                                                     ServletContext ctx) {
       log.debug("makeBasicDataSource('" + dbDriverClassname + "', '"
            + jdbcUrl + "', '" + username + "', '" + password + "')");
       BasicDataSource ds = new BasicDataSource();
       ds.setDriverClassName(dbDriverClassname);
       ds.setUrl(jdbcUrl);
       ds.setUsername(username);
       ds.setPassword(password);
       int[] maxActiveAndIdle = getMaxActiveAndIdle(ctx);
       ds.setMaxActive(maxActiveAndIdle[0]);
       ds.setMaxIdle(maxActiveAndIdle[1]);
       ds.setMaxWait(DEFAULT_MAXWAIT);
       ds.setValidationQuery(getValidationQuery(ctx));
       ds.setTestOnBorrow(DEFAULT_TESTONBORROW);
       ds.setTestOnReturn(DEFAULT_TESTONRETURN);
       ds.setMinEvictableIdleTimeMillis(DEFAULT_MINEVICTIONIDLETIME);
       ds.setNumTestsPerEvictionRun(maxActiveAndIdle[0]);
       ds.setTimeBetweenEvictionRunsMillis(DEFAULT_TIMEBETWEENEVICTIONS);
       ds.setInitialSize(ds.getMaxActive() / 10);

       try {
           ds.getConnection().close();
       } catch (Exception e) {
           e.printStackTrace();
       }

       return ds;
   }
   
   public enum TripleStoreType {
       RDB, SDB
   }
   
   public static boolean isFirstStartup() {
       return firstStartup;
   }
   
   public static void thisIsFirstStartup(){
       firstStartup = true;
   }
   
   protected Model makeDBModel(DataSource ds, 
                               String jenaDbModelname, 
                               OntModelSpec jenaDbOntModelSpec, 
                               ServletContext ctx) {
       return makeDBModel(
               ds, jenaDbModelname, jenaDbOntModelSpec, TripleStoreType.RDB, ctx);
   }
   
   protected Model makeDBModel(DataSource ds, 
           String jenaDbModelName, 
           OntModelSpec jenaDbOntModelSpec, 
           TripleStoreType storeType, ServletContext ctx) {
       return makeDBModel (ds, jenaDbModelName, jenaDbOntModelSpec, storeType, 
               getDbType(ctx), ctx);
   }
   
   public static Model makeDBModel(DataSource ds, 
                               String jenaDbModelName, 
                               OntModelSpec jenaDbOntModelSpec, 
                               TripleStoreType storeType, String dbType, 
                               ServletContext ctx) {
       Model dbModel = null;
       try {
           //  open the db model
        try {
            Graph g = null;
            switch (storeType) {
                case RDB:
                    g = new RegeneratingGraph(
                            new RDBGraphGenerator(
                                    ds, dbType, jenaDbModelName)); 
                    break;
                case SDB:
                    String layoutStr = ConfigurationProperties.getBean(ctx)
                            .getProperty(
                                    "VitroConnection.DataSource.sdb.layout",
                                    "layout2/hash");
                    String dbtypeStr = ConfigurationProperties.getBean(ctx)
                            .getProperty("VitroConnection.DataSource.dbtype",
                                    "MySQL");
                    StoreDesc desc = new StoreDesc(
                            LayoutType.fetch(layoutStr),
                            DatabaseType.fetch(dbtypeStr) );
                    g = new RegeneratingGraph(
                            new SDBGraphGenerator(
                                    ds, desc, jenaDbModelName)); 
                    break;
                default: throw new RuntimeException (
                        "Unsupported store type " + storeType); 
            }
            dbModel = ModelFactory.createModelForGraph(g);
            //log.debug("Using database at " + ds.getUrl());
            } catch (Throwable t) {
                t.printStackTrace();
            }
       } catch (Throwable t) {
           t.printStackTrace();
       }
       return dbModel;
   }

    public static void readOntologyFilesInPathSet(String path,
            ServletContext ctx, Model model) {
        log.debug("Reading ontology files from '" + path + "'");
        Set<String> paths = ctx.getResourcePaths(path);
        if (paths != null) {
            for (String p : paths) {
               readOntologyFileFromPath(p, model, ctx);
            }
        }
    }
   
    public static void readOntologyFileFromPath(String p, 
                                                Model model, 
                                                ServletContext ctx) {
        //Check that this is a file and not a directory
        File f = new File(ctx.getRealPath(p));
        if(f.exists() && f.isFile()){
            String format = getRdfFormat(p);
             log.info("Loading ontology file at " + p + 
                      " as format " + format);
             InputStream ontologyInputStream = ctx.getResourceAsStream(p);
             try {
                 model.read(ontologyInputStream, null, format);
                 log.debug("...successful");
             } catch (Throwable t) {
                 log.error("Failed to load ontology file at '" + p + 
                           "' as format " + format, t);
             }
        } else {
            if(!f.exists()) {
                log.info("File for path " + p + " does not exist");
            }
            else if(f.isDirectory()) {
                log.info("Path " + p + 
                        " corresponds to directory and not file so was not read in");
            }
        }
   }
    
    private static String getRdfFormat(String filename){
        String defaultformat = "RDF/XML";
        if( filename == null )
            return defaultformat;
        else if( filename.endsWith("n3") )
            return "N3";
        else if( filename.endsWith("ttl") )
            return "TURTLE";
        else 
            return defaultformat;
    }
    
    private static VitroJenaModelMaker vjmm = null;
    private static ModelMaker vsmm = null;
    private static VitroModelSource vms = null;
    private static final String sdbModelMaker = "vitroJenaSDBModelMaker";
    private static final String rdbModelMaker = "vitroJenaModelMaker";
    private static final String vitroModelSource = "vitroModelSource";
    
    //bdc34: is there any good reason that this doesn't just return the objects instead
    //of oddly passing them around as static properties on this class?
    protected void makeModelMakerFromConnectionProperties(TripleStoreType type, 
                                                          ServletContext ctx) {
        String jdbcUrl = getJdbcUrl(ctx);
        String dbtypeStr = ConfigurationProperties.getBean(ctx).getProperty(
                "VitroConnection.DataSource.dbtype","MySQL");
        String username = ConfigurationProperties.getBean(ctx).getProperty(
                "VitroConnection.DataSource.username");
        String password = ConfigurationProperties.getBean(ctx).getProperty(
                "VitroConnection.DataSource.password");
        
        if (TripleStoreType.RDB.equals(type)){
            vjmm = new VitroJenaModelMaker(
                    jdbcUrl, username, password, dbtypeStr, ctx);
        } else if (TripleStoreType.SDB.equals(type)) {
            StoreDesc storeDesc = new StoreDesc(
                    LayoutType.LayoutTripleNodesHash, DatabaseType.fetch(dbtypeStr));
            DataSource bds = WebappDaoSetup.makeC3poDataSource(
                    getDbDriverClassName(ctx), jdbcUrl, username, password, ctx);
//            DataSource bds = WebappDaoSetup.makeBasicDataSource(
//                    getDbDriverClassName(ctx), jdbcUrl, username, password, ctx);
//            bds.setMaxActive(4); // for now, the SDB model makers should not use more
//                                 // than a small handful of connections
//            bds.setMaxIdle(2);
            try {
                vsmm = new VitroJenaSDBModelMaker(storeDesc, bds);
            } catch (SQLException sqle) {
                log.error("Unable to set up SDB ModelMaker", sqle);
            }
        }
        
        return;
        
    }
    
    /**
     * Read all the files in the directory as RDF files
     * and return a model build from all RDF data found in those files.
     * This will attempt to load formats supported by getRdfFormat().
     */
    public static OntModel getModelFromDir( File dir){
        if( dir == null )
            throw new IllegalStateException("Must pass a File to getModelFromDir()");
        if( !dir.isDirectory() )
            throw new IllegalStateException(
                    "Directory must be a File object for a directory");
        if( !dir.canRead() )
            throw new IllegalStateException("getModelFromDir(): Directory " +
                    " must be readable, check premissions on " 
                    + dir.getAbsolutePath());
        
        OntModel model = ModelFactory.createOntologyModel();
        for( File file : dir.listFiles()){
            if( file.isFile() 
                && file.canRead() 
                && file.getName() != null ){
                String format = getRdfFormat( file.getName() );
                try{                   
                    model.read( new FileInputStream(file), null, format);
                    log.info("read in file " + file.getCanonicalPath() );
                }catch( Throwable th){
                    log.warn("Could not load file " + 
                            file.getAbsolutePath() + file.separator + file.getName() +
                            " check that it contains valid " + format + " data.", 
                            th);
                }                
            }
        }                                               
        return model;
    }
    
    public static void setVitroJenaModelMaker(VitroJenaModelMaker vjmm, 
                                              ServletContext ctx){
        ctx.setAttribute(rdbModelMaker, vjmm);
    }
    
    public static void setVitroJenaSDBModelMaker(ModelMaker vsmm, 
                                                 ServletContext ctx){
        ctx.setAttribute(sdbModelMaker, vsmm);
    }
    
    public static void setVitroModelSource(VitroModelSource vms,ServletContext ctx) {
        ctx.setAttribute( vitroModelSource, vms);
        
    }
    
    protected String getDefaultNamespace(ServletContext ctx) {
        String dns = ConfigurationProperties.getBean(ctx).getProperty(
                VITRO_DEFAULT_NAMESPACE);
        if ((dns != null) && (!dns.isEmpty())) {
            return dns;
        } else {
            throw new IllegalStateException("runtime.properties does not "
                    + "contain a value for '" + VITRO_DEFAULT_NAMESPACE + "'");
        }
    }
    
    protected VitroJenaModelMaker getVitroJenaModelMaker(){
        return vjmm;
    }
    
    protected ModelMaker getVitroJenaSDBModelMaker(){
        return vsmm;
    }

    public static VitroModelSource getVitroModelSource(ServletContext ctx){
        return (VitroModelSource)ctx.getAttribute(vitroModelSource);
    }
    
    private static String getDbType(ServletContext ctx) {
        return ConfigurationProperties.getBean(ctx).getProperty( // database type
                "VitroConnection.DataSource.dbtype", "MySQL");
    }

    private static String getDbDriverClassName(ServletContext ctx) {
        return ConfigurationProperties.getBean(ctx).getProperty(
                "VitroConnection.DataSource.driver", "com.mysql.jdbc.Driver");

    }

    private static String getValidationQuery(ServletContext ctx) {
        return ConfigurationProperties.getBean(ctx).getProperty(
                "VitroConnection.DataSource.validationQuery", "SELECT 1");
    }
    
    public static void setStartupDataset(Dataset dataset, ServletContext ctx) {
        ctx.setAttribute("startupDataset", dataset);
    }
    
    public static Dataset getStartupDataset(ServletContext ctx) {
        Object o = ctx.getAttribute("startupDataset");
        return (o instanceof Dataset) ? ((Dataset) o) : null;
    }

    protected OntModel ontModelFromContextAttribute(ServletContext ctx,
            String attribute) {
        OntModel ontModel;
        Object attributeValue = ctx.getAttribute(attribute);
        if (attributeValue != null && attributeValue instanceof OntModel) {
            ontModel = (OntModel) attributeValue;
        } else {
            ontModel = ModelFactory.createOntologyModel(MEM_ONT_MODEL_SPEC);
            ctx.setAttribute(attribute, ontModel);
        }
        return ontModel;
    }
    
    protected static void repairAppMetadataModel(Model applicationMetadataModel,
            Model aboxAssertions, 
            Model aboxInferences) {

        log.info("Moving application metadata from ABox to dedicated model");
        getAppMetadata(aboxAssertions, applicationMetadataModel);
        getAppMetadata(aboxInferences, applicationMetadataModel);
        aboxAssertions.remove(applicationMetadataModel);
        aboxInferences.remove(applicationMetadataModel);

        return;
    }
    
    protected static void getAppMetadata(Model source, Model target) {
        
        String amdQuery = "DESCRIBE ?x WHERE { " +
                    "{?x a <" + VitroVocabulary.PORTAL +"> } UNION " +
                    "{?x a <" + VitroVocabulary.PROPERTYGROUP +"> } UNION " +
                    "{?x a <" + VitroVocabulary.CLASSGROUP +"> } } ";
        
        try {                        
            Query q = QueryFactory.create(amdQuery, Syntax.syntaxARQ);
            QueryExecution qe = QueryExecutionFactory.create(q, source);
            qe.execDescribe(target);
           } catch (Exception e) {
            log.error("unable to create the application metadata model",e);
        }    
        
           return;
    }
    
    private static final String STOREDESC_ATTR = "storeDesc";
    private static final String STORE_ATTR = "kbStore";
    
    public static void setApplicationStoreDesc(StoreDesc storeDesc, 
                                          ServletContext ctx) {
        ctx.setAttribute(STOREDESC_ATTR, storeDesc);
    }
   
    public static StoreDesc getApplicationStoreDesc(ServletContext ctx) {
        return (StoreDesc) ctx.getAttribute(STOREDESC_ATTR);
    }
    
    public static void setApplicationStore(Store store,
                                           ServletContext ctx) {
        ctx.setAttribute(STORE_ATTR, store);
    }
    
    public static Store getApplicationStore(ServletContext ctx) {
        return (Store) ctx.getAttribute(STORE_ATTR);
    }
}
