<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://vitro.mannlib.cornell.edu/vitro/tags/PropertyEditLink" prefix="edLnk" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.VitroRequest" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.filters.VitroRequestPrep" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Individual" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.KeywordProperty" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.KeywordDao" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Keyword" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.KeywordIndividualRelation" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.KeywordIndividualRelationDao" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.Controllers" %>
<%@ page import="org.apache.commons.logging.Log" %>
<%@ page import="org.apache.commons.logging.LogFactory" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>
<%@ taglib uri="http://vitro.mannlib.cornell.edu/vitro/tags/StringProcessorTag" prefix="p" %>
<%@ taglib prefix="vitro" uri="/WEB-INF/tlds/VitroUtils.tld" %>

<%! 
public static Log log = LogFactory.getLog("edu.cornell.mannlib.vitro.webapp.jsp.templates.entity.entityKeywordsList.jsp");
%>

<vitro:confirmLoginStatus level="CURATOR" />

<%
Individual ent = (Individual)request.getAttribute("entity");
if (ent==null) {
    log.error("No incoming entity in entityKeywordsList.jsp");
}
VitroRequest vreq = new VitroRequest(request);
KeywordProperty kProp = new KeywordProperty("has keyword","keywords",0,null);
WebappDaoFactory wdf = vreq.getWebappDaoFactory();
KeywordIndividualRelationDao kirDao = wdf.getKeys2EntsDao();
KeywordDao kDao = wdf.getKeywordDao();
List<KeywordIndividualRelation> kirs = kirDao.getKeywordIndividualRelationsByIndividualURI(ent.getURI());
if (kirs != null) {
    int keyCount=0;
    Iterator kirIt = kirs.iterator();
    while (kirIt.hasNext()) {
        KeywordIndividualRelation kir = (KeywordIndividualRelation) kirIt.next();
        if (kir.getKeyId() > 0) {
            Keyword k = kDao.getKeywordById(kir.getKeyId());
            if (k != null) {
                ++keyCount;
				if (keyCount==1) {%>
					<h3 class="propertyName">Keywords</h3>
                    <div class="datatypePropertyValue">
<%				} else { %>
					<c:out value=", "/>
<%				}%>
                <c:out value="<%=k.getTerm()%>"/>
<% 			}
        }
    }%>
    </div><%
}
%>

