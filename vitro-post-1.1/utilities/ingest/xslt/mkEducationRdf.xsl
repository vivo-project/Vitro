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

<xsl:template match='/aiedu:EDUCATION_LIST'>
<rdf:RDF>
<xsl:for-each select='aiedu:EDUCATION'>
<xsl:if test='aiedu:netid != ""'>
<xsl:variable name='edid' select='@id'/>
<xsl:variable name='nidxml' select="concat($rawXmlPath,'/',aiedu:netid, '.xml')"/>

<xsl:variable name='ed' select='document($nidxml)//dm:EDUCATION[@id = $edid]' />
<xsl:if test='$ed/dm:PUBLIC_VIEW="Yes"'>
<rdf:Description rdf:about="{concat($g_instance,@id)}" >
<rdf:type rdf:resource='http://vivoweb.org/activity-insight#EducationalAttainment'/>
<rdf:type rdf:resource='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>
<xsl:variable name='degree'>
<xsl:choose>
<xsl:when test='$ed/dm:DEG = "Other"'>
<xsl:value-of select='$ed/dm:DEGOTHER'/>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select='$ed/dm:DEG'/>
</xsl:otherwise>
</xsl:choose>
</xsl:variable>
<rdfs:label>
<xsl:value-of select="$degree"/>
</rdfs:label>

<acti:educationSchool>
<xsl:value-of select='$ed/dm:SCHOOL'/></acti:educationSchool>
<acti:educationSchoolLocation>
<xsl:value-of select='$ed/dm:LOCATION'/></acti:educationSchoolLocation>

<acti:educationMajor>
<xsl:value-of select='$ed/dm:MAJOR'/></acti:educationMajor>

<acti:yearDegreeCompleted><xsl:value-of select='$ed/dm:YR_COMP'/></acti:yearDegreeCompleted>

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
