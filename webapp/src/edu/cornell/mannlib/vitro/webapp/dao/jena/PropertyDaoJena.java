/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.Restriction;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.sparql.resultset.ResultSetMem;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyInstance;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.jena.event.EditEvent;

public class PropertyDaoJena extends JenaBaseDao implements PropertyDao {
	
	protected static final Log log = LogFactory.getLog(PropertyDaoJena.class.getName());

    private static final Map<String, String> NAMESPACES = new HashMap<String, String>() {{
        put("afn", VitroVocabulary.AFN);
        put("owl", VitroVocabulary.OWL);
        put("rdf", VitroVocabulary.RDF);
        put("rdfs", VitroVocabulary.RDFS);
        put("vitro", VitroVocabulary.vitroURI);
        put("vitroPublic", VitroVocabulary.VITRO_PUBLIC);
    }};
    
    protected static final String PREFIXES;
    static {
        String prefixes = "";
        for (String key : NAMESPACES.keySet()) {
            prefixes += "PREFIX " + key + ": <" + NAMESPACES.get(key) + ">\n";
        }
        PREFIXES = prefixes;
        log.debug("Query prefixes: " + PREFIXES);
    }
    
    protected DatasetWrapperFactory dwf;
    
    public PropertyDaoJena(DatasetWrapperFactory dwf, 
                           WebappDaoFactoryJena wadf) {
        super(wadf);
        this.dwf = dwf;
    }
    
    @Override
    protected OntModel getOntModel() {
    	return getOntModelSelector().getTBoxModel();
    }

	public void addSuperproperty(ObjectProperty property, ObjectProperty superproperty) {
    	addSuperproperty(property.getURI(),superproperty.getURI());
    }
    
    public void addSuperproperty(String propertyURI, String superpropertyURI) {
		getOntModel().enterCriticalSection(Lock.WRITE);
		try {
			getOntModel().add(getOntModel().getResource(propertyURI),RDFS.subPropertyOf,getOntModel().getResource(superpropertyURI));
		} finally {
			getOntModel().leaveCriticalSection();
		}
	}
    
    public void removeSuperproperty(ObjectProperty property, ObjectProperty superproperty) {
    	removeSuperproperty(property.getURI(),superproperty.getURI());
    }
    
    public void removeSuperproperty(String propertyURI, String superpropertyURI) {
    	getOntModel().enterCriticalSection(Lock.WRITE);
    	try {
    		if (getOntModel().contains(getOntModel().getResource(propertyURI),RDFS.subPropertyOf,getOntModel().getResource(superpropertyURI))) {
    			getOntModel().remove(getOntModel().getResource(propertyURI),RDFS.subPropertyOf,getOntModel().getResource(superpropertyURI));
    		}
    	} finally {
    		getOntModel().leaveCriticalSection();
    	}
    }
    
    public void addSubproperty(ObjectProperty property, ObjectProperty subproperty) {
    	addSuperproperty(subproperty, property);
    }
    
    public void addSubproperty(String propertyURI, String subpropertyURI) {
    	addSuperproperty(subpropertyURI, propertyURI);
    }
    
    public void removeSubproperty(ObjectProperty property, ObjectProperty subproperty) {
    	removeSuperproperty(subproperty, property);
    }
    
    public void removeSubproperty(String propertyURI, String subpropertyURI) {
    	removeSuperproperty(subpropertyURI,propertyURI);
    }
    
    public List <String> getSubPropertyURIs(String propertyURI) {
    	List<String> subURIs = new LinkedList<String>();
    	getOntModel().enterCriticalSection(Lock.READ);
    	try {
            Iterator subIt = getOntModel().getOntProperty(propertyURI).listSubProperties(true);
            while (subIt.hasNext()) {
                try {
                    OntProperty prop = (OntProperty) subIt.next();
                    subURIs.add(prop.getURI());
                } catch (Exception cce) {}
            }
	    } catch (Exception e) {
	    	log.error(e, e); 
    	} finally {
    		getOntModel().leaveCriticalSection();
    	}
        return subURIs;
    }

