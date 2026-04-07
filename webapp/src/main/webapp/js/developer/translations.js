class PropAddr {
	constructor(node, number, args) {
		this.node = node;
		this.number = number;
		this.args = args;
	}
}

class PropInfo {
	constructor(rawText, formText, address) {
		this.rawText = rawText;
		this.formText = formText;
		this.addresses = [];
		this.addresses.push(address);
	}
}

	var pageTranslations = new Map();
	var overridenTranslations = new Map();
	var startSep = '\u25a4';
	var endSep = '\u25a5';
	var intSep = '\u25a6';
	var resultSep = '\u200b\uFEFF\u200b\uFEFF\u200b';
	var resultSepChars = '\u200b\uFEFF';

	function saveTranslations() {
		var storage = window.localStorage;
		var serializedTranslations = JSON.stringify(Array.from(overridenTranslations.entries()));
		storage.setItem("overridenTranslations", serializedTranslations);
	}

	function readTranslations() {
		var storage = window.localStorage;
		var serializedTranslations = storage.getItem("overridenTranslations");
		if (serializedTranslations != null) {
			overridenTranslations = new Map(JSON.parse(serializedTranslations));
		}
	}

	function createTranslationPanel() {
		var devPanel = document.getElementById("developerPanel");
		if (devPanel !== null) {
			var container = document.createElement("div");
			container.setAttribute("id", "translationPanel");
			container.setAttribute("style", "font-size:0.8em !important;width: 440px; resize: horizontal; \
			overflow: auto; padding: 10px; position: absolute;background-color:#f7dd8a;border:1px dotted;z-index:10000");
			devPanel.parentNode.insertBefore(container, devPanel.nextSibling);
			createTranslationControls(container);
			createPageTranslationsTable(container);
		}
	}

	function createTranslationControls(container) {
		var controls = document.createElement("div");
		controls.setAttribute("id", "translationControls");
		controls.setAttribute("style", "margin-bottom:8px;")
		container.appendChild(controls);

		var cleanButton = document.createElement("button");
		cleanButton.textContent = "Clean All";
		cleanButton.setAttribute("onclick", "cleanTranslationStorage()");
		cleanButton.setAttribute("style", "margin-right:10px;");
		controls.appendChild(cleanButton);

		var exportAllButton = document.createElement("button");
		exportAllButton.textContent = "Export All";
		exportAllButton.setAttribute("onclick", "exportTranslations()");
		exportAllButton.setAttribute("style", "margin-right:10px;");
		controls.appendChild(exportAllButton);

		var updateFileInput = document.createElement("input");
		var updateFileButton = document.createElement("button");
		updateFileButton.setAttribute("style", "margin-right:10px;");
		updateFileInput.type = "file";
		updateFileInput.setAttribute("id", "exportFile");
		updateFileInput.setAttribute("style", "display:none;");
		updateFileInput.setAttribute("accept", ".properties");
		var updateFileLabel = document.createElement("label");
		updateFileLabel.setAttribute("for", "exportFile");
		updateFileLabel.textContent = "Update file";
		updateFileLabel.setAttribute("style", "margin:0px;color:black;")
		updateFileButton.appendChild(updateFileLabel);
		controls.appendChild(updateFileButton);
		controls.appendChild(updateFileInput);
		updateFileInput.addEventListener("change", updateTranslationsFile);

		var importFileInput = document.createElement("input");
		var importFileButton = document.createElement("button");
		importFileInput.type = "file";
		importFileInput.setAttribute("style", "display:none;");
		importFileInput.setAttribute("id", "importFile");
		importFileInput.setAttribute("accept", ".properties");
		var importFileLabel = document.createElement("label");
		importFileLabel.setAttribute("style", "margin:0px;color:black;")
		importFileLabel.setAttribute("for", "importFile");
		importFileLabel.textContent = "Import from file";
		importFileButton.appendChild(importFileLabel);
		controls.appendChild(importFileButton);
		controls.appendChild(importFileInput);
		importFileInput.addEventListener("change", importTranslationsFromFile);
	}

	function cleanTranslationStorage() {
		overridenTranslations.clear();
		saveTranslations();
		location.reload();
	}

	function importTranslationsFromFile(e) {
		const fileList = e.target.files;
		const numFiles = fileList.length;
		if (numFiles > 0) {
			const file = fileList[0];
			var reader = new FileReader();
			reader.onload = function(progressEvent) {
				var lines = this.result.split(/\r\n|\n\r|\n|\r/);
				var followLine = false;
				var lineKey = null;
				var lineValue = null;
				for (var i = 0; i < lines.length; i++) {
					if (!isCommentLine(lines[i])) {
						if (followLine) {
							followLine = isNextLineFollow(lines[i]);
							lineValue = lines[i].replace(/\\$/, "");
							lineValue = unescapeHTML(lineValue);
							lineValue = charCodesToString(lineValue);
							overridenTranslations.set(lineKey, overridenTranslations.get(lineKey) + lineValue);
						} else {
							followLine = isNextLineFollow(lines[i]);
							lineKey = getLineKey(lines[i]);
							if (lineKey.trim() != "") {
								lineValue = getLineValue(lines[i]);
								lineValue = unescapeHTML(lineValue);
								lineValue = charCodesToString(lineValue);
								overridenTranslations.set(lineKey, lineValue);
							}
						}
					}
				}
				saveTranslations();
				location.reload()
			}
			reader.readAsText(file);
		}
	}

	function updateTranslationsFile(e) {
		const fileList = e.target.files;
		const numFiles = fileList.length;
		if (numFiles > 0) {
			const file = fileList[0];
			var fileName = e.target.value.split(/(\\|\/)/g).pop();
			var reader = new FileReader();
			reader.onload = function(progressEvent) {
				var lines = this.result.split(/\r\n|\n\r|\n|\r/);
				var followLine = false;
				var keyLineHasChanged = false;
				var lineKey = null;
				for (var i = 0; i < lines.length; i++) {
					if (!isCommentLine(lines[i])) {
						if (followLine) {
							followLine = isNextLineFollow(lines[i]);
							if (keyLineHasChanged) {
								//clean line as it's upper content has changed
								lines[i] = "";
								if (!followLine) {
									keyLineHasChanged = false;
								}
							}
							// skip line						
						} else {
							keyLineHasChanged = false;
							followLine = isNextLineFollow(lines[i]);
							lineKey = getLineKey(lines[i]);
							if (overridenTranslations.has(lineKey)) {
								var value = overridenTranslations.get(lineKey);
								value = toCharCodes(value);
								value = escapeHTML(value);
								lines[i] = lineKey + " = " + value;
								keyLineHasChanged = true;
							}
						}
					}
				}
				saveFile(fileName, lines);
			}
			reader.readAsText(file);
		}
	}

	function exportTranslations() {
		var date = new Date;
		var fileName = "export_" + date.toLocaleString() + "_all.properties";
		var lines = [];
		var storeValue = null;
		for (let [key, value] of overridenTranslations) {
			storeValue = toCharCodes(value);
			storeValue = escapeHTML(storeValue);
			lines.push(key + " = " + storeValue);
		}
		saveFile(fileName, lines);
	}

	function saveFile(fileName, lines) {
		var blob = new Blob([lines.join("\n")], { type: 'text/plain;charset=utf-8' });
		saveAs(blob, fileName);
	}

	function getLineKey(line) {
		var matches = line.match(/^\s*[^=\s]*(?=\s*=)/);
		var key;
		if (matches == null) {
			key = "";
		} else {
			key = matches[0].trim();
		}
		return key;
	}

	function getLineValue(line) {
		var value = line.replace(/^\s*[^=\s]*\s*=\s*/, "");
		value = value.replace(/\\$/, "");
		return value;
	}

	function isNextLineFollow(line) {
		return line.match(/\\(\\\\)*$/) != null;
	}

	function isCommentLine(line) {
		return line.match(/^\s*[#!]/) != null;
	}

	function createPageTranslationsTable(container) {
		var table = document.createElement("table");
		table.setAttribute("id", "translationsTable");
		table.setAttribute("style", "width:100%;");

		document.getElementById("translationPanel").appendChild(table);
		for (let [key, propInfo] of pageTranslations) {
			var tr = document.createElement("tr");
			table.appendChild(tr);
			var td1 = document.createElement("td");
			td1.setAttribute("style", " width:1%;white-space:nowrap;");
			var keyText = document.createTextNode(key);
			var td2 = document.createElement("td");
			var rawText = document.createElement("input");
			rawText.setAttribute("style", "width:100%; ");
			if (overridenTranslations.has(key)) {
				rawText.value = overridenTranslations.get(key);
				rawText.style.backgroundColor = "#536520";
			} else {
				rawText.value = propInfo.rawText;
			}
			var rawTextHidden = document.createElement("input");
			rawTextHidden.setAttribute("style", "display:none;");
			rawTextHidden.value = propInfo.rawText;
			td1.appendChild(keyText);
			tr.appendChild(td1);
			td2.appendChild(rawText);
			td2.appendChild(rawTextHidden);
			tr.appendChild(td2);
			rawText.addEventListener("blur", function() {
				updateTranslation(this);
			});
		}
	}

	function updateTranslation(input) {
		if (input.value != input.nextSibling.value) {
			var key = input.parentElement.previousSibling.firstChild.textContent;
			if (input.value == "") {
				input.value = input.nextSibling.value;
				input.style.backgroundColor = "white";
				overridenTranslations.delete(key);
				var value = input.nextSibling.value;
			} else {
				var value = input.value;
				if (pageTranslations.get(key).rawText != escapeHTML(value)) {
					input.style.backgroundColor = "#536520";
					overridenTranslations.set(key, value);
				} else {
					input.style.backgroundColor = "white";
					overridenTranslations.delete(key);
				}
			}
			saveTranslations();
			if (isJSHasChanged(key)) {
				location.reload();
			}
			updateTranslationOnPage(key, value);
		}
	}

	function isJSHasChanged(key) {
		var result = false;
		if (pageTranslations.has(key)) {
			var addresses = pageTranslations.get(key).addresses;
			for (let i = 0; i < addresses.length; i++) {
				var nodeName = addresses[i].node.nodeName;
				if (nodeName == "SCRIPT") {
					result = true;
				}
			}
		}
		return result;
	}

	function updateTranslationOnPage(key, value) {
		var propInfo = pageTranslations.get(key);
		var addresses = propInfo.addresses;
		for (let i = 0; i < addresses.length; i++) {
			var node = addresses[i].node;
			var number = addresses[i].number + 1;
			var content = node.textContent;
			var formattedValue = formatTranslation(value, addresses[i].args);
			var regexStr = resultSep + "[^" + resultSepChars + "]*" + resultSep;
			const regEx = new RegExp("^(?:[^" + resultSepChars + "]*" + regexStr + "){" + number + "}");
			var newString = content.replace(regEx,
				function(x) {
					return x.replace(RegExp(regexStr + "$"), resultSep + formattedValue + resultSep);
				});
			node.textContent = newString;
		}
	}

	function formatTranslation(value, args) {
		for (let i = 0; i < args.length; i++) {
			value = value.replaceAll("{" + i + "}", args[i]);
		}
		return value;
	}

	function parseHTMLTranslations() {
		var translatedTexts = [];
		var translatedAttrs = [];
		var xpath = "//attribute::*[contains(., '" + startSep + "')]";
		var result = document.evaluate(xpath, document, null, XPathResult.ANY_TYPE, null);
		var node = null;
		var node = null;
		while (node = result.iterateNext()) {
			translatedAttrs.push(node);
			parsePropsInNode(node);
		}
		xpath = "//*[text()[contains(.,'" + startSep + "')]]";
		result = document.evaluate(xpath, document, null, XPathResult.ANY_TYPE, null);
		while (node = result.iterateNext()) {
			translatedTexts.push(node);
			parsePropsInNode(node);
		}
		readTranslations();
		removePropInfoFromPage();
		updatePageWithOverridenTranslations();
		reloadJS();
	}

	function reloadJS() {
		var scriptBlocks = document.getElementsByTagName('script');
		for (let i = 0; i < scriptBlocks.length; i++) {
			var scriptBlock = scriptBlocks[i];
			if (scriptBlock.hasAttribute("src")) {
				var srcAttr = scriptBlock.getAttribute("src");
				if (!srcAttr.includes("translations.js")) {
					if (srcAttr.indexOf("?") == -1) {
						srcAttr += "?" + new Date().getTime();
					} else {
						srcAttr += "&" + new Date().getTime();
					}
					scriptBlock.remove();
					addJSLink(srcAttr);
				}
			} else {
				var content = scriptBlock.textContent;
				scriptBlock.remove();
				addInlineJS(content);
			}
		}
	}

	function addInlineJS(content) {
		var head = document.getElementsByTagName('head')[0];
		var script = document.createElement('script');
		script.textContent = content;
		head.appendChild(script);
	}

	function addJSLink(fileUrl) {
		var head = document.getElementsByTagName('head')[0];
		var script = document.createElement('script');
		script.type = "text/javascript";
		script.src = fileUrl;
		head.appendChild(script);
	}

	function updatePageWithOverridenTranslations() {
		for (let [key, value] of overridenTranslations) {
			if (pageTranslations.has(key)) {
				updateTranslationOnPage(key, value);
			}
		}
	}

	function removePropInfoFromPage() {
		for (let [key, propInfo] of pageTranslations) {
			var addresses = propInfo.addresses;
			for (let i = 0; i < addresses.length; i++) {
				var node = addresses[i].node;
				var content = node.textContent;
				var regexStr = startSep + "[^" + endSep + "]*" + intSep + "([^" + endSep + intSep + "]*)" + endSep;
				const regEx = new RegExp(regexStr, "g");
				var newString = content.replaceAll(regEx, resultSep + "$1" + resultSep);
				node.textContent = newString;
			}
		}
	}

	function parsePropsInNode(node) {
     
		if (node.nodeType === 1){
			var childs = node.childNodes;
			childs.forEach(function(child){
				if (child.nodeType === 3){
					parsePropsInTextNode(child);
				}
			});
		}else if(node.nodeType === 2){
			parsePropsInTextNode(node);	
		}
		
	}
	function parsePropsInTextNode(node){
		var i = 0;
		var textString = node.textContent;
		while (textString.indexOf(startSep) >= 0) {
			textString = textString.substring(textString.indexOf(startSep) + startSep.length);
			var prop = textString.substring(0, textString.indexOf(endSep));
			var address = new PropAddr(node, i, []);
			addToPageTranslations(prop, address);
			i++;
		}
	}

	function addToPageTranslations(prop, address) {
		var key = prop.substring(0, prop.indexOf(intSep));
		prop = prop.substring(prop.indexOf(intSep) + intSep.length);
		var rawText = prop.substring(0, prop.indexOf(intSep));
		prop = prop.substring(prop.indexOf(intSep) + intSep.length);
		var textArgs = [];
		while (prop.indexOf(intSep) >= 0) {
			var textArg = prop.substring(0, prop.indexOf(intSep));
			prop = prop.substring(prop.indexOf(intSep) + intSep.length);
			textArgs.push(textArg);
		}
		address.args = textArgs;
		var formText = prop;
		var propInfo = null;
		if (pageTranslations.has(key)) {
			propInfo = pageTranslations.get(key);
			propInfo.addresses.push(address);
		} else {
			propInfo = new PropInfo(rawText, formText, address);
			pageTranslations.set(key, propInfo);
		}
	}
	function toCharCodes(input) {
		return input
			.replace(/^\ /, "\\u0020")
			.replace(/\ $/, "\\u0020");
	}
	function charCodesToString(input) {
		return input.replace(/\\u[\dA-F]{4}/gi,
			function(match) {
				return String.fromCharCode(parseInt(match.replace(/\\u/g, ''), 16));
			});
	}
	function escapeHTML(input) {
		return input
		.replace(/&/g, "&amp;")
		.replace(/</g, "&lt;")
		.replace(/>/g, "&gt;")
		.replace(/"/g, "&quot;")
		.replace(/'/g, "&#039;")
		.replace(/‘/g, "&lsquo;")
		.replace(/’/g, "&rsquo;");
	}
	function unescapeHTML(input) {
		return input
		.replace(/&amp;/g, "&")
		.replace(/&lt;/g, "<")
		.replace(/&gt;/g, ">")
		.replace(/&quot;/g, "\"")
		.replace(/&#039;/g, "'")
		.replace(/&lsquo;/g, "‘")
		.replace(/&rsquo;/g, "’");
	}
	

window.addEventListener('load', function() {
	setTimeout(function() {
		var developerSetting = document.getElementById("developer_i18n_onlineTranslation");
		if (developerSetting !== null && developerSetting.checked) {
			parseHTMLTranslations();
			createTranslationPanel();
		}
	}, 1000);
})
