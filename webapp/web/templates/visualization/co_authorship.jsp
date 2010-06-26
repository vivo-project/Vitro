<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<c:set var="portalBean" value="${requestScope.portalBean}"/>
<c:set var="themeDir"><c:out value="${portalBean.themeDir}" /></c:set>

<c:url var="egoCoAuthorshipDataURL" value="/admin/visQuery">
	<c:param name="vis" value="coauthorship"/>
	<c:param name="render_mode" value="data"/>
	<c:param name="uri" value="${requestScope.egoURIParam}"/>
	<c:param name="labelField" value="name"/>
</c:url>

<c:url var="jquery" value="/js/jquery.js"/>
<c:url var="adobeFlashDetector" value="/js/visualization/coauthorship/AC_OETags.js"/>
<c:url var="style" value="/${themeDir}css/visualization/coauthorship/style.css"/>
<c:url var="noImage" value="/${themeDir}site_icons/visualization/coauthorship/no_image.png"/>
<c:url var="swfLink" value="/${themeDir}site_icons/visualization/coauthorship/CoAuthor.swf"/>


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
// -->
</script>

<script type="text/javascript" src="${jquery}"></script>
<link href="${style}" rel="stylesheet" type="text/css" />

</head>

<body>
<div id="topNav">
  <h1>Co-Author <span>Network</span></h1>

 
</div>
<div id="body">
  <div id="topShadow"></div>
  <div id="bodyPannel" style="height:900px;">
    <br class="spacer" />
   <div id="visPanel" style="float:left; width:610px;">
   <script language="JavaScript" type="text/javascript">

function nodeClickedJS(obj){
	$("#newsLetter").attr("style","visibility:visible");
	$("#authorName").empty().append(obj[0]);
	//$("#works").append("<img src='assets/Garfield.jpg'/><br /><br />");
	$("#works").empty().append(obj[1]);
	if(obj[2]){$("#profileUrl").attr("href",obj[2]);}
	else{$("#profileUrl").attr("href","#");}
	$("#coAuthorName").empty().append(obj[name]);	
	if(obj[6]){$("#coAuthorUrl").attr("href",obj[6]);}
	else{$("#coAuthorUrl").attr("href","#");}
	$("#coAuthors").empty().append(obj[5]);	
	$("#firstPublication").empty().append((obj[3])?obj[3]+" First Publication":"");
	$("#lastPublication").empty().append((obj[4])?obj[4]+" Last Publication":"");
	
	//obj[7]:the url parameter for node
	
}
<!--
// Version check for the Flash Player that has the ability to start Player Product Install (6.0r65)
var hasProductInstall = DetectFlashVer(6, 0, 65);

// Version check based upon the values defined in globals
var hasRequestedVersion = DetectFlashVer(requiredMajorVersion, requiredMinorVersion, requiredRevision);

if ( hasProductInstall && !hasRequestedVersion ) {
	// DO NOT MODIFY THE FOLLOWING FOUR LINES
	// Location visited after installation is complete if installation is required
	var MMPlayerType = (isIE == true) ? "ActiveX" : "PlugIn";
	var MMredirectURL = window.location;
    document.title = document.title.slice(0, 47) + " - Flash Player Installation";
    var MMdoctitle = document.title;

	AC_FL_RunContent(
		"src", "playerProductInstall",
		"FlashVars", "MMredirectURL="+MMredirectURL+'&MMplayerType='+MMPlayerType+'&MMdoctitle='+MMdoctitle+"",
		"width", "600",
		"height", "800",
		"align", "middle",
		"id", "CoAuthor",
		"quality", "high",
		"bgcolor", "#ffffff",
		"name", "CoAuthor",
		"allowScriptAccess","sameDomain",
		"type", "application/x-shockwave-flash",
		"pluginspage", "http://www.adobe.com/go/getflashplayer"
	);
} else if (hasRequestedVersion) {
	// if we've detected an acceptable version
	// embed the Flash Content SWF when all tests are passed
	AC_FL_RunContent(
			"src", "${swfLink}",
			"flashVars", "graphmlUrl=${egoCoAuthorshipDataURL}",			
			"width", "600",
			"height", "800",
			"align", "middle",
			"id", "CoAuthor",
			"quality", "high",
			"bgcolor", "#ffffff",
			"name", "CoAuthor",
			"allowScriptAccess","sameDomain",
			"type", "application/x-shockwave-flash",
			"pluginspage", "http://www.adobe.com/go/getflashplayer"
	);
  } else {  // flash is too old or we can't detect the plugin
    var alternateContent = 'Alternate HTML content should be placed here. '
  	+ 'This content requires the Adobe Flash Player. '
   	+ '<a href=http://www.adobe.com/go/getflash/>Get Flash</a>';
    document.write(alternateContent);  // insert non-flash content
  }
// -->
</script>

   </div>
   <div id="dataPanel" style="float:left; width:150px;">
   <br/><br/><br/><br/><br/><br/>
<div id="newsLetter" style="visibility:hidden"> <span class="nltop"></span>
          <div class="middle" id="nodeData">
          <div><img src="${noImage}" /></div>
          <div class="bold"><strong><span id="authorName">&nbsp;</span></strong></div>
         <!-- <div class="italicize">Professor</div>
          <div class="italicize">Department of <span>???</span></div>
          -->
          <br />
          <div class="works"><span class="numbers" style="width:40px;" id="works">6</span>&nbsp;&nbsp;<span class="title">Works</span></div>
          <div class="works"><span class="numbers" style="width:40px;" id="coAuthors">78</span>&nbsp;&nbsp;<span>Co-author(s)</span></div>
          <br/>
          <div id="firstPublication"><span ></span>&nbsp;<span>First Publication</span></div>
          <div id="lastPublication"><span ></span>&nbsp;Last Publication</div>
          <br/>
          <div><a href="#" id="profileUrl">Go to VIVO profile</a></div>
          <br/>
          <div><a href="#" id="coAuthorUrl">Go to ego-centric co-author network of <span id="coAuthorName"></span></a></div>
        </div>
          <br class="spacer">
          <span class="nlbottom"></span></div>
</div>
  </div>
  <div id="bottomShadow"></div>
  <br class="spacer" />
</div>
<script>
$(document).ready(function(){

});
</script>


</body>
</html>
