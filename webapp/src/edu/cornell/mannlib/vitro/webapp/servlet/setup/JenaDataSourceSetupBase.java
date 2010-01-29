package edu.cornell.mannlib.vitro.webapp.servlet.setup;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.cornell.mannlib.vitro.webapp.auth.policy.JenaNetidPolicy.ContextSetup;
import edu.cornell.mannlib.vitro.webapp.dao.jena.RDBGraphGenerator;
import edu.cornell.mannlib.vitro.webapp.dao.jena.RegeneratingGraph;

public class JenaDataSourceSetupBase {

    protected final static int DEFAULT_MAXWAIT = 10000, //ms
    DEFAULT_MAXACTIVE = 40,
    DEFAULT_MAXIDLE = 10,
    DEFAULT_TIMEBETWEENEVICTIONS = 30 * 60 * 1000, //ms
    DEFAULT_TESTSPEREVICTION = 3,
    DEFAULT_MINEVICTIONIDLETIME = 1000 * 60 * 30; //ms

    protected final static String DEFAULT_VALIDATIONQUERY = "SELECT 1";
    protected final static boolean DEFAULT_TESTONBORROW = true,
    DEFAULT_TESTONRETURN = true,
    DEFAULT_TESTWHILEIDLE = true;

   protected final static String CONNECTION_PROP_LOCATION = "/WEB-INF/classes/connection.properties";

   protected static String BASE = "/WEB-INF/ontologies/";
   protected static String USERPATH = BASE+"user/";
   protected static String SYSTEMPATH = BASE+"system/";
   protected static String AUTHPATH = BASE+"auth/";

   String DB_USER =   "jenatest";                          // database user id
   String DB_PASSWD = "jenatest";                          // database password
   String DB =        "MySQL";                             // database type
   String DB_DRIVER_CLASS_NAME = "com.mysql.jdbc.Driver";
   String JENA_DB_MODEL = "http://vitro.mannlib.cornell.edu/default/vitro-kb-2";
   String JENA_AUDIT_MODEL = "http://vitro.mannlib.cornell.edu/ns/db/experimental/audit";
   String JENA_INF_MODEL = "http://vitro.mannlib.cornell.edu/default/vitro-kb-inf";
   String JENA_USER_ACCOUNTS_MODEL = "http://vitro.mannlib.cornell.edu/default/vitro-kb-userAccounts";
   String JENA_APPLICATION_METADATA_MODEL = "http://vitro.mannlib.cornell.edu/default/vitro-kb-applicationMetadata";

   static final String DEFAULT_DEFAULT_NAMESPACE = "http://vitro.mannlib.cornell.edu/ns/default#";
   
   static String defaultNamespace = DEFAULT_DEFAULT_NAMESPACE; // TODO: improve this
   
   // use OWL models with no reasoning
   OntModelSpec DB_ONT_MODEL_SPEC = OntModelSpec.OWL_MEM;
   OntModelSpec MEM_ONT_MODEL_SPEC = OntModelSpec.OWL_MEM; 
   
   private static final Log log = LogFactory.getLog(JenaDataSourceSetupBase.class.getName());   
   
   /**
    * Sets up a Model and DB connection using values from 
    * a properties file.
    */
   public final Model makeDBModelFromPropertiesFile(final String filename) {
	   return makeDBModelFromPropertiesFile(filename, null, null);
   }
   
    /**
    * Sets up a Model and DB connection using values from
    * a properties file.
    */
   public final Model makeDBModelFromPropertiesFile(final String filename, String jenaDbModelName, OntModelSpec jenaDbOntModelSpec){

       if (filename == null || filename.length() <= 0) {
           throw new Error(
                   "To establish the DB model you MUST set the "
                   + "filename to the location of a "
                   + "connection.properties file with the database connection parameters.");
       }

       File propF = new File(filename );
       InputStream is;
       try {
           is = new FileInputStream(propF);
       } catch (FileNotFoundException e) {
           log.error("Could not load file "+filename);
           throw new Error("Could not load file " + filename
                   + '\n' + e.getMessage());
       }

       Properties dbProps = new Properties();
       try {
           dbProps.load(is);
           if (jenaDbModelName == null) {
        	   String specifiedModelName = dbProps.getProperty("Jena.modelName");
        	   jenaDbModelName = (specifiedModelName != null) ? specifiedModelName : JENA_DB_MODEL;
           }
           jenaDbOntModelSpec = (jenaDbOntModelSpec != null) ? jenaDbOntModelSpec : DB_ONT_MODEL_SPEC;
           String dns = dbProps.getProperty("Vitro.defaultNamespace");
           defaultNamespace = (dns != null && dns.length()>0) ? dns : null;
           return makeDBModelFromProperties(dbProps, jenaDbModelName, jenaDbOntModelSpec);
       } catch (IOException e) {
           throw new Error("Could not load properties from file " + filename + '\n'
                   + e.getMessage());
       }
   }

