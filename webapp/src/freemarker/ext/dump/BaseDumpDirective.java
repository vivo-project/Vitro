/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package freemarker.ext.dump;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import freemarker.core.Environment;
import freemarker.template.Template;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateDateModel;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateHashModelEx;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateModelIterator;
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
    
    enum Key {
        NAME("name"),
        TYPE("type"),
        VALUE("value"),
        DATE_TYPE("dateType");
                
        private final String key;
        
        Key(String key) {
            this.key = key;
        }
        
        public String toString() {
            return key;
        }
    }
    
    enum Type {
        STRING("String"),
        NUMBER("Number"),
        BOOLEAN("Boolean"),
        DATE("Date"),
        SEQUENCE("Sequence"),
        HASH("Hash"),
        // Technically it's a HashEx, but for the templates call it a Hash
        HASH_EX("Hash"), // ("HashEx")
        COLLECTION("Collection"),
        METHOD("Method"),
        DIRECTIVE("Directive");
        
        private final String type;
        
        Type(String type) {
            this.type = type;
        }        
        
        public String toString() {
            return type;
        }        
    }
    
    enum DateType {
        DATE("Date"),
        DATETIME("DateTime"),
        TIME("Time"),
        UNKNOWN("Unknown");
        
        private final String type;
        
        DateType(String type) {
            this.type = type;
        }

        public String toString() {
            return type;
        } 
    }
    
    
    protected Map<String, Object> getTemplateVariableData(String varName, Environment env) 
    throws TemplateModelException {
        
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(Key.NAME.toString(), varName);
        
        TemplateHashModel dataModel = env.getDataModel();       
        TemplateModel model =  dataModel.get(varName);

        // Don't return null if model == null. We still want to send the map to the template.
        if (model != null) {
            // TemplateMethodModel and TemplateDirectiveModel objects can only be
            // included in the data model at the top level.
            if (model instanceof TemplateMethodModel) {
                map.putAll( getTemplateModelData( ( TemplateMethodModel)model, varName ) );
                
            } else if (model instanceof TemplateDirectiveModel) {
                map.putAll( getTemplateModelData( ( TemplateDirectiveModel)model, varName ) );
                
            } else {
                map.putAll(getData(model));
            }
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

            } else if (model instanceof TemplateCollectionModel) {
                map.putAll( getTemplateModelData( ( TemplateCollectionModel)model ) );
                
            // Nodes and transforms not included here
                
            } else {
                map.putAll( getTemplateModelData( (TemplateModel)model ) );
            }
        }
        
        return map;
    }
    
    private Map<String, Object> getTemplateModelData(TemplateScalarModel model) throws TemplateModelException {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(Key.TYPE.toString(), Type.STRING);
        map.put(Key.VALUE.toString(), model.getAsString());
        return map;
    }
    
    private Map<String, Object> getTemplateModelData(TemplateBooleanModel model) throws TemplateModelException {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(Key.TYPE.toString(), Type.BOOLEAN);
        map.put(Key.VALUE.toString(), model.getAsBoolean());
        return map;
    }

    private Map<String, Object> getTemplateModelData(TemplateNumberModel model) throws TemplateModelException {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(Key.TYPE.toString(), Type.NUMBER);
        map.put(Key.VALUE.toString(), model.getAsNumber());
        return map;
    }
    
    private Map<String, Object> getTemplateModelData(TemplateDateModel model) throws TemplateModelException {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(Key.TYPE.toString(), Type.DATE);
        int dateType = model.getDateType();

        DateType type;
        switch (dateType) {
        case TemplateDateModel.DATE: 
            type = DateType.DATE;
            break;
        case TemplateDateModel.DATETIME:
            type = DateType.DATETIME;
            break;
        case TemplateDateModel.TIME:
            type = DateType.TIME;
            break;
        default:
            type = DateType.UNKNOWN;
        }
        map.put(Key.DATE_TYPE.toString(), type);
        
        map.put(Key.VALUE.toString(), model.getAsDate());
        return map;
    }

    private Map<String, Object> getTemplateModelData(TemplateHashModel model) throws TemplateModelException {
        // The data model is a hash; when else do we get here?
        log.debug("Dumping model " + model);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(Key.TYPE.toString(), Type.HASH);
        //map.put(Key.VALUE.toString(), ????);
        return map;
    }

    private Map<String, Object> getTemplateModelData(TemplateSequenceModel model) throws TemplateModelException {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(Key.TYPE.toString(), Type.SEQUENCE);
        int itemCount = model.size();
        List<Map<String, Object>> items = new ArrayList<Map<String, Object>>(itemCount);
        for ( int i = 0; i < itemCount; i++ ) {
            TemplateModel item = model.get(i);
            items.add(getData(item));
        }
        map.put(Key.VALUE.toString(), items);
        return map;
    }
    
    private Map<String, Object> getTemplateModelData(TemplateHashModelEx model) throws TemplateModelException {
        Object unwrappedModel = DeepUnwrap.permissiveUnwrap(model);
        // This seems to be the most reliable way of distinguishing a wrapped map from a wrapped object.
        // A map may be wrapped as a SimpleHash, and an object may be wrapped as a StringModel, but they could
        // be wrapped as other types as well.
        if ( unwrappedModel instanceof Map ) {
            return getMapData(model);
        }
        // Java objects are wrapped as TemplateHashModelEx-s.
        return getObjectData(model, unwrappedModel);
    }
    
    private Map<String, Object> getMapData(TemplateHashModelEx model) throws TemplateModelException {        
        Map<String, Object> map = new HashMap<String, Object>();        
        map.put(Key.TYPE.toString(), Type.HASH_EX);
        SortedMap<String, Object> items = new TreeMap<String, Object>();
        TemplateCollectionModel keys = model.keys();
        TemplateModelIterator iModel = keys.iterator();
        while (iModel.hasNext()) {
            String key = iModel.next().toString();
            TemplateModel value = model.get(key);
            items.put(key, getData(value));
        }        
        map.put(Key.VALUE.toString(), items);  
        return map;
    }

    private Map<String, Object> getObjectData(TemplateHashModelEx model, Object object) throws TemplateModelException {
        
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(Key.TYPE.toString(), object.getClass().getName());
        
        // Compile the set of properties and methods available to model
        SortedMap<String, Object> availableMethods = new TreeMap<String, Object>();
        
        // keys() gets only values visible to template based on the BeansWrapper used.
        // Note: if the BeansWrapper exposure level > BeansWrapper.EXPOSE_PROPERTIES_ONLY,
        // keys() returns both method and property name for any available method with no
        // parameters: e.g., both name and getName(). We are going to eliminate the latter.
        TemplateCollectionModel keys = model.keys();
        TemplateModelIterator iModel = keys.iterator();
        
        // Create a Set from keys so we can use the Set API.
        Set<String> keySet = new HashSet<String>();
        while (iModel.hasNext()) {
            String key = iModel.next().toString();
            keySet.add(key);
        }
        
        if (keySet.size() > 0) {
            
            Class<?> cls = object.getClass();
            Method[] methods = cls.getMethods(); 
    
            // Iterate through the methods rather than the keys, so that we can remove
            // some keys based on reflection on the methods. We also want to remove duplicates
            // like name/getName - we'll keep only the first form.
            for ( Method method : methods ) {
    
                // Eliminate methods declared on Object
                Class<?> c = method.getDeclaringClass();
                if (c.equals(java.lang.Object.class)) {
                    continue;
                }
                
                // Eliminate deprecated methods
                if (method.isAnnotationPresent(Deprecated.class)) {
                    continue;
                }

                // Include only methods included in keys(). This factors in visibility
                // defined by the model's BeansWrapper.                 
                String methodName = method.getName();
                String propertyName = getPropertyName(methodName);
 
                // If the method is available as a property, use that
                if (keySet.contains(propertyName)) {   
                    TemplateModel value = model.get(propertyName);
                    availableMethods.put(propertyName, getData(value));
                // Else look for the entire methodName in the key set
                } else if (keySet.contains(methodName)) {               
                    String methodDisplayName = getMethodDisplayName(method);
                    availableMethods.put(methodDisplayName, "");
                }
            }           
        }
        
        map.put(Key.VALUE.toString(), availableMethods);
        return map;
    }
    
    private String getMethodDisplayName(Method method) {
        String methodName = method.getName();
        Class<?>[] paramTypes = method.getParameterTypes();
        if (paramTypes.length > 0) {
            List<String> paramTypeList = new ArrayList<String>(paramTypes.length);
            for (Class<?> cls : paramTypes) {
                String name = cls.getName();
                String[] nameParts = name.split("\\.");
                String typeName = nameParts[nameParts.length-1];
                typeName = typeName.replaceAll(";", "s");
                paramTypeList.add(typeName);
            }
            methodName += "(" + StringUtils.join(paramTypeList, ", ") + ")";
        } else {
            methodName = methodName.replaceAll("^(get|is)", "");
            methodName = methodName.substring(0, 1).toLowerCase() + methodName.substring(1);           
        }
        
        return methodName;               
    }    

    // Return the method name as it is represented in TemplateHashModelEx.keys()
    private String getPropertyName(String methodName) {
        String keyName = methodName.replaceAll("^(get|is)", "");
        return StringUtils.uncapitalize(keyName);        
    }
    
    private Map<String, Object> getTemplateModelData(TemplateCollectionModel model) throws TemplateModelException {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(Key.TYPE.toString(), Type.COLLECTION);
        List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
        TemplateModelIterator iModel = model.iterator();
        while (iModel.hasNext()) {
            TemplateModel m = iModel.next();
            items.add(getData(m));
        }
        map.put(Key.VALUE.toString(), items);  
        return map;
    }
    
    private Map<String, Object> getTemplateModelData(TemplateMethodModel model, String varName) throws TemplateModelException {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(Key.TYPE.toString(), Type.METHOD);
        map.put("help", getHelp(model, varName));       
        return map;
    }
    
    private Map<String, Object> getTemplateModelData(TemplateDirectiveModel model, String varName) throws TemplateModelException {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(Key.TYPE.toString(), Type.DIRECTIVE);
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
        map.put(Key.TYPE.toString(), unwrappedModel.getClass().getName());
        map.put(Key.VALUE.toString(), unwrappedModel.toString());
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
