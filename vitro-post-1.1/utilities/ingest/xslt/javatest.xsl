<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs='http://www.w3.org/2001/XMLSchema'
	xmlns:IsoMatch='java:edu.cornell.saxonext.IsoMatch'
	exclude-result-prefixes='xs IsoMatch'
	>
<xsl:output method='xml' indent='yes'/>
<xsl:strip-space elements="*"/>
<xsl:template match='/'>
<result>

<xsl:value-of select='IsoMatch:scorereal("John","Quincy","Smith", "J","Quincy","Smith")'
	/>



</result>
<xsl:text>&#xA;</xsl:text>
</xsl:template>

</xsl:stylesheet>