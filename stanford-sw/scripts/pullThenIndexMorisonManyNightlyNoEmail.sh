#! /bin/bash

/home/blacklight/solrmarc-sw/stanford-sw/scripts/pullThenIndexMorisonNightlyNoEmailNoCrezNoCommit.sh 150610
/home/blacklight/solrmarc-sw/stanford-sw/scripts/pullThenIndexMorisonNightlyNoEmailNoCrezNoCommit.sh 150611
/home/blacklight/solrmarc-sw/stanford-sw/scripts/pullThenIndexMorisonNightlyNoEmailNoCrezNoCommit.sh 150612
/home/blacklight/solrmarc-sw/stanford-sw/scripts/pullThenIndexMorisonNightlyNoEmailNoCrezNoCommit.sh 150613
/home/blacklight/solrmarc-sw/stanford-sw/scripts/pullThenIndexMorisonNightlyNoEmailNoCrezNoCommit.sh 150614
/home/blacklight/solrmarc-sw/stanford-sw/scripts/pullThenIndexMorisonNightlyNoEmailNoCrezNoCommit.sh 150615
/home/blacklight/solrmarc-sw/stanford-sw/scripts/pullThenIndexMorisonNightlyNoEmailNoCrezNoCommit.sh 150616
/home/blacklight/solrmarc-sw/stanford-sw/scripts/pullThenIndexMorisonNightlyNoEmailNoCrezNoCommit.sh 150617
/home/blacklight/solrmarc-sw/stanford-sw/scripts/pullThenIndexMorisonNightlyNoEmailNoCrezNoCommit.sh 150618
/home/blacklight/solrmarc-sw/stanford-sw/scripts/pullThenIndexMorisonNightlyNoEmailNoCrezNoCommit.sh 150619
/home/blacklight/solrmarc-sw/stanford-sw/scripts/pullThenIndexMorisonNightlyNoEmailNoCrezNoCommit.sh 150620
/home/blacklight/solrmarc-sw/stanford-sw/scripts/pullThenIndexMorisonNightlyNoEmailNoCrezNoCommit.sh 150621
/home/blacklight/solrmarc-sw/stanford-sw/scripts/pullThenIndexMorisonNightlyNoEmailNoCrezNoCommit.sh 150622
/home/blacklight/solrmarc-sw/stanford-sw/scripts/pullThenIndexMorisonNightlyNoEmailNoCrezNoCommit.sh 150623
/home/blacklight/solrmarc-sw/stanford-sw/scripts/pullThenIndexMorisonNightlyNoEmailNoCrezNoCommit.sh 150624
# last one should do commit before crez processing
/home/blacklight/solrmarc-sw/stanford-sw/scripts/pullThenIndexMorisonNightlyNoEmailNoCrez.sh 150625

# include latest course reserves data IFF it's not done with above scripts
LANG="en_US.UTF-8"
export LANG

(source /usr/local/rvm/scripts/rvm && cd /home/blacklight/crez-sw-ingest && ./bin/index_latest_no_email.sh -s prod )

echo "!!! RUN GDOR, SEARCHWORKS TESTS before putting index into production !!!"
echo "!!! CHGRP before putting index into production !!!"

