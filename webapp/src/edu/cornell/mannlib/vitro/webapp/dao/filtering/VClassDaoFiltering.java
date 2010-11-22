/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.filtering;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.sf.jga.algorithms.Filter;
import net.sf.jga.fn.UnaryFunctor;
import net.sf.jga.fn.property.GetProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.InsertException;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.VitroFilters;

public class VClassDaoFiltering extends BaseFiltering implements VClassDao{
    final VClassDao innerVClassDao;
    final IndividualDao innerIndividualDao;    
    final VitroFilters filters;

    static UnaryFunctor<VClass,String> getURI =
        new GetProperty<VClass, String>(VClass.class, "URI");


    public VClassDaoFiltering(VClassDao classDao, IndividualDao individualDao,
            VitroFilters filters) {
        super();
        this.innerVClassDao = classDao;
        this.innerIndividualDao = individualDao;
        this.filters = filters;        
    }

    public void deleteVClass(String URI) {
        innerVClassDao.deleteVClass(URI);
    }

    public void deleteVClass(VClass cls) {
        innerVClassDao.deleteVClass(cls);
    }

    public List<String> getDisjointWithClassURIs(String classURI) {
    	return innerVClassDao.getDisjointWithClassURIs(classURI);
    }
    
    public void addDisjointWithClass(String classURI, String disjointClassURI) {
    	innerVClassDao.addDisjointWithClass(classURI, disjointClassURI);
    }
    
    public void removeDisjointWithClass(String classURI, String disjointClassURI) {
    	innerVClassDao.removeDisjointWithClass(classURI, disjointClassURI);
    }
    
    public List<String> getEquivalentClassURIs(String classURI) {
    	return innerVClassDao.getEquivalentClassURIs(classURI);
    }
    
    public void addEquivalentClass(String classURI, String equivalentClassURI) {
    	innerVClassDao.addEquivalentClass(classURI, equivalentClassURI);
    }
    
    public void removeEquivalentClass(String classURI, String equivalentClassURI) {
    	innerVClassDao.removeEquivalentClass(classURI, equivalentClassURI);
    }
    
    public void addSuperclass(VClass subclass, VClass superclass) {
    	innerVClassDao.addSuperclass(subclass, superclass);
    }
    
    public void addSuperclass(String classURI, String superclassURI) {
    	innerVClassDao.addSuperclass(classURI, superclassURI);
    }
    
    public void removeSuperclass(VClass vclass, VClass superclass) {
    	innerVClassDao.removeSuperclass(vclass, superclass);
    }
    
    public void removeSuperclass(String classURI, String superclassURI) {
    	innerVClassDao.removeSuperclass(classURI, superclassURI);
    }
    
    public void addSubclass(VClass vclass, VClass subclass) {
    	innerVClassDao.addSubclass(vclass, subclass);
    }
    
    public void addSubclass(String classURI, String subclassURI) {
    	innerVClassDao.addSubclass(classURI, subclassURI);
    }
    
    public void removeSubclass(VClass vclass, VClass subclass) {
    	innerVClassDao.removeSubclass(vclass, subclass);
    }
    
    public void removeSubclass(String classURI, String subclassURI) {
    	innerVClassDao.removeSubclass(classURI, subclassURI);
    }
    
    public List <String>getAllSubClassURIs(String classURI) {
        return innerVClassDao.getAllSubClassURIs(classURI);
    }

    public List <String>getAllSuperClassURIs(String classURI) {
        return innerVClassDao.getAllSuperClassURIs(classURI);
    }


    public List <VClass>getAllVclasses() {
        List<VClass> list = innerVClassDao.getAllVclasses();
        if(list == null )
            return null;
        filter(list,filters.getClassFilter());
        //correctVClassCounts(list);
        return list;
    }

    public List<VClass> getOntologyRootClasses(String ontologyURI) {
        return innerVClassDao.getOntologyRootClasses(ontologyURI);
    }


    public List <VClass>getRootClasses() {
        return innerVClassDao.getRootClasses();
    }


