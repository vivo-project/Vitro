/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.InvalidPropertyURIException;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.ResourceUtils;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;

import edu.cornell.mannlib.vedit.beans.EditProcessObject;
import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.EditOntology;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.jena.ModelContext;
import edu.cornell.mannlib.vitro.webapp.dao.jena.event.EditEvent;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.FileGraphSetup;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.JenaDataSourceSetupBase;

public class RefactorOperationController extends BaseEditController {
	
	private static final Log log = LogFactory.getLog(RefactorOperationController.class.getName());
	private static final boolean NOTIFY = true;
	
	private String doFixDataTypes(HttpServletRequest request, HttpServletResponse response)
	{
		try {
            super.doGet(request,response);
        } catch (Exception e) {
            log.error(this.getClass().getName()+" caught exception calling doGet()");
        }
		VitroRequest vreq = new VitroRequest(request);

        RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
        request.setAttribute("bodyJsp", Controllers.CHECK_DATATYPE_PROPERTIES);
        request.setAttribute("title","Check Datatype Properties");
        request.setAttribute("css", "<link rel=\"stylesheet\" type=\"text/css\" href=\""+vreq.getAppBean().getThemeDir()+"css/edit.css\"/>");
        
		OntModel ontModel = (OntModel) getServletContext().getAttribute("baseOntModel");
		ontModel.enterCriticalSection(Lock.WRITE);
		ArrayList<String> results = new ArrayList<String>();
				
		try
		{
			ExtendedIterator dataProperties = ontModel.listDatatypeProperties();
			int hasRange = 0;
			int consistent = 0;
			int inconsistent = 0;
			int total = 0;
			int fixed = 0;
			while(dataProperties.hasNext()) // Iterate through all datatype properties
			{
				total++;
				DatatypeProperty p = (DatatypeProperty)dataProperties.next();
				OntResource range = p.getRange();
				if(range != null) hasRange++;
				NodeIterator n = ontModel.listObjectsOfProperty(p);
				//if(!n.hasNext()) results.add(p.getLocalName()+" is not in any statements");
				while(n.hasNext()) // Iterate through all objects of all datatype properties
				{
					RDFNode node = n.nextNode();
					if(node.isLiteral())
					{
						if(range != null) // If a literal has a predicate with a defined range, check and fix the literal's datatype
							{
								Literal l = (Literal)node;
								StmtIterator usingPandL = ontModel.listStatements(null, p, l);
								int size = 0;
								results.add("Statements using property "+p.getLocalName()+" and literal "+l.getLexicalForm()+":");
								while(usingPandL.hasNext())
								{
									Statement st = usingPandL.nextStatement();
									results.add("    "+st.getSubject().getLocalName()+" "+p.getLocalName()+" "+l.getLexicalForm());
									size++;
								}
								usingPandL.close();
								boolean valid = range.getURI().equals(l.getDatatypeURI());
								if(valid) consistent+= size;
								else 
									{
										results.add(p.getLocalName()+" has object "+l.getLexicalForm()+" of type "+l.getDatatypeURI()+" which is inconsistent");
										String typeName = "";
										if(range.getURI().contains(XSDDatatype.XSD)) typeName = range.getURI().substring(XSDDatatype.XSD.length()+1);
										else results.add("ERROR: "+p.getLocalName()+" has a range which does not contain the XSD namespace");
										Literal newLiteral = null;
										try {
											newLiteral = ontModel.createTypedLiteral(l.getLexicalForm(), new XSDDatatype(typeName));
										}
										catch(NullPointerException e){
											results.add("ERROR: Can't create XSDDatatype for literal "+l.getLexicalForm());
										}
										StmtIterator badStatements = ontModel.listStatements(null, p, l);
										StmtIterator toRemove = ontModel.listStatements(null, p, l);
										ArrayList<Statement> queue = new ArrayList<Statement>();
										while(badStatements.hasNext()) 
										{
											Statement badState = badStatements.nextStatement();
											Statement goodState = ontModel.createStatement(badState.getSubject(), p, newLiteral);
											queue.add(goodState);
											results.add("    Replacing: "+badState.toString());
											results.add("    With:      "+goodState.toString());
											fixed++;
										}
										for(int i = 0; i<queue.size(); i++)
										{
											ontModel.add(queue.get(i));
										}
										ontModel.remove(toRemove);
										badStatements.close();
										toRemove.close();
									}
								if(valid) results.add("Literal "+l.getLexicalForm()+" is in the range of property "+p.getLocalName());
								results.add("--------------");
							}
					}
					else results.add("ERROR: "+node.toString()+" is not a literal");
					
				}
				n.close();
			}
			dataProperties.close();
			
			results.add(hasRange+" of "+total+" datatype properties have defined ranges.");
			results.add("Of the statements that contain datatype properties with defined ranges, "+consistent+" are consistent and "+fixed+" are inconsistent.");
			results.add(fixed+" statements have been fixed.");
			//for(int i=0; i<results.size(); i++) System.out.println(results.get(i));
			
		}
		finally
		{
			ontModel.leaveCriticalSection();
		}
		request.setAttribute("results", results);
		try {
            rd.forward(request, response);
        } catch (Exception e) {
            log.error(this.getClass().getName()+" could not forward to view.");
            log.error(e.getMessage());
            log.error(e.getStackTrace());
        }

		return "";
	}
	
