<?xml version='1.0' encoding="UTF-8"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:ai="http://www.digitalmeasures.com/schema/data"
	xmlns:aiic="http://vivoweb.org/ontology/activity-insight"
	xmlns="http://vivoweb.org/ontology/activity-insight"
	xmlns:dm="http://www.digitalmeasures.com/schema/data"
	xmlns:dmu="http://www.digitalmeasures.com/schema/user-metadata"
xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:xs='http://www.w3.org/2001/XMLSchema'
	xmlns:vfx='http://vivoweb.org/ext/functions'	
	exclude-result-prefixes='vfx xs xlink'
>


<xsl:output method='xml' indent='yes'/>
<xsl:strip-space elements="*"/>
<xsl:variable name='NL'>
<xsl:text>
</xsl:text>
</xsl:variable>

<!-- ================================================= -->
<xsl:template match='/Users'>

<xsl:element name='{local-name()}'>
<xsl:apply-templates select='User'/>
</xsl:element>


</xsl:template>

<xsl:template match='User'>
<xsl:element name='{local-name()}'>
<xsl:attribute name='netid' select='@username'/>
<xsl:attribute name='aiid' select='@dmu:userId'/>
<xsl:apply-templates select='FirstName|MiddleName|LastName|Email'/>
</xsl:element>
</xsl:template>

<xsl:template match='*'>
<xsl:element name='{local-name()}'>
<xsl:apply-templates/>
</xsl:element>

</xsl:template>

<xsl:function name='vfx:trim' as='xs:string'>
<xsl:param name='s1'/>
<xsl:choose>
<xsl:when test='$s1 != ""'>
<xsl:analyze-string select='$s1' regex='^\s*(.+?)\s*$'>
<xsl:matching-substring>
<xsl:value-of select='regex-group(1)'/>
</xsl:matching-substring>
</xsl:analyze-string>
</xsl:when>
<xsl:otherwise>
<xsl:text>Unknown</xsl:text>
</xsl:otherwise>
</xsl:choose>
</xsl:function>

<xsl:function name='vfx:clean' as='xs:string'>
<xsl:param name='s1'/>
<xsl:variable name='res' select='replace($s1, "\s", "")'/>
<xsl:value-of select='upper-case(replace($res,"[.,]",""))'/>
</xsl:function>

</xsl:stylesheet>
