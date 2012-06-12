/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.mindswap.pellet.jena.vocabulary.SWRL;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.appConfig.context.AppConfigContextService;
import edu.cornell.mannlib.vitro.webapp.dao.appConfig.jena.AppConfigContextServiceJena;

public class JenaBaseDao extends JenaBaseDaoUtils {

	public static final boolean KEEP_ONLY_IF_TRUE = true; //used for updatePropertyBooleanValue()
    public static final boolean KEEP_ONLY_IF_FALSE = false; //used for updatePropertyBooleanValue()
    
    public static final String JENA_ONT_MODEL_ATTRIBUTE_NAME = "jenaOntModel";
    public static final String ASSERTIONS_ONT_MODEL_ATTRIBUTE_NAME = "baseOntModel";
    public static final String INFERENCE_ONT_MODEL_ATTRIBUTE_NAME = "inferenceOntModel";
    
    protected String PSEUDO_BNODE_NS = VitroVocabulary.PSEUDO_BNODE_NS;
    
    protected String XSD = "http://www.w3.org/2001/XMLSchema#";
    private OntModelSelector ontModelSelector;
    private List<OntModel> writableOntModelList;
    private WebappDaoFactoryJena webappDaoFactory;

    private AppConfigContextService appConfigContextService;
    
    /* ******************* protected variables *************** */

    protected String DEFAULT_NAMESPACE;
    protected Set<String> NONUSER_NAMESPACES;
    protected List<String> PREFERRED_LANGUAGES;

    /* ******************* constructor ************************* */
    
    public JenaBaseDao(WebappDaoFactoryJena wadf) {
    	this.ontModelSelector = wadf.getOntModelSelector();
    	this.DEFAULT_NAMESPACE = wadf.getDefaultNamespace();
    	this.NONUSER_NAMESPACES = wadf.getNonuserNamespaces();
    	this.PREFERRED_LANGUAGES = wadf.getPreferredLanguages();
    	this.webappDaoFactory = wadf;
    	this.appConfigContextService = new AppConfigContextServiceJena(
    			wadf.getOntModelSelector().getDisplayModel(), 
    			wadf.config.getLocalAppNamespace());
    }

    /* ******************** accessors ************************** */

    protected OntModel getOntModel() {
        return ontModelSelector.getFullModel();
    }
    
    protected OntModelSelector getOntModelSelector() {
    	return ontModelSelector;
    }

    protected List<OntModel> getWritableOntModelList() {
        return writableOntModelList;
    }

    protected WebappDaoFactoryJena getWebappDaoFactory() {
        return webappDaoFactory;
    }

    /* ********** convenience methods for children ************* */

    
    /**
     * convenience method for updating the RDFS label
     */
    protected void updateRDFSLabel(OntResource ontRes, String label) {
    	
    	if (label != null && label.length() > 0) {
    		
    		String existingValue = ontRes.getLabel((String) getDefaultLanguage());
    	    
    		if (existingValue == null || !existingValue.equals(label)) {
    			ontRes.setLabel(label, (String) getDefaultLanguage());	
    	    }
    	} else {
    		ontRes.removeAll(RDFS.label);
    	}
    }
    
    private final boolean ALSO_TRY_NO_LANG = true;
    
    /**
     * Get the rdfs:label or vitro:label, working through PERFERED_LANGUAGES,
     * or get local name, bnode Id, or full URI if no labels found.
     */
    protected String getLabelOrId(OntResource r) {
    	String label = null;
    	r.getOntModel().enterCriticalSection(Lock.READ);
    	try {
    		label = getLabel(r);
    		if( label == null || label.length() == 0 )
    		    label = getLocalNameOrId(r);
    	} finally {
    		r.getOntModel().leaveCriticalSection();
    	}
        return label;
    }
    
    protected String getLabel(OntResource r){
        String label = null;
        Literal labelLiteral = getLabelLiteral(r);
        if (labelLiteral != null) {
            label = labelLiteral.getLexicalForm();
        }
        return label;
    }
    
