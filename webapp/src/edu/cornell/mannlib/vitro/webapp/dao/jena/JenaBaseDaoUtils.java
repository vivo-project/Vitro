/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.iri.IRI;
import com.hp.hpl.jena.iri.IRIFactory;
import com.hp.hpl.jena.iri.Violation;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * Class for static utility methods used by jean DAOs
 * 
 */
public class JenaBaseDaoUtils extends JenaBaseDaoCon {

    protected static final Log log = LogFactory.getLog(JenaBaseDaoUtils.class.getName());
    protected static DateTimeFormatter xsdDateFormat = ISODateTimeFormat.date();
    protected static DateTimeFormatter xsdDateTimeFormat = ISODateTimeFormat.dateTime();


    /**
     * convenience method
     */
    protected static Boolean getPropertyBooleanValue(OntResource res, Property dataprop) {
        if (dataprop != null) {
            try {
                ClosableIterator stateIt = res.getModel().listStatements(res,dataprop,(Literal)null);
                try {
                    if (stateIt.hasNext())
                        return ((Literal)((Statement)stateIt.next()).getObject()).getBoolean();
                    else
                        return null;
                } finally {
                    stateIt.close();
                }
            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }
    }
    
    /**
     * convenience method
     */
    protected static String getPropertyStringValue(OntResource res, Property dataprop) {
        if (dataprop != null) {
            try {
                ClosableIterator<Statement> stateIt = res.getModel().listStatements(res,dataprop,(Literal)null);
                try {
                    if (stateIt.hasNext())
                        return ((Literal)stateIt.next().getObject()).getString();
                    else
                        return null;
                } finally {
                    stateIt.close();
                }
            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * convenience method
     */
    protected static void addPropertyStringValue(Resource res, Property dataprop,
            String value, Model model) {
                if (res != null && dataprop != null && value != null && value.length()>0) {
                    model.add(res, dataprop, value, XSDDatatype.XSDstring);
                }
            }

    /**
     * convenience method
     */
    protected static void addPropertyBooleanValue(Resource res, Property prop,
            Boolean value, Model model) {
                if (res != null && prop != null && value != null ) {
                    model.add(res, prop, model.createTypedLiteral(value));
                }
            }

    /**
     * convenience method for use with functional datatype properties
     */
    protected static void updatePropertyStringValue(Resource res,
            Property dataprop, String value, Model model) {
                if (dataprop != null) {
                    String existingValue = null;
                    Statement stmt = res.getProperty(dataprop);
                    if (stmt != null) {
                        RDFNode object = stmt.getObject();
                        if (object != null && object.isLiteral()){
                            existingValue = ((Literal)object).getString();
                        }
                    }
                    
                    if (value == null  || value.length() == 0) {
                         model.removeAll(res, dataprop, null);
                    } else if (existingValue == null ) {
                         model.add(res, dataprop, value, XSDDatatype.XSDstring);	
                    } else if (!existingValue.equals(value)) {
                 		 model.removeAll(res, dataprop, null);
                   		 model.add(res, dataprop, value, XSDDatatype.XSDstring);
                    }
                }
            }

    /**
     * Convenience method for use with functional datatype properties.
     * 
     * Pass keepOnlyIfTrue if a lack of a value in the model indicates false.  
     * See ObjectPropertyDaoJena and PROPERTY_OFFERCREATENEWOPTIONANNOT for an
     * example.
     */
    protected static void updatePropertyBooleanValue(Resource res,
            Property dataprop, Boolean value, Model model, boolean keepOnlyIfTrue) {
                if (dataprop != null) {
                    Boolean existingValue = null;
                    Statement stmt = res.getProperty(dataprop);
                    if (stmt != null) {
                        RDFNode object = stmt.getObject();
                        if (object != null && object.isLiteral()){
                            existingValue = ((Literal)object).getBoolean();
                        }
                    }
                    if ( (existingValue!=null && value == null) || (existingValue!=null && value != null && !(existingValue.equals(value)))
                            || (existingValue!=null && existingValue == false && keepOnlyIfTrue)) {
                        model.removeAll(res, dataprop, null);
                    }
                    if ( (existingValue==null && value != null) || (existingValue!=null && value != null && !(existingValue.equals(value)) ) ) {
                        if (keepOnlyIfTrue) {
                            if (value==true) {
                                model.add(res, dataprop, model.createTypedLiteral(value));
                            }
                        } else {
                            model.add(res, dataprop, model.createTypedLiteral(value));
                        }
                    }
                }
            }

    /**
     * convenience method
     */
    protected static int getPropertyNonNegativeIntValue(OntResource res,
            Property dataprop) {
                if (dataprop != null) {
                    try {
                        return ((Literal)res.getPropertyValue(dataprop)).getInt();
                    } catch (Exception e) {
                        return -1;
                    }
                } else {
                    return -1;
                }
            }

    /**
     * convenience method
     */
    protected static Integer getPropertyNonNegativeIntegerValue(OntResource res,
            Property dataprop) {
                if (dataprop != null) {
                    try {                        
                    	Literal lit = (Literal)res.getPropertyValue(dataprop);
                    	if( lit != null )
                    		return lit.getInt();
                    	else
                    		return null;
                    } catch (Exception e) {
                        return null;
                    }
                } else {
                    return null;
                }
            }

    /**
     * convenience method
     */
    protected static void addPropertyIntValue(Resource res, Property dataprop,
            int value, Model model) {
                if (dataprop != null) {
                    model.add(res, dataprop, Integer.toString(value), XSDDatatype.XSDint);
                }
            }

    /**
     * convenience method
     */
    protected static void addPropertyLongValue(Resource res, Property dataprop,
            long value, Model model) {
            	if (dataprop != null) {
            		model.add(res, dataprop, Long.toString(value), XSDDatatype.XSDlong);
            	}
            }

    /**
     * convenience method for use with functional datatype properties
     */
    protected static void updatePropertyIntValue(Resource res, Property dataprop,
            int value, Model model) {
                
            	if (dataprop != null) {	
                    Integer existingValue = null;
                    Statement stmt = res.getProperty(dataprop);
                    if (stmt != null) {
                        RDFNode object = stmt.getObject();
                        if (object != null && object.isLiteral()){
                            existingValue = ((Literal)object).getInt();
                        }
                    }
                	
                    if (existingValue == null ) {
                          model.add(res, dataprop, Integer.toString(value), XSDDatatype.XSDint);	
                    } else if (existingValue.intValue() != value) {
                		  model.removeAll(res, dataprop, null);
                		  model.add(res, dataprop, Integer.toString(value), XSDDatatype.XSDint);
                    }            
                }
            }

    /**
     * convenience method
     */
    protected static int getPropertyIntValue(OntResource res, Property dataprop) {
        if (dataprop != null) {
            try {
                return ((Literal)res.getPropertyValue(dataprop)).getInt();
            } catch (Exception e) {
                return -1;
            }
        } else {
            return -1;
        }
    }

    /**
     * convenience method
     */
    protected static void addPropertyNonNegativeIntValue(Resource res,
            Property dataprop, int value, Model model) {
                if (dataprop != null && value>-1) {
                    model.add(res, dataprop, Integer.toString(value), XSDDatatype.XSDint);
                }
            }

    /**
     * convenience method for use with functional datatype properties
     */
    protected static void updatePropertyNonNegativeIntValue(Resource res,
            Property dataprop, int value, Model model) {
            	if (value < 0) {
            		// TODO fixme: the backend editor depends on this weird behavior.
            		if (model != null && res != null && dataprop != null) {
            			model.removeAll(res, dataprop, (RDFNode) null);
            		}
            	} else {
            		updatePropertyIntValue(res,dataprop,value,model);
            	}
            }

    /**
     * convenience method for use with functional datatype properties
     */
    protected static void updatePropertyNonNegativeIntegerValue(Resource res,
            Property dataprop, Integer value, Model model) {
                if (value != null) {
                	updatePropertyIntValue(res,dataprop,value,model);
            	} else {
            		model.removeAll(res, dataprop, (RDFNode) null);
            	}
            }

    /**
     * convenience method for use with functional datatype properties
     */
    protected static void updatePropertyLongValue(Resource res, Property dataprop,
            Long value, Model model) {
            	
            	if (dataprop != null) {
            		Long existingValue = null;
            		Statement stmt = res.getProperty(dataprop);
            		if (stmt != null) {
            			RDFNode object = stmt.getObject();
            			if (object != null && object.isLiteral()) {
            				existingValue = ((Literal) object).getLong();
            			}
            		}
            		
            		if (existingValue == null) {
            			model.add(res, dataprop, value.toString(),
            					XSDDatatype.XSDlong);
            		} else if (existingValue.longValue() != value) {
            			model.removeAll(res, dataprop, null);
            			model.add(res, dataprop, value.toString(),
            					XSDDatatype.XSDlong);
            		}
            	}
            }

    /**
     * convenience method
     */
    protected static long getPropertyLongValue(OntResource res, Property dataprop) {
        if (dataprop != null) {
            try {
                return ((Literal)res.getPropertyValue(dataprop)).getLong();
            } catch (Exception e) {
                return -1L;
            }
        } else {
            return -1L;
        }
    }

    /**
     * convenience method
     */
    protected static void addPropertyFloatValue(Resource res, Property dataprop,
            Float value, Model model) {
                if (dataprop != null && value!= null) {
                    model.add(res, dataprop, Float.toString(value), XSDDatatype.XSDfloat);
                }
            }

    /**
     * convenience method for use with functional properties
     */
    protected static void updatePropertyFloatValue(Resource res, Property dataprop,
            Float value, Model model) {
            
            	if (dataprop != null) {	
                    Float existingValue = null;
                    Statement stmt = res.getProperty(dataprop);
                    if (stmt != null) {
                        RDFNode object = stmt.getObject();
                        if (object != null && object.isLiteral()){
                            existingValue = ((Literal)object).getFloat();
                        }
                    }
            
                    if (value == null) {
                         model.removeAll(res, dataprop, null);
                    } else if (existingValue == null ) {
                         model.add(res, dataprop, Float.toString(value), XSDDatatype.XSDfloat);	
                    } else if (existingValue.compareTo(value) != 0) {
                 		 model.removeAll(res, dataprop, null);
                  		 model.add(res, dataprop, Float.toString(value), XSDDatatype.XSDfloat);
                    }
                }
            }

    protected static Float getPropertyFloatValue(OntResource res, Property prop) {
        if( prop != null ){
            try{
                return new Float( ((Literal)res.getPropertyValue(prop)).getFloat() );
            }catch(Exception e){
                return null;
            }
        }else
            return null;
    }

    /**
     * convenience method
     */
    protected static Date getPropertyDateValue(OntResource res, DatatypeProperty dataprop) {
        if (dataprop != null) {
            try {                
                 return xsdDateFormat.parseDateTime( ((Literal)res.getPropertyValue(dataprop)).toString() ).toDate();                
            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * convenience method
     * @param ind
     * @param dataprop
     * @param value
     */
    protected static void addPropertyDateValue(Resource res, DatatypeProperty dataprop,
            Date value, Model model) {
                if (dataprop != null && value != null) {
                    model.add(res, dataprop, xsdDateFormat(value), XSDDatatype.XSDdate);
                }
            }

    /**
     * convenience method for use with functional datatype properties
     */
    protected static void updatePropertyDateValue(Resource res, DatatypeProperty dataprop,
            Date value, Model model) {
                try {
                    if (dataprop != null) {
                        if (value == null) {
                            model.removeAll(res, dataprop, null);
                        } else {
                            Date existingValue = null;
                            Statement stmt = res.getProperty(dataprop);
                            if (stmt != null) {
                                RDFNode object = stmt.getObject();
                                if (object != null && object.isLiteral()){
                                    existingValue = (Date)((Literal)object).getValue();
                                }
                            }
            	                
                            if (existingValue == null ) {
                                 model.add(res, dataprop, xsdDateFormat(value), XSDDatatype.XSDdate);	
                            } else if (existingValue != value) {
                         		 model.removeAll(res, dataprop, null);	             		 
                         		 model.add(res, dataprop, xsdDateFormat(value), XSDDatatype.XSDdate);	             		 
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("Error in updatePropertyDateValue", e);
                }
            }

    /**
     * convenience method
     */
    protected static Date getPropertyDateTimeValue(OntResource res, Property dataprop) {
        if (dataprop != null) {
            try {
                String value = ((Literal)res.getPropertyValue(dataprop)).getString();
                return xsdDateTimeFormat.parseDateTime(value).toDate();                
            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * convenience method
     */
    protected static void addPropertyDateTimeValue(Resource res, Property dataprop,
            Date value, Model model) {
                if (dataprop != null && value != null) {
                    model.add(res, dataprop, xsdDateTimeFormat(value), XSDDatatype.XSDdateTime);
                }
            }

    /**
     * convenience method for use with functional datatype properties
     */
    protected static void updatePropertyDateTimeValue(Resource res,
            Property dataprop, Date value, Model model) {
                try {
                    if (dataprop != null) {
                        String existingValue = null;
                        Statement stmt = res.getProperty(dataprop);
                        if (stmt != null) {
                            RDFNode object = stmt.getObject();
                            if (object != null && object.isLiteral()){
                                existingValue = ((Literal)object).getString();
                            }
                        }
                        
                        String formattedDateStr = (value == null) ? null : xsdDateTimeFormat(value);
                        if ( (existingValue!=null && value == null) || (existingValue!=null && value != null && !(existingValue.equals(formattedDateStr)) ) ) {
                            model.removeAll(res, dataprop, null);
                        }
                        if ( (existingValue==null && value != null) || (existingValue!=null && value != null && !(existingValue.equals(formattedDateStr)) ) ) {
                            model.add(res, dataprop, formattedDateStr, XSDDatatype.XSDdateTime);
                        }
                    }
                } catch (Exception e) {
                    log.error("Error in updatePropertyDateTimeValue", e);
                }
            }

    /**
     * convenience method for use with functional object properties
     */
    protected static Collection<String> getPropertyResourceURIValues(Resource res,
            ObjectProperty prop) {
            	List<String> list = new ArrayList<String>();
            	if (prop != null) {
            		try {
            			ClosableIterator<Statement> stateIt = res.getModel().listStatements(res,prop,(Literal)null);
            			try {
            				while(stateIt.hasNext()) {
            					list.add(stateIt.next().getObject().asResource().getURI());
            				}
            			} finally {
            				stateIt.close();
            			}
            		} catch (Exception e) {
            			log.debug("can't get object property URI values: ", e);
            		}
            	}
            	return list;
            }

    /**
     * convenience method for use with functional object properties
     */
    protected static void addPropertyResourceURIValue(Resource res,
            ObjectProperty prop, String objectURI) {
                Resource objectRes = res.getModel().getResource(objectURI);
                if (prop != null && objectRes != null) {
                    res.addProperty(prop, objectRes);
                }
            }

    /**
     * convenience method for use with functional properties
     */
    protected static void updatePropertyResourceURIValue(Resource res,
            Property prop, String objectURI) {
            	Model model = res.getModel();    	
            	if (model != null) {
            		updatePropertyResourceURIValue(res, prop, objectURI, model);
            	}    	           
            }

    /**
     * convenience method for use with functional properties
     */
    protected static void updatePropertyResourceURIValue(Resource res,
            Property prop, String uri, Model model) {
            	log.debug("updatePropertyResourceURIValue(), resource="
            			+ (res == null ? "null" : res.getURI()) + ", property="
            			+ (prop == null ? "null" : prop.getURI()) + ", uri=" + uri);
            
                if (prop != null) {
                    if (uri == null || uri.length() == 0) { 
                    	// the empty string test is due to a deficiency in the
                    	// backend editing where empty strings are treated as nulls
                        model.removeAll(res, prop, null);
                    } else {
                        String badURIErrorStr = checkURI(uri);
                        if (badURIErrorStr != null) {
                        	log.error(badURIErrorStr);
                        	return;
                        }
            
                        Resource existingValue = null;
                        Statement stmt = res.getProperty(prop);
                        if (stmt != null) {
                            RDFNode object = stmt.getObject();
                            if (object != null && object.isResource()){
                                existingValue = (Resource)object;
                            }
                        }
                            
                        if (existingValue == null ) {
                             model.add(res, prop, model.createResource(uri));	
                        } else if (!(existingValue.getURI()).equals(uri)) {
                     		 model.removeAll(res, prop, null);
                      		 model.add(res, prop, model.createResource(uri));
                        }
                    }
                }
            }

    /**
     * convenience method for use with functional object properties
     */
    protected static void addPropertyResourceValue(Resource res, Property prop,
            Resource objectRes) {
                if (prop != null && objectRes != null) {
                    res.addProperty(prop, objectRes);
                }
            }

    /**
     * convenience method for use with functional properties
     */
    protected static void updatePropertyResourceValue(Resource res,
            Property prop, Resource objectRes) {
                
            	Model model = res.getModel();
            	
            	if (model != null) {
            		updatePropertyResourceValue(res, prop, objectRes, model);
                }
            	
            }

    /**
     * convenience method for use with functional properties
     */
    protected static void updatePropertyResourceValue(Resource res,
            Property prop, Resource objectRes, Model model) {
            	
                if (prop != null) {
                    if (objectRes == null) {
                        model.removeAll(res, prop, null);
                    } else {
                        Resource existingValue = null;
                        Statement stmt = res.getProperty(prop);
                        if (stmt != null) {
                            RDFNode object = stmt.getObject();
                            if (object != null && object.isResource()){
                                existingValue = (Resource)object;
                            }
                        }
                            
                        if (existingValue == null ) {
                             model.add(res, prop, objectRes);	
                        } else if (!existingValue.equals(objectRes)) {
                     		 model.removeAll(res, prop, null);
                      		 model.add(res, prop, objectRes);
                        }
                    }
                }
            }

    /**
     * convenience method to update the value(s) of a one-to-many object
     * property
     * 
     * NOTE: this should be run from within a CriticalSection(WRITE)
     */
    protected static void updatePropertyResourceURIValues(Resource res,
            Property prop, Collection<String> uris, Model model) {
            	log.debug("updatePropertyResourceURIValues(), resource="
            			+ (res == null ? "null" : res.getURI()) + ", property="
            			+ (prop == null ? "null" : prop.getURI()) + ", uris=" + uris);
            
            	if ((res == null) || (prop == null)) {
            		return;
            	}
            
            	// figure existing URIs
            	Set<String> existingUris = new HashSet<String>();
            	StmtIterator stmts = model.listStatements(res, prop, (RDFNode) null);
            	while (stmts.hasNext()) {
            		Statement stmt = stmts.next();
            		RDFNode o = stmt.getObject();
            		if (o instanceof Resource) {
            			existingUris.add(((Resource) o).getURI());
            		}
            	}
            
            	// figure which to add and which to remove
            	Set<String> addingUris = new HashSet<String>(uris);
            	addingUris.removeAll(existingUris);
            	Set<String> removingUris = new HashSet<String>(existingUris);
            	removingUris.removeAll(uris);
            
            	// for each to remove, remove it.
            	for (String removeUri : removingUris) {
            		Resource o = model.getResource(removeUri);
            		model.remove(res, prop, o);
            	}
            
            	// for each to add, add it, unless it is null, empty, or invalid.
            	for (String addUri : addingUris) {
            		if ((addUri != null) && (!addUri.isEmpty())) {
            			String badUriErrorStr = checkURI(addUri);
            			if (badUriErrorStr == null) {
            				Resource o = model.getResource(addUri);
            				model.add(res, prop, o);
            			} else {
            				log.warn(badUriErrorStr);
            			}
            		}
            	}
            }

    protected static Literal getLabel(String lang, List<RDFNode>labelList) {
    	Iterator<RDFNode> labelIt = labelList.iterator();
    	while (labelIt.hasNext()) {
    		RDFNode label = labelIt.next();
    		if (label.isLiteral()) {
    			Literal labelLit = ((Literal)label);
    			String labelLanguage = labelLit.getLanguage();
    			if ( (labelLanguage == null) && (lang == null || lang.isEmpty()) ) {
    				return labelLit;
    			}
    			if ( (lang != null) && (lang.equals(labelLanguage)) ) {
    				return labelLit;
    			}
    		}
    	}
    	return null;
    }

    /**
     * Get the local name, bnode or URI of the resource. 
     */
    protected static String getLocalNameOrId(OntResource r) {
        String label = null;
        r.getOntModel().enterCriticalSection(Lock.READ);
        try {                       
            String localName = r.getLocalName();
            if (localName != null) {
                label = localName;
            } else if (r.isAnon()) {
                label = r.getId().toString();
            } else {
                label = r.getURI();                
            }                       
        } finally {
            r.getOntModel().leaveCriticalSection();
        }
        return label;
    }

    /**
     * Checks a URI for validity.  Jena models can store invalid URIs,
     * but this causes RDF/XML output serialization to fail.
     * @param uri
     * @return null if URI is good, otherwise an error message String
     */
    protected static String checkURI(String uri) {
    	IRIFactory factory = IRIFactory.jenaImplementation();
        IRI iri = factory.create( uri );
        if (iri.hasViolation(false) ) {
        	String errorStr = ("Bad URI: "+ uri +
        	"\nOnly well-formed absolute URIrefs can be included in RDF/XML output: "
                 + ((Violation)iri.violations(false).next()).getShortMessage());
        	return errorStr;
        } else {
        	return null;
        }
    }

    protected static void smartRemove(OntResource ontRes, OntModel ontModel) {
        removeFromLists(ontRes, ontModel);
        List<Statement> stmtForDependentRes = DependentResourceDeleteJena.getDependentResourceDeleteList(ontRes,ontModel);
        removeUsingDescribe(ontRes, ontModel);
        ontModel.remove(stmtForDependentRes);
    }
 
    /**
     * Removes a resource from any rdf:Lists in which it is a member
     */
    private static void removeFromLists(OntResource res, OntModel ontModel) {
        // Iterate through all of the list nodes this resource is attached to
        Iterator<Resource> listNodeIt = ontModel.listSubjectsWithProperty(RDF.first, res);
        while (listNodeIt.hasNext()) {
            Resource listNode = listNodeIt.next();
            //get the next node in the list
            Statement nextNodeStmt = listNode.getProperty(RDF.rest);
            if (nextNodeStmt != null) {
                // link the previous node (or resource linking to the head of the list)
                // to the next node
                RDFNode nextNode = nextNodeStmt.getObject();
                StmtIterator prevNodeIt = ontModel.listStatements((Resource) null, null, listNode);
                while (prevNodeIt.hasNext()) {
                    Statement stmt = prevNodeIt.nextStatement();
                    if (!stmt.getPredicate().equals(RDF.rest)) {
                        // if current node is list head
                        if (!nextNode.equals(RDF.nil)) {
                            // only repair the list if there is more than one node
                            ontModel.add(stmt.getSubject(), RDF.rest, nextNode);
                        }
                    } else {
                        ontModel.add(stmt.getSubject(), RDF.rest, nextNode);
                    }
                }
            }
            //Remove any statements about this node
            ontModel.remove(listNode, (Property) null, (RDFNode) null);
        }
    }
        
    // removes a resource and its bnode closure using ARQ's DESCRIBE semantics 
    // plus any incoming properties
    private static void removeUsingDescribe(OntResource ontRes, OntModel ontModel) {
        Model temp = describeResource(ontRes, ontModel);
        temp.add(ontModel.listStatements((Resource) null, (Property) null, ontRes));
        ontModel.remove(temp);
    }
    
    protected static Model describeResource(Resource res, OntModel ontModel) {        
        Model temp = ModelFactory.createDefaultModel();
        
        // For now, not using DESCRIBE on blank nodes unless I can figure out
        // how to keep it from doing a full kb scan.
        if (res.isAnon()) {
            temp.add(ontModel.listStatements(res, (Property) null, (RDFNode) null));
            return temp;
        }
        
        String describeQueryStr =    "DESCRIBE <" + res.getURI() + ">" ;
        
//      ?   "PREFIX afn: <http://jena.hpl.hp.com/ARQ/function#> \n\n" +
//          "DESCRIBE ?bnode \n" +
//          "WHERE { \n" +
//          "    FILTER(afn:bnode(?bnode) = \"" + res.getId().toString() + "\")\n" +
//          "    ?bnode ?p ?o \n" +
//          "}"
         
        Query describeQuery = QueryFactory.create(describeQueryStr, Syntax.syntaxARQ);
        QueryExecution qe = QueryExecutionFactory.create(describeQuery, ontModel);
        qe.execDescribe(temp);
        
        return temp;
    }
    
    protected static String xsdDateFormat(Date value) {
        return xsdDateFormat.print(new DateTime( value ));
    }

    protected static String xsdDateTimeFormat(Date value) {
        return xsdDateTimeFormat.print(new DateTime( value ));
    }

    public JenaBaseDaoUtils() {
        super();
    }

}