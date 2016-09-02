/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.ontology.AllValuesFromRestriction;
import org.apache.jena.ontology.AnnotationProperty;
import org.apache.jena.ontology.CardinalityRestriction;
import org.apache.jena.ontology.ComplementClass;
import org.apache.jena.ontology.HasValueRestriction;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.IntersectionClass;
import org.apache.jena.ontology.MaxCardinalityRestriction;
import org.apache.jena.ontology.MinCardinalityRestriction;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.ontology.ProfileException;
import org.apache.jena.ontology.Restriction;
import org.apache.jena.ontology.SomeValuesFromRestriction;
import org.apache.jena.ontology.UnionClass;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.shared.Lock;
import org.apache.jena.util.iterator.ClosableIterator;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vitro.webapp.beans.Classes2Classes;
import edu.cornell.mannlib.vitro.webapp.beans.Ontology;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.dao.InsertException;
import edu.cornell.mannlib.vitro.webapp.dao.OntologyDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.jena.event.EditEvent;
import edu.cornell.mannlib.vitro.webapp.web.URLEncoder;

public class VClassDaoJena extends JenaBaseDao implements VClassDao {

    protected static final Log log = LogFactory.getLog(VClassDaoJena.class);
    private boolean isUnderlyingStoreReasoned = false;

    public VClassDaoJena(WebappDaoFactoryJena wadf, boolean isUnderlyingStoreReasoned) {
        super(wadf);
        this.isUnderlyingStoreReasoned = isUnderlyingStoreReasoned;
    }

    @Override
    protected OntModel getOntModel() {
        return getOntModelSelector().getTBoxModel();
    }
    
