/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.dataGetter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import edu.cornell.mannlib.vitro.webapp.web.templatemodels.individuallist.ListedIndividualBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import org.apache.jena.rdf.model.Model;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.json.JsonServlet;
import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.VClassGroupsForRequest;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VClassGroupCache;
import edu.cornell.mannlib.vitro.webapp.utils.JsonToFmModel;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.VClassGroupTemplateModel;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.VClassTemplateModel;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.individuallist.ListedIndividual;

public class BrowseDataGetter extends DataGetterBase implements DataGetter {
    final static Log log = LogFactory.getLog(BrowseDataGetter.class);
    String dataGetterURI;
    VitroRequest vreq;
    ServletContext context;

    /**
     * Constructor with display model and data getter URI that will be called by reflection.
     */
    public BrowseDataGetter(VitroRequest vreq, Model displayModel, String dataGetterURI){
        this.configure(vreq, displayModel,dataGetterURI);
    }   
    
    /**
     * Configure this instance based on the URI and display model.
     */
    protected void configure(VitroRequest vreq, Model displayModel, String dataGetterURI) {
    	if( vreq == null ) 
    		throw new IllegalArgumentException("VitroRequest  may not be null.");
	   if( displayModel == null ) 
           throw new IllegalArgumentException("Display Model may not be null.");
       if( dataGetterURI == null )
           throw new IllegalArgumentException("PageUri may not be null.");
               
       this.vreq = vreq;
       this.context = vreq.getSession().getServletContext();
       this.dataGetterURI = dataGetterURI;
    }
    
    @Override
    public Map<String, Object> getData(Map<String, Object> pageData) { 
        try{            
            Map params = vreq.getParameterMap();
            
            Mode mode = getMode( vreq, params );
            switch( mode ){          
                case VCLASS_ALPHA:
                    return doClassAlphaDisplay(params,vreq,context);
                case CLASS_GROUP:
                    return doClassGroupDisplay(params, vreq, context);
                case VCLASS:
                    return doClassDisplay(params, vreq, context);
                case ALL_CLASS_GROUPS:
                    return doAllClassGroupsDisplay( params, vreq, context);
                default:
                    return doAllClassGroupsDisplay( params, vreq, context);
            }
            }catch(Throwable th){
                log.error(th,th);
                return Collections.emptyMap();
            }
    }

    public String getType() { 
        return DisplayVocabulary.HOME_PAGE_TYPE;
    }
    
  //Get data servuice
    public String getDataServiceUrl() {
    	return UrlBuilder.getUrl("/dataservice?getSearchIndividualsByVClass=1&vclassId=");
    }
    private Map<String, Object> doClassAlphaDisplay( Map params, VitroRequest request, ServletContext context) throws Exception {
        Map<String,Object> body = new HashMap<String,Object>();
        body.putAll(getCommonValues(context, request));
        body.putAll(getClassAlphaValues(params,request,context));        
        return body;
    }

    private Map<String,Object> getClassAlphaValues( Map params, VitroRequest request, ServletContext context) throws Exception{
        Map<String,Object> map= new HashMap<String,Object>();
        
        String classUri = getParam(Mode.VCLASS, request, params);
        VitroRequest vreq = new VitroRequest(request);
        VClass vclass = vreq.getWebappDaoFactory().getVClassDao().getVClassByURI(classUri);
        map.put("class", new VClassTemplateModel(vclass));
        
        JSONObject vclassRes = JsonServlet.getSearchIndividualsByVClass(vclass.getURI(), request);        
        map.put("totalCount", JsonToFmModel.convertJSONObjectToMap( (String) vclassRes.get("totalCount") ));
        map.put("alpha", JsonToFmModel.convertJSONObjectToMap( (String) vclassRes.get("alpha") ));
        map.put("individuals", JsonToFmModel.convertJSONArrayToList( (JSONArray) vclassRes.get("individuals") ));
        map.put("pages", JsonToFmModel.convertJSONArrayToList( (JSONArray) vclassRes.get("pages") ));
        map.put("letters", JsonToFmModel.convertJSONArrayToList( (JSONArray) vclassRes.get("letters") ));
        
        return map;
    }
    
