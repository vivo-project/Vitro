/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.jsptags;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.ServletIdentifierBundleFactory;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyList;
import edu.cornell.mannlib.vitro.webapp.auth.policy.RequestPolicyList;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ServletPolicyList;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Authorization;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyIface;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AddDataPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AddObjectPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.DropDataPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.DropObjectPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.EditDataPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.EditObjPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestActionConstants;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.RdfLiteralHash;
import edu.cornell.mannlib.vitro.webapp.utils.FrontEndEditingUtils;
import edu.cornell.mannlib.vitro.webapp.utils.StringUtils;

/**
 * JSP tag to generate the HTML of links for edit, delete or
 * add of a Property.
 *
 * Maybe we should have a mode where it just sets a var to a
 * map with "href" = "edit/editDatapropDispatch.jsp?subjectUri=..." and "type" = "delete"
 *
 * @author bdc34
 *
 */
public class PropertyEditLinks extends TagSupport{
    Object item;
    String var;
    String icons;
    String data;
    
    private static final Log log = LogFactory.getLog(PropertyEditLinks.class.getName());
    
    public static final String ICON_DIR = "/images/edit_icons/";
    
    public Object getItem() { return item; }
    public void setItem(Object item) { this.item = item; }

    public String getVar(){ return var; }
    public void setVar(String var){ this.var = var; }

    public void setIcons(String ic){ icons = ic; }
    public String getIcons(){ return icons; }
    
    public void setData(String data){ this.data = data; }
    public String getData(){ return data; }
            
