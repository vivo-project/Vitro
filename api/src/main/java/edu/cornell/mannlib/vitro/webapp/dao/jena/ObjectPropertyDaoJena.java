/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.rdfservice.ResultSetConsumer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ontology.ConversionException;
import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.ontology.ProfileException;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.shared.Lock;
import org.apache.jena.util.iterator.ClosableIterator;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vitro.webapp.auth.policy.bean.RoleRestrictedProperty;
import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.dao.InsertException;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.jena.event.EditEvent;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;

public class ObjectPropertyDaoJena extends PropertyDaoJena implements ObjectPropertyDao {
    private static final Log log = LogFactory.getLog(ObjectPropertyDaoJena.class.getName());
        
    public ObjectPropertyDaoJena(RDFService rdfService,
                                 DatasetWrapperFactory dwf,
                                 Map<FullPropertyKey, String> customListViewConfigFileMap,
                                 WebappDaoFactoryJena wadf) {
        super(rdfService, dwf, wadf);
        this.customListViewConfigFileMap = customListViewConfigFileMap;
    }

    @Override
    protected OntModel getOntModel() {
    	return getOntModelSelector().getTBoxModel();
    }
    
    public void deleteObjectProperty(String propertyURI) {
        ObjectProperty op = getObjectPropertyByURI(propertyURI);
        deleteObjectProperty(op);
    }

    public void fillObjectPropertiesForIndividual(Individual entity) {
        if( entity == null ) return;
        List<ObjectProperty> props = new ArrayList<ObjectProperty>();
        props.addAll( getObjectPropertiesForObjectPropertyStatements(entity.getObjectPropertyStatements()) );
        entity.setPropertyList(props);
    }

    protected ObjectProperty propertyFromOntProperty(OntProperty op) {
        if (op==null) {
            return null;
        }
        if( op instanceof DatatypeProperty){
            return null;
        }
        ObjectProperty p = new ObjectProperty();
        try {
            p.setURI(op.getURI());
            p.setNamespace(op.getNameSpace());
            p.setLocalName(op.getLocalName());
            p.setLocalNameWithPrefix(getWebappDaoFactory().makeLocalNameWithPrefix(p));
            if (op.getLabel(null) != null)
                p.setDomainPublic(getLabelOrId(op));
            else
                p.setDomainPublic(op.getLocalName());
            if (p.getDomainPublic() == null)
                p.setDomainPublic("[related to]");
            p.setPickListName(getWebappDaoFactory().makePickListName(p));
            if (op.getDomain() != null)
                p.setDomainVClassURI( (op.getDomain().isAnon()) ? PSEUDO_BNODE_NS+op.getDomain().getId().toString() : op.getDomain().getURI());
            if (op.getRange() != null)
                p.setRangeVClassURI( (op.getRange().isAnon()) ? PSEUDO_BNODE_NS+op.getRange().getId().toString() : op.getRange().getURI() );
            OntProperty invOp = null;
            try {
            	invOp = op.getInverse();
            } catch (ProfileException pe) {}
            try {
            if (op.getSuperProperty() != null)
                p.setParentURI(op.getSuperProperty().getURI());
            } catch (ConversionException ce) { 
            	StmtIterator parentStmtIt = op.listProperties(RDFS.subPropertyOf);
            	if (parentStmtIt.hasNext()) {
            		Statement parentStmt = parentStmtIt.nextStatement();
            		if (parentStmt.getObject().isResource()) {
            			p.setParentURI(((Resource)parentStmt.getObject()).getURI());
            		}
            	}
            	parentStmtIt.close();
            }
            if (invOp != null) {
                p.setURIInverse(invOp.getURI());
                p.setNamespaceInverse(invOp.getNameSpace());
                p.setLocalNameInverse(invOp.getLocalName());
                p.setRangePublic(getLabelOrId(invOp));
            }
            try {
	            if (op.isTransitiveProperty() || (invOp != null && invOp.isTransitiveProperty()) )  {
	            	p.setTransitive(true);
	            }
            } catch (ProfileException pe) {}
            try {
	            if (op.isSymmetricProperty() || (invOp != null && invOp.isSymmetricProperty()) )  {
	            	p.setSymmetric(true);
	            }
            } catch (ProfileException pe) {}
            try {
	            if (op.isFunctionalProperty())  {
	            	p.setFunctional(true);
	            }
            } catch (ProfileException pe) {}
            try {
	            if (op.isInverseFunctionalProperty())  {
	            	p.setInverseFunctional(true);
	            }
        	} catch (ProfileException pe) {}
            p.setExample(getPropertyStringValue(op,EXAMPLE_ANNOT));            
            p.setDescription(getPropertyStringValue(op,DESCRIPTION_ANNOT));
            p.setPublicDescription(getPropertyStringValue(op,PUBLIC_DESCRIPTION_ANNOT));
           
           	p.setDomainDisplayTier(getPropertyNonNegativeIntegerValue(op,DISPLAY_RANK_ANNOT));
           	p.setRangeDisplayTier(getPropertyNonNegativeIntegerValue(invOp,DISPLAY_RANK_ANNOT));
            p.setDomainDisplayLimit(getPropertyNonNegativeIntValue(op,DISPLAY_LIMIT));
            p.setRangeDisplayLimit(getPropertyNonNegativeIntValue(invOp,DISPLAY_LIMIT));
            RDFNode objectIndividualSortPropertyNode = op.getPropertyValue(PROPERTY_OBJECTINDIVIDUALSORTPROPERTY);
            if (objectIndividualSortPropertyNode instanceof Resource) {  
            	p.setObjectIndividualSortPropertyURI( ((Resource)objectIndividualSortPropertyNode).getURI() ); 
            }
            p.setDomainEntitySortDirection(getPropertyStringValue(op,PROPERTY_ENTITYSORTDIRECTION));
            p.setRangeEntitySortDirection(getPropertyStringValue(invOp,PROPERTY_ENTITYSORTDIRECTION));
            
            //There might be multiple HIDDEN_FROM_DISPLAY_BELOW_ROLE_LEVEL_ANNOT properties, only use the highest
            StmtIterator it = op.listProperties(HIDDEN_FROM_DISPLAY_BELOW_ROLE_LEVEL_ANNOT);
            BaseResourceBean.RoleLevel hiddenRoleLevel = null;
            while( it.hasNext() ){
                Statement stmt = it.nextStatement();
                RDFNode obj;
                if( stmt != null && (obj = stmt.getObject()) != null && obj.isURIResource() ){
                    Resource res = obj.as(Resource.class);
                    if( res != null && res.getURI() != null ){
                        BaseResourceBean.RoleLevel roleFromModel =  BaseResourceBean.RoleLevel.getRoleByUri(res.getURI());
                        if( roleFromModel != null && 
                            (hiddenRoleLevel == null || roleFromModel.compareTo(hiddenRoleLevel) > 0 )){
                            hiddenRoleLevel = roleFromModel;                            
                        }
                    }
                }
            }            
            p.setHiddenFromDisplayBelowRoleLevel(hiddenRoleLevel); //this might get set to null

            //There might be multiple PROHIBITED_FROM_UPDATE_BELOW_ROLE_LEVEL_ANNOT properties, only use the highest
            it = op.listProperties(PROHIBITED_FROM_UPDATE_BELOW_ROLE_LEVEL_ANNOT);
            BaseResourceBean.RoleLevel prohibitedRoleLevel = null;
            while( it.hasNext() ){
                Statement stmt = it.nextStatement();
                RDFNode obj;
                if( stmt != null && (obj = stmt.getObject()) != null && obj.isURIResource() ){
                    Resource res = obj.as(Resource.class);
                    if( res != null && res.getURI() != null ){
                        BaseResourceBean.RoleLevel roleFromModel =  BaseResourceBean.RoleLevel.getRoleByUri(res.getURI());
                        if( roleFromModel != null && 
                            (prohibitedRoleLevel == null || roleFromModel.compareTo(prohibitedRoleLevel) > 0 )){
                            prohibitedRoleLevel = roleFromModel;                            
                        }
                    }
                }
            }            
            p.setProhibitedFromUpdateBelowRoleLevel(prohibitedRoleLevel); //this might get set to null

            //There might be multiple HIDDEN_FROM_PUBLISH_BELOW_ROLE_LEVEL_ANNOT properties, only use the highest
            it = op.listProperties(HIDDEN_FROM_PUBLISH_BELOW_ROLE_LEVEL_ANNOT);
            BaseResourceBean.RoleLevel publishRoleLevel = null;
            while( it.hasNext() ){
                Statement stmt = it.nextStatement();
                RDFNode obj;
                if( stmt != null && (obj = stmt.getObject()) != null && obj.isURIResource() ){
                    Resource res = obj.as(Resource.class);
                    if( res != null && res.getURI() != null ){
                        BaseResourceBean.RoleLevel roleFromModel =  BaseResourceBean.RoleLevel.getRoleByUri(res.getURI());
                        if( roleFromModel != null && 
                            (publishRoleLevel == null || roleFromModel.compareTo(publishRoleLevel) > 0 )){
                            publishRoleLevel = roleFromModel;                            
                        }
                    }
                }
            }            
            p.setHiddenFromPublishBelowRoleLevel(publishRoleLevel); //this might get set to null

            p.setCustomEntryForm(getPropertyStringValue(op,PROPERTY_CUSTOMENTRYFORMANNOT));
            Boolean selectFromObj = getPropertyBooleanValue(op,PROPERTY_SELECTFROMEXISTINGANNOT);
            p.setSelectFromExisting(selectFromObj==null ? true : selectFromObj);
            Boolean offerCreateObj = getPropertyBooleanValue(op,PROPERTY_OFFERCREATENEWOPTIONANNOT);
            p.setOfferCreateNewOption(offerCreateObj==null ? false : offerCreateObj);
            Boolean dependencyDeletionObj = getPropertyBooleanValue(op,PROPERTY_STUBOBJECTPROPERTYANNOT);
            p.setStubObjectRelation(dependencyDeletionObj==null ? false : dependencyDeletionObj);

            Boolean collateBySubclass = getPropertyBooleanValue(op,PROPERTY_COLLATEBYSUBCLASSANNOT);
            p.setCollateBySubclass(collateBySubclass==null ? false : collateBySubclass);
            
            Boolean editLinkSuppressed = getPropertyBooleanValue(op, PROPERTY_EDITLINKSUPPRESSED);
            p.setEditLinkSuppressed(editLinkSuppressed == null ? false : editLinkSuppressed);
            Boolean addLinkSuppressed = getPropertyBooleanValue(op, PROPERTY_ADDLINKSUPPRESSED);
            p.setAddLinkSuppressed(addLinkSuppressed == null ? false : addLinkSuppressed);
            Boolean deleteLinkSuppressed = getPropertyBooleanValue(op, PROPERTY_DELETELINKSUPPRESSED);
            p.setDeleteLinkSuppressed(deleteLinkSuppressed == null ? false : deleteLinkSuppressed);
            
            Resource groupRes = (Resource) op.getPropertyValue(PROPERTY_INPROPERTYGROUPANNOT);
            if (groupRes != null) {
                p.setGroupURI(groupRes.getURI());
            }
        } catch (Throwable t) {
            log.error(t, t);
        }
        return p;
    }

