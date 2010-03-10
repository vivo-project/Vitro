/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import java.io.InputStream;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

import edu.cornell.mannlib.vitro.webapp.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
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

   protected static String BASE = "/WEB-INF/ontologies/";
   protected static String USERPATH = BASE+"user/";
   protected static String SYSTEMPATH = BASE+"system/";
   protected static String AUTHPATH = BASE+"auth/";

   String DB_USER =   "jenatest";                          // database user id
   String DB_PASSWD = "jenatest";                          // database password
   String DB =        "MySQL";                             // database type
   String DB_DRIVER_CLASS_NAME = "com.mysql.jdbc.Driver";
   static final String JENA_DB_MODEL = "http://vitro.mannlib.cornell.edu/default/vitro-kb-2";
   static final String JENA_AUDIT_MODEL = "http://vitro.mannlib.cornell.edu/ns/db/experimental/audit";
   static final String JENA_INF_MODEL = "http://vitro.mannlib.cornell.edu/default/vitro-kb-inf";
   static final String JENA_USER_ACCOUNTS_MODEL = "http://vitro.mannlib.cornell.edu/default/vitro-kb-userAccounts";
   static final String JENA_APPLICATION_METADATA_MODEL = "http://vitro.mannlib.cornell.edu/default/vitro-kb-applicationMetadata";

   static final String DEFAULT_DEFAULT_NAMESPACE = "http://vitro.mannlib.cornell.edu/ns/default#";
   
   static String defaultNamespace = DEFAULT_DEFAULT_NAMESPACE; // TODO: improve this
   
   // use OWL models with no reasoning
   static final OntModelSpec DB_ONT_MODEL_SPEC = OntModelSpec.OWL_MEM;
   static final OntModelSpec MEM_ONT_MODEL_SPEC = OntModelSpec.OWL_MEM; 
   
   private static final Logger log = Logger.getLogger(JenaDataSourceSetupBase.class);   
   
    /**
    * Sets up a Model and DB connection using values from
    * a properties file.
    */
   public final Model makeDBModelFromConfigurationProperties(String jenaDbModelName, OntModelSpec jenaDbOntModelSpec){
       String dbDriverClassname = ConfigurationProperties.getProperty("VitroConnection.DataSource.driver", DB_DRIVER_CLASS_NAME);
	   String jdbcUrl = ConfigurationProperties.getProperty("VitroConnection.DataSource.url") + "?useUnicode=yes&characterEncoding=utf8";
	   String username = ConfigurationProperties.getProperty("VitroConnection.DataSource.username");
	   String password = ConfigurationProperties.getProperty("VitroConnection.DataSource.password");
	   BasicDataSource ds = makeBasicDataSource(dbDriverClassname, jdbcUrl, username, password);
	
       String dns = ConfigurationProperties.getProperty("Vitro.defaultNamespace");
       defaultNamespace = (dns != null && dns.length()>0) ? dns : null;
       
       jenaDbOntModelSpec = (jenaDbOntModelSpec != null) ? jenaDbOntModelSpec : DB_ONT_MODEL_SPEC;
	   return makeDBModel(ds, jenaDbModelName, jenaDbOntModelSpec);
   }

   protected BasicDataSource makeBasicDataSource(String dbDriverClassname, String jdbcUrl, String username, String password) {
	   BasicDataSource ds = new BasicDataSource();
       ds.setDriverClassName(dbDriverClassname);
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

	public static void readOntologyFilesInPathSet(String path,
			ServletContext ctx, Model model) {
		log.debug("Reading ontology files from '" + path + "'");
		Set<String> paths = ctx.getResourcePaths(path);
		if (paths != null) {
			for (String p : paths) {
				log.debug("Loading ontology file at " + p);
				InputStream ontologyInputStream = ctx.getResourceAsStream(p);
				try {
					model.read(ontologyInputStream, null);
					log.debug("...successful");
				} catch (Throwable t) {
					log.error("Failed to load ontology file at '" + p + "'", t);
				}
			}
		}
	}
   
	/**
	 * If the {@link ConfigurationProperties} has a name for the initial admin
	 * user, create the user and add it to the model.
	 */
	protected void createInitialAdminUser(Model model) {
		String initialAdminUsername = ConfigurationProperties
				.getProperty("initialAdminUser");
		if (initialAdminUsername == null) {
			return;
		}

		// A hard-coded MD5 encryption of "defaultAdmin"
		String initialAdminPassword = "22BA075EC8951A70960A0A95C0BC2294";

		String vitroDefaultNs = "http://vitro.mannlib.cornell.edu/ns/vitro/default#";

		Resource user = model.createResource(vitroDefaultNs
				+ "defaultAdminUser");
		model.add(model.createStatement(user, model
				.createProperty(VitroVocabulary.RDF_TYPE), model
				.getResource(VitroVocabulary.USER)));
		model.add(model.createStatement(user, model
				.createProperty(VitroVocabulary.USER_USERNAME), model
				.createTypedLiteral(initialAdminUsername)));
		model.add(model.createStatement(user, model
				.createProperty(VitroVocabulary.USER_MD5PASSWORD), model
				.createTypedLiteral(initialAdminPassword)));
		model.add(model.createStatement(user, model
				.createProperty(VitroVocabulary.USER_ROLE), model
				.createTypedLiteral("role:/50")));
	}

}