    @Override
    public int doStartTag() throws JspException {
        if( item == null ) {
            log.error("item passed to <edLnk> tag is null");
            return SKIP_BODY;
        }                                
        //try the policy in the request first, the look for a policy in the servlet context
        //request policy takes precedence
        PolicyIface policy = RequestPolicyList.getPolicies(pageContext.getRequest());
        if( policy == null || ( policy instanceof PolicyList && ((PolicyList)policy).size() == 0 )){
            policy = ServletPolicyList.getPolicies( pageContext.getServletContext() );
            if( policy == null || ( policy instanceof PolicyList && ((PolicyList)policy).size() == 0 )){            
                log.error("No policy found in request at " + RequestPolicyList.POLICY_LIST);
                return SKIP_BODY;
            }
        }              
        
        IdentifierBundle ids = (IdentifierBundle)ServletIdentifierBundleFactory
            .getIdBundleForRequest(pageContext.getRequest(), 
                    pageContext.getSession(), 
                    pageContext.getServletContext());
        
        if( ids == null ){
            log.error("No IdentifierBundle objects for request");
            return SKIP_BODY;
        }
        
        Individual entity = (Individual)pageContext.getRequest().getAttribute("entity");           
                 
        LinkStruct[] links = null;

        //get context prefix needs to end with a slash like "/vivo/" or "/"
        String contextPath =  ((HttpServletRequest)pageContext.getRequest()).getContextPath();
        if( ! contextPath.endsWith("/") )
            contextPath = contextPath + "/";
        if( ! contextPath.startsWith("/") )
            contextPath = "/" + contextPath;
        
        if( item instanceof ObjectPropertyStatement ){
            ObjectPropertyStatement prop = (ObjectPropertyStatement)item;           
            links = doObjPropStmt( prop, policyToAccess(ids, policy, prop), contextPath );  
            
        } else if( item instanceof DataPropertyStatement ){
            DataPropertyStatement prop = (DataPropertyStatement)item;
            links = doDataPropStmt( prop, policyToAccess(ids, policy, prop), contextPath );
            
        } else if (entity == null) {
            log.error("unable to find an Individual in request using var name 'entity'");
            return SKIP_BODY;         
            
        } else if( item instanceof ObjectProperty ){
            ObjectProperty prop = (ObjectProperty)item;                     
            links = doObjProp( prop, entity, policyToAccess(ids, policy, entity.getURI(), prop), contextPath );  
            
        } else if( item instanceof DataProperty ){
            DataProperty prop = (DataProperty)item; // a DataProperty populated for this subject individual            
            links = doDataProp( prop, entity, policyToAccess(ids, policy, entity.getURI(), prop),contextPath );
            
        // Vitro namespace property    
        } else if (item instanceof String) {            
            String predicateUri = (String) item;
            String subjectUri = entity.getURI();
            if (FrontEndEditingUtils.isVitroNsDataProp(predicateUri)) {
                if (data == null) { // link to add a new value
                    links = doVitroNsDataProp( subjectUri, predicateUri, policyToAccess(ids, policy, subjectUri, predicateUri), contextPath );
                } else { // links to edit or delete an existing value
                    DataPropertyStatement dps = (DataPropertyStatement) new DataPropertyStatementImpl(subjectUri, predicateUri, data); 
                    links = doVitroNsDataPropStmt( dps, entity, policyToAccess(ids, policy, dps), contextPath );                       
                }           
            } else if (FrontEndEditingUtils.isVitroNsObjProp(predicateUri)) {
                if (data == null) { // link to add a new value
                    links = doObjProp( subjectUri, predicateUri, policyToAccess(ids, policy, subjectUri, predicateUri), contextPath );
                } else { // links to edit or delete an existing value  
                    // RY **** May need new policyToAccess which gets the specific obj prop statement using the data as well as subject and predicate
                    // This is NOT the correct object property statement - we need the link individual uri in data, instead of the link URL
                    // Then we can combine this with doObjPropStmt
                    ObjectPropertyStatement prop = new ObjectPropertyStatementImpl(subjectUri, predicateUri, data);
                    links = doObjPropStmt( prop, policyToAccess(ids, policy, prop), contextPath );                       
                }                    
            }
            else {
                log.error("PropertyEditLinks cannot make links for an object of type " + predicateUri);
                return SKIP_BODY;
            }
            

        } else {
            log.error("PropertyEditLinks cannot make links for an object of type "+item.getClass().getName());
        	return SKIP_BODY;
        }

        if( getVar() != null ){
            pageContext.getRequest().setAttribute(getVar(), links);
        } else {
            try{    
                JspWriter out = pageContext.getOut();
                if( links != null){
                    for( LinkStruct ln : links ){
                        if( ln != null ){
                            out.print( makeElement( ln ) + '\n' );
                        }
                    }
                }               
            } catch(IOException ioe){
                log.error( ioe );
            }
        }

        return SKIP_BODY;
    }

    protected LinkStruct[] doDataProp(DataProperty dprop, Individual entity, EditLinkAccess[] allowedAccessTypeArray, String contextPath) {
        if( allowedAccessTypeArray == null || dprop == null || allowedAccessTypeArray.length == 0 ) {
            log.debug("null or empty access type array in doDataProp for dprop "+dprop.getPublicName()+"; most likely just a property prohibited from editing");
            return empty_array;
        }
        LinkStruct[] links = new LinkStruct[1];
        
        if (dprop.getDataPropertyStatements()!= null && dprop.getDisplayLimit()==1 && dprop.getDataPropertyStatements().size()>=1) {
            log.debug("not showing an \"add\" link for data property because it has a display limit of 1 (a hack until expose a choice to make the dataproperty functional)");
        } else {
            if( contains( allowedAccessTypeArray, EditLinkAccess.ADDNEW ) ){
                log.debug("data property "+dprop.getPublicName()+" gets an \"add\" link");
                String url = makeRelativeHref(contextPath + "edit/editDatapropStmtRequestDispatch.jsp",
                                              "subjectUri",   entity.getURI(),
                                              "predicateUri", dprop.getURI());
                LinkStruct ls = new LinkStruct();
                ls.setHref( url );
                ls.setType("add");
                ls.setMouseoverText("add a new entry");
                links[0] = ls;
            } else {
                log.debug("no add link generated for property "+dprop.getPublicName());
            }
        }
        return links;
    }

