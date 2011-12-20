/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.ontology.AllValuesFromRestriction;
import com.hp.hpl.jena.ontology.AnnotationProperty;
import com.hp.hpl.jena.ontology.CardinalityRestriction;
import com.hp.hpl.jena.ontology.ComplementClass;
import com.hp.hpl.jena.ontology.HasValueRestriction;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.IntersectionClass;
import com.hp.hpl.jena.ontology.MaxCardinalityRestriction;
import com.hp.hpl.jena.ontology.MinCardinalityRestriction;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.ontology.ProfileException;
import com.hp.hpl.jena.ontology.Restriction;
import com.hp.hpl.jena.ontology.SomeValuesFromRestriction;
import com.hp.hpl.jena.ontology.UnionClass;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vitro.webapp.beans.Classes2Classes;
import edu.cornell.mannlib.vitro.webapp.beans.Ontology;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.dao.InsertException;
import edu.cornell.mannlib.vitro.webapp.dao.OntologyDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.jena.event.EditEvent;
import edu.cornell.mannlib.vitro.webapp.dao.jena.pellet.PelletListener;

public class VClassDaoJena extends JenaBaseDao implements VClassDao {
	
    public VClassDaoJena(WebappDaoFactoryJena wadf) {
        super(wadf);
    }
    
    @Override
    protected OntModel getOntModel() {
    	return getOntModelSelector().getTBoxModel();
    }
    
    /* ************************************************** */
    
    public String getLabelForClass(OntClass cls,boolean withPrefix,boolean forPickList) {
    	cls.getModel().enterCriticalSection(Lock.READ);
    	try {
	    	if (cls.isAnon()) {
		    	if (cls.isRestriction()) {	    		
		    		Restriction rest = cls.asRestriction();
		    		OntProperty onProperty = rest.getOnProperty();
		    		String labelStr = "restriction on " + getLabelOrId(onProperty) + ": ";
		    		if (rest.isAllValuesFromRestriction() || rest.isSomeValuesFromRestriction()) {
			    		Resource fillerRes = null;
			    		if (rest.isAllValuesFromRestriction()) {
			    			AllValuesFromRestriction avfRest = rest.asAllValuesFromRestriction();
			    			fillerRes = avfRest.getAllValuesFrom();
			    			labelStr += "all values from ";
			    		} else {
			    			SomeValuesFromRestriction svfRest = rest.asSomeValuesFromRestriction();
			    			fillerRes = svfRest.getSomeValuesFrom();
			    			labelStr += "some values from ";
			    		}
		    			if (fillerRes.canAs(OntClass.class)) { 
		    				OntClass avf = (OntClass) fillerRes.as(OntClass.class);
		    				labelStr += getLabelForClass(avf,withPrefix,forPickList);
		    			} else {
		    				try {
		    					labelStr += getLabelOrId( (OntResource) fillerRes.as(OntResource.class));
		    				} catch (Exception e) {
		    					labelStr += "???";
		    				}
		    			}		    			
		    		} else if (rest.isHasValueRestriction()) {
		    			HasValueRestriction hvRest = rest.asHasValueRestriction();
		    			labelStr += "has value ";
		    			RDFNode fillerNode = hvRest.getHasValue();
		    			try {
			    			if (fillerNode.isResource()) {
			    				labelStr += getLabelOrId((OntResource)fillerNode.as(OntResource.class));
			    			} else {
			    				labelStr += ((Literal) fillerNode.as(Literal.class)).getLexicalForm(); 
			    			}
		    			} catch (Exception e) {
		    				labelStr += "???";
		    			}
		    		} else if (rest.isMinCardinalityRestriction()) {
		    			MinCardinalityRestriction mcRest = rest.asMinCardinalityRestriction();
		    			labelStr += "minimum cardinality ";
		    			labelStr += mcRest.getMinCardinality();
		    		} else if (rest.isMaxCardinalityRestriction()) {
		    			MaxCardinalityRestriction mcRest = rest.asMaxCardinalityRestriction();
		    			labelStr += "maximum cardinality ";
		    			labelStr += mcRest.getMaxCardinality();
		    		} else if (rest.isCardinalityRestriction()) {
		    			CardinalityRestriction cRest = rest.asCardinalityRestriction();
		    			labelStr += "cardinality ";
		    			labelStr += cRest.getCardinality();
		    		}
		    		return labelStr;
		    	} else if (isBooleanClassExpression(cls)) {
		    		String labelStr = "(";
		    		if (cls.isComplementClass()) {
		    			labelStr += "not ";
		    			ComplementClass ccls = (ComplementClass) cls.as(ComplementClass.class);
		    			labelStr += getLabelForClass(ccls.getOperand(),withPrefix,forPickList);		    			
		    		} else if (cls.isIntersectionClass()) {
		    			IntersectionClass icls = (IntersectionClass) cls.as(IntersectionClass.class);
		    			for (Iterator operandIt = icls.listOperands(); operandIt.hasNext();) {
		    				OntClass operand = (OntClass) operandIt.next();
		    				labelStr += getLabelForClass(operand,withPrefix,forPickList);
		    				if (operandIt.hasNext()) {
		    					labelStr += " and ";
		    				}
		    			}
		    		} else if (cls.isUnionClass()) {
		    			UnionClass icls = (UnionClass) cls.as(UnionClass.class);
		    			for (Iterator operandIt = icls.listOperands(); operandIt.hasNext();) {
		    				OntClass operand = (OntClass) operandIt.next();
		    				labelStr += getLabelForClass(operand,withPrefix,forPickList);
		    				if (operandIt.hasNext()) {
		    					labelStr += " or ";
		    				}
		    			}
		    		}
		    		return labelStr+")";
		    	} else {
		    		// BJL23 2009-02-19
		    		// I'm putting the link markup in because I need it,
		    		// but obviously we need to factor this out into the display layer.
		    		return "<a href=\"vclassEdit?uri="+URLEncoder.encode(getClassURIStr(cls),"UTF-8")+"\">[anonymous class]</a>";
		    	}
	    	} else {
	    	    if (withPrefix || forPickList) {
                    OntologyDao oDao=getWebappDaoFactory().getOntologyDao();
                    Ontology o = (Ontology)oDao.getOntologyByURI(cls.getNameSpace());
                    if (o!=null) {
                        if (withPrefix) {                        	
                            return(o.getPrefix()==null?(o.getName()==null?"unspec:"+getLabelOrId(cls):o.getName()+":"+getLabelOrId(cls)):o.getPrefix()+":"+getLabelOrId(cls));
                        } else {
                            return(getLabelOrId(cls)+(o.getPrefix()==null?(o.getName()==null?" (unspec)":" ("+o.getName()+")"):" ("+o.getPrefix()+")"));                            
                        }
                    } else {
                    	return getLabelOrId(cls);
                    }
	    	    }
	    		return getLabelOrId(cls);
	    	}
    	} catch (Exception e) {
    		return "???";
    	} finally {
    		cls.getModel().leaveCriticalSection();
    	}
    }

