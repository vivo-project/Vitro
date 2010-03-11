<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<% if (securityLevel >= loginHandler.DBA) { %>
    <div class="pageBodyGroup">
    
        <h3>Reports</h3>
    
        <ul>
            <li><a href="customsparql?queryType=fileupload">Custom Report: File Publication Date &gt; 1 YEAR AGO</a></li>   
            <li><a href="customsparql?queryType=filedelete">Custom Report: File Deleted &gt; 1 YEAR AGO</a></li>   
        </ul>
    </div>
<% } %> 
