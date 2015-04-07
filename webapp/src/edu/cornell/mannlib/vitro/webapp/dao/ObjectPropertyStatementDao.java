/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao;

import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;

/**
 * Created by IntelliJ IDEA.
 * User: bdc34
 * Date: Apr 18, 2007
 * Time: 7:08:20 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ObjectPropertyStatementDao {
	
    void deleteObjectPropertyStatement(ObjectPropertyStatement objPropertyStmt);
    
    List<ObjectPropertyStatement> getObjectPropertyStatements(ObjectProperty objectProperty);
    
    List<ObjectPropertyStatement> getObjectPropertyStatements(ObjectProperty objectProperty, int startIndex, int endIndex);

    List<ObjectPropertyStatement> getObjectPropertyStatements(ObjectPropertyStatement objPropertyStmt);
    
    Individual fillExistingObjectPropertyStatements( Individual entity );

    int insertNewObjectPropertyStatement(ObjectPropertyStatement objPropertyStmt );        

//    public List<Map<String, String>> getObjectPropertyStatementsForIndividualByProperty(
//			String subjectUri, 
//			String propertyUri, 
//			String objectKey, 
//			String queryString,
//			Set<String> constructQueryStrings);
    
    public Map<String, String> getMostSpecificTypesInClassgroupsForIndividual(String subjectUri);

	List<Map<String, String>> getObjectPropertyStatementsForIndividualByProperty(
			String subjectUri, String propertyUri, String objectKey, String domainUri, 
			String rangeUri, String queryString, Set<String> constructQueryStrings,
			String sortDirection);

	/**
	 * Inspect the elements of the statement to see whether it qualifies as a
	 * faux property.
	 * 
	 * That is, is there a faux property definition, such that
	 * <ul>
	 * <li>The property URI of this statement is the base URI of the faux
	 * property</li>
	 * <li>One of the VClasses of the subject of this statement is the domain of
	 * the faux property</li>
	 * <li>One of the VClasses of the object of this statement is the range of
	 * the faux property</li>
	 * </ul>
	 * 
	 * If that is so, then set the domain and range (and the domainURI and
	 * rangeURI) of the property of this statement to match the faux property.
	 */
	void resolveAsFauxPropertyStatement(ObjectPropertyStatement stmt);
  
}