    public void deleteVClass(VClass cls) {
    	deleteVClass(cls,getOntModel());
    }

    public void deleteVClass(String URI) {
    	deleteVClass(URI,getOntModel());
    }

    public void deleteVClass(String URI, OntModel ontModel) {
    	ontModel.enterCriticalSection(Lock.WRITE);
    	getOntModel().getBaseModel().notifyEvent(new EditEvent(getWebappDaoFactory().getUserURI(),true));
    	try {
    		OntClass cls = getOntClass(ontModel,URI);
    		if (cls != null) {
    			//Remove restriction class.
            	Iterator<Resource> restIt = ontModel.listSubjectsWithProperty(OWL.allValuesFrom, cls);
            	while(restIt.hasNext()) {
            		Resource restRes = restIt.next();
            		if (restRes.canAs(OntResource.class)) {
            			OntResource restOntRes = (OntResource) restRes.as(OntResource.class);
            			smartRemove(restOntRes, ontModel);
            		}
            	}
            	restIt = ontModel.listSubjectsWithProperty(OWL.someValuesFrom, cls);
            	while(restIt.hasNext()) {
            		Resource restRes = restIt.next();
            		if (restRes.canAs(OntResource.class)) {
            			OntResource restOntRes = (OntResource) restRes.as(OntResource.class);
            			smartRemove(restOntRes, ontModel);
            		}
            	}
    			removeRulesMentioningResource(cls, ontModel);
    			smartRemove(cls, ontModel);
    		}
    	} finally {
    		getOntModel().getBaseModel().notifyEvent(new EditEvent(getWebappDaoFactory().getUserURI(),false));
    		ontModel.leaveCriticalSection();
    	}
    }

    public void deleteVClass(VClass cls, OntModel ontModel) {
        deleteVClass(cls.getURI(), ontModel);
    }

    public List<String> getDisjointWithClassURIs(String classURI) {
    	OntClass ontClass = getOntClass(getOntModel(), classURI);
    	List<String> uriList = new ArrayList<String>();
    	getOntModel().enterCriticalSection(Lock.READ);
    	try {
	    	for (Iterator i = ontClass.listDisjointWith(); i.hasNext(); ) {
	    		OntClass disjointClass = (OntClass) i.next();
	    		uriList.add(getClassURIStr(disjointClass));
	    	}
    	} catch (ProfileException pe) {
    		// Current language profile does not support disjointWith axioms.
    		// We'd prefer to return an empty list instead of throwing an exception.
    	} finally {
    		getOntModel().leaveCriticalSection();
    	}
    	return uriList;
    }
    
    public void addDisjointWithClass(String classURI, String disjointClassURI) {
    	getOntModel().enterCriticalSection(Lock.WRITE);
    	getOntModel().getBaseModel().notifyEvent(new EditEvent(getWebappDaoFactory().getUserURI(),true));
    	try {
    		OntClass ontClass = getOntClass(getOntModel(),classURI);
    		OntClass disjointClass = getOntClass(getOntModel(),disjointClassURI);
    		ontClass.addDisjointWith(disjointClass);
    	} finally {
    		getOntModel().getBaseModel().notifyEvent(new EditEvent(getWebappDaoFactory().getUserURI(),false));
    		getOntModel().leaveCriticalSection();
    	}
    }
    
    public void removeDisjointWithClass(String classURI, String disjointClassURI) {
    	getOntModel().enterCriticalSection(Lock.WRITE);
    	getOntModel().getBaseModel().notifyEvent(new EditEvent(getWebappDaoFactory().getUserURI(),true));
    	try {
    		OntClass ontClass = getOntClass(getOntModel(),classURI);
    		OntClass disjointClass = getOntClass(getOntModel(),disjointClassURI);
    		ontClass.removeDisjointWith(disjointClass);
    	} finally {
    		getOntModel().getBaseModel().notifyEvent(new EditEvent(getWebappDaoFactory().getUserURI(),false));
    		getOntModel().leaveCriticalSection();
    	}
    }
    
