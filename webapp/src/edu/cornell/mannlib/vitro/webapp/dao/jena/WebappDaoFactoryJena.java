/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.iri.IRI;
import com.hp.hpl.jena.iri.IRIFactory;
import com.hp.hpl.jena.iri.Violation;
import com.hp.hpl.jena.ontology.OntClass;
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
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vitro.webapp.beans.PropertyGroup;
import edu.cornell.mannlib.vitro.webapp.dao.ApplicationDao;
import edu.cornell.mannlib.vitro.webapp.dao.Classes2ClassesDao;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.DatatypeDao;
import edu.cornell.mannlib.vitro.webapp.dao.DisplayModelDao;
import edu.cornell.mannlib.vitro.webapp.dao.FlagDao;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.LinksDao;
import edu.cornell.mannlib.vitro.webapp.dao.LinktypeDao;
import edu.cornell.mannlib.vitro.webapp.dao.MenuDao;
import edu.cornell.mannlib.vitro.webapp.dao.NamespaceDao;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.OntologyDao;
import edu.cornell.mannlib.vitro.webapp.dao.PageDao;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyInstanceDao;
import edu.cornell.mannlib.vitro.webapp.dao.UserAccountsDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.pellet.PelletListener;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.JenaDataSourceSetupBase;

public class WebappDaoFactoryJena implements WebappDaoFactory {

    protected IndividualDao entityWebappDao;
    protected FlagDao flagDao;
    protected LinksDao linksDao;
    protected LinktypeDao linktypeDao;
    protected ApplicationDaoJena applicationDao;
    protected UserAccountsDao userAccountsDao;
    protected VClassGroupDao vClassGroupDao;
    protected PropertyGroupDao propertyGroupDao;

    private PageDao pageDao;
    private MenuDao menuDao;
    
    protected OntModelSelector ontModelSelector;
    
    protected String defaultNamespace;
    protected HashSet<String> nonuserNamespaces;
    protected String[] preferredLanguages;
    
    protected PelletListener pelletListener;

    protected String userURI;

    protected Map<String,OntClass> flag2ValueMap;
    protected Map<Resource,String> flag2ClassLabelMap;
    
	protected boolean INCLUDE_TOP_CONCEPT = false;
	protected boolean INCLUDE_BOTTOM_CONCEPT = false;
	
	private Map<String,String> properties = new HashMap<String,String>();
	
	protected DatasetWrapperFactory dwf;

    // for temporary use to construct URIs for the things that still use integer IDs.
    // As these objects get changed to support getURI(), this should become unnecessary.

    /* **************** constructors **************** */

    public WebappDaoFactoryJena(WebappDaoFactoryJena base, String userURI) {
        this.ontModelSelector = base.ontModelSelector;
        this.defaultNamespace = base.defaultNamespace;
        this.nonuserNamespaces = base.nonuserNamespaces;
        this.preferredLanguages = base.preferredLanguages;
        this.userURI = userURI;
        this.flag2ValueMap = base.flag2ValueMap;
        this.flag2ClassLabelMap = base.flag2ClassLabelMap;
        this.dwf = base.dwf;
    }

