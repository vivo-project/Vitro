<%@ page import="edu.cornell.mannlib.vitro.webapp.visualization.personpubcount.VisVOContainer" %>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<c:set var='sparkline' value='${requestScope.sparklineVO}'/>

<div class="staticPageBackground">


<div id="vis_container">
${sparkline.sparklineContent}

</div>
${sparkline.sparklineContext}
</div>