    public List<String> getEquivalentClassURIs(String classURI) {
    	List<String> equivalentClassURIs = new ArrayList<String>();
    	getOntModel().enterCriticalSection(Lock.READ);
    	try {
	    	OntClass ontClass = getOntClass(getOntModel(), classURI);
	    	ClosableIterator equivalentOntClassIt = ontClass.listEquivalentClasses();
	    	try {
	    		for (Iterator eqOntClassIt = equivalentOntClassIt; eqOntClassIt.hasNext(); ) {
	    			OntClass eqClass = (OntClass) eqOntClassIt.next();
	    			equivalentClassURIs.add(getClassURIStr(eqClass));
	    		}
	    	} finally {
	    		equivalentOntClassIt.close();
	    	}
    	} catch (ProfileException pe) {
    		// Current language profile does not support equivalent classes.
    		// We'd prefer to return an empty list instead of throwing an exception
    	} catch (Exception e) {
            // we'll try this again using a different method that 
    	    // doesn't try to convert to OntClass
            List<Resource> supList = this.listDirectObjectPropertyValues(
                    getOntModel().getResource(classURI), OWL.equivalentClass);
            for (Resource res : supList) {
                equivalentClassURIs.add(getClassURIStr(res));
            }  
    	} finally {
    		getOntModel().leaveCriticalSection();
    	}
    	return equivalentClassURIs;
    }
    
    public void addSuperclass(VClass vclass, VClass superclass) {
    	addSuperclass(vclass.getURI(),superclass.getURI());
    }
    
    public void addSuperclass(String vclassURI, String superclassURI) {
    	Classes2Classes c2c = new Classes2Classes();
    	c2c.setSubclassURI(vclassURI);
    	c2c.setSuperclassURI(superclassURI);
    	getWebappDaoFactory().getClasses2ClassesDao().insertNewClasses2Classes(c2c);
    }
    
    public void removeSuperclass(VClass vclass, VClass superclass) {
    	removeSuperclass(vclass.getURI(),superclass.getURI());
    }
    
    public void removeSuperclass(String vclassURI, String superclassURI) {
    	Classes2Classes c2c = new Classes2Classes();
    	c2c.setSubclassURI(vclassURI);
    	c2c.setSuperclassURI(superclassURI);
    	getWebappDaoFactory().getClasses2ClassesDao().deleteClasses2Classes(c2c);
    }
    
    public void addSubclass(VClass vclass, VClass subclass) {
    	addSubclass(vclass.getURI(),subclass.getURI());
    }
    
    public void addSubclass(String vclassURI, String subclassURI) {
    	Classes2Classes c2c = new Classes2Classes();
    	c2c.setSubclassURI(subclassURI);
    	c2c.setSuperclassURI(vclassURI);
    	getWebappDaoFactory().getClasses2ClassesDao().insertNewClasses2Classes(c2c);
    }
    
    public void removeSubclass(VClass vclass, VClass subclass) {
    	removeSubclass(vclass.getURI(),subclass.getURI());
    }
    
    public void removeSubclass(String vclassURI, String subclassURI) {
    	Classes2Classes c2c = new Classes2Classes();
    	c2c.setSubclassURI(subclassURI);
    	c2c.setSuperclassURI(vclassURI);
    	getWebappDaoFactory().getClasses2ClassesDao().deleteClasses2Classes(c2c);
    }
    
    public void addEquivalentClass(String classURI, String equivalentClassURI) {
    	getOntModel().enterCriticalSection(Lock.WRITE);
    	getOntModel().getBaseModel().notifyEvent(new EditEvent(getWebappDaoFactory().getUserURI(),true));
    	try {
    		OntClass ontClass = getOntClass(getOntModel(),classURI);
    		OntClass eqClass = getOntClass(getOntModel(),equivalentClassURI);
    		ontClass.addEquivalentClass(eqClass);
    		eqClass.addEquivalentClass(ontClass);
    	} finally {
    		getOntModel().getBaseModel().notifyEvent(new EditEvent(getWebappDaoFactory().getUserURI(),false));
    		getOntModel().leaveCriticalSection();
    	}
    }
    
    public void removeEquivalentClass(String classURI, String equivalentClassURI) {
    	getOntModel().enterCriticalSection(Lock.WRITE);
    	getOntModel().getBaseModel().notifyEvent(new EditEvent(getWebappDaoFactory().getUserURI(),true));
    	try {
    		OntClass ontClass = getOntClass(getOntModel(),classURI);
    		OntClass eqClass = getOntClass(getOntModel(),equivalentClassURI);
    		ontClass.removeEquivalentClass(eqClass);
    		eqClass.removeEquivalentClass(ontClass);
    		if (ontClass.isAnon()) {
    			smartRemove(ontClass, getOntModel());
    		}
    		if (eqClass.isAnon()) {
    			smartRemove(eqClass, getOntModel());
    		}
    	} finally {
    		getOntModel().getBaseModel().notifyEvent(new EditEvent(getWebappDaoFactory().getUserURI(),false));
    		getOntModel().leaveCriticalSection();
    	}
    }
    
    public List<String> getAllSubClassURIs(String classURI) {
    	HashSet<String> nodeSet = new HashSet<String>();
    	nodeSet.add(classURI);
    	getAllSubClassURIs(classURI, nodeSet);
    	nodeSet.remove(classURI);
    	List<String> outputList = new ArrayList<String>();
    	outputList.addAll(nodeSet);
    	return outputList;
    }
    
    public void getAllSubClassURIs(String classURI, HashSet<String> subtree){
        List<String> directSubclasses = getSubClassURIs(classURI);     
        Iterator<String> it=directSubclasses.iterator();
        while(it.hasNext()){
            String uri = (String)it.next();
            if (!subtree.contains(uri)) {
            	subtree.add(uri);
            	getAllSubClassURIs(uri,subtree);
            }
        }
    }

