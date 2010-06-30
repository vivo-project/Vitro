<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Crop the replacement main image for an Individual, to produce a thumbnail. -->

${scripts.add("/js/jquery-plugins/jcrop/jquery-1.4.2.pack.js")}
${scripts.add("/js/jquery-plugins/jcrop/jquery.Jcrop.js")}

${stylesheets.addFromTheme("/uploadImages.css")}
${stylesheets.addFromTheme("/jquery.Jcrop.css")}

<script language="Javascript">

		(function($) {

			$(window).load(function(){

				var jcrop_api = $.Jcrop('#cropbox',{
					onChange: showPreview,
					onSelect: showPreview,
					aspectRatio: 1
				});

				var bounds = jcrop_api.getBounds();
				var boundx = bounds[0];
				var boundy = bounds[1];

				function showPreview(coords)
				{
					if (parseInt(coords.w) > 0)
					{
						var rx = 115 / coords.w;
						var ry = 115 / coords.h;

						$('#preview').css({
							width: Math.round(rx * boundx) + 'px',
							height: Math.round(ry * boundy) + 'px',
							marginLeft: '-' + Math.round(rx * coords.x) + 'px',
							marginTop: '-' + Math.round(ry * coords.y) + 'px'
						});
					}
				};

			});

		}(jQuery));

</script>

<div id="photoCroppingContainer">
       <h2>Photo Upload</h2>
       <!-- This is the image we're attaching Jcrop to -->
       <div id="photoCroppingPreview">
              <h6>Current Photo </h6>
              <p class="photoCroppingTitleBody">Your profile photo will look like the image below. </p>
              <div style="width:115px;height:115px;overflow:hidden;border:1px solid green;"> <img src="${imageUrl}" id="preview" /> </div>
              <div id="photoCroppingHowTo">
                     <p class="photoCroppingNote">To make adjustments, you can drag around and resize the blue square to the right. When you are happy with your photo click the “Save Photo” button. </p>
                     <form action="${formAction}"  method="post">
                     
                     <!-- Totally bogus -->
                     <input type="hidden" name="x" value="75">
                     <input type="hidden" name="y" value="50">
                     <input type="hidden" name="h" value="150">
                     <input type="hidden" name="w" value="150">
                     
                     <input type="submit" value="Save photo">
                     or <a href="${cancelUrl}">Cancel</a>
                     </form>
              </div>
       </div>
       <div id="photoCropping">
             
                     <img style="border:1px solid green;" src="${imageUrl}" id="cropbox" />
                   
       </div>
</div>
