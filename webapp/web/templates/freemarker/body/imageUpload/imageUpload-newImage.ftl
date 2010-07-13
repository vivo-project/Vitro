<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Upload a replacement main image for an Individual. -->

${scripts.add("/js/jquery.js")}
${scripts.add("/js/imageUpload/imageUploadUtils.js")}



${stylesheets.addFromTheme("/uploadImages.css")}


<noscript>
	<div id="javascriptDisableWrapper">
		<div id="javascriptDisableContent">
			<img src="${urls.siteIcons}/iconAlertBig.png" alt="Alert Icon"/>
			<p>In order to upload or edit a photo, you'll need to enable JavaScript.</p>
		</div>
	</div>
</noscript>




<div id="photoUploadContainer" class="hidden">
       <h2>Photo Upload</h2>
       
       
       <#if errorMessage??>
 		 <div id="errorAlert"><img src="${urls.siteIcons}/iconAlert.png"/>
                  <p>${errorMessage}</p>
           </div>
		</#if>
		 
		
		
       <div id="photoUploadDefaultImageContainer">
              <h6>Current Photo</h6>
              <img src="${thumbnailUrl}" width="115" height="115" /> </div>
              
       <div id="photoUploadForm">
       
              <form action="${formAction}" enctype="multipart/form-data" method="post">
                     <label>Upload a photo <span> (JPEG, GIF or PNG)</span></label>
                     <input id="datafile" type="file" name="datafile" size="30">
                     <input type="submit" value="Upload photo"> or <a class="cancelUpload"  href="${cancelUrl}">Cancel</a>
              </form>
       </div>
</div>

