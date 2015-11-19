<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Upload a replacement main image for an Individual. -->

${scripts.add('<script type="text/javascript" src="${urls.base}/js/jquery.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/imageUpload/imageUploadUtils.js"></script>')}

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/uploadImages.css" />')}

<#assign i18n = i18n() >

<section id="photoUploadContainer" role="region">
    <h2>${i18n.upload_heading}</h2>
              
    <#if errorMessage??>
        <section id="error-alert" role="alert"><img src="${urls.images}/iconAlert.png" alt="${i18n.alt_error_alert}" />
            <p>${errorMessage}</p>
        </section>
    </#if>

    <section id="photoUploadDefaultImage" role="region">
        <h3>${i18n.current_photo}</h3>
        
        <img src="${thumbnailUrl}" width="115" height="115" alt="${i18n.alt_thumbnail_photo}" /> 
    </section>
          
    <form id="photoUploadForm" action="${formAction}" enctype="multipart/form-data" method="post" role="form">
        <label>${i18n.upload_photo} <span>${i18n.photo_types}</span></label>
        
        <input id="datafile" type="file" name="datafile" size="30" />
         <p class="note">${i18n.maximum_file_size(maxFileSize)}<br />
        ${i18n.minimum_image_dimensions(thumbnailWidth, thumbnailHeight)}</p>
        <input class="submit" type="submit" value="${i18n.submit_upload}"/>
        
        <span class="or"> ${i18n.or} <a class="cancel"  href="${cancelUrl}" title="${i18n.cancel_title}">${i18n.cancel_link}</a></span>
    </form>
</section>

<script type="text/javascript">
    i18n_confirmDelete = ${i18n.confirm_delete}
</script>
