<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<script type="text/javascript" language="JavaScript" src="js/toggle.js"></script>
<script type="text/javascript" language="JavaScript">
/**************************************************** MODE OPTIONS  ***********************************************************/

function refreshModeValue() {
	var thumbnailMode = document.uploadForm.type[1].checked;
	thumbnailMode = !thumbnailMode;
	//alert("thumbnailMode set to " + thumbnailMode );
	if ( thumbnailMode ) {
		document.uploadForm.type[0].checked=true;
		document.uploadForm.type[1].checked=false;
	} else {
		document.uploadForm.type[0].checked=false;
		document.uploadForm.type[1].checked=true;
	}
	switchElementDisplay('thumbnailExtra');
}
</script>

<style type="text/css">
.dropdownExtra { display: none; }
</style>



