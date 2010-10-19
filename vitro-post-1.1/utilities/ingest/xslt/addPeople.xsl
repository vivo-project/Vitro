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
<xsl:param name='aiicXmlPath' required='yes'/>
<xsl:param name='aiicPrefix' required='yes'/>
<xsl:param name='extPerIn' required='yes'/>
<xsl:param name='extPerOut' required='yes'/>

<xsl:output method='xml' indent='yes'/>
<xsl:strip-space elements="*"/>

<xsl:include href='commonvars.xsl'/>

<xsl:variable name='alist' 
  select="document($abyjFile)//aiic:ARTICLE_INFO"/>

<xsl:variable name='unomap'
	select="document($unoMapFile)/Mapping"/>

<xsl:variable name='extantPersons'
	select="document($extPerIn)/ExtantPersons"/>
<!-- ================================== -->
<xsl:template match='/aiic:AUTHOR_LIST'>

<!-- =================================== -->

<xsl:variable name='prenewps'>
<xsl:element name='ExtantPersons' inherit-namespaces='no'>
<xsl:for-each select='aiic:ARTICLES_BY_AUTHOR'>
<xsl:variable name='ctr'  select='@counter'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>
<!-- xsl:comment><xsl:value-of 
select='concat(aiic:FirstName,"|",
	aiic:MiddleName,"|",
	aiic:LastName,"|"
	,aiic:Netid)'/></xsl:comment-->
<xsl:variable name='kUri' 
	select='vfx:knownUriByNetidOrName(aiic:FirstName, 
	                       		aiic:MiddleName, 
                               		aiic:LastName,
					aiic:Netid, 
                               		$extantPersons)'/>
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
</xsl:for-each>
</xsl:element>
</xsl:variable>

<!--
<xsl:call-template name='saveNewPeople'>
<xsl:with-param name='file' 
select='"/home/jrm424/aiw/test/store/feedback/Pbar0.xml"'/>
<xsl:with-param name='newpeople' select='$prenewps'/>
</xsl:call-template>
-->
<xsl:variable name='newps'>
<xsl:call-template name='newPeople'>
<!-- xsl:with-param name='list' select='aiic:ARTICLES_BY_AUTHOR'/-->
<xsl:with-param name='knowns' select='$prenewps/ExtantPersons'/>
</xsl:call-template>
</xsl:variable>

<!-- =================================== -->
<!--
<xsl:call-template name='saveNewPeople'>
<xsl:with-param name='file' 
select='"/home/jrm424/aiw/test/store/feedback/Pbar1.xml"'/>
<xsl:with-param name='newpeople' select='$newps'/>
</xsl:call-template>
-->



<xsl:call-template name='NewPeopleOut'>
<xsl:with-param name='file' select='$extPerOut'/>
<xsl:with-param name='newpeople' select='$newps'/>
</xsl:call-template>

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
