/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface WebappDaoFactory {

	/**
	 * Retrieves a map containing arbitrary key-value pairs describing this WebappDaoFactory
	 */
	public Map<String,String> getProperties();

	/**
	 * Checks a URI String for two things: well-formedness and uniqueness in the model.
	 * Ill-formed strings or those matching URIs already in use will cause an error message to be returned.
	 * @return error message String if invalid; otherwise null
	 */
	public String checkURI(String uriStr);
	
	/**
	 * Checks a URI String for two things: well-formedness and, optionally, uniqueness in the model.
	 * Ill-formed strings or those matching URIs already in use will cause an error message to be returned.
	 * @return error message String if invalid; otherwise null
	 */
	public String checkURI(String uriStr, boolean checkUniqueness);
	
	public int getLanguageProfile();
	
    public String getDefaultNamespace();
    
    public Set<String> getNonuserNamespaces();
    
    public String[] getPreferredLanguages();
    
    /**
     * BJL23 2008-05-20: Putting this here for lack of a more logical place.  We need to build better support for the RDFS vocabulary into our API.
     * Returns a list of the simple lexical form strings of the rdfs:comment values for a resource; empty list if none found.
     */
    public List<String> getCommentsForResource(String resourceURI);

    /**
     * Copy this DAO factory to a new object associated with the specified user URI, or return the same factory if a user-aware version cannot be used.
     * @param userURI
     * @return
     */
    public WebappDaoFactory getUserAwareDaoFactory(String userURI);

    /**
     * Return URI of user associated with this WebappDaoFactory, or null if not applicable.
     * @return
     */
    public String getUserURI();

    /* ======================== DAOs for ontology (TBox) manipulation ======================== */

    /**
     * returns a Data Access Object for working with class subsumption axioms in the model
     */
    public Classes2ClassesDao getClasses2ClassesDao();

    /**
     * returns a Data Access Object for working with DataProperties in the model
     */
    public DataPropertyDao getDataPropertyDao();

    /**
     * returns a Data Access Object for working with Datatypes
     */
    public DatatypeDao getDatatypeDao();

    /**
     * returns a Data Access Object for working with ObjectProperties in the model
     */
    public ObjectPropertyDao getObjectPropertyDao();

    /**
     * returns a Data Access Object for working with Ontologies in the model
     */
    public OntologyDao getOntologyDao();

    /**
     * returns a Data Access Object for working with ontology class objects in the model
     */
    public VClassDao getVClassDao();


    /* ======================== DAOs for ABox manipulation ======================== */

    /**
     * returns a Data Access Object for working with DatatypePropertyStatements in the model
     */
    public DataPropertyStatementDao getDataPropertyStatementDao();

    /**
     * returns a Data Access Object for working with Individuals in the model
     */
    public IndividualDao getIndividualDao();

    /**
     * returns a Data Access Object for working with ObjectPropertyStatements in the model
     */
    public ObjectPropertyStatementDao getObjectPropertyStatementDao();


    /* ======================== DAOs for other objects ======================== */

    public ApplicationDao getApplicationDao();
    public PortalDao getPortalDao();

    public TabDao getTabDao();
    public TabIndividualRelationDao getTabs2EntsDao();
    public TabVClassRelationDao getTabs2TypesDao();

    public KeywordIndividualRelationDao getKeys2EntsDao();
    public KeywordDao getKeywordDao();

    public LinksDao getLinksDao();
    public LinktypeDao getLinktypeDao();

    public FlagDao getFlagDao();

    public UserDao getUserDao();

    public VClassGroupDao getVClassGroupDao();

    public PropertyGroupDao getPropertyGroupDao();
    
    public NamespaceDao getNamespaceDao();
    
    public PropertyInstanceDao getPropertyInstanceDao();    
    
    public PropertyListDao getPropertyListDao();
}
