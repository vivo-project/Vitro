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


<#assign i18n = i18n() >

<section id="photoCroppingContainer" role="region">
    <h2>${i18n.upload_heading}</h2>
    
    <!-- This is the image we're attaching Jcrop to -->
    <section id="photoCroppingPreview" role="region">
        
        <p class="photoCroppingTitleBody">${i18n.cropping_caption}</p>
        
        <section class="photoCroppedPreview" role="region">
            <img src="${imageUrl}" id="preview" alt="${i18n.alt_image_to_crop}"/>
        </section>
        
        <section id="photoCroppingHowTo" role="region">
            <p class="photoCroppingNote">${i18n.cropping_note}</p>
            
            <form id="cropImage" action="${formAction}"  method="post" role="form">
                <!-- Javascript will populate these values -->
                <input type="hidden" name="x" value="" />
                <input type="hidden" name="y" value="" />
                <input type="hidden" name="w" value="" />
                <input type="hidden" name="h" value="" />
                                      
                <input  class="submit" type="submit" value="${i18n.submit_save}">
                
                <span class="or"> ${i18n.or} <a class="cancel"  href="${cancelUrl}" title="${i18n.cancel_title}">${i18n.cancel_link}</a></span>
            </form>
       </section>
    </section>
    
    <section id="photoCropping" role="region">
        <img src="${imageUrl}" id="cropbox" alt="${i18n.alt_preview_crop}" />
    </section
</section>

<div class="clear"></div>
