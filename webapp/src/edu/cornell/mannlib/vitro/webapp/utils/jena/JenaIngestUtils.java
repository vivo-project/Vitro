/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.jena;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.FunctionalProperty;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
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
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.ResourceUtils;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.InsertException;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

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
                                ResourceUtils.renameResource(outRes,getNextURI(namespaceEtc, dedupUnionModel));
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

    public Model renameBNodesByPattern(Model inModel, String namespaceEtc, Model dedupModel, String pattern, String property){
        Model outModel = ModelFactory.createDefaultModel();
        Property propertyRes = ResourceFactory.createProperty(property);
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
                        ClosableIterator closfIt = outModel.listStatements(res,propertyRes,(RDFNode)null);
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
                            if(stmt.getObject().isLiteral()){
                                String value = ((Literal) stmt.getObject()).getLexicalForm();
                                String suffix = (pattern.contains("$$$")) 
                                        ? pattern.replace("$$$", value)
                                                : pattern + value;
                                        ResourceUtils.renameResource(outRes, namespaceEtc + suffix);
                            }
                            doneSet.add(res.getId().toString());
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

    public Map<String, LinkedList<String>> generatePropertyMap(List<Model> sourceModels, ModelMaker maker){
        Map<String,LinkedList<String>> propertyMap = Collections.synchronizedMap(new HashMap<String, LinkedList<String>>());
        Set<String> doneList = new HashSet<String>();
        for(Model model : sourceModels) {
            ClosableIterator cItr = model.listSubjects();
            while(cItr.hasNext()){
                Resource res = (Resource) cItr.next();
                if(res.isAnon() && !doneList.contains(res.getId())){    
                    doneList.add(res.getId().toString());
                    StmtIterator stmtItr = model.listStatements(res, (Property)null, (RDFNode)null);
                    while(stmtItr.hasNext()){
                        Statement stmt = stmtItr.next();
                        if(!stmt.getObject().isResource()){
                            if(propertyMap.containsKey(stmt.getPredicate().getURI())){
                                LinkedList linkList = propertyMap.get(stmt.getPredicate().getURI());
                                linkList.add(stmt.getObject().toString());        
                            }
                            else{
                                propertyMap.put(stmt.getPredicate().getURI(), new LinkedList());
                                LinkedList linkList = propertyMap.get(stmt.getPredicate().getURI());
                                linkList.add(stmt.getObject().toString());                        
                            }                                    
                        }                                
                    }
                }
            }
            cItr = model.listObjects();
            while(cItr.hasNext()){
                RDFNode rdfn = (RDFNode) cItr.next();
                if(rdfn.isResource()){
                    Resource res = (Resource)rdfn;
                    if(res.isAnon() && !doneList.contains(res.getId())){
                        doneList.add(res.getId().toString());
                        StmtIterator stmtItr = model.listStatements(res, (Property)null, (RDFNode)null);
                        while(stmtItr.hasNext()){
                            Statement stmt = stmtItr.next();
                            if(!stmt.getObject().isResource()){
                                if(propertyMap.containsKey(stmt.getPredicate().getURI())){
                                    LinkedList linkList = propertyMap.get(stmt.getPredicate().getURI());
                                    linkList.add(stmt.getObject().toString());                    
                                }
                                else{
                                    propertyMap.put(stmt.getPredicate().getURI(), new LinkedList());
                                    LinkedList linkList = propertyMap.get(stmt.getPredicate().getURI());
                                    linkList.add(stmt.getObject().toString());    
                                }
                            }
                        }
                    }
                }
            }
            cItr.close();
        }
        return propertyMap;
    }

    private String getNextURI(String namespaceEtc, Model model) {
        String nextURI = null;
        boolean duplicate = true;
        while (duplicate) {
            nextURI = namespaceEtc+random.nextInt(9999999);
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
            log.error(e, e);
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
                        log.error(e, e);
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
        outModel.add(inModel);
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
                                continue;
                            }

                            ClosableIterator closgIt = inModel.listStatements(subj,(Property)null,(RDFNode)null);
                            try {
                                for (Iterator stmtIt = closgIt; stmtIt.hasNext();) {
                                    Statement stmt = (Statement) stmtIt.next();
                                    outModel.remove(stmt.getSubject(), stmt.getPredicate(), stmt.getObject());
                                    outModel.add(smushToThisResource, stmt.getPredicate(), stmt.getObject());
                                }
                            } finally {
                                closgIt.close();
                            }
                            closgIt = inModel.listStatements((Resource) null, (Property)null, subj);
                            try {
                                for (Iterator stmtIt = closgIt; stmtIt.hasNext();) {
                                    Statement stmt = (Statement) stmtIt.next();
                                    outModel.remove(stmt.getSubject(), stmt.getPredicate(), stmt.getObject());
                                    outModel.add(stmt.getSubject(), stmt.getPredicate(), smushToThisResource);
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

    public class MergeResult {
        private String resultText;
        private Model leftoverModel;

        public MergeResult() {}

        public MergeResult(String resultText, Model leftoverModel) {
            this.resultText = resultText;
            this.leftoverModel = leftoverModel;
        }

        public void setResultText(String resultText) {
            this.resultText = resultText;
        }

        public String getResultText() {
            return this.resultText;
        }

        public void setLeftoverModel(Model leftoverModel) {
            this.leftoverModel = leftoverModel;
        }

        public Model getLeftoverModel() {
            return this.leftoverModel;
        }    
    }

    public MergeResult doMerge(String uri1, String uri2, OntModel baseOntModel, 
            OntModel tboxOntModel, String usePrimaryLabelOnly){

        boolean functionalPresent = false;

        Resource res1 = baseOntModel.getResource(uri1); // primary resource
        Model res1Model = ModelFactory.createDefaultModel();        
        Resource res2 = baseOntModel.getResource(uri2); // secondary resource
        Model res2Model = ModelFactory.createDefaultModel();

        // get statements of both the resources
        baseOntModel.enterCriticalSection(Lock.READ);
        try {        
            res1Model.add(
                    baseOntModel.listStatements(res1, (Property)null, (RDFNode)null));
            res2Model.add(
                    baseOntModel.listStatements(res2, (Property)null, (RDFNode)null));
            res2Model.add(
                    baseOntModel.listStatements(
                            (Resource)null, (Property)null, (RDFNode)res2));
        } finally {
            baseOntModel.leaveCriticalSection();
        }

        // if primary resource has no statements, return
        if (res1Model.isEmpty()){
            return new MergeResult("resource 1 not present", null);
        } else if(res2Model.isEmpty()){
            return new MergeResult("resource 2 not present", null);
        }

        int counter = 0;
        Model leftoverModel = ModelFactory.createDefaultModel();

        // Iterate through statements of secondary resource
        StmtIterator stmtItr2 = res2Model.listStatements(
                res2, (Property) null, (RDFNode) null);            
        while(stmtItr2.hasNext()){
            Statement stmt = stmtItr2.nextStatement();            
            if(isFunctional(stmt.getPredicate(), tboxOntModel)) {
                // if the property is null or functional then dump the statement into 
                // the leftover model, else add it to base, ont and inf models as a 
                // part of the primary resource.
                leftoverModel.add(res2, stmt.getPredicate(), stmt.getObject());
                functionalPresent = true;
            } else if (stmt.getPredicate().equals(RDFS.label) &&
                    usePrimaryLabelOnly!=null && !usePrimaryLabelOnly.isEmpty()) {
                // if the checkbox is checked, use primary resource rdfs:labels only  
                // and dump secondary resource rdfs:labels into leftoverModel
                leftoverModel.add(res2, stmt.getPredicate(), stmt.getObject());
                functionalPresent = true;        
            } else {
                baseOntModel.enterCriticalSection(Lock.WRITE);
                try {
                    baseOntModel.add(res1, stmt.getPredicate(), stmt.getObject());
                    counter++;
                } finally {
                    baseOntModel.leaveCriticalSection();
                }
            }
        }

        // replace secondary resource with primary resource in all the statements 
        // where secondary resource is present as an object.
        StmtIterator stmtItr3 = res2Model.listStatements(
                (Resource) null, (Property) null, res2);
        while (stmtItr3.hasNext()){
            Statement stmt = stmtItr3.nextStatement();
            Resource sRes = stmt.getSubject();
            Property sProp = stmt.getPredicate();
            baseOntModel.enterCriticalSection(Lock.WRITE);
            try {
                baseOntModel.add(sRes, sProp, res1);
                counter++;
            } finally {
                baseOntModel.leaveCriticalSection();
            }
        }

        // Remove all the statements of secondary resource 
        baseOntModel.enterCriticalSection(Lock.WRITE);
        try {
            baseOntModel.remove(res2Model);
        } finally {
            baseOntModel.leaveCriticalSection();
        }

        MergeResult result = new MergeResult();
        if (!leftoverModel.isEmpty()) {
            result.setLeftoverModel(leftoverModel);   
        }

        if (counter > 0 && functionalPresent) {
            result.setResultText("merged " + counter + 
                    " statements. Some statements could not be merged.");
        } else if(counter>0 && !functionalPresent) {
            result.setResultText("merged " + counter + " statements.");    
        } else if (counter==0) {
            result.setResultText("No statements merged");
        }
        return result;

    }
    
    private boolean isFunctional(Property property, OntModel tboxOntModel) {
        tboxOntModel.enterCriticalSection(Lock.READ);
        try {
            return (tboxOntModel.contains(
                    property, RDF.type, OWL.FunctionalProperty));
        } finally {
            tboxOntModel.leaveCriticalSection();
        }
    }

    public void doPermanentURI(String oldModel, String newModel, String oldNamespace,
            String newNamespace, String dNamespace, ModelMaker maker, 
            VitroRequest vreq) {

        WebappDaoFactory wdf = vreq.getFullWebappDaoFactory();
        Model m = maker.getModel(oldModel);
        Model saveModel = maker.getModel(newModel);
        Model tempModel = ModelFactory.createDefaultModel();
        ResIterator rsItr = null;
        ArrayList<String> urlCheck = new ArrayList<String>();
        String changeNamespace = null;
        boolean urlFound = false;
        if(!oldModel.equals(newModel)){
            StmtIterator stmtItr = m.listStatements();
            while(stmtItr.hasNext()){
                Statement stmt = stmtItr.nextStatement();
                tempModel.add(stmt);
            }
            rsItr = tempModel.listResourcesWithProperty((Property)null);
        } else{
            rsItr = m.listResourcesWithProperty((Property)null); 
        }

        String uri = null;  
        while(rsItr.hasNext()){
            Resource res = rsItr.next();
            if(oldNamespace.equals(res.getNameSpace())){
                if(!newNamespace.equals("")){
                    do{
                        uri = getUnusedURI(newNamespace,wdf);
                        if(!urlCheck.contains(uri)){
                            urlCheck.add(uri);
                            urlFound = true;
                        }
                    }while(!urlFound);
                    urlFound = false;
                }
                else if(dNamespace.equals(vreq.getFullWebappDaoFactory().getDefaultNamespace())){
                    try{
                        do{
                            uri = wdf.getIndividualDao().getUnusedURI(null);
                            if(!urlCheck.contains(uri)){
                                urlCheck.add(uri);
                                urlFound = true;
                            }
                        }while(!urlFound);
                        urlFound = false;
                    }catch(InsertException ex){
                        log.error("could not create uri");
                    }           
                }
                ResourceUtils.renameResource(res, uri);                    
            }

        }
        boolean statementDone = false;
        if(!newNamespace.equals("")){
            changeNamespace = newNamespace;
        }
        else if(dNamespace.equals(vreq.getFullWebappDaoFactory().getDefaultNamespace())){
            changeNamespace = dNamespace;
        }
        if(!oldModel.equals(newModel)){
            StmtIterator stmtItr = tempModel.listStatements();
            while(stmtItr.hasNext()){
                statementDone = false;
                Statement stmt = stmtItr.nextStatement();
                Resource sRes = stmt.getSubject();
                Resource oRes = null;
                if(sRes.getNameSpace().equals(changeNamespace)){
                    saveModel.add(stmt);
                    statementDone = true;
                }
                try{
                    oRes = (Resource)stmt.getObject();
                    if(oRes.getNameSpace().equals(changeNamespace) && !statementDone){
                        saveModel.add(stmt);
                        statementDone = true;
                    }    
                }
                catch(Exception e){
                    continue;
                }
            }
        }
    }    

    public String getUnusedURI(String newNamespace,WebappDaoFactory wdf){
        String uri = null;
        String errMsg = null;
        Random random = new Random();
        boolean uriIsGood = false;
        int attempts = 0;

        while( uriIsGood == false && attempts < 30 ){            
            uri = newNamespace + "n" + random.nextInt( Math.min(Integer.MAX_VALUE,(int)Math.pow(2,attempts + 13)) );            
            errMsg = wdf.checkURI(uri);
            if(  errMsg != null)
                uri = null;
            else
                uriIsGood = true;                
            attempts++;
        }   

        return uri;
    }

}
