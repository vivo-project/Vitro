<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs='http://www.w3.org/2001/XMLSchema'
	xmlns:core="http://vivoweb.org/ontology/core#"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        xmlns:bibo="http://purl.org/ontology/bibo/"
        xmlns:foaf="http://xmlns.com/foaf/0.1/"
        xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
	xmlns:aiedu="http://vivoweb.org/activity-insight"
	xmlns:acti="http://vivoweb.org/activity-insight#"
        xmlns="http://vivoweb.org/activity-insight"
	xmlns:dm="http://www.digitalmeasures.com/schema/data"	
	xmlns:vfx='http://vivoweb.org/ext/functions'
	exclude-result-prefixes='xs vfx dm'
	>

<xsl:param name='unoMapFile'  required='yes'/>
<xsl:param name='rawXmlPath' required='yes'/>

<xsl:output method='xml' indent='yes'/>
<xsl:strip-space elements="*"/>

<xsl:include href='commonvars.xsl'/>

<xsl:variable name='unomap'
	select="document($unoMapFile)/Mapping"/>

<xsl:template match='/aiedu:EDITCHAIR_LIST'>
<rdf:RDF>
<xsl:for-each select='aiedu:EDITCHAIR'>
<xsl:if test='aiedu:netid != ""'>
<xsl:variable name='ecid' select='@id'/>
<xsl:variable name='nidxml' select="concat($rawXmlPath,'/',aiedu:netid, '.xml')"/>

<xsl:variable name='ec' select='document($nidxml)//dm:EDITCHAIR[@id = $ecid]' />
<xsl:if test='$ec/dm:PUBLIC_VIEW="Yes"'>
<rdf:Description rdf:about="{concat($g_instance,@id)}" >
<rdf:type rdf:resource='http://vivoweb.org/activity-insight#EditChair'/>
<rdf:type rdf:resource='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>
<rdfs:label>
<xsl:value-of select="vfx:trim($ec/dm:TITLE)"/>
</rdfs:label>

<acti:editChairType><xsl:value-of select='$ec/dm:TYPE'/></acti:editChairType>
<acti:month><xsl:value-of select='$ec/dm:DTM_END'/></acti:month>
<acti:day><xsl:value-of select='$ec/dm:DTD_END'/></acti:day>
<acti:year><xsl:value-of select='$ec/dm:DTY_END'/></acti:year>
<acti:editChairRole>
<xsl:choose>
<xsl:when test='$ec/dm:ROLE = "Other"'>
<xsl:value-of select='$ec/dm:ROLEOTHER'/>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select='$ec/dm:ROLE'/>
</xsl:otherwise>
</xsl:choose>
</acti:editChairRole>
<acti:numberArticlesReviewed><xsl:value-of select='$ec/dm:NUM_REVIEWED'/></acti:numberArticlesReviewed>
<acti:conferenceTopic><xsl:value-of select='$ec/dm:TYPE'/></acti:conferenceTopic>
<core:description>
<xsl:value-of select='$ec/dm:DESC'/>
</core:description>
</rdf:Description>
</xsl:if>

</xsl:if>
</xsl:for-each>
</rdf:RDF>
<xsl:value-of select='$NL'/>
</xsl:template>

<!-- ================================== -->


<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
