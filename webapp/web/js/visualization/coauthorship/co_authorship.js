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
			console.log("PROF - " + data);
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
			console.log("IMAGE - " + data);
		}
		}).responseText;

		 return contextPath + finalURL;
//		return finalURL;

	}

	// });

}

$.fn.image = function(src, successFunc, failureFunc){
	return this.each(function(){ 
		var i = new Image();
		i.src = src;
		i.onerror = failureFunc;
		i.onload = successFunc;

		// console.dir(i);
		// this.appendChild(i);

		return i;
	});
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
		var imageLink = getWellFormedURLs(obj[7], "image");

	} else{
		$("#profileUrl").attr("href","#");
		$("#coAuthorshipVisUrl").attr("href","#");
	}

	var imageContainer = $("#profileImage");
	imageContainer.image(imageLink, 
			function(){
		imageContainer.append(this); 
	},
	function(){
		/*
		 * For performing any action on failure to
		 * find the image.
		 */
	}
	);


	$("#coAuthorName").empty().append(obj[name]);	

	$("#coAuthors").empty().append(obj[5]);	
	$("#firstPublication").empty().append((obj[3])?obj[3]+" First Publication":"");
	$("#lastPublication").empty().append((obj[4])?obj[4]+" Last Publication":"");

	// obj[7]:the url parameter for node

}

function renderVisualization() {

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