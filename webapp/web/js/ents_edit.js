/* $This file is distributed under the terms of the license in /doc/license.txt$ */

/* this code uses:
   dwrutil in util.js
   vitro.js
   detect.js
*/
dojo.require("dojo.io.*");
dojo.require("dojo.event.*");

//DWREngine.setErrorHandler(function(data){alert("DWREngine error: " + data);});
//DWREngine.setWarningHandler(function(data){alert("DWREngine warning: " + data);});

/*
  ents_edit.js has several tasks:
  1) fill up the property table with one row for each ents2ents record
  2) delete ents2ents if requested to
  3) add a form to allow a new ents2ents relations to be created
  4) bring up a form to edit an existing ents2ents

  The html form for tasks 3 and 4 is from a div with id="propertydiv" on
  the ents_edit.JSP ( <-- notice, it's on the JSP ).  See the js function
  getForm().  

  There was a problem where the DWR calls were throwing error messages
  for larger (but not unreasonably large) sets of entity data.  Updating to DWR
  2.0rc2.8 did not fix this.  The current solution is a servlet that
  returns a JSON string for just the entitiy list.

  DWR is still used to get info about the entity and the list of ents2ents.

  CHANGES
  2006-11-21 bdc34: removing the 'new entity' button, added comments
 */

var gEntityUri; //used to hold the URI of the entity being edited 
var gProperty; // used to hold property on form
var gEntity; //entity that this page is editing
var editingNewProp = false; //true when editing a new property

var gVClassUri = null;

//hash: PropertyDWR.propertyId+"D" or "R" (for domain or range)  -->  obj: PropertyDWR
var gPropertyHash; 

//holds the xhtmlrequest so we can do an abort
var gEntRequest = null;

var justwritenProp = null;
var justwritenTr   = null;

if( vitroJsLoaded === undefined || vitroJsLoaded === null ){ 
  alert("ents_edit.js needs to have the code from vitro.js loaded first"); 
}

var rowId = 1;

function getNextRowId() {
   rowId++;
   return rowId;
}

/** This refreshes the dynamic parts of the page.
    It is also called on page load. */
function update( )   {
  gEntityUri = getEntityUriFromPage();
  abortExistingEntRequest();
  clearProp();
  updateTable();
  updateEntityAndPropHash();
  var but = $("newPropButton");
  if( but ) { but.disabled = false; }
}

/** called when the property editing form is dismissed */
function clearProp()  {
  abortExistingEntRequest();
  var clearMap={sunrise:"",sunset: ""};
  editingNewProp = false;
  gVClassUri = null;
  DWRUtil.setValues( clearMap );
  clear($("propertyList"));
  clear($("vClassList"));

  var ele = $("entitiesList");
  while( ele != null && ele.length > 0){
    ele.remove(0);
  }
}

/*****************************************************************
 * This updates the gPropertyHash and gEntity.
 *****************************************************************/
function updateEntityAndPropHash(){
  gPropertyHash = null;
  gProperty = null;
  gEntity = null;

  /* This is the method that is called to set gEntity and then get ents2ents properties */
  var setGEntity = function(entityObj){
    gEntity=entityObj;
    //once we set the gEntity we can get the ents2ents properties.
    //PropertyDWR.getAllPropInstByVClass(setPropertyHash, entityObj.VClassURI );
    if( entityObj != null && 'URI' in entityObj )
        PropertyDWR.getAllPossiblePropInstForIndividual(setPropertyHash, entityObj.URI);
    //else
        //alert("could not find an individual, usually this means that there is no type for the URI"); 
  };
  
  /* This is the method that builds the gPropertyHash. It is called by setGEntity() */
  var setPropertyHash = function(propArray) {  
    if( propArray == null || propArray.length < 1  ) {
    	gPropertyHash = null; /* propArray might be null if we have no properties in the system */
    }else{
    	gPropertyHash = new Object();
    	for(var i = 0; i < propArray.length; i++) {
          var hashid = propArray[i]["propertyURI"] ;
          if( propArray[i].subjectSide ){
            hashid = hashid + 'D';
          } else {
            hashid = hashid + 'R';
          }            
          gPropertyHash[ hashid ] = propArray[i];
    	}
    }
  };
  
  //get the gEntity and then build the gPropertyHash
  EntityDWR.entityByURI(setGEntity, gEntityURI);
}