    public List getAllObjectProperties() {
        getOntModel().enterCriticalSection(Lock.READ);
        try {
            List props = new ArrayList();
            try {
	            ClosableIterator opIt = getOntModel().listObjectProperties();
	            try {
	                while (opIt.hasNext()) {
	                    org.apache.jena.ontology.ObjectProperty op = (org.apache.jena.ontology.ObjectProperty) opIt.next();
	                    if (!NONUSER_NAMESPACES.contains(op.getNameSpace()))
	                    props.add(propertyFromOntProperty(op));
	                }
	            } finally {
	                opIt.close();
	            }
            } catch (ProfileException pe) {
            	ClosableIterator opIt = getOntModel().listSubjectsWithProperty(RDF.type,RDF.Property);
	            try {
	                while (opIt.hasNext()) {
	                	Resource res = (Resource) opIt.next();
	                	if ( (res.canAs(OntProperty.class)) && (!NONUSER_NAMESPACES.contains(res.getNameSpace())) ) {
	                		props.add(propertyFromOntProperty(res.as(OntProperty.class)));
	                	}
	                }
	            } finally {
	                opIt.close();
	            }
            }
            return props;
        } finally {
            getOntModel().leaveCriticalSection();
        }
    }

    public List getPropIdsForClass(String classURI, boolean direction) {
        return null;
    }

    public ObjectProperty getObjectPropertyByURI(String propertyURI) {
        
        if( propertyURI == null ) return null;
        
        OntModel ontModel = getOntModel();
        OntModel localModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        
        ontModel.enterCriticalSection(Lock.READ);
        try {
            Resource prop = ontModel.getResource(propertyURI);
            localModel.add(ontModel.listStatements(prop, null, (RDFNode) null));
            StmtIterator invit = ontModel.listStatements(prop, OWL.inverseOf, (RDFNode) null);
            while (invit.hasNext()) {
                Statement invSit = invit.nextStatement();
                if (invSit.getObject().isURIResource()) {
                    Resource invProp = (Resource) invSit.getObject();
                    localModel.add(ontModel.listStatements(invProp, null, (RDFNode) null));
                }
            }
            OntProperty op = localModel.getObjectProperty(propertyURI);
            return propertyFromOntProperty(op);
        } finally {
            ontModel.leaveCriticalSection();
        }
    }
    
