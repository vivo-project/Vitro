<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs='http://www.w3.org/2001/XMLSchema'
	xmlns:core="http://vivoweb.org/ontology/core#"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        xmlns:bibo="http://purl.org/ontology/bibo/"
        xmlns:foaf="http://xmlns.com/foaf/0.1/"
        xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
	xmlns:aiis="http://vivoweb.org/ontology/activity-insight"
	xmlns:acti="http://vivoweb.org/ontology/activity-insight#"
	xmlns:dm="http://www.digitalmeasures.com/schema/data"	
	xmlns:vfx='http://vivoweb.org/ext/functions'
	exclude-result-prefixes='xs vfx'
	>


<xsl:param name='unoMapFile'  required='yes'/>
<xsl:param name='aiisXmlPath' required='yes'/>
<xsl:param name='aiisPrefix' required='yes'/>
<xsl:param name='extConAreasIn' required='yes'/>
<xsl:param name='extConAreasOut' required='yes'/>

<xsl:output method='xml' indent='yes'/>
<xsl:strip-space elements="*"/>

<xsl:include href='commonvars.xsl'/>

<xsl:variable name='unomap'
	select="document($unoMapFile)/Mapping"/>

<xsl:variable name='extantConAreas'
	select="document($extConAreasIn)/ExtantConAreas"/>
<!-- ================================== -->
<xsl:template match='/aiis:CONAREA_LIST'>
<rdf:RDF>
<xsl:variable name='rawNewConAreas'>
<xsl:element name='ExtantConAreas' inherit-namespaces='no'>

<xsl:for-each select='aiis:IMPACT_STMTS_BY_CONAREA'>

<xsl:variable name='name' select='vfx:simple-trim(aiis:CONAREA_NAME)'/>

<xsl:if test='$name != ""'>
<xsl:variable name='ctr'  select='@counter'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>


<xsl:variable name='knownUri' 
	select='vfx:knownCaUri($name, $extantConAreas)'/>
<xsl:variable name='conauri' 
	select="if($knownUri != '') then 
		$knownUri else concat($g_instance,$uno)"/>

<xsl:if test='$knownUri= ""'>
<xsl:element name='conarea' namespace=''>

<xsl:element name='uri' namespace=''>
<xsl:value-of select='concat("NEW-",$conauri)'/>
</xsl:element>

<xsl:element name='title' namespace=''>
<xsl:value-of select='$name'/>
</xsl:element>

</xsl:element>
</xsl:if>
</xsl:if>
</xsl:for-each>
</xsl:element>

</xsl:variable>

<!--
<xsl:comment>
<xsl:value-of select='count($rawNewConAreas//conarea)'/>
</xsl:comment>
<xsl:value-of select='$NL'/>
-->
<xsl:variable name='uniqueNewConAreas'>
<xsl:call-template name='NewConAreas'>
<xsl:with-param name='knowns' select='$rawNewConAreas/ExtantConAreas'/>
</xsl:call-template>
</xsl:variable>
<xsl:value-of select='$NL'/>

<xsl:for-each select='aiis:IMPACT_STMTS_BY_CONAREA'>

<!-- create a core:ContributionArea for this contribution area
OR use an old one -->

<xsl:variable name='name' select='vfx:simple-trim(aiis:CONAREA_NAME)'/>
<xsl:if test='$name != ""'>
<xsl:variable name='ctr'  select='@counter'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>
<!--xsl:comment>
<xsl:value-of select='$ctr'/> - <xsl:value-of select='$uno'/>
</xsl:comment -->

<!-- =================================================== -->
<!-- Declare a acti:ContributionArea (use extant con area if it exists) -->

<xsl:variable name='knownUri' 
select='vfx:knownCaUri($name, 
			$extantConAreas union
			$rawNewConAreas/ExtantConAreas)'/>

<xsl:variable name='cauri' 
	select='if(starts-with($knownUri,"NEW-")) then 
		substring-after($knownUri,"NEW-") else 
		$knownUri'/>


<rdf:Description rdf:about="{$cauri}">

<xsl:comment>
<xsl:value-of select='count($rawNewConAreas//conarea)'/>
</xsl:comment>
<rdf:type rdf:resource=
'http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>

<rdf:type rdf:resource='http://vivoweb.org/ontology/core#ContributionArea'/>

<rdfs:label>
<xsl:value-of select='$name'/>
</rdfs:label>

<core:description>
<xsl:value-of select='$name'/>
</core:description>

</rdf:Description>


<!-- =================================================== -->
<!-- now process the impact stmts attributed to this conarea -->

<xsl:call-template name='process-conarea'>

<xsl:with-param name='isbyca' select='aiis:IMPACT_STMT_ID_LIST'/>
<xsl:with-param name='caref' select="$cauri"/>
</xsl:call-template>
</xsl:if>

</xsl:for-each>

<!-- =================================================== 

-->
<xsl:result-document href='{$extConAreasOut}'>
<xsl:element name='ExtantConAreas' namespace=''>
<!--
<xsl:value-of select='$NL'/>
<xsl:comment>
<xsl:value-of select='count($uniqueNewConAreas//conarea)'/>
</xsl:comment>
<xsl:value-of select='$NL'/>
 -->
<xsl:for-each select='$uniqueNewConAreas//conarea'>

<xsl:element name='conarea' namespace=''>

<xsl:element name='uri' namespace=''>
<xsl:value-of select=
	'if(starts-with(uri,"NEW-")) then 
		substring-after(uri,"NEW-") else uri'/>

</xsl:element>

<xsl:element name='title' namespace=''>
<xsl:value-of select='title'/>
</xsl:element>

</xsl:element>


</xsl:for-each>
</xsl:element>
<xsl:value-of select='$NL'/>
</xsl:result-document>

</rdf:RDF>
<xsl:value-of select='$NL'/>
</xsl:template>

<!-- =================================================== -->
<!-- =================================================== -->
<xsl:template name='process-conarea'>
<xsl:param name='isbyca'/>
<xsl:param name='caref'/>

<xsl:for-each select='$isbyca/aiis:IMPACT_STMT_ID'>
<xsl:if test='./@hasTitle = "Yes" and ./@hasGoodAuthor = "Yes"'>
<xsl:variable name='aiid' select='.'/>

<!-- =================================================== -->
<!-- Declare property mapping acti:ImpactProject to 
acti:ContributionArea -->

<rdf:Description rdf:about="{concat($g_instance,$aiid)}" >

<acti:hasContributionArea rdf:resource="{$caref}"/>

</rdf:Description>
</xsl:if>
</xsl:for-each>

<!-- =================================================== -->

<rdf:Description rdf:about="{$caref}">

<xsl:for-each select='$isbyca/aiis:IMPACT_STMT_ID'>
<xsl:if test='./@hasTitle = "Yes" and ./@hasGoodAuthor = "Yes"'>
<xsl:variable name='aiid' select='.'/>
<acti:contributionAreaOf
 rdf:resource="{concat($g_instance,$aiid)}"/>
</xsl:if>
</xsl:for-each>
</rdf:Description>

</xsl:template>

<!-- ================================== -->
<xsl:template match='aiis:IMPACT_STMT_LIST'/>

<xsl:template match='aiis:ALT_SRC_IMPACT_STMT_ID'/>

<xsl:template match='aiis:IMPACT_STMT_ID'/>


<!-- =================================================== -->

<!-- =================================================== -->


<!-- ================================== -->


<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
