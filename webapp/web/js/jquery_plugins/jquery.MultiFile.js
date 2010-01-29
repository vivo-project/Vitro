/*
 ### jQuery Multiple File Upload Plugin v1.28 - 2008-05-02 ###
 By Diego A., http://www.fyneworks.com, diego@fyneworks.com
 
 Project: http://jquery.com/plugins/project/MultiFile/
 Website: http://www.fyneworks.com/jquery/multiple-file-upload/
 Forums:  http://www.nabble.com/jQuery-Multiple-File-Upload-f20931.html
*/
/*
 CHANGE LOG:
	12-April-2007: v1.1
	Added events and file extension validation
	See website for details.
	
	06-June-2007: v1.2
	Now works in Opera.
	
	12-June-2007: v1.21
	Preserves name of file input so all current server-side
	functions don't need to be changed on new installations.
	
	24-June-2007: v1.22
	Now works perfectly in Opera, thanks to Adrian Wróbel <adrian [dot] wrobel [at] gmail.com>
	
	10-Jan-2008: v1.24
	Fixed bug in event trigger - sending incorrect parameters to event handlers
	
	14-Jan-2008: v1.24
	Fixed bug 1251: http://plugins.jquery.com/project/comments/add/1251
	
	25-Jan-2008: v1.24
	Implemented feature request: http://plugins.jquery.com/node/1363
	The plugin now automatically intercepts ajaxSubmit function (form plugin)
	and disbales empty file input elements for submission
	
	08-Feb-2008: v1.25
	Fixed bug: http://plugins.jquery.com/node/1495
	The last change caused the plugin to disabled input files that shouldn't have been ignored
	Now, every newly created input file (by this plugin) is marked by the MultiFile class.
	
	11-Feb-2008: v1.25
	Fixed bug: http://plugins.jquery.com/node/1495
	Plugin would override element ID everytime.
	
	11-Feb-2008: v1.26
	Modified plugin structure. After selecting and removing a file, the plugin would
	remove the original element from DOM. Now the plugin works back on its own tracks
	and removes the	last generated element.
	This will resolve compatibility issues with validation plugins and custom code.
	
	12-Feb-2008: v1.26
	Try to clear elements using the browser's native behaviour: .reset()
	This works around security policies in IE6 and Opera.
	
	17-Mar-2008: v1.27
	Added properties/methods for easier form integration
	$.MultiFile.autoIntercept - array of known jQuery plugins to intercept (default: 'submit', 'ajaxSubmit', 'validate');
	$.MultiFile.intercept - intercepts a jQuery plugin / anonymous function
	$.MultiFile.disableEmpty - disable empty inputs (required for some upload scripts)
	$.MultiFile.reEnableEmpty - re-enables empty inputs
	
	17-Mar-2008: v1.28
	MAJOR FIX - OPERA BUG
	MASTER.labels was a readyonly property and cause the script to fail.
	Renamed to MASTER.eLBL. Problem solved!
	
	29-Apr-2008: v1.28
	Added validation to stop duplicate selections
	Extracted default configuration to $.MultiFile.options
	Improved code organization / performance
	Default name is now files[] - square brackets indicate an array is being submitted
	Added namePattern options - allows user to configure name of slave elements
	 - $name = master name, $id = master id, $g = group count, $i = slave count
		- eg.: $name$i will result in file1, file2, file3 and so on...
*/

