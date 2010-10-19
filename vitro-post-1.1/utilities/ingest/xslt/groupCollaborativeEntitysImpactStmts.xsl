<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:ai="http://www.digitalmeasures.com/schema/data"
	xmlns:aiis="http://vivoweb.org/ontology/activity-insight"
        xmlns="http://vivoweb.org/ontology/activity-insight"
	xmlns:dm="http://www.digitalmeasures.com/schema/data">

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


<xsl:template match='aiis:IMPACT_STMTS_BY_COLLAB_ENTITY'>
<xsl:element name='aiis:IMPACT_STMTS_BY_COLLAB_ENTITY'>
 <xsl:attribute name='counter'>
	<xsl:number level='multiple'/> </xsl:attribute>

<xsl:copy-of select='aiis:COLLAB_ENTITY_NAME|aiis:Name|aiis:StateCountry'/>


<xsl:element name='aiis:IMPACT_STMT_LIST'>


<xsl:for-each select='aiis:IMPACT_STMT_LIST/aiis:IMPACT_STMT_INFO'>
<xsl:variable name='ssnode' select='.'/>

<xsl:choose>
<xsl:when test='. = preceding-sibling::aiis:IMPACT_STMT_INFO'>
<xsl:element name='aiis:ALT_SRC_IMPACT_STMT_INFO'>
<xsl:attribute name='ref_netid' select='$ssnode/@ref_netid'/>
<xsl:attribute name='collabRank' select='$ssnode/@collabRank'/>
<xsl:value-of select='$ssnode'/>
</xsl:element>
</xsl:when>
<xsl:otherwise>
<xsl:copy-of select='$ssnode'/>
</xsl:otherwise>
</xsl:choose>


</xsl:for-each>
</xsl:element>
</xsl:element>

</xsl:template>




</xsl:stylesheet>
