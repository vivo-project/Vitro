<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Crop the replacement main image for an Individual, to produce a thumbnail. -->

${scripts.add("/js/jquery.js")}
${scripts.add("/js/jquery_plugins/jcrop/jquery.Jcrop.js")}
${scripts.add("/js/imageUpload/cropImage.js")}


${stylesheets.addFromTheme("/uploadImages.css")}
${stylesheets.addFromTheme("/jquery.Jcrop.css")}


<#--Reduce original image to fit in the page layout  
	If the width of the image is bigger or equal to 500 pixels, 
	the script below will reduce the width to 500 pixels and 
	the height will be in proportion to the new height-->

<#macro newImageSize>
<#if (imageWidth >= 500)>
		width="500" height="${(500*imageHeight)/imageWidth}" 
</#if>	   
</#macro>
			

<div id="photoCroppingContainer">
       <h2>Photo Upload</h2>
       <!-- This is the image we're attaching Jcrop to -->
       <div id="photoCroppingPreview">
              <h6>Current Photo </h6>
              <p class="photoCroppingTitleBody">Your profile photo will look like the image below. </p>
              <div style="width:115px;height:115px;overflow:hidden;border:1px solid green;"> <img src="${imageUrl}" id="preview" /> </div>
              <div id="photoCroppingHowTo">
                     <p class="photoCroppingNote">To make adjustments, you can drag around and resize the photo to the right. When you are happy with your photo click the "Save Photo" button. </p>
                     <form id="cropImage" action="${formAction}"  method="post">
                     
                     <!-- Javascript will populate these values -->
                     <input type="hidden" name="x" value="" />
                     <input type="hidden" name="y" value="" />
                     <input type="hidden" name="w" value="" />
                     <input type="hidden" name="h" value="" />
                                          
                     <input  type="submit" value="Save photo">
                     or <a class="cancelUpload"  href="${cancelUrl}">Cancel</a>
                     </form>
              </div>
       </div>
       <div id="photoCropping">
             
                     <img style="border:1px solid green;" src="${imageUrl}" <@newImageSize/> id="cropbox" />
                   
       </div>
</div>
<div class="clear"></div>
