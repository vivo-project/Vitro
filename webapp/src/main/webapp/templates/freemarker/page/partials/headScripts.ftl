<#-- $This file is distributed under the terms of the license in LICENSE$ -->

<#-- Template for scripts that must be loaded in the head -->
<script>
var i18nStrings = {
    allCapitalized: '${i18n().all_capitalized}',
};
</script>
<script type="text/javascript" src="${urls.base}/js/jquery-1.12.4.min.js"></script>
<script type="text/javascript" src="${urls.base}/js/jquery-migrate-1.4.1.js"></script>
<script type="text/javascript" src="${urls.base}/js/vitroUtils.js"></script>

<#-- script for enabling new HTML5 semantic markup in IE browsers -->
<!--[if lt IE 9]>
<script type="text/javascript" src="${urls.base}/js/html5.js"></script>
<![endif]-->

${headScripts.list()}
