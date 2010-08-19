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


<xsl:for-each select='aiis:IMPACT_STMTS_BY_USDA_AREA'>

<!-- create an acti:USDA_Area -->

<xsl:variable name='name' select='vfx:trim(aiis:USDA_AREA_NAME)'/>
<xsl:variable name='ilk' select='@ilk'/>
<xsl:variable name='ctr'  select='@counter'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>
<!--xsl:comment>
<xsl:value-of select='$ctr'/> - <xsl:value-of select='$uno'/>
</xsl:comment -->

<!-- =================================================== -->
<!-- Declare a acti:USDA_Area (use extant USDA Area if it exists) -->
<!-- but do not create one if in the 'OTHER' case -->
<xsl:variable name='knownUri' select='vfx:knownUaUri(aiis:USDA_AREA_NAME, $extantUsdaAreas)'/>

<xsl:variable name='uauri' select="if($knownUri != '') then $knownUri else concat($g_instance,$uno)"/>

<xsl:if test='$knownUri = "" and not(contains($ilk,"OTHER"))'>
<rdf:Description rdf:about="{$uauri}">
<rdf:type rdf:resource='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>
<rdf:type rdf:resource='http://vivoweb.org/activity-insight#USDA_Area'/>

<rdfs:label>
<xsl:value-of select='vfx:trim(aiis:USDA_AREA_NAME)'/>
</rdfs:label>
<core:description>
<xsl:value-of select='vfx:trim(aiis:USDA_AREA_NAME)'/>
</core:description>
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

</xsl:for-each>

<!-- =================================================== 
 at this point we re-run part of the last for loop to make a new list of usda area
 and their uri's to save in the extant priority area Out xml file
-->
<xsl:result-document href='{$extUsdaAreasOut}'>
<xsl:element name='ExtantUsdaArea' namespace=''>
<xsl:for-each select='aiis:IMPACT_STMTS_BY_USDA_AREA'>
<xsl:variable name='ilk' select='@ilk'/>
<xsl:variable name='ctr'  select='@counter'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>
<xsl:variable name='knownUri' select='vfx:knownUaUri(aiis:USDA_AREA_NAME, $extantUsdaAreas)'/>

<xsl:variable name='uauri' select="if($knownUri != '') then $knownUri else concat($g_instance,$uno)"/>

<xsl:if test='not(contains($ilk,"OTHER"))'>
<xsl:element name='area' namespace=''>

<xsl:element name='uri' namespace=''>
<xsl:value-of select='$uauri'/>
</xsl:element>

<xsl:element name='name' namespace=''>
<xsl:value-of select='aiis:USDA_AREA_NAME'/>
</xsl:element>

</xsl:element>
</xsl:if>

</xsl:for-each>
</xsl:element>
</xsl:result-document>

</rdf:RDF>
</xsl:template>

<!-- =================================================== -->
<!-- =================================================== -->
<xsl:template name='process-usda-area'>
<xsl:param name='isbyua'/>
<xsl:param name='uaref'/>
<xsl:param name='ilk'/>
<xsl:param name='name'/>
<xsl:for-each select='$isbyua/aiis:IMPACT_STMT_ID'>
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
