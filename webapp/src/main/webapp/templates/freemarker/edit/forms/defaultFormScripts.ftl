<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template to setup and call scripts for form editing -->

<#-- Like original jsp, allow the passing of variables -->
<#assign defaultHeight="200" />
<#assign defaultWidth="75%" />
<#assign defaultButton="bold,italic,underline,separator,link,bullist,numlist,separator,sub,sup,charmap,separator,undo,redo,separator,code"/>   
<#assign defaultToolbarLocation = "top" />
<#if !height?has_content>
	<#assign height=defaultHeight/>
</#if>

<#if !width?has_content>
	<#assign width=defaultWidth />
</#if>

<#if !buttons?has_content>
	<#assign buttons = defaultButton />
</#if>

<#if !toolbarLocation?has_content>
	<#assign toolbarLocation = defaultToolbarLocation />
</#if>

<#-- Set up data -->
<script type="text/javascript">
    var customFormData = {
    	tinyMCEData : {
                theme : "advanced",
                mode : "textareas",
                theme_advanced_buttons1 : "${buttons}",
                theme_advanced_buttons2 : "",
                theme_advanced_buttons3 : "",
                theme_advanced_toolbar_location : "${toolbarLocation}",
                theme_advanced_toolbar_align : "left",
                theme_advanced_statusbar_location : "bottom",
                theme_advanced_path : false,
                theme_advanced_resizing : true,
                height : "${height}",
                width  : "${width}",
                valid_elements : "a[href|name|title],br,p,i,em,cite,strong/b,u,sub,sup,ul,ol,li",
                fix_list_elements : true,
                fix_nesting : true,
                cleanup_on_startup : true,
                gecko_spellcheck : true,
                forced_root_block: false,
                plugins : "paste",
                paste_use_dialog : false,
                paste_auto_cleanup_on_paste : true,
                paste_convert_headers_to_strong : true,
                paste_strip_class_attributes : "all",
                paste_remove_spans : true,
                paste_remove_styles : true,
                paste_retain_style_properties : ""
//                paste_text_sticky : true,
//                setup : function(ed) {
//                    ed.onInit.add(function(ed) {
//                        ed.pasteAsPlainText = true;
//                    });
//                }
                // plugins: "paste",
                // theme_advanced_buttons1_add : "pastetext,pasteword,selectall",
                // paste_create_paragraphs: false,
                // paste_create_linebreaks: false,
                // paste_use_dialog : true,
                // paste_auto_cleanup_on_paste: true,
                // paste_convert_headers_to_strong : true,
                // save_callback : "customSave",
                // content_css : "example_advanced.css",
                // extended_valid_elements : "a[href|target|name]"
                // plugins : "table",
                // theme_advanced_buttons3_add_before : "tablecontrols,separator",
                // invalid_elements : "li",
                // theme_advanced_styles : "Header 1=header1;Header 2=header2;Header 3=header3;Table Row=tableRow1", // Theme specific setting CSS classes
        }
    };
</script>

<#-- Script to enable browsing individuals within a class -->
<#--'<script type="text/javascript" src="${urls.base}/js/jquery_plugins/jquery.scrollTo-min.js"></script>',-->
${scripts.add('<script type="text/javascript" src="${urls.base}/js/tiny_mce/tiny_mce.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/tiny_mce/jquery.tinymce.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/edit/initTinyMce.js"></script>',
              '<script type="text/javascript" src="${urls.base}/templates/freemarker/edit/forms/js/defaultDataPropertyUtils.js"></script>')}