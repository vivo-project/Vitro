<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="com.hp.hpl.jena.rdf.model.ModelMaker"%>
<%@ page import="com.hp.hpl.jena.rdf.model.Model"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="java.net.URLEncoder"%>
<%@ page import="java.util.List"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>

<script type="text/javascript" src="js/jquery.js"></script>
<script type="text/javascript" src="js/jenaIngest/ingestUtils.js"></script>

<p><a href="ingest">Ingest Home</a></p>

<h2>Rename Resource</h2>
<form id="takeuri" action="ingest" method="get">
<input type="hidden" name="action" value="renameResource"/>
<p>Individual URI1  <input id="uri1" type="text" size="52" name="uri1"/></p>
<p>Individual URI2  <input id="uri2" type="text" size="52" name="uri2"/></p>
<p><input type="submit" name="submit" value="submit" /></p>
</form>