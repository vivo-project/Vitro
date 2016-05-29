JAVA_HOME=/usr/local/java/jdk1.5.0_06
TOMCAT_HOME=/usr/local/tomcat
CATALINA_HOME=/usr/local/tomcat

#these are for options that are only set at start time
# useful for jmx or remote debugging.
START_OPTS=""

# the gc log and the jvm options are saved in the logs directory
DATESTR=`date +%Y%m%d.%H%M`
GCFILE=$CATALINA_HOME/logs/gc$DATESTR.txt
OPT_FILE=$CATALINA_HOME/logs/opts$DATESTR.txt

# This is an example of the setenv.sh file from
# cugir-tng.mannlib.cornell.edu

# To use this file copy it to /usr/local/tomcat/bin/setenv.sh
# When catalina.sh is called this file will be used to
# set the environmental variables that are 
# in effect when starting the JVM for tomcat.

# java memory tools:
# jconsole is useful to watch the survivor, Edan, tenured and
# perm spaces.  gcviewer is useful for statistics.

#An acceptable group of settings, use this as a starting point
# CATALINA_OPTS=" -Xms1024m -Xmx1024m -XX:MaxPermSize=128m \
# -XX:+UseParallelGC \
# -Dfile.encoding=UTF-8 \
# -Xloggc:$GCFILE -XX:+PrintGCDetails -XX:+PrintGCTimeStamps "

# attempting a low GCTimeRatio, 
# GCTimeRatio indicates how much time to spend in gc vs. application
# app time / gc time = 1 / (1+ GCTimeRatio)
# GCTimeRatio of 11 is aprox %8 of the time in GC
#
# Result: best so far. After about 12 hours the heap is 
# hovering around 100mb.  This is the first group of settings
# that seem like they will be stable for a long period of time.
# throughput is 98.79%
# This is a log for these settings: /usr/local/tomcat/logs/gc20060809.1638.txt
CATALINA_OPTS=" -Xms1024m -Xmx1024m -XX:MaxPermSize=128m \
-XX:+UseParallelGC -XX:GCTimeRatio=11 -XX:+UseAdaptiveSizePolicy \
-Dfile.encoding=UTF-8 \
-Xloggc:$GCFILE -XX:+PrintGCDetails -XX:+PrintGCTimeStamps "

# bdc34 2006-08-08
# Trying adaptiveSizePolicy with parallel GC.
# Result: This did an out of memory when a crawler came along
# but even before that it looked bad.
# CATALINA_OPTS="-Xms1024m -Xmx1024m -XX:MaxPermSize=128m \
# -XX:NewSize=512m \
# -XX:+UseParallelGC -XX:+UseAdaptiveSizePolicy \
# -Dfile.encoding=UTF-8 \
# -XX:+PrintGCDetails -XX:+PrintGCTimeStamps \
# -Xloggc:$GCFILE "

# bdc34 2006-08-08
# Trying the concurrent gc
# Result: after 10min it seems odd.  The survivor space doesn't get
# used at all.  It is always showing up in jconsole as zero mb. So
# objects are going directly from the edan space to the tenured space?
#
# Result: sort of rapid growth of heap with odd paterns of collection.
# doesn't seem useful
# CATALINA_OPTS="-Xms512m -Xmx512m -XX:MaxPermSize=256m \
# -XX:+UseConcMarkSweepGC \
# -XX:+UseAdaptiveSizePolicy \
# -XX:-TraceClassUnloading \
# -Dfile.encoding=UTF-8 \
# -XX:+PrintGCDetails -XX:+PrintGCTimeStamps \
# -Xloggc:/usr/local/tomcat/logs/gc.txt "

# bdc34 2006-08-07
# Attempting to increase the PermGen Size. Notice
# that PermGen size is in addition to the heap
# size specified in -Xmx.
#
# Also removing GCTimeRatio to allow for
# a lot of garbage collecting.  I don't want
# any bound on GC, time or throughput. I don't
# know if not specifying a value will achieve this.
#
# Logging gc to /usr/local/tomcat/logs/gc.txt
# so I can look at it with http://www.tagtraum.com/gcviewer.html
#
# Setting max heap to initial heap to avoid growing the
# heap and associated gc cycles.
#
# Result: I ran this for two days and it seemed to work well.
# It would do minor collections for about 20h. The heap would
# get to around 1G and then the jvm would do 
# a full colleciton that would drop the heap down to 50m.
# It seems that having the heap at 1G only buys time and 
# that the application could live in 128-256mb.
# gcviewer was reporting 99.0% collection rates. 
# It seems that these settings would work but might need to
# be restarted about once a week.
#
# Increasing the MaxPermSize seems to be important.
# The permGen includes things like classes and code. This is
# a problem since JSPs are classes and code so reloading a JSP
# adds to the permGen.  The documentation weakly indicates that
# the permGen gets collected in full collections.  I have not
# seen evidence that this it does when watching jconsole.
# Sun's JVM seperates the heap and classes but IBM and BEA do not.
#
# CATALINA_OPTS="-Xms1024m -Xmx1024m -XX:MaxPermSize=256m \
# -XX:+UseParallelGC \
# -Dfile.encoding=UTF-8 \
# -XX:+PrintGCDetails -XX:+PrintGCTimeStamps \
# -Xloggc:/usr/local/tomcat/logs/gc.txt "

#bdc 2005-10-18
# CATALINA_OPTS="-server -Xms512m -Xmx1024m \
# -XX:+UseParallelGC \
# -XX:ParallelGCThreads=8 -XX:-PrintTenuringDistribution \
# -XX:MaxPermSize=256 \
# -XX:NewRatio=5 -XX:GCTimeRatio=19 -XX:SurvivorRatio=32 \
# -Dfile.encoding=UTF-8 \
# -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -Xloggc:/usr/local/tomcat/log/gc.txt \
# -verbose:gc "

#These are the java command args needed to allow remote jmx
JMXREMOTE=" -Dcom.sun.management.jmxremote.port=8022 "

#we only what jmxremote when we start, not on stop since the 
#port will be in use. This also could be done with jpda 
START_OPTS="$JMXREMOTE"

# we want to allow access to files created by tomcat by the mannit group
umask 002

# this makes a link from the current gc log to a place were we can get it over http
# those '&& true' are just to force success so we don't exit 
rm -f /usr/local/apache/htdocs/private/tmp/gc.txt && true
ln -f -s $GCFILE /usr/local/apache/htdocs/private/tmp/gc.txt && true

#We want a record of the JVM options to compare with the gc log.
echo "CATALINA_OPTS: $CATALINA_OPTS" > $OPT_FILE
echo "START_OPTS: $START_OPTS" >> $OPT_FILE

# We need to export any variables that are to be used outside of script:
export JAVA_HOME CATALINA_OPTS TOMCAT_HOME CATALINA_HOME
export START_OPTS

# displays a note about what parameters will be used:
#echo "Using CATALINA Opts from tomcat/bin/setenv.sh: "
#echo "    $CATALINA_OPTS"
