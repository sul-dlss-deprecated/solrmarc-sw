#! /bin/bash
/home/blacklight/solrmarc-sw/stanford-sw/scripts/indexNightlyNoEmailNoCrezNoCommit.sh 140308
/home/blacklight/solrmarc-sw/stanford-sw/scripts/indexNightlyNoEmailNoCrezNoCommit.sh 140309
/home/blacklight/solrmarc-sw/stanford-sw/scripts/indexNightlyNoEmailNoCrezNoCommit.sh 140310
/home/blacklight/solrmarc-sw/stanford-sw/scripts/indexNightlyNoEmailNoCrezNoCommit.sh 140311
/home/blacklight/solrmarc-sw/stanford-sw/scripts/indexNightlyNoEmailNoCrezNoCommit.sh 140312
/home/blacklight/solrmarc-sw/stanford-sw/scripts/indexNightlyNoEmailNoCrezNoCommit.sh 140313
/home/blacklight/solrmarc-sw/stanford-sw/scripts/indexNightlyNoEmailNoCrezNoCommit.sh 140314
/home/blacklight/solrmarc-sw/stanford-sw/scripts/indexNightlyNoEmailNoCrezNoCommit.sh 140315
/home/blacklight/solrmarc-sw/stanford-sw/scripts/indexNightlyNoEmailNoCrezNoCommit.sh 140316
/home/blacklight/solrmarc-sw/stanford-sw/scripts/indexNightlyNoEmailNoCrezNoCommit.sh 140317
/home/blacklight/solrmarc-sw/stanford-sw/scripts/indexNightlyNoEmailNoCrezNoCommit.sh 140318
/home/blacklight/solrmarc-sw/stanford-sw/scripts/indexNightlyNoEmailNoCrezNoCommit.sh 140319
/home/blacklight/solrmarc-sw/stanford-sw/scripts/indexNightlyNoEmailNoCrezNoCommit.sh 140320
# last one should do commit before crez processing
/home/blacklight/solrmarc-sw/stanford-sw/scripts/indexNightlyNoEmailNoCrez.sh 140321

# include latest course reserves data IFF it's not done with above scripts
JRUBY_OPTS="--1.9"
export JRUBY_OPTS
LANG="en_US.UTF-8"
export LANG

(source /usr/local/rvm/scripts/rvm && cd /home/blacklight/crez-sw-ingest && source ./.rvmrc && ./bin/index_latest_no_email.sh -s prod )

echo "!!! RUN GDOR, SEARCHWORKS TESTS before putting index into production !!!"
echo "!!! CHGRP before putting index into production !!!"
