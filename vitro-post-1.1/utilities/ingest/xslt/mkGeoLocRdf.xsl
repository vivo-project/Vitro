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
<xsl:variable name='rawNewGeos'>
<xsl:element name='ExtantGeoLocs' inherit-namespaces='no'>
<xsl:for-each select='aiis:IMPACT_STMTS_BY_GEO_PLACE'>

<xsl:variable name='geo' select='vfx:simple-trim(aiis:GEO_PLACE_NAME)'/>
<xsl:if test='$geo != ""'>

<xsl:variable name='ctr'  select='@counter'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>
<xsl:variable name='knownUri' 
	select='vfx:knownGeoUri($geo, $extantGeos)'/>
<xsl:variable name='geouri' 
	select="if($knownUri != '') then 
		$knownUri else concat($g_instance,$uno)"/>

<xsl:if test='$knownUri= ""'>
<xsl:element name='geo' namespace=''>

<xsl:element name='uri' namespace=''>
<xsl:value-of select='concat("NEW-",$geouri)'/>
</xsl:element>

<xsl:element name='title' namespace=''>
<xsl:value-of select='$geo'/>
</xsl:element>

</xsl:element>
</xsl:if>

</xsl:if>
</xsl:for-each>
</xsl:element>
</xsl:variable>

<xsl:variable name='uniqueNewGeos'>
<xsl:call-template name='NewGeos'>
<xsl:with-param name='knowns' select='$rawNewGeos/ExtantGeoLocs'/>
</xsl:call-template>
</xsl:variable>


<xsl:for-each select='aiis:IMPACT_STMTS_BY_GEO_PLACE'>

<!-- create a core:GeographicLocation for this geo location
OR use an old one -->

<xsl:variable name='geo' select='vfx:simple-trim(aiis:GEO_PLACE_NAME)'/>
<xsl:variable name='ctr'  select='@counter'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>
<!--xsl:comment>
<xsl:value-of select='$ctr'/> - <xsl:value-of select='$uno'/>
</xsl:comment -->

<!-- =================================================== -->
<!-- Declare a core:GeographicLocation (use extant geo loc if it exists) -->
<xsl:if test='$geo != ""'>
<xsl:variable name='knownUri' 
	select='vfx:knownGeoUri($geo, 
				$extantGeos union 
					$rawNewGeos/ExtantGeoLocs)'/>

<xsl:variable name='geouri' 
	select='if(starts-with($knownUri,"NEW-")) then 
		substring-after($knownUri,"NEW-") else 
		$knownUri'/>
<!--
<xsl:comment>
<xsl:value-of select='$geouri'/> - 
<xsl:value-of select='$knownUri'/></xsl:comment>
-->

<rdf:Description rdf:about="{$geouri}">
<xsl:if test='starts-with($knownUri,"NEW-")'>
<rdf:type rdf:resource=
	'http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>

<rdf:type rdf:resource=
	'http://vivoweb.org/ontology/core#GeographicLocation'/>

<rdfs:label>
<xsl:value-of select='$geo'/>
</rdfs:label>

<core:description>
<xsl:value-of select='$geo'/>
</core:description>
</xsl:if>

<acti:involvedAreaIlk>
<xsl:value-of select='aiis:GEO_PLACE_NAME/@ilk'/>
</acti:involvedAreaIlk>


</rdf:Description>


<!-- =================================================== -->
<!-- now process the impact stmts attributed to this geo loc -->

<xsl:call-template name='process-geo-loc'>
<xsl:with-param name='name' select='aiis:GEO_PLACE_NAME'/>
<xsl:with-param name='ilk' select='aiis:GEO_PLACE_NAME/@ilk'/>
<xsl:with-param name='isbygeo' select='aiis:IMPACT_STMT_ID_LIST'/>
<xsl:with-param name='georef' select="$geouri"/>
</xsl:call-template>

</xsl:if>

</xsl:for-each>

<!-- =================================================== 
 at this point we save new geos in the extant geo locs Out xml file
-->
<xsl:result-document href='{$extGeoOut}'>
<xsl:element name='ExtantGeoLocs' namespace=''>
<xsl:value-of select='$NL'/>
<xsl:comment>
<xsl:value-of select='count($uniqueNewGeos//geo)'/>
</xsl:comment>
<xsl:value-of select='$NL'/>
<xsl:for-each select='$uniqueNewGeos//geo'>

<xsl:element name='geo' namespace=''>

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
<xsl:template name='process-geo-loc'>
<xsl:param name='name'/>
<xsl:param name='ilk'/>
<xsl:param name='isbygeo'/>
<xsl:param name='georef'/>

<xsl:for-each select='$isbygeo/aiis:IMPACT_STMT_ID'>
<xsl:if test='./@hasTitle = "Yes" and ./@hasGoodAuthor = "Yes"'>
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
</xsl:if>
</xsl:for-each>

<!-- =================================================== -->

<rdf:Description rdf:about="{$georef}">

<xsl:for-each select='$isbygeo/aiis:IMPACT_STMT_ID'>
<xsl:if test='./@hasTitle = "Yes" and ./@hasGoodAuthor = "Yes"'>
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
