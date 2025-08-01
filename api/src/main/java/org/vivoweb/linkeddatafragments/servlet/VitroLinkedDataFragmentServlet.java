package org.vivoweb.linkeddatafragments.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.riot.Lang;
import org.linkeddatafragments.config.ConfigReader;
import org.linkeddatafragments.datasource.DataSourceFactory;
import org.linkeddatafragments.datasource.DataSourceTypesRegistry;
import org.linkeddatafragments.datasource.IDataSource;
import org.linkeddatafragments.datasource.IDataSourceType;
import org.linkeddatafragments.datasource.index.IndexDataSource;
import org.linkeddatafragments.exceptions.DataSourceNotFoundException;
import org.linkeddatafragments.fragments.FragmentRequestParserBase;
import org.linkeddatafragments.fragments.ILinkedDataFragment;
import org.linkeddatafragments.fragments.ILinkedDataFragmentRequest;
import org.linkeddatafragments.util.MIMEParse;
import org.linkeddatafragments.views.ILinkedDataFragmentWriter;
import org.vivoweb.linkeddatafragments.datasource.rdfservice.RDFServiceBasedRequestProcessorForTPFs;
import org.vivoweb.linkeddatafragments.datasource.rdfservice.RDFServiceDataSourceType;
import org.vivoweb.linkeddatafragments.views.HtmlTriplePatternFragmentWriterImpl;
import org.vivoweb.linkeddatafragments.views.LinkedDataFragmentWriterFactory;

import com.fasterxml.jackson.databind.JsonNode;

import edu.cornell.mannlib.vitro.webapp.beans.Ontology;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.config.ContextPath;
import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.dao.OntologyDao;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;

/**
 * Servlet that responds with a Linked Data Fragment.
 */
@WebServlet(name = "TpfServlet", urlPatterns = {"/tpf/*"})
public class VitroLinkedDataFragmentServlet extends VitroHttpServlet {

	private final static long serialVersionUID = 1L;
	
	private static final String PROPERTY_TPF_ACTIVE_FLAG = "tpf.activeFlag";
	private static final Log log = LogFactory.getLog(VitroLinkedDataFragmentServlet.class);
	