/*****************************************************************
 set up some kind of warning that there are no properties in the system
*****************************************************************/
function doNoPropsWarning(){
	alert(
		"There are no object properties in the system.\n\n"+
		"This is most likely because you have just finished installing. "+
		"The button you have clicked adds a new object property statement relating this individual " +
		"to another individual.  If you are looking to add new object properties to the " +
		"system, look under the 'About' menu.");
	var but = $("newPropButton");
  	if( but ) { but.disabled = true; }
}

/*****************************************************************
because dwr calls are async we have a callback 
*****************************************************************/
function updateTable(callback) {
  var cb = callback;
  function fillTable(props)  {
     DWRUtil.removeAllRows("propbody");
     /* This makes the row that gets added to the ents_edit form for each ents2ents object.  */
     addRows($("propbody"), props,
             [getDomain, getProperty, getRange, getEdit, getDelete],
             makeTr);  
     var newPropTr = $("justwritenTr");
     if( newPropTr != null ){
       Fat.fade_element( "justwritenTr" );
     }
     if(cb !== undefined && cb !== null ) { cb(); }
  }
  PropertyDWR.getExistingProperties(fillTable,gEntityURI);
}

/**************************************************************************/
/* ******************** table column builders *****************************/
/* Change these functions to change the way the html table gets built *****/
/* in the addRows() func each of these will get called and wrapped in a <tr>
   to make the row to stick into the table */
var getDomain = function(prop)  {
  return prop.subjectName;
};

var getProperty = function(prop) {
    return makePropLinkElement(prop.propertyURI, prop.domainPublic);
};

var getRange = function(prop) {
  return  makeEntLinkElement(prop.objectEntURI, prop.objectName);
};

var getDelete = function(prop)  {
  var quote = new RegExp('\'','g');
  var dquote = new RegExp('"','g');
  return '<input type="button" value="Delete" class="form-button" ' +
  'onclick="deleteProp(' + 
  '\'' + cleanForString(prop.subjectEntURI) + '\', ' +
  '\'' + cleanForString(prop.propertyURI)    + '\', ' +
  '\'' + cleanForString(prop.objectEntURI)   + '\', ' +
  '\'' + cleanForString(prop.objectName)     + '\', ' +
  '\'' + cleanForString(prop.domainPublic)   + '\' )">';
};

var getEdit = function(prop){
  var quote = new RegExp('\'','g');
  return '<input type="button" value="Edit" class="form-button" '+
    'onclick="editTable(this,' +
         '\''+ cleanForString(prop.subjectEntURI)+'\', ' +
         '\''+ cleanForString(prop.propertyURI)  +'\', ' +
         '\''+ cleanForString(prop.objectEntURI) +'\')">';
};

var quote = new RegExp('\'','g');
var dquote = new RegExp('"','g');

function cleanForString(strIn){
  var strOut = strIn.replace(quote, '\\\'');
  strOut = strOut.replace(dquote, '&quot;');
  return strOut;
}

/****************************************************************
This makeTr is a function with access to a closure that
   includes the vars previousPropName and currentClass.
   All of this work is to get the table row color to change when
   we start drawing a row for a property that is different than the
   previous row.  
*****************************************************************/
var makeTr = (function(){ /* outer func */
    // the reason for the outer func is to capture these values in a closure
    // This allows us to have a func that has state associated with it
    var previousPropName = "-1";
    var currentClass = "form-row-even";

    //each time makeTr() is called this is the code that gets executed.
    return (function(prop) {/* inner func */
        if( getProperty(prop) != previousPropName ){
          previousPropName = getProperty(prop);
          currentClass=(currentClass=="form-row-even"?"form-row-odd":"form-row-even");
        }
        tr = document.createElement("tr");
        //tr.id = "proprow" + prop.ents2entsId;
        tr.className = currentClass;
        if( justwritenProp != null &&
                justwritenProp.subjectEntURI == prop.subjectEntURI &&
                justwritenProp.propertyURI == prop.propertyURI &&
                justwritenProp.objectEntURI == prop.objectEntURI){
            tr.id ="justwritenTr";
            justwritenProp = null;
        }
        return tr;
      });/*close inner function*/
  } )(); /*close and call outer function to return the inner function */

/* **************** end table column builders **************** */

/****************************************************************
 ******** Functions for the Property Editing Form **************
 *****************************************************************/

/* called when Edit button is clicked */
function editTable(inputElement, subjectURI, predicateURI, objectURI){
    var rowIndex = inputElement.parentNode.parentNode.rowIndex-1 ;
    var table = $("propbody");
    table.deleteRow( rowIndex );
    table.insertRow(rowIndex);
    table.rows[rowIndex].insertCell(0);
    var vform = getForm();
    table.rows[rowIndex].cells[0].appendChild( vform );
    table.rows[rowIndex].cells[0].colSpan = tableMaxColspan( table );
    vform.style.display="block";

    PropertyDWR.getProperty(fillForm, subjectURI, predicateURI, objectURI);
    inputElement.parentNode.parentNode.style.display="none";
}

