<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<% if (securityLevel >= loginHandler.DBA) { %>
    <div class="pageBodyGroup">
    
        <h3>Advanced Data Tools</h3>
    
        <ul>
            <li><a href="ingest">Ingest tools</a></li>   
	        <li><a href="uploadRDFForm?home=<%=portal.getPortalId()%>">Add/Remove RDF data</a></li>
	        <li><a href="export?home=<%=portal.getPortalId()%>">RDF export</a></li>
	        <%-- <li><a href="refactorOp?home=<%=portal.getPortalId()%>&amp;modeStr=fixDataTypes">Datatype literal realignment</a></li> --%> 
	        <li><a href="admin/sparqlquery">SPARQL query</a></li> 
        </ul>
    </div>
<% } %> 
