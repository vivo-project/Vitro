<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs='http://www.w3.org/2001/XMLSchema'
	xmlns:core="http://vivoweb.org/ontology/core#"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        xmlns:bibo="http://purl.org/ontology/bibo/"
        xmlns:foaf="http://xmlns.com/foaf/0.1/"
        xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
	xmlns:acti="http://vivoweb.org/ontology/activity-insight#"
	xmlns:vfx='http://vivoweb.org/ext/functions'
	exclude-result-prefixes='xs vfx'
	>

<!-- Emphasis is more meaningful then 'priority area' -->

<xsl:output method='xml' indent='yes'/>
<xsl:strip-space elements="*"/>

<xsl:include href='commonvars.xsl'/>

<!-- ================================== -->
<xsl:template match='/ExtantEmphs'>
<rdf:RDF>

<xsl:for-each select='emph'>

<!-- create an acti:PriorityArea -->
<xsl:variable name='name' select='vfx:simple-trim(name)'/>

<xsl:if test='$name != ""'>
<rdf:Description rdf:about="{./uri}">
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
</xsl:if>

</xsl:for-each>

</rdf:RDF>
<xsl:value-of select='$NL'/>
</xsl:template>

<!-- ================================== -->

<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