    protected LinkStruct[] doVitroNsDataProp(String subjectUri, String propertyUri, EditLinkAccess[] allowedAccessTypeArray, String contextPath) {
        if( allowedAccessTypeArray == null || subjectUri == null || allowedAccessTypeArray.length == 0 ) {
            log.debug("null or empty access type array in doDataProp for vitro namespace property " + propertyUri + "; most likely just a property prohibited from editing");
            return empty_array;
        }
        LinkStruct[] links = new LinkStruct[1];
        
        Model model =  (Model)pageContext.getServletContext().getAttribute("jenaOntModel");
        StmtIterator stmts = model.listStatements(model.createResource(subjectUri), 
                                                  model.getProperty(propertyUri), 
                                                  (RDFNode) null);  
        
        if (stmts.hasNext()) {       
            log.debug("not showing an \"add\" link for vitro namespace property " + propertyUri + " because it has a limit of 1");
        } else {       
            if( contains( allowedAccessTypeArray, EditLinkAccess.ADDNEW ) ){
                log.debug("vitro namespace property "+propertyUri+" gets an \"add\" link");
                LinkStruct ls = null;
                if (propertyUri.equals(VitroVocabulary.IMAGETHUMB)) {
                    ls = getImageLink(subjectUri, contextPath, "add");                   
                } else {
                    String url = makeRelativeHref(contextPath +"edit/editDatapropStmtRequestDispatch.jsp",
                                                  "subjectUri",   subjectUri,
                                                  "predicateUri", propertyUri,
                                                  "vitroNsProp",  "true");
                    ls = new LinkStruct();
                    ls.setHref( url );
                    ls.setType("add");
                    ls.setMouseoverText("add a new entry");                   
                }
                links[0] = ls;
            } else {
                log.debug("no add link generated for vitro namespace property "+propertyUri);
            }
        }
        return links;
    }

    protected LinkStruct[] doObjProp(ObjectProperty oprop, Individual entity, EditLinkAccess[] allowedAccessTypeArray, String contextPath) {
        if( allowedAccessTypeArray == null || oprop == null || allowedAccessTypeArray.length == 0 ) {
            log.debug("null or empty access type array in doObjProp for oprop "+oprop.getDomainPublic()+"; most likely just a property prohibited from editing");
            return empty_array;
        }

        return doObjProp(entity.getURI(), oprop.getURI(), allowedAccessTypeArray, contextPath);
    }
    
    protected LinkStruct[] doObjProp(String subjectUri, String predicateUri, EditLinkAccess[] allowedAccessTypeArray, String contextPath) {
        if( allowedAccessTypeArray == null || subjectUri == null || allowedAccessTypeArray.length == 0 ) {
            log.debug("null or empty access type array in doObjProp for oprop "+ predicateUri +"; most likely just a property prohibited from editing");
            return empty_array;
        }
        LinkStruct[] links = new LinkStruct[1];

        if( contains( allowedAccessTypeArray, EditLinkAccess.ADDNEW )){           
            String url= makeRelativeHref(contextPath + "edit/editRequestDispatch.jsp",
                                         "subjectUri",   subjectUri,
                                         "predicateUri", predicateUri);
            LinkStruct ls = new LinkStruct();
            ls.setHref( url );
            ls.setType("add");
            ls.setMouseoverText("add " + getObjPropMouseoverLabel(predicateUri));
            links[0]=ls;
        }
        return links;
    }

