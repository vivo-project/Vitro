<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<!doctype html public "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@ taglib uri='WEB-INF/tlds/database.tld' prefix='database'%>
<%@ page import="java.util.*,java.io.*,javax.xml.parsers.*,java.net.*,org.gnu.stealthp.rsslib.*" %>
<%
/**
 * @version 1 2005-08-04
 * @author Jon Corson-Rikert
 *
 * CHANGE HISTORY
 *	2005-08-18 jc55  added "raw" mode for inclusion in tabs
 */

final int DEFAULT_PORTAL_ID=1;
String portalIdStr=(portalIdStr=request.getParameter("home"))==null?String.valueOf(DEFAULT_PORTAL_ID): portalIdStr.equals("")?String.valueOf(DEFAULT_PORTAL_ID):portalIdStr;
int incomingPortalId=Integer.parseInt(portalIdStr);

final String DEFAULT_RSS_URL="http://www.nsf.gov/mynsf/RSS/rss2news.xml"; //http://www.nsf.gov/mynsf/RSS/rss2discoveries.xml
boolean includeHeaders=true;
String rawStr=(rawStr=request.getParameter("raw"))==null?"false": rawStr.equals("")?"false":rawStr;
if (rawStr!=null && rawStr.equalsIgnoreCase("true")) {
	includeHeaders=false;
}
if (includeHeaders) {%>
	<html>
	<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
	<link rel="stylesheet" type="text/css" href="css/edit.css">
	<title>RSS</title>
<%	final String DEFAULT_APPNAME="RSS";
	final String DEFAULT_STYLESHEET="portal";
	String appName=DEFAULT_APPNAME;
	String styleSheet=DEFAULT_STYLESHEET;%>
	<database:query id="portal" scope="page">
		SELECT appName,styleSheet FROM portals WHERE id='<%=portalIdStr%>'
	</database:query>
	<database:rows query="portal">
<%		int portal_col=0; %>
		<database:columns query="portal" id="theValue">
<%			switch (portal_col) {
				case 0: appName=theValue; break;
				case 1: styleSheet="portal"; break; 
			}
			++portal_col; %>
		</database:columns>
	</database:rows>
	<database:release query="portal" />
	</head>
	<body><%
}%>
<table width="90%" border="0" cellspacing="0" cellpadding="0" align="center">
<%
if (includeHeaders) {%>
<tr><td colspan="7">
	<jsp:include page="header.jsp" flush="true" >
		<jsp:param name="home" value="<%=portalIdStr%>" />
	</jsp:include>
</td>
</tr>
<tr><td colspan="7" height="1"><img src="site_icons/transparent.gif" width="100%" height="1" border="0"></td></tr><%
} %>
<tr><td colspan="7">
<%	String noisyStr=(noisyStr=(String)request.getAttribute("noisy"))==null || noisyStr.equals("")?((noisyStr=request.getParameter("noisy"))==null || noisyStr.equals("")?"false":noisyStr):noisyStr;
	boolean NOISY=(noisyStr.equalsIgnoreCase("true"))?true:false;
	String urlStr=(urlStr=(String)request.getAttribute("url"))==null || urlStr.equals("")?((urlStr=request.getParameter("url"))==null || urlStr.equals("")?DEFAULT_RSS_URL:urlStr):urlStr;
	if (urlStr==null || urlStr.equals("")) {%>
		<h3>Error in URL parameter</h3>
		<p>System could not decode <%=urlStr%> as a URL</p>
<%	} else {
		URL u=null;
		try {
			u = new URL(urlStr);
			RSSHandler hand=new RSSHandler();
			try {
				RSSParser.parseXmlFile(u,hand,false);
				RSSChannel ch=	hand.getRSSChannel();%>
				<h3><%=ch.getTitle()%></h3>
<%				String lastDateStr=ch.getLastBuildDate();
				if (lastDateStr==null || lastDateStr.equals("")) {
					RSSDoublinCoreModule dcModule=ch.getRSSDoublinCoreModule();
					if (dcModule!=null){
						lastDateStr=dcModule.getDcDate();
						if (lastDateStr!=null && !lastDateStr.equals("")){ 
							int timeStartPos=lastDateStr.indexOf("T");
							int timeEndPos=lastDateStr.indexOf("Z");
							if (timeStartPos>0 && timeEndPos>0){ %>
								<p><i>listings current as of <%=lastDateStr.substring(0,timeStartPos)%> at <%=lastDateStr.substring(timeStartPos+1,timeEndPos)%></i></p>
<%							} else {%>
								<p><i>listings current as of: <%=lastDateStr%></i></p>
<%							}
						} else {%>
							<p>RSSDoublinCoreModule.getDcDate() returns null or blank String</p>
<%						}
					} else {%>
						<p>RSSDoublinCoreModule is null</p>
<%					}
				}
				String copyrightStr=ch.getCopyright();
				if (copyrightStr!=null && !copyrightStr.equals("")){%>
					<p><%=ch.getCopyright()%></p>
<%				}
				if (NOISY && ch.getRSSImage()!=null) {%>
					<p>IMAGE INFO:<br/><%=ch.getRSSImage().toString()%></p>
					<p>IMAGE IN HTML:<br/><%=ch.getRSSImage().toHTML()%></p>
<%				} else if (NOISY) {%>
					<p>CHANNEL HAS NO IMAGE</p>
<%				}

				if (NOISY && ch.getRSSTextInput()!=null) {%>
					<p>INPUT INFO:<br/><%=ch.getRSSTextInput().toString()%></p>
					<p>HTML INPUT:<br/><%=ch.getRSSTextInput().toHTML()%></p>
<%				} else if (NOISY) {%>
					<p>CHANNEL HAS NO FORM INPUT</p>
<%				}

				if (NOISY && ch.getDoublinCoreElements()!=null) {%>
					<p>DUBLIN CORE INFO:<br/><%=ch.getDoublinCoreElements().toString()%></p>
<%				} else if (NOISY) {%>
					<p>CHANNEL HAS NO DUBLIN CORE TAGS</p>
<%				}%>

<%				if (NOISY) {%>
					<h3>SEQUENCE INFO</h3>
<%					if (ch.getItemsSequence()!=null) {%>
						<p><%=hand.getRSSChannel().getItemsSequence().toString()%></p>
<%					} else if (NOISY) {%>
						<p>CHANNEL HAS NO SEQUENCE; MAYBE VERSION 0.9 or 2+?</p>
<%					}
				}
				LinkedList lst = hand.getRSSChannel().getItems();%>
				<h3>ITEMS INFO (<%=lst.size()%>)</h3>
				<ul>
<%				for (int i = 0; i < lst.size(); i++){
					RSSItem itm = (RSSItem)lst.get(i);%>
					<li><%=itm.toString()%></li>
<%					if (itm.getDoublinCoreElements()!=null) {%>
						<br/>DUBLIN CORE INFO FOR ITEM: <%=itm.getDoublinCoreElements().toString()%>
<%					} else if (NOISY) {%>
						<br/>ITEM HAS NO DUBLIN CORE TAGS
<%					}%>
					</li>
<%				} // end for
			} catch (org.gnu.stealthp.rsslib.RSSException ex) {%>
				<h3>error initializing RSSHandler</h3>
				<p><%=ex.getMessage()%></p>
<%			}
		} catch (java.net.MalformedURLException ex) {%>
			<h3>Error in URL parameter</h3>
			<p>System could not convert <%=urlStr%> to a Java URL: <%=ex.getMessage()%></p>
<%		}

	} // end else URLstr not null or blank%>
</td>
</tr>
<%
if (includeHeaders) {%>
<tr><td colspan="7">
	<jsp:include page="footer.jsp" flush="true">
		<jsp:param name="home" value="<%=portalIdStr%>" />
	</jsp:include>
	</td>
</tr><%
}%>
</table><%
if (includeHeaders) {%>
</body>
</html><%
}%>
