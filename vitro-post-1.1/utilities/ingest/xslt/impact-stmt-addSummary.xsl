<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"

xmlns:ai="http://www.digitalmeasures.com/schema/data"
xmlns:xs='http://www.w3.org/2001/XMLSchema'
xmlns:vfx='http://vivoweb.org/ext/functions'	
exclude-result-prefixes='vfx xs'
 version="2.0">   

<xsl:output method="xml" indent="yes"/> 
<!-- /home/jrm424/aiw
java -jar xslt/saxon9he.jar AIXMLS/cur-raw/cbb2.xml xslt/impact-stmt-Filter.xsl > foo.xml

java -jar xslt/saxon9he.jar foo.xml xslt/noname.xsl > impstmt.xml

java -jar xslt/saxon9he.jar impstmt.xml xslt/impact-stmt-addIndex.xsl > tmp.xml

java -jar xslt/saxon9he.jar tmp.xml xslt/impact-stmt-addSummary.xsl  > impstml-cbb2.xml

-->
<!-- ============================================================= -->
<xsl:template match='*'>

<xsl:copy-of select='node()|@*' copy-namespaces='no'/>

<xsl:element name="LOCAL_COLLABORATOR_LIST">

<xsl:for-each select='//IMPACT_STATEMENT_INVEST[not(vfx:hasMatchingName(./FNAME,./MNAME,./LNAME,preceding::IMPACT_STATEMENT_INVEST))]'>
<xsl:call-template name='Investigator'>
  <xsl:with-param name='cohort' select='.'/>
</xsl:call-template>
</xsl:for-each>


<xsl:for-each select='//IMPACT_STATEMENT_ENTITY[not(vfx:hasMatchingEntity(./ENTITY,./STATE_COUNTRY,preceding::IMPACT_STATEMENT_ENTITY))]'>
<xsl:call-template name='Entity'>
  <xsl:with-param name='cohort' select='.'/>
</xsl:call-template>
</xsl:for-each>

</xsl:element>

<xsl:element name="LOCAL_GEO_LIST">
<xsl:for-each select='//INVOLVED_GEO_PLACE[not(.=preceding::INVOLVED_GEO_PLACE)]'>
<xsl:call-template name='Geo'>
  <xsl:with-param name='place' select='.'/>
</xsl:call-template>
</xsl:for-each>
</xsl:element>

<xsl:element name="LOCAL_FUNDING_ORG_LIST">
<xsl:for-each select='//FUNDING_ORG[not(. = preceding::FUNDING_ORG)]'>
<xsl:call-template name='FundingOrg'>
  <xsl:with-param name='org' select='.'/>
</xsl:call-template>
</xsl:for-each>
</xsl:element>

<xsl:element name="EMPHASIS_LIST">
<xsl:for-each select='//EMPHASIS[not(. = preceding::EMPHASIS)]'>
<xsl:call-template name='Emphasis'>
  <xsl:with-param name='subj' select='.'/>
</xsl:call-template>
</xsl:for-each>
</xsl:element>

<xsl:element name="CONAREA_LIST">
<xsl:for-each select='//CONAREA[not(. = preceding::CONAREA)]'>
<xsl:call-template name='Conarea'>
  <xsl:with-param name='area' select='.'/>
</xsl:call-template>
</xsl:for-each>
</xsl:element>

<xsl:element name="USDA_LIST">
<xsl:for-each select='//USDA_INFO[not(. = preceding::USDA_INFO)]'>
<xsl:call-template name='Usda'>
  <xsl:with-param name='area' select='.'/>
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

<xsl:template name="Investigator">
<xsl:param name='cohort'/>
<xsl:variable name='lname' select='$cohort/LNAME'/>
<xsl:variable name='fname' select='$cohort/FNAME'/>
<xsl:variable name='mname' select='$cohort/MNAME'/>
<xsl:variable name='dep' select='$cohort/DEP'/>
<xsl:element name='COLLABORATOR'>
<xsl:attribute name='ilk' select='"invest"'/>
<xsl:copy-of select='LNAME|FNAME|MNAME|FACULTY_NAME|DEP' copy-namespaces='no' />

<INVESTIGATOR_IMPACT_STMT_LIST>
<xsl:copy-of select='IMPACT_STMT_INFO'/>