    protected Literal getLabelLiteral(String individualUri) {
        OntResource resource = webappDaoFactory.getOntModel().createOntResource(individualUri);
        return getLabelLiteral(resource);
    }

    /**
     * works through list of PREFERRED_LANGUAGES to find an appropriate 
     * label, or NULL if not found.  
     */
    protected Literal getLabelLiteral(OntResource r) {
        Literal labelLiteral = null;
        r.getOntModel().enterCriticalSection(Lock.READ);
        try {            
            // try rdfs:label with preferred languages
            labelLiteral = tryPropertyForPreferredLanguages( r, RDFS.label, ALSO_TRY_NO_LANG );
            // try vitro:label with preferred languages
            // Commenting out for NIHVIVO-1962
           /* if ( label == null ) {
                labelLiteral = tryPropertyForPreferredLanguages( r, r.getModel().getProperty(VitroVocabulary.label), ALSO_TRY_NO_LANG );
            }   */          
        } finally {
            r.getOntModel().leaveCriticalSection();
        }
        return labelLiteral;        
    }
    
    private Literal tryPropertyForPreferredLanguages( OntResource r, Property p, boolean alsoTryNoLang ) {
    	Literal label = null;
	    List<RDFNode> labels = r.listPropertyValues(p).toList();
	    
	    if (labels.size() == 0) {
	        return null;
	    }

	    // Sort by lexical value to guarantee consistent results
	    Collections.sort(labels, new Comparator<RDFNode>() {
	        public int compare(RDFNode left, RDFNode right) {
	            if (left == null) {
	                return (right == null) ? 0 : -1;
	            }
	            if ( left.isLiteral() && right.isLiteral()) {
	                return ((Literal) left).getLexicalForm().compareTo(((Literal) right).getLexicalForm());
	            } 
	            // Can't sort meaningfully if both are not literals
	            return 0;	            
	        }
	    });
	    
	    for (String lang : PREFERRED_LANGUAGES) {
	    	label = getLabel(lang,labels);
	    	if (label != null) {
	    		break;
	    	}
	    }
        if ( label == null && alsoTryNoLang ) {
        	label = getLabel("", labels);
        	// accept any label as a last resort
        	if (label == null) {
        	    for (RDFNode labelNode : labels) {
        	      if (labelNode instanceof Literal) {
        	          label = ((Literal) labelNode);
        	          break;
        	      }
        	    }
        	}
        }
	    return label;
    }

    protected String getDefaultLanguage() {
        return PREFERRED_LANGUAGES.get(0);
    }
    
    public static boolean isBooleanClassExpression(OntClass cls) {
    	return (cls.isComplementClass() || cls.isIntersectionClass() || cls.isUnionClass());
    }
    
    protected OntClass getOntClass(OntModel ontModel, String vitroURIStr) {
    	ontModel.enterCriticalSection(Lock.READ);
    	try {
    		OntClass cls = null;
    		if (vitroURIStr==null)
    			return null;
    		if (vitroURIStr.indexOf(PSEUDO_BNODE_NS)==0) {
    			String idStr = vitroURIStr.split("#")[1];
    			RDFNode rdfNode = ontModel.getRDFNode(Node.createAnon(AnonId.create(idStr)));
    			if ( (rdfNode != null) && (rdfNode.canAs(OntClass.class)) ) {
    				cls = (OntClass) rdfNode.as(OntClass.class);
    			}
			} else {
				try {
					cls = ontModel.getOntClass(vitroURIStr);
				} catch (Exception e) {
					cls = null;
				}
			}
    		return cls;
    	} finally {
    		ontModel.leaveCriticalSection();
    	}
    }
    
    protected String getClassURIStr(Resource cls) {
    	if (cls.isAnon()) {
    		return PSEUDO_BNODE_NS+cls.getId().toString();
    	} else {
    		return cls.getURI();
    	}
    }

