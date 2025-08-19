<#-- $This file is distributed under the terms of the license in LICENSE$ -->

<meta charset="utf-8" />
<!-- Google Chrome Frame open source plug-in brings Google Chrome's open web technologies and speedy JavaScript engine to Internet Explorer-->
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">

<#include "title.ftl">

<#include "stylesheets.ftl">

<#include "themeStylesheets.ftl">

<#include "headScripts.ftl">

<!--[if (gte IE 6)&(lte IE 8)]>
<script type="text/javascript" src="${urls.base}/js/selectivizr.js"></script>
<![endif]-->

<#if metaTags??>
    ${metaTags.list()}
</#if>

<#-- Inject head content specified in the controller. Currently this is used only to generate an rdf link on
an individual profile page. -->
${headContent!}

<#if customCssPath?has_content>
    <link id="custom-css-path" rel="stylesheet" href="${customCssPath}">
</#if>

<link rel="shortcut icon" type="image/x-icon" href="${urls.base}/favicon.ico">
