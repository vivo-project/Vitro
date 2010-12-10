/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.widgets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.VClassGroupTemplateModel;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.VClassTemplateModel;
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
        Map<String,Object> body = getAllClassGroupData(request);
        try {
            body.put("urls",env.getDataModel().get("urls"));
            body.put("urlMapping",env.getDataModel().get("urlMapping"));
        } catch (TemplateModelException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String macroName = Mode.ALL_CLASS_GROUPS.macroName;
        return new WidgetTemplateValues(macroName, body);
    }
   
    protected Map<String,Object> getAllClassGroupData(HttpServletRequest request){
        Map<String,Object> map = new HashMap<String,Object>();
        
        VitroRequest vreq = new VitroRequest(request);
        List<VClassGroup> classGroups =
            vreq.getWebappDaoFactory().getVClassGroupDao().getPublicGroupsWithVClasses();
        
        LinkedList<VClassGroupTemplateModel> cgList = new LinkedList<VClassGroupTemplateModel>();
        for( VClassGroup classGroup : classGroups){
            cgList.add( new VClassGroupTemplateModel( classGroup ));
        }
        map.put("vclassGroupList",cgList);
        return map;
    }
    
    protected WidgetTemplateValues doClassDisplay(Environment env, Map params,
            HttpServletRequest request, ServletContext context) {        
        Map<String,Object> body = getClassData(request);
        try {
            body.put("urls",env.getDataModel().get("urls"));
            body.put("urlMapping",env.getDataModel().get("urlMapping"));
        } catch (TemplateModelException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String macroName = Mode.VCLASS.macroName;
        return new WidgetTemplateValues(macroName, body);
    }

    private Map<String, Object> getClassData(HttpServletRequest request) {
        Map<String,Object> map = new HashMap<String,Object>();
        map.putAll(getClassGroupData(request));
        String classUri = request.getParameter(Mode.VCLASS.param);
        VitroRequest vreq = new VitroRequest(request);
        VClass vclass = vreq.getWebappDaoFactory().getVClassDao().getVClassByURI(classUri);
        map.put("class", new VClassTemplateModel(vclass));
        
        //TODO: add list of individuals for class?
        return map;
    }

    protected WidgetTemplateValues doClassGroupDisplay(Environment env,
            Map params, HttpServletRequest request, ServletContext context) {

        Map<String,Object> body = getClassGroupData(request);
        try {
            body.put("urls",env.getDataModel().get("urls"));
            body.put("urlMapping",env.getDataModel().get("urlMapping"));
        } catch (TemplateModelException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String macroName = Mode.CLASS_GROUP.macroName;
        return new WidgetTemplateValues(macroName, body);
    } 
        
    protected Map<String, Object> getClassGroupData(HttpServletRequest request) {
        Map<String,Object> map = new HashMap<String,Object>();
        
        String vcgUri = request.getParameter(Mode.CLASS_GROUP.param);
        VitroRequest vreq = new VitroRequest(request);
        VClassGroup vcg = vreq.getWebappDaoFactory().getVClassGroupDao().getGroupByURI(vcgUri);
        vreq.getWebappDaoFactory().getVClassDao().addVClassesToGroup(vcg, false, true);
        ArrayList<VClassTemplateModel> classes = new ArrayList<VClassTemplateModel>(vcg.size());
        for( VClass vc : vcg){
            classes.add(new VClassTemplateModel(vc));
        }
        map.put("classes", classes);
        
        map.put("classGroup", new VClassGroupTemplateModel(vcg));
        map.put(Mode.CLASS_GROUP.param, vcgUri);
        
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
            String param = request.getParameter( mode.param );
            if( param != null && !param.isEmpty() ){
                return mode;
            }
        }
        
        for( Mode mode : Mode.values()){
            String param = (String)params.get( mode.param );
            if( param != null && !param.isEmpty() ){
                return mode;
            }
        }
        
        return DEFAULT_MODE;        
    }
}
