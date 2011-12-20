/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

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
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.filestorage.model.ImageInfo;
import edu.cornell.mannlib.vitro.webapp.search.beans.ProhibitedFromSearch;

public class IndividualJena extends IndividualImpl implements Individual {

    private static final Log log = LogFactory.getLog(IndividualJena.class.getName());
    private OntResource ind = null;
    private WebappDaoFactoryJena webappDaoFactory = null;
    private Float _searchBoostJena = null;
    private boolean retrievedNullRdfsLabel = false;
    
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

    public String getRdfsLabel() {
        if (this.rdfsLabel != null) {
            return rdfsLabel;
        } else if( this.rdfsLabel == null && retrievedNullRdfsLabel ){
        	return null;
        } else { 
            ind.getOntModel().enterCriticalSection(Lock.READ);
            try {
                this.rdfsLabel = webappDaoFactory.getJenaBaseDao().getLabel(ind);
                retrievedNullRdfsLabel = this.rdfsLabel == null;
                return this.rdfsLabel;
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
		if (this.imageInfo == null) {
			this.imageInfo = ImageInfo.instanceFromEntityUri(webappDaoFactory, this);
			log.trace("figured imageInfo for " + getURI() + ": '"
					+ this.imageInfo + "'");
		}
		if (this.imageInfo == null) {
			this.imageInfo = ImageInfo.EMPTY_IMAGE_INFO;
			log.trace("imageInfo for " + getURI() + " is empty.");
		}
		return this.imageInfo.getMainImage().getBytestreamAliasUrl();
	}

	@Override
	public String getThumbUrl() {
		if (this.imageInfo == null) {
			this.imageInfo = ImageInfo.instanceFromEntityUri(webappDaoFactory, this);
			log.trace("figured imageInfo for " + getURI() + ": '"
					+ this.imageInfo + "'");
		}
		if (this.imageInfo == null) {
			this.imageInfo = ImageInfo.EMPTY_IMAGE_INFO;
			log.trace("imageInfo for " + getURI() + " is empty.");
		}
		return this.imageInfo.getThumbnail().getBytestreamAliasUrl();
	}

    public List<ObjectPropertyStatement> getObjectPropertyStatements() {
        if (this.objectPropertyStatements != null) {
            return this.objectPropertyStatements;
        } else {
            try {
                webappDaoFactory.getObjectPropertyStatementDao().fillExistingObjectPropertyStatements(this);
            } catch (Exception e) {
                log.error(this.getClass().getName()+" could not fill existing ObjectPropertyStatements for "+this.getURI(), e);
            }
            return this.objectPropertyStatements;
        }
    }
    
    @Override
    public List<ObjectPropertyStatement> getObjectPropertyStatements(String propertyURI) {
    	if (propertyURI == null) {
    		return null;
    	}
    	List<ObjectPropertyStatement> objectPropertyStatements = new ArrayList<ObjectPropertyStatement>();
    	ind.getOntModel().enterCriticalSection(Lock.READ);
    	try {
    		StmtIterator sit = ind.listProperties(ind.getModel().getProperty(propertyURI));
    		while (sit.hasNext()) {
    			Statement s = sit.nextStatement();
    			if (!s.getSubject().canAs(OntResource.class) || !s.getObject().canAs(OntResource.class)) {
    			    continue;	
    			}
    			Individual subj = new IndividualJena((OntResource) s.getSubject().as(OntResource.class), webappDaoFactory);
    			Individual obj = new IndividualJena((OntResource) s.getObject().as(OntResource.class), webappDaoFactory);
    			ObjectProperty op = webappDaoFactory.getObjectPropertyDao().getObjectPropertyByURI(s.getPredicate().getURI());
    			if (subj != null && obj != null && op != null) {
    				ObjectPropertyStatement ops = new ObjectPropertyStatementImpl();
    				ops.setSubject(subj);
    				ops.setSubjectURI(subj.getURI());
    				ops.setObject(obj);
    				ops.setObjectURI(obj.getURI());
    				ops.setProperty(op);
    				ops.setPropertyURI(op.getURI());
    				objectPropertyStatements.add(ops);
    			}
    		}
     	} finally {
    		ind.getOntModel().leaveCriticalSection();
    	}
     	return objectPropertyStatements;
    }

    @Override
    public List<Individual> getRelatedIndividuals(String propertyURI) {
    	if (propertyURI == null) {
    		return null;
    	}
    	List<Individual> relatedIndividuals = new ArrayList<Individual>();
    	ind.getOntModel().enterCriticalSection(Lock.READ);
    	try {
    	    NodeIterator values = ind.listPropertyValues(ind.getModel().getProperty(propertyURI));
    	    while (values.hasNext()) {
    	    	RDFNode value = values.nextNode();
    	    	if (value.canAs(OntResource.class)) {
        	    	relatedIndividuals.add(
        	    		new IndividualJena((OntResource) value.as(OntResource.class), webappDaoFactory) );  
        	    } 
    	    }
    	} finally {
    		ind.getOntModel().leaveCriticalSection();
    	}
    	return relatedIndividuals;
    }
    
    @Override
    public Individual getRelatedIndividual(String propertyURI) {
    	if (propertyURI == null) {
    		return null;
    	}
    	ind.getOntModel().enterCriticalSection(Lock.READ);
    	try {
    	    RDFNode value = ind.getPropertyValue(ind.getModel().getProperty(propertyURI));
    	    if (value != null && value.canAs(OntResource.class)) {
    	    	return new IndividualJena((OntResource) value.as(OntResource.class), webappDaoFactory);  
    	    } else {
    	    	return null;
    	    }
    	} finally {
    		ind.getOntModel().leaveCriticalSection();
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
    public List<ObjectProperty> getPopulatedObjectPropertyList() {
        if (populatedObjectPropertyList == null) {
            populatedObjectPropertyList = webappDaoFactory.getObjectPropertyDao().getObjectPropertyList(this);
        }
        return populatedObjectPropertyList;       
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
    public List<DataProperty> getPopulatedDataPropertyList() {
        if (populatedDataPropertyList == null) {
            populatedDataPropertyList = webappDaoFactory.getDataPropertyDao().getDataPropertyList(this);
        }
        return populatedDataPropertyList;       
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
	 * The base method in {@link IndividualImpl} is adequate if the reasoner is
	 * up to date. 
	 * 
	 * If the base method returns false, check directly to see if
	 * any of the super classes of the direct classes will satisfy this request.
	 */
	@Override
	public boolean isVClass(String uri) {
    	if (uri == null) {
    		return false;
    	}

		if (super.isVClass(uri)) {
			return true;
		}

		VClassDao vclassDao = webappDaoFactory.getVClassDao();
		for (VClass vClass : getVClasses(true)) {
			for (String superClassUri: vclassDao.getAllSuperClassURIs(vClass.getURI())) {
				if (uri.equals(superClassUri)) {
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean isMemberOfClassProhibitedFromSearch(ProhibitedFromSearch pfs) {
		ind.getModel().enterCriticalSection(Lock.READ);
		try {
			StmtIterator stmtIt = ind.listProperties(RDF.type);
			try {
				while(stmtIt.hasNext()) {
					Statement stmt = stmtIt.nextStatement();
					if (stmt.getObject().isURIResource()) {
						String typeURI = ((Resource)stmt.getObject()).getURI();
						if (pfs.isClassProhibitedFromSearch(typeURI)) {
							return true;
						}
					}
				}
			} finally {
				stmtIt.close();
			}
			return false;
		} finally {
			ind.getModel().leaveCriticalSection();
		}
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
                        log.error(e, e);
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
    		log.error(e, e);
    	}
    }
    
    
}
