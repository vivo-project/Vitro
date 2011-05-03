/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.store.DatabaseType;
import com.hp.hpl.jena.sdb.store.LayoutType;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.jena.JenaBaseDaoCon;
import edu.cornell.mannlib.vitro.webapp.dao.jena.RDBGraphGenerator;
import edu.cornell.mannlib.vitro.webapp.dao.jena.RegeneratingGraph;
import edu.cornell.mannlib.vitro.webapp.dao.jena.SDBGraphGenerator;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VitroJenaModelMaker;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VitroJenaSDBModelMaker;

public class JenaDataSourceSetupBase extends JenaBaseDaoCon {
    
    private static final Log log = LogFactory.getLog(
            JenaDataSourceSetupBase.class);

    protected final static int DEFAULT_MAXWAIT = 10000, // ms
            DEFAULT_MAXACTIVE = 40,
            DEFAULT_MAXIDLE = 10,
            DEFAULT_TIMEBETWEENEVICTIONS =  3 * 1000, // ms
            DEFAULT_TESTSPEREVICTION = DEFAULT_MAXACTIVE,
            DEFAULT_MINEVICTIONIDLETIME = 3 * 1000; // ms

    protected final static boolean DEFAULT_TESTONBORROW = true,
            DEFAULT_TESTONRETURN = true, DEFAULT_TESTWHILEIDLE = true;

    protected static String BASE = "/WEB-INF/ontologies/";
    protected static String USERPATH = BASE+"user/";
    protected static String SYSTEMPATH = BASE+"system/";
    protected static String AUTHPATH = BASE+"auth/";
    public static String APPPATH = BASE+"app/";
    protected static String SUBMODELS = "/WEB-INF/submodels/";

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

    static final String DEFAULT_DEFAULT_NAMESPACE = 
            "http://vitro.mannlib.cornell.edu/ns/default#";
   
    static String defaultNamespace = DEFAULT_DEFAULT_NAMESPACE; // FIXME
   
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
        BasicDataSource ds = makeBasicDataSource(
                getDbDriverClassName(ctx), jdbcUrl, username, password, ctx);

        String dns = ConfigurationProperties.getBean(ctx).getProperty(
                "Vitro.defaultNamespace");
        defaultNamespace = (dns != null && dns.length() > 0) ? dns : null;
       
       jenaDbOntModelSpec = (jenaDbOntModelSpec != null) 
               ? jenaDbOntModelSpec 
               : DB_ONT_MODEL_SPEC;
       