    private void getAllSubPropertyURIs(String propertyURI, HashSet<String> subtree){
        List<String> directSubproperties = getSubPropertyURIs(propertyURI);     
        Iterator<String> it=directSubproperties.iterator();
        while(it.hasNext()){
            String uri = (String)it.next();
            if (!subtree.contains(uri)) {
            	subtree.add(uri);
            	getAllSubPropertyURIs(uri,subtree);
            }
        }
    }

    public List<String> getAllSubPropertyURIs(String propertyURI) {
    	HashSet<String> nodeSet = new HashSet<String>();
    	nodeSet.add(propertyURI);
    	getAllSubPropertyURIs(propertyURI, nodeSet);
    	nodeSet.remove(propertyURI);
    	List<String> outputList = new LinkedList<String>();
    	outputList.addAll(nodeSet);
    	return outputList;
    }
    
    public List <String> getSuperPropertyURIs(String propertyURI, boolean direct) {
       	List<String> supURIs = new LinkedList<String>();
    	getOntModel().enterCriticalSection(Lock.READ);
    	try {
            Iterator supIt = getOntModel().getOntProperty(propertyURI).listSuperProperties(direct);
            while (supIt.hasNext()) {
                try {
                    OntProperty prop = (OntProperty) supIt.next();
                    supURIs.add(prop.getURI());
                } catch (Exception cce) {}
            }
	    } catch (Exception e) {
	    	log.error(e, e); 
    	} finally {
    		getOntModel().leaveCriticalSection();
    	}
        return supURIs;
    }

    private void getAllSuperPropertyURIs(String propertyURI, HashSet<String> subtree){
        List<String> directSuperproperties = getSuperPropertyURIs(propertyURI,true);     
        Iterator<String> it=directSuperproperties.iterator();
        while(it.hasNext()){
            String uri = (String)it.next();
            if (!subtree.contains(uri)) {
            	subtree.add(uri);
            	getAllSuperPropertyURIs(uri,subtree);
            }
        }
    }

    public List<String> getAllSuperPropertyURIs(String propertyURI) {
    	HashSet<String> nodeSet = new HashSet<String>();
    	nodeSet.add(propertyURI);
    	getAllSuperPropertyURIs(propertyURI, nodeSet);
    	nodeSet.remove(propertyURI);
    	List<String> outputList = new LinkedList<String>();
    	outputList.addAll(nodeSet);
    	return outputList;
    }

	public void addSubproperty(Property property, Property subproperty) {
		addSubproperty(property.getURI(), subproperty.getURI());
	}

	public void addSuperproperty(Property property, Property superproperty) {
		addSuperproperty(property.getURI(), superproperty.getURI());
	}

	public void removeSubproperty(Property property, Property subproperty) {
		removeSubproperty(property.getURI(), subproperty.getURI());	
	}

	public void removeSuperproperty(Property property, Property superproperty) {
		removeSuperproperty(property.getURI(), superproperty.getURI());
	}

	public void addEquivalentProperty(String propertyURI,
			String equivalentPropertyURI) {
		if (propertyURI == null || equivalentPropertyURI == null) {
			throw new RuntimeException("cannot assert equivalence of anonymous properties");
		}
		OntModel ontModel = getOntModel();	
		ontModel.enterCriticalSection(Lock.WRITE);
		try {
			Resource property = ontModel.getResource(propertyURI);
			Resource equivalentProperty = ontModel.getResource(equivalentPropertyURI);
			ontModel.add(property, OWL.equivalentProperty, equivalentProperty);
			ontModel.add(equivalentProperty, OWL.equivalentProperty, property);
		} finally {
			ontModel.leaveCriticalSection();
		}
	}

	public void addEquivalentProperty(Property property,
			Property equivalentProperty) {
		addEquivalentProperty(property.getURI(), equivalentProperty.getURI());
	}