    protected Node makeNodeForURI(String vitroURIStr) {
    	if (vitroURIStr.indexOf(PSEUDO_BNODE_NS)==0) {
			return Node.createAnon(AnonId.create(vitroURIStr.split("#")[1]));
    	} else {
    		return Node.createURI(vitroURIStr);
    	}
    }
    
    protected List<Resource> listDirectObjectPropertyValues(Resource subj, Property prop) {
    	// This is a quick and dirty algorithm for getting direct property values.
    	// It will only work properly if the full transitive closure is present in the graph;
    	// Otherwise, it will include additional values that are not strictly direct values.
    	Set<Resource> possibleValueSet = new HashSet<Resource>();
    	List<Resource> directValueList = new ArrayList<Resource>();
    	// List all of the property values
    	StmtIterator stmtIt = getOntModel().listStatements(subj, prop, (RDFNode)null);
    	while (stmtIt.hasNext()) {
    		Statement stmt = stmtIt.nextStatement();
    		if (stmt.getObject().isResource()) {
    			possibleValueSet.add((Resource)stmt.getObject());
    		}
    	}
    	// Now for each value, work backwards and see if it has an alternate path to the original resource.
    	// If not, add it to the list of direct values.
    	Iterator<Resource> possibleValueIt = possibleValueSet.iterator();
    	while (possibleValueIt.hasNext()) {
    		Resource possibleRes = possibleValueIt.next();
    		StmtIterator pStmtIt = getOntModel().listStatements((Resource)null, prop, possibleRes);
    		boolean hasAlternatePath = false;
        	while (stmtIt.hasNext()) {
        		Statement stmt = stmtIt.nextStatement();
        		if (possibleValueSet.contains(stmt.getSubject())) {
        			hasAlternatePath = true;
        			break;
        		}
        	}
        	if (!hasAlternatePath) {
        		directValueList.add(possibleRes);
        	}
    	}
    	return directValueList;
    }
    
    // the same thing as the previous method but going the other direction
    protected List<Resource> listDirectObjectPropertySubjects(Resource value, Property prop) {
    	Set<Resource> possibleSubjectSet = new HashSet<Resource>();
    	List<Resource> directSubjectList = new ArrayList<Resource>();
    	StmtIterator stmtIt = getOntModel().listStatements((Resource)null, prop, value);
    	while (stmtIt.hasNext()) {
    		Statement stmt = stmtIt.nextStatement();
    		possibleSubjectSet.add((Resource)stmt.getSubject());
    		
    	}
    	Iterator<Resource> possibleSubjectIt = possibleSubjectSet.iterator();
    	while (possibleSubjectIt.hasNext()) {
    		Resource possibleRes = possibleSubjectIt.next();
    		StmtIterator pStmtIt = getOntModel().listStatements(possibleRes, prop, (RDFNode)null);
    		boolean hasAlternatePath = false;
        	while (stmtIt.hasNext()) {
        		Statement stmt = stmtIt.nextStatement();
        		if (stmt.getObject().isResource() && possibleSubjectSet.contains((Resource)stmt.getObject())) {
        			hasAlternatePath = true;
        			break;
        		}
        	}
        	if (!hasAlternatePath) {
        		directSubjectList.add(possibleRes);
        	}
    	}
    	return directSubjectList;
    }    
 
    public void removeRulesMentioningResource(Resource res, OntModel ontModel) {
    	Iterator<Resource> impIt = ontModel.listSubjectsWithProperty(RDF.type, SWRL.Imp);
    	while (impIt.hasNext()) {
    		Resource imp = impIt.next();
    		boolean removeMe = false;
    		Model description = describeResource(imp, ontModel);
    		NodeIterator objIt = description.listObjects();
    		try {
	    		while(objIt.hasNext()) {
	    			RDFNode obj = objIt.nextNode();
	    			if (obj.equals(res)) {
	    				removeMe = true;
	    			}
	    		}
    		} finally {
    			objIt.close();
    		}
    		if (removeMe) {
    			ontModel.remove(description);
    		}	
    	}
    }    
  
}
