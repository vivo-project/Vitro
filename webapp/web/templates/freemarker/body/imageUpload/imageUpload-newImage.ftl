<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Upload a replacement main image for an Individual. -->

${scripts.add('<script type="text/javascript" src="${urls.base}/js/jquery.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/imageUpload/imageUploadUtils.js"></script>')}

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/uploadImages.css" />')}

<section id="photoUploadContainer" role="region">
    <h2>Photo Upload</h2>
              
    <#if errorMessage??>
        <section id="error-alert" role="alert"><img src="${urls.images}/iconAlert.png" alt="Error alert icon" />
            <p>${errorMessage}</p>
        </section>
    </#if>

    <section id="photoUploadDefaultImage" role="region">
        <h3>Current Photo</h3>
        
        <img src="${thumbnailUrl}" width="115" height="115" alt="Individual photo" /> 
    </section>
          
    <form id="photoUploadForm" action="${formAction}" enctype="multipart/form-data" method="post" role="form">
        <label>Upload a photo <span> (JPEG, GIF or PNG)</span></label>
        
        <input id="datafile" type="file" name="datafile" size="30" />
         <p class="note">Maximum file size: ${maxFileSize} megabytes<br />
        Minimum image dimensions: ${thumbnailWidth} x ${thumbnailHeight} pixels</p>
        <input class="submit" type="submit" value="Upload photo"/>
        
        <span class="or"> or <a class="cancel"  href="${cancelUrl}" title="cancel">Cancel</a></span>
    </form>
</section>