    public List<String> getAllSuperClassURIs(String classURI) {
    	
		List<String> superclassURIs = null;
		
		//String infersTypes = getWebappDaoFactory().getProperties().get("infersTypes");
		//if ("true".equalsIgnoreCase(infersTypes)) {
		
		PelletListener pl = getWebappDaoFactory().getPelletListener();
		if (pl != null && pl.isConsistent() && !pl.isInErrorState() && !pl.isReasoning()) {	
			superclassURIs = new ArrayList<String>();
			OntClass cls = getOntClass(getOntModel(),classURI);
			StmtIterator superClassIt = getOntModel().listStatements(cls,RDFS.subClassOf,(RDFNode)null);
			while (superClassIt.hasNext()) {
				Statement stmt = superClassIt.nextStatement();
				if (stmt.getObject().canAs(OntResource.class)) {
					OntResource superRes = (OntResource) stmt.getObject().as(OntResource.class);
					String test = getClassURIStr(superRes);
					superclassURIs.add(test);
				}
			}
			return superclassURIs;
		} else {
	    	HashSet<String> nodeSet = new HashSet<String>();
	    	nodeSet.add(classURI);
	    	getAllSuperClassURIs(classURI, nodeSet);
	    	//nodeSet.remove(classURI);
	    	return new ArrayList<String>(nodeSet);
		}
    }
    
    public void getAllSuperClassURIs(String classURI, HashSet<String> subtree){
        List<String> directSuperclasses = getSuperClassURIs(classURI, true);     
        Iterator<String> it=directSuperclasses.iterator();
        while(it.hasNext()){
            String uri = (String)it.next();
            if (!subtree.contains(uri)) {
            	subtree.add(uri);
            	getAllSuperClassURIs(uri,subtree);
            }
        }
    }

    public List <VClass> getAllVclasses() {
        List<VClass> classes = new ArrayList<VClass>();
        getOntModel().enterCriticalSection(Lock.READ);
        try {
            ClosableIterator<OntClass> classIt = getOntModel().listClasses();
            try {
                while (classIt.hasNext()) {
                    try {
                        OntClass cls = classIt.next();
                        if (!cls.isAnon() && !(NONUSER_NAMESPACES.contains(cls.getNameSpace()))) {
                            classes.add(new VClassJena(cls,getWebappDaoFactory()));
                        }
                    } catch (ClassCastException cce) {
                        log.error(cce, cce);
                    }
                }
            } finally {
                classIt.close();
            }
        } finally {
            getOntModel().leaveCriticalSection();
        }
        Collections.sort(classes);
        return classes;
    }
    
    private Iterator<OntClass> smarterListHierarchyRootClasses(OntModel ontModel) {
    	return smarterListHierarchyRootClasses(ontModel, null);
    }
    
    /** 
     * The basic idea here is that we ignore anonymous superclasses for the purpose 
     * of determining whether something is a root class.
     * We also avoid ClassCastExceptions deep in Jena-land by eschewing Jena's 
     * listSuperClasses() method.
     * @author bjl23
	 */
    private Iterator<OntClass> smarterListHierarchyRootClasses(OntModel ontModel, String ontologyURI) {
    	List<OntClass> rootClassList = new ArrayList<OntClass>();
    	ClosableIterator ci = ontModel.listClasses();
    	try {
	    	for (ClosableIterator i = ci ; i.hasNext(); ) {
	    		try {
		    		OntClass ontClass = (OntClass) i.next();
	    			boolean isRoot = true;
	    			for (Iterator<RDFNode> j = ontClass.listPropertyValues(RDFS.subClassOf); j.hasNext(); ) {
	    				Resource res = (Resource) j.next();
	    				if (res.canAs(OntClass.class)) {
		    				OntClass superClass = (OntClass) res.as(OntClass.class);
		    				if (!superClass.isAnon() && 
		    					((ontologyURI==null) || (ontologyURI.equals(superClass.getNameSpace()))) &&
		    					!OWL.Thing.equals(superClass) && 
		    					!superClass.equals(ontClass) && 
		    					!( ontModel.contains(ontClass,OWL.equivalentClass,superClass) || 
		    					  ontModel.contains(superClass,OWL.equivalentClass,ontClass) ) )  {	
									if ( (superClass.getNameSpace() != null) 
									        && (!(NONUSER_NAMESPACES.contains(
									                superClass.getNameSpace()))) ) {
			    				    	isRoot=false;
			    				    	break;
									}
		    				}
	    				}
	    			}
	    			if (isRoot) {
	    				rootClassList.add(ontClass);
	    			}
	    		} catch (ClassCastException cce) {
					log.error(cce);
				}
	    	}
    	} finally {
    		ci.close();
    	}
    	return rootClassList.iterator();
    }

    public List<VClass> getRootClasses() {
    	return getRootClasses(null);
    }
    
    private List <VClass> getRootClasses(String ontologyURI) {
        List<VClass> rootClasses = new ArrayList<VClass>();
        getOntModel().enterCriticalSection(Lock.READ);
        try {
        	Iterator<OntClass> rootIt = smarterListHierarchyRootClasses(
        	        getOntModel(), ontologyURI);
            while (rootIt.hasNext()) {    
                OntClass cls = rootIt.next();
                if (!cls.isAnon() && cls.getNameSpace() != null 
                        && !(NONUSER_NAMESPACES.contains(cls.getNameSpace()))) {
                    rootClasses.add(new VClassJena(cls,getWebappDaoFactory()));
                }
            }
            Collections.sort(rootClasses);
        } finally {
            getOntModel().leaveCriticalSection();
        }
        return rootClasses;
    }


