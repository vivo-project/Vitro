<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="edu.cornell.mannlib.vitro.webapp.web.*" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.VitroRequest" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page errorPage="/error.jsp"%>
<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet" %>

<%  /***********************************************
         Display a single Page  in the most basic fashion.
         The html <HEAD> is generated followed by the banners and menu.
         After that the result of the jsp in the attribute bodyJsp is inserted.
         Finally comes the footer.
         
         request.attributes:                    
            "bodyJsp" - jsp of the body of this page.
            "title" - title of page
            "css" - optional additional css for page
            "scripts" - optional name of file containing <script> elements to be included in the page
            "bodyAttr" - optional attributes for the <body> tag, e.g. 'onload': use leading space
            
          Consider sticking < % = MiscWebUtils.getReqInfo(request) % > in the html output
          for debugging info.
                 
         bdc34 2006-02-03 created
        **********************************************/
        /*
        String e = "";
        if (request.getAttribute("bodyJsp") == null){
            e+="basicPage.jsp expects that request parameter 'bodyJsp' be set to the jsp to display as the page body.\n";
        }
        if (request.getAttribute("title") == null){
            e+="basicPage.jsp expects that request parameter 'title' be set to the title to use for page.\n";
        }
        if (request.getAttribute("css") == null){
            e+="basicPage.jsp expects that request parameter 'css' be set to css to include in page.\n";
        }
        if( e.length() > 0 ){
            throw new JspException(e);
        }
        */
%>


<% /* Prepare Freemarker components to allow .ftl templates to be included from jsp */
    FreemarkerHttpServlet.getFreemarkerComponentsForJsp(request);
%>

<!DOCTYPE html>
<html lang="en">
    <head>
        ${ftl_head}
        
        <c:if test="${!empty scripts}"><jsp:include page="${scripts}"/></c:if>
    </head>
    
    <body ${requestScope.bodyAttr}>
            ${ftl_identity}
            ${ftl_search}
            ${ftl_menu}
            
                <c:import url="${bodyJsp}"/>
            
            ${ftl_footer}

    </body>
</html>