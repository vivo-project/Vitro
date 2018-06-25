/* $This file is distributed under the terms of the license in LICENSE$ */

package stubs.edu.cornell.mannlib.vitro.webapp.dao;

import static java.util.stream.Collectors.toList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.vocabulary.RDF;

import com.google.common.base.Objects;

import edu.cornell.mannlib.vitro.webapp.beans.Classes2Classes;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.dao.InsertException;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;

public class VClassDaoStub implements VClassDao {
	// ----------------------------------------------------------------------
	// Stub infrastructure
	// ----------------------------------------------------------------------

	private static class VClassHolder {
		final VClass vclass;
		final String parentUri;

		VClassHolder(VClass vclass, String parentUri) {
			this.vclass = vclass;
			this.parentUri = parentUri;
		}

		public VClass getVclass() {
			return vclass;
		}

		public String getParentUri() {
			return parentUri;
		}

		boolean isRoot() {
			return parentUri == null;
		}

		boolean inOntology(String ontologyUri) {
			return Objects.equal(ontologyUri, vclass.getNamespace());
		}
	}

	private final Map<String, VClassHolder> vclassMap = new HashMap<>();

	public void setVClass(VClass vclass) {
		setVClass(vclass, null);
	}

	public void setVClass(VClass vclass, String parentUri) {
		vclassMap.put(vclass.getURI(), new VClassHolder(vclass, parentUri));
	}

	// ----------------------------------------------------------------------
	// Stub methods
	// ----------------------------------------------------------------------

	@Override
	public List<VClass> getAllVclasses() {
		return vclassMap.values().stream().map(VClassHolder::getVclass)
				.collect(toList());
	}

	// Only direct (one-hop) sub-classes.
	@Override
	public List<String> getSubClassURIs(String classURI) {
		return vclassMap.values().stream()
				.filter(vch -> vch.parentUri == classURI)
				.map(vch -> vch.getVclass().getURI()).collect(toList());
	}

	@Override
	public VClass getVClassByURI(String URI) {
		VClassHolder vch = vclassMap.get(URI);
		return vch == null ? null : vch.getVclass();
	}

	@Override
	public List<VClass> getRootClasses() {
		return vclassMap.values().stream().filter(VClassHolder::isRoot)
				.map(VClassHolder::getVclass).collect(toList());
	}

	@Override
	public List<VClass> getOntologyRootClasses(String ontologyURI) {
		return getRootClasses().stream()
				.filter(vc -> Objects.equal(ontologyURI, vc.getNamespace()))
				.collect(toList());
	}

	@Override
	public VClass getTopConcept() {
		VClass top = new VClass();
		top.setURI(RDF.getURI() + "Resource");
		top.setName(top.getLocalName());
		return top;
	}

	// ----------------------------------------------------------------------
	// Un-implemented methods
	// ----------------------------------------------------------------------

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
	public void removeDisjointWithClass(String classURI,
			String disjointClassURI) {
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
	public void removeEquivalentClass(String classURI,
			String equivalentClassURI) {
		throw new RuntimeException(
				"VClassDaoStub.removeEquivalentClass() not implemented.");
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
	public VClass getBottomConcept() {
		throw new RuntimeException(
				"VClassDaoStub.getBottomConcept() not implemented.");
	}

}
