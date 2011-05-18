/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao;

import java.util.List;

import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;

public interface VClassDao {

    List<VClass> getRootClasses();

    List<VClass> getOntologyRootClasses(String ontologyURI);

    List<VClass> getAllVclasses();

    List<String> getDisjointWithClassURIs(String vclassURI);
    
    void addSuperclass(VClass subclass, VClass superclass);
    
    void addSuperclass(String classURI, String superclassURI);
    
    void removeSuperclass(VClass vclass, VClass superclass);
    
    void removeSuperclass(String classURI, String superclassURI);
    
    void addSubclass(VClass vclass, VClass subclass);
    
    void addSubclass(String classURI, String subclassURI);
    
    void removeSubclass(VClass vclass, VClass subclass);
    
    void removeSubclass(String classURI, String subclassURI);
    
    void addDisjointWithClass(String classURI, String disjointCLassURI);
    
    void removeDisjointWithClass(String classURI, String disjointClassURI);
    
    List<String> getEquivalentClassURIs(String classURI);
    
    void addEquivalentClass(String classURI, String equivalentClassURI);
    
    void removeEquivalentClass(String classURI, String equivalentClassURI);
    
    List <String> getSubClassURIs(String classURI);

    List <String> getAllSubClassURIs(String classURI);

    List <String> getSuperClassURIs(String classURI, boolean direct);

    List <String> getAllSuperClassURIs(String classURI);

    VClass getVClassByURI(String URI);

    void insertNewVClass(VClass cls ) throws InsertException;

    void updateVClass(VClass cls);

    void deleteVClass(String URI);

    void deleteVClass(VClass cls);

    List <VClass> getVClassesForProperty(String propertyURI, boolean domainSide);
    
    List <VClass> getVClassesForProperty(String vclassURI, String propertyURI);

    void addVClassesToGroup(VClassGroup group);

    @SuppressWarnings("unchecked")
    void addVClassesToGroup(VClassGroup group, boolean includeUninstantiatedClasses);/* (non-Javadoc)
    * @see edu.cornell.mannlib.vitro.webapp.dao.db.VClassDao#addVClassesToGroups(java.util.List)
    */

    void addVClassesToGroup(VClassGroup group, boolean includeUninstantiatedClasses, boolean getIndividualCount); /*
    * @see edu.cornell.mannlib.vitro.webapp.dao.db.VClassDao#addVClassesToGroups(java.util.List)
    */
    
    /*
    void addVClassesToGroup(VClassGroup group, boolean includeUninstantiatedClasses, boolean getIndividualCount, RoleLevel userVisibilityRoleLevel, RoleLevel userUpdateRoleLevel ); /*
    * @see edu.cornell.mannlib.vitro.webapp.dao.db.VClassDao#addVClassesToGroups(java.util.List)
    */
    
    void addVClassesToGroups(List <VClassGroup> groups );
    
    /**
     * @param vc1
     * @param vc2
     * @return true if subClassOf(vc1, vc2)
     */
    boolean isSubClassOf(VClass vc1, VClass vc2);
    
    /**
     * @param vc1
     * @param vc2
     * @return true if subClassOf(vc1, vc2)
     */
    boolean isSubClassOf(String vclassURI1, String vclassURI2);
    
    
    /**
     * Returns the top concept for the current modeling language (e.g. owl:Thing)
     */
    VClass getTopConcept();
    
    /**
     * Returns the bottom concept for the current modeling language (e.g. owl:Nothing)
     */
    VClass getBottomConcept();
}