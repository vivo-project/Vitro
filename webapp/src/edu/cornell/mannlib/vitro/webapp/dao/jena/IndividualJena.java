/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Timestamp;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.beans.Keyword;
import edu.cornell.mannlib.vitro.webapp.beans.Link;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.filestorage.FileModelHelper;
import edu.cornell.mannlib.vitro.webapp.filestorage.FileServingHelper;
import edu.cornell.mannlib.vitro.webapp.utils.FlagMathUtils;

public class IndividualJena extends IndividualImpl implements Individual {

    private static final Log log = LogFactory.getLog(IndividualJena.class.getName());
    private OntResource ind = null;
    private WebappDaoFactoryJena webappDaoFactory = null;
    private Float _searchBoostJena = null;
    
    public IndividualJena(OntResource ind, WebappDaoFactoryJena wadf) {
        this.ind = ind;
        if (ind.isAnon()) {
        	this.setNamespace(VitroVocabulary.PSEUDO_BNODE_NS);
        	this.setLocalName(ind.getId().toString());
        } else {
        	this.URI = ind.getURI();
        	this.namespace = ind.getNameSpace();
        	this.localName = ind.getLocalName();
        }
        this.webappDaoFactory = wadf;
    }

    public String getName() {
        if (this.name != null) {
            return name;
        } else {
            ind.getOntModel().enterCriticalSection(Lock.READ);
            try {
                this.name = webappDaoFactory.getJenaBaseDao().getLabelOrId(ind);
                if (this.name == null) {
                    this.name = "[null]";
                }
                return this.name;
            } finally {
                ind.getOntModel().leaveCriticalSection();
            }
        }
    }

    public String getVClassURI() {
        if (this.vClassURI != null) {
            return vClassURI;
        } else {
        	ind.getOntModel().enterCriticalSection(Lock.READ);
        	try {
                ClosableIterator typeIt = ind.listRDFTypes(true);
                try {
	                while (typeIt.hasNext()) {
	                    Resource type = (Resource) typeIt.next();
	                    if (type.getNameSpace()!=null && (!webappDaoFactory.getJenaBaseDao().NONUSER_NAMESPACES.contains(type.getNameSpace()) || type.getURI().equals(OWL.Thing.getURI())) ) {
	                    	this.vClassURI=type.getURI();
	                        break;
	                    }
	                }
                } finally {
                	typeIt.close();
                }
        	} finally {
        		ind.getOntModel().leaveCriticalSection();
        	}
        	return this.vClassURI;
        }
    }

    public VClass getVClass() {
        if (this.vClass != null) {
            return this.vClass;
        } else {
        	 ind.getOntModel().enterCriticalSection(Lock.READ);
             try {
                 ClosableIterator typeIt = ind.listRDFTypes(true);
                 try {
 	                while (typeIt.hasNext()) {
 	                    Resource type = (Resource) typeIt.next();
 	                    if (type.getNameSpace()!=null && (!webappDaoFactory.getJenaBaseDao().NONUSER_NAMESPACES.contains(type.getNameSpace()) || type.getURI().equals(OWL.Thing.getURI())) ) {
 	                    	this.vClassURI=type.getURI();
 	                        this.vClass = webappDaoFactory.getVClassDao().getVClassByURI(this.vClassURI);
 	                        break;
 	                    }
 	                }
                 } finally {
                 	typeIt.close();
                 }
                 return this.vClass;
             } finally {
                 ind.getOntModel().leaveCriticalSection();
             }
        }
    }

    public int getFlag1Numeric() {
        if (flag1Numeric > -1) {
            return flag1Numeric;
        } else {
            doFlag1();
            return flag1Numeric;
        }
    }

    public String getFlag1Set() {
        if (flag1Set != null) {
            return flag1Set;
        } else {
            doFlag1();
            return flag1Set;
        }
    }

    public String getFlag2Set() {
        if (flag2Set != null) {
            return flag2Set;
        } else {
            doFlag2();
            return flag2Set;
        }
    }