	private String doRenameResource(VitroRequest request, HttpServletResponse response, EditProcessObject epo) {
		
		String userURI = LoginStatusBean.getBean(request).getUserURI();
		String oldURIStr = (String) epo.getAttribute("oldURI");
		String newURIStr = request.getParameter("newURI");
			
		// validateURI
		String errorMsg = null;
		try {
			request.getFullWebappDaoFactory().checkURI(newURIStr);
		} catch (InvalidPropertyURIException ipue) {
			// TODO We don't know if we're editing a property's URI or not here!
		}
		
		if (errorMsg != null) {
			epo.setAttribute("errorMsg",errorMsg);
            String referer = request.getHeader("Referer");
            int epoKeyIndex = referer.indexOf("_epoKey");
            if (epoKeyIndex<0)
            	try {
            		response.sendRedirect(referer+"&_epoKey="+request.getParameter("_epoKey"));
            	} catch (IOException ioe) {}
            else{
                String url = referer.substring(0,epoKeyIndex) + "_epoKey="+request.getParameter("_epoKey");
                try {
                	response.sendRedirect(url);
                } catch (IOException ioe) {}
            }
            return "STOP";
		}
		
		// find the models that the resource is referred to in and change
		// the name in each of those models.		
		String queryStr = "SELECT distinct ?graph WHERE {{ GRAPH ?graph { ?subj <" +  oldURIStr  + "> ?obj }} ";
		queryStr += " union { GRAPH ?graph { <" + oldURIStr + "> ?prop ?obj }} ";
		queryStr += " union { GRAPH ?graph { ?subj ?prop <" +  oldURIStr  + ">}}}";
		Dataset dataset = request.getDataset();
		
        QueryExecution qexec = null;
    	dataset.getLock().enterCriticalSection(Lock.READ);        
    	try {
            qexec = QueryExecutionFactory.create(QueryFactory.create(queryStr), dataset);
    		ResultSet resultSet = qexec.execSelect();
    		
    		while (resultSet.hasNext()) {
    			QuerySolution qs = resultSet.next();
    			String graphURI = qs.get("graph").asNode().toString();
    			
    			if (graphURI.startsWith(FileGraphSetup.FILEGRAPH_URI_ROOT)) {
    			   continue;	
    			}
    			
    			boolean doNotify = false;
    			Model model = null;
    			
    			if (JenaDataSourceSetupBase.JENA_TBOX_ASSERTIONS_MODEL.equals(graphURI)) {
    				model = ModelContext.getBaseOntModelSelector(getServletContext()).getTBoxModel();
    				doNotify = true;
    			} else if (JenaDataSourceSetupBase.JENA_DB_MODEL.equals(graphURI)) {
					model = ModelContext.getBaseOntModelSelector(getServletContext()).getABoxModel();
					doNotify = true;
    			} else {
    			    model = dataset.getNamedModel(graphURI);
    		    }
    			
    			renameResourceInModel(model, userURI, oldURIStr, newURIStr, doNotify);
    		}	
    	} finally {
            if(qexec != null) qexec.close();
    		dataset.getLock().leaveCriticalSection();
    	}
		
		renameResourceInModel(ModelContext.getOntModelSelector(
				getServletContext()).getUserAccountsModel(), 
				        userURI, oldURIStr, newURIStr, !NOTIFY);
    	
		// there are no statements to delete, but we want indexes updated appropriately
		request.getFullWebappDaoFactory().getIndividualDao().deleteIndividual(oldURIStr);
		
		String redirectStr = null;
		
		/* we can't go back to the referer, because the URI is now different. */
		String refererStr;
		if ( (refererStr = epo.getReferer()) != null) {
			String controllerStr = null;
			String[] controllers = {"entityEdit", "propertyEdit", "datapropEdit", "ontologyEdit", "vclassEdit"};
			for (int i=0; i<controllers.length; i++) {
				if (refererStr.indexOf(controllers[i]) > -1) {
					controllerStr = controllers[i];
				}
			}
			if (controllerStr != null) {
				try {
					newURIStr = URLEncoder.encode(newURIStr, "UTF-8");
				} catch (UnsupportedEncodingException e) {}
				redirectStr = controllerStr+"?uri="+newURIStr;
			}
		}
		
		return redirectStr;
		
	}
	
