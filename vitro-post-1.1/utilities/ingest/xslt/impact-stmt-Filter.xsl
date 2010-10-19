<?xml version="1.0" encoding="iso-8859-1"?>
<!-- a XSLT that filters out nodes -->

<xsl:stylesheet 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:dm="http://www.digitalmeasures.com/schema/data"
    	xmlns:dmd="http://www.digitalmeasures.com/schema/data-metadata"
	xmlns="http://vivoweb.org/ai"
	exclude-result-prefixes='dm dmd'
	version="2.0">
    <xsl:output indent="yes"/>
    
 <!-- ================================================= -->

    <xsl:template match="/dm:Data">
	<xsl:text>&#xA;</xsl:text>
        <xsl:element name='Data'  namespace='{namespace-uri()}'>
            <xsl:apply-templates select="dm:Record"/>
        </xsl:element>

    </xsl:template>

<!-- ================================================= -->

 
    <xsl:template match="dm:Record">
	<xsl:element name='Record' namespace='{namespace-uri()}'>
	<xsl:attribute name='username' select='./@username'/>
	<xsl:attribute name='userId' select='./@userId'/>
            <xsl:apply-templates select='dmd:IndexEntry'/>
            <xsl:apply-templates select='dm:PCI'/>
	    <xsl:apply-templates select='dm:IMPACT_STATEMENT'/>
        </xsl:element>
    </xsl:template>

<!-- ================================================= -->

 
    <xsl:template  match="dm:PCI">
        <xsl:element  name='PCI' namespace='{namespace-uri()}'>
		<xsl:attribute name='id' select='./@id'/>
		<xsl:attribute name='lastModified' select='./@dmd:lastModified'/>
            <xsl:copy-of select="*" copy-namespaces='no'/>
        </xsl:element>
    </xsl:template>


<!-- ================================================= -->

 
    <xsl:template match="dmd:IndexEntry">
	
        <xsl:element name="IndexEntry">
		<xsl:attribute name='indexKey' select='./@indexKey'/>
            <xsl:copy-of select="@text"/>
        </xsl:element>
    </xsl:template>
 

<!-- ================================================= -->

     
    <xsl:template  match="dm:IMPACT_STATEMENT">
	<xsl:if test='count(./dm:IMPACT_STATEMENT_INVEST)>0'>
	<xsl:element name='{local-name()}' namespace='{namespace-uri()}'>
		<xsl:attribute name='id' select='./@id'/>
		<xsl:attribute name='lastModified' select='./@dmd:lastModified'/>
	<xsl:copy-of select='*'  copy-namespaces='no'/>
	</xsl:element>
	</xsl:if>
    </xsl:template>

 
</xsl:stylesheet>
	
	