    public ObjectProperty getObjectPropertyByURIs(String propertyURI,
            String domainURI, String rangeURI) {
        return getObjectPropertyByURIs(propertyURI, domainURI, rangeURI, null); 
    }
    
    public ObjectProperty getObjectPropertyByURIs(String propertyURI, 
            String domainURI, String rangeURI, ObjectProperty base) {
        if(log.isDebugEnabled()) {
            log.debug("Getting " + propertyURI + " with domain " + domainURI + " and range " + rangeURI);
        }
        long start = System.currentTimeMillis();
        ObjectProperty op = (base != null) ? base : getObjectPropertyByURI(propertyURI);
        if (op == null || rangeURI == null) {
            return op;
        }
        op.setDomainVClassURI(domainURI);
        op.setRangeVClassURI(rangeURI);
        String propQuery = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
                "PREFIX config: <http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#> \n" +
                "PREFIX vitro: <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#> \n" +
                "SELECT ?range ?rangeRoot ?label ?group ?customForm ?displayRank ?displayLevel " +
                "    ?updateLevel ?publishLevel ?editLinkSuppressed ?addLinkSuppressed ?deleteLinkSuppressed \n" +
                "    ?collateBySubclass ?displayLimit ?individualSortProperty \n" +
                "    ?entitySortDirection ?selectFromExisting ?offerCreateNew \n" +
                "    ?publicDescription ?stubDeletion \n" + 
                " WHERE { \n" +
                "    ?context config:configContextFor <" + propertyURI + "> . \n";
        if (domainURI != null && !domainURI.equals(OWL.Thing.getURI())) {
                propQuery += "    ?context config:qualifiedByDomain <" + domainURI + "> . \n";
        } else {
                propQuery += "   FILTER NOT EXISTS { ?context config:qualifiedByDomain ?domainURI } \n";
        }
        if (rangeURI != null) {
            propQuery += "    ?context config:qualifiedBy <" + rangeURI + "> . \n";
        };
                propQuery += "    OPTIONAL { ?context config:qualifiedByRoot ?rangeRoot } \n" +
                "    ?context config:hasConfiguration ?configuration . \n" +
                "    ?configuration a config:ObjectPropertyDisplayConfig . \n" +
                "    OPTIONAL { ?configuration config:propertyGroup ?group } \n" +
                "    OPTIONAL { ?configuration config:displayName ?label } \n" +
                "    OPTIONAL { ?configuration config:editLinkSuppressed ?editLinkSuppressed } \n" +
                "    OPTIONAL { ?configuration config:addLinkSuppressed ?addLinkSuppressed } \n" +
                "    OPTIONAL { ?configuration config:deleteLinkSuppressed ?deleteLinkSuppressed } \n" +
                "    OPTIONAL { ?configuration vitro:displayRankAnnot ?displayRank } \n" +
                "    OPTIONAL { ?configuration vitro:customEntryFormAnnot ?customForm } \n" +
                "    OPTIONAL { ?configuration vitro:hiddenFromDisplayBelowRoleLevelAnnot ?displayLevel } \n" +
                "    OPTIONAL { ?configuration vitro:prohibitedFromUpdateBelowRoleLevelAnnot ?updateLevel } \n" +
                "    OPTIONAL { ?configuration vitro:hiddenFromPublishBelowRoleLevelAnnot ?publishLevel } \n" +
                "    OPTIONAL { ?configuration <" + PROPERTY_COLLATEBYSUBCLASSANNOT.getURI() + "> ?collateBySubclass } \n" +
                "    OPTIONAL { ?configuration <" + DISPLAY_LIMIT.getURI() + "> ?displayLimit } \n" +
                "    OPTIONAL { ?configuration <" + PROPERTY_OBJECTINDIVIDUALSORTPROPERTY.getURI() + "> ?individualSortProperty } \n " +
                "    OPTIONAL { ?configuration <" + PROPERTY_ENTITYSORTDIRECTION.getURI() + "> ?entitySortDirection } \n" +
                "    OPTIONAL { ?configuration <" + PROPERTY_SELECTFROMEXISTINGANNOT.getURI() + "> ?selectFromExisting } \n" +
                "    OPTIONAL { ?configuration <" + PROPERTY_OFFERCREATENEWOPTIONANNOT.getURI() + "> ?offerCreateNew } \n" +
                "    OPTIONAL { ?configuration <" + PUBLIC_DESCRIPTION_ANNOT.getURI() + "> ?publicDescription } \n" +
                "    OPTIONAL { ?configuration <" + PROPERTY_STUBOBJECTPROPERTYANNOT.getURI() + "> ?stubDeletion } \n" +
                "}"; 
      
        Query q = QueryFactory.create(propQuery);
        QueryExecution qe = QueryExecutionFactory.create(q, getOntModelSelector().getDisplayModel());
        try {
            ResultSet rs = qe.execSelect();
            if (rs.hasNext()) {
                QuerySolution qsoln = rs.nextSolution();
                // This is a bit of hack, used for things where the type of the 
                // immediately-related object ("root," for lack of a better term)
                // is important to record but not the type directly associated with
                // a configuration
                Resource rangeRootRes = qsoln.getResource("rangeRoot");
                if (rangeRootRes != null) {
                    // reusing this obsolete field for now
                    op.setRangeEntityURI(rangeRootRes.getURI());
                } 
                Resource groupRes = qsoln.getResource("group");
                if (groupRes != null) {
                    op.setGroupURI(groupRes.getURI());
                } 
                Literal displayRankLit = qsoln.getLiteral("displayRank");
                if(displayRankLit != null) {
                    op.setDomainDisplayTier(
                            Integer.parseInt(displayRankLit.getLexicalForm()));
                } 
                Literal displayLimitLit = qsoln.getLiteral("displayLimit");
                if(displayLimitLit != null) {
                    op.setDomainDisplayLimit(
                            Integer.parseInt(displayLimitLit.getLexicalForm()));
                } 
                Resource displayLevelRes = qsoln.getResource("displayLevel");
                if (displayLevelRes != null) {
                    op.setHiddenFromDisplayBelowRoleLevel(
                            BaseResourceBean.RoleLevel.getRoleByUri(
                                    displayLevelRes.getURI()));
                }
                Resource updateLevelRes = qsoln.getResource("updateLevel");
                if (updateLevelRes != null) {
                    op.setProhibitedFromUpdateBelowRoleLevel(
                            BaseResourceBean.RoleLevel.getRoleByUri(
                                    updateLevelRes.getURI()));
                }
                Resource publishLevelRes = qsoln.getResource("publishLevel");
                if (publishLevelRes != null) {
                    op.setHiddenFromPublishBelowRoleLevel(
                            BaseResourceBean.RoleLevel.getRoleByUri(
                                    publishLevelRes.getURI()));
                }
                Literal labelLit = qsoln.getLiteral("label");
                if (labelLit != null) {
                    op.setDomainPublic(labelLit.getLexicalForm());
                } 
                Literal customFormLit = qsoln.getLiteral("customForm");
                if (customFormLit != null) {
                    op.setCustomEntryForm(customFormLit.getLexicalForm());
                } 
                Literal editLinkSuppressedLit = qsoln.getLiteral("editLinkSuppressed");
                if (editLinkSuppressedLit != null ) {
                    op.setEditLinkSuppressed(editLinkSuppressedLit.getBoolean());
                }
                Literal addLinkSuppressedLit = qsoln.getLiteral("addLinkSuppressed");
                if (addLinkSuppressedLit != null ) {
                    op.setAddLinkSuppressed(addLinkSuppressedLit.getBoolean());
                }
                Literal deleteLinkSuppressedLit = qsoln.getLiteral("deleteLinkSuppressed");
                if (deleteLinkSuppressedLit != null ) {
                    op.setDeleteLinkSuppressed(deleteLinkSuppressedLit.getBoolean());
                }
                Literal collateBySubclassLit = qsoln.getLiteral("collateBySubclass");
                if (collateBySubclassLit != null) {
                    op.setCollateBySubclass(collateBySubclassLit.getBoolean());
                }
                Resource individualSortPropertyRes = qsoln.getResource("individualSortProperty");
                if (individualSortPropertyRes != null) {
                    op.setObjectIndividualSortPropertyURI(individualSortPropertyRes.getURI());
                } 
                Literal entitySortDirectionLit = qsoln.getLiteral("entitySortDirection");
                if (entitySortDirectionLit != null) {
                    op.setDomainEntitySortDirection(entitySortDirectionLit.getLexicalForm());
                }
                Literal selectFromExistingLit = qsoln.getLiteral("selectFromExisting");
                if (selectFromExistingLit != null) {
                    op.setSelectFromExisting(selectFromExistingLit.getBoolean());
                }
                Literal offerCreateNewLit = qsoln.getLiteral("offerCreateNew");
                if (offerCreateNewLit != null) {
                    op.setOfferCreateNewOption(offerCreateNewLit.getBoolean());
                }
                Literal stubDeletionLit = qsoln.getLiteral("stubDeletion");
                if (stubDeletionLit != null) {
                    op.setStubObjectRelation(stubDeletionLit.getBoolean());
                }
                Literal publicDescriptionLit = qsoln.getLiteral("publicDescription");
                if (publicDescriptionLit != null) {
                    op.setPublicDescription(publicDescriptionLit.getLexicalForm());
                }        
             }  
        } finally {
            qe.close();
        }    
        return op;
    }
    

