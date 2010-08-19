<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 version="2.0">   

<xsl:import href='copy.xsl'/>
<xsl:output method='xml' indent='yes'/>
<xsl:template match="*">
<xsl:element name='{local-name()}'>
<xsl:apply-templates select='@*'/>
<xsl:apply-templates/>
</xsl:element>
</xsl:template>

</xsl:stylesheet>