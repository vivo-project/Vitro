/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller.jena;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.FULL_ASSERTIONS;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.FULL_INFERENCES;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.iri.IRI;
import org.apache.jena.iri.IRIFactory;

import org.apache.jena.ontology.AllValuesFromRestriction;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.Restriction;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.shared.Lock;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.modules.tboxreasoner.TBoxReasonerModule;

@WebServlet(name = "JenaAdminServlet", urlPatterns = {"/jenaAdmin"} )
public class JenaAdminActions extends BaseEditController {

	private static final Log log = LogFactory.getLog(JenaAdminActions.class.getName());

    private boolean checkURI( String uri ) {
    	IRIFactory factory = IRIFactory.jenaImplementation();
        IRI iri = factory.create( uri );
        if (iri.hasViolation(false) ) {
        	log.error("Bad URI: "+uri);
        	log.error( "Only well-formed absolute URIrefs can be included in RDF/XML output: "
                 + iri.violations(false).next().getShortMessage());
        	return true;
        } else {
        	return false;
        }
    }

	private static final String VITRO = "http://vitro.mannlib.cornell.edu/ns/vitro/0.7#";
    private static final String AKT_SUPPORT = "http://www.aktors.org/ontology/support#";
    private static final String AKT_PORTAL = "http://www.aktors.org/ontology/portal#";

    private void copyStatements(Model src, Model dest, Resource subj, Property pred, RDFNode obj) {
    	for (Statement stmt : src.listStatements(subj,pred,obj).toList()) {
    		String subjNs = stmt.getSubject().getNameSpace();
    		if (subjNs == null || (! (subjNs.equals(VITRO) || subjNs.equals(AKT_SUPPORT) || subjNs.equals(AKT_PORTAL) ) ) ) {
    			if (stmt.getObject().isLiteral()) {
    				dest.add(stmt);
    			} else if (stmt.getObject().isResource()) {
    				String objNs = ((Resource)stmt.getObject()).getNameSpace();
    				if (objNs == null || (! (objNs.equals(VITRO) || objNs.equals(AKT_SUPPORT) || objNs.equals(AKT_PORTAL) ) ) ) {
    					dest.add(stmt);
    				}
    			}
    		}
    	}
    }


