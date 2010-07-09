<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="edu.cornell.mannlib.vitro.webapp.visualization.valueobjects.SparklineVOContainer" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>

<c:set var='sparkline' value='${requestScope.sparklineVO}'/>

${sparkline.sparklineContent}
${sparkline.sparklineContext}
