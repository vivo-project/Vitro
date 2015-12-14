/* $This file is distributed under the terms of the license in /doc/license.txt$ */

if( vitroJsLoaded == null ){ 

  alert("seminar.js needs to have the code from vitro.js loaded first"); 

}



addEvent(window, 'load', init);



function init(){

//   if ($('monikerSelect').options.length=1) {
//      $('monikerSelectAlt').disabled = false;
//   }else{
//      $('monikerSelectAlt').disabled = true;
//   }

  $('monikerSelect').onchange = checkMonikers;  

  update();

}



function update(){ //updates moniker list when type is changed

  DWRUtil.useLoadingMessage();

  EntityDWR.monikers(createList,  document.getElementById("field2Value").value );
  
}



function createList(data) { //puts options in moniker select list

  fillList("monikerSelect", data, getCurrentMoniker() );

  var ele = $("monikerSelect");

  var opt = new Option("none","");
  ele.options[ele.options.length] = opt;

  var opt = new Option("[new moniker]","");
  ele.options[ele.options.length] = opt;

  DWRUtil.setValue("monikerSelect",getCurrentMoniker()); // getCurrentMoniker() is defined on jsp

  checkMonikers();
}



function checkMonikers(){ //checks if monikers is on [new moniker] and enables alt field

  var sel = $('monikerSelect');  

  if( sel.value == "" || sel.options.length <= 1){
    $('monikerSelectAlt').disabled = false;

  }else{

    $('monikerSelectAlt').disabled = true; 

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