	public List<String> getEquivalentPropertyURIs(String propertyURI) {
       	List<String> equivURIs = new LinkedList<String>();
    	getOntModel().enterCriticalSection(Lock.READ);
    	try {
            StmtIterator eqStmtIt = getOntModel().listStatements(getOntModel().getResource(propertyURI), OWL.equivalentProperty, (RDFNode) null);
            while (eqStmtIt.hasNext()) {
                Statement eqStmt = eqStmtIt.nextStatement();
                RDFNode prop = eqStmt.getObject();
                if (prop.isResource() && ((Resource) prop).getURI() != null) {
                	equivURIs.add(((Resource) prop).getURI());
                }
            }
	    } catch (Exception e) {
	    	log.error(e, e); 
    	} finally {
    		getOntModel().leaveCriticalSection();
    	}
        return equivURIs;
	}

	public void removeEquivalentProperty(String propertyURI, String equivalentPropertyURI) {
		if (propertyURI == null || equivalentPropertyURI == null) {
			throw new RuntimeException("cannot remove equivalence axiom about anonymous properties");
		}
		OntModel ontModel = getOntModel();	
		ontModel.enterCriticalSection(Lock.WRITE);
		try {
			Resource property = ontModel.getResource(propertyURI);
			Resource equivalentProperty = ontModel.getResource(equivalentPropertyURI);
			ontModel.remove(property, OWL.equivalentProperty, equivalentProperty);
			ontModel.remove(equivalentProperty, OWL.equivalentProperty, property);
		} finally {
			ontModel.leaveCriticalSection();
		}
	}

	public void removeEquivalentProperty(Property property,
			Property equivalentProperty) {
		removeEquivalentProperty(property, equivalentProperty);
	}
	
	protected void removeABoxStatementsWithPredicate(Property predicate) {
		// DO NOT issue a removeAll() with a null (wildcard) in predicate position!
		if (predicate == null) {
			log.debug("Cannot remove ABox statements with a null predicate.");
			return;
		} else {
			removeABoxStatementsWithPredicate(predicate.getURI());
		}
	}
	
	protected void removeABoxStatementsWithPredicate(String predicateURI) {
		if (predicateURI == null) {
			log.debug("Cannot remove ABox statements with null predicate URI.");
			return;
		}
        OntModel aboxModel = getOntModelSelector().getABoxModel();
        aboxModel.enterCriticalSection(Lock.WRITE);
        try {
        	aboxModel.getBaseModel().notifyEvent(new EditEvent(getWebappDaoFactory().getUserURI(),true));
        	aboxModel.removeAll((Resource) null, aboxModel.getProperty(predicateURI), (RDFNode) null);
        } finally {
        	aboxModel.getBaseModel().notifyEvent(new EditEvent(getWebappDaoFactory().getUserURI(),false));
        	aboxModel.leaveCriticalSection();
        }
    		
	}
	
	/**
	 * Finds the classes that have a definition involving a restriction
	 * on the given property. 
	 *
	 * @param   propertyURI  identifier of a property
	 * @return  a list of VClass objects representing the classes that have
	 *          definitions involving a restriction on the given property.
	 */

