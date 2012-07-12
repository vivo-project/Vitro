/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package stubs.edu.cornell.mannlib.vitro.webapp.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.beans.Classes2Classes;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.dao.InsertException;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;

public class VClassDaoStub implements VClassDao {
// ----------------------------------------------------------------------
// Stub infrastructure
// ----------------------------------------------------------------------

	private final Map<String, VClass> vclassesByUri = new HashMap<String, VClass>();
	
	public void setVClass(String uri, VClass vclass) {
		vclassesByUri.put(uri, vclass);
	}
	
// ----------------------------------------------------------------------
// Stub methods
// ----------------------------------------------------------------------

	@Override
	public VClass getVClassByURI(String URI) {
		return vclassesByUri.get(URI);
	}

// ----------------------------------------------------------------------
// Un-implemented methods
// ----------------------------------------------------------------------


	@Override
	public List<VClass> getRootClasses() {
		throw new RuntimeException(
				"VClassDaoStub.getRootClasses() not implemented.");
	}

	@Override
	public List<VClass> getOntologyRootClasses(String ontologyURI) {
		throw new RuntimeException(
				"VClassDaoStub.getOntologyRootClasses() not implemented.");
	}

	@Override
	public List<VClass> getAllVclasses() {
		throw new RuntimeException(
				"VClassDaoStub.getAllVclasses() not implemented.");
	}

	@Override
	public List<String> getDisjointWithClassURIs(String vclassURI) {
		throw new RuntimeException(
				"VClassDaoStub.getDisjointWithClassURIs() not implemented.");
	}

	@Override
	public void addSuperclass(VClass subclass, VClass superclass) {
		throw new RuntimeException(
				"VClassDaoStub.addSuperclass() not implemented.");
	}

	@Override
	public void addSuperclass(String classURI, String superclassURI) {
		throw new RuntimeException(
				"VClassDaoStub.addSuperclass() not implemented.");
	}

	@Override
	public void removeSuperclass(VClass vclass, VClass superclass) {
		throw new RuntimeException(
				"VClassDaoStub.removeSuperclass() not implemented.");
	}

	@Override
	public void removeSuperclass(String classURI, String superclassURI) {
		throw new RuntimeException(
				"VClassDaoStub.removeSuperclass() not implemented.");
	}

	@Override
	public void addSubclass(VClass vclass, VClass subclass) {
		throw new RuntimeException(
				"VClassDaoStub.addSubclass() not implemented.");
	}

	@Override
	public void addSubclass(String classURI, String subclassURI) {
		throw new RuntimeException(
				"VClassDaoStub.addSubclass() not implemented.");
	}

	@Override
	public void removeSubclass(VClass vclass, VClass subclass) {
		throw new RuntimeException(
				"VClassDaoStub.removeSubclass() not implemented.");
	}

	@Override
	public void removeSubclass(String classURI, String subclassURI) {
		throw new RuntimeException(
				"VClassDaoStub.removeSubclass() not implemented.");
	}

	@Override
	public void addDisjointWithClass(String classURI, String disjointCLassURI) {
		throw new RuntimeException(
				"VClassDaoStub.addDisjointWithClass() not implemented.");
	}

	@Override
	public void removeDisjointWithClass(String classURI, String disjointClassURI) {
		throw new RuntimeException(
				"VClassDaoStub.removeDisjointWithClass() not implemented.");
	}

	@Override
	public List<String> getEquivalentClassURIs(String classURI) {
		throw new RuntimeException(
				"VClassDaoStub.getEquivalentClassURIs() not implemented.");
	}

	@Override
	public void addEquivalentClass(String classURI, String equivalentClassURI) {
		throw new RuntimeException(
				"VClassDaoStub.addEquivalentClass() not implemented.");
	}

	@Override
	public void removeEquivalentClass(String classURI, String equivalentClassURI) {
		throw new RuntimeException(
				"VClassDaoStub.removeEquivalentClass() not implemented.");
	}

	@Override
	public List<String> getSubClassURIs(String classURI) {
		throw new RuntimeException(
				"VClassDaoStub.getSubClassURIs() not implemented.");
	}

	@Override
	public List<String> getAllSubClassURIs(String classURI) {
		throw new RuntimeException(
				"VClassDaoStub.getAllSubClassURIs() not implemented.");
	}

	@Override
	public List<String> getSuperClassURIs(String classURI, boolean direct) {
		throw new RuntimeException(
				"VClassDaoStub.getSuperClassURIs() not implemented.");
	}

	@Override
	public List<String> getAllSuperClassURIs(String classURI) {
		throw new RuntimeException(
				"VClassDaoStub.getAllSuperClassURIs() not implemented.");
	}

	@Override
	public void insertNewVClass(VClass cls) throws InsertException {
		throw new RuntimeException(
				"VClassDaoStub.insertNewVClass() not implemented.");
	}

	@Override
	public void updateVClass(VClass cls) {
		throw new RuntimeException(
				"VClassDaoStub.updateVClass() not implemented.");
	}

	@Override
	public void deleteVClass(String URI) {
		throw new RuntimeException(
				"VClassDaoStub.deleteVClass() not implemented.");
	}

	@Override
	public void deleteVClass(VClass cls) {
		throw new RuntimeException(
				"VClassDaoStub.deleteVClass() not implemented.");
	}

	@Override
	public List<VClass> getVClassesForProperty(String propertyURI,
			boolean domainSide) {
		throw new RuntimeException(
				"VClassDaoStub.getVClassesForProperty() not implemented.");
	}

	@Override
	public List<VClass> getVClassesForProperty(String vclassURI,
			String propertyURI) {
		throw new RuntimeException(
				"VClassDaoStub.getVClassesForProperty() not implemented.");
	}

	@Override
	public void addVClassesToGroup(VClassGroup group) {
		throw new RuntimeException(
				"VClassDaoStub.addVClassesToGroup() not implemented.");
	}

	@Override
	public void insertNewClasses2Classes(Classes2Classes c2c) {
		throw new RuntimeException(
				"VClassDaoStub.insertNewClasses2Classes() not implemented.");
	}

	@Override
	public void deleteClasses2Classes(Classes2Classes c2c) {
		throw new RuntimeException(
				"VClassDaoStub.deleteClasses2Classes() not implemented.");
	}

	@Override
	public void addVClassesToGroup(VClassGroup group,
			boolean includeUninstantiatedClasses) {
		throw new RuntimeException(
				"VClassDaoStub.addVClassesToGroup() not implemented.");
	}

	@Override
	public void addVClassesToGroup(VClassGroup group,
			boolean includeUninstantiatedClasses, boolean getIndividualCount) {
		throw new RuntimeException(
				"VClassDaoStub.addVClassesToGroup() not implemented.");
	}

	@Override
	public void addVClassesToGroups(List<VClassGroup> groups) {
		throw new RuntimeException(
				"VClassDaoStub.addVClassesToGroups() not implemented.");
	}

	@Override
	public boolean isSubClassOf(VClass vc1, VClass vc2) {
		throw new RuntimeException(
				"VClassDaoStub.isSubClassOf() not implemented.");
	}

	@Override
	public boolean isSubClassOf(String vclassURI1, String vclassURI2) {
		throw new RuntimeException(
				"VClassDaoStub.isSubClassOf() not implemented.");
	}

	@Override
	public VClass getTopConcept() {
		throw new RuntimeException(
				"VClassDaoStub.getTopConcept() not implemented.");
	}

	@Override
	public VClass getBottomConcept() {
		throw new RuntimeException(
				"VClassDaoStub.getBottomConcept() not implemented.");
	}

}
