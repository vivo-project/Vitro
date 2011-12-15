/* $This file is distributed under the terms of the license in /doc/license.txt$ */
			var namespaces = {
//                  now handled in GetAllPrefix.java
//					rdf		:	"http://www.w3.org/1999/02/22-rdf-syntax-ns#",
//					rdfs	:	"http://www.w3.org/2000/01/rdf-schema#",
//					xsd		:	"http://www.w3.org/2001/XMLSchema#",
//					owl		:	"http://www.w3.org/2002/07/owl#",
//					swrl	:	"http://www.w3.org/2003/11/swrl#",
//					swrlb	:	"http://www.w3.org/2003/11/swrlb#",
//					vitro	:	"http://vitro.mannlib.cornell.edu/ns/vitro/0.7#"
				};
			
			var level = 0;
			
			function init(){
				var url = "getAllClasses";
				var preurl = "getAllPrefix";

				var base = document.getElementById("subject(0,0)");
				base.level = 0;
				base.count = 0;
				var myAjax = new Ajax.Request( url, {method: "get", parameters: "", onComplete: function(originalRequest){
							var response = originalRequest.responseXML;
							var options = response.getElementsByTagName("option");
//							if (options == null || options.length == 0){
//								alert("Error: Cannot get all the classes.");
//								return;
//							}
							for(i=0; i<options.length; i++){
								base[base.length] = new Option(options[i].childNodes[0].firstChild.data, options[i].childNodes[1].firstChild.data);
							}
							
							var subdiv = document.getElementById("subject(0)");
							subdiv.appendChild(document.createElement("br"));
							
							
							var addprop = document.createElement("input");
							addprop.type = "button";
							addprop.value = "Add Property";
							addprop.level = 0;
							addprop.onclick = function() {
								return getProperty(this);
							}
							subdiv.appendChild(addprop);
							level ++;
						}
					}
				);
				
				var myPrefixAjax = new Ajax.Request( preurl, {method: "get", parameters: "", onComplete: function(originalRequest){
							var response = originalRequest.responseXML;
							var options = response.getElementsByTagName("option");
							if (options == null || options.length == 0) {
								alert("Error: Cannot get all the prefixes.");
								return;
							}
							for(i=0; i<options.length; i++)
								namespaces[options[i].childNodes[0].firstChild.data] = options[i].childNodes[1].firstChild.data;
						}
					}
				);
			}
			
			
			function getProperty(addprop){
				
				var url = "getClazzAllProperties";
				var base = document.getElementById("subject(" + addprop.level + ",0)");
				var subject = base.value;
				if (subject == ""){
					alert("Please select a class.");
				}
				else{
					var params = "vClassURI=" + subject.replace('#', '%23');
					var myAjax = new Ajax.Request( url, {method: "get", parameters: params, onComplete: function(originalRequest){
								var response = originalRequest.responseXML;
								var property = document.createElement("select");
								property.id = "predicate(" + base.level + "," + base.count + ")";
								property[property.length] = new Option("Properties", "");
								var options = response.getElementsByTagName('option');
								if (options == null || options.length == 0){
									alert("Error: Cannot get data properties for " + subject + ".");
									return;
								}
								for(i=0; i<options.length; i++){
									property[property.length] = new Option(options[i].childNodes[0].firstChild.data, options[i].childNodes[1].firstChild.data + options[i].childNodes[2].firstChild.data);
								}
								property.level = base.level;
								property.count = base.count;
								
								property.onchange = function() {
									return getObject(this);
								}
								
								var prediv = document.getElementById("predicate(" + base.level + ")");
								
								if (prediv.innerHTML.trim() != "") {
									var lastNode = prediv.lastChild.previousSibling;
									if (lastNode.selectedIndex == 0){
										alert("You have a undefined property, please make sure it has been initialized.");
										return;
									}
										
								}
								prediv.appendChild(property);
								
								base.count += 1
								prediv.appendChild(document.createElement("br"));
							}
						}
					);
				}
			}
			
			
			function getObject(property){
				var url = "getObjectClasses";
				
				var base = document.getElementById("subject(" + property.level + ",0)")
				var subject = base.value;
				
				//Disable the selection
				property.disabled = true;
				
				//DEL PROPERTY
				var delprop = document.createElement("input");
				delprop.type = "button";
				delprop.value = "Delete";
				delprop.count = base.count - 1;
				delprop.level = base.level;
				delprop.onclick = function() {
					return delProperty(this);
				}
				var prediv = document.getElementById("predicate(" + base.level + ")");
				prediv.insertBefore(delprop, property.nextSibling);
				
				
				var predicate = property.value;
				var type = predicate.charAt(predicate.length-1);
				predicate = predicate.substring(0, predicate.length-1);
				if (type == '0') {
					var objdiv = document.getElementById("object(" + base.level + ")");
					var dataprop = document.createElement("input");
					dataprop.type = "text";
					dataprop.size = 50;
					dataprop.count = base.count - 1;
					dataprop.level = base.level;
					dataprop.id = "object(" + base.level + "," + (base.count - 1) + ")";
					objdiv.appendChild(dataprop);
					objdiv.appendChild(document.createElement("br"));
				}
				else{
				var params = "predicate=" + predicate.replace('#', '%23');
				
				var myAjax = new Ajax.Request( url, {method: "get", parameters: params, onComplete: function(originalRequest){
								var response = originalRequest.responseXML;
								var objdiv = document.getElementById("object(" + base.level + ")");
								var options = response.getElementsByTagName('option');
								if (options == null || options.length == 0){
									alert("Error: Cannot get range classes for " + predicate + ".");
									return;
								}
								var obj = document.getElementById("object(" + base.level + "," + base.count + ")");
								if (obj == null){
									if (options.length > 0){
										obj = document.createElement("select");
										obj[obj.length] = new Option("Classes", "");
										for(i=0; i<options.length; i ++){
											obj[obj.length] = new Option(options[i].childNodes[0].firstChild.data, options[i].childNodes[1].firstChild.data);
										}
										obj.onchange = function(){
											return addClass(this);
										}
										
									}
									else{
										obj = document.createElement("input");
										obj.type = "text";
									}
									obj.id = "object(" + base.level + "," + (base.count - 1) + ")";
									
									obj.level = base.level;
									obj.count = base.count - 1;
									objdiv.appendChild(obj);
									objdiv.appendChild(document.createElement("br"));
								}
								else{
									var objpar = obj.parentNode;
									
									if (options.length > 0){
										var newobj = document.createElement("select");
										newobj[newobj.length] = new Option("Classes", "");
										for(i=0; i<options.length; i ++){
											newobj[newobj.length] = new Option(options[i].firstChild.data, options[i].firstChild.data);
										}
										newobj.onchange = function(){
											return addClass(this);
										}
										
									}
									else{
										newobj = document.createElement("input");
										newobj.type = "text";
									}
									newobj.id = "object(" + base.level + "," + base.count + ")";
									
									newobj.level = base.level;
									newobj.count = base.count;
									objpar.replaceChild(newobj, obj);
								}
							}
						}
					);
				}

			}
			
			function addClass(obj){
				addClazz();
				
				//disable the selection
				obj.disabled = true;
				
				var subject = document.createElement("select");
				
				subject[subject.length] = new Option(obj.options[obj.selectedIndex].text, obj.value);
				subject.disabled = true;
				subject.level = level;
				level ++;
				subject.count = 0;
				subject.id = "subject(" + subject.level + "," + subject.count + ")";
				
				var subdiv = document.getElementById("subject(" + subject.level +")");
				subdiv.appendChild(subject);
				
				var delclazz = document.createElement("input");
				delclazz.type = "button";
				delclazz.value = "Delete";
				delclazz.count = subject.count;
				delclazz.level = subject.level;
				delclazz.onclick = function() {
					return delClazz(this.level);
				}
				subdiv.appendChild(delclazz);
				subdiv.appendChild(document.createElement("br"));
				var addprop = document.createElement("input");
				addprop.type = "button";
				addprop.value = "Add Property";
				addprop.level = subject.level;
				addprop.onclick = function() {
					return getProperty(this);
				}
				subdiv.appendChild(addprop);
				
			}
			
			function addClazz(){
				var builder = document.getElementById("builder");
				
				var clazz = document.createElement("tr");
				clazz.id = "clazz(" + level + ")";
				var subject = document.createElement("td");
				subject.id = "subject(" + level + ")";
				var predicate = document.createElement("td");
				predicate.id = "predicate(" + level + ")";
				var object = document.createElement("td");
				object.id = "object(" + level + ")";
				
				clazz.appendChild(subject);
				clazz.appendChild(predicate);
				clazz.appendChild(object);
				
				builder.appendChild(clazz);
			}
			
			function delClazz(level){
				var clazz = document.getElementById("clazz(" + level +")");
				var builder = document.getElementById("builder");
				builder.removeChild(clazz);
			}
			
			function delProperty(delprop){
				var sub = document.getElementById("predicate(" + delprop.level + "," + delprop.count + ")");
				var obj = document.getElementById("object(" + delprop.level + "," + delprop.count + ")");
				var subdiv = document.getElementById("predicate(" + delprop.level +")");
				var objdiv = document.getElementById("object(" + delprop.level +")");	
				subdiv.removeChild(sub.nextSibling.nextSibling);
				subdiv.removeChild(sub.nextSibling);
				subdiv.removeChild(sub);
				
				objdiv.removeChild(obj.nextSibling);
				objdiv.removeChild(obj);
				
			}
			
			function genQuery(){
				var items = new Array();
				var criterias = new Array();
				var clazz = new Array();
				var number = 0;
				var _sub;
				var _obj;
				
				
				// namespaces shown in the SPARQL query box
				var namespace = getNamespace();
				//var gid = 0;
				for (i=0; i < level; i++){
					var subjects = document.getElementById("subject(" + i + ")");
					if (subjects == null){
						continue;
					}
					var subNodes = subjects.getElementsByTagName("select");
					var sub = subNodes[0].value;
					
					sub = getNameWithPrefix(sub);
					
					if (!clazz[sub.substring(sub.indexOf(":") + 1)]){
						clazz[sub.substring(sub.indexOf(":") + 1)] = 1;
						_sub = sub.substring(sub.indexOf(":") + 1) + 1;
					}
					else{
						_sub = sub.substring(sub.indexOf(":") + 1) + clazz[sub.substring(sub.indexOf(":") + 1)];
					}
					var subname = "?" + _sub;
					//gid++;
					//criterias[criterias.length] = "GRAPH ?g" + gid + " { " + subname + " rdf:type " + sub + " . }";
					criterias[criterias.length] = subname + " rdf:type " + sub + " .";
					
					
					var predicates = document.getElementById("predicate(" + i + ")");
					var preNodes = predicates.getElementsByTagName("select");
					var num = preNodes.length;
					
					for (j=0; j<num; j++){
						//gid++;
						var pre = preNodes[j];
						obj = document.getElementById("object(" + pre.level + "," + pre.count + ")");
						if (obj == null){
							alert("You have a undefined property, please make sure it has been initialized.");
							return;
						}
						pre = pre.value;
						pre = pre.substring(0, pre.length-1);
						pre = getNameWithPrefix(pre);
						if (obj.tagName == "INPUT"){
							var objname = subname + "_" + pre.substring(pre.indexOf(":") + 1);
							
							//criterias[criterias.length] = "GRAPH ?g" + gid + " { " + subname + " " + pre + " " + objname + " . }";
							criterias[criterias.length] = subname + " " + pre + " " + objname + " .";
							items[items.length] = objname;
							if (obj.value != ""){
								criterias[criterias.length] = "FILTER REGEX (str(" + objname + "), '" + obj.value + "', 'i')";
							}
						}
						else{
							
							obj = obj.value;
							obj = getNameWithPrefix(obj);
							if (!clazz[obj.substring(obj.indexOf(":") + 1)]){
								clazz[obj.substring(obj.indexOf(":") + 1)] = 1;
								_obj = obj.substring(obj.indexOf(":") + 1) + 1;
							}
							else{
								number = clazz[obj.substring(obj.indexOf(":") + 1)] + 1;
								clazz[obj.substring(obj.indexOf(":") + 1)] = number
								_obj = obj.substring(obj.indexOf(":") + 1) + number;
							}
							var objname = "?" + _obj;
							//criterias[criterias.length] = "GRAPH ?g" + gid + " { " + subname + " " + pre + " " + objname + " . }";
							criterias[criterias.length] = subname + " " + pre + " " + objname + " .";
						}
					}
					
				}
				if (items.length == 0) {
					var item = "*"
				}
				else{
					var item = "distinct " + items.join(" ");
				}
				var criteria = criterias.join("\n");
				
				var query = namespace+ "SELECT " + item + "\nWHERE{\n" + criteria + "\n}\n";
				var quediv = document.getElementById("sparqlquery");
				var quetextarea = document.getElementById("query");
				quediv.style.visibility = "visible";
				quetextarea.value = query;
			}
			
			function getNamespace(){
				var namespace = "";
				for (key in namespaces){
					namespace += "PREFIX " + key + ": <" + namespaces[key] + ">\n";
				}
				namespace += "\n";
				return namespace;
			}
			function getNameWithPrefix(name){
				for (key in namespaces){
					var index = name.indexOf(namespaces[key]);
					if (index == 0){
						return key + ":" + name.slice(namespaces[key].length);
					}
				}
				return name;
			}