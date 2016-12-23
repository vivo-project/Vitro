/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao;

import java.util.LinkedHashMap;
import java.util.List;

import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;

public interface VClassGroupDao {

    public abstract VClassGroup getGroupByURI(String uri);

    /**
     * Gets all of the ClassGroups as a map ordered by displayRank.
     * VClassGroup.getPublicName() -&gt; VClassGroup
     */
    public abstract LinkedHashMap<String,VClassGroup> getClassGroupMap();

    /**
     * Return a list of VClassGroups with their associated VClasses
     * @return List
     */
    public abstract List<VClassGroup> getPublicGroupsWithVClasses();

    /**
     * Return a list of VClassGroups with their associated VClasses
     * @param displayOrder  Display order
     * @return List
     */
    public abstract List<VClassGroup> getPublicGroupsWithVClasses(boolean displayOrder);

    /**
     * Return a list of VClassGroups with their associated VClasses
     * @param displayOrder Display order
     * @param includeUninstantiatedClasses Include all classes
     * @return List
     */
    public abstract List<VClassGroup> getPublicGroupsWithVClasses(boolean displayOrder, boolean includeUninstantiatedClasses);

    /**
     * Return a list of VClassGroups with their associated VClasses
     * @param displayOrder Display order
     * @param includeUninstantiatedClasses Include all classes
     * @param getIndividualCount Retrieve individual count
     * @return List
     */
    public abstract List<VClassGroup> getPublicGroupsWithVClasses(boolean displayOrder, boolean includeUninstantiatedClasses, boolean getIndividualCount);


    public abstract void sortGroupList(List<VClassGroup> groupList);

    public abstract int removeUnpopulatedGroups(List<VClassGroup> groups);

    int insertNewVClassGroup(VClassGroup vcg);

    void updateVClassGroup(VClassGroup vcg);

    void deleteVClassGroup(VClassGroup vcg);

    VClassGroup getGroupByName(String vcgName);
}