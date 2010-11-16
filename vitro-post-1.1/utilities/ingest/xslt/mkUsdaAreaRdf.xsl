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
<xsl:param name='extUsdaAreasIn' required='yes'/>
<xsl:param name='extUsdaAreasOut' required='yes'/>

<xsl:output method='xml' indent='yes'/>
<xsl:strip-space elements="*"/>

<xsl:include href='commonvars.xsl' />

<xsl:variable name='unomap'
	select="document($unoMapFile)/Mapping"/>

<xsl:variable name='extantUsdaAreas'
	select="document($extUsdaAreasIn)/ExtantUsdaAreas"/>
<!-- ================================== -->
<xsl:template match='/aiis:USDA_LIST'>
<rdf:RDF>

<xsl:variable name='rawNewUsdaAreas'>
<xsl:element name='ExtantUsdaAreas' inherit-namespaces='no'>

<xsl:for-each select='aiis:IMPACT_STMTS_BY_USDA_AREA'>

<xsl:variable name='name' select='vfx:simple-trim(aiis:USDA_AREA_NAME)'/>
<xsl:if test='$name != ""'>

<xsl:variable name='ilk' select='@ilk'/>
<xsl:variable name='ctr'  select='@counter'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>

<xsl:variable name='knownUri'
select='vfx:knownUaUri($name, $extantUsdaAreas)'/>
<xsl:variable name='uauri'
        select="if($knownUri != '') then
                        $knownUri else
                        concat($g_instance,$uno)"/>

<xsl:if test='$knownUri= ""'>
<xsl:element name='area' namespace=''>

<xsl:element name='uri' namespace=''>
<xsl:value-of select='concat("NEW-",$uauri)'/>
</xsl:element>

<xsl:element name='name' namespace=''>
<xsl:value-of select='$name'/>
</xsl:element>

</xsl:element>
</xsl:if>
</xsl:if>
</xsl:for-each>
</xsl:element>
</xsl:variable>


<xsl:variable name='uniqueNewUsdaAreas'>
<xsl:call-template name='NewUsdaAreas'>
<xsl:with-param name='knowns' select='$rawNewUsdaAreas/ExtantUsdaAreas'/>
</xsl:call-template>
</xsl:variable>


<xsl:for-each select='aiis:IMPACT_STMTS_BY_USDA_AREA'>

<!-- create an acti:USDA_Area -->

<xsl:variable name='name' select='vfx:simple-trim(aiis:USDA_AREA_NAME)'/>
<xsl:if test='$name != ""'>

<xsl:variable name='ilk' select='@ilk'/>
<xsl:variable name='ctr'  select='@counter'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>
<!--xsl:comment>
<xsl:value-of select='$ctr'/> - <xsl:value-of select='$uno'/>
</xsl:comment -->

<!-- =================================================== -->
<!-- Declare a acti:USDA_Area (use extant USDA Area if it exists) -->
<!-- but do not create one if in the 'OTHER' case -->
<xsl:variable name='knownUri' 
	select='vfx:knownUaUri($name,
                               $extantUsdaAreas union
			       $rawNewUsdaAreas/ExtantUsdaAreas)'/>

<xsl:variable name='uauri' 
        select='if(starts-with($knownUri,"NEW-")) then
                substring-after($knownUri,"NEW-") else
                $knownUri'/>

<xsl:if test='not(contains($ilk,"OTHER"))'>
<rdf:Description rdf:about="{$uauri}">

<xsl:if test='starts-with($knownUri,"NEW-")'>
<rdf:type rdf:resource=
	'http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>
<rdf:type rdf:resource=
	'http://vivoweb.org/ontology/activity-insight#USDA_Area'/>

<rdfs:label>
<xsl:value-of select='vfx:simple-trim(aiis:USDA_AREA_NAME)'/>
</rdfs:label>
<core:description>
<xsl:value-of select='vfx:simple-trim(aiis:USDA_AREA_NAME)'/>
</core:description>
</xsl:if>

<acti:usdaAreaIlk><xsl:value-of select='$ilk'/> </acti:usdaAreaIlk>

</rdf:Description>
</xsl:if>

<!-- =================================================== -->
<!-- now process the impact stmts attributed to this usda area -->

<xsl:call-template name='process-usda-area'>
<xsl:with-param name='isbyua' select='aiis:IMPACT_STMT_ID_LIST'/>
<xsl:with-param name='uaref' select="$uauri"/>
<xsl:with-param name='ilk' select='$ilk'/>
<xsl:with-param name='name' select='$name'/>
</xsl:call-template>

</xsl:if>
</xsl:for-each>

<!-- =================================================== 
-->
<xsl:result-document href='{$extUsdaAreasOut}'>
<xsl:element name='ExtantUsdaAreas' namespace=''>
<xsl:for-each select='$uniqueNewUsdaAreas//area'>
<xsl:element name='area' namespace=''>

<xsl:element name='uri' namespace=''>
<xsl:value-of select=
        'if(starts-with(uri,"NEW-")) then
                substring-after(uri,"NEW-") else uri'/>
</xsl:element>

<xsl:element name='name' namespace=''>
<xsl:value-of select='name'/>
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
<xsl:template name='process-usda-area'>
<xsl:param name='isbyua'/>
<xsl:param name='uaref'/>
<xsl:param name='ilk'/>
<xsl:param name='name'/>
<xsl:for-each select='$isbyua/aiis:IMPACT_STMT_ID'>
<xsl:if test='./@hasTitle = "Yes" and ./@hasGoodAuthor = "Yes"'>
<xsl:variable name='aiid' select='.'/>

<!-- =================================================== -->
<!-- Declare property mapping acti:ImpactProject to acti:USDA_Area
 -->
<!-- 5 -->
<rdf:Description rdf:about="{concat($g_instance,$aiid)}" >
<!-- rdf:Description rdf:about="{concat($g_instance,$aiid)}" -->
<xsl:if test='not(contains($ilk,"OTHER"))'>
<acti:hasUSDAArea
	rdf:resource="{$uaref}"/>
</xsl:if>
<xsl:if test='contains($ilk,"OTHER")'>
<acti:usdaAreaOther><xsl:value-of select='$name'/></acti:usdaAreaOther>
</xsl:if>
</rdf:Description>


<rdf:Description rdf:about="{$uaref}">
<acti:USDAAreaOf
	rdf:resource="{concat($g_instance,$aiid)}"/>
</rdf:Description>
</xsl:if>
</xsl:for-each>

</xsl:template>

<!-- ================================== -->
<xsl:template match='aiis:IMPACT_STMT_ID_LIST'/>

<xsl:template match='aiis:ALT_SRC_IMPACT_STMT_ID'/>

<xsl:template match='aiis:IMPACT_STMT_ID'/>


<!-- =================================================== -->

<!-- =================================================== -->


<!-- ================================== -->


<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
