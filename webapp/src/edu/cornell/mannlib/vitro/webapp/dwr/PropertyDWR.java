/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dwr;

import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.directwebremoting.WebContext;
import org.directwebremoting.WebContextFactory;

import edu.cornell.mannlib.vitro.webapp.beans.PropertyInstance;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

/**
   Represents a Vitro entity to entity property.  It includes values
   from the entities, object property statements, properties, and tables
   bundled up in a usable object.

   This is a class intended to support Direct Web Remoting(DWR).
   It exposes methods that can be called from javascript from browsers.
   We could expose some other objects but this allows us to expose only
   the methods we want for security reasons, though some of these are
   destructive.
*/
public class PropertyDWR {
	
	private static final Log log = LogFactory.getLog(PropertyDWR.class.getName());
	
    public static boolean debug = true;

    public PropertyDWR(){               
    }
    
    public Object test(){
        WebContext ctx = WebContextFactory.get();
        HttpServletRequest req = ctx.getHttpServletRequest();
        VitroRequest vreq = new VitroRequest(req);
        WebappDaoFactory wdf = vreq.getWebappDaoFactory();
        
        Collection c = 
            wdf.getPropertyInstanceDao().getExistingProperties("http://example.org/property", null);
        return c.iterator().next();
    }

    /**
     * Gets only unhidden properties.
     * @returns Collection of PropertyInstance objs
     */
    public Collection getAllPropInstByVClass(String classURI){
        WebContext ctx = WebContextFactory.get();
        HttpServletRequest req = ctx.getHttpServletRequest();
        VitroRequest vreq = new VitroRequest(req);
        WebappDaoFactory wdf = vreq.getFullWebappDaoFactory();
        
        Collection c = 
            wdf.getPropertyInstanceDao().getAllPropInstByVClass(classURI);
        return c;
    }
    
    public Collection getAllPossiblePropInstForIndividual(String individualURI) {
        WebContext ctx = WebContextFactory.get();
        HttpServletRequest req = ctx.getHttpServletRequest();
        VitroRequest vreq = new VitroRequest(req);
        WebappDaoFactory wdf = vreq.getFullWebappDaoFactory();

        Collection c = 
            wdf.getPropertyInstanceDao().getAllPossiblePropInstForIndividual(individualURI);
    	return c;
    }

    public PropertyInstance getProperty(String subjectURI, String predicateURI, String objectURI) {
        WebContext ctx = WebContextFactory.get();
        HttpServletRequest req = ctx.getHttpServletRequest();
        VitroRequest vreq = new VitroRequest(req);
        WebappDaoFactory wdf = vreq.getFullWebappDaoFactory();
        
       return wdf.getPropertyInstanceDao().getProperty(subjectURI, predicateURI, objectURI);
    }

    public int insertProp( PropertyInstance prop) {
        WebContext ctx = WebContextFactory.get();
        HttpServletRequest req = ctx.getHttpServletRequest();
        VitroRequest vreq = new VitroRequest(req);
        return vreq.getFullWebappDaoFactory().getPropertyInstanceDao().insertProp(prop);                
    }

    public int deleteProp(String subjectUri, String predicateUri, String objectUri){
        WebContext ctx = WebContextFactory.get();
        HttpServletRequest req = ctx.getHttpServletRequest();
        VitroRequest vreq = new VitroRequest(req);
        vreq.getFullWebappDaoFactory().getPropertyInstanceDao().deleteObjectPropertyStatement(subjectUri, predicateUri, objectUri);
        return 0;
    }

    /**
       Gets all of the existing qualifiers for a given properties.id
    */
    @Deprecated
    public Collection getExistingQualifiers(String propertyURI){
        Collection c = new ArrayList(1);
        c.add("qualifiers are no longer supporeted ");
        return c;
    }

    /**
       Gets all of the properties that exist on the object property statements table for this entity.
       This includes all rows that have the entityId as the domain and as the range.
       Propeties for which the entityId is the domain will be getDomainSide() == true
       and for which the entityId is the range side will be getDomainSide() == false.
    */
   public Collection getExistingProperties(String entityURI){
       WebContext ctx = WebContextFactory.get();
       HttpServletRequest req = ctx.getHttpServletRequest();
       VitroRequest vreq = new VitroRequest(req);
       WebappDaoFactory wdf = vreq.getFullWebappDaoFactory();
       
       return wdf.getPropertyInstanceDao().getExistingProperties(entityURI, null);       
   }
   
//   private PropertyInstance prepare(PropertyInstance in){
//       return in;
//   }
//   
//   public Collection prepare(Collection properties){
//       if( properties == null || properties.size() == 0 )
//           return Collections.EMPTY_LIST;
//       
//       ArrayList outProps = new ArrayList(properties.size());
//       for( Object prop : properties){
//           if( prop instanceof PropertyIn
//       return outProps;
//   }
}
