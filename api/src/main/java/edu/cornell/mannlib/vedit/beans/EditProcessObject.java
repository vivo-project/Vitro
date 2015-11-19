/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vedit.beans;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import edu.cornell.mannlib.vedit.forwarder.PageForwarder;
import edu.cornell.mannlib.vedit.listener.ChangeListener;
import edu.cornell.mannlib.vedit.listener.EditPreProcessor;
import edu.cornell.mannlib.vedit.validator.Validator;

public class EditProcessObject implements Serializable {

    private String key = null;

    private Class<?> beanClass = null;
    private Class<?> implementationClass = null;
    private boolean useRecycledBean = false;

    private Object beanMask = null;
    private List<Object[] /* Object[2] */> simpleMask = new LinkedList<Object[]>();

    private Map<String, List<Validator>> validatorMap = new HashMap<String, List<Validator>>();
    private Map<String, String> errMsgMap = new HashMap<String, String>();

    private Map<String, String> defaultValueMap = new HashMap<String, String>();

    private List<EditPreProcessor> preProcessorList = new LinkedList<EditPreProcessor>();
    private List<ChangeListener> changeListenerList = new LinkedList<ChangeListener>();

    private Object originalBean = null;
    private Object newBean = null;

    private String idFieldName = null;
    private Class<?> idFieldClass = null;

    private FormObject formObject = null;

    private Object dataAccessObject = null;
    private HashMap<String, Object /* DAO */> additionalDaoMap = new HashMap<String, Object>();

    private Method insertMethod = null;
    private Method updateMethod = null;
    private Method deleteMethod = null;

    private PageForwarder postInsertPageForwarder = null;
    private PageForwarder postUpdatePageForwarder = null;
    private PageForwarder postDeletePageForwarder = null;

    private HttpSession session = null;
    private String referer = null;

    private String action = null;

    private Map<String, String[]> requestParameterMap = null;

    private Map<String, String> badValueMap = new HashMap<String, String>();
    
    private Map<String,Object> attributeMap = new HashMap<String, Object>();

    private Method getMethod = null;
    //assumed to take an integer primary key argument, at least for now

    public String getKey(){
        return key;
    }

    public void setKey(String key){
        this.key = key;
    }

    public Class<?> getBeanClass(){
        return beanClass;
    }

    public void setBeanClass(Class<?> beanClass){
        this.beanClass = beanClass;
    }

    public Class<?> getImplementationClass(){
        return implementationClass;
    }

    public void setImplementationClass(Class<?> implementationClass){
        this.implementationClass = implementationClass;
    }

    public Object getBeanMask() {
        return beanMask;
    }

    public void setBeanMask(Object beanMask) {
        this.beanMask = beanMask;
    }

    public List<Object[]> getSimpleMask(){
        return simpleMask;
    }

    public void setSimpleMask(List<Object[]> simpleMask){
        this.simpleMask = simpleMask;
    }

    public List<ChangeListener> getChangeListenerList() {
        return changeListenerList;
    }

    public void setChangeListenerList(List<? extends ChangeListener> changeListenerList) {
        this.changeListenerList = new ArrayList<ChangeListener>(changeListenerList);
    }

    public List<EditPreProcessor> getPreProcessorList() {
        return preProcessorList;
    }

    public void setPreProcessorList(List<? extends EditPreProcessor> preProcessorList) {
        this.preProcessorList = new ArrayList<EditPreProcessor>(preProcessorList);
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

    public Class<?> getIdFieldClass() {
        return idFieldClass;
    }

    public void setIdFieldClass(Class<?> cls) {
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

    public Map<String, String[]> getRequestParameterMap() {
        return requestParameterMap;
    }

    public void setRequestParameterMap (Map<String, String[]> rpmap) {
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

    public HashMap<String, Object> getAdditionalDaoMap() {
        return additionalDaoMap;
    }
    public void setAdditionalDaoMap(HashMap<String, Object> adm) {
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

    public Map<String, String> getDefaultValueMap() {
        return defaultValueMap;
    }

    public void setDefaultValueMap(Map<String, String> dvh) {
        this.defaultValueMap = dvh;
    }

    public Map<String, List<Validator>> getValidatorMap(){
        return validatorMap;
    }

    public void setValidatorMap(Map<String, List<Validator>> validatorMap){
        this.validatorMap = validatorMap;
    }

    public Map<String, String> getErrMsgMap() {
        return errMsgMap;
    }

    public void setErrMsgMap(Map<String, String> emh){
        errMsgMap = emh;
    }

    public Map<String, String> getBadValueMap() {
        return badValueMap;
    }

    public void setBadValueMap(Map<String, String> bvh){
        badValueMap = bvh;
    }

    public Map<String, Object> getAttributeMap() {
    	return this.attributeMap;
    }
    
    public Object getAttribute(String key) {
    	return this.attributeMap.get(key);
    }
    
    public void setAttribute(String key, Object value) {
    	this.attributeMap.put(key, value);
    }
    
}