    public List <VClass> getClassesWithRestrictionOnProperty(String propertyURI) {
    	
    	if (propertyURI == null) {
    		log.info("getClassesWithRestrictionOnProperty: called with null propertyURI");
    		return null;
    	}
    	    	
		OntModel ontModel = getOntModel();	
		ontModel.enterCriticalSection(Lock.READ);
		
		HashSet<String> classURISet = new HashSet<String>();
		
		try {
			Resource targetProp = ontModel.getResource(propertyURI);
			   
			if (targetProp != null) {
			
			    StmtIterator stmtIter = ontModel.listStatements((Resource) null, OWL.onProperty, (RDFNode) targetProp);
	
			    while (stmtIter.hasNext()) {
				   Statement statement = stmtIter.next();
				   
				   if ( statement.getSubject().canAs(OntClass.class) ) {
					   classURISet.addAll(getRelatedClasses((OntClass) statement.getSubject().as(OntClass.class)));
				   } else {
					   log.warn("getClassesWithRestrictionOnProperty: Unexpected use of onProperty: it is not applied to a class");
				   }
			    }
			} else {
	    		log.error("getClassesWithRestrictionOnProperty: Error: didn't find a Property in the ontology model for the URI: " +  propertyURI);				
			}
		} finally {
			ontModel.leaveCriticalSection();
		}

		List<VClass> classes = new ArrayList<VClass>();
		Iterator<String> iter = classURISet.iterator();
		
		VClassDao vcd = getWebappDaoFactory().getVClassDao();
		
		while (iter.hasNext()) {
		
		   String curi = iter.next();
		   VClass vc = vcd.getVClassByURI(curi);
		  
		   if (vc != null) {
		       classes.add(vc);	  
		   } else {
			   log.error("getClassesWithRestrictionOnProperty: Error: no VClass found for URI: " + curi);
		   }	
		}
       
        return (classes.size()>0) ? classes : null;
    }

	/**
	 * Finds all named superclasses, subclasses and equivalent classes of
	 * the given class.
	 *
	 * @param   resourceURI  identifier of a class
	 * @return  set of class URIs
	 * 
	 * Note: this method assumes that the caller holds a read lock on
	 * the ontology model.
	 */

    public HashSet<String> getRelatedClasses(OntClass ontClass) {
    	
        HashSet<String> classSet = new HashSet<String>();
  
        List<OntClass> classList = ontClass.listEquivalentClasses().toList();
        classList.addAll(ontClass.listSubClasses().toList());
        classList.addAll(ontClass.listSuperClasses().toList());
        
        Iterator<OntClass> it = classList.iterator();
		         
        while (it.hasNext()) {
        	OntClass oc = it.next();
        	
        	if (!oc.isAnon()) {
        		classSet.add(oc.getURI());
        	}
        }
        		
        return classSet;
    }
     
    protected ResultSet getPropertyQueryResults(Query query) {        
        log.debug("SPARQL query:\n" + query.toString());
        
        // RY Removing prebinding due to Jena bug: when isLiteral(?object) or 
        // isURI(?object) is added to the query as a filter, the query fails with prebinding
        // but succeeds when the subject uri is concatenated into the query string.
        //QuerySolutionMap subjectBinding = new QuerySolutionMap();
        //subjectBinding.add("subject", ResourceFactory.createResource(subjectUri));
                
        // Run the SPARQL query to get the properties
        DatasetWrapper w = dwf.getDatasetWrapper();
        Dataset dataset = w.getDataset();
        dataset.getLock().enterCriticalSection(Lock.READ);
        ResultSet rs = null;
        try {
            QueryExecution qexec = QueryExecutionFactory.create(
                    query, dataset); //, subjectBinding);
            try {
                rs = new ResultSetMem(qexec.execSelect());
            } finally {
                qexec.close();
            }
        } finally {
            dataset.getLock().leaveCriticalSection();
            w.close();
        }
        return rs;
    }
    
