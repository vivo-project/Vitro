<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:ai="http://www.digitalmeasures.com/schema/data"
xmlns:aiic="http://vivoweb.org/activity-insight"
	xmlns:dmd="http://www.digitalmeasures.com/schema/data-metadata"
	xmlns="http://www.digitalmeasures.com/schema/data"
	xmlns:dm="http://www.digitalmeasures.com/schema/data">

<xsl:output method='xml' indent='yes'/>
<xsl:strip-space elements="*"/>

<xsl:variable name='NL'>
<xsl:text>
</xsl:text>
</xsl:variable>

<xsl:variable name='aiid_netid' 
  select="document('../aiid-netid.xml')//aiic:mapterm"/>

<xsl:template match='/'>
<xsl:element name='ai:res'>
<xsl:apply-templates select='//dm:INTELLCONT'/>
</xsl:element>
<xsl:value-of select='$NL'/>
</xsl:template>

<xsl:template match='dm:INTELLCONT'>
<xsl:variable name='icid' select='@id'/>
<xsl:variable name='userId' select='../dm:userId'/>
<xsl:variable name='netid' select='../dm:username'/>
<xsl:element name='ai:icid'>
<xsl:value-of select='$icid'/>
</xsl:element>
<xsl:for-each select='dm:NON_JOURNAL_AUTHORLIST/dm:INTELLCONT_AUTH'>


<xsl:if test="dm:FACULTY_NAME != $userId and dm:FACULTY_NAME !=''">
<xsl:element name='ai:id'>
<xsl:value-of select='$userId'/>
</xsl:element>
<xsl:call-template name='idmap'>
   <xsl:with-param name='aiid' select='dm:FACULTY_NAME'/>
   </xsl:call-template>

<xsl:copy-of select='.'/><xsl:value-of select='$NL'/>
</xsl:if>

</xsl:for-each>

</xsl:template>

<xsl:template name='idmap'>
<xsl:param name='aiid'/>
<xsl:element name='ai:NetId'>
<xsl:if test='$aiid'>
<xsl:for-each select='$aiid_netid'>
<xsl:if test='$aiid = aiic:aiid'>
<xsl:value-of select='aiic:netid'/>
</xsl:if>
</xsl:for-each>
</xsl:if>
</xsl:element> 
</xsl:template>
</xsl:stylesheet>