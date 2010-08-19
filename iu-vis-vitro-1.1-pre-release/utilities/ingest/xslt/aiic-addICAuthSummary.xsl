<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:dm="http://www.digitalmeasures.com/schema/data"
xmlns:dmd="http://www.digitalmeasures.com/schema/data-metadata"
xmlns="http://www.digitalmeasures.com/schema/data"
xmlns:ai="http://www.digitalmeasures.com/schema/data"
 version="2.0">   

<xsl:output method="xml" indent="yes"/> 

<!-- ============================================================= -->
<xsl:template match='*'>

<xsl:copy-of select='node()|@*' copy-namespaces='no'/>

<xsl:element name="ai:INTELLCONT_AUTHORSHIP">
<xsl:for-each select='//dm:INTELLCONT_AUTH[not(./dm:LNAME=preceding::dm:INTELLCONT_AUTH/dm:LNAME) or not(./dm:FNAME=preceding::dm:INTELLCONT_AUTH/dm:FNAME)]'>
<xsl:call-template name='IntellcontAuthorShipCalc'>
  <xsl:with-param name='auth' select='.'/>
</xsl:call-template>
</xsl:for-each>
</xsl:element>

</xsl:template>
<!-- ============================================================= -->

<xsl:template match='/'>
<Data>
<xsl:apply-templates select='*'/>
</Data>
</xsl:template>



<!-- ============================================================= -->
<xsl:template name="IntellcontAuthorShipCalc">
<xsl:param name='auth'/>
<xsl:variable name='lname' select='$auth/dm:LNAME'/>
<xsl:variable name='fname' select='$auth/dm:FNAME'/>
<xsl:element name='ai:AUTHOR'>

<xsl:copy-of select='dm:LNAME|dm:FNAME|dm:MNAME|dm:FACULTY_NAME' copy-namespaces='no' />

<ai:INTELLCONT_AUTHORLIST>
<xsl:copy-of select='dm:INTELLCONT_AUTHORSHIP_ORDER'/>

<xsl:if test='following::dm:INTELLCONT_AUTH'>
  <xsl:for-each select='following::dm:INTELLCONT_AUTH'>

    <xsl:if test='$lname = ./dm:LNAME and $fname =./dm:FNAME'>
      <xsl:copy-of select='./dm:INTELLCONT_AUTHORSHIP_ORDER' copy-namespaces='no'/>
    </xsl:if>

  </xsl:for-each>
</xsl:if>

</ai:INTELLCONT_AUTHORLIST>

</xsl:element>
</xsl:template>


</xsl:stylesheet>
