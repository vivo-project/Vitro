/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.DataSource;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.resultset.ResultSetFormat;
import com.hp.hpl.jena.vocabulary.XSD;

import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.UseAdvancedDataToolsPages;
import edu.cornell.mannlib.vitro.webapp.beans.Ontology;
import edu.cornell.mannlib.vitro.webapp.dao.OntologyDao;
import edu.cornell.mannlib.vitro.webapp.utils.SparqlQueryUtils;


/**
 * Services a sparql query.  This will return a simple error message and a 501 if
 * there is no jena Model.
 *
 * @author bdc34
 *
 */
public class SparqlQueryServlet extends BaseEditController {
    private static final Log log = LogFactory.getLog(SparqlQueryServlet.class.getName());
    
    protected static HashMap<String,ResultSetFormat>formatSymbols = new HashMap<String,ResultSetFormat>();
    static{
        formatSymbols.put( ResultSetFormat.syntaxXML.getSymbol(),     ResultSetFormat.syntaxXML);
        formatSymbols.put( ResultSetFormat.syntaxRDF_XML.getSymbol(), ResultSetFormat.syntaxRDF_XML);
        formatSymbols.put( ResultSetFormat.syntaxRDF_N3.getSymbol(),  ResultSetFormat.syntaxRDF_N3);
        formatSymbols.put( ResultSetFormat.syntaxText.getSymbol() ,   ResultSetFormat.syntaxText);
        formatSymbols.put( ResultSetFormat.syntaxJSON.getSymbol() ,   ResultSetFormat.syntaxJSON);
        formatSymbols.put( "vitro:csv", null);
    }
    
    protected static HashMap<String,String> rdfFormatSymbols = new HashMap<String,String>();
    static {
    	rdfFormatSymbols.put( "RDF/XML", "application/rdf+xml" );
    	rdfFormatSymbols.put( "RDF/XML-ABBREV", "application/rdf+xml" );
    	rdfFormatSymbols.put( "N3", "text/n3" );
    	rdfFormatSymbols.put( "N-TRIPLE", "text/plain" );
    	rdfFormatSymbols.put( "TTL", "application/x-turtle" );
    }