<xsl:if test='following::IMPACT_STATEMENT_INVEST'>
  <xsl:for-each select='following::IMPACT_STATEMENT_INVEST'>

    <xsl:if test='vfx:collapse($lname) = vfx:collapse(./LNAME) and vfx:collapse($mname) = vfx:collapse(./MNAME) and vfx:collapse($fname) = vfx:collapse(./FNAME) and vfx:collapse($dep) = vfx:collapse(./DEP)'>
      <xsl:copy-of select='./IMPACT_STMT_INFO' copy-namespaces='no'/>
    </xsl:if>

  </xsl:for-each>
</xsl:if>

</INVESTIGATOR_IMPACT_STMT_LIST>

</xsl:element>
</xsl:template>

<!-- ============================================================= -->

<xsl:template name="Entity">
<xsl:param name='cohort'/>
<xsl:variable name='name' select='$cohort/ENTITY'/>
<xsl:variable name='stcntry' select='$cohort/STATE_COUNTRY'/>
<xsl:element name='COLLABORATOR'>
<xsl:attribute name='ilk' select='"entity"'/>
<xsl:copy-of select='ENTITY|STATE_COUNTRY' copy-namespaces='no' />

<ENTITY_IMPACT_STMT_LIST>
<xsl:copy-of select='IMPACT_STMT_INFO'/>

<xsl:if test='following::IMPACT_STATEMENT_ENTITY'>
  <xsl:for-each select='following::IMPACT_STATEMENT_ENTITY'>

    <xsl:if test='vfx:collapse($name) = vfx:collapse(./ENTITY) and vfx:collapse($stcntry) = vfx:collapse(./STATE_COUNTRY)'>
      <xsl:copy-of select='./IMPACT_STMT_INFO' copy-namespaces='no'/>
    </xsl:if>

  </xsl:for-each>
</xsl:if>

</ENTITY_IMPACT_STMT_LIST>

</xsl:element>
</xsl:template>

<xsl:template name="Geo">
<xsl:param name='place'/>
<GEO_PLACE>
<xsl:copy-of select='$place/@ilk'/>
<GEO_PLACE_NAME>
<xsl:value-of select='$place'/>
</GEO_PLACE_NAME>
<IMPACT_STMT_ID_LIST>
<IMPACT_STMT_ID>
<xsl:attribute name='hasTitle' select=
	'if(../../TITLE = "") then "No" else "Yes"'/>
<xsl:attribute name='hasGoodAuthor' select=
	'if(vfx:IS-hasOneGoodName(../../COLLABORATORS/IMPACT_STATEMENT_INVEST)) 
	then "Yes" else "No"'/>
<xsl:value-of select='$place/@isid'/>
</IMPACT_STMT_ID>
<xsl:if test='following::INVOLVED_GEO_PLACE'>
<xsl:for-each select='following::INVOLVED_GEO_PLACE'>
	<xsl:if test='vfx:collapse($place) = vfx:collapse(.)'>
	<IMPACT_STMT_ID>
	<xsl:attribute name='hasTitle' select=
		'if(../../TITLE = "") then "No" else "Yes"'/>
	<xsl:attribute name='hasGoodAuthor' select=
		'if(vfx:IS-hasOneGoodName(../../COLLABORATORS/IMPACT_STATEMENT_INVEST)) 
			then "Yes" else "No"'/>
	<xsl:value-of select='./@isid'/>
	</IMPACT_STMT_ID>
	</xsl:if>
</xsl:for-each>
</xsl:if>

</IMPACT_STMT_ID_LIST>
</GEO_PLACE>
</xsl:template>


<xsl:template name="FundingOrg">
<xsl:param name='org'/>
<FORG>
<xsl:copy-of select='$org/@ilk'/>
<FORG_NAME>
<xsl:value-of select='$org'/>
</FORG_NAME>
<IMPACT_STMT_ID_LIST>
<IMPACT_STMT_ID>
<xsl:attribute name='hasTitle' select=
	'if(../../TITLE = "") then "No" else "Yes"'/>
<xsl:attribute name='hasGoodAuthor' select=
	'if(vfx:IS-hasOneGoodName(../../COLLABORATORS/IMPACT_STATEMENT_INVEST)) 
	then "Yes" else "No"'/>
