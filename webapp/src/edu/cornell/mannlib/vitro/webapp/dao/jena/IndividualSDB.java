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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

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
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactorySDB.SDBDatasetMode;
import edu.cornell.mannlib.vitro.webapp.filestorage.model.ImageInfo;
import edu.cornell.mannlib.vitro.webapp.search.beans.ProhibitedFromSearch;

public class IndividualSDB extends IndividualImpl implements Individual {

    private static final Log log = LogFactory.getLog(
                IndividualSDB.class.getName());
    private OntResource ind = null;
    private WebappDaoFactoryJena webappDaoFactory = null;
    private Float _searchBoostJena = null;
    private boolean retreivedNullRdfsLabel = false;
    private DatasetWrapperFactory dwf = null;
    private SDBDatasetMode datasetMode = 
                SDBDatasetMode.ASSERTIONS_AND_INFERENCES;
    private String individualURI = null; 
    private Model model = null;
    private Boolean _hasThumb = null; 
    
    public IndividualSDB(String individualURI, 
                         DatasetWrapperFactory datasetWrapperFactory,
                         SDBDatasetMode datasetMode,
                         WebappDaoFactoryJena wadf,
                         Model initModel) {
    	this.individualURI = individualURI;
    	this.dwf = datasetWrapperFactory;
      
    	try {
	    	initModel.getLock().enterCriticalSection(Lock.READ);
	    	String getStatements = 
	    		"CONSTRUCT \n" +
	    		"{ <"+individualURI+">  <" + RDFS.label.getURI() + 
	    		        "> ?ooo. \n" +
	    		   "<"+individualURI+">  a ?type . \n" +
	    		 "} \n" +
	    		 "WHERE { \n" +
	    		 	"{ <"+individualURI+">  <" + RDFS.label.getURI() + 
	    		 	        "> ?ooo }  \n" +
	    		 	" UNION { <"+individualURI+"> a ?type } \n" +
	    		 "} "; 
    		this.model = QueryExecutionFactory.create(
    		        QueryFactory.create(getStatements), initModel)
    		                .execConstruct();
    	} finally {
    		initModel.getLock().leaveCriticalSection();
    	}
    	
    	OntModel ontModel = ModelFactory.createOntologyModel(
    	        OntModelSpec.OWL_MEM, model);
    	this.ind = ontModel.createOntResource(individualURI);  
    	setUpURIParts(ind);
        this.webappDaoFactory = wadf;
    }
    
    public IndividualSDB(String individualURI, 
            DatasetWrapperFactory datasetWrapperFactory, 
            SDBDatasetMode datasetMode,
            WebappDaoFactoryJena wadf,
            boolean skipInitialization) {
    	this.individualURI = individualURI;
    	this.datasetMode = datasetMode;
    	this.dwf = datasetWrapperFactory;
    	
    	if (skipInitialization) {
            OntModel ontModel = ModelFactory.createOntologyModel(
                    OntModelSpec.OWL_MEM);
            this.ind = ontModel.createOntResource(individualURI);  
    	} else {
        	DatasetWrapper w = getDatasetWrapper();
        	Dataset dataset = w.getDataset();
        	try {
    	    	dataset.getLock().enterCriticalSection(Lock.READ);
    	    	String getStatements = 
    	    		"CONSTRUCT " +
    	    		"{ <"+individualURI+">  <" + RDFS.label.getURI() + 
    	    		        "> ?ooo \n" +
    	    		 "} WHERE {" +
    	    		 	"{ <"+individualURI+">  <" + RDFS.label.getURI() + 
    	    		 	        "> ?ooo } \n" +
    	    		 "}";
        		model = QueryExecutionFactory.create(
        		        QueryFactory.create(getStatements), dataset)
        		                .execConstruct();
        	} finally {
        		dataset.getLock().leaveCriticalSection();
        		w.close();
        	}
        	
        	OntModel ontModel = ModelFactory.createOntologyModel(
        	        OntModelSpec.OWL_MEM, model);
        	
        	if (model.isEmpty() && noTriplesFor(individualURI)) {
        	    throw new IndividualNotFoundException();
        	}
        	
        	this.ind = ontModel.createOntResource(individualURI);  
    	}
    	setUpURIParts(ind);
        this.webappDaoFactory = wadf;
    }
    
