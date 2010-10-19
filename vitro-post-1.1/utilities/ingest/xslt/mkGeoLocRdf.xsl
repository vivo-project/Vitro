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
        xmlns="http://vivoweb.org/ontology/activity-insight"
	xmlns:dm="http://www.digitalmeasures.com/schema/data"	
	xmlns:vfx='http://vivoweb.org/ext/functions'
	exclude-result-prefixes='xs vfx'
	>


<xsl:param name='unoMapFile'  required='yes'/>
<xsl:param name='aiisXmlPath' required='yes'/>
<xsl:param name='aiisPrefix' required='yes'/>
<xsl:param name='extGeoIn' required='yes'/>
<xsl:param name='extGeoOut' required='yes'/>

<xsl:output method='xml' indent='yes'/>
<xsl:strip-space elements="*"/>

<xsl:include href='commonvars.xsl'/>


<xsl:variable name='unomap'
	select="document($unoMapFile)/Mapping"/>

<xsl:variable name='extantGeos'
	select="document($extGeoIn)/ExtantGeoLocs"/>
<!-- ================================== -->
<xsl:template match='/aiis:GEO_LIST'>
<rdf:RDF>


<xsl:for-each select='aiis:IMPACT_STMTS_BY_GEO_PLACE'>

<!-- create a core:GeographicLocation for this geo location
OR use an old one -->


<xsl:variable name='ctr'  select='@counter'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>
<!--xsl:comment>
<xsl:value-of select='$ctr'/> - <xsl:value-of select='$uno'/>
</xsl:comment -->

<!-- =================================================== -->
<!-- Declare a core:GeographicLocation (use extant geo loc if it exists) -->

<xsl:variable name='knownUri' 
	select='vfx:knownGeoUri(aiis:GEO_PLACE_NAME, $extantGeos)'/>

<xsl:variable name='geouri' 
	select="if($knownUri != '') then 
		$knownUri else concat($g_instance,$uno)"/>

<!-- xsl:comment>
<xsl:value-of select='$geouri'/> - 
<xsl:value-of select='$knownUri'/></xsl:comment -->

<xsl:if test='$knownUri = ""'>
<rdf:Description rdf:about="{$geouri}">

<rdf:type rdf:resource=
	'http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>

<rdf:type rdf:resource=
	'http://vivoweb.org/ontology/core#GeographicLocation'/>

<rdfs:label>
<xsl:value-of select='vfx:trim(aiis:GEO_PLACE_NAME)'/>
</rdfs:label>

<core:description>
<xsl:value-of select='vfx:trim(aiis:GEO_PLACE_NAME)'/>
</core:description>

<acti:involvedAreaIlk>
<xsl:value-of select='aiis:GEO_PLACE_NAME/@ilk'/>
</acti:involvedAreaIlk>

</rdf:Description>
</xsl:if>

<!-- =================================================== -->
<!-- now process the impact stmts attributed to this geo loc -->

<xsl:call-template name='process-geo-loc'>
<xsl:with-param name='name' select='aiis:GEO_PLACE_NAME'/>
<xsl:with-param name='ilk' select='aiis:GEO_PLACE_NAME/@ilk'/>
<xsl:with-param name='isbygeo' select='aiis:IMPACT_STMT_ID_LIST'/>
<xsl:with-param name='georef' select="$geouri"/>
</xsl:call-template>

</xsl:for-each>

<!-- =================================================== 
 at this point we re-run part of the last for loop to get a new list of
 geo locs
 and their uri's to save in the extant geo locs Out xml file
-->
<xsl:result-document href='{$extGeoOut}'>
<xsl:element name='ExtantGeoLocs' namespace=''>
<xsl:for-each select='aiis:IMPACT_STMTS_BY_GEO_PLACE'>

<xsl:variable name='ctr'  select='@counter'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>
<xsl:variable name='knownUri' 
select='vfx:knownGeoUri(aiis:GEO_PLACE_NAME, $extantGeos)'/>

<xsl:variable name='geouri' 
select="if($knownUri != '') then 
	$knownUri else concat($g_instance,$uno)"/>


<xsl:element name='geo' namespace=''>

<xsl:element name='uri' namespace=''>
<xsl:value-of select='$geouri'/>
</xsl:element>

<xsl:element name='title' namespace=''>
<xsl:value-of select='aiis:GEO_PLACE_NAME'/>
</xsl:element>

</xsl:element>


</xsl:for-each>
</xsl:element>
</xsl:result-document>

</rdf:RDF>
</xsl:template>

<!-- =================================================== -->
<!-- =================================================== -->
<xsl:template name='process-geo-loc'>
<xsl:param name='name'/>
<xsl:param name='ilk'/>
<xsl:param name='isbygeo'/>
<xsl:param name='georef'/>

<xsl:for-each select='$isbygeo/aiis:IMPACT_STMT_ID'>
<xsl:variable name='aiid' select='.'/>

<!-- =================================================== -->
<!-- Declare property mapping acti:ImpactProject to 
core:GeographicLocation -->

<rdf:Description rdf:about="{concat($g_instance,$aiid)}" >

<xsl:choose>
<xsl:when test=' $ilk = "COUNTRY" and $name != "United States"'>
<!-- 1 -->
<core:internationalGeographicFocus
	rdf:resource="{$georef}"/>
</xsl:when>

<xsl:otherwise>
<!-- 3 -->
<core:domesticGeographicFocus
	rdf:resource="{$georef}"/>
</xsl:otherwise>
</xsl:choose>

</rdf:Description>
</xsl:for-each>

<!-- =================================================== -->

<rdf:Description rdf:about="{$georef}">

<xsl:for-each select='$isbygeo/aiis:IMPACT_STMT_ID'>

<xsl:variable name='aiid' select='.'/>
<xsl:choose>
<xsl:when test=' $ilk = "COUNTRY"  and $name != "United States" '>
<!-- 2 -->
<core:internationalGeographicFocusOf 
	rdf:resource="{concat($g_instance,$aiid)}"/>
<rdf:type rdf:resource='http://vivoweb.org/ontology/core#Country'/>
</xsl:when>
<xsl:when test=' $ilk = "STATE" '>
<!-- 4 -->
<core:domesticGeographicFocusOf 
	rdf:resource="{concat($g_instance,$aiid)}"/>
<rdf:type 
	rdf:resource='http://vivoweb.org/ontology/core#StateOrProvence'/>
</xsl:when>
<xsl:otherwise>
<!-- 4 -->
<core:domesticGeographicFocusOf 
	rdf:resource="{concat($g_instance,$aiid)}"/>
<rdf:type 
	rdf:resource='http://vivoweb.org/ontology/core#County'/>
</xsl:otherwise>
</xsl:choose>
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