<xsl:value-of select='$org/@isid'/>
</IMPACT_STMT_ID>
<xsl:if test='following::FUNDING_ORG'>
<xsl:for-each select='following::FUNDING_ORG'>
	<xsl:if test='vfx:collapse($org) = vfx:collapse(.)'>
	<IMPACT_STMT_ID>
	<xsl:attribute name='hasTitle' select=
		'if(../../TITLE = "") then "No" else "Yes"'/>
	<xsl:attribute name='hasGoodAuthor' select=
		'if(vfx:IS-hasOneGoodName(../../COLLABORATORS/IMPACT_STATEMENT_INVEST)) 
			then "Yes" else "No"'/>
	<xsl:value-of select='./@isid'/>
	</IMPACT_STMT_ID>
	</xsl:if>
</xsl:for-each>
</xsl:if>

</IMPACT_STMT_ID_LIST>
</FORG>
</xsl:template>


<xsl:template name="Emphasis">
<xsl:param name='subj'/>
<EMPHASIS_INFO>
<xsl:copy-of select='$subj/@ilk'/>
<EMPHASIS_NAME>
<xsl:value-of select='$subj'/>
</EMPHASIS_NAME>
<IMPACT_STMT_ID_LIST>
<IMPACT_STMT_ID>
<xsl:attribute name='hasTitle' select=
	'if(../../TITLE = "") then "No" else "Yes"'/>
<xsl:attribute name='hasGoodAuthor' select=
	'if(vfx:IS-hasOneGoodName(../../COLLABORATORS/IMPACT_STATEMENT_INVEST)) 
	then "Yes" else "No"'/>
<xsl:value-of select='$subj/@isid'/>
</IMPACT_STMT_ID>
<xsl:if test='following::EMPHASIS'>
<xsl:for-each select='following::EMPHASIS'>
	<xsl:if test='vfx:collapse($subj) = vfx:collapse(.)'>
	<IMPACT_STMT_ID>
	<xsl:attribute name='hasTitle' select=
		'if(../../TITLE = "") then "No" else "Yes"'/>
	<xsl:attribute name='hasGoodAuthor' select=
		'if(vfx:IS-hasOneGoodName(../../COLLABORATORS/IMPACT_STATEMENT_INVEST)) 
			then "Yes" else "No"'/>
	<xsl:value-of select='./@isid'/>
	</IMPACT_STMT_ID>
	</xsl:if>
</xsl:for-each>
</xsl:if>

</IMPACT_STMT_ID_LIST>
</EMPHASIS_INFO>
</xsl:template>


<xsl:template name="Conarea">
<xsl:param name='area'/>
<CONAREA_INFO>
<CONAREA_NAME>
<xsl:value-of select='$area'/>
</CONAREA_NAME>
<IMPACT_STMT_ID_LIST>
<IMPACT_STMT_ID>
<xsl:attribute name='hasTitle' select=
	'if(../../TITLE = "") then "No" else "Yes"'/>
<xsl:attribute name='hasGoodAuthor' select=
	'if(vfx:IS-hasOneGoodName(../../COLLABORATORS/IMPACT_STATEMENT_INVEST)) 
	then "Yes" else "No"'/>
<xsl:value-of select='$area/@isid'/>
</IMPACT_STMT_ID>
<xsl:if test='following::CONAREA'>
<xsl:for-each select='following::CONAREA'>
	<xsl:if test='vfx:collapse($area) = vfx:collapse(.)'>
	<IMPACT_STMT_ID>
	<xsl:attribute name='hasTitle' select=
		'if(../../TITLE = "") then "No" else "Yes"'/>
	<xsl:attribute name='hasGoodAuthor' select=
		'if(vfx:IS-hasOneGoodName(../../COLLABORATORS/IMPACT_STATEMENT_INVEST)) 
			then "Yes" else "No"'/>
	<xsl:value-of select='./@isid'/>
	</IMPACT_STMT_ID>
	</xsl:if>
</xsl:for-each>
</xsl:if>

</IMPACT_STMT_ID_LIST>
</CONAREA_INFO>
</xsl:template>

<xsl:template name="Usda">
<xsl:param name='area'/>
<USDA_AREA_INFO>
<xsl:copy-of select='$area/@ilk'/>
<USDA_AREA_NAME>
<xsl:value-of select='$area'/>
</USDA_AREA_NAME>
<IMPACT_STMT_ID_LIST>
<IMPACT_STMT_ID>
<xsl:attribute name='hasTitle' select=
	'if(../../TITLE = "") then "No" else "Yes"'/>
<xsl:attribute name='hasGoodAuthor' select=
	'if(vfx:IS-hasOneGoodName(../../COLLABORATORS/IMPACT_STATEMENT_INVEST)) 
	then "Yes" else "No"'/>
