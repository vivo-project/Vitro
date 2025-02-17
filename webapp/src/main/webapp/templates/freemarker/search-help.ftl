<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#if origination?has_content && origination == "helpLink">
    <h2>${i18n().search_tips_header}</h2>
    <span id="searchHelp">
        <a href="#" onClick="history.back();return false;" title="${i18n().back_to_results}">${i18n().back_to_results}</a>
    </span>
<#else>
    <h3>${i18n().search_tips_header}</h3>
</#if>
<ul class="searchTips">
    <li>${i18n().search_tip_one}</li>
    <li>${i18n().search_tip_two}</li>
    <li>${i18n().search_tip_three}</li>
    <li>${i18n().search_tip_four}</li>
</ul>
    
<h4><a id="advTipsLink" href="#">${i18n().advanced_search_tips_header}</a></h4>
<ul id="advanced" class="searchTips" style="visibility:hidden">
    <li>${i18n().advanced_search_tip_one}</li>
    <li>${i18n().advanced_search_tip_two}</li>
    <li>${i18n().advanced_search_tip_three}</li>
    <li>${i18n().advanced_search_tip_four}</li>
    <li>${i18n().advanced_search_tip_five}</li>
    <li>${i18n().advanced_search_tip_six}</li>
</ul>
<a id="closeLink" href="#"  style="visibility:hidden;font-size:.825em;padding-left:8px">${i18n().close_capitalized}</a>
${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/search.css" />')}
<script type="text/javascript">
    $(document).ready(function(){
        $('a#advTipsLink').on("click", function() {
           $('ul#advanced').css("visibility","visible"); 
           $('a#closeLink').css("visibility","visible");
           $('a#closeLink').on("click", function() {
              $('ul#advanced').css("visibility","hidden"); 
              $('a#closeLink').css("visibility","hidden");
           });

        });
    });
    
</script>