    protected boolean isUnderlyingStoreReasoned() {
        return this.isUnderlyingStoreReasoned;
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
                            OntClass avf = fillerRes.as(OntClass.class);
                            labelStr += getLabelForClass(avf,withPrefix,forPickList);
                        } else {
                            try {
                                labelStr += getLabelOrId(fillerRes.as(OntResource.class));
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
                                labelStr += getLabelOrId(fillerNode.as(OntResource.class));
                            } else {
                                labelStr += fillerNode.as(Literal.class).getLexicalForm(); 
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
                        ComplementClass ccls = cls.as(ComplementClass.class);
                        labelStr += getLabelForClass(ccls.getOperand(),withPrefix,forPickList);		    			
                    } else if (cls.isIntersectionClass()) {
                        IntersectionClass icls = cls.as(IntersectionClass.class);
                        for (Iterator<? extends OntClass> operandIt = 
                                icls.listOperands(); operandIt.hasNext();) {
                            OntClass operand = operandIt.next();
                            labelStr += getLabelForClass(operand,withPrefix,forPickList);
                            if (operandIt.hasNext()) {
                                labelStr += " and ";
                            }
                        }
                    } else if (cls.isUnionClass()) {
                        UnionClass icls = cls.as(UnionClass.class);
                        for (Iterator<? extends OntClass> operandIt = 
                                icls.listOperands(); operandIt.hasNext();) {
                            OntClass operand = operandIt.next();
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
                    return "<a href=\"vclassEdit?uri=" + 
                            URLEncoder.encode(getClassURIStr(cls)) + 
                                    "\">[anonymous class]</a>";
                }
            } else {
                if (withPrefix || forPickList) {
                    OntologyDao oDao=getWebappDaoFactory().getOntologyDao();
                    Ontology o = oDao.getOntologyByURI(cls.getNameSpace());
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
            log.error(e, e);
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
                        OntResource restOntRes = restRes.as(OntResource.class);
                        smartRemove(restOntRes, ontModel);
                    }
                }
                restIt = ontModel.listSubjectsWithProperty(OWL.someValuesFrom, cls);
                while(restIt.hasNext()) {
                    Resource restRes = restIt.next();
                    if (restRes.canAs(OntResource.class)) {
                        OntResource restOntRes = restRes.as(OntResource.class);
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
            for (Iterator<? extends OntClass> i = 
                    ontClass.listDisjointWith(); i.hasNext(); ) {
                OntClass disjointClass = i.next();
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
            ClosableIterator<OntClass> equivalentOntClassIt = ontClass.listEquivalentClasses();
            try {
                for (Iterator<OntClass> eqOntClassIt = 
                        equivalentOntClassIt; eqOntClassIt.hasNext(); ) {
                    OntClass eqClass = eqOntClassIt.next();
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
        insertNewClasses2Classes(c2c);
    }

    public void removeSuperclass(VClass vclass, VClass superclass) {
        removeSuperclass(vclass.getURI(),superclass.getURI());
    }

    public void removeSuperclass(String vclassURI, String superclassURI) {
        Classes2Classes c2c = new Classes2Classes();
        c2c.setSubclassURI(vclassURI);
        c2c.setSuperclassURI(superclassURI);
        deleteClasses2Classes(c2c);
    }

    public void addSubclass(VClass vclass, VClass subclass) {
        addSubclass(vclass.getURI(),subclass.getURI());
    }

    public void addSubclass(String vclassURI, String subclassURI) {
        Classes2Classes c2c = new Classes2Classes();
        c2c.setSubclassURI(subclassURI);
        c2c.setSuperclassURI(vclassURI);
        insertNewClasses2Classes(c2c);
    }

    public void removeSubclass(VClass vclass, VClass subclass) {
        removeSubclass(vclass.getURI(),subclass.getURI());
    }

    public void removeSubclass(String vclassURI, String subclassURI) {
        Classes2Classes c2c = new Classes2Classes();
        c2c.setSubclassURI(subclassURI);
        c2c.setSuperclassURI(vclassURI);
        deleteClasses2Classes(c2c);
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
            String uri = it.next();
            if (!subtree.contains(uri)) {
                subtree.add(uri);
                getAllSubClassURIs(uri,subtree);
            }
        }
    }

    public List<String> getAllSuperClassURIs(String classURI) {

        List<String> superclassURIs = null;

        if (isUnderlyingStoreReasoned()) {	
            superclassURIs = new ArrayList<String>();
            Resource cls = ResourceFactory.createResource(classURI);
            StmtIterator superClassIt = getOntModel().listStatements(
                    cls, RDFS.subClassOf, (RDFNode)null);
            while (superClassIt.hasNext()) {
                Statement stmt = superClassIt.nextStatement();
                if (stmt.getObject().canAs(OntResource.class)) {
                    OntResource superRes = stmt.getObject().as(OntResource.class);
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
            String uri = it.next();
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
            ClosableIterator<Individual> classIt = getOntModel().listIndividuals(OWL.Class);
            try {
                while (classIt.hasNext()) {
                    try {
                        Individual classInd = classIt.next();
                        if(classInd.canAs(OntClass.class)) {
                            OntClass cls = classInd.as(OntClass.class);
                            if (!cls.isAnon() && !(NONUSER_NAMESPACES.contains(cls.getNameSpace()))) {
                                classes.add(new VClassJena(cls,getWebappDaoFactory()));
                            }
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

    /** 
     * The basic idea here is that we ignore anonymous superclasses for the purpose 
     * of determining whether something is a root class.
     * We also avoid ClassCastExceptions deep in Jena-land by eschewing Jena's 
     * listSuperClasses() method.
     */
    private Iterator<OntClass> smarterListHierarchyRootClasses(OntModel ontModel, String ontologyURI) {
        List<OntClass> rootClassList = new ArrayList<OntClass>();
        ResIterator ci = ontModel.listResourcesWithProperty(RDF.type, OWL.Class);
        try {
            for (ResIterator i = ci ; i.hasNext(); ) {
                try {
                    Resource ontClass = i.nextResource();
                    boolean isRoot = true;
                    for (StmtIterator j = ontClass.listProperties(RDFS.subClassOf); j.hasNext(); ) {
                        Statement stmt = j.nextStatement();
                        if (!stmt.getObject().isResource()) {
                            continue;
                        }
                        Resource superClass = (Resource) stmt.getObject();
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
                    if (isRoot && ontClass.canAs(OntClass.class)) {
                        rootClassList.add(ontClass.as(OntClass.class));
                    }
                } catch (ClassCastException cce) {
                    log.error(cce, cce);
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
            Iterator<Resource> ontClassIt = ontModel.listResourcesWithProperty(
                    RDF.type, OWL.Class);
            while (ontClassIt.hasNext()) {
                Resource ontClass = ontClassIt.next();
                if (ontologyURI.equals(ontClass.getNameSpace())) {
                    boolean root = true;
                    StmtIterator superStmtIt = ontModel.listStatements(
                            ontClass, RDFS.subClassOf, (RDFNode) null);
                    try {
                        while (superStmtIt.hasNext()) {
                            Statement superStmt = superStmtIt.nextStatement();
                            if ( superStmt.getObject().isResource() 
                                    && ontologyURI.equals(
                                            ((Resource) superStmt.getObject())
                                            .getNameSpace()) ) {
                                root = false;
                                break;
                            }
                        }
                    } finally {
                        superStmtIt.close();
                    }
                    if (root && ontClass.canAs(OntClass.class)) {
                        ontologyRootClasses.add(new VClassJena(
                                (OntClass) ontClass.as(OntClass.class), 
                                getWebappDaoFactory()));
                    }
                }
            }
        } finally {
            ontModel.leaveCriticalSection();
        }
        return ontologyRootClasses;
    }

    public List <String> getSubClassURIs(String classURI) {
        List<String> subURIs = new ArrayList<String>();
        OntClass superClass = getOntClass(getOntModel(),classURI);
        try {
            Iterator<OntClass> subIt = superClass.listSubClasses(true);
            while (subIt.hasNext()) {
                OntClass cls = subIt.next();
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
        List<String> supURIs = new ArrayList<String>();
        OntClass subClass = getOntClass(getOntModel(), classURI);
        try {
            Iterator<OntClass> supIt = subClass.listSuperClasses(direct);
            while (supIt.hasNext()) {
                OntClass cls = (OntClass) supIt.next();
                supURIs.add(getClassURIStr(cls));
            }
        } catch (Exception e) {
            log.debug(e,e);
            // we'll try this again using a different method 
            // that doesn't try to convert to OntClass
            supURIs.clear();
            List<Resource> supList = (direct) 
                    ? listDirectObjectPropertyValues(subClass, RDFS.subClassOf)
                    : listObjectPropertyValues(subClass, RDFS.subClassOf);
            for (Resource res : supList) {
                supURIs.add(getClassURIStr(res));
            }
        }
        return supURIs;
    }

    private List<Resource> listObjectPropertyValues(Resource res, Property prop) {
        List<Resource> values = new ArrayList<Resource>();
        StmtIterator stmtIt = res.listProperties(prop);
        while (stmtIt.hasNext()) {
            Statement s = stmtIt.nextStatement();
            if (s.getObject().isResource()) {
                values.add(s.getObject().asResource());
            }
        }
        return values;
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
                    List<String> superproperties = getWebappDaoFactory()
                            .getObjectPropertyDao()
                            .getSuperPropertyURIs(propertyURI, false);
                    superproperties.add(propertyURI);
                    HashSet<String> subjSuperclasses = new HashSet<String>(
                            getAllSuperClassURIs(vclassURI));
                    subjSuperclasses.add(vclassURI); 
                    for (String objectPropertyURI : superproperties) {
                        for (Iterator restStmtIt = getOntModel().listStatements(
                                null,OWL.onProperty,getOntModel().getProperty(objectPropertyURI)); restStmtIt.hasNext();) {
                            Statement restStmt = (Statement) restStmtIt.next();
                            Resource restRes = restStmt.getSubject();	            		
                            for (Iterator axStmtIt = getOntModel().listStatements(null,null,restRes); axStmtIt.hasNext();) {
                                Statement axStmt = (Statement) axStmtIt.next();
                                OntResource subjOntRes = null;
                                if (axStmt.getSubject().canAs(OntResource.class)) {
                                    subjOntRes = axStmt.getSubject().as(OntResource.class);
                                }
                                if (
                                        (subjOntRes != null) && (subjSuperclasses.contains(getClassURIStr(subjOntRes))) &&
                                        (axStmt.getPredicate().equals(RDFS.subClassOf) || (axStmt.getPredicate().equals(OWL.equivalentClass)))	
                                        ) {
                                    if (restRes.canAs(AllValuesFromRestriction.class)) {
                                        AllValuesFromRestriction avfRest = restRes.as(AllValuesFromRestriction.class);
                                        Resource avf = avfRest.getAllValuesFrom();
                                        if (avf.canAs(OntClass.class)) {
                                            superclass = avfRest.getAllValuesFrom().as(OntClass.class);
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
                    	//this section to prevent all subclasses of owl:Thing
                        //returned if range is owl:Thing, refer to NIHVIVO-3357 NIHVIVO-3814
                    	//This is unfortunate case of warping the model for the ease of the display.
                    	return Collections.emptyList();                        
                    }
                }
                if (superclass != null) {
                    VClass superVclass;
                    if (superclass.isAnon()) {
                        superVclass = getVClassByURI(getClassURIStr(superclass));
                    } else {
                        superVclass = getVClassByURI(superclass.getURI());
                    }
                    if( OWL.Thing.equals( superclass )){
                    	//this section to prevent all subclasses of owl:Thing
                        //returned if range is owl:Thing, refer to NIHVIVO-3357 NIHVIVO-3814
                    	//This is unfortunate case of warping the model for the ease of the display.
                    	return Collections.emptyList();
                    }
                    if (superVclass != null) {
                        vClasses.add(superVclass);                                                                       
						// if this model infers types based on the taxonomy, adding the subclasses will only
						// waste time for no benefit
						if (!isUnderlyingStoreReasoned()) {
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
                                Resource cls = annot.getSubject();
                                VClass vcw = getVClassByURI(cls.getURI());
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
            String errMsgStr = getWebappDaoFactory().checkURIForEditableEntity(cls.getURI());
            if (errMsgStr != null) {
                throw new InsertException(errMsgStr);
            }
            OntClass ontCls = ontModel.createClass(cls.getURI());
            try {
                if (cls.getName() != null && cls.getName().length() > 0) {
                    ontCls.setLabel(cls.getName(), getDefaultLanguage());
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

            ontCls.removeAll(HIDDEN_FROM_PUBLISH_BELOW_ROLE_LEVEL_ANNOT);
            if (HIDDEN_FROM_PUBLISH_BELOW_ROLE_LEVEL_ANNOT != null && cls.getHiddenFromPublishBelowRoleLevel() != null) { // only need to add if present
                try {
                    ontCls.addProperty(HIDDEN_FROM_PUBLISH_BELOW_ROLE_LEVEL_ANNOT, ResourceFactory.createResource(cls.getHiddenFromPublishBelowRoleLevel().getURI()));
                } catch (Exception e) {
                    log.error("error adding HiddenFromPublishBelowRoleLevel annotation to class "+cls.getURI());
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

                if (cls.getHiddenFromPublishBelowRoleLevel() != null) {
                    updatePropertyResourceURIValue(ontCls,HIDDEN_FROM_PUBLISH_BELOW_ROLE_LEVEL_ANNOT,cls.getHiddenFromPublishBelowRoleLevel().getURI(),ontModel);
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

    public void deleteClasses2Classes( Classes2Classes c2c ) {
        deleteClasses2Classes(c2c, getOntModelSelector().getTBoxModel());
    }

    public void deleteClasses2Classes( Classes2Classes c2c, OntModel ontModel )
    {
        ontModel.enterCriticalSection(Lock.WRITE);
        ontModel.getBaseModel().notifyEvent(new EditEvent(getWebappDaoFactory().getUserURI(),true));
        try {
            OntResource subclass = getOntClass(ontModel,c2c.getSubclassURI());
            OntResource superclass = getOntClass(ontModel,c2c.getSuperclassURI());
            if(subclass == null || superclass == null) {
                log.warn("unable to delete " + c2c.getSubclassURI() + 
                        " rdfs:subClassOf " + c2c.getSuperclassURI());
                if (subclass == null) {
                    log.warn(c2c.getSubclassURI() + " not found in the model.");
                }
                if (superclass == null) {
                    log.warn(c2c.getSuperclassURI() + " not found in the model.");
                }
                return;
            }
            Model removal = ModelFactory.createDefaultModel();
            Model additions = ModelFactory.createDefaultModel(); // to repair any rdf:Lists
            removal.add(ontModel.listStatements(subclass, RDFS.subClassOf, superclass));
            if (subclass.isAnon()) {
                Model[] changeSet = getSmartRemoval(subclass, getOntModel());
                removal.add(changeSet[0]);
                additions.add(changeSet[1]);
            }
            if (superclass.isAnon()) {
                Model[] changeSet = getSmartRemoval(superclass, getOntModel());
                removal.add(changeSet[0]);
                additions.add(changeSet[1]);
            }
            ontModel.remove(removal);
            ontModel.add(additions);
        } finally {
            ontModel.getBaseModel().notifyEvent(new EditEvent(getWebappDaoFactory().getUserURI(),false));
            ontModel.leaveCriticalSection();
        }
    }

    public void insertNewClasses2Classes( Classes2Classes c2c ) {
        insertNewClasses2Classes(c2c, getOntModelSelector().getTBoxModel());
    }

    public void insertNewClasses2Classes( Classes2Classes c2c, OntModel ontModel )
    {
        ontModel.enterCriticalSection(Lock.WRITE);
        ontModel.getBaseModel().notifyEvent(new EditEvent(getWebappDaoFactory().getUserURI(),true));
        try {
            Resource subclass = ontModel.getResource(c2c.getSubclassURI());
            Resource superclass = ontModel.getResource(c2c.getSuperclassURI());
            if ((subclass != null) && (superclass != null)) {
                ontModel.add(subclass, RDFS.subClassOf, superclass);
            }
        } finally {
            ontModel.getBaseModel().notifyEvent(new EditEvent(getWebappDaoFactory().getUserURI(),false));
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
            Resource oc1 = ontModel.getResource(vclassURI1);
            Resource oc2 = ontModel.getResource(vclassURI2);
            return ontModel.contains(oc1, RDFS.subClassOf, oc2);
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

}