    public List<ObjectProperty> getObjectPropertiesForObjectPropertyStatements(List objPropertyStmts) {
        if( objPropertyStmts == null || objPropertyStmts.size() < 1) return new ArrayList();
        HashMap<String,ObjectProperty> hash = new HashMap<String,ObjectProperty>();
        String uris ="";
        getOntModel().enterCriticalSection(Lock.READ);
        try {
            Iterator it = objPropertyStmts.iterator();
            while(it.hasNext()){
                ObjectPropertyStatement objPropertyStmt = (ObjectPropertyStatement)it.next();
                if (hash.containsKey(objPropertyStmt.getPropertyURI())) {
                    ObjectProperty p = hash.get(objPropertyStmt.getPropertyURI());
                    p.addObjectPropertyStatement(objPropertyStmt);
                } else {
                    OntProperty op = getOntModel().getOntProperty(objPropertyStmt.getPropertyURI());
                    if (op != null) {
                        ObjectProperty p = propertyFromOntProperty(op);
                        hash.put(p.getURI(),p);
                        p.addObjectPropertyStatement(objPropertyStmt);
                    }
                }
            }

            List<ObjectProperty> props = new ArrayList<ObjectProperty>();
            Iterator<String> keyIt = hash.keySet().iterator();
            while (keyIt.hasNext()) {
                Object key = keyIt.next();
                props.add(hash.get(key));
            }
            return props;
        } finally {
            getOntModel().leaveCriticalSection();
        }
    }

	// This is a funny method to address the fact that the editing code will
	// often supply us the property beans containing an inverse namespace but no
	// inverse local name, which should be interpreted as a lack of an inverse
	// property entirely.  Yes, this is a hack.
	private boolean hasInverse(ObjectProperty prop) {
		boolean hasInverse = false;
		if ( (prop.getURIInverse() != null) && (!("".equals(prop.getURIInverse()))) ) {
			hasInverse = true;
		} 
		if ( (prop.getNamespaceInverse() != null) && (!(prop.getNamespaceInverse().equals(""))) &&
			 ( (prop.getLocalNameInverse()==null) || ("".equals(prop.getLocalNameInverse()) ) ) ) {
			hasInverse = false;
		}
		return hasInverse;
	}

    public int insertObjectProperty(ObjectProperty prop) throws InsertException {
    	return insertProperty(prop,getOntModel());
    }

