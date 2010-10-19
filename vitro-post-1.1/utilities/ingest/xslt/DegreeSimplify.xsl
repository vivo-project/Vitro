<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:sp="http://www.w3.org/2005/sparql-results#"
>


<xsl:output method='xml' indent='yes'/>
<xsl:strip-space elements="*"/>
<xsl:template match='//sp:results'>
<xsl:variable name='NL'>
<xsl:text>
</xsl:text>
</xsl:variable>

<xsl:element name='KnownCUVivoDegrees' inherit-namespaces='no'>
<xsl:for-each select='sp:result'>
<xsl:sort select='sp:binding[@name="label"]/sp:literal'/>
<xsl:element name='degree' inherit-namespaces='no'>
<xsl:for-each select='./sp:binding'>
<xsl:call-template name='parts'/>
</xsl:for-each>
<xsl:element name='abbrev' inherit-namespaces='no'>
<xsl:value-of select='"       "'/>
</xsl:element>
<xsl:element name='ai-abbrev' inherit-namespaces='no'>
<xsl:value-of select='"       "'/>
</xsl:element>
</xsl:element>

</xsl:for-each>
</xsl:element>
<xsl:value-of select='$NL'/>
</xsl:template>

<xsl:template name='parts'>


<xsl:choose>
<xsl:when test='./@name ="uri"'>
<xsl:element name='uri' inherit-namespaces='no'><xsl:value-of select='normalize-space(./sp:uri)'/></xsl:element>
</xsl:when>
<xsl:when test='./@name ="label"'>
<xsl:element name='name' inherit-namespaces='no'><xsl:value-of select='normalize-space(./sp:literal)'/></xsl:element>
</xsl:when>
</xsl:choose>


</xsl:template>


</xsl:stylesheet>

