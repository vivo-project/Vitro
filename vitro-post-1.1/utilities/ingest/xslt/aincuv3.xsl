<?xml version='1.0' encoding="UTF-8"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:ai="http://www.digitalmeasures.com/schema/data"
	xmlns:aiic="http://vivoweb.org/ontology/activity-insight"
	xmlns="http://vivoweb.org/ontology/activity-insight"
	xmlns:dm="http://www.digitalmeasures.com/schema/data"
	xmlns:dmu="http://www.digitalmeasures.com/schema/user-metadata"
xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:xs='http://www.w3.org/2001/XMLSchema'
	xmlns:vfx='http://vivoweb.org/ext/functions'	
	exclude-result-prefixes='vfx xs xlink ai aiic dm dmu'
>

<xsl:param name='fpx'  required='yes'/>

<xsl:output method='xml' indent='yes'/>
<xsl:strip-space elements="*"/>
<xsl:variable name='NL'>
<xsl:text>
</xsl:text>
</xsl:variable>

<xsl:variable name='fps' select='document($fpx)/ExtantPersons'/>
<!-- ================================================= -->
<xsl:template match='/Users'>

<xsl:element name='{concat("AI-NOT-CUIV-",local-name())}' namespace=''>
<xsl:apply-templates select='User'/>

</xsl:element>

<xsl:value-of select='$NL'/>
</xsl:template>

<xsl:template match='User'>
<xsl:variable name='nid' select='@username'/>
<xsl:variable name='aid' select='@dmu:userId'/>

<!-- xsl:if test='not($fps/person[lower-case(cornell)=$nid])' -->

<xsl:variable name='x' select=
'$fps/person[vfx:sameName2(fname,mname,lname,FirstName,MiddleName,LastName) and cornell != $nid]'/>
<xsl:if test='$x'>
<xsl:element name='person'  namespace=''>
<xsl:attribute name='netid'  select='$nid'/>

<xsl:element name='aiid'  namespace=''>
<xsl:value-of select='$aid'/>
</xsl:element>

<xsl:element name='ai-fname'  namespace=''>
<xsl:value-of select='FirstName'/>
</xsl:element>
<xsl:element name='cuv-fname'  namespace=''>
<xsl:value-of select='$x/fname'/>
</xsl:element>
<xsl:element name='ai-mname'  namespace=''>
<xsl:value-of select='MiddleName'/>
</xsl:element>
<xsl:element name='cuv-mname'  namespace=''>
<xsl:value-of select='vfx:simple-trim($x/mname)'/>
</xsl:element>
<xsl:element name='ai-lname'  namespace=''>
<xsl:value-of select='LastName'/>
</xsl:element>
<xsl:element name='cuv-lname'  namespace=''>
<xsl:value-of select='vfx:simple-trim($x/lname)'/>
</xsl:element>
<xsl:element name='cornell'  namespace=''>
<xsl:value-of select='vfx:simple-trim($x/cornell)'/>
</xsl:element>
</xsl:element>
</xsl:if>
<!-- /xsl:if -->

</xsl:template>


<!-- ================================== -->
<xsl:function name='vfx:sameName' as='xs:boolean'>
<xsl:param name='fn1'/>
<xsl:param name='mn1'/>
<xsl:param name='ln1'/>
<xsl:param name='fn2'/>
<xsl:param name='mn2'/>
<xsl:param name='ln2'/>
<xsl:variable name='n1' select='concat(vfx:trim($fn1),"|",vfx:trim($mn1) ,"|",vfx:trim($ln1))'/>
<xsl:variable name='n2' select='concat(vfx:trim($fn2),"|",vfx:trim($mn2) ,"|",vfx:trim($ln2))'/>
<xsl:value-of select='$n1 = $n2'/>

</xsl:function>

<xsl:function name='vfx:sameName2' as='xs:boolean'>
<xsl:param name='fn1'/>
<xsl:param name='mn1'/>
<xsl:param name='ln1'/>
<xsl:param name='fn2'/>
<xsl:param name='mn2'/>
<xsl:param name='ln2'/>
<xsl:value-of select='vfx:trim($fn1) = vfx:trim($fn2) and vfx:trim($mn1) = vfx:trim($mn2) and vfx:trim($ln1) = vfx:trim($ln2)'/>

</xsl:function>
<xsl:include href='vivofuncs.xsl'/>


</xsl:stylesheet>
