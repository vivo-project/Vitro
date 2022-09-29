<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#if origination?has_content && origination == "helpLink">
    <h2>Советы для поиска</h2>
    <span id="searchHelp">
        <a href="#" onClick="history.back();return false;" title="back to results">Back to results</a>
    </span>
<#else>
    <h3>Советы для поиска</h3>        
</#if>
<ul class="searchTips">
    <li>Будьте проще! Используйте короткие, односложные термины, если только поиск не дает слишком много результатов.</li>
    <li>Используйте кавычки для поиска целого словосочетания, например, "<i>свёртывание белка</i>".</li>
    <li>За исключением булевых операторов, поиск <strong>не</strong> чувствителен к регистру, поэтому запросы "Женева" и "женева" эквивалентны</li>
    <li>Если вы не уверены в правильности написания, добавьте знак ~ в конце поискового запроса, например запрос <i>морков~</i> отыщет <i>морковь/i>, а по запросу <i>степан~</i> будет найдено и <i>Степун</i>, и <i>Стефан</i> (а также и другие похожие имена).</li>
</ul>
    
<h4><a id="advTipsLink" href="#">Дополнительные советы</a></h4>    
<ul id="advanced" class="searchTips" style="visibility:hidden">
    <li>Если вы введете более одного термина, поиск вернет результаты, содержащие все из них, если только вы не добавите логическое "OR" - например, <i>курица</i> OR <i>яйцо</i>.</li>
    <li>"NOT" может помочь ограничить поиск, например <i>климат</i> NOT <i>изменение</i>.</li>
    <li>Поиск по словосочетанию может комбинироваться с булевыми операторами, например "<i>изменение климата</i>" OR "<i>глобальное потупление</i>".</li>
    <li>Также можно найти близкие варианты слов, например <i>очередь</i> подойдёт для поиска и <i>очереди</i>, и <i>очередной</i>.</li>
    <li>Используйте символ * для поиска еще более разнообразных вариантов, например <i>нано*</i> подойдёт как для <i>нанотехнологии</i>, так и для <i>нанопроизводства</i>.</li>
    <li>Поиск использует сокращенные версии слов, например, поиск по поиск по запросу <i>когнити*</i> не даст ничего, в то время, как запрос <i>когнит*</i> найдёт и <i>когнитивный</i> and <i>когнитариат</i>.</li> 
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
