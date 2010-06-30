function getWellFormedURLs(given_uri, type) {

	// general best practice is to put javascript code inside document.ready
	// but in this case when i do that the function does not get called
	// properly.
	// so removing it for now.

	// $(document).ready(function() {

	if (type == "coauthorship") {

		var finalURL = $.ajax({
			url: contextPath + "/admin/visQuery",
			data: ({vis: "utilities", vis_mode: "COAUTHORSHIP_URL", uri: given_uri}),
			dataType: "text",
			async: false,
			success:function(data){
			// console.log("COA - " + data);
		}
		}).responseText;

		return finalURL;


	} else if (type == "profile") {

		var finalURL = $.ajax({
			url: contextPath + "/admin/visQuery",
			data: ({vis: "utilities", vis_mode: "PROFILE_URL", uri: given_uri}),
			dataType: "text",
			async: false,
			success:function(data){
		}
		}).responseText;

		return finalURL;

	} else if (type == "image") {

		var finalURL = $.ajax({
			url: contextPath + "/admin/visQuery",
			data: ({vis: "utilities", vis_mode: "IMAGE_URL", uri: given_uri}),
			dataType: "text",
			async: false,
			success:function(data){
		}
		}).responseText;

		return finalURL;

	} else if (type == "profile_info") {

		var profileInfoJSON = $.ajax({
			url: contextPath + "/admin/visQuery",
			data: ({vis: "utilities", vis_mode: "PROFILE_INFO", uri: given_uri}),
			dataType: "json",
			async: false,
			success:function(data){
		}
		}).responseText;

		return profileInfoJSON;

	}
	
	// });
}

$.fn.image = function(src, successFunc, failureFunc){
	return this.each(function(){ 
		var profileImage = new Image();
		profileImage.src = src;
		profileImage.width = 150;
		profileImage.onerror = failureFunc;
		profileImage.onload = successFunc;
		
		
		return profileImage;
	});
}

function setProfileImage(imageContainerID, rawPath, contextPath) {
	
	if (imageContainerID == "") {
		return;
	}
	
	
	var imageLink =  contextPath + rawPath;
	
	var imageContainer = $("#" + imageContainerID);
	imageContainer.image(imageLink, 
			function(){
		imageContainer.empty().append(this); 
	},
	function(){
		 //For performing any action on failure to
		 //find the image.
		imageContainer.empty();
	}
	);
	
}

function setProfileMoniker(monikerContainerID, moniker) {
	
	if (monikerContainerID == "") {
		return;
	}
	
	$("#" + monikerContainerID).empty().text(moniker);
	
}

function setProfileName(nameContainerID, name) {
	
	if (nameContainerID == "") {
		return;
	}
	
	$("#" + nameContainerID).empty().text(name);
	
	
}

function processProfileInformation(nameContainerID,
								   monikerContainerID,
								   imageContainerID,
								   profileInfoJSON) {
	
	
	var name, imageRawPath, imageContextPath, moniker;
	
	$.each(profileInfoJSON, function(key, set){
		
		 if (key.search(/imageThumb/i) > -1) {
			 
			 imageRawPath = set[0];
			 
		 } else if (key.search(/imageContextPath/i) > -1) {
			 
			 imageContextPath = set[0];
			 
		 } else if (key.search(/moniker/i) > -1) {
			 
			 moniker = set[0];
			 
		 } else if (key.search(/label/i) > -1) {
			 
			 name = set[0];
			 
		 }
		 
      });
	
	setProfileName(nameContainerID, name);
	setProfileMoniker(monikerContainerID, moniker);
	setProfileImage(imageContainerID, imageRawPath, imageContextPath);
	
}



function nodeClickedJS(obj){

	$("#newsLetter").attr("style","visibility:visible");
	$("#authorName").empty().append(obj[0]);
	$("#works").empty().append(obj[1]);

	/*
	 * Here obj[7] points to the uri of that individual
	 */
	if(obj[7]){
		$("#profileUrl").attr("href", getWellFormedURLs(obj[7], "profile"));
		$("#coAuthorshipVisUrl").attr("href", getWellFormedURLs(obj[7], "coauthorship"));
		processProfileInformation("", 
								  "profileMoniker",
								  "profileImage",
								  jQuery.parseJSON(getWellFormedURLs(obj[7], "profile_info")));

	} else{
		$("#profileUrl").attr("href","#");
		$("#coAuthorshipVisUrl").attr("href","#");
	}

	$("#coAuthorName").empty().append(obj[0]);	

	$("#coAuthors").empty().append(obj[5]);	
	$("#firstPublication").empty().append((obj[3])?obj[3]+" First Publication":"");
	$("#lastPublication").empty().append((obj[4])?obj[4]+" Last Publication":"");

	// obj[7]:the url parameter for node

}

function renderSparklineVisualization(visualizationURL) {
	
	 $(document).ready(function() {
		 
	//$("#ego_sparkline").empty().html('<img src="${loadingImageLink}" />');

	   $.ajax({
		   url: visualizationURL,
		   dataType: "html",
		   success:function(data){
		     $("#ego_sparkline").html(data);

		   }
		 });
	   
	   
	 });

}

function renderCoAuthorshipVisualization() {

	//Version check for the Flash Player that has the ability to start Player
	//Product Install (6.0r65)
	var hasProductInstall = DetectFlashVer(6, 0, 65);
	
	//Version check based upon the values defined in globals
	var hasRequestedVersion = DetectFlashVer(requiredMajorVersion, requiredMinorVersion, requiredRevision);
	
	if ( hasProductInstall && !hasRequestedVersion ) {
		// DO NOT MODIFY THE FOLLOWING FOUR LINES
		// Location visited after installation is complete if installation is
		// required
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
				"src", swfLink,
				"flashVars", "graphmlUrl=" + egoCoAuthorshipDataURL,			
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

}