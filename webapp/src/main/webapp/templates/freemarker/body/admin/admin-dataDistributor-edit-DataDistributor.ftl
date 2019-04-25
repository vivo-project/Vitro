<#-- $This file is distributed under the terms of the license in LICENSE$ -->
<#if objectClass??>
    <h2>${i18n()["dd_config_" + objectClass.name]}</h2>
</#if>
<#if addType??>
    <#assign submitUrl = "${submitUrlBase}?addType=${addType?url}" />
<#else>
    <#assign submitUrl = "${submitUrlBase}?editUri=${editUri?url}" />
</#if>
<noscript>
    <section id="error-alert">
        <img src="${urls.images}/iconAlertBig.png" alt="${i18n().alert_icon}"/>
        <p>${i18n().javascript_require_to_edit} <a href="http://www.enable-javascript.com" title="${i18n().javascript_instructions}">${i18n().to_enable_javascript}</a>.</p>
    </section>
</noscript>
<section id="dataa-config" role="region">
    <form id="ddConfigForm" action="${submitUrl}" method="post">
        <#-- List the action or builder name first -->
        <#list properties as property>
            <#if property.propertyUri == 'http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#actionName'>
                <@fieldForProperty property />
            <#elseif property.propertyUri == 'http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#builderName'>
                <@fieldForProperty property />
            </#if>
        </#list>
        <#list properties as property>
            <#if property.propertyUri == 'http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#actionName'>
            <#elseif property.propertyUri == 'http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#builderName'>
            <#else>
                <@fieldForProperty property />
            </#if>
        </#list>
        <#if readOnly??>
            <a href="${submitUrlBase}">cancel</a>
        <#else>
            <input type="hidden" name="submitted" value="submitted" />
            <input id="save" value="${i18n().dd_config_save}" role="button" type="submit" class="submit" />
            <a href="${submitUrlBase}">cancel</a>
            <#if addType??>
            <#else>
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                <button onclick="return confirmDeleteThis()">
                    <img src="${urls.images!}/individual/deleteIcon.gif" alt="${i18n().dd_config_delete}"> ${i18n().dd_config_delete}
                </button>
                <script lang="text/javascript">
                    function confirmDeleteThis() {
                        var confirmed = confirm("${i18n().dd_config_delete_confirm}");
                        if (confirmed) {
                            window.location.href = "${submitUrlBase}?deleteUri=${editUri?url}";
                        }
                        return false;
                    }
                </script>
            </#if>
        </#if>
    </form>
</section>

<#macro fieldForProperty property>
    <#if property.minOccurs &gt; 0>
        <#local required = true />
    <#else>
        <#local required = false />
    </#if>
    <div id="field_${property.propertyUri}" class="field">
        <label>
            <#if required>
                <span class="required">*</span>
            </#if>
            ${i18n()["dd_config_" + property.propertyUri]}
        </label>
        <#switch property.parameterType.name>
            <#case 'java.lang.String'>
                <#if fields[property.propertyUri]??>
                    <#assign values = fields[property.propertyUri] />
                    <#list values as value>
                        <@inputField property.propertyUri value required />
                    </#list>
                <#else>
                    <@inputField property.propertyUri "" required />
                </#if>
            <#break>
            <#case 'edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributor'>
                <select name="${property.propertyUri}">
                    <#list datadistributors as datadistributor>
                        <option value="${datadistributor.uri}" <#if datadistributor.uri == property.propertyUri>selected</#if> >${datadistributor.name}</option>
                    </#list>
                </select>
            <#break>
            <#case 'edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.graphbuilder.GraphBuilder'>
                <select name="${property.propertyUri}">
                    <#list graphbuilders as graphbuilder>
                        <option value="${graphbuilder.uri}" <#if graphbuilder.uri == property.propertyUri>selected</#if> >${graphbuilder.name}</option>
                    </#list>
                </select>
                <#break>
            <#default>
                <div>${property.parameterType.name}</div>
                <#if fields[property.propertyUri]??>
                    <#assign values = fields[property.propertyUri] />
                    <#list values as value>
                        <div>${value}</div>
                    </#list>
                <#else>
                </#if>
            <#break>
        </#switch>
    </div>
    <#switch property.parameterType.name>
        <#case 'java.lang.String'>
            <#if property.maxOccurs &gt; 1>
                <#if property.maxOccurs &gt; 25>
                    <#local maxOccurs=25 />
                <#else>
                    <#local maxOccurs=property.maxOccurs />
                </#if>
                <p><a onclick="return addTextField('${property.propertyUri}', ${maxOccurs})"  id="add_${property.propertyUri}"><img src="${urls.images!}/individual/addIcon.gif" alt="${i18n().dd_config_add_field}"> ${i18n().dd_config_add_field}</a></p>
            </#if>
        <#break>
    </#switch>
</#macro>

<#macro inputField propertyUri value required>
    <#switch propertyUri>
        <#case 'http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#query'>
        <#case 'http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#constructQuery'>
        <#case 'http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#drillDownQuery'>
            <textarea id="yasqe" name='${propertyUri}' rows='30' cols='100' class="span-23 maxWidth" <#if readOnly??>readonly</#if>>${value}</textarea>
            <#if readOnly??>
            <#else>
                <#assign enableYasqe = true />
            </#if>
        <#break>
        <#default>
            <p><input type="text" name="${propertyUri}" size="50" value="${value}" <#if required>required</#if> <#if readOnly??>readonly</#if> /></p>
        <#break>
    </#switch>
</#macro>

<#if enableYasqe??>
    ${stylesheets.add('<link rel="stylesheet" href="//cdn.jsdelivr.net/yasqe/2.6.1/yasqe.min.css" />')}
    ${headScripts.add('<script type="text/javascript" src="//cdn.jsdelivr.net/yasqe/2.6.1/yasqe.bundled.min.js"></script>')}
    <script type="text/javascript">
        YASQE.defaults.createShareLink = false;
        YASQE.defaults.persistent = false;
        var yasqe = YASQE.fromTextArea(document.getElementById('yasqe'));
    </script>
</#if>

<script lang="text/javascript">
    function addTextField(id, count) {
        var element = document.getElementById('field_' + id);
        if (element != null) {
            if (element.childElementCount - 1 < count) {
                element.insertAdjacentHTML('beforeend', '<p><input type="text" name="' + id + '" size="50" /></p>');
            }

            if (element.childElementCount - 1 >= count) {
                var link = document.getElementById('add_' + id);
                if (link != null) {
                    link.remove();
                }
            }
        }

        return false;
    }
</script>
