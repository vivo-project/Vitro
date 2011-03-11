<#-- dump.ftl
  --
  -- Generates tree representations of data model items.
  --
  -- Usage:
  -- <#import "dump.ftl" as dumper>
  --
  -- <#assign foo = something.in["your"].data[0].model />
  --
  -- <@dumper.dump foo />
  --
  -- When used within html pages you've to use <pre>-tags to get the wanted
  -- result:
  -- <pre>
  -- <@dumper.dump foo />
  -- <pre>
  -->

<#-- The black_list contains bad hash keys. Any hash key which matches a 
  -- black_list entry is prevented from being displayed.
  -->
<#assign black_list = ["class"] />


<#-- 
  -- The main macro.
  -->
  
<#macro dump data>
(root)
<#if data?is_enumerable>
<@printList data,[] />
<#elseif data?is_hash_ex>
<@printHashEx data,[] />
</#if>
</#macro>

<#-- private helper macros. it's not recommended to use these macros from 
  -- outside the macro library.
  -->

<#macro printList list has_next_array>
<#local counter=0 />
<#list list as item>
<#list has_next_array+[true] as has_next><#if !has_next>    <#else>  | </#if></#list>
<#list has_next_array as has_next><#if !has_next>    <#else>  | </#if></#list><#t>
<#t><@printItem item?if_exists,has_next_array+[item_has_next], counter />
<#local counter = counter + 1/>
</#list>
</#macro>

<#macro printHashEx hash has_next_array>
<#list hash?keys as key>
<#list has_next_array+[true] as has_next><#if !has_next>    <#else>  | </#if></#list>
<#list has_next_array as has_next><#if !has_next>    <#else>  | </#if></#list><#t>
<#t><@printItem hash[key]?if_exists,has_next_array+[key_has_next], key />
</#list>
</#macro>

<#macro printItem item has_next_array key>
<#if item?is_method>
  +- ${key} = ?? (method)
<#elseif item?is_enumerable>
  +- ${key}
  <@printList item, has_next_array /><#t>
<#elseif item?is_hash_ex && omit(key?string)><#-- omit bean-wrapped java.lang.Class objects -->
  +- ${key} (omitted)
<#elseif item?is_hash_ex>
  +- ${key}
  <@printHashEx item, has_next_array /><#t>
<#elseif item?is_number>
  +- ${key} = ${item}
<#elseif item?is_string>
  +- ${key} = "${item}"
<#elseif item?is_boolean>
  +- ${key} = ${item?string}
<#elseif item?is_date>
  +- ${key} = ${item?string("yyyy-MM-dd HH:mm:ss zzzz")}
<#elseif item?is_transform>
  +- ${key} = ?? (transform)
<#elseif item?is_macro>
  +- ${key} = ?? (macro)
<#elseif item?is_hash>
  +- ${key} = ?? (hash)
<#elseif item?is_node>
  +- ${key} = ?? (node)
</#if>
</#macro>

<#function omit key>
    <#local what = key?lower_case>
    <#list black_list as item>
        <#if what?index_of(item) gte 0>
            <#return true>
        </#if>
    </#list>
    <#return false>
</#function>