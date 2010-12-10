/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao;

import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;

import java.util.LinkedHashMap;
import java.util.List;

public interface VClassGroupDao {

    public abstract VClassGroup getGroupByURI(String uri);

    /**
     * Gets all of the ClassGroups as a map ordered by displayRank.
     * VClassGroup.getPublicName() -> VClassGroup
     *
     * @return
     */
    public abstract LinkedHashMap<String,VClassGroup> getClassGroupMap();

    /**
     * Return a list of VClassGroups with their associated VClasses
     * @return List
     */
    public abstract List<VClassGroup> getPublicGroupsWithVClasses();

    /**
     * Return a list of VClassGroups with their associated VClasses
     * @param displayOrder
     * @return List
     */
    public abstract List<VClassGroup> getPublicGroupsWithVClasses(boolean displayOrder);

    /**
     * Return a list of VClassGroups with their associated VClasses
     * @param displayOrder
     * @param includeUninstantiatedClasses
     * @return List
     */
    public abstract List<VClassGroup> getPublicGroupsWithVClasses(boolean displayOrder, boolean includeUninstantiatedClasses);

    /**
     * Return a list of VClassGroups with their associated VClasses
     * @param displayOrder
     * @param includeUninstantiatedClasses
     * @param getIndividualCount
     * @return List
     */
    public abstract List<VClassGroup> getPublicGroupsWithVClasses(boolean displayOrder, boolean includeUninstantiatedClasses, boolean getIndividualCount);


    public abstract void sortGroupList(List<VClassGroup> groupList);

    public abstract int removeUnpopulatedGroups(List<VClassGroup> groups);

    public void removeClassesHiddenFromSearch(List<VClassGroup> groups);    
    
    int insertNewVClassGroup(VClassGroup vcg);

    void updateVClassGroup(VClassGroup vcg);

    void deleteVClassGroup(VClassGroup vcg);

    VClassGroup getGroupByName(String vcgName);
}