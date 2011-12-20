<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#--Template for removing menu items -->

<h3>Remove menu item</h3>

<section id="remove-menu-item" role="region">
    <form method="POST" action="${formUrls}" class="customForm" role="remove menu item">
        <input type="hidden" name="menuItem" id="menuItem" value="${menuItem}" role="input" />
        <input type="hidden" name="cmd" id="cmd" value="Remove" role="input" />
        <input type="hidden" name="switchToDisplayModel" id="switchToDisplayModel" value="true" role="input" />

        <p>Are you sure you want to remove <em>${menuName}</em> menu item?</p>

        <input type="submit" name="removeMenuItem" value="Remove menu item" class="submit" role="input" /> or <a class="cancel" href="${cancelUrl}" title="cancel">Cancel</a>
    </form>
</section>

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/menupage/menuManagement.css" />')}
${stylesheets.add('<link rel="stylesheet" href="${urls.base}/edit/forms/css/customForm.css" />')}
