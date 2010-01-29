<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%

request.setAttribute("dwrDisabled", new Boolean(false));
String context = request.getContextPath();

%>


<%
if (!(Boolean)request.getAttribute("dwrDisabled")) {
%>

<script type="text/javascript" xml:space="preserve">
    var gEntityURI="${entity.URI}";
</script> <!-- There has got to be a better way to pass this to the js -->

<script type='text/javascript' src='<%=context%>/dwr/interface/PropertyDWR.js'></script>
<script type='text/javascript' src='<%=context%>/dwr/interface/EntityDWR.js'></script>
<script type='text/javascript' src='<%=context%>/dwr/interface/VClassDWR.js'></script>
<script type='text/javascript' src='<%=context%>/dwr/engine.js'></script>
<script type='text/javascript' src='<%=context%>/dwr/util.js'></script>
<script type='text/javascript' src='<%=context%>/js/betterDateInput.js'></script>
<script type='text/javascript' src='<%=context%>/js/vitro.js'></script>
<script type='text/javascript' src='<%=context%>/dojo.js'></script>
<script type='text/javascript' src='<%=context%>/js/ents_edit.js'></script> 
<script type='text/javascript' src='<%=context%>/js/detect.js'></script>
<script language="JavaScript" type="text/javascript" src="js/toggle.js"></script>


<%
} %>