    protected LinkStruct[] doDataPropStmt(DataPropertyStatement dpropStmt, EditLinkAccess[] allowedAccessTypeArray, String contextPath) {
        if( allowedAccessTypeArray == null || dpropStmt == null || allowedAccessTypeArray.length == 0 ) {
            log.info("null or empty access type array in doDataPropStmt for "+dpropStmt.getDatapropURI());
            return empty_array;
        }
        LinkStruct[] links = new LinkStruct[2];
        int index=0;

        String dpropHash = String.valueOf(RdfLiteralHash.makeRdfLiteralHash( dpropStmt ));

        if( contains( allowedAccessTypeArray, EditLinkAccess.MODIFY ) ){
            log.debug("permission found to UPDATE data property statement "+dpropStmt.getDatapropURI()+" ("+dpropStmt.getData()+") so icon created");
            String url = ( contains( allowedAccessTypeArray, EditLinkAccess.DELETE ) ) 
                ? makeRelativeHref(contextPath + "edit/editDatapropStmtRequestDispatch.jsp",
                        "subjectUri",   dpropStmt.getIndividualURI(),
                        "predicateUri", dpropStmt.getDatapropURI(),
                        "datapropKey",  dpropHash)
                : makeRelativeHref(contextPath + "edit/editDatapropStmtRequestDispatch.jsp",
                		"subjectUri",   dpropStmt.getIndividualURI(),
                        "predicateUri", dpropStmt.getDatapropURI(),
                        "datapropKey",  dpropHash,
                        "deleteProhibited", "prohibited");
            LinkStruct ls = new LinkStruct();
            ls.setHref( url );
            ls.setType("edit");
            ls.setMouseoverText("edit this text");
            links[index] = ls;             index++;

        } else {
            log.debug("NO permission to UPDATE this data property statement ("+dpropStmt.getDatapropURI()+") found in policy");
        }
        if( contains( allowedAccessTypeArray, EditLinkAccess.DELETE ) ){
            log.debug("permission found to DELETE data property statement "+dpropStmt.getDatapropURI()+" so icon created");
            String url = makeRelativeHref(contextPath + "edit/editDatapropStmtRequestDispatch.jsp",
                                          "subjectUri",   dpropStmt.getIndividualURI(),
                                          "predicateUri", dpropStmt.getDatapropURI(),
                                          "datapropKey",  dpropHash,
                                          "cmd", "delete");
            LinkStruct ls = new LinkStruct();
            ls.setHref( url );
            ls.setType("delete");
            ls.setMouseoverText("delete this text");
            links[index] = ls;     index++;

        } else {
            log.debug("NO permission to DELETE this data property statement ("+dpropStmt.getDatapropURI()+") found in policy");
        }
        return links;
    }

