notes about jar files in lib dir:

commons-pool-1.2.jar - for connection pools and dbcp
commons-collections-3.1.jar - for commons-pool-1.2.jar
These previous two jars were not in the lib directory because
we had been putting them in tomcat/common/lib for the
server.xml database connection.

cos.jar 
com.oreilly.servlet.* classes. Many different utilities for
servlets from http://servlets.com/cos/
There is a set of file upload tools, an email utility, a cache,
a base64 encode/decode, a parameter parser.

updated by bdc who need ServletUtils.
