<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs='http://www.w3.org/2001/XMLSchema'
	xmlns:core="http://vivoweb.org/ontology/core#"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        xmlns:bibo="http://purl.org/ontology/bibo/"
        xmlns:foaf="http://xmlns.com/foaf/0.1/"
        xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
	xmlns:aiah="http://vivoweb.org/activity-insight"
	xmlns:acti="http://vivoweb.org/activity-insight#"
        xmlns="http://vivoweb.org/activity-insight"
xmlns:dm="http://www.digitalmeasures.com/schema/data"	
	xmlns:vfx='http://vivoweb.org/ext/functions'
	exclude-result-prefixes='xs vfx'
	>

<xsl:param name='unoMapFile'  required='yes'/>


<xsl:output method='xml' indent='yes'/>
<xsl:strip-space elements="*"/>

<xsl:include href='commonvars.xsl'/>

<xsl:variable name='unomap'
	select="document($unoMapFile)/Mapping"/>



<xsl:template match='/aiah:AWARD_LIST'>
<rdf:RDF>
<xsl:for-each select='aiah:AWARD'>
<!-- award processing -->
<rdf:Description rdf:about="{concat($g_instance,'AI-',@isid)}" >
<rdf:type rdf:resource='http://vivoweb.org/ontology/core#AwardOrHonor'/>
<rdf:type rdf:resource='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>
<rdfs:label>
<xsl:value-of select="vfx:trim(aiah:award_name)"/>
</rdfs:label>
<core:year><xsl:value-of select="aiah:year"/></core:year>
</rdf:Description>

</xsl:for-each>

</rdf:RDF>
</xsl:template>

<!-- ================================== -->


<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
