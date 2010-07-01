<%@ page import="edu.cornell.mannlib.vitro.webapp.visualization.personpubcount.VisVOContainer"%>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>

<c:url var="visImageContextPath" value="/${themeDir}site_icons/visualization/" />

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

<div id="body"><%-- Label --%>
<div class="datatypePropertyValue">
<div class="statementWrap"><span id="ego_label"
	class="author_name"></span></div>
</div>

<%-- Moniker--%>
<div class="datatypeProperties">
<div class="datatypePropertyValue">
<div class="statementWrap"><span id="ego_moniker"
	class="author_moniker"></span></div>
</div>
</div>

<%-- Image --%>
<div class="datatypeProperties">
<div class="datatypePropertyValue">
<div id="ego_profile_image" class="statementWrap thumbnail"></div>
</div>
</div>

<%-- Sparkline --%>
<div class="datatypeProperties">
<div class="datatypePropertyValue">
<div id="ego_sparkline">${sparkline.sparklineContent}</div>
</div>
</div>


<div id="topShadow"></div>
<div id="bodyPannel" style="height: 900px;"><br class="spacer" />
<div id="visPanel" style="float: left; width: 610px;">


<script type="text/javascript">

<!--

renderCoAuthorshipVisualization();

//-->

</script></div>
<div id="dataPanel" style="float: left; width: 150px;"><br />
<br />
<br />
<br />
<br />
<br />

<div id="newsLetter" style="visibility: hidden"><span
	class="nltop"></span>
<div class="middle" id="nodeData">
<div id="profileImage"></div>
<div class="bold"><strong><span id="authorName"
	class="author_name">&nbsp;</span></strong></div>
<div class="italicize"><span id="profileMoniker"
	class="author_moniker"></span></div>
<div class="works"><span class="numbers" style="width: 40px;"
	id="works">6</span>&nbsp;&nbsp;<span class="title">Works</span></div>
<div class="works"><span class="numbers" style="width: 40px;"
	id="coAuthors">78</span>&nbsp;&nbsp;<span>Co-author(s)</span></div>
<br />
<div id="firstPublication"><span></span>&nbsp;<span>First
Publication</span></div>
<div id="lastPublication"><span></span>&nbsp;Last Publication</div>
<br />
<div><a href="#" id="profileUrl">VIVO profile</a></div>
<br />
<div><a href="#" id="coAuthorshipVisUrl">Co-author network of
<span id="coAuthorName"></span></a></div>
</div>
<br class="spacer"> <span class="nlbottom"></span>
</div>

</div>

 <span class="no_href_styles"> <a href="${coAuthorshipDownloadFile}"><img
	src="${visImageContextPath}download_graphml.png" width="91" height="25" /></a>
</span>
<div id="bottomShadow"></div>


</div>

<br class="spacer" />

<style type="text/css">
.vis-stats {
	width: 760px;
	margin: 0;
	padding: 0;
}

.vis-tables {
	width: 25%;
	padding: 5px;
	margin: 5px;
	background-color: #FFF;
	border: 1px solid #ddebf1;
	float: left;
}

p.datatable {
	font-size: 12px;
	display: block;
	margin: 2px;
	padding: 0
}

.datatable table {
	text-align: left;
}

.datatable img {
	float: right;
	cursor: pointer;
}

.datatable table caption {
	color: #16234c;
	margin: 0;
	padding: 0;
	font-size: 14px;
}
</style>

<div class="vis-stats">
<div class="vis-tables">
<p class="datatable">${sparkline.table} 
<a href="${egoSparklineDataURL}" class="no_href_styles">
	<img src="${visImageContextPath}download_csv.png" width="91" height="25" />
</a>
</p>
</div>

<div class="vis-tables">
<p id="coauth_table_container" class="datatable"></p>
</div>

</div>

</div>
<script>
$(document).ready(function(){

	processProfileInformation("ego_label", 
							  "ego_moniker",
							  "ego_profile_image",
							  jQuery.parseJSON(getWellFormedURLs("${requestScope.egoURIParam}", "profile_info")));

});
</script>