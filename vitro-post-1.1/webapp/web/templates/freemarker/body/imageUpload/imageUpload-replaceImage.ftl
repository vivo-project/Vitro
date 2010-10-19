<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Upload a replacement main image for an Individual. -->

${scripts.add("/js/jquery.js")}
${scripts.add("/js/imageUpload/imageUploadUtils.js")}

${stylesheets.addFromTheme("/css/uploadImages.css")}

<div id="photoUploadContainer">
    <h2>Photo Upload</h2>
    <#if errorMessage??>
        <div id="errorAlert"><img src="${urls.siteIcons}/iconAlert.png"/>
            <p>${errorMessage}</p>
        </div>
    </#if>
    <div id="photoUploadDefaultImageContainer">
        <h6>Current Photo</h6>
            <img src="${thumbnailUrl}" width="115" />
            <a class="thumbnail" href="${deleteUrl}">Delete photo</a>
    </div>
       
    <div id="photoUploadForm">
        <form action="${formAction}" enctype="multipart/form-data" method="post">
            <label>Replace Photo <span> (JPEG, GIF or PNG)</span></label>
            <input  type="file" name="datafile" size="30">
            <input   type="submit" value="Upload photo"> 
            <span class="or"> or 
            <a class="cancelUpload"  href="${cancelUrl}">Cancel</a></span>
         </form>
    </div>
</div>
