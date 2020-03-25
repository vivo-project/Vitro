<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#if origination?has_content && origination == "helpLink">
    <h2>Conseils pour la recherche</h2>
    <span id="searchHelp">
        <a href="#" onClick="history.back();return false;" title="back to results">Retour aux résultats</a>
    </span>
<#else>
    <h3>Astuces de recherche</h3>        
</#if>
<ul class="searchTips">
    <li>Gardez les choses simples ! Utilisez des termes courts et simples, à moins que vos recherches ne donnent trop de résultats.</li>
    <li>Utilisez les guillemets pour rechercher une phrase entière -- par exemple, "<i>écologie marine</i>.</li>
    <li>Sauf pour les opérateurs booléens, les recherches ne sont <strong>pas</strong> sensibles à la casse, donc "Genève" et "genève" sont équivalents</li>
    <li>Si vous n'êtes pas sûr de la bonne orthographe, mettez ~ à la fin de votre terme de recherche -- par exemple, <i>cabage~</i> donne <i>cabbage</i>, <i>steven~</i> donne <i>Stephen</i> et <i>Stefan</i> (ainsi que d'autres noms similaires).</li>
</ul>
    
<h4><a id="advTipsLink" href="#">Conseils avancés</a></h4>    
<ul id="advanced" class="searchTips" style="visibility:hidden">
    <li>Lorsque vous entrez plus d'un terme, la recherche donnera des résultats contenant tous ces termes à moins que vous n'ajoutiez le " OU " booléen -- par exemple, <i>poulet</i> OU <i>oeuf</i>.</li>
    <li>NOT" peut aider à limiter les recherches -- par exemple, <i>climat</i> NOT <i>change</i>.</li>
    <li>Les recherches de phrases peuvent être combinées avec des opérateurs booléens -- par exemple "<i>changemenst climatiques</i>". OU "<i>réchauffement global</i>".</li>
    <li>Des variations de mots proches seront également trouvées -- par exemple, <i>sequence</i> correspond à <i>sequences</i> et <i>sequencing</i>.</li>
    <li>Use the wildcard * character to match an even wider variation -- e.g., <i>nano*</i> will match both <i>nanotechnology</i> and <i>nanofabrication</i>.</li>
    <li>La recherche utilise des versions abrégées des mots -- par exemple, une recherche pour <i>cogniti*</i> ne trouve rien, tandis que <i>cognit*</i> trouve à la fois <i>cognitive</i> et <i>cognition</i>.</li> 
</ul>
<a id="closeLink" href="#"  style="visibility:hidden;font-size:.825em;padding-left:8px">Fermer</a>
${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/search.css" />')}
<script type="text/javascript">
    $(document).ready(function(){
        $('a#advTipsLink').click(function() {
           $('ul#advanced').css("visibility","visible"); 
           $('a#closeLink').css("visibility","visible");
           $('a#closeLink').click(function() {
              $('ul#advanced').css("visibility","hidden"); 
              $('a#closeLink').css("visibility","hidden");
           });

        });
    });
    
</script>