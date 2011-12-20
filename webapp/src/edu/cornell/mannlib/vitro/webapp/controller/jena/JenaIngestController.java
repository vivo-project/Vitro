/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.jena;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mindswap.pellet.exceptions.InconsistentOntologyException;
import org.mindswap.pellet.jena.PelletReasonerFactory;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecException;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.query.Syntax;
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
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.sql.JDBC;
import com.hp.hpl.jena.sdb.store.DatabaseType;
import com.hp.hpl.jena.sdb.store.LayoutType;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.ResourceUtils;
import com.hp.hpl.jena.util.iterator.ClosableIterator;

import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.UseAdvancedDataToolsPages;
import edu.cornell.mannlib.vitro.webapp.beans.Ontology;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.SparqlQueryServlet;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.OntologyDao;
import edu.cornell.mannlib.vitro.webapp.dao.jena.JenaBaseDao;
import edu.cornell.mannlib.vitro.webapp.dao.jena.ModelContext;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VitroJenaModelMaker;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VitroJenaSDBModelMaker;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VitroJenaSpecialModelMaker;
import edu.cornell.mannlib.vitro.webapp.dao.jena.event.EditEvent;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.JenaDataSourceSetup;
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
    private static final String CONNECT_DB_JSP = "/jenaIngest/connectDB.jsp";
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

    private static final Map<String, Model> attachedModels = new HashMap<String, Model>();
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        if (!isAuthorizedToDisplayPage(request, response, new Actions(
                new UseAdvancedDataToolsPages()))) {
            return;
        }
        
        VitroRequest vreq = new VitroRequest(request);   
        ModelMaker maker = getVitroJenaModelMaker(vreq);
        String modelType = getModelType(vreq, maker);
        
        String actionStr = vreq.getParameter("action");
        actionStr = (actionStr != null) ? actionStr : "";
        
        if("listModels".equals(actionStr)) {
            processListModelsRequest(vreq, maker, modelType);
        } else if("rdbModels".equals(actionStr)){
            processRDBModelsRequest(vreq, maker, modelType);
        } else if("sdbModels".equals(actionStr)){
            processSDBModelsRequest(vreq, maker, modelType);
        } else if("createModel".equals(actionStr)) {
            processCreateModelRequest(vreq, maker, modelType);
        } else if("removeModel".equals(actionStr)) {
            processRemoveModelRequest(vreq, maker, modelType);
        } else if("loadRDFData".equals(actionStr)) {
            processLoadRDFDataRequest(vreq, maker, modelType);
        } else if("cleanLiterals".equals(actionStr)) {
            processCleanLiteralsRequest(vreq, maker, modelType);
        } else if("outputModel".equals(actionStr)) {
            processOutputModelRequest(vreq, response, maker, modelType);
            return; // don't attempt to display a JSP
        } else if("clearModel".equals(actionStr)) {
            processClearModelRequest(vreq, maker, modelType);
        } else if("attachModel".equals(actionStr)) {
            processAttachModelRequest(vreq, maker, modelType);
        } else if("detachModel".equals(actionStr)) {
            processDetachModelRequest(vreq, maker, modelType);
        } else if("renameBNodes".equals(actionStr)) {
            processRenameBNodesRequest(vreq, maker, modelType);
        } else if("renameBNodesURISelect".equals(actionStr)){
            processRenameBNodesURISelectRequest(vreq, maker, modelType);
        } else if("smushSingleModel".equals(actionStr)) {
            processSmushSingleModelRequest(vreq, maker, modelType);
        } else if("connectDB".equals(actionStr)) {
            processConnectDBRequest(vreq, maker, modelType);
        } else if("csv2rdf".equals(actionStr)) {
            processCsv2rdfRequest(vreq, maker, modelType);
        } else if("processStrings".equals(actionStr)) {
            processProcessStringsRequest(vreq, maker, modelType);
        } else if("splitPropertyValues".equals(actionStr)) {
            processSplitPropertyValuesRequest(vreq, maker, modelType);
        } else if("subtractModels".equals(actionStr)) {
            processSubtractModelRequest(vreq, maker, modelType);
        } else if("executeWorkflow".equals(actionStr)) {
            processExecuteWorkflowRequest(vreq, maker, modelType);
        } else if("executeSparql".equals(actionStr)) {
            processExecuteSparqlRequest(vreq, maker, modelType);
        } else if ("generateTBox".equals(actionStr)) {
            processGenerateTBoxRequest(vreq, maker, modelType);
        } else if("permanentURI".equals(actionStr)){
            processPermanentURIRequest(vreq, maker, modelType);          
        } else if("mergeResources".equals(actionStr)){
            processMergeResourceRequest(vreq, maker, modelType);
        } else if("renameResource".equals(actionStr)){
            processRenameResourceRequest(vreq, response, maker, modelType);
        } else if("mergeResult".equals(actionStr)){
            processMergeResultRequest(vreq, response, maker, modelType);
        }
        
        else {
            request.setAttribute("title","Ingest Menu");
            request.setAttribute("bodyJsp",INGEST_MENU_JSP);
        }
        
        maker = getVitroJenaModelMaker(vreq); 
        request.setAttribute("modelNames", maker.listModels().toList());
        request.setAttribute("vitroJenaModelMaker", maker);
   
        RequestDispatcher rd = request.getRequestDispatcher(
                Controllers.BASIC_JSP);      
        try {
            rd.forward(request, response);
        } catch (Exception e) {
            log.error(e,e);
            throw new ServletException(e);
        }
        
    }
    
    private void processListModelsRequest(VitroRequest vreq, ModelMaker maker, String modelType) {
        showModelList(vreq, maker, modelType);
    }
    
    private void processRDBModelsRequest(VitroRequest vreq, ModelMaker maker, String modelType) {
        VitroJenaModelMaker vjmm = (VitroJenaModelMaker) getServletContext().getAttribute("vitroJenaModelMaker");
        vreq.getSession().setAttribute("vitroJenaModelMaker", vjmm);
        showModelList(vreq, vjmm, "rdb");
    }
    
    private void processSDBModelsRequest(VitroRequest vreq, ModelMaker maker, String modelType) {
        VitroJenaSDBModelMaker vsmm = (VitroJenaSDBModelMaker) getServletContext().getAttribute("vitroJenaSDBModelMaker");
        vreq.getSession().setAttribute("vitroJenaModelMaker", vsmm);
        showModelList(vreq, vsmm, "sdb");
    }
    
    private void processCreateModelRequest(VitroRequest vreq, ModelMaker maker, String modelType) {
        String modelName = vreq.getParameter("modelName");
        if (modelName != null) {
            doCreateModel(modelName, maker);
            showModelList(vreq, maker, modelType);
        } else {
            vreq.setAttribute("modelType", modelType);
            vreq.setAttribute("title","Create New Model");
            vreq.setAttribute("bodyJsp",CREATE_MODEL_JSP);
        }
    }
    
    private void processRemoveModelRequest(VitroRequest vreq, ModelMaker maker, String modelType) {
        String modelName = vreq.getParameter("modelName");
        if (modelName!=null) {
            doRemoveModel(modelName, maker);
        }
        showModelList(vreq, maker, modelType);
    }
    
    private void processClearModelRequest(VitroRequest vreq, ModelMaker maker, String modelType) {
        String modelName = vreq.getParameter("modelName");
        if (modelName != null) {
            doClearModel(modelName,maker);
        }
        showModelList(vreq, maker, modelType);
    }
    
    private void processLoadRDFDataRequest(VitroRequest vreq, ModelMaker maker, String modelType) {
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
                                           HttpServletResponse response, 
                                           ModelMaker maker, 
                                           String modelType) {
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
    
    private void processCleanLiteralsRequest(VitroRequest vreq, ModelMaker maker, String modelType) {
        String modelNameStr = vreq.getParameter("modelName");
        Model model = getModel(modelNameStr,vreq);
        doCleanLiterals(model);
        vreq.setAttribute("title","Ingest Menu");
        vreq.setAttribute("bodyJsp",INGEST_MENU_JSP);
    }
    
    private void processAttachModelRequest(VitroRequest vreq, ModelMaker maker, String modelType) {
        String modelName = vreq.getParameter("modelName");
        if (modelName != null) {
            doAttachModel(modelName,maker);
        }
        showModelList(vreq, maker, modelType);
    }
    
    private void processDetachModelRequest(VitroRequest vreq, ModelMaker maker, String modelType) {
        String modelName = vreq.getParameter("modelName");
        if (modelName != null) {
            doDetachModel(modelName,maker);
        }
        //request.setAttribute("title","Ingest Menu");
        //request.setAttribute("bodyJsp",INGEST_MENU_JSP);
        showModelList(vreq, maker, modelType);
    }
    
    private void processRenameBNodesRequest(VitroRequest vreq, ModelMaker maker, String modelType) {
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
    
    private void processRenameBNodesURISelectRequest(VitroRequest vreq, ModelMaker maker, String modelType) {
        String namespaceEtcStr = vreq.getParameter("namespaceEtcStr");
        String pattern = vreq.getParameter("pattern");
        String concatenate = vreq.getParameter("concatenate");
        String[] sourceModel = (String[]) vreq.getParameterValues("sourceModelName");
        if(namespaceEtcStr != null) {
            if (namespaceEtcStr.isEmpty()) {
                if ("true".equals(vreq.getParameter("csv2rdf"))) {
                    processCsv2rdfRequest(vreq, maker, modelType);
                    return;
                } else {
                    vreq.setAttribute("errorMsg", "Please enter a value.");
                    processRenameBNodesRequest(vreq, maker, modelType);
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
    
    private void processSmushSingleModelRequest(VitroRequest vreq, ModelMaker maker, String modelType) {
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
    
    private void processConnectDBRequest(VitroRequest vreq, ModelMaker maker, String modelType) {
        String jdbcUrl = vreq.getParameter("jdbcUrl");
        String tripleStore = vreq.getParameter("tripleStore");
        if (jdbcUrl != null) {
            try {
                doConnectDB(vreq);
            } catch (SQLException sqle) {
                throw new RuntimeException("Unable to connect to DB", sqle);
            }
            if ("SDB".equals(tripleStore)) {
                getServletContext().setAttribute("modelT", "sdb");
                 getServletContext().setAttribute("info", "SDB models");
                vreq.setAttribute("modelType", "sdb");
                vreq.setAttribute("infoLine", "SDB models");
            } else {
                getServletContext().setAttribute("modelT", "rdb");
                getServletContext().setAttribute("info", "RDB models");
                vreq.setAttribute("modelType", "rdb");
                vreq.setAttribute("infoLine", "RDB models");
            }
            vreq.setAttribute("title","Ingest Menu");
            vreq.setAttribute("bodyJsp",INGEST_MENU_JSP);
        } else {
            List<String> dbTypes = DatabaseType.allNames();
            Collections.sort(dbTypes, new CollationSort());
            vreq.setAttribute("dbTypes", dbTypes);
            vreq.setAttribute("title", "Connect Jena Database");
            vreq.setAttribute("bodyJsp",CONNECT_DB_JSP);
        }
    }
    
    private void processCsv2rdfRequest(VitroRequest vreq, ModelMaker maker, String modelType) {
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
    
    private void processProcessStringsRequest(VitroRequest vreq, ModelMaker maker, String modelType) {
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
    
    private void processSplitPropertyValuesRequest(VitroRequest vreq, ModelMaker maker, String modelType) {
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
    
    private void processSubtractModelRequest(VitroRequest vreq, ModelMaker maker, String modelType) {
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
    
    private void processExecuteWorkflowRequest(VitroRequest vreq, ModelMaker maker, String modelType) {
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
                    getVitroJenaModelMaker(vreq)).getWorkflowSteps(null));
            vreq.setAttribute("title", "Choose Workflow Step");
            vreq.setAttribute("bodyJsp", WORKFLOW_STEP_JSP);
        } else {
            OntModel jenaOntModel = (OntModel) getServletContext().getAttribute("jenaOntModel");
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
    
    private void processExecuteSparqlRequest(VitroRequest vreq, ModelMaker maker, String modelType) {
        String sparqlQueryStr = vreq.getParameter("sparqlQueryStr");
        OntModel jenaOntModel = (OntModel) getServletContext().getAttribute("jenaOntModel");
        jenaOntModel.enterCriticalSection(Lock.READ);
        List<Individual> savedQueryList = new LinkedList<Individual>();
        try {
            Resource sparqlConstructClassRes = ResourceFactory.createResource(SPARQL_CONSTRUCT_CLASS);
            savedQueryList.addAll(jenaOntModel.listIndividuals(sparqlConstructClassRes).toList());
        } finally {
            jenaOntModel.leaveCriticalSection();
        }
        /*ass92*/
        OntologyDao daoObj = vreq.getFullWebappDaoFactory().getOntologyDao();
        List ontologiesObj = daoObj.getAllOntologies();
        ArrayList prefixList = new ArrayList();       
        if(ontologiesObj !=null && ontologiesObj.size()>0){
            Iterator ontItr = ontologiesObj.iterator();
            while(ontItr.hasNext()){
                Ontology ont = (Ontology) ontItr.next();
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
    
    private void processGenerateTBoxRequest(VitroRequest vreq, ModelMaker maker, String modelType) {
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
    
    private void processPermanentURIRequest(VitroRequest vreq, ModelMaker maker, String modelType) {
        String modelName = vreq.getParameter("modelName");
        String oldModel = vreq.getParameter("oldModel");
        String newModel = vreq.getParameter("newModel");
        String oldNamespace = vreq.getParameter("oldNamespace");
        String newNamespace = vreq.getParameter("newNamespace");
        String dNamespace = vreq.getParameter("defaultNamespace");      
        if(modelName!=null){
            Model m = maker.getModel(modelName);
            ArrayList namespaceList = new ArrayList();
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
            String defaultNamespace = vreq.getFullWebappDaoFactory().getDefaultNamespace();
            vreq.setAttribute("modelName", modelName);
            vreq.setAttribute("defaultNamespace", defaultNamespace);
              vreq.setAttribute("namespaceList", namespaceList);
            vreq.setAttribute("title","Permanent URI");
            vreq.setAttribute("bodyJsp",PERMANENT_URI);
        } else if(oldModel != null) {
            JenaIngestUtils utils = new JenaIngestUtils();
            utils.doPermanentURI(oldModel,newModel,oldNamespace,newNamespace,dNamespace,maker,vreq);
            vreq.setAttribute("title","Ingest Menu");
            vreq.setAttribute("bodyJsp",INGEST_MENU_JSP);
        }
    }
    
    private void processMergeResourceRequest(VitroRequest vreq, ModelMaker maker, String modelType) {
          String uri1 = vreq.getParameter("uri1"); // get primary uri
          String uri2 = vreq.getParameter("uri2"); // get secondary uri
          String usePrimaryLabelOnly = vreq.getParameter("usePrimaryLabelOnly");
          
          if(uri1!=null){
              JenaIngestUtils utils = new JenaIngestUtils();
              /*
               * get baseOnt and infOnt models
               */
              OntModel baseOntModel = ModelContext.getBaseOntModel(
                      getServletContext());
              OntModel tboxOntModel = ModelContext.getUnionOntModelSelector(
                      getServletContext()).getTBoxModel();
              
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
    
    private void processRenameResourceRequest(VitroRequest vreq, 
                                              HttpServletResponse response, 
                                              ModelMaker maker, 
                                              String modelType) {
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
                  String result = doRename(oldNamespace, newNamespace, response);
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
                                           HttpServletResponse response, 
                                           ModelMaker maker, 
                                           String modelType) {

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
    
    protected ModelMaker getVitroJenaModelMaker(HttpServletRequest request) {
        ModelMaker myVjmm = (ModelMaker) request.getSession().getAttribute("vitroJenaModelMaker");
        myVjmm = (myVjmm == null) ? (ModelMaker) getServletContext().getAttribute("vitroJenaSDBModelMaker") : myVjmm;
        return new VitroJenaSpecialModelMaker(myVjmm, request);
    }
    
    protected Model getModel(String name, HttpServletRequest request) {
        if ("vitro:jenaOntModel".equals(name)) {
            Object sessionOntModel = request.getSession().getAttribute("jenaOntModel");
            if (sessionOntModel != null && sessionOntModel instanceof OntModel) {
                return (OntModel) sessionOntModel;
            } else {
                return (OntModel) getServletContext().getAttribute("jenaOntModel");
            }
        } else if ("vitro:baseOntModel".equals(name)) {
            Object sessionOntModel = request.getSession().getAttribute("baseOntModel");
            if (sessionOntModel != null && sessionOntModel instanceof OntModel) {
                return (OntModel) sessionOntModel;
            } else {
                return (OntModel) getServletContext().getAttribute("baseOntModel");
            }
        } else if ("vitro:inferenceOntModel".equals(name)) {
            Object sessionOntModel = request.getSession().getAttribute("inferenceOntModel");
            if (sessionOntModel != null && sessionOntModel instanceof OntModel) {
                return (OntModel) sessionOntModel;
            } else {
                return (OntModel) getServletContext().getAttribute("inferenceOntModel");
            }
        } else {
            return getVitroJenaModelMaker(request).getModel(name);
        }
    }
    
    protected String getModelType(VitroRequest vreq, ModelMaker maker) {
        String modelType = vreq.getParameter("modelType");
        maker = (maker instanceof VitroJenaSpecialModelMaker) 
                ? ((VitroJenaSpecialModelMaker) maker).getInnerModelMaker()
                : maker;
        if (modelType == null) {
            if (maker instanceof VitroJenaSDBModelMaker) {
                modelType = "sdb";
            } else {
                modelType = "rdb";
            }
        }
        return modelType;
    }
    
    private void doCreateModel(String modelName, ModelMaker modelMaker) {
        modelMaker.createModel(modelName);
    }
    
    private void doRemoveModel(String modelName, ModelMaker modelMaker) {
        //Try to detach first since it cause problems to remove an attached model.        
        doDetachModel(modelName, modelMaker);
        log.debug("Removing " + modelName + " from webapp");
        modelMaker.removeModel(modelName);        
    }
    
    private void doClearModel(String modelName, ModelMaker modelMaker) {
        Model m = modelMaker.getModel(modelName);
        OntModel o = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM,m);
        o.enterCriticalSection(Lock.WRITE);
        try {
            o.removeAll(null,null,null);
        } finally {
            o.leaveCriticalSection();
        }
        // removeAll() doesn't work with the listeners!
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
            return;
        }
        Model m = modelMaker.getModel(modelName);
        ModelContext.getBaseOntModelSelector(getServletContext()).getTBoxModel().addSubModel(m);
        ModelContext.getBaseOntModelSelector(getServletContext()).getABoxModel().addSubModel(m);
        ModelContext.getUnionOntModelSelector(getServletContext()).getABoxModel().addSubModel(m);
        ModelContext.getUnionOntModelSelector(getServletContext()).getTBoxModel().addSubModel(m);
        attachedModels.put(modelName, m);
        log.info("Attached " + modelName + " (" + m.hashCode() + ") to webapp");
    }
    
    private void doDetachModel(String modelName, ModelMaker modelMaker) {
        Model m = attachedModels.get(modelName);
        if (m == null) {
            return;
        }
        ModelContext.getBaseOntModelSelector(getServletContext()).getTBoxModel().removeSubModel(m);
        ModelContext.getBaseOntModelSelector(getServletContext()).getABoxModel().removeSubModel(m);
        ModelContext.getUnionOntModelSelector(getServletContext()).getABoxModel().removeSubModel(m);
        ModelContext.getUnionOntModelSelector(getServletContext()).getTBoxModel().removeSubModel(m);
        attachedModels.remove(modelName);
        log.info("Detached " + modelName + " (" + m.hashCode() + ") from webapp");
    }
    
    private void doRenameBNodes(VitroRequest vreq, String namespaceEtc, boolean patternBoolean, String pattern, String[] sourceModel) {
        OntModel source = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        String property = vreq.getParameter("property");
        
        Boolean csv2rdf = false;
        try {
            csv2rdf = (Boolean) Boolean.parseBoolean(vreq.getParameter("csv2rdf"));
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
        OntModel jenaOntModel = (OntModel) getServletContext().getAttribute("jenaOntModel");
        OntModel source = null;
        if ("pellet".equals(vreq.getParameter("reasoning"))) {
            source = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC);
        } else {
             source = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        }
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
    
    public void doConnectDB(VitroRequest vreq) throws SQLException {
        String jdbcUrl = vreq.getParameter("jdbcUrl");
        String username = vreq.getParameter("username");
        String password = vreq.getParameter("password");
        String dbType = vreq.getParameter("dbType");
        String tripleStore = vreq.getParameter("tripleStore");
        DatabaseType dbTypeObj = null;
        if ("MySQL".equals(dbType)) {
            jdbcUrl += (jdbcUrl.contains("?")) ? "&" : "?";
            jdbcUrl += "useUnicode=yes&characterEncoding=utf8";
        }
        dbTypeObj = DatabaseType.fetch(dbType);
        String driver = loadDriver(dbTypeObj);
        log.debug("Connecting to DB at "+jdbcUrl);
        StoreDesc storeDesc = new StoreDesc(LayoutType.LayoutTripleNodesHash,dbTypeObj) ; 
        ServletContext ctx = vreq.getSession().getServletContext();
        BasicDataSource bds = JenaDataSourceSetup.makeBasicDataSource(
                driver, jdbcUrl, username, password, ctx);
        try {
            VitroJenaSDBModelMaker vsmm = new VitroJenaSDBModelMaker(storeDesc, bds);
              VitroJenaModelMaker vjmm = new VitroJenaModelMaker(jdbcUrl, username, password, dbType, ctx);
            getServletContext().setAttribute("vitroJenaSDBModelMaker", vsmm);
            getServletContext().setAttribute("vitroJenaModelMaker", vjmm);
            if("SDB".equals(tripleStore))
                vreq.getSession().setAttribute("vitroJenaModelMaker",vsmm);
            else
                vreq.getSession().setAttribute("vitroJenaModelMaker",vjmm);
        } catch (SQLException sqle) {
            throw new RuntimeException("Unable to create SDB ModelMaker", sqle);
        }
    }
    
    
    private String loadDriver(DatabaseType dbType) {
        String driverName = JDBC.getDriver(dbType);
        JDBC.loadDriver(driverName);
        return driverName;
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
            Class stringProcessorClass = Class.forName(className);
            Object processor = stringProcessorClass.newInstance();
            Class[] methArgs = {String.class};
            Method meth = stringProcessorClass.getMethod(methodName,methArgs);
            Property prop = ResourceFactory.createProperty(propertyName);
            Property newProp = ResourceFactory.createProperty(newPropertyName);
            destination.enterCriticalSection(Lock.READ);
            try {
                ClosableIterator closeIt = destination.listStatements((Resource)null,prop,(RDFNode)null);
                for (Iterator stmtIt = closeIt; stmtIt.hasNext(); ) {
                    Statement stmt = (Statement) stmtIt.next();
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
            ClosableIterator closeIt = model.listStatements();
            try {
                for (Iterator stmtIt = closeIt; stmtIt.hasNext();) {
                    Statement stmt = (Statement) stmtIt.next();
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
                jenaOntModel.getIndividual(workflowURI),getVitroJenaModelMaker(
                        vreq)).run(jenaOntModel.getIndividual(workflowStepURI));
    }
    
    private String doRename(String oldNamespace,String newNamespace,HttpServletResponse response){    
        String uri = null;
        String result = null;
        Integer counter = 0;
        Boolean namespacePresent = false;
        OntModel baseOntModel = (OntModel)
        getServletContext().getAttribute("baseOntModel");
        OntModel ontModel = (OntModel)
        getServletContext().getAttribute("jenaOntModel");
        OntModel infOntModel = (OntModel)
        getServletContext().getAttribute(JenaBaseDao.INFERENCE_ONT_MODEL_ATTRIBUTE_NAME);
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
            baseOntModel.enterCriticalSection(Lock.WRITE);
            ontModel.enterCriticalSection(Lock.WRITE);
            infOntModel.enterCriticalSection(Lock.WRITE);
            try{
                long time1 = System.currentTimeMillis();
                Resource res = baseOntModel.getResource(oldURIStr);
                Resource infRes = infOntModel.getResource(oldURIStr);
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
                ResourceUtils.renameResource(res,newURIStr);
                ResourceUtils.renameResource(infRes,newURIStr);
                long time4 = System.currentTimeMillis();
                log.debug(" time to rename : " + Long.toString( time4 - time3));
                log.debug(" time for one resource: " + 
                        Long.toString( time4 -time1));
            } finally {
                infOntModel.leaveCriticalSection();
                ontModel.leaveCriticalSection();
                baseOntModel.leaveCriticalSection();
            }
            counter++;
        }        
        result = counter.toString() + " resources renamed";
        return result;
    }
    
    protected void showModelList(VitroRequest vreq, ModelMaker maker, String modelType) {
        if(modelType.equals("rdb")){
            vreq.setAttribute("modelType", "rdb");
            vreq.setAttribute("infoLine", "RDB models");
        } else{
            vreq.setAttribute("modelType", "sdb");
            vreq.setAttribute("infoLine", "SDB models");
        }
        vreq.setAttribute("modelNames", maker.listModels().toList());
        vreq.setAttribute("bodyAttr", "onLoad=\"init()\"");
        vreq.setAttribute("title","Available Models");
        vreq.setAttribute("bodyJsp",LIST_MODELS_JSP);
    }

    private class CollationSort implements Comparator<String> {
        
        Collator collator = Collator.getInstance();
        
        public int compare(String s1, String s2) {
            return collator.compare(s1, s2);
        }
        
    }
    
}