    /* Consider the flagBitMask as a mask to & with flags.
   if flagBitMask bit zero is set then return true if
   the individual is in portal 2,
   if flagBitMask bit 1 is set then return true if
   the individua is in portal 4
   etc.
   Portal uris look like this:
   "http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing"
    */
    public boolean doesFlag1Match(int flagBitMask) {
        Long [] numerics = FlagMathUtils.numeric2numerics(flagBitMask);
        ind.getOntModel().enterCriticalSection(Lock.READ);
        try{
            for( Long numericPortal : numerics){
                int portalid = FlagMathUtils.numeric2Portalid(numericPortal);
                String portalTypeUri = VitroVocabulary.vitroURI + "Flag1Value" + portalid + "Thing";
                Resource portalType = ResourceFactory.createResource(portalTypeUri);
                if( ! ind.getOntModel().contains(ind,RDF.type,portalType))
                    return false;
            }
        }finally{
            ind.getOntModel().leaveCriticalSection();
        }
        return true;
    }

    private void doFlag1() {
        ind.getOntModel().enterCriticalSection(Lock.READ);

        try {
            ClosableIterator typeIt=null;
            int portalNumeric = 0;
            String portalSet = "";
            try{
                typeIt = ind.getOntModel().listStatements(ind, RDF.type ,(String) null);

                while (typeIt.hasNext()) {
                    Statement stmt = (Statement) typeIt.next();
                    Resource type = (Resource)stmt.getObject();
                    String typeName = type.getLocalName();
                    if(type.getNameSpace() != null && type.getNameSpace().equals(VitroVocabulary.vitroURI) && typeName.indexOf("Flag1Value")==0) {
                        try {
                            int portalNumber = Integer.decode(typeName.substring(10,typeName.length()-5));
                            portalNumeric = portalNumeric | (1 << portalNumber);
                            if (portalSet.length() > 0) {
                                portalSet+=",";
                            }
                            portalSet+=Integer.toString(portalNumber);
                        } catch (Exception e) {}
                    }
                }
            }finally{
                if( typeIt != null ) typeIt.close() ;
            }
            flag1Set = portalSet;
            flag1Numeric = portalNumeric;
        } finally {
            ind.getOntModel().leaveCriticalSection();
        }
    }

    private void doFlag2() {
        ind.getOntModel().enterCriticalSection(Lock.READ);
        try {
            ClosableIterator typeIt=null;
            String flagSet = "";
            try{
                typeIt = ind.getOntModel().listStatements(ind, RDF.type ,(String) null);
                while (typeIt.hasNext()) {
                    Statement stmt = (Statement) typeIt.next();
                    Resource type = (Resource)stmt.getObject();
                    String typeName = type.getLocalName();
                    if(type.getNameSpace() != null && type.getNameSpace().equals(VitroVocabulary.vitroURI) && typeName.indexOf("Flag2Value")==0) {
                        try {
                            String flagValue = ((WebappDaoFactoryJena)webappDaoFactory).getFlag2ClassLabelMap().get(type);
                            if (flagSet.length() > 0) {
                                flagSet+=",";
                            }
                            flagSet+=flagValue;
                        } catch (Exception e) {}
                    }
                }
            }finally{
                if( typeIt != null ) typeIt.close() ;
            }
            flag2Set = flagSet;
        } finally {
            ind.getOntModel().leaveCriticalSection();
        }
    }


    public Date getSunrise() {
        if (sunrise != null) {
            return sunrise;
        } else {
            ind.getOntModel().enterCriticalSection(Lock.READ);
            try {
                sunrise = webappDaoFactory.getJenaBaseDao().getPropertyDateTimeValue(ind,webappDaoFactory.getJenaBaseDao().SUNRISE);
                return sunrise;
            } finally {
                ind.getOntModel().leaveCriticalSection();
            }
        }
    }

    public Date getSunset() {
        if (sunset != null) {
            return sunset;
        } else {
            ind.getOntModel().enterCriticalSection(Lock.READ);
            try {
                sunset = webappDaoFactory.getJenaBaseDao().getPropertyDateTimeValue(ind,webappDaoFactory.getJenaBaseDao().SUNSET);
                return sunset;
            } finally {
                ind.getOntModel().leaveCriticalSection();
            }
        }
    }

    public Date getTimekey() {
        if (timekey != null) {
            return timekey;
        } else {
            ind.getOntModel().enterCriticalSection(Lock.READ);
            try {
                timekey = webappDaoFactory.getJenaBaseDao().getPropertyDateTimeValue(ind,webappDaoFactory.getJenaBaseDao().TIMEKEY);
                return timekey;
            } finally {
                ind.getOntModel().leaveCriticalSection();
            }
        }
    }

