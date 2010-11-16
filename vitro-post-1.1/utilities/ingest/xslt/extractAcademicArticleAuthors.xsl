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

	exclude-result-prefixes='xs vfx'
	>

<xsl:param name='abyjFile'  required='yes'/>
<xsl:param name='unoMapFile'  required='yes'/>
<xsl:param name='extPerIn' required='yes'/>

<xsl:output method='xml' indent='yes'/>
<xsl:strip-space elements="*"/>

<xsl:include href='commonvars.xsl'/>

<xsl:variable name='alist' 
  select="document($abyjFile)//aiic:ARTICLE_INFO"/>

<xsl:variable name='unomap'
	select="document($unoMapFile)/Mapping"/>

<xsl:variable name='extantPersons'
	select="document($extPerIn)/ExtantPersons"/>

<xsl:key name='peepsKey' match='person' use='vfx:clean(lname)'/>
<!-- ================================== -->
<xsl:template match='/aiic:AUTHOR_LIST'>

<!-- =================================== -->

<xsl:variable name='prenewps'>
<xsl:element name='ExtantPersons' inherit-namespaces='no'>
<xsl:for-each select='aiic:ARTICLES_BY_AUTHOR'>
<xsl:if test='vfx:goodName(aiic:FirstName,
                           aiic:MiddleName,
                           aiic:LastName)'>


<xsl:variable name='ctr'  select='@counter'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>
<!-- xsl:comment><xsl:value-of 
select='concat(aiic:FirstName,"|",
	aiic:MiddleName,"|",
	aiic:LastName,"|"
	,aiic:Netid)'/></xsl:comment-->
<xsl:variable name='kUri' 
	select='vfx:knownUriByNetidOrNameKeyed(aiic:FirstName, 
	                       		aiic:MiddleName, 
                               		aiic:LastName,
					aiic:Netid, 
                               		key("peepsKey",
					    vfx:clean(aiic:LastName),
                                            $extantPersons))'/>
<!-- xsl:comment><xsl:value-of select='$kUri'/></xsl:comment -->
<xsl:variable name='furi' 
select="if($kUri != '') then $kUri 
                            else concat($g_instance,$uno)"/>


<xsl:if test='$kUri = ""'>

<xsl:element name='person' inherit-namespaces='no'>
<xsl:element name='uri' inherit-namespaces='no'>
<xsl:value-of select='concat("NEW-",$furi)'/></xsl:element>
<xsl:element name='fname' inherit-namespaces='no'>
<xsl:value-of select='aiic:FirstName'/></xsl:element>
<xsl:element name='mname' inherit-namespaces='no'>
<xsl:value-of select='aiic:MiddleName'/></xsl:element>
<xsl:element name='lname' inherit-namespaces='no'>
<xsl:value-of select='aiic:LastName'/></xsl:element>
<xsl:element name='netid' inherit-namespaces='no'>
<xsl:value-of select='aiic:Netid'/></xsl:element>
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

<!-- =================================== -->

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



<!-- ================================== -->
<xsl:template match='aiic:ARTICLE_LIST'/>

<xsl:template match='aiic:ALT_SRC_ARTICLE_INFO'/>

<xsl:template match='aiic:ARTICLE_INFO'/>


<!-- =================================================== -->
<!-- =================================================== -->
<xsl:template name='mkArticles'/>


<!-- ================================== -->


<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
