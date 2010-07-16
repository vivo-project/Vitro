<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:ai="http://www.digitalmeasures.com/schema/data"
	xmlns:aiic="http://vivoweb.org/activity-insight"
xmlns="http://vivoweb.org/activity-insight"
	xmlns:dm="http://www.digitalmeasures.com/schema/data">

<xsl:param name='listxml' required='yes'/>
<xsl:output method='xml' indent='yes'/>
<xsl:strip-space elements="*"/>
<xsl:variable name='NL'>
<xsl:text>
</xsl:text>
</xsl:variable>

<!-- ================================================= -->
<xsl:template match='/'>

<xsl:variable name='docs' as='node()*'
	select='collection($listxml)'/>
<xsl:element name="aiic:aiidmap" namespace="http://vivoweb.org/activity-insight">
<xsl:for-each select='$docs/dm:Data/dm:Record'>
<xsl:sort select='upper-case(dm:username)'/>
<xsl:element name="aiic:mapterm">
<xsl:element name="aiic:netid">
<xsl:value-of select='dm:username'/>
</xsl:element>
<xsl:element name="aiic:aiid">
<xsl:value-of select='dm:userId'/>
</xsl:element>

</xsl:element>
</xsl:for-each>
</xsl:element>

</xsl:template>

</xsl:stylesheet>