    public Timestamp getModTime() {
        if (modTime != null) {
            return modTime;
        } else {
            ind.getOntModel().enterCriticalSection(Lock.READ);
            try {
                Date modDate = webappDaoFactory.getJenaBaseDao().getPropertyDateTimeValue(ind,webappDaoFactory.getJenaBaseDao().MODTIME);
                if (modDate != null) {
                    modTime = new Timestamp(modDate.getTime());
                }
                return modTime;
            } finally {
                ind.getOntModel().leaveCriticalSection();
            }
        }
    }

    public String getMoniker() {
        if (moniker != null) {
            return moniker;
        } else {
            ind.getOntModel().enterCriticalSection(Lock.READ);
            try {
                moniker = webappDaoFactory.getJenaBaseDao().getPropertyStringValue(ind,webappDaoFactory.getJenaBaseDao().MONIKER);
                if (moniker == null) {
                	try {
                        // trying to deal with the fact that an entity may have more than 1 VClass
                        List<VClass> clasList = this.getVClasses(true);
                        if (clasList == null || clasList.size() < 2) {
                            moniker = getVClass().getName();
                        } else {
                            VClass preferredClass = null;
                            for (VClass clas : clasList) {
                                if (clas.getCustomDisplayView() != null && clas.getCustomDisplayView().length()>0) {
                                    // arbitrarily deciding that the preferred class (could be >1) is one with a custom view
                                    preferredClass = clas;
                                    log.debug("Found direct class ["+clas.getName()+"] with custom view "+clas.getCustomDisplayView()+"; resetting entity vClass to this class");
                                }
                            }
                            if (preferredClass == null) {
                                // no basis for selecting a preferred class name to use
                                moniker = null; // was this.getVClass().getName();
                            } else {
                                moniker = preferredClass.getName();
                            }
                        }
                	} catch (Exception e) {}
                }
                return moniker;
            } finally {
                ind.getOntModel().leaveCriticalSection();
            }
        }
    }

    /* 2009-01-27 hold off on individual-level filtering for now
    @Override
    public RoleLevel getHiddenFromDisplayBelowRoleLevel(){
        if( this.hiddenFromDisplayBelowRoleLevel != null )
            return this.hiddenFromDisplayBelowRoleLevel;
        
        OntModel model = ind.getOntModel(); 
        model.enterCriticalSection(Lock.READ);
        try {
            NodeIterator it = model.listObjectsOfProperty(ind, model.getAnnotationProperty(VitroVocabulary.HIDDEN_FROM_DISPLAY_BELOW_ROLE_LEVEL_ANNOT));
            if( it == null )
                return BaseResourceBean.RoleLevel.values()[0];
            BaseResourceBean.RoleLevel role = BaseResourceBean.RoleLevel.values()[0];            
            while( it.hasNext() ) {
                RDFNode node = it.nextNode();
                if( node != null && node.isURIResource() ) {
                    BaseResourceBean.RoleLevel foundRole = BaseResourceBean.RoleLevel.getRoleByUri( node.asNode().getURI() );
                    if( role.compareTo(foundRole ) < 0 ) { // find the lowest of all levels
                        role = foundRole;
                    }
                }
            }
            return this.hiddenFromDisplayBelowRoleLevel = role;
        } finally {
            model.leaveCriticalSection();
        }
    }

    /**
     * this seems like a mismatch with the RDF storage model.
     * We could have multiple statements in the model that associated
     * the individual with multiple or redundant edit display levels.
     */
    
    /*
    @Override
    public RoleLevel getProhibitedFromUpdateBelowRoleLevel() {
        if( this.prohibitedFromUpdateBelowRoleLevel != null )
            return this.prohibitedFromUpdateBelowRoleLevel;
        
        OntModel model = ind.getOntModel(); 
        model.enterCriticalSection(Lock.READ);
        try {
            NodeIterator it = model.listObjectsOfProperty(ind, model.getAnnotationProperty(VitroVocabulary.PROHIBITED_FROM_UPDATE_BELOW_ROLE_LEVEL_ANNOT));
            if( it == null )
                return BaseResourceBean.RoleLevel.values()[0];
            BaseResourceBean.RoleLevel role = BaseResourceBean.RoleLevel.values()[0];
            while( it.hasNext() ){
                RDFNode node = it.nextNode();
                if( node != null && node.isURIResource() ) {
                    BaseResourceBean.RoleLevel foundRole = BaseResourceBean.RoleLevel.getRoleByUri( node.asNode().getURI() );
                    if( role.compareTo(foundRole ) < 0 ) // find the lowest of all roles
                        role = foundRole;                    
                }
            } 
            return this.prohibitedFromUpdateBelowRoleLevel = role;            
        } finally {
            model.leaveCriticalSection();
        }                
    }
    */
  

