/* $This file is distributed under the terms of the license in /doc/license.txt$ */

/**
   These are utility functions for Vitro js pages.
   Much of this is based on the util.js of the DWR package.

   @author Brian Caruso
*/

var vitroJsLoaded = true; //just indicates that this file was loaded.

    // public method for url encoding

//from http://www.kanolife.com/escape/2006/03/escape-and-unescape-javascript.html
function encodeUrl(pstrString) {
  if (pstrString == "") {
    return "";
  }
    //TODO: needs to be updated to UTF-8, javascript escape() only does ascii
    return escape(pstrString);
//  var iPos = 0;
//  var strOut = "";
//  var strChar;
//  var strString = escape(pstrString);
//  while (iPos < strString.length) {
//    strChar = strString.substr(iPos, 1);
//    if (strChar == "%") {
//      strNextChar = strString.substr(iPos + 1, 1);
//      if (strNextChar == "u") {
//        strOut += strString.substr(iPos, 6);
//        iPos += 6;
//      }
//      else {
//        strOut += "%u00" +
//                  strString.substr(iPos + 1, 2);
//        iPos += 3;
//      }
//    }
//    else {
//      strOut += strChar;
//      iPos++;
//    }
//  }
//  return strOut;
}

/**
   @param ele - element to addRows to
   @param data - array of data to add as rows to ele
   @param rowFunc - function that returns a <tr> element 
   @param cellFuncs - array of funcs, data will be passed as a parameter
    and the result will be added to the <tr> as a <td>. something like:
    function(row){return dosument.createElement("tr");}
*/
function addRows(ele, data, cellFuncs, rowFunc) {
  var frag = document.createDocumentFragment();
  if (DWRUtil._isArray(data))    {
    for (var i = 0; i < data.length; i++) {
      frag.appendChild( makeRow( data[i], cellFuncs, rowFunc) );
    }
  } else if (typeof data == "object") {
    for (var row in data) {
      frag.appendChild( makeRow(row, cellFuncs, rowFunc) );
    }
  }
  ele.appendChild(frag);
}

function makeRow( row, cellFuncs, rowFunc){
  if( rowFunc != null ){ 
    var tr = rowFunc(row); 
  }  else { 
    var tr = document.createElement("tr"); 
  }

  for(var j=0; j < cellFuncs.length; j++) {
    var func = cellFuncs[j]
      var td
      var reply = func(row)
      if (DWRUtil._isHTMLElement(reply, "td")) {
        td = reply;
      } else if (DWRUtil._isHTMLElement(reply, "a")) {
        td = document.createElement("td");
        td.appendChild( reply );
      } else {
        td = document.createElement("td");
        td.innerHTML = reply;
      }
    tr.appendChild(td);
  }
  return tr;
}

/** added this from the DWRUtil.js */
isDate = function(data) {     
    return (data && data.toUTCString) ? true : false; 
};

/** returns the summed colspan of the cells of the row.  */
function trColspan( tr ){
  var allCells = tr.cells;
  var colspan = 0;
  for( var i = 0; i < allCells.length ; i++ ){
    if( allCells[i].colSpan > 1 ){
      colspan += allCells.colSpan;
    }else{
      colspan++;
    }
  }
  return colspan;
}
/** returns the max colSpan of the table's tr's */
function tableMaxColspan( table ){
  var rows = table.rows;
  var max = 0;
  var tmp = 0;
  for(var i=0; i<rows.length; i++){
    tmp = trColspan(rows[i]);
    if( tmp > max ){ max = tmp; }
  }
  return max;
}

function isValidDate(ele) {
  var str = ele.value;
  var date = iso8601toDate( str );
  var valid = (isDate( date ) ) ?  true : false;
  if( !valid ) { alert("Date is invalid" );}
  return valid;
}

/** returns local time YYYY-MM-DD format string from Java Date or Js Date */
function date2iso8601( jdate ){
  if(jdate == null || !jdate.toUTCString ) return "0000-01-01";
  if(jdate == null ) return "0000-01-01";
  var str = "";
  var jsDate = new Date( jdate.getTime() );
  year = jsDate.getFullYear();
  month = jsDate.getMonth() + 1;//zero based: jan=0, feb=1, etc.
  day = jsDate.getDate();
  var monthS=month.toString(), dayS=day.toString();
  if( monthS.length == 1 ) { monthS = "0" + monthS; }
  if( dayS.length == 1 )  { dayS = "0" + dayS; }
  return year + "-" + monthS + "-" + dayS;
}

function setDateValue(ele, date, date2StrFunc){  
  if( date == null || ! isDate(date)) { return; }
  if( date2StrFunc == null ) { date2StrFunc = date2iso8601; }
  ele = $(ele);
  DWRUtil.setValue(ele, date2StrFunc( date ));  
}

/* removes all children and if it can, removes options */
function clear(ele){
    while (ele && ele.childNodes && ele.childNodes.length > 0) {ele.removeChild(ele.firstChild);}
    if( ele != null && ele.options ){    // Empty the list
      ele.options.length = 0;
    }
}

