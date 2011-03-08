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

<#-- For v1.3: The controller should pass in the dataservice url. -->
<script type="text/javascript">
    var menupageData = {
        baseUrl: '${urls.base}',
        dataServiceUrl: '${urls.base}/dataservice?getLuceneIndividualsByVClass=1&vclassId=',
        defaultBrowseVClassUri: '${firstNonEmptyVClass}'
    };
</script>

<#-- Script to enable browsing individuals within a class -->
${scripts.add('<script type="text/javascript" src="${urls.base}/js/jquery_plugins/jquery.scrollTo-min.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/menupage/browseByVClass.js"></script>')}
