<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs='http://www.w3.org/2001/XMLSchema'
	xmlns:core="http://vivoweb.org/ontology/core#"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        xmlns:bibo="http://purl.org/ontology/bibo/"
        xmlns:foaf="http://xmlns.com/foaf/0.1/"
        xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
	xmlns:aiis="http://vivoweb.org/ontology/activity-insight"
	xmlns:acti="http://vivoweb.org/ontology/activity-insight#"
	xmlns:dm="http://www.digitalmeasures.com/schema/data"	
	xmlns:vfx='http://vivoweb.org/ext/functions'
	xmlns:mapid="http://vivoweb.org/ontology/activity-insight"
	exclude-result-prefixes='xs vfx'
	>

<xsl:include href='commonvars.xsl' />

<xsl:output method='xml' indent='yes'/>
<xsl:strip-space elements="*"/>

<xsl:param name='aiusers' required='yes'/>


<xsl:variable name='aiUsers'
	select="document($aiusers)/mapid:aiidmap"/>
	
<xsl:template match='/ExtantPersons'>
<rdf:RDF>
<xsl:for-each select='person'>
<xsl:variable name='per' select='.'/>
<xsl:variable name='nid' select='./netid'/>
<xsl:variable name='uri' select='./uri'/>
<xsl:variable name='aiid' 
	select='$aiUsers/mapid:mapterm
		[$nid != "" and vfx:sameNetid(mapid:netid,$nid)]/mapid:aiid'/>
<xsl:if test='$aiid'>

<rdf:Description rdf:about='{$uri}'>
<rdf:type rdf:resource='http://xmlns.com/foaf/0.1/Person'/>
<rdf:type rdf:resource=
	'http://vivoweb.org/ontology/activity-insight#ActivityInsightPerson'/>
<rdf:type rdf:resource=
	'http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>
<acti:aiUser>
<xsl:value-of select='$aiid'/>=<xsl:value-of select='$nid'/>
</acti:aiUser>
<rdfs:label>
<xsl:value-of select='vfx:mkFullname($per/fname,$per/mname,$per/lname)'/>
</rdfs:label>
<foaf:firstName><xsl:value-of select='vfx:simple-trim($per/fname)'/></foaf:firstName>
<core:middleName><xsl:value-of select='vfx:simple-trim($per/mname)'/></core:middleName>
<foaf:lastName><xsl:value-of select='vfx:simple-trim($per/lname)'/></foaf:lastName>

</rdf:Description>

</xsl:if>	

</xsl:for-each>
</rdf:RDF>
<xsl:value-of select='$NL'/>

</xsl:template>

<xsl:function name='vfx:mkFullname'>
<xsl:param name='fn'/>
<xsl:param name='mn'/>
<xsl:param name='ln'/>
<xsl:variable name='fullname0' select='vfx:simple-trim($ln)'/>
<xsl:variable name='fullname1' 
select='if(vfx:simple-trim($fn)!="") 
        then 
          concat($fullname0,", ",vfx:simple-trim($fn)) 
        else 
        $fullname0'/>
<xsl:variable name='fullname2'
select='if(vfx:simple-trim($mn)!="" and $fullname1 = $fullname0)
	then
	  concat($fullname1,", ",vfx:simple-trim($mn))
	else
	  concat($fullname1," ",vfx:simple-trim($mn))'/>
<xsl:value-of select='vfx:simple-trim($fullname2)'/>

</xsl:function>

<xsl:include href='vivofuncs.xsl'/>
</xsl:stylesheet>