    public WebappDaoFactoryJena(OntModelSelector ontModelSelector, 
                                OntModelSelector baseOntModelSelector,
                                OntModelSelector inferenceOntModelSelector,
                                String defaultNamespace, 
                                HashSet<String> nonuserNamespaces, 
                                String[] preferredLanguages, 
                                String userURI){
    	
        this.ontModelSelector = ontModelSelector;
        
        // BJL23 2009-04-27
        // As I understand it, the the following setting should allow more 
        // relaxed use of .as() in Jena's polymorphic framework and avoid
        // ClassCastExceptions deep in Jena when, for example, a resource 
        // used as a class is not explicitly typed as such.  In reality,
        // this setting seems to have dangerous consequences and causes
        // bizarre other exceptions to be thrown in ways that make the model
        // seem to behave even *more* strictly.  Uncomment at your own risk:
        
        // this.ontModel.setStrictMode(false);
        
        if (defaultNamespace != null) {
            this.defaultNamespace = defaultNamespace;
        } else {
            initDefaultNamespace();
        }
        if (nonuserNamespaces != null) {
            this.nonuserNamespaces = nonuserNamespaces;
        } else {
            initNonuserNamespaces();
        }
        if (preferredLanguages != null) {
            this.preferredLanguages = preferredLanguages;
        } else {
            initPreferredLanguages();
        }
        this.userURI = userURI;
        makeFlag2ConvenienceMaps();
        Model languageUniversalsModel = ModelFactory.createDefaultModel();
        if (INCLUDE_TOP_CONCEPT) {
        	Resource top = getTopConcept();
        	if (top != null) {
        		languageUniversalsModel.add(top, RDF.type, this.ontModelSelector.getTBoxModel().getProfile().CLASS());
        	}
        }
        if (INCLUDE_BOTTOM_CONCEPT) {
        	Resource bottom = getBottomConcept();
        	if (bottom != null) {
        		languageUniversalsModel.add(bottom, RDF.type, this.ontModelSelector.getTBoxModel().getProfile().CLASS());
        	}
        }
        if (languageUniversalsModel.size()>0) {
        	this.ontModelSelector.getTBoxModel().addSubModel(languageUniversalsModel);
        }
        
        Model assertions = (baseOntModelSelector != null) 
                ? baseOntModelSelector.getFullModel()
                : ontModelSelector.getFullModel();
        Model inferences = (inferenceOntModelSelector != null) 
                ? inferenceOntModelSelector.getFullModel()
                : null;
        
        Dataset dataset = makeInMemoryDataset(assertions, inferences);      
        this.dwf = new StaticDatasetFactory(dataset);
        
    } 
    
