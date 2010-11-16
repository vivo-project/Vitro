<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs='http://www.w3.org/2001/XMLSchema'
	xmlns:core="http://vivoweb.org/ontology/core#"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        xmlns:bibo="http://purl.org/ontology/bibo/"
        xmlns:foaf="http://xmlns.com/foaf/0.1/"
        xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
	xmlns:aiedu="http://vivoweb.org/ontology/activity-insight"
	xmlns:acti="http://vivoweb.org/ontology/activity-insight#"
        xmlns="http://vivoweb.org/ontology/activity-insight"
	xmlns:dm="http://www.digitalmeasures.com/schema/data"	
	xmlns:dmd="http://www.digitalmeasures.com/schema/data-metadata"
	xmlns:vfx='http://vivoweb.org/ext/functions'
	exclude-result-prefixes='xs vfx dm dmd'
	>

<xsl:param name='unoMapFile'  required='yes'/>
<xsl:param name='knownDegrees' required='yes'/>
<xsl:param name='rawXmlPath' required='yes'/>

<xsl:output method='xml' indent='yes'/>
<xsl:strip-space elements="*"/>

<xsl:include href='commonvars.xsl'/>

<xsl:variable name='unomap'
	select="document($unoMapFile)/Mapping"/>

<xsl:variable name='knownDegs' 
	select="document($knownDegrees)/KnownVivoDegrees"/>
<xsl:template match='/aiedu:EDUCATION_LIST'>
<rdf:RDF>
<xsl:for-each select='aiedu:EDUCATION'>
<xsl:if test='aiedu:netid != ""'>
<xsl:variable name='edid' select='@id'/>
<xsl:variable name='nidxml' select="concat($rawXmlPath,'/',
					   aiedu:netid, 
					   '.xml')"/>

<xsl:variable name='ed' select=
	'document($nidxml)//dm:EDUCATION[@id = $edid]' />
<xsl:variable name='lmd' select='$ed/@dmd:lastModified'/>
<xsl:variable name='degree'>
<xsl:choose>
<xsl:when test='$ed/dm:DEG = "Other"'>
<xsl:value-of select='vfx:simple-trim($ed/dm:DEGOTHER)'/>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select='$ed/dm:DEG'/>
</xsl:otherwise>
</xsl:choose>
</xsl:variable>

<xsl:if test='$ed/dm:PUBLIC_VIEW="Yes" and $degree != ""'>



<rdf:Description rdf:about="{concat($g_instance,'AI-',@id)}" >
<rdf:type rdf:resource=
	'http://vivoweb.org/ontology/core#EducationalTraining'/>
<rdf:type rdf:resource=
	'http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>

<xsl:comment><xsl:value-of select='$degree'/></xsl:comment>
<xsl:choose>
  <xsl:when test='$knownDegs/degree[ai-abbrev = $degree]/uri != ""'>

	<core:degreeEarned rdf:resource=
		"{$knownDegs/degree[ai-abbrev = $degree]/uri}"/>

  </xsl:when>
  <xsl:otherwise>
	<acti:otherDegreeText>
	<xsl:value-of select='vfx:simple-trim($ed/dm:DEGOTHER)'/>
	</acti:otherDegreeText>
  </xsl:otherwise>
</xsl:choose>



<rdfs:label>
<xsl:value-of select="$ed/dm:MAJOR"/> <xsl:value-of select='$ed/dm:SUPPAREA'/>
</rdfs:label>
<!--
<acti:educationSchool>
<xsl:value-of select='$ed/dm:SCHOOL'/></acti:educationSchool>
<acti:educationSchoolLocation>
<xsl:value-of select='$ed/dm:LOCATION'/></acti:educationSchoolLocation>
-->
<core:majorField>
<xsl:value-of select='$ed/dm:MAJOR'/></core:majorField>

<acti:lastModified><xsl:value-of select='$lmd'/></acti:lastModified>

<xsl:if test='$ed/dm:YR_COMP != ""'>
<core:year rdf:datatype="http://www.w3.org/2001/XMLSchema#gYear">
<xsl:value-of select='$ed/dm:YR_COMP'/>
</core:year>
</xsl:if>

<acti:educationSupportingArea>
<xsl:value-of select='$ed/dm:SUPPAREA'/>
</acti:educationSupportingArea>

<acti:educationDissertationTitle>
<xsl:value-of select='$ed/dm:DISSTITLE'/>
</acti:educationDissertationTitle> 

<acti:educationDistinction>
<xsl:value-of select='$ed/dm:DISTINCTION'/>
</acti:educationDistinction>

<acti:educationIsHighestDegree>
<xsl:value-of select='$ed/dm:HIGHEST'/></acti:educationIsHighestDegree>
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