    public List <VClass> getOntologyRootClasses(String ontologyURI) {
    	if (ontologyURI == null) {
    		throw new RuntimeException("can't find root classes for null ontology URI");
    	}
    	// return getRootClasses(ontologyURI);
    	List<VClass> ontologyRootClasses = new ArrayList<VClass>(); 
    	OntModel ontModel = getOntModel();
    	ontModel.enterCriticalSection(Lock.READ);
    	try {
    		Iterator<OntClass> ontClassIt = ontModel.listClasses();
    		while (ontClassIt.hasNext()) {
    			OntClass ontClass = ontClassIt.next();
    			if (ontologyURI.equals(ontClass.getNameSpace())) {
    				boolean root = true;
    				StmtIterator superStmtIt = ontModel.listStatements(ontClass, RDFS.subClassOf, (RDFNode) null);
    				try {
	    				while (superStmtIt.hasNext()) {
	    					Statement superStmt = superStmtIt.nextStatement();
	    					if ( superStmt.getObject().isResource() && ontologyURI.equals(((Resource) superStmt.getObject()).getNameSpace()) ) {
	    						root = false;
	    						break;
	    					}
	    				}
    				} finally {
    					superStmtIt.close();
    				}
    				if (root) {
    					ontologyRootClasses.add(new VClassJena(ontClass,getWebappDaoFactory()));
    				}
    			}
    		}
    	} finally {
    		ontModel.leaveCriticalSection();
    	}
    	return ontologyRootClasses;
    }

    public List <String> getSubClassURIs(String classURI) {
        List subURIs = new ArrayList();
        OntClass superClass = getOntClass(getOntModel(),classURI);
        try {
            Iterator subIt = superClass.listSubClasses(true);
            while (subIt.hasNext()) {
                OntClass cls = (OntClass) subIt.next();
                subURIs.add(getClassURIStr(cls));
            }
        } catch (Exception e) {
           	// we'll try this again using a different method that doesn't try to convert to OntClass
        	List<Resource> supList = this.listDirectObjectPropertySubjects(getOntModel().getResource(classURI), RDFS.subClassOf);
        	for (Resource res : supList) {
        		subURIs.add(res.getURI());
        	}
        }
        return subURIs;
    }
    
    public List <String> getSuperClassURIs(String classURI, boolean direct) {
        List supURIs = new ArrayList();
        OntClass subClass = getOntClass(getOntModel(), classURI);
        try {
            Iterator supIt = subClass.listSuperClasses(direct);
            while (supIt.hasNext()) {
                OntClass cls = (OntClass) supIt.next();
                supURIs.add(getClassURIStr(cls));
            }
        } catch (Exception e) {
        	//TODO make this attempt respect the direct argument
        	// we'll try this again using a different method that doesn't try to convert to OntClass
        	List<Resource> supList = this.listDirectObjectPropertyValues(getOntModel().getResource(classURI), RDFS.subClassOf);
        	for (Resource res : supList) {
        		supURIs.add(getClassURIStr(res));
        	}
        }
        return supURIs;
    }

    public VClass getTopConcept() {
      	VClass top = new VClass();
    	if (getOntModel().getProfile().NAMESPACE().equals(RDFS.getURI())) {
    		top.setURI(RDF.getURI()+"Resource");
    	} else {
    		top.setURI( (getOntModel().getProfile().THING().getURI()!=null) ? (getOntModel().getProfile().THING().getURI()): null);		
    	}
    	if (top.getURI() != null) {
    		top.setName(top.getLocalName());
    		return top;
    	} else {
    		return null;
    	} 
    }
    
    public VClass getBottomConcept() {
    	VClass bottom = new VClass();
    	if (getOntModel().getProfile().NAMESPACE().equals(RDFS.getURI())) {
    		return null;
    	} else {
    		bottom.setURI( (getOntModel().getProfile().NOTHING().getURI()!=null) ? (getOntModel().getProfile().NOTHING().getURI()): null);		
    	}
    	if (bottom.getURI() != null) {
    		bottom.setName(bottom.getLocalName());
    		return bottom;
    	} else {
    		return null;
    	}
    }
    
    public VClass getVClassByURI(String URIStr) {
        getOntModel().enterCriticalSection(Lock.READ);
        try {
        	OntClass cls = getOntClass(getOntModel(),URIStr);
            if (cls != null) {
                return new VClassJena(cls,getWebappDaoFactory());
            } else {
            	return null;
            }
        } finally {
            getOntModel().leaveCriticalSection();
        }
    }

    public void insertNewVClass(VClass cls) throws InsertException {
    	insertNewVClass(cls,getOntModelSelector().getTBoxModel());
    }

    public List<VClass> getVClassesForProperty(String propertyURI, boolean domainSide) {    
    	return getVClassesForProperty(null, propertyURI, domainSide);
    }
    
    public List<VClass> getVClassesForProperty(String vclassURI, String propertyURI) {
    	return getVClassesForProperty(vclassURI, propertyURI, true);
    }
    
