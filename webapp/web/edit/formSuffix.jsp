<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.filters.VitroRequestPrep"%>

<c:set var='themeDir'><c:out value='${portalBean.themeDir}' /></c:set>
		</div> <!-- #content.form -->

     </div>
    <div class="push"></div>

    <jsp:include page="/${themeDir}jsp/footer.jsp" flush="true"/>

  </div><!-- end wrap -->

</body>
</html>