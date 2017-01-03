/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.UnionClass;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.shared.Lock;

import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

/**
 * A Java class representing a lazy loading ontology ("VIVO") class
 *
 */
public class VClassJena extends VClass {
	
    private static final Log log = LogFactory.getLog(VClassJena.class.getName());
    private OntClass cls = null;
    private WebappDaoFactoryJena webappDaoFactory = null;

    public VClassJena(OntClass cls, WebappDaoFactoryJena wadf) {
        this.cls = cls;
        
        if (cls.isAnon()) {
        	this.setNamespace(VitroVocabulary.PSEUDO_BNODE_NS);
        	this.setLocalName(cls.getId().toString());
        } else {
        	this.URI = cls.getURI();
        	this.namespace = cls.getNameSpace();
        	this.localName = cls.getLocalName();
        }
                
        this.webappDaoFactory = wadf;
    }
    
    /**
 	 * Constructs the VClassJena as a deep copy of an existing VClassJena. 
 	 */
     @Override
	public VClassJena copy() {
     	VClassJena that = new VClassJena(this.cls, this.webappDaoFactory);
     	copyFields(that);
     	return that;
     }

    /**
     * What this VClass is called
     */
    @Override
    public String getName() {
    	
        if (this.myName != null) {
            return myName;
        } else {
            cls.getOntModel().enterCriticalSection(Lock.READ);
            try {
                VClassDao vClassDao = webappDaoFactory.getVClassDao();
                
                if (vClassDao instanceof VClassDaoJena) {
                	this.myName = ((VClassDaoJena) vClassDao).getLabelForClass(cls,false,false);
                } else {
                    log.error("WebappDaoFactory returned a type of " + vClassDao.getClass().getName() + ". Expected  VClassDaoJena");
                    this.myName = webappDaoFactory.getJenaBaseDao().getLabelOrId(cls);
                }
                         
                return this.myName;
            } finally {
                cls.getOntModel().leaveCriticalSection();
            }
        }
    }
    
    @Override 
    public String getLabel() {
        return getName();
    }
    
    @Override
    public String getLocalNameWithPrefix() { 	
        if (this.localNameWithPrefix != null) {
            return localNameWithPrefix;
        } else {
            this.localNameWithPrefix = webappDaoFactory.makeLocalNameWithPrefix(this);
            return this.localNameWithPrefix;
        }
    }
    
    @Override
    public String getPickListName() {
        if (this.pickListName != null) {
            return pickListName;
        } else {
            this.pickListName = webappDaoFactory.makePickListName(this);
            return this.pickListName;
        }
    }

    /**
     * An example member of this VClass
     */
    @Override
    public  String getExample()  {
    	
        if (this.myExample != null) {
            return myExample;
        } else {
            cls.getOntModel().enterCriticalSection(Lock.READ);
            try {
            	setExample(webappDaoFactory.getJenaBaseDao().getPropertyStringValue(cls, webappDaoFactory.getJenaBaseDao().EXAMPLE_ANNOT));
            	return myExample;
            } finally {
                cls.getOntModel().leaveCriticalSection();
            }
        }
    }
    
    /**
     * Information about the type of information expected of a member of this VClass
     */
    @Override
    public  String getDescription()  {
    	
        if (this.myDescription != null) {
            return myDescription;
        } else {
            cls.getOntModel().enterCriticalSection(Lock.READ);
            try {
            	setDescription(webappDaoFactory.getJenaBaseDao().getPropertyStringValue(cls, webappDaoFactory.getJenaBaseDao().DESCRIPTION_ANNOT));
            	return myDescription;
            } finally {
                cls.getOntModel().leaveCriticalSection();
            }
        }
    }
   
    @Override
    public  String getShortDef()  {
    	
        if (this.myShortDefinition != null) {
            return myShortDefinition;
        } else {
            cls.getOntModel().enterCriticalSection(Lock.READ);
            try {
            	setShortDef(webappDaoFactory.getJenaBaseDao().getPropertyStringValue(cls, webappDaoFactory.getJenaBaseDao().SHORTDEF));
            	return myShortDefinition;
            } finally {
                cls.getOntModel().leaveCriticalSection();
            }
        }		
    }
       
    @Override
    public  int  getDisplayLimit() {
    	
        if (this.displayLimit != null) {
            return displayLimit;
        } else {
            cls.getOntModel().enterCriticalSection(Lock.READ);
            try {
            	setDisplayLimit(webappDaoFactory.getJenaBaseDao().getPropertyNonNegativeIntValue(cls, webappDaoFactory.getJenaBaseDao().DISPLAY_LIMIT));
            	return displayLimit;
            } finally {
                cls.getOntModel().leaveCriticalSection();
            }
        }		 	 	
    }
   
