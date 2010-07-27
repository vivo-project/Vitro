<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%-- used by RefactorOperationController.java --%>

<%@ page import="java.util.*, java.lang.String.*"%>
<%@ page import="edu.cornell.mannlib.vedit.beans.ButtonForm" %>

<%
if (request.getAttribute("title") != null) { %>
    <h2><%=request.getAttribute("title")%></h2><%
}

ArrayList<String> logResults = (ArrayList<String>)request.getAttribute("results");

%>

<div class="editingForm">
	<table style="margin-bottom:1.5ex;">
		<tr>
			<td>
			<%=logResults.get(logResults.size()-3)%>
			</td>
		</tr>
		<tr>
			<td>
			<%=logResults.get(logResults.size()-2)%>
			</td>
		</tr>
		<tr>
			<td>
			<%=logResults.get(logResults.size()-1)%>
			</td>
		</tr>
		<tr>
			<td><p></p></td>
		</tr>
		<tr>
			<td>
				Log:
			</td>
		</tr>
		<tr>
			<td><p></p></td>
		</tr>
		<%
		for(int i=0; i< logResults.size()-3; i++)
		{
			%><tr>
				<td><%=logResults.get(i)%></td>
				</tr>
				
		<%			
		}
	%>
	</table>
</div>
