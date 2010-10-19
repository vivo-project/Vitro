<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:aisvcprof="http://vivoweb.org/ontology/activity-insight"
	xmlns:dm="http://www.digitalmeasures.com/schema/data"
	xmlns:xs='http://www.w3.org/2001/XMLSchema'
	xmlns:vfx='http://vivoweb.org/ext/functions'	
	exclude-result-prefixes='vfx xs dm'
>

<xsl:param name='listxml' required='yes'/>
<xsl:output method='xml' indent='yes'/>
<xsl:strip-space elements="*"/>
<xsl:variable name='NL'>
<xsl:text>
</xsl:text>
</xsl:variable>


<xsl:template match='/'>

<xsl:variable name='docs' as='node()*'
	select='collection($listxml)'/>

<aisvcprof:SERVICE_PROFESSIONAL_LIST>

<xsl:for-each-group select='$docs//dm:SERVICE_PROFESSIONAL' 
	group-by='@id'>
<xsl:sort select='@id'/>
<xsl:variable name='pos' select='position()'/>
<xsl:for-each select='current-group()'>

<xsl:if test="dm:PUBLIC_VIEW='Yes'">
<aisvcprof:SERVICE_PROFESSIONAL>
<xsl:attribute name='id' select='@id'/>
<xsl:attribute name='index' select='$pos'/>
<aisvcprof:netid><xsl:value-of select='../../dm:Record/@username'/></aisvcprof:netid>
<aisvcprof:uid><xsl:value-of select='../../dm:Record/@userId'/></aisvcprof:uid>
</aisvcprof:SERVICE_PROFESSIONAL>
</xsl:if>
</xsl:for-each>

</xsl:for-each-group>
</aisvcprof:SERVICE_PROFESSIONAL_LIST>
<xsl:value-of select='$NL'/>
</xsl:template>

<!-- ================================== -->


<xsl:include href='vivofuncs.xsl'/>


</xsl:stylesheet>
