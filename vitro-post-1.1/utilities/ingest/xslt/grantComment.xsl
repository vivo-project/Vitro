<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:aigrant="http://vivoweb.org/ontology/activity-insight"
xmlns:dm='http://www.digitalmeasures.com/schema/data'
xmlns:xs='http://www.w3.org/2001/XMLSchema'
xmlns:vfx='http://vivoweb.org/ext/functions'	
exclude-result-prefixes='vfx xs dm'
>

<!-- created 20100806130702 -->
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

<aigrant:GRANT_COMMENT_LIST>

<xsl:for-each-group select='$docs//dm:Record/dm:CONGRANT/dm:CONGRANT_COMMENT' 
	group-by='vfx:collapse(../@id)'>
<xsl:sort select='vfx:collapse(../@id)'/>

<!-- define variables here -->

<aigrant:GRANT_COMMENT>
<xsl:attribute name='index' select='position()'/>
<xsl:attribute name='grcomid' select='@id'/>
<xsl:attribute name='skip' select='if(./dm:COMMENT != "") then "N" else "Y"'/>
<aigrant:COMMENT><xsl:value-of select='dm:COMMENT'/></aigrant:COMMENT>

<aigrant:GRANTS_LIST>

<xsl:for-each select='current-group()'>

<xsl:if test='../dm:USER_REFERENCE_CREATOR = "Yes"'>
<aigrant:GRANT_INFO>
<!-- define member attributes here -->

<xsl:attribute name='grid' select='../@id'/>
<xsl:attribute name='ref-netid' select='../../../dm:Record/@username'/>
<!-- define member property sub tags here -->

</aigrant:GRANT_INFO>
</xsl:if>
</xsl:for-each>

</aigrant:GRANTS_LIST>
</aigrant:GRANT_COMMENT>

</xsl:for-each-group>

</aigrant:GRANT_COMMENT_LIST>
<xsl:value-of select='$NL'/>
</xsl:template>

<!-- ================================== -->


<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
