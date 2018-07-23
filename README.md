# solrmarc-sw

[<img src="https://travis-ci.org/sul-dlss/solrmarc-sw.svg?branch=master"
alt="Build Status" />](https://travis-ci.org/sul-dlss/solrmarc-sw) [<img
src="https://scan.coverity.com/projects/2194/badge.svg" alt="Coverity Scan
Build Status" />](https://scan.coverity.com/projects/2194)

This is a FORK of the SolrMarc** project;  it was streamlined for the single
purpose of indexing Stanford Marc data for the SearchWorks application (a
Blacklight application).

## Usage

1. Clone the project
```
git clone git://github.com/sul-dlss/solrmarc-sw.git
```

2. Create a Solr instance
  * This is known to work with solr version 4.4.0, Java7 (openjdk64-1.7.0.181), and ant 1.9.
  * See test/solr for a vanilla example of config files, solr war file, solr.xml, etc.
  * See stanford-sw/solr  for config files, solr war file, solr.xml, etc.
  * See Solr project at http://lucene.apache.org/solr

3. Install java and ant
  * Use homebrew to [install jenv](http://www.jenv.be/), java7 and ant 1.9.
  * `brew install jenv`
  * `brew cask install caskroom/versions/zulu7`
  * `brew install ant@1.9`

4. Run the dist ant task in the correct java environment
  * `jenv local oracle64-1.7.0.79`
  * `jenv exec ant dist_site`

5. To run the test suite
  * `jenv local oracle64-1.7.0.79`
  * `jenv exec ant test_site`

6. You can see all the ant targets with
  * `ant -p`

7. Create a script to run your MARC data through the solrmarc instance to write to a Solr index
  * See examples in stanford-sw/scripts.
    * You will have to update the values for `HOMEDIR`, `RAW_DATA_DIR`, and `JAVA_HOME`.
  * Run your script
  * Query the Solr index to see your data


## Contributing

1.  Fork it
2.  Create your feature branch (`git checkout -b my-new-feature`)
3.  Commit your changes (`git commit -am 'Added some feature'`)
4.  Push to the branch (`git push origin my-new-feature`)
5.  Create new Pull Request


## **SolrMarc

SolrMarc is a utility that reads in MaRC records from a file, extracts
information from various fields as specified in an indexing configuration
script, and adds that information to a specified Apache Solr index.

SolrMarc provides a rich set of techniques for mapping the tags, fields, and
subfields contained in the MARC record to the fields you wish to include in
your Solr documents, but it also allows the creation of custom indexing
functions if  you cannot achieve what you require using the predefined mapping
techniques.

Aside from inline comments, the best place to find documentation is on the
non-forked SolrMarc project pages at

http://code.google.com/p/solrmarc/w/list
