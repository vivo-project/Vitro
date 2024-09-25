package edu.cornell.mannlib.vitro.webapp.controller.api.software;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.mannlib.vitro.webapp.controller.api.sparqlquery.InvalidQueryTypeException;
import edu.cornell.mannlib.vitro.webapp.controller.api.sparqlquery.SparqlQueryApiExecutor;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.utils.http.AcceptHeaderParsingException;
import edu.cornell.mannlib.vitro.webapp.utils.http.NotAcceptableException;
import org.apache.jena.query.QueryParseException;

@WebServlet(name = "softwareController", urlPatterns = {"/software", "/software/*"}, loadOnStartup = 5)
public class SoftwareController extends HttpServlet {

    private final String FIND_ALL_QUERY =
            "PREFIX rdf:      <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
            "PREFIX rdfs:     <http://www.w3.org/2000/01/rdf-schema#>\n" +
            "PREFIX xsd:      <http://www.w3.org/2001/XMLSchema#>\n" +
            "PREFIX owl:      <http://www.w3.org/2002/07/owl#>\n" +
            "PREFIX swrl:     <http://www.w3.org/2003/11/swrl#>\n" +
            "PREFIX swrlb:    <http://www.w3.org/2003/11/swrlb#>\n" +
            "PREFIX vitro:    <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#>\n" +
            "PREFIX bibo:     <http://purl.org/ontology/bibo/>\n" +
            "PREFIX c4o:      <http://purl.org/spar/c4o/>\n" +
            "PREFIX cito:     <http://purl.org/spar/cito/>\n" +
            "PREFIX dcterms:  <http://purl.org/dc/terms/>\n" +
            "PREFIX event:    <http://purl.org/NET/c4dm/event.owl#>\n" +
            "PREFIX fabio:    <http://purl.org/spar/fabio/>\n" +
            "PREFIX foaf:     <http://xmlns.com/foaf/0.1/>\n" +
            "PREFIX geo:      <http://aims.fao.org/aos/geopolitical.owl#>\n" +
            "PREFIX obo:      <http://purl.obolibrary.org/obo/>\n" +
            "PREFIX vivo:     <http://vivoweb.org/ontology/core#>\n" +
            "PREFIX vcard:    <http://www.w3.org/2006/vcard/ns#>\n" +
            "\n" +
            "SELECT ?label ?author ?authorIdentifier ?datePublished ?funding ?funder ?keywords " +
            "?version ?abstract ?identifier ?sameAs ?url\n" +
            "WHERE\n" +
            "{\n" +
            "    ?software rdf:type obo:ERO_0000071\n" +
            "    OPTIONAL { ?software rdfs:label ?label }\n" +
            "    OPTIONAL { ?software vivo:relatedBy ?relatedObject .\n" +
            "        ?relatedObject vitro:mostSpecificType vivo:Authorship .\n" +
            "        OPTIONAL { ?relatedObject vivo:relates ?authorObject .\n" +
            "            OPTIONAL { ?authorObject rdfs:label ?author }\n" +
            "            OPTIONAL { ?authorObject vivo:orcidId ?authorIdentifier }\n" +
            "        }\n" +
            "        FILTER (?author != ?label)\n" +
            "    }\n" +
            "    OPTIONAL { ?software vivo:dateTimeValue ?dateObject .\n" +
            "        OPTIONAL { ?dateObject vivo:dateTime ?datePublished }\n" +
            "    }\n" +
            "    OPTIONAL { ?software vivo:informationResourceSupportedBy ?funderObject .\n" +
            "      OPTIONAL { ?funderObject vivo:assignedBy ?funder }\n" +
            "      OPTIONAL { ?funderObject rdfs:label ?funding }\n" +
            "    }\n" +
            "    OPTIONAL { ?software vivo:freetextKeywords ?keywords }\n" +
            "    OPTIONAL { ?software obo:ERO_0000072 ?version }\n" +
            "    OPTIONAL { ?software bibo:abstract ?abstract }\n" +
            "    OPTIONAL { ?software vivo:swhid ?identifier }\n" +
            "    OPTIONAL { ?software owl:sameAs ?sameAsObject .\n" +
            "        OPTIONAL { ?sameAsObject rdfs:label ?sameAs }\n" +
            "    }\n" +
            "    OPTIONAL { ?software obo:ARG_2000028 ?contactInfo .\n" +
            "        OPTIONAL { ?contactInfo vcard:hasURL ?url }\n" +
            "    }\n" +
            "} LIMIT 20\n";

