<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>


<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<%@ page import="com.hp.hpl.jena.rdf.model.Model"%>
<p><a href="ingest">Ingest Home</a></p>

<h2>Merge Individuals</h2>
<form action="ingest" method="get">
<input type="hidden" name="action" value="mergeResult"/>
<%String result = (String) request.getAttribute("result");%>
<%Model leftoverModel = (Model) request.getSession().getAttribute("leftoverModel"); 
 request.getSession().setAttribute("leftoverModel",leftoverModel);%>
<p><b><%=result%></b></p>
<input type="hidden" name="model" value="<%=leftoverModel%>"></input>
<%if(!result.equals("resource 1 not present") && !result.equals("resource 2 not present")){ %>
<TR>
                <br>
                 <p>Download statements that could not be merged.</p>
                 <p><input type="submit" name="Download" value="Download" ></input></p>
                <br>
  </TR>
<%} %>

</form>