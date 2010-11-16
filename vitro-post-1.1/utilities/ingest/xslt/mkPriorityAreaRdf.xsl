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
<xsl:variable name='rawNewEmphs'>
<xsl:element name='ExtantEmphs' inherit-namespaces='no'>
<xsl:for-each select='aiis:IMPACT_STMTS_BY_EMPHASIS'>

<xsl:variable name='name' select='vfx:simple-trim(aiis:EMPHASIS_NAME)'/>
<xsl:if test='$name != ""'>
<xsl:variable name='ctr'  select='@counter'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>

<xsl:variable name='knownUri' 
	select='vfx:knownEmphUri($name, $extantEmphs)'/>

<xsl:variable name='emphuri' 
	select="if($knownUri != '') then 
		   $knownUri else 
		   concat($g_instance,$uno)"/>

<xsl:if test='$knownUri= ""'>
<xsl:element name='emph' namespace=''>

<xsl:element name='uri' namespace=''>
<xsl:value-of select='concat("NEW-",$emphuri)'/>
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

<xsl:variable name='uniqueNewEmphs'>
<xsl:call-template name='NewEmphs'>
<xsl:with-param name='knowns' select='$rawNewEmphs/ExtantEmphs'/>
</xsl:call-template>
</xsl:variable>



<xsl:for-each select='aiis:IMPACT_STMTS_BY_EMPHASIS'>

<!-- create an acti:PriorityArea -->

<xsl:variable name='name' select='vfx:simple-trim(aiis:EMPHASIS_NAME)'/>

<xsl:variable name='ctr'  select='@counter'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>
<!--xsl:comment>
<xsl:value-of select='$ctr'/> - <xsl:value-of select='$uno'/>
</xsl:comment -->

<!-- =================================================== -->
<!-- Declare a acti:PriorityArea (use extant PA if it exists) -->
<xsl:if test='$name'>
<xsl:variable name='knownUri' 
	select='vfx:knownEmphUri($name, 
				$extantEmphs union
				$rawNewEmphs/ExtantEmphs)'/>

<xsl:variable name='emphuri'
	select='if(starts-with($knownUri,"NEW-")) then 
		substring-after($knownUri,"NEW-") else 
		$knownUri'/>

<xsl:comment><xsl:value-of select='$emphuri'/> - 
<xsl:value-of select='$knownUri'/></xsl:comment>


<rdf:Description rdf:about="{$emphuri}">
<rdf:type rdf:resource=
	'http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>
<rdf:type rdf:resource=
	'http://vivoweb.org/ontology/activity-insight#PriorityArea'/>

<rdfs:label>
<xsl:value-of select='$name'/>
</rdfs:label>
<core:description>
<xsl:value-of select='$name'/>
</core:description>
</rdf:Description>


<!-- =================================================== -->
<!-- now process the impact stmts attributed to this priority area -->

<xsl:call-template name='process-emphasis'>
<xsl:with-param name='isbyemph' select='aiis:IMPACT_STMT_ID_LIST'/>
<xsl:with-param name='emphref' select="$emphuri"/>
</xsl:call-template>
</xsl:if>

</xsl:for-each>

<!-- =================================================== 

-->
<xsl:result-document href='{$extEmphOut}'>
<xsl:element name='ExtantEmphs' namespace=''>
<xsl:value-of select='$NL'/>
<xsl:comment>
<xsl:value-of select='count($uniqueNewEmphs//emph)'/>
</xsl:comment>
<xsl:value-of select='$NL'/>
<xsl:for-each select='$uniqueNewEmphs//emph'>

<xsl:element name='emph' namespace=''>

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
<xsl:template name='process-emphasis'>
<xsl:param name='isbyemph'/>
<xsl:param name='emphref'/>

<xsl:for-each select='$isbyemph/aiis:IMPACT_STMT_ID'>
<xsl:if test='./@hasTitle = "Yes" and ./@hasGoodAuthor = "Yes"'>
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
