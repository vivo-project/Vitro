<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@taglib prefix="vitro" uri="/WEB-INF/tlds/VitroUtils.tld" %>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.UseAdvancedDataToolsPages" %>
<% request.setAttribute("requestedActions", new UseAdvancedDataToolsPages()); %>
<vitro:confirmAuthorization />

    <h2><a class="ingestMenu" href="ingest">Ingest Menu</a> > Create New Model</h2>

    <form style="margin-bottom:2ex;" action="ingest" method="post">
        <input type="hidden" name="action" value="createModel"/>
	Model name: <input type="text" size="32" name="modelName"/>
        <input class="submit" type="submit" name="submit" value="Create Model"/>
        <input type="hidden" name="modelType" value="${modelType}"/>
    </form>