    public int insertProperty(ObjectProperty prop, OntModel ontModel) throws InsertException {
        if (prop.getURI()==null || prop.getURI().length()<3) {
            return 1;
        }
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
	        getOntModel().getBaseModel().notifyEvent(new EditEvent(getWebappDaoFactory().getUserURI(),true));
	    	String errMsgStr = getWebappDaoFactory().checkURIForEditableEntity(prop.getURI());
	    	if (errMsgStr != null) {
	    		throw new InsertException(errMsgStr);
	    	}
	        org.apache.jena.ontology.ObjectProperty p = ontModel.createObjectProperty(prop.getURI());
	        org.apache.jena.ontology.ObjectProperty inv = null;
	        if (hasInverse(prop)) {
	        	log.debug("non-null inverse URI: " +prop.getURIInverse());	        	
	        	errMsgStr = getWebappDaoFactory().checkURIForEditableEntity(prop.getURIInverse());
	        	if (errMsgStr != null) {
	        		throw new InsertException("Unusable URI for inverse property: "+errMsgStr);
	        	}
	            inv = ontModel.createObjectProperty(prop.getURIInverse());
	            inv.setInverseOf(p);
	            p.setInverseOf(inv);
	        }
	        doUpdate(prop,p,inv,ontModel);
	        return 0;
        } finally {
        	getOntModel().getBaseModel().notifyEvent(new EditEvent(getWebappDaoFactory().getUserURI(),false));
        	ontModel.leaveCriticalSection();
        }
    }

    public void updateObjectProperty(ObjectProperty prop) {
    	updateProperty(prop,getOntModel());
    }

    
    public void updateProperty(ObjectProperty prop, OntModel ontModel) {
        if (prop.getURI()==null || prop.getURI().length()<3) {
            return;
        }
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
	        getOntModel().getBaseModel().notifyEvent(new EditEvent(getWebappDaoFactory().getUserURI(),true));     
	        org.apache.jena.ontology.ObjectProperty p = ontModel.getObjectProperty(prop.getURI());
	        if (p == null) {
	            return;
	        }
	        org.apache.jena.ontology.ObjectProperty inv = null;
	        if (hasInverse(prop)) {
			    try {
		            	inv = ontModel.getObjectProperty(prop.getURIInverse());
		            	
		            	if  (!inv.isInverseOf(p)) {
		            	  inv.setInverseOf(p);
		            	  p.setInverseOf(inv);
		            	}
	            } catch (Exception e) {
	                   log.debug("Couldn't set "+prop.getURIInverse()+" as inverse");
				       // BJL: What we really want to do here is create a new property as inverse
			    }      
	        }
	        
            try {
	             doUpdate(prop,p,inv,ontModel);
            } catch (Exception e) {
                 log.error(e, e);
            }
        } finally {
        	getOntModel().getBaseModel().notifyEvent(new EditEvent(getWebappDaoFactory().getUserURI(),false));
        	ontModel.leaveCriticalSection();
        }
    }
    
    /* This method assumes that the caller is locking and unlocking the model */
    private void doUpdate(ObjectProperty prop, OntProperty p, OntProperty inv, OntModel ontModel) {
        
        updateRDFSLabel(p,prop.getDomainPublic());

        if (inv != null) {
    		updateRDFSLabel(inv,prop.getRangePublic());
    	}

    	try {
	        String parentURI = prop.getParentURI();
	        if (parentURI != null) {
		        org.apache.jena.ontology.ObjectProperty parent = ontModel.getObjectProperty(prop.getParentURI());
		        if (parent != null) {
		        	
		        	if (!p.hasSuperProperty(parent, true)) {
		               p.addSuperProperty(parent);
		        	}
		        	
		            if (inv != null) {
		                OntProperty parentInv = parent.getInverse();
		                
		                if (parentInv != null) {
		                	if (!inv.hasSuperProperty(parentInv,true)) {
		                	   inv.setSuperProperty(parentInv);
		                	}
		                }		                
		            }
		        }
	        }
    	} catch (Exception e) {
    		// something odd happens here with layered models
    		// plus, this whole section needs reworking
    		log.error("Unable to update parent property for "+prop.getURI(), e);
    	}
    	
        if (prop.getTransitive()) {
        	if (!getOntModel().contains(p,RDF.type,OWL.TransitiveProperty)) {
        		getOntModel().add(p,RDF.type,OWL.TransitiveProperty);
        	}
        	
        	if ( (inv != null) && (!getOntModel().contains(inv,RDF.type,OWL.TransitiveProperty))) {
        		getOntModel().add(inv,RDF.type,OWL.TransitiveProperty);
        	}
        } else {
        	if (getOntModel().contains(p,RDF.type,OWL.TransitiveProperty)) {
        		getOntModel().remove(p,RDF.type,OWL.TransitiveProperty);
        	}
        	if ( (inv != null) && (getOntModel().contains(inv,RDF.type,OWL.TransitiveProperty))) {
        		getOntModel().remove(inv,RDF.type,OWL.TransitiveProperty);
        	}
        }
        
        if (prop.getSymmetric()) {
        	if (!getOntModel().contains(p,RDF.type,OWL.SymmetricProperty)) {
        		getOntModel().add(p,RDF.type,OWL.SymmetricProperty);
        	}
        	if ( (inv != null) && (!getOntModel().contains(inv,RDF.type,OWL.SymmetricProperty))) {
        		getOntModel().add(inv,RDF.type,OWL.SymmetricProperty);
        	}
        } else {
        	if (getOntModel().contains(p,RDF.type,OWL.SymmetricProperty)) {
        		getOntModel().remove(p,RDF.type,OWL.SymmetricProperty);
        	}
        	if ( (inv != null) && (getOntModel().contains(inv,RDF.type,OWL.SymmetricProperty))) {
        		getOntModel().remove(inv,RDF.type,OWL.SymmetricProperty);
        	}
        }
        
        if (prop.getFunctional()) {
        	if (!getOntModel().contains(p,RDF.type,OWL.FunctionalProperty)) {
        		getOntModel().add(p,RDF.type,OWL.FunctionalProperty);
        	}
        } else {
        	if (getOntModel().contains(p,RDF.type,OWL.FunctionalProperty)) {
        		getOntModel().remove(p,RDF.type,OWL.FunctionalProperty);
        	}
        }
        
        if (prop.getInverseFunctional()) {
        	if (!getOntModel().contains(p,RDF.type,OWL.InverseFunctionalProperty)) {
        		getOntModel().add(p,RDF.type,OWL.InverseFunctionalProperty);
        	}
        } else {
        	if (getOntModel().contains(p,RDF.type,OWL.InverseFunctionalProperty)) {
        		getOntModel().remove(p,RDF.type,OWL.InverseFunctionalProperty);
        	}
        }
     	
        if ( (prop.getDomainVClassURI() != null) && (prop.getDomainVClassURI().length()>0) ) {
            if (!p.hasDomain(ontModel.getResource(prop.getDomainVClassURI()))) {
        	    p.setDomain(ontModel.getResource(prop.getDomainVClassURI()));
                if (inv != null) {
                   inv.setRange(p.getDomain());
                }
            }
        } else {
            p.removeAll(RDFS.domain);
            if (inv != null) {
            	inv.removeAll(RDFS.range);
            }        	
        }
                
        if ( (prop.getRangeVClassURI() != null) && (prop.getRangeVClassURI().length()>0) ) {
            if (!p.hasRange(ontModel.getResource(prop.getRangeVClassURI()))) {
               p.setRange(ontModel.getResource(prop.getRangeVClassURI()));
               if (inv != null) {
                   inv.setDomain(p.getRange());
               }
            }
        } else {
            p.removeAll(RDFS.range);
            if (inv != null) {
            	inv.removeAll(RDFS.domain);
            }        	
        }
        
        updatePropertyStringValue(p,EXAMPLE_ANNOT,prop.getExample(),getOntModel());
        updatePropertyStringValue(p,DESCRIPTION_ANNOT,prop.getDescription(),getOntModel());
        updatePropertyStringValue(p,PUBLIC_DESCRIPTION_ANNOT,prop.getPublicDescription(),getOntModel());
        updatePropertyNonNegativeIntegerValue(p,DISPLAY_LIMIT,prop.getDomainDisplayLimitInteger(),getOntModel());
        updatePropertyStringValue(p,PROPERTY_ENTITYSORTDIRECTION,prop.getDomainEntitySortDirection(),getOntModel());
        if (inv != null) {
            updatePropertyStringValue(inv,EXAMPLE_ANNOT,prop.getExample(),getOntModel());
            updatePropertyStringValue(inv,DESCRIPTION_ANNOT,prop.getDescription(),getOntModel());
            updatePropertyNonNegativeIntegerValue(inv,DISPLAY_LIMIT,prop.getRangeDisplayLimitInteger(),getOntModel());
            updatePropertyStringValue(inv,PROPERTY_ENTITYSORTDIRECTION,prop.getRangeEntitySortDirection(),getOntModel());
        }
                
    	updatePropertyNonNegativeIntegerValue(p, DISPLAY_RANK_ANNOT, prop.getDomainDisplayTierInteger(), getOntModel());
    	if (inv != null) {
        	updatePropertyNonNegativeIntegerValue(inv, DISPLAY_RANK_ANNOT, prop.getRangeDisplayTierInteger(), getOntModel());
		}
    	
        String oldObjectIndividualSortPropertyURI = null;
    	RDFNode sortPropertyNode = p.getPropertyValue(PROPERTY_OBJECTINDIVIDUALSORTPROPERTY);
    	if (sortPropertyNode != null && sortPropertyNode instanceof Resource) {
    		oldObjectIndividualSortPropertyURI = ((Resource)sortPropertyNode).getURI();
    	}
        if ( ( oldObjectIndividualSortPropertyURI != null && prop.getObjectIndividualSortPropertyURI() == null ) ||
        	 ( oldObjectIndividualSortPropertyURI != null && prop.getObjectIndividualSortPropertyURI() != null && !(oldObjectIndividualSortPropertyURI.equals(prop.getObjectIndividualSortPropertyURI()))))	{
        	p.removeAll(PROPERTY_OBJECTINDIVIDUALSORTPROPERTY);
        } 
        if ( ( oldObjectIndividualSortPropertyURI == null && prop.getObjectIndividualSortPropertyURI() != null ) ||
           	 ( oldObjectIndividualSortPropertyURI != null && prop.getObjectIndividualSortPropertyURI() != null && !(oldObjectIndividualSortPropertyURI.equals(prop.getObjectIndividualSortPropertyURI()))))	{
        	Resource newObjectIndividualSortProperty = null;
        	try {
        		if (prop.getObjectIndividualSortPropertyURI().length()>0) {
        			newObjectIndividualSortProperty = ResourceFactory.createResource(prop.getObjectIndividualSortPropertyURI());
        		}
        	} catch (Exception e) {}
        	if (newObjectIndividualSortProperty != null) {
        		p.addProperty(PROPERTY_OBJECTINDIVIDUALSORTPROPERTY,newObjectIndividualSortProperty);
        	}
        }

        if (prop.getHiddenFromDisplayBelowRoleLevel() != null) {
        	updatePropertyResourceURIValue(p, HIDDEN_FROM_DISPLAY_BELOW_ROLE_LEVEL_ANNOT, prop.getHiddenFromDisplayBelowRoleLevel().getURI());
        }

        if (prop.getProhibitedFromUpdateBelowRoleLevel() != null) {
        	updatePropertyResourceURIValue(p, PROHIBITED_FROM_UPDATE_BELOW_ROLE_LEVEL_ANNOT, prop.getProhibitedFromUpdateBelowRoleLevel().getURI());
        }

        if (prop.getHiddenFromPublishBelowRoleLevel() != null) {
        	updatePropertyResourceURIValue(p, HIDDEN_FROM_PUBLISH_BELOW_ROLE_LEVEL_ANNOT, prop.getHiddenFromPublishBelowRoleLevel().getURI());
        }

        updatePropertyStringValue(p,PROPERTY_CUSTOMENTRYFORMANNOT,prop.getCustomEntryForm(),ontModel);
        updatePropertyBooleanValue(p,PROPERTY_SELECTFROMEXISTINGANNOT,prop.getSelectFromExisting(),ontModel,JenaBaseDao.KEEP_ONLY_IF_FALSE);
        updatePropertyBooleanValue(p,PROPERTY_OFFERCREATENEWOPTIONANNOT,prop.getOfferCreateNewOption(),ontModel,JenaBaseDao.KEEP_ONLY_IF_TRUE);
        updatePropertyBooleanValue(p,PROPERTY_STUBOBJECTPROPERTYANNOT,prop.getStubObjectRelation(),ontModel,JenaBaseDao.KEEP_ONLY_IF_TRUE);
        updatePropertyBooleanValue(p,PROPERTY_COLLATEBYSUBCLASSANNOT,prop.getCollateBySubclass(),ontModel,JenaBaseDao.KEEP_ONLY_IF_TRUE);
        
        updatePropertyResourceURIValue(p, PROPERTY_INPROPERTYGROUPANNOT, prop.getGroupURI());
    }

    public void deleteObjectProperty(ObjectProperty prop) {
    	deleteProperty(prop,getOntModel());
    }

    public void deleteProperty(ObjectProperty prop, OntModel ontModel) {
        ontModel.enterCriticalSection(Lock.WRITE);
        getOntModel().getBaseModel().notifyEvent(new EditEvent(getWebappDaoFactory().getUserURI(),true));
        try {
            OntProperty op = ontModel.getOntProperty(prop.getURI());
            if (op != null) {            	
            	//Remove restriction class.
            	Iterator<Resource> restIt = ontModel.listSubjectsWithProperty(OWL.onProperty, op);
            	while(restIt.hasNext()) {
            		Resource restRes = restIt.next();
            		if (restRes.canAs(OntResource.class)) {
            			OntResource restOntRes = restRes.as(OntResource.class);
            			smartRemove(restOntRes, ontModel);
            		}
            	}            	
            	removeRulesMentioningResource(op, ontModel);
            	
            	//Remove vitro:DependentResource
            	List<Statement> depResStmts = new LinkedList<Statement>();
            	StmtIterator depResIt = ontModel.listStatements(null, op, (RDFNode)null);
            	while(depResIt.hasNext()) {
            		depResStmts.addAll(DependentResourceDeleteJena
            				.getDependentResourceDeleteList(depResIt.nextStatement(), ontModel) );
            	}
            	ontModel.remove(depResStmts);
                op.remove();
            }                    	
        	
            if ( (prop.getURIInverse() != null) && (prop.getURIInverse().length()>0) ) {
            	OntProperty invOp = ontModel.getOntProperty(prop.getURIInverse());
	            if (invOp != null) {
	            	Iterator<Resource> restIt = ontModel.listSubjectsWithProperty(OWL.onProperty, op);
	            	while(restIt.hasNext()) {
	            		Resource restRes = restIt.next();
	            		if (restRes.canAs(OntResource.class)) {
	            			OntResource restOntRes = restRes.as(OntResource.class);
	            			smartRemove(restOntRes, ontModel);
	            		}
	            	}
	            	removeRulesMentioningResource(invOp, ontModel);
	            	
	            	//bdc34: I'm not sure if we want to remove DependentResources on a ObjectProperty delete.
	                invOp.remove();
	            }
            }
        } finally {
        	getOntModel().getBaseModel().notifyEvent(new EditEvent(getWebappDaoFactory().getUserURI(),false));
            ontModel.leaveCriticalSection();
        }
        // remove ABox statements after removing property
        // so dependentResource deletion test will pass
        removeABoxStatementsWithPredicate(prop);
    }

    public List<ObjectProperty> getRootObjectProperties() {
        List rootProperties = new ArrayList();
        getOntModel().enterCriticalSection(Lock.READ);
        try {
        	ClosableIterator propIt;
        	try {
        		propIt = getOntModel().listObjectProperties();
        	} catch (ProfileException pe) {
        		propIt = getOntModel().listSubjectsWithProperty(RDF.type, RDF.Property);
        	}
            try {
                while (propIt.hasNext()) {
                	Resource res = (Resource) propIt.next();
                	if (res.canAs(OntProperty.class)) {
	                    org.apache.jena.ontology.OntProperty op = res.as(OntProperty.class);
	                    boolean isRoot = false;
	                    Iterator parentIt = op.listSuperProperties();
	                    if (parentIt != null) {
	                    	List<Property> parentList = new ArrayList<Property>();
	                    	try {
	                    		parentList.addAll(op.listSuperProperties().toList());
	                    	} catch (ConversionException ce) {
	                    		StmtIterator parentStmtIt = op.listProperties(RDFS.subPropertyOf);
	                    		while (parentStmtIt.hasNext()) {
	                    			Statement parentStmt = parentStmtIt.nextStatement();
	                    			if (parentStmt.getObject().isResource()) {
	                    				Resource parentRes = (Resource) parentStmt.getObject();
	                    				if (parentRes.getURI() != null) {
	                    					Property parentProperty = ResourceFactory.createProperty(parentRes.getURI());
	                    					parentList.add(parentProperty);
	                    				}
	                    			}
	                    		}
	                    		parentStmtIt.close();
	                    	}
	                    	if (parentList.size()==0) {
	                    		isRoot = true;
	                    	} else {
	                    		isRoot = true;
	                    	    Iterator<? extends Property> pit = parentList.iterator();
	                    	    while (pit.hasNext()) {
	                    	    	Property pt = pit.next();
	                    	    	if ( (!pt.equals(op)) && (!(getOntModel().contains(op,OWL.equivalentProperty,pt)) || (getOntModel().contains(pt,OWL.equivalentProperty,op))) ) {
	                    	    		isRoot = false;
	                    	    	}
	                    	    }
	                    	} 
	                    } else {
	                    	isRoot = true;
	                    }
	                    
	                    if (isRoot) {
	                        if (!NONUSER_NAMESPACES.contains(op.getNameSpace())) {
	                            rootProperties.add(propertyFromOntProperty(op));
	                        }
	                    }
                	}
                }
            } finally {
                propIt.close();
            }
        } finally {
            getOntModel().leaveCriticalSection();
        }
        if (rootProperties.size()==0)
            return null;
        else
            return rootProperties;
    }

    public List<String> getSuperPropertyURIs(String objectPropertyURI) {
        List supURIs = new ArrayList();
        try {
            Iterator supIt = getOntModel().getOntProperty(objectPropertyURI).listSuperProperties(true);
            while (supIt.hasNext()) {
                try {
                    OntProperty op = (OntProperty) supIt.next();
                    supURIs.add(op.getURI());
                } catch (Exception cce) {}
            }
        } catch (Exception e) {}
        return supURIs;
    }

    public List<String> getSubPropertyURIs(String objectPropertyURI) {
        List subURIs = new ArrayList();
        try {
            Iterator subIt = getOntModel().getOntProperty(objectPropertyURI).listSubProperties(true);
            while (subIt.hasNext()) {
                try {
                    OntProperty op = (OntProperty) subIt.next();
                    subURIs.add(op.getURI());
                } catch (Exception cce) {}
            }
        } catch (Exception e) {}
        return subURIs;
    }

    // checks for annotation property vitro:skipEditForm.
    // Used by N3 editing system to govern behavior of edit link:
    // if annotation is present, editing system redirects to display
    // the object individual instead of a normal editing form.
    public boolean skipEditForm(String predicateURI) {
    	OntModel ontModel = getOntModel();
    	try {
    		ontModel.enterCriticalSection(Lock.READ);
    		if (getOntModel().contains(
    				ontModel.getResource(predicateURI), 
    				ontModel.getProperty(VitroVocabulary.SKIP_EDIT_FORM),
    				ontModel.createTypedLiteral("true", XSDDatatype.XSDboolean) ) ) {
    			return true;
    		}
    	} finally {
    		ontModel.leaveCriticalSection();
    	}
    	return false;
    }

    /*
     * SPARQL-based methods for getting the individual's object properties.
     * Ideally this implementation should replace the existing way of getting
     * the object property list, but the consequences of this may be far-reaching,
     * so we are implementing a new method now and will merge the old approach
     * into the new one in a future release.
     */
    
    protected static final List<String> EXCLUDED_NAMESPACES = Arrays.asList(
                                                                            //"http://www.w3.org/2002/07/owl#"            
        ); 
    /*
     * This is a hack to throw out properties in the vitro, rdf, rdfs, and owl namespaces.
     * It will be implemented in a better way in v1.3 (Editing and Display Configuration).
     */
    protected static final String PROPERTY_FILTERS;
    static {
        List<String> namespaceFilters = new ArrayList<String>();
        for (String namespace : EXCLUDED_NAMESPACES) {
            namespaceFilters.add("( !regex(str(?property), \"^" + namespace + "\" ))");
        }
        // A hack to include the vitro:primaryLink and vitro:additionalLink properties in the list
        namespaceFilters.add("( ?property = vitro:primaryLink ||" +
                               "?property = vitro:additionalLink ||" +
                               "!regex(str(?property), \"^http://vitro.mannlib.cornell.edu/ns/vitro/0.7#\" ))");
        PROPERTY_FILTERS = StringUtils.join(namespaceFilters, " && ");
    }   
    
    protected static final String OBJECT_PROPERTY_QUERY_STRING = 
        PREFIXES + "\n" +
        "SELECT DISTINCT ?property ?objType WHERE { \n" +
        "   ?subject ?property ?object . \n" +
        "   ?object a ?objType . \n" + 
