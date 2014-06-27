Vitro depends on solr.  This directory provides a basic installation of solr to be used along side vitro.

solr-4.7.2.war is the exact war as provided by Solr.

additions-to-solr-war contains files that will be are merged into the war by the build script:
    log4j.properties
    jcl-over-slf4j-1.6.1.jar
    jul-to-slf4j-1.6.1.jar
    log4j-1.2.16.jar
    slf4j-api-1.6.1.jar
    slf4j-log4j12-1.6.1.jar
       The Solr WAR file does not include logging JARs, assuming that they will be provided 
       by the supporting container: Tomcat or equivalent. But we want the WAR to be 
       self-contained, so we provide the JARs. We also want to reduce the amount of logging 
       messages, so we provide a log4j.properties.
    jts-1.13.jar
       Don't know.
       
template.context.xml is the basis for a Tomcat context fragment. The build script will
   modify this so it provides a JNDI reference to the Solr home directory, and disables 
   persistence across Tomcat restarts.
   
   Non-Tomcat containers will likely ignore this.
   
homeDirectoryTemplate is a modified version of the example home directory provided by Solr.
   Don't know what those modifications are, but they certainly include our own version of
   conf/schema.xml.
       
