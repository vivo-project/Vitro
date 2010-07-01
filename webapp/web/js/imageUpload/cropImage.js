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
		
		function removeAlpha(str) {
			return str.replace(/[^\d-]/g,'');
		};
		
		$('#cropImage').submit(function() {
			var preview = $('#preview');
			$('input[name=x]').val(removeAlpha(preview.css('marginLeft')));
			$('input[name=y]').val(removeAlpha(preview.css('marginTop')));
			$('input[name=w]').val(removeAlpha(preview.css('width')));
			$('input[name=h]').val(removeAlpha(preview.css('height')));			
		});

	});

}(jQuery));