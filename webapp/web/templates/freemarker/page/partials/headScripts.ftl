<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for scripts that must be loaded in the head -->

<script type="text/javascript" src="${urls.base}/js/jquery.js"></script>
<script type="text/javascript" src="${urls.base}/js/vitroUtils.js"></script>

<#-- script for enabling new HTML5 semantic markup in IE browsers -->
<!--[if lt IE 9]>
<script type="text/javascript" src="${urls.base}/js/html5.js"></script>
<![endif]-->

${headScripts.list()} 

<!--[if lt IE 7]>
<script type="text/javascript" src="${urls.base}/js/jquery_plugins/supersleight.js"></script>
<![endif]-->

<!--[if lt IE 7]>
<link rel="stylesheet" href="${urls.base}/css/vitroIE6.css" />
<![endif]-->

<!--[if IE 7]>
<link rel="stylesheet" href="${urls.base}/css/vitroIE7.css" />
<![endif]-->
