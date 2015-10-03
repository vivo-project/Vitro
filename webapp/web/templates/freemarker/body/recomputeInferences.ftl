<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#if formAction?has_content>
    <form method="post" action="${formAction}">
        <input class="submit" type="submit" value="${i18n().recompute_inferences}" name="submit" role="input" />
    </form>
</#if>

<#if message?has_content>
    <p>${message}</p>
</#if>

<div id="reasonerHistory">
    Reasoner history
</div>

<script>
    reasonerStatusUrl = '${statusUrl}'
</script>

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/reasoner/reasoner.css" />')}

${scripts.add('<script type="text/javascript" src="${urls.base}/js/reasoner/reasoner.js"></script>')}
${scripts.add('<script type="text/javascript" src="${urls.base}/js/jquery-ui/js/jquery-ui-1.8.9.custom.min.js"></script>')}
