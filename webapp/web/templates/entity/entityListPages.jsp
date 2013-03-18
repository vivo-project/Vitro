<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><%/* this odd thing points to something in web.xml */ %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%-- Show pages to select from --%>
<%  
if( request.getAttribute("alpha") != null && ! "all".equalsIgnoreCase((String)request.getAttribute("alpha"))) {  
  request.setAttribute("pageAlpha",request.getAttribute("alpha"));
}else{
  request.setAttribute("pageAlpha",request.getAttribute("all"));
}
%>

<c:if test="${ requestScope.showPages }">
    <div class="searchpages minimumFontMain">    
    
    Pages:
    <c:forEach items='${requestScope.pages }' var='page'>
       <c:url var='pageUrl' value=".${requestScope.servlet}">                           
         <c:param name="page">${page.index}</c:param>        
         <c:if test="${not empty requestScope.alpha}">       
           <c:param name="alpha">${requestScope.pageAlpha}</c:param>
          </c:if>
       </c:url>
       <c:if test="${ page.selected }">
         ${page.text}
       </c:if>
       <c:if test="${ not page.selected }">
         <a class="minimumFontMain" href="${pageUrl}&amp;${requestScope.controllerParam}">${page.text} </a>    
       </c:if>   
    </c:forEach>
    </div>
</c:if>