/** called when Delete button is clicked */
function deleteProp( subjectURI, predicateURI, objectURI, objectName, predicateName)  {
  if( PropertyDWR.deleteProp ){
    if (confirm("Are you sure you want to delete the property\n"
                 + objectName + " " + predicateName + "?")) {
      PropertyDWR.deleteProp(update, subjectURI, predicateURI, objectURI);
    }
  } else {
    alert("Deletion of object property statements is disabled.");
  }
}

/*****************************************************************
 adds the editing form <tr> to the propeties table 
*****************************************************************/
function appendPropForm(tr, colspan ){
  var form = getForm();
  while(tr.cells.length > 0 ){
    tr.deleteCell(0);
  }
  var td = tr.insertCell(-1);
  td.appendChild( form );

  if( colspan !== undefined && colspan > 0){ 
      td.colSpan=colspan; 
  }
  form.style.display="block";
}

/*****************************************************************
This gets called when the button to make a new property 
for the entity is pressed. 
*****************************************************************/
function newProp() {
  if( gPropertyHash == null || gPropertyHash.length < 1 ) {
    	/* propArray might be null if we have no properties in the system */
    	doNoPropsWarning();    	
    }else{
  	var innerNew = function (){
      var newP = {};
    	newP.domainClass = gEntity.vClassId;
    	newP.subjectName = gEntity.name;
    	newP.subjectEntURI = gEntity.URI;
        
    	fillForm( newP );
    	editingNewProp = true;
    	fillRangeVClassList();
    	var table = $("propbody");
        var tr = table.insertRow(0);
    	tr.id = "newrow";
    	appendPropForm( tr, tableMaxColspan( table ) );
 
      //table.insertBefore(tr, table.rows[0]);
  	};
  	updateTable( innerNew );
  }
}

/****************************************************************
 Fills out the property edit form with the given property 
*****************************************************************/
function fillForm(aprop)  {
  clearProp();
  gProperty = aprop;
  var vclass = gProperty.domainClass;
  
  DWRUtil.setValues(gProperty);

  toggleDisabled("newPropButton");
  
  fillPropList(vclass); // this will also fill the vclass and ents lists      
}

/****************************************************************
 This will fill the select list will all of the properties found
 in  the gPropertyHash and then trigger a update of the vClasList 
******************************************************************/
function fillPropList(classId) {  
  /* This function fills up the form's select list with options 
     Notice that the option id is the propertyid + 'D' or 'R'
     so that domain and range properties can be distinguished  */
  var propList = $("propertyList");
  clear(propList);

  //add properties as options
  for( var i in gPropertyHash ) {
    var prop = gPropertyHash[i];
    var text = "";
    var value = prop.propertyURI;
    if(prop.subjectSide){
      text= prop.domainPublic;
      if (prop.rangeClassName != null) {
    	  text += " ("+prop.rangeClassName +")";
      }
      value = value + 'D';
    } else {
      text= prop.rangePublic;
      if (prop.domainClassName != null) {
    	  text += " ("+prop.domainClassName+")";
      }
      value = value + 'R';
    }
    var opt = new Option(text, value);
    if( gProperty.propertyURI == prop.propertyURI ){
      opt.selected = true; 
    }
    propList.options[propList.options.length] = opt;
  }

  fillRangeVClassList( null );
}

/*****************************************************************
 Fill up the range VClass list on the property editing form. 
 If propId is null then the one on the property select list will be used
*****************************************************************/
function fillRangeVClassList( propId ){
  //If propId is null then the one on the property select list will be used
  if( propId == null ) { propId = DWRUtil.getValue("propertyList");}
	
  //clear the list and put the loading message up
  var vclassListEle = $("vClassList");
  clear(vclassListEle);
  vclassListEle.options[vclassListEle.options.length] = new Option("Loading...",-10);
  //vclassListEle.options[0].selected = true;
  //vclassListEle.options[vclassListEle.options.length] = new Option("Crapping...",-15);
	
  var prop = gPropertyHash[propId];	
	
  VClassDWR.getVClasses(
                        function(vclasses){ 
                          addVClassOptions( vclasses );             
                        },
                        prop.domainClassURI, prop.propertyURI, prop.subjectSide);
}		
	
