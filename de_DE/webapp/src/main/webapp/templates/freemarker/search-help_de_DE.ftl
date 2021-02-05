<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#if origination?has_content && origination == "helpLink">
    <h2>Search Tips</h2>
    <span id="searchHelp">
        <a href="#" onClick="history.back();return false;" title="back to results">Zurück zu den Suchergebnissen</a>
    </span>
<#else>
    <h3>Suchtipps</h3>        
</#if>
<ul class="searchTips">
    <li>Halten Sie es einfach! Verwenden Sie kurze, einzelne Begriffe, wenn ihre Suche zu viele Ergebnisse liefert.</li>
    <li>Verwenden Sie Anführungszeichen für die Suche nach eine Phrase - z.B. "<i>protein folding</i>".</li>
    <li>Mit Ausnahme Boolescher Operatoren, sind Suchen <strong>nicht</strong> case-sensitive, also sind "Genf" und "genf" identisch</li>
    <li>Wenn Sie sich unsicher über die korrekte Rechtschreibung sind, nutzen Sie ~ am Ende Ihres Suchbegriffes -- Bsp.: <i>cabage~</i> findet <i>cabbage</i>, <i>steven~</i> findet <i>Stephen</i> und <i>Stefan</i> (und andere, ähnliche Namen).</li>
</ul>
    
<h4><a id="advTipsLink" href="#">Tipps für Fortgeschrittene</a></h4>    
<ul id="advanced" class="searchTips" style="visibility:hidden">
    <li>Wenn Sie mehr als einen Begriff angeben, wird die Suche nur Ergebnisse liefern, die alle Begriffe enthalten, es sei denn Sie ergänzen "OR" - z.B. <i>Huhn</i> OR <i>Ei</i>.</li>
    <li>"NOT" kann zur Einschränkung der Suche verwendet werden, - z.B. <i>Klima</i> NOT <i>Veränderung</i>.</li>
    <li>Wortgruppensuchen können mit Booleschen Operatoren kombiniert werden - z.B. "<i>Klimaveränderung</i>" OR "<i>Globale Erwärmung</i>".</li>
    <li>Wortvariationen werden auch gefunden - Bsp.: <i>Sequenz</i> findet <i>Sequenzen</i> and <i>Sequenzierung</i>.</li>
    <li>Verwenden Sie das Wildcard-Zeichen * um mehr Variationen zu finden - Bsp.: <i>nano*</i> findet <i>Nanotechnology</i> sowie <i>Nanofabrikation</i>.</li>
</ul>
<a id="closeLink" href="#"  style="visibility:hidden;font-size:.825em;padding-left:8px">Schließen</a>
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