    private List<VClass> getVClassesForProperty(String vclassURI, String propertyURI, boolean domainSide) {
        List<VClass> vClasses = new ArrayList<VClass>();
        getOntModel().enterCriticalSection(Lock.READ);
        try {
            ObjectProperty op = getOntModel().getObjectProperty(propertyURI);
            if (op != null) {
            	OntResource superclass = null;
            	if (vclassURI != null) {
            		// TODO need a getAllSuperPropertyURIs method in ObjectPropertyDao
            		List<String> superproperties = getWebappDaoFactory().getObjectPropertyDao().getSuperPropertyURIs(propertyURI,false);
            		superproperties.add(propertyURI);
            		HashSet<String> subjSuperclasses = new HashSet<String>(getAllSuperClassURIs(vclassURI));
            		subjSuperclasses.add(vclassURI); 
        			for (String objectPropertyURI : superproperties) {
		            	for (Iterator restStmtIt = getOntModel().listStatements(null,OWL.onProperty,getOntModel().getProperty(objectPropertyURI)); restStmtIt.hasNext();) {
		            		Statement restStmt = (Statement) restStmtIt.next();
		            		Resource restRes = restStmt.getSubject();	            		
		            		for (Iterator axStmtIt = getOntModel().listStatements(null,null,restRes); axStmtIt.hasNext();) {
		            			Statement axStmt = (Statement) axStmtIt.next();
		            				OntResource subjOntRes = null;
		            				if (axStmt.getSubject().canAs(OntResource.class)) {
		            					subjOntRes = (OntResource) axStmt.getSubject().as(OntResource.class);
		            				}
		            				if (
		            				 (subjOntRes != null) && (subjSuperclasses.contains(getClassURIStr(subjOntRes))) &&
		            				(axStmt.getPredicate().equals(RDFS.subClassOf) || (axStmt.getPredicate().equals(OWL.equivalentClass)))	
		            				) {
				            		if (restRes.canAs(AllValuesFromRestriction.class)) {
				            			AllValuesFromRestriction avfRest = (AllValuesFromRestriction) restRes.as(AllValuesFromRestriction.class);
				            			Resource avf = avfRest.getAllValuesFrom();
				            			if (avf.canAs(OntClass.class)) {
				            				superclass = (OntClass) avfRest.getAllValuesFrom().as(OntClass.class);
				            			}
				            		} 
		            			}
		            		}
            			}
            		}
            	}    	
            	if (superclass == null) {
            		superclass = (domainSide) ? op.getRange() : op.getDomain();
            		if (superclass == null) {
            			superclass = getOntModel().getOntResource(OWL.Thing.getURI());
            		}
            	}
                if (superclass != null) {
                	VClass superVclass;
                	if (superclass.isAnon()) {
                		superVclass = getVClassByURI(getClassURIStr(superclass));
                	} else {
                		superVclass = getVClassByURI(superclass.getURI());
                	}
                    if (superVclass != null) {
                        vClasses.add(superVclass);
						String isInferencing = getWebappDaoFactory().getProperties().get("infersTypes");
						// if this model infers types based on the taxonomy, adding the subclasses will only
						// waste time for no benefit
						PelletListener pl = getWebappDaoFactory().getPelletListener();
						if (pl == null || !pl.isConsistent() || pl.isInErrorState() || pl.isReasoning() 
								|| isInferencing == null || "false".equalsIgnoreCase(isInferencing)) {
                        	Iterator classURIs = getAllSubClassURIs(getClassURIStr(superclass)).iterator();
                        	while (classURIs.hasNext()) {
                            	String classURI = (String) classURIs.next();
                            	VClass vClass = getVClassByURI(classURI);
                            	if (vClass != null)
                            	    vClasses.add(vClass);
                        	}
						} 
                    }
                }
            }
        } finally {
            getOntModel().leaveCriticalSection();
        }
        return vClasses;
    }

/////////////////////////////////////////////////////////////////////////////////////////

       public void addVClassesToGroup(VClassGroup group) {
            addVClassesToGroup(group, true);
        }

        @Deprecated
        public void addVClassesToGroup(VClassGroup group, boolean includeUninstantiatedClasses) {
            addVClassesToGroup(group, includeUninstantiatedClasses, false);
        }

        @Deprecated
        public void addVClassesToGroup(VClassGroup group, boolean includeUninstantiatedClasses, boolean getIndividualCount) {
            getOntModel().enterCriticalSection(Lock.READ);
            
            if (getIndividualCount) {
                group.setIndividualCount( getClassGroupInstanceCount(group));
            } 
            
            try {
                if ((group != null) && (group.getURI() != null)) {
                    Resource groupRes = ResourceFactory.createResource(group.getURI());
                    AnnotationProperty inClassGroup = getOntModel().getAnnotationProperty(VitroVocabulary.IN_CLASSGROUP);
                    if (inClassGroup != null) {
                        ClosableIterator annotIt = getOntModel().listStatements((OntClass)null,inClassGroup,groupRes);
                        try {
                            while (annotIt.hasNext()) {
                                try {
                                    Statement annot = (Statement) annotIt.next();
                                    Resource cls = (Resource) annot.getSubject();
                                    VClass vcw = (VClass) getVClassByURI(cls.getURI());
                                    if (vcw != null) {
                                        boolean classIsInstantiated = false;
                                        if (getIndividualCount) {
                                        	Model aboxModel = getOntModelSelector().getABoxModel();
                                        	aboxModel.enterCriticalSection(Lock.READ);
                                        	int count = 0;
                                        	try {
                                        		String countQueryStr = "SELECT COUNT(*) WHERE \n" +
                                        		                       "{ ?s a <" + cls.getURI() + "> } \n";
                                        		Query countQuery = QueryFactory.create(countQueryStr, Syntax.syntaxARQ);
                                        		QueryExecution qe = QueryExecutionFactory.create(countQuery, aboxModel);
                                        		ResultSet rs =qe.execSelect();
                                        		count = Integer.parseInt(((Literal) rs.nextSolution().get(".1")).getLexicalForm());
                                        		//count = aboxModel.listStatements(null,RDF.type,cls).toList().size();
                                        	} finally {
                                        		aboxModel.leaveCriticalSection();
                                        	}
                                        	vcw.setEntityCount(count);
                                        	classIsInstantiated = (count > 0);
                                        } else if (includeUninstantiatedClasses == false) {
	                                        // Note: to support SDB models, may want to do this with 
	                                        // SPARQL and LIMIT 1 if SDB can take advantage of it
                                        	Model aboxModel = getOntModelSelector().getABoxModel();
                                        	aboxModel.enterCriticalSection(Lock.READ);
                                        	try {
		                                        ClosableIterator countIt = aboxModel.listStatements(null,RDF.type,cls);
		                                        try {
		                                            if (countIt.hasNext()) {
		                                            	classIsInstantiated = true;
		                                            }
		                                        } finally {
		                                            countIt.close();
		                                        }
                                        	} finally {
                                        		aboxModel.leaveCriticalSection();
                                        	}
                                        }
                                        
                                        if (includeUninstantiatedClasses || classIsInstantiated) {
                                            group.add(vcw);
                                        }
                                    }
                                } catch (ClassCastException cce) {
                                    log.error(cce, cce);
                                }
                            }
                        } finally {
                            annotIt.close();
                        }
                    }
                }
                java.util.Collections.sort(group.getVitroClassList());
            } finally {
                getOntModel().leaveCriticalSection();
            }
        }

