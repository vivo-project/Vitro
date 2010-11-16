<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs='http://www.w3.org/2001/XMLSchema'
	xmlns:core="http://vivoweb.org/ontology/core#"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        xmlns:bibo="http://purl.org/ontology/bibo/"
        xmlns:foaf="http://xmlns.com/foaf/0.1/"
        xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
	xmlns:aiic="http://vivoweb.org/ontology/activity-insight"
	xmlns:acti="http://vivoweb.org/ontology/activity-insight#"
	xmlns:dm="http://www.digitalmeasures.com/schema/data"
	xmlns:vfx='http://vivoweb.org/ext/functions'	
	exclude-result-prefixes='vfx xs'
	>

<xsl:output method='xml' indent='yes'/>
<xsl:strip-space elements="*"/>

<xsl:include href='commonvars.xsl'/>

<xsl:template match='/ExtantJournals'>
<rdf:RDF>

<xsl:for-each select='journal'>
<!-- create a bibo:Journal for this journal -->
<xsl:variable name='name' 
	select='vfx:simple-trim(name)'/>
<xsl:if test='$name != ""'>
<rdf:Description rdf:about="{./uri}">
<rdf:type rdf:resource=
	'http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>
<rdf:type rdf:resource='http://purl.org/ontology/bibo/Journal'/>
<rdfs:label>
<xsl:value-of select='$name'/>
</rdfs:label>
<core:title><xsl:value-of select='$name'/></core:title>
</rdf:Description>
</xsl:if>
</xsl:for-each>

</rdf:RDF>
<xsl:value-of select='$NL'/>
</xsl:template>

<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
