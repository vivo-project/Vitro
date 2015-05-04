<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- 
        Used to display both the object and data property hierarchies, though there are
        separate controllers for those. Also used to display lists of "all" object and 
        data properties, though there are separate controllers for those, too.
 -->
 <#if propertyType??>
     <#assign propType = propertyType>
<#else>
    <#assign propType = "group">
</#if>


<section role="region">
  <h2>${pageTitle!}</h2>
  <#if message?has_content>
	<p>${message}</p>
  <#else>
  <#if !displayOption?has_content>
        <#assign displayOption = "listing">
  </#if>
  <form name="fauxListingForm" id="fauxListing" action="listFauxProperties" method="post" role="classHierarchy">
    <label id="displayOptionLabel" class="inline">${i18n().display_options}</label>
    <select id="displayOption" name="displayOption">
      <option value="listing" <#if displayOption == "listing">selected</#if> >faux properties alphabetically</option>
      <option value="byBase" <#if displayOption == "byBase">selected</#if> >faux properties by base property</option>
    </select>
  </form>
  <section id="container" style="margin-top:10px">
  <#if displayOption == "listing" >
    <#assign keys = fauxProps?keys/>
    <#list keys as key>
      <#assign ks = fauxProps[key] />
      <section id="classContainer1">
      <div>
        <a href='editForm?controller=FauxProperty&baseUri=${ks["baseURI"]?url!}&<#if ks["domainURI"]?has_content>domainUri=${ks["domainURI"]?url}&</#if>rangeUri=${ks["rangeURI"]?url!}'>${key?substring(0,key?index_of("@@"))}</a>
      </div>
      <table id="classHierarchy1" class="classHierarchy">
        <tbody>
          <tr>
            <td class="classDetail">${i18n().base_property_capitalized}:</td>
	        <td><a href='propertyEdit?uri=${ks["baseURI"]?url!}'>${ks["base"]!}</a></td>
	      </tr>
	      <tr>
            <td class="classDetail">${i18n().group_capitalized}:</td>
	        <td>${ks["group"]?capitalize}</td>
	      </tr>
	      <tr>
            <td class="classDetail">${i18n().domain_class}:</td>
            <td>
              ${ks["domain"]!}
              <span class="rangeClass">${i18n().range_class}:</span>
		      ${ks["range"]!}
            </td>
          </tr>
        </tbody>
      </table>
      </section>	
    </#list>
  <#else>
	<#assign keys = fauxProps?keys />
	<#list keys as key>
      <#assign fauxList = fauxProps[key] />
      <section id="classContainer1">
		<#assign baseLabel = key?substring(0,key?index_of("|")) />
		<#assign baseUri = key?substring(key?index_of("|")+1) />
        <div>
          <a href='propertyEdit?uri=${baseUri?url}'>${baseLabel}</a>
        </div>
	    <#assign keysTwo = fauxList?keys />
		<#assign firstLoop = true />
	    <#list keysTwo as k2>
	      <#assign faux = fauxList[k2] />
        <table id="classHierarchy1" class="classHierarchy" <#if !firstLoop >style="margin-top:-16px"</#if>>
          <tbody>
              <tr>
                <td class="classDetail">${i18n().faux_property_capitalized}:</td>
	            <td><a href='editForm?controller=FauxProperty&baseUri=${faux["baseURI"]?url!}&<#if faux["domainURI"]?has_content>domainUri=${faux["domainURI"]?url}&</#if>rangeUri=${faux["rangeURI"]?url!}'>${k2?substring(0,k2?index_of("@@"))}</a></td>
	          </tr>
	          <tr>
                <td class="classDetail">${i18n().group_capitalized}:</td>
	            <td>${faux["group"]?capitalize}
   	            </td>
	          </tr>
	          <tr>
                <td class="classDetail">${i18n().domain_class}:</td>
                <td>
                  ${faux["domain"]!}
                  <span class="rangeClass">${i18n().range_class}:</span>
		          ${faux["range"]!}
                </td>
              </tr>
		    </tbody>
		  </table>
		  <#assign firstLoop = false />
      </#list>
      </section>	
	</#list>
  </#if> 
  </section>
  </#if>
</section>

<script language="javascript" type="text/javascript" >
    var i18nStrings = {
        '',
    };
</script>


<script language="javascript" type="text/javascript" >
$(document).ready(function() {
    fauxPropertiesListingUtils.onLoad();
});    
</script>


${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/classHierarchy.css" />')}

${scripts.add('<script type="text/javascript" src="${urls.base}/js/jquery-ui/js/jquery-ui-1.8.9.custom.min.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/siteAdmin/fauxPropertiesListingUtils.js"></script>')}
              
