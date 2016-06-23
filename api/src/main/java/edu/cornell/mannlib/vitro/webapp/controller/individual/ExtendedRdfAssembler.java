/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.individual;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.FULL_ASSERTIONS;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.OntModel;
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
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.RdfResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.utils.jena.ExtendedLinkedDataUtils;
import edu.cornell.mannlib.vitro.webapp.utils.jena.JenaOutputUtils;
import edu.cornell.mannlib.vitro.webapp.web.ContentType;

/**
 * TODO Keep this around until release 1.7, in case anyone is relying on it.
 */
@Deprecated
public class ExtendedRdfAssembler {
	private static final Log log = LogFactory
			.getLog(ExtendedRdfAssembler.class);
	
    private static final String RICH_EXPORT_ROOT = "/WEB-INF/rich-export/";
    private static final String PERSON_CLASS_URI = "http://xmlns.com/foaf/0.1/Person";
    private static final String INCLUDE_ALL = "all";

    @SuppressWarnings("serial")
    private static final Map<String, String> namespaces = new HashMap<String, String>() {{
    	put("display", VitroVocabulary.DISPLAY);
    	put("vitro", VitroVocabulary.vitroURI);
    	put("vitroPublic", VitroVocabulary.VITRO_PUBLIC);
    }};
    
	private static final Property extendedLinkedDataProperty = ResourceFactory.createProperty(namespaces.get("vitro") + "extendedLinkedData");
	private static final Literal xsdTrue = ResourceFactory.createTypedLiteral("true", XSDDatatype.XSDboolean);
	
	private final VitroRequest vreq;
	private final ServletContext ctx;
	private final Individual individual;
	private final ContentType rdfFormat;

	public ExtendedRdfAssembler(VitroRequest vreq, Individual individual,
			ContentType rdfFormat) {
		this.vreq = vreq;
		this.ctx = vreq.getSession().getServletContext();
		this.individual = individual;
		this.rdfFormat = rdfFormat;
	}

	/**
	 */
	public ResponseValues assembleRdf() {
		OntModel ontModel = vreq.getJenaOntModel();

		String[] includes = vreq.getParameterValues("include");
		Model newModel = getRDF(individual, ontModel, ModelFactory.createDefaultModel(), 0, includes);
		JenaOutputUtils.setNameSpacePrefixes(newModel, vreq.getWebappDaoFactory());
		return new RdfResponseValues(rdfFormat, newModel);
	}

    private Model getRDF(Individual entity, OntModel contextModel, Model newModel, int recurseDepth, String[] includes) {
    	
    	Resource subj = newModel.getResource(entity.getURI());
    	
    	List<DataPropertyStatement> dstates = entity.getDataPropertyStatements();
    	TypeMapper typeMapper = TypeMapper.getInstance();
    	for (DataPropertyStatement ds: dstates) {
    		Property dp = newModel.getProperty(ds.getDatapropURI());
	    	Literal lit = null;
	        if ((ds.getLanguage()) != null && (ds.getLanguage().length()>0)) {
	        	lit = newModel.createLiteral(ds.getData(),ds.getLanguage());
	        } else if ((ds.getDatatypeURI() != null) && (ds.getDatatypeURI().length()>0)) {
	        	lit = newModel.createTypedLiteral(ds.getData(),typeMapper.getSafeTypeByName(ds.getDatatypeURI()));
	        } else {
	        	lit = newModel.createLiteral(ds.getData());
	        } 
    		newModel.add(newModel.createStatement(subj, dp, lit));
    	}
    	
    	if (recurseDepth < 5) {
	    	List<ObjectPropertyStatement> ostates = entity.getObjectPropertyStatements();
	    	
	    	for (ObjectPropertyStatement os: ostates) {
	    		Property prop = newModel.getProperty(os.getPropertyURI());
	    		Resource obj = newModel.getResource(os.getObjectURI());
	    		newModel.add(newModel.createStatement(subj, prop, obj));
	    		if ( includeInLinkedData(obj, contextModel)) {
	    			newModel.add(getRDF(os.getObject(), contextModel, newModel, recurseDepth + 1, includes));
	    	    } else {
	    	    	contextModel.enterCriticalSection(Lock.READ);
	    			try {
	    				newModel.add(contextModel.listStatements(obj, RDFS.label, (RDFNode)null));
	    			} finally {
	    				contextModel.leaveCriticalSection();
	    			} 
	    	    }
	    	}
    	}
    	
    	newModel = getLabelAndTypes(entity, contextModel, newModel );
		newModel = getStatementsWithUntypedProperties(subj, contextModel,
				ModelAccess.on(vreq).getOntModel(FULL_ASSERTIONS), newModel);
    	
    	//bdc34: The following code adds all triples where entity is the Subject. 
//    	contextModel.enterCriticalSection(Lock.READ);
//		try {
//			StmtIterator iter = contextModel.listStatements(subj, (Property) null, (RDFNode) null);
//			while (iter.hasNext()) {
//				Statement stmt = iter.next();
//				if (!newModel.contains(stmt)) {
//				   newModel.add(stmt);
//				}
//			}  
//		} finally {
//			contextModel.leaveCriticalSection();
//		} 
			
		if (recurseDepth == 0 && includes != null && entity.isVClass(PERSON_CLASS_URI)) {
			
	        for (String include : includes) {
	       
	        	String rootDir = null;
	        	if (INCLUDE_ALL.equals(include)) {
	        		rootDir = RICH_EXPORT_ROOT;
	        	} else {
	        		rootDir = RICH_EXPORT_ROOT +  include + "/";
	        	}
	        	
	        	long start = System.currentTimeMillis();
				Model extendedModel = ExtendedLinkedDataUtils.createModelFromQueries(ctx, rootDir, contextModel, entity.getURI());
	        	long elapsedTimeMillis = System.currentTimeMillis()-start;
	        	log.info("Time to create rich export model: msecs = " + elapsedTimeMillis);
	        	
				newModel.add(extendedModel);
	        }
		}
		
    	return newModel;
    }

