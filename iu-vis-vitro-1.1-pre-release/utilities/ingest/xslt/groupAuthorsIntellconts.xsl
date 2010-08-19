<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:ai="http://www.digitalmeasures.com/schema/data"
	xmlns:aiic="http://vivoweb.org/activity-insight"
        xmlns="http://vivoweb.org/activity-insight"
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


<xsl:template match='aiic:INTELLCONT_ITEMS_BY_AUTHOR'>
<xsl:element name='aiic:INTELLCONT_ITEMS_BY_AUTHOR'>
 <xsl:attribute name='counter'>
	<xsl:number level='multiple'/> </xsl:attribute>

<xsl:copy-of select='aiic:AUTHOR_NAME|aiic:LastName|aiic:FirstName|aiic:MiddleName|aiic:AiUserId|aiic:NetId'/>

<xsl:variable name='aalist' 
    select='aiic:INTELLCONT_LIST/aiic:INTELLCONT_INFO[@ref_netid = ../../aiic:NetId]'/>


<xsl:element name='aiic:INTELLCONT_LIST'>
<!-- xsl:comment><xsl:value-of select='count($aalist)'/> - <xsl:value-of select='count($aalist)'/> 
</xsl:comment -->

<xsl:for-each select='$aalist'>
<xsl:copy-of select='.'/>
</xsl:for-each>

<!-- xsl:value-of select='count($aalist)'/ -->


<xsl:for-each select='aiic:INTELLCONT_LIST/aiic:INTELLCONT_INFO'>
<xsl:variable name='ssnode' select='.'/>

<xsl:if test='./@ref_netid != ../../aiic:NetId '>

<xsl:choose>
<xsl:when test='$aalist = . or . = preceding-sibling::aiic:INTELLCONT_INFO'>
<xsl:element name='aiic:ALT_SRC_INTELLCONT_INFO'>
<xsl:attribute name='ref_netid' select='$ssnode/@ref_netid'/>
<xsl:attribute name='ai_userid' select='$ssnode/@ai_userid'/>
<xsl:attribute name='authorRank' select='$ssnode/@authorRank'/>
<xsl:attribute name='public' select='$ssnode/@public'/>
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
