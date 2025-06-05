<#-- $This file is distributed under the terms of the license in LICENSE$ -->

<div id="developerPanel" > </div>
<script>
    developerAjaxUrl = '${urls.developerAjax}'
    developerCssLinks = ["${urls.base}/css/developer/developerPanel.css", "${urls.base}/webjars/jquery-ui-themes/smoothness/jquery-ui.min.css"]
</script>
${scripts.add('<script type="text/javascript" src="${urls.base}/js/developer/developerPanel.js"></script>')}
${scripts.add('<script type="text/javascript" src="${urls.base}/webjars/jquery-ui/jquery-ui.min.js"></script>')}
${scripts.add('<script type="text/javascript" src="${urls.base}/js/developer/FileSaver.js"></script>')}
${scripts.add('<script defer type="text/javascript" src="${urls.base}/js/developer/translations.js"></script>')}
