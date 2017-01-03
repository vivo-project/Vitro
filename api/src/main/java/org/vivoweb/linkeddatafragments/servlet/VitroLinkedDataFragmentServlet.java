package org.vivoweb.linkeddatafragments.servlet;

import com.google.gson.JsonObject;
import edu.cornell.mannlib.vitro.webapp.beans.Ontology;
import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.dao.OntologyDao;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import org.apache.commons.io.IOUtils;
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
import org.vivoweb.linkeddatafragments.views.HtmlTriplePatternFragmentWriterImpl;
import org.vivoweb.linkeddatafragments.views.LinkedDataFragmentWriterFactory;
import org.vivoweb.linkeddatafragments.datasource.rdfservice.RDFServiceBasedRequestProcessorForTPFs;
import org.vivoweb.linkeddatafragments.datasource.rdfservice.RDFServiceDataSource;
import org.vivoweb.linkeddatafragments.datasource.rdfservice.RDFServiceDataSourceType;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

/**
 * Servlet that responds with a Linked Data Fragment.
 */
public class VitroLinkedDataFragmentServlet extends VitroHttpServlet {

    private final static long serialVersionUID = 1L;

    private ConfigReader config;
    private final HashMap<String, IDataSource> dataSources = new HashMap<>();
    private final Collection<String> mimeTypes = new ArrayList<>();

    private File getConfigFile(ServletConfig config) throws IOException {
        String path = config.getServletContext().getRealPath("/");
        if (path == null) {
            // this can happen when running standalone
            path = System.getProperty("user.dir");
        }
        File cfg = new File(path, "config-example.json");
        if (!cfg.exists()) {
            throw new IOException("Configuration file " + cfg + " not found.");
        }
        if (!cfg.isFile()) {
            throw new IOException("Configuration file " + cfg + " is not a file.");
        }
        return cfg;
    }

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        try {
            ServletContext ctx = servletConfig.getServletContext();
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
            for (Entry<String, JsonObject> dataSource : config.getDataSources().entrySet()) {
                dataSources.put(dataSource.getKey(), DataSourceFactory.create(dataSource.getValue()));
            }

            // register content types
            MIMEParse.register("text/html");
            MIMEParse.register(Lang.TTL.getHeaderString());
            MIMEParse.register(Lang.JSONLD.getHeaderString());
            MIMEParse.register(Lang.NTRIPLES.getHeaderString());
            MIMEParse.register(Lang.RDFXML.getHeaderString());

            HtmlTriplePatternFragmentWriterImpl.setContextPath(servletConfig.getServletContext().getContextPath());
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

    private IDataSource getDataSource(HttpServletRequest request) throws DataSourceNotFoundException {
        String contextPath = request.getContextPath();
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
        int fileNamePos = request.getRequestURI().toLowerCase().lastIndexOf("/tpf/assets/");
        if (fileNamePos > 0) {
            try {
                String fileName = request.getRequestURI().substring(fileNamePos + 12);
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
                try {
                    response.setStatus(404);
                    writer.writeNotFound(response.getOutputStream(), request);
                } catch (Exception ex1) {
                    throw new ServletException(ex1);
                }
            } catch (Exception e) {
                response.setStatus(500);
                writer.writeError(response.getOutputStream(), e);
            }
          
        } catch (Exception e) {
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
        configJson.append("    \"RDFServiceDatasource\": \"" + RDFServiceDataSourceType.class.getCanonicalName() + "\"\n");
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

        configJson.append("  }\n");
        configJson.append("}\n");

        return configJson.toString();
    }
}
