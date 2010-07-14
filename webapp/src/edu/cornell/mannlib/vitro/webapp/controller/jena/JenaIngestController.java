/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.jena;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mindswap.pellet.exceptions.InconsistentOntologyException;
import org.mindswap.pellet.jena.PelletReasonerFactory;

import com.hp.hpl.jena.db.DBConnection;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
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
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.sql.JDBC;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.DatabaseType;
import com.hp.hpl.jena.sdb.store.LayoutType;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.ResourceUtils;
import com.hp.hpl.jena.util.iterator.ClosableIterator;

import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.InsertException;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.JenaBaseDao;
import edu.cornell.mannlib.vitro.webapp.dao.jena.SimpleOntModelSelector;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VitroJenaModelMaker;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VitroJenaSDBModelMaker;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VitroJenaSpecialModelMaker;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactoryJena;
import edu.cornell.mannlib.vitro.webapp.dao.jena.event.EditEvent;
import edu.cornell.mannlib.vitro.webapp.utils.Csv2Rdf;
import edu.cornell.mannlib.vitro.webapp.utils.jena.JenaIngestUtils;
import edu.cornell.mannlib.vitro.webapp.utils.jena.JenaIngestWorkflowProcessor;
import edu.cornell.mannlib.vitro.webapp.utils.jena.WorkflowOntology;

import edu.cornell.mannlib.vitro.webapp.beans.Ontology;
import edu.cornell.mannlib.vitro.webapp.dao.OntologyDao;

public class JenaIngestController extends BaseEditController {

	private static final Log log = LogFactory.getLog(JenaIngestController.class);
	
	private OntModelSpec ONT_MODEL_SPEC = OntModelSpec.OWL_MEM;
	
	private static final String INGEST_MENU_JSP = "/jenaIngest/ingestMenu.jsp";
	private static final String LIST_MODELS_JSP = "/jenaIngest/listModels.jsp";
	private static final String CREATE_MODEL_JSP = "/jenaIngest/createModel.jsp";
	private static final String LOAD_RDF_DATA_JSP = "/jenaIngest/loadRDFData.jsp";
	private static final String EXECUTE_SPARQL_JSP = "/jenaIngest/sparqlConstruct.jsp";
	private static final String RENAME_BNODES_JSP = "/jenaIngest/renameBNodes.jsp";
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
	private static final String MERGE_INDIVIDUALS = "/jenaIngest/mergeIndividuals.jsp";
	private static final String MERGE_RESULT = "/templates/edit/specific/merge_result.jsp";
	private static final String SPARQL_CONSTRUCT_CLASS = "http://vitro.mannlib.cornell.edu/ns/vitro/0.7/sparql#SPARQLCONSTRUCTQuery";
	private static final String SPARQL_QUERYSTR_PROP = "http://vitro.mannlib.cornell.edu/ns/vitro/0.7/sparql#queryStr";
	private static final String RENAME_RESOURCE = "/jenaIngest/renameResource.jsp";
	private static final String RENAME_RESULT = "/jenaIngest/renameResult.jsp";

