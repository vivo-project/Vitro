/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.widgets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VClassGroupCache;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.VClassGroupTemplateModel;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.VClassTemplateModel;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual.IndividualTemplateModel;
import freemarker.core.Environment;
import freemarker.template.TemplateModelException;

/**
 * This is a widget to display a classgroups, 
 * classes in classgroup, and indviduals in class.
 *  
 * @author bdc34
 *
 */
public class BrowseWidget extends Widget {
    final static Log log = LogFactory.getLog(BrowseWidget.class);
    
    @Override
    protected WidgetTemplateValues process(Environment env, Map params,
            HttpServletRequest request, ServletContext context) throws Exception 
    {
        Mode mode = getMode( request, params );
        switch( mode ){          
            case VCLASS_ALPHA:
                return doClassAlphaDisplay(env,params,request,context);
            case CLASS_GROUP:
                return doClassGroupDisplay(env, params, request, context);
            case VCLASS:
                return doClassDisplay(env, params, request, context);
            case ALL_CLASS_GROUPS:
                return doAllClassGroupsDisplay(env, params, request, context);
            default:
                return doAllClassGroupsDisplay(env, params, request, context);
        }
    }
    
    private WidgetTemplateValues doClassAlphaDisplay(Environment env,
            Map params, HttpServletRequest request, ServletContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    protected WidgetTemplateValues doAllClassGroupsDisplay(Environment env, Map params,
            HttpServletRequest request, ServletContext context) {
        Map<String,Object> body = getAllClassGroupData(request, params, context);
        try {
            body.put("urls",env.getDataModel().get("urls"));
            body.put("currentPage", env.getDataModel().get("currentPage"));                        
        } catch (TemplateModelException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String macroName = Mode.ALL_CLASS_GROUPS.macroName;
        return new WidgetTemplateValues(macroName, body);
    }
   
    /**
     * Gets a list of all VClassGroups with vclasses with individual counts.
     */
    protected Map<String,Object> getAllClassGroupData(HttpServletRequest request, Map params, ServletContext context){
        Map<String,Object> map = new HashMap<String,Object>();
                
        VitroRequest vreq = new VitroRequest(request);
        
        VClassGroupCache vcgc = VClassGroupCache.getVClassGroupCache(context);
        List<VClassGroup> cgList = vcgc.getGroups(vreq.getPortalId());
        
//        List<VClassGroup> classGroups =
//            vreq.getWebappDaoFactory().getVClassGroupDao().getPublicGroupsWithVClasses();
//        
        LinkedList<VClassGroupTemplateModel> cgtmList = new LinkedList<VClassGroupTemplateModel>();
        for( VClassGroup classGroup : cgList){
            cgtmList.add( new VClassGroupTemplateModel( classGroup ));
        }
        map.put("vclassGroupList",cgtmList);
        return map;
    }
    
    protected WidgetTemplateValues doClassDisplay(Environment env, Map params,
            HttpServletRequest request, ServletContext context) {        
        
        Map<String,Object> body = getClassData(request,params,context);
        
        try {
            body.put("urls",env.getDataModel().get("urls"));
            body.put("currentPage", env.getDataModel().get("currentPage"));            
        } catch (TemplateModelException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String macroName = Mode.VCLASS.macroName;
        return new WidgetTemplateValues(macroName, body);
    }

    private Map<String, Object> getClassData(HttpServletRequest request, Map params, ServletContext context) {
        Map<String,Object> map = new HashMap<String,Object>();
        
        map.putAll(getClassGroupData(request, params,context));
        
        String classUri = getParam(Mode.VCLASS, request, params);
        VitroRequest vreq = new VitroRequest(request);
        VClass vclass = vreq.getWebappDaoFactory().getVClassDao().getVClassByURI(classUri);
        map.put("class", new VClassTemplateModel(vclass));
        
        List<Individual> inds = vreq.getWebappDaoFactory().getIndividualDao()
            .getIndividualsByVClass(vclass);
        
        List<IndividualTemplateModel> tInds = new ArrayList<IndividualTemplateModel>(inds.size());
        for( Individual ind : inds){
            tInds.add(new IndividualTemplateModel(ind, vreq));
        }
        map.put("individualsInClass", tInds);

        return map;
    }

    protected WidgetTemplateValues doClassGroupDisplay(Environment env,
            Map params, HttpServletRequest request, ServletContext context) {

        Map<String,Object> body = getClassGroupData(request,params, context);
        
        try {
            body.put("urls",env.getDataModel().get("urls"));
            body.put("currentPage", env.getDataModel().get("currentPage"));            
        } catch (TemplateModelException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String macroName = Mode.CLASS_GROUP.macroName;
        return new WidgetTemplateValues(macroName, body);
    } 
        
    protected Map<String, Object> getClassGroupData(HttpServletRequest request, Map params, ServletContext context) {
        Map<String,Object> map = new HashMap<String,Object>();
        
        String vcgUri = getParam(Mode.CLASS_GROUP, request, params);
        VitroRequest vreq = new VitroRequest(request);        
        //VClassGroup vcg = vreq.getWebappDaoFactory().getVClassGroupDao().getGroupByURI(vcgUri);
        
        VClassGroupCache vcgc = VClassGroupCache.getVClassGroupCache(context);
        VClassGroup vcg = vcgc.getGroup(vreq.getPortalId(), vcgUri);        
        
        //vreq.getWebappDaoFactory().getVClassDao().addVClassesToGroup(vcg, false, true);
        ArrayList<VClassTemplateModel> classes = new ArrayList<VClassTemplateModel>(vcg.size());
        for( VClass vc : vcg){
            classes.add(new VClassTemplateModel(vc));
        }
        map.put("classes", classes);
        
        map.put("classGroup", new VClassGroupTemplateModel(vcg));
        map.put("classGroupName", vcg.getPublicName());
        map.put("classGroupUri", vcg.getURI());
        
        return map;
    }
    
    enum Mode{
        VCLASS_ALPHA("vclassAlpha","vclassAlpha"),
        VCLASS("vclass","vclassUri"),
        CLASS_GROUP("classGroup","classgroupUri"),
        ALL_CLASS_GROUPS("allClassGroups","all");                
        
        String macroName;
        String param;
        Mode(String macroName, String param){
            this.macroName = macroName;
            this.param = param;
        }
    }
    
    protected final static Mode DEFAULT_MODE = Mode.ALL_CLASS_GROUPS;
    
    protected Mode getMode(HttpServletRequest request, Map params){
        for( Mode mode : Mode.values()){
            String queryParam = request.getParameter( mode.param );
            if( queryParam != null && !queryParam.isEmpty() ){
                return mode;
            }
            Object obj = params.get( mode.param );
            String param = obj != null ? obj.toString():null;
            if( param != null && !param.isEmpty() ){
                return mode;
            }
        }                
        return DEFAULT_MODE;        
    }
    
    protected String getParam(Mode mode, HttpServletRequest request, Map params){
        if( request.getParameter(mode.param) != null )
            return request.getParameter(mode.param);
        if( params.get(mode.param) != null )
            return params.get(mode.param).toString();
        else
            return null;
    }
}