    /**
     * This doesn't really print just the TBox.  It takes a copy of the model, removes all the individuals, and writes the result.
     */
    private void outputTbox(HttpServletResponse response) {
        OntModel memoryModel = ModelAccess.on(getServletContext()).getOntModel(FULL_ASSERTIONS);
        try {
        	OntModel tempOntModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        	memoryModel.enterCriticalSection(Lock.READ);
        	try {
        		copyStatements(memoryModel,tempOntModel,null,RDF.type,OWL.Class);
        		copyStatements(memoryModel,tempOntModel,null,RDF.type,OWL.ObjectProperty);
        		copyStatements(memoryModel,tempOntModel,null,RDF.type,OWL.DatatypeProperty);
        		copyStatements(memoryModel,tempOntModel,null,RDF.type,OWL.AnnotationProperty);
        		copyStatements(memoryModel,tempOntModel,null,RDFS.subClassOf,null);
        		copyStatements(memoryModel,tempOntModel,null,RDFS.subPropertyOf,null);
        		copyStatements(memoryModel,tempOntModel,null,RDFS.domain,null);
        		copyStatements(memoryModel,tempOntModel,null,RDFS.range,null);
        		copyStatements(memoryModel,tempOntModel,null,OWL.inverseOf,null);
        	} finally {
        		memoryModel.leaveCriticalSection();
        	}
            response.setContentType("application/rdf+xml");
            OutputStream out = response.getOutputStream();
            tempOntModel.write(out);
            out.flush();
            out.close();
            tempOntModel = null; // Hit it, GC
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Model extractTaxonomy(OntModel ontModel) {
    	ontModel.enterCriticalSection(Lock.READ);
    	Model taxonomyModel = ModelFactory.createDefaultModel();
    	try {
    		HashSet<Resource> typeSet = new HashSet<Resource>();
    		for (Statement stmt : ontModel.listStatements((Resource)null,RDF.type,(RDFNode)null).toList()) {
    			if (stmt.getObject().isResource()) {
    				typeSet.add((Resource) stmt.getObject());
    			}
    		}
			typeSet.addAll(ontModel.listClasses().toList());
    		for (Resource ontClass : typeSet) {
    			if (!ontClass.isAnon()) { // Only query for named classes
    				System.out.println("Describing "+ontClass.getURI());
    				// We want a subgraph describing this class, including related BNodes
    				String queryStr = "DESCRIBE <"+ontClass.getURI()+">";
    				Query describeQuery = QueryFactory.create(queryStr);
    				QueryExecution qe = QueryExecutionFactory.create(describeQuery,ontModel);
    				qe.execDescribe(taxonomyModel);
    			}
			}
    	} finally {
    		ontModel.leaveCriticalSection();
    	}
    	System.out.println("Cleaning out the vitro properties");
    	Model cleanModel = ModelFactory.createDefaultModel();
    	StmtIterator stmtIt = taxonomyModel.listStatements();
    	while (stmtIt.hasNext()) {
    		Statement stmt = stmtIt.nextStatement();
    		if ( !(stmt.getPredicate().getURI().indexOf(VitroVocabulary.vitroURI)==0) ) {
    			// if it's not a vitro internal property, copy it over
    			cleanModel.add(stmt);
    		}
    	}
    	return cleanModel;
    }

	private String testWriteXML() {
		StringBuilder output = new StringBuilder();
		output.append("<html><head><title>Test Write XML</title></head><body><pre>\n");
		OntModel model = ModelAccess.on(getServletContext()).getOntModel();
		Model tmp = ModelFactory.createDefaultModel();
		boolean valid = true;
		for (Statement stmt : model.listStatements().toList() ) {
			tmp.add(stmt);
				StringWriter writer = new StringWriter();
				try {
					tmp.write(writer, "RDF/XML");
				} catch (Exception e) {
					valid = false;
					output.append("-----\n");
					output.append("Unable to write statement as RDF/XML:\n");
					output.append("Subject : \n").append(stmt.getSubject().getURI());
					output.append("Subject : \n").append(stmt.getPredicate().getURI());
					String objectStr = (stmt.getObject().isLiteral()) ? ((Literal)stmt.getObject()).getLexicalForm() : ((Resource)stmt.getObject()).getURI();
					output.append("Subject : \n").append(objectStr);
					output.append("Exception: \n");
					e.printStackTrace();
				}
			tmp.removeAll();
		}
		if (valid) {
			output.append("All statements were able to be written as RDF/XML\n");
		}
		output.append("</body></html>");
		return output.toString();
	}

    private void printRestrictions() {
    	TBoxReasonerModule reasoner = ApplicationUtils.instance().getTBoxReasonerModule();
    	for (Restriction rest : reasoner.listRestrictions() ) {
    		if (rest.isAllValuesFromRestriction()) {
    			log.trace("All values from: ");
    			AllValuesFromRestriction avfr = rest.asAllValuesFromRestriction();
    			Resource res = avfr.getAllValuesFrom();
    			if (res.canAs(OntClass.class)) {
    				OntClass resClass = res.as(OntClass.class);
    				for (Resource inst : resClass.listInstances().toList() ) {
    					log.trace("    -"+inst.getURI());
    				}
    			}
    		} else if (rest.isSomeValuesFromRestriction()) {
    			log.trace("Some values from: ");
    		} else if (rest.isHasValueRestriction()) {
    			log.trace("Has value: ");
    		}
    		log.trace("On property "+rest.getOnProperty().getURI());
    		for (Resource inst : rest.listInstances().toList() ) {
    			log.trace("     "+inst.getURI());
    		}

    	}
    }

    private void removeLongLiterals() {
		OntModel memoryModel = ModelAccess.on(getServletContext()).getOntModel();
    	memoryModel.enterCriticalSection(Lock.WRITE);
    	try {
    		List<Statement> statementsToRemove = new LinkedList<Statement>();
    		for (Statement stmt :  memoryModel.listStatements(null,null,(Literal)null).toList() ) {
    			if (stmt.getObject().isLiteral()) {
    				Literal lit = (Literal) stmt.getObject();
    				if ( lit.getString().length() > 24) {
    					statementsToRemove.add(stmt);
    				}
    			}
    		}
			for (Statement stmt : statementsToRemove) {
				memoryModel.remove(stmt);
			}
    	} finally {
    		memoryModel.leaveCriticalSection();
    	}
    }

    @Override
	public void doGet(HttpServletRequest req, HttpServletResponse response) {
        if (!isAuthorizedToDisplayPage(req, response, SimplePermission.USE_MISCELLANEOUS_ADMIN_PAGES.ACTION)) {
        	return;
        }

        VitroRequest request = new VitroRequest(req);
        String actionStr = request.getParameter("action");

		switch (actionStr) {
			case "printRestrictions":
				printRestrictions();
				break;
			case "outputTbox":
				outputTbox(response);
				break;
			case "testWriteXML":
				try {
					response.getWriter().write(testWriteXML());
				} catch (IOException ioe) {
					throw new RuntimeException(ioe);
				}
				break;
		}

        if (actionStr.equals("checkURIs")) {
    		OntModel memoryModel = ModelAccess.on(getServletContext()).getOntModel();
        	StmtIterator stmtIt = memoryModel.listStatements();
        	try {
        		for (Statement stmt : stmtIt.toList() ) {
	        		boolean sFailed = false;
	        		boolean pFailed = false;
	        		boolean oFailed = false;
	        		String sURI = "<bNode>";
	        		String pURI = "???";
	        		String oURI = "<bNode>";
	        		if (stmt.getSubject().getURI() != null) {
	        			sFailed = checkURI(sURI = stmt.getSubject().getURI());
	        		}
	        		if (stmt.getPredicate().getURI() != null) {
	        			pFailed = checkURI(pURI = stmt.getPredicate().getURI());
	        		}
	        		if (stmt.getObject().isResource() && ((Resource)stmt.getObject()).getURI() != null) {
	        			oFailed = checkURI(oURI = ((Resource)stmt.getObject()).getURI());
	        		}
	        		if (sFailed || pFailed || oFailed) {
	        			log.debug(sURI+" | "+pURI+" | "+oURI);
	        		}
	        	}
        	} finally {
        		stmtIt.close();
        	}
        }

        if (actionStr.equals("output")) {
            OntModel memoryModel = null;
	    if (request.getParameter("assertionsOnly") != null) {
	        memoryModel = ModelAccess.on(getServletContext()).getOntModel(FULL_ASSERTIONS);
	    	System.out.println("baseOntModel");
	    } else if (request.getParameter("inferences") != null) {
	    	memoryModel = ModelAccess.on(getServletContext()).getOntModel(FULL_INFERENCES);
	    	System.out.println("inferenceOntModel");
	    } else {
			memoryModel = ModelAccess.on(getServletContext()).getOntModel();
	    	System.out.println("jenaOntModel");
	    }
	    int subModelCount = memoryModel.listSubModels().toList().size();
	    System.out.println("Submodels: "+subModelCount);
	        try {
	            //response.setContentType("application/rdf+xml");
	        	response.setContentType("application/x-turtle");
	            OutputStream out = response.getOutputStream();
	            memoryModel.write(out, "TTL");
	            out.flush();
	            out.close();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
        }

        if (actionStr.equals("removeLongLiterals")) {
        	removeLongLiterals();
        }

        if (actionStr.equals("outputTaxonomy")) {
            OntModel ontModel = ModelAccess.on(getServletContext()).getOntModel(FULL_ASSERTIONS);
        	Model taxonomyModel = extractTaxonomy(ontModel);
        	try {
        		taxonomyModel.write(response.getOutputStream());
        	} catch (Exception e) {
        		log.error(e, e);
        	}
        }
    }


    @Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
        doGet(request ,response);
    }

}