    private Map<String,Object> getCommonValues( ServletContext context, VitroRequest vreq){
        Map<String,Object> values = new HashMap<String,Object>();              
                
        VClassGroupsForRequest vcgc = VClassGroupCache.getVClassGroups(vreq);
        List<VClassGroup> cgList = vcgc.getGroups();        
        LinkedList<VClassGroupTemplateModel> cgtmList = new LinkedList<VClassGroupTemplateModel>();
        for( VClassGroup classGroup : cgList){
            cgtmList.add( new VClassGroupTemplateModel( classGroup ));
        }
        values.put("vClassGroups",cgtmList);
        
        return values;
    }
    
    protected Map<String, Object> doAllClassGroupsDisplay( Map params, VitroRequest request, ServletContext context) {        
        Map<String,Object> body = new HashMap<String,Object>();
        body.putAll(getCommonValues(context,request));        
        body.putAll(getAllClassGroupData(request, params, context));
                        
        return body;
    }
   
    /**
     * Gets a list of all VClassGroups with vclasses with individual counts.
     * @param request current VitroRequest
     * @param params a parameter map
     * @param context current servlet context
     */
    protected Map<String,Object> getAllClassGroupData(VitroRequest request, Map params, ServletContext context){
        Map<String,Object> map = new HashMap<String,Object>();                  
        return map;
    }
    
    protected Map<String, Object> doClassDisplay( Map params,
            VitroRequest request, ServletContext context) {                
        Map<String,Object> body = new HashMap<String,Object>();
        
        body.putAll(getCommonValues(context,request));        
        body.putAll(getClassData(request,params,context));
            
        return body;
    }

    private Map<String, Object> getClassData(VitroRequest request, Map params, ServletContext context) {
        Map<String,Object> map = new HashMap<String,Object>();
        
        map.putAll(getClassGroupData(request, params,context));
        
        String classUri = getParam(Mode.VCLASS, request, params);
        VitroRequest vreq = new VitroRequest(request);
        VClass vclass = vreq.getWebappDaoFactory().getVClassDao().getVClassByURI(classUri);
        map.put("class", new VClassTemplateModel(vclass));
        
        List<Individual> inds = vreq.getWebappDaoFactory().getIndividualDao()
            .getIndividualsByVClass(vclass);
        
        List<ListedIndividual> tInds = new ArrayList<ListedIndividual>(inds.size());
        for( Individual ind : inds){
            tInds.add(ListedIndividualBuilder.build(ind, vreq));
        }
        map.put("individualsInClass", tInds);

        return map;
    }

    protected Map<String, Object> doClassGroupDisplay(Map params, VitroRequest request, ServletContext context) {
        Map<String,Object> body = new HashMap<String,Object>();
        body.putAll(getCommonValues(context,request));        
        body.putAll( getClassGroupData(request,params, context));
   
        return body;
    } 
        
    protected Map<String, Object> getClassGroupData(VitroRequest request, Map params, ServletContext context) {
        Map<String,Object> map = new HashMap<String,Object>();
        
        String vcgUri = getParam(Mode.CLASS_GROUP, request, params);
        VitroRequest vreq = new VitroRequest(request);        
        
        VClassGroupsForRequest vcgc = VClassGroupCache.getVClassGroups(request);
        VClassGroup vcg = vcgc.getGroup(vcgUri);        
        
        ArrayList<VClassTemplateModel> classes = new ArrayList<VClassTemplateModel>(vcg.size());
        for( VClass vc : vcg){
            classes.add(new VClassTemplateModel(vc));
        }
        map.put("classes", classes);
        
        map.put("classGroup", new VClassGroupTemplateModel(vcg));
        
        return map;
    }
    
        
    enum Mode{
        VCLASS_ALPHA("vclassAlpha"),
        VCLASS("vclassUri"),
        CLASS_GROUP("classgroupUri"),
        ALL_CLASS_GROUPS("all");                                
        String param;
        Mode(String param){        
            this.param = param;
        }
    }
    
    protected final static Mode DEFAULT_MODE = Mode.ALL_CLASS_GROUPS;
    
    protected Mode getMode(VitroRequest request, Map<String, Object> params){
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
    
    public static String getParam(Mode mode, VitroRequest request, Map params){
        if( request.getParameter(mode.param) != null )
            return request.getParameter(mode.param);
        if( params.get(mode.param) != null )
            return params.get(mode.param).toString();
        else
            return null;
    }
    
    /**
     * For processig of JSONObject
     */
    public JSONObject convertToJSON(Map<String, Object> dataMap, VitroRequest vreq) {
    	JSONObject rObj = null;
    	return rObj;
    }


}
