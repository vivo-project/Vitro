<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for Site Administration data input panel -->

<#import "lib-form.ftl" as form>
<#import "lib-generator-classes.ftl" as generators />

<#if dataInput?has_content>
    <section class="pageBodyGroup" role="region">
        <h3>${i18n().data_input}</h3>

        <form id="addIndividualClass" action="${dataInput.formAction}" method="get">
            <select id="VClassURI" name="typeOfNew" class="form-item long-options" role="select">
                <@form.optionGroups groups=dataInput.groupedClassOptions />
            </select>
            <input type="hidden" name="editForm" value="${generators.NewIndividualFormGenerator}" role="input" />
            <input type="submit" id="submit" value="${i18n().add_individual_of_class}" role="button" />
        </form>
        
        <section id="addClassBubble" role="region">
            <p>${i18n().please_create} <a title="${i18n().create_classgroup}" href="${urls.base}/editForm?controller=Classgroup">${i18n().a_classgroup}</a> ${i18n().associate_classes_with_group}</p>
        </section>
    </section>
</#if>

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/js/jquery-ui/css/smoothness/jquery-ui-1.8.9.custom.css" />')}

${scripts.add('<script type="text/javascript" src="${urls.base}/js/jquery-ui/js/jquery-ui-1.8.9.custom.min.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/siteAdmin/siteAdminUtils.js"></script>')}