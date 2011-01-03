<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->
<section id="intro-menupage" role="region">
    <h3>People</h3>
    
    <section id="content-foaf-person" role="region">
        <h4>Visual Graph</h4>
        
        <#-- <@widget name="browse" classGroup="people"/> -->
        <nav role="navigation">
            <ul id="vgraph-childClasses">
            <#list vClassGroup as vClass>
                <li><a href="#browse-by" title="Browse all people in this class" data-uri="${vClass.URI}">${vClass.name} <span class="count-classes">(indivCount)</span></a></li>
            </#list>
            </ul>
        </nav>
          
        <section id="foaf-person-graph" role="region">
            <img src="${urls.images}/menupage/visual-graph.jpg" alt="" />
        </section>
    </section>
    
    <section id="find-by" role="region">
        <nav role="navigation">
            <h3>Find By</h3>
            
            <ul id="find-filters">
                <li><a href="#">Research Area</a></li>
                <li><a href="#">Authorship</a></li>
                <li><a href="#">Subject Area</a></li>
                <li><a href="#">Department</a></li>
                <li><a href="#">International Geographic Focus</a></li>
                <li><a href="#">Courses</a></li>
                <li><a href="#">Keywords</a></li>
            </ul>
        </nav>
    
    </section>
</section>

<section id="network-stats" role="region">
    <h3>Network stats</h3>
    
    <p>(n) Persons | (n) with authorship | (n) researchers | (n) are principal investigators | (n) with awards | (n) are teaching | (n) have positions in organization</p>
</section>

<section id="researchers" role="region">
    <h3>Researchers</h3>
    
    <p>A | B | C | D | E | F | G | H | I | J | K | L | M | N | O | P | Q | R | S | T | U | V | W | X | Y | Z | All</p>
    
    <section id="researchers-slider" role="region">
        <div id="alpha-display">A</div>
    
        <nav id="profile-photo-display" role = "navigation">
            <ul>
                <li><img src="${urls.images}/menupage/person-thumbnail.jpg" width="90" height="90" alt="foaf:lastName, foaf:firstName" /></li>
                <li><img src="${urls.images}/menupage/person-thumbnail.jpg" width="90" height="90" alt="foaf:lastName, foaf:firstName" /></li>
                <li><img src="${urls.images}/menupage/person-thumbnail.jpg" width="90" height="90" alt="foaf:lastName, foaf:firstName" /></li>
                <li><img src="${urls.images}/menupage/person-thumbnail.jpg" width="90" height="90" alt="foaf:lastName, foaf:firstName" /></li>
                <li><img src="${urls.images}/menupage/person-thumbnail.jpg" width="90" height="90" alt="foaf:lastName, foaf:firstName" /></li>
                <li><img src="${urls.images}/menupage/person-thumbnail.jpg" width="90" height="90" alt="foaf:lastName, foaf:firstName" /></li>
            </ul>
        </nav>
        
        <div id="nav-display">
            <p>All</p>
            
            <a href="#"><img src="${urls.images}/menupage/arrow-carousel-people.jpg" alt="" width="44" height="58" /></a> 
        </div>
    </section>
</section>

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
                <li id="${vClassCamel}"><a href="#${vClassCamel}" title="Browse all people in this class" data-uri="${vClass.URI}">${vClass.name} <span class="count-classes">(indivCount)</span></a></li>
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

${stylesheets.add("/css/menupage/menupage.css")}

<#----------------------------------------------------------------------------------
requestedPage is currently provided by FreemarkerHttpServlet. Should this be moved
to PageController? Maybe we should have Java provide the domain name directly
instead of the full URL of the requested page? Chintan was also asking for a
template variable with the domain name for an AJAX request with visualizations.
------------------------------------------------------------------------------------>
<#assign domainName = requestedPage?substring(0, requestedPage?index_of("/", 7)) />

<script type="text/javascript">
var menupageData = {
    baseUrl: '${domainName + urls.base}',
    dataServiceUrl: '${domainName + urls.base}/dataservice?getLuceneIndividualsByVClass=1&vclassId=',
    defaultBrowseVClassUri: '${vClassGroup[0].URI}'
};
</script>

${scripts.add("/js/menupage/browseByVClass.js")}