        int getClassGroupInstanceCount(VClassGroup vcg){        
            Model ontModel = getOntModel();
            ontModel.enterCriticalSection(Lock.READ);
            int count = 0;
            try {
                String queryText =              
                    "SELECT COUNT( DISTINCT ?instance ) WHERE { \n" +                    
                    "      ?class <"+VitroVocabulary.IN_CLASSGROUP+"> <"+vcg.getURI() +"> .\n" +                
                    "      ?instance a ?class .  \n" +                    
                    "}" ;                
                Query countQuery = QueryFactory.create(queryText, Syntax.syntaxARQ);
                QueryExecution qe = QueryExecutionFactory.create(countQuery, ontModel);
                ResultSet rs =qe.execSelect();
                count = Integer.parseInt(((Literal) rs.nextSolution().get(".1")).getLexicalForm());
            }catch(Exception ex){
                log.error(ex,ex);
            } finally {
                ontModel.leaveCriticalSection();
            }
            return count;
        }
        
        
        public void addVClassesToGroups(List <VClassGroup> groups) {
            getOntModel().enterCriticalSection(Lock.READ);
            try {
                if (groups != null) {
                    Iterator groupIt = groups.iterator();
                    while (groupIt.hasNext()) {
                        VClassGroup g = (VClassGroup) groupIt.next();
                        addVClassesToGroup(g);
                    }
                } 
            } finally {
                getOntModel().leaveCriticalSection();
            }
        }

        public int insertNewVClass(VClass cls, OntModel ontModel) throws InsertException {
            ontModel.enterCriticalSection(Lock.WRITE);
            getOntModel().getBaseModel().notifyEvent(new EditEvent(getWebappDaoFactory().getUserURI(),true));
            try {
            	String errMsgStr = getWebappDaoFactory().checkURI(cls.getURI());
            	if (errMsgStr != null) {
            		throw new InsertException(errMsgStr);
            	}
                OntClass ontCls = ontModel.createClass(cls.getURI());
                try {
                	if (cls.getName() != null && cls.getName().length() > 0) {
                		ontCls.setLabel(cls.getName(), (String) getDefaultLanguage());
                	} else {
                		ontCls.removeAll(RDFS.label);
                	}
                } catch (Exception e) {
                    log.error("error setting label for class "+cls.getURI());
                }
                try {
                	if (cls.getGroupURI() != null && cls.getGroupURI().length()>0) {
	                	String badURIErrorStr = checkURI(cls.getGroupURI());
	                	if (badURIErrorStr == null) {
	                		ontCls.addProperty(IN_CLASSGROUP, getOntModel().getResource(cls.getGroupURI()));
	                	} else {
	                		log.error(badURIErrorStr);
	                	}
                	}
                } catch (Exception e) {
                    log.error("error linking class "+cls.getURI()+" to class group");
                }
                addPropertyStringValue(ontCls,SHORTDEF,cls.getShortDef(),ontModel);
                addPropertyStringValue(ontCls,EXAMPLE_ANNOT,cls.getExample(),ontModel);
                addPropertyStringValue(ontCls,DESCRIPTION_ANNOT,cls.getDescription(),ontModel);
                addPropertyIntValue(ontCls,DISPLAY_LIMIT,cls.getDisplayLimit(),ontModel);
                addPropertyIntValue(ontCls,DISPLAY_RANK_ANNOT,cls.getDisplayRank(),ontModel);
                
                ontCls.removeAll(HIDDEN_FROM_DISPLAY_BELOW_ROLE_LEVEL_ANNOT);
                if (HIDDEN_FROM_DISPLAY_BELOW_ROLE_LEVEL_ANNOT != null && cls.getHiddenFromDisplayBelowRoleLevel() != null) { // only need to add if present
                    try {
                        ontCls.addProperty(HIDDEN_FROM_DISPLAY_BELOW_ROLE_LEVEL_ANNOT, ResourceFactory.createResource(cls.getHiddenFromDisplayBelowRoleLevel().getURI()));
                    } catch (Exception e) {
                        log.error("error adding HiddenFromDisplayBelowRoleLevel annotation to class "+cls.getURI());
                    }
                }
                
                ontCls.removeAll(PROHIBITED_FROM_UPDATE_BELOW_ROLE_LEVEL_ANNOT);
                if (PROHIBITED_FROM_UPDATE_BELOW_ROLE_LEVEL_ANNOT != null && cls.getProhibitedFromUpdateBelowRoleLevel() != null) { // only need to add if present
                    try {
                        ontCls.addProperty(PROHIBITED_FROM_UPDATE_BELOW_ROLE_LEVEL_ANNOT, ResourceFactory.createResource(cls.getProhibitedFromUpdateBelowRoleLevel().getURI()));
                    } catch (Exception e) {
                        log.error("error adding ProhibitedFromUpdateBelowRoleLevel annotation to class "+cls.getURI());
                    }
                }
                
                /* OPTIONAL annotation properties */
                addPropertyStringValue(ontCls,PROPERTY_CUSTOMENTRYFORMANNOT,cls.getCustomEntryForm(),ontModel);
                addPropertyStringValue(ontCls,PROPERTY_CUSTOMDISPLAYVIEWANNOT,cls.getCustomDisplayView(),ontModel);
                addPropertyStringValue(ontCls,PROPERTY_CUSTOMSHORTVIEWANNOT,cls.getCustomShortView(),ontModel);
                addPropertyStringValue(ontCls,PROPERTY_CUSTOMSEARCHVIEWANNOT,cls.getCustomSearchView(),ontModel);
                addPropertyFloatValue(ontCls, SEARCH_BOOST_ANNOT, cls.getSearchBoost(), ontModel);
            } finally {
            	getOntModel().getBaseModel().notifyEvent(new EditEvent(getWebappDaoFactory().getUserURI(),false));
                ontModel.leaveCriticalSection();           
            }
            return 0;
        }

