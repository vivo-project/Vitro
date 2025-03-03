<#-- $This file is distributed under the terms of the license in LICENSE$ -->
<h2>${i18n().dd_config_title}</h2>
<section id="datadistributor-config" role="region">
    <form id="ddConfigForm" action="${submitUrlBase}" method="get">
        <select name="addType">
            <#list distributorTypes as distributorType>
                <option value="${distributorType.name}">${i18n()["dd_config_" + distributorType.name]}</option>
            </#list>
        </select>
        <input id="add" value="${i18n().dd_config_add}" role="button" type="submit" class="submit" accesskey="a">
    </form>
    <table id="table-listing">
        <tr>
            <th>${i18n().dd_config_name}</th>
            <th>${i18n().dd_config_type}</th>
            <th>${i18n().dd_config_edit}</th>
        </tr>
        <#list distributors as distributor>
            <tr>
                <td>
                    ${distributor.name}
                </td>
                <td>
                    ${i18n()["dd_config_" + distributor.className]}
                </td>
                <td>
                    <a href="${submitUrlBase}?editUri=${distributor.uri?url}"><img src="${urls.images!}/individual/editIcon.gif" alt="${i18n().dd_config_edit}"></a>
                    <#-- if distributor.persistent>
                        <a href="${submitUrlBase}?deleteUri=${distributor.uri?url}"><img src="${urls.images!}/individual/deleteIcon.gif" alt="${i18n().dd_config_delete}"></a>
                    </#if -->
                    <a href="${urls.base}/api/dataRequest/${distributor.name}"><img src="${urls.images!}/run.png" alt="${i18n().dd_config_run}"></a>
                </td>
            </tr>
        </#list>
    </table>
</section>

<h2>${i18n().dd_graphbuilder_config_title}</h2>
<section id="datadistributor-config" role="region">
    <form id="ddConfigForm" action="${submitUrlBase}" method="get">
        <select name="addType">
            <#list graphbuilderTypes as graphbuilderType>
                <option value="${graphbuilderType.name}">${i18n()["dd_config_" + graphbuilderType.name]}</option>
            </#list>
        </select>
        <input id="add" value="${i18n().dd_config_add}" role="button" type="submit" class="submit" accesskey="a">
    </form>
    <table id="table-listing">
        <tr>
            <th>${i18n().dd_config_name}</th>
            <th>${i18n().dd_config_type}</th>
            <th>${i18n().dd_config_edit}</th>
        </tr>
        <#list graphbuilders as graphbuilder>
            <tr>
                <td>
                    ${graphbuilder.name}
                </td>
                <td>
                    ${i18n()["dd_config_" + graphbuilder.className]}
                </td>
                <td>
                    <a href="${submitUrlBase}?editUri=${graphbuilder.uri?url}"><img src="${urls.images!}/individual/editIcon.gif" alt="${i18n().dd_config_edit}"></a>
                    <#-- if graphbuilder.persistent>
                        <a href="${submitUrlBase}?deleteUri=${graphbuilder.uri?url}"><img src="${urls.images!}/individual/deleteIcon.gif" alt="${i18n().dd_config_delete}"></a>
                    </#if -->
                </td>
            </tr>
        </#list>
    </table>
</section>