    protected LinkStruct[] doVitroNsDataPropStmt(DataPropertyStatement dpropStmt, Individual subject, EditLinkAccess[] allowedAccessTypeArray, String contextPath) {
        
        if( allowedAccessTypeArray == null || dpropStmt == null || allowedAccessTypeArray.length == 0 ) {
            log.debug("Null or empty access type array for vitro namespace property " + dpropStmt.getDatapropURI());
            return empty_array;
        }

        String subjectUri = subject.getURI();
        String predicateUri = dpropStmt.getDatapropURI();
        
        LinkStruct[] links = new LinkStruct[2]; 
        
        if (predicateUri.equals(VitroVocabulary.IMAGETHUMB)) {
            if( contains( allowedAccessTypeArray, EditLinkAccess.MODIFY ) ){
                log.debug("permission found to UPDATE vitro namepsace property statement "+ predicateUri);
                links[0] = getImageLink(subjectUri, contextPath, "edit");
                return links; 
            }
            else {
                log.debug("NO permission found to DELETE vitro namespace property " + predicateUri);
            }           
        }
        
        String value = dpropStmt.getData();        
        Model model =  (Model)pageContext.getServletContext().getAttribute("jenaOntModel");
        
        String dpropHash = null;
        try{
        	dpropHash = String.valueOf(RdfLiteralHash.makeVitroNsLiteralHash( subject, predicateUri, value, model ));
        }catch(IllegalArgumentException ex){
        	log.debug("could not create hash for " + subject + " " + predicateUri + " " + value);
        	return links;
        }
        
        String dispatchUrl = contextPath + "edit/editDatapropStmtRequestDispatch.jsp";

        int index = 0;
        
        boolean deleteAllowed = ( contains( allowedAccessTypeArray, EditLinkAccess.DELETE ) && 
            !( predicateUri.equals(VitroVocabulary.LABEL)) || predicateUri.equals(VitroVocabulary.RDF_TYPE) );

        if( contains( allowedAccessTypeArray, EditLinkAccess.MODIFY ) ){
            log.debug("permission found to UPDATE vitro namepsace property statement "+ predicateUri);
            LinkStruct ls = new LinkStruct();
            String url = deleteAllowed 
                ? makeRelativeHref(dispatchUrl,
                        "subjectUri",   subjectUri,
                        "predicateUri", predicateUri,
                        "datapropKey",  dpropHash,
                        "vitroNsProp",  "true")
                : makeRelativeHref(dispatchUrl,
                        "subjectUri",   subjectUri,
                        "predicateUri", predicateUri,
                        "datapropKey",  dpropHash,
                        "vitroNsProp",  "true",
                        "deleteProhibited", "prohibited");
                
            ls.setHref(url);
            ls.setType("edit");
            ls.setMouseoverText(predicateUri.equals("label") ? "edit name" : "edit this text");
            links[index] = ls; 
            index++;
        }
        else {
            log.debug("NO permission found to UPDATE vitro namespace property " + predicateUri);
        }

        // Name and type can be edited but not deleted
        if ( deleteAllowed ) {
            LinkStruct ls = new LinkStruct();
            log.debug("Permission found to DELETE vitro namespace property " + predicateUri);
            String url = makeRelativeHref(dispatchUrl, 
                                          "subjectUri", subjectUri,
                                          "predicateUri", predicateUri,
                                          "datapropKey",  dpropHash,
                                          "vitroNsProp", "true",
                                          "cmd", "delete");
            ls.setHref(url);
            ls.setType("delete");
            ls.setMouseoverText("delete this text");
            links[index] = ls; 
            index++;
        }
        else {
            log.debug("NO permission found to DELETE vitro namespace property " + predicateUri);
        }
            
        return links;
    }

    protected LinkStruct[] doObjPropStmt(ObjectPropertyStatement opropStmt, EditLinkAccess[] allowedAccessTypeArray, String contextPath) {
        if( allowedAccessTypeArray == null || opropStmt == null || allowedAccessTypeArray.length == 0 ) {
            log.info("null or empty access type array in doObjPropStmt for "+opropStmt.getPropertyURI());
            return empty_array;
        }
        
        String subjectUri = opropStmt.getSubjectURI();
        String predicateUri = opropStmt.getPropertyURI();
        String objectUri = opropStmt.getObjectURI();
        
        String mouseoverLabel = getObjPropMouseoverLabel(predicateUri);
        
        LinkStruct[] links = new LinkStruct[2];
        int index=0;
        
        if( contains( allowedAccessTypeArray, EditLinkAccess.MODIFY ) ){            
            log.debug("permission found to UPDATE object property statement "+ predicateUri +" so icon created");
            String url = ( contains( allowedAccessTypeArray, EditLinkAccess.DELETE ) ) 
                ? makeRelativeHref(contextPath + "edit/editRequestDispatch.jsp",
                    "subjectUri",   subjectUri,
                    "predicateUri", predicateUri,
                    "objectUri",    objectUri)
                : makeRelativeHref(contextPath + "edit/editRequestDispatch.jsp",
                    "subjectUri",   subjectUri,
                    "predicateUri", predicateUri,
                    "objectUri",    objectUri,
                    "deleteProhibited", "prohibited");

            LinkStruct ls = new LinkStruct();
            ls.setHref( url );
            ls.setType("edit");
            ls.setMouseoverText("change this " + mouseoverLabel);
            links[index] = ls; index++;

        } else {
            log.debug("NO permission to UPDATE this object property statement (" + predicateUri + ") found in policy");
        }
        if( contains( allowedAccessTypeArray, EditLinkAccess.DELETE ) ){
            log.debug("permission found to DELETE object property statement "+ predicateUri + " so icon created");
            String url = makeRelativeHref(contextPath + "edit/editRequestDispatch.jsp",
                    "subjectUri",   subjectUri,
                    "predicateUri", predicateUri,
                    "objectUri",    objectUri,
                    "cmd",          "delete");
            LinkStruct ls = new LinkStruct();
            ls.setHref( url );
            ls.setType("delete");
            ls.setMouseoverText("delete this " + mouseoverLabel);
            links[index] = ls; index++;

        } else {
            log.debug("NO permission to DELETE this object property statement (" + predicateUri + ") found in policy");
        }
        return links;
    }
    

