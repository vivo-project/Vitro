/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.jena;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.WhichService.CONFIGURATION;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.WhichService.CONTENT;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.ABOX_ASSERTIONS;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.FULL_ASSERTIONS;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.TBOX_ASSERTIONS;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.TBOX_UNION;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.semanticweb.owlapi.reasoner.InconsistentOntologyException;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecException;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ClosableIterator;

import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.beans.Ontology;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.OntologyDao;
import edu.cornell.mannlib.vitro.webapp.dao.jena.BlankNodeFilteringModelMaker;
import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceGraph;
import edu.cornell.mannlib.vitro.webapp.dao.jena.event.EditEvent;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.WhichService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeSet;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.adapters.VitroModelFactory;
import edu.cornell.mannlib.vitro.webapp.utils.SparqlQueryUtils;
import edu.cornell.mannlib.vitro.webapp.utils.jena.JenaIngestUtils;
import edu.cornell.mannlib.vitro.webapp.utils.jena.JenaIngestUtils.MergeResult;
import edu.cornell.mannlib.vitro.webapp.utils.jena.JenaIngestWorkflowProcessor;
import edu.cornell.mannlib.vitro.webapp.utils.jena.JenaOutputUtils;
import edu.cornell.mannlib.vitro.webapp.utils.jena.WorkflowOntology;

public class JenaIngestController extends BaseEditController {
	private static final Log log = LogFactory.getLog(JenaIngestController.class);
    
    private static final String INGEST_MENU_JSP = "/jenaIngest/ingestMenu.jsp";
    private static final String LIST_MODELS_JSP = "/jenaIngest/listModels.jsp";
    private static final String CREATE_MODEL_JSP = "/jenaIngest/createModel.jsp";
    private static final String LOAD_RDF_DATA_JSP = "/jenaIngest/loadRDFData.jsp";
    private static final String EXECUTE_SPARQL_JSP = "/jenaIngest/sparqlConstruct.jsp";
    private static final String RENAME_BNODES_JSP = "/jenaIngest/renameBNodes.jsp";
    private static final String RENAME_BNODES_URI_SELECT_JSP = "/jenaIngest/renameBNodesURISelect.jsp";
    private static final String SMUSH_JSP = "/jenaIngest/smushSingleModel.jsp";
    private static final String CSV2RDF_JSP = "/jenaIngest/csv2rdf.jsp";
    private static final String PROCESS_STRINGS_JSP = "/jenaIngest/processStrings.jsp";
    private static final String SUBTRACT_MODELS_JSP = "/jenaIngest/subtractModels.jsp";
    private static final String SPLIT_PROPERTY_VALUES_JSP = "/jenaIngest/splitPropertyValues.jsp";
    private static final String EXECUTE_WORKFLOW_JSP = "/jenaIngest/executeWorkflow.jsp";
    private static final String WORKFLOW_STEP_JSP = "/jenaIngest/workflowStep.jsp";
    private static final String GENERATE_TBOX_JSP = "/jenaIngest/generateTBox.jsp";
    private static final String PERMANENT_URI = "/jenaIngest/permanentURI.jsp";
    private static final String MERGE_RESOURCES = "/jenaIngest/mergeResources.jsp";
    private static final String MERGE_RESULT = "/jenaIngest/merge_result.jsp";
    private static final String SPARQL_CONSTRUCT_CLASS = "http://vitro.mannlib.cornell.edu/ns/vitro/0.7/sparql#SPARQLCONSTRUCTQuery";
    private static final String SPARQL_QUERYSTR_PROP = "http://vitro.mannlib.cornell.edu/ns/vitro/0.7/sparql#queryStr";
    private static final String RENAME_RESOURCE = "/jenaIngest/renameResource.jsp";
    private static final String RENAME_RESULT = "/jenaIngest/renameResult.jsp";
    private static final String CREATED_GRAPH_BASE_URI = "http://vitro.mannlib.cornell.edu/a/graph/";
    private static final String WHICH_MODEL_MAKER = "jenaIngestModelMakerID";

    private static final Map<String, Model> attachedModels = new HashMap<String, Model>();
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
		if (!isAuthorizedToDisplayPage(request, response,
				SimplePermission.USE_ADVANCED_DATA_TOOLS_PAGES.ACTION)) {
            return;
        }
        
        VitroRequest vreq = new VitroRequest(request);   
        ModelMaker maker = getModelMaker(vreq);
        WhichService modelType = getModelType(vreq);
        
        String actionStr = vreq.getParameter("action");
        actionStr = (actionStr != null) ? actionStr : "";
        
        if("listModels".equals(actionStr)) {
            processListModelsRequest(vreq, maker, modelType);
        } else if("configModels".equals(actionStr)){
            processConfigModelsRequest(vreq);
        } else if("contentModels".equals(actionStr)){
            processContentModelsRequest(vreq);
        } else if("createModel".equals(actionStr)) {
            processCreateModelRequest(vreq, maker, modelType);
        } else if("removeModel".equals(actionStr)) {
            processRemoveModelRequest(vreq, maker, modelType);
        } else if("loadRDFData".equals(actionStr)) {
            processLoadRDFDataRequest(vreq, maker);
        } else if("cleanLiterals".equals(actionStr)) {
            processCleanLiteralsRequest(vreq);
        } else if("outputModel".equals(actionStr)) {
            processOutputModelRequest(vreq, response);
            return; // don't attempt to display a JSP
        } else if("clearModel".equals(actionStr)) {
            processClearModelRequest(vreq, maker, modelType);
        } else if("attachModel".equals(actionStr)) {
            processAttachModelRequest(vreq, maker, modelType);
        } else if("detachModel".equals(actionStr)) {
            processDetachModelRequest(vreq, maker, modelType);
        } else if("renameBNodes".equals(actionStr)) {
            processRenameBNodesRequest(vreq, maker);
        } else if("renameBNodesURISelect".equals(actionStr)){
            processRenameBNodesURISelectRequest(vreq, maker);
        } else if("smushSingleModel".equals(actionStr)) {
            processSmushSingleModelRequest(vreq);
        } else if("csv2rdf".equals(actionStr)) {
            processCsv2rdfRequest(vreq);
        } else if("processStrings".equals(actionStr)) {
            processProcessStringsRequest(vreq);
        } else if("splitPropertyValues".equals(actionStr)) {
            processSplitPropertyValuesRequest(vreq);
        } else if("subtractModels".equals(actionStr)) {
            processSubtractModelRequest(vreq);
        } else if("executeWorkflow".equals(actionStr)) {
            processExecuteWorkflowRequest(vreq);
        } else if("executeSparql".equals(actionStr)) {
            processExecuteSparqlRequest(vreq);
        } else if ("generateTBox".equals(actionStr)) {
            processGenerateTBoxRequest(vreq);
        } else if("permanentURI".equals(actionStr)){
            processPermanentURIRequest(vreq, maker);          
        } else if("mergeResources".equals(actionStr)){
            processMergeResourceRequest(vreq);
        } else if("renameResource".equals(actionStr)){
            processRenameResourceRequest(vreq);
        } else if("mergeResult".equals(actionStr)){
            processMergeResultRequest(vreq, response);
            return;
        } else if ("dumpRestore".equals(actionStr)) {
        	processDumpRestore(vreq, response);
        	return;
        }
        
