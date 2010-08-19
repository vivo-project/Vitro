<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:ai="http://www.digitalmeasures.com/schema/data"
	xmlns:aiic="http://vivoweb.org/activity-insight"
xmlns="http://vivoweb.org/activity-insight"
	xmlns:dm="http://www.digitalmeasures.com/schema/data">

<xsl:output method='xml' indent='yes'/>
<xsl:strip-space elements="*"/>
<xsl:variable name='NL'>
<xsl:text>
</xsl:text>
</xsl:variable>
<xsl:template match='/'>
<xsl:element name='aiic:possible_cornell_author' namespace='http://vivoweb.org/activity-insight'>
<xsl:apply-templates select='*'/>
</xsl:element>
</xsl:template>
<xsl:template match='node()|@*'>

<xsl:apply-templates select='node()|@*'/>

</xsl:template>

<xsl:template match='//aiic:ARTICLES_BY_AUTHOR/aiic:ARTICLE_LIST'>

<xsl:if test='count(aiic:ARTICLE_ID) &gt; count(aiic:ARTICLE_ID[@cornell_id != ""]) and count(aiic:ARTICLE_ID) &gt; count(aiic:ARTICLE_ID[@cornell_id = ""]) and count(aiic:ARTICLE_ID) > 1'>
<xsl:element name='aiic:possible'>
<xsl:element name='aiic:counts'>
<xsl:text>[</xsl:text><xsl:value-of select='count(aiic:ARTICLE_ID)'/><xsl:text>, </xsl:text><xsl:value-of select='count(aiic:ARTICLE_ID[@cornell_id != ""])'/><xsl:text>]</xsl:text>
</xsl:element>
<xsl:copy-of select='..'/>
</xsl:element>
</xsl:if>

</xsl:template>

</xsl:stylesheet>