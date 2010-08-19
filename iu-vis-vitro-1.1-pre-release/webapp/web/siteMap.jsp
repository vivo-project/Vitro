<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="java.util.*, java.lang.String.*"%>
<%@ page import="edu.cornell.mannlib.vedit.beans.ButtonForm" %>
<%@ page import="java.util.ArrayList" %>

<% 

	ArrayList<String> list = (ArrayList<String>)request.getAttribute("list");	
	String changeFrequency = "weekly";
	response.setContentType("application/xml");
%>

<?xml version="1.0" encoding="UTF-8"?>
<urlset xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://www.sitemaps.org/schemas/sitemap/0.9 http://www.sitemaps.org/schemas/sitemap/0.9/sitemap.xsd"
         xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">

<%
		// List Sitemaps for individuals
	for(int i=0; i<list.size(); i++)
	{
	%>
		<url>
			<loc><%= list.get(i) %></loc>
			<changefreq><%= changeFrequency %></changefreq>
		</url> 
<%	}
%>
</urlset>