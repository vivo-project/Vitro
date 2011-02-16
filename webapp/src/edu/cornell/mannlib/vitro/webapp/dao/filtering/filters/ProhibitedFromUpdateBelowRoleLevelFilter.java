/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.filtering.filters;

import java.util.List;

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

/*
 * bdc34: This is not yet used anywhere in the system.
 */ 
public class ProhibitedFromUpdateBelowRoleLevelFilter extends VitroFiltersImpl {

    protected final RoleLevel userRole;    
    protected final WebappDaoFactory wdf;
    
    private final static Log log = LogFactory.getLog(ProhibitedFromUpdateBelowRoleLevelFilter.class);
    
    public ProhibitedFromUpdateBelowRoleLevelFilter( RoleLevel role , WebappDaoFactory wdf ){
        super();
        log.debug("initializing ProhibitedFromUpdateBelowRoleLevelFilter");
        if( role == null ) 
            throw new IllegalArgumentException("ProhibitedFromUpdateBelowRoleLevelFilter must have a RoleLevel ");        
        if( wdf == null ) 
            throw new IllegalArgumentException("ProhibitedFromUpdateBelowRoleLevelFilter must have a DaoFactory");        
        this.userRole = role;        
        this.wdf = wdf;
        
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
        return ( userRole.compareTo(other) >= 0 );            
    }
    
    private boolean canViewOddItems( ){
        return sameLevelOrHigher( RoleLevel.DB_ADMIN ) ;            
    }
    
    @SuppressWarnings("serial")
    private class  RoleFilter<E extends ResourceBean> extends UnaryFunctor<E,Boolean>{
        @Override
        public Boolean fn(E resource) {
            log.debug("fn for update RoleFilter");
            try{
                if( resource == null )
                    return canViewOddItems();                
                else
                    return sameLevelOrHigher( resource.getProhibitedFromUpdateBelowRoleLevel() );                
            }catch(RuntimeException th){
                log.warn("Error checking update prohibition status for " + resource, th);
                return false;
            }
        }
    }
    
    @SuppressWarnings("serial")
    private class IndividualRoleFilter extends UnaryFunctor<Individual,Boolean>{
        @Override
        public Boolean fn(Individual ind){
            log.debug("fn for update IndividualRoleFilter");
            if( ind == null ) 
                return canViewOddItems();
            
            try{
                if( ! sameLevelOrHigher( ind.getProhibitedFromUpdateBelowRoleLevel() ) )
                    return false;
                
                List<VClass> vclasses =  ind.getVClasses(true);
                
                if( vclasses == null ){
                    VClass clazz = wdf.getVClassDao().getVClassByURI(ind.getVClassURI());
                    if( clazz == null )
                        return canViewOddItems();
                    else 
                        return sameLevelOrHigher(clazz.getProhibitedFromUpdateBelowRoleLevel() );
                }
                
                for( VClass vclass : vclasses ){                    
                    if( ! sameLevelOrHigher(vclass.getProhibitedFromUpdateBelowRoleLevel()) )
                        return false;
                }                
                return true;
                
            }catch(RuntimeException ex){
                log.warn("Error checking update prohibition status for " + ind );
                return false;
            }   
        }
    }
   
    @SuppressWarnings("serial")
    private class  DataPropertyStatementRoleFilter<E extends DataPropertyStatement>
    extends UnaryFunctor<E,Boolean>{
        @Override
        public Boolean fn(E dPropStmt) {
            log.debug("fn for update DataPropertyStatementRoleFilter");
            try {
                if( dPropStmt == null ) return false; //don't know why this would happen
                
                DataProperty prop = wdf.getDataPropertyDao().getDataPropertyByURI(dPropStmt.getDatapropURI());
                if( prop == null ) {
                    if( ! canViewOddItems() ){ return false; }
                }else{
                    if( sameLevelOrHigher( prop.getProhibitedFromUpdateBelowRoleLevel() ) == false)
                        return false;
                }
                
                Individual subject = dPropStmt.getIndividual();                        
                if( subject == null ) {
                    if( ! canViewOddItems() ){  return false; }
                }else{
                    if( sameLevelOrHigher( subject.getProhibitedFromUpdateBelowRoleLevel() ) == false)
                        return false;
                }
                
                VClass subjectClass = 
                    (subject.getVClass() != null ? subject.getVClass() : wdf.getVClassDao().getVClassByURI(subject.getVClassURI()));                       
                if( subjectClass == null ){
                    if( ! canViewOddItems() ){ return false; }
                }else{
                    if( sameLevelOrHigher( subjectClass.getProhibitedFromUpdateBelowRoleLevel() ) == false )
                        return false;
                }                    
            } catch (RuntimeException e) {
                log.warn("Error checking update prohibition status of data property statement \"" + dPropStmt + "\"", e);
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
            log.debug("fn for update ObjectPropertyStatementRoleFilter");

            if( stmt == null )
                return false;
            
            try {
                ObjectProperty prop = stmt.getProperty();
				if (prop == null) {
					prop = wdf.getObjectPropertyDao().getObjectPropertyByURI(stmt.getPropertyURI());
				}
				if (prop == null) {
					if (!canViewOddItems()) {
						return false;
					}
				} else {
					if (sameLevelOrHigher(prop.getProhibitedFromUpdateBelowRoleLevel()) == false) {
						return false;
					}
				}
                
                Individual subject = 
                    (stmt.getSubject() != null ? stmt.getSubject() : wdf.getIndividualDao().getIndividualByURI( stmt.getSubjectURI()));            
                if( subject == null ) {
                    if( ! canViewOddItems() ){ return false; }
                }else{
                    if( sameLevelOrHigher( subject.getProhibitedFromUpdateBelowRoleLevel() ) == false)
                        return false;
                }
                
                Individual object = 
                    (stmt.getObject() != null ? stmt.getObject() : wdf.getIndividualDao().getIndividualByURI( stmt.getObjectURI() ));            
                if( object == null ) {
                    if( ! canViewOddItems() ) { return false; }
                }else{
                    if( sameLevelOrHigher( subject.getProhibitedFromUpdateBelowRoleLevel() ) == false)
                        return false;
                }
                
                VClass subjectClass = 
                    (subject.getVClass() != null ? subject.getVClass() : wdf.getVClassDao().getVClassByURI(subject.getVClassURI()));                       
                if( subjectClass == null ){
                    if( ! canViewOddItems() ){ return false; }
                }else{
                    if( sameLevelOrHigher( subjectClass.getProhibitedFromUpdateBelowRoleLevel() ) == false )
                        return false;
                }
                        
                VClass objectClass = 
                    (object.getVClass() != null ? object.getVClass() : wdf.getVClassDao().getVClassByURI(object.getVClassURI()));                       
                if( objectClass == null ){
                    if( ! canViewOddItems() ){ return false; }
                }else{
                    if( sameLevelOrHigher( objectClass.getProhibitedFromUpdateBelowRoleLevel() ) == false )
                        return false;
                }                                         
            } catch (RuntimeException e) {
                log.warn("Error checking update prohibition status of " + stmt, e);
                return false;
            }
            return true;
        }
    }
    

}
