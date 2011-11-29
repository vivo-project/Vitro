<?xml version="1.0" encoding="UTF-8"?>
<!-- $This file is distributed under the terms of the license in /doc/license.txt$ -->
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"    
    xmlns:vitro="http://vitro.mannlib.cornell.edu/ns/vitro/0.7#"    
    version="2.0">    
<xsl:output method="xml" indent="yes"/>    
    <!--
        XSLT to produce RDF/XML from XML.  This template does not handle
        narrative content.
        
        This uses XSLT 2.0 and XPath 2.0 so use Saxon.
        Here is an example of how to do use XSLT 2.0 in ant:
        
        <xslt style="${ingest.dir}/xsl/activityInsight2n3Rdf.xsl" 
            in ="${ingest.dir}/ai_xml/document1.xml"
            out="${ingest.dir}/ai_xml/document1.n3"      
            <classpath location="${ingest.dir}/lib/saxon8.jar" /> 
        
        Here is an example of how to run saxon from the command line:
        java -Xmx512m -jar /home/bdc34/bin/saxon9he.jar \
        -s:filteredbjltest1.xml  -xsl:xsl/activityInsight2n3Rdf.xsl  -o:filteredbjltest2.rdf
        
        2009 Mann Library, Brian Caruso.
    -->
    
    <!-- This XSL will attempt to use URIs from namespaces that are found
        in the XML document.  If an Element lacks a namespace then this NS is used. -->
    <xsl:variable name="defaultNS">http://ingest.mannlib.cornell.edu/generalizedXMLtoRDF/0.1/</xsl:variable>
    
    <!-- This is a regular expression used to test then QNames of nodes.  Any node 
         with a QName that matches this regex will have position dataproperties on each of 
         their children nodes -->
    <xsl:param name="positionParentRegex" select="''"/>    
    
    <xsl:template match="/">
      <rdf:RDF>                               
        <xsl:apply-templates mode="rdfDescription" select="*"/>
      </rdf:RDF>
    </xsl:template>
    
    <!-- create description elements -->
    <xsl:template mode="rdfDescription" match="*">        
      <rdf:Description>                             
        <xsl:element name="rdf:type">
          <xsl:attribute name="rdf:resource" 
                         select="concat((if(namespace-uri())then namespace-uri() else $defaultNS),local-name())"/>
        </xsl:element>
        <xsl:apply-templates select="*"/>
      </rdf:Description>
    </xsl:template>   
    
    <!-- Recursive tempate to turn Elements with Attributes and/or Text into bnodes -->
    <xsl:template match="*">
        <xsl:element 
            name="{ local-name() }" 
            namespace="{ (if(namespace-uri() and string-length(namespace-uri())>0 )then namespace-uri() else $defaultNS) }" >
            <rdf:Description>
            <xsl:apply-templates select="*|@*"/>
                
            <xsl:if test="not(*) and string-length(text())>0">
                    <vitro:value><xsl:copy-of select="text()"/></vitro:value>
            </xsl:if>
                                                            
            <xsl:if test="matches(../name(),$positionParentRegex) or 
                          matches(concat(../namespace-uri(),../local-name()),$positionParentRegex)">                            
                <vitro:position><xsl:value-of select="position()"/></vitro:position>
            </xsl:if>

            </rdf:Description>
        </xsl:element>
    </xsl:template>
    
    <!-- Match all leaf Elements that have Attributes and/or Text and turn them into bnodes -->
    <xsl:template  match="*[not(*) and @* and string-length(text())>0]">
        <xsl:element
            name="{ local-name() }" 
            namespace="{ (if(namespace-uri() and string-length(namespace-uri())>0 )then namespace-uri() else $defaultNS) }" >                                                   
            <rdf:Description>
                <xsl:apply-templates  select="@*"/>
                <vitro:value>
                    <xsl:value-of select="."/>
                </vitro:value>
            </rdf:Description>
        </xsl:element>
    </xsl:template>
    
    <!-- Match all attributes and turn them into data properties. -->
    <xsl:template match="@*">       
        <xsl:element 
            name="{ local-name() }" 
            namespace="{ (if(namespace-uri() and string-length(namespace-uri())>0 )then namespace-uri() else $defaultNS) }" >                                   
            <xsl:value-of select="."/>
        </xsl:element>
    </xsl:template>
   
   <!-- Match all Elements with only a text node and turn them into data properties. -->
    <xsl:template match="*[not(*) and not(@*) and string-length(text())>0]">       
        <xsl:element 
            name="{ local-name() }" 
            namespace="{ (if(namespace-uri() and string-length(namespace-uri())>0 )then namespace-uri() else $defaultNS) }" >             
            <xsl:value-of select="."/>
        </xsl:element>
    </xsl:template>
    
</xsl:stylesheet>
