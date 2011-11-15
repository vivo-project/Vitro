/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.iri.IRI;
import com.hp.hpl.jena.iri.IRIFactory;
import com.hp.hpl.jena.iri.Violation;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.query.DataSource;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ClosableIterator;

import edu.cornell.mannlib.vitro.webapp.dao.ApplicationDao;
import edu.cornell.mannlib.vitro.webapp.dao.Classes2ClassesDao;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.DatatypeDao;
import edu.cornell.mannlib.vitro.webapp.dao.DisplayModelDao;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.MenuDao;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.OntologyDao;
import edu.cornell.mannlib.vitro.webapp.dao.PageDao;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyInstanceDao;
import edu.cornell.mannlib.vitro.webapp.dao.UserAccountsDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactoryConfig;
import edu.cornell.mannlib.vitro.webapp.dao.jena.pellet.PelletListener;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.JenaDataSourceSetupBase;

public class WebappDaoFactoryJena implements WebappDaoFactory {

    protected IndividualDao entityWebappDao;
    protected ApplicationDaoJena applicationDao;
    protected UserAccountsDao userAccountsDao;
    protected VClassGroupDao vClassGroupDao;
    protected PropertyGroupDao propertyGroupDao;

    private PageDao pageDao;
    private MenuDao menuDao;
    
    protected OntModelSelector ontModelSelector;
    
    protected WebappDaoFactoryConfig config;
    
    protected PelletListener pelletListener;

    protected String userURI;
	
	private Map<String,String> properties = new HashMap<String,String>();
	
	protected DatasetWrapperFactory dwf;
	
    /* **************** constructors **************** */

    public WebappDaoFactoryJena(WebappDaoFactoryJena base, String userURI) {
        this.ontModelSelector = base.ontModelSelector;
        this.config = base.config;
        this.userURI = userURI;
        this.dwf = base.dwf;
    }

    public WebappDaoFactoryJena(OntModelSelector ontModelSelector, 
                                OntModelSelector baseOntModelSelector,
                                OntModelSelector inferenceOntModelSelector,
                                WebappDaoFactoryConfig config, 
                                String userURI) {
    	
        this.ontModelSelector = ontModelSelector;
        this.config = config;
        this.userURI = userURI;
        
        Model assertions = (baseOntModelSelector != null) 
                ? baseOntModelSelector.getFullModel()
                : ontModelSelector.getFullModel();
        Model inferences = (inferenceOntModelSelector != null) 
                ? inferenceOntModelSelector.getFullModel()
                : null;
        
        Dataset dataset = makeInMemoryDataset(assertions, inferences);      
        this.dwf = new StaticDatasetFactory(dataset);
        
    } 

    public WebappDaoFactoryJena(OntModelSelector ontModelSelector, 
                                WebappDaoFactoryConfig config,
                                String userURI) {
        this(ontModelSelector, null, null, config, userURI);
    }

    public WebappDaoFactoryJena(OntModelSelector ontModelSelector, 
                                WebappDaoFactoryConfig config) {
        this(ontModelSelector, config, null);
    }
    
    public WebappDaoFactoryJena(OntModelSelector ontModelSelector, 
    		                    OntModelSelector baseOntModelSelector, 
    		                    OntModelSelector inferenceOntModelSelector, 
    		                    WebappDaoFactoryConfig config) {
        this(ontModelSelector, 
             baseOntModelSelector, 
             inferenceOntModelSelector, 
             config,
             null);
    }

    public WebappDaoFactoryJena(OntModelSelector ontModelSelector) {
        this(ontModelSelector, new WebappDaoFactoryConfig(), null);
    }
    
    public WebappDaoFactoryJena(OntModel ontModel) {
    	this(new SimpleOntModelSelector(
    			ontModel), new WebappDaoFactoryConfig(), null);
    }
    
    public OntModelSelector getOntModelSelector() {
    	return this.ontModelSelector;
    }
    
    public OntModel getOntModel() {
    	return this.ontModelSelector.getFullModel();
    }
       
