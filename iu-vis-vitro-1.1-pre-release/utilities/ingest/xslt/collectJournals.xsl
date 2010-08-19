<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:ai="http://www.digitalmeasures.com/schema/data"
	xmlns:aiic="http://vivoweb.org/activity-insight"
	xmlns="http://vivoweb.org/activity-insight"
	xmlns:dm="http://www.digitalmeasures.com/schema/data"
	xmlns:xs='http://www.w3.org/2001/XMLSchema'
	xmlns:vfx='http://vivoweb.org/ext/functions'	
	exclude-result-prefixes='vfx xs'	
>

<xsl:param name='listxml' required='yes'/>
<xsl:output method='xml' indent='yes'/>
<xsl:strip-space elements="*"/>
<xsl:variable name='NL'>
<xsl:text>
</xsl:text>
</xsl:variable>
<xsl:template match='/'>


<xsl:variable name='docs' as='node()*'
	select='collection($listxml)'/>

<xsl:element name="aiic:JOURNAL_LIST" namespace="http://vivoweb.org/activity-insight">

<xsl:for-each-group select='$docs/dm:Data/ai:JOURNALS/ai:JOURNAL' 
	group-by='vfx:clean(ai:INTELLCONT_JOURNAL_NAME)'>
<xsl:sort select='vfx:clean(ai:INTELLCONT_JOURNAL_NAME)'/>


<xsl:element name="aiic:ARTICLES_BY_JOURNAL" >

<!-- xsl:for-each select='ai:INTELLCONT_JOURNAL_NAME' -->

<xsl:element name='aiic:JOURNAL_NAME'>
<xsl:attribute name='numrefs' select='count(current-group())'/>
<!--
<xsl:value-of select='count(current-group())'/>
-->

<xsl:value-of select='vfx:trim(ai:INTELLCONT_JOURNAL_NAME)'/>
</xsl:element>


<xsl:text>&#xA;</xsl:text>
<xsl:element name='aiic:ARTICLE_LIST'>
<xsl:for-each select='current-group()'>
<xsl:variable name='ref_netid' select="../../dm:Record/dm:username"/>
<xsl:for-each select='ai:INTELLCONT_JOURNAL_ID'>
<xsl:sort select='.'/>
<xsl:element name='aiic:ARTICLE_INFO'>
<xsl:attribute name='ref_netid'><xsl:value-of select='$ref_netid'/></xsl:attribute>AI-<xsl:value-of select='.'/>
</xsl:element>
</xsl:for-each>
<xsl:text>&#xA;</xsl:text>
<xsl:text>&#xA;</xsl:text>
</xsl:for-each>
<!-- aiic:ARTICLE_LIST -->
</xsl:element> 

<!-- /xsl:for-each -->
<!-- aiic:ARTICLES_BY_JOURNAL -->
</xsl:element>
</xsl:for-each-group>
<!-- aiic:JOURNAL_LIST -->
</xsl:element> 
</xsl:template>

<xsl:function name='vfx:clean' as='xs:string'>
<xsl:param name='s1'/>

<xsl:variable name='res' select='replace($s1, "\s", "")'/>
<xsl:value-of select='upper-case(replace($res,"[.,]",""))'/>

</xsl:function>
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
</xsl:stylesheet>