       return makeDBModel(ds, jenaDbModelName, jenaDbOntModelSpec, ctx);
       
   }
   
    /**
    * Sets up a BasicDataSource using values from
    * a properties file.
    */
    public final BasicDataSource makeDataSourceFromConfigurationProperties(ServletContext ctx){
		String dbDriverClassname = ConfigurationProperties.getBean(ctx)
				.getProperty("VitroConnection.DataSource.driver",
						getDbDriverClassName(ctx));
        String jdbcUrl = getJdbcUrl(ctx);
		String username = ConfigurationProperties.getBean(ctx).getProperty(
				"VitroConnection.DataSource.username");
		String password = ConfigurationProperties.getBean(ctx).getProperty(
				"VitroConnection.DataSource.password");
        return makeBasicDataSource(
                dbDriverClassname, jdbcUrl, username, password, ctx);
    }
   
   public void setApplicationDataSource(BasicDataSource bds, 
                                        ServletContext ctx) {
	   ctx.setAttribute(getDataSourceAttributeName(), bds);
   }
   
   public static BasicDataSource getApplicationDataSource(ServletContext ctx) {
	   return (BasicDataSource) ctx.getAttribute(getDataSourceAttributeName());
   }
   
   private static String getDataSourceAttributeName() {
	   return JenaDataSourceSetupBase.class.getName() + ".dataSource";
   }

   public static BasicDataSource makeBasicDataSource(String dbDriverClassname,
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
       int maxActiveInt = DEFAULT_MAXACTIVE;
		String maxActiveStr = ConfigurationProperties.getBean(ctx).getProperty(
				"VitroConnection.DataSource.pool.maxActive");
       if (!StringUtils.isEmpty(maxActiveStr)) {
           try {
               maxActiveInt = Integer.parseInt(maxActiveStr);    
           } catch (NumberFormatException nfe) {
               log.error("Unable to parse connection pool maxActive setting " 
                       + maxActiveStr + " as an integer");
               }
       }
       int maxIdleInt = (maxActiveInt > DEFAULT_MAXACTIVE) 
               ? maxActiveInt / 4
               : DEFAULT_MAXIDLE;
		String maxIdleStr = ConfigurationProperties.getBean(ctx).getProperty(
				"VitroConnection.DataSource.pool.maxIdle");
       if (!StringUtils.isEmpty(maxIdleStr)) {
           try {
               maxIdleInt = Integer.parseInt(maxIdleStr);    
           } catch (NumberFormatException nfe) {
               log.error("Unable to parse connection pool maxIdle setting " 
                       + maxIdleStr + " as an integer");
           }
       }
       ds.setMaxActive(maxActiveInt);
       ds.setMaxIdle(maxIdleInt);
       ds.setMaxWait(DEFAULT_MAXWAIT);
       ds.setValidationQuery(getValidationQuery(ctx));
       ds.setTestOnBorrow(DEFAULT_TESTONBORROW);
       ds.setTestOnReturn(DEFAULT_TESTONRETURN);
       ds.setMinEvictableIdleTimeMillis(DEFAULT_MINEVICTIONIDLETIME);
       ds.setNumTestsPerEvictionRun(maxActiveInt);
       ds.setTimeBetweenEvictionRunsMillis(DEFAULT_TIMEBETWEENEVICTIONS);

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
   
   protected Model makeDBModel(BasicDataSource ds, 
                               String jenaDbModelname, 
                               OntModelSpec jenaDbOntModelSpec, 
                               ServletContext ctx) {
	   return makeDBModel(
	           ds, jenaDbModelname, jenaDbOntModelSpec, TripleStoreType.RDB, ctx);
   }
   
   protected Model makeDBModel(BasicDataSource ds, 
           String jenaDbModelName, 
           OntModelSpec jenaDbOntModelSpec, 
           TripleStoreType storeType, ServletContext ctx) {
       return makeDBModel (
               ds, jenaDbModelName, jenaDbOntModelSpec, storeType, getDbType(ctx), ctx);
   }
   
   public static Model makeDBModel(BasicDataSource ds, 
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
    /**
     * If the {@link ConfigurationProperties} has a name for the initial admin
     * user, create the user and add it to the model.
     */
    protected void createInitialAdminUser(Model model, ServletContext ctx) {
        String initialAdminUsername = ConfigurationProperties
                .getBean(ctx).getProperty("initialAdminUser");
        if (initialAdminUsername == null) {
            return;
        }

        // A hard-coded MD5 encryption of "defaultAdmin"
        String initialAdminPassword = "22BA075EC8951A70960A0A95C0BC2294";

        String vitroDefaultNs = DEFAULT_DEFAULT_NAMESPACE;

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
    
    protected final static String DB_TYPE = "MySQL";
    private static VitroJenaModelMaker vjmm = null;
    private static VitroJenaSDBModelMaker vsmm = null;
    private static final String sdbModelMaker = "vitroJenaSDBModelMaker";
    private static final String rdbModelMaker = "vitroJenaModelMaker";
    
    protected void makeModelMakerFromConnectionProperties(TripleStoreType type, ServletContext ctx){
    	String jdbcUrl = getJdbcUrl(ctx);
    	String username = ConfigurationProperties.getBean(ctx).getProperty(
    	        "VitroConnection.DataSource.username");
    	String password = ConfigurationProperties.getBean(ctx).getProperty(
    	        "VitroConnection.DataSource.password");
    	
    	if (TripleStoreType.RDB.equals(type)){
    		vjmm = new VitroJenaModelMaker(
    		        jdbcUrl, username, password, DB_TYPE, ctx);
    	}
    	
    	else if(TripleStoreType.SDB.equals(type)){
    		StoreDesc storeDesc = new StoreDesc(
    		        LayoutType.LayoutTripleNodesHash, DatabaseType.MySQL);
    	    BasicDataSource bds = JenaDataSourceSetup.makeBasicDataSource(
    	            getDbDriverClassName(ctx), jdbcUrl, username, password, ctx);
    	    try {
    	        vsmm = new VitroJenaSDBModelMaker(storeDesc, bds);
    	    } catch (SQLException sqle) {
    	        log.error("Unable to set up SDB ModelMaker", sqle);
    	    }
    	}
    	
		return;
		
    }
    
    public static void setVitroJenaModelMaker(VitroJenaModelMaker vjmm, 
                                              ServletContextEvent sce){
    	sce.getServletContext().setAttribute(rdbModelMaker, vjmm);
    }
    
    public static void setVitroJenaSDBModelMaker(VitroJenaSDBModelMaker vsmm, 
                                                 ServletContextEvent sce){
    	sce.getServletContext().setAttribute(sdbModelMaker, vsmm);
    }
    
	public static boolean isSDBActive(ServletRequest req) {
		if (!(req instanceof HttpServletRequest)) {
			return false;
		}
		HttpServletRequest hreq = (HttpServletRequest) req;
		return isSDBActive(hreq.getSession().getServletContext());
	}
    
    public static boolean isSDBActive(ServletContext ctx) {
    	String tripleStoreTypeStr = 
    		ConfigurationProperties.getBean(ctx).getProperty(
    				"VitroConnection.DataSource.tripleStoreType", "RDB");
    	return ("SDB".equals(tripleStoreTypeStr)); 
    }
    
    protected VitroJenaModelMaker getVitroJenaModelMaker(){
    	return vjmm;
    }
    
    protected VitroJenaSDBModelMaker getVitroJenaSDBModelMaker(){
    	return vsmm;
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

}