/****************************************************************
 Adds vClasses to the vClassList and trigger an update of
 the entitiesList.
 ****************************************************************/
function addVClassOptions( vclassArray  ){
  //  DWRUtil.addOptions("entitiesList",null);
  var vclassEle = $("vClassList");
  clear( vclassEle );
  vclassEle.disabled = false;

  if( vclassArray == null || vclassArray.length < 1){
    vclassEle.disabled = true;
    entsEle = $("entitiesList");
    clear(entsEle);
    var msg="There are no entities defined yet that could fill this role";
    var opt = new Option(msg,-1);
    entsEle.options[entsEle.options.length] = opt;
    entsEle.disabled = true;
    return;
  }

  addOptions("vClassList", vclassArray,
             function(vclass){ return vclass.URI; },
             function(vclass){ 
               var count = "";
               if( vclass.entityCount != null && 
                   vclass.entityCount >= 0){
                 count = " ("+vclass.entityCount+")";
               }
               return vclass.name+count;
             });
               
  //attempt to set the selected option to the current vclass
  var vclassURI = null;
  var prop = gPropertyHash[ DWRUtil.getValue("propertyList") ];
  if( gProperty.propertyURI == prop.propertyURI ){
    vclassURI = gProperty.rangeClassURI;

    DWRUtil.setValue(vclassEle, vclassURI );
    //here we were unable to set the vclass select option to the vclassid
    //of the entity.  this means that the vclass of the entity is not one that
    //is permited by the PropertyInheritance and other restrictions.
    if( DWRUtil.getValue(vclassEle) != vclassURI){
        alert("This entity's class does not match the class of this property.  This is usually the "+
              "result of the class of the entity having been changed.  Properties that were added when " +
              "this entity had the old class still reflect that value.\n" +
              "In general the vitro system handles this but the editing of these misaligned records "+
              "is not fully supported.\n" );
    }
  }
  fillEntsList(vclassEle );
}

/*****************************************************************
  Fill up the entity list in a property editing form.
  The propId should have the id + 'D' or 'R' to 
  indicate domain or range. 
*****************************************************************/
function fillEntsList( vclassEle ){
  if( vclassEle == null )
      vclassEle = $("vClassList");     
  var vclassUri =  DWRUtil.getValue( vclassEle );

  if( vclassUri == gVClassUri )
    return;
  else
    gVClassUri = vclassUri;
    
  var entsListEle = $("entitiesList");
  clear(entsListEle);
  entityOptToSelect = null;
    
  entsListEle.disabled = true;
  entsListEle.options[entsListEle.options.length] = new Option("Loading...",-12);
  entsListEle.options[entsListEle.options.length-1].selected = true;

  if( vclassUri ){
   //Notice that this is using the edu.cornell.mannlib.vitro.JSONServlet
   var base =  getURLandContext();
   //these are parameters to the dojo JSON call

   var bindArgs = {
    url:  "dataservice?getEntitiesByVClass=1&vclassURI="+encodeUrl(vclassUri),
    error: function(type, data, evt){
       if( type == null ){ type = "none" ; }
       if( data == null ){ data = "none" ; }
       if( evt == null ){ evt = "none" ; }       
       alert("An error occurred while attempting to get the individuals of vclass "
               + vclassUri + "\ntype: " + type +"\ndata: "+
               data +"\nevt: " + evt );
     },
    load: function(type, data, evt){
       //clear here since we will be using addEntOptions for multiple adds
//       var entsListEle = $("entitiesList");
//        clear(entsListEle);
       addEntOptions(data, -1);
    },
    mimetype: "text/json"
   };
   abortExistingEntRequest();
   gEntRequest = dojo.io.bind(bindArgs);   
  } else {
    clear(entsListEle);
  }
}

/** add entities in entArray as options elements to select element "Individual" */
function addEntOptions( entArray ){
  var entsListEle = $("entitiesList");

    if( entArray == null || entArray.length == 0){
        clear(entsListEle);
        return;
    }

  var previouslySelectedEntUri = gProperty.objectEntURI;

  //check if the last element indicates that there are more results to get
  contObj = null;
  if( entArray != null && entArray[entArray.length-1].nextUrl != null ){
    contObj = entArray.pop();
  }
  
  var CUTOFF = 110; //shorten entity names longer then this value.
  var foundEntity = false;
  var text;
  var value;
  var opt;
  for (var i = 0; i < entArray.length; i++) {
    text = entArray[i].name;
    if( text.length > CUTOFF){ text = text.substr(0,CUTOFF) + "..."; }
    value = entArray[i].URI;
    opt = new Option(text, value);
    entsListEle.options[entsListEle.options.length] = opt;
    if( previouslySelectedEntUri == value ){
        entityOptToSelect = opt;
    }
  }

  if( contObj != null ){
    entsListEle.item(0).text = entsListEle.item(0).text + ".";
    addMoreEntOptions( contObj );
  } else {    
    gEntRequests = [];
    entsListEle.disabled = false;
    if( entsListEle.length > 0 && entsListEle.item(0).value == -12){
      entsListEle.remove(0);
    }
    if( entityOptToSelect != null ){
        entityOptToSelect.selected = true;
    }
  }
}
var entityOptToSelect =null;

