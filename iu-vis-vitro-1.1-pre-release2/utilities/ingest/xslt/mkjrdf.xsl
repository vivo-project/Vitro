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
        xmlns="http://vivoweb.org/activity-insight"
	xmlns:dm="http://www.digitalmeasures.com/schema/data"
	xmlns:vfx='http://vivoweb.org/ext/functions'	
	exclude-result-prefixes='vfx xs'
	>

<xsl:output method='xml' indent='yes'/>
<xsl:strip-space elements="*"/>
<xsl:variable name='NL'>
<xsl:text>
</xsl:text>
</xsl:variable>
<xsl:variable name='pfxlen' select='4'/>
<xsl:param name='unoMapFile'  required='yes'/>

<!-- 
for each article in a journal make one of these
<rdf:Description about='http://vivoweb.org/individual/AI-10580942849'>
	<core:hasPublicationVenue rdf:resource='http://vivoweb.org/individual/AJ-AC41F'/>
</rdf:Description>

and one of these ...

<rdf:Description about='http://vivoweb.org/individual/AJ-AC41F'>
	<core:publicationVenueFor rdf:resource='http://vivoweb.org/individual/AI-10580942849'/>
</rdf:Description>

for each journal make one of these ...

<rdf:Description about='http://vivoweb.org/individual/AJ-AC41F'>
	<rdf:type rdf:resource='http://purl.org/ontology/bibo/Journal'>
	<rdfs:label>Journal of Fun and Games</rdfs:label>
	<core:title>Journal of Fun and Games</core:title>
</rdf:Description>

<rdf:Description rdf:about="http://vivoweb.org/individual/AJ-AC41F">
      <core:informationResourceInAuthorship rdf:resource="http://vivoweb.org/individual/AI-10588997633-1"/>
      <core:informationResourceInAuthorship rdf:resource="http://vivoweb.org/individual/AI-10588997633-2"/>
      <core:informationResourceInAuthorship rdf:resource="http://vivoweb.org/individual/AI-10588997633-3"/>

      <core:informationResourceInAuthorship rdf:resource="http://vivoweb.org/individual/AI-11114143745-1"/>
      ...
</rdf:Description>
-->
<xsl:variable name='unomap'
	select="document($unoMapFile)/Mapping"/>


<xsl:template match='/aiic:JOURNAL_LIST'>
<rdf:RDF>

<xsl:for-each select='aiic:ARTICLES_BY_JOURNAL'>
<!-- create a bibo:Journal for this journal -->
<xsl:variable name='ctr'  select='position()'/>

<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>
<xsl:variable name='juri' select="concat('http://vivoweb.org/individual/',$uno)"/>
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
<core:publicationVenueFor rdf:resource='{concat("http://vivoweb.org/individual/",.)}'/>
</rdf:Description>

<rdf:Description rdf:about='{concat("http://vivoweb.org/individual/",.)}'>
<core:hasPublicationVenue rdf:resource='{$juri}'/>
</rdf:Description>

</xsl:for-each>
</xsl:for-each>
</rdf:RDF>

</xsl:template>
<!--
<xsl:template name='declareJournalAuthorships'>
<xsl:param name='article'/>
<xsl:param name='juri'/>
<xsl:variable name='rawaiid' select='substring($article,$pfxlen)'/>
<xsl:variable name='rnid' select='./@ref_netid'/>
<xsl:variable name='path' select="concat('../AIIC_XMLs/AIIC_', $rnid, '.xml')"/>
<xsl:variable name='ijpath' select="document($path)//dm:INTELLCONT_JOURNAL[@id=$rawaiid]"/>

<xsl:for-each select='$ijpath/dm:AuthorList/dm:INTELLCONT_JOURNAL_AUTH'>
<xsl:variable name='authorship' select='dm:ARTICLE_AUTHORSHIP_ORDER'/>
<xsl:variable name='rank' select='$authorship/dm:AUTHORSHIP_POSITION'/>
<xsl:variable name='jaid' select='$authorship/dm:ARTICLE_ID'/>
<xsl:variable name='auri' select='concat("http://vivoweb.org/individual/AI-",$jaid,"-",$rank)'/>
<rdf:Description rdf:about='{$juri}'>
<core:informationResourceInAuthorship rdf:resource='{$auri}'/>
</rdf:Description>
</xsl:for-each>
</xsl:template>
-->



<xsl:template match='aiic:ARTICLE_LIST'/>

<xsl:template match='aiic:JOURNAL_NAME'/>

<xsl:template match='aiic:ARTICLE_INFO'/>


<xsl:template name='mkJournals'>

</xsl:template>

<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
