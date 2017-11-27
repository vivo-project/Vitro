<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#if origination?has_content && origination == "helpLink">
    <h2>Search Tips</h2>
    <span id="searchHelp">
        <a href="#" onClick="history.back();return false;" title="back to results">Voltar aos resultados</a>
    </span>
<#else>
    <h3>Dicas de busca</h3>        
</#if>
<ul class="searchTips">
    <li>Mantenha simples! Use, termos curtas, a menos que suas busca estaja retornando muitos resultados.</li>
    <li>Use aspas para procurar uma frase inteira - por exemplo, "<i>protein folding</i>".</li>
    <li>Exceto para as operadores booleanas, pesquisas <strong>not</strong> são maiúsculas e minúsculas, por isso "Genebra" e "genebra" são equivalentes</li>
    <li>Se você não tiver certeza da grafia correta, colocar ~ no final do seu termo de busca - por exemplo,<i>cabage~</i> encontra <i>cabbage</i>, <i>steven~</i> encontra <i>Stephen</i> e <i>Stefan</i> (bem como outros nomes similares).</li>
</ul>
    
<h4><a id="advTipsLink" href="#">Dicas Avançadas</a></h4>    
<ul id="advanced" class="searchTips" style="visibility:hidden">
    <li>Quando você insere mais de um termo, a busca irá retornar resultados contendo todos eles a menos que você adicione o booleano "OR" - por exemplo, <i>chicken</i> OR <i>egg</i>.</li>
    <li>NOT" pode ajudar em limitar as pesquisas - por exemplo,<i>climate</i> NOT <i>change</i>.</li>
    <li>Pesquisas de frases podem ser combinadas com operadores Booleanos - por exemplo, "<i>climate change</i>" OR "<i>global warming</i>".</li>
    <li> palavras de variações proximas também serão encontradas - por exemplo, <i>sequence</i> matches <i>sequences</i> and <i>sequencing</i>.</li>
    <li>Use o caracterer curinga * para corresponder a uma variação ainda maior - por exemplo,<i>nano*</i> irá corresponder tanto <i>nanotechnology</i> como <i>nanofabrication</i>.</li>
    <li>Pesquisar usando versões abreviadas de palavras - por exemplo, uma pesquisa por <i>cogniti*</i> não encontra nada, enquanto <i>cognit*</i> encontra ambas <i>cognitive</i> e <i>cognition</i>.</li> 
</ul>
<a id="closeLink" href="#"  style="visibility:hidden;font-size:.825em;padding-left:8px">Close</a>
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