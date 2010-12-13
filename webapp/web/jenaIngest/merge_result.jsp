<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>


<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<%@ page import="com.hp.hpl.jena.rdf.model.Model"%>
<h2><a href="ingest">Ingest Home</a></h2>

<h3>Merge Individuals</h3>
<form action="ingest" method="get">
<input type="hidden" name="action" value="mergeResult"/>
<%String result = (String) request.getAttribute("result");%>
<%Model leftoverModel = (Model) request.getSession().getAttribute("leftoverModel"); 
 request.getSession().setAttribute("leftoverModel",leftoverModel);%>
<p><b><%=result%></b></p>
<input type="hidden" name="model" value="<%=leftoverModel%>"></input>
<%if(!result.equals("resource 1 not present") && !result.equals("resource 2 not present")){ 
if(!result.equals("No statements merged") && !result.endsWith("statements.")){%>
<TR>
                <br>
                 <p>Download non-mergeable statements.</p>
                 <p><input id="submit"  type="submit" name="Download" value="Download" ></input></p>
                <br>
  </TR>
<%}} %>

</form>