    private final String FIND_BY_ID_QUERY_TEMPLATE =
            "PREFIX rdf:      <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
            "PREFIX rdfs:     <http://www.w3.org/2000/01/rdf-schema#>\n" +
            "PREFIX xsd:      <http://www.w3.org/2001/XMLSchema#>\n" +
            "PREFIX owl:      <http://www.w3.org/2002/07/owl#>\n" +
            "PREFIX swrl:     <http://www.w3.org/2003/11/swrl#>\n" +
            "PREFIX swrlb:    <http://www.w3.org/2003/11/swrlb#>\n" +
            "PREFIX vitro:    <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#>\n" +
            "PREFIX bibo:     <http://purl.org/ontology/bibo/>\n" +
            "PREFIX c4o:      <http://purl.org/spar/c4o/>\n" +
            "PREFIX cito:     <http://purl.org/spar/cito/>\n" +
            "PREFIX dcterms:  <http://purl.org/dc/terms/>\n" +
            "PREFIX event:    <http://purl.org/NET/c4dm/event.owl#>\n" +
            "PREFIX fabio:    <http://purl.org/spar/fabio/>\n" +
            "PREFIX foaf:     <http://xmlns.com/foaf/0.1/>\n" +
            "PREFIX geo:      <http://aims.fao.org/aos/geopolitical.owl#>\n" +
            "PREFIX obo:      <http://purl.obolibrary.org/obo/>\n" +
            "PREFIX vivo:     <http://vivoweb.org/ontology/core#>\n" +
            "PREFIX vcard:    <http://www.w3.org/2006/vcard/ns#>\n" +
            "\n" +
            "SELECT ?label ?author ?authorIdentifier ?datePublished ?funding ?funder ?keywords " +
            "?version ?abstract ?identifier ?sameAs ?url\n" +
            "WHERE\n" +
            "{\n" +
            "    BIND (<%s> AS ?software)\n" +
            "    ?software rdf:type obo:ERO_0000071\n" +
            "    OPTIONAL { ?software rdfs:label ?label }\n" +
            "    OPTIONAL { ?software vivo:relatedBy ?relatedObject .\n" +
            "        ?relatedObject vitro:mostSpecificType vivo:Authorship .\n" +
            "        OPTIONAL { ?relatedObject vivo:relates ?authorObject .\n" +
            "            OPTIONAL { ?authorObject rdfs:label ?author }\n" +
            "            OPTIONAL { ?authorObject vivo:orcidId ?authorIdentifier }\n" +
            "        }\n" +
            "        FILTER (?author != ?label)\n" +
            "    }\n" +
            "    OPTIONAL { ?software vivo:dateTimeValue ?dateObject .\n" +
            "        OPTIONAL { ?dateObject vivo:dateTime ?datePublished }\n" +
            "    }\n" +
            "    OPTIONAL { ?software vivo:informationResourceSupportedBy ?funderObject .\n" +
            "      OPTIONAL { ?funderObject vivo:assignedBy ?funder }\n" +
            "      OPTIONAL { ?funderObject rdfs:label ?funding }\n" +
            "    }\n" +
            "    OPTIONAL { ?software vivo:freetextKeywords ?keywords }\n" +
            "    OPTIONAL { ?software obo:ERO_0000072 ?version }\n" +
            "    OPTIONAL { ?software bibo:abstract ?abstract }\n" +
            "    OPTIONAL { ?software vivo:swhid ?identifier }\n" +
            "    OPTIONAL { ?software owl:sameAs ?sameAsObject .\n" +
            "        OPTIONAL { ?sameAsObject rdfs:label ?sameAs }\n" +
            "    }\n" +
            "    OPTIONAL { ?software obo:ARG_2000028 ?contactInfo .\n" +
            "        OPTIONAL { ?contactInfo vcard:hasURL ?url }\n" +
            "    }\n" +
            "}\n";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws IOException, ServletException {

        String pathInfo = req.getPathInfo();
        String queryString;

        if (pathInfo != null && pathInfo.length() > 1) {
            String softwareId = pathInfo.substring(1);
            String softwareUri = "http://vivo.mydomain.edu/individual/" + softwareId;
            queryString = String.format(FIND_BY_ID_QUERY_TEMPLATE, softwareUri);
        } else {
            queryString = FIND_ALL_QUERY;
        }

        RDFService rdfService = ModelAccess.on(getServletContext()).getRDFService();

        try {
            String format = "application/sparql-results+json";
            SparqlQueryApiExecutor core = SparqlQueryApiExecutor.instance(
                rdfService, queryString, format);
            resp.setContentType(core.getMediaType());

            core.executeAndFormat(resp.getOutputStream());
        } catch (InvalidQueryTypeException e) {
            do400BadRequest("Query type is not SELECT, ASK, CONSTRUCT, "
                + "or DESCRIBE: '" + queryString + "'", resp);
        } catch (QueryParseException e) {
            do400BadRequest("Failed to parse query: '" + queryString + "''", e,
                resp);
        } catch (NotAcceptableException | AcceptHeaderParsingException e) {
            do500InternalServerError("Problem with the page fields: the "
                + "selected fields do not include an "
                + "acceptable content type.", e, resp);
        } catch (RDFServiceException e) {
            do500InternalServerError("Problem executing the query.", e, resp);
        }
    }

    private void do400BadRequest(String message, HttpServletResponse resp)
        throws IOException {
        resp.setStatus(400);
        resp.getWriter().println(message);
    }

    private void do400BadRequest(String message, Exception e,
                                 HttpServletResponse resp) throws IOException {
        resp.setStatus(400);
        PrintWriter w = resp.getWriter();
        w.println(message);
        e.printStackTrace(w);
    }

    private void do500InternalServerError(String message, Exception e,
                                          HttpServletResponse resp) throws IOException {
        resp.setStatus(500);
        PrintWriter w = resp.getWriter();
        w.println(message);
        e.printStackTrace(w);
    }
}
