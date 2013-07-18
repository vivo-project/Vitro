<!-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<!-- <script type='text/javascript' src='dojo.js'></script> -->

<script language="JavaScript">
function confirmDelete() {
    var msg="Are you SURE you want to delete this individual? If in doubt, CANCEL."
    return confirm(msg);
}
</script>

<script type='text/javascript' src='dwr/interface/EntityDWR.js'></script>
<script type='text/javascript' src='dwr/engine.js'></script>
<script type='text/javascript' src='dwr/util.js'></script>
<script type='text/javascript' src='js/vitro.js'></script>

<script language="javascript" type="text/javascript">

if( vitroJsLoaded == null ){ 

  alert("seminar.js needs to have the code from vitro.js loaded first"); 

}

// addEvent(window, 'load', monikerInit);

function monikerInit(){

//   if ($('monikerSelect').options.length=1) {
//      $('monikerSelectAlt').disabled = false;
//   }else{
//      $('monikerSelectAlt').disabled = true;
//   }

  $('Moniker').onchange = checkMonikers;  

  update();

}



function update(){ //updates moniker list when type is changed

  DWRUtil.useLoadingMessage();

  EntityDWR.monikers(createList,  document.getElementById("VClassURI").value );
  
}



function createList(data) { //puts options in moniker select list

  fillList("Moniker", data, getCurrentMoniker() );

  var ele = $("Moniker");

  var opt = new Option("none","");
  ele.options[ele.options.length] = opt;

  var opt = new Option("[new moniker]","");
  ele.options[ele.options.length] = opt;

  DWRUtil.setValue("Moniker",getCurrentMoniker()); // getCurrentMoniker() is defined on jsp

  checkMonikers();
}

function getCurrentMoniker() {
	return document.getElementById("MonikerSelectAlt").value;
}


function checkMonikers(){ //checks if monikers is on [new moniker] and enables alt field

  var sel = $('Moniker');  

  if( sel.value == "" || sel.options.length <= 1){
    $('MonikerSelectAlt').disabled = false;

  }else{

    $('MonikerSelectAlt').disabled = true; 

  }        

}



function fillList(id, data, selectedtext) {

  var ele = $(id);

  if (ele == null)    {

    alert("fillList() can't find an element with id: " + id + ".");

    throw id;

  }



  ele.options.length = 0;     // Empty the list

  if (data == null) { return; }



  for (var i = 0; i < data.length; i++)    {

    var text = DWRUtil.toDescriptiveString(data[i]);

    var value = text;



    var opt = new Option(text, value);

    if (selectedtext != null && selectedtext == text){

      opt.selected=true;

    }

    ele.options[ele.options.length] = opt;

  }

}

</script>

<script language="javascript" type="text/javascript" src="js/toggle.js"></script>
<script language="javascript" type="text/javascript" src="js/tiny_mce/tiny_mce.js"></script>
<script language="javascript" type="text/javascript">
	// Notice: The simple theme does not use all options some of them are limited to the advanced theme
	
	tinyMCE.init({
		theme : "advanced",
		mode : "exact",
		elements : "Description",
		theme_advanced_buttons1 : "bold,italic,underline,separator,link,bullist,numlist,separator,sub,sup,charmap,separator,undo,redo,separator,removeformat,cleanup,help,code",
		theme_advanced_buttons2 : "",
		theme_advanced_buttons3 : "",
		theme_advanced_toolbar_location : "top",
		theme_advanced_toolbar_align : "left",
		theme_advanced_resizing : true,
		height : "10",
		width : "95%",
		valid_elements : "a[href|target|name|title],br,p,i,em,cite,strong/b,u,sub,sup,ul,ol,li,h2,h3,h4,h5,h6",
		forced_root_block: false
		
		// elements : "elm1,elm2",
		// save_callback : "customSave",
		// content_css : "example_advanced.css",
		// extended_valid_elements : "a[href|target|name]",
		// plugins : "table",
		// theme_advanced_buttons3_add_before : "tablecontrols,separator",
		// invalid_elements : "li",
		// theme_advanced_styles : "Header 1=header1;Header 2=header2;Header 3=header3;Table Row=tableRow1", // Theme specific setting CSS classes
	});
	
