<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.DataProperty" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Individual" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.VitroRequest" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditConfiguration" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.RdfLiteralHash" %>
<%@ page import="edu.cornell.mannlib.vedit.beans.LoginFormBean" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.filters.VitroRequestPrep" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.web.MiscWebUtils" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.Controllers" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Portal" %>
<%@ page import="java.util.HashMap" %>
<%
    org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog("edu.cornell.mannlib.vitro.webapp.jsp.edit.editDatapropStmtRequestDispatch");
    //Log log = LogFactory.getLog("edu.cornell.mannlib.vitro.webapp.jsp.edit.editDatapropStmtRequestDispatch");
%>
<%
    // Decide which form to forward to, set subjectUri, subjectUriJson, predicateUri, predicateUriJson in request
    // Also get the Individual for the subjectUri and put it in the request scope
    // If a datapropKey is sent it as an http parameter, then set datapropKey and datapropKeyJson in request, and
    // also get the DataPropertyStatement matching the key and put it in the request scope
    /* *************************************
    Parameters:
        subjectUri
        predicateUri
        datapropKey (optional)
        cmd (optional -- deletion)
        formParam (optional)
      ************************************** */

    final String DEFAULT_DATA_FORM = "defaultDatapropForm.jsp";
    final String DEFAULT_ERROR_FORM = "error.jsp";
   
    if (!VitroRequestPrep.isSelfEditing(request) && !LoginFormBean.loggedIn(request, LoginFormBean.NON_EDITOR)) {        
        %> <c:redirect url="<%= Controllers.LOGIN %>" /> <%  
    }

    VitroRequest vreq = new VitroRequest(request);
    if( EditConfiguration.getEditKey( vreq ) == null ){
        vreq.setAttribute("editKey",EditConfiguration.newEditKey(session));
    }else{
        vreq.setAttribute("editKey", EditConfiguration.getEditKey( vreq ));
    }

    String subjectUri   = vreq.getParameter("subjectUri");
    String predicateUri = vreq.getParameter("predicateUri");
    String formParam    = vreq.getParameter("editForm");
    String command      = vreq.getParameter("cmd");

    if( subjectUri == null || subjectUri.trim().length() == 0 ) {
        log.error("required subjectUri parameter missing");
        throw new Error("subjectUri was empty, it is required by editDatapropStmtRequestDispatch");
    }
    if( predicateUri == null || predicateUri.trim().length() == 0) {
        log.error("required subjectUri parameter missing");
        throw new Error("predicateUri was empty, it is required by editDatapropStmtRequestDispatch");
    }
    vreq.setAttribute("subjectUri", subjectUri);
    vreq.setAttribute("subjectUriJson", MiscWebUtils.escape(subjectUri));
    vreq.setAttribute("predicateUri", predicateUri);
    vreq.setAttribute("predicateUriJson", MiscWebUtils.escape(predicateUri));

    /* since we have the URIs let's put the individual, data property, and optional data property statement in the request */
    
    WebappDaoFactory wdf = vreq.getWebappDaoFactory();

    Individual subject = wdf.getIndividualDao().getIndividualByURI(subjectUri);
    if( subject == null ) {
        log.error("Could not find subject Individual '"+subjectUri+"' in model");
        throw new Error("editDatapropStmtRequest.jsp: Could not find subject Individual in model: '" + subjectUri + "'");
    }
    vreq.setAttribute("subject", subject);

    DataProperty dataproperty = wdf.getDataPropertyDao().getDataPropertyByURI( predicateUri );
    if( dataproperty == null ) {
        log.error("Could not find data property '"+predicateUri+"' in model");
        throw new Error("editDatapropStmtRequest.jsp: Could not find DataProperty in model: " + predicateUri);
    }
    vreq.setAttribute("predicate", dataproperty);

    String url= "/edit/editDatapropStmtRequestDispatch.jsp"; //I'd like to get this from the request but...
    vreq.setAttribute("formUrl", url + "?" + vreq.getQueryString());


    String datapropKeyStr = vreq.getParameter("datapropKey");
    int dataHash = 0;
    if( datapropKeyStr != null ){
        try {
            dataHash = Integer.parseInt(datapropKeyStr);
            vreq.setAttribute("datahash", dataHash);
            log.debug("Found a datapropKey in parameters and parsed it to int: " + dataHash);
         } catch (NumberFormatException ex) {
            throw new JspException("Cannot decode incoming datapropKey value "+datapropKeyStr+" as an integer hash in editDatapropStmtRequestDispatch.jsp");
        }
    }

    DataPropertyStatement dps = null;
    if( dataHash != 0) {
        dps = RdfLiteralHash.getDataPropertyStmtByHash( subject ,dataHash);

        if (dps==null) {
            log.error("No match to existing data property \""+predicateUri+"\" statement for subject \""+subjectUri+"\" via key "+datapropKeyStr);
            %><jsp:forward page="/edit/messages/dataPropertyStatementMissing.jsp"></jsp:forward> <%
            return;
        }                     
        vreq.setAttribute("dataprop", dps );
    }

    if( log.isDebugEnabled() ){
        log.debug("predicate for DataProperty from request is " + dataproperty.getURI() + " with rangeDatatypeUri of '" + dataproperty.getRangeDatatypeURI() + "'");
        if( dps == null )
            log.debug("no exisitng DataPropertyStatement statement was found, making a new statemet");
        else{
            log.debug("Found an existing DataPropertyStatement");
            String msg = "existing datapropstmt: ";
            msg += " subject uri: <"+dps.getIndividualURI() + ">\n";
            msg += " prop uri: <"+dps.getDatapropURI() + ">\n";
            msg += " prop data: \"" + dps.getData() + "\"\n";
            msg += " datatype: <" + dps.getDatatypeURI() + ">\n";
            msg += " hash of this stmt: " + RdfLiteralHash.makeRdfLiteralHash(dps);
            log.debug(msg);
        }
    }
    
    vreq.setAttribute("preForm", "/edit/formPrefix.jsp");
    vreq.setAttribute("postForm", "/edit/formSuffix.jsp");

    if( "delete".equals(command) ){ %>
        <jsp:forward page="/edit/forms/datapropStmtDelete.jsp"/>
<%      return;
    }

    if( formParam == null ){        
        String form = dataproperty.getCustomEntryForm();
        if (form != null && form.length()>0) {
            log.warn("have a custom form for this data property: "+form);
            vreq.setAttribute("hasCustomForm","true");
        } else {
            form = DEFAULT_DATA_FORM;
        }
        vreq.setAttribute("form" ,form);
    } else {
        vreq.setAttribute("form", formParam);
    }

    if( session.getAttribute("requestedFromEntity") == null )
        session.setAttribute("requestedFromEntity", subjectUri );
%>
<jsp:forward page="/edit/forms/${form}"  />
