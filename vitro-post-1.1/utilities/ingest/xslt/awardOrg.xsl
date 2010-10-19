<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:ai="http://www.digitalmeasures.com/schema/data"
	xmlns:aiah="http://vivoweb.org/ontology/activity-insight"
	xmlns:aiic="http://vivoweb.org/ontology/activity-insight"
	xmlns="http://vivoweb.org/ontology/activity-insight"
	xmlns:dm="http://www.digitalmeasures.com/schema/data"
	xmlns:xs='http://www.w3.org/2001/XMLSchema'
	xmlns:vfx='http://vivoweb.org/ext/functions'	
	exclude-result-prefixes='vfx xs'
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

<aiah:ORG_LIST>

<xsl:for-each-group select='$docs//dm:Record/dm:AWARDHONOR' 
	group-by='vfx:collapse(dm:ORG)'>
<xsl:sort select='vfx:collapse(dm:ORG)'/>


<xsl:variable name='auth' select='..'/>
<xsl:variable name='cur_netid' select='$auth/@username'/>
<xsl:variable name='cur_aiid' select='$auth/@userId'/>
<aiah:ORG>
<xsl:attribute name='counter' select='position()'/>
<aiah:org_name><xsl:value-of select='dm:ORG'/></aiah:org_name>
<aiah:AWARD_LIST>

<xsl:for-each select='current-group()'>


<aiah:AWARD>
<xsl:attribute name='ahid' select='@id'/>
<xsl:attribute name='display' select='dm:PUBLIC_VIEW'/>
<xsl:attribute name='netid' select='../../dm:Record/@username'/>
</aiah:AWARD>
</xsl:for-each>


</aiah:AWARD_LIST>
</aiah:ORG>
</xsl:for-each-group>

</aiah:ORG_LIST>
</xsl:template>
<!-- ================================== -->


<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>