/*# AVOID COLLISIONS #*/
;if(jQuery) (function($){
/*# AVOID COLLISIONS #*/
 
 // extend jQuery - $.MultiFile hook
 $.extend($, {
  MultiFile: function( o /* Object */ ){
   //return $("INPUT[@type='file'].multi").MultiFile(o);
   return $("input:file.multi").MultiFile(o);
  }
 });
 
	//===
	
	// extend $.MultiFile - default options
 $.extend($.MultiFile, {
  options: {
			accept: '', max: -1,
			// error handling function
			error: function(s){
				if($.blockUI){
					$.blockUI({
      message: s.replace(/\n/gi,'<br/>'),
						css: { 
							border:'none', padding:'15px', size:'12.0pt',
							backgroundColor:'#900', color:'#fff',
							opacity:'.8','-webkit-border-radius': '10px','-moz-border-radius': '10px'
						}
     });
					window.setTimeout($.unblockUI, 2000);
				}
				else{
					alert(s);
				}
			},
			// namePattern: $name/$id (from master element), $i (slave count), $g (group count)
			namePattern: '$name',
			// STRING: collection lets you show messages in different languages
			STRING: {
				remove:'remove',
				denied:'You cannot select a $ext file.\nTry again...',
				selected:'File selected: $file',
				duplicate:'This file has already been selected:\n$file'
			}
		}
 });
 
	//===
	
 // extend $.MultiFile - global methods
 $.extend($.MultiFile, {
  disableEmpty: function(){
			$('input:file'/*.multi'*/).each(function(){
				var $this = $(this);
				if($this.val()=='') $this.addClass('mfD').each(function(){ this.disabled = true });
			});
  },
		reEnableEmpty: function(){
   $('input:file.mfD').removeClass('mfD').each(function(){ this.disabled = false });
		},
		autoIntercept: [ 'submit', 'ajaxSubmit', 'validate' /* array of methods to intercept */ ],
		intercepted: {},
		intercept: function(methods, context, args){
			var method, value; args = args || [];
			if(args.constructor.toString().indexOf("Array")<0) args = [ args ];
			if(typeof(methods)=='function'){
				$.MultiFile.disableEmpty();
				value = methods.apply(context || window, args);
				$.MultiFile.reEnableEmpty();
				return value;
			};
			if(methods.constructor.toString().indexOf("Array")<0) methods = [methods];
			for(var i=0;i<methods.length;i++){
				method = methods[i]+''; // make sure that we have a STRING
				if(method) (function(method){ // make sure that method is ISOLATED for the interception
					$.MultiFile.intercepted[method] = $.fn[method] || function(){};
					$.fn[method] = function(){
						$.MultiFile.disableEmpty();
						//if(console) console.log(['$.MultiFile.intercepted', method, this, arguments]);
						value = $.MultiFile.intercepted[method].apply(this, arguments);
						$.MultiFile.reEnableEmpty();
						return value;
					}; // interception
				})(method); // MAKE SURE THAT method IS ISOLATED for the interception
			};// for each method
		}
 });
 
	//===
	
 // extend jQuery function library
 $.extend($.fn, {
   
			// Use this function to clear values of file inputs
			// This doesn't always work: $(element).val('').attr('value', '')[0].value = '';
			reset: function(){ return this.each(function(){ try{ this.reset(); }catch(e){} }); },
				
   // MultiFile function
   MultiFile: function( o /* Object */ ){
				
    //### http://plugins.jquery.com/node/1363
    // utility method to integrate this plugin with others...
    if($.MultiFile.autoIntercept){
				 $.MultiFile.intercept( $.MultiFile.autoIntercept /* array of methods to intercept */ );
					$.MultiFile.autoIntercept = null; /* only run this once */
				};
				
				//===
				
    // Bind to each element in current jQuery object
    return $(this).each(function(group_count){
     if(this._MultiFile) return; this._MultiFile = true;
     
       // BUG 1251 FIX: http://plugins.jquery.com/project/comments/add/1251
       // variable group_count would repeat itself on multiple calls to the plugin.
       // this would cause a conflict with multiple elements
       // changes scope of variable to global so id will be unique over n calls
							window.MultiFile = (window.MultiFile || 0) + 1;
       group_count = window.MultiFile;
       
       // Copy parent attributes - Thanks to Jonas Wagner
       // we will use this one to create new input elements
       var MASTER = this, xCLONE = $(MASTER).clone();
       
							//===
       
       //# USE CONFIGURATION
       if(typeof o=='number') o = {max:o};
       if(typeof o=='string') o = {accept:o};
       o = $.extend({},
								$.MultiFile.options,
        ($.metadata ? $(MASTER).metadata()/*NEW metadata plugin*/ :
        ($.meta ? $(MASTER).data()/*OLD metadata plugin*/ : 
        null/*metadata plugin not available*/)) || {},
								o || {}
							);
       // limit number of files that can be selected?
       if(!(o.max>0) /*IsNull(MASTER.max)*/){
        o.max = $(MASTER).attr('maxlength');
        if(!(o.max>0) /*IsNull(MASTER.max)*/){
         o.max = (String(MASTER.className.match(/\b(max|limit)\-([0-9]+)\b/gi) || ['']).match(/[0-9]+/gi) || [''])[0];
         if(!(o.max>0)) o.max = -1;
									else           o.max = String(o.max).match(/[0-9]+/gi)[0];
        }
       };
       o.max = new Number(o.max);
       // limit extensions?
							o.accept = o.accept || $(MASTER).attr('accept') || '';
       if(!o.accept){
        o.accept = (MASTER.className.match(/\b(accept\-[\w\|]+)\b/gi)) || '';
        o.accept = new String(o.accept).replace(/^(accept|ext)\-/i,'');
       };
							
							//===
       
       // APPLY CONFIGURATION
       $.extend(MASTER, o || {});
       MASTER.STRING = $.extend({},$.MultiFile.options.STRING,MASTER.STRING);
							
							//===
       
       //#########################################
       // PRIVATE PROPERTIES/METHODS
       $.extend(MASTER, {
        n: 0, // How many elements are currently selected?
        slaves: [], files: [],
								instanceKey: MASTER.id || 'MultiFile'+String(group_count), // Instance Key?
        generateID: function(z){ return MASTER.instanceKey + (z>0 ?'_F'+String(z):''); },
								trigger: function(event, element){
									var handler = MASTER[event], value = $(element).attr('value');
									if(handler){
										var returnValue = handler(element, value, MASTER);
										if( returnValue!=null ) return returnValue;
									}
									return true;
								}
       });
       
							//===
       
       // Setup dynamic regular expression for extension validation
       // - thanks to John-Paul Bader: http://smyck.de/2006/08/11/javascript-dynamic-regular-expresions/
       if(String(MASTER.accept).length>1){
        MASTER.rxAccept = new RegExp('\\.('+(MASTER.accept?MASTER.accept:'')+')$','gi');
       };
       
							//===
       
							// Create wrapper to hold our file list
       MASTER.wrapID = MASTER.instanceKey+'_wrap'; // Wrapper ID?
       $(MASTER).wrap('<div id="'+MASTER.wrapID+'"></div>');
       MASTER.eWRP = $('#'+MASTER.wrapID+'');
       
							//===
       
       // MASTER MUST have a name
							MASTER.name = MASTER.name || 'file[]';
       
							//===
       
       // Create a wrapper for the labels
							// * OPERA BUG: NO_MODIFICATION_ALLOWED_ERR ('labels' is a read-only property)
       // this changes allows us to keep the files in the order they were selected
							MASTER.eWRP.append( '<span id="'+MASTER.wrapID+'_labels"></span>' );
							MASTER.eLBL = $('#'+MASTER.wrapID+'_labels');
       
							//===
       
       // Bind a new element
       MASTER.addSlave = function( slave, slave_count ){
        // Keep track of how many elements have been displayed
        MASTER.n++;
        // Add reference to master element
        slave.MASTER = MASTER;
								// Count slaves
        slave.i = slave_count;
								
								// BUG FIX: http://plugins.jquery.com/node/1495
 							// Clear identifying properties from clones
								if(slave.i>0) slave.id = slave.name = null;
        
								// Define element's ID and name (upload components need this!)
        slave.id = slave.id || MASTER.generateID(slave.i);
        
								//slave.name = (slave.name || $(MASTER).attr('name') || 'file');// + (slave.i>0?slave.i:''); // same name as master element
								// 2008-Apr-29: New customizable naming convention (see url below)
								// http://groups.google.com/group/jquery-dev/browse_frm/thread/765c73e41b34f924#
        slave.name = String(MASTER.namePattern
								 /*master name*/.replace(/\$name/gi,$(MASTER).attr('name'))
								 /*master id  */.replace(/\$id/gi,  $(MASTER).attr('id'))
								 /*group count*/.replace(/\$g/gi,   (group_count>0?group_count:''))
								 /*slave count*/.replace(/\$i/gi,   (slave_count>0?slave_count:''))
								);
								
 							// Clear value
								$(slave).val('').attr('value','')[0].value = '';
        
        // If we've reached maximum number, disable input slave
        if( (MASTER.max > 0) && ((MASTER.n-1) > (MASTER.max)) )//{ // MASTER.n Starts at 1, so subtract 1 to find true count
         slave.disabled = true;
        //};
        
        // Remember most recent slave
        MASTER.eCur = MASTER.slaves[slave.i] = slave;
        
        /// now let's use jQuery
        slave = $(slave);
        
        // Triggered when a file is selected
        $(slave).change(function(){
          
          //# Trigger Event! onFileSelect
          if(!MASTER.trigger('onFileSelect', this, MASTER)) return false;
          //# End Event!
          
										//# Retrive value of selected file from element
          var ERROR = '', v = String(this.value || ''/*.attr('value)*/);
										
          // check extension
          if(MASTER.accept){
           if(v!=''){
												if(!v.match(MASTER.rxAccept)){
             ERROR = MASTER.STRING.denied.replace('$ext', String(v.match(/\.\w{1,4}$/gi)));
												}
           }
          };
          
          // Disallow duplicates
										for(var f=0;f<MASTER.slaves.length;f++){
											if(MASTER.slaves[f]!=this){
												if(MASTER.slaves[f].value==v){
													ERROR = MASTER.STRING.duplicate.replace('$file', v.match(/[^\/\\]+$/gi));
												}
											}
          };
										
          // Create a new file input element
          //var newEle = $('<input name="'+($(MASTER).attr('name') || '')+'" type="file"/>');
          var newEle = xCLONE.clone();// Copy parent attributes - Thanks to Jonas Wagner
										//# Let's remember which input we've generated so
										// we can disable the empty ones before submission
										// See: http://plugins.jquery.com/node/1495
										newEle.addClass('MultiFile');
          
										// Handle error
										if(ERROR!=''){
												// Handle error
												MASTER.error(ERROR);
												
												// Clear element value (DOES NOT WORK in some browsers)
            //slave.reset().val('').attr('value', '')[0].value = '';
												
												// 2007-06-24: BUG FIX - Thanks to Adrian Wróbel <adrian [dot] wrobel [at] gmail.com>
												// Ditch the trouble maker and add a fresh new element
            MASTER.n--;
            MASTER.addSlave(newEle[0], this.i);
            slave.parent().prepend(newEle);
            slave.remove();
            return false;
										};
          
          // Hide this element (NB: display:none is evil!)
          $(this).css({ position:'absolute', top: '-3000px' });
          
          // Add new element to the form
          MASTER.eLBL.before(newEle);//.append(newEle);
          
          // Update list
          MASTER.addToList( this );
          
          // Bind functionality
          MASTER.addSlave( newEle[0], this.i+1 );
          
          //# Trigger Event! afterFileSelect
          if(!MASTER.trigger('afterFileSelect', this, MASTER)) return false;
          //# End Event!
          
        }); // slave.change()
				    
       };// MASTER.addSlave
       // Bind a new element
       
       
       
       // Add a new file to the list
       MASTER.addToList = function( slave ){
        
        //# Trigger Event! onFileAppend
        if(!MASTER.trigger('onFileAppend', slave, MASTER)) return false;
        //# End Event!
        
        // Create label elements
        var
         r = $('<div></div>'),
         v = String(slave.value || ''/*.attr('value)*/),
         a = $('<span class="file" title="'+MASTER.STRING.selected.replace('$file', v)+'">'+v.match(/[^\/\\]+$/gi)[0]+'</span>'),
         b = $('<a href="#'+MASTER.wrapID+'">'+MASTER.STRING.remove+'</a>');
        
        // Insert label
        MASTER.eLBL.append(
         r.append('[', b, ']&nbsp;', a)//.prepend(slave.i+': ')
        );
        
        b.click(function(){
         
          //# Trigger Event! onFileRemove
          if(!MASTER.trigger('onFileRemove', slave, MASTER)) return false;
          //# End Event!
          
          MASTER.n--;
          MASTER.eCur.disabled = false;
										
										// Remove element, remove label, point to current
										if(slave.i==0){
											$(MASTER.eCur).remove();
           MASTER.eCur = slave;
										}
										else{
											$(slave).remove();
										};
          $(this).parent().remove();
										
          // Show most current element again (move into view) and clear selection
										$(MASTER.eCur).css({ position:'', top: '' }).reset().val('').attr('value', '')[0].value = '';
          
          //# Trigger Event! afterFileRemove
          if(!MASTER.trigger('afterFileRemove', slave, MASTER)) return false;
          //# End Event!

          
          return false;
        });
        
        //# Trigger Event! afterFileAppend
        if(!MASTER.trigger('afterFileAppend', slave, MASTER)) return false;
        //# End Event!
        
       }; // MASTER.addToList
       // Add element to selected files list
       
       
       
       // Bind functionality to the first element
       if(!MASTER.MASTER) MASTER.addSlave(MASTER, 0);
       
       // Increment control count
       //MASTER.I++; // using window.MultiFile
       MASTER.n++;
       
    });
    // each element
   
   }
   // MultiFile function
 
 });
 // extend jQuery function library



/*
 ### Default implementation ###
 The plugin will attach itself to file inputs
 with the class 'multi' when the page loads
 
 Use the jQuery start plugin to 
*/
/*
if($.start){ $.start('MultiFile', $.MultiFile) }
else $(function(){ $.MultiFile() });
*/
if($.start)
 $.start('MultiFile', function(context){ context = context || this; // attach to start-up
  $("INPUT[@type='file'].multi", context).MultiFile();
 });
// $.start
else
 $(function(){ $.MultiFile() });
// $()



/*# AVOID COLLISIONS #*/
})(jQuery);
/*# AVOID COLLISIONS #*/
