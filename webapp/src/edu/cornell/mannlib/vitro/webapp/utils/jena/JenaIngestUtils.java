package edu.cornell.mannlib.vitro.webapp.utils.jena;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.ResourceUtils;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

public class JenaIngestUtils {
    
    private static final Log log = LogFactory.getLog(JenaIngestUtils.class.getName());

	private Random random = new Random(System.currentTimeMillis());

	/**
	 * Returns a new copy of the input model with blank nodes renamed with namespaceEtc plus a random int. 
	 * @param namespaceEtc
	 * @return
	 */
	public Model renameBNodes(Model inModel, String namespaceEtc) {	
		return renameBNodes(inModel, namespaceEtc, null);
	}
		
	/**
	 * Returns a new copy of the input model with blank nodes renamed with namespaceEtc plus a random int.
	 * Will prevent URI collisions with supplied dedupModel 
	 * @param namespaceEtc
	 * @return
	 */
	public Model renameBNodes(Model inModel, String namespaceEtc, Model dedupModel) {
		Model outModel = ModelFactory.createDefaultModel();
		OntModel dedupUnionModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); // we're not using OWL here, just the OntModel submodel infrastructure
		dedupUnionModel.addSubModel(outModel);
		if (dedupModel != null) {
			dedupUnionModel.addSubModel(dedupModel);
		}
		// the dedupUnionModel is so we can guard against reusing a URI in an 
		// existing model, as well as in the course of running this process
		inModel.enterCriticalSection(Lock.READ);
		Set<String> doneSet = new HashSet<String>();
		try {
			outModel.add(inModel);
			ClosableIterator closeIt = inModel.listSubjects();
			try {
				for (Iterator it = closeIt; it.hasNext();) {
					Resource res = (Resource) it.next();
					if (res.isAnon() && !(doneSet.contains(res.getId()))) {
						// now we do something hacky to get the same resource in the outModel, since there's no getResourceById();
						ClosableIterator closfIt = outModel.listStatements(res,(Property)null,(RDFNode)null);
						Statement stmt = null;
						try {
							if (closfIt.hasNext()) {
								stmt = (Statement) closfIt.next();
							}
						} finally {
							closfIt.close();
						}
						if (stmt != null) {
							Resource outRes = stmt.getSubject();
							ResourceUtils.renameResource(outRes,getNextURI(namespaceEtc,dedupUnionModel));
							doneSet.add(res.getId().toString());
						}
					}
				}
			} finally {
				closeIt.close();
			}
			closeIt = inModel.listObjects();
			try {
				for (Iterator it = closeIt; it.hasNext();) {
					RDFNode rdfn = (RDFNode) it.next();
					if (rdfn.isResource()) {
						Resource res = (Resource) rdfn;
						if (res.isAnon() && !(doneSet.contains(res.getId()))) {
							// now we do something hacky to get the same resource in the outModel, since there's no getResourceById();
							ClosableIterator closfIt = outModel.listStatements((Resource)null,(Property)null,res);
							Statement stmt = null;
							try {
								if (closfIt.hasNext()) {
									stmt = (Statement) closfIt.next();
								}
							} finally {
								closfIt.close();
							}
							if (stmt != null) {
								Resource outRes = stmt.getSubject();
								ResourceUtils.renameResource(outRes,namespaceEtc+random.nextInt());
								doneSet.add(res.getId().toString());
							}
						}
					}
				}
			} finally {
				closeIt.close();
			}
		} finally {
			inModel.leaveCriticalSection();
		}
		return outModel;
	}
	
	private String getNextURI(String namespaceEtc, Model model) {
		String nextURI = null;
		boolean duplicate = true;
		while (duplicate) {
			nextURI = namespaceEtc+random.nextInt();
			Resource res = ResourceFactory.createResource(nextURI);
			duplicate = false;
			ClosableIterator closeIt = model.listStatements(res, (Property)null, (RDFNode)null);
			try {
				if (closeIt.hasNext()) {
					duplicate = true;
				}
			} finally {
				closeIt.close();
			}
			if (duplicate == false) {
				closeIt = model.listStatements((Resource)null, (Property)null, res);
				try {
					if (closeIt.hasNext()) {
						duplicate = true;
					}
				} finally {
					closeIt.close();
				}
			}
		}
		return nextURI;
	}
	
	public void processPropertyValueStrings(Model source, Model destination, Model additions, Model retractions, 
			String processorClass, String processorMethod, String originalPropertyURI, String newPropertyURI) {
		Model additionsModel = ModelFactory.createDefaultModel();
		Model retractionsModel = ModelFactory.createDefaultModel();
		Class stringProcessorClass = null;
		Object processor = null;
		Class[] methArgs = {String.class};
		Method meth = null;
		try {
			stringProcessorClass = Class.forName(processorClass);
			processor = stringProcessorClass.newInstance();
			meth = stringProcessorClass.getMethod(processorMethod,methArgs);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		Property prop = ResourceFactory.createProperty(originalPropertyURI);
		Property newProp = ResourceFactory.createProperty(newPropertyURI);
		source.enterCriticalSection(Lock.READ);
		try {
			ClosableIterator closeIt = source.listStatements((Resource)null,prop,(RDFNode)null);
			for (Iterator stmtIt = closeIt; stmtIt.hasNext(); ) {
				Statement stmt = (Statement) stmtIt.next();
				if (stmt.getObject().isLiteral()) {
					Literal lit = (Literal) stmt.getObject();
					String lex = lit.getLexicalForm();
					Object[] args = {lex};
					String newLex = null;
					try {
					    if (log.isDebugEnabled()) { 
					        log.debug("invoking string processor method on ["+lex.substring(0,lex.length()>50 ? 50 : lex.length())+"...");
					    }
						newLex = (String) meth.invoke(processor,args);
					} catch (Exception e) {
						e.printStackTrace();
						return;
					}
					if (!newLex.equals(lex)) {
						retractionsModel.add(stmt);	
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
			if (destination != null) {
				destination.enterCriticalSection(Lock.WRITE);
				try {
					destination.add(additionsModel);
					destination.remove(retractionsModel);
				} finally {
					destination.leaveCriticalSection();
				}
			}
			if (additions != null)  {
				additions.enterCriticalSection(Lock.WRITE);
				try {
					additions.add(additionsModel);
				} finally {
					additions.leaveCriticalSection();
				}
			}
			if (retractions != null) {
				retractions.enterCriticalSection(Lock.WRITE);
				try {
					retractions.add(retractionsModel);
				} finally {
					retractions.leaveCriticalSection();
				}
			}
		} finally {
			source.leaveCriticalSection();
		} 
	}
	
	/**
	 * Splits values for a given data property URI on a supplied regex and 
	 * asserts each value using newPropertyURI.  New statements returned in
	 * a Jena Model.  Split values may be optionally trim()ed.
	 * @param inModel
	 * @param propertyURI
	 * @param splitRegex
	 * @param newPropertyURI
	 * @param trim
	 * @return outModel
	 */
	public Model splitPropertyValues(Model inModel, String propertyURI, String splitRegex, String newPropertyURI, boolean trim) {
		Model outModel = ModelFactory.createDefaultModel();
		Pattern delimiterPattern = Pattern.compile(splitRegex);
		Property theProp = ResourceFactory.createProperty(propertyURI);
		Property newProp = ResourceFactory.createProperty(newPropertyURI);
		inModel.enterCriticalSection(Lock.READ);
		try {
			StmtIterator stmtIt = inModel.listStatements( (Resource)null, theProp, (RDFNode)null );
			try {
				while(stmtIt.hasNext()) {
					Statement stmt = stmtIt.nextStatement();
					Resource subj = stmt.getSubject();
					RDFNode obj = stmt.getObject();
					if (obj.isLiteral()) {
						Literal lit = (Literal) obj;
						String unsplitStr = lit.getLexicalForm();
						String[] splitPieces = delimiterPattern.split(unsplitStr);
						for (int i=0; i<splitPieces.length; i++) {
							String newLexicalForm = splitPieces[i];
							if (trim) {
								newLexicalForm = newLexicalForm.trim();
							}
							if (newLexicalForm.length() > 0) {
								Literal newLiteral = null;
								if (lit.getDatatype() != null) {
									newLiteral = outModel.createTypedLiteral(newLexicalForm, lit.getDatatype());
								} else {
									if (lit.getLanguage() != null) {
										newLiteral = outModel.createLiteral(newLexicalForm, lit.getLanguage());
									} else {
										newLiteral = outModel.createLiteral(newLexicalForm);
									}
								}
								outModel.add(subj,newProp,newLiteral);
							}
						}
					}
				}
			} finally {
				stmtIt.close();
			}
		} finally {
			inModel.leaveCriticalSection();
		}	
		return outModel;
	}
	
	/**
	 * A simple resource smusher based on a supplied inverse-functional property.  
	 * A new model containing only resources about the smushed statements is returned.
	 * @param inModel
	 * @param prop
	 * @return
	 */
	public Model smushResources(Model inModel, Property prop) { 
		Model outModel = ModelFactory.createDefaultModel();
		inModel.enterCriticalSection(Lock.READ);
		try {
			ClosableIterator closeIt = inModel.listObjectsOfProperty(prop);
			try {
				for (Iterator objIt = closeIt; objIt.hasNext();) {
					RDFNode rdfn = (RDFNode) objIt.next();
					ClosableIterator closfIt = inModel.listSubjectsWithProperty(prop, rdfn);
					try {
						boolean first = true;
						Resource smushToThisResource = null;
						for (Iterator subjIt = closfIt; closfIt.hasNext();) {
							Resource subj = (Resource) subjIt.next();
							if (first) {
								smushToThisResource = subj;
								first = false;
							}
							ClosableIterator closgIt = inModel.listStatements(subj,(Property)null,(RDFNode)null);
							try {
								for (Iterator stmtIt = closgIt; stmtIt.hasNext();) {
									Statement stmt = (Statement) stmtIt.next();
									outModel.add(smushToThisResource, stmt.getPredicate(), stmt.getObject());
								}
							} finally {
								closgIt.close();
							}
						}
					} finally {
						closfIt.close();
					}
				}
			} finally {
				closeIt.close();
			}
		} finally {
			inModel.leaveCriticalSection();
		}
		return outModel;
	}
	
	/**
	 * Returns a model where redundant individuals that are sameAs one another are smushed
	 * using URIs in preferred namespaces where possible.
	 * @param model
	 * @param preferredIndividualNamespace
	 * @return
	 */
	public Model dedupAndExtract( Model model, String preferredNamespace ) {
		
		Model extractsModel = ModelFactory.createDefaultModel();
		
		HashMap<String, String> rewriteURIUsing = new HashMap<String, String>();
		
		Iterator haveSameAsIt = model.listSubjectsWithProperty(OWL.sameAs);
		while (haveSameAsIt.hasNext()) {
			String preferredURI = null;
			Resource hasSameAs = (Resource) haveSameAsIt.next();
			List<Statement> sameAsList = hasSameAs.listProperties(OWL.sameAs).toList();
			if (sameAsList.size()>1) { // if sameAs something other than the same URI (we assume reasoning model)
				List<String> sameAsURIs = new LinkedList<String>();
				Iterator sameAsStmtIt = sameAsList.iterator();
				for (int i=0; i<sameAsList.size(); i++) {
					Statement sameAsStmt = (Statement) sameAsStmtIt.next();
					if (!sameAsStmt.getObject().isResource()) {
						throw new RuntimeException( sameAsStmt.getResource().getURI() + " is sameAs() a literal!" );
					}
					Resource sameAsRes = (Resource) sameAsStmt.getObject();
					if (!sameAsRes.isAnon()) {
						sameAsURIs.add(sameAsRes.getURI());
						if (preferredNamespace != null && preferredNamespace.equals(sameAsRes.getNameSpace())) {
							preferredURI = sameAsRes.getURI();
						}
					}
					if (preferredURI == null) {
						preferredURI = sameAsURIs.get(0);
					}
					for (String s : sameAsURIs) {
						rewriteURIUsing.put(s,preferredURI);
					}
				}
			}
		}
		
		StmtIterator modelStmtIt = model.listStatements();
		while (modelStmtIt.hasNext()) {
			Statement origStmt = modelStmtIt.nextStatement();
			Resource newSubj = null;
			RDFNode newObj = null;
			if (!origStmt.getSubject().isAnon()) { 
				String rewriteURI = rewriteURIUsing.get(origStmt.getSubject().getURI());
				if (rewriteURI != null) {
					newSubj = extractsModel.getResource(rewriteURI);
				}
			}
			if (origStmt.getObject().isResource() && !origStmt.getResource().isAnon()) {
				String rewriteURI = rewriteURIUsing.get(((Resource) origStmt.getObject()).getURI());
				if (rewriteURI != null) {
					newObj = extractsModel.getResource(rewriteURI);
				}
			}
			if (newSubj == null) {
				newSubj = origStmt.getSubject();
			}
			if (newObj == null) {
				newObj = origStmt.getObject();
			}
			extractsModel.add(newSubj, origStmt.getPredicate(), newObj);
		}
		
		return extractsModel;
		
	}
	
	public OntModel generateTBox(Model abox) {
		OntModel tboxOntModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		StmtIterator sit = abox.listStatements();
		while (sit.hasNext()) {
			Statement stmt = sit.nextStatement();
			if (RDF.type.equals(stmt.getPredicate())) {
				makeClass(stmt.getObject(), tboxOntModel);
			} else if (stmt.getObject().isResource()) {
				makeObjectProperty(stmt.getPredicate(), tboxOntModel);
			} else if (stmt.getObject().isLiteral()) {
				makeDatatypeProperty(stmt.getPredicate(), tboxOntModel);
			}
 		}
		return tboxOntModel;
	}
	
	private void makeClass(RDFNode node, OntModel tboxOntModel) {
		if (!node.isResource() || node.isAnon()) {
			return;
		}
		Resource typeRes = (Resource) node;
		if (tboxOntModel.getOntClass(typeRes.getURI()) == null) {
			tboxOntModel.createClass(typeRes.getURI());
		}
	}
	
	private void makeObjectProperty(Property property, OntModel tboxOntModel) {
		if (tboxOntModel.getObjectProperty(property.getURI()) == null) {
			tboxOntModel.createObjectProperty(property.getURI());
		}
	}
	
	private void makeDatatypeProperty(Property property, OntModel tboxOntModel) {
		if (tboxOntModel.getDatatypeProperty(property.getURI()) == null) {
			tboxOntModel.createDatatypeProperty(property.getURI());
		}
	}
	
}
