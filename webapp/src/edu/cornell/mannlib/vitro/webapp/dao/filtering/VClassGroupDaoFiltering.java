/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.filtering;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.VitroFilters;

public class VClassGroupDaoFiltering extends BaseFiltering implements VClassGroupDao {

    private final VClassGroupDao innerDao;
    private final WebappDaoFactoryFiltering filteredDaos;
    private final VitroFilters filters;

    public VClassGroupDaoFiltering(VClassGroupDao classGroupDao,
            WebappDaoFactoryFiltering webappDaoFactoryFiltering,
            VitroFilters filters) {
        
        this.innerDao = classGroupDao;
        this.filteredDaos = webappDaoFactoryFiltering;
        this.filters =  filters;
    }


    public void deleteVClassGroup(VClassGroup vcg) {
        innerDao.deleteVClassGroup(vcg);
    }


    public LinkedHashMap<String, VClassGroup> getClassGroupMap() {
        LinkedHashMap<String, VClassGroup> lhm = innerDao.getClassGroupMap();
        Set<String> keys = lhm.keySet();
        for( Object key : keys){
            VClassGroup vcg = (VClassGroup)lhm.get(key);
            if( vcg == null || !filters.getVClassGroupFilter().fn(vcg) ){
                lhm.remove(key);
            }
        }
        return lhm;
    }

    public VClassGroup getGroupByURI(String uri) {
        VClassGroup vg = innerDao.getGroupByURI(uri);
        if( vg != null && filters.getVClassGroupFilter().fn(vg))
            return vg;
        else
            return null;
    }

    public List<VClassGroup> getPublicGroupsWithVClasses() {
        return this.getPublicGroupsWithVClasses(false);
    }

    public List<VClassGroup> getPublicGroupsWithVClasses(boolean displayOrder) {
        return this.getPublicGroupsWithVClasses(displayOrder,true);
    }
    public List<VClassGroup> getPublicGroupsWithVClasses(boolean displayOrder, boolean includeUninstantiatedClasses) {
        return this.getPublicGroupsWithVClasses(displayOrder,includeUninstantiatedClasses,false);
    }
    /** filter both vclassgroups and their vclasses */
    public List<VClassGroup> getPublicGroupsWithVClasses(boolean displayOrder, boolean includeUninstantiatedClasses,
            boolean getIndividualCount) {

        LinkedHashMap<String, VClassGroup> groupMap = this.getClassGroupMap();
        List<VClassGroup> groups = new ArrayList<VClassGroup>(groupMap.values());

        VClassDao vclassDao = filteredDaos.getVClassDao();
        for( VClassGroup vg : groups){
            vclassDao.addVClassesToGroup(vg, includeUninstantiatedClasses, getIndividualCount);
        }
        if( !includeUninstantiatedClasses ){
            ListIterator<VClassGroup> it = groups.listIterator();
            while(it.hasNext()){
                if( it.next().size() == 0)
                    it.remove();
            }
        }
        return groups;
    }


    public int insertNewVClassGroup(VClassGroup vcg) {
        return innerDao.insertNewVClassGroup(vcg);
    }


    public int removeUnpopulatedGroups(List<VClassGroup> groups) {
        return innerDao.removeUnpopulatedGroups(groups);
    }


    public void sortGroupList(List<VClassGroup> groupList) {
        innerDao.sortGroupList(groupList);
    }


    public void updateVClassGroup(VClassGroup vcg) {
        innerDao.updateVClassGroup(vcg);
    }


    @Override
    public void removeClassesHiddenFromSearch(List<VClassGroup> groups) {
        innerDao.removeClassesHiddenFromSearch(groups);        
    }


    @Override
    public VClassGroup getGroupByName(String vcgName) {
        return innerDao.getGroupByName(vcgName);
    }


}