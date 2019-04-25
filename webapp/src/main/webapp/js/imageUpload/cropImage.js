/* $This file is distributed under the terms of the license in LICENSE$ */

(function($) {

	$(window).on("load", function(){

		var jcrop_api = $.Jcrop('#cropbox',{
			/*onChange: showPreview,*/
			onSelect: showPreview,
			setSelect:   [ 0, 0, 115, 115 ],
			minSize: [ 115, 115 ],
			boxWidth: 650,
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

				$('input[name=x]').val(Math.round(coords.x));
				$('input[name=y]').val(Math.round(coords.y));
				$('input[name=w]').val(Math.round(coords.w));
				$('input[name=h]').val(Math.round(coords.h));
			}
		};

	});

}(jQuery));
