<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:dm="http://www.digitalmeasures.com/schema/data"
xmlns:dmd="http://www.digitalmeasures.com/schema/data-metadata"
xmlns="http://www.digitalmeasures.com/schema/data"
xmlns:ai="http://www.digitalmeasures.com/schema/data"
xmlns:xs='http://www.w3.org/2001/XMLSchema'
xmlns:vfx='http://vivoweb.org/ext/functions'	
exclude-result-prefixes='vfx xs'
 version="2.0">   

<xsl:output method="xml" indent="yes"/> 

<!-- ============================================================= -->
<xsl:template match='*'>

<xsl:copy-of select='node()|@*' copy-namespaces='no'/>

<xsl:element name="ai:INTELLCONT_PROCEEDINGS">
<xsl:for-each select='//dm:INTELLCONT[ not(./dm:BOOK_TITLE=preceding::dm:BOOK_TITLE)]'>
<xsl:comment><xsl:value-of select='./dm:CONTYPE'/></xsl:comment>
<xsl:if test='vfx:isProceedings(.)'>
<xsl:call-template name='IntellcontProceedings'>
  <xsl:with-param name='proceedings' select='.'/>
</xsl:call-template>
</xsl:if>
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
<xsl:template name="IntellcontProceedings">
<xsl:param name='proceedings'/>
<xsl:variable name='id' select='$proceedings/@id'/>
<xsl:variable name='pname' select='$proceedings/dm:BOOK_TITLE'/>

<xsl:element name='ai:PROCEEDINGS'>
<ai:ILK>
<xsl:value-of select='vfx:trim($proceedings/dm:CONTYPE)'/>
</ai:ILK>
<ai:INTELLCONT_PROCEEDINGS_NAME>
<xsl:value-of select='$pname'/>
</ai:INTELLCONT_PROCEEDINGS_NAME>
<ai:INTELLCONT_PROCEEDINGS_ID>
<xsl:value-of select='$id'/>
</ai:INTELLCONT_PROCEEDINGS_ID>



<xsl:if test='following::dm:INTELLCONT'>
  <xsl:for-each select='following::dm:INTELLCONT'>

    <xsl:if test=' $pname =./dm:BOOK_TITLE'>

      	<ai:INTELLCONT_PROCEEDINGS_ID>
	<xsl:value-of select='./@id'/>
	</ai:INTELLCONT_PROCEEDINGS_ID>

    </xsl:if>

  </xsl:for-each>
</xsl:if>

</xsl:element>
</xsl:template>

<xsl:function name='vfx:isProceedings' as='xs:boolean'>
<xsl:param name='n'/>

<xsl:choose>
<xsl:when test='vfx:clean( $n/dm:CONTYPE )  = "CONFERENCEPROCEEDING"'>
<xsl:value-of select='xs:boolean(1)'/>
</xsl:when>
<xsl:when test='vfx:clean( $n/dm:CONTYPE )  = "BOOK"'>
<xsl:value-of select='xs:boolean(1)'/>
</xsl:when>
<xsl:when test='vfx:clean( $n/dm:CONTYPE )  = "BOOKCHAPTER"'>
<xsl:value-of select='xs:boolean(1)'/>
</xsl:when>
<xsl:when test='vfx:clean( $n/dm:CONTYPE )  = "BOOKSECTION"'>
<xsl:value-of select='xs:boolean(1)'/>
</xsl:when>
<xsl:when test='vfx:clean( $n/dm:CONTYPE )  = "BOOKSCHOLARLY"'>
<xsl:value-of select='xs:boolean(1)'/>
</xsl:when>
<xsl:when test='vfx:clean( $n/dm:CONTYPE )  = "BOOKTEXTBOOK"'>
<xsl:value-of select='xs:boolean(1)'/>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select='xs:boolean(0)'/>
</xsl:otherwise>
</xsl:choose>
</xsl:function>

<!-- ============================================= -->
<!-- this extension function  
	1. removes whitespace and . and , from the argument string
	2. shifts alphabetic characters to upper case
	3. returns the adjusted string for comparison
-->
<xsl:function name='vfx:clean' as='xs:string'>
<xsl:param name='s1'/>
<xsl:variable name='res' select='replace($s1, "\s", "")'/>
<xsl:value-of select='upper-case(replace($res,"[.,]",""))'/>
</xsl:function>

<!-- ============================================= -->
<!-- this extension function 
removes leading and trailing whitespace from the argument string
-->
<xsl:function name='vfx:trim' as='xs:string'>
<xsl:param name='s1'/>
<xsl:choose>
<xsl:when test='$s1 != ""'>
<xsl:analyze-string select='$s1' regex='^\s*(.+?)\s*$'>
<xsl:matching-substring>
<xsl:value-of select='regex-group(1)'/>
</xsl:matching-substring>
</xsl:analyze-string>
</xsl:when>
<xsl:otherwise>
<xsl:text>Unknown</xsl:text>
</xsl:otherwise>
</xsl:choose>
</xsl:function>

</xsl:stylesheet>
