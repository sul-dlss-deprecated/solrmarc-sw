#! /bin/bash
# index_incr_sirsi.sh
# Remove deleted records from index and update index per marc records given
#  Naomi Dushay 2010-01-08

# get filename date
if [ $1 ] ; then
  COUNTS_FNAME=$1"_dates_counts"
  DEL_KEYS_FNAME=$1"_ckeys_delete.del"
  RECORDS_FNAME=$1"_uni_increment.marc"
  LOG_SUBDIR=$1
else
  TODAY=`eval date +%y%m%d`
  COUNTS_FNAME=$TODAY"_dates_counts"
  DEL_KEYS_FNAME=$TODAY"_ckeys_delete.del"
  RECORDS_FNAME=$TODAY"_uni_increment.marc"
  LOG_SUBDIR=$TODAY
fi

JAVA_HOME=/usr/lib/jvm/java

BLACKLIGHT_HOMEDIR=/home/blacklight
SOLRMARC_JAR=$BLACKLIGHT_HOMEDIR/solrmarc/dist/swSolrMarc.jar
RAW_DATA_DIR=$BLACKLIGHT_HOMEDIR/data/unicorn/latest/updates
REC_FNAME=$RAW_DATA_DIR/$RECORDS_FNAME

DEL_ARG="-Dmarc.ids_to_delete="$RAW_DATA_DIR/$DEL_KEYS_FNAME

# create log directory
LOG_PARENT_DIR=$RAW_DATA_DIR/logs
LOG_DIR=$LOG_PARENT_DIR/$LOG_SUBDIR
mkdir $LOG_DIR

# index the files
nohup java -Xmx4g -Xms4g $DEL_ARG -jar $SOLRMARC_JAR $REC_FNAME &>$LOG_DIR/$RECORDS_FNAME".txt"
#nohup java -Xmx16g -Xms16g $DEL_ARG -Dsolr.optimize_at_end="true" -jar $SOLRMARC_JAR $REC_FNAME &>$LOG_DIR/$RECORDS_FNAME".txt"

exit 0