    /**
     * requires SPARQL 1.1 (or ARQ) property path support
     * @param vclassURI
     * @return list of property resources with union domains that include the vclass
     */
    protected List<Resource> getPropertiesWithAppropriateDomainFor(String vclassURI) {
        List<Resource> propertyResList = new ArrayList<Resource>();
    	String queryStr = 
    		      "PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
                  "PREFIX rdfs:  <http://www.w3.org/2000/01/rdf-schema#> \n" +
                  "PREFIX owl:   <http://www.w3.org/2002/07/owl#> \n\n " +
                  "SELECT ?p WHERE { \n" +
                  "  { \n" +
                  "    ?p rdfs:domain <" + vclassURI + "> . \n" +
                  "  } UNION { \n" +
                  "    ?parent rdfs:domain <" + vclassURI + "> . \n" +
                  "    ?p rdfs:subPropertyOf* ?parent. \n" + 
                  "    OPTIONAL { \n" +
                  "      ?p rdfs:domain ?childDomain \n" +
                  "    } \n" +
                  "    FILTER (!bound(?childDomain)) \n" +
                  "  } UNION { \n" +
                  "    ?f rdf:first <" + vclassURI + "> . \n" +
                  "    ?u rdf:rest* ?f . \n" +
                  "    ?d owl:unionOf ?u . \n" +
                  "    ?p rdfs:domain ?d . \n" +
                  "  } UNION { \n" +
                  "    ?f rdf:first <" + vclassURI + "> . \n" +
                  "    ?u rdf:rest* ?f . \n" +
                  "    ?d owl:unionOf ?u . \n" +
                  "    ?parent rdfs:domain ?d . \n" +
                  "    ?p rdfs:subPropertyOf* ?parent. \n" + 
                  "    OPTIONAL { \n" +
                  "      ?p rdfs:domain ?childDomain \n" +
                  "    } \n" +
                  "    FILTER (!bound(?childDomain)) \n" +
                  "  } \n" +
                  "  FILTER(?p != owl:bottomDataProperty \n" +
                  "      && ?p != owl:bottomObjectProperty) \n" +
                  "}";
    	Query q = QueryFactory.create(queryStr, Syntax.syntaxSPARQL_11);
    	QueryExecution qe = QueryExecutionFactory.create(
    			q, getOntModelSelector().getTBoxModel());
    	try {
    	    ResultSet rs = qe.execSelect();
    	    while (rs.hasNext()) {
    	    	QuerySolution qs = rs.nextSolution();
    	    	propertyResList.add(qs.getResource("p"));
    	    }
    	} finally {
    		qe.close();
    	}
    	return propertyResList;
    }
    
    public List<PropertyInstance> getAllPossiblePropInstForIndividual(String individualURI) {
    	Individual ind = getWebappDaoFactory().getIndividualDao().getIndividualByURI(individualURI);
    	VClassDao vcDao = getWebappDaoFactory().getVClassDao();
    	
    	List<VClass> allTypes = ind.getVClasses(false); // include indirect types
        
        Set<String> allSuperclassURIs = new HashSet<String>();
       
        for (VClass type : allTypes) {
            String classURI = type.getURI();
            if (classURI != null) {
                allSuperclassURIs.add(type.getURI());
            }
            for (String equivURI : vcDao.getEquivalentClassURIs(classURI)) {
                allSuperclassURIs.add(equivURI);
                allSuperclassURIs.addAll(vcDao.getAllSuperClassURIs(equivURI));
            }
            allSuperclassURIs.addAll(vcDao.getAllSuperClassURIs(classURI));
        }
        
        List<VClass> vclasses = new ArrayList<VClass>();
        for(String vclassURI : allSuperclassURIs) {
            VClass vclass = vcDao.getVClassByURI(vclassURI);
            if (vclass != null) {
                vclasses.add(vclass);
            }
        }
    	
    	List<PropertyInstance> piList = getAllPropInstByVClasses(vclasses);
    	
    	for (PropertyInstance pi : piList) {
    		pi.setDomainClassURI(ind.getVClassURI());
    		// TODO: improve.  This is so the DWR property editing passes the 
    		// individual's VClass to get the right restrictions
    	}

		return piList;
    }
    