        else {
            request.setAttribute("title","Ingest Menu");
            request.setAttribute("bodyJsp",INGEST_MENU_JSP);
        }
        
        maker = getModelMaker(vreq);
        List<String> modelNames = maker.listModels().toList();
        for (int mnIdx = modelNames.size() - 1; mnIdx > -1; mnIdx--) {
            if (!modelNames.get(mnIdx).startsWith("http")) {
                modelNames.remove(mnIdx);
            }
        }

        request.setAttribute("modelNames", modelNames);
   
        RequestDispatcher rd = request.getRequestDispatcher(
                Controllers.BASIC_JSP);      
        try {
            rd.forward(request, response);
        } catch (Exception e) {
            log.error(e,e);
            throw new ServletException(e);
        }
        
    }
    
    private void processListModelsRequest(VitroRequest vreq, ModelMaker maker, WhichService modelType) {
        showModelList(vreq, maker, modelType);
    }
    
    protected static boolean isUsingMainStoreForIngest(HttpServletRequest req) {
    	return CONFIGURATION != req.getSession().getAttribute(WHICH_MODEL_MAKER); 
    }
    
    private void processConfigModelsRequest(VitroRequest vreq) {
        ModelMaker vjmm = ModelAccess.on(getServletContext()).getModelMaker(CONFIGURATION);
        vreq.getSession().setAttribute(WHICH_MODEL_MAKER, CONFIGURATION);
        showModelList(vreq, vjmm, CONFIGURATION);
    }
    
    private void processContentModelsRequest(VitroRequest vreq) {
        ModelMaker vsmm = ModelAccess.on(getServletContext()).getModelMaker(CONTENT);
        vreq.getSession().setAttribute(WHICH_MODEL_MAKER, CONTENT);
        showModelList(vreq, vsmm, CONTENT);
    }
    
    private void processCreateModelRequest(VitroRequest vreq, ModelMaker maker, WhichService modelType) {
        String modelName = vreq.getParameter("modelName");
    	
        if (modelName != null) {
        	try {
            	URI graphURI = new URI(modelName);
            	
            	if (graphURI.getScheme() == null) {
            	   String origName = modelName;	
            	   modelName =CREATED_GRAPH_BASE_URI + modelName;
                   log.info("The model name has been changed from " + origName + " to " + modelName);
            	}
            	           	
                doCreateModel(modelName, maker);
                showModelList(vreq, maker, modelType);      
        	} catch (URISyntaxException e) {
        		throw new RuntimeException("the model name must be a valid URI");
        	}
        } else {
            vreq.setAttribute("modelType", modelType.toString());
            vreq.setAttribute("title","Create New Model");
            vreq.setAttribute("bodyJsp",CREATE_MODEL_JSP);
        }
    }
    
    private void processRemoveModelRequest(VitroRequest vreq, ModelMaker maker, WhichService modelType) {
        String modelName = vreq.getParameter("modelName");
        if (modelName!=null) {
            doRemoveModel(modelName, maker);
        }
        showModelList(vreq, maker, modelType);
    }
    
    private void processClearModelRequest(VitroRequest vreq, ModelMaker maker, WhichService modelType) {
        String modelName = vreq.getParameter("modelName");
        if (modelName != null) {
            doClearModel(modelName,maker);
        }
        showModelList(vreq, maker, modelType);
    }
    
    private void processLoadRDFDataRequest(VitroRequest vreq, ModelMaker maker) {
        String docLoc = vreq.getParameter("docLoc");
        String filePath = vreq.getParameter("filePath");
        String modelName = vreq.getParameter("modelName");
        String languageParam = null;
        String language = ( (languageParam = vreq.getParameter("language")) != null) ? languageParam : "RDF/XML";
        if (docLoc!=null && modelName != null) {
            doLoadRDFData(modelName,docLoc,filePath,language,maker);
            vreq.setAttribute("title","Ingest Menu");
            vreq.setAttribute("bodyJsp",INGEST_MENU_JSP);
        } else {
            vreq.setAttribute("title","Load RDF Data");
            vreq.setAttribute("bodyJsp",LOAD_RDF_DATA_JSP);
        }
    }
    
    private void processOutputModelRequest(VitroRequest vreq, 
                                           HttpServletResponse response) {
        String modelNameStr = vreq.getParameter("modelName");
        Model model = getModel(modelNameStr,vreq);
        JenaOutputUtils.setNameSpacePrefixes(model,vreq.getWebappDaoFactory());
        model.enterCriticalSection(Lock.READ);
        try {
            OutputStream out = response.getOutputStream();
            response.setContentType("application/x-turtle");
            //out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n".getBytes());
            model.write(out, "TTL");
            out.flush();
            out.close();
        } catch (com.hp.hpl.jena.shared.CannotEncodeCharacterException cece) {
            // there's got to be a better way to do this
            byte[] badCharBytes = String.valueOf(cece.getBadChar()).getBytes();
            String errorMsg = "Cannot encode character with byte values: (decimal) ";
            for (int i=0; i<badCharBytes.length; i++) {
                errorMsg += badCharBytes[i];
            }
            throw new RuntimeException(errorMsg, cece);
        } catch (Exception e) {
            log.error(e, e);
        } finally {
            model.leaveCriticalSection();
        }
    }
    
    private void processCleanLiteralsRequest(VitroRequest vreq) {
        String modelNameStr = vreq.getParameter("modelName");
        Model model = getModel(modelNameStr,vreq);
        doCleanLiterals(model);
        vreq.setAttribute("title","Ingest Menu");
        vreq.setAttribute("bodyJsp",INGEST_MENU_JSP);
    }
    
    private void processAttachModelRequest(VitroRequest vreq, ModelMaker maker, WhichService modelType) {
        String modelName = vreq.getParameter("modelName");
        if (modelName != null) {
            doAttachModel(modelName,maker);
        }
        showModelList(vreq, maker, modelType);
    }
    
    private void processDetachModelRequest(VitroRequest vreq, ModelMaker maker, WhichService modelType) {
        String modelName = vreq.getParameter("modelName");
        if (modelName != null) {
            doDetachModel(modelName);
        }
        showModelList(vreq, maker, modelType);
    }
    
    private void processRenameBNodesRequest(VitroRequest vreq, ModelMaker maker) {
        String[] sourceModel = vreq.getParameterValues("sourceModelName");
        JenaIngestUtils utils = new JenaIngestUtils();
        if(sourceModel != null && sourceModel.length != 0) {
            List<Model> sourceModelList = new ArrayList<Model>();
            for (int i = 0; i < sourceModel.length ; i++) {
                Model m = maker.getModel(sourceModel[i]);
                if (m != null) {
                    sourceModelList.add(m);
                }
            }
            Map<String,LinkedList<String>> propertyMap = 
                    utils.generatePropertyMap(sourceModelList, maker);
            List<String> sourceModelNameList = Arrays.asList(sourceModel);
            vreq.setAttribute("sourceModel",sourceModelNameList);
            vreq.setAttribute("propertyMap", propertyMap);
            vreq.setAttribute("enablePropertyPatternURIs", !propertyMap.isEmpty());
            vreq.setAttribute("title","URI Select");
            vreq.setAttribute("bodyJsp",RENAME_BNODES_URI_SELECT_JSP);
        } else {
            vreq.setAttribute("title","Rename Blank Nodes");
            vreq.setAttribute("bodyJsp",RENAME_BNODES_JSP);
        }
    }
    
    private void processRenameBNodesURISelectRequest(VitroRequest vreq, ModelMaker maker) {
        String namespaceEtcStr = vreq.getParameter("namespaceEtcStr");
        String pattern = vreq.getParameter("pattern");
        String concatenate = vreq.getParameter("concatenate");
        String[] sourceModel = vreq.getParameterValues("sourceModelName");
        if(namespaceEtcStr != null) {
            if (namespaceEtcStr.isEmpty()) {
                if ("true".equals(vreq.getParameter("csv2rdf"))) {
                    processCsv2rdfRequest(vreq);
                    return;
                } else {
                    vreq.setAttribute("errorMsg", "Please enter a value.");
                    processRenameBNodesRequest(vreq, maker);
                    return;
                }
            }
            if (concatenate.equals("integer")) {
                doRenameBNodes(vreq,namespaceEtcStr, false, null, sourceModel);
            } else {
                pattern = pattern.trim();
                doRenameBNodes(vreq,namespaceEtcStr, true, pattern, sourceModel);
            }
            vreq.setAttribute("title", "Ingest Menu");
            vreq.setAttribute("bodyJsp", INGEST_MENU_JSP);
        }
        else{
            vreq.setAttribute("title", "URI Select");
            vreq.setAttribute("bodyJsp", RENAME_BNODES_URI_SELECT_JSP);
        }
    }
    
    private void processSmushSingleModelRequest(VitroRequest vreq) {
        String propertyURIStr = vreq.getParameter("propertyURI");
        if (propertyURIStr != null) {
            doSmushSingleModel(vreq);
            vreq.setAttribute("title","Ingest Menu");
            vreq.setAttribute("bodyJsp",INGEST_MENU_JSP);
        } else {
            vreq.setAttribute("title","Smush Resources");
            vreq.setAttribute("bodyJsp",SMUSH_JSP);
        }
    }
    
    private void processCsv2rdfRequest(VitroRequest vreq) {
        String csvUrl = vreq.getParameter("csvUrl");
        if (csvUrl != null) {
            /*doExecuteCsv2Rdf(vreq);*/
            vreq.setAttribute("title","IngestMenu");
            vreq.setAttribute("bodyJsp", INGEST_MENU_JSP);
        } else {
            vreq.setAttribute("title","Convert CSV to RDF");
            vreq.setAttribute("bodyJsp",CSV2RDF_JSP);
        }
    }
    
    private void processProcessStringsRequest(VitroRequest vreq) {
        String className = vreq.getParameter("className");
        if (className != null) {
            doProcessStrings(vreq);
            vreq.setAttribute("title","IngestMenu");
            vreq.setAttribute("bodyJsp", INGEST_MENU_JSP);
        } else {
            vreq.setAttribute("title","Process Strings");
            vreq.setAttribute("bodyJsp",PROCESS_STRINGS_JSP);
        }
    }
    
    private void processSplitPropertyValuesRequest(VitroRequest vreq) {
        String splitRegex = vreq.getParameter("splitRegex");
        if (splitRegex != null) {
            doSplitPropertyValues(vreq);
            vreq.setAttribute("title","IngestMenu");
            vreq.setAttribute("bodyJsp", INGEST_MENU_JSP);
        } else {
            vreq.setAttribute("title","Split PropertyValues");
            vreq.setAttribute("bodyJsp",SPLIT_PROPERTY_VALUES_JSP);
        }
    }
    
    private void processSubtractModelRequest(VitroRequest vreq) {
        String modela = vreq.getParameter("modela");
        if (modela != null) {
            doSubtractModels(vreq);
            vreq.setAttribute("title","IngestMenu");
            vreq.setAttribute("bodyJsp", INGEST_MENU_JSP);
        } else {
            vreq.setAttribute("title", "Subtract Models");
            vreq.setAttribute("bodyJsp",SUBTRACT_MODELS_JSP);
        }
    }
    
    private void processExecuteWorkflowRequest(VitroRequest vreq) {
        String workflowURIStr = vreq.getParameter("workflowURI");
        String workflowStepURIStr = vreq.getParameter("workflowStepURI");
        if (workflowURIStr != null && workflowStepURIStr != null) {
            doExecuteWorkflow(vreq);
            vreq.setAttribute("title","IngestMenu");
            vreq.setAttribute("bodyJsp", INGEST_MENU_JSP);
        } else if (workflowURIStr != null) {
            // Select the workflow step at which to start
            OntModel jenaOntModel = (OntModel) getModel("vitro:jenaOntModel",vreq);
            vreq.setAttribute("workflowSteps", new JenaIngestWorkflowProcessor(
                    jenaOntModel.getIndividual(workflowURIStr),
                    getModelMaker(vreq)).getWorkflowSteps(null));
            vreq.setAttribute("title", "Choose Workflow Step");
            vreq.setAttribute("bodyJsp", WORKFLOW_STEP_JSP);
        } else {
    		OntModel jenaOntModel = ModelAccess.on(getServletContext()).getOntModel();
            jenaOntModel.enterCriticalSection(Lock.READ);
            List<Individual> savedQueryList = new LinkedList<Individual>();
            try {
                Resource workflowClassRes = WorkflowOntology.Workflow;
                savedQueryList.addAll(jenaOntModel.listIndividuals(workflowClassRes).toList());
            } finally {
                jenaOntModel.leaveCriticalSection();
            }
            vreq.setAttribute("workflows",savedQueryList);
            vreq.setAttribute("title", "Execute Workflow");
            vreq.setAttribute("bodyJsp", EXECUTE_WORKFLOW_JSP);
        }
    }
    
    private void processExecuteSparqlRequest(VitroRequest vreq) {
        String sparqlQueryStr = vreq.getParameter("sparqlQueryStr");
		OntModel jenaOntModel = ModelAccess.on(getServletContext()).getOntModel();
        jenaOntModel.enterCriticalSection(Lock.READ);
        List<Individual> savedQueryList = new LinkedList<Individual>();
        try {
            Resource sparqlConstructClassRes = ResourceFactory.createResource(SPARQL_CONSTRUCT_CLASS);
            savedQueryList.addAll(jenaOntModel.listIndividuals(sparqlConstructClassRes).toList());
        } finally {
            jenaOntModel.leaveCriticalSection();
        }
        /*ass92*/
        OntologyDao daoObj = vreq.getUnfilteredWebappDaoFactory().getOntologyDao();
        List<Ontology> ontologiesObj = daoObj.getAllOntologies();
        List<String> prefixList = new ArrayList<>();       
        if(ontologiesObj !=null && ontologiesObj.size()>0){
            Iterator<Ontology> ontItr = ontologiesObj.iterator();
            while(ontItr.hasNext()){
                Ontology ont = ontItr.next();
                prefixList.add(ont.getPrefix() == null ? "(not yet specified)" : ont.getPrefix());
                prefixList.add(ont.getURI() == null ? "" : ont.getURI());
            }
        }
        else{
            prefixList.add("<strong>" + "No Ontologies added" + "</strong>");
            prefixList.add("<strong>" + "Load Ontologies" + "</strong>");
        }     
        vreq.setAttribute("prefixList", prefixList);
        /*complete*/
        if (sparqlQueryStr != null) {
            String validationMessage = "";
            if (vreq.getParameterValues("sourceModelName") == null) {
                validationMessage += "<p>Please select one or more source models.</p>";
            }
            if (vreq.getParameter("destinationModelName") == null) {
                validationMessage += "<p>Please select a destination model</p>";
            }
            if (validationMessage.length() > 0) {
                vreq.setAttribute("validationMessage", validationMessage);
            } else {
                long constructedStmtCount = 0;
                try {
                    constructedStmtCount = doExecuteSparql(vreq);
                } catch (QueryParseException qpe) {
                    String errorMsg = "<p>Unable to parse query:</p>";
                    if (qpe.getMessage() != null) {
                        errorMsg += "<p>" + qpe.getMessage() + "</p>"; 
                    }
                    vreq.setAttribute("errorMsg", errorMsg);
                } catch (InconsistentOntologyException ioe) {
                    String errorMsg = "<p>Inconsistent source ontology:</p>";
                    if (ioe.getMessage() != null) {
                        errorMsg += "<p>" + ioe.getMessage() + "</p>";
                    }
                    vreq.setAttribute("errorMsg", errorMsg);
                }
                vreq.setAttribute("constructedStmtCount", constructedStmtCount);
            }
            vreq.setAttribute("savedQueries",savedQueryList);
            vreq.setAttribute("title","SPARQL CONSTRUCT result");
            vreq.setAttribute("bodyJsp",EXECUTE_SPARQL_JSP);
        } else {    
            vreq.setAttribute("savedQueries",savedQueryList);
            vreq.setAttribute("title","Execute SPARQL Construct");
            vreq.setAttribute("bodyJsp",EXECUTE_SPARQL_JSP);
        }
    }
    
    private void processGenerateTBoxRequest(VitroRequest vreq) {
        String testParam = vreq.getParameter("sourceModelName");
        if (testParam != null) {
            doGenerateTBox(vreq);
            vreq.setAttribute("title","Ingest Menu");
            vreq.setAttribute("bodyJsp",INGEST_MENU_JSP);
        } else {
            vreq.setAttribute("title","Generate TBox from Assertions Data");
            vreq.setAttribute("bodyJsp",GENERATE_TBOX_JSP);
        }    
    }
    
    private void processPermanentURIRequest(VitroRequest vreq, ModelMaker maker) {
        String modelName = vreq.getParameter("modelName");
        String oldModel = vreq.getParameter("oldModel");
        String newModel = vreq.getParameter("newModel");
        String oldNamespace = vreq.getParameter("oldNamespace");
        String newNamespace = vreq.getParameter("newNamespace");
        String dNamespace = vreq.getParameter("defaultNamespace");
        newNamespace = (newNamespace == null || newNamespace.isEmpty()) ? oldNamespace : newNamespace;
        newNamespace = (dNamespace != null) ? dNamespace : newNamespace;
        if(modelName!=null){
            Model m = maker.getModel(modelName);
            List<String> namespaceList = new ArrayList<>();
            ResIterator resItr = m.listResourcesWithProperty((Property)null);
            if(resItr!=null){
                while(resItr.hasNext()){
                    String namespace = resItr.nextResource().getNameSpace();
                    if(!namespaceList.contains(namespace)){
                        namespaceList.add(namespace);
                    }        
                }
            } else {
                namespaceList.add("no resources present");
            }
            String defaultNamespace = vreq.getUnfilteredWebappDaoFactory().getDefaultNamespace();
            vreq.setAttribute("modelName", modelName);
            vreq.setAttribute("defaultNamespace", defaultNamespace);
              vreq.setAttribute("namespaceList", namespaceList);
            vreq.setAttribute("title","Permanent URI");
            vreq.setAttribute("bodyJsp",PERMANENT_URI);
        } else if(oldModel != null) {
            JenaIngestUtils utils = new JenaIngestUtils();
            utils.doPermanentURI(oldModel, newModel, oldNamespace, newNamespace, maker, vreq);
            vreq.setAttribute("title","Ingest Menu");
            vreq.setAttribute("bodyJsp",INGEST_MENU_JSP);
        }
    }
    
    private void processMergeResourceRequest(VitroRequest vreq) {
          String uri1 = vreq.getParameter("uri1"); // get primary uri
          String uri2 = vreq.getParameter("uri2"); // get secondary uri
          String usePrimaryLabelOnlyStr = vreq.getParameter("usePrimaryLabelOnly");;
          boolean usePrimaryLabelOnly = usePrimaryLabelOnlyStr != null && !usePrimaryLabelOnlyStr.isEmpty();
          
          if(uri1!=null){
              JenaIngestUtils utils = new JenaIngestUtils();
              /*
               * get baseOnt and infOnt models
               */
              OntModel baseOntModel = ModelAccess.on(getServletContext()).getOntModel(FULL_ASSERTIONS);
              OntModel tboxOntModel = ModelAccess.on(getServletContext()).getOntModel(TBOX_UNION);
              
              /*
               * calling method that does the merge operation.
               */
              MergeResult result = utils.doMerge(
                      uri1, uri2, baseOntModel, tboxOntModel, usePrimaryLabelOnly);
              
              vreq.getSession().setAttribute(
                      "leftoverModel", result.getLeftoverModel());
              vreq.setAttribute("result", result);
              vreq.setAttribute("title", "Merge Resources");
              vreq.setAttribute("bodyJsp", MERGE_RESULT);
          } else{
              vreq.setAttribute("title","Merge Resources");
              vreq.setAttribute("bodyJsp",MERGE_RESOURCES);  
          }
    }
    
    private void processRenameResourceRequest(VitroRequest vreq) {
          String oldNamespace = vreq.getParameter("oldNamespace");
          String newNamespace = vreq.getParameter("newNamespace");
          String errorMsg = "";
          if (oldNamespace != null) {
              if (oldNamespace.isEmpty() && !newNamespace.isEmpty()) {
                  errorMsg = "Please enter the old namespace to be changed.";
              } else if (!oldNamespace.isEmpty() && newNamespace.isEmpty()) {
                  errorMsg = "Please enter the new namespace."; 
              } else if (oldNamespace.isEmpty() && newNamespace.isEmpty()) {
                  errorMsg = "Please enter the namespaces.";
              } else if (oldNamespace.equals(newNamespace)) {
                  errorMsg = "Please enter two different namespaces.";
              }
              if (!errorMsg.isEmpty()) {
                  vreq.setAttribute("errorMsg", errorMsg);
                  vreq.setAttribute("oldNamespace", oldNamespace);
                  vreq.setAttribute("newNamespace", newNamespace);
                  vreq.setAttribute("title","Rename Resource");
                  vreq.setAttribute("bodyJsp",RENAME_RESOURCE);                  
              } else {
                  String result = doRename(oldNamespace, newNamespace);
                  vreq.setAttribute("result",result);
                  vreq.setAttribute("title","Rename Resources");
                  vreq.setAttribute("bodyJsp",RENAME_RESULT);               
              }
          } else{
              vreq.setAttribute("title","Rename Resource");
              vreq.setAttribute("bodyJsp",RENAME_RESOURCE);  
          }
    }
    
    private void processMergeResultRequest(VitroRequest vreq, 
                                           HttpServletResponse response) {

        Model lmodel = (Model) vreq.getSession().getAttribute("leftoverModel");
        response.setContentType("RDF/XML-ABBREV");
        try    {
            OutputStream outStream = response.getOutputStream();
            outStream.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>".getBytes());
            lmodel.write( outStream,"RDF/XML-ABBREV");
            outStream.flush();
            outStream.close();
        }
        catch(IOException ioe){
            throw new RuntimeException(ioe);
        }
    }

    /**
     * Get the model type from the request, or from the session.
     */
    protected WhichService getModelType(VitroRequest vreq) {
        String modelType = vreq.getParameter("modelType");
		if (modelType != null) {
			if (modelType.equals(CONFIGURATION.toString())) {
				return CONFIGURATION;
			} else {
				return CONTENT;
			}
		}
		if (vreq.getSession().getAttribute(WHICH_MODEL_MAKER) == CONFIGURATION) {
			return CONFIGURATION;
		} else {
			return CONTENT;
		}
    }
    
    private void doCreateModel(String modelName, ModelMaker modelMaker) {
        modelMaker.createModel(modelName);
    }
    
    private void doRemoveModel(String modelName, ModelMaker modelMaker) {
        //Try to detach first since it cause problems to remove an attached model.        
        doDetachModel(modelName);
        log.debug("Removing " + modelName + " from webapp");
        modelMaker.removeModel(modelName);        
    }
    
    private void doClearModel(String modelName, ModelMaker modelMaker) {
        Model m = modelMaker.getModel(modelName);
        OntModel o = VitroModelFactory.createOntologyModel(m);
        o.enterCriticalSection(Lock.WRITE);
        try {
            o.removeAll();
        } finally {
            o.leaveCriticalSection();
        }
    }
    
    private void doLoadRDFData(String modelName, String docLoc, String filePath, String language, ModelMaker modelMaker) {
        Model m = modelMaker.getModel(modelName);
        m.enterCriticalSection(Lock.WRITE);
        try {
            if ( (docLoc != null) && (docLoc.length()>0) ) {
                m.read(docLoc, language);
            } else if ( (filePath != null) && (filePath.length()>0) ) {
                File file = new File(filePath);
                File[] files;
                if (file.isDirectory()) {
                    files = file.listFiles();
                } else {
                    files = new File[1];
                    files[0] = file;
                }
                for (int i=0; i<files.length; i++) {
                    File currentFile = files[i];
                    log.info("Reading file "+currentFile.getName());
                    FileInputStream fis;
                    try {
                        fis = new FileInputStream(currentFile);
                        m.read(fis, null, language);
                        fis.close();
                    } catch (IOException ioe) {
                        throw new RuntimeException(ioe);
                    }
                }
            }
        } finally { 
            m.leaveCriticalSection();
        }
    }
    
    private void doAttachModel(String modelName, ModelMaker modelMaker) {
        if (attachedModels.containsKey(modelName)) {
            doDetachModel(modelName);
        }
        Model m = ModelFactory.createDefaultModel();
        m.add(modelMaker.getModel(modelName));        
        ModelAccess.on(getServletContext()).getOntModel(TBOX_ASSERTIONS).addSubModel(m);
        attachedModels.put(modelName, m);
        log.info("Attached " + modelName + " (" + m.hashCode() + ") to webapp");
    }
    
    private void doDetachModel(String modelName) {
        Model m = attachedModels.get(modelName);
        if (m == null) {
            return;
        }
        ModelAccess.on(getServletContext()).getOntModel(TBOX_ASSERTIONS).removeSubModel(m);
        attachedModels.remove(modelName);
        log.info("Detached " + modelName + " (" + m.hashCode() + ") from webapp");
    }
    
    private void doRenameBNodes(VitroRequest vreq, String namespaceEtc, boolean patternBoolean, String pattern, String[] sourceModel) {
        OntModel source = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        String property = vreq.getParameter("property");
        
        Boolean csv2rdf = false;
        try {
            csv2rdf = Boolean.parseBoolean(vreq.getParameter("csv2rdf"));
        } catch (Exception e) {
            log.error(e, e);
        }
        
        if (csv2rdf) {
            source.addSubModel(
                    (Model) vreq.getSession().getAttribute("csv2rdfResult")); 
        } else {
            for (int i=0; i<sourceModel.length; i++) {
                Model m = getModel(sourceModel[i],vreq);
                source.addSubModel(m);
            }
        }
        
        Model destination = (csv2rdf) 
               ? ModelFactory.createDefaultModel()
               : getModel(vreq.getParameter("destinationModelName"),vreq);
               
        JenaIngestUtils utils = new JenaIngestUtils();
        destination.enterCriticalSection(Lock.WRITE);
        try {
            if(!patternBoolean){
                destination.add(utils.renameBNodes(source, namespaceEtc, vreq.getJenaOntModel()));
            }
            else{
                destination.add(utils.renameBNodesByPattern(source, namespaceEtc, vreq.getJenaOntModel(), pattern, property));
            }
            if (csv2rdf) {
                Model ultimateDestination = getModel(vreq.getParameter("destinationModelName"),vreq);
                ultimateDestination.add(destination);
            }
        } finally {
            destination.leaveCriticalSection();
        }
    }
    
    private void doSmushSingleModel(VitroRequest vreq) {
        OntModel source = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        String[] sourceModel = vreq.getParameterValues("sourceModelName");
        for (int i=0; i<sourceModel.length; i++) {
            Model m = getModel(sourceModel[i],vreq);
            source.addSubModel(m);
        }
        Model destination = getModel(vreq.getParameter("destinationModelName"),vreq);
        String propertyURIStr = vreq.getParameter("propertyURI");
        Property prop = ResourceFactory.createProperty(propertyURIStr);
        JenaIngestUtils utils = new JenaIngestUtils();
        destination.enterCriticalSection(Lock.WRITE);
        try {
            destination.add(utils.smushResources(source, prop));
        } finally {
            destination.leaveCriticalSection();
        }
    }
    
    private long doExecuteSparql(VitroRequest vreq) {
		OntModel jenaOntModel = ModelAccess.on(getServletContext()).getOntModel();
        OntModel source = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        String[] sourceModel = vreq.getParameterValues("sourceModelName");
        for (int i=0; i<sourceModel.length; i++) {
            Model m = getModel(sourceModel[i],vreq);
            source.addSubModel(m);
        }
        Model destination = getModel(vreq.getParameter("destinationModelName"),vreq); 
        String sparqlQueryStr = vreq.getParameter("sparqlQueryStr");
        String savedQueryURIStr = vreq.getParameter("savedQuery");
        String queryStr;
        if (savedQueryURIStr.length()==0) {
            log.debug("Using entered query");
            queryStr = sparqlQueryStr;
        } else {
            Property queryStrProp = ResourceFactory.createProperty(SPARQL_QUERYSTR_PROP);
            jenaOntModel.enterCriticalSection(Lock.READ);
            try {
                Individual ind = jenaOntModel.getIndividual(savedQueryURIStr);
                log.debug("Using query "+savedQueryURIStr);
                queryStr = ( (Literal) ind.getPropertyValue(queryStrProp)).getLexicalForm();
                queryStr = StringEscapeUtils.unescapeHtml(queryStr); // !!! We need to turn off automatic HTML-escaping for data property editing.
            } finally {
                jenaOntModel.leaveCriticalSection();
            }
        }
        Model tempModel = ModelFactory.createDefaultModel();
        Query query = SparqlQueryUtils.create(queryStr);
        QueryExecution qexec = QueryExecutionFactory.create(query,source);
        try {
            qexec.execConstruct(tempModel);
        } catch (QueryExecException qee) {
            qexec.execDescribe(tempModel);
        }
        destination.enterCriticalSection(Lock.WRITE);
        try {
            if (destination instanceof OntModel) {
                ((OntModel) destination).getBaseModel().notifyEvent(new EditEvent(null, true));
            } else {
                destination.notifyEvent(new EditEvent(null, true));
            }
            destination.add(tempModel);      
        } finally {
            if (destination instanceof OntModel) {
                ((OntModel) destination).getBaseModel().notifyEvent(new EditEvent(null, false));
            } else {
                destination.notifyEvent(new EditEvent(null, false));
            }
            destination.leaveCriticalSection();
        }
        return tempModel.size();     
    }
    
    public void doSubtractModels(VitroRequest vreq) {
            String modela = vreq.getParameter("modela");
            String modelb = vreq.getParameter("modelb");
            String destination = vreq.getParameter("destinationModelName");
            Model ma = getModel(modela,vreq);
            Model mb = getModel(modelb,vreq);
            Model destinationModel = getModel(destination,vreq);
            if(!destination.equals(modela))
                destinationModel.add(ma.difference(mb));
            else
                ma.remove(mb);
    }
    
    public void doSplitPropertyValues(VitroRequest vreq) {
        String sourceModelStr = vreq.getParameter("sourceModelName");
        String destinationModelStr = vreq.getParameter("destinationModelName");
        Model sourceModel = getModel(sourceModelStr,vreq);
        Model destinationModel = getModel(destinationModelStr,vreq);
        String propertyURI = vreq.getParameter("propertyURI");
        String splitRegex = vreq.getParameter("splitRegex");
        String newPropertyURI = vreq.getParameter("newPropertyURI");
        String trimStr = vreq.getParameter("trim");
        boolean trim = ( "TRUE".equalsIgnoreCase(trimStr) );
        destinationModel.add( (new JenaIngestUtils()).splitPropertyValues(sourceModel, propertyURI, splitRegex, newPropertyURI, trim));
    }
    
    public void doGenerateTBox(VitroRequest vreq) {
        OntModel source = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        String[] sourceModel = vreq.getParameterValues("sourceModelName");
        for (int i=0; i<sourceModel.length; i++) {
            Model m = getModel(sourceModel[i],vreq);
            source.addSubModel(m);
        }
        String destinationModelStr = vreq.getParameter("destinationModelName");
        Model destination = getModel(destinationModelStr,vreq);
        destination.add( (new JenaIngestUtils()).generateTBox(source));
    }
        
    public void doProcessStrings(VitroRequest vreq) {
        try {
            String className = vreq.getParameter("className");
            String methodName = vreq.getParameter("methodName");
            String propertyName = vreq.getParameter("propertyName");
            String newPropertyName = vreq.getParameter("newPropertyName");
            // for now, we'll make the destination and source models the same
            Model destination = getModel(vreq.getParameter("destinationModelName"),vreq);
            String processModel = vreq.getParameter("processModel");
            Model savedAdditionsModel = null;
            Model savedRetractionsModel = null;
            String additionsModelStr = vreq.getParameter("additionsModel");
            if ( (additionsModelStr != null) && ( additionsModelStr.length() > 0 ) ) {
                savedAdditionsModel = getModel(additionsModelStr, vreq);
            }
            String retractionsModelStr = vreq.getParameter("retractionsModel");
            if ( (retractionsModelStr != null) && ( retractionsModelStr.length() > 0 ) ) {
                savedRetractionsModel = getModel(retractionsModelStr, vreq);
            } 
            Model additionsModel = ModelFactory.createDefaultModel();
            Model retractionsModel = ModelFactory.createDefaultModel();
            Class<?> stringProcessorClass = Class.forName(className);
            Object processor = stringProcessorClass.newInstance();
            Class<?>[] methArgs = {String.class};
            Method meth = stringProcessorClass.getMethod(methodName,methArgs);
            Property prop = ResourceFactory.createProperty(propertyName);
            Property newProp = ResourceFactory.createProperty(newPropertyName);
            destination.enterCriticalSection(Lock.READ);
            try {
                ClosableIterator<Statement> closeIt = destination.listStatements((Resource)null,prop,(RDFNode)null);
                for (Iterator<Statement> stmtIt = closeIt; stmtIt.hasNext(); ) {
                    Statement stmt = stmtIt.next();
                    if (stmt.getObject().isLiteral()) {
                        Literal lit = (Literal) stmt.getObject();
                        String lex = lit.getLexicalForm();
                        Object[] args = {lex};
                        String newLex = "";
                        try {
                            newLex = (String) meth.invoke(processor,args);
                        } catch (InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                        if (!newLex.equals(lex)) {
                            retractionsModel.add(stmt);
                            if (newLex.length()>0) {    
                                    Literal newLit = null;
                                    if (lit.getLanguage()!=null && lit.getLanguage().length()>0) {
                                        newLit = additionsModel.createLiteral(newLex,lit.getLanguage());
                                    } else if (lit.getDatatype() != null) {
                                        newLit = additionsModel.createTypedLiteral(newLex,lit.getDatatype());
                                    } else {
                                        newLit = additionsModel.createLiteral(newLex);
                                    }
                                    additionsModel.add(stmt.getSubject(),newProp,newLit);
                            }
                        }
                    }
                }
                if (processModel != null) {
                    destination.add(additionsModel);
                    destination.remove(retractionsModel);
                }
                if (savedAdditionsModel != null)  {
                    savedAdditionsModel.add(additionsModel);
                }
                if (savedRetractionsModel != null) {
                    savedRetractionsModel.add(retractionsModel);
                }
            } finally {
                destination.leaveCriticalSection();
            } 
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void doCleanLiterals(Model model) {
        Model retractionsModel = ModelFactory.createDefaultModel();
        Model additionsModel = ModelFactory.createDefaultModel();
        model.enterCriticalSection(Lock.WRITE);
        try {
            ClosableIterator<Statement> closeIt = model.listStatements();
            try {
                for (Iterator<Statement> stmtIt = closeIt; stmtIt.hasNext();) {
                    Statement stmt = stmtIt.next();
                    if (stmt.getObject().isLiteral()) {
                        Literal lit = (Literal) stmt.getObject();
                        String lex = lit.getLexicalForm();
                        char[] chars = lex.toCharArray();
                        char[] cleanChars = new char[chars.length];
                        int cleanPos = 0;
                        boolean badChar = false;
                        for (int i=0; i<chars.length; i++) {
                            if (java.lang.Character.getNumericValue(chars[i])>31 && java.lang.Character.isDefined(chars[i])) {
                                cleanChars[cleanPos] = chars[i];
                                cleanPos++;
                            } else {
                                log.error("Bad char in " + lex);
                                log.error("Numeric value " + java.lang.Character.getNumericValue(chars[i])); 
                                badChar = true;
                            }
                        }
                        String cleanLex = new String(cleanChars);
                        if (badChar) {
                            retractionsModel.add(stmt);
                            Literal newLit = null;
                            if (lit.getLanguage()!=null && lit.getLanguage().length()>0) {
                                newLit = additionsModel.createLiteral(cleanLex,lit.getLanguage());
                            } else if (lit.getDatatype() != null) {
                                newLit = additionsModel.createTypedLiteral(cleanLex,lit.getDatatype());
                            } else {
                                newLit = additionsModel.createLiteral(cleanLex);
                            }
                            additionsModel.add(stmt.getSubject(),stmt.getPredicate(),newLit);
                        }
                    }
                }    
            } finally {
                closeIt.close();
            }
            model.remove(retractionsModel);
            model.add(additionsModel);
            log.debug("Cleaned " + additionsModel.size() + " literals");
        } finally {
            model.leaveCriticalSection();
        }
    }
    
    private void doExecuteWorkflow(VitroRequest vreq) {
        String workflowURI = vreq.getParameter("workflowURI");
        String workflowStepURI = vreq.getParameter("workflowStepURI");
        OntModel jenaOntModel = (OntModel) getModel("vitro:jenaOntModel",vreq);
        new JenaIngestWorkflowProcessor(
                jenaOntModel.getIndividual(workflowURI),getModelMaker(
                        vreq)).run(jenaOntModel.getIndividual(workflowStepURI));
    }
    
    private String doRename(String oldNamespace,String newNamespace){    
        String uri = null;
        String result = null;
        Integer counter = 0;
        Boolean namespacePresent = false;
		RDFService rdfService = ModelAccess.on(getServletContext())
				.getRDFService();
        try {
            Model baseOntModel = RDFServiceGraph.createRDFServiceModel
                    (new RDFServiceGraph(
                            rdfService, ABOX_ASSERTIONS));
    		OntModel ontModel = ModelAccess.on(getServletContext()).getOntModel();
            List<String> urisToChange = new LinkedList<String>();        
            ontModel.enterCriticalSection(Lock.READ);
            try {
                Iterator<Individual> indIter = ontModel.listIndividuals();
                while( indIter.hasNext()){
                    Individual ind = indIter.next();
                    String namespace = ind.getNameSpace();
                    if( namespace != null ){
                        if( oldNamespace.equals(namespace) ){
                            uri = ind.getURI();    
                            urisToChange.add(uri);
                            namespacePresent = true;
                        }
                    }
                }
            } finally {
                ontModel.leaveCriticalSection();
            }
            if(!namespacePresent){
                result = "no resources renamed";
                return result;
            }    
            for( String oldURIStr : urisToChange){
                long time1 = System.currentTimeMillis();
                Resource res = baseOntModel.getResource(oldURIStr);
                long time2 = System.currentTimeMillis();
                String newURIStr=null;
                Pattern p = Pattern.compile(oldNamespace);
                String candidateString = res.getURI();
                Matcher matcher = p.matcher(candidateString);
                newURIStr = matcher.replaceFirst(newNamespace);
                long time3 = System.currentTimeMillis();
                log.debug("time to get new uri: " + 
                        Long.toString(time3 - time2));
                log.debug("Renaming "+ oldURIStr + " to " + newURIStr);
         
                String whereClause = "} WHERE { \n" +
                        "  GRAPH <" + ABOX_ASSERTIONS + "> { \n" +  
                        "   { <" + oldURIStr + "> ?p <" + oldURIStr + "> } \n " +
                        "     UNION \n" +  
                        "   { <" + oldURIStr + "> ?q ?o } \n " +
                        "     UNION \n" +
                        "   { ?s ?r <" + oldURIStr + "> } \n" +
                        "  } \n" +
                        "}";
                
                String removeQuery = "CONSTRUCT { \n" + 
                                   "   <" + oldURIStr + "> ?p <" + oldURIStr + "> . \n " +
                                   "   <" + oldURIStr + "> ?q ?o . \n " +
                                   "   ?s ?r <" + oldURIStr + "> \n" + whereClause;

                String addQuery = "CONSTRUCT { \n" + 
                        "   <" + newURIStr + "> ?p <" + newURIStr + "> . \n " +
                        "   <" + newURIStr + "> ?q ?o . \n " +
                        "   ?s ?r <" + newURIStr + "> \n" + whereClause;                   
                try {                   
                    ChangeSet cs = rdfService.manufactureChangeSet();
                    cs.addAddition(rdfService.sparqlConstructQuery(
                            addQuery, RDFService.ModelSerializationFormat.N3), 
                                    RDFService.ModelSerializationFormat.N3, 
                                            ABOX_ASSERTIONS);
                    cs.addRemoval(rdfService.sparqlConstructQuery(
                            removeQuery, RDFService.ModelSerializationFormat.N3), 
                                    RDFService.ModelSerializationFormat.N3, 
                                            ABOX_ASSERTIONS); 
                    rdfService.changeSetUpdate(cs);
                } catch (RDFServiceException e) {
                    throw new RuntimeException(e);
                }
                 
                long time4 = System.currentTimeMillis();
                log.debug(" time to rename : " + Long.toString( time4 - time3));
                log.debug(" time for one resource: " + 
                        Long.toString( time4 -time1));          
                counter++;
            }        
            result = counter.toString() + " resources renamed";
            return result;
        } finally {
            if (rdfService != null) {
                rdfService.close();
            }
        }
    }
    
    protected void showModelList(VitroRequest vreq, ModelMaker maker, WhichService modelType) {
    	vreq.setAttribute("modelType", modelType.toString());
        if(modelType == CONTENT){
        	vreq.setAttribute("infoLine", "Main Store models");            
        } else {
        	vreq.setAttribute("infoLine", "Configuration models");
        }
        vreq.setAttribute("modelNames", maker.listModels().toList());
        vreq.setAttribute("bodyAttr", "onLoad=\"init()\"");
        vreq.setAttribute("title","Available Models");
        vreq.setAttribute("bodyJsp",LIST_MODELS_JSP);
    }

	private void processDumpRestore(VitroRequest vreq,
			HttpServletResponse response) throws ServletException, IOException {
		vreq.getRequestDispatcher("/dumpRestore").forward(vreq, response);
	}
	
	public static Model getModel(String name, HttpServletRequest request) {
		return getModelMaker(request).getModel(name);
	}
    
    protected static ModelMaker getModelMaker(HttpServletRequest req){
        ServletContext ctx = req.getSession().getServletContext();
		if (isUsingMainStoreForIngest(req)) {
			RDFService rdfService = ModelAccess.on(ctx).getRDFService(CONTENT);
			return new BlankNodeFilteringModelMaker(rdfService, ModelAccess.on(
					ctx).getModelMaker(CONTENT));
		} else {
			RDFService rdfService = ModelAccess.on(ctx).getRDFService(CONFIGURATION);
			return new BlankNodeFilteringModelMaker(rdfService, ModelAccess.on(
					ctx).getModelMaker(CONFIGURATION));
		}
    }

}
