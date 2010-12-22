<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="com.hp.hpl.jena.rdf.model.ModelMaker"%>
<%@ page import="com.hp.hpl.jena.rdf.model.Model"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="java.net.URLEncoder"%>
<%@ page import="java.util.List"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>

<script type="text/javascript" src="js/jquery.js"></script>
<script type="text/javascript" src="js/jenaIngest/ingestUtils.js"></script>

<h2><a class="ingestMenu" href="ingest">Ingest Menu</a> > Change Namespace of Resources</h2>

<p>This tool will change all resources in the supplied "old namespace" 
to be in the "new namespace."  Additionally, the local names will be updated
to follow the established "n" + random integer naming convention.</p>

<p>This tool operates on the main web application model only, not on any 
   of the additional Jena models.</p>

<form id="takeuri" action="ingest" method="get">
<input type="hidden" name="action" value="renameResource"/>
<p>Old Namespace  <input id="uri1" type="text" size="52" name="uri1"/></p>
<p>New Namespace  <input id="uri2" type="text" size="52" name="uri2"/></p>
<p><input id="submit" type="submit" name="submit" value="Change namespace" /></p>
</form>