/*
 * Add more entity options to the list if there are more on the request 
 *
example of a continueObj
{"nextUrl":"http://localhost:8080/vivo/dataService?getEntitiesByVClass=1&vclassId=318",
"entsInVClass":2773}
 */
function addMoreEntOptions( continueObj ){
  if( continueObj.nextUrl != null ){
    var bindArgs = {
    url: continueObj.nextUrl,
    error: function(type, data, evt){
        if( type == null ){ type = "none" ; }
        if( data == null ){ data = "none" ; }
        if( evt == null ){ evt = "none" ; }       
        alert("An error addMoreEntOptions()"+"\ntype: " + type +"\ndata: "+
              data +"\nevt: " + evt );
      },
    load: function(type, data, evt){
        addEntOptions( data );
      },
    mimetype: "text/json"
    };    
    abortExistingEntRequest();
    gEntRequest = dojo.io.bind(bindArgs);    
  }
}

/*
Entities are now sent back as JSON in groups of 556.  This is to defend against
odd network effects and browser flakyness.  DWR was choaking on large datasets
and isn't very flexable so I moved to JSON.  That still caused problems with
large data sets ( size > 1500 ) so I'm breaking them up.

The reply from the JSON servlet is a JSON array.  If the last item in the
array lacks a "id" property then it is information about additional entites.
If the last item in the JSON array has an id then you don't have to go back
for more results.

Example:
[ ... 
 {"moniker":"recent journal article", "name":"{beta}2 and {beta}4
     Subunits of BK Channels Confer Differential Sensitivity to Acute
     Modulation by Steroid Hormones.", "vClassId":318, "id":18120},
 {"resultGroup":3,"entsInVClass":2773,"nextResultGroup":4,"standardReplySize":556}
]

*/

/****************************************************************
Check to see if property edit form is valid 
*****************************************************************/
function validateForm(){
  var dateEx = "\nDates should be in the format YYYY-MM-DD";
  /* used to check dates here */ 
  return true;
}

/**************************************************************
Write or update ents2ents row.
***************************************************************/
function writeProp()  {
  if( PropertyDWR.insertProp == null  ){
    alert("Writing of properties disabled for security reasons.");
    return;
  }
  if( !validateForm() ) { return; }
  var prop = gPropertyHash[DWRUtil.getValue("propertyList")];
  var newP = {};
  var oldP = gProperty;
  newP.propertyURI = prop.propertyURI;

  var selected = DWRUtil.getValue("entitiesList");
  newP.subjectEntURI= gEntity.URI;
  newP.objectEntURI = selected ;
  
  var callback = function(result){
    editingNewProp = false;
    justwritenProp = newP;
    update();  };

  if( editingNewProp ){    
    newP.ents2entsId = -1;
    PropertyDWR.insertProp(callback, newP );
  } else {
      var afterDelete=
              function(result){
                  PropertyDWR.insertProp(callback, newP);
              };
      PropertyDWR.deleteProp(afterDelete,
              gProperty.subjectEntURI,
              gProperty.propertyURI,
              gProperty.objectEntURI);
  }
}

/************ Things that happen on load *****************************/
addEvent(window, 'load', update);

/********************* some utilities ***********************************/
/* this clones the property edit from a div on the ents_edit.jsp */
function getForm(){  return $("propeditdiv").cloneNode(true); }

/* a function to display a lot of info about an object */
function disy( obj, note ){ alert( (note!==null?note:"") + DWRUtil.toDescriptiveString(obj, 3));}

/* attempts to get the URI of the entity being edited */
function getEntityUriFromPage(){
  return document.getElementById("entityUriForDwr").nodeValue;      
}

function abortExistingEntRequest(){
  if( gEntRequest != null ){
    if( gEntRequest.abort ){
        gEntRequest.abort();
    }else{
        alert("No abort function found on gEntRequest");
    }
    gEntRequest = null;
 }    
}
