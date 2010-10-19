<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:aisvcprof="http://vivoweb.org/ontology/activity-insight"
xmlns:dm='http://www.digitalmeasures.com/schema/data'
xmlns:xs='http://www.w3.org/2001/XMLSchema'
xmlns:vfx='http://vivoweb.org/ext/functions'	
exclude-result-prefixes='vfx xs dm aisvcprof'
>


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

<aisvcprof:SERVICE_PROFESSIONAL_ORG_LIST>

<xsl:for-each-group select='$docs//dm:Record/dm:SERVICE_PROFESSIONAL[vfx:notEvent(dm:ORG)]' 
	group-by='vfx:collapse(dm:ORG)'>
<xsl:sort select='vfx:collapse(dm:ORG)'/>

<xsl:if test='vfx:notEvent(dm:ORG)'>
<!-- define variables here -->

<aisvcprof:SERVICE_PROFESSIONAL_ORG>

<xsl:attribute name='index' select='position()'/>

<aisvcprof:SERVICE_PROFESSIONAL_ORG_NAME>
<xsl:variable name='gen_orgname' select='vfx:trim(dm:ORG)'/>
<xsl:choose>
  <xsl:when test='$gen_orgname != ""'>
  <xsl:value-of select='$gen_orgname'/>
  </xsl:when>
  <xsl:otherwise>
  <xsl:value-of select='"Unspecified"'/>
  </xsl:otherwise>
</xsl:choose>

</aisvcprof:SERVICE_PROFESSIONAL_ORG_NAME>

<aisvcprof:SERVICE_PROFESSIONAL_LIST>

<xsl:for-each select='current-group()'>
<xsl:sort select='@id'/>

<xsl:if test='dm:PUBLIC_VIEW = "Yes"'>
<aisvcprof:SERVICE_PROFESSIONAL_INFO>
<!-- define member attributes here -->

<xsl:attribute name='id' select='@id'/>
<xsl:attribute name='ref_netid'>
<xsl:value-of select='../../dm:Record/@username'/></xsl:attribute>
<!-- define member property sub tags here -->

</aisvcprof:SERVICE_PROFESSIONAL_INFO>
</xsl:if>

</xsl:for-each>

</aisvcprof:SERVICE_PROFESSIONAL_LIST>
</aisvcprof:SERVICE_PROFESSIONAL_ORG>
</xsl:if>
</xsl:for-each-group>

</aisvcprof:SERVICE_PROFESSIONAL_ORG_LIST>
<xsl:value-of select='$NL'/>
</xsl:template>

<!-- ================================== -->


<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
