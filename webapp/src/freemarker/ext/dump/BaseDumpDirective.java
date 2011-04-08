/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package freemarker.ext.dump;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import freemarker.core.Environment;
import freemarker.template.Template;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateDateModel;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateHashModelEx;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateNumberModel;
import freemarker.template.TemplateScalarModel;
import freemarker.template.TemplateSequenceModel;
import freemarker.template.utility.DeepUnwrap;

/* TODO
 * - Check error messages generated for TemplateModelException-s. If too generic, need to catch, create specific
 * error message, and rethrow.
 */

public abstract class BaseDumpDirective implements TemplateDirectiveModel {

    private static final Log log = LogFactory.getLog(BaseDumpDirective.class);
    
    protected Map<String, Object> getTemplateVariableData(String varName, Environment env) 
    throws TemplateModelException {
        
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("name", varName);
        
        TemplateHashModel dataModel = env.getDataModel();       
        TemplateModel model =  dataModel.get(varName);
        
        // TemplateMethodModel and TemplateDirectiveModel objects can only be
        // included in the data model at the top level.
        if (model instanceof TemplateMethodModel) {
            map.putAll( getTemplateModelData( ( TemplateMethodModel)model, varName ) );
            
        } else if (model instanceof TemplateDirectiveModel) {
            map.putAll( getTemplateModelData( ( TemplateDirectiveModel)model, varName ) );
            
        } else {
            map.putAll(getData(model));
        }
        
        return map;
    }

    private Map<String, Object> getData(TemplateModel model) throws TemplateModelException {
        Map<String, Object> map = new HashMap<String, Object>();
        
        // Don't return null if model == null. We still want to send the map to the template.
        if (model != null) {
            // Do TemplateHashModel cases first, because models of some Java objects are
            // StringModels, and so are both TemplateScalarModels and TemplateHashModelExs.
            if (model instanceof TemplateHashModelEx) {
                map.putAll( getTemplateModelData( ( TemplateHashModelEx)model ) );
            
            } else if (model instanceof TemplateHashModel) {
                map.putAll( getTemplateModelData( ( TemplateHashModel)model ) );
                
            } else if (model instanceof TemplateScalarModel) {
                map.putAll( getTemplateModelData( (TemplateScalarModel)model ) );

            } else if (model instanceof TemplateBooleanModel) {
                map.putAll( getTemplateModelData( (TemplateBooleanModel)model ) );
                
            } else if (model instanceof TemplateNumberModel) {
                map.putAll( getTemplateModelData( (TemplateNumberModel)model ) );
                
            } else if (model instanceof TemplateDateModel) { 
                map.putAll( getTemplateModelData( (TemplateDateModel)model ) );
                
            } else if (model instanceof TemplateSequenceModel){
                map.putAll( getTemplateModelData( ( TemplateSequenceModel)model ) );

            } else {
                map.putAll( getTemplateModelData( (TemplateModel)model ) );
            }
        }
        
        return map;
    }
    
    private Map<String, Object> getTemplateModelData(TemplateScalarModel model) throws TemplateModelException {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("type", "String");
        map.put("value", model.getAsString());
        return map;
    }
    
    private Map<String, Object> getTemplateModelData(TemplateBooleanModel model) throws TemplateModelException {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("type", "Boolean");
        map.put("value", model.getAsBoolean());
        return map;
    }

    private Map<String, Object> getTemplateModelData(TemplateNumberModel model) throws TemplateModelException {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("type", "Number");
        map.put("value", model.getAsNumber());
        return map;
    }
    
    private Map<String, Object> getTemplateModelData(TemplateDateModel model) throws TemplateModelException {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("type", "Date");
        map.put("dateType", model.getDateType());
        map.put("value", model.getAsDate());
        return map;
    }

    private Map<String, Object> getTemplateModelData(TemplateHashModel model) throws TemplateModelException {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("type", "Hash");
        //map.put("value", model.getAsBoolean());
        return map;
    }

    private Map<String, Object> getTemplateModelData(TemplateSequenceModel model) throws TemplateModelException {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("type", "Sequence");
        //map.put("value", model.getAsNumber());
        return map;
    }
    
    private Map<String, Object> getTemplateModelData(TemplateHashModelEx model) throws TemplateModelException {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("type", "HashModelEx");;
        //map.put("value", model.getAsDate());
        return map;
    }
 
    private Map<String, Object> getTemplateModelData(TemplateMethodModel model, String varName) throws TemplateModelException {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("type", "Method");
        map.put("help", getHelp(model, varName));       
        return map;
    }
    
    private Map<String, Object> getTemplateModelData(TemplateDirectiveModel model, String varName) throws TemplateModelException {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("type", "Directive");;
        map.put("help", getHelp(model, varName));
        return map;
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> getHelp(TemplateModel model, String varName) {
        Map<String, Object> map = null;
        if ( model instanceof TemplateMethodModel || model instanceof TemplateDirectiveModel ) {
            Class<?> cls = model.getClass();
            Method[] methods = cls.getMethods();
            for (Method method : methods) {
                if ( method.getName().equals("help") ) {
                    try {
                        map = (Map<String, Object>) method.invoke(model, varName);
                    } catch (Exception e) {    
                        String modelClass = model instanceof TemplateMethodModel ? "TemplateMethodModel" : "TemplateDirectiveModel";
                        log.error("Error invoking method help() on " + modelClass + " of class " + cls.getName());
                    } 
                    break;
                }
            }            
        }
        return map;
    }
    
    private Map<String, Object> getTemplateModelData(TemplateModel model) throws TemplateModelException {
        // One of the above cases should have applied. Track whether this actually occurs.
        log.debug("Found model with no known type"); 
        Map<String, Object> map = new HashMap<String, Object>();
        Object unwrappedModel = DeepUnwrap.permissiveUnwrap(model);
        map.put("type", unwrappedModel.getClass().getName());
        map.put("value", unwrappedModel.toString());
        return map;        
    }
    
    protected void dump(String templateName, Map<String, Object> map, String modelName, Environment env) 
    throws TemplateException, IOException {
        
        Template template = env.getConfiguration().getTemplate(templateName);
        StringWriter sw = new StringWriter();
        template.process(map, sw);     
        Writer out = env.getOut();
        out.write(sw.toString());
                
    }
    
}
