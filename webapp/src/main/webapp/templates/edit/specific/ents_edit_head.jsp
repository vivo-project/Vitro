<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%

request.setAttribute("dwrDisabled", new Boolean(false));
String context = request.getContextPath();

%>


<%
if (!(Boolean)request.getAttribute("dwrDisabled")) {
%>
<script type="text/javascript">
    // DWR 3 is a better citizen for co-existing with jQuery.
    // All of the ents_edit.js and vitro.js have been updated so that $(..) references use the dwr.util method directly
    // As such, it should be possible to avoid the noConflict() mode.
    // This then prevents a few errors that would otherwise occur in jQuery expecting JS on the same page
    // relinquish jQuery's control of the $ variable to avoid conflicts with DWR
//    jQuery.noConflict();
</script>

<script type="text/javascript" xml:space="preserve">
    var gEntityURI="${entity.URI}";
</script> <!-- There has got to be a better way to pass this to the js -->

<script type="text/javascript" src="<%=context%>/dwr/engine.js"></script>
<script type="text/javascript" src="<%=context%>/dwr/util.js"></script>
<script type="text/javascript" src="<%=context%>/dwr/interface/PropertyDWR.js"></script>
<script type="text/javascript" src="<%=context%>/dwr/interface/EntityDWR.js"></script>
<script type="text/javascript" src="<%=context%>/dwr/interface/VClassDWR.js"></script>
<script type="text/javascript" src="<%=context%>/js/betterDateInput.js"></script>
<script type="text/javascript" src="<%=context%>/js/vitro.js"></script>
<script type="text/javascript" src="<%=context%>/dojo.js"></script>
<script type="text/javascript" src="<%=context%>/js/ents_edit.js"></script>
<script type="text/javascript" src="<%=context%>/js/detect.js"></script>
<script type="text/javascript" src="<%=context%>/js/toggle.js"></script>

<%
} %>

