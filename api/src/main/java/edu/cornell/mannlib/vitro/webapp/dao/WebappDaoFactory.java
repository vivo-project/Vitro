/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao;

import java.util.List;
import java.util.Set;

public interface WebappDaoFactory {

    /**
     * Free any resources associated with this WebappDaoFactory  
     */
    public void close();

	/**
	 * Checks a URI String for two things: well-formedness and uniqueness in the
	 * model.  Ill-formed strings or those matching URIs already in use will 
	 * cause an error message to be returned.
	 * @return error message String if invalid; otherwise null
	 */
	public String checkURI(String uriStr);
	
	/**
	 * Checks a URI String to see whether it is suitable for use on a new editable entity.
	 * It must be well-formed, and it must not be declared in the model as a Thing, Class, 
	 * ObjectProperty or DataProperty. Ill-formed strings or those so declared will 
	 * cause an error message to be returned.
	 * @return error message String if invalid; otherwise null
	 */
	public String checkURIForEditableEntity(String uriStr);
	
	/**
	 * Check if a given URI string exists in the system:
	 * checks for the following conditions: URI found as subject in a statement or an object or as a property
	 * @param uriStr URI String
	 */
	public boolean hasExistingURI(String uriStr);
	
    public String getDefaultNamespace();
    
    public Set<String> getNonuserNamespaces();
    
    public List<String> getPreferredLanguages();
    
    /**
     * BJL23 2008-05-20: Putting this here for lack of a more logical place.  
     * We need to build better support for the RDFS vocabulary into our API.
     * Returns a list of the simple lexical form strings of the rdfs:comment 
     * values for a resource; empty list if none found.
     */
    public List<String> getCommentsForResource(String resourceURI);

    /**
     * Copy this DAO factory to a new object associated with the specified user 
     * URI, or return the same factory if a user-aware version cannot be used.
     * @param userURI User URI
     */
    public WebappDaoFactory getUserAwareDaoFactory(String userURI);

    /**
     * Return URI of user associated with this WebappDaoFactory, 
     * or null if not applicable.
     */
    public String getUserURI();

    /* =============== DAOs for ontology (TBox) manipulation =============== */

    /**
     * returns a Data Access Object for working with DataProperties
     */
    public DataPropertyDao getDataPropertyDao();

    /**
     * returns a Data Access Object for working with Datatypes
     */
    public DatatypeDao getDatatypeDao();

    /**
     * returns a Data Access Object for working with ObjectProperties
     */
    public ObjectPropertyDao getObjectPropertyDao();

    /**
     * returns a Data Access Object for working with Ontologies
     */
    public OntologyDao getOntologyDao();

    /**
     * returns a Data Access Object for working with ontology class objects 
     */
    public VClassDao getVClassDao();
    
    /**
     * returns a Data Access Object for working with "faux" ObjectProperties.
     */
    public FauxPropertyDao getFauxPropertyDao();


    /* ==================== DAOs for ABox manipulation ===================== */

    /**
     * returns a Data Access Object for working with DatatypePropertyStatements 
     */
    public DataPropertyStatementDao getDataPropertyStatementDao();

    /**
     * returns a Data Access Object for working with Individuals 
     */
    public IndividualDao getIndividualDao();

    /**
     * returns a Data Access Object for working with ObjectPropertyStatements 
     */
    public ObjectPropertyStatementDao getObjectPropertyStatementDao();


    public DisplayModelDao getDisplayModelDao();
    
    /* ====================== DAOs for other objects ======================= */

    public ApplicationDao getApplicationDao();

    public UserAccountsDao getUserAccountsDao();

    public VClassGroupDao getVClassGroupDao();

    public PropertyGroupDao getPropertyGroupDao();
    
    public PropertyInstanceDao getPropertyInstanceDao();

    public PageDao getPageDao();    
    
    public MenuDao getMenuDao();    
    
}
