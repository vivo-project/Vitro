<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<div class="staticPageBackground">

<h2> Export to RDF </h2>

<form action="" method="get">

<ul>
    <li style="list-style-type:none;"><input type="radio" name="subgraph" checked="checked" value="full"/> Export entire RDF model (including application metadata)</li>
    <li style="list-style-type:none;"><input type="radio" name="subgraph" value="tbox"/> Export ontology/ontologies (TBox)</li>
    <li style="list-style-type:none;"><input type="radio" name="subgraph" value="abox"/> Export instance data (ABox)</li>
</ul>

<hr/>

<ul>
    <li style="list-style-type:none;"><input type="radio" name="assertedOrInferred" checked="checked" value="asserted"/> Export only asserted statements </li>
    <li style="list-style-type:none;"><input type="radio" name="assertedOrInferred" value="inferred"/> Export only inferred statements </li>
    <li style="list-style-type:none;"><input type="radio" name="assertedOrInferred" value="full"/> Export asserted and inferred statements together </li>
</ul>

<h3>Select format</h3>
<select name="format">
    <option value="RDF/XML">RDF/XML</option>
    <option value="RDF/XML-ABBREV">RDF/XML abbrev.</option>
    <option value="N3">N3</option>
    <option value="N-TRIPLES">N-Triples</option>
    <option value="TURTLE">Turtle</option>
</select>

<input type="submit" name="submit" value="Export"/>

</form>

</div>