<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Upload a replacement main image for an Individual. -->

${stylesheets.addFromTheme("/uploadImages.css")}

<#if errorMessage??>
    <h2>${errorMessage}</h2>
</#if>

<div id="photoUploadContainer">
       <h2>Photo Upload</h2>
       <div id="photoUploadDefaultImageContainer">
              <h6>Current Photo</h6>
              <img src="${thumbnailUrl}" width="115" height="115" /> <br />
       <a href="${deleteUrl}">Delete photo</a></div>
       <div id="photoUploadForm">
              <form action="${formAction}" enctype="multipart/form-data" method="post">
                     <label>Replace  Photo <span> (JPEG, GIF or PNG. Max Size: 50 MB)</span></label>
                     <input type="file" name="datafile" size="30">
                     <input type="submit" value="Upload a photo"> or <a href="${cancelUrl}">Cancel</a>
              </form>
       </div>
</div>