    @Override
    public  int  getDisplayRank()  {
    	
        if (this.displayRank != null) {
            return displayRank;
        } else {
            cls.getOntModel().enterCriticalSection(Lock.READ);
            try {
            	setDisplayRank(webappDaoFactory.getJenaBaseDao().getPropertyNonNegativeIntValue(cls, webappDaoFactory.getJenaBaseDao().DISPLAY_RANK_ANNOT));
            	return displayRank;
            } finally {
                cls.getOntModel().leaveCriticalSection();
            }
        }		 	    
    }
   
    @Override
    public String  getGroupURI()  {
    	
        if (this.groupURI != null) {
            return this.groupURI;
        } else {
            cls.getOntModel().enterCriticalSection(Lock.READ);
            try {
                Resource groupRes = (Resource) cls.getPropertyValue(webappDaoFactory.getJenaBaseDao().IN_CLASSGROUP);
                
                if (groupRes != null) {
                    setGroupURI(groupRes.getURI());
                }
                
            } catch (Exception e) {
                log.error("error retrieving vitro:inClassGroup property value for " + cls.getURI());
                log.trace(e);           	
            } finally {
                cls.getOntModel().leaveCriticalSection();
            }
            
            return this.groupURI;
        }		 	         
    }
    
    @Override
    public  String getCustomEntryForm()  {
    	
        if (this.customEntryForm != null) {
            return this.customEntryForm;
        } else {
            cls.getOntModel().enterCriticalSection(Lock.READ);
            try {
            	setCustomEntryForm(webappDaoFactory.getJenaBaseDao().getPropertyStringValue(cls, webappDaoFactory.getJenaBaseDao().PROPERTY_CUSTOMENTRYFORMANNOT));
            	return this.customEntryForm;
            } finally {
                cls.getOntModel().leaveCriticalSection();
            }
        }		       
    }
    
    @Override
    public  String getCustomDisplayView()  {
    	
        if (this.customDisplayView != null) {
            return this.customDisplayView;
        } else {
            cls.getOntModel().enterCriticalSection(Lock.READ);
            try {
            	setCustomDisplayView(webappDaoFactory.getJenaBaseDao().getPropertyStringValue(cls, webappDaoFactory.getJenaBaseDao().PROPERTY_CUSTOMDISPLAYVIEWANNOT));
            	return this.customDisplayView;
            } finally {
                cls.getOntModel().leaveCriticalSection();
            }
        }		        	
    }
    
    @Override
    public  String getCustomShortView() { 
    	
        if (this.customShortView != null) {
            return this.customShortView;
        } else {
            cls.getOntModel().enterCriticalSection(Lock.READ);
            try {
            	setCustomShortView(webappDaoFactory.getJenaBaseDao().getPropertyStringValue(cls, webappDaoFactory.getJenaBaseDao().PROPERTY_CUSTOMSHORTVIEWANNOT));
            	return this.customShortView;
            } finally {
                cls.getOntModel().leaveCriticalSection();
            }
        }		        	   		
    }
    
    @Override
    public  String getCustomSearchView() {
    	
        if (this.customSearchView != null) {
            return this.customSearchView;
        } else {
            cls.getOntModel().enterCriticalSection(Lock.READ);
            try {
            	setCustomSearchView(webappDaoFactory.getJenaBaseDao().getPropertyStringValue(cls, webappDaoFactory.getJenaBaseDao().PROPERTY_CUSTOMSEARCHVIEWANNOT));
            	return this.customSearchView;
            } finally {
                cls.getOntModel().leaveCriticalSection();
            }
        }		        	   		
    }
        
    @Override
    public Float getSearchBoost() {
    	
        if (this.searchBoost != null) {
            return this.searchBoost;
        } else {
            cls.getOntModel().enterCriticalSection(Lock.READ);
            try {
            	setSearchBoost(webappDaoFactory.getJenaBaseDao().getPropertyFloatValue(cls, webappDaoFactory.getJenaBaseDao().SEARCH_BOOST_ANNOT));
            	return this.searchBoost;
            } finally {
                cls.getOntModel().leaveCriticalSection();
            }
        }		 	    
    } 
    
    @Override
    public RoleLevel getHiddenFromDisplayBelowRoleLevel() {
       
        if (this.hiddenFromDisplayBelowRoleLevel != null) {
            return this.hiddenFromDisplayBelowRoleLevel;
        } else {
            cls.getOntModel().enterCriticalSection(Lock.READ);
            try {            	
                //There might be multiple HIDDEN_FROM_EDIT_DISPLAY_ANNOT properties, only use the highest
                StmtIterator it = cls.listProperties(webappDaoFactory.getJenaBaseDao().HIDDEN_FROM_DISPLAY_BELOW_ROLE_LEVEL_ANNOT);
                BaseResourceBean.RoleLevel hiddenRoleLevel = null;
            
                while( it.hasNext() ){
                    Statement stmt = it.nextStatement();
                    RDFNode obj;
                    if( stmt != null && (obj = stmt.getObject()) != null && obj.isURIResource() ){
                        Resource res = obj.as(Resource.class);
                        if( res != null && res.getURI() != null ){
                            BaseResourceBean.RoleLevel roleFromModel = BaseResourceBean.RoleLevel.getRoleByUri(res.getURI());
                            if( roleFromModel != null && 
                                (hiddenRoleLevel == null || roleFromModel.compareTo(hiddenRoleLevel) > 0 )){
                                hiddenRoleLevel = roleFromModel;                            
                            }
                        }
                    }
                }
                
                setHiddenFromDisplayBelowRoleLevel(hiddenRoleLevel); //this might get set to null
            	return this.hiddenFromDisplayBelowRoleLevel;
            } finally {
                cls.getOntModel().leaveCriticalSection();
            }
        }		 	               
    }
    
