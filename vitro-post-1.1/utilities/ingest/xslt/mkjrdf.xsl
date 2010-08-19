<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs='http://www.w3.org/2001/XMLSchema'
	xmlns:core="http://vivoweb.org/ontology/core#"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        xmlns:bibo="http://purl.org/ontology/bibo/"
        xmlns:foaf="http://xmlns.com/foaf/0.1/"
        xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
	xmlns:aiic="http://vivoweb.org/activity-insight"
	xmlns:acti="http://vivoweb.org/activity-insight#"
        xmlns="http://vivoweb.org/activity-insight"
	xmlns:dm="http://www.digitalmeasures.com/schema/data"
	xmlns:vfx='http://vivoweb.org/ext/functions'	
	exclude-result-prefixes='vfx xs'
	>

<xsl:output method='xml' indent='yes'/>
<xsl:strip-space elements="*"/>


<xsl:param name='unoMapFile'  required='yes'/>
<xsl:param name='extJournalIn'  required='yes'/>
<xsl:param name='extJournalOut'  required='yes'/>

<xsl:include href='commonvars.xsl'/>

<xsl:variable name='unomap'
	select="document($unoMapFile)/Mapping"/>
<xsl:variable name='extantJournals'
	select="document($extJournalIn)/ExtantJournals"/>

<xsl:template match='/aiic:JOURNAL_LIST'>
<rdf:RDF>

<xsl:for-each select='aiic:ARTICLES_BY_JOURNAL'>
<!-- create a bibo:Journal for this journal -->
<xsl:variable name='ctr'  select='position()'/>

<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>
<xsl:variable name='juri' select="concat($g_instance,$uno)"/>
<rdf:Description rdf:about="{$juri}">
<rdf:type rdf:resource='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>
<rdf:type rdf:resource='http://purl.org/ontology/bibo/Journal'/>
<rdfs:label>
<xsl:value-of select='vfx:trim(aiic:JOURNAL_NAME)'/>
</rdfs:label>
<core:title><xsl:value-of select='vfx:trim(aiic:JOURNAL_NAME)'/></core:title>
</rdf:Description>
<xsl:for-each select='aiic:ARTICLE_LIST/aiic:ARTICLE_INFO'>
<rdf:Description rdf:about='{$juri}'>
<core:publicationVenueFor 
rdf:resource='{concat($g_instance,.)}'/>
</rdf:Description>

<rdf:Description rdf:about='{concat($g_instance,.)}'>
<core:hasPublicationVenue rdf:resource='{$juri}'/>
</rdf:Description>

</xsl:for-each>
</xsl:for-each>
</rdf:RDF>

</xsl:template>



<xsl:template match='aiic:ARTICLE_LIST'/>

<xsl:template match='aiic:JOURNAL_NAME'/>

<xsl:template match='aiic:ARTICLE_INFO'/>


<xsl:template name='mkJournals'>

</xsl:template>

<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