    public String getBlurb() {
        if (this.blurb != null) {
            return blurb;
        } else {
            ind.getOntModel().enterCriticalSection(Lock.READ);
            try {
                blurb = webappDaoFactory.getJenaBaseDao().getPropertyStringValue(ind,webappDaoFactory.getJenaBaseDao().BLURB);
                return blurb;
            } finally {
                ind.getOntModel().leaveCriticalSection();
            }
        }
    }

    public String getDescription() {
        if (this.description != null) {
            return description;
        } else {
            ind.getOntModel().enterCriticalSection(Lock.READ);
            try {
                description = webappDaoFactory.getJenaBaseDao().getPropertyStringValue(ind,webappDaoFactory.getJenaBaseDao().DESCRIPTION);
                return description;
            } finally {
                ind.getOntModel().leaveCriticalSection();
            }
        }
    }


    public Float getSearchBoost(){
        if( this._searchBoostJena != null ){
            return this._searchBoostJena;
        }else{
            ind.getOntModel().enterCriticalSection(Lock.READ);
            try{
                try {
                    searchBoost = 
                        ((Literal)ind.getPropertyValue(webappDaoFactory.getJenaBaseDao().SEARCH_BOOST_ANNOT)).getFloat();
                } catch (Exception e) {
                    searchBoost = null;
                }                
                return searchBoost;
            }finally{
                ind.getOntModel().leaveCriticalSection();
            }
        }
    }
    
    public String getCitation() {
        if (this.citation != null) {
            return citation;
        } else {
            ind.getOntModel().enterCriticalSection(Lock.READ);
            try {
                citation = webappDaoFactory.getJenaBaseDao().getPropertyStringValue(ind,webappDaoFactory.getJenaBaseDao().CITATION);
                return citation;
            } finally {
                ind.getOntModel().leaveCriticalSection();
            }
        }
    }

	@Override
	public String getMainImageUri() {
		if (this.mainImageUri != NOT_INITIALIZED) {
			return mainImageUri;
		} else {
			for (ObjectPropertyStatement stmt : getObjectPropertyStatements()) {
				if (stmt.getPropertyURI()
						.equals(VitroVocabulary.IND_MAIN_IMAGE)) {
					mainImageUri = stmt.getObjectURI();
					return mainImageUri;
				}
			}
			return null;
		}
	}

	@Override
	public String getImageUrl() {
		if (this.imageUrl != null) {
			log.debug("imageUrl was cached for " + getURI() + ": '"
					+ this.imageUrl + "'");
			return imageUrl;
		} else {
			String imageUri = FileModelHelper.getMainImageBytestreamUri(this);
			String filename = FileModelHelper.getMainImageFilename(this);
			imageUrl = FileServingHelper.getBytestreamAliasUrl(imageUri,
					filename);
			log.debug("figured imageUrl for " + getURI() + ": '"
					+ this.imageUrl + "'");
			return imageUrl;
		}
	}

	@Override
	public String getThumbUrl() {
		if (this.thumbUrl != null) {
			log.debug("thumbUrl was cached for " + getURI() + ": '"
					+ this.thumbUrl + "'");
			return thumbUrl;
		} else {
			String imageUri = FileModelHelper.getThumbnailBytestreamUri(this);
			String filename = FileModelHelper.getThumbnailFilename(this);
			thumbUrl = FileServingHelper.getBytestreamAliasUrl(imageUri, filename);
			log.debug("figured thumbUrl for " + getURI() + ": '"
					+ this.thumbUrl + "'");
			return thumbUrl;
		}
	}

    public String getAnchor() {
        if (this.anchor != null) {
            return anchor;
        } else {
            doUrlAndAnchor();
            return anchor;
        }
    }

