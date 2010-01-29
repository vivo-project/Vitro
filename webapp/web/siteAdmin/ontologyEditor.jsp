<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.jena.pellet.PelletListener"%>

<% if (securityLevel >= loginHandler.CURATOR) { %>
    
        <div class="pageBodyGroup">
    
            <h3>Ontology Editor</h3>
<%                         
            Object plObj = getServletContext().getAttribute("pelletListener");
            if ( (plObj != null) && (plObj instanceof PelletListener) ) {
                PelletListener pelletListener = (PelletListener) plObj;
                if (!pelletListener.isConsistent()) {
%>
	                <p class="notice">
	                    INCONSISTENT ONTOLOGY: reasoning halted.
	                </p>
	                <p class="notice">
	                Cause: <%=pelletListener.getExplanation()%>
	                </p>
<%              } else if (pelletListener.isInErrorState()) { %>
					<p class="notice">
					    An error occurred during reasoning; 
                        reasoning has been halted.
                        See error log for details.
					</p>
	   
<%                }
            }
%>       
            <ul>
                <li><a href="listOntologies?home=<%=portal.getPortalId()%>">Ontology list</a></li>
            </ul>
        
            <h4>Class Management</h4>
            <ul>
                <li><a href="showClassHierarchy?home=<%=portal.getPortalId()%>">Class hierarchy</a></li> 
                <li><a href="listGroups?home=<%=portal.getPortalId()%>">Class groups</a></li>
            </ul>
        
            <h4>Property Management</h4>
            <ul>
                <li><a href="showObjectPropertyHierarchy?home=${portalBean.portalId}&amp;iffRoot=true">Object property hierarchy</a></li>
                <li><a href="showDataPropertyHierarchy?home=<%=portal.getPortalId()%>">Data property hierarchy</a></li>      
                <li><a href="listPropertyGroups?home=<%=portal.getPortalId()%>">Property groups</a></li>
            </ul>

            <c:set var="verbosePropertyListing" value="${verbosePropertyListing == true ? true : false}" />
            <form id="verbosePropertyForm" action="${Controllers.SITE_ADMIN}#verbosePropertyForm" method="get">
                <input type="hidden" name="verbose" value="${!verbosePropertyListing}" />
                <span>Verbose property display for this session is <b>${verbosePropertyListing == true ? 'on' : 'off'}</b>.</span>
                <input type="submit" value="Turn ${verbosePropertyListing == true ? 'off' : 'on'}" />
            </form>   

    </div>               

<%  } %>