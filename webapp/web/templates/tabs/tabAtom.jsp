<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page errorPage="/error.jsp"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.web.TabWebUtil" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Tab" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.VitroRequest" %>
<%@ page import="java.util.Collection" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Individual" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Link" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="org.joda.time.format.DateTimeFormatter" %>
<%@ page import="org.joda.time.format.ISODateTimeFormat" %>
<%@ page import="org.joda.time.DateTime" %>
<% 
/******************************
Takes tab and populates Atom feed with:
	requested tab and its description
	entities associated with this tab
	immediate children of this tab and their descriptions
******************************/
response.setContentType("application/atom+xml");
Tab leadingTab = (Tab) request.getAttribute("leadingTab");
if (leadingTab == null) {
    String e = "tabprimary expects that request attribute 'leadingTab' be set to a TabBean object";
    throw new JspException(e);
}
Collection<Individual> individuals = leadingTab.getRelatedEntities();
Collection<Tab> tabs = leadingTab.getChildTabs();

int portalId = 1;

DateTime dt = new DateTime();
DateTimeFormatter dtf = ISODateTimeFormat.basicDateTimeNoMillis();
String time = dtf.print(dt.getMillis());
time = time.substring(0,4)+"-"+time.substring(4,6)+"-"+time.substring(6,11)+":"+time.substring(11,13)+":"+time.substring(13,15)+"Z";
%>
<%!
public String forURL(String frag)
{
        String result = null;
        try 
        {
                result = URLEncoder.encode(frag, "UTF-8");
    } catch (Exception ex) {
        throw new RuntimeException("UTF-8 not supported", ex);
    }
        return result;
}

public String escapeEntity(String frag)
{
        if(frag.contains("&")) frag = replaceAll(frag, "&", "&amp;");
        if(frag.contains("'")) frag = replaceAll(frag, "'", "&apos;");
        if(frag.contains("\"")) frag = replaceAll(frag, "\"", "&quot;");
        if(frag.contains(">")) frag = replaceAll(frag, ">", "&gt;");
        if(frag.contains("<")) frag = replaceAll(frag, "<", "&lt;");
        return frag;
}

public String replaceAll(String original, String match, String replace)
{
	int index1 = original.indexOf(match);
	int index2 = original.indexOf(replace);
	if(index1 == index2 && index1 != -1) 
		{
			original = original.substring(0, index1+replace.length()+1)+replaceAll(original.substring(index1+replace.length()+1), match, replace);
			return original;
		}
	if(index1 == -1) return original;
	String before = original.substring(0, index1) + replace;
	return before + replaceAll(original.substring(index1+1), match, replace);
}
%>

<?xml version="1.0" encoding="utf-8"?> 
<feed xmlns="http://www.w3.org/2005/Atom" xml:lang="en" xml:base="http://<%= request.getServerName()+":"+request.getLocalPort() %>">
        <title><%= escapeEntity(leadingTab.getTitle()) %></title>
        <subtitle><%= leadingTab.getDescription() %></subtitle>
        <link href="<%= "http://"+request.getServerName()+":"+request.getLocalPort()+request.getContextPath()+escapeEntity("/index.jsp?home="+portalId+"&"+leadingTab.getTabDepthName()+"="+leadingTab.getTabId()+"&view=atom") %>" rel="self" type="application/atom+xml" />
        <link href="<%= "http://"+request.getServerName()+":"+request.getLocalPort()+request.getContextPath()+escapeEntity("/index.jsp?home="+portalId+"&"+leadingTab.getTabDepthName()+"="+leadingTab.getTabId()) %>" rel="alternate" type="text/html" />
        <id><%= "http://"+request.getServerName()+":"+request.getLocalPort()+request.getContextPath()+escapeEntity("/index.jsp?home="+portalId+"&"+leadingTab.getTabDepthName()+"="+leadingTab.getTabId()) %></id>
        <updated><%= time %></updated>
        <author>
                <name>Vivo</name>
                <email>vivo@cornell.edu</email>
        </author>
	<% for(Individual i:individuals) { %>
        <entry>
                <title><%= escapeEntity(i.getName()) %></title>
            <% if (i.getLinksList() == null) { %>
	        	<% for(Link l: i.getLinksList()) { %>
	                <link href="<%= l.getUrl() %>" rel="alternate" type="text/html" />
	            <% } %>
	        <% } else { %>
	        	<link href="<%= escapeEntity(request.getContextPath().substring(1)+"/entity?home="+portalId+"&uri="+forURL(i.getURI())) %>" rel="alternate" type="text/html" />
	        <% } %>
                <id><%= i.getURI() %></id>
                <updated><%= time %></updated>
            <% if (i.getBlurb() != null) { %>
                <% if (i.getBlurb().matches(".*<.*>.*")) { %>
                	<summary type="xhtml"><div xmlns="http://www.w3.org/1999/xhtml"><%= i.getBlurb() %></div></summary>
                <% } %>
                <% else { %>
                	<summary type="text"><%= i.getBlurb() %></summary>
                <% } %>
            <% } %>
            <% if (i.getDescription() != null) { %>
                <content type="xhtml"><div xmlns="http://www.w3.org/1999/xhtml"><%= i.getDescription() %></div></content>
            <% } %>
        </entry>
	<% } %>
	<% if (tabs != null) { %>
		<% for(Tab t:tabs) { %>
	        <entry>
	                <title><%= escapeEntity(t.getTitle()) %></title>
					<link href="<%= escapeEntity(request.getContextPath().substring(1)+"/index.jsp?home="+portalId+"&"+t.getTabDepthName()+"="+t.getTabId()) %>" rel="alternate" type="text/html" />
	                <id><%= "http://"+request.getServerName()+":"+request.getLocalPort()+request.getContextPath()+escapeEntity("/index.jsp?home="+portalId+"&"+t.getTabDepthName()+"="+t.getTabId()) %></id>
	                <updated><%= time %></updated>
	            <% if (t.getDescription() != null) { %>
	                <% if (t.getDescription().matches(".*<.*>.*")) { %>
	                	<summary type="xhtml"><div xmlns="http://www.w3.org/1999/xhtml"><%= t.getDescription() %></div></summary>
	                <% } %>
	                <% else { %>
	                	<summary type="text"><%= t.getDescription() %></summary>
	                <% } %>
	            <% } %>
	    		<% if (t.getBody() != null) { %>
	                <content type="xhtml"><div xmlns="http://www.w3.org/1999/xhtml"><%= t.getBody() %></div></content>
	            <% } %>
	        </entry>
		<% } %>
	<% } %>
</feed>
