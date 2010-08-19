<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs='http://www.w3.org/2001/XMLSchema'
	xmlns:merge="http:www.ora.com/XSLTCookbook/mnamespaces/merge"
	exclude-result-prefixes='xs'>

<xsl:output method='xml' indent='yes'/>
<xsl:strip-space elements="*"/>
<xsl:variable name='NL'>
<xsl:text>
</xsl:text>
</xsl:variable>
<xsl:include href='merge-by-key.xsl'/>

<xsl:key name='merge:key' match='person' 
	use='concat(fname,"|",mname,"|",lname)'/>

<xsl:template name='merge:key-value'>
<xsl:value-of select='concat(fname,"|",mname,"|",lname)'/>
</xsl:template>

</xsl:stylesheet>
