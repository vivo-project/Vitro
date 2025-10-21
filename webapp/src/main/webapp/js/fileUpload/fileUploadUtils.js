/* $This file is distributed under the terms of the license in LICENSE$ */

$(document).ready(function(){

	var xpath = "//attribute::href[contains(., '/uploadFile')]";
	var result = document.evaluate(xpath, document, null, XPathResult.ANY_TYPE, null);
	var node = null;
	while (node = result.iterateNext()) {
		if(isDeleteUploadFile(node)){
			$(node.ownerElement).on("click", function(){
      			var answer = confirm(i18n_confirmDeleteUploadedFile);
      			return answer;
  			});	
		} else if (isUploadFile(node)){
			$(node.ownerElement).on("click", function(){
      			uploadFileRequest(event.target);
      			return false;
  			});
		}
	}
});

function isDeleteUploadFile(node){
	var url = node.nodeValue;
	if (url.match("&action=delete")){
		return true;
	}
	return false;
}

function isUploadFile(node){
	var url = node.nodeValue;
	if (url.match("&action=upload")){
		return true;
	}
	return false;
}

function uploadFileRequest(node){
	var aElement = node.parentElement;
	var form = document.createElement("form");
	form.setAttribute("method", "post");
	form.setAttribute("action", aElement.href);
	form.setAttribute("enctype","multipart/form-data");
	form.setAttribute("role","form");
	document.body.insertBefore(form, null);
	var inputFile = document.createElement("input");
	inputFile.type = "file";
	inputFile.name = "datafile";
	var inputId = "fileUploadInput" + Math.floor(Math.random() * 1000000);
	inputFile.setAttribute("id", inputId);
	inputFile.setAttribute("style", "display:none;");
	form.insertBefore(inputFile, null);
	inputFile.trigger("click");
	inputFile.addEventListener("change", onFileSelect);
}

function onFileSelect(e) {
	e.target.parentElement.trigger("submit");
	}

