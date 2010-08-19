<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:dm="http://www.digitalmeasures.com/schema/data"
xmlns:dmd="http://www.digitalmeasures.com/schema/data-metadata"
xmlns="http://www.digitalmeasures.com/schema/data"
xmlns:ai="http://www.digitalmeasures.com/schema/data"
xmlns:xs='http://www.w3.org/2001/XMLSchema'
xmlns:vfx='http://vivoweb.org/ext/functions'	
exclude-result-prefixes='vfx xs'
 version="2.0">   

<xsl:output method="xml" indent="yes"/> 

<xsl:function name='vfx:isomorphic' as='xs:boolean'>
<xsl:param name='s1'/>
<xsl:param name='s2'/>
<xsl:variable name='cs1' select='vfx:collapse($s1)'/>
<xsl:variable name='cs2' select='vfx:collapse($s2)'/>
<xsl:value-of select='$cs1 = $cs2'/>
</xsl:function>

<!-- ============================================= -->
<!-- this extension function  
	1. removes whitespace and special chrs from the argument string
	2. shifts alphabetic characters to upper case
	3. returns the adjusted string for comparison
-->
<xsl:function name='vfx:collapse' as='xs:string'>
<xsl:param name='s1'/>
<xsl:variable name='res' select='replace($s1, "\s", "")'/>
<xsl:variable name='res1' select="replace($res,'''','')"/>
<xsl:value-of select='upper-case(replace($res1,"[\-.,;""]",""))'/>

</xsl:function>

<!-- ============================================= -->
<!-- this extension function 
removes leading and trailing whitespace from the argument string
-->
<xsl:function name='vfx:trim' as='xs:string'>
<xsl:param name='s1'/>
<xsl:choose>
<xsl:when test='$s1 != ""'>
<xsl:value-of select='normalize-space($s1)'/>
</xsl:when>
<xsl:otherwise>
<xsl:text>Unspecified</xsl:text>
</xsl:otherwise>
</xsl:choose>
</xsl:function>

<xsl:template name='hasMatch'>
<xsl:param name='n'/>
<xsl:param name='nlist'/>
<xsl:param name='res' select='false()'/>
<xsl:choose>
<xsl:when test='$nlist and not($res)'>
<xsl:variable name='comp' select='vfx:collapse($n) = vfx:collapse($nlist[1])'/>
<!-- xsl:variable name='comp' select='$n = $nlist[1]'/ -->
<xsl:call-template name='hasMatch'>
<xsl:with-param name='n' select='$n'/>
<xsl:with-param name='nlist' select='$nlist[position()>1]'/>
<xsl:with-param name='res' select='$res or $comp'/>
</xsl:call-template>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select='$res'/>
</xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:function name='vfx:hasMatch' as='xs:boolean'>
<xsl:param name='n'/>
<xsl:param name='nlist'/>
<xsl:call-template name='hasMatch'>
<xsl:with-param name='n' select='$n'/>
<xsl:with-param name='nlist' select='$nlist'/>
</xsl:call-template>
</xsl:function>

<xsl:template name='hasMatchingName'>
<xsl:param name='fn'/>
<xsl:param name='mn'/>
<xsl:param name='ln'/>
<xsl:param name='nlist'/>
<xsl:param name='res' select='false()'/>
<xsl:choose>
<xsl:when test='$nlist and not($res)'>
<xsl:variable name='fnmnln' select='concat($fn,"|",$mn ,"|",$ln)'/>
<xsl:variable name='listfnmnln' select='concat($nlist[1]/dm:FNAME,"|",$nlist[1]/dm:MNAME, "|",$nlist[1]/dm:LNAME)'/>
<xsl:variable name='comp' select='vfx:collapse($fnmnln) = vfx:collapse($listfnmnln)'/>

<xsl:call-template name='hasMatchingName'>
<xsl:with-param name='fn' select='$fn'/>
<xsl:with-param name='mn' select='$mn'/>
<xsl:with-param name='ln' select='$ln'/>
<xsl:with-param name='nlist' select='$nlist[position()>1]'/>
<xsl:with-param name='res' select='$res or $comp'/>
</xsl:call-template>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select='$res'/>
</xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:function name='vfx:hasMatchingName' as='xs:boolean'>
<xsl:param name='fn'/>
<xsl:param name='mn'/>
<xsl:param name='ln'/>
<xsl:param name='nlist'/>
<xsl:call-template name='hasMatchingName'>
<xsl:with-param name='fn' select='$fn'/>
<xsl:with-param name='mn' select='$mn'/>
<xsl:with-param name='ln' select='$ln'/>
<xsl:with-param name='nlist' select='$nlist'/>
</xsl:call-template>
</xsl:function>

