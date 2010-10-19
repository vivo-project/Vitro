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


<xsl:param name='fn1'  required='yes'/>
<xsl:param name='mn1'  required='yes'/>
<xsl:param name='ln1'  required='yes'/>
<xsl:param name='extPerIn' required='yes'/>


<xsl:output method='xml' indent='yes'/>
<xsl:strip-space elements="*"/>

<xsl:include href='commonvars.xsl'/>


<xsl:variable name='extantPersons'
	select="document($extPerIn)/ExtantPersons"/>
<!-- ================================== -->
<xsl:template match='/'>
<rdf:RDF>
<!--
<xsl:variable name='res' as='node()*' 
		select='vfx:bestChoices($extantPersons)'/>
	  <xsl:value-of select='$res[2]/uri'/> 
-->


<xsl:variable name='res' as='node()*' 
 select='vfx:knownPersonByNetidOrName($fn1,$mn1,$ln1,"",$extantPersons)'/>
 <xsl:copy-of select='$res'/> 

<!--

<xsl:variable name='longest' 
  select='vfx:maxNameLength($extantPersons)' as='xs:integer'/>

<xsl:variable name='list' as='node()*' >
<xsl:sequence
select='$extantPersons/person[string-length(concat(normalize-space(./lname),"|",
                                           normalize-space(./fname),"|",
                                           normalize-space(./mname)))
                      = 8]' />
</xsl:variable>

<xsl:value-of select='$longest'/> 
<xsl:value-of select='$NL'/>
<xsl:copy-of select='$list[2]'/> 
-->

</rdf:RDF>
<xsl:value-of select='$NL'/>
</xsl:template>


<!-- ================================== -->


<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
