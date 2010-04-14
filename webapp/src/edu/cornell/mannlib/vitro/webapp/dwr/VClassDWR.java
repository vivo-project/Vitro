/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dwr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import javax.servlet.http.HttpServletRequest;

import org.directwebremoting.WebContext;
import org.directwebremoting.WebContextFactory;

import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

public class VClassDWR {    

    public VClassDWR(){        
    }

    /**
     * Returns all vclasses that the given vclas can have as the other side of
     * of the given property.
     *
     * Gets vclasses for the vclass select drop down on the ent_edit.jsp dynamic
     * add proprties form.
     *
     *
     * @param vclassId - vclass we want to make a property for
     * @param propertyId - property we want to use
     * @param filterOutUninstanciated - if true filter out any vclasses with zero instances.
     * @return a list of VClass objects, one for each vclass that could be in the
     * relation indicated by the parameters.
     */
    public Collection <VClass> getVClasses(String vclassURI, String propertyURI, boolean filterOutUninstanciated){
        List <VClass> vclasses = new ArrayList <VClass>();
        
        WebContext ctx = WebContextFactory.get();
        HttpServletRequest req = ctx.getHttpServletRequest();
        VitroRequest vreq = new VitroRequest(req);
        
        vclasses = vreq.getWebappDaoFactory().getVClassDao().getVClassesForProperty(vclassURI, propertyURI);
//        if( vclasses != null && filterOutUninstanciated ){
//            ListIterator<VClass> it = vclasses.listIterator();
//            while(it.hasNext()){
//                VClass clazz = it.next();
//                if( clazz.getEntityCount() == 0 )
//                    it.remove();
//            }
//        }
        return vclasses;
    }

}
