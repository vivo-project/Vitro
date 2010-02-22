<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<% if (securityLevel >= loginHandler.CURATOR) { %>
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
            
<%          if (securityLevel >= loginHandler.DBA) { %>
                <li><a href="listUsers?home=<%=portal.getPortalId()%>">User accounts</a></li>    
                <li><a href="usermail?home=<%=portal.getPortalId()%>">Email All Users</a></li>
<%          } %>      
        </ul>
    </div>
<% } %>  
  
  
               
