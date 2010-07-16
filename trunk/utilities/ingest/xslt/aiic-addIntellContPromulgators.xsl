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

<xsl:element name="ai:INTELLCONT_PROMULGATORS">

<xsl:variable name='props'>

<!-- xsl:for-each select='//dm:INTELLCONT[ not(./dm:BOOK_TITLE = preceding::dm:BOOK_TITLE)]' -->
<!-- xsl:for-each select='//dm:INTELLCONT[ not(vfx:hasMatch(./dm:BOOK_TITLE,preceding::dm:BOOK_TITLE))]' -->

<xsl:for-each select='//dm:INTELLCONT[ not(vfx:hasMatch(./dm:PROMULGATED_BY,preceding::dm:PROMULGATED_BY))]'>
<!--xsl:comment><xsl:value-of select='./dm:CONTYPE'/></xsl:comment-->

<!-- xsl:if test='vfx:isPromulgatee(.)' -->
<xsl:call-template name='IntellcontPromulgators'>
  <xsl:with-param name='n' select='.'/>
</xsl:call-template>
<!-- /xsl:if -->
</xsl:for-each>
</xsl:variable>


<xsl:for-each select='$props/ai:PROMULGATOR'>
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
<xsl:template name="IntellcontPromulgators">
<xsl:param name='n'/>
<xsl:variable name='id' select='$n/@id'/>
<xsl:variable name='name' select='normalize-space($n/dm:PROMULGATED_BY)'/>

<xsl:element name='ai:PROMULGATOR'>
<xsl:attribute name='ontoClass' select='vfx:classify($n)'/>
<xsl:attribute name='ontoClassProm' select='vfx:classifyPromulgator($n)'/>
<ai:ILK>
<xsl:value-of select='vfx:trim($n/dm:CONTYPE)'/>
</ai:ILK>
<ai:INTELLCONT_PROMULGATOR_NAME>
<xsl:value-of select='$name'/>
</ai:INTELLCONT_PROMULGATOR_NAME>
<ai:INTELLCONT_ID>
<xsl:value-of select='$id'/>
</ai:INTELLCONT_ID>



<xsl:if test='following::dm:INTELLCONT'>
  <xsl:for-each select='following::dm:INTELLCONT'>

    <xsl:if test=' $name = normalize-space(./dm:PROMULGATED_BY)'>

      	<ai:INTELLCONT_ID>
	<xsl:value-of select='./@id'/>
	</ai:INTELLCONT_ID>

    </xsl:if>

  </xsl:for-each>
</xsl:if>

</xsl:element>
</xsl:template>

<!-- ============================================================= -->

<xsl:function name='vfx:isPromulgatee' as='xs:boolean'>
<xsl:param name='n'/>

<xsl:choose>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "ABSTRACT"'>
<xsl:value-of select='xs:boolean(1)'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "BOOKCHAPTER"'>
<xsl:value-of select='xs:boolean(1)'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "BOOKREVIEW"'>
<xsl:value-of select='xs:boolean(1)'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "BOOKSECTION"'>
<xsl:value-of select='xs:boolean(1)'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "BOOKSCHOLARLY"'>
<xsl:value-of select='xs:boolean(1)'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "BOOKTEXTBOOK"'>
<xsl:value-of select='xs:boolean(1)'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "CITEDRESEARCH"'>
<xsl:value-of select='xs:boolean(1)'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "CONFERENCEPROCEEDING"'>
<xsl:value-of select='xs:boolean(1)'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "INSTRUCTORSMANUAL"'>
<xsl:value-of select='xs:boolean(1)'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "INTERNET"'>
<xsl:value-of select='xs:boolean(1)'/>
</xsl:when>

<xsl:when test='starts-with(vfx:collapse( $n/dm:CONTYPE ), "MAGAZINE")'>
<xsl:value-of select='xs:boolean(1)'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "MONOGRAPH"'>
<xsl:value-of select='xs:boolean(1)'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "NEWSLETTER"'>
<xsl:value-of select='xs:boolean(1)'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "NEWSPAPER"'>
<xsl:value-of select='xs:boolean(1)'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "OTHER"'>
<xsl:value-of select='xs:boolean(1)'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "POLICYREPORT"'>
<xsl:value-of select='xs:boolean(1)'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "POSTER"'>
<xsl:value-of select='xs:boolean(1)'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "RADIO"'>
<xsl:value-of select='xs:boolean(1)'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "RESEARCHREPORT"'>
<xsl:value-of select='xs:boolean(1)'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "SOFTWARE"'>
<xsl:value-of select='xs:boolean(1)'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "TV"'>
<xsl:value-of select='xs:boolean(1)'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "TECHNICALREPORT"'>
<xsl:value-of select='xs:boolean(1)'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "TRADEPUBLICATION"'>
<xsl:value-of select='xs:boolean(1)'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "TRAININGMANUAL"'>
<xsl:value-of select='xs:boolean(1)'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "TRANSLATION"'>
<xsl:value-of select='xs:boolean(1)'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "WRITTENCASE"'>
<xsl:value-of select='xs:boolean(1)'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "ECORNELLCOURSE"'>
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
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "ABSTRACT"'>
<xsl:value-of select='"bibo:DocumentPart"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "BOOKCHAPTER"'>
<xsl:value-of select='"bibo:BookChapter"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "BOOKREVIEW"'>
<xsl:value-of select='"core:Review"'/>
</xsl:when>