    private boolean noTriplesFor(String individualURI) {
        String ask = "ASK { <" + individualURI + "> ?p ?o }";
        DatasetWrapper w = getDatasetWrapper();
        Dataset dataset = w.getDataset();
        dataset.getLock().enterCriticalSection(Lock.READ);
        try {
            Query askQuery = QueryFactory.create(ask, Syntax.syntaxARQ);
            QueryExecution qe = QueryExecutionFactory.create(askQuery, dataset);
            try {
                return !qe.execAsk();
            } finally {
                qe.close();
            }
        } finally {
            dataset.getLock().leaveCriticalSection();
            w.close();
        }
    }
    
    static final boolean SKIP_INITIALIZATION = true;
    
    public IndividualSDB(String individualURI, 
            DatasetWrapperFactory datasetWrapperFactory,
            SDBDatasetMode datasetMode,
            WebappDaoFactoryJena wadf) {
        this(individualURI, 
             datasetWrapperFactory, 
             datasetMode, 
             wadf, 
             !SKIP_INITIALIZATION);
    }
    
    public class IndividualNotFoundException extends RuntimeException {}
    
    private void setUpURIParts(OntResource ind) {
        if (ind != null) {
            if (ind.isAnon()) {
                this.setNamespace(VitroVocabulary.PSEUDO_BNODE_NS);
                this.setLocalName(ind.getId().toString());
            } else {
                this.URI = ind.getURI();
                this.namespace = ind.getNameSpace();
                this.localName = ind.getLocalName();
            }
        } else if (individualURI != null) {
            log.warn("Null individual returned for URI " + individualURI);
        }
    }

    private DatasetWrapper getDatasetWrapper() {
        return this.dwf.getDatasetWrapper();
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
        } else if( this.rdfsLabel == null && retreivedNullRdfsLabel ){
        	return null;
        } else { 
           
        	ind.getOntModel().enterCriticalSection(Lock.READ);
            try {
                this.rdfsLabel = webappDaoFactory.getJenaBaseDao().getLabel(ind);
                retreivedNullRdfsLabel = this.rdfsLabel == null;
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
        	List<VClass> clist = getVClasses(true);
        	return (clist.size() > 0) ? clist.get(0).getURI() : null; 
        }
    }

    public VClass getVClass() {
        if (this.vClass != null) {
            return this.vClass;
        } else {
        	List<VClass> clist = getVClasses(true);
            return (clist.size() > 0) ? clist.get(0) : null ; 
        } 
    }

    public Timestamp getModTime() {
        if (modTime != null) {
            return modTime;
        } else {
           
        	ind.getOntModel().enterCriticalSection(Lock.READ);
            try {
                Date modDate = webappDaoFactory.getJenaBaseDao()
                        .getPropertyDateTimeValue(
                                ind,webappDaoFactory.getJenaBaseDao().MODTIME);
                if (modDate != null) {
                    modTime = new Timestamp(modDate.getTime());
                }
                return modTime;
            } finally {
               
            	ind.getOntModel().leaveCriticalSection();
            }
        }
    }

    private synchronized void constructProperty(OntResource ind, String propertyURI) {
        DatasetWrapper w = getDatasetWrapper();
        Dataset dataset = w.getDataset();
        dataset.getLock().enterCriticalSection(Lock.READ);
        try {
            String[] graphVars = { "?g" };
            String queryStr = 
                "CONSTRUCT { <"+ind.getURI()+"> <" + propertyURI + "> ?value } \n" +
                    "WHERE {  \n" +
                    "<" + ind.getURI() +"> <" + propertyURI + "> ?value \n" + 
                    WebappDaoFactorySDB.getFilterBlock(graphVars, datasetMode) +
                    "\n} ";
            Query query = QueryFactory.create(queryStr);
            QueryExecution qe = QueryExecutionFactory.create(
                    query, dataset);
            qe.execConstruct(ind.getModel());
        } finally {
            dataset.getLock().leaveCriticalSection();
            w.close();
        }
    }