	private void renameResourceInModel(Model model, String userURI, String oldURIStr, String newURIStr, boolean doNotify) {
				
		model.enterCriticalSection(Lock.WRITE);
		
		if (doNotify) {
		   model.notifyEvent(new EditEvent(userURI,true));
		}
		
		try {
			Property prop = model.getProperty(oldURIStr); // this will create a resource if there isn't
			                                              // one by this URI (we don't expect this to happen
			                                              // and will also return a resource if the given
			                                              // URI is the URI of a class.
			try {
				Property newProp = model.createProperty(newURIStr);
				StmtIterator statements = model.listStatements(null, prop, (RDFNode)null);
				try {
					while(statements.hasNext()) {
						Statement statement = (Statement)statements.next();
						Resource subj = statement.getSubject();
						RDFNode obj = statement.getObject();
						Statement newStatement = model.createStatement(subj, newProp, obj);
						model.add(newStatement);
					}
				} finally {
					if (statements != null) {
						statements.close();
					}
				}
				model.remove(model.listStatements(null, prop, (RDFNode)null));
			} catch (InvalidPropertyURIException ipue) {
				/* if it can't be a property, don't bother with predicates */ 
			}			
			Resource res = model.getResource(oldURIStr);
			ResourceUtils.renameResource(res,newURIStr);
			
		} finally {
			if (doNotify) {
			   model.notifyEvent(new EditEvent(userURI,false));
			}
			model.leaveCriticalSection();
		}		
	}
	
	private void doMovePropertyStatements(VitroRequest request, HttpServletResponse response, EditProcessObject epo) {
		String userURI = LoginStatusBean.getBean(request).getUserURI();
		
		OntModel ontModel = (OntModel) getServletContext().getAttribute("jenaOntModel");
		
		Model tempRetractModel = ModelFactory.createDefaultModel();
		Model tempAddModel = ModelFactory.createDefaultModel();
		
		String oldURIStr = (String) epo.getAttribute("propertyURI");
		String newURIStr = request.getParameter("NewPropertyURI");
		String subjectClassURIStr = request.getParameter("SubjectClassURI");
		String objectClassURIStr = request.getParameter("ObjectClassURI");
		
		ontModel.enterCriticalSection(Lock.READ);
		try {
			Resource res = ontModel.getResource(oldURIStr);
			Resource subjClass = (subjectClassURIStr.equals("") ? null : ResourceFactory.createResource(subjectClassURIStr));
			Property prop = ResourceFactory.createProperty(oldURIStr);
			Property newProp = (newURIStr.equals("")) ? null : ResourceFactory.createProperty(newURIStr);
			OntProperty propInv = null;
			OntProperty newPropInv = null;
			try {
				propInv = ontModel.getObjectProperty(prop.getURI()).getInverse();
			} catch (Exception e) { }
			try {
				newPropInv = ontModel.getObjectProperty(newProp.getURI()).getInverse();
			} catch (Exception e) { }
			RDFNode objClass = (objectClassURIStr == null || objectClassURIStr.equals("")) ? null : ResourceFactory.createResource(objectClassURIStr);
			
			ClosableIterator closeIt = (epo.getAttribute("propertyType").equals("ObjectProperty")) ?
				ontModel.listStatements(null,prop,(Resource)null) :
				ontModel.listStatements(null,prop,(Literal)null);
			try {
				for (Iterator stmtIt = closeIt; stmtIt.hasNext();) {
					Statement stmt = (Statement) stmtIt.next();
					Resource subj = stmt.getSubject();
					boolean moveIt = true;
					if (objClass != null) {
						Resource obj = (Resource) stmt.getObject();
						if (!ontModel.contains(obj,RDF.type,objClass)) {
							moveIt = false;
						}
					}
					if (moveIt && subjClass != null) {
						if (!ontModel.contains(subj,RDF.type,subjClass)) {
							moveIt = false;
						}
					}
					if (moveIt) {
						tempRetractModel.add(stmt);
						if (propInv != null) {
							tempRetractModel.add((Resource)stmt.getObject(),propInv,stmt.getSubject());
						}
						if (newProp != null) {
							tempAddModel.add(stmt.getSubject(),newProp,stmt.getObject());
							if (newPropInv != null) {
								tempAddModel.add((Resource)stmt.getObject(),newPropInv,stmt.getSubject());
							}
						}
					}
				}
			} finally {
				closeIt.close();
			}
		} finally {
			ontModel.leaveCriticalSection();
		}
		ontModel.enterCriticalSection(Lock.WRITE);
		ontModel.getBaseModel().notifyEvent(new EditEvent(userURI,true));
		try {
			ontModel.remove(tempRetractModel);
			ontModel.add(tempAddModel);
		} finally {
			ontModel.getBaseModel().notifyEvent(new EditEvent(userURI,false));
			ontModel.leaveCriticalSection();
		}

	}
	
