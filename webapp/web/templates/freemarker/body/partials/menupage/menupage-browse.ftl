<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for browsing individuals in class groups for menupages -->

<section id="browse-by" role="region">
    <h2>Browse by</h2>
    
    <nav role="navigation">
        <ul id="browse-childClasses">
            <#list vClassGroup as vClass>
                <#------------------------------------------------------------
                Need to replace vClassCamel with full URL that allows function
                to degrade gracefully in absence of JavaScript. Something
                similar to what Brian had setup with widget-browse.ftl
                ------------------------------------------------------------->
                <#assign vClassCamel = vClass.name?capitalize?replace(" ", "")?uncap_first />
                <li id="${vClassCamel}"><a href="#${vClassCamel}" title="Browse all people in this class" data-uri="${vClass.URI}">${vClass.name} <span class="count-classes">(${vClass.entityCount})</span></a></li>
            </#list>
        </ul>
        <nav role="navigation">
            <ul id="alpha-browse-childClass">
                <li><a href="#" class="selected" data-alpha="all">All<span class="count-classes"> (280)</span></a></li>
                <li><a href="#" data-alpha="a">A<span class="count-classes"> (280)</span></a></li>
                <li><a href="#" data-alpha="b">B<span class="count-classes"> (280)</span></a></li>
                <li><a href="#" data-alpha="d">D<span class="count-classes"> (280)</span></a></li>
                <li><a href="#" data-alpha="f">F<span class="count-classes"> (280)</span></a></li>
                <li><a href="#" data-alpha="g">G<span class="count-classes"> (280)</span></a></li>
                <li><a href="#" data-alpha="h">H<span class="count-classes"> (280)</span></a></li>
                <li><a href="#" data-alpha="i">I<span class="count-classes"> (280)</span></a></li>
                <li><a href="#" data-alpha="k">K<span class="count-classes"> (280)</span></a></li>
                <li><a href="#" data-alpha="l">L<span class="count-classes"> (280)</span></a></li>
                <li><a href="#" data-alpha="n">N<span class="count-classes"> (280)</span></a></li>
                <li><a href="#" data-alpha="p">P<span class="count-classes"> (280)</span></a></li>
                <li><a href="#" data-alpha="r">R<span class="count-classes"> (280)</span></a></li>
                <li><a href="#" data-alpha="u">U<span class="count-classes"> (280)</span></a></li>
                <li><a href="#" data-alpha="v">V<span class="count-classes"> (280)</span></a></li>
                <li><a href="#" data-alpha="y">Y<span class="count-classes"> (280)</span></a></li>
                <li><a href="#" data-alpha="z">Z<span class="count-classes"> (280)</span></a></li>
            </ul>
        </nav>
    </nav>
    
    <section id="individuals-in-childClass" role="region">
        
    </section>
</section>