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

<xsl:template match='node()|@*'>
<xsl:copy>
<xsl:apply-templates select='node()|@*'/>
</xsl:copy>
</xsl:template>
<xsl:template match='aiic:ARTICLES_BY_AUTHOR'>
<xsl:element name='aiic:ARTICLES_BY_AUTHOR'>
 <xsl:attribute name='counter'>
	<xsl:number level='multiple'/> </xsl:attribute>
<xsl:apply-templates select='node()|@*'/>
</xsl:element>
</xsl:template>
<xsl:template match='aiic:ARTICLE_INFO'>
<xsl:variable name='psnodes' select='preceding-sibling::aiic:ARTICLE_INFO'/><xsl:text>&#xA;</xsl:text>
<xsl:variable name='snode' select='.'/>
<!--
<xsl:comment>
<xsl:text>[</xsl:text>
<xsl:value-of select='count(distinct-values($snode union $psnodes))'/>
<xsl:text>,</xsl:text>
<xsl:value-of select='count(distinct-values($psnodes))'/>
<xsl:text>]</xsl:text>
</xsl:comment><xsl:text>&#xA;</xsl:text>
-->
<!-- 
xsl:if test='count($snode union preceding-sibling::aiic:ARTICLE_INFO) != count(preceding-sibling::aiic:ARTICLE_INFO)' 
-->
<xsl:choose>
<xsl:when test='$snode[not( $snode = preceding-sibling::aiic:ARTICLE_INFO)]'>
<xsl:copy-of select='.'/>

</xsl:when>
<xsl:otherwise>
<xsl:element name='aiic:ALT_SRC_ARTICLE_INFO'>
<xsl:attribute name='ref_netid' select='@ref_netid'/>
<xsl:attribute name='ai_userid' select='@ai_userid'/>
<xsl:attribute name='authorRank' select='@authorRank'/>
<xsl:attribute name='public' select='@public'/>
<xsl:value-of select='.'/>
</xsl:element>
</xsl:otherwise>
</xsl:choose>
</xsl:template>

</xsl:stylesheet>