<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "BOOKSECTION"'>
<xsl:value-of select='"bibo:BookSection"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "BOOKSCHOLARLY"'>
<xsl:value-of select='"bibo:Book"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "BOOKTEXTBOOK"'>
<xsl:value-of select='"bibo:Book"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "CITEDRESEARCH"'>
<xsl:value-of select='"core:informationResource"'/>
</xsl:when>

<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "CONFERENCEPROCEEDING"'>
<xsl:value-of select='"core:ConferencePaper"'/>
</xsl:when>

<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "INSTRUCTORSMANUAL"'>
<xsl:value-of select='"bibo:Manual"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "INTERNET"'>
<xsl:value-of select='"bibo:WebSite"'/>
</xsl:when>
<xsl:when test='starts-with(vfx:collapse( $n/dm:CONTYPE ), "MAGAZINE")'>
<xsl:value-of select='"bibo:Article"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "MONOGRAPH"'>
<xsl:value-of select='"bibo:Book"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "NEWSLETTER"'>
<xsl:value-of select='"bibo:Article"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "NEWSPAPER"'>
<xsl:value-of select='"bibo:Article"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "OTHER"'>
<xsl:value-of select='"core:InformationResource"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "POLICYREPORT"'>
<xsl:value-of select='"bibo:Report"'/>
</xsl:when>

<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "POSTER"'>
<xsl:value-of select='"core:ConferencePoster"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "RADIO"'>
<xsl:value-of select='"bibo:AudioDocument"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "RESEARCHREPORT"'>
<xsl:value-of select='"bibo:Report"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "SOFTWARE"'>
<xsl:value-of select='"core:Software"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "TV"'>
<xsl:value-of select='"bibo:AudioVisualDocument"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "TECHNICALREPORT"'>
<xsl:value-of select='"bibo:Report"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "TRADEPUBLICATION"'>
<xsl:value-of select='"bibo:Article"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "TRAININGMANUAL"'>
<xsl:value-of select='"bibo:Manual"'/>
</xsl:when>

<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "TRANSLATION"'>
<xsl:value-of select='"core:Translation"'/>
</xsl:when>

<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "WRITTENCASE"'>
<xsl:value-of select='"core:CaseStudy"'/>
</xsl:when>

<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "eCORNELLCOURSE"'>
<xsl:value-of select='"bibo:AudioVisualDocument"'/>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select='"ai:Intellcont"'/>
</xsl:otherwise>
</xsl:choose>
</xsl:function>

<!-- ===================================================== -->

<xsl:function name='vfx:classifyPromulgator' as='xs:string'>
<xsl:param name='n'/>

<xsl:choose>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "ABSTRACT"'>
<xsl:value-of select='"nil"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "BOOKCHAPTER"'>
<xsl:value-of select='"bibo:Book"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "BOOKREVIEW"'>
<xsl:value-of select='"nil"'/>
</xsl:when>

<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "BOOKSECTION"'>
<xsl:value-of select='"bibo:Book"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "BOOKSCHOLARLY"'>
<xsl:value-of select='"nil"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "BOOKTEXTBOOK"'>
<xsl:value-of select='"nil"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "CITEDRESEARCH"'>
<xsl:value-of select='"nil"'/>
</xsl:when>

<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "CONFERENCEPROCEEDING"'>
<xsl:value-of select='"bibo:Proceedings"'/>
</xsl:when>

<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "INSTRUCTORSMANUAL"'>
<xsl:value-of select='"nil"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "INTERNET"'>
<xsl:value-of select='"nil"'/>
</xsl:when>
<xsl:when test='starts-with(vfx:collapse( $n/dm:CONTYPE ), "MAGAZINE")'>
<xsl:value-of select='"nil"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "MONOGRAPH"'>
<xsl:value-of select='"nil"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "NEWSLETTER"'>
<xsl:value-of select='"nil"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "NEWSPAPER"'>
<xsl:value-of select='"bibo:Newspaper"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "OTHER"'>
<xsl:value-of select='"nil"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "POLICYREPORT"'>
<xsl:value-of select='"nil"'/>
</xsl:when>

<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "POSTER"'>
<xsl:value-of select='"nil"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "RADIO"'>
<xsl:value-of select='"nil"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "RESEARCHREPORT"'>
<xsl:value-of select='"nil"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "SOFTWARE"'>
<xsl:value-of select='"nil"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "TV"'>
<xsl:value-of select='"nil"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "TECHNICALREPORT"'>
<xsl:value-of select='"nil"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "TRADEPUBLICATION"'>
<xsl:value-of select='"bibo:Periodical"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "TRAININGMANUAL"'>
<xsl:value-of select='"nil"'/>
</xsl:when>

<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "TRANSLATION"'>
<xsl:value-of select='"nil"'/>
</xsl:when>

<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "WRITTENCASE"'>
<xsl:value-of select='"nil"'/>
</xsl:when>

<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "eCORNELLCOURSE"'>
<xsl:value-of select='"nil"'/>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select='"nil"'/>
</xsl:otherwise>
</xsl:choose>
</xsl:function>

<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
