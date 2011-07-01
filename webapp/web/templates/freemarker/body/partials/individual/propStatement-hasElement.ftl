<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for display:hasElement (used for menu system). 
    
     This template must be self-contained and not rely on other variables set for the individual page, because it
     is also used to generate the property statement during a deletion.  
 -->

<#-- Anchor is here for convenience during development. Should be removed before release -->
<a href="${profileUrl(statement.menuItem)}">${statement.linkText}</a>

<script type="text/javascript">
    menuItemData.push({
        'menuItemUri': '${statement.menuItem}'
    });
</script>