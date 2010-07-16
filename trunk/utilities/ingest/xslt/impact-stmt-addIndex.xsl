<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"

xmlns:xs='http://www.w3.org/2001/XMLSchema'
xmlns:vfx='http://vivoweb.org/ext/functions'	
exclude-result-prefixes='vfx xs'
 version="2.0">   

<xsl:output method="xml" indent="yes"/> 

<xsl:variable name='aiuid'>
<xsl:value-of select='//Record/@userId'/>
</xsl:variable>

<!-- ============================================================= -->

<xsl:template match="node()| @*">
<xsl:copy>
<xsl:apply-templates select="node() | @*"/>
</xsl:copy>
</xsl:template>

<!-- ============================================================= -->

<xsl:template match='Record'>
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

<xsl:template match='IMPACT_STATEMENT'>

<xsl:element name='{name()}' namespace='{namespace-uri()}'>
<xsl:attribute name='id'><xsl:value-of select='@id'/></xsl:attribute>
<xsl:attribute name='lastModified'>
	    <xsl:value-of select='./@lastModified'/>
</xsl:attribute>
<!-- OTHER ATTRIBUTES -->

<xsl:element name='FUNDING_ORGS'>
<xsl:apply-templates select='FUNDING_FEDRCH|FUNDING_FEDRCHOTHER|FUNDING_FEDEXT|FUNDING_FEDEXTOTHER|FUNDING_ACAD|FUNDING_ACADOTHER|FUNDING_USDA|FUNDING_OTHERFED|FUNDING_STATE|FUNDING_PRIVATE|FUNDING_PRIVATEOTHER'/>
</xsl:element>

<!--xsl:apply-templates select='FUNDING_FEDRCH| FUNDING_FEDRCHOTHER| FUNDING_FEDEXT |FUNDING_FEDEXTOTHER|FUNDING_ACAD|FUNDING_ACADOTHER|FUNDING_USDA|FUNDING_OTHERFED|FUNDING_STATE|FUNDING_PRIVATE|FUNDING_PRIVATEOTHER'/-->

<xsl:apply-templates select='DTY_START|START_START| START_END|DTY_END| END_START| END_END '/>

<xsl:apply-templates select='TITLE'/>

<xsl:element name='CONAREAS'>
<xsl:apply-templates select='CONAREA'/>
</xsl:element>
<xsl:apply-templates select='RCHTYPE'/>

<xsl:element name='EMPHASIZES'>
<xsl:apply-templates select='PRIORITY_AREA |PRIORITY_AREAOTHER'/>
</xsl:element>

<xsl:apply-templates select='OTHER_COUNTRIES | USDA_AREA | USDA_AREAOTHER'/>

<xsl:element name='INVOLVED_GEO_PLACES'>
<xsl:apply-templates select='INVOLVED_STATE| INVOLVED_COUNTY| INVOLVED_COUNTRY'/>
</xsl:element>

<xsl:apply-templates select='SUMMARY|ISSUE|RESPONSE|IMPACT|FUNDTYPE|FUNDTYPE_OTHER '/>

<xsl:element name='COLLABORATORS'>
<xsl:apply-templates select='IMPACT_STATEMENT_ENTITY|IMPACT_STATEMENT_INVEST'/>
</xsl:element>

</xsl:element>

</xsl:template>

<!-- ============================================================= -->

<xsl:template match="IMPACT_STATEMENT_ENTITY|IMPACT_STATEMENT_INVEST">

<xsl:element name='{name()}' namespace='{namespace-uri()}'>
<xsl:copy-of select='*|@*'/>
<xsl:element name='IMPACT_STMT_INFO' namespace='{namespace-uri()}'>

<xsl:element name='COLLABORATION_POSITION' namespace='{namespace-uri()}'>
<xsl:number/>
</xsl:element>

<xsl:element name='IMPACT_STMT_ID' namespace='{namespace-uri()}'>
<xsl:value-of select='../@id'/>
</xsl:element>

<xsl:element name='PUBLIC' namespace='{namespace-uri()}'>
<xsl:if test='FACULTY_NAME = $aiuid'>
<xsl:value-of select='../PUBLIC_VIEW'/>
</xsl:if>
</xsl:element>

</xsl:element>
</xsl:element>

</xsl:template>

<!-- ============================================================= -->
<xsl:template match="FUNDING_FEDRCH|FUNDING_FEDRCHOTHER|FUNDING_FEDEXT|FUNDING_FEDEXTOTHER|FUNDING_ACAD|FUNDING_ACADOTHER|FUNDING_USDA|FUNDING_OTHERFED|FUNDING_STATE|FUNDING_PRIVATE|FUNDING_PRIVATEOTHER">
<xsl:variable name='kind' select='substring-after(local-name(),"_")'/>
<xsl:if test='. != ""'>
<xsl:element name='FUNDING_ORG'>
<xsl:attribute name='ilk' select='$kind'/>
<xsl:attribute name='isid' select='../@id'/>
<xsl:value-of select= '.'/>
</xsl:element>
<!-- xsl:copy-of select='.'/ -->
</xsl:if>



</xsl:template>

<xsl:template match="INVOLVED_STATE| INVOLVED_COUNTY | INVOLVED_COUNTRY">
<xsl:variable name='kind' select='substring-after(local-name(),"_")'/>
<xsl:if test='. != ""'>
<xsl:element name='INVOLVED_GEO_PLACE'>
<xsl:attribute name='ilk' select='$kind'/>
<xsl:attribute name='isid' select='../@id'/>
<xsl:value-of select= '.'/>
</xsl:element>
<!-- xsl:copy-of select='.'/ -->
</xsl:if>
</xsl:template>

<xsl:template match="PRIORITY_AREA | PRIORITY_AREAOTHER">
<xsl:variable name='kind' select='local-name()'/>
<xsl:if test='. != ""'>
<xsl:element name='EMPHASIS'>
<xsl:attribute name='ilk' select='$kind'/>
<xsl:attribute name='isid' select='../@id'/>
<xsl:value-of select= '.'/>
</xsl:element>
<!-- xsl:copy-of select='.'/ -->
</xsl:if>
</xsl:template>

<xsl:template match="CONAREA">
<xsl:if test='. != ""'>
<xsl:element name='{local-name()}'>
<xsl:attribute name='isid' select='../@id'/>
<xsl:value-of select= '.'/>
</xsl:element>
<!-- xsl:copy-of select='.'/ -->
</xsl:if>
</xsl:template>

<!--
<xsl:choose>
<xsl:when test='name()="FUNDING_FEDRCH"'>

</xsl:when>
<xsl:when test='name()="FUNDING_FEDRCHOTHER"'>

</xsl:when>
<xsl:when test='name()="FUNDING_FEDEXT"'>

</xsl:when>
<xsl:when test='name()="FUNDING_FEDEXTOTHER"'>

</xsl:when>
<xsl:when test='name()="FUNDING_ACAD"'>

</xsl:when>
<xsl:when test='name()="FUNDING_ACADOTHER"'>

</xsl:when>
<xsl:when test='name()="FUNDING_USDA"'>

</xsl:when>
<xsl:when test='name()="FUNDING_OTHERFED"'>

</xsl:when>
<xsl:when test='name()="FUNDING_STATE"'>

</xsl:when>
<xsl:when test='name()="FUNDING_PRIVATE"'>

</xsl:when>
<xsl:when test='name()="FUNDING_PRIVATEOTHER"'>

</xsl:when>
<xsl:when test='name()=""'>

</xsl:when>
</xsl:choose>
-->

</xsl:stylesheet>