<xsl:value-of select='$area/@isid'/>
</IMPACT_STMT_ID>

<xsl:if test='following::USDA_INFO'>
<xsl:for-each select='following::USDA_INFO'>
	<xsl:if test='vfx:collapse($area) = vfx:collapse(.)'>
	<IMPACT_STMT_ID>
	<xsl:attribute name='hasTitle' select=
		'if(../../TITLE = "") then "No" else "Yes"'/>
	<xsl:attribute name='hasGoodAuthor' select=
		'if(vfx:IS-hasOneGoodName(../../COLLABORATORS/IMPACT_STATEMENT_INVEST)) 
			then "Yes" else "No"'/>
	<xsl:value-of select='./@isid'/>
	</IMPACT_STMT_ID>
	</xsl:if>
</xsl:for-each>
</xsl:if>

</IMPACT_STMT_ID_LIST>
</USDA_AREA_INFO>
</xsl:template>




<xsl:template name='hasMatchingInvestigator'>
<xsl:param name='fn'/>
<xsl:param name='mn'/>
<xsl:param name='ln'/>
<xsl:param name='nlist'/>
<xsl:param name='res' select='false()'/>
<xsl:choose>
<xsl:when test='$nlist and not($res)'>
<xsl:variable name='fnmnln' select='concat($fn,"|",$mn,"|",$ln)'/>
<xsl:variable name='listfnmnln' select='concat($nlist[1]/FNAME,"|",$nlist[1]/MNAME,"|",$nlist[1]/LNAME)'/>
<xsl:variable name='comp' select='vfx:collapse($fnmnln) = vfx:collapse($listfnmnln)'/>
<!-- xsl:variable name='comp' select='$fa = $nlist[1]'/ -->
<xsl:call-template name='hasMatchingInvestigator'>
<xsl:with-param name='fn' select='$fn'/>
<xsl:with-param name='mn' select='$mn'/>
<xsl:with-param name='ln' select='$ln'/>
<xsl:with-param name='nlist' select='$nlist[position()>1]'/>
<xsl:with-param name='res' select='$res or $comp'/>
</xsl:call-template>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select='$res'/>
</xsl:otherwise>
</xsl:choose>
</xsl:template>

<!-- Similarly================================================= -->

<xsl:function name='vfx:hasMatchingInvestigator' as='xs:boolean'>
<xsl:param name='fn'/>
<xsl:param name='mn'/>
<xsl:param name='ln'/>
<xsl:param name='nlist'/>
<xsl:call-template name='hasMatchingInvestigator'>
<xsl:with-param name='fn' select='$fn'/>
<xsl:with-param name='mn' select='$mn'/>
<xsl:with-param name='ln' select='$ln'/>
<xsl:with-param name='nlist' select='$nlist'/>
</xsl:call-template>
</xsl:function>

<xsl:template name='hasMatchingEntity'>
<xsl:param name='fa'/>
<xsl:param name='sa'/>
<xsl:param name='nlist'/>
<xsl:param name='res' select='false()'/>
<xsl:choose>
<xsl:when test='$nlist and not($res)'>
<xsl:variable name='fasa' select='concat($fa,$sa)'/>
<xsl:variable name='listfasa' select='concat($nlist[1]/ENTITY,$nlist[1]/STATE_COUNTRY)'/>
<xsl:variable name='comp' select='vfx:collapse($fasa) = vfx:collapse($listfasa)'/>
<!-- xsl:variable name='comp' select='$fa = $nlist[1]'/ -->
<xsl:call-template name='hasMatchingEntity'>
<xsl:with-param name='fa' select='$fa'/>
<xsl:with-param name='sa' select='$sa'/>
<xsl:with-param name='nlist' select='$nlist[position()>1]'/>
<xsl:with-param name='res' select='$res or $comp'/>
</xsl:call-template>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select='$res'/>
</xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:function name='vfx:hasMatchingEntity' as='xs:boolean'>
<xsl:param name='fa'/>
<xsl:param name='sa'/>
<xsl:param name='nlist'/>
<xsl:call-template name='hasMatchingEntity'>
<xsl:with-param name='fa' select='$fa'/>
<xsl:with-param name='sa' select='$sa'/>
<xsl:with-param name='nlist' select='$nlist'/>
</xsl:call-template>
</xsl:function>

<xsl:include href='vivofuncs.xsl'/>
</xsl:stylesheet>
