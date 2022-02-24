<#-- $This file is distributed under the terms of the license in LICENSE$ -->

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
        pageString: '${i18n().page?js_string}',
        viewPageString: '${i18n().view_page?js_string}',
        ofTheResults: '${i18n().of_the_results?js_string}',
        thereAreNoEntriesStartingWith: '${i18n().there_are_no_entries_starting_with?js_string}',
        tryAnotherLetter: '${i18n().try_another_letter?js_string}',
        indsInSystem: '${i18n().individuals_in_system?js_string}',
        selectAnotherClass: '${i18n().select_another_class?js_string}'
    };
</script>




<#-- Script to enable browsing individuals within a class -->
${scripts.add('<script type="text/javascript" src="${urls.base}/js/jquery_plugins/jquery.scrollTo-min.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/menupage/browseByVClass.js"></script>')}
