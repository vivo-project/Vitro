<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:dm="http://www.digitalmeasures.com/schema/data"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:dmd="http://www.digitalmeasures.com/schema/data-metadata"
xmlns="http://www.digitalmeasures.com/schema/data"
xmlns:xs='http://www.w3.org/2001/XMLSchema'
xmlns:vfx='http://vivoweb.org/ext/functions'	
exclude-result-prefixes='vfx xs'
 version="2.0">   

<xsl:output method="xml" indent="yes"/> 

<xsl:variable name='aiuid'>
<xsl:value-of select='//dm:Record/@userId'/>
</xsl:variable>

<!-- ============================================================= -->

<xsl:template match="node()| @*">
<xsl:copy>
<xsl:apply-templates select="node() | @*"/>
</xsl:copy>
</xsl:template>

<!-- ============================================================= -->

<xsl:template match='dm:Record'>
<xsl:element name='{name()}'>
<userId>
<xsl:value-of select='./@userId'/>
</userId>
<username>
<xsl:value-of select='./@username'/>
</username>
<xsl:apply-templates/>
</xsl:element>
</xsl:template>

<!-- ============================================================= -->

<xsl:template match='dm:INTELLCONT_JOURNAL'>

<xsl:element name='{name()}' namespace='{namespace-uri()}'>
<xsl:attribute name='id'><xsl:value-of select='@id'/></xsl:attribute>
<xsl:attribute name='dmd:lastModified'>
	    <xsl:value-of select='./@dmd:lastModified'/>
	  </xsl:attribute>
<!-- OTHER ATTRIBUTES -->

<xsl:apply-templates select='dm:DTM_PUB | dm:DTD_PUB | dm:DTY_PUB | dm:PUB_START | dm:PUB_END| dm:CONAREA | dm:JOURNAL_NAME | dm:JOURNAL_NAME_OTHER | dm:STATUS | dm:REFEREED| dm:TITLE | dm:VOLUME | dm:ISSUE | dm:PAGENUM| dm:COMMENTS | dm:USER_REFERENCE_CREATOR | dm:PUBLIC_VIEW '/>

<xsl:element name='AuthorList'>

<xsl:apply-templates select='dm:INTELLCONT_JOURNAL_AUTH'/>

</xsl:element>

</xsl:element>

</xsl:template>

<!-- ============================================================= -->

<xsl:template match="dm:INTELLCONT_JOURNAL_AUTH">

<xsl:element name='{name()}' namespace='{namespace-uri()}'>
<xsl:copy-of select='*|@*'/>
<xsl:element name='ARTICLE_AUTHORSHIP_ORDER' namespace='{namespace-uri()}'>

<xsl:element name='AUTHORSHIP_POSITION' namespace='{namespace-uri()}'>
<xsl:number/>
</xsl:element>

<xsl:element name='ARTICLE_ID' namespace='{namespace-uri()}'>
<xsl:value-of select='../@id'/>
</xsl:element>

<xsl:element name='PUBLIC' namespace='{namespace-uri()}'>
<xsl:if test='dm:FACULTY_NAME = $aiuid'>
<xsl:value-of select='../dm:PUBLIC_VIEW'/>
</xsl:if>
</xsl:element>

</xsl:element>
</xsl:element>

</xsl:template>

<!-- ============================================================= -->

<xsl:template match='dm:INTELLCONT'>
<xsl:element name='{name()}' namespace='{namespace-uri()}'>
<xsl:attribute name='id'><xsl:value-of select='@id'/></xsl:attribute>
<xsl:attribute name='dmd:lastModified'>
	    <xsl:value-of select='./@dmd:lastModified'/>
	  </xsl:attribute>
<!-- OTHER ATTRIBUTES -->

<xsl:apply-templates select='dm:DTM_PUB | dm:DTD_PUB | dm:DTY_PUB | dm:PUB_START | dm:PUB_END| dm:CONAREA | dm:CONTYPE | dm:CONTYPEOTHER  | dm:STATUS | dm:REFEREED|  dm:PUBLICAVAIL | dm:TITLE | dm:PUBLISHER | dm:PUBCTYST | dm:VOLUME | dm:ISSUE | dm:BOOK_TITLE | dm:PAGENUM | dm:EDITORS | dm:COMMENTS | dm:PUBLIC_VIEW | dm:USER_REFERENCE_CREATOR'/>

<xsl:element name='PROMULGATED_BY'>
<xsl:value-of select='vfx:determinePromulgator(dm:CONAREA,dm:CONTYPE,dm:BOOK_TITLE,dm:PUBLISHER)'/>
</xsl:element>

<xsl:element name='NON_JOURNAL_AUTHORLIST'>

<xsl:apply-templates select='dm:INTELLCONT_AUTH'/>

</xsl:element>

</xsl:element>

</xsl:template>

<!-- ============================================================= -->

<xsl:template match="dm:INTELLCONT_AUTH">

<xsl:element name='{name()}' namespace='{namespace-uri()}'>
<xsl:copy-of select='*|@*'/>
<xsl:element name='INTELLCONT_AUTHORSHIP_ORDER' namespace='{namespace-uri()}'>
<xsl:element name='AUTHORSHIP_POSITION' namespace='{namespace-uri()}'>
<xsl:number/>
</xsl:element>

<xsl:element name='INTELLCONT_ID' namespace='{namespace-uri()}'>
<xsl:value-of select='../@id'/>
</xsl:element>

<xsl:element name='PUBLIC' namespace='{namespace-uri()}'>
<xsl:value-of select='../PUBLIC_VIEW'/>
</xsl:element>

</xsl:element>
</xsl:element>

</xsl:template>

<xsl:function name='vfx:determinePromulgator' as='xs:string'>
<xsl:param name='conarea'/>
<xsl:param name='contype'/>
<xsl:param name='booktitle'/>
<xsl:param name='publisher'/>

<xsl:choose>
<xsl:when test='$booktitle != ""'>
<xsl:value-of select='$booktitle'/>
</xsl:when>
<xsl:when test='$publisher != ""'>
<xsl:value-of select='$publisher'/>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select='"nil"'/>
</xsl:otherwise>
</xsl:choose>

</xsl:function>
</xsl:stylesheet>
