<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="com.hp.hpl.jena.rdf.model.ModelMaker" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.net.URLEncoder" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>

<%

    ModelMaker maker = (ModelMaker) request.getSession().getAttribute("vitroJenaModelMaker");
    if (maker == null) {
	maker = (ModelMaker) getServletContext().getAttribute("vitroJenaModelMaker");
    }

%>

    <p><a href="ingest">Ingest Home</a></p>

    <h2>Available Jena Models</h2>

    <form action="ingest" method="get">
        <input type="hidden" name="action" value="createModel"/>
        <input type="submit" name="submit" value="Create Model"/>
    </form>

    <ul>

<%
    for (Iterator it = maker.listModels(); it.hasNext(); ) {
	String modelName = (String) it.next();
        %> <li style="padding-bottom:2em;padding-top:2em;"> <%=modelName%>
            <table style="margin-left:2em;"><tr>
            <td>
            <form action="ingest" method="get">
                <input type="hidden" name="action" value="loadRDFData"/>
                <input type="hidden" name="modelName" value="<%=modelName%>"/>
                <input type="submit" name="submit" value="load RDF data"/>
            </form>
            </td>
            <td>
            <c:url var="outputModelURL" value="ingest">
            	<c:param name="action" value="outputModel"/>
            	<c:param name="modelName" value="<%=modelName%>"/>
            </c:url>
            <a href="${outputModelURL}">output model</a>
            </td>
            <td>
            <form action="ingest" method="post">
                <input type="hidden" name="action" value="clearModel"/>
                <input type="hidden" name="modelName" value="<%=modelName%>"/>
                <input type="submit" name="submit" value="clear statements"/>
            </form>
            </td>
            <td>
            <form action="ingest" method="post">
                <input type="hidden" name="action" value="removeModel"/>
                <input type="hidden" name="modelName" value="<%=modelName%>"/>
                <input type="submit" name="submit" value="remove"/>
            </form>
            </td>
            </tr>
            <tr>
            <td>
            <form action="ingest" method="post">
                <input type="hidden" name="action" value="setWriteLayer"/>
                <input type="hidden" name="modelName" value="<%=modelName%>"/>
                <input type="submit" name="submit" value="set as current write layer"/>
            </form>
            <td>
            <form action="ingest" method="post">
                <input type="hidden" name="action" value="attachModel"/>
                <input type="hidden" name="modelName" value="<%=modelName%>"/>
                <input type="submit" name="submit" value="attach to webapp"/>
            </form>
            </td>
            <td>
            <form action="ingest" method="post">
                <input type="hidden" name="action" value="detachModel"/>
                <input type="hidden" name="modelName" value="<%=modelName%>"/>
                <input type="submit" name="submit" value="detach from webapp"/>
            </form>
            </td>
            <td>
			<form action="ingest" method="get"><input type="hidden"
				name="action" value="permanentURI" /> <input type="hidden"
				name="modelName" value="<%=modelName%>" /> <input type="submit"
				name="submit" value="generate permanent URIs" /></form>
			</td>
            <td>&nbsp;</td>
            </tr>
            </table>
        </li> <%    
    }
%>
    </ul>