//        "   ?property a owl:ObjectProperty . \n" +
        "   FILTER ( \n" +
        "       isURI(?object) && \n" +
                PROPERTY_FILTERS + "\n" +
        "   ) \n" +
        "} ORDER BY ?property ?objType";    
    
    @Override
    public List<ObjectProperty> getObjectPropertyList(Individual subject) {
        return getObjectPropertyList(subject.getURI());
    }

    // Returns a list of ObjectProperty objects for which statements exist about
    // the individual.  Note that this method now returns additional copies of
    // a given predicate, with the rangeVClassURI changed to indicate the distinct
    // types of the related objects.  This supports finding the approriate list
    // views for the "faux" qualified properties.
    @Override
    public List<ObjectProperty> getObjectPropertyList(String subjectUri) {

        // Due to a Jena bug, prebinding on ?subject combined with the isURI()
        // filter causes the query to fail. Using string concatenation to insert the
        // subject uri instead.
        String queryString = QueryUtils.subUriForQueryVar(OBJECT_PROPERTY_QUERY_STRING, "subject", subjectUri); 

        Query query = null;
        try {
            query = QueryFactory.create(queryString);
        } catch(Throwable th){
            log.error("could not create SPARQL query for query string " + th.getMessage());
            log.error(queryString);
            return null;
        } 
        log.debug("Object property query:\n" + query);
        

        final List<ObjectProperty> properties = new ArrayList<ObjectProperty>();
        getPropertyQueryResults(queryString, new ResultSetConsumer() {
            ObjectProperty propRegister = new ObjectProperty();
            {
                propRegister.setURI("");
            }

            @Override
            protected void processQuerySolution(QuerySolution qs) {
                Resource resource = qs.getResource("property");
                String uri = resource.getURI();
                Resource objType = qs.getResource("objType");
                String objTypeUri = objType.getURI();
                log.debug("Found populated object property " + uri + " with object type " + objType);
                ObjectProperty property = null;
                if (uri.equals(propRegister.getURI())) {
                    property = propRegister.clone();
                } else {
                    ObjectProperty newProperty = getObjectPropertyByURI(uri);
                    if (newProperty != null) {
                        propRegister = newProperty;
                        // add canonical instance of the property first in the list
                        // before the range-changed versions
                        properties.add(newProperty);
                        // isolate the canonical prop from what's about to happen next
                        property = newProperty.clone();
                    }
                }
                if (property != null) {
                    property.setRangeVClassURI(objTypeUri);
                    properties.add(property);
                }
            }
        });

        return properties;
    }

    protected static final String LIST_VIEW_CONFIG_FILE_QUERY_STRING =
        "PREFIX display: <http://vitro.mannlib.cornell.edu/ontologies/display/1.1#> \n" +
        "PREFIX config: <http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#> \n" +
        "SELECT ?property ?range ?domain ?filename WHERE { \n" +
        "    { ?property display:listViewConfigFile ?filename \n" +
        "    } UNION { \n" +
        "        ?configuration config:listViewConfigFile ?filename . \n " +
        "        ?context config:hasConfiguration ?configuration . \n" +
        "        ?context config:configContextFor ?property . \n" +
        "        ?context config:qualifiedBy ?range . \n" +
        "        OPTIONAL { ?context config:qualifiedByDomain ?domain } \n" +
        "    } \n" +
        "}";
        
    protected static Query listViewConfigFileQuery = null;
    static {
        try {
            listViewConfigFileQuery = QueryFactory.create(LIST_VIEW_CONFIG_FILE_QUERY_STRING);
        } catch(Throwable th){
            log.error("could not create SPARQL query for LIST_VIEW_CONFIG_FILE_QUERY_STRING " + th.getMessage());
            log.error(LIST_VIEW_CONFIG_FILE_QUERY_STRING);
        }           
    }
    
    //TODO private void addPropertyClassCombinationsToListViewMap(HashMap)    
    
    // Map key is inner pair of object property and range class URI,
    // with first member of outer pair being a domain class URI.
    // If domain or range is unspecified, OWL.Thing.getURI() is used in the key.
    Map<FullPropertyKey, String> customListViewConfigFileMap;
    
    @Override
    public String getCustomListViewConfigFileName(ObjectProperty op) {
        if (customListViewConfigFileMap == null) {
            customListViewConfigFileMap = new HashMap<>();
        }
        if (customListViewConfigFileMap.isEmpty()) {
            long start = System.currentTimeMillis();
            OntModel displayModel = getOntModelSelector().getDisplayModel();
            //Get all property to list view config file mappings in the system
            QueryExecution qexec = QueryExecutionFactory.create(listViewConfigFileQuery, displayModel); 
            ResultSet results = qexec.execSelect();  
            //Iterate through mappings looking for the current property and setting up a hashmap for subsequent retrieval
            while (results.hasNext()) {
                QuerySolution soln = results.next();
                String propertyUri = soln.getResource("property").getURI();
                RDFNode rangeNode = soln.get("range");
                String rangeUri = (rangeNode != null)
                        ? ((Resource) rangeNode).getURI()
                        : OWL.Thing.getURI();
                RDFNode domainNode = soln.get("domain");
                String domainUri = (domainNode != null)
                        ? ((Resource) domainNode).getURI()
                        : OWL.Thing.getURI();
                ObjectProperty prop = getObjectPropertyByURI(propertyUri);
                if (prop == null) {
                	//This is a warning only if this property is the one for which we're searching
                	if(op.getURI().equals(propertyUri)){
                		log.warn("Can't find property for uri " + propertyUri);
                	} else {
                		log.debug("Can't find property for uri " + propertyUri);
                	}
                } else {
                    String filename = soln.getLiteral("filename").getLexicalForm();
                    FullPropertyKey key = new FullPropertyKey(domainUri, prop.getURI(), rangeUri);
                    log.debug("putting " + key + " " + filename + " into list view map");
					customListViewConfigFileMap.put(key, filename);     
                }
            }
            // If there are no custom list views, put a bogus entry in the map
            // to avoid further recomputation
            if (customListViewConfigFileMap.isEmpty()) {
                customListViewConfigFileMap.put(
                        new FullPropertyKey(null, OWL.NS + "bottomObjectProperty", null), 
                        "nothing");
            }
            qexec.close();
        }
        FullPropertyKey key = new FullPropertyKey((RoleRestrictedProperty)op);
		String customListViewConfigFileName = customListViewConfigFileMap.get(key);
        if (customListViewConfigFileName == null) {
            log.debug("no list view found for " + key);
            key = new FullPropertyKey(null, op.getURI(), op.getRangeVClassURI());
			customListViewConfigFileName = customListViewConfigFileMap.get(key);
        }
        if (customListViewConfigFileName == null) {
        	log.debug("no list view found for " + key);
            key = new FullPropertyKey(null, op.getURI(), null);
			customListViewConfigFileName = customListViewConfigFileMap.get(key);
        }
        
        return customListViewConfigFileName;
    }

}
