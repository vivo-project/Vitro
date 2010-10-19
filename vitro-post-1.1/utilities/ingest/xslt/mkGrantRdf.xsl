<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs='http://www.w3.org/2001/XMLSchema'
	xmlns:core="http://vivoweb.org/ontology/core#"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        xmlns:bibo="http://purl.org/ontology/bibo/"
        xmlns:foaf="http://xmlns.com/foaf/0.1/"
        xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
	xmlns:aigrant="http://vivoweb.org/ontology/activity-insight"
	xmlns:acti="http://vivoweb.org/ontology/activity-insight#"
        xmlns="http://vivoweb.org/ontology/activity-insight"
	xmlns:dm="http://www.digitalmeasures.com/schema/data"	
	xmlns:vfx='http://vivoweb.org/ext/functions'
	exclude-result-prefixes='xs vfx'
	>

<xsl:param name='unoMapFile'  required='yes'/>
<xsl:param name='rawXmlPath' required='yes'/>

<xsl:output method='xml' indent='yes'/>
<xsl:strip-space elements="*"/>

<xsl:include href='commonvars.xsl'/>

<xsl:variable name='unomap'
	select="document($unoMapFile)/Mapping"/>


<xsl:template match='/aigrant:GRANT_LIST'>
<rdf:RDF>
<xsl:for-each select='aigrant:GRANT'>
<!-- grant processing -->

<rdf:Description rdf:about="{concat($g_instance,aigrant:GRANT_ID)}" >
<rdf:type rdf:resource='http://vivoweb.org/ontology/core#Grant'/>
<rdf:type rdf:resource='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>
<rdfs:label>
<xsl:value-of select="vfx:trim(aigrant:GRANT_TITLE)"/>
</rdfs:label>
<xsl:if test='@netid != ""'>
<xsl:variable name='grid' select='substring-after(aigrant:GRANT_ID,"AI-")'/>
<xsl:variable name='nidxml' select="concat($rawXmlPath,'/',@netid, '.xml')"/>
<xsl:variable name='gr' select='document($nidxml)//dm:CONGRANT[@id = $grid]' />

<acti:ospId>
<xsl:value-of select ='$gr/dm:OSP_ID'/>
</acti:ospId>
<acti:ospStart><xsl:value-of select ='$gr/dm:OSP_START'/></acti:ospStart>
<acti:ospEnd><xsl:value-of select ='$gr/dm:OSP_END'/></acti:ospEnd>
<acti:ospLink><xsl:value-of select ='$gr/dm:WEBSITE'/></acti:ospLink>
<acti:sponsorProjectId>
<xsl:value-of select ='$gr/dm:SPONSOR_ID'/>
</acti:sponsorProjectId>
<acti:sponsorFundSource>
<xsl:value-of select ='$gr/dm:SPONSOR_FUND_SOURCE'/>
</acti:sponsorFundSource>

<acti:status><xsl:value-of select ='$gr/dm:STATUS'/></acti:status>

<acti:abstract><xsl:value-of select ='$gr/dm:ABSTRACT'/></acti:abstract>
<acti:function><xsl:value-of select ='$gr/dm:FUNCTION'/></acti:function>
<acti:type><xsl:value-of select ='$gr/dm:TYPE'/></acti:type>
</xsl:if>
</rdf:Description>

</xsl:for-each>

</rdf:RDF>
</xsl:template>

<!-- ================================== -->


<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