    public String getUrl() {
        if (this.url != null) {
            return url;
        } else {
            doUrlAndAnchor();
            return url;
        }
    }

    private void doUrlAndAnchor() {
        ind.getOntModel().enterCriticalSection(Lock.READ);
        try {
            if (webappDaoFactory.getJenaBaseDao().PRIMARY_LINK != null) {
                Iterator links = ind.listPropertyValues(webappDaoFactory.getJenaBaseDao().PRIMARY_LINK);
                if (links.hasNext()) {
                    try {
                        com.hp.hpl.jena.ontology.Individual linkInd = ((com.hp.hpl.jena.ontology.Individual)((Resource) links.next()).as(com.hp.hpl.jena.ontology.Individual.class));
                        if (webappDaoFactory.getJenaBaseDao().LINK_ANCHOR != null) {
                            try {
                                Literal l = (Literal) linkInd.getPropertyValue(webappDaoFactory.getJenaBaseDao().LINK_ANCHOR);
                                if (l != null) {
                                    anchor = l.getString();
                                }
                            } catch (ClassCastException e) {}
                        }
                        if (webappDaoFactory.getJenaBaseDao().LINK_URL != null) {
                            try {
                                Literal l = (Literal) linkInd.getPropertyValue(webappDaoFactory.getJenaBaseDao().LINK_URL);
                                if (l != null) {
                                    try {
                                        url = URLDecoder.decode(l.getString(), "UTF-8");
                                    } catch (UnsupportedEncodingException use) {}
                                }
                            } catch (ClassCastException e) {}
                        }
                    } catch (ClassCastException cce) {}
                }
            }
        } finally {
            ind.getOntModel().leaveCriticalSection();
        }
    }

    public List <Link> getLinksList() {
        if (this.linksList != null) {
            return this.linksList;
        } else {
            try {
                webappDaoFactory.getLinksDao().addLinksToIndividual( this );
            } catch (Exception e) {
                log.error(this.getClass().getName()+" could not addLinksToIndividual for "+this.getURI());
            }
            return this.linksList;
        }
    }

    public Link getPrimaryLink() {
        if (this.primaryLink != null) {
            return this.primaryLink;
        } else {
            try {
                webappDaoFactory.getLinksDao().addPrimaryLinkToIndividual( this );
            } catch (Exception e) {
                log.error(this.getClass().getName()+" could not addPrimaryLinkToIndividual for "+this.getURI());
            }
            return this.primaryLink;
        }
    }


    public List<String> getKeywords() {
        if (this.keywords != null) {
            return this.keywords;
        } else {
            try {
                this.setKeywords(webappDaoFactory.getIndividualDao().getKeywordsForIndividual(this.getURI()));
            } catch (Exception e) {
                log.error(this.getClass().getName()+" could not getKeywords for "+this.getURI());
            }
            return this.keywords;
        }
    }
    
    public List<Keyword> getKeywordObjects() {
        if (this.keywordObjects != null) {
            return this.keywordObjects;
        } else {
            try {
                this.setKeywordObjects(webappDaoFactory.getIndividualDao().getKeywordObjectsForIndividual(this.getURI()));
            } catch (Exception e) {
                log.error(this.getClass().getName()+" could not get Keyword Objects for "+this.getURI());
            }
        }
        return this.keywordObjects;
    }

    public List<ObjectPropertyStatement> getObjectPropertyStatements() {
        if (this.objectPropertyStatements != null) {
            return this.objectPropertyStatements;
        } else {
            try {
                webappDaoFactory.getObjectPropertyStatementDao().fillExistingObjectPropertyStatements(this);
                Iterator stmtIt = this.getObjectPropertyStatements().iterator();
                while (stmtIt.hasNext()) {
                    ObjectPropertyStatement stmt = (ObjectPropertyStatement) stmtIt.next();
                    stmt.setObject(webappDaoFactory.getIndividualDao().getIndividualByURI(stmt.getObject().getURI()));
                }
            } catch (Exception e) {
                log.error(this.getClass().getName()+" could not fill existing ObjectPropertyStatements for "+this.getURI());
            }
            return this.objectPropertyStatements;
        }
    }