   protected BasicDataSource makeBasicDataSource(String dbDriverClassname, String jdbcUrl, String username, String password) {
	   BasicDataSource ds = new BasicDataSource();
       ds.setDriverClassName( (dbDriverClassname==null) ? DB_DRIVER_CLASS_NAME : dbDriverClassname );
       ds.setUrl(jdbcUrl);
       ds.setUsername(username);
       ds.setPassword(password);
       ds.setMaxActive(DEFAULT_MAXACTIVE);
       ds.setMaxIdle(DEFAULT_MAXIDLE);
       ds.setMaxWait(DEFAULT_MAXWAIT);
       ds.setValidationQuery(DEFAULT_VALIDATIONQUERY);
       ds.setTestOnBorrow(DEFAULT_TESTONBORROW);
       ds.setTestOnReturn(DEFAULT_TESTONRETURN);
       ds.setMinEvictableIdleTimeMillis(DEFAULT_MINEVICTIONIDLETIME);
       ds.setNumTestsPerEvictionRun(DEFAULT_TESTSPEREVICTION);
       ds.setTimeBetweenEvictionRunsMillis(DEFAULT_TIMEBETWEENEVICTIONS);

       try {
           ds.getConnection().close();
       } catch (Exception e) {
           e.printStackTrace();
       }

       //Class.forName(DB_DRIVER_CLASS_NAME);
       //Create database connection
       //IDBConnection conn = new DBConnection ( jdbcUrl, username, password, DB );
       //IDBConnection conn = new DBConnection(ds.getConnection(),DB);
       //ModelMaker maker = ModelFactory.createModelRDBMaker(conn) ;

       //ReconnectingGraphRDBMaker maker = new ReconnectingGraphRDBMaker(jdbcUrl, username, password, DB, null, ReificationStyle.Convenient);

       return ds;
   }
   
   private Model makeDBModel(BasicDataSource ds, String jenaDbModelName, OntModelSpec jenaDbOntModelSpec) {
       Model dbModel = null;
       try {
           //  open the db model
            try {
            	Graph g = new RegeneratingGraph(new RDBGraphGenerator(ds, DB, jenaDbModelName));
            	Model m = ModelFactory.createModelForGraph(g);
            	dbModel = m;
                //dbModel = ModelFactory.createOntologyModel(jenaDbOntModelSpec,m);
               
               //Graph g = maker.openGraph(JENA_DB_MODEL,false);
               //dbModel = ModelFactory.createModelForGraph(g);
               //maker.openModel(JENA_DB_MODEL);
               log.debug("Using database at "+ds.getUrl());
            } catch (Throwable t) {
                t.printStackTrace();
            }
       } catch (Throwable t) {
           t.printStackTrace();
       }

       return dbModel;
   }

   private Model makeDBModelFromProperties(Properties dbProps, String jenaDbModelName, OntModelSpec jenaDbOntModelSpec) {
       String dbDriverClassname = dbProps.getProperty("VitroConnection.DataSource.driver");
       String jdbcUrl = dbProps.getProperty("VitroConnection.DataSource.url") + "?useUnicode=yes&characterEncoding=utf8";
       String username = dbProps.getProperty("VitroConnection.DataSource.username");
       String password = dbProps.getProperty("VitroConnection.DataSource.password");
       BasicDataSource ds = makeBasicDataSource(dbDriverClassname, jdbcUrl, username, password);
       return makeDBModel(ds, jenaDbModelName, jenaDbOntModelSpec);
   }
   
   public static void readOntologyFilesInPathSet(String path, ServletContext ctx, Model model) {
	   Set<String> paths = ctx.getResourcePaths(path);
	   for(String p : paths) {
           log.debug("Loading ontology file at " + p);
           InputStream ontologyInputStream = ctx.getResourceAsStream(p);
           try {
               model.read(ontologyInputStream,null);
               log.debug("...successful");
           } catch (Throwable t) {
               log.debug("...unsuccessful");
           }
       }
   }
   
}