    /*
     * sorts VClasses so that subclasses come before superclasses
     */
    private class VClassHierarchyRanker implements Comparator<VClass> {
    	private VClassDao vcDao;
    	public VClassHierarchyRanker(VClassDao vcDao) {
    		this.vcDao = vcDao;
    	}
    	public int compare(VClass vc1, VClass vc2) {
    		if (vcDao.isSubClassOf(vc1, vc2)) {
    			return -1;
    		} else if (vcDao.isSubClassOf(vc2, vc1)) {
    		    return 1;
    		} else {
    		    return 0;
    		}
    	}
    }
    
    
    public List<PropertyInstance> getAllPropInstByVClass(String classURI) {
        if (classURI==null || classURI.length()<1) {
            return null;
        }
        
        VClassDao vcDao = getWebappDaoFactory().getVClassDao();
        
        Set<String> allSuperclassURIs = new HashSet<String>();
       
        allSuperclassURIs.add(classURI);
        for (String equivURI : vcDao.getEquivalentClassURIs(classURI)) {
            allSuperclassURIs.add(equivURI);
            allSuperclassURIs.addAll(vcDao.getAllSuperClassURIs(equivURI));
        }
        allSuperclassURIs.addAll(vcDao.getAllSuperClassURIs(classURI));
        
        List<VClass> vclasses = new ArrayList<VClass>();
        for(String vclassURI : allSuperclassURIs) {
            VClass vclass = vcDao.getVClassByURI(vclassURI);
            if (vclass != null) {
                vclasses.add(vclass);
            }
        }
        return getAllPropInstByVClasses(vclasses);
    }
    
    private void updatePropertyRangeMap(Map<String, Resource[]> map, 
                                        String propURI, 
                                        Resource[] ranges) {
        Resource[] existingRanges = map.get(propURI);
        if (existingRanges == null) {
            map.put(propURI, ranges);
        } else if (existingRanges[0] == null && existingRanges[1] != null) {
            existingRanges[0] = ranges[0];
            map.put(propURI, existingRanges);
        } else if (existingRanges[0] != null && existingRanges[1] == null) {
            existingRanges[1] = ranges[1];
            map.put(propURI, existingRanges);            
        }
    }
    
