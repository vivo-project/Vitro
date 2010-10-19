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

<xsl:param name='abbr'  required='yes'/>
<xsl:output method='text' indent='yes'/>
<xsl:strip-space elements="*"/>
<xsl:variable name='NL'>
<xsl:text>
</xsl:text>
</xsl:variable>

<xsl:template match='/KnownVivoDegrees'>
<xsl:value-of select='degree[ai-abbrev = $abbr]/name'/>
<xsl:value-of select='$NL'/>
<xsl:value-of select='degree[ai-abbrev = $abbr]/uri'/>
<xsl:value-of select='$NL'/>
</xsl:template>

</xsl:stylesheet>