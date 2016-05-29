<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#if origination?has_content && origination == "helpLink">
    <h2>Search Tips</h2>
    <span id="searchHelp">
        <a href="#" onClick="history.back();return false;" title="back to results">Back to results</a>
    </span>
<#else>
    <h3>Search Tips</h3>        
</#if>
<ul class="searchTips">
    <li>Keep it simple! Use short, single terms unless your searches are returning too many results.</li>
    <li>Use quotes to search for an entire phrase -- e.g., "<i>protein folding</i>".</li>
    <li>Except for boolean operators, searches are <strong>not</strong> case-sensitive, so "Geneva" and "geneva" are equivalent</li>
    <li>If you are unsure of the correct spelling, put ~ at the end of your search term -- e.g., <i>cabage~</i> finds <i>cabbage</i>, <i>steven~</i> finds <i>Stephen</i> and <i>Stefan</i> (as well as other similar names).</li>
</ul>
    
<h4><a id="advTipsLink" href="#">Advanced Tips</a></h4>    
<ul id="advanced" class="searchTips" style="visibility:hidden">
    <li>When you enter more than one term, search will return results containing all of them unless you add the Boolean "OR" -- e.g., <i>chicken</i> OR <i>egg</i>.</li>
    <li>NOT" can help limit searches -- e.g., <i>climate</i> NOT <i>change</i>.</li>
    <li>Phrase searches may be combined with Boolean operators -- e.g. "<i>climate change</i>" OR "<i>global warming</i>".</li>
    <li>Close word variations will also be found -- e.g., <i>sequence</i> matches <i>sequences</i> and <i>sequencing</i>.</li>
    <li>Use the wildcard * character to match an even wider variation -- e.g., <i>nano*</i> will match both <i>nanotechnology</i> and <i>nanofabrication</i>.</li>
    <li>Search uses shortened versions of words -- e.g., a search for <i>cogniti*</i> finds nothing, while <i>cognit*</i> finds both <i>cognitive</i> and <i>cognition</i>.</li> 
</ul>
<a id="closeLink" href="#"  style="visibility:hidden;font-size:.825em;padding-left:8px">Close</a>
${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/search.css" />')}
<script type="text/javascript">
    $(document).ready(function(){
        $('a#advTipsLink').click(function() {
           $('ul#advanced').css("visibility","visible"); 
           $('a#closeLink').css("visibility","visible");
           $('a#closeLink').click(function() {
              $('ul#advanced').css("visibility","hidden"); 
              $('a#closeLink').css("visibility","hidden");
              return false;
           });
           return false;
        });
    });
    
</script>