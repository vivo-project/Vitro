package edu.cornell.mannlib.vitro.webapp.search.solr;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.CoreContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactoryJena;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceFactorySingle;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.model.RDFServiceModel;
import edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames;
import edu.cornell.mannlib.vitro.webapp.search.solr.documentBuilding.IndividualToSolrDocument;

/**
 * This tests what results will be returned for queries to the
 * Solr service.
 * 
 * The test will setup a temporary Solr service, setup a temporary RDF store, load the 
 * store with some individuals, and then index the individuals in the store.
 * 
 * Once this is done a set of test queries will be run against that service.
 * 
 * This code is based on a similar test from the CUL Discovery and Access
 * integration layer.
 * 
 * All RDF/XML files in webapp/test/testontologies/SolrQueryTestRDF will be 
 * loaded into the model.
 */
public class SolrQueryTest extends AbstractTestClass {
	/** Key of system property for build directory. */
	final static String buildDirSystemPropertyKey = "vitro.build.dir";
	
	/** Solr index to run test queries on. */
	static SolrServer solr = null;
	
    /** Container for solr */
    static CoreContainer coreContainer = null;

	/** Folder to store the temporary Solr index. */ 
    public static TemporaryFolder solrIndexFolder = null;

	/**
	 * This test needs to load RDF data to use as a 
	 * source of individuals to to build documents. 
	 * This is relative to the build directory.
	 */
	 final static String testRDFDir =  
        "/webapp/test/testontologies/SolrQueryTestRDF";
				
    @Before
    public void setup() throws Exception{
        //solr makes a lot of output
        suppressSysout();
        suppressSyserr();

		File buildDir = findBuildDir();
		
		solrIndexFolder = new TemporaryFolder();
		solrIndexFolder.create();
		
		File tempSolrBase = solrIndexFolder.newFolder("tempSolrBase");						
		FileUtils.copyDirectory(getSolrTemplateDir(buildDir), tempSolrBase );		
		solr = setupSolr( tempSolrBase );			
		
		indexRdf( loadTestRDF( buildDir ) );
    }

	@After
	public void takedown() throws Exception{
        if( coreContainer != null )
            coreContainer.shutdown();
            
        restoreOutputStreams();

		if( solrIndexFolder != null )
			solrIndexFolder.delete();
	}

	/**
	 * This will return the directory to use as the Solr
	 * home template directory.  
	 * 
	 * Throws an exception if the directory is not found.
	 * @param buildDir - must not be null, must be the base of the build.
	 */
	private   File getSolrTemplateDir(File buildDir) throws Exception {				
		if(buildDir == null || !buildDir.exists() )
			throw new Exception("buildDir must not be null");
		
		String solrTemplateDirName = buildDir.getAbsolutePath() + "/solr/homeDirectoryTemplate";
		File solrTemplateDir = new File(solrTemplateDirName); 
		
		if( solrTemplateDir == null || ! solrTemplateDir.exists())
			throw new Exception("Solr home directory template " +
					"was not found at " + solrTemplateDirName);
		else
			return solrTemplateDir;		
	}
	
	protected   SolrServer setupSolr(File solrBase) 
	throws ParserConfigurationException, IOException, SAXException{		
		System.setProperty("solr.solr.home", solrBase.getAbsolutePath());
		CoreContainer.Initializer initializer = new CoreContainer.Initializer();
		coreContainer = initializer.initialize();
		return new EmbeddedSolrServer(coreContainer, "");
	}

	private  OntModel loadTestRDF(File buildDir) throws Exception {
		String dirname = buildDir.getAbsolutePath() + testRDFDir;
		File rdfDir = new File( dirname );		
		assertNotNull("could not find dir " + dirname , rdfDir);
		assertTrue(dirname + " must be a directory." ,rdfDir.isDirectory());
		
		//load all files in test dir.
		File[] files = rdfDir.listFiles();
		assertNotNull("no test RDF files found",files);
		assertTrue("no test RDF files found", files.length > 0 );
		
		OntModel model = ModelFactory.createOntologyModel();
		for (File file : files ){
			InputStream in = FileManager.get().open( file.getAbsolutePath() );	
			assertNotNull("Could not load file " + file.getAbsolutePath(), in );
			try{
				model.read(in,null);
			}catch(Throwable th){
				throw new Exception( "Could not load RDF/XML file " 
						+ file.getAbsolutePath() , th);
			}
		}				 
		return  model ;
	}
	
