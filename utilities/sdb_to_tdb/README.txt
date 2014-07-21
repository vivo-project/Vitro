A tool to convert triples from SDB to TDB.

Unlike the RDF-migration tool, this tool is not tied to the VIVO
properties files. Instead, you must provide:
* The URL for the SDB, including username and password parameters
* The directory path for the TDB. 
     The directory must exist. 
     The directory must be empty, unless the "force" optiopn is used.

Examples:

   java -jar sdb2tdb.jar \ 
   		jdbc:mysql://localhost/vitrodb?user=vivoUser&password=vivoPass \ 
   		/usr/local/my/tdb
   		
   java -Xms512m -Xmx4096m -jar .work/sdb2tdb.jar \
        'jdbc:mysql://localhost/weill17?user=vivoUser&password=vivoPass' \
         /Users/jeb228/Testing/instances/weill-develop/vivo_home/contentTdb \
         force   