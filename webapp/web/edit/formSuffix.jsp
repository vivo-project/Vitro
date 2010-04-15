<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.filters.VitroRequestPrep"%>

<c:set var='themeDir'><c:out value='${portalBean.themeDir}' /></c:set>
                </div> <!-- #content.form -->

            </div>
        <div class="push"></div>

        <jsp:include page="/${themeDir}jsp/footer.jsp" flush="true"/>

    </div><!-- end wrap -->

    <script language="javascript" type="text/javascript" src="../js/extensions/String.js"></script>
    <script language="javascript" type="text/javascript" src="../js/jquery.js"></script>
    <script language="javascript" type="text/javascript" src="../js/jquery_plugins/jquery.bgiframe.pack.js"></script>
    <script language="javascript" type="text/javascript" src="../js/jquery_plugins/thickbox/thickbox-compressed.js"></script>
    <!-- <script language="javascript" type="text/javascript" src="../js/jquery_plugins/ui.datepicker.js"></script> -->
    <script language="javascript" type="text/javascript" src="../js/jquery_plugins/jquery-autocomplete/jquery.autocomplete.pack.js"></script>
        
    <c:forEach var="jsFile" items="${customJs}">
        <script type="text/javascript" src="${jsFile}"></script>
    </c:forEach>  
    
</body>
</html>