	private void doMoveInstances(VitroRequest request, HttpServletResponse response, EditProcessObject epo) {
		String userURI = LoginStatusBean.getBean(request).getUserURI();
		
		OntModel ontModel = (OntModel) getServletContext().getAttribute("baseOntModel");
		if (ontModel==null) {
			ontModel = (OntModel) getServletContext().getAttribute("jenaOntModel");
		}
		
		String oldClassURIStr = (String) epo.getAttribute("VClassURI");
		String newClassURIStr = (String) request.getParameter("NewVClassURI");
		
		Model tempRetractModel = ModelFactory.createDefaultModel();
		Model tempAddModel = ModelFactory.createDefaultModel();
		
		ontModel.enterCriticalSection(Lock.READ);
		try {
			Resource oldClassRes = ontModel.getResource(oldClassURIStr);
			Resource newClassRes = (newClassURIStr.equals("")) ? null : ontModel.getResource(newClassURIStr);
			ClosableIterator closeIt = ontModel.listStatements(null, RDF.type, oldClassRes);
			try {
				for (Iterator stmtIt = closeIt; stmtIt.hasNext();) {
					Statement stmt = (Statement) stmtIt.next();
					tempRetractModel.add(stmt);
					if (newClassRes != null) {
						tempAddModel.add(stmt.getSubject(),stmt.getPredicate(),newClassRes);
					}
				}
			} finally {
				closeIt.close();
			}
		} finally {
			ontModel.leaveCriticalSection();
		}
		
		ontModel.enterCriticalSection(Lock.WRITE);
		ontModel.getBaseModel().notifyEvent(new EditEvent(userURI,true));
		try {
			ontModel.remove(tempRetractModel);
			ontModel.add(tempAddModel);
		} finally {
			ontModel.getBaseModel().notifyEvent(new EditEvent(userURI,false));
			ontModel.leaveCriticalSection();
		}
		
	}
	
    public void doPost(HttpServletRequest req, HttpServletResponse response) {
        if (!isAuthorizedToDisplayPage(req, response, new Actions(new EditOntology()))) {
        	return;
        }

    	VitroRequest vreq = new VitroRequest(req);
    	String defaultLandingPage = getDefaultLandingPage(vreq);
    	
        HashMap epoHash = null;
        EditProcessObject epo = null;
        try {
            epoHash = (HashMap) vreq.getSession().getAttribute("epoHash");
            epo = (EditProcessObject) epoHash.get(vreq.getParameter("_epoKey"));
        } catch (NullPointerException e) {
            //session or edit process expired
            try {
                response.sendRedirect(defaultLandingPage);
            } catch (IOException f) {
                log.error(f, f);
                throw new RuntimeException(f);
            }
            return;
        }
        
        String modeStr;
        if (epo == null) 
        {
        	// Handles the case where we want to a type check on objects of datatype properties
        	handleConsistencyCheckRequest(vreq, response);
        	return;
        }
        else modeStr = (String)epo.getAttribute("modeStr");
        
        String redirectStr = null;
        
        if (vreq.getParameter("_cancel") == null) {
	        if (modeStr != null) {
	        	
	        	if (modeStr.equals("renameResource")) {
	        		redirectStr = doRenameResource(vreq, response, epo);
	        	} else if (modeStr.equals("movePropertyStatements")) {
	        		doMovePropertyStatements(vreq, response, epo);
	        	} else if (modeStr.equals("moveInstances")) {
	        		doMoveInstances(vreq, response, epo);
	        	} 
	        }
        }
        
        if (!"STOP".equals(redirectStr)) {
	        if (redirectStr == null) {
	        	redirectStr = (epo.getReferer()==null) ? defaultLandingPage : epo.getReferer();
	        }
	        try {
	            response.sendRedirect(redirectStr);
	        } catch (IOException e) {
                log.error(e, e);
                throw new RuntimeException(e);
	        }
        }

    }
    
    
    private void handleConsistencyCheckRequest(HttpServletRequest req, HttpServletResponse response)
    {
    	String modeStr = req.getParameter("modeStr");
    	if(modeStr != null)
    		if (modeStr.equals("fixDataTypes")) doFixDataTypes(req,response);
    }
    
    public void doGet(HttpServletRequest req, HttpServletResponse response)
    {
    	doPost(req, response);
    }
        
}
        

