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

<xsl:output method='xml' indent='yes'/>
<xsl:strip-space elements="*"/>

<xsl:include href='commonvars.xsl'/>

<xsl:template match='/ExtantOrgs'>
<rdf:RDF>
<xsl:for-each select='org'>



<!-- =================================================== -->
<!-- Declare a foaf:Organization -->
<xsl:variable name='name' select='vfx:simple-trim(name)'/>
<xsl:variable name='sc' select='vfx:simple-trim(stateCountry)'/>

<xsl:if test='$name != ""'>

<rdf:Description rdf:about="{./uri}">
<rdf:type rdf:resource=
	'http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>
<rdf:type rdf:resource='http://xmlns.com/foaf/0.1/Organization'/>

<rdfs:label>
<xsl:value-of select='$name'/>
</rdfs:label>
<core:description>
<xsl:value-of select='$name'/><xsl:text>; </xsl:text>
<xsl:value-of select='$sc'/>
</core:description>
</rdf:Description>
<xsl:value-of select='$NL'/>
</xsl:if>


</rdf:RDF>
<xsl:value-of select='$NL'/>

</xsl:template>

<!-- =================================================== -->

<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
