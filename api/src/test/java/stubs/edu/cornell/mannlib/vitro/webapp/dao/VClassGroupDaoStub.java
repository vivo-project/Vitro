package stubs.edu.cornell.mannlib.vitro.webapp.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.jena.ext.com.google.common.base.Objects;

import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.dao.VClassGroupDao;

public class VClassGroupDaoStub implements VClassGroupDao {
	// ----------------------------------------------------------------------
	// Stub infrastructure
	// ----------------------------------------------------------------------

	private List<VClassGroup> groups = new ArrayList<>();

	public void setGroups(VClassGroup... groups) {
		this.groups = new ArrayList<>(Arrays.asList(groups));
	}

	// ----------------------------------------------------------------------
	// Stub methods
	// ----------------------------------------------------------------------

	@Override
	public VClassGroup getGroupByURI(String uri) {
		for (VClassGroup group: groups) {
			if (Objects.equal(group.getURI(), uri)) {
				return group;
			}
		}
		return null;
	}

	@Override
	public List<VClassGroup> getPublicGroupsWithVClasses() {
		List<VClassGroup> list = new ArrayList<>();
		for (VClassGroup group: groups) {
			if (group != null) {
				list.add(group);
			}
		}
		return list;
	}

	// ----------------------------------------------------------------------
	// Un-implemented methods
	// ----------------------------------------------------------------------

	@Override
	public LinkedHashMap<String, VClassGroup> getClassGroupMap() {
		throw new RuntimeException(
				"VClassGroupDaoStub.getClassGroupMap() not implemented.");
	}

	@Override
	public List<VClassGroup> getPublicGroupsWithVClasses(boolean displayOrder) {
		throw new RuntimeException(
				"VClassGroupDaoStub.getPublicGroupsWithVClasses() not implemented.");
	}

	@Override
	public List<VClassGroup> getPublicGroupsWithVClasses(boolean displayOrder,
			boolean includeUninstantiatedClasses) {
		throw new RuntimeException(
				"VClassGroupDaoStub.getPublicGroupsWithVClasses() not implemented.");
	}

	@Override
	public List<VClassGroup> getPublicGroupsWithVClasses(boolean displayOrder,
			boolean includeUninstantiatedClasses, boolean getIndividualCount) {
		throw new RuntimeException(
				"VClassGroupDaoStub.getPublicGroupsWithVClasses() not implemented.");
	}

	@Override
	public void sortGroupList(List<VClassGroup> groupList) {
		throw new RuntimeException(
				"VClassGroupDaoStub.sortGroupList() not implemented.");
	}

	@Override
	public int removeUnpopulatedGroups(List<VClassGroup> groups) {
		throw new RuntimeException(
				"VClassGroupDaoStub.removeUnpopulatedGroups() not implemented.");
	}

	@Override
	public int insertNewVClassGroup(VClassGroup vcg) {
		throw new RuntimeException(
				"VClassGroupDaoStub.insertNewVClassGroup() not implemented.");
	}

	@Override
	public void updateVClassGroup(VClassGroup vcg) {
		throw new RuntimeException(
				"VClassGroupDaoStub.updateVClassGroup() not implemented.");
	}

	@Override
	public void deleteVClassGroup(VClassGroup vcg) {
		throw new RuntimeException(
				"VClassGroupDaoStub.deleteVClassGroup() not implemented.");
	}

	@Override
	public VClassGroup getGroupByName(String vcgName) {
		throw new RuntimeException(
				"VClassGroupDaoStub.getGroupByName() not implemented.");
	}

}
