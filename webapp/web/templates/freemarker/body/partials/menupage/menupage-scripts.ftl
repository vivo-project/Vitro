<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template to setup and call scripts for menupages -->



<#list vClassGroup as vClass>
    <#if (vClass.entityCount > 0)>
        <#assign firstNonEmptyVClass = vClass.URI />
        <#break>
    </#if>
    <#-- test if we're at the last class. If we've gotten this far, none of the classes have any individuals -->
    <#if !vClass_has_next>
        <#assign firstNonEmptyVClass = "false">
    </#if>
</#list>

<script type="text/javascript">
    var firstBrowseClass = $("ul#browse-classes li:first").find("a").attr("data-uri");
    if ( !firstBrowseClass || firstBrowseClass.length == 0 ) {
        firstBrowseClass = '${firstNonEmptyVClass}';
    }
    var menupageData = {
        baseUrl: '${urls.base}',
        <#if internalClass?has_content>
            dataServiceUrl: '${dataServiceUrlIndividualsByVClass}${internalClass}&vclassId=',
        <#else>
            dataServiceUrl: '${dataServiceUrlIndividualsByVClass}',
        </#if>
        defaultBrowseVClassUri: firstBrowseClass //'${firstNonEmptyVClass}'
    };
    var i18nStrings = {
        pageString: '${i18n().page}',
        viewPageString: '${i18n().view_page}',
        ofTheResults: '${i18n().of_the_results}',
        thereAreNo: '${i18n().there_are_no}',
        indNamesStartWith: '${i18n().individuals_names_starting_with}',
        tryAnotherLetter: '${i18n().try_another_letter}',
        indsInSystem: '${i18n().individuals_in_system}',
        selectAnotherClass: '${i18n().select_another_class}'
    };
</script>




<#-- Script to enable browsing individuals within a class -->
${scripts.add('<script type="text/javascript" src="${urls.base}/js/jquery_plugins/jquery.scrollTo-min.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/menupage/browseByVClass.js"></script>')}