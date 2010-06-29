<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<c:set var="portalBean" value="${requestScope.portalBean}" />
<c:set var="themeDir">
	<c:out value="${portalBean.themeDir}" />
</c:set>
<c:set var="contextPath">
	<c:out value="${pageContext.request.contextPath}" />
</c:set>

<c:url var="egoCoAuthorshipDataURL" value="/admin/visQuery">
	<c:param name="vis" value="coauthorship" />
	<c:param name="render_mode" value="data" />
	<c:param name="uri" value="${requestScope.egoURIParam}" />
	<c:param name="labelField" value="name" />
</c:url>

<c:url var="jquery" value="/js/jquery.js" />
<c:url var="adobeFlashDetector"
	value="/js/visualization/coauthorship/AC_OETags.js" />
<c:url var="coAuthorShipJavaScript"
	value="/js/visualization/coauthorship/co_authorship.js" />
<c:url var="style"
	value="/${themeDir}css/visualization/coauthorship/style.css" />
<c:url var="noImage"
	value="/${themeDir}site_icons/visualization/coauthorship/no_image.png" />
<c:url var="swfLink"
	value="/${themeDir}site_icons/visualization/coauthorship/CoAuthor.swf" />


<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />


<title>Co-Authorship Visualization</title>

<script type="text/javascript" src="${adobeFlashDetector}"></script>


<script language="JavaScript" type="text/javascript">
<!--
// -----------------------------------------------------------------------------
// Globals
// Major version of Flash required
var requiredMajorVersion = 10;
// Minor version of Flash required
var requiredMinorVersion = 0;
// Minor version of Flash required
var requiredRevision = 0;
// -----------------------------------------------------------------------------


var swfLink = "${swfLink}";
var egoCoAuthorshipDataURL = "${egoCoAuthorshipDataURL}";
var contextPath = "${contextPath}";


// -->
</script>

<script type="text/javascript" src="${jquery}"></script>
<link href="${style}" rel="stylesheet" type="text/css" />


<script type="text/javascript" src="${coAuthorShipJavaScript}"></script>

</head>

<body>
<div id="topNav">
<h1>Co-Author <span>Network</span></h1>


</div>
<div id="body">
<div id="topShadow"></div>
<div id="bodyPannel" style="height: 900px;"><br class="spacer" />
<div id="visPanel" style="float: left; width: 610px;">

<script type="text/javascript">

<!--

renderVisualization();

//-->

</script>


</div>
<div id="dataPanel" style="float: left; width: 150px;"><br />
<br />
<br />
<br />
<br />
<br />

	<div id="newsLetter" style="visibility: hidden">
		<span class="nltop"></span>
		<div class="middle" id="nodeData">
		<div id="profileImage"></div>
		<div class="bold"><strong><span id="authorName">&nbsp;</span></strong></div>
		<!-- <div class="italicize">Professor</div>
		          <div class="italicize">Department of <span>???</span></div>
		          --> <br />
		<div class="works"><span class="numbers" style="width: 40px;"
			id="works">6</span>&nbsp;&nbsp;<span class="title">Works</span></div>
		<div class="works"><span class="numbers" style="width: 40px;"
			id="coAuthors">78</span>&nbsp;&nbsp;<span>Co-author(s)</span></div>
		<br />
		<div id="firstPublication"><span></span>&nbsp;<span>First
		Publication</span></div>
		<div id="lastPublication"><span></span>&nbsp;Last Publication</div>
		<br />
		<div><a href="#" id="profileUrl">Go to VIVO profile</a></div>
		<br />
		<div><a href="#" id="coAuthorshipVisUrl">Go to ego-centric
		co-author network of <span id="coAuthorName"></span></a></div>
		</div>
		<div id="image_test"></div>
		<br class="spacer"> <span class="nlbottom"></span>
	</div>

</div>
</div>
<div id="bottomShadow"></div>
<br class="spacer" />
</div>
<script>
$(document).ready(function(){

	var obj = jQuery.parseJSON('{"name":"John"}');
	console.log(obj)

	var obj = jQuery.parseJSON('{"imageOffset2":["sup"],"A":["2001","2002","2003","2090","Unknown"],"B":["2001","2002","2003","2090","Unknown"],"C":["2001","2002","2003","2090","Unknown"],"imageOffset":["2090","2002","2003","2001"]}');
	console.log(obj)

	 $.each(obj, function(i, item){
		 console.log("i - " + i + " item - " + item);
				$.each(item, function(index, vals) {
						console.log(index + " - val - " + vals);
					});
		 
       });
	
});
</script>


</body>
</html>
