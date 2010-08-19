<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:aigrant="http://vivoweb.org/activity-insight"
xmlns:dm='http://www.digitalmeasures.com/schema/data'
xmlns:xs='http://www.w3.org/2001/XMLSchema'
xmlns:vfx='http://vivoweb.org/ext/functions'	
exclude-result-prefixes='vfx xs dm'
>

<!-- created 20100806 -->
<xsl:param name='listxml' required='yes'/>
<xsl:output method='xml' indent='yes'/>
<xsl:strip-space elements="*"/>
<xsl:variable name='NL'>
<xsl:text>
</xsl:text>
</xsl:variable>

<!-- define global variables here -->

<xsl:template match='/'>

<xsl:variable name='docs' as='node()*'
	select='collection($listxml)'/>

<aigrant:PREVIOUS_FUNDING_AGENCIES xmlns:aigrant='http://vivoweb.org/activity-insight'>

<xsl:for-each-group select='$docs//dm:Record/dm:CONGRANT/dm:CONGRANT_PREVIOUS_AGENCY' 
	group-by='vfx:collapse(dm:AGENCY)'>
<xsl:sort select='vfx:collapse(dm:AGENCY)'/>

<!-- define variables here -->

<aigrant:AGENCY>
<xsl:attribute name='index' select='position()'/>

<aigrant:AGENCY_NAME><xsl:value-of select='dm:AGENCY'/></aigrant:AGENCY_NAME>

<aigrant:GRANTS_LIST>

<xsl:for-each select='current-group()'>
<xsl:sort select='@id'/>
<xsl:if test='../dm:USER_REFERENCE_CREATOR = "Yes"'>
<aigrant:GRANT_INFO>
<!-- define member attributes here -->

<xsl:attribute name='grid' select='../@id'/>
<xsl:attribute name='ref_netid' select='../../../dm:Record/@username'/>
<!-- define member property sub tags here -->

</aigrant:GRANT_INFO>
</xsl:if>

</xsl:for-each>

</aigrant:GRANTS_LIST>
</aigrant:AGENCY>

</xsl:for-each-group>

</aigrant:PREVIOUS_FUNDING_AGENCIES>
<xsl:value-of select='$NL'/>
</xsl:template>

<!-- ================================== -->


<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
