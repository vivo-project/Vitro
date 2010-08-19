<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="java.util.*"%>
<%@ page import="java.net.URLEncoder"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.*" %>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %><%/* this odd thing points to something in web.xml */ %>
<%@ page errorPage="/error.jsp"%>
<%
/***********************************************
 Display a}x9n;:[
 Search Results

 request.attributes:
 a Map object with the name "collatedResultsLists"
 collatedGroupNames
 request.parameters:
 None yet.

 Consider sticking < % = MiscWebUtils.getReqInfo(request) % > in the html output
 for debugging info.

 **********************************************/
 if (request.getAttribute("collatedResultsLists") == null) {
     String e = "searchBaisc.jsp expects that request attribute " +
             "'collatedResultsLists' be set to a Map of Lists of results to display.";
     throw new JspException(e);
 }
 if (request.getAttribute("collatedGroupNames") == null) {
     String e = "searchBaisc.jsp expects that request attribute "
             + "'collatedGroupNames' be set to a list of keys in collatedResultsLists.";
     throw new JspException(e);
 }

 int switchdivs = 0; // for making IDs for the plus-icon expansion divs
 
 edu.cornell.mannlib.vitro.webapp.beans.Portal portal = (Portal) request.getAttribute("portalBean");
 int portalId = portal.getPortalId();

 Map results = (Map) request.getAttribute("collatedResultsLists");

 out.println("<div id='content' class='contentsBrowseGroup'>");

 //do classgroup toc
 Iterator it = results.keySet().iterator();
 out.println("<p class='searchTOC'>Jump to results of type: ");
 while (it.hasNext()) {
     Object key = it.next();
     VClassGroup grp = (VClassGroup) results.get(key);
     out.println(" <a href='#" + grp.getLocalName() + "'>" + key + "</a> ");
 }
 out.println(" </p>");

 //get each ClassGroup
 it = results.keySet().iterator();
 while (it.hasNext()) {
     Object key = it.next();
     VClassGroup grp = (VClassGroup) results.get(key);
     out.println("<h2 id='" + grp.getLocalName() + "'>" + key + "</h2>");
     
     //get each VClassList
     Iterator it2 = grp.iterator();
     while (it2.hasNext()) {
         VClassList vcl = (VClassList) it2.next();

         int resultSetSize = vcl.getEntities().size();
         int displayLimit = vcl.getDisplayLimit();
         if (resultSetSize - displayLimit == 1)
             ++displayLimit;
         boolean hiddenDivStarted = false;

         out.println("<h3>" + vcl.getName() + " (" + resultSetSize + ")</h3>");
         out.println("<ul>");

         List ents = vcl.getEntities();
         if (ents == null || ents.size() == 0)
             out.println("<li>none</li>");
         else {
			//get each entity
			Iterator it3 = ents.iterator();
			int count = 0;
			while (it3.hasNext()) {
				Individual ent = (Individual) it3.next();
				++count;
				String escapedURIStr = "";
				try {
					escapedURIStr = URLEncoder.encode(ent.getURI(),"UTF-8");
				} catch (Exception e) {
					/*unsupported encoding?*/
				}
				out.println("<li>");
				out.println("<a href='"
                    		 + response.encodeURL(request.getContextPath()+"/entity?uri=" + escapedURIStr + "&amp;home=" + portalId)
                    		 + "'>" + ent.getName().replaceAll("&","&amp;") + "</a>");
                if (ent.getMoniker() != null && ent.getMoniker().length() > 0) {
                    out.println(" | " + ent.getMoniker().replaceAll("&","&amp;"));
				}
                if (portal.getPortalId() == 6) { //show anchors in impact portal for submitter's name
                    if (ent.getAnchor() != null && ent.getAnchor().length() > 0) {
                         out.println(" | <span class='externalLink'>" + ent.getAnchor() + "</span>");
                 	}
				}
    		/*	if (portal.getAppName().equalsIgnoreCase("VIVO") || portal.getAppName().equalsIgnoreCase("Research")) {
                    //Medha's desired display
                    if (ent.getUrl() != null && ent.getUrl().length() > 0) {
                        out.println(" | <a class='externalLink' href='"
                        			+ response.encodeURL(ent.getUrl().replaceAll("&","&amp;")) + "'>"
                                 	+ ent.getAnchor().replaceAll("&","&amp;") + "</a>");
                     } else if (ent.getAnchor() != null && ent.getAnchor().length() > 0) {
                         out.println(" | <span class='externalLink'>" + ent.getAnchor().replaceAll("&","&amp;") + "</span>");
                     }
                     List linksList = ent.getLinksList();
                     if (linksList != null) {
                        Iterator lit = linksList.iterator();
                        while (lit.hasNext()) {
                            Link l = (Link) lit.next();
                             if (l.getUrl() != null && l.getUrl().length() > 0) {
                                out.println(" | <a class='externalLink' href='"
                                         	+ response.encodeURL(l.getUrl().replaceAll("&","&amp;")) + "'>"
                                         	+ l.getAnchor().replaceAll("&","&amp;") + "</a>");
                             } else {
                                out.println(" | <span class='externalLink'>" + l.getAnchor().replaceAll("&","&amp;") + "</span>");
                               }
                         }
                     }
				} else { //show the Google-like excerpt */                
                	if (ent.getDescription() != null && ent.getDescription().length() > 0) {
                        out.println("<div>" + ent.getDescription() + "</div>");
                 	}
             /*	} */
				out.println("</li>");
                int remaining = resultSetSize - count;
                if (count == displayLimit && remaining > 0) {
                	hiddenDivStarted = true; switchdivs++; %>
                  	</ul>
                  	<div style="color: black; cursor: pointer;" onclick="javascript:switchGroupDisplay('extra_ib<%=switchdivs%>','extraSw_ib<%=switchdivs%>','<%= response.encodeURL(portal.getThemeDir())%>site_icons')"
                         title="click to toggle additional entities on or off" class="navlinkblock" onmouseover="onMouseOverHeading(this)"
                         onmouseout="onMouseOutHeading(this)">                                   
                        <span class="resultsMoreSpan"><img src='<%= response.encodeURL( portal.getThemeDir() )+"site_icons/plus.gif"%>' id="extraSw_ib<%=switchdivs%>" alt="more results"/>
<%							out.println("<strong>"+remaining+" more</strong>"); %>
                        </span>
                  	</div>
<%					out.println("<div id='extra_ib"+switchdivs+"' style='display:none'>");
                  	out.println("  <ul>");
                }
                if ((count == resultSetSize) && (hiddenDivStarted)) {
                 	out.println("</ul></div> <!--  extra_ib"+switchdivs+"-->"); 
                }
			} // END while it3.hasNext()
            if (!hiddenDivStarted) {
                out.println("</ul>");
            }                                     
        } // END else have entities
    } // END while it2.hasNext()
} // END while it.hasNext()
%>
</div><!--contentsBrowseGroup-->
