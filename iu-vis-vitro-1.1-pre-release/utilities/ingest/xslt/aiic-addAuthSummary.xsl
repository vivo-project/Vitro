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

<xsl:element name="ai:AUTHORSHIP">
<xsl:for-each select='//dm:INTELLCONT_JOURNAL_AUTH[not(./dm:LNAME=preceding::dm:INTELLCONT_JOURNAL_AUTH/dm:LNAME) or not(./dm:FNAME=preceding::dm:INTELLCONT_JOURNAL_AUTH/dm:FNAME)]'>
<xsl:call-template name='AuthorShipCalc'>
  <xsl:with-param name='auth' select='.'/>
</xsl:call-template>
</xsl:for-each>
</xsl:element>

<xsl:element name="ai:JOURNALS">
<xsl:for-each select='//dm:INTELLCONT_JOURNAL[ not(./dm:JOURNAL_NAME=preceding::dm:JOURNAL_NAME)]'>
<xsl:call-template name='JournalCalc'>
  <xsl:with-param name='journal' select='.'/>
</xsl:call-template>
</xsl:for-each>
</xsl:element>

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

<xsl:template name="AuthorShipCalc">
<xsl:param name='auth'/>
<xsl:variable name='lname' select='$auth/dm:LNAME'/>
<xsl:variable name='fname' select='$auth/dm:FNAME'/>
<xsl:element name='ai:AUTHOR'>

<xsl:copy-of select='dm:LNAME|dm:FNAME|dm:MNAME|dm:FACULTY_NAME' copy-namespaces='no' />

<ai:ARTICLE_LIST_WITH_AUTHORSHIP_ORDERING>
<xsl:copy-of select='dm:ARTICLE_AUTHORSHIP_ORDER'/>

<xsl:if test='following::dm:INTELLCONT_JOURNAL_AUTH'>
  <xsl:for-each select='following::dm:INTELLCONT_JOURNAL_AUTH'>

    <xsl:if test='$lname = ./dm:LNAME and $fname =./dm:FNAME'>
      <xsl:copy-of select='./dm:ARTICLE_AUTHORSHIP_ORDER' copy-namespaces='no'/>
    </xsl:if>

  </xsl:for-each>
</xsl:if>

</ai:ARTICLE_LIST_WITH_AUTHORSHIP_ORDERING>

</xsl:element>
</xsl:template>

<!-- ============================================================= -->


<xsl:template name="JournalCalc">
<xsl:param name='journal'/>
<xsl:variable name='id' select='$journal/@id'/>
<xsl:variable name='jname' select='$journal/dm:JOURNAL_NAME'/>

<xsl:element name='ai:JOURNAL'>
<ai:INTELLCONT_JOURNAL_NAME>
<xsl:value-of select='$jname'/>
</ai:INTELLCONT_JOURNAL_NAME>
<ai:INTELLCONT_JOURNAL_ID>
<xsl:value-of select='$id'/>
</ai:INTELLCONT_JOURNAL_ID>



<xsl:if test='following::dm:INTELLCONT_JOURNAL'>
  <xsl:for-each select='following::dm:INTELLCONT_JOURNAL'>

    <xsl:if test=' $jname =./dm:JOURNAL_NAME'>

      	<ai:INTELLCONT_JOURNAL_ID>
	<xsl:value-of select='./@id'/>
	</ai:INTELLCONT_JOURNAL_ID>

    </xsl:if>

  </xsl:for-each>
</xsl:if>

</xsl:element>

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
