<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:ai="http://www.digitalmeasures.com/schema/data"
	xmlns:aiic="http://vivoweb.org/ontology/activity-insight"
	xmlns:aiis="http://vivoweb.org/ontology/activity-insight"
        xmlns="http://vivoweb.org/ontology/activity-insight"
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

<xsl:template match='aiic:ARTICLE_LIST'>
<xsl:element name='aiic:ARTICLE_LIST'>
<xsl:for-each select='aiic:ARTICLE_INFO'>
<xsl:sort select='.'/>
<xsl:copy-of select='.'/><xsl:value-of select='$NL'/>
</xsl:for-each>

<xsl:for-each select='aiic:ALT_SRC_ARTICLE_INFO'>
<xsl:sort select='.'/>
<xsl:copy-of select='.'/>
</xsl:for-each>

</xsl:element>
</xsl:template>


<xsl:template match='aiic:INTELLCONT_LIST'>
<xsl:element name='aiic:INTELLCONT_LIST'>
<xsl:for-each select='aiic:INTELLCONT_INFO'>
<xsl:sort select='.'/>
<xsl:copy-of select='.'/><xsl:value-of select='$NL'/>
</xsl:for-each>

<xsl:for-each select='aiic:ALT_SRC_INTELLCONT_INFO'>
<xsl:sort select='.'/>
<xsl:copy-of select='.'/>
</xsl:for-each>

</xsl:element>
</xsl:template>

<xsl:template match='aiis:IMPACT_STMT_LIST'>
<xsl:element name='aiis:IMPACT_STMT_LIST'>

<xsl:for-each select='aiis:IMPACT_STMT_INFO'>
<xsl:sort select='.'/>
<xsl:copy-of select='.'/><xsl:value-of select='$NL'/>
</xsl:for-each>

<xsl:for-each select='aiis:ALT_SRC_IMPACT_STMT_INFO'>
<xsl:sort select='.'/>
<xsl:copy-of select='.'/>
</xsl:for-each>

</xsl:element>

</xsl:template>
<xsl:template match='aiis:IMPACT_STMT_ID_LIST'>
<xsl:element name='aiis:IMPACT_STMT_ID_LIST'>

<xsl:for-each select='aiis:IMPACT_STMT_ID'>
<xsl:sort select='.'/>
<xsl:copy-of select='.'/><xsl:value-of select='$NL'/>
</xsl:for-each>

<xsl:for-each select='aiis:ALT_SRC_IMPACT_STMT_ID'>
<xsl:sort select='.'/>
<xsl:copy-of select='.'/>
</xsl:for-each>

</xsl:element>
</xsl:template>

</xsl:stylesheet>
