<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<% if (loginBean.isLoggedInAtLeast(LoginStatusBean.CURATOR)) { %>
    <div class="pageBodyGroup">
    
        <h3>Site Configuration</h3>
    
        <ul>
            <c:if test="${requestScope.singlePortal == true }">
                <li><a href="editForm?home=<%=portal.getPortalId()%>&amp;controller=Portal&amp;id=<%=portal.getPortalId()%>">Site information</a></li>
            </c:if>      
            <c:if test="${requestScope.singlePortal == false }">
                <li><a href="editForm?home=<%=portal.getPortalId()%>&amp;controller=Portal&amp;id=<%=portal.getPortalId()%>">Current portal information</a></li>
                <li><a href="listPortals?home=<%=portal.getPortalId()%>">List all portals</a></li>
            </c:if>  
        
            <li><a href="listTabs?home=<%=portal.getPortalId()%>">Tab management</a></li>
            
<%          if (loginBean.isLoggedInAtLeast(LoginStatusBean.DBA)) { %>
                <li><a href="listUsers?home=<%=portal.getPortalId()%>">User accounts</a></li>    
<%          } %>      
        </ul>
    </div>
<% } %>  
  
  
               