    public static Dataset makeInMemoryDataset(Model assertions, Model inferences) {
        DataSource dataset = DatasetFactory.create();
        
        OntModel union = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        
        if (assertions != null) {
            dataset.addNamedModel(JenaDataSourceSetupBase.JENA_DB_MODEL, assertions);
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
    
    public WebappDaoFactoryJena(OntModelSelector ontModelSelector, 
            String defaultNamespace, 
            HashSet<String> nonuserNamespaces, 
            String[] preferredLanguages, 
            String userURI){
        this(ontModelSelector, 
             null, 
             null,
             defaultNamespace,
             nonuserNamespaces, 
             preferredLanguages, 
             userURI);
    }

    public WebappDaoFactoryJena(OntModelSelector ontModelSelector, String defaultNamespace, HashSet<String> nonuserNamespaces, String[] preferredLanguages){
        this(ontModelSelector, defaultNamespace, nonuserNamespaces, preferredLanguages, null);
    }
    
    public WebappDaoFactoryJena(OntModelSelector ontModelSelector, OntModelSelector baseOntModelSelector, OntModelSelector inferenceOntModelSelector, String defaultNamespace, HashSet<String> nonuserNamespaces, String[] preferredLanguages){
        this(ontModelSelector, baseOntModelSelector, inferenceOntModelSelector, defaultNamespace, nonuserNamespaces, preferredLanguages, null);
    }

    public WebappDaoFactoryJena(OntModelSelector ontModelSelector) {
        this(ontModelSelector, null, null, null, null, null);
    }

    public WebappDaoFactoryJena(OntModel ontModel, String defaultNamespace, HashSet<String> nonuserNamespaces, String[] preferredLanguages, String userURI){
    	this(new SimpleOntModelSelector(ontModel), defaultNamespace, nonuserNamespaces, preferredLanguages, userURI);
    } 

    public WebappDaoFactoryJena(OntModel ontModel, String defaultNamespace, HashSet<String> nonuserNamespaces, String[] preferredLanguages){
        this(new SimpleOntModelSelector(ontModel), defaultNamespace, nonuserNamespaces, preferredLanguages, null);
    }

    public WebappDaoFactoryJena(OntModel ontModel) {
        this(new SimpleOntModelSelector(ontModel), null, null, null, null, null);
    }
    
    public OntModelSelector getOntModelSelector() {
    	return this.ontModelSelector;
    }
    
    public OntModel getOntModel() {
    	return this.ontModelSelector.getFullModel();
    }
    
    /**
     * Return the current language profile's Top concept as a Jena resource, or null if not applicable.
     * The special case is RDFS, where we use rdfs:Resource as the analog of Top, rather than returning null.
     * @return
     */
    public Resource getTopConcept() {
      	Resource top = null;
    	if (this.ontModelSelector.getTBoxModel().getProfile().NAMESPACE().equals(RDFS.getURI())) {
    		top = RDFS.Resource;
    	} else {
    		top = this.ontModelSelector.getTBoxModel().getProfile().THING();
    	}
    	return top;
    }
    
    /**
     * Return the current language profile's Bottom concept as a Jena resource, or null if not applicable.
     * @return
     */
    public Resource getBottomConcept() {
    	return this.ontModelSelector.getTBoxModel().getProfile().THING();
    }
    
    private void initDefaultNamespace() {
        defaultNamespace = "http://vivo.library.cornell.edu/ns/0.1#";
    }

    private void initNonuserNamespaces() {
        nonuserNamespaces = new HashSet<String>();
        //nonuserNamespaces.add(VitroVocabulary.RDF);
        //nonuserNamespaces.add(VitroVocabulary.RDFS);
        //nonuserNamespaces.add(VitroVocabulary.OWL);
        nonuserNamespaces.add(VitroVocabulary.vitroURI);
        nonuserNamespaces.add("http://lowe.mannlib.cornell.edu/ns/vitro0.1/vitro.owl#"); // obsolete Vitro URI
    }

    private void initPreferredLanguages() {
        preferredLanguages = new String[3];
        preferredLanguages[0] = "en-US";
        preferredLanguages[1] = "en";
        preferredLanguages[2] = "EN";
    }

    private void makeFlag2ConvenienceMaps() {
        HashMap<String,OntClass> flag2ValueHashMap = new HashMap<String,OntClass>();
        HashMap<Resource,String> flag2ClassLabelHashMap = new HashMap<Resource,String>();
        for (Iterator classIt = ontModelSelector.getTBoxModel().listClasses(); classIt.hasNext(); ) {
            OntClass ontClass = (OntClass) classIt.next();
            String ontClassName = ontClass.getLocalName();
            if(ontClassName != null && ontClass.getNameSpace().equals(VitroVocabulary.vitroURI) && ontClassName.indexOf("Flag2Value")==0) {
                String ontClassLabel = ontClass.getLabel(null);
                if (ontClassLabel != null) {
                    flag2ValueHashMap.put(ontClassLabel, ontClass);
                    flag2ClassLabelHashMap.put(ontClass, ontClassLabel);
                }
            }
        }
        this.flag2ValueMap = flag2ValueHashMap;
        this.flag2ClassLabelMap = flag2ClassLabelHashMap;
    }

    private Map<Resource,String> makeFlag2ClassLabelMap() {
        HashMap<Resource,String> flag2ClassLabelMap = new HashMap<Resource,String>();
        return flag2ClassLabelMap;
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
		String duplicateMsg = "URI is already in use. Please enter another URI. ";
		IRIFactory factory = IRIFactory.jenaImplementation();
	    IRI iri = factory.create( uriStr );
	    if (iri.hasViolation(false) ) {
	    	validURI = false;
	    	errorMsg += ((Violation)iri.violations(false).next()).getShortMessage()+" ";
	    } else if (checkUniqueness) {
	    	OntModel ontModel = ontModelSelector.getFullModel(); 
			ontModel.enterCriticalSection(Lock.READ);
			try {
				Resource newURIAsRes = ResourceFactory.createResource(uriStr);
				Property newURIAsProp = ResourceFactory.createProperty(uriStr);
				ClosableIterator closeIt = ontModel.listStatements(newURIAsRes, null, (RDFNode)null);
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
					closeIt = ontModel.listStatements(null, newURIAsProp, (RDFNode)null);
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
        // TODO: put the user-aware factories in a hashmap so we don't keep re-creating them
        return new WebappDaoFactoryJena(this, userURI);
    }

    public String getUserURI() {
        return userURI;
    }

    /* **************** accessors ***************** */

    public String getDefaultNamespace() {
        return defaultNamespace;
    }
    
    public static int OWL_CONST = 202;
    public static int RDFS_CONST = 100;
    
    public int getLanguageProfile() {
    	OntModel ontModel = ontModelSelector.getTBoxModel();
    	if (ontModel.getProfile().NAMESPACE().equals(OWL.NAMESPACE.getURI())) {
    		return OWL_CONST;
    	} else if (ontModel.getProfile().NAMESPACE().equals(RDFS.getURI())) {
    		return RDFS_CONST;
    	} else {
    		return -1;
    	}
    }
    
    public String[] getPreferredLanguages() {
    	return this.preferredLanguages;
    }
    
    public Set<String> getNonuserNamespaces() {
    	return nonuserNamespaces;
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
    			ClosableIterator closeIt = res.listComments(null);
    			try {
    				for (Iterator commIt = closeIt; commIt.hasNext();) {
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

    NamespaceDao namespaceDao = null;
    public NamespaceDao getNamespaceDao() {
        if( namespaceDao == null )
            namespaceDao = new NamespaceDaoJena(this);
        return namespaceDao;
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
            propertyInstanceDao = new PropertyInstanceDaoJena(this);
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

    public Map<String,OntClass> getFlag2ValueMap() {
        return this.flag2ValueMap;
    }

    public Map<Resource,String> getFlag2ClassLabelMap() {
        return this.flag2ClassLabelMap;
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
    
    //Method for creating a copy - does not pass the same object
    public WebappDaoFactoryJena (WebappDaoFactoryJena base) {
    	//Not sure if selector somehow has greater longevity so making a copy instead of reference
    	
    	if(base.ontModelSelector instanceof OntModelSelectorImpl) {
    		OntModelSelectorImpl selector = new OntModelSelectorImpl();
    		selector.setABoxModel(base.ontModelSelector.getABoxModel());
    		selector.setApplicationMetadataModel(base.ontModelSelector.getApplicationMetadataModel());
    		selector.setDisplayModel(base.ontModelSelector.getDisplayModel());
    		selector.setFullModel(base.ontModelSelector.getFullModel());
    		selector.setTBoxModel(base.ontModelSelector.getTBoxModel());
    		selector.setUserAccountsModel(base.ontModelSelector.getUserAccountsModel());
    		this.ontModelSelector = selector;
    	} else if(base.ontModelSelector instanceof SimpleOntModelSelector) {
    		SimpleOntModelSelector selector = new SimpleOntModelSelector();
    		selector.setABoxModel(base.ontModelSelector.getABoxModel());
    		selector.setApplicationMetadataModel(base.ontModelSelector.getApplicationMetadataModel());
    		selector.setDisplayModel(base.ontModelSelector.getDisplayModel());
    		selector.setFullModel(base.ontModelSelector.getFullModel());
    		selector.setTBoxModel(base.ontModelSelector.getTBoxModel());
    		selector.setUserAccountsModel(base.ontModelSelector.getUserAccountsModel());
    		this.ontModelSelector = selector;
    	} else {
    		//Not sure what this is but will set to equivalence here
    		this.ontModelSelector =base.ontModelSelector;
    	}
    	
        this.defaultNamespace = base.defaultNamespace;
        this.nonuserNamespaces = base.nonuserNamespaces;
        this.preferredLanguages = base.preferredLanguages;
        this.userURI = base.userURI;
        this.flag2ValueMap = new HashMap<String,OntClass>();
        this.flag2ValueMap.putAll(base.flag2ValueMap);
        this.flag2ClassLabelMap = new HashMap<Resource, String>();
        this.flag2ClassLabelMap.putAll(base.flag2ClassLabelMap);
        this.dwf = base.dwf;
    }
    
    /**
     * Method for using special model for webapp dao factory, such as display model.  
     * The goal here is to modify this WebappDaoFactory so that it is using the
     * specialModel, specialTboxModel and the specialDisplayModel for individual 
     * editing. 
     * 
     * DAOs related to the application configuration, user accounts, and namespaces
     * should remain unchanged.
     */    
    public void setSpecialDataModel(OntModel specialModel, OntModel specialTboxModel, OntModel specialDisplayModel) {
        if( specialModel == null )
            throw new IllegalStateException( "specialModel must not be null");
        
    	//Can we get the "original" models here from somewhere?
    	OntModelSelector originalSelector = this.getOntModelSelector();
    	
    	//Set up model selector for this special WDF
    	//The selector is used by the object property DAO, therefore should be set up even though we 
    	//use the new webapp dao factory object to generate portions to overwrite the regular webapp dao factory
    	
    	//The WDF expects the full model in the OntModelSelect that has 
    	//both the ABox and TBox.  This is used to run SPARQL queries against.    	
    	OntModel unionModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        unionModel.addSubModel(specialModel);
        
    	OntModelSelectorImpl specialSelector = new OntModelSelectorImpl();
    	specialSelector.setFullModel(unionModel);
    	//Keeping original application metadata model and adding special model
    	//adding both allows us to prevent errors in ApplicationDao which may depend on 
    	//a specific individual from the regular application metadata model to pick theme
    	//Adding the new model would take care of special situations where the switch model may
    	//contain important information
    	OntModel newApplicationModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
    	newApplicationModel.add(specialModel);
    	newApplicationModel.add(originalSelector.getApplicationMetadataModel());
    	specialSelector.setApplicationMetadataModel(newApplicationModel);    	
    	
    	if(specialDisplayModel != null) {
    		specialSelector.setDisplayModel(specialDisplayModel);
    	    unionModel.addSubModel(specialDisplayModel);
    	} else {
    		OntModel selectorDisplayModel = originalSelector.getDisplayModel();
    		if(selectorDisplayModel != null) {
    			specialSelector.setDisplayModel(originalSelector.getDisplayModel());
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
    	//although we're only use part of the new wadf and copy over below, the object property dao
    	//utilizes methods that will employ the display model returned from the simple ontmodel selector
    	//so if the object property dao is to be copied over we need to ensure we have the correct display model
    	//and tbox model
    	WebappDaoFactoryJena specialWadfj = new WebappDaoFactoryJena(specialSelector);
    	entityWebappDao = specialWadfj.getIndividualDao();
    	vClassGroupDao = specialWadfj.getVClassGroupDao();
    	//To allow for testing, add a property group, this will allow
    	//the unassigned group method section to be executed and main Image to be assigned to that group
    	//otherwise the dummy group does not allow for the unassigned group to be executed
    	propertyGroupDao = specialWadfj.getPropertyGroupDao();
    	objectPropertyDao = specialWadfj.getObjectPropertyDao();
    	objectPropertyStatementDao = specialWadfj.getObjectPropertyStatementDao();
    	dataPropertyDao = specialWadfj.getDataPropertyDao();
    	dataPropertyStatementDao = specialWadfj.getDataPropertyStatementDao();
    	//Why can't we set the selector to be the same?
    	ontModelSelector = specialSelector;
    	
    }
   
}