<xsl:function name='vfx:expandClass' as='xs:string'>
<xsl:param name='s'/>
<xsl:variable name='before' select='substring-before($s,":")'/>
<xsl:variable name='after' select='substring-after($s,":")'/>
<xsl:choose>
<xsl:when test='$before = "bibo"'>
<xsl:value-of select='concat("http://purl.org/ontology/bibo/",$after)'/>
</xsl:when>
<xsl:when test='$before = "core"'>
<xsl:value-of select='concat("http://vivoweb.org/ontology/core#",$after)'/>
</xsl:when>
<xsl:when test='$before = "ai"'>
<xsl:value-of select='concat("http://vivoweb.org/activity-insight#",$after)'/>
</xsl:when>
<xsl:when test='true()'>
<xsl:value-of select='concat("http://vivoweb.org/activity-insight#",$after)'/>
</xsl:when>
</xsl:choose>

</xsl:function>

<xsl:function name='vfx:knownUri'>
<xsl:param name='fn'/>
<xsl:param name='mn'/>
<xsl:param name='ln'/>
<xsl:param name='ep'/>
<xsl:choose>
<xsl:when test='$ep'>
<xsl:variable name='results' select='$ep/person[fname=$fn and mname = $mn and lname = $ln]/uri'/>
	<xsl:choose>
	<xsl:when test='$results[1]'>
	<xsl:value-of select='$results[1]'/>
	</xsl:when>
	<xsl:otherwise>
	<xsl:value-of select='""'/>
	</xsl:otherwise>
	</xsl:choose>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select='""'/>
</xsl:otherwise>
</xsl:choose>
</xsl:function>

<xsl:function name='vfx:knownCEOrgUri'>
<xsl:param name='n'/>
<xsl:param name='sc'/>
<xsl:param name='ep'/>
<xsl:choose>
<xsl:when test='$ep'>
<xsl:value-of select='$ep/person[name=$n and stateCountry = $sc]/uri'/>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select='""'/>
</xsl:otherwise>
</xsl:choose>
</xsl:function>

<xsl:function name='vfx:knownOrgUri'>
<xsl:param name='n'/>
<xsl:param name='ep'/>
<xsl:choose>
<xsl:when test='$ep'>
<xsl:value-of select='$ep/org[name=$n]/uri'/>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select='""'/>
</xsl:otherwise>
</xsl:choose>
</xsl:function>

<xsl:function name='vfx:knownGeoUri'>
<xsl:param name='n'/>
<xsl:param name='ep'/>
<xsl:choose>
<xsl:when test='$ep'>
<xsl:value-of select='$ep/geo[title=$n]/uri'/>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select='""'/>
</xsl:otherwise>
</xsl:choose>
</xsl:function>

<xsl:function name='vfx:knownEmphUri'>
<xsl:param name='n'/>
<xsl:param name='ep'/>
<xsl:choose>
<xsl:when test='$ep'>
<xsl:value-of select='$ep/emphasis[title=$n]/uri'/>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select='""'/>
</xsl:otherwise>
</xsl:choose>
</xsl:function>

<xsl:function name='vfx:knownUaUri'>
<xsl:param name='n'/>
<xsl:param name='ep'/>
<xsl:choose>
<xsl:when test='$ep'>
<xsl:value-of select='$ep/area[name=$n]/uri'/>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select='""'/>
</xsl:otherwise>
</xsl:choose>
</xsl:function>

<xsl:function name='vfx:knownCaUri'>
<xsl:param name='n'/>
<xsl:param name='ep'/>
<xsl:choose>
<xsl:when test='$ep'>
<xsl:value-of select='$ep/conarea[name=$n]/uri'/>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select='""'/>
</xsl:otherwise>
</xsl:choose>
</xsl:function>

</xsl:stylesheet>