    @Override
    public RoleLevel getProhibitedFromUpdateBelowRoleLevel() {
        
        if (this.prohibitedFromUpdateBelowRoleLevel != null) {
            return this.prohibitedFromUpdateBelowRoleLevel;
        } else {
            cls.getOntModel().enterCriticalSection(Lock.READ);
            try {            	
                //There might be multiple PROHIBITED_FROM_UPDATE_BELOW_ROLE_LEVEL_ANNOT properties, only use the highest
            	StmtIterator it = cls.listProperties(webappDaoFactory.getJenaBaseDao().PROHIBITED_FROM_UPDATE_BELOW_ROLE_LEVEL_ANNOT);
                BaseResourceBean.RoleLevel prohibitedRoleLevel = null;
                while( it.hasNext() ){
                    Statement stmt = it.nextStatement();
                    RDFNode obj;
                    if( stmt != null && (obj = stmt.getObject()) != null && obj.isURIResource() ){
                        Resource res = obj.as(Resource.class);
                        if( res != null && res.getURI() != null ){
                            BaseResourceBean.RoleLevel roleFromModel = BaseResourceBean.RoleLevel.getRoleByUri(res.getURI());
                            if( roleFromModel != null && 
                                (prohibitedRoleLevel == null || roleFromModel.compareTo(prohibitedRoleLevel) > 0 )){
                                prohibitedRoleLevel = roleFromModel;                            
                            }
                        }
                    }
                }
                
                setProhibitedFromUpdateBelowRoleLevel(prohibitedRoleLevel); //this might get set to null
            	return this.prohibitedFromUpdateBelowRoleLevel;
            } finally {
                cls.getOntModel().leaveCriticalSection();
            }
        }		 	                      
    }
    
    @Override
    public RoleLevel getHiddenFromPublishBelowRoleLevel() {
       
        if (this.hiddenFromPublishBelowRoleLevel != null) {
            return this.hiddenFromPublishBelowRoleLevel;
        } else {
            cls.getOntModel().enterCriticalSection(Lock.READ);
            try {            	
                //There might be multiple HIDDEN_FROM_PUBLISH_BELOW_ROLE_LEVEL_ANNOT properties, only use the highest
                StmtIterator it = cls.listProperties(webappDaoFactory.getJenaBaseDao().HIDDEN_FROM_PUBLISH_BELOW_ROLE_LEVEL_ANNOT);
                BaseResourceBean.RoleLevel publishRoleLevel = null;
            
                while( it.hasNext() ){
                    Statement stmt = it.nextStatement();
                    RDFNode obj;
                    if( stmt != null && (obj = stmt.getObject()) != null && obj.isURIResource() ){
                        Resource res = obj.as(Resource.class);
                        if( res != null && res.getURI() != null ){
                            BaseResourceBean.RoleLevel roleFromModel = BaseResourceBean.RoleLevel.getRoleByUri(res.getURI());
                            if( roleFromModel != null && 
                                (publishRoleLevel == null || roleFromModel.compareTo(publishRoleLevel) > 0 )){
                                publishRoleLevel = roleFromModel;                            
                            }
                        }
                    }
                }
                
                setHiddenFromPublishBelowRoleLevel(publishRoleLevel); //this might get set to null
            	return this.hiddenFromPublishBelowRoleLevel;
            } finally {
                cls.getOntModel().leaveCriticalSection();
            }
        }		 	               
    }
    
    @Override
    public boolean isUnion() {
        return this.cls.isUnionClass();
    }
    
    //TODO consider anonymous components
    @Override
    public List<VClass> getUnionComponents() {
        List<VClass> unionComponents = new ArrayList<VClass>();
        if (isUnion()) {
            UnionClass union = this.cls.as(UnionClass.class);
            Iterator<? extends OntClass> opIt = union.listOperands();
            while(opIt.hasNext()) {
                OntClass component = opIt.next();
                if (!component.isAnon()) {
                    unionComponents.add(new VClassJena(component, this.webappDaoFactory));  
                }
            }
        }
        return unionComponents;
    }
    
}
