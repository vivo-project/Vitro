<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>


<div class="contents">

<h1>Import OWL/RDFS Ontologies</h1>

<span class="warning">Note:  This is not yet fully stable.  Avoid running on production servers.</span>

<form name="owlFromUri" method="post" action="importOwl">
<p>Import OWL/RDF XML ontology from URI:</p>

<input name="owlUri" size="60%"/>
<input type="submit" value="Import"/>

<p>Value to use for property domains/ranges that are not specified or cannot be interpreted:
<select name="nulldomrange">
	<option value="none"><em>none</em></option>
	<option value="owl:Thing" selected="selected">owl:Thing</option>
</select></p>

<p>Format <select name="isN3">
	<option value="false">RDF/XML</option>
	<option value="true">N3</option>
</select></p>
<!-- <input type="checkbox" disabled="disabled" name="omitInd" />
import classes and properties only (no individuals) -->
</form>

</div><!-- contents -->


</div>

