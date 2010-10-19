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
	xmlns:ai="http://vivoweb.org/ontology/activity-insight#"
        xmlns="http://vivoweb.org/ontology/activity-insight#"
	xmlns:dm="http://www.digitalmeasures.com/schema/data"
	exclude-result-prefixes='xs'>

<xsl:output method='xml' indent='yes'/>
<xsl:strip-space elements="*"/>
<xsl:variable name='NL'>
<xsl:text>
</xsl:text>
</xsl:variable>

<xsl:template match='node()|@*'>
<xsl:copy>
<xsl:apply-templates select='node()|@*'/>
</xsl:copy>
</xsl:template>

<xsl:template match='rdf:RDF'>
<rdf:RDF>
<xsl:for-each select='rdf:Description'>
<xsl:sort select='./@rdf:about'/>
<xsl:copy-of select='.' copy-namespaces='no'/><xsl:value-of select='$NL'/>
</xsl:for-each>
</rdf:RDF>
</xsl:template>

</xsl:stylesheet>
