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
    var menupageData = {
        baseUrl: '${urls.base}',
        <#if internalClass?has_content>
            dataServiceUrl: '${dataServiceUrlIndividualsByVClass}${internalClass}&vclassId=',
        <#else>
            dataServiceUrl: '${dataServiceUrlIndividualsByVClass}',
        </#if>
        defaultBrowseVClassUri: '${firstNonEmptyVClass}'
    };
</script>

<#-- Script to enable browsing individuals within a class -->
${scripts.add('<script type="text/javascript" src="${urls.base}/js/jquery_plugins/jquery.scrollTo-min.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/menupage/browseByVClass.js"></script>')}