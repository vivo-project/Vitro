/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.filtering.filters;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.sf.jga.fn.UnaryFunctor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.ResourceBean;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

public class HiddenFromDisplayBelowRoleLevelFilter extends VitroFiltersImpl {

    protected final RoleLevel userRole;
    protected final WebappDaoFactory wdf;

    /* looking up data properties was taking %11 of search index build time
     * So this is a cache of DataProperty objects for this filter */
    protected final Map<String,DataProperty> dataPropertyMap;    

    /* Controls if whether to filter out individuals if the classes that
     * the individual belongs to are not visible. This has been
     * a minor performance problem.  */
    protected final static boolean FILTER_ON_INDIVIDUAL_VCLASSES = false;

    private final static Log log = LogFactory.getLog(HiddenFromDisplayBelowRoleLevelFilter.class);

    public HiddenFromDisplayBelowRoleLevelFilter( RoleLevel role , WebappDaoFactory wdf ){
        super();
        if( role == null ) {
            throw new IllegalArgumentException("HiddenFromRoleLevelFilter must have a RoleLevel ");
        } else {
            log.debug("initializing HiddenFromDisplayBelowRoleLevelFilter with role "+role.getShorthand());
        }
        if( wdf == null )
            throw new IllegalArgumentException("HiddenFromRoleLevelFilter must have a DaoFactory");
        this.userRole = role;
        this.wdf = wdf;
        this.dataPropertyMap = new TreeMap<String,DataProperty>();        
        
        setIndividualFilter(new IndividualRoleFilter() );
        setClassFilter(new RoleFilter<VClass>());
        setDataPropertyFilter(new RoleFilter<DataProperty>() );
        setObjectPropertyFilter(new RoleFilter<ObjectProperty>() );
        setDataPropertyStatementFilter(new DataPropertyStatementRoleFilter<DataPropertyStatement>( ) );
        setObjectPropertyStatementFilter(new ObjectPropertyStatementRoleFilter<ObjectPropertyStatement>() );
    }

    private boolean sameLevelOrHigher( RoleLevel other ){
        if( other == null )
            return true; //default to visible
        int comparison = userRole.compareTo(other);
        if (log.isDebugEnabled()) {
            if (comparison == 0) {
                log.debug("user role "+userRole.getShorthand()+" judged equal to current user role "+other.getShorthand());
            } else if (comparison > 0) {
                log.debug("user role "+userRole.getShorthand()+" judged greater than current user role "+other.getShorthand());
            } else if (comparison < 0) {
                log.debug("user role "+userRole.getShorthand()+" judged less than current user role "+other.getShorthand());
            }
        }
        return ( comparison >= 0 );
    }

    private boolean canViewOddItems( ){
        return sameLevelOrHigher( RoleLevel.DB_ADMIN ) ;
    }

    @SuppressWarnings("serial")
    private class  RoleFilter<E extends ResourceBean> extends UnaryFunctor<E,Boolean>{
        @Override
        public Boolean fn(E resource) {            
            try{
                if( resource == null )
                    return canViewOddItems();
                else
                    log.debug("checking hidden status for \"" + resource.getURI() + "\"");
                    return sameLevelOrHigher( resource.getHiddenFromDisplayBelowRoleLevel() );
            }catch(RuntimeException th){
                log.warn("Error checking hidden status for " + resource, th);
                return false;
            }
        }
    }

    @SuppressWarnings("serial")
    private class IndividualRoleFilter extends UnaryFunctor<Individual,Boolean>{
        @Override
        public Boolean fn(Individual ind){
            if( ind == null ) {
            	log.debug("checking hidden status for null Individual");
                return canViewOddItems(); 
            }
            log.debug("checking hidden status for Individual \"" + ind.getName() + "\"");

            try{
                if( ! sameLevelOrHigher( ind.getHiddenFromDisplayBelowRoleLevel() ) )
                    return false;

                if( FILTER_ON_INDIVIDUAL_VCLASSES ){
                    List<VClass> vclasses =  ind.getVClasses(true);

                    if( vclasses == null ){
                        VClass clazz = wdf.getVClassDao().getVClassByURI(ind.getVClassURI());
                        if( clazz == null )
                            return canViewOddItems();
                        else
                            return sameLevelOrHigher(clazz.getHiddenFromDisplayBelowRoleLevel() );
                    }

                    for( VClass vclass : vclasses ){
                        if( ! sameLevelOrHigher(vclass.getHiddenFromDisplayBelowRoleLevel() ) )
                            return false;
                    }
                }
                return true;
            }catch(RuntimeException ex){
                log.warn("Error checking hidden status for " + ind.getName() );
                return false;
            }
        }
    }

