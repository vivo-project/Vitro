/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vedit.beans;

import java.util.List;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import javax.servlet.http.HttpSession;

import edu.cornell.mannlib.vedit.forwarder.PageForwarder;
import edu.cornell.mannlib.vedit.beans.BeanDependency;
import edu.cornell.mannlib.vedit.beans.FormObject;
import java.lang.reflect.Method;
import java.io.Serializable;

public class EditProcessObject implements Serializable {

    private String key = null;

    private Class beanClass = null;
    private Class implementationClass = null;
    private boolean useRecycledBean = false;

    private Object beanMask = null;
    private List simpleMask = new LinkedList();

    private HashMap validatorMap = new HashMap();
    private HashMap errMsgMap = new HashMap();

    private HashMap defaultValueMap = new HashMap();

    private List preProcessorList = new LinkedList();
    private List changeListenerList = new LinkedList();

    private Object originalBean = null;
    private Object newBean = null;

    private String idFieldName = null;
    private Class idFieldClass = null;

    private FormObject formObject = null;

    private Object dataAccessObject = null;
    private HashMap additionalDaoMap = new HashMap();

    private Method insertMethod = null;
    private Method updateMethod = null;
    private Method deleteMethod = null;

    private PageForwarder postInsertPageForwarder = null;
    private PageForwarder postUpdatePageForwarder = null;
    private PageForwarder postDeletePageForwarder = null;

    private HttpSession session = null;
    private String referer = null;

    private String action = null;

    private Map requestParameterMap = null;

    private HashMap badValueMap = new HashMap();
    
    private HashMap<String,Object> attributeMap = new HashMap<String,Object>();

    /***** experimental ******/
    private Stack epoStack = new Stack();
    private HashMap beanDependencies = new HashMap();

    private Method getMethod = null;
    //assumed to take an integer primary key argument, at least for now

    public String getKey(){
        return key;
    }

    public void setKey(String key){
        this.key = key;
    }

    public Class getBeanClass(){
        return beanClass;
    }

    public void setBeanClass(Class beanClass){
        this.beanClass = beanClass;
    }

    public Class getImplementationClass(){
        return implementationClass;
    }

    public void setImplementationClass(Class implementationClass){
        this.implementationClass = implementationClass;
    }

    public Object getBeanMask() {
        return beanMask;
    }

    public void setBeanMask(Object beanMask) {
        this.beanMask = beanMask;
    }

    public List getSimpleMask(){
        return simpleMask;
    }

    public void setSimpleMask(List simpleMask){
        this.simpleMask = simpleMask;
    }

    public List getChangeListenerList() {
        return changeListenerList;
    }

    public void setChangeListenerList(List changeListenerList) {
        this.changeListenerList = changeListenerList;
    }

    public List getPreProcessorList() {
        return preProcessorList;
    }

    public void setPreProcessorList(List preProcessorList) {
        this.preProcessorList = preProcessorList;
    }

    public Object getOriginalBean(){
        return originalBean;
    }

    public void setOriginalBean(Object originalBean){
        this.originalBean = originalBean;
    }

    public Object getNewBean(){
        return newBean;
    }

    public void setNewBean(Object newBean){
        this.newBean = newBean;
    }

    public String getIdFieldName() {
        return idFieldName;
    }

    public void setIdFieldName(String ifn) {
        this.idFieldName = ifn;
    }

    public Class getIdFieldClass() {
        return idFieldClass;
    }

    public void setIdFieldClass(Class cls) {
        this.idFieldClass = cls;
    }

    public FormObject getFormObject() {
        return formObject;
    }

    public void setFormObject(FormObject foo){
        formObject=foo;
    }

    public HttpSession getSession(){
        return session;
    }

    public boolean getUseRecycledBean(){
        return useRecycledBean;
    }

    public void setUseRecycledBean(boolean useRecycledBean){
        this.useRecycledBean = useRecycledBean;
    }

    public void setSession(HttpSession session){
        this.session = session;
    }

    public String getReferer(){
        return referer;
    }

    public void setReferer(String referer){
        this.referer = referer;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Map getRequestParameterMap() {
        return requestParameterMap;
    }

    public void setRequestParameterMap (Map rpmap) {
        requestParameterMap = rpmap;
    }

    public PageForwarder getPostInsertPageForwarder(){
        return postInsertPageForwarder;
    }

    public void setPostInsertPageForwarder(PageForwarder pipf){
        postInsertPageForwarder = pipf;
    }

    public PageForwarder getPostUpdatePageForwarder(){
        return postUpdatePageForwarder;
    }

    public void setPostUpdatePageForwarder(PageForwarder pupf){
        postUpdatePageForwarder = pupf;
    }

    public PageForwarder getPostDeletePageForwarder(){
        return postDeletePageForwarder;
    }

    public void setPostDeletePageForwarder(PageForwarder pdpf){
        postDeletePageForwarder = pdpf;
    }

    public Object getDataAccessObject() {
        return dataAccessObject;
    }

    public void setDataAccessObject(Object dao) {
        dataAccessObject = dao;
    }

    public HashMap getAdditionalDaoMap() {
        return additionalDaoMap;
    }
    public void setAdditionalDaoMap(HashMap adm) {
        additionalDaoMap = adm;
    }

    public Method getInsertMethod(){
        return insertMethod;
    }

    public void setInsertMethod(Method insertMethod){
        this.insertMethod = insertMethod;
    }

    public Method getUpdateMethod(){
        return updateMethod;
    }

    public void setUpdateMethod(Method updateMethod){
        this.updateMethod = updateMethod;
    }

    public Method getDeleteMethod(){
        return deleteMethod;
    }

    public void setDeleteMethod(Method deleteMethod){
        this.deleteMethod = deleteMethod;
    }

    public Method getGetMethod(){
        return getMethod;
    }

    public void setGetMethod(Method getMethod){
        this.getMethod = getMethod;
    }

    public HashMap getDefaultValueMap() {
        return defaultValueMap;
    }

    public void setDefaultValueMap(HashMap dvh) {
        this.defaultValueMap = dvh;
    }

    public HashMap getValidatorMap(){
        return validatorMap;
    }

    public void setValidatorMap(HashMap validatorMap){
        this.validatorMap = validatorMap;
    }

    public HashMap getErrMsgMap() {
        return errMsgMap;
    }

    public void setErrMsgMap(HashMap emh){
        errMsgMap = emh;
    }

    public HashMap getBadValueMap() {
        return badValueMap;
    }

    public void setBadValueMap(HashMap bvh){
        badValueMap = bvh;
    }

    public Map getAttributeMap() {
    	return this.attributeMap;
    }
    
    public Object getAttribute(String key) {
    	return this.attributeMap.get(key);
    }
    
    public void setAttribute(String key, Object value) {
    	this.attributeMap.put(key, value);
    }
    
    public Stack getEpoStack(){
        return epoStack;
    }

    public HashMap /*to BeanDependency*/ getBeanDependencies(){
        return beanDependencies;
    }

    public void setBeanDependencies(HashMap beanDependencies){
        this.beanDependencies = beanDependencies;
    }

    public BeanDependency getBeanDependency(String name){
        return (BeanDependency) beanDependencies.get(name);
    }

    public Object getDependentBean(String name){
        return ((BeanDependency)beanDependencies.get(name)).getBean();
    }

    /******* probably will need to change this *******/
    public void setEpoStack(Stack epoStack){
        this.epoStack = epoStack;
    }

}
