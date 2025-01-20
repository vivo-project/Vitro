/* $This file is distributed under the terms of the license in LICENSE$ */

function enforceAspectRatio(c, maxAspectRatio) {
	const aspectRatio = c.w / c.h;

	if (aspectRatio < 1) {				
		c.w = c.h; // Force 1:1		
		if (this.setSelect) this?.setSelect([c.x, c.y, c.x + c.w, c.y + c.h]);

	} else if (aspectRatio > maxAspectRatio) {
		c.h = c.w / (maxAspectRatio - 0.05); // Force 1:maxAspectRatio
		if (this.setSelect) this?.setSelect([c.x, c.y, c.x + c.w, c.y + c.h]);
	}

	return;
}

function enforceAspectRatioSmall(c) {
	enforceAspectRatio.call(this, c, 3);
}

function enforceAspectRatioLarge(c) {
	enforceAspectRatio.call(this, c, 7.5);
}



(function($) {

	$(window).on("load", function(){



		var urlParams = new URLSearchParams(window.location.search);
		var photoType = urlParams.get('photoType');
		let aspectRatio = 1;
		let minSize = [ 115, 115 ]

		let checkRatio = undefined
		if (photoType === 'portalLogo') {
			aspectRatio = undefined
			minSize = [ 48, 48 ]
			checkRatio = enforceAspectRatioLarge
		} else if (photoType === 'portalLogoSmall') {
			aspectRatio = undefined
			minSize = [ 48, 48 ]
			checkRatio = enforceAspectRatioSmall
		}

		
		var jcrop_api = $.Jcrop('#cropbox',{
			/*onChange: showPreview,*/
			onSelect: showPreview,
			setSelect:   [ 0, 0, 115, 115 ],
			minSize: minSize,
			boxWidth: 650,
			onChange: onCropChange,
			onSelect: onCropSelect,
			aspectRatio: aspectRatio

		});


		function onCropChange(c) {			
			checkRatio?.call(this,c)
		}

		function onCropSelect(c) {
			checkRatio?.call(this,c)
			showPreview(c)
		}





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

				$('input[name=x]').val(Math.round(coords.x));
				$('input[name=y]').val(Math.round(coords.y));
				$('input[name=w]').val(Math.round(coords.w));
				$('input[name=h]').val(Math.round(coords.h));
			}
		};

	});

}(jQuery));
