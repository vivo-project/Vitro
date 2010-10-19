<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs='http://www.w3.org/2001/XMLSchema'
	xmlns:core="http://vivoweb.org/ontology/core#"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        xmlns:bibo="http://purl.org/ontology/bibo/"
        xmlns:foaf="http://xmlns.com/foaf/0.1/"
        xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
	xmlns:aichr="http://vivoweb.org/ontology/activity-insight"
	xmlns:acti="http://vivoweb.org/ontology/activity-insight#"
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

<xsl:template match='/aichr:CHRESEARCH_LIST'>
<rdf:RDF>
<xsl:for-each select='aichr:CHRESEARCH'>
<xsl:if test='aichr:netid != ""'>
<xsl:variable name='objid' select='./aichr:CHRESEARCH_ID'/>
<xsl:variable name='nidxml' 
select="concat($rawXmlPath,'/',aichr:netid, '.xml')"/>

<xsl:variable name='ref' 
select='document($nidxml)//dm:CHRESEARCH[@id = $objid]' />
<xsl:if test='$ref/dm:PUBLIC_VIEW !=""'>
<rdf:Description rdf:about="{concat($g_instance,$objid)}" >
<rdf:type 
rdf:resource='http://vivoweb.org/ontology/activity-insight#CHResearch'/>
<rdf:type 
rdf:resource='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>

<rdfs:label>
<xsl:value-of select="$ref/dm:PUB_TITLE"/>
</rdfs:label>

<core:year><xsl:value-of select="$ref/dm:DTY_PUB"/></core:year>

<acti:classification>
<xsl:value-of select="$ref/dm:CLASSIFICATION"/>
</acti:classification>

<xsl:variable name='type'>
<xsl:choose>
<xsl:when test='dm:TYPE != "Other"'>
<xsl:value-of select="$ref/dm:TYPE"/>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select="$ref/dm:TYPE_OTHER"/>
</xsl:otherwise>
</xsl:choose>
</xsl:variable>
<acti:reaearchType>
<xsl:value-of select="$type"/>
</acti:reaearchType>

<core:publicationStatus>
<xsl:value-of select='$ref/dm:STATUS'/>
</core:publicationStatus>

<acti:number><xsl:value-of select='$ref/dm:NBR'/></acti:number>

<acti:volume><xsl:value-of select='$ref/dm:VOLUME'/></acti:volume>
<core:description><xsl:value-of select='$ref/dm:COMMENTS'/></core:description>

<!-- 

  put other data proerties here 

-->

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