    protected static HashMap<String, String>mimeTypes = new HashMap<String,String>();
    static{
        mimeTypes.put( ResultSetFormat.syntaxXML.getSymbol() ,         "text/xml" );
        mimeTypes.put( ResultSetFormat.syntaxRDF_XML.getSymbol(),      "application/rdf+xml"  );
        mimeTypes.put( ResultSetFormat.syntaxRDF_N3.getSymbol(),       "text/plain" );
        mimeTypes.put( ResultSetFormat.syntaxText.getSymbol() ,        "text/plain");
        mimeTypes.put( ResultSetFormat.syntaxJSON.getSymbol(),         "application/javascript" );
        mimeTypes.put( "vitro:csv",                                    "text/csv");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        this.doGet(request,response);
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {    	    	   	
    	if (!isAuthorizedToDisplayPage(request, response, new Actions(new UseAdvancedDataToolsPages()))) {
    		return;
    	}

        VitroRequest vreq = new VitroRequest(request);

        Model model = vreq.getJenaOntModel(); 
        if( model == null ){
            doNoModelInContext(response);
            return;
        }

        String queryParam = vreq.getParameter("query");
        log.debug("queryParam was : " + queryParam);

        String resultFormatParam = vreq.getParameter("resultFormat");
        log.debug("resultFormat was: " + resultFormatParam);
        
        String rdfResultFormatParam = vreq.getParameter("rdfResultFormat");
        if (rdfResultFormatParam == null) {
        	rdfResultFormatParam = "RDF/XML-ABBREV";
        }
        log.debug("rdfResultFormat was: " + rdfResultFormatParam);

        if( queryParam == null || "".equals(queryParam) ||
            resultFormatParam == null || "".equals(resultFormatParam) ||
            !formatSymbols.containsKey(resultFormatParam) || 
            rdfResultFormatParam == null || "".equals(rdfResultFormatParam) ||
            !rdfFormatSymbols.keySet().contains(rdfResultFormatParam) ) {
            doHelp(request,response);
            return;
        }
        
        Dataset dataset = chooseDatasetForQuery(vreq);

        executeQuery(response, resultFormatParam, rdfResultFormatParam, queryParam, dataset); 
        return;
    }
    
	private Dataset chooseDatasetForQuery(VitroRequest vreq) {
		Map<String, Model> modelMap = getModelsFromRequest(vreq);
		if (!modelMap.isEmpty()) {
			return buildDataSetFromNamedModels(modelMap);
		}
		
		String queryParam = vreq.getParameter("query");
		String[] tokens = queryParam.split("\\s");
	    for(int i = 0; i < tokens.length; i++){
	    	if("graph".equalsIgnoreCase(tokens[i])){
	    		return vreq.getDataset();
	    	}
	    }
		
		DataSource dataSource = DatasetFactory.create();
		dataSource.setDefaultModel(vreq.getJenaOntModel());
		return dataSource;
	}
    
	private Map<String, Model> getModelsFromRequest(HttpServletRequest request) {
		String modelNames[] = request.getParameterValues("sourceModelName");
		if ((modelNames == null) || (modelNames.length == 0)) {
			return Collections.emptyMap();
		}

		ModelMaker maker = (ModelMaker) getServletContext().getAttribute(
				"vitroJenaModelMaker");

		Map<String, Model> map = new HashMap<String, Model>();
		for (String modelName : modelNames) {
			Model model = maker.getModel(modelName);
			if (model != null) {
				map.put(modelName, model);
			}
		}

		return map;
	}

	private Dataset buildDataSetFromNamedModels(Map<String, Model> modelMap) {
		DataSource dataSource = DatasetFactory.create();
		for (String name : modelMap.keySet()) {
			Model model = modelMap.get(name);
			dataSource.addNamedModel(name, model);
		}

    	// For now, people expect to query these graphs without using 
    	// FROM NAMED, so we'll also add to the background
		OntModel ontModel = ModelFactory
				.createOntologyModel(OntModelSpec.OWL_MEM);
		for (String name : modelMap.keySet()) {
			Model model = modelMap.get(name);
			ontModel.addSubModel(model);
		}

		dataSource.setDefaultModel(ontModel);
		return dataSource;
	}

    
    private void executeQuery(HttpServletResponse response, 
                              String resultFormatParam, 
                              String rdfResultFormatParam, 
                              String queryParam, 
                              Dataset dataset ) throws IOException {
        
    	ResultSetFormat rsf = null;
    	/* BJL23 2008-11-06
    	 * modified to support CSV output.
    	 * Unfortunately, ARQ doesn't make it easy to
    	 * do this by implementing a new ResultSetFormat, because 
    	 * ResultSetFormatter is hardwired with expected values.
    	 * This slightly ugly approach will have to do for now. 
    	 */
        if ( !("vitro:csv").equals(resultFormatParam) ) {
        	rsf = formatSymbols.get(resultFormatParam);
        }                       
        String mimeType = mimeTypes.get(resultFormatParam);
        
        QueryExecution qe = null;
        try{
            Query query = SparqlQueryUtils.create(queryParam);
            qe = QueryExecutionFactory.create(query, dataset);
            if( query.isSelectType() ){
                ResultSet results = null;
                results = qe.execSelect();
                response.setContentType(mimeType);
                if (rsf != null) {
                	OutputStream out = response.getOutputStream();
                	ResultSetFormatter.output(out, results, rsf);
                } else {
                	Writer out = response.getWriter();
                	toCsv(out, results);
                }
            } else {
                Model resultModel = null;
                if( query.isConstructType() ){
                    resultModel = qe.execConstruct();
                }else if ( query.isDescribeType() ){
                    resultModel = qe.execDescribe();
                }else if(query.isAskType()){
                	// Irrespective of the ResultFormatParam, 
                	// this always prints a boolean to the default OutputStream.
                	String result = (qe.execAsk() == true) ? "true" : "false";
                	PrintWriter p = response.getWriter();
                	p.write(result);
                    return;
                }
                response.setContentType(rdfFormatSymbols.get(rdfResultFormatParam));
                OutputStream out = response.getOutputStream();
                resultModel.write(out, rdfResultFormatParam);
            }
        }finally{
            if( qe != null)
                qe.close();
        }        
    }

    private void doNoModelInContext(HttpServletResponse res){
        try {
            res.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
            ServletOutputStream sos = res.getOutputStream();
            sos.println("<html><body>this service is not supporeted by the current " +
                    "webapp configuration. A jena model is required in the servlet context.</body></html>" );
        } catch (IOException e) {
            log.error("Could not write to ServletOutputStream");
        }
    }

    private void toCsv(Writer out, ResultSet results) {
    	// The Skife library wouldn't quote and escape the normal way, 
    	// so I'm trying it manually.   	
    	while (results.hasNext()) {
    		QuerySolution solution = (QuerySolution) results.next();
    		List<String> valueList = new LinkedList<String>();
    		Iterator<String> varNameIt = solution.varNames();
    		while (varNameIt.hasNext()) {
    			String varName = varNameIt.next();
    			String value = "";
    			try {
    				Literal lit = solution.getLiteral(varName);
    				if (lit != null) { 
    					value = lit.getLexicalForm();
    					if (XSD.anyURI.getURI().equals(lit.getDatatypeURI())) {
    						value = URLDecoder.decode(value, "UTF-8");
    					}
    				}
    			} catch (Exception e) {
    				try {
    					Resource res = solution.getResource(varName);
    					if (res.isAnon()) {
    						value = res.getId().toString();
    					} else {
    						value = res.getURI();
    					}
	    			} catch (Exception f) {}
    			}
    			valueList.add(value);
    		}

    		Iterator<String> valueIt = valueList.iterator();
			StringBuffer rowBuff = new StringBuffer();
    		while (valueIt.hasNext()) {
    			String value = valueIt.next();
    			value.replaceAll("\"", "\\\"");

    			rowBuff.append("\"").append(value).append("\"");
    			if (valueIt.hasNext()) {
    				rowBuff.append(",");
    			}
    		}
    		rowBuff.append("\n");
    		try {
    			out.write(rowBuff.toString());
    		} catch (IOException ioe) {
    			log.error(ioe);
    		}
    	}
    	try {
    		out.flush();
    	} catch (IOException ioe) {
    		log.error(ioe);
    	}
    }
    
    private void doHelp(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
            VitroRequest vreq = new VitroRequest(req);
            
            OntologyDao daoObj = vreq.getFullWebappDaoFactory().getOntologyDao();
            List<Ontology> ontologiesObj = daoObj.getAllOntologies();
            ArrayList<String> prefixList = new ArrayList<String>();
            
            if(ontologiesObj !=null && ontologiesObj.size()>0){
            	for(Ontology ont: ontologiesObj) {
            		prefixList.add(ont.getPrefix() == null ? "(not yet specified)" : ont.getPrefix());
            		prefixList.add(ont.getURI() == null ? "" : ont.getURI());
            	}
            }
            else{
            	prefixList.add("<strong>" + "No Ontologies added" + "</strong>");
            	prefixList.add("<strong>" + "Load Ontologies" + "</strong>");
            }
            
            req.setAttribute("prefixList", prefixList);
            
            // nac26: 2009-09-25 - this was causing problems in safari on localhost installations because the href did not include the context.  The edit.css is not being used here anyway (or anywhere else for that matter)
            // req.setAttribute("css", "<link rel=\"stylesheet\" type=\"text/css\" href=\""+portal.getThemeDir()+"css/edit.css\"/>");
            req.setAttribute("title","SPARQL Query");
            req.setAttribute("bodyJsp", "/admin/sparqlquery/sparqlForm.jsp");
            
            RequestDispatcher rd = req.getRequestDispatcher("/"+Controllers.BASIC_JSP);
            rd.forward(req,res);
    }

}
