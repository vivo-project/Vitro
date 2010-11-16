<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs='http://www.w3.org/2001/XMLSchema'
	xmlns:core="http://vivoweb.org/ontology/core#"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        xmlns:bibo="http://purl.org/ontology/bibo/"
        xmlns:foaf="http://xmlns.com/foaf/0.1/"
        xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
	xmlns:acti="http://vivoweb.org/ontology/activity-insight#"	
	xmlns:aiic="http://vivoweb.org/ontology/activity-insight"
	xmlns:dm="http://www.digitalmeasures.com/schema/data"
	xmlns:vfx='http://vivoweb.org/ext/functions'

	exclude-result-prefixes='xs vfx dm aiic acti rdfs foaf bibo rdf core'
	>
<!--

java -Xmx8192m -Xms8192m -XX:MaxPermSize=256m -jar xslt/saxon9he.jar xslt/empty.xml xslt/testKnownUriByNetidOrName.xsl extPerIn=../store/feedback/personsAtTimeZero.xml

-->

<xsl:param name='extPerIn' required='yes'/>

<xsl:output method='xml' indent='yes'/>
<xsl:strip-space elements="*"/>

<xsl:include href='commonvars.xsl'/>

<xsl:variable name='extantPersons'
	select="document($extPerIn)/ExtantPersons"/>

<xsl:key name='peepsKey' match='person' use='vfx:clean(lname)'/>
<!-- ================================== -->
<xsl:variable name='fn' select='"A"'/>
<xsl:variable name='mn' select='"J"'/>
<xsl:variable name='ln' select='"Smith"'/>
<xsl:variable name='nid' select='""'/>
<xsl:template match='/'>

<!-- =================================== -->
<ans><xsl:value-of select='$NL'/>
<xsl:comment><xsl:value-of select='count(key("peepsKey",
				       vfx:clean($ln),
			               $extantPersons))'/> </xsl:comment>

<xsl:value-of select='$NL'/>
<xsl:variable name='kUri' 
	select='vfx:knownUriByNetidOrNameKeyed($fn,$mn,$ln,$nid,
                               			key("peepsKey",
						    vfx:clean($ln),
						    $extantPersons))'/>


<xsl:value-of select='$kUri'/>
<xsl:value-of select='$NL'/>
<xsl:variable name='kUri2' 
	select='vfx:knownUriByNetidOrName($fn,$mn,$ln,$nid,$extantPersons)'/>
<xsl:value-of select='$kUri2'/>
<xsl:value-of select='$NL'/>

<xsl:variable name='peep' 
	select='vfx:knownPersonByNetidOrNameKeyed($fn,$mn,$ln,$nid,
                               			key("peepsKey",
						    vfx:clean($ln),
						    $extantPersons))'/>
<parts>
<fn>
<xsl:value-of select='$peep/fname'/>
</fn>
<mn>
<xsl:value-of select='$peep/mname'/>
</mn>
<ln>
<xsl:value-of select='$peep/lname'/>
</ln>
<nid>
<xsl:value-of select='$peep/netid'/>
</nid>
<uri>
<xsl:value-of select='$peep/uri'/>
</uri>
</parts>
<xsl:value-of select='$NL'/>
</ans>
<xsl:value-of select='$NL'/>
<!-- ================================== -->
</xsl:template>

<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
