<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:sp="http://www.w3.org/2005/sparql-results#"
xmlns:xs='http://www.w3.org/2001/XMLSchema'
xmlns:vfx='http://vivoweb.org/ext/functions'
exclude-result-prefixes='vfx xs'
>


<xsl:output method='xml' indent='yes'/>
<xsl:strip-space elements="*"/>
<xsl:template match='//sp:results'>

<xsl:element name='ExtantPersons' inherit-namespaces='no'>
<xsl:for-each select='sp:result'>
<xsl:variable name='parse'>
<xsl:choose>
<xsl:when test='not(sp:binding[@name="firstName"]) 
                  or not(sp:binding[@name="lastName"])'>
<xsl:text>Yes</xsl:text>
</xsl:when>
<xsl:otherwise>
<xsl:text>No</xsl:text>
</xsl:otherwise>
</xsl:choose>
</xsl:variable>


<xsl:variable name='emailPresent'>
<xsl:choose>
<xsl:when test='sp:binding[@name="plainEmail"]'>
<xsl:text>Yes</xsl:text>
</xsl:when>
<xsl:otherwise>
<xsl:text>No</xsl:text>
</xsl:otherwise>
</xsl:choose>
</xsl:variable>

<xsl:element name='person' inherit-namespaces='no'>
<xsl:for-each select='./sp:binding'>


<xsl:call-template name='parts'>
<xsl:with-param name='parse' select='$parse'/>
<xsl:with-param name='emailPresent' select='$emailPresent'/>
 
</xsl:call-template>

</xsl:for-each>
</xsl:element>
</xsl:for-each>
</xsl:element>

</xsl:template>

<xsl:template name='parts'>
<xsl:param name='parse'/>
<xsl:param name='emailPresent'/>

<xsl:choose>
<!-- handle uriPerson -->
<xsl:when test='./@name ="uriPerson"'>
<xsl:element name='uri' inherit-namespaces='no'>
<xsl:value-of select='normalize-space(./sp:uri)'/>
</xsl:element>
</xsl:when><!-- end of handle uriPerson -->

<!-- handle first name -->
<xsl:when test='./@name ="firstName" and $parse = "No"'>
<xsl:element name='fname' inherit-namespaces='no'>
<xsl:value-of select='normalize-space(./sp:literal)'/>
</xsl:element>
</xsl:when><!-- end of handle first name -->

<!-- handle last name -->
<xsl:when test='./@name ="lastName" and $parse = "No"'>
<xsl:element name='lname' inherit-namespaces='no'>
<xsl:variable name='last' select='normalize-space(./sp:literal)'/>

<!-- if necessary, get rid of Jr, Sr, II and III in last name -->
<xsl:choose>

<xsl:when test='ends-with($last," Jr") or 
ends-with($last," Jr.") or 
ends-with($last," Sr") or 
ends-with($last," Sr.") or 
ends-with($last," II") or 
ends-with($last," III")'>
<xsl:value-of select='normalize-space(substring-before($last," "))'/>
</xsl:when>

<xsl:otherwise>
<xsl:value-of select='$last'/>
</xsl:otherwise>

</xsl:choose>
</xsl:element>
</xsl:when><!-- end of handle last name -->


<!-- handle stringPerson -->
<xsl:when test='./@name ="stringPerson" and $parse = "No"'>
<!-- 
    first and last names are available so just try to get middle name 
-->
<xsl:element name='mname' inherit-namespaces='no'>
<xsl:value-of select='vfx:nameParts(./sp:literal,"M")'/>
</xsl:element>
</xsl:when>

<!-- have to parse for every thing -->
<xsl:when test='./@name ="stringPerson" and $parse = "Yes"'>

<!-- Just handle bone-head special cases literally -->
<xsl:choose>

<xsl:when test='./sp:literal = "Gisela Podleski" '>
<xsl:element name='fname' inherit-namespaces='no'>Gisela</xsl:element>
<xsl:element name='lname' inherit-namespaces='no'>Podleski</xsl:element>
</xsl:when>