    @SuppressWarnings("serial")
    private class  DataPropertyStatementRoleFilter<E extends DataPropertyStatement>
    extends UnaryFunctor<E,Boolean>{
        @Override
        public Boolean fn(E dPropStmt) {
        	if( dPropStmt == null ) return false; //don't know why this would happen
            log.debug("checking hidden status for data property statement \"" + dPropStmt.getDatapropURI() + "\"");
            try {
                String propUri = dPropStmt.getDatapropURI();
                if (propUri == null) {
                	if ( ! canViewOddItems() ){ return false; }
                } else {
	                DataProperty prop = null;                
	                if( dataPropertyMap.containsKey(propUri) ){
	                    prop = dataPropertyMap.get(propUri);
	                }else{
	                    prop = wdf.getDataPropertyDao().getDataPropertyByURI(propUri);                    
	                    dataPropertyMap.put(propUri, prop);
	                }
	                if( prop == null ) {
	                    if( ! canViewOddItems() ){ return false; }
	                }else{
	                    if( sameLevelOrHigher( prop.getHiddenFromDisplayBelowRoleLevel() ) == false)
	                        return false;
	                }
                }

                Individual subject = dPropStmt.getIndividual();
                if( subject == null ) {
                    if( ! canViewOddItems() ){  return false; }
                }else{
                    if( sameLevelOrHigher( subject.getHiddenFromDisplayBelowRoleLevel() ) == false)
                        return false;
                }

                if( FILTER_ON_INDIVIDUAL_VCLASSES ){
                    VClass subjectClass =
                        (subject.getVClass() != null ? subject.getVClass() : wdf.getVClassDao().getVClassByURI(subject.getVClassURI()));
                    if( subjectClass == null ){
                        if( ! canViewOddItems() ){ return false; }
                    }else{
                        if( sameLevelOrHigher( subjectClass.getHiddenFromDisplayBelowRoleLevel() ) == false )
                            return false;

                    }
                }

            } catch (RuntimeException e) {
                log.warn("Error checking hidden status of data property statement \"" + dPropStmt.getDatapropURI() +"\"", e);
                return false;
            }
            return true;
        }
    }


    @SuppressWarnings("serial")
    private class  ObjectPropertyStatementRoleFilter<E extends ObjectPropertyStatement>
    extends UnaryFunctor<E,Boolean>{
        @Override
        public Boolean fn(E stmt) {
            if( stmt == null ) {
                log.debug("checking hidden status for null object property statement");
                return false;
            }
            log.debug("checking hidden status for object property statement \"" + stmt.getPropertyURI() + "\"");

            try {
                ObjectProperty prop = stmt.getProperty();                
                if( prop == null ){
                    String objPropUri = stmt.getPropertyURI();
                    prop = wdf.getObjectPropertyDao().getObjectPropertyByURI(objPropUri);                    
                }

                if (prop == null){
                    if (!canViewOddItems()) {
                        return false;
                    }
                } else {
                    if (sameLevelOrHigher(prop.getHiddenFromDisplayBelowRoleLevel()) == false){
                        return false;
                    }
                }

                Individual subject =
                    (stmt.getSubject() != null ?
                            stmt.getSubject()
                            : wdf.getIndividualDao().getIndividualByURI( stmt.getSubjectURI()));

                if (subject == null) {
                    if (!canViewOddItems()) {
                        return false;
                    }
                } else {
                    if (sameLevelOrHigher(subject
                            .getHiddenFromDisplayBelowRoleLevel()) == false)
                        return false;
                }

                Individual object =
                    (stmt.getObject() != null ?
                            stmt.getObject()
                            : wdf.getIndividualDao().getIndividualByURI( stmt.getObjectURI() ));

                if (object == null) {
                    if (!canViewOddItems()) {
                        return false;
                    }
                } else {
                    if (sameLevelOrHigher(object
                            .getHiddenFromDisplayBelowRoleLevel()) == false)
                        return false;
                }

                if( FILTER_ON_INDIVIDUAL_VCLASSES ){
                    VClass subjectClass =
                        (subject.getVClass() != null ? subject.getVClass() : wdf.getVClassDao().getVClassByURI(subject.getVClassURI()));
                    if( subjectClass == null ){
                        if( ! canViewOddItems() ){ return false; }
                    }else{
                        if( sameLevelOrHigher( subjectClass.getHiddenFromDisplayBelowRoleLevel() ) == false )
                            return false;
                    }

                    VClass objectClass =
                        (object.getVClass() != null ? object.getVClass() : wdf.getVClassDao().getVClassByURI(object.getVClassURI()));
                    if( objectClass == null ){
                        if( ! canViewOddItems() ){ return false; }
                    }else{
                        if( sameLevelOrHigher( objectClass.getHiddenFromDisplayBelowRoleLevel() ) == false )
                            return false;
                    }
                }
            } catch (RuntimeException e) {
                log.warn("Error checking hidden status of object property statement \"" + stmt.getPropertyURI()+"\"", e);
                return false;
            }
            return true;
        }
    }


}
