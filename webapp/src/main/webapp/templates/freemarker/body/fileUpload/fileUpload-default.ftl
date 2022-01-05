<#-- $This file is distributed under the terms of the license in LICENSE$ -->


${scripts.add('<script type="text/javascript" src="${urls.base}/js/fileUpload/fileUploadUtils.js"></script>')}

<#assign i18n = i18n() >

<section id="fileploadContainer" role="region">
    <h2>${i18n.file_upload_heading}</h2>

    <#if errorMessage??>
        <section id="error-alert" role="alert"><img src="${urls.images}/iconAlert.png" alt="${i18n.alt_error_alert}" />
            <p>${errorMessage}</p>
        </section>
    </#if>
	<#if action?? && action == "upload" >
	    <form id="fileUploadForm" action="${formAction}" enctype="multipart/form-data" method="post" role="form">
	        <label>${i18n.file_upload_supported_media}</label>
			<#if supportedMediaTypes??>
	        	<p>${supportedMediaTypes}</p>
			</#if>
	        <input id="datafile" type="file" name="datafile" size="30" />
	        <p class="note">${i18n.maximum_file_size(maxFileSize)}</p>
	        <input class="submit" type="submit" value="${i18n.file_upload_submit_label}"/>
	        <span class="or"> ${i18n.or} <a class="cancel"  href="${referrer}" title="${i18n.cancel_title}">${i18n.cancel_link}</a></span>
	    </form>
    </#if>
</section>