    public static Dataset makeInMemoryDataset(Model assertions, 
                                              Model inferences) {
        DataSource dataset = DatasetFactory.create();        
        OntModel union = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);        
        if (assertions != null) {
            dataset.addNamedModel(
            		JenaDataSourceSetupBase.JENA_DB_MODEL, assertions);
            union.addSubModel(assertions);
        } 
        if (inferences != null) {
            dataset.addNamedModel(JenaDataSourceSetupBase.JENA_INF_MODEL, 
                    inferences);
            union.addSubModel(inferences);
        }
        dataset.setDefaultModel(union);
        return dataset;
    }

    /* ******************************************** */

	public Map<String,String> getProperties() {
		return this.properties;
	}

    public String checkURI(String uriStr) {
    	return checkURI(uriStr, true);
    }
    
    public String checkURI(String uriStr, boolean checkUniqueness) {
        uriStr = (uriStr == null) ? " " : uriStr;
		boolean validURI = true;
		String errorMsg = "";
		String duplicateMsg = "URI is already in use. " +
		                      "Please enter another URI. ";
		IRIFactory factory = IRIFactory.jenaImplementation();
	    IRI iri = factory.create( uriStr );
	    if (iri.hasViolation(false) ) {
	    	validURI = false;
	    	errorMsg += ((Violation)iri.violations(false).next())
	    	                    .getShortMessage() + " ";
	    } else if (checkUniqueness) {
	    	OntModel ontModel = ontModelSelector.getFullModel(); 
			ontModel.enterCriticalSection(Lock.READ);
			try {
				Resource newURIAsRes = ResourceFactory.createResource(uriStr);
				Property newURIAsProp = ResourceFactory.createProperty(uriStr);
				StmtIterator closeIt = ontModel.listStatements(
						newURIAsRes, null, (RDFNode)null);
				if (closeIt.hasNext()) {
					validURI = false;
					errorMsg+="Not a valid URI.  Please enter another URI. ";
					errorMsg+=duplicateMsg;
				}
				if (validURI) {
					closeIt = ontModel.listStatements(null, null, newURIAsRes);
					if (closeIt.hasNext()) {
						validURI = false;
						errorMsg+=duplicateMsg;
					}
				}
				if (validURI) {
					closeIt = ontModel.listStatements(
							null, newURIAsProp, (RDFNode)null);
					if (closeIt.hasNext()) {
						validURI = false;
						errorMsg+=duplicateMsg;
					}
				}
			} finally {
				ontModel.leaveCriticalSection();
			}
	    }
	    return (errorMsg.length()>0) ? errorMsg : null;
    }
    
    public WebappDaoFactory getUserAwareDaoFactory(String userURI) {
        return new WebappDaoFactoryJena(this, userURI);
    }

    public String getUserURI() {
        return userURI;
    }

    /* **************** accessors ***************** */

    public String getDefaultNamespace() {
        return config.getDefaultNamespace();
    }
    
    public String[] getPreferredLanguages() {
    	return config.getPreferredLanguages();
    }
    
    public Set<String> getNonuserNamespaces() {
    	return config.getNonUserNamespaces();
    }
    
    /**
     * This enables the WebappDaoFactory to check the status of a reasoner.
     * This will likely be refactored in future releases.
     */
    public void setPelletListener(PelletListener pl) {
    	this.pelletListener = pl;
    }
    
    public PelletListener getPelletListener() {
    	return this.pelletListener;
    }
    
    public List<String> getCommentsForResource(String resourceURI) {
    	List<String> commentList = new LinkedList<String>();
    	OntModel ontModel = ontModelSelector.getFullModel();
    	ontModel.enterCriticalSection(Lock.READ);
    	try {
    		OntResource res = ontModel.getOntResource(resourceURI);
    		if (res != null) {
    			ClosableIterator<RDFNode> closeIt = res.listComments(null);
    			try {
    				for(Iterator<RDFNode> commIt = closeIt; commIt.hasNext();) {
    					Literal lit = (Literal) commIt.next();
    					commentList.add(lit.getLexicalForm());
    				}
    			} finally {
    				closeIt.close();
    			}
    		}
    	} finally {
    		ontModel.leaveCriticalSection();
    	}
    	return commentList;
    }

    public IndividualDao getIndividualDao() {
        if (entityWebappDao != null)
            return entityWebappDao;
        else
            return entityWebappDao = new IndividualDaoJena(this);
    }
    
    public ApplicationDao getApplicationDao() {
    	if (applicationDao != null) {
    		return applicationDao;
    	} else {
    		return applicationDao = new ApplicationDaoJena(this);
    	}
    }

    public VClassGroupDao getVClassGroupDao() {
        if (vClassGroupDao != null)
            return vClassGroupDao;
        else
            return vClassGroupDao = new VClassGroupDaoJena(this);
    }
    
    public PropertyGroupDao getPropertyGroupDao() {
        if (propertyGroupDao != null)
            return propertyGroupDao;
        else
            return propertyGroupDao = new PropertyGroupDaoJena(this);
    }

    public UserAccountsDao getUserAccountsDao() {
    	if (userAccountsDao != null)
    		return userAccountsDao;
    	else
    		return userAccountsDao = new UserAccountsDaoJena(this);
    }
    
    Classes2ClassesDao classes2ClassesDao = null;
    public Classes2ClassesDao getClasses2ClassesDao() {
        if(classes2ClassesDao == null )
            classes2ClassesDao = new Classes2ClassesDaoJena(this);
        return classes2ClassesDao;
    }

    DataPropertyStatementDao dataPropertyStatementDao = null;
    public DataPropertyStatementDao getDataPropertyStatementDao() {
        if( dataPropertyStatementDao == null )
            dataPropertyStatementDao = new DataPropertyStatementDaoJena(
                    dwf, this);
        return dataPropertyStatementDao;
    }

    DatatypeDao datatypeDao = null;
    public DatatypeDao getDatatypeDao() {
        if( datatypeDao == null )
            datatypeDao = new DatatypeDaoJena(this);
        return datatypeDao;
    }

    DataPropertyDao dataPropertyDao = null;
    public DataPropertyDao getDataPropertyDao() {
        if( dataPropertyDao == null )
            dataPropertyDao = new DataPropertyDaoJena(dwf, this);
        return dataPropertyDao;
    }

    IndividualDao individualDao = null;
    public IndividualDao getEntityDao() {
        if( individualDao == null )
            individualDao = new IndividualDaoJena(this);
        return individualDao;
    }

    ObjectPropertyStatementDao objectPropertyStatementDao = null;
    public ObjectPropertyStatementDao getObjectPropertyStatementDao() {
        if( objectPropertyStatementDao == null )
            objectPropertyStatementDao = new ObjectPropertyStatementDaoJena(
                    dwf, this);
        return objectPropertyStatementDao;
    }

    private OntologyDao ontologyDao = null;
    public OntologyDao getOntologyDao() {
        if( ontologyDao == null )
            ontologyDao = new OntologyDaoJena(this);
        return ontologyDao;
    }

    private ObjectPropertyDao objectPropertyDao = null;
    public ObjectPropertyDao getObjectPropertyDao() {
        if( objectPropertyDao == null )
            objectPropertyDao = new ObjectPropertyDaoJena(dwf, this);
        return objectPropertyDao;
    }

    private PropertyInstanceDao propertyInstanceDao = null;
    public PropertyInstanceDao getPropertyInstanceDao() {
        if( propertyInstanceDao == null )
            propertyInstanceDao = new PropertyInstanceDaoJena(dwf, this);
        return propertyInstanceDao;
    }

    protected VClassDao vClassDao = null;
    public VClassDao getVClassDao() {
        if( vClassDao == null )
            vClassDao = new VClassDaoJena(this);
        return vClassDao;
    }

    private JenaBaseDao jenaBaseDao = null;    
    
    public JenaBaseDao getJenaBaseDao() {
        if (jenaBaseDao == null) {
            jenaBaseDao = new JenaBaseDao(this);
        }
        return jenaBaseDao;
    }

    @Override
    public PageDao getPageDao() {
        if( pageDao == null )
            pageDao = new PageDaoJena(this);
        return pageDao;
    }

    @Override
    public MenuDao getMenuDao(){
        if( menuDao == null )
            menuDao = new MenuDaoJena(this);
        return menuDao;
    }
    
    @Override
    public DisplayModelDao getDisplayModelDao(){
        return new DisplayModelDaoJena( this );
    }
    
    @Override
    public void close() {
        if (applicationDao != null) {
            applicationDao.close();
        }   
    }
    
    /**
     * Method for creating a copy - does not pass the same object
     * @param base
     */
    public WebappDaoFactoryJena (WebappDaoFactoryJena base) {
    	// Not sure if selector somehow has greater longevity so 
    	// making a copy instead of reference.    	
    	if(base.ontModelSelector instanceof OntModelSelectorImpl) {
    		OntModelSelectorImpl selector = new OntModelSelectorImpl();
    		selector.setABoxModel(base.ontModelSelector.getABoxModel());
    		selector.setApplicationMetadataModel(
    				base.ontModelSelector.getApplicationMetadataModel());
    		selector.setDisplayModel(base.ontModelSelector.getDisplayModel());
    		selector.setFullModel(base.ontModelSelector.getFullModel());
    		selector.setTBoxModel(base.ontModelSelector.getTBoxModel());
    		selector.setUserAccountsModel(
    				base.ontModelSelector.getUserAccountsModel());
    		this.ontModelSelector = selector;
    	} else if(base.ontModelSelector instanceof SimpleOntModelSelector) {
    		SimpleOntModelSelector selector = new SimpleOntModelSelector();
    		selector.setABoxModel(base.ontModelSelector.getABoxModel());
    		selector.setApplicationMetadataModel(
    				base.ontModelSelector.getApplicationMetadataModel());
    		selector.setDisplayModel(base.ontModelSelector.getDisplayModel());
    		selector.setFullModel(base.ontModelSelector.getFullModel());
    		selector.setTBoxModel(base.ontModelSelector.getTBoxModel());
    		selector.setUserAccountsModel(
    				base.ontModelSelector.getUserAccountsModel());
    		this.ontModelSelector = selector;
    	} else {
    		//Not sure what this is but will set to equivalence here
    		this.ontModelSelector = base.ontModelSelector;
    	}   	
        this.config = base.config;
        this.userURI = base.userURI;
        this.dwf = base.dwf;
    }
    
    /**
     * Method for using a special model, such as the display model, with the 
     * WebappDaoFactory.  The goal here is to modify this WebappDaoFactory so 
     * that it is using specialModel, specialTboxModel and specialDisplayModel 
     * for individual editing. 
     * 
     * DAOs related to the application configuration and user accounts
     * should remain unchanged.
     */    
    public void setSpecialDataModel(OntModel specialModel, 
                                    OntModel specialTboxModel, 
                                    OntModel specialDisplayModel) {
        if( specialModel == null )
            throw new IllegalStateException("specialModel must not be null");
        
    	//Can we get the "original" models here from somewhere?
    	OntModelSelector originalSelector = this.getOntModelSelector();
    	
    	// Set up model selector for this special WDF
    	// The selector is used by the object property DAO, therefore should be
    	// set up even though we use the new webapp dao factory object to 
    	// generate portions to overwrite the regular webapp dao factory.
    	
    	//The WDF expects the full model in the OntModelSelect that has 
    	//both the ABox and TBox.  This is used to run SPARQL queries against.    	
    	OntModel unionModel = ModelFactory.createOntologyModel(
    			OntModelSpec.OWL_MEM);
        unionModel.addSubModel(specialModel);
        
    	OntModelSelectorImpl specialSelector = new OntModelSelectorImpl();
    	specialSelector.setFullModel(unionModel);
    	// Keeping original application metadata model and adding special model.
    	// Adding both  allows us to prevent errors in  ApplicationDao which may 
    	// depend on a specific individual from the regular application metadata 
    	// model to  pick the theme.   Adding the new  model would  take care of 
    	// special  situations  where  the switch  model  may  contain important 
    	// information.
    	OntModel newApplicationModel = ModelFactory.createOntologyModel(
    			OntModelSpec.OWL_MEM);
    	newApplicationModel.add(specialModel);
    	newApplicationModel.add(originalSelector.getApplicationMetadataModel());
    	specialSelector.setApplicationMetadataModel(newApplicationModel);    	
    	
    	if(specialDisplayModel != null) {
    		specialSelector.setDisplayModel(specialDisplayModel);
    	    unionModel.addSubModel(specialDisplayModel);
    	} else {
    		OntModel selectorDisplayModel = originalSelector.getDisplayModel();
    		if(selectorDisplayModel != null) {
    			specialSelector.setDisplayModel(
    					originalSelector.getDisplayModel());
    		}
    	}
    	if(specialTboxModel != null) {
    	    unionModel.addSubModel(specialTboxModel);
    		specialSelector.setTBoxModel(specialTboxModel);
    	} else {
    		OntModel selectorTboxModel = originalSelector.getTBoxModel();
    		if(selectorTboxModel != null) {
    			specialSelector.setTBoxModel(originalSelector.getTBoxModel());
    		}
    	}    	    
    	
    	specialSelector.setABoxModel(specialModel);
    	specialSelector.setUserAccountsModel(specialModel);
    	// Although we're only using part of the new wadf and copy over below, 
    	// the object property dao utilizes methods that will employ the display 
    	// model returned from the simple ontmodel selector, so if the object 
    	// property dao is to be copied over we need to ensure we have the 
    	// correct display model and tbox model.
    	WebappDaoFactoryJena specialWadfj = new WebappDaoFactoryJena(
    			specialSelector);
    	entityWebappDao = specialWadfj.getIndividualDao();
    	vClassGroupDao = specialWadfj.getVClassGroupDao();
    	// To allow for testing, add a property group, this will allow
    	// the unassigned group method section to be executed and main Image to 
    	// be assigned to that group.  Otherwise, the dummy group does not allow 
    	// for the unassigned group to be executed.
    	propertyGroupDao = specialWadfj.getPropertyGroupDao();
    	objectPropertyDao = specialWadfj.getObjectPropertyDao();
    	objectPropertyStatementDao = specialWadfj.getObjectPropertyStatementDao();
    	dataPropertyDao = specialWadfj.getDataPropertyDao();
    	dataPropertyStatementDao = specialWadfj.getDataPropertyStatementDao();
    	// Why can't we set the selector to be the same?
    	ontModelSelector = specialSelector;
    	
    }
   
}
