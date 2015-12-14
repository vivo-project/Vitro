//GLOBAL VARIABLES
//***************************************************************************************
//vars from DHTMLapi.js, Edition 2
//****************************************************************************************
// Global variables
var isCSS, isW3C, isIE4, isNN4, isIE6CSS;
// initialize upon load to let all browsers establish content objects
function initDHTMLAPI() {
    if (document.images) {
        isCSS = (document.body && document.body.style) ? true : false;
        isW3C = (isCSS && document.getElementById) ? true : false;
        isIE4 = (isCSS && document.all) ? true : false;
        isNN4 = (document.layers) ? true : false;
        isIE6CSS = (document.compatMode && document.compatMode.indexOf("CSS1") >= 0) ? true : false;
		  //alert("isCSS: "+isCSS+", isW3C: "+isW3C+", isIE4: "+isIE4	+", isNN4: "+isNN4+", isIE6CSS: "+isIE6CSS );
	} else {
		alert("document.images not defined in initDHTMLAPI()");
	}
}
// set event handler to initialize API
window.onload = initDHTMLAPI;
//window.onresize = resizeWindow;


//***************************************************************************************
//vars from ZoomBox.htm
//****************************************************************************************
// Global vars for browser type and version
var isNav = (navigator.appName.indexOf("Netscape")>=0);
var isNav4 = false;
var isIE4_old = false;
var is5up = false;
//alert(navigator.appVersion);
if (isNav) {
	if (parseFloat(navigator.appVersion)<5) {
		isNav4=true;
		//alert("Netscape 4.x or older");
	} else {
		is5up = true;
	}
} else {
	isIE4_old=true;
	if (navigator.appVersion.indexOf("MSIE 5")>0) {
		isIE4_old = false;
		is5up = true;
		//alert("IE5");
	}
}

function getTagById(tagId) {
	var selectedTag;
	if (document.all) {
		selectedTag=document.all.item(tagId);
	} else {
		selectedTag=document.getElementById(tagId);
	}
	return selectedTag;
}

function switchGroupDisplay( whichTag, whichToggleImage, imageDirectory ) {
	var tagToSwitch = getTagById( whichTag );
	if ( tagToSwitch == null )
		return;
	if ( imageDirectory == null ) {
		imageDirectory="site_icons";
	}
	var toggleIcon = getTagById( whichToggleImage );
	if ( tagToSwitch.style.display == "" || tagToSwitch.style.display == "none" ) {
		tagToSwitch.style.display = "block";
		toggleIcon.src = imageDirectory + ((whichTag=="textblock") ? "/togglelegend.gif" : "/minus.gif");
		//if ( document.all ) {
		//	tagToSwitch.scrollIntoView();
		//}
		if (whichTag != "legend" && whichTag != "themelist" && whichTag != "textblock" && whichTag != "overviewMap" ) {
			closeAllButThisGroupDisplay( whichTag );
		}
	} else {
		tagToSwitch.style.display="none";
		toggleIcon.src= imageDirectory + ((whichTag=="textblock") ? "/Toc.gif" : "/plus.gif");
	}
}

function closeGroupDisplay( whichTag, whichToggleImage, imageDirectory ) {
	var tagToClose = getTagById( whichTag );
	if ( tagToClose == null )
		return;
	var toggleIcon = getTagById( whichToggleImage );
	if ( tagToClose.style.display != "" && tagToClose.style.display != "none" ) {
		tagToClose.style.display="none";
		toggleIcon.src = imageDirectory + "/plus.gif";
	}
}

function closeAllButThisGroupDisplay( whichTag, whichVal ) {
	if ( whichTag != "layerGrp1" ) closeGroupDisplay("layerGrp1","layerGrp1Switch");
	if ( whichTag != "layerGrp2" ) closeGroupDisplay("layerGrp2","layerGrp2Switch");
	if ( whichTag != "layerGrp3" ) closeGroupDisplay("layerGrp3","layerGrp3Switch");
	if ( whichTag != "layerGrp4" ) closeGroupDisplay("layerGrp4","layerGrp4Switch");
	if ( whichTag != "layerGrp5" ) closeGroupDisplay("layerGrp5","layerGrp5Switch");
}

function initGroupDisplay( whichTag, value ) {
	var tagToSet = getTagById( whichTag );
	if (tagToSet == null ) {
		alert("whichTag " + whichTag + " cannot be found in initGroupDisplay");
		return;
	}
	if ( value == "block" ) {
		tagToSet.style.display="block";
	} else {
		tagToSet.style.display="none";
	}
	tagToSet.style.color="#6A5ACD";
}

function getGroupDisplayValue( whichTag ) {
	var tagToSwitch = getTagById( whichTag );
	if ( tagToSwitch == null )
		return;
	return tagToSwitch.style.display;
}

function onMouseOverHeading( tagToHighlight ) {
	// don't do getTagById because tag does not have id: var tagToHighlight = getTagById( whichTag );
	//if (tagToHighlight == null ) // leave in for Mozilla diagnostics
	//	return;
	//thisTag.style.textDecoration = 'underline';
	tagToHighlight.style.color = "#CC9933"; //<a:hover>
	tagToHighlight.style.cursor="pointer";
}

function onMouseOutHeading( tagToUnHighlight ) {
	// don't do this because tag does not have id: var tagToUnHighlight = getTagById( whichTag );
	//if (tagToUnHighlight == null ) // leave in for Mozilla diagnostics
	//	return;
	//tagToUnHighlight.style.textDecoration = 'none';
	tagToUnHighlight.style.color = "black" //"#6A5ACD"; // <a>
	tagToUnHighlight.style.cursor="pointer";
}