    private ConfigReader config;
    private final HashMap<String, IDataSource> dataSources = new HashMap<>();
    private ConfigurationProperties configProps;
    private String tpfActiveFlag;

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        try {
            ServletContext ctx = servletConfig.getServletContext();
            configProps = ConfigurationProperties.getBean(ctx);
            
            if (!configurationPresent()) {
            	throw new ServletException("TPF is currently disabled. To enable, add '" 
                        + PROPERTY_TPF_ACTIVE_FLAG + " = true' to runtime.properties.");
            } else if (!tpfActiveFlag.equalsIgnoreCase("true")) {
        		throw new ServletException("TPF is currently disabled. To enable, set '" 
        	            + PROPERTY_TPF_ACTIVE_FLAG + " = true' in runtime.properties.");
            }
            
            RDFService rdfService = ModelAccess.on(ctx).getRDFService();
            RDFServiceBasedRequestProcessorForTPFs.setRDFService(rdfService);

            OntologyDao dao = ModelAccess.on(ctx).getWebappDaoFactory().getOntologyDao();

            // load the configuration
            config = new ConfigReader(new StringReader(getConfigJson(dao)));

            // register data source types
            for ( Entry<String,IDataSourceType> typeEntry : config.getDataSourceTypes().entrySet() ) {
                if (!DataSourceTypesRegistry.isRegistered(typeEntry.getKey())) {
                    DataSourceTypesRegistry.register( typeEntry.getKey(),
                            typeEntry.getValue() );
                }
            }

            // register data sources
            for (Entry<String, JsonNode> dataSource : config.getDataSources().entrySet()) {
                dataSources.put(dataSource.getKey(), DataSourceFactory.create(dataSource.getValue()));
            }

            // register content types
            MIMEParse.register("text/html");
            MIMEParse.register(Lang.TTL.getHeaderString());
            MIMEParse.register(Lang.JSONLD.getHeaderString());
            MIMEParse.register(Lang.NTRIPLES.getHeaderString());
            MIMEParse.register(Lang.RDFXML.getHeaderString());

            HtmlTriplePatternFragmentWriterImpl.setContextPath(ContextPath.getPath(ctx));
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    public void destroy()
    {
        for ( IDataSource dataSource : dataSources.values() ) {
            try {
                dataSource.close();
            }
            catch( Exception e ) {
                // ignore
            }
        }
    }

    private boolean configurationPresent() {
    	String activeFlag = configProps.getProperty(PROPERTY_TPF_ACTIVE_FLAG);
    	if (StringUtils.isNotEmpty(activeFlag)) {
    		this.tpfActiveFlag = activeFlag;
    		return true;
    	} else {
    		return false;
    	}
    }
    
    private IDataSource getDataSource(HttpServletRequest request) throws DataSourceNotFoundException {
        String contextPath = ContextPath.getPath(request);
        String requestURI = request.getRequestURI();

        String path = contextPath == null
                ? requestURI
                : requestURI.substring(contextPath.length());

        if (path.startsWith("/tpf")) {
            path = path.substring(4);
        }

        if (path.equals("/") || path.isEmpty()) {
            final String baseURL = FragmentRequestParserBase.extractBaseURL(request, config);
            return new IndexDataSource(baseURL, dataSources);
        }

        String dataSourceName = path.substring(1);
        IDataSource dataSource = dataSources.get(dataSourceName);
        if (dataSource == null) {
            throw new DataSourceNotFoundException(dataSourceName);
        }

        return dataSource;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        int fileNamePos = request.getRequestURI().toLowerCase().lastIndexOf("tpf/assets/");
        if (fileNamePos > 0) {
            try {
                String fileName = request.getRequestURI().substring(fileNamePos + 11);
                InputStream in = VitroLinkedDataFragmentServlet.class.getResourceAsStream(fileName);
                if (in != null) {
                    IOUtils.copy(in, response.getOutputStream());
                }
                return;
            } catch (IOException ioe) {
            }
        }

        ILinkedDataFragment fragment = null;
        try {
            // do conneg
            String bestMatch = MIMEParse.bestMatch(request.getHeader("Accept"));

            // set additional response headers
            response.setHeader("Server", "Linked Data Fragments Server");
            response.setContentType(bestMatch);
            response.setCharacterEncoding("utf-8");

            // create a writer depending on the best matching mimeType
            ILinkedDataFragmentWriter writer = LinkedDataFragmentWriterFactory.create(config.getPrefixes(), dataSources, bestMatch);

            try {

                final IDataSource dataSource = getDataSource( request );

                final ILinkedDataFragmentRequest ldfRequest =
                        dataSource.getRequestParser()
                                  .parseIntoFragmentRequest( request, config );

                fragment = dataSource.getRequestProcessor()
                                  .createRequestedFragment( ldfRequest );

                response.setHeader("Access-Control-Allow-Origin", "*");
                writer.writeFragment(response.getOutputStream(), dataSource, fragment, ldfRequest);

            } catch (DataSourceNotFoundException ex) {
                log.error(ex, ex);
                try {
                    response.setStatus(404);
                    writer.writeNotFound(response.getOutputStream(), request);
                } catch (Exception ex1) {
                    log.error(ex1, ex1);
                    throw new ServletException(ex1);
                }
            } 

        } catch (Exception e) {
            log.error(e, e);
            throw new ServletException(e);
        }
        finally {
            // close the fragment
            if ( fragment != null ) {
                try {
                    fragment.close();
                }
                catch ( Exception e ) {
                    // ignore
                }
            }
        }
    }

    private String getConfigJson(OntologyDao dao) {
        StringBuilder configJson = new StringBuilder();
        configJson.append("{\n");
        configJson.append("  \"title\": \"Linked Data Fragments server\",\n");
        configJson.append("\n");
        configJson.append("  \"datasourcetypes\": {\n");
        configJson.append("    \"RDFServiceDatasource\": \"").append(RDFServiceDataSourceType.class.getCanonicalName()).append("\"\n");
        configJson.append("  },\n");
        configJson.append("\n");
        configJson.append("  \"datasources\": {\n");
        configJson.append("    \"core\": {\n");
        configJson.append("      \"title\": \"core\",\n");
        configJson.append("      \"type\": \"RDFServiceDatasource\",\n");
        configJson.append("      \"description\": \"All data\"\n");
        configJson.append("    }\n");
        configJson.append("  },\n");
        configJson.append("\n");
        configJson.append("  \"prefixes\": {\n");
        configJson.append("    \"rdf\":         \"http://www.w3.org/1999/02/22-rdf-syntax-ns#\",\n");
        configJson.append("    \"rdfs\":        \"http://www.w3.org/2000/01/rdf-schema#\",\n");
        configJson.append("    \"hydra\": \"http://www.w3.org/ns/hydra/core#\",\n");
        configJson.append("    \"void\":        \"http://rdfs.org/ns/void#\"");

        List<Ontology> onts = dao.getAllOntologies();
        if (onts != null) {
            for (Ontology ont : onts) {
                if (ont != null && ont.getPrefix() != null) {
                    switch (ont.getPrefix()) {
                        case "rdf":
                        case "rdfs":
                        case "hydra":
                        case "void":
                            break;

                        default:
                            configJson.append(",\n");
                            configJson.append("    \"");
                            configJson.append(ont.getPrefix());
                            configJson.append("\":         \"");
                            configJson.append(ont.getURI());
                            configJson.append("\"");
                            break;
                    }
                }
            }
        }

        configJson.append("  }\n");
        configJson.append("}\n");

        return configJson.toString();
    }
}
