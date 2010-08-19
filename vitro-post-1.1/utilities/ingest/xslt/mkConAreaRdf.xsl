<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs='http://www.w3.org/2001/XMLSchema'
	xmlns:core="http://vivoweb.org/ontology/core#"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        xmlns:bibo="http://purl.org/ontology/bibo/"
        xmlns:foaf="http://xmlns.com/foaf/0.1/"
        xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
	xmlns:aiis="http://vivoweb.org/activity-insight"
	xmlns:acti="http://vivoweb.org/activity-insight#"
        xmlns="http://vivoweb.org/activity-insight"
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


<xsl:for-each select='aiis:IMPACT_STMTS_BY_CONAREA'>

<!-- create a core:ContributionArea for this contribution area
OR use an old one -->


<xsl:variable name='ctr'  select='@counter'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>
<!--xsl:comment>
<xsl:value-of select='$ctr'/> - <xsl:value-of select='$uno'/>
</xsl:comment -->

<!-- =================================================== -->
<!-- Declare a acti:ContributionArea (use extant con area if it exists) -->

<xsl:variable name='knownUri' 
select='vfx:knownCaUri(aiis:CONAREA_NAME, $extantConAreas)'/>

<xsl:variable name='cauri' 
select="if($knownUri != '') then $knownUri else 
concat($g_instance,$uno)"/>



<xsl:if test='$knownUri = ""'>
<rdf:Description rdf:about="{$cauri}">

<rdf:type rdf:resource='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>

<rdf:type rdf:resource='http://vivoweb.org/ontology/core#ContributionArea'/>

<rdfs:label>
<xsl:value-of select='vfx:trim(aiis:CONAREA_NAME)'/>
</rdfs:label>

<core:description>
<xsl:value-of select='vfx:trim(aiis:CONAREA_NAME)'/>
</core:description>


</rdf:Description>
</xsl:if>

<!-- =================================================== -->
<!-- now process the impact stmts attributed to this conarea -->

<xsl:call-template name='process-conarea'>

<xsl:with-param name='isbyca' select='aiis:IMPACT_STMT_ID_LIST'/>
<xsl:with-param name='caref' select="$cauri"/>
</xsl:call-template>

</xsl:for-each>

<!-- =================================================== 
 at this point we re-run part of the last for loop to get a new list of
 geo locs
 and their uri's to save in the extant geo locs Out xml file
-->
<xsl:result-document href='{$extConAreasOut}'>
<xsl:element name='ExtantConAreas' namespace=''>
<xsl:for-each select='aiis:IMPACT_STMTS_BY_CONAREA'>

<xsl:variable name='ctr'  select='@counter'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>
<xsl:variable name='knownUri' 
select='vfx:knownCaUri(aiis:CONAREA_NAME, $extantConAreas)'/>

<xsl:variable name='cauri' 
select="if($knownUri != '') then $knownUri else 
concat($g_instance,$uno)"/>


<xsl:element name='conarea' namespace=''>

<xsl:element name='uri' namespace=''>
<xsl:value-of select='$cauri'/>
</xsl:element>

<xsl:element name='title' namespace=''>
<xsl:value-of select='aiis:CONAREA_NAME'/>
</xsl:element>

</xsl:element>


</xsl:for-each>
</xsl:element>
</xsl:result-document>

</rdf:RDF>
</xsl:template>

<!-- =================================================== -->
<!-- =================================================== -->
<xsl:template name='process-conarea'>
<xsl:param name='isbyca'/>
<xsl:param name='caref'/>

<xsl:for-each select='$isbyca/aiis:IMPACT_STMT_ID'>
<xsl:variable name='aiid' select='.'/>

<!-- =================================================== -->
<!-- Declare property mapping acti:ImpactProject to 
acti:ContributionArea -->

<rdf:Description rdf:about="{concat($g_instance,$aiid)}" >

<acti:hasContributionArea rdf:resource="{$caref}"/>

</rdf:Description>
</xsl:for-each>

<!-- =================================================== -->

<rdf:Description rdf:about="{$caref}">

<xsl:for-each select='$isbyca/aiis:IMPACT_STMT_ID'>
<xsl:variable name='aiid' select='.'/>
<acti:contributionAreaOf
 rdf:resource="{concat($g_instance,$aiid)}"/>

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
