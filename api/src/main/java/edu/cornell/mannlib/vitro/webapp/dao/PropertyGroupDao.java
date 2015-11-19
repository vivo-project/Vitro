/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao;

import java.util.List;

import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyGroup;

public interface PropertyGroupDao {

	public abstract PropertyGroup getGroupByURI(String uri);
	
	public abstract List<PropertyGroup> getPublicGroups(boolean withProperties);
	
	public abstract int removeUnpopulatedGroups(List<PropertyGroup> groups);
	
	public PropertyGroup createDummyPropertyGroup(String name, int rank);
	
	public String insertNewPropertyGroup(PropertyGroup group);
	
	public void updatePropertyGroup(PropertyGroup group);
	
	public void deletePropertyGroup(PropertyGroup group);
	
}
