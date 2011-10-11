/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.AllValuesFromRestriction;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.ontology.ProfileException;
import com.hp.hpl.jena.ontology.Restriction;
import com.hp.hpl.jena.ontology.SomeValuesFromRestriction;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Property;
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

import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.Ontology;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyInstance;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.InsertException;
import edu.cornell.mannlib.vitro.webapp.dao.OntologyDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.jena.event.EditEvent;
import edu.cornell.mannlib.vitro.webapp.dao.jena.pellet.PelletListener;

public class DataPropertyDaoJena extends PropertyDaoJena implements
        DataPropertyDao {
    
    protected static final Log log = LogFactory.getLog(DataPropertyDaoJena.class.getName());
    
    private class DataPropertyRanker implements Comparator<DataProperty> {
        @Override
		public int compare (DataProperty dp1, DataProperty dp2) {
            int diff = dp1.getDisplayTier() - dp2.getDisplayTier();
            if (diff==0)
                return dp1.getPublicName().compareTo(dp2.getPublicName());
            else
                return diff;
        }
    }

    public DataPropertyDaoJena(DatasetWrapperFactory dwf, 
                               WebappDaoFactoryJena wadf) {
        super(dwf, wadf);
    }

    public void deleteDataProperty(DataProperty dtp) {
        deleteDataProperty(dtp.getURI());
    }

    public void deleteDataProperty(String URI) {
        deleteDataProperty(URI, getOntModelSelector().getTBoxModel());
    }

    public boolean annotateDataPropertyAsExternalIdentifier(String dataPropertyURI) {
    	OntModel ontModel = getOntModelSelector().getTBoxModel();
    	ontModel.enterCriticalSection(Lock.WRITE);
    	try {
	        com.hp.hpl.jena.ontology.OntResource ind = ontModel.getOntResource(dataPropertyURI);
	        if( ind != null ){
	            ontModel.add(ind,(Property)DATAPROPERTY_ISEXTERNALID, "TRUE");
	            return true;
	        }else{
	            return false;
	        }
    	} finally {
    		ontModel.leaveCriticalSection();
    	}
    }

    public void deleteDataProperty(String URI, OntModel ontModel) {
    	// TODO check if used as onProperty of restriction
    	ontModel.enterCriticalSection(Lock.WRITE);
    	try {
    		getOntModel().getBaseModel().notifyEvent(new EditEvent(getWebappDaoFactory().getUserURI(),true));
	        DatatypeProperty dp = ontModel.getDatatypeProperty(URI);
	        if (dp != null) {
	           	Iterator<Resource> restIt = ontModel.listSubjectsWithProperty(OWL.onProperty, dp);
            	while(restIt.hasNext()) {
            		Resource restRes = restIt.next();
            		if (restRes.canAs(OntResource.class)) {
            			OntResource restOntRes = (OntResource) restRes.as(OntResource.class);
            			smartRemove(restOntRes, ontModel);
            		}
            	}
            	removeRulesMentioningResource(dp, ontModel);
                dp.remove();
	        }
	        getOntModel().getBaseModel().notifyEvent(new EditEvent(getWebappDaoFactory().getUserURI(),false));
    	} finally {
    		ontModel.leaveCriticalSection();
    	}
        // remove ABox statements after removing property
        // so dependentResource deletion test will pass
        removeABoxStatementsWithPredicate(URI);
    }

    @Override
	public void fillDataPropertiesForIndividual(Individual entity) {
        if( entity == null ) return;
        List<DataProperty> dataprops = new ArrayList<DataProperty>();
        dataprops.addAll( getDataPropertyStatements(entity.getDataPropertyStatements()) );
        entity.setDatatypePropertyList(dataprops);
    }

    private List<DataProperty> getDataPropertyStatements(List<DataPropertyStatement> dataPropertyStmts) {
		if (dataPropertyStmts == null || dataPropertyStmts.isEmpty()) {
			return new ArrayList<DataProperty>();
		}

		HashMap<String, DataProperty> hash = new HashMap<String, DataProperty>();
        for (DataPropertyStatement dataPropertyStmt: dataPropertyStmts) {
            if (hash.containsKey(dataPropertyStmt.getDatapropURI())) {
                DataProperty p = hash.get(dataPropertyStmt.getDatapropURI());
                p.addDataPropertyStatement(dataPropertyStmt);
            } else {
            	OntModel ontModel = getOntModelSelector().getTBoxModel();
            	ontModel.enterCriticalSection(Lock.READ);
	            try {
	                OntProperty op = ontModel.getOntProperty(dataPropertyStmt.getDatapropURI());
	                if (op != null) {
	                    DataProperty p = datapropFromOntProperty(op);
	                    hash.put(p.getURI(),p);
	                    p.addDataPropertyStatement(dataPropertyStmt);
	                }
            	} finally {
            		ontModel.leaveCriticalSection();
            	}
            }
        }

        List<DataProperty> dataprops = new ArrayList<DataProperty>(hash.values());
        Collections.sort(dataprops, new DataPropertyRanker());
        return dataprops;
    }

    private DataProperty datapropFromOntProperty(OntProperty op) {
	if (op==null) {
	    return null;
	}
        if (op.getURI()==null) {
            return null;
        } else {
            DataProperty dp = new DataProperty();
            dp.setURI(op.getURI());
            dp.setNamespace(op.getNameSpace());
            dp.setLocalName(op.getLocalName());
            OntologyDao oDao=getWebappDaoFactory().getOntologyDao();
            Ontology o = (Ontology)oDao.getOntologyByURI(dp.getNamespace());
            if (o==null) {
                if (!VitroVocabulary.vitroURI.equals(dp.getNamespace())) {
                    log.debug("datapropFromOntProperty(): no ontology object found for the namespace "+dp.getNamespace());
                }
            } else {
                dp.setLocalNameWithPrefix(o.getPrefix()==null?(o.getName()==null?"unspec:"+dp.getLocalName():o.getName()+":"+dp.getLocalName()):o.getPrefix()+":"+dp.getLocalName());
                dp.setPickListName(dp.getLocalName()+o.getPrefix()==null?(o.getName()==null?" (unspec:)":" ("+o.getName()+")"):" ("+o.getPrefix()+")");
            }
            dp.setName(op.getLocalName());
            dp.setPublicName(getLabelOrId(op));
            Resource dRes = op.getDomain();
            if (dRes != null) {
                dp.setDomainClassURI(dRes.getURI());
            }
            Resource rRes = op.getRange();
            if (rRes != null) {
                dp.setRangeDatatypeURI(rRes.getURI());
            }
            if (op.isFunctionalProperty()) {
            	dp.setFunctional(true);
            }
            dp.setExample(getPropertyStringValue(op,EXAMPLE_ANNOT));
            dp.setDescription(getPropertyStringValue(op,DESCRIPTION_ANNOT));
            dp.setPublicDescription(getPropertyStringValue(op,PUBLIC_DESCRIPTION_ANNOT));
            dp.setDisplayTier(((WebappDaoFactoryJena)getWebappDaoFactory()).getJenaBaseDao().getPropertyNonNegativeIntValue(op, DISPLAY_RANK_ANNOT));
            dp.setDisplayLimit(((WebappDaoFactoryJena)getWebappDaoFactory()).getJenaBaseDao().getPropertyNonNegativeIntValue(op, DISPLAY_LIMIT));
            
            //There might be multiple HIDDEN_FROM_DISPLAY_BELOW_ROLE_LEVEL_ANNOT properties, only use the highest
            StmtIterator it = op.listProperties(HIDDEN_FROM_DISPLAY_BELOW_ROLE_LEVEL_ANNOT);
            BaseResourceBean.RoleLevel hiddenRoleLevel = null;
            while( it.hasNext() ){
                Statement stmt = it.nextStatement();
                RDFNode obj;
                if( stmt != null && (obj = stmt.getObject()) != null && obj.isURIResource() ){
                    Resource res = (Resource)obj.as(Resource.class);
                    if( res != null && res.getURI() != null ){
                        BaseResourceBean.RoleLevel roleFromModel =  BaseResourceBean.RoleLevel.getRoleByUri(res.getURI());
                        if( roleFromModel != null && 
                            (hiddenRoleLevel == null || roleFromModel.compareTo(hiddenRoleLevel) > 0 )){
                            hiddenRoleLevel = roleFromModel;                            
                        }
                    }
                }
            }            
            dp.setHiddenFromDisplayBelowRoleLevel(hiddenRoleLevel);//this might get set to null

            //There might be multiple PROHIBITED_FROM_UPDATE_BELOW_ROLE_LEVEL_ANNOT properties, only use the highest
            it = op.listProperties(PROHIBITED_FROM_UPDATE_BELOW_ROLE_LEVEL_ANNOT);
            BaseResourceBean.RoleLevel prohibitedRoleLevel = null;
            while( it.hasNext() ){
                Statement stmt = it.nextStatement();
                RDFNode obj;
                if( stmt != null && (obj = stmt.getObject()) != null && obj.isURIResource() ){
                    Resource res = (Resource)obj.as(Resource.class);
                    if( res != null && res.getURI() != null ){
                        BaseResourceBean.RoleLevel roleFromModel =  BaseResourceBean.RoleLevel.getRoleByUri(res.getURI());
                        if( roleFromModel != null && 
                            (prohibitedRoleLevel == null || roleFromModel.compareTo(prohibitedRoleLevel) > 0 )){
                            prohibitedRoleLevel = roleFromModel;                            
                        }
                    }
                }
            }            
            dp.setProhibitedFromUpdateBelowRoleLevel(prohibitedRoleLevel);//this might get set to null
            dp.setCustomEntryForm(getPropertyStringValue(op,PROPERTY_CUSTOMENTRYFORMANNOT));
            
            dp.setExternalId( getOntModelSelector().getTBoxModel().contains(op, DATAPROPERTY_ISEXTERNALID, "TRUE") );
            Resource groupRes = (Resource) op.getPropertyValue(PROPERTY_INPROPERTYGROUPANNOT);
            if (groupRes != null) {
                dp.setGroupURI(groupRes.getURI());
            }
            return dp;
        }
    }

    public List getAllDataProperties() {
        List allDataprops = new ArrayList();
        OntModel ontModel = getOntModelSelector().getTBoxModel();
        try {
	        ontModel.enterCriticalSection(Lock.READ);
	        try {
	            ClosableIterator dataprops = ontModel.listDatatypeProperties();
	            try {
	                while (dataprops.hasNext()) {
	                    com.hp.hpl.jena.ontology.DatatypeProperty jDataprop = (com.hp.hpl.jena.ontology.DatatypeProperty) dataprops.next();
	                    DataProperty dataprop = datapropFromOntProperty(jDataprop);
	                    if (dataprop != null && !(NONUSER_NAMESPACES.contains(dataprop.getNamespace())))
	                        allDataprops.add(dataprop);
	                }
	            } finally {
	                dataprops.close();
	            }
	        } finally {
	            ontModel.leaveCriticalSection();
	        }
        } catch (ProfileException pe) {
        	// TODO language profile doesn't support data properties.
        	// With RDFS, we might like to return properties with rdfs:range containing a datatype
        }
        return allDataprops;
    }

    public List getAllExternalIdDataProperties() {
        List allDataprops = new ArrayList();
        OntModel ontModel = getOntModelSelector().getTBoxModel();
        ontModel.enterCriticalSection(Lock.READ);
        try {
            ClosableIterator dataprops = ontModel.listDatatypeProperties();
            try {
                while (dataprops.hasNext()) {
                    com.hp.hpl.jena.ontology.DatatypeProperty jDataprop = (com.hp.hpl.jena.ontology.DatatypeProperty) dataprops.next();
                    DataProperty dataprop = datapropFromOntProperty(jDataprop);
                    if (dataprop != null && ontModel.contains(jDataprop, DATAPROPERTY_ISEXTERNALID, ResourceFactory.createTypedLiteral(true)))
                        allDataprops.add(dataprop);
                }
            } finally {
                dataprops.close();
            }
        } finally {
            ontModel.leaveCriticalSection();
        }
        if (allDataprops.size() < 0)
            return null;
        else
            return allDataprops;
    }

    public List<DataProperty> getDataPropertiesForVClass(String vclassURI) {
        return getDatapropsForClass(vclassURI);
    }

    public List<DataProperty> getDatapropsForClass(String vclassURI) {
        return filterAndConvertToDataProperties(getAllPropInstByVClass(vclassURI));
    }
    
    public Collection<DataProperty> getAllPossibleDatapropsForIndividual(String individualURI) {
    	return filterAndConvertToDataProperties(getAllPossiblePropInstForIndividual(individualURI));
    }
    
    private List<DataProperty> filterAndConvertToDataProperties(
    		                                List<PropertyInstance> propInsts) {
    	List<DataProperty> dataprops = new ArrayList<DataProperty>();
    	for (PropertyInstance propInst : propInsts) {
       		OntModel tboxModel = getOntModel();
    		tboxModel.enterCriticalSection(Lock.READ);
    		boolean add = false;
    		try { 
    			add = (propInst.getPropertyURI() != null 
    					&& tboxModel.contains(
    							tboxModel.getResource(
    									propInst.getPropertyURI()), 
    									RDF.type, 
    									OWL.DatatypeProperty));
	    	} finally {
	    		tboxModel.leaveCriticalSection();
	    	}
	    	if (add) {
	    		DataProperty dataprop = getDataPropertyByURI(propInst.getPropertyURI());
	    		dataprop.setRangeDatatypeURI(propInst.getRangeClassURI());
	    		dataprops.add(dataprop);
	    	}	    	
    	}
        return dataprops;
    }

    protected boolean reasoningAvailable() {
    	PelletListener pl = getWebappDaoFactory().getPelletListener();
    	return !(
    			( pl == null || !pl.isConsistent() || pl.isInErrorState() )
    	);
    }
    
    private String getRequiredDatatypeURI(Individual individual, DataProperty dataprop, List<String> vclassURIs) {
        OntModel ontModel = getOntModelSelector().getTBoxModel();
        String datatypeURI = dataprop.getRangeDatatypeURI();    
    
        ontModel.enterCriticalSection(Lock.READ);
        try {
            // get universal restrictions applicable to data property
            Iterator<Resource> restIt = ontModel.listSubjectsWithProperty(OWL.onProperty, ontModel.getResource(dataprop.getURI()));
            while (restIt.hasNext()) {
                Resource restRes = restIt.next();
                if (restRes.canAs(Restriction.class)) {
                    Restriction rest = (Restriction) restRes.as(Restriction.class);
                    if (rest.isAllValuesFromRestriction()) {
                        AllValuesFromRestriction avfrest = rest.asAllValuesFromRestriction();
                        if (avfrest.getAllValuesFrom() != null) {
                            // check if the individual has the restriction as one of its types
                            if (!individual.isAnonymous() &&
                                ontModel.contains(ontModel.getResource(individual.getURI()),
                                                  RDF.type,
                                                  rest)
                                ) {
                                        datatypeURI = convertRequiredDatatypeURI(
                                                avfrest.getAllValuesFrom().getURI());
                                        break; 
                            } else {
                                // check if the restriction applies to one of the individual's types
                                List<Resource> equivOrSubResources = new ArrayList<Resource>();
                                equivOrSubResources.addAll(ontModel.listSubjectsWithProperty(RDFS.subClassOf, rest).toList());
                                equivOrSubResources.addAll(ontModel.listSubjectsWithProperty(OWL.equivalentClass, rest).toList());
                                for(Resource equivOrSubRes : equivOrSubResources) {
                                    if (!equivOrSubRes.isAnon() && vclassURIs.contains(equivOrSubRes.getURI())) {
                                        datatypeURI = convertRequiredDatatypeURI(
                                                avfrest.getAllValuesFrom().getURI());
                                        break;
                                    }
                                }
                            }
                        } 
                    }
                }
            }
        } finally {
            ontModel.leaveCriticalSection();
        }
        return datatypeURI;
    }
    
    /**
     * Converts datatypes used in allValuesFromRestrictions to actual
     * requirements for editing.  Initially, this means we filter out
     * rdfs:Literal because we don't want to populate literals with this
     * as a datatype.
     */
    private String convertRequiredDatatypeURI(String datatypeURI) {
        return (RDFS.Literal.getURI().equals(datatypeURI)) 
                ? null 
                : datatypeURI;
    }
    
    public String getRequiredDatatypeURI(Individual individual, DataProperty dataprop) {    	    		    	
    	return getRequiredDatatypeURI(individual,dataprop,getVClassURIs(individual));
    }
    
    private List<String> getVClassURIs(Individual individual){
        List<String> vclassURIs = null;
        if (reasoningAvailable()) {
            vclassURIs = new ArrayList<String>();
            for (VClass vc : individual.getVClasses(INDIRECT)) {
                if (vc.getURI() != null) {
                    vclassURIs.add(vc.getURI());
                }
            }
        } else {
            vclassURIs = getSupertypeURIs(individual);
        }
        return vclassURIs;
    }
    
    private boolean DIRECT = true;
    private boolean INDIRECT = !DIRECT;
    
    /**
     * This method will iterate through each of an individual's direct types
     * and get the URIs of all supertypes.
     */
    protected List<String> getSupertypeURIs(Individual ind) {
    	List<String> supertypeURIs = new ArrayList<String>();
    	if (ind.getVClasses() != null) {
	    	for (VClass vc : ind.getVClasses(DIRECT)) {
	    		String vcURI = vc.getURI();
	    		if (vcURI != null) {
	    			supertypeURIs.add(vcURI);
	    			supertypeURIs.addAll(
	    				getWebappDaoFactory().getVClassDao().getAllSuperClassURIs(vcURI)
	    			);
	    		}
	    		
	    	}
    	} else if (ind.getVClassURI() != null) { 
    		supertypeURIs.add(ind.getVClassURI());
    		supertypeURIs.addAll(
    		    getWebappDaoFactory().getVClassDao().getAllSuperClassURIs(ind.getVClassURI())
    		);
    	}
    	return supertypeURIs;
    }
    
    public DataProperty getDataPropertyByURI(String dataPropertyURI) {
        return datapropFromOntProperty(getOntModelSelector().getTBoxModel().getDatatypeProperty(dataPropertyURI));
    }

    public String insertDataProperty(DataProperty dtp) throws InsertException{
        return insertDataProperty(dtp, getOntModelSelector().getTBoxModel());
    }

    public String insertDataProperty(DataProperty dtp, OntModel ontModel) throws InsertException {
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
        	getOntModel().getBaseModel().notifyEvent(new EditEvent(getWebappDaoFactory().getUserURI(),true));
        	String errMsgStr = getWebappDaoFactory().checkURI(dtp.getURI());
        	if (errMsgStr != null) {
        		throw new InsertException(errMsgStr);
        	}
            com.hp.hpl.jena.ontology.DatatypeProperty jDataprop = ontModel.createDatatypeProperty(dtp.getURI());
            if (dtp.getPublicName() != null && dtp.getPublicName().length() > 0) {
            	jDataprop.setLabel(dtp.getPublicName(), (String) getDefaultLanguage());
            } else {
            	jDataprop.removeAll(RDFS.label);
            }
            jDataprop.removeAll(RDFS.domain);
            if ( (dtp.getDomainClassURI() != null) && (dtp.getDomainClassURI().length()>0) ) {
            	jDataprop.setDomain(ontModel.getResource(dtp.getDomainClassURI()));
            }
            if (dtp.getRangeDatatypeURI() != null && !dtp.getRangeDatatypeURI().equals("")) {
                Resource rangeResource = ontModel.getResource(dtp.getRangeDatatypeURI());
                if (rangeResource != null)
                    jDataprop.setRange(rangeResource);
            }
            if (dtp.getFunctional()) {
               	ontModel.add(jDataprop,RDF.type,OWL.FunctionalProperty);
            }
            addPropertyStringValue(jDataprop, EXAMPLE, dtp.getExample(), ontModel);
            addPropertyStringValue(jDataprop, DESCRIPTION_ANNOT, dtp.getDescription(), ontModel);
            addPropertyStringValue(jDataprop, PUBLIC_DESCRIPTION_ANNOT, dtp.getPublicDescription(), ontModel);
            addPropertyNonNegativeIntValue(jDataprop, DISPLAY_RANK_ANNOT, dtp.getDisplayTier(), ontModel);
            addPropertyNonNegativeIntValue(jDataprop, DISPLAY_LIMIT, dtp.getDisplayLimit(), ontModel);
            //addPropertyStringValue(jDataprop, HIDDEN_ANNOT, dtp.getHidden(), ontModel);
            jDataprop.removeAll(HIDDEN_FROM_DISPLAY_BELOW_ROLE_LEVEL_ANNOT);
            if (HIDDEN_FROM_DISPLAY_BELOW_ROLE_LEVEL_ANNOT != null && dtp.getHiddenFromDisplayBelowRoleLevel() != null) { // only need to add if present
                jDataprop.addProperty(HIDDEN_FROM_DISPLAY_BELOW_ROLE_LEVEL_ANNOT, ResourceFactory.createResource(dtp.getHiddenFromDisplayBelowRoleLevel().getURI()));
            }
            jDataprop.removeAll(PROHIBITED_FROM_UPDATE_BELOW_ROLE_LEVEL_ANNOT);
            if (PROHIBITED_FROM_UPDATE_BELOW_ROLE_LEVEL_ANNOT != null && dtp.getProhibitedFromUpdateBelowRoleLevel() != null) { // only need to add if present
                jDataprop.addProperty(PROHIBITED_FROM_UPDATE_BELOW_ROLE_LEVEL_ANNOT, ResourceFactory.createResource(dtp.getProhibitedFromUpdateBelowRoleLevel().getURI()));
            }
            /*
            if (dtp.isSelfEditProhibited()) { // only add the property if it's true
                addPropertyBooleanValue(jDataprop, PROPERTY_SELFEDITPROHIBITEDANNOT, dtp.isSelfEditProhibited(), ontModel);
            }
            if (dtp.isCuratorEditProhibited()) { // only add the property if it's true
                addPropertyBooleanValue(jDataprop, PROPERTY_CURATOREDITPROHIBITEDANNOT, dtp.isCuratorEditProhibited(), ontModel);
            } */
            try {
            	if (dtp.getGroupURI() != null && dtp.getGroupURI().length()>0) {
                	String badURIErrorStr = checkURI(dtp.getGroupURI());
                	if (badURIErrorStr == null) {
                		jDataprop.addProperty(PROPERTY_INPROPERTYGROUPANNOT, ontModel.getResource(dtp.getGroupURI()));
                	} else {
                		log.error(badURIErrorStr);
                	}
            	}
            } catch (Exception e) {
                log.error("error linking data property "+dtp.getURI()+" to property group");
            }
            addPropertyStringValue(jDataprop,PROPERTY_CUSTOMENTRYFORMANNOT,dtp.getCustomEntryForm(),ontModel);
            getOntModel().getBaseModel().notifyEvent(new EditEvent(getWebappDaoFactory().getUserURI(),false));
        } finally {
            ontModel.leaveCriticalSection();
        }
        return dtp.getURI();
    }

    public void updateDataProperty(DataProperty dtp) {
        updateDataProperty(dtp, getOntModelSelector().getTBoxModel());
    }

    public void updateDataProperty(DataProperty dtp, OntModel ontModel) {
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
        	getOntModel().getBaseModel().notifyEvent(new EditEvent(getWebappDaoFactory().getUserURI(),true));
            com.hp.hpl.jena.ontology.DatatypeProperty jDataprop = ontModel.getDatatypeProperty(dtp.getURI());
            
            updateRDFSLabel(jDataprop, dtp.getPublicName());
            
            updatePropertyResourceURIValue(jDataprop, RDFS.domain,dtp.getDomainClassURI(),ontModel);
            updatePropertyResourceURIValue(jDataprop, RDFS.range,dtp.getRangeDatatypeURI(),ontModel);
            
            if (dtp.getFunctional()) {
               	if (!ontModel.contains(jDataprop,RDF.type,OWL.FunctionalProperty)) {
            		ontModel.add(jDataprop,RDF.type,OWL.FunctionalProperty);
            	}
            } else {
            	if (ontModel.contains(jDataprop,RDF.type,OWL.FunctionalProperty)) {
            		ontModel.remove(jDataprop,RDF.type,OWL.FunctionalProperty);
            	}
            }
            
            updatePropertyStringValue(jDataprop, EXAMPLE, dtp.getExample(), ontModel);
            updatePropertyStringValue(jDataprop, DESCRIPTION_ANNOT, dtp.getDescription(), ontModel);
            updatePropertyStringValue(jDataprop, PUBLIC_DESCRIPTION_ANNOT, dtp.getPublicDescription(), ontModel);
            updatePropertyNonNegativeIntValue(jDataprop, DISPLAY_RANK_ANNOT, dtp.getDisplayTier(), ontModel);
            updatePropertyNonNegativeIntValue(jDataprop, DISPLAY_LIMIT, dtp.getDisplayLimit(), ontModel);
           
            if (dtp.getHiddenFromDisplayBelowRoleLevel() != null) {
              updatePropertyResourceURIValue(jDataprop,HIDDEN_FROM_DISPLAY_BELOW_ROLE_LEVEL_ANNOT,dtp.getHiddenFromDisplayBelowRoleLevel().getURI(),ontModel);                    
            }            
            
            if (dtp.getProhibitedFromUpdateBelowRoleLevel() != null) {
                updatePropertyResourceURIValue(jDataprop,PROHIBITED_FROM_UPDATE_BELOW_ROLE_LEVEL_ANNOT,dtp.getProhibitedFromUpdateBelowRoleLevel().getURI(),ontModel);                    
            }            

            if (dtp.getGroupURI() != null) {
                updatePropertyResourceURIValue(jDataprop,PROPERTY_INPROPERTYGROUPANNOT,dtp.getGroupURI(),ontModel);                    
            }                        
                        
            updatePropertyStringValue(jDataprop,PROPERTY_CUSTOMENTRYFORMANNOT,dtp.getCustomEntryForm(),ontModel);
            getOntModel().getBaseModel().notifyEvent(new EditEvent(getWebappDaoFactory().getUserURI(),false));
        } finally {
            ontModel.leaveCriticalSection();
        }
    }
    
    public List<DataProperty> getRootDataProperties() {
        List rootProperties = new ArrayList();
        OntModel ontModel = getOntModelSelector().getTBoxModel();
        ontModel.enterCriticalSection(Lock.READ);
        try {
            ClosableIterator propIt = ontModel.listDatatypeProperties();
            try {
                while (propIt.hasNext()) {
                    com.hp.hpl.jena.ontology.DatatypeProperty op = (com.hp.hpl.jena.ontology.DatatypeProperty) propIt.next();
                    boolean isRoot = false;
                    Iterator parentIt = op.listSuperProperties();
                    if (parentIt != null) {
                    	List<Property> parentList = new ArrayList<Property>();
                    	Iterator<RDFNode> parentNodeIt = op.listPropertyValues(RDFS.subPropertyOf);
                    	while (parentNodeIt.hasNext()) {
                    		RDFNode parentNode = parentNodeIt.next();
                    		if (parentNode.canAs(Property.class)) {
                    			parentList.add((Property) parentNode.as(Property.class));
                    		}
                    	}
                    	if (parentList.size()==0) {
                    		isRoot = true;
                    	} else {
                    		isRoot = true;
                    	    Iterator<Property> pit = parentList.iterator();
                    	    while (pit.hasNext()) {
                    	    	Property pt = pit.next();
                    	    	if ( (!pt.equals(op)) && (!(ontModel.contains(op,OWL.equivalentProperty,pt)) || (ontModel.contains(pt,OWL.equivalentProperty,op))) ) {
                    	    		isRoot = false;
                    	    	}
                    	    }
                    	} 
                    } else {
                    	isRoot = true;
                    }
                    
                    if (isRoot) {
                        if (!NONUSER_NAMESPACES.contains(op.getNameSpace())) {
                            rootProperties.add(datapropFromOntProperty(op));
                        }
                    }
                }
            } finally {
                propIt.close();
            }
        } finally {
            ontModel.leaveCriticalSection();
        }
        if (rootProperties.size()==0)
            return null;
        else
            return rootProperties;
    }

    /*
     * SPARQL-based methods for getting the individual's data properties.
     * Ideally this implementation should replace the existing way of getting
     * the data property list, but the consequences of this may be far-reaching,
     * so we are implementing a new method now and will merge the old approach
     * into the new one in a future release.
     */
    
    /* This may be the intent behind JenaBaseDao.NONUSER_NAMESPACES, but that
     * value does not contain all of these namespaces.
     */
    protected static final List<String> EXCLUDED_NAMESPACES = Arrays.asList(
            // Don't need to exclude these, because they are not owl:DatatypeProperty
            //"http://www.w3.org/1999/02/22-rdf-syntax-ns#",
            //"http://www.w3.org/2000/01/rdf-schema#",
            "http://www.w3.org/2002/07/owl#",
            //"http://vitro.mannlib.cornell.edu/ns/vitro/0.7#",
            "http://vitro.mannlib.cornell.edu/ns/vitro/public#"
        ); 

    /*
     * This is a hack to throw out properties in the vitro, rdf, rdfs, and owl namespaces.
     * It will be implemented in a better way in v1.3 (Editing and Display Configuration).
     */
    protected static final String PROPERTY_FILTERS;
    static {
        List<String> namespaceFilters = new ArrayList<String>();
        for (String namespace : EXCLUDED_NAMESPACES) {
            namespaceFilters.add("( afn:namespace(?property) != \"" + namespace + "\" )");
        }
        PROPERTY_FILTERS = StringUtils.join(namespaceFilters, " && ");
    } 
    
    protected static final String DATA_PROPERTY_QUERY_STRING = 
        PREFIXES + "\n" +
        "SELECT DISTINCT ?property WHERE { \n" +
        "   ?subject ?property ?object . \n" + 
        "   ?property a owl:DatatypeProperty . \n" +
        "   FILTER ( \n" +
        "       isLiteral(?object) && \n" +
        "       ( afn:namespace(?property) != \"" + VitroVocabulary.PUBLIC + "\" ) && \n" +
        "       ( afn:namespace(?property) != \"" + VitroVocabulary.OWL + "\" ) && \n" + 
        // NIHVIVO-2790 vitro:moniker has been deprecated, but display existing values for editorial management (deletion is encouraged).
        // This property will be hidden from public display by default.
        "       ( ?property = <" + VitroVocabulary.MONIKER + "> || afn:namespace(?property) != \"" + VitroVocabulary.vitroURI + "\" ) \n" +           
        "   ) \n" +
        "}";
    
    @Override
    public List<DataProperty> getDataPropertyList(Individual subject) {
        return getDataPropertyList(subject.getURI());
    }
    
    @Override
    public List<DataProperty> getDataPropertyList(String subjectUri) {

        // Due to a Jena bug, prebinding on ?subject combined with the isLiteral()
        // filter causes the query to fail. Insert the subjectUri manually instead.
        // QuerySolutionMap initialBindings = new QuerySolutionMap();
        // initialBindings.add("subject", ResourceFactory.createResource(subjectUri));
        String queryString = QueryUtils.subUriForQueryVar(DATA_PROPERTY_QUERY_STRING, "subject", subjectUri);
        log.debug(queryString);
        
        Query query = null;
        try {
            query = QueryFactory.create(queryString);
        } catch(Throwable th){
            log.error("could not create SPARQL query for query string " + th.getMessage());
            log.error(queryString);
            return null;
        }                     
        log.debug("Data property query string:\n" + query);         
     
        ResultSet results = getPropertyQueryResults(query);
        List<DataProperty> properties = new ArrayList<DataProperty>();
        while (results.hasNext()) {
            QuerySolution sol = results.next();
            Resource resource = sol.getResource("property");
            String uri = resource.getURI();
            DataProperty property = getDataPropertyByURI(uri);
            if (property != null) {
                properties.add(property);
            }
        }
        return properties; 
    }
    
}
