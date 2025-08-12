<#-- $This file is distributed under the terms of the license in LICENSE$ -->

<#-- Template for scripts that must be loaded in the head -->
<script>
var i18nStrings = {
    allCapitalized: '${i18n().all_capitalized?js_string}',
};
var baseUrl = '${urls.base}';
</script>
<script type="text/javascript" src="${urls.base}/webjars/jquery/jquery.min.js"></script>
<script type="text/javascript" src="${urls.base}/webjars/jquery-migrate/jquery-migrate.min.js"></script>
<script type="text/javascript" src="${urls.base}/js/vitroUtils.js"></script>

<#-- script for enabling new HTML5 semantic markup in IE browsers -->
<!--[if lt IE 9]>
<script type="text/javascript" src="${urls.base}/js/html5.js"></script>
<![endif]-->

${headScripts.list()}
