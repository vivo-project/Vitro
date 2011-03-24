<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<!-- this is a page set up to be parsed by the dynamicFields tag -->
<!-- the datapropsNN, $$, $foo, and @x symbols are special notations understood by this tag and are not part of JSP -->

<!-- @pre -->

<li style="padding-top:0.8em;padding-bottom:0.5em;" id="datapropsNNsuper">
<strong>$fieldName</strong><a id="datapropsNNaddLink" style="margin-left:0.5em;font-style:italic;font-weight:bold;" href="#" onClick="addLine(this, 'dataprops');return false;">add</a>

<span id="datapropsNNgenTaName" style="display:none;">$genTaName</span>

<ul id="datapropsNNul">

<li style="display:none;">This is a dummy li to make sure the ul has at least one child.</li>

<!-- @template -->

<li id="datapropsNN" style="margin-left:0em;margin-top:0.4em;margin-bottom:0.4em;">
    <div id="datapropsNNcontent">
        <span id="datapropsNNcontentValue">$$</span>
        <a id="datapropsNNeditLink" href="#" style="margin-left:0.8em;font-style:italic" onClick="convertLiToTextarea(this, 'dataprops');return false;">edit</a>
        <a id="datapropsNNdeleteLink" href="#" style="margin-left:0.8em;font-style:italic" onClick="deleteLine(this, 'dataprops');return false;">remove</a>
	<a id="datapropsNNundeleteLink" href="#" style="display:none;margin-left:0.8em;font-style:italic" onClick="undeleteLine(this, 'dataprops');return false;">restore</a>
    </div>
    <div id="datapropsNNta" style="display:none;">
        <div style="padding:0;margin:0;">
            <textarea style="margin:0;padding:0;width:95%;height:16ex;" id="datapropsNNtata" name="$taName">$$</textarea>
        </div>
        <div style="padding:0;margin:0;">
            <input id="datapropsNNokLink" style="margin:0;padding:0;" type="button" onClick="backToLi(this);return false;" value='OK'/>
            <input id="datapropsNNcancelLink" style="margin:0;padding:0;" type="button" onClick="cancelBackToLi(this);return false;" value='cancel'/>
            <input id="datapropsNNosLink" style="margin-left:5em;padding:0;" type="button" onClick="backToLi(this);submitPage();return false;" value='OK & save all changes'/>
        </div>
    </div>
</li>

<!-- @post -->

</ul>
</li>

