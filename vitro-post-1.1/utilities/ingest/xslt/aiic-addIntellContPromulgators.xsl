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

<xsl:variable name='NL'>
<xsl:text>
</xsl:text>
</xsl:variable>
<!-- ============================================================= -->
<xsl:template match='*'>

<xsl:copy-of select='node()|@*' copy-namespaces='no'/>

<xsl:element name="ai:INTELLCONT_PROMULGATORS">

<xsl:variable name='props'>



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
<xsl:attribute name='hasTitle' select=
		'if($n/dm:TITLE = "") then "No" else "Yes"'/>
<xsl:attribute name='public' select=
		'if($n/dm:PUBLIC_VIEW = "Yes") then "Yes" else "No"'/>
<xsl:attribute name='hasGoodAuthor' select=
		'if(vfx:hasOneGoodName($n/dm:NON_JOURNAL_AUTHORLIST))
		 then "Yes" else "No"'/>
<xsl:value-of select='$id'/>
</ai:INTELLCONT_ID>



<xsl:if test='following::dm:INTELLCONT'>
  <xsl:for-each select='following::dm:INTELLCONT'>

    <xsl:if test=' vfx:collapse($name) = vfx:collapse(./dm:PROMULGATED_BY)'>

      	<ai:INTELLCONT_ID>
		<xsl:attribute name='hasTitle' select=
		'if(./dm:TITLE = "") then "No" else "Yes"'/>
		<xsl:attribute name='public' select=
		'if(./dm:PUBLIC_VIEW = "Yes") then "Yes" else "No"'/>
		<xsl:attribute name='hasGoodAuthor' select=
		'if(vfx:hasOneGoodName(./dm:NON_JOURNAL_AUTHORLIST))
		 then "Yes" else "No"'/>
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
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "BOOK CHAPTER"'>
<xsl:value-of select='xs:boolean(1)'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "BOOK REVIEW"'>
<xsl:value-of select='xs:boolean(1)'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "BOOK SECTION"'>
<xsl:value-of select='xs:boolean(1)'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "BOOK SCHOLARLY"'>
<xsl:value-of select='xs:boolean(1)'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "BOOK TEXTBOOK"'>
<xsl:value-of select='xs:boolean(1)'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "CITED RESEARCH"'>
<xsl:value-of select='xs:boolean(1)'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "CONFERENCE PROCEEDING"'>
<xsl:value-of select='xs:boolean(1)'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "INSTRUCTORS MANUAL"'>
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
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "POLICY REPORT"'>
<xsl:value-of select='xs:boolean(1)'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "POSTER"'>
<xsl:value-of select='xs:boolean(1)'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "RADIO"'>
<xsl:value-of select='xs:boolean(1)'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "RESEARCH REPORT"'>
<xsl:value-of select='xs:boolean(1)'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "SOFTWARE"'>
<xsl:value-of select='xs:boolean(1)'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "TV"'>
<xsl:value-of select='xs:boolean(1)'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "TECHNICAL REPORT"'>
<xsl:value-of select='xs:boolean(1)'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "TRADE PUBLICATION"'>
<xsl:value-of select='xs:boolean(1)'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "TRAINING MANUAL"'>
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
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "BOOK CHAPTER"'>
<xsl:value-of select='"bibo:BookChapter"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "BOOK REVIEW"'>
<xsl:value-of select='"core:Review"'/>
</xsl:when>

<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "BOOK SECTION"'>
<xsl:value-of select='"bibo:BookSection"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "BOOK SCHOLARLY"'>
<xsl:value-of select='"bibo:Book"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "BOOK TEXTBOOK"'>
<xsl:value-of select='"bibo:Book"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "CITED RESEARCH"'>
<xsl:value-of select='"core:informationResource"'/>
</xsl:when>

<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "CONFERENCE PROCEEDING"'>
<xsl:value-of select='"core:ConferencePaper"'/>
</xsl:when>

<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "INSTRUCTORS MANUAL"'>
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
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "POLICY REPORT"'>
<xsl:value-of select='"bibo:Report"'/>
</xsl:when>

<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "POSTER"'>
<xsl:value-of select='"core:ConferencePoster"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "RADIO"'>
<xsl:value-of select='"bibo:AudioDocument"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "RESEARCH REPORT"'>
<xsl:value-of select='"bibo:Report"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "SOFTWARE"'>
<xsl:value-of select='"core:Software"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "TV"'>
<xsl:value-of select='"bibo:AudioVisualDocument"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "TECHNICAL REPORT"'>
<xsl:value-of select='"bibo:Report"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "TRADE PUBLICATION"'>
<xsl:value-of select='"bibo:Article"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "TRAINING MANUAL"'>
<xsl:value-of select='"bibo:Manual"'/>
</xsl:when>

<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "TRANSLATION"'>
<xsl:value-of select='"core:Translation"'/>
</xsl:when>

<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "WRITTENCASE"'>
<xsl:value-of select='"core:CaseStudy"'/>
</xsl:when>

<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "ECORNELL COURSE"'>
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
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "BOOK CHAPTER"'>
<xsl:value-of select='"bibo:Book"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "BOOK REVIEW"'>
<xsl:value-of select='"nil"'/>
</xsl:when>

<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "BOOK SECTION"'>
<xsl:value-of select='"bibo:Book"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "BOOK SCHOLARLY"'>
<xsl:value-of select='"nil"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "BOOK TEXTBOOK"'>
<xsl:value-of select='"nil"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "CITED RESEARCH"'>
<xsl:value-of select='"nil"'/>
</xsl:when>

<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "CONFERENCE PROCEEDING"'>
<xsl:value-of select='"bibo:Proceedings"'/>
</xsl:when>

<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "INSTRUCTORS MANUAL"'>
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
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "POLICY REPORT"'>
<xsl:value-of select='"nil"'/>
</xsl:when>

<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "POSTER"'>
<xsl:value-of select='"nil"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "RADIO"'>
<xsl:value-of select='"nil"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "RESEARCH REPORT"'>
<xsl:value-of select='"nil"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "SOFTWARE"'>
<xsl:value-of select='"nil"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "TV"'>
<xsl:value-of select='"nil"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "TECHNICAL REPORT"'>
<xsl:value-of select='"nil"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "TRADE PUBLICATION"'>
<xsl:value-of select='"bibo:Periodical"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "TRAINING MANUAL"'>
<xsl:value-of select='"nil"'/>
</xsl:when>

<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "TRANSLATION"'>
<xsl:value-of select='"nil"'/>
</xsl:when>

<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "WRITTEN CASE"'>
<xsl:value-of select='"nil"'/>
</xsl:when>

<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "ECORNELL COURSE"'>
<xsl:value-of select='"nil"'/>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select='"nil"'/>
</xsl:otherwise>
</xsl:choose>
</xsl:function>


<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
