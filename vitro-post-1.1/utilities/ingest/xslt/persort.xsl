<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>

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

<xsl:template match='ExtantPersons'>
<ExtantPersons>
<xsl:for-each select='person'>
<xsl:sort select='lname'/>
<xsl:copy-of select='.' copy-namespaces='no'/><xsl:value-of select='$NL'/>
</xsl:for-each>
</ExtantPersons>
</xsl:template>

</xsl:stylesheet>
