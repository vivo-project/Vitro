<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs='http://www.w3.org/2001/XMLSchema'
	xmlns:core="http://vivoweb.org/ontology/core#"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        xmlns:bibo="http://purl.org/ontology/bibo/"
        xmlns:foaf="http://xmlns.com/foaf/0.1/"
        xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
	xmlns:aigrant="http://vivoweb.org/ontology/activity-insight"
	xmlns:acti="http://vivoweb.org/ontology/activity-insight#"
        xmlns="http://vivoweb.org/ontology/activity-insight"
xmlns:dm="http://www.digitalmeasures.com/schema/data"	
	xmlns:vfx='http://vivoweb.org/ext/functions'
	exclude-result-prefixes='xs' >



<xsl:param name='extPerIn' required='yes'/>


<xsl:output method='xml' indent='yes'/>
<xsl:strip-space elements="*"/>

<xsl:include href='commonvars.xsl'/>


<xsl:variable name='extantPersons'
	select="document($extPerIn)/ExtantPersons"/>
<!-- ================================== -->
<xsl:template match='/aigrant:INVESTIGATOR_LIST'>
<rdf:RDF>

<xsl:for-each select='aigrant:GRANTS_BY_INVESTIGATOR'>
<xsl:value-of select='$NL'/>


<xsl:comment>
<xsl:value-of select='
concat("[",aigrant:FirstName,"|",aigrant:MiddleName,"|",aigrant:LastName,"]")'/>
</xsl:comment>
<xsl:value-of select='$NL'/>

<xsl:variable name='known' select='vfx:knownPerson(aigrant:FirstName, aigrant:MiddleName, aigrant:LastName, $extantPersons)'/>

<xsl:variable name='res' select="if($known != '') then 'YES' else 'NO' "/>

<xsl:comment><xsl:value-of select='$res'/> - 
<xsl:value-of select='$known'/></xsl:comment>

<xsl:if test='$res = "YES"'>
<xsl:variable name='results' 
select='$extantPersons/person[uri = $known]'/>
<xsl:comment>
<xsl:value-of select='
concat("{",$results/fname,"|",$results/mname,"|",$results/lname,"}")'/>
</xsl:comment>
<xsl:value-of select='$NL'/>
<xsl:comment>
<xsl:value-of select='vfx:isoScore(aigrant:FirstName,aigrant:MiddleName,aigrant:LastName,$results/fname,$results/mname,$results/lname)'/>
</xsl:comment>
</xsl:if>


<xsl:value-of select='$NL'/>


</xsl:for-each>


</rdf:RDF>
<xsl:value-of select='$NL'/>
</xsl:template>


<!-- ================================== -->


<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
