<%@ page import="edu.cornell.mannlib.vitro.webapp.visualization.personpubcount.VisVOContainer"%>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>

<c:set var="portalBean" value="${requestScope.portalBean}" />
<c:set var="themeDir"><c:out value="${portalBean.themeDir}" /></c:set>
<c:url var="visImageContextPath" value="/${themeDir}site_icons/visualization/" />
<c:url var="loadingImageLink" value="/${themeDir}site_icons/visualization/ajax-loader.gif"></c:url>

<c:set var='sparkline' value='${requestScope.sparklineVO}' />

<c:url var="egoSparklineDataURL" value="/admin/visQuery">
	<c:param name="render_mode" value="data" />
	<c:param name="vis" value="person_pub_count" />
	<c:param name="uri" value="${requestScope.egoURIParam}" />
</c:url>

<c:url var="coAuthorshipDownloadFile" value="/admin/visQuery">
	<c:param name="vis" value="person_level" />
	<c:param name="render_mode" value="data" />
	<c:param name="uri" value="${requestScope.egoURIParam}" />
</c:url>

<div id="body">


<style type="text/css">

#ego_profile {
	padding:10px;
}

#ego_label {
	font-size:1.1em;
	margin-left:100px;
	margin-top:9px;
	position:absolute;
}

#ego_moniker {
	margin-left:100px;
	margin-top:27px;
	position:absolute;
}

#ego_profile_image {
	width: 100px;
}

#ego_sparkline {
	cursor:pointer;
	height:36px;
	margin-left:10px;
	margin-top:69px;
	position:absolute;
	width:471px;
}

</style>

<div id="ego_profile">

	<%-- Label --%>
			<span id="ego_label" class="author_name"></span>
	
	<%-- Moniker--%>
			<span id="ego_moniker" class="author_moniker"></span>
	
	<%-- Image --%>
			<span id="ego_profile_image" class="thumbnail"></span>
	
	<%-- Sparkline --%>
			<span id="ego_sparkline">${sparkline.sparklineContent}</span>

</div>



<div id="topShadow"></div>
<div id="bodyPannel" style="height: 900px;">
	<br class="spacer" />
	
	<div id="visPanel" style="float: left; width: 610px;">
		<script language="JavaScript" type="text/javascript">
			<!--
			renderCoAuthorshipVisualization();
			//-->
		</script>
	</div>
	
	<div id="dataPanel" style="float: left; width: 150px;" style="visibility:hidden;" >
		<br /><br /><br /><br /><br /><br />
		
		<div id="profileImage"></div>
		
		<div class="bold"><strong><span id="authorName" class="author_name">&nbsp;</span></strong></div>
		
		<div class="italicize"><span id="profileMoniker" class="author_moniker"></span></div>
		<br />
		<div class="works"><span class="numbers" style="width: 40px;" id="works"></span>&nbsp;&nbsp;<span class="title">Works</span></div>
		<div class="works"><span class="numbers" style="width: 40px;" id="coAuthors"></span>&nbsp;&nbsp;<span>Co-author(s)</span></div>
		
		<div class="works" id="fPub" style="visibility:hidden"><span class="numbers" style="width:40px;" id="firstPublication"></span>&nbsp;&nbsp;<span>First Publication</span></div>
		<div class="works" id="lPub" style="visibility:hidden"><span class="numbers" style="width:40px;" id="lastPublication"></span>&nbsp;&nbsp;<span>Last Publication</span></div>
		
		<br /><br />
		   
		<div><a href="#" id="profileUrl">VIVO profile</a></div>
		<br />
		<div><a href="#" id="coAuthorshipVisUrl">Co-author network of <span id="coAuthorName"></span></a></div>
	</div>
	
	<span class="no_href_styles"><a href="${coAuthorshipDownloadFile}">
 	<img src="${visImageContextPath}download_graphml.png" width="91" height="25" /></a>
</span>

	<br class="spacer">
</div>




</div>


<div class="vis-stats">

	<div class="vis-tables">
		<p class="datatable">
			${sparkline.table} 
			<a href="${egoSparklineDataURL}" class="no_href_styles">
				<img src="${visImageContextPath}download_csv.png" width="91" height="25" />
			</a>
		</p>
	</div>

	<div class="vis-tables">
		<p id="coauth_table_container" class="datatable"></p>
	</div>

</div>

<script language="JavaScript" type="text/javascript">
$(document).ready(function(){

	$("#coauth_table_container").empty().html('<img id="loadingData" with="auto" src="${loadingImageLink}" />');

	processProfileInformation("ego_label", 
							  "ego_moniker",
							  "ego_profile_image",
							  jQuery.parseJSON(getWellFormedURLs("${requestScope.egoURIParam}", "profile_info")));

});
</script>