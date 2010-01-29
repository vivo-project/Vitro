<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="java.util.*, java.lang.String.*"%>
<%@ page import="edu.cornell.mannlib.vedit.beans.ButtonForm" %>
<%@ page import="java.util.ArrayList" %>

<% 
	int individuals = (Integer)request.getAttribute("individuals");
	String context = (String)request.getAttribute("context");
	int numPortals = 16;
	response.setContentType("application/xml");
%>

<?xml version="1.0" encoding="UTF-8"?>
<sitemapindex xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://www.sitemaps.org/schemas/sitemap/0.9 http://www.sitemaps.org/schemas/sitemap/0.9/siteindex.xsd"
         xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">

<%
	// List Sitemaps for individuals
	for(int i=0; i<individuals; i++)
	{
	%>
		<sitemap>
			<loc>http://<%= context %>/sitemap.xml?entityListNum=<%= i %></loc>
		</sitemap>
	<%		
	}
	for(int i=0; i<numPortals; i++)
	{
	%>
		<sitemap>
			<loc>http://<%= context %>/sitemap.xml?tabsPortalNum=<%= i %></loc>
		</sitemap>
	<%		
	}
	%>
</sitemapindex>