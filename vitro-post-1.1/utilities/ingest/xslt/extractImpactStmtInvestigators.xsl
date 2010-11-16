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
	exclude-result-prefixes='xs vfx'
	>

<xsl:param name='isByInvFile'  required='yes'/>
<xsl:param name='unoMapFile'  required='yes'/>
<xsl:param name='extPerIn' required='yes'/>

<xsl:output method='xml' indent='yes'/>
<xsl:strip-space elements="*"/>

<xsl:include href='commonvars.xsl' />
<xsl:key name='peepsKey' match='person' use='vfx:clean(lname)'/>
<xsl:variable name='islist' 
  select="document($isByInvFile)//aiis:IMPACT_STMT_INFO"/>

<xsl:variable name='unomap'
	select="document($unoMapFile)/Mapping"/>

<xsl:variable name='extantPersons'
	select="document($extPerIn)/ExtantPersons"/>
<!-- ================================== -->
<xsl:template match='/aiis:INVESTIGATOR_LIST'>
<rdf:RDF>

<!-- =================================== -->

<xsl:variable name='prenewps'>
<xsl:element name='ExtantPersons' inherit-namespaces='no'>
<xsl:for-each select='aiis:IMPACT_STMTS_BY_INVESTIGATOR'>

<xsl:if test='vfx:goodName(aiis:FirstName, 
	                   aiis:MiddleName, 
                           aiis:LastName)'>

<xsl:variable name='ctr'  select='@counter'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>

<xsl:variable name='kUri' 
	select='vfx:knownUriByNetidOrName(aiis:FirstName, 
	                       		aiis:MiddleName, 
                               		aiis:LastName,
					aiis:Netid, 
                               		key("peepsKey",vfx:clean(aiis:LastName),$extantPersons))'/>
<xsl:comment><xsl:value-of select='$kUri'/></xsl:comment>
<xsl:variable name='furi' 
select="if($kUri != '') then $kUri 
                            else concat($g_instance,$uno)"/>


<xsl:if test='$kUri = ""'>

<xsl:element name='person' inherit-namespaces='no'>
<xsl:element name='uri' inherit-namespaces='no'>
<xsl:value-of select='concat("NEW-",$furi)'/></xsl:element>
<xsl:element name='fname' inherit-namespaces='no'>
<xsl:value-of select='aiis:FirstName'/></xsl:element>
<xsl:element name='mname' inherit-namespaces='no'>
<xsl:value-of select='aiis:MiddleName'/></xsl:element>
<xsl:element name='lname' inherit-namespaces='no'>
<xsl:value-of select='aiis:LastName'/></xsl:element>
<xsl:element name='netid' inherit-namespaces='no'>
<xsl:value-of select='aiis:Netid'/></xsl:element>
</xsl:element>
</xsl:if>
</xsl:if>
</xsl:for-each>
</xsl:element>
</xsl:variable>



<xsl:variable name='newps'>
<xsl:call-template name='newPeople'>
<xsl:with-param name='knowns' select='$prenewps/ExtantPersons'/>
</xsl:call-template>
</xsl:variable>

<xsl:element name='ExtantPersons' inherit-namespaces='no'>
<xsl:value-of select='$NL'/>

<xsl:comment>
<xsl:value-of select=
	'concat("Number of new persons ",count($newps//person))'/>
</xsl:comment>

<xsl:for-each select='$newps//person'>

<xsl:element name='person' inherit-namespaces='no'>
<xsl:element name='uri' inherit-namespaces='no'>
<xsl:value-of select='substring-after(uri,"NEW-")'/></xsl:element>
<xsl:element name='fname' inherit-namespaces='no'>
<xsl:value-of select='normalize-space(fname)'/></xsl:element>
<xsl:element name='mname' inherit-namespaces='no'>
<xsl:value-of select='normalize-space(mname)'/></xsl:element>
<xsl:element name='lname' inherit-namespaces='no'>
<xsl:value-of select='normalize-space(lname)'/></xsl:element>
<xsl:element name='netid' inherit-namespaces='no'>
<xsl:value-of select='netid'/></xsl:element>
</xsl:element>


</xsl:for-each>
</xsl:element>
<xsl:value-of select='$NL'/>

</xsl:template>


<!-- =================================== -->


<!-- ================================== -->
<xsl:template match='aiis:IMPACT_STMT_LIST'/>

<xsl:template match='aiis:ALT_SRC_IMPACT_STMT_INFO'/>

<xsl:template match='aiis:IMPACT_STMT_INFO'/>


<!-- =================================================== -->

<!-- ================================== -->


<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
