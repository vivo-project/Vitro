package edu.cornell.mannlib.vitro.webapp.visualization.personpubcount;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;

import com.hp.hpl.jena.iri.IRI;
import com.hp.hpl.jena.iri.IRIFactory;
import com.hp.hpl.jena.iri.Violation;
import com.hp.hpl.jena.query.DataSource;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.RDFNode;

import edu.cornell.mannlib.vitro.webapp.visualization.exceptions.MalformedQueryParametersException;
import edu.cornell.mannlib.vitro.webapp.visualization.sparqlutils.QueryConstants;
import edu.cornell.mannlib.vitro.webapp.visualization.sparqlutils.QueryFieldLabels;
import edu.cornell.mannlib.vitro.webapp.visualization.valueobjects.BiboDocument;



/**
 * Very dumb name of the class. change it.
 * @author cdtank
 *
 */
public class QueryHandler {

	protected static final Syntax SYNTAX = Syntax.syntaxARQ;

	private String queryParam, resultFormatParam, rdfResultFormatParam;
	private DataSource dataSource;

	private Log log;

	private static final String SPARQL_QUERY_COMMON_SELECT_CLAUSE = "" +
			"SELECT (str(?authorLabel) as ?authorLabelLit) " +
			"		(str(?document) as ?documentLit) " +
			"		(str(?documentMoniker) as ?documentMonikerLit) " +
			"		(str(?documentLabel) as ?documentLabelLit) " +
			"		(str(?documentBlurb) as ?documentBlurbLit) " +
			"		(str(?publicationYear) as ?publicationYearLit) " +
			"		(str(?documentDescription) as ?documentDescriptionLit) ";

	private static final String SPARQL_QUERY_COMMON_WHERE_CLAUSE = "" +
			"?document rdf:type bibo:Document ." +
			"?document rdfs:label ?documentLabel ." +
			"OPTIONAL {  ?document vivo:publicationYear ?publicationYear } ." +
			"OPTIONAL {  ?document vitro:moniker ?documentMoniker } ." +
			"OPTIONAL {  ?document vitro:blurb ?documentBlurb } ." +
			"OPTIONAL {  ?document vitro:description ?documentDescription }";

	public QueryHandler(String queryParam,
			String resultFormatParam, String rdfResultFormatParam,
			DataSource dataSource, Log log) {

		this.queryParam = queryParam;
		this.resultFormatParam = resultFormatParam;
		this.rdfResultFormatParam = rdfResultFormatParam;
		this.dataSource = dataSource;
		this.log = log;

	}

	private List<BiboDocument> createJavaValueObjects(ResultSet resultSet) {
		List<BiboDocument> authorDocuments = new ArrayList<BiboDocument>();

		while (resultSet.hasNext()) {
			QuerySolution solution = resultSet.nextSolution();

			BiboDocument biboDocument = new BiboDocument(
											solution.get(QueryFieldLabels.DOCUMENT_URL)
												.toString());

			RDFNode documentLabelNode = solution.get(QueryFieldLabels.DOCUMENT_LABEL);
			if (documentLabelNode != null) {
				biboDocument.setDocumentLabel(documentLabelNode.toString());
			}


			RDFNode documentBlurbNode = solution.get(QueryFieldLabels.DOCUMENT_BLURB);
			if (documentBlurbNode != null) {
				biboDocument.setDocumentBlurb(documentBlurbNode.toString());
			}

			RDFNode documentmonikerNode = solution.get(QueryFieldLabels.DOCUMENT_MONIKER);
			if (documentmonikerNode != null) {
				biboDocument.setDocumentMoniker(documentmonikerNode.toString());
			}

			RDFNode documentDescriptionNode = solution.get(QueryFieldLabels.DOCUMENT_DESCRIPTION);
			if (documentDescriptionNode != null) {
				biboDocument.setDocumentDescription(documentDescriptionNode.toString());
			}

			RDFNode publicationYearNode = solution.get(QueryFieldLabels.DOCUMENT_PUBLICATION_YEAR);
			if (publicationYearNode != null) {
				biboDocument.setPublicationYear(publicationYearNode.toString());
			}
			
			RDFNode authorURLNode = solution.get(QueryFieldLabels.AUTHOR_URL);
			if (authorURLNode != null) {
				biboDocument.setAuthorURL(authorURLNode.toString());
			}

			RDFNode authorLabelNode = solution.get(QueryFieldLabels.AUTHOR_LABEL);
			if (authorLabelNode != null) {
				biboDocument.setAuthorLabel(authorLabelNode.toString());
			}

			authorDocuments.add(biboDocument);
		}
		return authorDocuments;
	}

