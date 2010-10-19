<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="java.util.*"%>
<%@ page import="java.net.URLEncoder"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.*" %>
<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Portal" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ page errorPage="/error.jsp"%>

<%
/***********************************************
 Display Paged Search Results

 request.attributes:
   List object with the name "beans"
 request.parameters:
   None yet.
 ********************************************* */ 

 if (request.getAttribute("beans") == null) {
     String e = "searchBaisc.jsp expects that request attribute " +
             "'beans' be set to a List of Individuals to display.";
     throw new JspException(e);
 }
 Portal portal = (Portal) request.getAttribute("portalBean");
 String portalParm = "&amp;home=" + portal.getPortalId(); 
 


%>
  <div id='content'><!--  searchPaged.jsp --> 
  <h2>Search Results for '<c:out value="${querytext}"></c:out>'
  <c:if test="${! (empty requestScope.classgroupName)}"> 
    limited to type '${requestScope.classgroupName}'</c:if>
  <c:if test="${! (empty requestScope.typeName)}"> 
    limited to type '${requestScope.typeName}'</c:if>
  </h2>
  <div class='contentsBrowseGroup'>
<%

 if( request.getAttribute("classgroups") != null ){
     %> <div class="searchTOC"><span class="jumpText">Show only results of this <b>type</b>:</span> <%      
     List groups = (List) request.getAttribute("classgroups");
     Iterator it = groups.iterator();
     
     while(it.hasNext()){
         VClassGroup grp = (VClassGroup)it.next();
         out.println("<a href='"
                    + "./search?querytext=" 
                    + URLEncoder.encode(request.getParameter("querytext"))
                    + "&classgroup=" 
                    + URLEncoder.encode(grp.getURI())  
                 + "'>");
         out.println(StringEscapeUtils.escapeHtml(grp.getPublicName()));
         out.println("</a>");         
     }
     out.println("</div>");
 }
    

if( request.getAttribute("types") != null ){
    %> <div class="searchTOC"><span class="jumpText">Show only results of this <b>sub-type</b>:</span> <%      
    List types = (List) request.getAttribute("types");
    Iterator it = types.iterator();
    
    while(it.hasNext()){
        VClass type = (VClass)it.next();
        out.println("<a href='"
                   + "./search?querytext=" 
                   + URLEncoder.encode(request.getParameter("querytext"))
                   + "&type=" 
                   + URLEncoder.encode(type.getURI())  
                + "'>");
        out.println(StringEscapeUtils.escapeHtml(type.getName()));
        out.println("</a>");         
    }
    out.println("</div>");
}

 /* generate search result list */
 List beans = (List) request.getAttribute("beans");
 Iterator it = beans.iterator();
 out.println("<ul class='searchhits'>");
 
 while (it.hasNext()) {         
    Individual ent = (Individual) it.next();                
    String escapedURIStr = "";
    try {
        escapedURIStr = URLEncoder.encode(ent.getURI(),"UTF-8");
    } catch (Exception e)  {  } // unsupported encoding?
        
    out.println("<li>");
    out.print("<a href='"
               + response.encodeURL(
                       request.getContextPath()
                       +"/entity?uri=" + escapedURIStr 
                       + portalParm )
               + "'>" 
               + StringEscapeUtils.escapeHtml( ent.getName() )
               + "</a> ");
    
    if (ent.getMoniker() != null && ent.getMoniker().length() > 0) 
        out.println(" | " + StringEscapeUtils.escapeHtml(ent.getMoniker()));        
      
    // For now, custom search views just add additional information to the name and moniker
    String searchViewPrefix = "/templates/search/";
    String customSearchView = null;
    for (VClass type : ent.getVClasses(true)) { // first get directly asserted class(es)
        if (type!=null) {
            customSearchView = type.getCustomSearchView();
            if (customSearchView!=null && customSearchView.length()>0 ) {
                // NOTE we are NOT putting "individualURL" in the request scope
                // An included custom search view jsp can optionally implement a test for "individualURL"
                // as a way to optionally render additional text as a link
                // SEE entityList.jsp and searchViewWithTimekey.jsp as an example
                request.setAttribute("individual",ent); %>
                | <jsp:include page="<%=searchViewPrefix+type.getCustomSearchView()%>"/>
<%                          request.removeAttribute("individual");
                // TODO: figure out which of the directly asserted classes should have precedence; for now, just take the 1st
                break; // have to break because otherwise customSearchView may get reset to null and trigger more evaluation
            }
        }
    }
    if (customSearchView == null ) { // try inferred classes, too
        for (VClass type : ent.getVClasses()) {
            if (type!=null) {
                customSearchView = type.getCustomSearchView();
                if (customSearchView!=null && customSearchView.length()>0 ) {
                    // SEE NOTE just above
                    request.setAttribute("individual",ent); 
                    %><jsp:include page="<%=searchViewPrefix+type.getCustomSearchView()%>"/><%
                    request.removeAttribute("individual");
                    //TODO: figure out which of the inferred classes should have precedence; for now, just take the 1st
                    break;
                }
            }
        }
    }
    
    if (ent.getDescription() != null && ent.getDescription().length() > 0) {
        out.println("<div class='searchFragment'>" + ent.getDescription() + "</div>");
    }
        
    out.println("</li>");     
 } // END while it.hasNext()
 out.println("</ul>");
 
 /* generate pageing list */
 int startIndex = (Integer)request.getAttribute("startIndex");
 int hitsPerPage = (Integer)request.getAttribute("hitsPerPage");
 int hitsLength = (Integer)request.getAttribute("hitsLength");
 int maxHitSize = (Integer)request.getAttribute("maxHitSize");
 String basePageUrl =  
     request.getContextPath() + "/search?querytext="
         +URLEncoder.encode(request.getParameter("querytext"),"UTF-8") +
         request.getAttribute("refinement");
 
 out.println("<div class='searchpages'>");
 out.println("Pages:");
 for(int i=0; i<hitsLength; i=i+hitsPerPage){     
     if( i < maxHitSize - hitsPerPage){
         String classCurrentPage = i >= (startIndex) && i < (startIndex+ hitsPerPage)?"class='currentPage'":"";                 
         out.println("<a "+classCurrentPage+" href='"+ basePageUrl 
                 + "&startIndex="+ i + "&hitsPerPage=" + hitsPerPage 
                 + "'>" + ((i/hitsPerPage) + 1) + "</a>");         
     }else{         
         out.println("<a class='moreHits' href='"+ basePageUrl 
                 + "&startIndex="+ i + "&hitsPerPage=" + hitsPerPage 
                 + "'>more...</a>");
         break;
     }
 }
 out.println("</div>");
%>
</div>
</div><!--content from searchPaged.jsp -->