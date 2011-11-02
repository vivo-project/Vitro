/**
 * jQuery TinyMCE (http://mktgdept.com/jquery-tinymce-plugin)
 * A jQuery plugin for unobtrusive TinyMCE
 *
 * v0.0.2 - 28 August 2009
 *
 * Copyright (c) 2009 Chad Smith (http://twitter.com/chadsmith)
 * Dual licensed under the MIT and GPL licenses.
 * http://www.opensource.org/licenses/mit-license.php
 * http://www.opensource.org/licenses/gpl-license.php
 *
 * Add TinyMCE to an element using: $(element).tinymce([settings]);
 * Note that TinyMCE has released a Jquery version of itself that includes a plugin for jquery
 * and we should probably use that instead but that would require overwriting the tiny mce javscript
 * that currently exists
 *
 **/
;jQuery.fn.tinymce=jQuery.fn.tinyMCE=jQuery.fn.TinyMCE=function(d,e){return this.each(function(i){var a,b,c=this.id=this.id||this.name||(this.className||'jMCE')+i;if(d&&Object===d.constructor){e=d;d=null}if(!d&&tinyMCE.get(c)){d='remove';b=true}switch(d){case'remove':a='mceRemoveControl';break;case'toggle':a='mceToggleEditor';break;default:a='mceAddControl'}tinyMCE.settings=e;tinyMCE.execCommand(a,false,c);if(b)$(this).tinyMCE(e)})};