	/** 
	 * Find the base of the build directories.
	 * @return This method will return a non-null file to use
	 * or it will throw an exception if none can be found.
	 * @throws Exception 
	 */
	private  File findBuildDir() throws Exception {							
		
		//First try to find the base directory 
		//of the build in the system properties.
		
		String buildDirName = System.getProperty(buildDirSystemPropertyKey);
		if( buildDirName != null ){
			File buildDir = new File(buildDirName);
			if( buildDir.exists() && buildDir.isDirectory() ){
				return buildDir;
			}
		}
		
		//If there is no system property try to 
		//guess the location based on the working directory 
		File f = null;		
		String[] fallBackBases = {"","../","../../"};
		List<String> attempted = new ArrayList<String>();
		for( String base: fallBackBases){
			String attemptedDir =base + "solr/homeDirectoryTemplate";		
			f = new File( attemptedDir );
			attempted.add( System.getProperty("user.dir") + '/' + attemptedDir );
			if( f != null && f.exists() && f.isDirectory() ){				
				f = new File( base );
				break;
			}else{
				f = null;
			}
		}				
		
		if( f == null ){
			throw new Exception(
				"Could not find the base of the " +
				"build directory for the project, " +
				"checked the system property " + buildDirSystemPropertyKey 
				+ " and checked in these locations: \n" +
				StringUtils.join(attempted,"\n  "));
		}else{
			return f;
		}
	}


	/** Query the RDF, build Solr Documents from results, load them to Solr. */
	private  void indexRdf(OntModel model) throws Exception {
		RDFServiceModel rdfService = new RDFServiceModel(model);
							
		IndividualToSolrDocument r2d =  
		    SolrSetup.setupTransltion(
		            model,
		            model,
		            new RDFServiceFactorySingle(rdfService),
		            null, null);
		
		WebappDaoFactory wdf = new WebappDaoFactoryJena(model);
		
		for( String uri: getURISToIndex( model ) ){
			SolrInputDocument doc;
			try {				
				doc = r2d.translate( wdf.getIndividualDao().getIndividualByURI(uri));
			} catch (Exception e) {
				throw new Exception("Failed on building document for uri:" + uri, e);
			}
			try {
				solr.add( doc );
			} catch (Exception e) {
				throw new Exception("Failed adding doc to solr for uri:" + uri, e);
			}
		}
		solr.commit();
	}
	
	private  List<String> getURISToIndex(Model model) {
		//just return all the URIs in the subject position		
		List<String> uris = new LinkedList<String>();
		ResIterator it = model.listSubjects();
		while(it.hasNext() ){
			Resource res = it.nextResource();
			if( res != null && res.isURIResource() ){
				uris.add( res.getURI() );
			}
		}
		return uris;		
	}

	@Test
	public void testSolrWasStarted() throws SolrServerException, IOException {
		assertNotNull( solr );
		solr.ping();//this will throw an exception of the server is not reachable 
	}
	
	@Test
	public void testCorsonSearch() throws SolrServerException{
		
		/* make sure that we have the document in the index before we do anything */
		SolrQuery query = new SolrQuery().setQuery("corson");

		testQueryGetsDocs("Expect to find a doc when searching for 'corson'",
				query,new String[]{ "http://vivo.cornell.edu/individual/individual22972" } ) ;
	}



	/** 
	 * Test that a document with the given URIs are in the results for the query. 
	 * @throws SolrServerException */
	void testQueryGetsDocs(String errmsg, SolrQuery query, String[] expectedUris) throws SolrServerException{
		assertNotNull(errmsg + " but query was null", query);
		assertNotNull(errmsg + " but expected URIs was null", expectedUris );
									
		QueryResponse resp = solr.query(query);
		if( resp == null )
			fail( errmsg + " but Could not get a solr response");
		
		Set<String> uris = new HashSet<String>(Arrays.asList( expectedUris ));
		for( SolrDocument doc : resp.getResults()){
			assertNotNull(errmsg + ": solr doc was null", doc);
			String uri = (String) doc.getFirstValue( VitroSearchTermNames.URI );
			assertNotNull(errmsg+": no URI field in solr doc" , uri);
			uris.remove( uri );
		}
		if( uris.size() > 0){
			String errorMsg = 
					"\nThe query '"+ query + "' was expected " +
					"to return the following URIs but did not:";
			for( String uri : uris){
				errorMsg= errorMsg+"\n" + uri;
			}					
			
			fail( errmsg + errorMsg);
		}
	}
}
