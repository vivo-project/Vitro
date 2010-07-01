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
		
		$('#submitPhoto').click(function() {
			var preview = $('#preview');
			$('input[name=x]').val(preview.css('marginLeft').replace(/[^\d-]/g,''));
			$('input[name=y]').val(preview.css('marginTop').replace(/[^\d-]/g,''));
			$('input[name=w]').val(preview.css('width').replace(/[^\d-]/g,''));
			$('input[name=h]').val(preview.css('height').replace(/[^\d-]/g,''));			
		});

	});

}(jQuery));