    public List<ObjectProperty> getObjectPropertyList() {
        if (this.propertyList != null) {
            return this.propertyList;
        } else {
            try {
                webappDaoFactory.getObjectPropertyDao().fillObjectPropertiesForIndividual( this );
            } catch (Exception e) {
                log.error(this.getClass().getName()+" could not fillEntityProperties for "+this.getURI());
            }
            return this.propertyList;
        }
    }
    
    @Override
    public Map<String,ObjectProperty> getObjectPropertyMap() {
    	if (this.objectPropertyMap != null) {
    		return objectPropertyMap;
    	} else {
    		Map map = new HashMap<String,ObjectProperty>();
    		if (this.propertyList == null) {
    			getObjectPropertyList();
    		}
    		for (Iterator i = this.propertyList.iterator(); i.hasNext();) { 
    			ObjectProperty op = (ObjectProperty) i.next();
    			if (op.getURI() != null) {
    				map.put(op.getURI(), op);
    			}
    		}
    		this.objectPropertyMap = map;
    		return map;    		
    	}
    }

    public List<DataPropertyStatement> getDataPropertyStatements() {
        if (this.dataPropertyStatements != null) {
            return this.dataPropertyStatements;
        } else {
            try {
                webappDaoFactory.getDataPropertyStatementDao().fillExistingDataPropertyStatementsForIndividual(this/*,false*/);
            } catch (Exception e) {
                log.error(this.getClass().getName()+" could not fill existing DataPropertyStatements for "+this.getURI());
            }
            return this.dataPropertyStatements;
        }
    }

    public List getDataPropertyList() {
        if (this.datatypePropertyList != null) {
            return this.datatypePropertyList;
        } else {
            try {
                webappDaoFactory.getDataPropertyDao().fillDataPropertiesForIndividual( this );
            } catch (Exception e) {
                log.error(this.getClass().getName()+" could not fill data properties for "+this.getURI());
            }
            return this.datatypePropertyList;
        }
    }
    
    @Override
    public Map<String,DataProperty> getDataPropertyMap() {
    	if (this.dataPropertyMap != null) {
    		return dataPropertyMap;
    	} else {
    		Map map = new HashMap<String,DataProperty>();
    		if (this.datatypePropertyList == null) {
    			getDataPropertyList();
    		}
    		for (Iterator i = this.datatypePropertyList.iterator(); i.hasNext();) { 
    			DataProperty dp = (DataProperty) i.next();
    			if (dp.getURI() != null) {
    				map.put(dp.getURI(), dp);
    			}
    		}
    		this.dataPropertyMap = map;
    		return map;    		
    	}
    }

    public List<DataPropertyStatement> getExternalIds() {
        // BJL 2007-11-11: need to decide whether we want to use Collections or Lists in our interfaces - we seem to be leaning toward Lists
        if (this.externalIds != null) {
            return this.externalIds;
        } else {
            try {
                List<DataPropertyStatement> dpsList = new ArrayList<DataPropertyStatement>();
                dpsList.addAll(webappDaoFactory.getIndividualDao().getExternalIds(this.getURI(), null));
                this.externalIds = dpsList;
            } catch (Exception e) {
                log.error(this.getClass().getName()+" could not fill external IDs for "+this.getURI());
            }
            return this.externalIds;
        }
    }
    
    @Override
    public List<VClass> getVClasses() {
    	return getVClasses(false);
    }
    
    @Override
    public List<VClass> getVClasses(boolean direct) {
    	if (direct) {
    		if (directVClasses != null) {
    			return directVClasses;
    		} else {
    			directVClasses = getMyVClasses(true);
    			return directVClasses;
    		}
    	} else {
    		if (allVClasses != null) {
    			return allVClasses;
    		} else {
    			allVClasses = getMyVClasses(false);
    			return allVClasses;
    		}
    	}
    }
    
    private List<VClass> getMyVClasses(boolean direct) {
		List<VClass> vClassList = new ArrayList<VClass>(); 
		OntModel ontModel = ind.getOntModel();
		ontModel.enterCriticalSection(Lock.READ);
		try {
			ClosableIterator typeIt = ind.listRDFTypes(direct); 
			try {
				for (Iterator it = typeIt; it.hasNext();) {
					Resource type = (Resource) typeIt.next();
					String typeURI = (!type.isAnon()) ? type.getURI() : VitroVocabulary.PSEUDO_BNODE_NS + type.getId().toString();
					if (type.getNameSpace() == null || (!webappDaoFactory.getNonuserNamespaces().contains(type.getNameSpace())) ) {
						VClass vc = webappDaoFactory.getVClassDao().getVClassByURI(typeURI);
						if (vc != null) {
							vClassList.add(vc);
						}
					}
					
				}
			} finally {
				typeIt.close();
			}
		} finally {
			ontModel.leaveCriticalSection();
		}
		try {
			Collections.sort(vClassList);
		} catch (Exception e) {}
		return vClassList;
	}
    
