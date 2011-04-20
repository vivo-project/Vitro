<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@page
   import="edu.cornell.mannlib.vitro.webapp.auth.identifier.RequestIdentifiers"%>
<%@page
   import="java.util.List"%>

<%-- doesn't use vitro:requiresAuthorizationFor becuase the we want to be able to see IDs for any user. --%>
<%-- uses "security through obscurity", and doesn't give away much information. --%>

<%
      List idb = RequestIdentifiers.getIdBundleForRequest(request);

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
