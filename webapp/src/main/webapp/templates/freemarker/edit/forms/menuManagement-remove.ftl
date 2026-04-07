<#-- $This file is distributed under the terms of the license in LICENSE$ -->

<#--Template for removing menu items -->

<h3>${i18n().remove_menu_item}</h3>

<section id="remove-menu-item" role="region">
    <form method="POST" action="${formUrls}" class="customForm" role="remove menu item">
        <input type="hidden" name="menuItem" id="menuItem" value="${menuItem}" />
        <input type="hidden" name="cmd" id="cmd" value="Remove" />
        <input type="hidden" name="switchToDisplayModel" id="switchToDisplayModel" value="true" />

        <p>${i18n().confirm_menu_item_delete} <em>${menuName}</em> ${i18n().menu_item}?</p>

        <input type="submit" name="removeMenuItem" value="${i18n().remove_menu_item}" class="submit" /> ${i18n().or} <a class="cancel" href="${cancelUrl}" title="${i18n().cancel_title}">${i18n().cancel_link}</a>
    </form>
</section>

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/menupage/menuManagement.css" />')}
${stylesheets.add('<link rel="stylesheet" href="${urls.base}/templates/freemarker/edit/forms/css/customForm.css" />')}