    public Float getSearchBoost(){ 
        if( this._searchBoostJena != null ){
            return this._searchBoostJena;
        }else{
            String[] graphVars = { "?g" };
            String getPropertyValue = 
            	"SELECT ?value \n" +
            	"WHERE { GRAPH ?g { \n" +
            	"<" +individualURI+ "> <" +webappDaoFactory.getJenaBaseDao().SEARCH_BOOST_ANNOT+ "> ?value }\n" + 
            	WebappDaoFactorySDB.getFilterBlock(graphVars, datasetMode) + "\n" +
            	"}";
            
            DatasetWrapper w = getDatasetWrapper();
            Dataset dataset = w.getDataset();
        	dataset.getLock().enterCriticalSection(Lock.READ);
            try{
                ResultSet rs = QueryExecutionFactory.create(
                		QueryFactory.create(getPropertyValue),
                		dataset).execSelect();
                
                if(rs.hasNext()){
                	QuerySolution qs = rs.nextSolution();
                	if(qs.get("value") !=null){
                		Literal value = qs.get("value").asLiteral();
                		searchBoost = Float.parseFloat(value.getLexicalForm());
                		return searchBoost;
                	}
                }
                else{
                	    return null;
                }
            } catch (Exception e){
            	log.error(e,e); 
                return null;            	
            }finally{
            	dataset.getLock().leaveCriticalSection();
            	w.close();
            }
        }
        return null;
    }    

	@Override
	public String getMainImageUri() {  
		if (this.mainImageUri != NOT_INITIALIZED) {
			return mainImageUri;
		} else {
			List<ObjectPropertyStatement> mainImgStmts = 
					getObjectPropertyStatements(VitroVocabulary.IND_MAIN_IMAGE);
			if (mainImgStmts != null && mainImgStmts.size() > 0) {
				// arbitrarily return the first value in the list
				mainImageUri = mainImgStmts.get(0).getObjectURI();
				return mainImageUri;				
			} 
			return null;
		}
	}

	@Override
	public String getImageUrl() { 
		if (this.imageInfo == null) {
			this.imageInfo = ImageInfo.instanceFromEntityUri(
			        webappDaoFactory, this);
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
			this.imageInfo = ImageInfo.instanceFromEntityUri(
			        webappDaoFactory, this);
			log.trace("figured imageInfo for " + getURI() + ": '"
					+ this.imageInfo + "'");
		}
		if (this.imageInfo == null) {
			this.imageInfo = ImageInfo.EMPTY_IMAGE_INFO;
			log.trace("imageInfo for " + getURI() + " is empty.");
		}
		return this.imageInfo.getThumbnail().getBytestreamAliasUrl();
	}

	@Override
	public boolean hasThumb(){
	    if( _hasThumb != null ){
	        return _hasThumb;	    
        }else{            
            String ask = 
                "ASK { " +
                "    <" + individualURI + "> <http://vitro.mannlib.cornell.edu/ns/vitro/public#mainImage> ?mainImage . \n" +
                "    ?mainImage <http://vitro.mannlib.cornell.edu/ns/vitro/public#thumbnailImage> ?thumbImage . }\n"  ;                     
          DatasetWrapper w = getDatasetWrapper();
            Dataset dataset = w.getDataset();
            dataset.getLock().enterCriticalSection(Lock.READ);
            QueryExecution qexec = null;
            try{            
                qexec = QueryExecutionFactory.create(QueryFactory.create(ask), dataset);
                _hasThumb = qexec.execAsk();
            }catch(Exception ex){
                _hasThumb = false;
                log.error(ex,ex);
            }finally{
                if(qexec!=null) qexec.close();
                dataset.getLock().leaveCriticalSection();
                w.close();
            }
            return _hasThumb;
        }
	}
	
