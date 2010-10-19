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


<xsl:template match='aiis:IMPACT_STMTS_BY_INVESTIGATOR'>
<xsl:element name='aiis:IMPACT_STMTS_BY_INVESTIGATOR'>
 <xsl:attribute name='counter'>
	<xsl:number level='multiple'/> </xsl:attribute>

<xsl:copy-of select='aiis:INVESTIGATOR_NAME|aiis:LastName|aiis:FirstName|aiis:MiddleName|aiis:AiUserId|aiis:NetId|aiis:Department'/>

<xsl:variable name='aalist' 
    select='aiis:IMPACT_STMT_LIST/aiis:IMPACT_STMT_INFO[@ref_netid = ../../aiis:NetId]'/>


<xsl:element name='aiis:IMPACT_STMT_LIST'>
<!-- xsl:comment><xsl:value-of select='count($aalist)'/> - <xsl:value-of select='count($aalist)'/> 
</xsl:comment -->

<xsl:for-each select='$aalist'>
<xsl:copy-of select='.'/>
</xsl:for-each>

<!-- xsl:value-of select='count($aalist)'/ -->


<xsl:for-each select='aiis:IMPACT_STMT_LIST/aiis:IMPACT_STMT_INFO'>
<xsl:variable name='ssnode' select='.'/>

<xsl:if test='./@ref_netid != ../../aiis:NetId '>

<xsl:choose>
<xsl:when test='$aalist = . or . = preceding-sibling::aiis:IMPACT_STMT_INFO'>
<xsl:element name='aiis:ALT_SRC_IMPACT_STMT_INFO'>
<xsl:attribute name='ref_netid' select='$ssnode/@ref_netid'/>
<xsl:attribute name='ai_userid' select='$ssnode/@ai_userid'/>
<xsl:attribute name='collabRank' select='$ssnode/@collabRank'/>
<xsl:value-of select='$ssnode'/>
</xsl:element>
</xsl:when>
<xsl:otherwise>
<xsl:copy-of select='$ssnode'/>
</xsl:otherwise>
</xsl:choose>

</xsl:if>

</xsl:for-each>
</xsl:element>
</xsl:element>

</xsl:template>




</xsl:stylesheet>
