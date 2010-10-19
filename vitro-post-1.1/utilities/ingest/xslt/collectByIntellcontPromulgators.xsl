<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:ai="http://www.digitalmeasures.com/schema/data"
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

<xsl:element name="aiic:INTELLCONT_PROMULGATOR_LIST" 
namespace="http://vivoweb.org/ontology/activity-insight">

<xsl:for-each-group select='$docs/dm:Data/ai:INTELLCONT_PROMULGATORS/ai:PROMULGATOR' 
	group-by='vfx:collapse(concat(ai:ILK,ai:INTELLCONT_PROMULGATOR_NAME))'>
<xsl:sort select='vfx:collapse(concat(ai:ILK,ai:INTELLCONT_PROMULGATOR_NAME))'/>


<xsl:element name="aiic:INTELLCONTS_BY_PROMULGATOR" >
<xsl:attribute name='ontoClass' select='./@ontoClass'/>
<xsl:attribute name='ontoClassProm' select='./@ontoClassProm'/>
<xsl:element name='aiic:INTELLCONT_PROMULGATOR_ILK'>
<xsl:value-of select='ai:ILK'/>
</xsl:element>
<!-- xsl:for-each select='ai:INTELLCONT_PROMULGATOR_NAME' -->

<xsl:element name='aiic:INTELLCONT_PROMULGATOR_NAME'>
<xsl:attribute name='numrefs' select='count(current-group())'/>

<!--
<xsl:value-of select='count(current-group())'/>
-->

<xsl:value-of select='vfx:trim(ai:INTELLCONT_PROMULGATOR_NAME)'/>

</xsl:element>


<xsl:text>&#xA;</xsl:text>
<xsl:element name='aiic:INTELLCONT_LIST'>
<xsl:for-each select='current-group()'>
<xsl:variable name='ref_netid' select="../../dm:Record/dm:username"/>
<xsl:for-each select='ai:INTELLCONT_ID'>
<xsl:sort select='.'/>
<xsl:element name='aiic:INTELLCONT_INFO'>
<xsl:attribute name='ref_netid'>
<xsl:value-of select='$ref_netid'/>
</xsl:attribute>
<xsl:attribute name='hasTitle' select='./@hasTitle'/>
<xsl:attribute name='hasGoodAuthor' select='./@hasGoodAuthor'/>AI-<xsl:value-of select='.'/>

</xsl:element>
</xsl:for-each>
<xsl:text>&#xA;</xsl:text>
<xsl:text>&#xA;</xsl:text>
</xsl:for-each>
<!-- aiic:INTELLCONT_LIST -->
</xsl:element> 

<!-- /xsl:for-each -->
<!-- aiic:INTELLCONTS_BY_PROMULGATOR -->
</xsl:element>
</xsl:for-each-group>
<!-- aiic:INTELLCONT_PROMULGATOR_LIST -->
</xsl:element> 
</xsl:template>

<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