    public static boolean includeInLinkedData(Resource object, Model contextModel) {
    	 
       	boolean retval = false;
       	
       	contextModel.enterCriticalSection(Lock.READ);
       	
       	try {
	    	StmtIterator iter = contextModel.listStatements(object, RDF.type, (RDFNode)null);
	    	    	
	    	while (iter.hasNext()) {
	    		Statement stmt = iter.next();
	    		
	    		if (stmt.getObject().isResource() && contextModel.contains(stmt.getObject().asResource(), extendedLinkedDataProperty, xsdTrue)) {
	    			retval = true;
	    		    break;
	    		}	
	    	}
       	} finally {
       		contextModel.leaveCriticalSection();
       	}
    	   	
    	return retval;
    }    

    /* Get the properties that are difficult to get via a filtered WebappDaoFactory. */
    private Model getLabelAndTypes(Individual entity, Model ontModel, Model newModel){
    	for( VClass vclass : entity.getVClasses()){
    		newModel.add(newModel.getResource(entity.getURI()), RDF.type, newModel.getResource(vclass.getURI()));
    	}
    	
    	ontModel.enterCriticalSection(Lock.READ);
		try {
			newModel.add(ontModel.listStatements(ontModel.getResource(entity.getURI()), RDFS.label, (RDFNode)null));
		} finally {
			ontModel.leaveCriticalSection();
		}
		
    	return newModel;
    }
    
    /* This method adds in statements in which the property does not 
     * have an rdf type in the asserted model. 
     * This was added for release 1.5 to handle cases such as the 
     * reasoning-plugin inferred dcterms:creator assertion
     */
    private Model getStatementsWithUntypedProperties(Resource subject, OntModel contextModel, OntModel assertionsModel, Model newModel) {
    	contextModel.enterCriticalSection(Lock.READ);
		try { 	    	
			StmtIterator iter = contextModel.listStatements(subject, (Property) null, (RDFNode) null);
			while (iter.hasNext()) {
				Statement stmt = iter.next();
				Property property = stmt.getPredicate();
		    	assertionsModel.enterCriticalSection(Lock.READ);
				try {
				    if (!assertionsModel.contains(property, RDF.type) && !newModel.contains(stmt)) {	
					   newModel.add(stmt);
				    }
				} finally {
					assertionsModel.leaveCriticalSection();
				} 
			}  
		} finally {
			contextModel.leaveCriticalSection();
		} 
	
    	return newModel;
    }
}
