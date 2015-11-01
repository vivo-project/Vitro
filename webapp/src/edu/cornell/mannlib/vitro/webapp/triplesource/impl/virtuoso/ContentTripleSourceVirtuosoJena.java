/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.triplesource.impl.virtuoso;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceDataset;
import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceModelMaker;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;
import edu.cornell.mannlib.vitro.webapp.modelaccess.adapters.MemoryMappingModelMaker;
import edu.cornell.mannlib.vitro.webapp.modelaccess.adapters.ModelMakerWithPersistentEmptyModels;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ontmodels.OntModelCache;
import edu.cornell.mannlib.vitro.webapp.modules.Application;
import edu.cornell.mannlib.vitro.webapp.modules.ComponentStartupStatus;
import edu.cornell.mannlib.vitro.webapp.modules.tripleSource.ContentTripleSource;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceFactory;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceFactorySingle;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.logging.LoggingRDFServiceFactory;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.virtuoso.RDFServiceVirtuosoJena;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.JenaDataSourceSetupBase;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;
import edu.cornell.mannlib.vitro.webapp.utils.logging.ToString;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import virtuoso.jdbc4.VirtuosoConnectionPoolDataSource;

import javax.servlet.ServletContext;
import javax.sql.ConnectionPoolDataSource;
import java.sql.SQLException;

import static edu.cornell.mannlib.vitro.webapp.triplesource.impl.BasicCombinedTripleSource.CONTENT_UNIONS;

/**
 * So far, it's just like a ContentTripleSourceSPARQL but it uses an instance of
 * RDFServiceVirtuoso.
 */
public class ContentTripleSourceVirtuosoJena extends ContentTripleSource {
	private final static Log log = LogFactory.getLog(ContentTripleSourceVirtuosoJena.class);

	private ServletContext ctx;
	private ConnectionPoolDataSource ds;

	private volatile RDFService rdfService;
	private RDFServiceFactory rdfServiceFactory;
	private RDFService unclosableRdfService;
	private Dataset dataset;
	private ModelMaker modelMaker;

	static class Configuration {
		String baseUri;
		String username;
		String password;
	}

	private static Configuration config = new Configuration();

	@Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#hasBaseURI")
	public void setBaseUri(String uri) {
		if (config.baseUri == null) {
			config.baseUri = uri;
		} else {
			throw new IllegalStateException("Configuration includes multiple instances of BaseURI: " + config.baseUri + ", and " + uri);
		}
	}

	@Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#hasUsername")
	public void setUsername(String user) {
		if (config.username == null) {
			config.username = user;
		} else {
			throw new IllegalStateException("Configuration includes multiple instances of Username: " + config.username + ", and " + user);
		}
	}

	@Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#hasPassword")
	public void setPassword(String pass) {
		if (config.password == null) {
			config.password = pass;
		} else {
			throw new IllegalStateException("Configuration includes multiple instances of Password: " + config.password + ", and " + pass);
		}
	}

	@Override
	public String toString() {
		return "ContentTripleSourceVirtuoso[" + ToString.hashHex(this) + ", baseUri=" + config.baseUri + ", username=" + config.username + "]";
	}

	@Override
	public RDFServiceFactory getRDFServiceFactory() {
		return this.rdfServiceFactory;
	}

	@Override
	public RDFService getRDFService() {
		return this.unclosableRdfService;
	}

	@Override
	public Dataset getDataset() {
		return this.dataset;
	}

	@Override
	public ModelMaker getModelMaker() {
		return this.modelMaker;
	}

	@Override
	public OntModelCache getShortTermOntModels(RDFService shortTermRdfService, OntModelCache longTermOntModelCache) {
		return longTermOntModelCache;
	}

	@Override
	public void startup(Application application, ComponentStartupStatus ss) {
		try {
			this.ctx = application.getServletContext();

			this.ds = new VirtuosoDataSource(this.ctx).getDataSource(config);


		this.rdfService = new RDFServiceVirtuosoJena(this.ds);
		this.rdfServiceFactory = createRDFServiceFactory();
		this.unclosableRdfService = this.rdfServiceFactory.getRDFService();
		this.dataset = new RDFServiceDataset(this.unclosableRdfService);
		this.modelMaker = createModelMaker();
		checkForFirstTimeStartup();
		ss.info("Initialized the RDF source for Virtuoso");

		} catch (SQLException e) {
			throw new RuntimeException(
					"Failed to set up the content data structures for Virtuoso", e);
		}
	}

	@Override
	public void shutdown(Application application) {
		synchronized (this) {
			if (this.rdfService != null) {
				this.rdfService.close();
				this.rdfService = null;
			}
		}
	}

	private RDFServiceFactory createRDFServiceFactory() {
		return new LoggingRDFServiceFactory(new RDFServiceFactorySingle(
				this.rdfService));
	}

	private ModelMaker createModelMaker() {
		return addContentDecorators(new ModelMakerWithPersistentEmptyModels(
				new MemoryMappingModelMaker(new RDFServiceModelMaker(
						this.unclosableRdfService), SMALL_CONTENT_MODELS)));
	}

	private void checkForFirstTimeStartup() {
		if (this.dataset.getNamedModel(ModelNames.TBOX_ASSERTIONS).getGraph().isEmpty()) {
			JenaDataSourceSetupBase.thisIsFirstStartup();
		}
	}
}
