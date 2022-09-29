<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#if origination?has_content && origination == "helpLink">
    <h2>Saveti za Pretragu</h2>
    <span id="searchHelp">
        <a href="#" onClick="history.back();return false;" title="back to results">Nazad na rezultate</a>
    </span>
<#else>
    <h3>Saveti za pretragu</h3>        
</#if>
<ul class="searchTips">
    <li>Pretraga treba da bude jednostavna! Koristite kratke, pojedinačne izraze osim ako Vaša pretraga vraća previše rezultata.</li>
    <li>Koristite navodnike kako bi ste tražili čitave fraze -- npr. "<i>savijanje proteina</i>".</li>
    <li>Izuzev boolean operatora, pretrage <strong>NE RAZLIKUJU</strong> mala i velika slova, te su izrazi "Ženeva" i "ženeva" ekvivalentni</li>
    <li>Ako niste sigurni kako pravilno da napišete reč, stavite ~ na kraj izraza po kom radite pretragu -- npr. <i>cabage~</i> će pronaći <i>cabbage</i>, <i>steven~</i> će pronaći <i>Stephen</i> i <i>Stefan</i> (i ostala slična imena).</li>
</ul>
    
<h4><a id="advTipsLink" href="#">Saveti za naprednu pretragu</a></h4>    
<ul id="advanced" class="searchTips" style="visibility:hidden">
    <li>Kada unesete više od jednog izraza, pretraga će vratiti samo rezultate koji sadrže svaki oid njih osim ako ne iskoristite boolean operator "OR" -- npr. <i>piletina</i> OR <i>jaja</i>.</li>
    <li>NOT" vam može pomoći u ograničenju pretrage -- npr. <i>klimatske</i> NOT <i>promene</i>.</li>
    <li>Možete kombinovati boolean operatore i pretragu po frazama -- npr. "<i>klimatske promene</i>" OR "<i>globalno zagrejavanje</i>".</li>
    <li>Pretraga takođe vraća rezultate koji sadrže reči slične izrazu pretrage -- npr., <i>sekvenca</i> će se podudarati sa <i>sekvencama</i>.</li>
    <li>Koristite Džoker karakter * da bi ste pronašli još više varijacija izraza po kom pretražujete -- npr. <i>nano*</i> će vratiti i izraz <i>nanotehnologija</i> i izraz <i>nanofabrikacija</i>.</li>
    <li>Pretraga koristi osnove izraza koje unesete -- npr. pretraga po izrazu <i>cogniti*</i> neće vratiti ništa, dok pretraga po izrazu <i>cognit*</i> vraća i <i>cognitive</i> i <i>cognition</i>.</li> 
</ul>
<a id="closeLink" href="#"  style="visibility:hidden;font-size:.825em;padding-left:8px">Sakrijte</a>
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
