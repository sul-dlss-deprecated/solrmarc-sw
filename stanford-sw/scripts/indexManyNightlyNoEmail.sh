#! /bin/bash
/home/blacklight/solrmarc-sw/stanford-sw/scripts/indexNightlyNoEmailNoCrezNoCommit.sh 140611
/home/blacklight/solrmarc-sw/stanford-sw/scripts/indexNightlyNoEmailNoCrezNoCommit.sh 140612
/home/blacklight/solrmarc-sw/stanford-sw/scripts/indexNightlyNoEmailNoCrezNoCommit.sh 140613
/home/blacklight/solrmarc-sw/stanford-sw/scripts/indexNightlyNoEmailNoCrezNoCommit.sh 140614
# last one should do commit before crez processing
/home/blacklight/solrmarc-sw/stanford-sw/scripts/indexNightlyNoEmailNoCrez.sh 140615

# include latest course reserves data IFF it's not done with above scripts
LANG="en_US.UTF-8"
export LANG
(source /usr/local/rvm/scripts/rvm && cd /home/blacklight/crez-sw-ingest && ./bin/index_latest_no_email.sh -s prod )

echo "!!! RUN GDOR, SEARCHWORKS TESTS before putting index into production !!!"
echo "!!! CHGRP before putting index into production !!!"
