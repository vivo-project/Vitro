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

<!-- Emphasis is more meaningful then 'priority area' -->

<xsl:param name='unoMapFile'  required='yes'/>
<xsl:param name='aiisXmlPath' required='yes'/>
<xsl:param name='aiisPrefix' required='yes'/>
<xsl:param name='extEmphIn' required='yes'/>
<xsl:param name='extEmphOut' required='yes'/>

<xsl:output method='xml' indent='yes'/>
<xsl:strip-space elements="*"/>

<xsl:include href='commonvars.xsl'/>

<xsl:variable name='unomap'
	select="document($unoMapFile)/Mapping"/>

<xsl:variable name='extantEmphs'
	select="document($extEmphIn)/ExtantEmphs"/>
<!-- ================================== -->
<xsl:template match='/aiis:EMPHASIS_LIST'>
<rdf:RDF>


<xsl:for-each select='aiis:IMPACT_STMTS_BY_EMPHASIS'>

<!-- create an acti:PriorityArea -->


<xsl:variable name='ctr'  select='@counter'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>
<!--xsl:comment>
<xsl:value-of select='$ctr'/> - <xsl:value-of select='$uno'/>
</xsl:comment -->

<!-- =================================================== -->
<!-- Declare a acti:PriorityArea (use extant PA if it exists) -->

<xsl:variable name='knownUri' select='vfx:knownEmphUri(aiis:EMPHASIS_NAME, $extantEmphs)'/>

<xsl:variable name='emphuri' select="if($knownUri != '') then $knownUri else concat($g_instance,$uno)"/>

<!-- xsl:comment><xsl:value-of select='$emphuri'/> - <xsl:value-of select='$knownUri'/></xsl:comment -->

<xsl:if test='$knownUri = ""'>
<rdf:Description rdf:about="{$emphuri}">
<rdf:type rdf:resource='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>
<rdf:type rdf:resource='http://vivoweb.org/activity-insight#PriorityArea'/>

<rdfs:label>
<xsl:value-of select='vfx:trim(aiis:EMPHASIS_NAME)'/>
</rdfs:label>
<core:description>
<xsl:value-of select='vfx:trim(aiis:EMPHASIS_NAME)'/>
</core:description>
</rdf:Description>
</xsl:if>

<!-- =================================================== -->
<!-- now process the impact stmts attributed to this priority area -->

<xsl:call-template name='process-emphasis'>
<xsl:with-param name='isbyemph' select='aiis:IMPACT_STMT_ID_LIST'/>
<xsl:with-param name='emphref' select="$emphuri"/>
</xsl:call-template>

</xsl:for-each>

<!-- =================================================== 
 at this point we re-run part of the last for loop to make a new list of
priority area
 and their uri's to save in the extant priority area Out xml file
-->
<xsl:result-document href='{$extEmphOut}'>
<xsl:element name='ExtantEmphs' namespace=''>
<xsl:for-each select='aiis:IMPACT_STMTS_BY_EMPHASIS'>

<xsl:variable name='ctr'  select='@counter'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>
<xsl:variable name='knownUri' select='vfx:knownEmphUri(aiis:EMPHASIS_NAME, $extantEmphs)'/>

<xsl:variable name='emphuri' select="if($knownUri != '') then $knownUri else concat($g_instance,$uno)"/>


<xsl:element name='emph' namespace=''>

<xsl:element name='uri' namespace=''>
<xsl:value-of select='$emphuri'/>
</xsl:element>

<xsl:element name='name' namespace=''>
<xsl:value-of select='aiis:EMPHASIS_NAME'/>
</xsl:element>

</xsl:element>


</xsl:for-each>
</xsl:element>
</xsl:result-document>

</rdf:RDF>
</xsl:template>

<!-- =================================================== -->
<!-- =================================================== -->
<xsl:template name='process-emphasis'>
<xsl:param name='isbyemph'/>
<xsl:param name='emphref'/>

<xsl:for-each select='$isbyemph/aiis:IMPACT_STMT_ID'>
<xsl:variable name='aiid' select='.'/>

<!-- =================================================== -->
<!-- Declare property mapping acti:ImpactProject to acti:PriorityArea
 -->
<!-- 5 -->
<rdf:Description rdf:about="{concat($g_instance,$aiid)}" >
<acti:hasPriorityArea
	rdf:resource="{$emphref}"/>
</rdf:Description>
<rdf:Description rdf:about="{$emphref}">
<acti:priorityAreaOf
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
