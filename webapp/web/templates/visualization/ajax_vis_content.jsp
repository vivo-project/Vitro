<%@ page import="edu.cornell.mannlib.vitro.webapp.visualization.personpubcount.VisVOContainer" %>

<c:set var='sparkline' value='${requestScope.sparklineVO}'/>

${sparkline.sparklineContent}
${sparkline.sparklineContext}