    public List<PropertyInstance> getAllPropInstByVClasses(List<VClass> vclasses) {
        
        List<PropertyInstance> propInsts = new ArrayList<PropertyInstance>();
        
        if(vclasses == null || vclasses.isEmpty()) {
            return propInsts;
        }
        
        Collections.sort(vclasses, new VClassHierarchyRanker(this.getWebappDaoFactory().getVClassDao()));
        
        OntModel ontModel = getOntModelSelector().getTBoxModel();
        
        try {
        
        	ontModel.enterCriticalSection(Lock.READ);
        	
        	// map object property URI to an array of two resources:
        	// the first is the "allValuesFrom" resource and the second is
        	// "someValuesFrom"
        	Map<String, Resource[]> applicableProperties = 
        	        new HashMap<String, Resource[]>();
        	
        	try {
		        for (VClass vclass : vclasses) {
		            if (vclass.isAnonymous()) {
		                continue; 
		            }
		            String VClassURI = vclass.getURI();
		            
		        	OntClass ontClass = getOntClass(ontModel,VClassURI);
		        	if (ontClass != null) {
		        	    List<OntClass> relatedClasses = new ArrayList<OntClass>();
		        	    relatedClasses.addAll(ontClass.listEquivalentClasses().toList());
		        	    relatedClasses.addAll(ontClass.listSuperClasses().toList());
		        	    for (OntClass relatedClass : relatedClasses) {
    		        	    // find properties in restrictions
    		        		if (relatedClass.isRestriction()) {
    		        			// TODO: check if restriction is something like
    		        			// maxCardinality 0 or allValuesFrom owl:Nothing,
    		        			// in which case the property is NOT applicable!
    		        			Restriction rest = (Restriction) relatedClass.as(Restriction.class);
    		        			OntProperty onProperty = rest.getOnProperty();
    		        			if (onProperty != null) {
    		        			    Resource[] ranges = new Resource[2];
    		        			    if (rest.isAllValuesFromRestriction()) {
    		        			        ranges[0] = (rest.asAllValuesFromRestriction()).getAllValuesFrom();
    		        			    } else if (rest.isSomeValuesFromRestriction()) {
                                        ranges[1] = (rest.asSomeValuesFromRestriction()).getSomeValuesFrom();
                                    }
    		        				updatePropertyRangeMap(applicableProperties, onProperty.getURI(), ranges);
    		        			}
    		        		}
		        	    }
		        		
		        	    List<Resource> propertyList = 
		        	    	    getPropertiesWithAppropriateDomainFor(VClassURI);
		        		for (Resource prop : propertyList) {
		        		    if (prop.getNameSpace() != null 
		        		            && !NONUSER_NAMESPACES.contains(
		        		                    prop.getNameSpace()) ) {
		        		        StmtIterator rangeSit = prop.listProperties(
		        		                RDFS.range);
		        		        Resource rangeRes = null;
		        		        while (rangeSit.hasNext()) {    
		        		            Statement s = rangeSit.nextStatement();
		        		            if (s.getObject().isURIResource()) {
		        		                rangeRes = (Resource) s.getObject();
		        		            }
		        		        }
		        		        Resource[] ranges = new Resource[2];
		        		        ranges[0] = rangeRes;
		        		        updatePropertyRangeMap(
		        		                applicableProperties, prop.getURI(), ranges);
		        		        
		        		    }
		        		}
		        		
		        	}
		        }       
        	} catch (Exception e) {
        		log.error("Unable to get applicable properties " +
        		          "by examining property restrictions and domains", e);
        	}
        	
        	// make the PropertyInstance objects
	        for (String propertyURI : applicableProperties.keySet()) {
	        	OntProperty op = ontModel
	        	        .getOntProperty(propertyURI);
	        	if (op == null) {
	        	    continue;
	        	}
	        	String domainURIStr = getURIStr(op.getDomain());
	        	Resource[] foundRanges = applicableProperties.get(propertyURI);
	        	Resource rangeRes = (foundRanges[0] != null) 
	        	        ? foundRanges[0]
	        	        : (op.getRange() == null && foundRanges[1] != null)
	        	                ? foundRanges[1]
	        	                : op.getRange();
                PropertyInstance pi = new PropertyInstance();
                if (rangeRes != null) {
                	String rangeClassURI;
                	if (rangeRes.isAnon()) {
                		rangeClassURI = PSEUDO_BNODE_NS + rangeRes.getId()
                		        .toString();
                	} else {
                		rangeClassURI = (String) rangeRes.getURI();
                	}
                    pi.setRangeClassURI(rangeClassURI);
                	VClass range = getWebappDaoFactory().getVClassDao()
                	        .getVClassByURI(rangeClassURI);
                	if (range == null) {
                		range = new VClass();
                		range.setURI(rangeClassURI);
                		range.setName(range.getLocalName());
                	}
                	pi.setRangeClassName(range.getName());
                } else {
                	pi.setRangeClassURI(OWL.Thing.getURI()); // TODO see above
                }
                pi.setDomainClassURI(domainURIStr);
                VClass domain = getWebappDaoFactory().getVClassDao()
    	                .getVClassByURI(domainURIStr);
		    	if (domain == null) {
		    		domain = new VClass();
		    		domain.setURI(domainURIStr);
		    		domain.setName(domain.getLocalName());
		    	}
                pi.setDomainClassName(domain.getName());
                pi.setSubjectSide(true);
                pi.setPropertyURI(op.getURI());
                pi.setPropertyName(getLabelOrId(op)); // TODO
                pi.setRangePublic(getLabelOrId(op));
                pi.setDomainPublic(getLabelOrId(op));
                propInsts.add(pi);
	        }      
        } finally {
        	ontModel.leaveCriticalSection();
        }
           
        return propInsts;
        
    }

    private String getURIStr(Resource res) {
    	String URIStr;
    	if (res == null) {
    		URIStr = OWL.Thing.getURI(); // TODO: rdf:Resource if using RDF model; or option to turn off entirely
    	} else {
            if (res.isAnon()) {
            	URIStr = PSEUDO_BNODE_NS+res.getId().toString();
            } else {
            	URIStr = res.getURI();
            }
    	}
    	return URIStr;
    }
    
    
}