    /**
     * Overriding the base method so that we can do the sorting by arbitrary property here.  An
     * IndividualJena has a reference back to the model; everything else is just a dumb bean (for now).
     */
    @Override
    protected void sortEnts2EntsForDisplay(){
        if( getObjectPropertyList() == null ) return;

        Iterator it = getObjectPropertyList().iterator();
        while(it.hasNext()){
            ObjectProperty prop = (ObjectProperty)it.next();
        /*  if (prop.getObjectIndividualSortPropertyURI()==null) {
            	prop.sortObjectPropertyStatementsForDisplay(prop,prop.getObjectPropertyStatements());
            } else {*/
            	prop.sortObjectPropertyStatementsForDisplay(prop,prop.getObjectPropertyStatements());
        /*  }*/
        }
    }
    
    private Collator collator = Collator.getInstance();
    
    private void sortObjectPropertyStatementsForDisplay(ObjectProperty prop) {
    	try {
    		log.info("Doing special sort for "+prop.getDomainPublic());
    		final String sortPropertyURI = prop.getObjectIndividualSortPropertyURI();
            String tmpDir;
            boolean tmpAsc;

            tmpDir = prop.getDomainEntitySortDirection();

            //valid values are "desc" and "asc", anything else will default to ascending
            tmpAsc = !"desc".equalsIgnoreCase(tmpDir);

            final boolean dir = tmpAsc;
            Comparator comp = new Comparator(){
                final boolean cAsc = dir;

                public final int compare(Object o1, Object o2){
                    ObjectPropertyStatement e2e1= (ObjectPropertyStatement)o1, e2e2=(ObjectPropertyStatement)o2;
                    Individual e1 , e2;
                    e1 = e2e1 != null ? e2e1.getObject():null;
                    e2 = e2e2 != null ? e2e2.getObject():null;

                    Object val1 = null, val2 = null;
                    if( e1 != null ){
                        try {
                        	DataProperty dp = e1.getDataPropertyMap().get(sortPropertyURI);
                        	if (dp.getDataPropertyStatements() != null && dp.getDataPropertyStatements().size()>0) {
                        		val1 = dp.getDataPropertyStatements().get(0).getData();
                        	}
                        }
                        catch (Exception e) {
                        	val1 = "";
                        }
                    } else {
                        log.warn( "IndividualJena.sortObjectPropertiesForDisplay passed object property statement with no range entity.");
                    }

                    if( e2 != null ){
                        try {
                        	DataProperty dp = e2.getDataPropertyMap().get(sortPropertyURI);
                        	if (dp.getDataPropertyStatements() != null && dp.getDataPropertyStatements().size()>0) {
                        		val2 = dp.getDataPropertyStatements().get(0).getData();
                        	}
                        }
                        catch (Exception e) {
                        	val2 = "";
                        }
                    } else {
                        log.warn( "IndividualJena.sortObjectPropertyStatementsForDisplay() was passed an object property statement with no range entity.");
                    }

                    int rv = 0;
                    try {
                        if( val1 instanceof String )
                        	rv = collator.compare( ((String)val1) , ((String)val2) );
                            //rv = ((String)val1).compareTo((String)val2);
                        else if( val1 instanceof Date ) {
                            DateTime dt1 = new DateTime((Date)val1);
                            DateTime dt2 = new DateTime((Date)val2);
                            rv = dt1.compareTo(dt2);
                        }
                        else
                            rv = 0;
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }

                    if( cAsc )
                        return rv;
                    else
                        return rv * -1;
                }
            };
            try {
                Collections.sort(getObjectPropertyStatements(), comp);
            } catch (Exception e) {
                log.error("Exception sorting object property statements for object property "+this.getURI());
            }

    		
    	} catch (Exception e) {
    		log.error(e);
    	}
    }
    
    
}
