<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:ai="http://www.digitalmeasures.com/schema/data"
	xmlns:aiis="http://vivoweb.org/activity-insight"
xmlns="http://vivoweb.org/activity-insight"
	xmlns:dm="http://www.digitalmeasures.com/schema/data">


<xsl:output method='xml' indent='yes'/>
<xsl:strip-space elements="*"/>
<xsl:variable name='NL'>
<xsl:text>
</xsl:text>
</xsl:variable>

<!-- ================================================= -->
<xsl:template match='/'>


<xsl:element name="aiis:AllImpactStmts" namespace="http://vivoweb.org/activity-insight">
<xsl:for-each select='//aiis:IMPACT_STMT_INFO'>
<xsl:sort select='.'/>
<xsl:variable name='x' select='.'/>
<xsl:if test=' not(preceding::aiis:IMPACT_STMT_INFO = $x) '>
<xsl:copy-of select='.'/>
</xsl:if>
</xsl:for-each>
</xsl:element>
<xsl:value-of select='$NL'/>
</xsl:template>




</xsl:stylesheet>