    public List<String> getSubClassURIs(String classURI) {
        return innerVClassDao.getSubClassURIs(classURI);
    }

    public List<String> getSuperClassURIs(String classURI, boolean direct) {
        return  innerVClassDao.getSuperClassURIs(classURI, direct);
    }

    public VClass getVClassByURI(String URI) {
        return innerVClassDao.getVClassByURI(URI);
    }


    public List<VClass> getVClassesForProperty(String propertyURI, boolean domainSide) {
        return innerVClassDao.getVClassesForProperty(propertyURI, domainSide);
        //return correctVClassCounts(filter(list,filters.getClassFilter()));
    }
    
    public List<VClass> getVClassesForProperty(String vclassURI, String propertyURI) {
    	return innerVClassDao.getVClassesForProperty(vclassURI, propertyURI);
    	//return correctVClassCounts(filter(list,filters.getClassFilter()));
    }

    public void insertNewVClass(VClass cls) throws InsertException {
        innerVClassDao.insertNewVClass(cls);
    }

    public void updateVClass(VClass cls) {
        innerVClassDao.updateVClass(cls);
    }

///////////////////////////////////


    public void addVClassesToGroup(VClassGroup group) {
        this.addVClassesToGroup(group,true);
    }

    public void addVClassesToGroup(VClassGroup group, boolean includeUninstantiatedClasses) {
        this.addVClassesToGroup(group, includeUninstantiatedClasses, false);
    }

    public void addVClassesToGroup(VClassGroup group,
            boolean includeUninstantiatedClasses, boolean setIndividualCount) {
        innerVClassDao.addVClassesToGroup(group, includeUninstantiatedClasses, false);
        
        List<VClass> classes = group.getVitroClassList();
        List<VClass> out = new LinkedList<VClass>();        
        Filter.filter(classes,filters.getClassFilter(), out);
        group.clear();
        group.addAll(out);
        
        if (setIndividualCount) {
            for(VClass vc : group){
                if( vc instanceof VClass ){
                    correctVClassCount((VClass)vc);
                }
            }
        }
    }

    public void addVClassesToGroups(List groups) {
        if ((groups != null) && (groups.size()>0)) {
            Iterator groupIt = groups.iterator();
            while (groupIt.hasNext()) {
                VClassGroup g = (VClassGroup) groupIt.next();
                this.addVClassesToGroup(g);
            }
        } else {
            VClassGroup vcg = new VClassGroup();
            vcg.setURI("null://null#0");
            vcg.setNamespace("null://null#");
            vcg.setLocalName("0");
            vcg.setPublicName("Browse Categories");
            vcg.addAll( this.getAllVclasses() );
            java.util.Collections.sort(vcg.getVitroClassList(),new Comparator(){
                public int compare(Object o1, Object o2){
                    return ((VClass)o1).getName().compareTo(((VClass)o2).getName());
                }
            });
            groups.add(vcg);
        }
    }


    private void correctVClassCount(VClass vclass){
        List<Individual> ents = innerIndividualDao.getIndividualsByVClass(vclass);
        long count = 0;
        if( ents == null) return;
        List out = new ArrayList(ents.size());
        Filter.filter(ents,filters.getIndividualFilter(),out);
        if( out != null )
            vclass.setEntityCount(out.size());
        System.out.println(vclass.getURI() + " count: " + vclass.getEntityCount());
        return;
    }

    private List<VClass> correctVClassCounts(List<VClass> vclasses){
        for( VClass vclass: vclasses){
            correctVClassCount(vclass);
        }
        return vclasses;
    }
    
    public VClass getTopConcept() {
    	return innerVClassDao.getTopConcept();
    }
    
    public VClass getBottomConcept() {
    	return innerVClassDao.getBottomConcept();
    }
    
    public boolean isSubClassOf(VClass vc1, VClass vc2) {
		return innerVClassDao.isSubClassOf(vc1, vc2);
	}

	public boolean isSubClassOf(String vclassURI1, String vclassURI2) {
		return innerVClassDao.isSubClassOf(vclassURI1, vclassURI2);
	}
    
}