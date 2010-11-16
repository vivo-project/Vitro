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
<xsl:variable name='prenewJournals'>
<xsl:element name='ExtantJournals' inherit-namespaces='no'>

<xsl:for-each select='aiic:ARTICLES_BY_JOURNAL'>
<xsl:variable name='ctr'  select='position()'/>

<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>

<xsl:variable name='knownUri' 
select='vfx:knownJournalUri(aiic:JOURNAL_NAME, $extantJournals)'/>
<!-- xsl:comment><xsl:value-of select='aiic:JOURNAL_NAME' separator='|'/> </xsl:comment-->
<xsl:variable name='juri' 
	select="if($knownUri != '') then $knownUri else concat($g_instance,$uno)"/>

<xsl:if test='$knownUri= ""'>
<xsl:element name='journal' inherit-namespaces='no'>

<xsl:element name='name' inherit-namespaces='no'>
<xsl:value-of select='aiic:JOURNAL_NAME'/>
</xsl:element>

<xsl:element name='uri' inherit-namespaces='no'>
<xsl:value-of select='concat("NEW-",$juri)'/>
</xsl:element>

</xsl:element>

</xsl:if>

</xsl:for-each>
</xsl:element>
</xsl:variable>
<!-- xsl:comment>
<xsl:value-of select='$prenewJournals/ExtantJournals' separator='|'/> </xsl:comment-->
<xsl:variable name='newJournals'>
<xsl:call-template name='NewJournals'>
<xsl:with-param name='knowns' select='$prenewJournals/ExtantJournals'/>
</xsl:call-template>
</xsl:variable>
<xsl:comment>
<xsl:value-of select='concat("count=",count($prenewJournals//journal))' separator=' | '/> 
</xsl:comment>




<xsl:for-each select='aiic:ARTICLES_BY_JOURNAL'>
<!-- create a bibo:Journal for this journal -->
<xsl:variable name='ctr'  select='position()'/>

<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>

<xsl:variable name='knownUri' 
select='vfx:knownJournalUri(aiic:JOURNAL_NAME, 
			    $extantJournals union 
				$prenewJournals/ExtantJournals)'/>

<xsl:variable name='juri' 
	select='if(starts-with($knownUri,"NEW-")) then 
		substring-after($knownUri,"NEW-") else 
		$knownUri'/>


<rdf:Description rdf:about="{$juri}">
<rdf:type rdf:resource=
	'http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>
<rdf:type rdf:resource='http://purl.org/ontology/bibo/Journal'/>
<rdfs:label>
<xsl:value-of select='vfx:trim(aiic:JOURNAL_NAME)'/>
</rdfs:label>
<core:title><xsl:value-of select='vfx:trim(aiic:JOURNAL_NAME)'/></core:title>
</rdf:Description>


<xsl:for-each select='aiic:ARTICLE_LIST/aiic:ARTICLE_INFO'>
<xsl:if test='./@hasTitle = "Yes"'>
<rdf:Description rdf:about='{$juri}'>
<core:publicationVenueFor 
rdf:resource='{concat($g_instance,.)}'/>
</rdf:Description>

<rdf:Description rdf:about='{concat($g_instance,.)}'>
<core:hasPublicationVenue rdf:resource='{$juri}'/>
</rdf:Description>
</xsl:if>
</xsl:for-each>

</xsl:for-each>



<xsl:result-document href='{$extJournalOut}'>
<xsl:element name='ExtantJournals' namespace=''>
<xsl:value-of select='$NL'/>
<xsl:if test='count($newJournals//journal)>0'>
<xsl:comment>
<xsl:value-of select='count($newJournals//journal)'/>
</xsl:comment>
</xsl:if>

<xsl:for-each select='$newJournals//journal'>

<xsl:element name='journal' namespace=''>

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
</xsl:result-document>


</rdf:RDF>
<xsl:value-of select='$NL'/>
</xsl:template>



<xsl:template match='aiic:ARTICLE_LIST'/>

<xsl:template match='aiic:JOURNAL_NAME'/>

<xsl:template match='aiic:ARTICLE_INFO'/>


<xsl:template name='mkJournals'>

</xsl:template>






<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
