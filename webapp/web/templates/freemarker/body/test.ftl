<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- FreeMarker test cases -->

<h2>${title}</h2>

<@widget name="test" />

${stylesheets.add("/css/testfrombody.css")}
${scripts.add("/js/jstest.js")}

${bodyClass}

<ul><@list.firstLastList><li>apples</li><li>bananas</li></@list.firstLastList></ul>
