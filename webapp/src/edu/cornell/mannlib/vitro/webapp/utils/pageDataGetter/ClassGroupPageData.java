package edu.cornell.mannlib.vitro.webapp.utils.pageDataGetter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VClassGroupCache;

/**
 * This will pass these variables to the template:
 * classGroupUri: uri of the classgroup associated with this page.
 * vClassGroup: a data structure that is the classgroup associated with this page.     
 */
public class ClassGroupPageData implements PageDataGetter{
    private static final Log log = LogFactory.getLog(ClassGroupPageData.class);
    
    public Map<String,Object> getData(ServletContext context, VitroRequest vreq, String pageUri, Map<String, Object> page, String type ){
        HashMap<String, Object> data = new HashMap<String,Object>();
        String classGroupUri = vreq.getWebappDaoFactory().getPageDao().getClassGroupPage(pageUri);
        data.put("classGroupUri", classGroupUri);

        VClassGroupCache vcgc = VClassGroupCache.getVClassGroupCache(context);
        List<VClassGroup> vcgList = vcgc.getGroups(vreq.getPortalId());
        VClassGroup group = null;
        for( VClassGroup vcg : vcgList){
            if( vcg.getURI() != null && vcg.getURI().equals(classGroupUri)){
                group = vcg;
                break;
            }
        }
        if( classGroupUri != null && !classGroupUri.isEmpty() && group == null ){ 
            /*This could be for two reasons: one is that the classgroup doesn't exist
             * The other is that there are no individuals in any of the classgroup's classes */
            group = vreq.getWebappDaoFactory().getVClassGroupDao().getGroupByURI(classGroupUri);
            if( group != null ){
                List<VClassGroup> vcgFullList = vreq.getWebappDaoFactory().getVClassGroupDao()
                    .getPublicGroupsWithVClasses(false, true, false);
                for( VClassGroup vcg : vcgFullList ){
                    if( classGroupUri.equals(vcg.getURI()) ){
                        group = vcg;
                        break;
                    }                                
                }
                if( group == null ){
                    log.error("Cannot get classgroup '" + classGroupUri + "' for page '" + pageUri + "'");
                }else{
                    setAllClassCountsToZero(group);
                }
            }else{
                log.error("classgroup " + classGroupUri + " does not exist in the system");
            }
            
        }
                    
        data.put("vClassGroup", group);  //may put null            
        return data;
    }        
    
    
    public String getType(){
        return DisplayVocabulary.CLASSGROUP_PAGE_TYPE;
    } 
    
    protected void setAllClassCountsToZero(VClassGroup vcg){
        for(VClass vc : vcg){
            vc.setEntityCount(0);
        }
    }
}