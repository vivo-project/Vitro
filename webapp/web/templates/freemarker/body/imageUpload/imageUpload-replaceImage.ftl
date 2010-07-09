<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Upload a replacement main image for an Individual. -->

${scripts.add("/js/jquery.js")}
${scripts.add("/js/imageUpload/imageUploadUtils.js")}



${stylesheets.addFromTheme("/uploadImages.css")}

<#if errorMessage??>
   <script type="text/javascript">  
    window.onload = load;  
	function load(){  
 		alert("${errorMessage}");
 		}
 	</script>
</#if>

<div id="photoUploadContainer">
       <h2>Photo Upload</h2>
       <div id="photoUploadDefaultImageContainer">
              <h6>Current Photo</h6>
              <img src="${thumbnailUrl}" width="115" height="115" />
       <a href='javascript:delete_photo("${deleteUrl}");'>Delete photo</a></div>
       <div id="photoUploadForm">
              <form action="${formAction}" enctype="multipart/form-data" method="post">
                     <label>Replace Photo <span> (JPEG, GIF or PNG)</span></label>
                     <input  type="file" name="datafile" size="30">
                     <input   type="submit" value="Upload photo"> or <a class="cancelUpload" href="${cancelUrl}">Cancel</a>
              </form>
       </div>
</div>