<xsl:otherwise>
<xsl:element name='fname' inherit-namespaces='no'>
<xsl:value-of select='vfx:nameParts(./sp:literal,"F")'/>
</xsl:element>
<xsl:element name='lname' inherit-namespaces='no'>
<xsl:value-of select='vfx:nameParts(./sp:literal,"L")'/>
</xsl:element>
<xsl:element name='mname' inherit-namespaces='no'>
<xsl:value-of select='vfx:nameParts(./sp:literal,"M")'/>
</xsl:element>
<xsl:if test=' $emailPresent ="No"'>
<xsl:element name='cornell' inherit-namespaces='no'>Unknown</xsl:element>
</xsl:if>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<!-- end of handle stringPerson -->

<!-- handle  plainEmail -->
<xsl:when test='./@name ="plainEmail"'>
<xsl:element name='cornell' inherit-namespaces='no'>

<xsl:value-of select='if ( substring-after(./sp:literal,"@") = "cornell.edu" ) 
then substring-before(./sp:literal,"@") 
else "Unknown" '/>

</xsl:element>
</xsl:when>
<!-- end of handle  plainEmail -->

<xsl:when test=' $emailPresent ="No"'>
<xsl:element name='cornell' inherit-namespaces='no'>Unknown</xsl:element>
</xsl:when>


<xsl:otherwise>
</xsl:otherwise>
</xsl:choose>
</xsl:template>



<xsl:function name="vfx:nameParts">
<xsl:param name='sp'/>
<xsl:param name='part'/>

<xsl:choose><!-- ============== OUTER ============== -->
<xsl:when test='contains($sp, ",")'>

<xsl:variable name='last' select='normalize-space(substring-before($sp,","))'/>
<xsl:variable name='rest' select='normalize-space(substring-after($sp,","))'/>

<xsl:variable name='first' select='if(contains($rest," ")) 
then substring-before(normalize-space($rest)," ") 
else $rest'/>

<xsl:variable name='mid' select='substring-after($rest," ")'/>


<xsl:choose><!-- ============== CASES ============== -->

<xsl:when test='$part="F"'><!-- ============== CASE F ============== -->
<xsl:value-of select='$first'/>
</xsl:when>

<xsl:when test='$part="L"'><!-- ============== CASE L ============== -->
<!-- ============== -->
<xsl:choose>
<xsl:when test='ends-with($last," Jr") or 
ends-with($last," Jr.") or 
ends-with($last," Sr") or 
ends-with($last," Sr.") or 
ends-with($last," II") or 
ends-with($last," III")'>
<xsl:value-of select='normalize-space(substring-before($last," "))'/>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select='$last'/>
</xsl:otherwise>
</xsl:choose>
<!-- ============== -->
</xsl:when><!-- ============== CASE L ============== -->

<xsl:when test='$part="M"'><!-- ============== CASE M ============== -->

<xsl:choose>
<xsl:when test='ends-with($mid,", Jr") or 
ends-with($mid,", Jr.") or 
ends-with($mid,", Sr") or 
ends-with($mid,", Sr.") or
ends-with($mid,", II") or
ends-with($mid,", III") '>
<xsl:value-of select='normalize-space(substring-before($mid,","))'/>
</xsl:when>
<xsl:when test='$mid = "Jr." or $mid = "III" or $mid = "II"'>
<xsl:value-of select='""'/>
</xsl:when>
<xsl:when test='ends-with($mid," Jr") or ends-with($mid," Jr.") '>
<xsl:value-of select='normalize-space(substring-before($mid," "))'/>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select='$mid'/>
</xsl:otherwise>
</xsl:choose>

</xsl:when><!-- ============== CASE M ============== -->
<xsl:otherwise><!-- ============== CASE  ============== -->
</xsl:otherwise>
</xsl:choose><!-- ============== CASES ============== -->

</xsl:when><!-- ============== OUTER ============== -->

<xsl:otherwise><!-- ============== OUTER ============== -->

</xsl:otherwise><!-- ============== OUTER ============== -->

</xsl:choose><!-- ============== OUTER ============== -->
</xsl:function>

</xsl:stylesheet>

