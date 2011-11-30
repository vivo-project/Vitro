<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Crop the replacement main image for an Individual, to produce a thumbnail. -->

${scripts.add('<script type="text/javascript" src="${urls.base}/js/jquery.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/jquery_plugins/jcrop/jquery.Jcrop.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/imageUpload/cropImage.js"></script>')}

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/uploadImages.css" />',
                  '<link rel="stylesheet" href="${urls.base}/js/jquery_plugins/jcrop/jquery.Jcrop.css" />')}

<#--Reduce original image to fit in the page layout  
	If the width of the image is bigger or equal to 500 pixels, 
	the script below will reduce the width to 500 pixels and 
	the height will be in proportion to the new height-->

<#--<#macro newImageSize>
<#if (imageWidth >= 500)>
		width="500" height="${(500*imageHeight)/imageWidth}" 
</#if>	   
</#macro>-->


<section id="photoCroppingContainer" role="region">
    <h2>Photo Upload</h2>
    
    <!-- This is the image we're attaching Jcrop to -->
    <section id="photoCroppingPreview" role="region">
        
        <p class="photoCroppingTitleBody">Your profile photo will look like the image below. </p>
        
        <section class="photoCroppedPreview" role="region">
            <img src="${imageUrl}" id="preview" alt="Image to be cropped"/>
        </section>
        
        <section id="photoCroppingHowTo" role="region">
            <p class="photoCroppingNote">To make adjustments, you can drag around and resize the photo to the right. When you are happy with your photo click the "Save Photo" button. </p>
            
            <form id="cropImage" action="${formAction}"  method="post" role="form">
                <!-- Javascript will populate these values -->
                <input type="hidden" name="x" value="" />
                <input type="hidden" name="y" value="" />
                <input type="hidden" name="w" value="" />
                <input type="hidden" name="h" value="" />
                                      
                <input  class="submit" type="submit" value="Save photo">
                
                <span class="or"> or <a class="cancel"  href="${cancelUrl}" title="cancel">Cancel</a></span>
            </form>
       </section>
    </section>
    
    <section id="photoCropping" role="region">
        <img src="${imageUrl}" id="cropbox" alt="Preview of photo cropped" />
    </section
</section>

<div class="clear"></div>
