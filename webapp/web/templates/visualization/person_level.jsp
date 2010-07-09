<%@ page import="edu.cornell.mannlib.vitro.webapp.visualization.valueobjects.SparklineVOContainer"%>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>

<c:set var="portalBean" value="${requestScope.portalBean}" />
<c:set var="themeDir"><c:out value="${portalBean.themeDir}" /></c:set>
<c:url var="visImageContextPath" value="/${themeDir}site_icons/visualization/" />
<c:url var="loadingImageLink" value="/${themeDir}site_icons/visualization/ajax-loader.gif"></c:url>

<c:set var='egoPubSparkline' value='${requestScope.egoPubSparklineVO}' />
<c:set var='uniqueCoauthorsSparkline' value='${requestScope.uniqueCoauthorsSparklineVO}' />

<c:set var='egoPubSparklineContainerID' value='${requestScope.egoPubSparklineContainerID}' />
<c:set var='uniqueCoauthorsSparklineVisContainerID' value='${requestScope.uniqueCoauthorsSparklineVisContainerID}' />

<c:set var='numOfAuthors' value='${requestScope.numOfAuthors}' />
<c:set var='numOfCoAuthorShips' value='${requestScope.numOfCoAuthorShips}' />


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

#body h1 {
	margin:0.0em;
} 

.sparkline_wrapper_table {
	display: inline;
	vertical-align: bottom;
}

.author_name {
	color: #13968c;
	font-weight: bold;
}

.neutral_author_name {
	color: black;
	font-weight: bold;
}

.author_moniker {
	color: #9C9C9C;
}

.sub_headings {
	color: #121b3c;
	padding-top: 5px;
}

.sub_headings a {
	font-size:0.7em;
	font-weight:normal;
}


.inline_href {
}


#ego_profile {
	padding-left:10px;
	padding-top:10px;
	min-height: 100px;
}

#ego_label {
	font-size:1.1em;
	/*margin-left:100px;
	margin-top:9px;
	position:absolute;*/
}

#ego_moniker {
	/*margin-left:100px;
	margin-top:27px;
	position:absolute;*/
}

#ego_profile_image {
	float:left;
	padding-right: 5px;

	/*width: 100px;*/
}

#ego_sparkline {
	cursor:pointer;
	height:36px;
	/*
	margin-left:10px;
	margin-top:69px;
	position:absolute;*/
	width:471px;
}

#dataPanel {
	/*
	float: left; 
	width: 150px; 
	visibility:hidden;*/
}

</style>

<!--[if IE]>
	<style type="text/css">
	
	#${egoPubSparklineContainerID},
	#${uniqueCoauthorsSparklineVisContainerID} {
		padding-bottom:15px;
	}
	
	
	</style>
<![endif]-->

<div id="ego_profile">

	
	<%-- Image --%>
			<span id="ego_profile_image"></span>
			
	<%-- Label --%>
			<h1><span id="ego_label" class="author_name"></span></h1>
	
	<%-- Moniker--%>
			<span id="ego_moniker" class="author_moniker"></span>


	<div style="clear:both;"></div>
	
	<%-- Sparkline --%>
		<h2 class="sub_headings">General Statistics</h2>
			<div id="${egoPubSparklineContainerID}">
				${egoPubSparkline.sparklineContent}
			</div>
			
			<div id="${uniqueCoauthorsSparklineVisContainerID}">
				${uniqueCoauthorsSparkline.sparklineContent}
			</div>
			
		<h2 class="sub_headings">Co-Author Network 
				<%-- A simple if/else condition --%>
		<c:choose>
		    <c:when test='${numOfCoAuthorShips > 0}'>
		       <a href="${coAuthorshipDownloadFile}">(.GraphML File)</a></h2>
		    </c:when>
		    <c:otherwise>
		        </h2>
		        <span id="no_coauthorships">Currently there are no multi-author papers for <span id="no_coauthorships_person">this author</span> in the VIVO database.</span>
		    </c:otherwise>
		</c:choose>
		


</div>	

<c:if test='${numOfCoAuthorShips > 0}'>

<div id="bodyPannel">
	
	
	<div id="visPanel" style="float: left; width: 600px;">
		<script language="JavaScript" type="text/javascript">
			<!--
			renderCoAuthorshipVisualization();
			//-->
		</script>
	</div>
	
	<div id="dataPanel">
		
		<div id="profileImage"></div>
		
		<div class="bold"><strong><span id="authorName" class="neutral_author_name">&nbsp;</span></strong></div>
		
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

</div>

</c:if>

<div class="vis_stats">

<h2 class="sub_headings">Tables</h2>

	<div class="vis-tables">
		<p id="publications_table_container" class="datatable">
			${egoPubSparkline.table} 
		</p>
	</div>
	
	<c:if test='${numOfCoAuthorShips > 0}'>

		<div class="vis-tables">
			<p id="coauth_table_container" class="datatable"></p>
		</div>
	
	</c:if>

</div>


</div>



<script language="JavaScript" type="text/javascript">
$(document).ready(function(){

	<c:choose>
	    <c:when test='${numOfCoAuthorShips > 0}'>
	    	$("#coauth_table_container").empty().html('<img id="loadingData" with="auto" src="${loadingImageLink}" />');
	    </c:when>
	    <c:otherwise>
	    	setProfileName('no_coauthorships_person', $('#ego_label').text());
	    </c:otherwise>
	</c:choose>

	
	
	processProfileInformation("ego_label", 
							  "ego_moniker",
							  "ego_profile_image",
							  jQuery.parseJSON(getWellFormedURLs("${requestScope.egoURIParam}", "profile_info")));

	

	 

	  

});
</script>