	private ResultSet executeQuery(String queryURI,
            String resultFormatParam, String rdfResultFormatParam, DataSource dataSource) {

        QueryExecution queryExecution = null;
        try{
            Query query = QueryFactory.create(generateSparqlQuery(queryURI), SYNTAX);

//            QuerySolutionMap qs = new QuerySolutionMap();
//            qs.add("authPerson", queryParam); // bind resource to s
            
            queryExecution = QueryExecutionFactory.create(query, dataSource);
            

            //remocve this if loop after knowing what is describe & construct sparql stuff.
            if( query.isSelectType() ){
                return queryExecution.execSelect();
            }
        } finally {
            if(queryExecution != null) {
            	queryExecution.close();
            }

        }
		return null;
    }

	private String generateSparqlQuery(String queryURI) {
//		Resource uri1 = ResourceFactory.createResource(queryURI);

		String sparqlQuery = QueryConstants.SPARQL_QUERY_PREFIXES
							+ SPARQL_QUERY_COMMON_SELECT_CLAUSE
							+ "(str(<" + queryURI + ">) as ?authPersonLit) "
							+ "WHERE { "
							+ "<" + queryURI + "> rdf:type foaf:Person ; vivo:authorOf ?document ; rdfs:label ?authorLabel.  "
							+  SPARQL_QUERY_COMMON_WHERE_CLAUSE
							+ "}";

		log.debug("SPARQL query for person pub count -> \n" + sparqlQuery);

		return sparqlQuery;
	}

	public List<BiboDocument> getVisualizationJavaValueObjects()
		throws MalformedQueryParametersException {

        if(this.queryParam == null || "".equals(queryParam)) {
        	throw new MalformedQueryParametersException("URI parameter is either null or empty.");
        } else {

        	/*
        	 * To test for the validity of the URI submitted.
        	 * */
        	IRIFactory iRIFactory = IRIFactory.jenaImplementation();
    		IRI iri = iRIFactory.create(this.queryParam);
            if (iri.hasViolation(false)) {
                String errorMsg = ((Violation)iri.violations(false).next()).getShortMessage()+" ";
                log.error("Pub Count vis Query " + errorMsg);
                throw new MalformedQueryParametersException("URI provided for an individual is malformed.");
            }
        }



		ResultSet resultSet	= executeQuery(this.queryParam,
										   this.resultFormatParam,
										   this.rdfResultFormatParam,
										   this.dataSource);

		return createJavaValueObjects(resultSet);
	}

	public Map<String, Integer> getYearToPublicationCount(
			List<BiboDocument> authorDocuments) {

		//List<Integer> publishedYears = new ArrayList<Integer>();

    	/*
    	 * Create a map from the year to number of publications. Use the BiboDocument's
    	 * parsedPublicationYear to populate the data.
    	 * */
    	Map<String, Integer> yearToPublicationCount = new TreeMap<String, Integer>();

    	for (BiboDocument curr : authorDocuments) {

    		/*
    		 * Increment the count because there is an entry already available for
    		 * that particular year.
    		 * */
    		String publicationYear;
    		if (curr.getPublicationYear() != null 
    				&& curr.getPublicationYear().length() != 0 
    				&& curr.getPublicationYear().trim().length() != 0) {
    			publicationYear = curr.getPublicationYear();
    		} else {
    			publicationYear = curr.getParsedPublicationYear();
    		}
    		
			if (yearToPublicationCount.containsKey(publicationYear)) {
    			yearToPublicationCount.put(publicationYear,
    									   yearToPublicationCount
    									   		.get(publicationYear) + 1);

    		} else {
    			yearToPublicationCount.put(publicationYear, 1);
    		}

//    		if (!parsedPublicationYear.equalsIgnoreCase(BiboDocument.DEFAULT_PUBLICATION_YEAR)) {
//    			publishedYears.add(Integer.parseInt(parsedPublicationYear));
//    		}

    	}

		return yearToPublicationCount;
	}





}