    public List<ObjectPropertyStatement> getObjectPropertyStatements() { 
        if (this.objectPropertyStatements != null) {
            return this.objectPropertyStatements;
        } else {
            try {
                webappDaoFactory.getObjectPropertyStatementDao()
                        .fillExistingObjectPropertyStatements(this);
            } catch (Exception e) {
                log.error("Could not fill existing ObjectPropertyStatements for "
                        + this.getURI(), e);
            }
            return this.objectPropertyStatements;
        }
    }
    
    @Override
    public List<ObjectPropertyStatement> getObjectPropertyStatements(String propertyURI) {
    	if (propertyURI == null) {
    		return null;
    	}
    	List<ObjectPropertyStatement> objectPropertyStatements = new ArrayList
    	        <ObjectPropertyStatement>();
	    Model tempModel = ModelFactory.createDefaultModel();
        OntModel ontModel = ModelFactory.createOntologyModel(
                OntModelSpec.OWL_MEM);
        DatasetWrapper w = getDatasetWrapper();
        Dataset dataset = w.getDataset();
    	dataset.getLock().enterCriticalSection(Lock.READ);
        QueryExecution qexec = null;
    	try {
    		String valuesOfProperty = 
    			"CONSTRUCT{ <" + this.individualURI + "> <" + propertyURI + "> ?object }" +
    			"WHERE{ <" + this.individualURI + "> <" + propertyURI + "> ?object } \n";
            qexec = QueryExecutionFactory.create(QueryFactory.create(valuesOfProperty), dataset);
    	    tempModel = qexec.execConstruct();
    	    ontModel.add(tempModel.listStatements());
    	    Resource ontRes = ontModel.getResource(this.individualURI);
    	    StmtIterator sit = ontRes.listProperties(ontRes.getModel().getProperty(propertyURI));
    	    while (sit.hasNext()) {
    			Statement s = sit.nextStatement();
    			if (!s.getSubject().canAs(OntResource.class) || !s.getObject().canAs(OntResource.class)) {
    			    continue;	
    			}
    			Individual subj = new IndividualSDB(((OntResource) s.getSubject().as(OntResource.class)).getURI(), this.dwf, datasetMode, webappDaoFactory);
    			Individual obj = new IndividualSDB(((OntResource) s.getObject().as(OntResource.class)).getURI(), this.dwf, datasetMode, webappDaoFactory);
    			ObjectProperty op = webappDaoFactory.getObjectPropertyDao().getObjectPropertyByURI(s.getPredicate().getURI());
    			// We don't want to filter out statements simply because we 
    			// can't find a type for the property, so we'll just make a 
    			// new ObjectProperty bean if we can't get one from the DAO.
    			if (op == null) {
    				op = new ObjectProperty();
    				op.setURI(propertyURI);
    			}
    			if (subj != null && obj != null) {
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
            if(qexec!=null) qexec.close();
    		tempModel.close();
    		ontModel.close();
     		dataset.getLock().leaveCriticalSection();
     		w.close();
    	}
     	return objectPropertyStatements;
    }

    @Override
    public List<Individual> getRelatedIndividuals(String propertyURI) { 
    	if (propertyURI == null) {
    		return null;
    	}
    	List<Individual> relatedIndividuals = new ArrayList<Individual>();
    	
    	DatasetWrapper w = getDatasetWrapper();
    	Dataset dataset = w.getDataset();
    	dataset.getLock().enterCriticalSection(Lock.READ);
    	try {
    		String valuesOfProperty = 
    			"SELECT ?object" +
    			"WHERE{ <" + this.individualURI + "> <" + 
    			        propertyURI + "> ?object } \n";
    		ResultSet values = QueryExecutionFactory.create(
    		        QueryFactory.create(valuesOfProperty), dataset)
    		                .execSelect();
    		QuerySolution result = null;
    	    while (values.hasNext()) {
    	    	result = values.next();
    	    	RDFNode value = result.get("object");
    	    	if (value.canAs(OntResource.class)) {
        	    	relatedIndividuals.add(
        	    		new IndividualSDB(
        	    		        ((OntResource) value.as(OntResource.class))
        	    		                .getURI(), 
        	    		                this.dwf, 
        	    		                datasetMode, 
        	    		                webappDaoFactory) );  
        	    } 
    	    }
    	} finally {
    		dataset.getLock().leaveCriticalSection();
    		w.close();
    	}
    	return relatedIndividuals;
    }
    
    @Override
    public Individual getRelatedIndividual(String propertyURI) { 
    	if (propertyURI == null) {
    		return null;
    	}
    	DatasetWrapper w = getDatasetWrapper();
    	Dataset dataset = w.getDataset();
    	dataset.getLock().enterCriticalSection(Lock.READ);
    	try {
    		String valueOfProperty = 
    			"SELECT ?object " +
    			"WHERE{ <" + this.individualURI + "> <" + 
    			        propertyURI + "> ?object } \n";
    		QueryExecution qe = QueryExecutionFactory.create(
                    QueryFactory.create(valueOfProperty), dataset);
    		try {
    		    ResultSet results = qe.execSelect();
        		if (results.hasNext()) {
            		QuerySolution result = results.next();
            		RDFNode value = result.get("object");
            	    if (value != null && value.canAs(OntResource.class)) {
            	    	return new IndividualSDB(
            	    	        ((OntResource) value.as(OntResource.class)).getURI(), 
            	    	                dwf, datasetMode, webappDaoFactory);  
            	    } 
        	    }
        		return null;
    		} finally {
    		    qe.close();
    		}
    	} finally {
    		dataset.getLock().leaveCriticalSection();
    		w.close();
    	}
    }
    
    public List<ObjectProperty> getObjectPropertyList() { 
        if (this.propertyList != null) {
            return this.propertyList;
        } else {
            try {
                webappDaoFactory.getObjectPropertyDao()
                        .fillObjectPropertiesForIndividual( this );
            } catch (Exception e) {
                log.error("Could not fillEntityProperties for " + this.getURI(), e);
            }
            return this.propertyList;
        }
    }

    @Override 
    public List<ObjectProperty> getPopulatedObjectPropertyList() {
        if (populatedObjectPropertyList == null) {
            populatedObjectPropertyList = webappDaoFactory
                    .getObjectPropertyDao().getObjectPropertyList(this);
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
                webappDaoFactory.getDataPropertyStatementDao()
                        .fillExistingDataPropertyStatementsForIndividual(this);
            } catch (Exception e) {
                log.error("Could not fill existing DataPropertyStatements for "
                                + this.getURI(), e);
            }
            return this.dataPropertyStatements;
        }
    }

    public List getDataPropertyList() {
        if (this.datatypePropertyList != null) { 
            return this.datatypePropertyList;
        } else {
            try {
                webappDaoFactory.getDataPropertyDao()
                        .fillDataPropertiesForIndividual( this );
            } catch (Exception e) {
                log.error("Could not fill data properties for " + this.getURI(), e);
            }
            return this.datatypePropertyList;
        }
    }

    @Override 
    public List<DataProperty> getPopulatedDataPropertyList() {
        if (populatedDataPropertyList == null) {
            populatedDataPropertyList = webappDaoFactory.getDataPropertyDao()
                    .getDataPropertyList(this);
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
        if (this.externalIds != null) {
            return this.externalIds;
        } else {
            try {
                List<DataPropertyStatement> dpsList = 
                        new ArrayList<DataPropertyStatement>();
                dpsList.addAll(webappDaoFactory.getIndividualDao()
                        .getExternalIds(this.getURI(), null));
                this.externalIds = dpsList;
            } catch (Exception e) {
                log.error("Could not fill external IDs for " + this.getURI(), e);
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
		Model tempModel = null;
		if (ind.getModel().contains((Resource) null, RDF.type, (RDFNode) null)){
		    tempModel = ind.getModel();
		} else {
			String[] graphVars = { "?g" };
    		String getTypes = 
        		"CONSTRUCT{ <" + this.individualURI + "> <" + RDF.type +
        		        "> ?types }\n" +
        		"WHERE{ GRAPH ?g"   
        		+ " { <" + this.individualURI +"> <" +RDF.type+ "> ?types } \n" 
   		        + WebappDaoFactorySDB.getFilterBlock(
   		        		graphVars, (direct 
   		        				? WebappDaoFactorySDB.SDBDatasetMode
   		        						.ASSERTIONS_ONLY 
   		        			    : datasetMode)) 
        		+ "} \n";        	
        	DatasetWrapper w = getDatasetWrapper();
        	Dataset dataset = w.getDataset();
        	dataset.getLock().enterCriticalSection(Lock.READ);
        	try {
        	    tempModel = QueryExecutionFactory.create(
        	            QueryFactory.create(getTypes), dataset).execConstruct();
        	} finally {
        	    dataset.getLock().leaveCriticalSection();
        	    w.close();
        	}
		}
    	StmtIterator stmtItr = tempModel.listStatements(
    	        (Resource) null, RDF.type, (RDFNode) null);
    	LinkedList<String> list = new LinkedList<String>();
    	while(stmtItr.hasNext()){
    		Statement stmt = stmtItr.nextStatement();
    		if (stmt.getObject().isResource() && !stmt.getObject().isAnon()) {
    			list.add(((Resource) stmt.getObject()).getURI());
    		}
    	}
    	Iterator<String> itr = null;
    	VClassDao checkSubClass = this.webappDaoFactory.getVClassDao();
    	boolean directTypes = false;
    	String currentType = null;
	    ArrayList<String> done = new ArrayList<String>();
	    
	    /* Loop for comparing starts here */
	    if(direct){
        	while(!directTypes){
        		 itr = list.listIterator();
        		 
        		do{
        			if(itr.hasNext()){
        		 currentType = itr.next();}
        			else{
        				directTypes = true; // get next element for comparison
        			 break;}
        		}while(done.contains(currentType));
        		
        		if(directTypes)
        			break; 
        		    // check to see if it's all over otherwise start comparing
        		else
        	    itr = list.listIterator();	
        		
            	while(itr.hasNext()){
            		String nextType = itr.next();
            	    if(checkSubClass.isSubClassOf(currentType, nextType) 
            	            && !currentType.equalsIgnoreCase(nextType)){
            	    	itr.remove();
            	    }
            	}
            	
            	done.add(currentType);  // add the uri to done list. 
        	}
	    }
    	
    	/* Loop for comparing ends here */
	    Iterator<String> typeIt = list.iterator();
		
		for (Iterator it = typeIt; it.hasNext();) {
			Resource type = ResourceFactory
			        .createResource(it.next().toString());
			String typeURI = (!type.isAnon()) 
			        ? type.getURI() 
			        : VitroVocabulary.PSEUDO_BNODE_NS 
			                + type.getId().toString();
			if (type.getNameSpace() == null || 
			        (!webappDaoFactory.getNonuserNamespaces()
			                .contains(type.getNameSpace())) ) {
				VClass vc = webappDaoFactory.getVClassDao()
				        .getVClassByURI(type.getURI());
				if (vc != null) {
					vClassList.add(vc);
				}
			}
		}

		try {
			Collections.sort(vClassList);
		} catch (Exception e) {
		    log.error("Unable to sort VClass list", e);
		}
		
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
			for (String superClassUri: vclassDao.getAllSuperClassURIs(
			        vClass.getURI())) {
				if (uri.equals(superClassUri)) {
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean isMemberOfClassProhibitedFromSearch(ProhibitedFromSearch pfs) {  

		List<VClass> types =  getVClasses(false);
		Iterator<VClass> itr = types.iterator();

		while(itr.hasNext()) {
			String typeURI = itr.next().getURI();
			if (pfs.isClassProhibitedFromSearch(typeURI)) {
				return true;
			}
		}

		return false;

	}
    /**
     * Overriding the base method so that we can do the sorting by arbitrary property here.  An
     * IndividualSDB has a reference back to the model; everything else is just a dumb bean (for now).
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
                        log.warn( "IndividualSDB.sortObjectPropertiesForDisplay passed object property statement with no range entity.");
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
                        log.warn( "IndividualSDB.sortObjectPropertyStatementsForDisplay() was passed an object property statement with no range entity.");
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