</script>

<script type="text/javascript">
<!--

	/*
	dojo.require("dojo.dom.*");
	*/
	
	nextId=99999;
	// fix this with separate values per type
	
	function getAllChildren(theNode, childArray) {
		var ImmediateChildren = theNode.childNodes;
		if (ImmediateChildren) {
			for (var i=0;i<ImmediateChildren.length;i++) {
				childArray.push(ImmediateChildren[i]);
				getAllChildren(ImmediateChildren[i],childArray);
			}
		}
	}
	
	function addLine(baseNode, typeStr)
	{
		baseId = baseNode.id.substr(0,baseNode.id.length-7);	
		newTaName = document.getElementById(baseId+"genTaName").firstChild.nodeValue;
		theList = document.getElementById(baseId+"ul");
		nextId++;
		TrId = nextId;
		templateRoot = document.getElementById(typeStr+"NN");
		newRow = templateRoot.cloneNode(true);
		// newRow.id = newRow.id.substr(0,newRow.id.length-2)+TrId;
		newRow.id=TrId;
		newRowChildren = new Array();
		getAllChildren(newRow,newRowChildren);
		for (i=0;i<newRowChildren.length;i++) {
			if (newRowChildren[i].id) {
				if(newRowChildren[i].id.substr(0,typeStr.length+2)==typeStr+"NN") {
					newRowChildren[i].id=TrId+newRowChildren[i].id.substr(typeStr.length+2,newRowChildren[i].id.length-typeStr.length-2);
				}
			}
		}
		theList.appendChild(newRow);
		theContent=document.getElementById(TrId+"content");
		theContent.style.display="none";
		theContentValue=document.getElementById(TrId+"contentValue");
		theTaTa = document.getElementById(TrId+"tata");
		theTaTa.name=newTaName;
		theTaTa.style.display="block";
		theTa = document.getElementById(TrId+"ta");
		theTa.style.display="block";
		
		// scroll the window to make sure the user sees our new textarea
		var coors = findPos(theTaTa);
		window.scrollTo(coors[0]-150,coors[1]-150);
		
		// switch the textarea to a TinyMCE instance
		tinyMCE.execCommand('mceAddControl',false,TrId+"tata");			
		
		return false;
	}
	
	function deleteLine(deleteLinkNode, typeStr) {
	
		TrId = deleteLinkNode.id.substr(0,deleteLinkNode.id.length-10);
		clickedRowContentValue = document.getElementById(TrId+"contentValue");
		clickedRowContentValue.style.textDecoration="line-through";
		
		//tinyMCE.execCommand('mceAddControl',false,TrId+'tata');
		//tinyMCE.activeEditor.setContent('', {format : 'raw'});
		//tinyMCE.execCommand('mceSetContent',false,'');
		//tinyMCE.execCommand('mceRemoveControl',false,TrId+'tata');
		
		document.getElementById(TrId+'tata').innerHTML = '';
		clickedRowEditLink = document.getElementById(TrId+"editLink");
		clickedRowEditLink.style.display="none";
		clickedRowDeleteLink = document.getElementById(TrId+"deleteLink");
		clickedRowDeleteLink.style.display="none";
		clickedRowUndeleteLink = document.getElementById(TrId+"undeleteLink");
		clickedRowUndeleteLink.style.display="inline";
	
	}
	
	function undeleteLine(undeleteLinkNode, typeStr) {
		index = undeleteLinkNode.id.substr(0,undeleteLinkNode.id.length-12);
		theContentValue=document.getElementById(index+"contentValue");
		//theContentValueBackup=document.getElementById(index+"contentValueBackup");
		//theContentValue.innerHTML = theContentValueBackup.innerHTML;
		theContentValue.style.textDecoration="none";
		theTaTa = document.getElementById(index+"tata");
		theTaTa.innerHTML=theContentValue.innerHTML;
		clickedRowEditLink = document.getElementById(TrId+"editLink");
		clickedRowEditLink.style.display="inline";
		clickedRowDeleteLink = document.getElementById(TrId+"deleteLink");
		clickedRowDeleteLink.style.display="inline";
		clickedRowUndeleteLink = document.getElementById(TrId+"undeleteLink");
		clickedRowUndeleteLink.style.display="none";
	}
	
	function convertLiToTextarea(linkNode, typeStr) {
		LiId = linkNode.id.substr(0,linkNode.id.length-8);
		theLic = document.getElementById(LiId+"content");
		theLic.style.display="none";
		theTa = document.getElementById(LiId+"ta");
		theTa.style.display="block";
		tinyMCE.execCommand('mceAddControl',false,LiId+'tata');
		return false;
	}
	
	function backToLi(okLinkNode) { 
		index = okLinkNode.id.substr(0,okLinkNode.id.length-6);
		textareaDivId = index+"ta";
		liDivId = index+"contentValue";
		content = tinyMCE.activeEditor.getContent();
		tinyMCE.execCommand('mceRemoveControl',false,textareaDivId+'ta');
		theTextarea = document.getElementById(textareaDivId);
		theTextarea.style.display="none";
		theLi = document.getElementById(liDivId);
		// replace this:
		theLi.innerHTML = content;
		theLi.parentNode.style.display = 'block';
		return false;
	}
	
	function cancelBackToLi(cancelLinkNode) {
		index = cancelLinkNode.id.substr(0,cancelLinkNode.id.length-10);
		textareaDivId = index+"ta";
		liDivId = index+"contentValue";
		tinyMCE.execCommand('mceRemoveControl',false,textareaDivId+'ta');
		theTextarea = document.getElementById(textareaDivId);
		theTextarea.style.display="none";
		theTaTa = document.getElementById(textareaDivId+"ta");
		theLi = document.getElementById(liDivId);
		theLi.parentNode.style.display = 'block';
		theTaTa.innerHTML = theLi.innerHTML;
		// if unused new row, just hide it completely (delete?)
		if (theLi.innerHTML=="") {
			theUnusedRow = document.getElementById(index);
			theUnusedRow.style.display="none";
		}
		return false;
	}
	
	function submitPage() {
		theForm = document.getElementById("editForm");
		theButtonWeWantToClick = document.getElementById("primaryAction");
		theHiddenInput = document.createElement("input")
		theHiddenInput.type="hidden";
		theHiddenInput.name=theButtonWeWantToClick.name;
		theHiddenInput.value=theButtonWeWantToClick.name;
		theForm.appendChild(theHiddenInput);
		theForm.submit();
	}
		
	function findPos(obj)
	{
		var curleft = curtop = 0;
		if (obj.offsetParent) {
			curleft = obj.offsetLeft
			curtop = obj.offsetTop
			while (obj = obj.offsetParent) {
				curleft += obj.offsetLeft
				curtop += obj.offsetTop
			}
		}
		return [curleft,curtop];
	}
-->

    // -------------------------------------------------------------------------------
    // Using jQuery to step in for DWR and provide original moniker selection behavior
    // -------------------------------------------------------------------------------
    var monikerSelection = {
        // onChange event listener for moniker select list
        monikerListener: function() {
            $('#Moniker').change(function() {
                // alert('The moniker has changed');
                // if "[new moniker]" is selected, then enable the alt field
                if ( $('#Moniker option:selected').text() == "[new moniker]" ) {
                    $('#MonikerSelectAlt').removeAttr('disabled');
                } else {
                    $('#MonikerSelectAlt').val('');
                    $('#MonikerSelectAlt').attr('disabled', 'disabled');
                }
            });
        }
    }

    $(document).ready(function() {
        monikerSelection.monikerListener();
    });
</script>