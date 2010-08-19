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

<xsl:element name="ai:INTELLCONT_GROUPINGS">
<xsl:variable name='groups'>
<xsl:for-each select='//dm:INTELLCONT[ not(./dm:BOOK_TITLE=preceding::dm:BOOK_TITLE)]'>
<!--xsl:comment><xsl:value-of select='./dm:CONTYPE'/></xsl:comment-->
<xsl:if test='vfx:isGroupable(.)'>
<xsl:call-template name='IntellcontGroupings'>
  <xsl:with-param name='n' select='.'/>
</xsl:call-template>
</xsl:if>
</xsl:for-each>
</xsl:variable>


<xsl:for-each select='$groups/ai:GROUPING'>
<xsl:sort select='ai:ILK'/>
<xsl:copy-of select='.'/>
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
<xsl:template name="IntellcontGroupings">
<xsl:param name='n'/>
<xsl:variable name='id' select='$n/@id'/>
<xsl:variable name='name' select='$n/dm:BOOK_TITLE'/>

<xsl:element name='ai:GROUPING'>
<xsl:attribute name='onto_class' select='vfx:classify($n)'/>
<ai:ILK>
<xsl:value-of select='vfx:trim($n/dm:CONTYPE)'/>
</ai:ILK>
<ai:INTELLCONT_GROUPING_NAME>
<xsl:value-of select='$name'/>
</ai:INTELLCONT_GROUPING_NAME>
<ai:INTELLCONT_GROUPING_ID>
<xsl:value-of select='$id'/>
</ai:INTELLCONT_GROUPING_ID>



<xsl:if test='following::dm:INTELLCONT'>
  <xsl:for-each select='following::dm:INTELLCONT'>

    <xsl:if test=' $name =./dm:BOOK_TITLE'>

      	<ai:INTELLCONT_GROUPING_ID>
	<xsl:value-of select='./@id'/>
	</ai:INTELLCONT_GROUPING_ID>

    </xsl:if>

  </xsl:for-each>
</xsl:if>

</xsl:element>
</xsl:template>

<!-- ============================================================= -->

<xsl:function name='vfx:isGroupable' as='xs:boolean'>
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
<xsl:when test='vfx:clean( $n/dm:CONTYPE )  = "MONOGRAPH"'>
<xsl:value-of select='xs:boolean(1)'/>
</xsl:when>
<xsl:when test='starts-with(vfx:clean( $n/dm:CONTYPE ), "MAGAZINE")'>
<xsl:value-of select='xs:boolean(1)'/>
</xsl:when>
<xsl:when test='vfx:clean( $n/dm:CONTYPE )  = "NEWSPAPER"'>
<xsl:value-of select='xs:boolean(1)'/>
</xsl:when>
<xsl:when test='vfx:clean( $n/dm:CONTYPE )  = "TRADEPUBLICATION"'>
<xsl:value-of select='xs:boolean(1)'/>
</xsl:when>
<xsl:when test='vfx:clean( $n/dm:CONTYPE )  = "INTERNET"'>
<xsl:value-of select='xs:boolean(1)'/>
</xsl:when>
<xsl:when test='vfx:clean( $n/dm:CONTYPE )  = "TV"'>
<xsl:value-of select='xs:boolean(1)'/>
</xsl:when>
<xsl:when test='vfx:clean( $n/dm:CONTYPE )  = "RADIO"'>
<xsl:value-of select='xs:boolean(1)'/>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select='xs:boolean(0)'/>
</xsl:otherwise>
</xsl:choose>
</xsl:function>
<!-- ============================================================= -->

<xsl:function name='vfx:classify' as='xs:string'>
<xsl:param name='n'/>

<xsl:choose>
<xsl:when test='vfx:clean( $n/dm:CONTYPE )  = "CONFERENCEPROCEEDING"'>
<xsl:value-of select='"ai:ConferenceProceeding"'/>
</xsl:when>
<xsl:when test='vfx:clean( $n/dm:CONTYPE )  = "BOOK"'>
<xsl:value-of select='"bibo:Book"'/>
</xsl:when>
<xsl:when test='vfx:clean( $n/dm:CONTYPE )  = "BOOKCHAPTER"'>
<xsl:value-of select='"ai:BookChapter"'/>
</xsl:when>
<xsl:when test='vfx:clean( $n/dm:CONTYPE )  = "BOOKSECTION"'>
<xsl:value-of select='"ai:BookSection"'/>
</xsl:when>
<xsl:when test='vfx:clean( $n/dm:CONTYPE )  = "BOOKSCHOLARLY"'>
<xsl:value-of select='"ai:ScholarlyBook"'/>
</xsl:when>
<xsl:when test='vfx:clean( $n/dm:CONTYPE )  = "BOOKTEXTBOOK"'>
<xsl:value-of select='"bibo:Book"'/>
</xsl:when>
<xsl:when test='vfx:clean( $n/dm:CONTYPE )  = "MONOGRAPH"'>
<xsl:value-of select='"bibo:Book"'/>
</xsl:when>
<xsl:when test='starts-with(vfx:clean( $n/dm:CONTYPE ), "MAGAZINE")'>
<xsl:value-of select='"ai:Magazine"'/>
</xsl:when>
<xsl:when test='vfx:clean( $n/dm:CONTYPE )  = "NEWSPAPER"'>
<xsl:value-of select='"ai:Newspaper"'/>
</xsl:when>
<xsl:when test='vfx:clean( $n/dm:CONTYPE )  = "TRADEPUBLICATION"'>
<xsl:value-of select='"ai:TradePublication"'/>
</xsl:when>
<xsl:when test='vfx:clean( $n/dm:CONTYPE )  = "INTERNET"'>
<xsl:value-of select='"ai:Internet"'/>
</xsl:when>
<xsl:when test='vfx:clean( $n/dm:CONTYPE )  = "TV"'>
<xsl:value-of select='"ai:Radio"'/>
</xsl:when>
<xsl:when test='vfx:clean( $n/dm:CONTYPE )  = "RADIO"'>
<xsl:value-of select='"ai:Television"'/>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select='"Unknown"'/>
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

<xsl:template name='hasMatch'>
<xsl:param name='n'/>
<xsl:param name='nlist'/>
<xsl:param name='res' select='false()'/>
<xsl:choose>
<xsl:when test='$nlist and not($res)'>
<xsl:variable name='comp' select='vfx:trim($n) = vfx:trim($nlist[1])'/>
<xsl:call-template name='hasMatch'>
<xsl:with-param name='n'/>
<xsl:with-param name='nlist' select='$nlist[position()>1]'/>
<xsl:with-param name='res' select='$res or $comp'/>
</xsl:call-template>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select='$res'/>
</xsl:otherwise>
</xsl:choose>

</xsl:template>
</xsl:stylesheet>