    /* ********************* utility methods ********************************* */
    protected static String makeRelativeHref( String baseUrl, String ... queries ) {
        String href = baseUrl;
        if( queries == null || queries.length % 2 != 0 )
            log.debug("makeRelativeHref() needs an even number of queries");

        for( int i=0; i < queries.length; i=i+2){
            String separator = ( i==0 ? "?" : "&amp;" );
            try {
                href = href + separator + URLEncoder.encode(queries[i], "UTF-8") + '=' + URLEncoder.encode(queries[i+1],"UTF-8");
            } catch (UnsupportedEncodingException e) { log.error( e ); }
        }
        return href;
    }

    protected static boolean contains(EditLinkAccess[] allowedAccessTypeArray, EditLinkAccess accessType) {
        if( allowedAccessTypeArray == null || allowedAccessTypeArray.length == 0 || accessType == null  )
            return false;

        for( int i=0; i< allowedAccessTypeArray.length ; i ++ ){
            if( allowedAccessTypeArray[i] == accessType ) return true;
        }
        return false;
    }

    protected String makeElement( LinkStruct ln ){
        String element = 
            "<a class=\"image " + ln.getType() + "\" href=\"" + ln.getHref() + 
            "\" title=\"" + (ln.getMouseoverText()==null ? ln.getType() : ln.getMouseoverText()) + "\">" ;
        
        if( "true".equalsIgnoreCase(getIcons()) ){
            String contextPath=((HttpServletRequest)pageContext.getRequest()).getContextPath();
            String imagePath=null;
            if (contextPath==null) {
                imagePath = ICON_DIR + ln.getType() + ".gif";
                log.debug("image path when context path null: \""+imagePath+"\".");
            } else if (contextPath.equals("")) {
                imagePath = ICON_DIR + ln.getType() + ".gif";
                log.debug("image path when context path blank: \""+imagePath+"\".");
            } else {
                imagePath = contextPath + ICON_DIR + ln.getType() + ".gif";
                log.debug("image path when non-zero context path (\""+contextPath+"\"): \""+imagePath+"\".");
            }
            element +=  "<img src=\"" + imagePath+ "\" alt=\"" + ln.getType() + "\"/>";            
        } else {                          
            element +=  ln.getType() ;
        }        
        return element + "</a>\n";
    }

    public static final EditLinkAccess[] NO_ACCESS = {};
    public static final EditLinkAccess[] ACCESS_TEMPLATE = {}; 

    protected EditLinkAccess[] policyToAccess( IdentifierBundle ids, PolicyIface policy, String subjectUri, ObjectProperty item){
        EditLinkAccess[] allowedAccessTypeArray;
        
        RequestedAction action = new AddObjectPropStmt(subjectUri, item.getURI(), RequestActionConstants.SOME_URI);
        PolicyDecision dec = policy.isAuthorized(ids, action);
        
        if( dec != null && dec.getAuthorized() == Authorization.AUTHORIZED ){
            allowedAccessTypeArray = new EditLinkAccess[1];
            allowedAccessTypeArray[0] = EditLinkAccess.ADDNEW;
        }else
            allowedAccessTypeArray = NO_ACCESS;
        
        return allowedAccessTypeArray;
    }