	public void doGet (HttpServletRequest request, HttpServletResponse response) {

		if (!checkLoginStatus(request,response)) {
			return;
		}
		
		VitroRequest vreq = new VitroRequest(request);
		
		ModelMaker maker = getVitroJenaModelMaker(vreq);
		
		String actionStr = vreq.getParameter("action");
		actionStr = (actionStr != null) ? actionStr : "";
		
		if ("listModels".equals(actionStr)) {
			request.setAttribute("title","Available Models");
			request.setAttribute("bodyJsp",LIST_MODELS_JSP);
		} else if ("createModel".equals(actionStr)) {
			String modelName = vreq.getParameter("modelName");
			if (modelName != null) {
				doCreateModel(modelName, maker);
				request.setAttribute("title","Available Models");
				request.setAttribute("bodyJsp",LIST_MODELS_JSP);
			} else {
				request.setAttribute("title","Create New Model");
				request.setAttribute("bodyJsp",CREATE_MODEL_JSP);
			}
		} else if ("removeModel".equals(actionStr)) {
			String modelName = vreq.getParameter("modelName");
			if (modelName!=null) {
				doRemoveModel(modelName, maker);
			}
			request.setAttribute("title","Available Models");
			request.setAttribute("bodyJsp",LIST_MODELS_JSP);
		} else if ("loadRDFData".equals(actionStr)) {
			String docLoc = vreq.getParameter("docLoc");
			String filePath = vreq.getParameter("filePath");
			String modelName = vreq.getParameter("modelName");
			String languageParam = null;
			String language = ( (languageParam = vreq.getParameter("language")) != null) ? languageParam : "RDF/XML";
			if (docLoc!=null && modelName != null) {
				doLoadRDFData(modelName,docLoc,filePath,language,maker);
				request.setAttribute("title","Ingest Menu");
				request.setAttribute("bodyJsp",INGEST_MENU_JSP);
			} else {
				request.setAttribute("title","Load RDF Data");
				request.setAttribute("bodyJsp",LOAD_RDF_DATA_JSP);
			}
		} else if ("cleanLiterals".equals(actionStr)) {
			String modelNameStr = vreq.getParameter("modelName");
			Model model = getModel(modelNameStr,vreq);
			doCleanLiterals(model);
			request.setAttribute("title","Ingest Menu");
			request.setAttribute("bodyJsp",INGEST_MENU_JSP);
		} else if ("outputModel".equals(actionStr)) {
			String modelNameStr = vreq.getParameter("modelName");
			Model model = getModel(modelNameStr,vreq);
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
				System.out.println("Cannot encode character with byte values: (decimal) ");
				for (int i=0; i<badCharBytes.length; i++) {
					System.out.println(badCharBytes[i]);
				}
			} catch (Exception e) {
				// Well if we can't write out to the response I guess there ain't much we can do.
				e.printStackTrace();
			} finally {
				model.leaveCriticalSection();
			}
			return;
		} else if ("clearModel".equals(actionStr)) {
			String modelName = vreq.getParameter("modelName");
			if (modelName != null) {
				doClearModel(modelName,maker);
			}
			request.setAttribute("title","Ingest Menu");
			request.setAttribute("bodyJsp",INGEST_MENU_JSP);
		} else if ("setWriteLayer".equals(actionStr)) {
			String modelName = vreq.getParameter("modelName");
			if (modelName != null) {
				OntModel mainModel = (OntModel) getServletContext().getAttribute("jenaOntModel");
				WebappDaoFactoryJena existingDaoFactory = null;
				try {
					existingDaoFactory = (WebappDaoFactoryJena) getServletContext().getAttribute("webappDaoFactory");
				} catch (Exception e) {}
				Model writeModel = maker.getModel(modelName);
				Model dynamicUnion = ModelFactory.createUnion(writeModel,mainModel);
				OntModel ontModelForDaos = ModelFactory.createOntologyModel(ONT_MODEL_SPEC, dynamicUnion);
				WebappDaoFactory wadf = new WebappDaoFactoryJena(new SimpleOntModelSelector(ontModelForDaos), (existingDaoFactory != null) ? existingDaoFactory.getDefaultNamespace() : null, null, null);
				request.getSession().setAttribute("webappDaoFactory", wadf);
				request.getSession().setAttribute("jenaOntModel",ontModelForDaos);
				System.out.println("Setting jenaOntModel session attribute");
				Model baseModel = (OntModel) getServletContext().getAttribute("baseOntModel");
				OntModel ontModelForAssertions = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM,ModelFactory.createUnion(writeModel,baseModel));
				request.getSession().setAttribute("assertionsWebappDaoFactory", new WebappDaoFactoryJena(new SimpleOntModelSelector(ontModelForAssertions)));
				request.getSession().setAttribute("baseOntModel", ontModelForAssertions);
			}
			request.setAttribute("title","Ingest Menu");
			request.setAttribute("bodyJsp",INGEST_MENU_JSP);
		} else if ("attachModel".equals(actionStr)) {
			String modelName = vreq.getParameter("modelName");
			if (modelName != null) {
				doAttachModel(modelName,maker);
			}
			request.setAttribute("title","Ingest Menu");
			request.setAttribute("bodyJsp",INGEST_MENU_JSP);
		} else if ("detachModel".equals(actionStr)) {
			String modelName = vreq.getParameter("modelName");
			if (modelName != null) {
				doDetachModel(modelName,maker);
			}
			request.setAttribute("title","Ingest Menu");
			request.setAttribute("bodyJsp",INGEST_MENU_JSP);
		} else if ("renameBNodes".equals(actionStr)) {
			String namespaceEtcStr = vreq.getParameter("namespaceEtcStr");
			if (namespaceEtcStr != null) {
				doRenameBNodes(vreq);
				request.setAttribute("title","Ingest Menu");
				request.setAttribute("bodyJsp",INGEST_MENU_JSP);
			} else {
				request.setAttribute("title","Rename Blank Nodes");
				request.setAttribute("bodyJsp",RENAME_BNODES_JSP);
			}
		} else if ("smushSingleModel".equals(actionStr)) {
			String propertyURIStr = vreq.getParameter("propertyURI");
			if (propertyURIStr != null) {
				doSmushSingleModel(vreq);
				request.setAttribute("title","Ingest Menu");
				request.setAttribute("bodyJsp",INGEST_MENU_JSP);
			} else {
				request.setAttribute("title","Smush Resources");
				request.setAttribute("bodyJsp",SMUSH_JSP);
			}
		} else if ("connectDB".equals(actionStr)) {
			String jdbcUrl = vreq.getParameter("jdbcUrl");
			if (jdbcUrl != null) {
				doConnectDB(vreq);
				request.setAttribute("title","Ingest Menu");
				request.setAttribute("bodyJsp",INGEST_MENU_JSP);
			} else {
				request.setAttribute("title", "Connect Jena Database");
				request.setAttribute("bodyJsp",CONNECT_DB_JSP);
			}
		} else if ("csv2rdf".equals(actionStr)) {
			String csvUrl = vreq.getParameter("csvUrl");
			if (csvUrl != null) {
				doExecuteCsv2Rdf(vreq);
				request.setAttribute("title","IngestMenu");
				request.setAttribute("bodyJsp", INGEST_MENU_JSP);
			} else {
				request.setAttribute("title","Convert CSV to RDF");
				request.setAttribute("bodyJsp",CSV2RDF_JSP);
			}
		} else if ("processStrings".equals(actionStr)) {
			String className = vreq.getParameter("className");
			if (className != null) {
				doProcessStrings(vreq);
				request.setAttribute("title","IngestMenu");
				request.setAttribute("bodyJsp", INGEST_MENU_JSP);
			} else {
				request.setAttribute("title","Process Strings");
				request.setAttribute("bodyJsp",PROCESS_STRINGS_JSP);
			}
		} else if ("splitPropertyValues".equals(actionStr)) {
			String splitRegex = vreq.getParameter("splitRegex");
			if (splitRegex != null) {
				doSplitPropertyValues(vreq);
				request.setAttribute("title","IngestMenu");
				request.setAttribute("bodyJsp", INGEST_MENU_JSP);
			} else {
				request.setAttribute("title","Split PropertyValues");
				request.setAttribute("bodyJsp",SPLIT_PROPERTY_VALUES_JSP);
			}
		} else if ("subtractModels".equals(actionStr)) {
			String modela = vreq.getParameter("modela");
			if (modela != null) {
				doSubtractModels(vreq);
				request.setAttribute("title","IngestMenu");
				request.setAttribute("bodyJsp", INGEST_MENU_JSP);
			} else {
				request.setAttribute("title", "Subtract Models");
				request.setAttribute("bodyJsp",SUBTRACT_MODELS_JSP);
			}
		} else if ("executeWorkflow".equals(actionStr)) {
			String workflowURIStr = vreq.getParameter("workflowURI");
			String workflowStepURIStr = vreq.getParameter("workflowStepURI");
			if (workflowURIStr != null && workflowStepURIStr != null) {
				doExecuteWorkflow(vreq);
				request.setAttribute("title","IngestMenu");
				request.setAttribute("bodyJsp", INGEST_MENU_JSP);
			} else if (workflowURIStr != null) {
				// Select the workflow step at which to start
				OntModel jenaOntModel = (OntModel) getModel("vitro:jenaOntModel",vreq);
				request.setAttribute("workflowSteps", new JenaIngestWorkflowProcessor(jenaOntModel.getIndividual(workflowURIStr),getVitroJenaModelMaker(vreq)).getWorkflowSteps(null));
				request.setAttribute("title", "Choose Workflow Step");
				request.setAttribute("bodyJsp", WORKFLOW_STEP_JSP);
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
				request.setAttribute("workflows",savedQueryList);
				request.setAttribute("title", "Execute Workflow");
				request.setAttribute("bodyJsp", EXECUTE_WORKFLOW_JSP);
			}
		} else if ("executeSparql".equals(actionStr)) {
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
	            
	            request.setAttribute("prefixList", prefixList);
	            /*complete*/
			if (sparqlQueryStr != null) {
				String validationMessage = "";
				if (request.getParameterValues("sourceModelName") == null) {
					validationMessage += "<p>Please select one or more source models.</p>";
				}
				if (request.getParameter("destinationModelName") == null) {
					validationMessage += "<p>Please select a destination model</p>";
				}
				if (validationMessage.length() > 0) {
					request.setAttribute("validationMessage", validationMessage);
				} else {
					long constructedStmtCount = 0;
					try {
						constructedStmtCount = doExecuteSparql(vreq);
					} catch (QueryParseException qpe) {
						String errorMsg = "<p>Unable to parse query:</p>";
						if (qpe.getMessage() != null) {
							errorMsg += "<p>" + qpe.getMessage() + "</p>"; 
						}
						request.setAttribute("errorMsg", errorMsg);
					} catch (InconsistentOntologyException ioe) {
						String errorMsg = "<p>Inconsistent source ontology:</p>";
						if (ioe.getMessage() != null) {
							errorMsg += "<p>" + ioe.getMessage() + "</p>";
						}
						request.setAttribute("errorMsg", errorMsg);
					}
					request.setAttribute("constructedStmtCount", constructedStmtCount);
				}
				request.setAttribute("savedQueries",savedQueryList);
				request.setAttribute("title","SPARQL CONSTRUCT result");
				request.setAttribute("bodyJsp",EXECUTE_SPARQL_JSP);
			} else {	
				request.setAttribute("savedQueries",savedQueryList);
				request.setAttribute("title","Execute SPARQL Construct");
				request.setAttribute("bodyJsp",EXECUTE_SPARQL_JSP);
			}
		} else if ("generateTBox".equals(actionStr)) {
			String testParam = vreq.getParameter("sourceModelName");
			if (testParam != null) {
				doGenerateTBox(vreq);
				request.setAttribute("title","Ingest Menu");
				request.setAttribute("bodyJsp",INGEST_MENU_JSP);
			} else {
				request.setAttribute("title","Generate TBox from Assertions Data");
				request.setAttribute("bodyJsp",GENERATE_TBOX_JSP);
			}			
		}else if("permanentURI".equals(actionStr)){
			  String modelName = vreq.getParameter("modelName");
			  String oldModel = vreq.getParameter("oldModel");
			  String newModel = vreq.getParameter("newModel");
			  String oldNamespace = vreq.getParameter("oldNamespace");
			  String newNamespace = vreq.getParameter("newNamespace");
			  String dNamespace = vreq.getParameter("defaultNamespace");
			  
			  if(modelName!=null){
			  Model m = maker.getModel(modelName);
			  ArrayList namespaceList = new ArrayList();
			  Iterator namespaceItr = m.listNameSpaces();
			  
			  if(namespaceItr!=null){
          	while(namespaceItr.hasNext()){
          		
          		namespaceList.add(namespaceItr.next().toString());
          		
          	}
          	}
			  else {
				  namespaceList.add("no resources present");
			  }
			  String defaultNamespace = vreq.getFullWebappDaoFactory().getDefaultNamespace();
			  request.setAttribute("modelName", modelName);
			  request.setAttribute("defaultNamespace", defaultNamespace);
          	  request.setAttribute("namespaceList", namespaceList);
			  request.setAttribute("title","Permanent URI");
			  request.setAttribute("bodyJsp",PERMANENT_URI);
			  }
			  else if(oldModel!=null){
				  doPermanentURI(oldModel,newModel,oldNamespace,newNamespace,dNamespace,maker,vreq);
				  request.setAttribute("title","Ingest Menu");
				  request.setAttribute("bodyJsp",INGEST_MENU_JSP);
			  }
			  
			  
		  }
		else if("mergeIndividuals".equals(actionStr)){
			  String uri1 = vreq.getParameter("uri1");
			  String uri2 = vreq.getParameter("uri2");
			  if(uri1!=null){
				  String result = doMerge(uri1,uri2,response,request);
				  request.setAttribute("result",result);
				  request.setAttribute("title","Merge Individuals");
				  request.setAttribute("bodyJsp",MERGE_RESULT);
				   
			  }
			  else{
				  request.setAttribute("title","Merge Individuals");
				  request.setAttribute("bodyJsp",MERGE_INDIVIDUALS);  
			  }
			   
		  }
		else if("renameResource".equals(actionStr)){
			  String uri1 = vreq.getParameter("uri1");
			  String uri2 = vreq.getParameter("uri2");
			  if(uri1!=null){
				  String result = doRename(uri1,uri2,response);
				  request.setAttribute("result",result);
				  request.setAttribute("title","Rename Resources");
				  request.setAttribute("bodyJsp",RENAME_RESULT);
				   
			  }
			  else{
				  request.setAttribute("title","Rename Resource");
				  request.setAttribute("bodyJsp",RENAME_RESOURCE);  
			  }
			  
		  }
		
		else {
			request.setAttribute("title","Ingest Menu");
			request.setAttribute("bodyJsp",INGEST_MENU_JSP);
		}
		
        Portal portal = vreq.getPortal();
		RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);      
        request.setAttribute("portalBean",portal);
        request.setAttribute("css", "<link rel=\"stylesheet\" type=\"text/css\" href=\""+portal.getThemeDir()+"css/edit.css\"/>");

        try {
            rd.forward(request, response);
        } catch (Exception e) {
            System.out.println(this.getClass().getName()+" could not forward to view.");
            System.out.println(e.getMessage());
            System.out.println(e.getStackTrace());
        }
		
	}
	
	private ModelMaker getVitroJenaModelMaker(HttpServletRequest request) {
		ModelMaker myVjmm = (ModelMaker) request.getSession().getAttribute("vitroJenaModelMaker");
		myVjmm = (myVjmm == null) ? (ModelMaker) getServletContext().getAttribute("vitroJenaModelMaker") : myVjmm;
		return new VitroJenaSpecialModelMaker(myVjmm, request);
	}
	
	private Model getModel(String name, HttpServletRequest request) {
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
	
	private void doCreateModel(String modelName, ModelMaker modelMaker) {
		modelMaker.createModel(modelName);
	}
	
	private void doRemoveModel(String modelName, ModelMaker modelMaker) {
	    //Try to detach first since it cause problems to remove an attached model.	    
	    doDetachModel(modelName, modelMaker);
	    System.out.println("Removing "+modelName+" from webapp");
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
		Model m = modelMaker.getModel(modelName);
		OntModel vitroJenaModel = (OntModel) getServletContext().getAttribute("baseOntModel");
		System.out.println("Attaching "+modelName+" ("+m.hashCode()+") to webapp");
		vitroJenaModel.addSubModel(m);
	}
	
	private void doDetachModel(String modelName, ModelMaker modelMaker) {
		Model m = modelMaker.getModel(modelName);
		OntModel vitroJenaModel = (OntModel) getServletContext().getAttribute("baseOntModel");
		System.out.println("Detaching "+modelName+" ("+m.hashCode()+") from webapp");
		vitroJenaModel.removeSubModel(m);
	}
	
	private void doRenameBNodes(VitroRequest vreq) {
		OntModel source = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
		String[] sourceModel = vreq.getParameterValues("sourceModelName");
		for (int i=0; i<sourceModel.length; i++) {
			Model m = getModel(sourceModel[i],vreq);
			source.addSubModel(m);
		}
		Model destination = getModel(vreq.getParameter("destinationModelName"),vreq);
		String namespaceEtc = vreq.getParameter("namespaceEtcStr");
		JenaIngestUtils utils = new JenaIngestUtils();
		destination.enterCriticalSection(Lock.WRITE);
		try {
			destination.add(utils.renameBNodes(source, namespaceEtc));
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
			System.out.println("Using entered query");
			queryStr = sparqlQueryStr;
		} else {
			Property queryStrProp = ResourceFactory.createProperty(SPARQL_QUERYSTR_PROP);
			jenaOntModel.enterCriticalSection(Lock.READ);
			try {
				Individual ind = jenaOntModel.getIndividual(savedQueryURIStr);
				System.out.println("Using query "+savedQueryURIStr);
				queryStr = ( (Literal) ind.getPropertyValue(queryStrProp)).getLexicalForm();
				queryStr = StringEscapeUtils.unescapeHtml(queryStr); // !!! We need to turn off automatic HTML-escaping for data property editing.
			} finally {
				jenaOntModel.leaveCriticalSection();
			}
		}
		//System.out.println(queryStr);
		Model tempModel = ModelFactory.createDefaultModel();
		Query query = QueryFactory.create(queryStr, Syntax.syntaxARQ);
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
	
	public void doConnectDB(VitroRequest vreq) {
		String jdbcUrl = vreq.getParameter("jdbcUrl");
		String username = vreq.getParameter("username");
		String password = vreq.getParameter("password");
		String dbType = vreq.getParameter("dbType");
		String tripleStore = vreq.getParameter("tripleStore");
		DatabaseType dbTypeObj = null;
		if ("MySQL".equals(dbType)) {
			jdbcUrl += (jdbcUrl.contains("?")) ? "&" : "?";
			jdbcUrl += "useUnicode=yes&characterEncoding=utf8";
			dbTypeObj = DatabaseType.MySQL;
			JDBC.loadDriverMySQL();
		}
		if ("SDB".equals(tripleStore)) {
			StoreDesc storeDesc = new StoreDesc(LayoutType.LayoutTripleNodesHash,dbTypeObj) ;
        	SDBConnection conn = new SDBConnection(jdbcUrl, username, password) ; 
        	Store store = SDBFactory.connectStore(conn, storeDesc);
        	VitroJenaSDBModelMaker vjmm = new VitroJenaSDBModelMaker(store);
        	vreq.getSession().setAttribute("vitroJenaModelMaker",vjmm);
		} else {
			DBConnection dbConn = new DBConnection(jdbcUrl,username,password,dbType);
			System.out.println("Connecting to DB at "+jdbcUrl);
	        ModelMaker mMaker = ModelFactory.createModelRDBMaker(dbConn);
	        VitroJenaModelMaker vjmm = new VitroJenaModelMaker(mMaker);
	        vreq.getSession().setAttribute("vitroJenaModelMaker",vjmm);
		}
	}
	
	public void doExecuteCsv2Rdf(VitroRequest vreq) {
		char[] quoteChars = {'"'};
		String namespace = vreq.getParameter("namespace");
		String tboxNamespace = vreq.getParameter("tboxNamespace");
		String typeName = vreq.getParameter("typeName");
		String csvUrl = vreq.getParameter("csvUrl");
		Model destination = null;
		String destinationModelNameStr = vreq.getParameter("destinationModelName");
		if (destinationModelNameStr != null && destinationModelNameStr.length()>0) {
			destination = getModel(destinationModelNameStr, vreq);
		}
		Model tboxDestination = null;
		String tboxDestinationModelNameStr = vreq.getParameter("tboxDestinationModelName");
		if (tboxDestinationModelNameStr != null && tboxDestinationModelNameStr.length()>0) {
			tboxDestination = getModel(tboxDestinationModelNameStr, vreq);
		}
		
		char separatorChar = ',';
		if ("tab".equalsIgnoreCase(vreq.getParameter("separatorChar"))) {
			separatorChar = '\t';
		}
		
		Csv2Rdf c2r = new Csv2Rdf(separatorChar, quoteChars,namespace,tboxNamespace,typeName);
		
		InputStream is = null;
		
		try {
			is = new URL(csvUrl).openStream();
		} catch (IOException e) {
			System.out.println("IOException opening URL "+csvUrl);
			return;
		}
		
		Model[] models = null;
		
		try {
			 models = c2r.convertToRdf(is);
		} catch (IOException e) {
			System.out.println("IOException converting "+csvUrl+" to RDF");
		}
		
		if (destination != null) {
			destination.add(models[0]);
		}
		if (tboxDestination != null) {
			tboxDestination.add(models[1]);
		}
				
	}

	public void doSubtractModels(VitroRequest vreq) {
			String modela = vreq.getParameter("modela");
			String modelb = vreq.getParameter("modelb");
			String destination = vreq.getParameter("destinationModelName");
			Model ma = getModel(modela,vreq);
			Model mb = getModel(modelb,vreq);
			Model destinationModel = getModel(destination,vreq);
			destinationModel.add(ma.difference(mb));
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
							e.getTargetException().printStackTrace();
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
			e.printStackTrace();
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
								System.out.println("Bad char in "+lex);
								System.out.println("Numeric value "+java.lang.Character.getNumericValue(chars[i])); 
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
			System.out.println("Cleaned "+additionsModel.size()+" literals");
		} finally {
			model.leaveCriticalSection();
		}
	}
	
	private void doExecuteWorkflow(VitroRequest vreq) {
		String workflowURI = vreq.getParameter("workflowURI");
		String workflowStepURI = vreq.getParameter("workflowStepURI");
		OntModel jenaOntModel = (OntModel) getModel("vitro:jenaOntModel",vreq);
		new JenaIngestWorkflowProcessor(jenaOntModel.getIndividual(workflowURI),getVitroJenaModelMaker(vreq)).run(jenaOntModel.getIndividual(workflowStepURI));
	}

	private void doPermanentURI(String oldModel,String newModel,String oldNamespace,
			String newNamespace,String dNamespace,ModelMaker maker,VitroRequest vreq){
			
			
		    WebappDaoFactory wdf = vreq.getFullWebappDaoFactory();
			Model m = maker.getModel(oldModel);
			Model saveModel = maker.getModel(newModel);
			ResIterator rsItr = m.listResourcesWithProperty((Property)null);
			
			String uri = null;  
			while(rsItr.hasNext()){
				Resource res = rsItr.next();
				if(oldNamespace.equals(res.getNameSpace())){
					Resource newRes = null;
					if(!newNamespace.equals("")){
						uri = getUnusedURI(newNamespace);
					}
					else if(!dNamespace.equals("")){
						try{
				    		 uri = wdf.getIndividualDao().getUnusedURI(null);
				        }catch(InsertException ex){
				        	log.error("could not create uri");
				        }     	  
					}
					newRes = saveModel.createResource(uri);
					StmtIterator stmItr = m.listStatements(res, (Property)null, (RDFNode)null);
					while(stmItr.hasNext()){
						Statement stmt = stmItr.next();
						Property prop = stmt.getPredicate();
						RDFNode  node = stmt.getObject();
						saveModel.add(newRes, prop, node);
					}
					res.removeAll((Property)null);
					
				}
			}
			
		
			
		}
	private String getUnusedURI(String newNamespace){
		String uri = null;
		Random random = new Random();
		boolean resourcePresent=true;
		OntModel vitroJenaModel = (OntModel) getServletContext().getAttribute("baseOntModel");
		Resource res = null;
		do{
		uri = newNamespace + "individual" + random.nextInt();
		res = vitroJenaModel.getResource(uri);
		StmtIterator stmtItr1 = vitroJenaModel.listStatements(res,(Property)null,(RDFNode)null);
		if(!stmtItr1.hasNext()){
			resourcePresent=false;
			res.removeAll((Property)null);
		}
		}while(resourcePresent);
		return uri;
	}
	private String doMerge(String uri1, String uri2,HttpServletResponse response,HttpServletRequest request){
		OntModel vitroJenaModel = (OntModel) getServletContext().getAttribute("baseOntModel");
		Resource res1 = vitroJenaModel.getResource(uri1);
		Resource res2 = vitroJenaModel.getResource(uri2);
		String result = null;
		vitroJenaModel.enterCriticalSection(Lock.WRITE);
		StmtIterator stmtItr1 = vitroJenaModel.listStatements(res1,(Property)null,(RDFNode)null);
		StmtIterator stmtItr2 = vitroJenaModel.listStatements(res2,(Property)null,(RDFNode)null);
		if(!stmtItr1.hasNext()){
			result = "resource 1 not present";
			res1.removeAll((Property)null);
			vitroJenaModel.leaveCriticalSection();
			return result;
		}
		else if(!stmtItr2.hasNext()){
			result = "resource 2 not present";
			res2.removeAll((Property)null);
			vitroJenaModel.leaveCriticalSection();
			return result;
		}
		
		int counter = 0;
		Model leftoverModel = ModelFactory.createDefaultModel();
		while(stmtItr2.hasNext()){
		
			Statement stmt = stmtItr2.next();
			Property prop = stmt.getPredicate();
			OntProperty oprop = vitroJenaModel.getOntProperty(prop.getURI());
		    if(oprop!=null && oprop.isFunctionalProperty()){
		    	leftoverModel.add(res2,stmt.getPredicate(),stmt.getObject());
		    }
		    else{
		    vitroJenaModel.add(res1,stmt.getPredicate(),stmt.getObject()); 
		    counter++;
		    }
		}
		res2.removeAll((Property)null);
		vitroJenaModel.leaveCriticalSection();
		response.setContentType("RDF/XML-ABBREV");
		try{
		OutputStream outStream = response.getOutputStream();
		outStream.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>".getBytes());
		leftoverModel.write( outStream,"RDF/XML-ABBREV");
		request.getSession().setAttribute("leftoverModel", leftoverModel);
		outStream.flush();
		outStream.close();
		}
		catch(IOException ioe){
			throw new RuntimeException(ioe);
		}
		result = "merging done for " + counter + " statements.";
		return result;
			
	}
private String doRename(String uri1,String uri2,HttpServletResponse response){
		
		String userURI = null;
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
		WebappDaoFactory wdf =
		(WebappDaoFactory)getServletContext().getAttribute("webappDaoFactory");
		List<String> urisToChange = new LinkedList<String>();
		
		ontModel.enterCriticalSection(Lock.READ);
		try {
		Iterator<Individual> indIter = ontModel.listIndividuals();
		while( indIter.hasNext()){
		Individual ind = indIter.next();
		String namespace = ind.getNameSpace();
		if( namespace != null ){
		if( uri1.equals(namespace) ){
		uri = ind.getURI();	
		urisToChange.add(uri);
		namespacePresent = true;
		}
		}
		}
		}finally {
		ontModel.leaveCriticalSection();
		}
		if(!namespacePresent){
			result = "0 resource renamed";
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
		newURIStr = getUnusedURI(uri2);
		long time3 = System.currentTimeMillis();
		System.out.println("time to get new uri: " + Long.toString(time3 - time2)
		);
		System.out.println("Renaming "+ oldURIStr + " to " + newURIStr);
		ResourceUtils.renameResource(res,newURIStr);
		ResourceUtils.renameResource(infRes,newURIStr);
		long time4 = System.currentTimeMillis();
		System.out.println(" time to rename : " + Long.toString( time4 - time3));
		System.out.println(" time for one resource: " + Long.toString( time4 -
		time1));
		} finally {
		infOntModel.leaveCriticalSection();
		ontModel.leaveCriticalSection();
		baseOntModel.leaveCriticalSection();
		}
		try {
			Thread.currentThread().sleep(200);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		counter++;
		}
		
		/*baseOntModel.enterCriticalSection(Lock.WRITE);
		ontModel.enterCriticalSection(Lock.WRITE);
		try{
		baseOntModel.getBaseModel().notifyEvent(new EditEvent(null,true));
		baseOntModel.getBaseModel().notifyEvent(new EditEvent(null,false));
		} finally {
		ontModel.leaveCriticalSection();
		baseOntModel.leaveCriticalSection();
		}*/
		result = counter.toString() + " resources renamed";
		return result;
		
	}
	public void prepareSmush (VitroRequest vreq) {
		String smushPropURI = vreq.getParameter("smushPropURI");
	}
	
}
