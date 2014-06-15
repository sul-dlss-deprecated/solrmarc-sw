#! /bin/bash
# indexNightlyNoEmailNoCrezNoCommit.sh
# defaults to today's date; can take a date arg in the form yymmdd
# Remove deleted records (per file of ids) from index and update index (with marc records in file)
#
# updated for Naomi's FORK of solrmarc 2011-01-23
# Naomi Dushay 2010-01-08

# get filename date, either from command line or default to today's date
if [ $1 ] ; then
  COUNTS_FNAME=$1"_dates_counts"
  DEL_KEYS_FNAME=$1"_ckeys_delete.del"
  RECORDS_FNAME=$1"_uni_increment.marc"
else
  TODAY=`eval date +%y%m%d`
  COUNTS_FNAME=$TODAY"_dates_counts"
  DEL_KEYS_FNAME=$TODAY"_ckeys_delete.del"
  RECORDS_FNAME=$TODAY"_uni_increment.marc"
fi

RAW_DATA_DIR=/data/sirsi/latest/updates

REC_FNAME=$RAW_DATA_DIR/$RECORDS_FNAME
DEL_ARG="-Dmarc.ids_to_delete="$RAW_DATA_DIR/$DEL_KEYS_FNAME

JAVA_HOME=/usr/lib/jvm/java

# set up the classpath
HOMEDIR=/home/blacklight
SOLRMARC_BASEDIR=$HOMEDIR/solrmarc-sw
DIST_DIR=$SOLRMARC_BASEDIR/dist
SITE_JAR=$DIST_DIR/StanfordSearchWorksSolrMarc.jar
CP=$SITE_JAR:$DIST_DIR:$DIST_DIR/lib

# create log directory
LOG_DIR=$RAW_DATA_DIR/logs
mkdir -p $LOG_DIR
LOG_FILE=$LOG_DIR/$RECORDS_FNAME".txt"

# index the files
nohup java -Xmx1g -Xms256m $DEL_ARG -cp $CP -jar $SITE_JAR $REC_FNAME &>$LOG_FILE
#nohup java -Xmx1g -Xms256m $DEL_ARG -Dsolr.commit_at_end="true" -cp $CP -jar $SITE_JAR $REC_FNAME &>$LOG_FILE
#mail -s 'pullThenIndexSirsiIncr.sh output' searchworks-reports@lists.stanford.edu, datacontrol@stanford.edu < $LOG_FILE
# email the solr log messages
#$SOLRMARC_BASEDIR/stanford-sw/scripts/grep_and_email_tomcat_log.sh

# include latest course reserves data
#LANG="en_US.UTF-8"
#export LANG
#(source /usr/local/rvm/scripts/rvm && cd /home/blacklight/crez-sw-ingest && ./bin/pull_and_index_latest -s prod )

exit 0