    protected EditLinkAccess[] policyToAccess( IdentifierBundle ids, PolicyIface policy, ObjectPropertyStatement item){
        ArrayList<EditLinkAccess> list = new ArrayList<EditLinkAccess>(2);
        
        RequestedAction action = new EditObjPropStmt( item );
        PolicyDecision dec = policy.isAuthorized(ids, action);        
        if( dec != null && dec.getAuthorized() == Authorization.AUTHORIZED ){
            list.add( EditLinkAccess.MODIFY);
        }
        
        action = new DropObjectPropStmt(item.getSubjectURI(), item.getPropertyURI(), item.getObjectURI());
        dec = policy.isAuthorized(ids, action);        
        if( dec != null && dec.getAuthorized() == Authorization.AUTHORIZED ){
            list.add( EditLinkAccess.DELETE );
        }                        
        return list.toArray(ACCESS_TEMPLATE);
    }

    protected EditLinkAccess[] policyToAccess( IdentifierBundle ids,  PolicyIface policy, DataPropertyStatement item) {
        ArrayList<EditLinkAccess> list = new ArrayList<EditLinkAccess>(2);
        
        RequestedAction action = new EditDataPropStmt( item );
        PolicyDecision dec = policy.isAuthorized(ids, action);        
        if(  dec != null && dec.getAuthorized() == Authorization.AUTHORIZED ){
            list.add( EditLinkAccess.MODIFY);
        }
        
        action = new DropDataPropStmt( item );
        dec = policy.isAuthorized(ids, action);        
        if( dec != null && dec.getAuthorized() == Authorization.AUTHORIZED ){
            list.add( EditLinkAccess.DELETE );
        }                        
        return list.toArray(ACCESS_TEMPLATE);
    }

    protected EditLinkAccess[] policyToAccess( IdentifierBundle ids, PolicyIface policy, String subjectUri, DataProperty item) {
       
        String propertyUri = item.getURI();
        return policyToAccess(ids, policy, subjectUri, propertyUri);
    }

    protected EditLinkAccess[] policyToAccess( IdentifierBundle ids, PolicyIface policy, String subjectUri, String propertyUri) {
  
        EditLinkAccess[] access;
  
        RequestedAction action = new AddDataPropStmt(subjectUri, propertyUri, RequestActionConstants.SOME_LITERAL, null, null);
        PolicyDecision dec = policy.isAuthorized(ids, action);
        
        if(  dec != null && dec.getAuthorized() == Authorization.AUTHORIZED ){
            access = new EditLinkAccess[1];
            access[0] = EditLinkAccess.ADDNEW;
        }else
            access = NO_ACCESS;
        
        return access;
    }
    
    public enum EditLinkAccess{ MODIFY, DELETE, ADDNEW, INFO, ADMIN  };

    public class LinkStruct {
        String href;
        String type;
        String mouseoverText;
        
        public String getHref() {
            return href;
        }
        public void setHref(String href) {
            this.href = href;
        }
        public String getType() {
            return type;
        }
        public void setType(String type) {
            this.type = type;
        }
        
        public String getMouseoverText() {
            return mouseoverText;
        }
        
        public void setMouseoverText(String s) {
            mouseoverText = s;
        }
        
    }

    private LinkStruct[] empty_array = {};
    
    private LinkStruct getImageLink(String subjectUri, String contextPath, String action) {
        LinkStruct ls = new LinkStruct();
        String url = makeRelativeHref(contextPath + "uploadimages.jsp",
                                      "entityUri", subjectUri);
        ls.setHref(url);
        ls.setType(action);
        ls.setMouseoverText("upload a  new image");        
        return ls;
    }
    
    private String getObjPropMouseoverLabel(String propertyUri) {
        String mouseoverText = "relationship"; // default

        if (StringUtils.equalsOneOf(propertyUri, VitroVocabulary.ADDITIONAL_LINK, VitroVocabulary.PRIMARY_LINK)) {
            mouseoverText = "link";
        } 
        
        return mouseoverText;
    }

}
