<#-- $Este archivo esta distribuido sobre los terminos de la licencia en /doc/license.txt$ -->

<#if origination?has_content && origination == "helpLink">
    <h2>Consejos de Búsqueda</h2>
    <span id="searchHelp">
        <a href="#" onClick="history.back();return false;" title="regresar a los resultados">regresar a los resultados</a>
    </span>
<#else>
    <h3>Consejos de Búsqueda</h3>        
</#if>
<ul class="searchTips">
    <li>Mantenlo simple! Use términos cortos, a menos que sus búsquedas devuelvan demasiados resultados.</li>
    <li>Utilice comillas para buscar una frase entera -- por ejemplo, "<i>el plegamiento de proteínas</i>".</li>
    <li>A excepción de los operadores booleanos, las búsquedas <strong>no distinguen</strong> entre mayúsculas y minúsculas, por lo que "Ginebra" y "ginebra" son equivalentes</li>
    <li>Si no está seguro de la ortografía correcta, ponga ~ al final de su término de búsqueda -- por ejemplo, <i>cabage~</i> encuentra <i>cabbage</i>, <i>steven~</i> encuentra <i>Stephen</i> y <i>Stefan</i> (así como otros nombres similares).</li>
</ul>
    
<h4><a id="advTipsLink" href="#">Consejos Avanzados</a></h4>    
<ul id="advanced" class="searchTips" style="visibility:hidden">
    <li>Cuando se introduce más de un término, la búsqueda devolverá resultados que contengan todas ellas a menos que agregue el operador booleano "OR" -- por ejemplo, <i>pollo</i> OR <i>huevos</i>.</li>
    <li>"NOT" puede ayudar a limitar búsquedas -- por ejemplo, <i>clima</i> NOT <i>cambia</i>.</li>
    <li>Las búsquedas de frases se pueden combinar con operadores booleanos -- por ejemplo, "<i>cambio climático</i>" OR "<i>calentamiento global</i>".</li>
    <li>Asimismo, se encuentra variaciones de palabras -- por ejemplo, <i>secuencia</i> similar <i>secuencias</i> y <i>secuenciación</i>.</li>
    <li>Utilice el carácter comodín * para que coincida con una variación aún mayor -- por ejemplo, <i>nano*</i> encuentra también <i>nanotecnología</i> y <i>nanofabricación</i>.</li>
    <li>Una búsqueda utiliza versiones acortadas de palabras -- por ejemplo, una búsqueda de <i>cogniti*</i> no encuentra nada, mientras que "cogni*" encuentra tanto <i>cognitivo</i> y <i>cognición</i>.</li> 
</ul>
<a id="closeLink" href="#"  style="visibility:hidden;font-size:.825em;padding-left:8px">Cerrar</a>
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