package stubs.edu.cornell.mannlib.vitro.webapp.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.beans.PropertyGroup;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyGroupDao;

public class PropertyGroupDaoStub implements PropertyGroupDao {
	// ----------------------------------------------------------------------
	// Stub infrastructure
	// ----------------------------------------------------------------------

	private final Map<String, PropertyGroup> map = new HashMap<>();

	public void addPropertyGroup(PropertyGroup group) {
		map.put(group.getURI(), group);
	}

	// ----------------------------------------------------------------------
	// Stub methods
	// ----------------------------------------------------------------------

	@Override
	public PropertyGroup getGroupByURI(String uri) {
		return (uri == null) ? null : copyGroup(map.get(uri), false);
	}

	@Override
	public List<PropertyGroup> getPublicGroups(boolean withProperties) {
		List<PropertyGroup> list = new ArrayList<>();
		for (PropertyGroup group: map.values()) {
			list.add(copyGroup(group, withProperties));
		}
		return list;
	}

	private PropertyGroup copyGroup(PropertyGroup source, boolean withProperties) {
		if (source == null) {
			return null;
		}

		PropertyGroup target = new PropertyGroup();

		target.setURI(source.getURI());
		target.setPickListName(source.getPickListName());

		target.setDisplayRank(source.getDisplayRank());
		target.setName(source.getName());
		target.setStatementCount(source.getStatementCount());
		target.setPublicDescription(source.getPublicDescription());

		if (withProperties) {
			target.setPropertyList(source.getPropertyList());
		}

		return target;
	}

	// ----------------------------------------------------------------------
	// Un-implemented methods
	// ----------------------------------------------------------------------

	@Override
	public int removeUnpopulatedGroups(List<PropertyGroup> groups) {
		throw new RuntimeException(
				"PropertyGroupDaoStub.removeUnpopulatedGroups() not implemented.");
	}

	@Override
	public PropertyGroup createDummyPropertyGroup(String name, int rank) {
		throw new RuntimeException(
				"PropertyGroupDaoStub.createDummyPropertyGroup() not implemented.");
	}

	@Override
	public String insertNewPropertyGroup(PropertyGroup group) {
		throw new RuntimeException(
				"PropertyGroupDaoStub.insertNewPropertyGroup() not implemented.");
	}

	@Override
	public void updatePropertyGroup(PropertyGroup group) {
		throw new RuntimeException(
				"PropertyGroupDaoStub.updatePropertyGroup() not implemented.");
	}

	@Override
	public void deletePropertyGroup(PropertyGroup group) {
		throw new RuntimeException(
				"PropertyGroupDaoStub.deletePropertyGroup() not implemented.");
	}

}