function hideElementById(eleId){
  var ele = $(eleId);
  if( ele != null ){  ele.style.display="none"; } 
}

function makeEntLinkElement(entityURI, text){
  link = document.createElement("a");
  link.href = "entityEdit?uri=" + encodeUrl(entityURI);
  link.innerHTML = text;
  //DWRUtil.setValue(link, "test" + text);
  return link;
}

function makePropLinkElement(propURI, text){
  link = document.createElement("a");
  link.href="propertyEdit?uri="+encodeUrl(propURI);
  link.innerHTML = text;
  return link;
}

/** returns date obj if str can be parsed, null otherwise.
    Bad day values will rollover into the next month: 2000-02-30
    will parse as 2000-03-02.
    @returns on error a string with description of what went wrong.
*/
function iso8601toDate( str ){
  if( str == null || str.length < 8 || str.length > 10 ) { 
    return "too long or short"; 
  }
  parts = str.split("-");
  if( parts.length != 3 ) { return "bad split, use -"; }
  yearPart = parts[0], monthPart = parts[1], dayPart = parts[2];

  year = parseInt(yearPart);
  if( isNaN(year) ) { return "year not a number"; }
  month = parseInt(monthPart);
  if( isNaN(month) ) { return "month not a number"; }
  day = parseInt(dayPart);
  if( isNaN(day) ) { return "day not a number"; }

  if( month > 12 || month < 1 ) {return "month bad"; }
  if( day > 31 || day < 1 ) {return "day bad"; } //could be better
  month = month - 1; //months are jan=0, feb=1, etc
  return new Date(year, month, day);   //do we need a try/catch here?  
}

/** use with Date.getYear() which is broken to get the correct year.
    Found on quirksmode.org
    ex of usage:
    var today = new Date();
    Year = takeYear(today);
*/
function takeYear(theDate){
	x = theDate.getYear();
	var y = x % 100;
	y += (y < 38) ? 2000 : 1900; //why 38? the 32bit epoch ends in 2038
	return y;
}

/*
bdc34
addEvent() from http://www.sitepoint.com/article/structural-markup-javascript
Simon Willison, "Enhancing Structural Markup with JavaScript" December 10th 2003
Author of code as cited by Willison: Scott Andrew, license unknown.
ex: addEvent(window, 'load', functionToRunOnWindowLoad);
*/

function addEvent(obj, evType, fn){
 if (obj.addEventListener){
   obj.addEventListener(evType, fn, true);
   return true;
 } else if (obj.attachEvent){
   var r = obj.attachEvent("on"+evType, fn);
   return r;
 } else {
   return false;
 }
}

// @name      The Fade Anything Technique
// @namespace http://www.axentric.com/aside/fat/
// @version   1.0-RC1
// @author    Adam Michela
var Fat = {
	make_hex : function (r,g,b) 
	{
		r = r.toString(16); if (r.length == 1) r = '0' + r;
		g = g.toString(16); if (g.length == 1) g = '0' + g;
		b = b.toString(16); if (b.length == 1) b = '0' + b;
		return "#" + r + g + b;
	},
	fade_all : function ()
	{
		var a = document.getElementsByTagName("*");
		for (var i = 0; i < a.length; i++) 
		{
			var o = a[i];
			var r = /fade-?(\w{3,6})?/.exec(o.className);
			if (r)
			{
				if (!r[1]) r[1] = "";
				if (o.id) Fat.fade_element(o.id,null,null,"#"+r[1]);
			}
		}
	},
	fade_element : function (id, fps, duration, from, to) 
	{
		if (!fps) fps = 30;
		if (!duration) duration = 3000;
		if (!from || from=="#") from = "#FFFF33";
		if (!to) to = this.get_bgcolor(id);
		
		var frames = Math.round(fps * (duration / 1000));
		var interval = duration / frames;
		var delay = interval;
		var frame = 0;
		
		if (from.length < 7) from += from.substr(1,3);
		if (to.length < 7) to += to.substr(1,3);
		
		var rf = parseInt(from.substr(1,2),16);
		var gf = parseInt(from.substr(3,2),16);
		var bf = parseInt(from.substr(5,2),16);
		var rt = parseInt(to.substr(1,2),16);
		var gt = parseInt(to.substr(3,2),16);
		var bt = parseInt(to.substr(5,2),16);
		
		var r,g,b,h;
		while (frame < frames)
		{
			r = Math.floor(rf * ((frames-frame)/frames) + rt * (frame/frames));
			g = Math.floor(gf * ((frames-frame)/frames) + gt * (frame/frames));
			b = Math.floor(bf * ((frames-frame)/frames) + bt * (frame/frames));
			h = this.make_hex(r,g,b);
		
			setTimeout("Fat.set_bgcolor('"+id+"','"+h+"')", delay);

			frame++;
			delay = interval * frame; 
		}
		setTimeout("Fat.set_bgcolor('"+id+"','"+to+"')", delay);
	},
	set_bgcolor : function (id, c)
	{
		var o = document.getElementById(id);
		o.style.backgroundColor = c;
	},
	get_bgcolor : function (id)
	{
		var o = document.getElementById(id);
		while(o)
		{
			var c;
			if (window.getComputedStyle) c = window.getComputedStyle(o,null).getPropertyValue("background-color");
			if (o.currentStyle) c = o.currentStyle.backgroundColor;
			if ((c != "" && c != "transparent") || o.tagName == "BODY") { break; }
			o = o.parentNode;
		}
		if (c == undefined || c == "" || c == "transparent") c = "#FFFFFF";
		var rgb = c.match(/rgb\s*\(\s*(\d{1,3})\s*,\s*(\d{1,3})\s*,\s*(\d{1,3})\s*\)/);
		if (rgb) c = this.make_hex(parseInt(rgb[1]),parseInt(rgb[2]),parseInt(rgb[3]));
		return c;
	}
}