        public void updateVClass(VClass cls) {
        	updateVClass(cls,getOntModel());
        }

        public void updateVClass(VClass cls, OntModel ontModel) {
            ontModel.enterCriticalSection(Lock.WRITE);
            ontModel.getBaseModel().notifyEvent(new EditEvent(getWebappDaoFactory().getUserURI(),true));
            try {
                OntClass ontCls = ontModel.getOntClass(cls.getURI());
                
                if (ontCls != null) {
                	updateRDFSLabel(ontCls, cls.getName());
                    updatePropertyResourceURIValue(ontCls,IN_CLASSGROUP,cls.getGroupURI(),ontModel);
                    updatePropertyStringValue(ontCls,SHORTDEF,cls.getShortDef(),ontModel);
                    updatePropertyStringValue(ontCls,EXAMPLE_ANNOT,cls.getExample(),ontModel);
                    updatePropertyStringValue(ontCls,DESCRIPTION_ANNOT,cls.getDescription(),ontModel);
                    updatePropertyNonNegativeIntValue(ontCls,DISPLAY_LIMIT,cls.getDisplayLimit(),ontModel);
                    updatePropertyNonNegativeIntValue(ontCls,DISPLAY_RANK_ANNOT,cls.getDisplayRank(),ontModel);
                    updatePropertyFloatValue(ontCls, SEARCH_BOOST_ANNOT, cls.getSearchBoost(), ontModel);
                    
                    if (cls.getHiddenFromDisplayBelowRoleLevel() != null) {
                      updatePropertyResourceURIValue(ontCls,HIDDEN_FROM_DISPLAY_BELOW_ROLE_LEVEL_ANNOT,cls.getHiddenFromDisplayBelowRoleLevel().getURI(),ontModel);                    
                    }
                      
                    if (cls.getProhibitedFromUpdateBelowRoleLevel() != null) {
                      updatePropertyResourceURIValue(ontCls,PROHIBITED_FROM_UPDATE_BELOW_ROLE_LEVEL_ANNOT,cls.getProhibitedFromUpdateBelowRoleLevel().getURI(),ontModel);
                    }
                    
                    updatePropertyStringValue(ontCls,PROPERTY_CUSTOMENTRYFORMANNOT,cls.getCustomEntryForm(),ontModel);
                    updatePropertyStringValue(ontCls,PROPERTY_CUSTOMDISPLAYVIEWANNOT,cls.getCustomDisplayView(),ontModel);
                    updatePropertyStringValue(ontCls,PROPERTY_CUSTOMSHORTVIEWANNOT,cls.getCustomShortView(),ontModel);
                    updatePropertyStringValue(ontCls,PROPERTY_CUSTOMSEARCHVIEWANNOT,cls.getCustomSearchView(),ontModel);
                } else {
                    log.error("error: cannot find jena class "+cls.getURI()+" for updating");
                }
            } finally {
            	getOntModel().getBaseModel().notifyEvent(new EditEvent(getWebappDaoFactory().getUserURI(),false));
                ontModel.leaveCriticalSection();
            }
        }
 
    public boolean isSubClassOf(VClass vc1, VClass vc2) {
    	if (vc1 == null || vc2 == null) {
    		return false;
    	}
    	return isSubClassOf(vc1.getURI(), vc2.getURI());
    }
    
    public boolean isSubClassOf(String vclassURI1, String vclassURI2) {
    	if (vclassURI1 == null || vclassURI2 == null) {
    		return false;
    	}
       	OntModel ontModel = getOntModel();
    	try {
    		ontModel.enterCriticalSection(Lock.READ);
    		OntClass oc1 = getOntClass(ontModel, vclassURI1);
    		OntClass oc2 = getOntClass(ontModel, vclassURI2);
    		if (oc1 == null || oc2 == null) {
    			return false;
    		} 
    		return ontModel.contains(oc1, RDFS.subClassOf, oc2);
    	} finally {
    		ontModel.leaveCriticalSection();
    	}
    }
        
}
