<#-- $This file is distributed under the terms of the license in LICENSE$ -->

<section id="pageList">
    <div class="tab">
        <h2>${i18n().page_management}</h2>
    </div>


<#if pages?has_content >
<table id="table-listing" style="margin-bottom:2px">  <caption>${i18n().page_management}</caption>

    <thead>
      <tr>
        <th scope="col">${i18n().title_capitalized}</th>
        <!--th scope="col">${i18n().type_capitalized}</th-->
        <th scope="col">URL</th>
        <th scope="col">${i18n().custom_template}</th>
        <th id="isMenuPage" scope="col" >${i18n().menu_page}</th>
        <th id="iconColumns" scope="col">${i18n().controls}</th>
      </tr>
    </thead>

    <tbody>
    <#list pages as pagex>
    	 <tr>
            <td>
            	<#if pagex.listedPageUri?has_content>
            	    <#if pagex.listedPageTitle == "Home" >
            	        ${pagex.listedPageTitle!}
            	    <#else>
            		<a href="${urls.base}/editRequestDispatch?subjectUri=${pagex.listedPageUri?url}&switchToDisplayModel=1&editForm=edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators.ManagePageGenerator" title="${i18n().listed_page_title}">${(pagex.listedPageTitle)!i18n().untitled}</a>
            		</#if>

            	<#else>
            		${i18n().uri_not_defined}
            	</#if>
            </td>
            <!--td> {pagex.dataGetterLabel}</td-->
            <td>${pagex.listedPageUrlMapping}</td>
            <td>${(pagex.listedPageTemplate)!''}</td>
            <td style="text-align:center">
            <#if pagex.listedPageMenuItem?has_content>
            	<div class="menuFlag"></div>
            </#if>
            </td>
            <td>
                <a href="${urls.base}/editRequestDispatch?subjectUri=${pagex.listedPageUri?url}&switchToDisplayModel=1&editForm=edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators.ManagePageGenerator" title=""><img src="${urls.images!}/individual/editIcon.gif" alt="${i18n().edit_page}"></a>
                &nbsp;&nbsp;
                <a href="${urls.base}/individual?uri=${pagex.listedPageUri?url}&switchToDisplayModel=1" title="${i18n().view_profile_for_page}"><img src="${urls.images!}/profile-page-icon.png" alt="${i18n().view_profile_for_page}"></a>
                &nbsp;&nbsp;
                <#if !pagex.listedPageCannotDeletePage?has_content >
                    <a cmd="deletePage" pageTitle=" ${pagex.listedPageTitle!}"  href="${urls.base}/deletePageController?pageURI=${pagex.listedPageUri?url}" title="${i18n().delete_page}"><img src="${urls.images!}/individual/deleteIcon.gif" alt="${i18n().delete_page}"></a>
                </#if>
            </td>
        </tr>


    </#list>
    </tbody>
  </table>

<#else>
    <p>${i18n().no_pages_defined}</p>
</#if>

  <form id="pageListForm" action="${urls.base}/editRequestDispatch" method="get">
      <input type="hidden" name="typeOfNew" value="http://vitro.mannlib.cornell.edu/ontologies/display/1.1#Page">
      <input type="hidden" name="switchToDisplayModel" value="1">
      <input type="hidden" name="editForm" value="edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators.ManagePageGenerator" role="input">
 	<input id="submit" value="${i18n().add_page}" role="button" type="submit" >
  </form>
  <br />
 <p style="margin-top:10px">${i18n().use_capitalized} <a id="menuMgmtLink" href="${urls.base}/individual?uri=http%3A%2F%2Fvitro.mannlib.cornell.edu%2Fontologies%2Fdisplay%2F1.1%23DefaultMenu&switchToDisplayModel=true&previous=pageManagement" title="">${i18n().menu_orering}</a> ${i18n().to_order_menu_items}</p>
</section>

<script>
    var i18nStrings = {
        confirmPageDeletion: "${i18n().confirm_page_deletion?js_string}"
    };
</script>
${stylesheets.add('<link rel="stylesheet" href="${urls.base}/js/jquery-ui/css/smoothness/jquery-ui-1.12.1.css" />',
				'<link rel="stylesheet" href="${urls.base}/css/menupage/pageList.css" />')}

${scripts.add('<script type="text/javascript" src="${urls.base}/js/jquery-ui/js/jquery-ui-1.12.1.min.js"></script>')}
${scripts.add('<script type="text/javascript" src="${urls.base}/js/customFormUtils.js"></script>')}
${scripts.add('<script type="text/javascript" src="${urls.base}/js/browserUtils.js"></script>')}
${scripts.add('<script type="text/javascript" src="${urls.base}/js/pageDeletion.js"></script>')}