// window.onload = function () 
// 	{
// 	Fat.fade_all();
// 	}
//end of Adam Michela's code


/* from javascript & DHTML cookbook, Goodman, 2003, O'Reilly */

/* note that when an iframe is stuck into a page the iframe nees to be resized */

/* <iframe id="myFrame" frameborder="0" vspace="0" hspace="0" marginwidth="0" 
marginheight="0" width="100%" src="external.html" scrolling="no" 
style="overflow:visible"></iframe>
...
<body ... onload = "adjustIFrameSize('myFrame');">
 */

// function adjustIFrameSize(id) {
//     var myIframe = document.getElementById(id);
//     if (myIframe) {
//         if (myIframe .contentDocument && myIframe.contentDocument.body.offsetHeight) {
//             // W3C DOM (and Mozilla) syntax
//             myIframe.height = myIframe.contentDocument.body.offsetHeight;    
//         } else if (myIframe.Document && myIframe.Document.body.scrollHeight) {
//             // IE DOM syntax
//             myIframe.height = myIframe.Document.body.scrollHeight;
//         }
//     }
// }

function adjustIFrameSize(id) {
    var myIframe = document.getElementById(id);
    if (myIframe) {
        if (myIframe.contentDocument && myIframe.contentDocument.body.offsetHeight) {
            // W3C DOM (and Mozilla) syntax
            myIframe.height = myIframe.contentDocument.body.offsetHeight;    
        } else if (myIframe.Document && myIframe.Document.body.scrollHeight) {
            // IE DOM syntax
            myIframe.height = myIframe.Document.body.scrollHeight;
        }
        // bind onload events to iframe
        if (myIframe.addEventListener) {
            myIframe.addEventListener("load", resizeIframe, false);
        } else {
            myIframe.attachEvent("onload", resizeIframe);
        }
   }
}

function toggleDisabled(ele){
  ele = $(ele);
  if(ele == null) {return;}
  ele.disabled = !ele.disabled ;
}

function resizeIframe(evt) {
    evt = (evt) ? evt : event;
    var target = (evt.target) ? evt.target : evt.srcElement;
    // take care of W3C event processing from iframe's root document
    if (target.nodeType == 9) {
      if (evt.currentTarget && evt.currentTarget.tagName.toLowerCase() == "iframe") {
            target = evt.currentTarget;    
        }
    }
    if (target) {
        adjustIFrameSize(target.id);
    }
}
/* end of code from Goodman */


/*******************************
Stuff a selectList with values.
valueFunc is a function that takes one parameter, an object,
and returns a value for use in the select option value.
textFun is a function that takes one parameter, an object,
and returns a value for use in the select option text.

This is based on the code in DWRUtil but lacks the error checking.
 */
addOptions = function(ele, data, valueFunc, textFunc) {
  var orig = ele;
  ele = $(ele);
  if (ele == null) {
    DWRUtil.debug("addOptions() can't find an element with id: " + orig + ".");
    return;
  }
  var useOptions = DWRUtil._isHTMLElement(ele, "select");  
  if (data == null) return;

  var text;
  var value;
  var opt;
  for (var i = 0; i < data.length; i++) {
    if ( valueFunc != null ){
      if ( textFunc != null) {
        text = textFunc(data[i]);
        value = valueFunc(data[i]);
      }
      else {
        value = valueFunc(data[i]);
        text = value;
      }
    }
    else
      {
        text = textFunc(data[i]);
        value = text;
      }
    if (text || value) {
      opt = new Option(text, value);
      ele.options[ele.options.length] = opt;
    }
  }
}

/*
 returns a string like "http://somehost.edu/vivo/"
*/
getURLandContext = function(){
    if( document == null ) return "could not get document object.";
    var full = document.URL;
    var skip_httpSlashes = 7;
    var index = full.indexOf("/",skip_httpSlashes) + 1;
    if( index == -1 ) { // something like http://somehost.edu:8080
      return full + "/";
    }
    index = full.indexOf("/",index) + 1;
    if( index == -1 ){ // something like http://somehost.edu:8080/
      return full;
    }
    // something like http://somehost.edu:8080/vivo/junk/junk.jsp?2kl3j4=23k
    return full.slice(0,index);
}
