/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.edit.EditLiteral;
import edu.cornell.mannlib.vitro.webapp.search.beans.ObjectSourceIface;

public interface IndividualDao extends ObjectSourceIface {

	/**
	 * Returns a collection of DataPropertyStatements involving all the external ID literals for a given Individual.
	 */
    public abstract Collection<DataPropertyStatement> getExternalIds(String individualURI);

    public abstract Collection<DataPropertyStatement> getExternalIds(String individualURI, String dataPropertyURI);

    /**
     * Adds the specified Individual to the specified VClass (i.e. adds rdf:type).
     * @param individualURI
     * @param vclassURI
     */
    public abstract void addVClass(String individualURI, String vclassURI);
    
    /**
     * Removes the specified Individual from the specificed VClass (i.e. retracts rdf:type)
     * @param individualURI
     * @param vclassURI
     */
    public abstract void removeVClass(String individualURI, String vclassURI);
    
    /**
     * Returns a list of all the Individuals in the specified VClass.
     * @param vclass
     * @return
     */
    public abstract List <Individual> getIndividualsByVClass(VClass vclass);

    /**
     * Returns a list of Individuals in a given VClass.
     */
    public abstract List <Individual> getIndividualsByVClassURI(String vclassURI);
    
    /**
     * Returns a list of Individuals in a given VClass.
     */
    public abstract List <Individual> getIndividualsByVClassURI(String vclassURI, int offset,
            int quantity);

    /**
     * @returns new individual URI  if success.
     */
    public abstract String insertNewIndividual(Individual individual) throws InsertException;

    /**
     * updates a single individual in the knowledge base.
     * @return 0 on failed
     */
    public abstract int updateIndividual(Individual individual);

    /**
     * deletes a single individual from the knowledge base.
     * @param id
     * @return 0 on failed
     */
    public abstract int deleteIndividual(String individualURI);

    public abstract int deleteIndividual(Individual individual);

    public abstract void markModified(Individual individual);

    /**
     * Get a row from the entities table and make an Entity.
     * PropertiesList will not be filled out.
     * VClass will be filled out.
     * @param entityId
     * @return an Entity object or null if not found.
     */
    public abstract Individual getIndividualByURI(String individualURI);

    /**
     * Returns an Iterator over all Individuals in the model that are user-viewable.
     */
    public abstract Iterator<String> getAllOfThisTypeIterator();

    /**
     * Returns an Iterator over all Individuals in the model that are user-viewable and have been updated since the specified time.
     */
    public abstract Iterator<String> getUpdatedSinceIterator(long updatedSince);

    public boolean isIndividualOfClass(String vclassURI, String indURI);
    
    /**
     * Returns a list of individuals with the given value for the given dataProperty.  If
     * there are no Indiviuals that fit the criteria then an empty list is returned.
     */
    public List<Individual> getIndividualsByDataProperty(String dataPropertyUri, String value);

    public List<Individual> getIndividualsByDataProperty(String dataPropertyUri, String value, String datatypeUri, String lang);
    
	void fillVClassForIndividual(Individual individual);

	/**
	 * Standard way to get a new URI that is not yet used.
	 * @param individual, may be null
	 * @return new URI that is not found in the subject, predicate or object position of any statement.
	 * @throws InsertException Could not create a URI
	 */
	String getUnusedURI(Individual individual) throws InsertException;
	
	EditLiteral getLabelEditLiteral(String individualUri);
	
}