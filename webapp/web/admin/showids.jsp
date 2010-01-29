<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@page
   import="edu.cornell.mannlib.vitro.webapp.auth.identifier.ServletIdentifierBundleFactory"%>
<%@page
   import="java.util.List"%>


<%
      List idb = ServletIdentifierBundleFactory.getIdBundleForRequest(request, session, application);

out.write("<html><body>");
out.write("<h2>Identifiers in effect: </h2>");
out.write("<p>This is a utility that shows which identifiers are in effect.</p>\n");
out.write("<table><tr><th>class</th><th>value</th></tr>\n");
for( Object id : idb ){
    out.write( "<tr>" );
    out.write( "<td>" + id.getClass().getName() + "</td>");
    out.write( "<td>" + id.toString() + "</td>" );
    out.write( "</tr>\n" );
}
out.write("</table>\n");
out.write("</body></html>");

%>
